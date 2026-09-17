package com.caseplatform.integration.enterprisearchitect.parser;

import com.caseplatform.integration.enterprisearchitect.model.UMLImportAttribute;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportClass;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportMethod;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportModel;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportRelation;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportValidationResult;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.model.VisibilidadUML;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementación robusta del analizador XMI para archivos exportados por Enterprise Architect
 * y herramientas CASE estándar basadas en OMG UML / XMI 2.1 y XMI 1.x.
 */
@Slf4j
@Component
public class XMIParserImpl implements XMIParser {

    private static final Pattern STYLE_LEFT_PATTERN = Pattern.compile("left=(-?\\d+)");
    private static final Pattern STYLE_TOP_PATTERN = Pattern.compile("top=(-?\\d+)");

    @Override
    public UMLImportModel parse(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        // Protección contra XXE (XML External Entity Injection)
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (Exception e) {
            log.debug("Aviso al configurar características de seguridad XML: {}", e.getMessage());
        }

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        doc.getDocumentElement().normalize();

        return extractModel(doc);
    }

    @Override
    public UMLImportModel parse(String xmlContent) throws Exception {
        if (xmlContent == null || xmlContent.isBlank()) {
            throw new IllegalArgumentException("El contenido XML de entrada no puede estar vacío");
        }
        try (InputStream is = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
            return parse(is);
        }
    }

    @Override
    public UMLImportValidationResult validate(UMLImportModel model) {
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        if (model == null) {
            errores.add("El modelo analizado es nulo");
            return UMLImportValidationResult.builder()
                    .valido(false)
                    .errores(errores)
                    .build();
        }

        if (model.getClases() == null || model.getClases().isEmpty()) {
            errores.add("El archivo XMI no contiene ninguna clase UML interpretable");
        }

        Set<String> nombresClases = new HashSet<>();
        Map<String, UMLImportClass> idToClassMap = new HashMap<>();

        if (model.getClases() != null) {
            for (UMLImportClass c : model.getClases()) {
                if (c.getNombre() == null || c.getNombre().trim().isEmpty()) {
                    errores.add("Se detectó una clase sin nombre (ID: " + c.getXmiId() + ")");
                } else {
                    String nombreNorm = c.getNombre().trim().toLowerCase();
                    if (nombresClases.contains(nombreNorm)) {
                        advertencias.add("Nombre de clase duplicado detectado: '" + c.getNombre() + "'. Se unificará o renombrará.");
                    }
                    nombresClases.add(nombreNorm);
                }
                if (c.getXmiId() != null) {
                    idToClassMap.put(c.getXmiId(), c);
                }
                if (c.getNombre() != null) {
                    idToClassMap.put(c.getNombre().trim().toLowerCase(), c);
                }
            }
        }

        // Validar relaciones
        int totalRelacionesValidas = 0;
        if (model.getRelaciones() != null) {
            for (UMLImportRelation rel : model.getRelaciones()) {
                boolean origenOk = (rel.getClaseOrigenId() != null && idToClassMap.containsKey(rel.getClaseOrigenId())) ||
                                   (rel.getClaseOrigenNombre() != null && idToClassMap.containsKey(rel.getClaseOrigenNombre().toLowerCase()));
                boolean destinoOk = (rel.getClaseDestinoId() != null && idToClassMap.containsKey(rel.getClaseDestinoId())) ||
                                    (rel.getClaseDestinoNombre() != null && idToClassMap.containsKey(rel.getClaseDestinoNombre().toLowerCase()));

                if (!origenOk || !destinoOk) {
                    advertencias.add("Relación '" + (rel.getNombre() != null ? rel.getNombre() : rel.getXmiId()) +
                            "' descartada por apuntar a clases no encontradas (Origen: " +
                            rel.getClaseOrigenNombre() + ", Destino: " + rel.getClaseDestinoNombre() + ")");
                } else {
                    totalRelacionesValidas++;
                }
            }
        }

        int totalAtributos = 0;
        int totalMetodos = 0;
        if (model.getClases() != null) {
            for (UMLImportClass c : model.getClases()) {
                if (c.getAtributos() != null) totalAtributos += c.getAtributos().size();
                if (c.getMetodos() != null) totalMetodos += c.getMetodos().size();
            }
        }

        boolean esValido = errores.isEmpty();

        return UMLImportValidationResult.builder()
                .valido(esValido)
                .errores(errores)
                .advertencias(advertencias)
                .totalClases(model.getClases() != null ? model.getClases().size() : 0)
                .totalAtributos(totalAtributos)
                .totalMetodos(totalMetodos)
                .totalRelaciones(totalRelacionesValidas)
                .nombreModelo(model.getNombre())
                .xmiVersion(model.getXmiVersion())
                .exportador(model.getExporter())
                .build();
    }

