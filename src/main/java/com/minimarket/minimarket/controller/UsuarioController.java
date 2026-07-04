package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.UsuarioResponse;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.minimarket.minimarket.security.util.InputSanitizer.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        return usuarios
            .stream()
            .map(UsuarioResponse :: new)
            .collect(Collectors.toList());
        
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        UsuarioResponse usuarioResponse;
        if (!usuario.isEmpty()){
            usuarioResponse = new UsuarioResponse(usuario.get());
            return ResponseEntity.ok(usuarioResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public UsuarioResponse guardarUsuario(@RequestBody Usuario usuario) {
        sanitizarUsuario(usuario); //Sanitizar input de cliente
        usuario.setId(null);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Encriptar contrasena
        return new UsuarioResponse(usuarioService.save(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        sanitizarUsuario(usuario); //Sanitizar input de cliente
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            usuario.setId(id);
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Encriptar contrasena
            return ResponseEntity.ok(new UsuarioResponse(usuarioService.save(usuario)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) { // Verifica si el usuario existe
            usuarioService.deleteById(id); // Elimina al usuario
            return ResponseEntity.noContent().build(); // Respuesta 204 (sin contenido)
        }
        return ResponseEntity.notFound().build(); // Respuesta 404 (no encontrado)
    }

    //Metodo que sanitiza los atributos de tipo String de un usuario, para evitar la insercion de scripts
    private void sanitizarUsuario(Usuario usuario){
        usuario.setPassword(sanitizeInput(usuario.getPassword()));
        usuario.setUsername(sanitizeInput(usuario.getUsername()));
    }
}
