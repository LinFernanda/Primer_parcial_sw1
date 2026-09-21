package com.caseplatform.integration.enterprisearchitect.exporter;

import com.caseplatform.model.AtributoUML;
import com.caseplatform.model.ClaseUML;
import com.caseplatform.model.MetodoUML;
import com.caseplatform.model.ModeloUML;
import com.caseplatform.model.RelacionUML;
import com.caseplatform.model.TipoRelacionUML;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de exportación XMI 2.1 (OMG UML 2.1 / 2.5) compatible con Enterprise Architect.
 * Incluye extensiones Sparx Systems para preservar la disposición y geometría de los diagramas.
 */
@Slf4j
@Component
public class XMIExporterImpl implements XMIExporter {

    @Override
    public String exportToXMI(ModeloUML modelo) {
        if (modelo == null) {
            throw new IllegalArgumentException("El modelo UML a exportar no puede ser nulo");
        }

        StringBuilder xml = new StringBuilder();
        String modelName = escapeXml(modelo.getNombre() != null ? modelo.getNombre() : "Modelo_UML");
        Long modelId = modelo.getId() != null ? modelo.getId() : 1L;

        List<ClaseUML> clases = modelo.getClases() != null ? modelo.getClases() : new ArrayList<>();
        List<RelacionUML> relaciones = modelo.getRelaciones() != null ? modelo.getRelaciones() : new ArrayList<>();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<xmi:XMI xmi:version=\"2.1\" xmlns:uml=\"http://schema.omg.org/spec/UML/2.1\" xmlns:xmi=\"http://schema.omg.org/spec/XMI/2.1\">\n");
        xml.append("  <xmi:Documentation exporter=\"Enterprise Architect Compatible CASE Platform\" exporterVersion=\"2.1\"/>\n");
        xml.append("  <uml:Model xmi:type=\"uml:Model\" name=\"").append(modelName).append("\" visibility=\"public\">\n");
        xml.append("    <packagedElement xmi:type=\"uml:Package\" xmi:id=\"EAPK_").append(modelId)
           .append("\" name=\"").append(modelName).append("\" visibility=\"public\">\n");