    private UMLImportModel extractModel(Document doc) {
        UMLImportModel result = new UMLImportModel();
        Element root = doc.getDocumentElement();

        // Metadatos de versión y exportador
        result.setXmiVersion(getAttributeValue(root, "xmi:version", "version"));
        extractDocumentation(root, result);

        Map<String, UMLImportClass> classById = new HashMap<>();
        Map<String, UMLImportClass> classByName = new HashMap<>();
        List<UMLImportClass> classes = new ArrayList<>();
        List<UMLImportRelation> relations = new ArrayList<>();

        // 1. Extraer posiciones del diagrama en extensiones Enterprise Architect
        Map<String, double[]> diagramPositions = extractDiagramPositions(root);

        // 2. Extraer Clases (Soporta XMI 2.1 <packagedElement xmi:type="uml:Class"> y XMI 1.x <UML:Class>)
        NodeList classNodes = doc.getElementsByTagName("*");
        for (int i = 0; i < classNodes.getLength(); i++) {
            Node node = classNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            Element elem = (Element) node;

            if (isClassElement(elem)) {
                UMLImportClass clazz = parseClassElement(elem);
                if (clazz != null && clazz.getNombre() != null && !clazz.getNombre().isBlank()) {
                    // Aplicar coordenadas si existen en extensiones de EA
                    if (diagramPositions.containsKey(clazz.getXmiId())) {
                        double[] pos = diagramPositions.get(clazz.getXmiId());
                        clazz.setPosicionX(pos[0]);
                        clazz.setPosicionY(pos[1]);
                    }

                    classes.add(clazz);
                    if (clazz.getXmiId() != null) {
                        classById.put(clazz.getXmiId(), clazz);
                    }
                    classByName.put(clazz.getNombre().trim().toLowerCase(), clazz);
                }
            }
        }

        // Si no tenían posiciones de diagrama, distribuirlas en rejilla
        distributeGridPositions(classes);

        // 3. Extraer Generalizaciones (Herencia) dentro de las clases
        for (UMLImportClass clazz : classes) {
            extractGeneralizations(doc, clazz, classById, classByName, relations);
        }

        // 4. Extraer Asociaciones y Dependencias (XMI 2.1 <packagedElement xmi:type="uml:Association"> o conectores EA)
        extractAssociations(doc, classById, classByName, relations);

        // 5. Extraer conectores desde extensiones de Enterprise Architect si existieran
        extractEAConnectors(root, classById, classByName, relations);

        // 6. Asignar nombre del modelo si se encontró
        String modelName = extractModelName(root);
        result.setNombre(modelName != null && !modelName.isBlank() ? modelName : "Modelo_Importado_EA");
        result.setClases(classes);
        result.setRelaciones(relations);

        return result;
    }

    private void extractDocumentation(Element root, UMLImportModel model) {
        NodeList docNodes = root.getElementsByTagName("xmi:Documentation");
        if (docNodes.getLength() == 0) {
            docNodes = root.getElementsByTagName("Documentation");
        }
        if (docNodes.getLength() > 0) {
            Element docElem = (Element) docNodes.item(0);
            model.setExporter(getAttributeValue(docElem, "exporter"));
            model.setExporterVersion(getAttributeValue(docElem, "exporterVersion"));
        }
        if (model.getExporter() == null) {
            model.setExporter("Enterprise Architect");
        }
    }

