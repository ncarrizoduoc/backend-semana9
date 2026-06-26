package com.minimarket.minimarket.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class InventarioTest {

    // Prueba que verifica que el producto asociado a un inventario sea correcto y que
    // los datos del producto sean correctos
    @Test
    public void ProductoAsociadoAInventarioEsCorrectoTest(){
        // Arrange
        Categoria categoria = new Categoria();
        Producto producto = new Producto();
        producto.setId(Long.valueOf(1));
        producto.setNombre("Arroz");
        producto.setPrecio(2000.0);
        producto.setStock(99);
        producto.setCategoria(categoria);

        Inventario inventario = new Inventario();
        inventario.setProducto(producto);

        // Act
        Producto asociado = inventario.getProducto();

        // Assert
        assertNotNull(asociado); // Verificar que el producto asociado al inventario no sea null
        assertEquals(asociado, producto);
        assertEquals(asociado.getNombre(), "Arroz"); // Verificacion de los datos individuales del producto
        assertEquals(asociado.getId(), Long.valueOf(1));
        assertEquals(asociado.getPrecio(), 2000.0);
        assertEquals(asociado.getStock(), 99);
        assertEquals(asociado.getCategoria(), categoria);
    }

}
