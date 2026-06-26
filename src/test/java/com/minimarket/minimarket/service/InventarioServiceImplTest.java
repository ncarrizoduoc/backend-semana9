package com.minimarket.minimarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.repository.InventarioRepository;
import com.minimarket.minimarket.service.impl.InventarioServiceImpl;

@ExtendWith(MockitoExtension.class)
public class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepo;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    // Prueba que verifica que al guardar un inventario, se retorne el inventario
    // con sus todos sus datos 
    @Test
    public void agregarInventarioTest(){
        // Arrange
        Inventario inventario = new Inventario();
        Producto producto = new Producto();
        
        inventario.setId(Long.valueOf(1));
        inventario.setProducto(producto);
        inventario.setCantidad(10);
        inventario.setTipoMovimiento("Salida");
        inventario.setFechaMovimiento(java.sql.Date.valueOf("2026-01-31"));

        when(inventarioRepo.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Inventario guardado = inventarioService.save(inventario);

        // Assert
        assertNotNull(guardado); //Verifica que retorne un objeto no null
        assertEquals(guardado.getId(), Long.valueOf(1)); // Verifica que el ID del inventario sea correcto
        assertEquals(guardado.getProducto(), producto); // Verifica que el producto del inventario sea correcto
        assertEquals(guardado.getCantidad(), 10); // Verifica que la cantidad del inventario sea correcto
        assertEquals(guardado.getTipoMovimiento(), "Salida"); // Verifica que el tipo de movimiento sea correcto
        assertEquals(guardado.getFechaMovimiento(), java.sql.Date.valueOf("2026-01-31")); // Verificar que la fecha sea correcta
        verify(inventarioRepo, times(1)).save(inventario);
    }

    // Prueba que verifica que el metodo findAll de InventarioService retorne todos los inventarios
    @Test
    public void findAllRetornaTodosLosInventarioTest(){
        // Arrange
        Inventario inventario1 = new Inventario();
        inventario1.setId(Long.valueOf(1));

        Inventario inventario2 = new Inventario();
        inventario2.setId(Long.valueOf(2));

        when(inventarioRepo.findAll()).thenReturn(new ArrayList<Inventario>(List.of(inventario1, inventario2)));

        // Act
        List<Inventario> lista = inventarioService.findAll();

        // Assert
        assertEquals(lista.size(), 2); // Se verifica que el largo de la lista sea el esperado
        assertTrue(lista.contains(inventario1)); // Se verifica que la lista contenga cada objeto Inventario
        assertTrue(lista.contains(inventario2));
        verify(inventarioRepo, times(1))
            .findAll(); // Se verifica que se llame al metodo findAll de InventarioRepository
    }

    // Prueba que verifica que al buscar un inventario por ID, retorne el inventario esperado con
    // sus datos correctos
    @Test
    public void findByIdretornaInventarioCorrectoTest(){
        // Arrange
        Inventario inventario = new Inventario();
        inventario.setId(Long.valueOf(1));
        inventario.setCantidad(99);
        when(inventarioRepo.findById(Long.valueOf(1))).thenReturn(Optional.of(inventario));

        // Act
        Inventario buscado = inventarioService.findById(Long.valueOf(1));

        // Assert
        assertNotNull(buscado); // Se verifica que el inventario retornado no sea null
        assertEquals(buscado, inventario); // Se verifica que el inventario retornado y el esperado sean el mismo
        assertEquals(buscado.getId(), Long.valueOf(1)); // Se verifica que los atributos del inventario sean los esperados
        assertEquals(buscado.getCantidad(), 99);
        verify(inventarioRepo, times(1))
            .findById(Long.valueOf(1)); // Se verifica llamada a metodo findById de InventarioRepository
    }

    // Prueba que verifica que al buscar usuario por ID, si no existe, retorne null
    @Test
    public void findByIdRetornaNullSiNoExisteTest(){
        // Arrange
        when(inventarioRepo.findById(Long.valueOf(1))).thenReturn(Optional.empty());

        Inventario buscado = inventarioService.findById(Long.valueOf(1));

        assertNull(buscado); // Se verifica que retorne un null
        verify(inventarioRepo, times(1))
            .findById(Long.valueOf(1)); // Se verifica llamada a metodo findById de InventarioRepository
    }

    // Prueba que verifica que al buscar inventarios por ID de producto, se retornen los inventarios esperados
    @Test
    public void findByProductoIdRetornaInventariosTest(){
        // Arrange
        Producto producto = new Producto();
        producto.setId(Long.valueOf(1));

        Inventario inventario1 = new Inventario();
        inventario1.setProducto(producto);
        Inventario inventario2 = new Inventario(); 
        inventario2.setProducto(producto);

        when(inventarioRepo.findByProductoId(Long.valueOf(1)))
            .thenReturn(new ArrayList<Inventario>(List.of(inventario1, inventario2)));

        // Act
        List<Inventario> lista = inventarioService.findByProductoId(Long.valueOf(1));

        // Assert
        assertEquals(lista.size(), 2);
        assertTrue(lista.contains(inventario1));
        assertTrue(lista.contains(inventario2));
        assertEquals(lista.get(0).getProducto().getId(), Long.valueOf(1));
        assertEquals(lista.get(1).getProducto().getId(), Long.valueOf(1));
        verify(inventarioRepo, times(1))
            .findByProductoId(Long.valueOf(1));
    }

}