        // 1. Exportar Clases
        for (ClaseUML clase : clases) {
            String cId = "EAID_" + clase.getId();
            String cName = escapeXml(clase.getNombre());
            String cVis = clase.getVisibilidad() != null ? clase.getVisibilidad().name().toLowerCase() : "public";

            xml.append("      <packagedElement xmi:type=\"uml:Class\" xmi:id=\"").append(cId)
               .append("\" name=\"").append(cName)
               .append("\" visibility=\"").append(cVis).append("\">\n");

            // Atributos
            if (clase.getAtributos() != null) {
                for (AtributoUML attr : clase.getAtributos()) {
                    String aId = "EAID_ATTR_" + attr.getId();
                    String aName = escapeXml(attr.getNombre());
                    String aVis = attr.getVisibilidad() != null ? attr.getVisibilidad().name().toLowerCase() : "private";
                    String aType = attr.getTipoDato() != null ? attr.getTipoDato() : "String";

                    xml.append("        <ownedAttribute xmi:type=\"uml:Property\" xmi:id=\"").append(aId)
                       .append("\" name=\"").append(aName)
                       .append("\" visibility=\"").append(aVis).append("\">\n");
                    xml.append("          <type xmi:type=\"uml:PrimitiveType\" href=\"http://schema.omg.org/spec/UML/2.1/uml.xml#")
                       .append(escapeXml(aType)).append("\"/>\n");
                    if (attr.getValorInicial() != null && !attr.getValorInicial().isBlank()) {
                        xml.append("          <defaultValue xmi:type=\"uml:LiteralString\" xmi:id=\"EAID_DEF_").append(attr.getId())
                           .append("\" value=\"").append(escapeXml(attr.getValorInicial())).append("\"/>\n");
                    }
                    xml.append("        </ownedAttribute>\n");
                }
            }

            // Métodos / Operaciones
            if (clase.getMetodos() != null) {
                for (MetodoUML metodo : clase.getMetodos()) {
                    String mId = "EAID_METH_" + metodo.getId();
                    String mName = escapeXml(metodo.getNombre());
                    String mVis = metodo.getVisibilidad() != null ? metodo.getVisibilidad().name().toLowerCase() : "public";
                    String mRet = metodo.getTipoRetorno() != null ? metodo.getTipoRetorno() : "void";

                    xml.append("        <ownedOperation xmi:type=\"uml:Operation\" xmi:id=\"").append(mId)
                       .append("\" name=\"").append(mName)
                       .append("\" visibility=\"").append(mVis).append("\">\n");

                    // Parámetro de Retorno
                    xml.append("          <ownedParameter xmi:id=\"EAID_RET_").append(metodo.getId())
                       .append("\" name=\"return\" direction=\"return\">\n");
                    xml.append("            <type xmi:type=\"uml:PrimitiveType\" href=\"http://schema.omg.org/spec/UML/2.1/uml.xml#")
                       .append(escapeXml(mRet)).append("\"/>\n");
                    xml.append("          </ownedParameter>\n");

                    // Parámetros formales
                    if (metodo.getParametros() != null && !metodo.getParametros().isBlank()) {
                        String[] params = metodo.getParametros().split(",");
                        int pIdx = 1;
                        for (String p : params) {
                            String pClean = p.trim();
                            if (!pClean.isEmpty()) {
                                String pName = pClean;
                                String pType = "Object";
                                if (pClean.contains(":")) {
                                    String[] parts = pClean.split(":");
                                    pName = parts[0].trim();
                                    pType = parts[1].trim();
                                }
                                xml.append("          <ownedParameter xmi:id=\"EAID_PARAM_").append(metodo.getId())
                                   .append("_").append(pIdx)
                                   .append("\" name=\"").append(escapeXml(pName)).append("\">\n");
                                xml.append("            <type xmi:type=\"uml:PrimitiveType\" href=\"http://schema.omg.org/spec/UML/2.1/uml.xml#")
                                   .append(escapeXml(pType)).append("\"/>\n");
                                xml.append("          </ownedParameter>\n");
                                pIdx++;
                            }
                        }
                    }

                    xml.append("        </ownedOperation>\n");
                }
            }

            // Generalizaciones (Herencia donde esta clase es la hija)
            for (RelacionUML rel : relaciones) {
                if (rel != null && rel.getTipoRelacion() == TipoRelacionUML.HERENCIA &&
                    rel.getClaseOrigen() != null && rel.getClaseDestino() != null &&
                    rel.getClaseOrigen().getId() != null && rel.getClaseDestino().getId() != null &&
                    java.util.Objects.equals(rel.getClaseOrigen().getId(), clase.getId())) {
                    String genId = rel.getId() != null ? String.valueOf(rel.getId()) : String.valueOf(System.identityHashCode(rel));
                    xml.append("        <generalization xmi:type=\"uml:Generalization\" xmi:id=\"EAID_GEN_").append(genId)
                       .append("\" general=\"EAID_").append(rel.getClaseDestino().getId()).append("\"/>\n");
                }
            }

            xml.append("      </packagedElement>\n");
        }

