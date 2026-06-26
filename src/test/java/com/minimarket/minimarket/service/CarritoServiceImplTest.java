package com.minimarket.minimarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.CarritoRepository;
import com.minimarket.minimarket.service.impl.CarritoServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepo;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Carrito carrito;

    @BeforeEach
    void setUp(){
        carrito = new Carrito();
        carrito.setId(Long.valueOf(1));
    }

    @AfterEach
    void tearDown(){
        carrito = null;
    }

    // Prueba que verifica que CarritoServiceImpl guarda un carrito correctamente 
    // El stock del producto debe ser mayor a la cantidad agregada
    @Test
    public void agregarCarritoValidoTest(){
        // Arrange
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        producto.setStock(10);

        carrito.setProducto(producto);
        carrito.setCantidad(1);

        when(carritoRepo.save(any(Carrito.class))).thenAnswer(invocation ->{
            return invocation.getArgument(0);
        });

        // Act
        Carrito carritoCreado = carritoService.save(carrito);

        // Assert
        assertNotNull(carritoCreado); // Verifica que devuelva un objeto no nulo
        assertEquals(carrito, carritoCreado); // Verifica que devuelva el mismo objeto creado
        verify(carritoRepo, times(1)).save(carrito); // Verifica que se haya llamado al metodo save de CarritoRepository
    }

    // Prueba que verifica que se lance una excepcion si se intenta agregar un
    // Carrito con un producto con stock insuficiente
    @Test
    public void stockInsuficienteLanzaExcepcionTest(){
        // Arrange
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        producto.setStock(10);

        carrito.setProducto(producto);
        carrito.setCantidad(20);

        // Assert
        assertThrows(StockInsuficienteException.class, () -> {
            carritoService.save(carrito);  
        }, "Deberia lanzar StockInsuficienteException");
    }

    // Prueba que verifica que el metodo findAll() de CarritoService retorne una
    // lista con todos los carritos
    @Test
    public void findAllRetornaTodosCarritosTest(){
        // Arrange
        Carrito carrito2 = new Carrito();

        when(carritoRepo.findAll()).thenReturn(new ArrayList<Carrito>(List.of(carrito, carrito2)));

        // Act
        List<Carrito> carritos = carritoService.findAll();

        // Assert
        assertEquals(carritos.size(), 2);
        assertTrue(carritos.contains(carrito));
        assertTrue(carritos.contains(carrito2));
        verify(carritoRepo, times(1)).findAll();
    }

    // Prueba que verifica que el metodo findById de Carrito Service retorne el carrito buscado por ID
    @Test
    public void findByIdRetornaCarritoTest(){
        // Arrange
        when(carritoRepo.findById(Long.valueOf(1))).thenReturn(Optional.of(carrito));

        // Act
        Carrito carrito = carritoService.findById(Long.valueOf(1));

        // Assert
        assertNotNull(carrito); // Verifica que retorne un carrito no nulo
        assertEquals(carrito.getId(), Long.valueOf(1)); // Verifica el carrito tenga el ID esperado 
        verify(carritoRepo, times(1)).findById(Long.valueOf(1)); // Verifica que se haya llamado al metodo findById de CarritoRepository
    }

    // Prueba que verifica que el metodo findById de CarritoService retorne null
    // si el carrito buscado no existe
    @Test
    public void findByIdRetornaEmptyTest(){
        // Arrange
        when(carritoRepo.findById(Long.valueOf(2))).thenReturn(Optional.empty());

        // Act
        Carrito carritoBuscado = carritoService.findById(Long.valueOf(2));

        // Assert
        assertNull(carritoBuscado);
        verify(carritoRepo, times(1)).findById(Long.valueOf(2));
    }

    @Test
    public void buscaCarritosPorUsuarioIdTest(){
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(Long.valueOf(10));
        
        carrito.setUsuario(usuario);

        Carrito carrito2 = new Carrito();
        carrito2.setUsuario(usuario);

        when(carritoRepo.findByUsuarioId(Long.valueOf(10)))
            .thenReturn(new ArrayList<Carrito>(List.of(carrito, carrito2)));

        // Act
        List<Carrito> carritos = carritoService.findByUsuarioId(Long.valueOf(10));

        // Assert
        assertEquals(carritos.size(), 2);
        assertTrue(carritos.contains(carrito));
        assertTrue(carritos.contains(carrito2));
        verify(carritoRepo, times(1)).findByUsuarioId(Long.valueOf(10));

    }

}
