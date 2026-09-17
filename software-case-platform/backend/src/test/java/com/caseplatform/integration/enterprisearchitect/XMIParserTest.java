package com.caseplatform.integration.enterprisearchitect;

import com.caseplatform.integration.enterprisearchitect.model.UMLImportClass;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportModel;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportRelation;
import com.caseplatform.integration.enterprisearchitect.model.UMLImportValidationResult;
import com.caseplatform.integration.enterprisearchitect.parser.XMIParserImpl;
import com.caseplatform.model.TipoRelacionUML;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XMIParserTest {

    private XMIParserImpl parser;

    @BeforeEach
    void setUp() {
        parser = new XMIParserImpl();
    }

    @Test
    @DisplayName("Debe parsear correctamente un archivo XMI 2.1 estándar de Enterprise Architect")
    void testParseValidEAXMI() throws Exception {
        String xmiXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xmi:XMI xmi:version="2.1" xmlns:uml="http://schema.omg.org/spec/UML/2.1" xmlns:xmi="http://schema.omg.org/spec/XMI/2.1">
              <xmi:Documentation exporter="Enterprise Architect" exporterVersion="6.5"/>
              <uml:Model xmi:type="uml:Model" name="ModeloComercial" visibility="public">
                <packagedElement xmi:type="uml:Package" xmi:id="EAPK_1" name="PaquetePrincipal" visibility="public">
                  <packagedElement xmi:type="uml:Class" xmi:id="EAID_C1" name="Cliente" visibility="public">
                    <ownedAttribute xmi:type="uml:Property" xmi:id="EAID_A1" name="id" visibility="private">
                      <type xmi:type="uml:PrimitiveType" href="http://schema.omg.org/spec/UML/2.1/uml.xml#Integer"/>
                    </ownedAttribute>
                    <ownedAttribute xmi:type="uml:Property" xmi:id="EAID_A2" name="nombre" visibility="private">
                      <type xmi:type="uml:PrimitiveType" href="http://schema.omg.org/spec/UML/2.1/uml.xml#String"/>
                    </ownedAttribute>
                    <ownedOperation xmi:type="uml:Operation" xmi:id="EAID_M1" name="calcularDescuento" visibility="public">
                      <ownedParameter name="return" direction="return">
                        <type xmi:type="uml:PrimitiveType" href="http://schema.omg.org/spec/UML/2.1/uml.xml#Double"/>
                      </ownedParameter>
                      <ownedParameter name="porcentaje" type="EAJava_Double"/>
                    </ownedOperation>
                    <generalization xmi:type="uml:Generalization" xmi:id="EAID_G1" general="EAID_C2"/>
                  </packagedElement>
                  <packagedElement xmi:type="uml:Class" xmi:id="EAID_C2" name="Persona" visibility="public">
                    <ownedAttribute xmi:type="uml:Property" xmi:id="EAID_A3" name="cedula" visibility="private">
                      <type xmi:type="uml:PrimitiveType" href="http://schema.omg.org/spec/UML/2.1/uml.xml#String"/>
                    </ownedAttribute>
                  </packagedElement>
                  <packagedElement xmi:type="uml:Class" xmi:id="EAID_C3" name="Pedido" visibility="public">
                    <ownedAttribute xmi:type="uml:Property" xmi:id="EAID_A4" name="numero" visibility="private">
                      <type xmi:type="uml:PrimitiveType" href="http://schema.omg.org/spec/UML/2.1/uml.xml#String"/>
                    </ownedAttribute>
                  </packagedElement>
                  <packagedElement xmi:type="uml:Association" xmi:id="EAID_Assoc1" name="Cliente_Pedido">
                    <memberEnd xmi:idref="EAID_END_DST"/>
                    <memberEnd xmi:idref="EAID_END_SRC"/>
                    <ownedEnd xmi:type="uml:Property" xmi:id="EAID_END_SRC" type="EAID_C1" association="EAID_Assoc1">
                      <lowerValue xmi:type="uml:LiteralInteger" value="1"/>
                      <upperValue xmi:type="uml:LiteralUnlimitedNatural" value="1"/>
                    </ownedEnd>
                    <ownedEnd xmi:type="uml:Property" xmi:id="EAID_END_DST" type="EAID_C3" association="EAID_Assoc1" aggregation="none">
                      <lowerValue xmi:type="uml:LiteralInteger" value="0"/>
                      <upperValue xmi:type="uml:LiteralUnlimitedNatural" value="*"/>
                    </ownedEnd>
                  </packagedElement>
                </packagedElement>
              </uml:Model>
            </xmi:XMI>
            """;

        UMLImportModel model = parser.parse(xmiXml);
        assertNotNull(model);
        assertEquals("ModeloComercial", model.getNombre());
        assertEquals("Enterprise Architect", model.getExporter());

        // Verificar Clases
        assertEquals(3, model.getClases().size());
        UMLImportClass cliente = model.getClases().stream()
                .filter(c -> c.getNombre().equals("Cliente"))
                .findFirst()
                .orElse(null);
        assertNotNull(cliente);
        assertEquals(2, cliente.getAtributos().size());
        assertEquals(1, cliente.getMetodos().size());
        assertEquals("calcularDescuento", cliente.getMetodos().get(0).getNombre());
        assertEquals("Double", cliente.getMetodos().get(0).getTipoRetorno());

        // Verificar Relaciones (Herencia + Asociación)
        assertEquals(2, model.getRelaciones().size());

        UMLImportRelation herencia = model.getRelaciones().stream()
                .filter(r -> r.getTipoRelacion() == TipoRelacionUML.HERENCIA)
                .findFirst()
                .orElse(null);
        assertNotNull(herencia);
        assertEquals("Cliente", herencia.getClaseOrigenNombre());
        assertEquals("Persona", herencia.getClaseDestinoNombre());

        UMLImportRelation assoc = model.getRelaciones().stream()
                .filter(r -> r.getTipoRelacion() == TipoRelacionUML.ASOCIACION)
                .findFirst()
                .orElse(null);
        assertNotNull(assoc);
        assertEquals("1", assoc.getCardinalidadOrigen());
        assertEquals("0..*", assoc.getCardinalidadDestino());

        // Validación
        UMLImportValidationResult validation = parser.validate(model);
        assertTrue(validation.isValido());
        assertEquals(3, validation.getTotalClases());
        assertEquals(4, validation.getTotalAtributos());
        assertEquals(1, validation.getTotalMetodos());
        assertEquals(2, validation.getTotalRelaciones());
    }

    @Test
    @DisplayName("Debe detectar errores si el XMI no tiene clases")
    void testValidateWithoutClasses() throws Exception {
        String xmiEmpty = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xmi:XMI xmi:version="2.1" xmlns:uml="http://schema.omg.org/spec/UML/2.1" xmlns:xmi="http://schema.omg.org/spec/XMI/2.1">
              <uml:Model name="Vacio"/>
            </xmi:XMI>
            """;

        UMLImportModel model = parser.parse(xmiEmpty);
        UMLImportValidationResult validation = parser.validate(model);
        assertFalse(validation.isValido());
        assertFalse(validation.getErrores().isEmpty());
    }
}