        // 2. Exportar Asociaciones, Composiciones, Agregaciones y Dependencias
        for (RelacionUML rel : relaciones) {
            if (rel == null || rel.getClaseOrigen() == null || rel.getClaseDestino() == null ||
                rel.getClaseOrigen().getId() == null || rel.getClaseDestino().getId() == null) continue;

            String rId = "EAID_REL_" + (rel.getId() != null ? rel.getId() : System.identityHashCode(rel));
            String srcId = "EAID_" + rel.getClaseOrigen().getId();
            String dstId = "EAID_" + rel.getClaseDestino().getId();

            if (rel.getTipoRelacion() == TipoRelacionUML.HERENCIA) {
                // Ya se exportó dentro de la clase como <generalization>
                continue;
            }

            if (rel.getTipoRelacion() == TipoRelacionUML.DEPENDENCIA) {
                xml.append("      <packagedElement xmi:type=\"uml:Dependency\" xmi:id=\"").append(rId)
                   .append("\" client=\"").append(srcId)
                   .append("\" supplier=\"").append(dstId)
                   .append("\" visibility=\"public\"/>\n");
            } else {
                // Asociación, Agregación o Composición
                TipoRelacionUML tipo = rel.getTipoRelacion() != null ? rel.getTipoRelacion() : TipoRelacionUML.ASOCIACION;
                String aggregation = switch (tipo) {
                    case COMPOSICION -> "composite";
                    case AGREGACION -> "shared";
                    default -> "none";
                };

                String cardSrc = rel.getCardinalidadOrigen() != null ? rel.getCardinalidadOrigen() : "1";
                String cardDst = rel.getCardinalidadDestino() != null ? rel.getCardinalidadDestino() : "1";

                String endSrcId = "EAID_END_SRC_" + (rel.getId() != null ? rel.getId() : System.identityHashCode(rel));
                String endDstId = "EAID_END_DST_" + (rel.getId() != null ? rel.getId() : System.identityHashCode(rel));

                xml.append("      <packagedElement xmi:type=\"uml:Association\" xmi:id=\"").append(rId)
                   .append("\" name=\"").append(escapeXml(rel.getDescripcion() != null ? rel.getDescripcion() : "rel_" + rId))
                   .append("\">\n");

                xml.append("        <memberEnd xmi:idref=\"").append(endDstId).append("\"/>\n");
                xml.append("        <memberEnd xmi:idref=\"").append(endSrcId).append("\"/>\n");

                // Extremo Origen
                xml.append("        <ownedEnd xmi:type=\"uml:Property\" xmi:id=\"").append(endSrcId)
                   .append("\" type=\"").append(srcId).append("\" association=\"").append(rId).append("\">\n");
                appendMultiplicityXml(xml, cardSrc, "SRC_" + (rel.getId() != null ? rel.getId() : System.identityHashCode(rel)));
                xml.append("        </ownedEnd>\n");

                // Extremo Destino (con agregación)
                xml.append("        <ownedEnd xmi:type=\"uml:Property\" xmi:id=\"").append(endDstId)
                   .append("\" type=\"").append(dstId).append("\" association=\"").append(rId)
                   .append("\" aggregation=\"").append(aggregation).append("\">\n");
                appendMultiplicityXml(xml, cardDst, "DST_" + (rel.getId() != null ? rel.getId() : System.identityHashCode(rel)));
                xml.append("        </ownedEnd>\n");

                xml.append("      </packagedElement>\n");
            }
        }

        xml.append("    </packagedElement>\n");
        xml.append("  </uml:Model>\n");

        // 3. Extensiones Sparx Systems / Enterprise Architect para Diagrama y Geometría
        xml.append("  <xmi:Extension extender=\"Enterprise Architect\" extenderID=\"6.5\">\n");
        xml.append("    <elements>\n");
        for (ClaseUML c : clases) {
            if (c == null) continue;
            String cId = "EAID_" + (c.getId() != null ? c.getId() : System.identityHashCode(c));
            xml.append("      <element xmi:idref=\"").append(cId)
               .append("\" xmi:type=\"uml:Class\" name=\"").append(escapeXml(c.getNombre() != null ? c.getNombre() : "Clase"))
               .append("\" scope=\"").append(c.getVisibilidad() != null ? c.getVisibilidad().name().toLowerCase() : "public")
               .append("\"/>\n");
        }
        xml.append("    </elements>\n");

