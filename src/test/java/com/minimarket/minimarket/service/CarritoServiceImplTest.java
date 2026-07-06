package com.minimarket.minimarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.CarritoRepository;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.service.impl.CarritoServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepo;

    @Mock private ProductoRepository productoRepo;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Carrito carrito;
    private Producto producto;
    private Categoria categoria;
    private Usuario usuario;
    private Producto productoOriginal;
    private Carrito carritoOriginal;

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
        
        usuario = Usuario.builder()
            .id(Long.valueOf(1))
            .username("username")
            .password("password")
            .roles(new HashSet<Rol>())
            .build();
        
        carrito = Carrito.builder()
            .id(Long.valueOf(1))
            .usuario(usuario)
            .producto(producto)
            .cantidad(1)
            .build();

        productoOriginal = Producto.builder()
            .id(Long.valueOf(1))
            .nombre("Arroz")
            .precio(2690.0)
            .categoria(categoria)
            .stock(10)
            .build();

        carritoOriginal = Carrito.builder()
            .id(Long.valueOf(1))
            .usuario(usuario)
            .producto(producto)
            .cantidad(1)
            .build();
        
    }

    @AfterEach
    void tearDown(){
        carrito = null;
        usuario = null;
        producto = null;
        categoria = null;
        productoOriginal = null;
        carritoOriginal = null;
    }

    // Prueba que verifica que CarritoServiceImpl guarda un carrito correctamente 
    // El stock del producto debe ser mayor a la cantidad agregada
    @Test
    public void agregarCarritoValidoTest(){
        carrito.getProducto().setStock(10);
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
        carrito.getProducto().setStock(1);
        carrito.setCantidad(10);
        
        // Assert
        assertThrows(StockInsuficienteException.class, () -> {
            carritoService.save(carrito);  
        }, "Deberia lanzar StockInsuficienteException");
    }

    // Prueba que verifica que el metodo findAll() de CarritoServiceImp retorne una
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

    // Prueba que verifica que el metodo findAll() de CarritoServiceImpl retorne una lista vacia
    // si no hay carritos en la base de datos
    @Test
    public void findAllRetornaEmptyTest(){
        when(carritoRepo.findAll()).thenReturn(new ArrayList<Carrito>());

        List<Carrito> carritos = carritoService.findAll();

        assertEquals(carritos.size(), 0);
        assertTrue(carritos.isEmpty());
        verify(carritoRepo, times(1)).findAll();
    }

    // Prueba que verifica que el metodo findById de CarritoServiceImpl retorne el carrito buscado por ID
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

    // Prueba que verifica que el metodo findById de CarritoServiceImpl retorne null
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

    // Prueba que verifica que el metodo findByUsuarioId() de CarritoServiceImpl
    // retorne todos los carritos asociados a un ID de usuario
    @Test
    public void buscaCarritosPorUsuarioIdRetornaCarritosTest(){
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

    // Prueba que verifica que el metodo deleteById() de CarritoServiceImpl elimine
    // un producto por ID si existe. Tambien verifica que se restaure el stock del producto
    // asociado al carrito
    @Test
    public void deleteByIdEliminaCarritoSiExisteTest(){
        // Arrange
        when(carritoRepo.findById(any(Long.class))).thenReturn(Optional.of(carrito));
        when(productoRepo.save(any(Producto.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        carritoService.deleteById(Long.valueOf(1));

        // Assert
        verify(carritoRepo, times(1)).deleteById(Long.valueOf(1));
        verify(productoRepo, times(1)).save(carrito.getProducto());
        assertEquals(producto.getStock(), 11); // Verificar que se haya restaurado el stock del producto
    }

    // Prueba que verifica que el metodo deleteById de CarritoServiceImpl lance una
    // excepcion del tipo ResourceNotFound si el carrito con el ID ingresado no existe
    @Test
    public void deleteByIdLanzaExcepcionSiCarritoNoExisteTest(){
        // Arrange
        when(carritoRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            carritoService.deleteById(Long.valueOf(1));
            fail("Se esperaba ResourceNotFoundException");
        } catch(Exception e){
            assertEquals(e.getClass(), ResourceNotFoundException.class);
            assertEquals(e.getMessage(), "No existe el carrito con el ID ingresado"); // Verificar mensaje de excepcion
        } finally {
            verify(carritoRepo, times(1)).findById(Long.valueOf(1));
        }
    }

    // Verifica que el metodo update() de CarritoServiceImpl retorne el carrito actualizado
    // (que recibe como argumento). Para este test se considera que solo cambia el atributo cantidad
    // en el carrito, por lo que se debe verificar la correcta actualizacion del stock del producto
    @Test
    public void updateRetornaCarritoConMismoProductoTest(){
        carritoOriginal.setCantidad(5);
        when(carritoRepo.findById(any(Long.class))).thenReturn(Optional.of(carritoOriginal));
        when(carritoRepo.save(any(Carrito.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(productoRepo.findById(any(Long.class))).thenReturn(Optional.of(productoOriginal));
        when(productoRepo.save(any(Producto.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        Carrito actualizado = carritoService.update(carrito);

        verify(carritoRepo, times(1)).findById(carrito.getId());
        verify(productoRepo, times(1)).findById(carrito.getProducto().getId());
        verify(carritoRepo, times(1)).save(carrito);
        verify(productoRepo, times(2)).save(any(Producto.class));
        assertEquals(actualizado, carrito);
        assertEquals(producto.getStock(), 14); // Se verifica que se actualice el stock del producto
            
    }

    // Verifica que el metodo update() de CarritoServiceImpl retorne el carrito actualizado
    // (que recibe como argumento). Para este test se considera que cambia el producto asociado al carrito,
    // por lo que debe verificarse que se actualice el stock del producto viejo y del nuevo
    @Test
    public void updateRetornaCarritoConDiferenteProductoTest(){
        // Arrange
        productoOriginal = Producto.builder()
            .id(Long.valueOf(2))
            .nombre("Leche")
            .precio(3990.0)
            .stock(12)
            .categoria(categoria)
            .build();

        carritoOriginal.setProducto(productoOriginal);

        when(carritoRepo.findById(any(Long.class))).thenReturn(Optional.of(carritoOriginal));
        when(carritoRepo.save(any(Carrito.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(productoRepo.findById(Long.valueOf(1))).thenReturn(Optional.of(producto));
        when(productoRepo.save(any(Producto.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        Carrito actualizado = carritoService.update(carrito);

        // Assert
        assertEquals(actualizado, carrito); // Se verifica que retorne el producto actualizado
        assertEquals(actualizado.getProducto(), producto); // Se verifica que se haya cambiado correctamente el producto
        assertEquals(productoOriginal.getStock(), 13); // Se verifica que se haya restaurado el stock original del producto
        assertEquals(carrito.getProducto().getStock(), 9); // Se verifica que se haya actualizado el stock del nuevo producto
        verify(carritoRepo, times(1)).findById(carrito.getId());
        verify(productoRepo, times(1)).findById(carrito.getProducto().getId());
        verify(carritoRepo, times(1)).save(carrito);
        verify(productoRepo, times(1)).save(carrito.getProducto());
        verify(productoRepo, times(1)).save(productoOriginal);

    }

    // Prueba que verifica que el metodo update() de CarritoServiceImpl lance una
    // excepcion si no existe en base de datos un carrito con el ID del carrito entregado
    // como argumento
    @Test
    public void updateLanzaExcepcionSiCarritoNoExisteTest(){
        // Arrange
        when(carritoRepo.findById(any(Long.class))).thenReturn(Optional.empty());
        
        // Act
        try {
            carritoService.update(carrito);
            fail("Se esperaba ResourceNotFoundException");
        } catch (Exception e){
            assertEquals(e.getClass(), ResourceNotFoundException.class);
            assertEquals(e.getMessage(), "No existe el carrito con el ID ingresado");
        } finally {
            verify(carritoRepo, times(1)).findById(carrito.getId());
        }
    
    }

    // Prueba que verifica que el metodo update() de CarritoServiceImpl lance una
    // excepcion si no existe en base de datos un producto con el ID del producto asociado
    // al carrito entregado como argumento
    @Test
    public void updateLanzaExcepcionSiProductoNoExisteTest(){
        // Arrange
        when(carritoRepo.findById(any(Long.class))).thenReturn(Optional.of(carritoOriginal));
        when(productoRepo.findById(any(Long.class))).thenReturn(Optional.empty());
        
        // Act
        try {
            carritoService.update(carrito);
            fail("Se esperaba ResourceNotFoundException");
        } catch (Exception e){
            assertEquals(e.getClass(), ResourceNotFoundException.class);
            assertEquals(e.getMessage(), "No existe el producto con el ID ingresado");
        } finally {
            verify(carritoRepo, times(1)).findById(carrito.getId());
            verify(productoRepo, times(1)).findById(carrito.getProducto().getId());
        }
    
    }

}
