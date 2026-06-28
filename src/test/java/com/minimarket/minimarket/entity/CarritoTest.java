package com.minimarket.minimarket.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CarritoTest {

    private Usuario usuario;
    private Carrito carrito;

    @BeforeEach
    void SetUp(){
        usuario = new Usuario();
        usuario.setId(Long.valueOf(99));
        usuario.setUsername("username");
        usuario.setPassword("password");

        carrito = new Carrito();
    }

    @AfterEach
    void tearDown(){
        usuario = null;
        carrito = null;
    }

    // Prueba que verifica que el usuario asociado a un carrito sea correcto (tenga todos sus datos)
    @Test
    public void usuarioAsociadoACarritoEsCorrectoTest(){
        // Arrange
        carrito.setUsuario(usuario);

        // Act
        Usuario usuarioCarrito = carrito.getUsuario();

        // Assert
        assertNotNull(usuarioCarrito); // Se verifica que el usuario no sea Null
        assertEquals(usuario, usuarioCarrito); // Se verifica que el usuario guardado sea igual al retornado
        assertEquals(usuario.getId(), Long.valueOf(99)); // Se verifican los datos del usuario
        assertEquals(usuario.getUsername(), "username");
        assertEquals(usuario.getPassword(), "password");


    }

}
