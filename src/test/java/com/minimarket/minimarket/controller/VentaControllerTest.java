package com.minimarket.minimarket.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.minimarket.dto.VentaRequest;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.mapper.VentaRequestMapper;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;
import com.minimarket.minimarket.service.impl.VentaServiceImpl;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@WebMvcTest(VentaController.class)
@Import(SecurityConfig.class)
public class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaServiceImpl ventaService;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private VentaRequestMapper requestMapper;

    // Declaracion de objetos
    private Rol rol;
    private Usuario usuario;
    private DetalleVenta detalle;
    private Categoria categoria;
    private Producto producto;
    private Venta venta;
    private VentaRequest request;

    // Se crean objetos (de clase Venta y otros) para probar las llamadas a endpoints
    @BeforeEach
    void setUp(){
        // Rol
        rol = Rol.builder()
            .id(Long.valueOf(1))
            .nombre("CLIENTE")
            .usuarios(new HashSet<Usuario>())
            .build();

        //Usuario
        usuario = Usuario.builder()
            .id(Long.valueOf(5))
            .username("UsuarioPrueba")
            .password("ContrasenaPrueba")
            .roles(new HashSet<>(Set.of(rol)))
            .build();

        // Venta request (DTO para solicitudes)
        request = VentaRequest.builder()
            .id(Long.valueOf(1))
            .usuarioId(Long.valueOf(5))
            .fecha(new Date())
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

        // Detalle de venta
        detalle = DetalleVenta.builder()
            .id(Long.valueOf(1))
            .venta(venta)
            .producto(producto)
            .cantidad(10)
            .precio(990.0)
            .build();

        // Venta
        venta = Venta.builder()
            .id(Long.valueOf(1))
            .usuario(usuario)
            .fecha(new Date())
            .detalles(List.of(detalle))
            .build();
        
        detalle.setVenta(venta);
    }

    // Despues de cada prueba, se asigna null a los objetos para liberar espacio
    @AfterEach
    void tearDown(){
        rol = null;
        usuario = null;
        detalle = null;
        categoria = null;
        producto = null;
        venta = null;
        request = null;
    }

    // Prueba que verifica que un usuario con rol CAJERO pueda acceder al endpoint [POST /api/ventas] y
    // guardar una venta (debe incluir un RequestBody con una venta valida)
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void cajeroPuedeGuardarVentaTest() throws Exception{
        // Arrange
        when(requestMapper.toVenta(any(VentaRequest.class))).thenReturn(venta);
        when(ventaService.save(any(Venta.class))).thenAnswer(invocation ->{
            return invocation.getArgument(0);
        });

        // Act y Assert
        mockMvc.perform(post("/api/ventas") // Se llama al endpoint [POST /api/ventas]
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(request))) // El body contiene un objeto VentaRequest valido
            .andDo(print())
            .andExpect(status().isOk()) // Verificar que retorna un status OK
            .andExpect(jsonPath("$.usuario.id").value(Long.valueOf(5))) // Verifica que el ID del usuario de la venta sea el esperado
            .andExpect(jsonPath("$.detalles[0].producto.nombre").value("Arroz")) // Verifica el nombre del producto
            .andExpect(jsonPath("$.total").value(9900.0)); // Valida el calculo del total
    }

    // Prueba que verifica que un usuario no autorizado (sin rol CAJERO) no pueda acceder
    // al endpoint [POST /api/ventas] para registrar una venta, pues el endpoint solo admite
    // usuarios con rol CAJERO
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeGuardarVentaTest() throws Exception{
        mockMvc.perform(post("/api/ventas") // Se llama al endpoint [POST /api/ventas]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request))) // El body contiene un objeto Venta valido
            .andExpect(status().isForbidden()); // Espera un Status 403 (prohibido)
    }

    // Prueba que verifica que un usuario con rol CAJERO pueda ver las ventas en sistema
    // llamando al endpoint [GET /api/ventas]
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void cajeroPuedeVerVentasTest() throws Exception{
        // Arrange
        List<Venta> ventas = new ArrayList<Venta>(List.of(venta));
        when(ventaService.findAll()).thenReturn(ventas);

        // Assert
        mockMvc.perform(get("/api/ventas")) // Se llama al endpoint [GET /api/ventas]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$", hasSize(1))) // Se verifica que la lista de ventas retornada tenga 1 elemento
            .andExpect(jsonPath("$[0].id").value(Long.valueOf(1))); // Se verifica que el ID del primer elemento sea 1
    }

    // Prueba que verifica que un usuario no autorizado (sin rol CAJERO) no pueda ver el listado de ventas
    // llamando al endpoint [GET /api/ventas]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeVerVentasTest() throws Exception{
        mockMvc.perform(get("/api/ventas")) // Se llama al endpoint [GET /api/ventas]
            .andExpect(status().isForbidden()); // Se espera un codigo 403 (Forbidden)
    }

    // Prueba que verifica que si un usuario con rol CAJERO llama al endpoint [GET /api/ventas]
    // para buscar una venta por ID, retorne la venta (cuando exista)
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void cajeroPuedeBuscarVentaExistentePorIdTest() throws Exception{
        // Arrange
        when(ventaService.findById(Long.valueOf(1))).thenReturn(venta);

        // Act y Assert
        mockMvc.perform(get("/api/ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/ventas/1]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))); // Se verifica que el ID de la venta retornada sea 1
    }

    // Metodo que verifica que si un usuario con rol CAJERO llama al endpoint [GET /api/ventas]
    // para buscar una venta por ID, y la venta no existe, recibe una respuesta vacia
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void BuscarVentaPorIdRetornaNullSiNoExisteTest() throws Exception{
        // Arrange
        when(ventaService.findById(Long.valueOf(1))).thenReturn(null);

        // Act y Assert
        mockMvc.perform(get("/api/ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/ventas/1]
            .andExpect(status().isNotFound()) // Se espera un codigo 404 (NotFound)
            .andExpect(content().string("")); // Se verifica que el body de la respuesta este vacio
    }
    
    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda acceder al 
    // endpoint [GET /api/ventas]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeBuscarVenta() throws Exception{
        mockMvc.perform(get("/api/ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/ventas/1]
            .andExpect(status().isForbidden()); // Se espera un codigo 403 (Forbidden)
    }

    // Prueba que valida que el endpoint [POST /api/ventas] retorne Bad Request si el usuario adjunta
    // una Venta no valida (que no cumple con las restricciones de datos implementadas en la clase
    // Venta) en el body de la solicitud. Espera como respuesta un status Bad Request
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void guardarVentaNoValidaLanzaErrorTest() throws Exception{
        request.setFecha(null); // Se asigna fecha null (que no esta permitido)

        mockMvc.perform(post("/api/ventas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest()); // Se espera un status Bad Request

    }

}
