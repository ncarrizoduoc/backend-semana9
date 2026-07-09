package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.UsuarioRequest;
import com.minimarket.minimarket.dto.UsuarioResponse;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.UsuarioRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse[].class)),
            links = {
                @Link(name = "self", description = "Enlace a datos de usuario", operationId = "obtenerUsuarioPorId"),
                @Link(name = "actualizarUsuario", description = "Enlace a actualizacion de usuario", operationId = "actualizarUsuario"),
                @Link(name = "eliminarUsuario", description = "Enlace a eliminacion del usuario", operationId = "eliminarUsuario")
            }
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
    public ResponseEntity<List<EntityModel<UsuarioResponse>>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();

        // Se guarda cada usuario de la lista en una lista con links asociados
        List<EntityModel<UsuarioResponse>> lista = new ArrayList<>();
        for(Usuario usuario:usuarios){
            lista.add(EntityModel.of(new UsuarioResponse(usuario),
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).actualizarUsuario(usuario.getId(), new UsuarioRequest())).withRel("actualizarUsuario"),
                linkTo(methodOn(UsuarioController.class).eliminarUsuario(usuario.getId())).withRel("eliminarUsuario")
            )
        );
        }

        return ResponseEntity.ok(lista);
        
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class)),
            links = {
                @Link(name = "self", description = "Enlace a datos del usuario buscado", operationId = "obtenerUsuarioPorId"),
                @Link(name = "listarUsuarios", description = "Enlace a la lista de todos los usuarios", operationId = "listarUsuarios"),
                @Link(name = "actualizarUsuario", description = "Enlace a actualizacion de usuario buscado", operationId = "actualizarUsuario"),
                @Link(name = "eliminarUsuario", description = "Enlace a eliminacion del usuario buscado", operationId = "eliminarUsuario")
            }
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
    public ResponseEntity<EntityModel<UsuarioResponse>> obtenerUsuarioPorId(
        @Parameter(description = "ID del usuario buscado", required = true) @PathVariable Long id) {
        Optional<Usuario> buscado = usuarioService.findById(id);
        if (!buscado.isEmpty()){
            Usuario usuario = buscado.get();
            UsuarioResponse usuarioResponse = new UsuarioResponse(usuario);

            EntityModel<UsuarioResponse> response = EntityModel.of(usuarioResponse,
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(id)).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("listarUsuarios"),
                linkTo(methodOn(UsuarioController.class).actualizarUsuario(id, new UsuarioRequest())).withRel("actualizarUsuario"),
                linkTo(methodOn(UsuarioController.class).eliminarUsuario(id)).withRel("eliminarUsuario")
            );
            
            return ResponseEntity.ok(response);
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class)),
            links = {
                @Link(name = "self", description = "Enlace a datos del usuario creado", operationId = "obtenerUsuarioPorId"),
                @Link(name = "listarUsuarios", description = "Enlace a la lista de todos los usuarios", operationId = "listarUsuarios"),
                @Link(name = "actualizarUsuario", description = "Enlace a actualizacion de usuario creado", operationId = "actualizarUsuario"),
                @Link(name = "eliminarUsuario", description = "Enlace a eliminacion del usuario creado", operationId = "eliminarUsuario")
            }
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
    public ResponseEntity<EntityModel<UsuarioResponse>> guardarUsuario(
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
        
        // Guardar usuario
        Usuario creado = usuarioService.save(usuario);
        UsuarioResponse response =  new UsuarioResponse(creado);

        // Generar EntityModel con links
        EntityModel<UsuarioResponse> usuarioModel = EntityModel.of(response,
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(creado.getId())).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("listarUsuarios"),
            linkTo(methodOn(UsuarioController.class).actualizarUsuario(creado.getId(), new UsuarioRequest())).withRel("actualizarUsuario"),
            linkTo(methodOn(UsuarioController.class).eliminarUsuario(creado.getId())).withRel("eliminarUsuario")
        );

        return ResponseEntity.ok(usuarioModel);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar datos de usuario",
        description = "Modifica los datos del usuario en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Usuario actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class)),
            links = {
                @Link(name = "self", description = "Enlace a datos del usuario actualizado", operationId = "obtenerUsuarioPorId"),
                @Link(name = "listarUsuarios", description = "Enlace a la lista de todos los usuarios", operationId = "listarUsuarios"),
                @Link(name = "eliminarUsuario", description = "Enlace a eliminacion del usuario actualizado", operationId = "eliminarUsuario")
            }
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
    public ResponseEntity<EntityModel<UsuarioResponse>> actualizarUsuario(
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
            
            // Actualizar usuario
            Usuario actualizado = usuarioService.save(usuario);
            UsuarioResponse response = new UsuarioResponse(actualizado);

            // Generar EntityModel con links
            EntityModel<UsuarioResponse> usuarioModel = EntityModel.of(response,
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(id)).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("listarUsuarios"),
                linkTo(methodOn(UsuarioController.class).eliminarUsuario(id)).withRel("eliminarUsuario")
            );

            return ResponseEntity.ok(usuarioModel);
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
            responseCode = "200", description = "Usuario eliminado exitosamente (No content)",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true)),
            links = {
                @Link(name = "listarUsuarios", description = "Enlace a lista con todos los usuarios", operationId = "listarUsuarios"),
                @Link(name = "guardarUsuario", description = "Enlace para crear nuevo usuario", operationId = "guardarUsuario")
            }
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
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarUsuario(
        @Parameter(description = "ID del usuario que se desea eliminar", required = true) @PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) { // Verifica si el usuario existe
            usuarioService.deleteById(id); // Elimina al usuario
            
            EntityModel<Map<String, String>> responseModel = EntityModel.of(
                Map.of("message", "Usuario eliminado exitosamente"),
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("listarUsuarios"),
                linkTo(methodOn(UsuarioController.class).guardarUsuario(new UsuarioRequest())).withRel("guardarUsuario")
            );

            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build(); // Respuesta 404 (no encontrado)
    }

    //Metodo que sanitiza los atributos de tipo String de un usuario, para evitar la insercion de scripts
    private void sanitizarUsuario(UsuarioRequest usuario){
        usuario.setPassword(sanitizeInput(usuario.getPassword()));
        usuario.setUsername(sanitizeInput(usuario.getUsername()));
    }
}
