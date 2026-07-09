package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.ProductoResponse;
import com.minimarket.minimarket.dto.UsuarioRequest;
import com.minimarket.minimarket.dto.UsuarioResponse;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.UsuarioRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(
    name = "Usuario", 
    description = "API para gestionar usuarios en base de datos y realizar consultas."
)
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRequestMapper requestMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(
        summary = "Listar todos los usuarios",
        description = "Retorna la lista completa de usuarios en la base de datos. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse[].class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
        }
    )
    public List<UsuarioResponse> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        return usuarios
            .stream()
            .map(UsuarioResponse :: new)
            .collect(Collectors.toList());
        
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar usuario por ID",
        description = "Busca un usuario en la base de datos por su ID y retorna sus datos."
            + " El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Usuario recuperado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404", description = "Usuario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(
        @Parameter(description = "ID del usuario buscado", required = true) @PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (!usuario.isEmpty()){
            UsuarioResponse usuarioResponse = new UsuarioResponse(usuario.get());
            return ResponseEntity.ok(usuarioResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(
        summary = "Guardar usuario",
        description = "Crea un usuario y lo guarda en la base de datos. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Usuario registrado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404", description = "Rol no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public UsuarioResponse guardarUsuario(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Usuario para guardar en base de datos", 
            required = true
        )
        @RequestBody UsuarioRequest request
    ) {
        sanitizarUsuario(request); //Sanitizar input de cliente
        Usuario usuario = requestMapper.toUsuario(request);
        usuario.setId(null);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Encriptar contrasena
        return new UsuarioResponse(usuarioService.save(usuario));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar datos de usuario",
        description = "Modifica los datos del usuario en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Usuario actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404", description = "Usuario no encontrado o rol no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
        @Parameter(description = "ID del usuario que se desea actualizar", required = true) @PathVariable Long id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Usuario con datos actualizados", 
            required = true
        )
        @RequestBody UsuarioRequest request) {
        sanitizarUsuario(request); //Sanitizar input de cliente
        Optional<Usuario> usuarioExistente = usuarioService.findById(id); // Verificar que usuario existe en base de datos
        if (usuarioExistente.isPresent()) {
            Usuario usuario = requestMapper.toUsuario(request);
            usuario.setId(id);
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Encriptar contrasena
            return ResponseEntity.ok(new UsuarioResponse(usuarioService.save(usuario)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina el usuario en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "Usuario eliminado exitosamente (No content)",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404", description = "Usuario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<Void> eliminarUsuario(
        @Parameter(description = "ID del usuario que se desea eliminar", required = true) @PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) { // Verifica si el usuario existe
            usuarioService.deleteById(id); // Elimina al usuario
            return ResponseEntity.noContent().build(); // Respuesta 204 (sin contenido)
        }
        return ResponseEntity.notFound().build(); // Respuesta 404 (no encontrado)
    }

    //Metodo que sanitiza los atributos de tipo String de un usuario, para evitar la insercion de scripts
    private void sanitizarUsuario(UsuarioRequest usuario){
        usuario.setPassword(sanitizeInput(usuario.getPassword()));
        usuario.setUsername(sanitizeInput(usuario.getUsername()));
    }
}
