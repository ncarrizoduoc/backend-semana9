package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.CategoriaRequest;
import com.minimarket.minimarket.dto.CategoriaResponse;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minimarket.minimarket.security.util.InputSanitizer.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categoria", description = "API para gestionar categorias en base de datos y realizar consultas.")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @Operation(
        summary = "Listar todas las categorias",
        description = "Retorna la lista completa de categorias en la base de datos. El acceso es público.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Lista de categorias obtenida exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse[].class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de categoria", operationId = "obtenerCategoriaPorId"),
                    @Link(name = "crearCategoria", description = "Enlace para crear nueva categoria", operationId = "guardarCategoria"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de categoria", operationId = "actualizarCategoria"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion de la categoria", operationId = "eliminarCategoria")
                }
            ),
            @ApiResponse(
                responseCode = "403", description = "Prohibido",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    public ResponseEntity<List<EntityModel<CategoriaResponse>>> listarCategorias() {
        List<Categoria> categorias = categoriaService.findAll();

        List<EntityModel<CategoriaResponse>> responseModel = new ArrayList<>();
        for (Categoria categoria : categorias){
            responseModel.add(EntityModel.of(new CategoriaResponse(categoria),
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).guardarCategoria(new CategoriaRequest())).withRel("crearCategoria"),
                linkTo(methodOn(CategoriaController.class).actualizarCategoria(categoria.getId(), new CategoriaRequest())).withRel("actualizar"),
                linkTo(methodOn(CategoriaController.class).eliminarCategoria(categoria.getId())).withRel("eliminar")
            ));
        }

        return ResponseEntity.ok(responseModel);
    }

    //---------------------------------------------
    

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar categoria por ID",
        description = "Busca una categoria en la base de datos por su ID y retorna sus datos. El acceso es público.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Categoria recuperada exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de la categoria buscada", operationId = "obtenerCategoriaPorId"),
                    @Link(name = "listarCategorias", description = "Enlace a la lista de todas las categorias", operationId = "listarCategorias"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de categoria", operationId = "actualizarCategoria"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion de la categoria", operationId = "eliminarCategoria")
                }
            ),
            @ApiResponse(
                responseCode = "400", description = "Solicitud incorrecta",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
            ),
            @ApiResponse(
                responseCode = "403", description = "Prohibido",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "404", description = "Categoria no encontrada",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    public ResponseEntity<EntityModel<CategoriaResponse>> obtenerCategoriaPorId(
        @Parameter(description = "ID de la categoria buscada", required = true, example = "1") @PathVariable Long id
    ) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            EntityModel<CategoriaResponse> responseModel = EntityModel.of(new CategoriaResponse(categoria),
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("listarCategorias"),
                linkTo(methodOn(CategoriaController.class).actualizarCategoria(categoria.getId(), new CategoriaRequest())).withRel("actualizar"),
                linkTo(methodOn(CategoriaController.class).eliminarCategoria(categoria.getId())).withRel("eliminar")
            );

            return ResponseEntity.ok(responseModel);
                         
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------


    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Registrar categoria",
        description = "Crea una categoria y la guarda en la base de datos. El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Categoria registrada exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de la categoria creada", operationId = "obtenerCategoriaPorId"),
                    @Link(name = "listarCategorias", description = "Enlace a la lista de todas las categorias", operationId = "listarCategorias"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de categoria", operationId = "actualizarCategoria"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion de la categoria", operationId = "eliminarCategoria")
                }
            ),
            @ApiResponse(
                responseCode = "400", description = "Solicitud incorrecta (no se ha ingresado un objeto CategoriaRequest valido)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
            ),
            @ApiResponse(
                responseCode = "403", description = "Prohibido",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<CategoriaResponse>> guardarCategoria(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Categoria para guardar en base de datos", 
            required = true
        )
        @Valid @RequestBody CategoriaRequest request
    ) {
        sanitizarCategoria(request);
        Categoria categoria = request.toCategoria();
        categoria.setId(null);
        Categoria creada = categoriaService.save(categoria);

        EntityModel<CategoriaResponse> responseModel = EntityModel.of(new CategoriaResponse(creada),
            linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(creada.getId())).withSelfRel(),
            linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("listarCategorias"),
            linkTo(methodOn(CategoriaController.class).actualizarCategoria(creada.getId(), new CategoriaRequest())).withRel("actualizar"),
            linkTo(methodOn(CategoriaController.class).eliminarCategoria(creada.getId())).withRel("eliminar")
        );

        return ResponseEntity.ok(responseModel);
    }

    //---------------------------------------------


    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Actualizar datos de categoria",
        description = "Modifica los datos de la categoria en la base de datos con el ID ingresado. El acceso requiere rol ADMIN.",
    responses = {
        @ApiResponse(
            responseCode = "200", description = "Categoria actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class)),
            links = {
                @Link(name = "self", description = "Enlace a datos de la categoria creada", operationId = "obtenerCategoriaPorId"),
                @Link(name = "listarCategorias", description = "Enlace a la lista de todas las categorias", operationId = "listarCategorias"),
                @Link(name = "eliminar", description = "Enlace a eliminacion de la categoria", operationId = "eliminarCategoria")
            }
        ),
        @ApiResponse(
            responseCode = "400", description = "Solicitud incorrecta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "Prohibido",
            content = @Content(schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404", description = "Categoria no encontrada",
            content = @Content(schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<EntityModel<CategoriaResponse>> actualizarCategoria(
        @Parameter(description = "ID de la categoria modificada", required = true, example = "1") @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Categoria con datos actualizados", 
            required = true
        )
        @Valid @RequestBody CategoriaRequest request
    ) {
        sanitizarCategoria(request);
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            Categoria categoria = request.toCategoria();
            categoria.setId(id);
            categoria.setProductos(categoriaExistente.getProductos());
            Categoria actualizada = categoriaService.save(categoria);

            EntityModel<CategoriaResponse> responseModel = EntityModel.of(new CategoriaResponse(actualizada),
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(id)).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("listarCategorias"),
                linkTo(methodOn(CategoriaController.class).eliminarCategoria(id)).withRel("eliminar")
            );

            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------


    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Eliminar categoria",
        description = "Elimina la categoria en la base de datos con el ID ingresado. El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Categoria eliminada exitosamente (No content)",
                content = @Content(
                    mediaType = "application/json", 
                    examples = @ExampleObject(
                        name = "Confirmacion de eliminacion",
                        description = "Respuesta con mensaje que confirma la eliminacion de la categoria",
                        value = """
                                {
                                    "message": "Categoria eliminada exitosamente"
                                }
                                """
                    )
                ),
                links = {
                    @Link(name = "listarCategorias", description = "Enlace a la lista de todas las categorias", operationId = "listarCategorias"),
                    @Link(name = "crearCategoria", description = "Enlace para crear nueva categoria", operationId = "guardarCategoria")
                }

            ),
            @ApiResponse(
                responseCode = "400", description = "Solicitud incorrecta",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
            ),
            @ApiResponse(
                responseCode = "403", description = "Prohibido",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "404", description = "Categoria no encontrada",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarCategoria(
        @Parameter(description = "ID de la categoria que se desea eliminar", required = true, example = "1") @PathVariable Long id
    ) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            
            EntityModel<Map<String, String>> responseModel = EntityModel.of(
                Map.of("message", "Categoria eliminada exitosamente"),
                    linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("listarCategorias"),
                    linkTo(methodOn(CategoriaController.class).guardarCategoria(new CategoriaRequest())).withRel("crearCategoria")
            );

            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarCategoria(CategoriaRequest categoria){
        categoria.setNombre(sanitizeInput(categoria.getNombre()));
    }
}
