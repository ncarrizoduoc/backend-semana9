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
import com.minimarket.minimarket.dto.CarritoRequest;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.mapper.CarritoRequestMapper;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;
import com.minimarket.minimarket.service.impl.CarritoServiceImpl;

@WebMvcTest(CarritoController.class)
@Import(SecurityConfig.class)
public class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoRequestMapper requestMapper;

    @MockitoBean
    private CarritoServiceImpl carritoService;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Rol rol;
    private Usuario usuario;
    private Categoria categoria;
    private Producto producto;
    private Carrito carrito;
    private CarritoRequest request;


    @BeforeEach
    void setUp(){
        rol = Rol.builder()
            .id(Long.valueOf(1))
            .nombre("ADMIN")
            .usuarios(Set.of())
            .build();

        usuario = Usuario.builder()
            .id(Long.valueOf(1))
            .username("prueba")
            .password("password123")
            .roles(Set.of(rol))
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
            .stock(10)
            .categoria(categoria)
            .build();

        request = CarritoRequest.builder()
            .id(Long.valueOf(1))
            .usuarioId(Long.valueOf(1))
            .productoId(Long.valueOf(1))
            .cantidad(5)
            .build();
            
        carrito = Carrito.builder()
            .id(Long.valueOf(1))
            .usuario(usuario)
            .producto(producto)
            .cantidad(5)
            .build();
    }

    @AfterEach
    void tearDown(){
        rol = null;
        usuario = null;
        categoria = null;
        producto = null;
        carrito = null;
        request = null;
    }

    // Prueba que valida que un usuario autorizado (con rol CLIENTE o ADMIN) pueda acceder al endpoint
    // [PUT /api/carrito] para editar un carrito
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void usuarioAutorizadoPuedeModificarCarritoTest() throws Exception{
        // Arrange
        when(carritoService.findById(any(Long.class))).thenReturn(carrito);
        when(carritoService.update(any(Carrito.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(requestMapper.toCarrito(any(CarritoRequest.class))).thenReturn(carrito);

        mockMvc.perform(put("/api/carrito/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/carrito/1]
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request))) // El body contiene un objeto CarritoRequest
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Valida que el carrito retornado tenga ID 1
            .andExpect(jsonPath("$.cantidad").value(5)); // Valida que la cantidad sea la esperada
    }

    // Prueba que valida que un usuario autorizado (con rol CLIENTE) llama al endpoint [PUT /api/carrito/{id}]
    // para modificar un carrito que no existe (por ID), recibe un status Not Found
    @Test
    @WithMockUser(authorities = {"CLIENTE"}) 
    public void retornaNotFoundSiCarritoModificadoNoExisteTest() throws Exception{
        when(carritoService.findById(any(Long.class))).thenReturn(null);

        mockMvc.perform(put("/api/carrito/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/carrito/1]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isNotFound()); // Se espera un status Not Found

    }


    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda acceder al endpoint
    // [PUT /api/carrito/{id}] para editar un carrito
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeModificarCarritoTest() throws Exception{
        mockMvc.perform(put("/api/carrito/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/carrito/1]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden()); // Se espera un codigo 403 (Forbidden)
    }

    // Prueba que verifica que solo un usuario autorizado (rol CAJERO) pueda
    // acceder al endpoint [GET /api/carrito] para ver los carritos
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void usuarioAutorizadoPuedeVerCarritosTest() throws Exception{
        // Arrange
        List<Carrito> carritos = new ArrayList<Carrito>(List.of(carrito));
        when(carritoService.findAll()).thenReturn(carritos);

        // Assert
        mockMvc.perform(get("/api/carrito")) // Se llama al endpoint [GET /api/carrito]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$", hasSize(1))) // Se verifica que la lista de carrito retornada tenga 1 elemento
            .andExpect(jsonPath("$[0].id").value(Long.valueOf(1))); // Se verifica que el ID del elemento en la lista sea 1
    }

    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda
    // acceder al endpoint [GET /api/carrito]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeVerCarritosTest() throws Exception{
        mockMvc.perform(get("/api/carrito")) // Llama el endpoint [GET /api/carrito]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que si un usuario autorizado (rol CLIENTE) llama al endpoint
    // [GET /api/carrito] retorne el carrito buscado por su ID, si existe
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void buscarPorIdRetornaCarritoSiExisteTest() throws Exception{
        // Arrange
        when(carritoService.findById(any(Long.class))).thenReturn(carrito);

        // Assert
        mockMvc.perform(get("/api/carrito/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/carrito/1]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Se valida el ID del carrito encontrado
            .andExpect(jsonPath("$.cantidad").value(5)); // Se valida la cantidad de producto en el carrito
    }

    // Prueba que valida que si un usuario autorizado (rol CAJERO) llama al endpoint
    // [GET /api/carrito] retorne un body vacio, si el carrito buscado no existe
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void buscarPorIdRetornaNullSiNoExisteTest() throws Exception{
        // Arrange
        when(carritoService.findById(any(Long.class))).thenReturn(null);

        // Assert
        mockMvc.perform(get("/api/carrito/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/carrito/1]
            .andExpect(status().isNotFound()) // Se espera un status Not Found
            .andExpect(content().string("")); // Se espera un body de respuesta vacio
            
    }

    // Prueba que valida que un usuario autorizado (rol CLIENTE) pueda acceder al
    // endpoint [POST /api/carrito] para guardar un carrito
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void usuarioAutorizadoPuedeGuardarCarritoTest() throws Exception{
        // Arrange
        when(carritoService.save(any(Carrito.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(requestMapper.toCarrito(any(CarritoRequest.class))).thenReturn(carrito);

        // Act y Assert
        mockMvc.perform(post("/api/carrito") // Se llama al endpoint [POST /api/carrito]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk()) // Espera un codigo 200 (OK)
            .andExpect(jsonPath("$.cantidad").value(5)); // Valida la cantidad del carrito retornado

    }

    // Prueba que valida que un usuario no autorizado (sin rol CLIENTE) no pueda acceder
    // al endpoint [POST /api/carrito]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeGuardarCarritoTest() throws Exception{
        mockMvc.perform(post("/api/carrito") // Se llama al endpoint [POST /api/carrito]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que un usuario autorizado (con rol CLIENTE) pueda acceder al endpoint
    // [DELETE /api/carrito] para eliminar un carrito por ID, si el carrito existe
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void usuarioAutorizadoPuedeEliminarCarritoSiExisteTest() throws Exception{
        // Arrange
        when(carritoService.findById(any(Long.class))).thenReturn(carrito);

        // Act y Assert
        mockMvc.perform(delete("/api/carrito/{id}", Long.valueOf(1))) // Se llama al endpoint [DELETE /api/carrito/1]
            .andExpect(status().isOk()); // Espera un status OK
    }

    // Prueba que valida que si un usuario autorizado (rol CLIENTE) intenta eliminar un carrito
    // que no existe, a traves del endpoint [DELETE /api/carrito/{id}], recibe un status Not Found
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void eliminarCarritoRetornaNotFoundSiNoExisteTest() throws Exception{
        // Arrange
        when(carritoService.findById(Long.valueOf(1))).thenReturn(null);

        // Act y Assert
        mockMvc.perform(delete("/api/carrito/{id}", Long.valueOf(1))) // Se llama al endpoint [DELETE /api/carrito/1]
            .andExpect(status().isNotFound()); // Espera un status Not Found
    }

    // Prueba que valida que un usuario no autorizado no pueda acceder al endpoint [DELETE /api/carrito/{id}]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeEliminarCarritoTest() throws Exception{
        mockMvc.perform(delete("/api/carrito/{id}", Long.valueOf(1))) // Llama al endpoint [DELETE /api/carrito/1]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }


    // Prueba que valida que el endpoint [POST /api/carrito] retorne Bad Request si el usuario adjunta
    // un CarritoRequest no valido (no cumple con las restricciones de datos implementadas en la clase
    // CarritoRequest). Espera como respuesta un status Bad Request
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void guardarCarritoNoValidoLanzaErrorTest() throws Exception{
        request.setUsuarioId(null); // Se asigna un ID de usuario null (no valido)

        mockMvc.perform(post("/api/carrito")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Se espera un status Bad Request

    }

    // Prueba que valida que el endpoint [PUT /api/carrito] retorne Bad Request si el usuario adjunta
    // un CarritoRequest no valido. Espera como respuesta un status Bad Request
    @Test
    @WithMockUser(authorities = {"CLIENTE"})
    public void modificarCarritoNoValidoLanzaErrorTest() throws Exception{
        request.setCantidad(-2); // Se asigna una cantidad negativa (que no esta permitido)

        mockMvc.perform(put("/api/carrito/{id}", Long.valueOf(1))
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Se espera un status Bad Request

    }


}
