package com.minimarket.minimarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.DetalleVentaRepository;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.repository.VentaRepository;
import com.minimarket.minimarket.service.impl.DetalleVentaServiceImpl;

@ExtendWith(MockitoExtension.class)
public class DetalleVentaServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleRepo;

    @Mock
    private ProductoRepository productoRepo;

    @Mock
    private VentaRepository ventaRepo;

    @InjectMocks
    private DetalleVentaServiceImpl detalleService;

    private Producto producto;
    private DetalleVenta detalle;

    @BeforeEach
    void SetUp(){
        producto = Producto.builder()
            .id(Long.valueOf(1))
            .nombre("Arroz")
            .categoria(new Categoria())
            .stock(10)
            .precio(2690.0)
            .build();

        detalle = DetalleVenta.builder()
            .id(Long.valueOf(1))
            .venta(new Venta())
            .producto(producto)
            .cantidad(5)
            .precio(1990.0)
            .build();
    }

    @AfterEach
    void tearDown(){
        producto = null;
        detalle = null;
    }

    // Prueba que verifica que findAll() retorne una lista con todos los detalles de venta
    @Test
    public void findAllRetornaTodosLosDetallesTest(){
        // Arrange
        DetalleVenta detalle1 = new DetalleVenta();
        DetalleVenta detalle2 = new DetalleVenta();
        when(detalleRepo.findAll()).thenReturn(List.of(detalle1, detalle2));

        List<DetalleVenta> detalles = detalleService.findAll();

         // Assert
        assertNotNull(detalles); // Verifica que retorne un objeto no null
        assertEquals(detalles.size(), 2); // Verifica que la lista de detalles incluya los dos detalles agregados 
        assertTrue(detalles.contains(detalle1));
        assertTrue(detalles.contains(detalle2));
        verify(detalleRepo).findAll(); // Verifica que se haya llamado al metodo findAll de DetalleVentaRepository
    }

    // Prueba que valida que findById retorne un detalle de venta buscado por su ID, si existe
    @Test
    public void findByIdRetornaDetalleSiExisteTest(){
        // Arrange
        when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.of(detalle));

        // Act
        DetalleVenta buscado = detalleService.findById(Long.valueOf(1));

        // Assert
        assertNotNull(buscado); // Se verifica que se retorne una venta
        assertEquals(Long.valueOf(1), buscado.getId()); // Se verifica que la venta retornada tenga el ID buscado
        assertEquals(buscado.getProducto(), producto); // verificar que el usuario sea correcto
        verify(detalleRepo).findById(Long.valueOf(1));

    }

    // Prueba que valida que findById retorne un Optional vacio si el detalle de venta buscado por ID no existe
    @Test
    public void findByIdRetornaEmptySiNoExisteTest(){
        // Arrange
        when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        DetalleVenta buscado = detalleService.findById(Long.valueOf(1));

        // Assert
        assertNull(buscado); // Se verifica que se retorne una venta
        verify(detalleRepo).findById(Long.valueOf(1));

    }

    // Prueba que valida que save() guarde un detalle de venta si hay stock suficiente del producto
    @Test
    public void saveGuardaDetalleSiHayStockTest(){
        // Arrange
        when(detalleRepo.save(any(DetalleVenta.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        DetalleVenta creado = detalleService.save(detalle);

        // Assert
        assertNotNull(creado);
        assertEquals(creado, detalle);
        assertEquals(creado.getId(), Long.valueOf(1));
        assertEquals(creado.getCantidad(), 5);
        assertEquals(creado.getProducto().getStock(), 5);
        verify(detalleRepo, times(1)).save(detalle);

    }

    // Prueba que valida que save() lance una excepcion si el producto asociado al detalle no tiene stock suficiente
    @Test
    public void saveLanzaExcepcionSiNoHayStockTest(){
        // Arrange
        detalle.setCantidad(200);

        // Act
        try{
            detalleService.save(detalle);
            fail("Se esperaba StockInsuficienteException");
        } catch(Exception e){
            assertEquals(e.getClass(), StockInsuficienteException.class);
            assertEquals(e.getMessage(), "No hay suficiente stock del producto: " + producto.getNombre());
        }
    }

    // Prueba que valida que deleteById() elimine un detalle de venta por ID, si existe
    @Test
    public void deleteByIdEliminaDetalleSiExisteTest(){
        // Arrange
        when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.of(detalle));

        // Act
        detalleService.deleteById(Long.valueOf(1));

        // Assert
        verify(detalleRepo, times(1)).deleteById(Long.valueOf(1));
        assertEquals(producto.getStock(), 15); // Verifica que se reponga el stock del producto

    }

    // Prueba que valida que deleteById() lance una excepcion si no existe un detalle con el ID ingresado
    @Test
    public void deleteByIdLanzaExcepcionSiNoExisteTest(){
        // Arrange
        when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        try{
            detalleService.deleteById(Long.valueOf(1));
            fail("Se esperaba ResourceNotFoundException");
        } catch (Exception e){
            assertEquals(e.getClass(), ResourceNotFoundException.class);
            assertEquals(e.getMessage(), "No existe el detalle de venta con ID: " + Long.valueOf(1));
        }
    }

    // Clase anidada para pruebas del metodo update(). Requieren un setUp() adicional
    @Nested
    class UpdateTests{
        private Producto productoOriginal;
        private DetalleVenta original;

        @BeforeEach
        void setUp(){
            productoOriginal = Producto.builder()
                .id(Long.valueOf(2))
                .nombre("Leche")
                .categoria(new Categoria())
                .stock(100)
                .precio(3990.0)
                .build();
            
            original = DetalleVenta.builder()
                .id(Long.valueOf(1))
                .venta(new Venta())
                .producto(productoOriginal)
                .cantidad(5)
                .precio(990.0)
                .build();
        }

        @AfterEach
        void tearDown(){
            productoOriginal = null;
            original = null;
        }

        // Prueba que valida que update() lance una excepcion si el detalle con el ID ingresado
        // no existe en la base de datos
        @Test
        public void updateLanzaExcepcionSiDetalleNoExiste(){
            // Arrange
            when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.empty());

            try {
                detalleService.update(detalle);
                fail("Se esperaba ResourceNotFoundException");
            } catch (Exception e){
                assertEquals(e.getClass(), ResourceNotFoundException.class);
                assertEquals(e.getMessage(), "No existe el detalle de venta con ID: " + detalle.getId());
            }
        }

        // Prueba que valida que update() actualice un detalle de venta cuando cambia solo la cantidad.
        // Debe actualizar tambien el stock del producto
        @Test
        public void updateCambiaSoloCantidadTest(){
            // Arrange
            original.setProducto(producto);
            original.setCantidad(3);
            when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.of(original));
            when(detalleRepo.save(any(DetalleVenta.class))).thenAnswer(invocation -> {
                return invocation.getArgument(0);
            });

            DetalleVenta actualizado = detalleService.update(detalle);

            assertEquals(actualizado, detalle);
            assertEquals(actualizado.getProducto(), producto);
            assertEquals(producto.getStock(), 8); // Se verifica el stock del producto
            verify(detalleRepo, times(1)).findById(detalle.getId());
            verify(detalleRepo, times(1)).save(detalle);

        }

        // Prueba que valida que update() actualice un detalle cuando cambia solo el producto asociado.
        // Valida que actualice el stock del producto original y del producto nuevo
        @Test
        public void updateCambiaSoloProductoTest(){
            // Arrange
            when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.of(original));
            when(detalleRepo.save(any(DetalleVenta.class))).thenAnswer(invocation -> {
                return invocation.getArgument(0);
            });
        
            // Act
            DetalleVenta actualizado = detalleService.update(detalle);

            // Assert
            assertEquals(actualizado, detalle);
            assertEquals(actualizado.getProducto(), producto);
            assertEquals(producto.getStock(), 5);
            assertEquals(productoOriginal.getStock(), 105);
            verify(productoRepo, times(1)).save(productoOriginal);
            verify(detalleRepo, times(1)).findById(detalle.getId());
            verify(detalleRepo, times(1)).save(detalle);

        }

        // Prueba que valida que update actualice un detalle cuando cambia el producto asociado y la cantidad.
        // Debe actualizar tambien los stocks del producto original y del producto nuevo
        @Test
        public void updateCambiaProductoYCantidadTest(){
            // Arrange
            original.setCantidad(3);
            when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.of(original));
            when(detalleRepo.save(any(DetalleVenta.class))).thenAnswer(invocation -> {
                return invocation.getArgument(0);
            });

            // Act
            DetalleVenta actualizado = detalleService.update(detalle);

            // Assert
            assertEquals(actualizado, detalle);
            assertEquals(actualizado.getProducto(), producto);
            assertEquals(producto.getStock(), 5);
            assertEquals(productoOriginal.getStock(), 103);
            verify(productoRepo, times(1)).save(productoOriginal);
            verify(detalleRepo, times(1)).findById(detalle.getId());
            verify(detalleRepo, times(1)).save(detalle);

        }

        // Prueba que valida que update() lance una excepcion si no hay stock del producto
        // suficiente para la nueva cantidad 
        @Test
        public void updateLanzaExcepcionSiNoHayStock(){
            when(detalleRepo.findById(any(Long.class))).thenReturn(Optional.of(original));
            detalle.setCantidad(1000);

            try {
                detalleService.update(detalle);
                fail("Se esperaba StockInsuficienteException");
            } catch(Exception e){
                assertEquals(e.getClass(), StockInsuficienteException.class);
                assertEquals(e.getMessage(), "No hay suficiente stock del producto: " + detalle.getProducto().getNombre());
            }
        }

        
    }

}
