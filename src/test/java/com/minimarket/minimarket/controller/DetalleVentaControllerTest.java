package com.minimarket.minimarket.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.minimarket.dto.DetalleVentaRequest;
import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.mapper.DetalleVentaRequestMapper;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;
import com.minimarket.minimarket.service.impl.DetalleVentaServiceImpl;

@WebMvcTest(DetalleVentaController.class)
@Import(SecurityConfig.class)
public class DetalleVentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetalleVentaRequestMapper requestMapper;

    @MockitoBean
    private DetalleVentaServiceImpl detalleService;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Rol rol;
    private Usuario usuario;
    private Venta venta;
    private Categoria categoria;
    private Producto producto;
    private DetalleVentaRequest request;
    private DetalleVenta detalle;

    @BeforeEach
    void setUp(){
        // Rol
        rol = Rol.builder()
            .id(Long.valueOf(1))
            .nombre("CAJERO")
            .usuarios(new HashSet<Usuario>())
            .build();

        //Usuario
        usuario = Usuario.builder()
            .id(Long.valueOf(5))
            .username("UsuarioPrueba")
            .password("ContrasenaPrueba")
            .roles(new HashSet<>(Set.of(rol)))
            .build();

        // Venta
        venta = Venta.builder()
            .id(Long.valueOf(1))
            .usuario(usuario)
            .fecha(new Date())
            .detalles(List.of())
            .build();

        categoria = Categoria.builder()
            .id(Long.valueOf(1))
            .nombre("Abarrotes")
            .productos(new ArrayList<Producto>())
            .build();
        
        producto = Producto.builder()
            .id(Long.valueOf(1))
            .nombre("Arroz")
            .precio(2690.0)
            .stock(20)
            .categoria(categoria)
            .build();

        request = DetalleVentaRequest.builder()
            .id(Long.valueOf(1))
            .ventaId(Long.valueOf(1))
            .productoId(Long.valueOf(1))
            .cantidad(10)
            .precio(990.0)
            .build();

        detalle = DetalleVenta.builder()
            .id(Long.valueOf(1))
            .venta(venta)
            .producto(producto)
            .cantidad(10)
            .precio(990.0)
            .build();
    }

    @AfterEach
    void tearDown(){
        rol = null;
        usuario = null;
        venta = null;
        categoria = null;
        producto = null;
        request = null;
        detalle = null;
    }

    // Prueba que valida que un usuario autorizado (con rol CAJERO) pueda acceder al endpoint
    // [PUT /api/detalle-ventas] para editar un detalle de venta
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void usuarioAutorizadoPuedeModificarDetalleVentaTest() throws Exception{
        // Arrange
        when(detalleService.findById(any(Long.class))).thenReturn(detalle);
        when(detalleService.update(any(DetalleVenta.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(requestMapper.toDetalleVenta(any(DetalleVentaRequest.class))).thenReturn(detalle);

        mockMvc.perform(put("/api/detalle-ventas/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/detalle-ventas/1]
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request))) // El body contiene un objeto DetalleVentaRequest
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Valida que el detalle de venta retornado tenga ID 1
            .andExpect(jsonPath("$.producto.nombre").value("Arroz")) // Valida el nombre del producto
            .andExpect(jsonPath("$.cantidad").value(10)); // Valida que la cantidad sea la esperada
    }

    // Prueba que valida que si un usuario autorizado (con rol CAJERO) llama al endpoint [PUT /api/detalle-ventas/{id}]
    // para modificar un detalle de venta que no existe (por ID), recibe un status Not Found
    @Test
    @WithMockUser(authorities = {"CAJERO"}) 
    public void retornaNotFoundSiDetalleVentaModificadoNoExisteTest() throws Exception{
        when(detalleService.findById(any(Long.class))).thenReturn(null);

        mockMvc.perform(put("/api/detalle-ventas/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/detalle-ventas/1]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isNotFound()); // Se espera un status Not Found

    }


    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda acceder al endpoint
    // [PUT /api/detalle-ventas/{id}] para editar un detalle de venta
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeModificarDetalleVentaTest() throws Exception{
        mockMvc.perform(put("/api/detalle-ventas/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/detalle-ventas/1]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden()); // Se espera un codigo 403 (Forbidden)
    }

    // Prueba que verifica que solo un usuario autorizado (rol CAJERO) pueda
    // acceder al endpoint [GET /api/detalle-ventas] para ver los detalles de venta
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void usuarioAutorizadoPuedeVerDetalleVentasTest() throws Exception{
        // Arrange
        List<DetalleVenta> detalles = new ArrayList<DetalleVenta>(List.of(detalle));
        when(detalleService.findAll()).thenReturn(detalles);

        // Assert
        mockMvc.perform(get("/api/detalle-ventas")) // Se llama al endpoint [GET /api/detalle-ventas]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$", hasSize(1))) // Se verifica que la lista de detalles de venta retornada tenga 1 elemento
            .andExpect(jsonPath("$[0].id").value(Long.valueOf(1))); // Se verifica que el ID del elemento en la lista sea 1
    }

    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda
    // acceder al endpoint [GET /api/detalle-ventas]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeVerDetalleVentasTest() throws Exception{
        mockMvc.perform(get("/api/detalle-ventas")) // Llama el endpoint [GET /api/detalle-ventas]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que si un usuario autorizado (rol CAJERO) llama al endpoint
    // [GET /api/detalle-ventas] retorne el detalle de venta buscado por su ID, si existe
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void buscarPorIdRetornaDetalleVentaSiExisteTest() throws Exception{
        // Arrange
        when(detalleService.findById(any(Long.class))).thenReturn(detalle);

        // Assert
        mockMvc.perform(get("/api/detalle-ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/detalle-ventas/1]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Se valida el ID del detalle de venta encontrado
            .andExpect(jsonPath("$.cantidad").value(10)); // Se valida la cantidad de producto en el detalle de venta
    }

    // Prueba que valida que si un usuario autorizado (rol CAJERO) llama al endpoint
    // [GET /api/detalle-ventas] retorne un body vacio, si el detalle de venta buscado no existe
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void buscarPorIdRetornaNullSiNoExisteTest() throws Exception{
        // Arrange
        when(detalleService.findById(any(Long.class))).thenReturn(null);

        // Assert
        mockMvc.perform(get("/api/detalle-ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/detalle-ventas/1]
            .andExpect(status().isNotFound()) // Se espera un status Not Found
            .andExpect(content().string("")); // Se espera un body de respuesta vacio
            
    }

    // Prueba que valida que un usuario autorizado (rol CAJERO) pueda acceder al
    // endpoint [POST /api/detalle-ventas] para guardar un detalle de venta
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void usuarioAutorizadoPuedeGuardarDetalleVentaTest() throws Exception{
        // Arrange
        when(detalleService.save(any(DetalleVenta.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(requestMapper.toDetalleVenta(any(DetalleVentaRequest.class))).thenReturn(detalle);

        // Act y Assert
        mockMvc.perform(post("/api/detalle-ventas") // Se llama al endpoint [POST /api/detalle-ventas]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk()) // Espera un codigo 200 (OK)
            .andExpect(jsonPath("$.cantidad").value(10)); // Valida la cantidad del detalle de venta retornado

    }

    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda acceder
    // al endpoint [POST /api/detalle-ventas]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeGuardarDetalleVentaTest() throws Exception{
        mockMvc.perform(post("/api/detalle-ventas") // Se llama al endpoint [POST /api/detalle-ventas]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que un usuario autorizado (con rol CAJERO) pueda acceder al endpoint
    // [DELETE /api/detalle-ventas] para eliminar un detalle de venta por ID, si el detalle de venta existe
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void usuarioAutorizadoPuedeEliminarDetalleVentaSiExisteTest() throws Exception{
        // Arrange
        when(detalleService.findById(any(Long.class))).thenReturn(detalle);

        // Act y Assert
        mockMvc.perform(delete("/api/detalle-ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [DELETE /api/detalle-ventas/1]
            .andExpect(status().isOk()) // Espera un status OK
            .andExpect(jsonPath("$.message").value("Detalle de venta eliminado exitosamente"));
    }

    // Prueba que valida que si un usuario autorizado (rol CAJERO) intenta eliminar un detalle de venta
    // que no existe, a traves del endpoint [DELETE /api/detalle-ventas/{id}], recibe un status Not Found
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void eliminarDetalleVentaRetornaNotFoundSiNoExisteTest() throws Exception{
        // Arrange
        when(detalleService.findById(Long.valueOf(1))).thenReturn(null);

        // Act y Assert
        mockMvc.perform(delete("/api/detalle-ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [DELETE /api/detalle-ventas/1]
            .andExpect(status().isNotFound()); // Espera un status Not Found
    }

    // Prueba que valida que un usuario no autorizado no pueda acceder al endpoint [DELETE /api/detalle-ventas/{id}]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeEliminarDetalleVentaTest() throws Exception{
        mockMvc.perform(delete("/api/detalle-ventas/{id}", Long.valueOf(1))) // Llama al endpoint [DELETE /api/detalle-ventas/1]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }


    // Prueba que valida que el endpoint [POST /api/detalle-ventas] retorne Bad Request si el usuario adjunta
    // un DetalleVentaRequest no valido (no cumple con las restricciones de datos implementadas en la clase
    // DetalleVentaRequest). Espera como respuesta un status Bad Request
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void guardarDetalleVentaNoValidoLanzaErrorTest() throws Exception{
        request.setCantidad(-1); // Se asigna una cantidad negativa (no valida)

        mockMvc.perform(post("/api/detalle-ventas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Se espera un status Bad Request

    }

    // Prueba que valida que el endpoint [PUT /api/detalle-ventas] retorne Bad Request si el usuario adjunta
    // un DetalleVentaRequest no valido. Espera como respuesta un status Bad Request
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void modificarDetalleVentaNoValidoLanzaErrorTest() throws Exception{
        request.setCantidad(-1); // Se asigna una cantidad negativa (no valida)

        mockMvc.perform(put("/api/detalle-ventas/{id}", Long.valueOf(1))
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Se espera un status Bad Request

    }

}
