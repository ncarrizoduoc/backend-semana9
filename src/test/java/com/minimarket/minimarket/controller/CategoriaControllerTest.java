package com.minimarket.minimarket.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
import com.minimarket.minimarket.dto.CategoriaRequest;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;
import com.minimarket.minimarket.service.impl.CategoriaServiceImpl;

@WebMvcTest(CategoriaController.class)
@Import(SecurityConfig.class)
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaServiceImpl categoriaService;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private CategoriaRequest request;
    private Categoria categoria;

    @BeforeEach
    void setUp(){
        request = CategoriaRequest.builder()
            .id(Long.valueOf(1))
            .nombre("PRUEBA")
            .build();

        categoria = Categoria.builder()
            .id(Long.valueOf(1))
            .nombre("PRUEBA")
            .build();
    }

    @AfterEach
    void tearDown(){
        request = null;
        categoria = null;
    }

    // Prueba que valida que un usuario autorizado (con rol ADMIN) pueda acceder al endpoint
    // [PUT /api/categorias] para editar un categoria
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void usuarioAutorizadoPuedeModificarCategoriaTest() throws Exception{
        // Arrange
        when(categoriaService.findById(any(Long.class))).thenReturn(categoria);
        when(categoriaService.save(any(Categoria.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        mockMvc.perform(put("/api/categorias/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/categorias/99]
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request))) // El body contiene un objeto Categoria valido
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Se valida que el Categoria retornado tenga ID 99
            .andExpect(jsonPath("$.nombre").value("PRUEBA")); // Se valida que el nombre del categoria sea el esperado
    }

    // Prueba que valida que si un usuario autorizado (con rol ADMIN) llama al endpoint [PUT /api/categorias/{id}]
    // para modificar un categoria que no existe (por ID), recibe un status Not Found
    @Test
    @WithMockUser(authorities = {"ADMIN"}) 
    public void respondeNotFoundSiCategoriaModificadoNoExisteTest() throws Exception{
        when(categoriaService.findById(any(Long.class))).thenReturn(null);

        mockMvc.perform(put("/api/categorias/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/categorias/99]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isNotFound()); // Se espera un status Not Found

    }


    // Prueba que valida que un usuario no autorizado (sin rol ADMIN) no pueda acceder al endpoint
    // [PUT /api/categorias/{id}] para editar un categoria
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeModificarCategoriaTest() throws Exception{
        mockMvc.perform(put("/api/categorias/{id}", Long.valueOf(1)) // Se llama al endpoint [PUT /api/categorias/99]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden()); // Se espera un codigo 403 (Forbidden)
    }

    // Prueba que verifica que el endpoint [GET /api/categorias] sea publico
    // Se hace la prueba con un usuario anonimo (sin rol)
    @Test
    @WithAnonymousUser
    public void cualquierUsuarioPuedeVerCategoriasTest() throws Exception{
        // Arrange
        when(categoriaService.findAll()).thenReturn(List.of(categoria));

        // Assert
        mockMvc.perform(get("/api/categorias")) // Se llama al endpoint [GET /api/categorias]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$", hasSize(1))) // Se verifica que la lista de categorias retornada tenga 1 elemento
            .andExpect(jsonPath("$[0].id").value(Long.valueOf(1))) // Se verifica que el ID del elemento en la lista sea 1
            .andExpect(jsonPath("$[0].nombre").value("PRUEBA"));
    }

    // Prueba que valida que el endpoint publico [GET /api/categorias] retorne
    // un categoria buscado por su ID, si existe
    @Test
    @WithAnonymousUser
    public void buscarCategoriaPorIdRetornaCategoriaSiExisteTest() throws Exception{
        // Arrange
        when(categoriaService.findById(any(Long.class))).thenReturn(categoria);

        // Assert
        mockMvc.perform(get("/api/categorias/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/categorias/1]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Se valida el ID del categoria encontrado
            .andExpect(jsonPath("$.nombre").value("PRUEBA")); // Se valida el nombre de la categoria encontrada
            verify(categoriaService, times(1)).findById(Long.valueOf(1));
    }

    // Prueba que valida que el endpoint publico [GET /api/categorias] retorne
    // un body vacio si el categoria con ID buscado no existe
    @Test
    @WithAnonymousUser
    public void buscarCategoriaPorIdRetornaNullSiNoExisteTest() throws Exception{
        // Arrange
        when(categoriaService.findById(Long.valueOf(1))).thenReturn(null);

        // Assert
        mockMvc.perform(get("/api/categorias/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/categorias/1]
            .andExpect(status().isNotFound()) // Se espera un status Not Found
            .andExpect(content().string("")); // Se espera un body de respuesta vacio
            
    }

    // Prueba que valida que un usuario autorizado (con rol ADMIN) pueda acceder al
    // endpoint [POST /api/categorias] para guardar un categoria
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void usuarioAutorizadoPuedeGuardarCategoriaTest() throws Exception{
        // Arrange
        when(categoriaService.save(any(Categoria.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act y Assert
        mockMvc.perform(post("/api/categorias") // Se llama al endpoint [POST /api/categorias]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk()) // Espera un codigo 200 (OK)
            .andExpect(jsonPath("$.nombre").value("PRUEBA")); // Valida el nombre de la categoria retornada

            verify(categoriaService, times(1)).save(any(Categoria.class));

    }

    // Prueba que valida que un usuario no autorizado (sin rol ADMIN) no pueda acceder
    // al endpoint [POST /api/categorias]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeGuardarCategoriaTest() throws Exception{
        mockMvc.perform(post("/api/categorias") // Se llama al endpoint [POST /api/categorias]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que un usuario autorizado (con rol ADMIN) pueda acceder al endpoint
    // [DELETE /api/categorias] para eliminar un categoria por ID, si el categoria existe
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void usuarioAutorizadoPuedeEliminarCategoriaSiExisteTest() throws Exception{
        // Arrange
        when(categoriaService.findById(any(Long.class))).thenReturn(categoria);

        // Act y Assert
        mockMvc.perform(delete("/api/categorias/{id}", Long.valueOf(1))) // Se llama al endpoint [DELETE /api/categorias/1]
            .andExpect(status().isOk()) // Espera un status OK
            .andExpect(jsonPath("$.message").value("Categoria eliminada exitosamente"));
    }

    // Prueba que valida que si un usuario autorizado (con rol ADMIN) intenta eliminar un categoria
    // que no existe, a traves del endpoint [DELETE /api/categorias/{id}], recibe un status Not Found
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void eliminarCategoriaRetornaNotFoundSiNoExisteTest() throws Exception{
        // Arrange
        when(categoriaService.findById(any(Long.class))).thenReturn(null);

        // Act y Assert
        mockMvc.perform(delete("/api/categorias/{id}", Long.valueOf(1))) // Se llama al endpoint [DELETE /api/categorias/1]
            .andExpect(status().isNotFound()); // Espera un status Not Found
    }

    // Prueba que valida que un usuario no autorizado no pueda acceder al endpoint [DELETE /api/categorias/{id}]
    @Test
    @WithMockUser(authorities = {"NOAUTORIZADO"})
    public void usuarioNoAutorizadoNoPuedeEliminarCategoriaTest() throws Exception{
        mockMvc.perform(delete("/api/categorias/{id}", Long.valueOf(1))) // Llama al endpoint [DELETE /api/categorias/1]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que el endpoint [POST /api/categorias] retorne Bad Request si el usuario adjunta
    // un CategoriaRequest no valido (que no cumple con las restricciones de datos implementadas en la clase
    // CategoriaRequest) en el body de la solicitud. Espera como respuesta un status Bad Request
    // Se valida el correcto funcionamiento de la anotacion @Valid
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void guardarCategoriaNoValidoLanzaErrorTest() throws Exception{
        request.setNombre(null); // Se asigna un nombre null a la categoria (que no esta permitido)

        mockMvc.perform(post("/api/categorias")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Se espera un status Bad Request

    }

    // Prueba que valida que el endpoint [PUT /api/categorias] retorne Bad Request si el usuario adjunta
    // un CategoriaRequest no valido (que no cumple con las restricciones de datos implementadas en la clase
    // CategoriaRequest) en el body de la solicitud. Espera como respuesta un status Bad Request
    // Se valida el correcto funcionamiento de la anotacion @Valid
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void modificarCategoriaNoValidoLanzaErrorTest() throws Exception{
        request.setNombre(""); // Se asigna un nombre en blanco a la categoria (que no esta permitido)

        mockMvc.perform(put("/api/categorias/{id}", Long.valueOf(1))
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Se espera un status Bad Request

    }

}
