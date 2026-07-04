package com.minimarket.minimarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.repository.UsuarioRepository;
import com.minimarket.minimarket.service.impl.UsuarioServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    void setUp(){
        rol = Rol.builder()
            .id(Long.valueOf(1))
            .nombre("CLIENTE")
            .usuarios(new HashSet<Usuario>())
            .build();

        usuario = Usuario.builder()
            .id(Long.valueOf(1))
            .username("usernamePrueba")
            .password("passwordPrueba")
            .roles(Set.of(rol))
            .build();
    }

    @AfterEach
    void tearDown(){
        usuario = null;
        rol = null;
    }

    // Metodo que verifica que se guarde un usuario
    // Se espera que UsuarioService retorne el mismo usuario que se guardo, con sus datos
    @Test
    public void guardaUsuarioTest(){
        // Arrange
        when(usuarioRepo.save(usuario)).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        //Act
        Usuario usuarioCreado = usuarioService.save(usuario);

        // Assert
        assertNotNull(usuarioCreado); // Se verifica que el usuario retornado no sea null
        assertEquals(usuarioCreado.getUsername(), "usernamePrueba"); // Se verifica que el nombre de usuario sea correcto
        assertEquals(usuarioCreado.getPassword(), "passwordPrueba"); // Se verifica que la contrasena sea correcta
        assertEquals(usuarioCreado.getRoles().size(), 1); // Se verifica que el numero de roles sea correcto
        verify(usuarioRepo).save(usuario); // Se verifica que se haya llamado al metodo save de UsuarioRepository
    }

    // Verifica que el metodo findAll retorne todos los usuarios en la base de datos
    @Test
    public void findAllRetornaTodosUsuariosTest(){
        // Arrange
        Usuario usuario2 = new Usuario();
        usuario2.setId(Long.valueOf(2));

        when(usuarioRepo.findAll()).thenReturn(List.of(usuario, usuario2));

        // Act
        List<Usuario> usuarios = usuarioService.findAll();

        // Assert
        assertEquals(usuarios.size(), 2); // Verifica que el largo de la venta de listas sea 2
        assertTrue(usuarios.contains(usuario)); // Verifica que la contenga las ventas guardadas
        assertTrue(usuarios.contains(usuario2));
        verify(usuarioRepo).findAll(); // Verifica que se haya llamado al metodo findAll de UsuarioRepository

    }

    // Verifica que findById retorna un usuario buscado por ID
    @Test
    public void findByIdRetornaUsuarioTest(){
        // Arrange
        when(usuarioRepo.findById(Long.valueOf(1))).thenReturn(Optional.of(usuario));

        // Act
        Usuario usuarioBuscar = usuarioService.findById(Long.valueOf(1)).get();

        // Assert
        assertNotNull(usuarioBuscar); // Verifica que el usuario retornado no sea null
        assertEquals(Long.valueOf(1), usuarioBuscar.getId()); // Verifica que el ID del usuario retornado sea correcto
        assertEquals(usuario.getUsername(), "usernamePrueba"); // Verifica que el nombre de usuario retornado sea correcto
        verify(usuarioRepo).findById(Long.valueOf(1)); // Verifica que se haya llamado al metodo findById de UsuarioRepository
    }

    // Verifica que findById retorna un Optional empty si el usuario buscado por ID no existe
    @Test
    public void findByIdRetornaEmptySiNoExisteTest(){
        // Arrange
        when(usuarioRepo.findById(Long.valueOf(1))).thenReturn(Optional.empty());

        // Act
        Optional<Usuario> usuarioBuscar = usuarioService.findById(Long.valueOf(1));

        // Assert
        assertEquals(usuarioBuscar, Optional.empty()); // Verifica que retorne un Optional empty
        verify(usuarioRepo).findById(Long.valueOf(1)); // Verifica que se haya llamado al metodo findById de UsuarioRepository

    }

    // Verifica que deleteById llame al metodo deleteById de UsuarioRepository 
    @Test
    public void deleteByIdBorraUsuarioTest(){
        // Act
        usuarioService.deleteById(Long.valueOf(1));

        // Assert
        verify(usuarioRepo).deleteById(Long.valueOf(1));

    }


}
