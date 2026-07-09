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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.exception.TipoMovimientoNoValidoException;
import com.minimarket.minimarket.repository.InventarioRepository;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.service.impl.InventarioServiceImpl;

@ExtendWith(MockitoExtension.class)
public class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepo;

    @Mock
    private ProductoRepository productoRepo;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Categoria categoria;
    private Producto producto;
    private Inventario inventario;

    @BeforeEach
    void setUp(){
        categoria = Categoria.builder()
            .id(Long.valueOf(1))
            .nombre("Abarrotes")
            .productos(new ArrayList<Producto>())
            .build();
        
        producto = Producto.builder()
            .id(Long.valueOf(1))
            .nombre("Arroz")
            .precio(2690.0)
            .stock(10)
            .categoria(categoria)
            .build();

        inventario = Inventario.builder()
            .id(Long.valueOf(1))
            .producto(producto)
            .cantidad(5)
            .tipoMovimiento("Entrada")
            .fechaMovimiento(new Date())
            .build();
    }

    // Prueba que verifica que al guardar un inventario, se retorne el inventario
    // con sus todos sus datos. Se considera un tipo de movimiento de Entrada
    @Test
    public void agregarInventarioEntradaRetornaInventarioTest(){
        // Arrange
        when(inventarioRepo.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Inventario guardado = inventarioService.save(inventario);

        // Assert
        assertNotNull(guardado); //Verifica que retorne un objeto no null
        assertEquals(guardado.getId(), Long.valueOf(1)); // Verifica que el ID del inventario sea correcto
        assertEquals(guardado.getProducto(), producto); // Verifica que el producto del inventario sea correcto
        assertEquals(guardado.getCantidad(), 5); // Verifica que la cantidad del inventario sea correcto
        assertEquals(guardado.getTipoMovimiento(), "Entrada"); // Verifica que el tipo de movimiento sea correcto
        assertEquals(guardado.getFechaMovimiento(), inventario.getFechaMovimiento()); // Verificar que la fecha sea correcta
        assertEquals(guardado.getProducto().getStock(), 15); // Verificar que el stock de producto se actualizo
        verify(inventarioRepo, times(1)).save(inventario);
    }

    // Prueba que verifica que al guardar un inventario, se retorne el inventario
    // con sus todos sus datos. Se considera un tipo de movimiento de Salida
    @Test
    public void agregarInventarioSalidaRetornaInventarioTest(){
        inventario.setTipoMovimiento("Salida");

        // Arrange
        when(inventarioRepo.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Inventario guardado = inventarioService.save(inventario);

        // Assert
        assertNotNull(guardado); //Verifica que retorne un objeto no null
        assertEquals(guardado.getId(), Long.valueOf(1)); // Verifica que el ID del inventario sea correcto
        assertEquals(guardado.getProducto(), producto); // Verifica que el producto del inventario sea correcto
        assertEquals(guardado.getCantidad(), 5); // Verifica que la cantidad del inventario sea correcto
        assertEquals(guardado.getTipoMovimiento(), "Salida"); // Verifica que el tipo de movimiento sea correcto
        assertEquals(guardado.getFechaMovimiento(), inventario.getFechaMovimiento()); // Verificar que la fecha sea correcta
        assertEquals(guardado.getProducto().getStock(), 5); // Verificar que el stock de producto se actualizo
        verify(inventarioRepo, times(1)).save(inventario);
    }

    // Prueba que verifica que al guardar un inventario, se retorne el inventario
    // con sus todos sus datos. Se considera un tipo de movimiento de Salida
    @Test
    public void agregarInventarioConStockInsuficienteLanzaExcepcionTest(){
        inventario.setTipoMovimiento("Salida");
        inventario.setCantidad(100);

        // Arrange
        try {
            inventarioService.save(inventario);
            fail("Se esperaba StockInsuficienteException");
        } catch (Exception e){
            assertEquals(e.getClass(), StockInsuficienteException.class);
            assertEquals(e.getMessage(), "Error al registrar movimiento de inventario: No hay stock suficiente del producto: "
                + "Arroz");
        }
    }

    // Prueba que verifica que al guardar un inventario, se retorne el inventario
    // con sus todos sus datos. Se considera un tipo de movimiento de Salida
    @Test
    public void agregarInventarioConMovimientoInvalidoLanzaExcepcionTest(){
        inventario.setTipoMovimiento("No valido");

        try {
            inventarioService.save(inventario);
            fail("Se esperaba TipoMovimientoNoValidoException");
        } catch (Exception e){
            assertEquals(e.getClass(), TipoMovimientoNoValidoException.class);
            assertEquals(e.getMessage(), "Error al guardar: El tipo de movimiento no es valido");
        }
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
        when(inventarioRepo.findById(Long.valueOf(1))).thenReturn(Optional.of(inventario));

        // Act
        Inventario buscado = inventarioService.findById(Long.valueOf(1));

        // Assert
        assertNotNull(buscado); // Se verifica que el inventario retornado no sea null
        assertEquals(buscado, inventario); // Se verifica que el inventario retornado y el esperado sean el mismo
        assertEquals(buscado.getId(), Long.valueOf(1)); // Se verifica que los atributos del inventario sean los esperados
        assertEquals(buscado.getCantidad(), 5);
        verify(inventarioRepo, times(1))
            .findById(Long.valueOf(1)); // Se verifica llamada a metodo findById de InventarioRepository
    }

    // Prueba que verifica que al buscar usuario por ID, si no existe, retorne null
    @Test
    public void findByIdRetornaNullSiNoExisteTest(){
        // Arrange
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        Inventario buscado = inventarioService.findById(Long.valueOf(1));

        assertNull(buscado); // Se verifica que retorne un null
        verify(inventarioRepo, times(1))
            .findById(Long.valueOf(1)); // Se verifica llamada a metodo findById de InventarioRepository
    }

    // Prueba que verifica que al buscar inventarios por ID de producto, se retornen los inventarios esperados
    @Test
    public void findByProductoIdRetornaInventariosTest(){
        // Arrange
        Inventario inventario2 = Inventario.builder()
            .id(Long.valueOf(1))
            .producto(producto)
            .cantidad(3)
            .tipoMovimiento("Salida")
            .fechaMovimiento(new Date())
            .build();

        when(inventarioRepo.findByProductoId(Long.valueOf(1)))
            .thenReturn(new ArrayList<Inventario>(List.of(inventario, inventario2)));

        // Act
        List<Inventario> lista = inventarioService.findByProductoId(Long.valueOf(1));

        // Assert
        assertEquals(lista.size(), 2);
        assertTrue(lista.contains(inventario));
        assertTrue(lista.contains(inventario2));
        assertEquals(lista.get(0).getProducto().getId(), Long.valueOf(1));
        assertEquals(lista.get(1).getProducto().getId(), Long.valueOf(1));
        verify(inventarioRepo, times(1))
            .findByProductoId(Long.valueOf(1));
    }

    // Prueba que verifica que el metodo deleteById() elimine correctamente un
    // inventario con tipo de movimiento "Entrada" y que actualice el stock del producto
    // asociado al inventario eliminado
    @Test
    public void deleteByIdEliminaInventarioEntradaTest(){
        // Arrange
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.of(inventario));
        when(productoRepo.save(any(Producto.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        inventarioService.deleteById(Long.valueOf(1));

        // Assert
        assertEquals(inventario.getProducto().getStock(), 5); // Se verifica que se actualice el stock del producto
        verify(productoRepo, times(1)).save(inventario.getProducto());
        verify(inventarioRepo, times(1)).deleteById(Long.valueOf(1));

    }

    // Prueba que verifica que el metodo deleteById() elimine correctamente un
    // inventario con tipo de movimiento "Salida" y que actualice el stock del producto
    // asociado al inventario eliminado
    @Test
    public void deleteByIdEliminaInventarioSalidaTest(){
        // Arrange
        inventario.setTipoMovimiento("Salida");
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.of(inventario));
        when(productoRepo.save(any(Producto.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        inventarioService.deleteById(Long.valueOf(1));

        // Assert
        assertEquals(inventario.getProducto().getStock(), 15); // Se verifica que se actualice el stock del producto
        verify(productoRepo, times(1)).save(inventario.getProducto());
        verify(inventarioRepo, times(1)).deleteById(Long.valueOf(1));

    }

    // Prueba que verifica que el metodo deleteById() lance una excepcion
    // si el inventario con ID ingresado no existe
    @Test
    public void deleteByIdLanzaExcepcionSiInventarioNoExisteTest(){
        // Arrange
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act y Assert
        try {
            inventarioService.deleteById(Long.valueOf(1));
            fail("Se esperaba ResourceNotFoundException");
        } catch (Exception e){
            assertEquals(e.getClass(), ResourceNotFoundException.class);
            assertEquals(e.getMessage(), "No existe el inventario con el ID ingresado");
        }
    }

    // Prueba que verifica que el metodo deleteById() lance una excepcion
    // si al eliminar el inventario y actualizar el stock del producto asociado,
    // el producto resultaria con stock negativo (no valido)
    @Test
    public void deleteByIdLanzaExcepcionSiNoHayStockTest(){
        // Arrange
        inventario.setCantidad(100);
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.of(inventario));

        // Act y Assert
        try {
            inventarioService.deleteById(Long.valueOf(1));
            fail("Se esperaba StockInsuficienteException");
        } catch (Exception e){
            assertEquals(e.getClass(), StockInsuficienteException.class);
            assertEquals(e.getMessage(), "Error al registrar movimiento de inventario: No hay stock suficiente del producto: "
                + "Arroz");
        }
    }

    // Prueba que verifica que el metodo update() actualice un Inventario correctamente
    // cuando solo se modifica la fecha del inventario (no modifica stock de producto)
    @Test
    public void updateSoloActualizaFechaTest(){
        Date fecha = new Date();
        ZonedDateTime zonedDateTime = fecha.toInstant().atZone(ZoneId.systemDefault());
        ZonedDateTime fechaPosterior = zonedDateTime.plusDays(5);
        Date fechaOriginal = Date.from(fechaPosterior.toInstant());

        // Arrange
        Inventario inventarioOriginal = Inventario.builder()
            .id(Long.valueOf(1))
            .producto(producto)
            .cantidad(5)
            .tipoMovimiento("Entrada")
            .fechaMovimiento(fechaOriginal)
            .build();
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.of(inventarioOriginal));
        when(inventarioRepo.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Inventario actualizado = inventarioService.update(inventario);

        // Assert
        assertNotNull(actualizado);
        assertEquals(actualizado.getProducto(), producto);
        assertEquals(actualizado.getId(), inventario.getId());
        assertEquals(actualizado.getFechaMovimiento(), inventario.getFechaMovimiento());
        verify(inventarioRepo, times(1)).save(inventario);
    }

    // Metodo que verifica que update() lanza una excepcion si el ID del inventario recibido
    // no corresponde a un inventario guardado en base de datos
    @Test
    public void updateLanzaExcepcionSiInventarioNoExisteTest(){
        // Arrange
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            inventarioService.update(inventario);
            fail("Se esperaba ResourceNotFoundException");
        } catch (Exception e){
            assertEquals(e.getClass(), ResourceNotFoundException.class);
            assertEquals(e.getMessage(), "No existe el inventario con el ID ingresado");
            verify(inventarioRepo, times(1)).findById(inventario.getId());
        }
    }

    // Metodo que valida que update() actualice un Inventario y el stock de su
    // producto asociado si solo cambia la cantidad de producto
    // Para tipo de movimiento "Entrada"
    @Test
    public void updateActualizaStockDeProductoSiCambiaCantidadEntradaTest(){
        // Arrange
        Inventario inventarioOriginal = Inventario.builder()
            .id(Long.valueOf(1))
            .producto(producto)
            .cantidad(2)
            .tipoMovimiento("Entrada")
            .fechaMovimiento(inventario.getFechaMovimiento())
            .build();
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.of(inventarioOriginal));
        when(inventarioRepo.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Inventario actualizado = inventarioService.update(inventario);

        // Assert
        assertNotNull(actualizado);
        assertEquals(actualizado, inventario);
        assertEquals(actualizado.getProducto().getStock(), 13);
        verify(productoRepo, times(1)).save(inventarioOriginal.getProducto());
        verify(inventarioRepo, times(1)).save(inventario);

    }

    // Metodo que valida que update() actualice un Inventario y el stock de su
    // producto asociado si solo cambia la cantidad de producto
    // Para tipo de movimiento "Salida"
    @Test
    public void updateActualizaStockDeProductoSiCambiaCantidadSalidaTest(){
        inventario.setTipoMovimiento("Salida");

        // Arrange
        Inventario inventarioOriginal = Inventario.builder()
            .id(Long.valueOf(1))
            .producto(producto)
            .cantidad(2)
            .tipoMovimiento("Salida")
            .fechaMovimiento(inventario.getFechaMovimiento())
            .build();
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.of(inventarioOriginal));
        when(inventarioRepo.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Inventario actualizado = inventarioService.update(inventario);

        // Assert
        assertNotNull(actualizado);
        assertEquals(actualizado, inventario);
        assertEquals(actualizado.getProducto().getStock(), 7);
        verify(productoRepo, times(1)).save(inventarioOriginal.getProducto());
        verify(inventarioRepo, times(1)).save(inventario);

    }

    // Metodo que valida que update() actualice un Inventario y el stock de el nuevo
    // producto asociado, asi como del producto original, si se modifica el producto asociado
    @Test
    public void updateActualizaAmbosProductosSiCambiaProductoTest(){
        // Arrange
        Producto productoOriginal = Producto.builder()
            .id(Long.valueOf(2))
            .nombre("Leche")
            .precio(3990.0)
            .stock(200)
            .categoria(categoria)
            .build();
        
        Inventario inventarioOriginal = Inventario.builder()
            .id(Long.valueOf(1))
            .producto(productoOriginal)
            .cantidad(5)
            .tipoMovimiento("Entrada")
            .fechaMovimiento(inventario.getFechaMovimiento())
            .build();

        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.of(inventarioOriginal));
        when(inventarioRepo.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Inventario actualizado = inventarioService.update(inventario);

        // Assert
        assertNotNull(actualizado);
        assertEquals(actualizado, inventario);
        assertEquals(actualizado.getProducto(), producto);
        assertEquals(actualizado.getProducto().getStock(), 15); // Verificar que se actualice el stock del producto original
        assertEquals(productoOriginal.getStock(), 195); // Verificar que se actualice el stock del producto nuevo
        verify(inventarioRepo, times(1)).findById(inventario.getId());
        verify(inventarioRepo, times(1)).save(inventario);

    }

    // Metodo que valida que update() actualice un Inventario y el stock del 
    // producto asociado, si solo cambia el tipo de movimiento (Entrada o Salida)
    @Test
    public void updateActualizaStockSiCambiaTipoMovimientoTest(){
        // Arrange
        Inventario inventarioOriginal = Inventario.builder()
            .id(Long.valueOf(1))
            .producto(producto)
            .cantidad(5)
            .tipoMovimiento("Salida")
            .fechaMovimiento(inventario.getFechaMovimiento())
            .build();
        when(inventarioRepo.findById(any(Long.class))).thenReturn(Optional.of(inventarioOriginal));
        when(inventarioRepo.save(any(Inventario.class))).thenAnswer(invocation ->{
            return invocation.getArgument(0);
        });

        // Act
        Inventario actualizado = inventarioService.update(inventario);

        // Assert
        assertNotNull(actualizado);
        assertEquals(actualizado, inventario);
        assertEquals(actualizado.getProducto(), producto);
        assertEquals(actualizado.getProducto().getStock(), 20);
        verify(productoRepo, times(1)).save(producto);
        verify(inventarioRepo, times(1)).findById(inventario.getId());
        verify(inventarioRepo, times(1)).save(inventario);
    }

}
