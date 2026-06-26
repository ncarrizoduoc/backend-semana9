package com.minimarket.minimarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
public class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Prueba que valida que un usuario con rol EMPLEADO puede acceder a una ruta de ventas
    @Test
    @WithMockUser(authorities = {"EMPLEADO"})
    public void empleadoPuedeVerVentaTest() throws Exception{
        // Arrange
        mockMvc.perform(get("/api/ventas"))
            .andExpect(status().isOk()); // Se espera un status OK
    }

    // Prueba que valida que las rutas de VentaController esten prohibidas para usuarios sin rol EMPLEADO
    @Test
    @WithMockUser(authorities = {"CLIENTE", "ADMIN"})
    public void noEmpleadoNoPuedeVerVentaTest() throws Exception{
        // Arrange
        mockMvc.perform(get("/api/ventas"))
            .andExpect(status().isForbidden()); // Se espera un status Forbidden
    }

}