        xml.append("    <connectors>\n");
        for (RelacionUML r : relaciones) {
            if (r == null || r.getClaseOrigen() == null || r.getClaseDestino() == null ||
                r.getClaseOrigen().getId() == null || r.getClaseDestino().getId() == null) continue;
            TipoRelacionUML rTipo = r.getTipoRelacion() != null ? r.getTipoRelacion() : TipoRelacionUML.ASOCIACION;
            String eaType = switch (rTipo) {
                case HERENCIA -> "Generalization";
                case AGREGACION -> "Aggregation";
                case COMPOSICION -> "Composition";
                case DEPENDENCIA -> "Dependency";
                default -> "Association";
            };
            String rId = "EAID_REL_" + (r.getId() != null ? r.getId() : System.identityHashCode(r));
            xml.append("      <connector xmi:idref=\"").append(rId).append("\">\n");
            xml.append("        <source xmi:idref=\"EAID_").append(r.getClaseOrigen().getId()).append("\">\n");
            xml.append("          <type multiplicity=\"").append(escapeXml(r.getCardinalidadOrigen() != null ? r.getCardinalidadOrigen() : "1")).append("\"/>\n");
            xml.append("        </source>\n");
            xml.append("        <target xmi:idref=\"EAID_").append(r.getClaseDestino().getId()).append("\">\n");
            xml.append("          <type multiplicity=\"").append(escapeXml(r.getCardinalidadDestino() != null ? r.getCardinalidadDestino() : "1")).append("\"/>\n");
            xml.append("        </target>\n");
            xml.append("        <properties ea_type=\"").append(eaType).append("\"/>\n");
            xml.append("      </connector>\n");
        }
        xml.append("    </connectors>\n");

        // Diagrama con posiciones exactas
        xml.append("    <diagrams>\n");
        xml.append("      <diagram xmi:id=\"EAID_DIAG_").append(modelId).append("\">\n");
        xml.append("        <model package=\"EAPK_").append(modelId).append("\"/>\n");
        xml.append("        <properties name=\"").append(modelName).append("\" type=\"Logical\"/>\n");
        xml.append("        <elements>\n");
        int seq = 1;
        for (ClaseUML c : clases) {
            if (c == null) continue;
            long cId = c.getId() != null ? c.getId() : seq;
            int left = c.getPosicionX() != null ? c.getPosicionX().intValue() : (80 + (seq % 3) * 260);
            int top = c.getPosicionY() != null ? c.getPosicionY().intValue() : (80 + (seq / 3) * 200);
            int right = left + 160;
            int bottom = top + 110;
            String style = String.format("DUID=D%d;left=%d;top=%d;right=%d;bottom=%d;", cId, left, top, right, bottom);

            xml.append("          <element subject=\"EAID_").append(cId)
               .append("\" seqno=\"").append(seq)
               .append("\" style=\"").append(style).append("\"/>\n");
            seq++;
        }
        xml.append("        </elements>\n");
        xml.append("      </diagram>\n");
        xml.append("    </diagrams>\n");

        xml.append("  </xmi:Extension>\n");
        xml.append("</xmi:XMI>\n");

        return xml.toString();
    }

    private void appendMultiplicityXml(StringBuilder xml, String card, String suffix) {
        String lower = "1";
        String upper = "1";

        if (card != null && !card.isBlank()) {
            if (card.contains("..")) {
                String[] parts = card.split("\\.\\.");
                lower = parts[0].trim();
                upper = parts.length > 1 ? parts[1].trim() : parts[0].trim();
            } else {
                lower = card.trim();
                upper = card.trim();
            }
        }

        xml.append("          <lowerValue xmi:type=\"uml:LiteralInteger\" xmi:id=\"EAID_LOWER_").append(suffix)
           .append("\" value=\"").append(escapeXml(lower)).append("\"/>\n");
        xml.append("          <upperValue xmi:type=\"uml:LiteralUnlimitedNatural\" xmi:id=\"EAID_UPPER_").append(suffix)
           .append("\" value=\"").append(escapeXml(upper)).append("\"/>\n");
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