    private String extractModelName(Element root) {
        NodeList models = root.getElementsByTagName("uml:Model");
        if (models.getLength() == 0) models = root.getElementsByTagName("Model");
        if (models.getLength() > 0) {
            Element m = (Element) models.item(0);
            String name = getAttributeValue(m, "name");
            if (name != null && !name.isBlank()) return name;
        }
        return "Modelo Importado EA";
    }

    private boolean isClassElement(Element elem) {
        String tagName = elem.getTagName();
        String xmiType = getAttributeValue(elem, "xmi:type", "type");

        if (tagName.equalsIgnoreCase("packagedElement") && "uml:Class".equalsIgnoreCase(xmiType)) {
            return true;
        }
        if (tagName.endsWith(":Class") || tagName.equalsIgnoreCase("Class")) {
            return true;
        }
        return false;
    }

    private UMLImportClass parseClassElement(Element elem) {
        String xmiId = getAttributeValue(elem, "xmi:id", "xmi.id", "id");
        String name = getAttributeValue(elem, "name");
        String visibilityStr = getAttributeValue(elem, "visibility");
        String isAbstractStr = getAttributeValue(elem, "isAbstract");

        VisibilidadUML visibilidad = parseVisibility(visibilityStr);
        boolean isAbstract = Boolean.parseBoolean(isAbstractStr);

        UMLImportClass clazz = UMLImportClass.builder()
                .xmiId(xmiId != null ? xmiId : ("EAID_" + name))
                .nombre(cleanIdentifier(name))
                .visibilidad(visibilidad)
                .isAbstract(isAbstract)
                .atributos(new ArrayList<>())
                .metodos(new ArrayList<>())
                .build();

        // Parsear atributos (<ownedAttribute> o <UML:Attribute>)
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;
            Element childElem = (Element) child;
            String childTag = childElem.getTagName();
            String childType = getAttributeValue(childElem, "xmi:type", "type");

            if (childTag.equalsIgnoreCase("ownedAttribute") ||
                (childTag.equalsIgnoreCase("packagedElement") && "uml:Property".equalsIgnoreCase(childType)) ||
                childTag.endsWith(":Attribute") || childTag.equalsIgnoreCase("Attribute")) {

                // Si es un atributo de asociación sin nombre, ignorarlo aquí (se maneja en relaciones)
                String attrName = getAttributeValue(childElem, "name");
                String assocRef = getAttributeValue(childElem, "association");
                if ((attrName == null || attrName.isBlank()) && assocRef != null) {
                    continue;
                }

                UMLImportAttribute attr = parseAttributeElement(childElem);
                if (attr != null) {
                    clazz.getAtributos().add(attr);
                }
            } else if (childTag.equalsIgnoreCase("ownedOperation") ||
                       childTag.endsWith(":Operation") || childTag.equalsIgnoreCase("Operation")) {
                UMLImportMethod method = parseMethodElement(childElem);
                if (method != null) {
                    clazz.getMetodos().add(method);
                }
            }
        }

