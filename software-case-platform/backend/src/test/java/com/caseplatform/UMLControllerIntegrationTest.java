package com.caseplatform;

import com.caseplatform.dto.uml.AtributoUMLDTO;
import com.caseplatform.dto.uml.ClaseUMLDTO;
import com.caseplatform.dto.uml.ProyectoUMLDTO;
import com.caseplatform.dto.uml.RelacionUMLDTO;
import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.TipoRelacionUML;
import com.caseplatform.model.Usuario;
import com.caseplatform.model.VisibilidadUML;
import com.caseplatform.repository.UsuarioRepository;
import com.caseplatform.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UMLControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.caseplatform.repository.ProyectoUMLRepository proyectoUMLRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;

    @BeforeEach
    void setUp() {
        proyectoUMLRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombreCompleto("Ingeniero UML")
                .email("uml.dev@caseplatform.com")
                .password(passwordEncoder.encode("DevPass123*"))
                .rol(Rol.INGENIERO)
                .estado(EstadoUsuario.ACTIVO)
                .build());

        token = jwtService.generarToken(usuario);
    }

    @Test
    @DisplayName("Seguridad: Rechaza acceso a /api/proyectos sin autenticación (401)")
    void testAccesoProyectosSinToken_Devuelve401() throws Exception {
        mockMvc.perform(get("/api/proyectos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Flujo Completo UML: Proyecto -> Modelo -> Clases -> Atributos -> Relaciones")
    void testFlujoCompletoUML() throws Exception {
        // 1. Crear Proyecto UML
        ProyectoUMLDTO nuevoProyecto = ProyectoUMLDTO.builder()
                .nombre("Sistema Bancario")
                .descripcion("Core bancario transaccional")
                .build();

        MvcResult resProyecto = mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoProyecto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Sistema Bancario"))
                .andExpect(jsonPath("$.modelos[0].id").isNumber())
                .andReturn();

        ProyectoUMLDTO proyectoCreado = objectMapper.readValue(
                resProyecto.getResponse().getContentAsString(), ProyectoUMLDTO.class);
        Long modeloId = proyectoCreado.getModelos().get(0).getId();

        // 2. Crear Clase 1: Cliente
        ClaseUMLDTO claseClienteDto = ClaseUMLDTO.builder()
                .nombre("Cliente")
                .visibilidad(VisibilidadUML.PUBLIC)
                .posicionX(100.0)
                .posicionY(150.0)
                .build();

        MvcResult resCliente = mockMvc.perform(post("/api/modelos/" + modeloId + "/clases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claseClienteDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Cliente"))
                .andReturn();

        ClaseUMLDTO clienteCreado = objectMapper.readValue(
                resCliente.getResponse().getContentAsString(), ClaseUMLDTO.class);

        // 3. Crear Clase 2: CuentaBancaria
        ClaseUMLDTO claseCuentaDto = ClaseUMLDTO.builder()
                .nombre("CuentaBancaria")
                .visibilidad(VisibilidadUML.PUBLIC)
                .posicionX(350.0)
                .posicionY(150.0)
                .build();

        MvcResult resCuenta = mockMvc.perform(post("/api/modelos/" + modeloId + "/clases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claseCuentaDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("CuentaBancaria"))
                .andReturn();

        ClaseUMLDTO cuentaCreada = objectMapper.readValue(
                resCuenta.getResponse().getContentAsString(), ClaseUMLDTO.class);

        // 4. Agregar Atributo a Cliente
        AtributoUMLDTO attrDni = AtributoUMLDTO.builder()
                .nombre("numeroDocumento")
                .tipoDato("String")
                .visibilidad(VisibilidadUML.PRIVATE)
                .build();

        mockMvc.perform(post("/api/clases/" + clienteCreado.getId() + "/atributos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attrDni)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("numeroDocumento"))
                .andExpect(jsonPath("$.tipoDato").value("String"));

        // 5. Crear Relación entre Cliente (1) y CuentaBancaria (*)
        RelacionUMLDTO relacionDto = RelacionUMLDTO.builder()
                .tipoRelacion(TipoRelacionUML.ASOCIACION)
                .claseOrigenId(clienteCreado.getId())
                .claseDestinoId(cuentaCreada.getId())
                .cardinalidadOrigen("1")
                .cardinalidadDestino("1..*")
                .descripcion("Un cliente posee una o más cuentas bancarias")
                .build();

        MvcResult resRel = mockMvc.perform(post("/api/relaciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relacionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoRelacion").value("ASOCIACION"))
                .andExpect(jsonPath("$.cardinalidadOrigen").value("1"))
                .andExpect(jsonPath("$.cardinalidadDestino").value("1..*"))
                .andReturn();

        RelacionUMLDTO relCreada = objectMapper.readValue(
                resRel.getResponse().getContentAsString(), RelacionUMLDTO.class);

        // 6. Consultar Modelo Completo ensamblado
        mockMvc.perform(get("/api/modelos/" + modeloId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clases.length()").value(2))
                .andExpect(jsonPath("$.relaciones.length()").value(1));

        // 7. Modificar Clase
        ClaseUMLDTO updateClase = ClaseUMLDTO.builder()
                .nombre("ClienteTitular")
                .visibilidad(VisibilidadUML.PUBLIC)
                .build();

        mockMvc.perform(put("/api/clases/" + clienteCreado.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateClase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ClienteTitular"));

        // 8. Eliminar Relación
        mockMvc.perform(delete("/api/relaciones/" + relCreada.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
