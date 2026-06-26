package com.minimarket.minimarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.VentaRepository;
import com.minimarket.minimarket.service.impl.VentaServiceImpl;

@ExtendWith(MockitoExtension.class)
public class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepo;

    @InjectMocks
    private VentaServiceImpl ventaService;

    // Prueba que verifica que se calcule correctamente el total de una venta con detalles de venta
    @Test
    public void calculaTotalCorrectamenteTest(){
        // Arrange
        Venta venta = new Venta();
        DetalleVenta detalle1 = new DetalleVenta();
        detalle1.setPrecio(Double.valueOf(500));
        detalle1.setCantidad(3);

        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setPrecio(Double.valueOf(2000));
        detalle2.setCantidad(5);

        venta.setDetalles(List.of(detalle1, detalle2));

        Double totalEsperado = Double.valueOf((500 * 3) + (2000 * 5));

        // Act
        Double total = ventaService.calcularTotal(venta);

        // Assert
        assertEquals(total, totalEsperado);

    }

    // Prueba que verifica que el calculo del total de una venta sin detalles de venta sea igual a 0
    @Test
    public void totalVentaRetornaCeroSinProductosTest(){
        // Arrange
        Venta venta = new Venta();
        venta.setDetalles(new ArrayList<DetalleVenta>());

        // Act
        Double total = ventaService.calcularTotal(venta);

        // Assert
        assertEquals(total, Double.valueOf(0));
    }

    // Metodo que verifica que el metodo findAll retorna una lista con todas las ventas en la base de datos
    @Test
    public void findAllRetornaTodasLasVentasTest(){
        // Arrange
        Venta venta1 = new Venta();
        Venta venta2 = new Venta();
        when(ventaRepo.findAll()).thenReturn(new ArrayList<Venta>(List.of(venta1, venta2)));

        // Act
        List<Venta> ventas = ventaService.findAll();

        // Assert
        assertNotNull(ventas); // Verifica que retorne un objeto no null
        assertEquals(ventas.size(), 2); // Verifica que la lista de ventas incluya las dos ventas agregadas 
        assertTrue(ventas.contains(venta1));
        assertTrue(ventas.contains(venta2));
        verify(ventaRepo).findAll(); // Verifica que se haya llamado al metodo findAll de VentaRepository
    }

    // Verifica que al buscar una venta por ID retorne la venta con el ID buscado
    @Test
    public void findByIdretornaVentaPorIdTest(){
        // Arrange
        Venta venta = new Venta();
        Long id = Long.valueOf(1);
        venta.setId(id);
        when(ventaRepo.findById(Long.valueOf(1))).thenReturn(Optional.of(venta));

        // Act
        Venta ventaBuscar = ventaService.findById(Long.valueOf(1));

        // Assert
        assertNotNull(ventaBuscar); // Se verifica que se retorne una venta
        assertEquals(Long.valueOf(1), ventaBuscar.getId()); // Se verifica que la venta retornada tenga el ID buscado
        verify(ventaRepo).findById(Long.valueOf(1)); // Se verifica que se haya llamado al metodo findById de VentaRepository
    }

    // Verifica que VentaService retorne null si el usuario buscado por ID no existe
    @Test
    public void findByIdRetornaNullSiNoExisteTest(){
        // Arrange
        when(ventaRepo.findById(Long.valueOf(1))).thenReturn(Optional.empty());

        // Act
        Venta venta = ventaService.findById(Long.valueOf(1));

        // Assert
        assertNull(venta);
    }

    // Metodo que verifica que el metodo findByUsuarioId retorne las ventas asociadas a un ID de usuario
    @Test
    public void findByUsuarioIdretornaVentasDeUsuarioTest(){
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(Long.valueOf(1));
        Venta venta1 = new Venta();
        venta1.setUsuario(usuario);
        Venta venta2 = new Venta();
        venta2.setUsuario(usuario);
        when(ventaRepo.findByUsuarioId(Long.valueOf(1))).thenReturn(List.of(venta1, venta2));

        // Act
        List<Venta> ventas = ventaService.findByUsuarioId(Long.valueOf(1));

        // Assert
        assertEquals(ventas.size(), 2); // Se verifica que la lista contenga 2 ventas
        assertTrue(ventas.contains(venta1)); // Se verifica que la lista contenga las ventas esperadas
        assertTrue(ventas.contains(venta2));
        verify(ventaRepo).findByUsuarioId(Long.valueOf(1)); // Se verifica que se haya llamado al metodo findByUsuarioId de VentaRepository

    }

    @Test
    public void saveVentaTest(){
        // Arrange
        Venta venta = new Venta(); // Crear venta con sus datos
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        producto.setStock(10);

        DetalleVenta detalle = new DetalleVenta(); //Crear detalle de venta con sus datos
        detalle.setProducto(producto);
        detalle.setCantidad(9);

        Usuario usuario = new Usuario(); //Crear usuario con sus datos
        usuario.setId(Long.valueOf(1));
        usuario.setUsername("username");
        usuario.setPassword("password");

        venta.setDetalles(List.of(detalle));
        venta.setUsuario(usuario);

        when(ventaRepo.save(venta)).thenReturn(venta);

        // Act
        Venta ventaGuardada = ventaService.save(venta);

        // Assert
        assertEquals(venta, ventaGuardada); // Se verifica que la venta creada sea igual la entregada como argumento
        verify(ventaRepo).save(venta); // Se verifica que se haya llamado al metodo save de VentaRepository

        // Assert que los datos obligatorios del usuario se guarden correctamente
        assertNotNull(venta.getUsuario());
        assertEquals(usuario.getId(), Long.valueOf(1));
        assertEquals(usuario.getUsername(), "username");
        assertEquals(usuario.getPassword(), "password");
    }

    @Test
    public void lanzaExcepcionCuandoStockInsuficienteTest(){
        // Arrange
        Venta venta = new Venta();
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        producto.setStock(10);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(12);

        venta.setDetalles(List.of(detalle));

        // Assert
        // Se verifica que se lance una excepcion si se intenta guardar
        // un producto con stock insuficiente en una venta
        assertThrows(StockInsuficienteException.class, () -> {
            ventaService.save(venta);
        }, "Deberia lanzar una StockInsuficienteException");

    }


}
