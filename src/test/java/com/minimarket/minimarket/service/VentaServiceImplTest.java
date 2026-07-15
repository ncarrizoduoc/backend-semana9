package com.minimarket.minimarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.repository.VentaRepository;
import com.minimarket.minimarket.service.impl.VentaServiceImpl;

@ExtendWith(MockitoExtension.class)
public class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepo;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Usuario usuario;
    private Producto producto;
    private DetalleVenta detalle;
    private Venta venta;

    @BeforeEach
    void setUp(){
        usuario = Usuario.builder()
            .id(Long.valueOf(1))
            .username("prueba")
            .password("prueba123")
            .roles(new HashSet<Rol>())
            .build();

        producto = Producto.builder()
            .id(Long.valueOf(1))
            .nombre("Arroz")
            .precio(2690.0)
            .stock(10)
            .categoria(new Categoria())
            .build();

        detalle = DetalleVenta.builder()
            .id(Long.valueOf(1))
            .venta(null)
            .producto(new Producto())
            .cantidad(10)
            .build();

        venta = Venta.builder()
            .id(Long.valueOf(1))
            .usuario(usuario)
            .fecha(new Date())
            .detalles(List.of(detalle))
            .build();

    }

    @AfterEach
    void tearDown(){
        usuario = null;
        producto = null;
        detalle = null;
        venta = null;
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
        when(ventaRepo.findById(any(Long.class))).thenReturn(Optional.of(venta));

        // Act
        Venta ventaBuscar = ventaService.findById(Long.valueOf(1));

        // Assert
        assertNotNull(ventaBuscar); // Se verifica que se retorne una venta
        assertEquals(Long.valueOf(1), ventaBuscar.getId()); // Se verifica que la venta retornada tenga el ID buscado
        assertEquals(ventaBuscar.getUsuario(), usuario); // verificar que el usuario sea correcto
        verify(ventaRepo).findById(Long.valueOf(1)); // Se verifica que se haya llamado al metodo findById de VentaRepository
    }

    // Verifica que VentaService retorne null si la venta buscada por ID no existe
    @Test
    public void findByIdRetornaNullSiNoExisteTest(){
        // Arrange
        when(ventaRepo.findById(Long.valueOf(1))).thenReturn(Optional.empty());

        // Act
        Venta ventaBuscar = ventaService.findById(Long.valueOf(1));

        // Assert
        assertNull(ventaBuscar);
    }

    // Metodo que verifica que el metodo findByUsuarioId retorne las ventas asociadas a un ID de usuario
    @Test
    public void findByUsuarioIdretornaVentasDeUsuarioTest(){
        // Arrange
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
        when(ventaRepo.save(any(Venta.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Venta ventaGuardada = ventaService.save(venta);

        // Assert
        assertEquals(venta, ventaGuardada); // Se verifica que la venta creada sea igual la entregada como argumento
        verify(ventaRepo).save(venta); // Se verifica que se haya llamado al metodo save de VentaRepository

        // Assert que los datos obligatorios del usuario se guarden correctamente
        assertNotNull(venta.getUsuario());
        assertEquals(usuario.getId(), Long.valueOf(1));
        assertEquals(usuario.getUsername(), "prueba");
        assertEquals(usuario.getPassword(), "prueba123");
    }

}