        return clazz;
    }

    private UMLImportAttribute parseAttributeElement(Element elem) {
        String attrId = getAttributeValue(elem, "xmi:id", "xmi.id", "id");
        String name = getAttributeValue(elem, "name");
        if (name == null || name.isBlank()) return null;

        String visStr = getAttributeValue(elem, "visibility");
        VisibilidadUML visibilidad = parseVisibility(visStr);

        String tipo = extractDataType(elem);
        String defaultValue = extractDefaultValue(elem);

        return UMLImportAttribute.builder()
                .id(attrId)
                .nombre(cleanIdentifier(name))
                .tipoDato(tipo)
                .visibilidad(visibilidad)
                .valorInicial(defaultValue)
                .build();
    }

    private UMLImportMethod parseMethodElement(Element elem) {
        String methodId = getAttributeValue(elem, "xmi:id", "xmi.id", "id");
        String name = getAttributeValue(elem, "name");
        if (name == null || name.isBlank()) return null;

        String visStr = getAttributeValue(elem, "visibility");
        VisibilidadUML visibilidad = parseVisibility(visStr);

        String returnType = "void";
        StringBuilder paramBuilder = new StringBuilder();

        NodeList paramNodes = elem.getElementsByTagName("*");
        for (int i = 0; i < paramNodes.getLength(); i++) {
            Node pNode = paramNodes.item(i);
            if (pNode.getNodeType() != Node.ELEMENT_NODE) continue;
            Element pElem = (Element) pNode;
            String pTag = pElem.getTagName();

            if (pTag.equalsIgnoreCase("ownedParameter") || pTag.endsWith(":Parameter") || pTag.equalsIgnoreCase("Parameter")) {
                String direction = getAttributeValue(pElem, "direction");
                String pName = getAttributeValue(pElem, "name");
                String pType = extractDataType(pElem);

                if ("return".equalsIgnoreCase(direction) || (pName != null && pName.equalsIgnoreCase("return"))) {
                    returnType = pType != null ? pType : "void";
                } else if (pName != null && !pName.isBlank()) {
                    if (paramBuilder.length() > 0) paramBuilder.append(", ");
                    paramBuilder.append(pName).append(": ").append(pType != null ? pType : "Object");
                }
            }
        }

        return UMLImportMethod.builder()
                .id(methodId)
                .nombre(cleanIdentifier(name))
                .tipoRetorno(returnType)
                .visibilidad(visibilidad)
                .parametros(paramBuilder.length() > 0 ? paramBuilder.toString() : null)
                .build();
    }

    private String extractDataType(Element elem) {
        // 1. Atributo directo 'type'
        String typeAttr = getAttributeValue(elem, "type");
        if (typeAttr != null && !typeAttr.isBlank()) {
            return normalizeTypeName(typeAttr);
        }

        // 2. Nodo hijo <type>
        NodeList typeNodes = elem.getElementsByTagName("type");
        if (typeNodes.getLength() > 0) {
            Element tElem = (Element) typeNodes.item(0);
            String href = getAttributeValue(tElem, "href");
            if (href != null && href.contains("#")) {
                return normalizeTypeName(href.substring(href.indexOf("#") + 1));
            }
            String name = getAttributeValue(tElem, "name");
            if (name != null && !name.isBlank()) {
                return normalizeTypeName(name);
            }
        }

        return "String";
    }

    private String extractDefaultValue(Element elem) {
        NodeList defNodes = elem.getElementsByTagName("defaultValue");
        if (defNodes.getLength() > 0) {
            Element dElem = (Element) defNodes.item(0);
            String val = getAttributeValue(dElem, "value");
            if (val != null && !val.isBlank()) return val;
        }
        return null;
    }

    private void extractGeneralizations(Document doc, UMLImportClass clazz,
                                        Map<String, UMLImportClass> classById,
                                        Map<String, UMLImportClass> classByName,
                                        List<UMLImportRelation> relations) {
        // Buscar generalizaciones donde esta clase sea el elemento hijo (especialización)
        NodeList genNodes = doc.getElementsByTagName("*");
        for (int i = 0; i < genNodes.getLength(); i++) {
            Node n = genNodes.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            Element elem = (Element) n;
            String tag = elem.getTagName();

            if (tag.equalsIgnoreCase("generalization") || tag.endsWith(":Generalization")) {
                // Verificar si pertenece a esta clase
                Node parent = elem.getParentNode();
                if (parent instanceof Element) {
                    Element parentElem = (Element) parent;
                    String parentId = getAttributeValue(parentElem, "xmi:id", "xmi.id", "id");
                    if (parentId != null && parentId.equals(clazz.getXmiId())) {
                        String generalId = getAttributeValue(elem, "general");
                        UMLImportClass superClass = classById.get(generalId);
                        if (superClass == null && generalId != null) {
                            superClass = classByName.get(generalId.toLowerCase());
                        }

                        if (superClass != null && !superClass.getXmiId().equals(clazz.getXmiId())) {
                            relations.add(UMLImportRelation.builder()
                                    .xmiId(getAttributeValue(elem, "xmi:id", "id"))
                                    .nombre("Herencia_" + clazz.getNombre() + "_" + superClass.getNombre())
                                    .tipoRelacion(TipoRelacionUML.HERENCIA)
                                    .claseOrigenId(clazz.getXmiId())
                                    .claseOrigenNombre(clazz.getNombre())
                                    .claseDestinoId(superClass.getXmiId())
                                    .claseDestinoNombre(superClass.getNombre())
                                    .cardinalidadOrigen("1")
                                    .cardinalidadDestino("1")
                                    .descripcion("Especialización/Herencia de " + superClass.getNombre())
                                    .build());
                        }
                    }
                }
            }
        }
    }

    private void extractAssociations(Document doc,
                                    Map<String, UMLImportClass> classById,
                                    Map<String, UMLImportClass> classByName,
                                    List<UMLImportRelation> relations) {
        NodeList assocNodes = doc.getElementsByTagName("*");
        for (int i = 0; i < assocNodes.getLength(); i++) {
            Node n = assocNodes.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            Element elem = (Element) n;
            String tag = elem.getTagName();
            String xmiType = getAttributeValue(elem, "xmi:type", "type");

            if ((tag.equalsIgnoreCase("packagedElement") && "uml:Association".equalsIgnoreCase(xmiType)) ||
                tag.endsWith(":Association") || tag.equalsIgnoreCase("Association")) {

                parseAssociationElement(elem, classById, classByName, relations);
            } else if ((tag.equalsIgnoreCase("packagedElement") && "uml:Dependency".equalsIgnoreCase(xmiType)) ||
                       tag.endsWith(":Dependency") || tag.equalsIgnoreCase("Dependency")) {
                parseDependencyElement(elem, classById, classByName, relations);
            }
        }
    }

    private void parseAssociationElement(Element elem,
                                         Map<String, UMLImportClass> classById,
                                         Map<String, UMLImportClass> classByName,
                                         List<UMLImportRelation> relations) {
        String assocId = getAttributeValue(elem, "xmi:id", "xmi.id", "id");
        String name = getAttributeValue(elem, "name");

        NodeList ownedEnds = elem.getElementsByTagName("ownedEnd");
        if (ownedEnds.getLength() < 2) {
            // Intentar con memberEnd
            return;
        }

        Element end1 = (Element) ownedEnds.item(0);
        Element end2 = (Element) ownedEnds.item(1);

        String type1 = getAttributeValue(end1, "type");
        String type2 = getAttributeValue(end2, "type");

        UMLImportClass class1 = classById.get(type1);
        UMLImportClass class2 = classById.get(type2);

        if (class1 == null || class2 == null) {
            return;
        }

        String card1 = extractMultiplicity(end1);
        String card2 = extractMultiplicity(end2);

        String agg1 = getAttributeValue(end1, "aggregation");
        String agg2 = getAttributeValue(end2, "aggregation");

        TipoRelacionUML tipoRelacion = TipoRelacionUML.ASOCIACION;
        if ("composite".equalsIgnoreCase(agg1) || "composite".equalsIgnoreCase(agg2)) {
            tipoRelacion = TipoRelacionUML.COMPOSICION;
        } else if ("shared".equalsIgnoreCase(agg1) || "shared".equalsIgnoreCase(agg2)) {
            tipoRelacion = TipoRelacionUML.AGREGACION;
        }

        // Evitar duplicados exactos
        boolean exists = relations.stream().anyMatch(r ->
                (r.getClaseOrigenId().equals(class1.getXmiId()) && r.getClaseDestinoId().equals(class2.getXmiId())) ||
                (r.getClaseOrigenId().equals(class2.getXmiId()) && r.getClaseDestinoId().equals(class1.getXmiId()))
        );

        if (!exists) {
            relations.add(UMLImportRelation.builder()
                    .xmiId(assocId)
                    .nombre(name != null && !name.isBlank() ? name : ("Rel_" + class1.getNombre() + "_" + class2.getNombre()))
                    .tipoRelacion(tipoRelacion)
                    .claseOrigenId(class1.getXmiId())
                    .claseOrigenNombre(class1.getNombre())
                    .claseDestinoId(class2.getXmiId())
                    .claseDestinoNombre(class2.getNombre())
                    .cardinalidadOrigen(card1)
                    .cardinalidadDestino(card2)
                    .descripcion("Asociación importada desde Enterprise Architect")
                    .build());
        }
    }

    private void parseDependencyElement(Element elem,
                                        Map<String, UMLImportClass> classById,
                                        Map<String, UMLImportClass> classByName,
                                        List<UMLImportRelation> relations) {
        String depId = getAttributeValue(elem, "xmi:id", "xmi.id", "id");
        String client = getAttributeValue(elem, "client");
        String supplier = getAttributeValue(elem, "supplier");

        UMLImportClass clientClass = classById.get(client);
        UMLImportClass supplierClass = classById.get(supplier);

        if (clientClass != null && supplierClass != null) {
            relations.add(UMLImportRelation.builder()
                    .xmiId(depId)
                    .nombre("Dep_" + clientClass.getNombre() + "_" + supplierClass.getNombre())
                    .tipoRelacion(TipoRelacionUML.DEPENDENCIA)
                    .claseOrigenId(clientClass.getXmiId())
                    .claseOrigenNombre(clientClass.getNombre())
                    .claseDestinoId(supplierClass.getXmiId())
                    .claseDestinoNombre(supplierClass.getNombre())
                    .cardinalidadOrigen("1")
                    .cardinalidadDestino("1")
                    .descripcion("Dependencia UML")
                    .build());
        }
    }

    private void extractEAConnectors(Element root,
                                     Map<String, UMLImportClass> classById,
                                     Map<String, UMLImportClass> classByName,
                                     List<UMLImportRelation> relations) {
        NodeList connectors = root.getElementsByTagName("connector");
        for (int i = 0; i < connectors.getLength(); i++) {
            Element conn = (Element) connectors.item(i);
            String connId = getAttributeValue(conn, "xmi:idref");

            NodeList sources = conn.getElementsByTagName("source");
            NodeList targets = conn.getElementsByTagName("target");
            NodeList props = conn.getElementsByTagName("properties");

            if (sources.getLength() > 0 && targets.getLength() > 0) {
                Element src = (Element) sources.item(0);
                Element tgt = (Element) targets.item(0);

                String srcId = getAttributeValue(src, "xmi:idref");
                String tgtId = getAttributeValue(tgt, "xmi:idref");

                UMLImportClass cSrc = classById.get(srcId);
                UMLImportClass cTgt = classById.get(tgtId);

                if (cSrc != null && cTgt != null) {
                    // Determinar tipo desde ea_type
                    TipoRelacionUML tipo = TipoRelacionUML.ASOCIACION;
                    if (props.getLength() > 0) {
                        Element p = (Element) props.item(0);
                        String eaType = getAttributeValue(p, "ea_type");
                        if ("Generalization".equalsIgnoreCase(eaType)) tipo = TipoRelacionUML.HERENCIA;
                        else if ("Aggregation".equalsIgnoreCase(eaType)) tipo = TipoRelacionUML.AGREGACION;
                        else if ("Composition".equalsIgnoreCase(eaType)) tipo = TipoRelacionUML.COMPOSICION;
                        else if ("Dependency".equalsIgnoreCase(eaType)) tipo = TipoRelacionUML.DEPENDENCIA;
                    }

                    String cardSrc = extractEAMultiplicity(src);
                    String cardTgt = extractEAMultiplicity(tgt);
                    final TipoRelacionUML finalTipo = tipo;

                    boolean alreadyExists = relations.stream().anyMatch(r ->
                            r.getClaseOrigenId().equals(cSrc.getXmiId()) &&
                            r.getClaseDestinoId().equals(cTgt.getXmiId()) &&
                            r.getTipoRelacion() == finalTipo
                    );

                    if (!alreadyExists) {
                        relations.add(UMLImportRelation.builder()
                                .xmiId(connId != null ? connId : ("EA_CONN_" + i))
                                .nombre("Conn_" + cSrc.getNombre() + "_" + cTgt.getNombre())
                                .tipoRelacion(tipo)
                                .claseOrigenId(cSrc.getXmiId())
                                .claseOrigenNombre(cSrc.getNombre())
                                .claseDestinoId(cTgt.getXmiId())
                                .claseDestinoNombre(cTgt.getNombre())
                                .cardinalidadOrigen(cardSrc)
                                .cardinalidadDestino(cardTgt)
                                .descripcion("Conector Enterprise Architect (" + tipo + ")")
                                .build());
                    }
                }
            }
        }
    }

    private String extractEAMultiplicity(Element endElem) {
        NodeList typeNodes = endElem.getElementsByTagName("type");
        if (typeNodes.getLength() > 0) {
            Element t = (Element) typeNodes.item(0);
            String mult = getAttributeValue(t, "multiplicity");
            if (mult != null && !mult.isBlank()) return mult;
        }
        return "1";
    }

    private String extractMultiplicity(Element endElem) {
        String lower = "1";
        String upper = "1";

        NodeList lowers = endElem.getElementsByTagName("lowerValue");
        if (lowers.getLength() > 0) {
            String val = getAttributeValue((Element) lowers.item(0), "value");
            if (val != null) lower = val;
        }

        NodeList uppers = endElem.getElementsByTagName("upperValue");
        if (uppers.getLength() > 0) {
            String val = getAttributeValue((Element) uppers.item(0), "value");
            if (val != null) upper = val;
        }

        if (lower.equals(upper)) return lower;
        return lower + ".." + upper;
    }

    private Map<String, double[]> extractDiagramPositions(Element root) {
        Map<String, double[]> positions = new HashMap<>();
        NodeList diagrams = root.getElementsByTagName("diagram");
        for (int i = 0; i < diagrams.getLength(); i++) {
            Element diag = (Element) diagrams.item(i);
            NodeList elements = diag.getElementsByTagName("element");
            for (int j = 0; j < elements.getLength(); j++) {
                Element el = (Element) elements.item(j);
                String subject = getAttributeValue(el, "subject");
                String style = getAttributeValue(el, "style");

                if (subject != null && style != null) {
                    Matcher mLeft = STYLE_LEFT_PATTERN.matcher(style);
                    Matcher mTop = STYLE_TOP_PATTERN.matcher(style);

                    if (mLeft.find() && mTop.find()) {
                        try {
                            double left = Math.abs(Double.parseDouble(mLeft.group(1)));
                            double top = Math.abs(Double.parseDouble(mTop.group(1)));
                            positions.put(subject, new double[]{Math.max(50.0, left), Math.max(50.0, top)});
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
        return positions;
    }

    private void distributeGridPositions(List<UMLImportClass> classes) {
        int index = 0;
        for (UMLImportClass c : classes) {
            if (c.getPosicionX() == null || c.getPosicionX() == 0.0 ||
                c.getPosicionY() == null || c.getPosicionY() == 0.0) {
                int col = index % 3;
                int row = index / 3;
                c.setPosicionX(80.0 + col * 280.0);
                c.setPosicionY(80.0 + row * 220.0);
                index++;
            }
        }
    }

    private VisibilidadUML parseVisibility(String vis) {
        if (vis == null) return VisibilidadUML.PUBLIC;
        return switch (vis.toLowerCase().trim()) {
            case "private" -> VisibilidadUML.PRIVATE;
            case "protected" -> VisibilidadUML.PROTECTED;
            case "package", "default" -> VisibilidadUML.PACKAGE;
            default -> VisibilidadUML.PUBLIC;
        };
    }

    private String normalizeTypeName(String rawType) {
        if (rawType == null) return "String";
        String t = rawType.replace("EAJava_", "").replace("uml:", "").trim();
        return switch (t.toLowerCase()) {
            case "integer", "int" -> "Integer";
            case "long" -> "Long";
            case "double" -> "Double";
            case "float" -> "Float";
            case "boolean", "bool" -> "Boolean";
            case "date" -> "LocalDate";
            case "datetime", "timestamp" -> "LocalDateTime";
            case "bigdecimal", "decimal" -> "BigDecimal";
            case "void" -> "void";
            default -> t.substring(0, 1).toUpperCase() + t.substring(1);
        };
    }

    private String cleanIdentifier(String raw) {
        if (raw == null) return "Clase";
        return raw.replaceAll("[^a-zA-Z0-9_]", "").trim();
    }

    private String getAttributeValue(Element elem, String... attrNames) {
        for (String name : attrNames) {
            if (elem.hasAttribute(name)) {
                String val = elem.getAttribute(name);
                if (val != null && !val.isBlank()) return val;
            }
        }
        return null;
    }
}
