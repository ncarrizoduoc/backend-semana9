package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.InventarioRequest;
import com.minimarket.minimarket.dto.InventarioResponse;
import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.InventarioRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.InventarioService;

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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static com.minimarket.minimarket.security.util.InputSanitizer.*;


@RestController
@RequestMapping("/api/inventario")
@Tag(
    name = "Inventario", 
    description = "API para gestionar movimientos de inventario en base de datos y realizar consultas."
)
@SecurityRequirement(name = "bearerAuth")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InventarioRequestMapper requestMapper;

    @GetMapping
    @Operation(
        summary = "Listar todos los movimientos de inventario",
        description = "Retorna la lista completa de movimientos de inventario en la base de datos. El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Lista de movimientos de inventario obtenida exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponse[].class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de movimiento de inventario", operationId = "obtenerMovimientoPorId"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de movimiento de inventario", operationId = "actualizarMovimiento"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del movimiento de inventario", operationId = "eliminarMovimiento")
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
    public ResponseEntity<List<EntityModel<InventarioResponse>>> listarMovimientosDeInventario() {
        List<Inventario> inventarios = inventarioService.findAll();

        // Se guarda cada carrito en una lista con sus links asociados
        List<EntityModel<InventarioResponse>> lista = new ArrayList<>();
        for(Inventario inventario : inventarios){
            lista.add(EntityModel.of(new InventarioResponse(inventario),
                linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(inventario.getId())).withSelfRel(),
                linkTo(methodOn(InventarioController.class).actualizarMovimiento(inventario.getId(), new InventarioRequest())).withRel("actualizar"),
                linkTo(methodOn(InventarioController.class).eliminarMovimiento(inventario.getId())).withRel("eliminar")
            )
        );
        }

        return ResponseEntity.ok(lista);
    }

    //---------------------------------------------

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar movimiento de inventario por ID",
        description = "Busca un movimiento de inventario en la base de datos por su ID y retorna sus datos."
            + " El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Movimiento de inventario recuperado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del movimiento buscado", operationId = "obtenerMovimientoPorId"),
                    @Link(name = "listarMovimientos", description = "Enlace a la lista de todos los movimientos de inventario", operationId = "listarMovimientosDeInventario"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de movimiento buscado", operationId = "actualizarMovimiento"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del movimiento buscado", operationId = "eliminarMovimiento")
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
                responseCode = "404", description = "Movimiento de inventario no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<InventarioResponse>> obtenerMovimientoPorId(
        @Parameter(description = "ID del movimiento de inventario buscado", required = true, example = "1") @PathVariable Long id
    ) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null){
            InventarioResponse inventarioResponse = new InventarioResponse(inventario);
            EntityModel<InventarioResponse> response = EntityModel.of(inventarioResponse,
                linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(id)).withSelfRel(),
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("listarMovimientos"),
                linkTo(methodOn(InventarioController.class).actualizarMovimiento(id, new InventarioRequest())).withRel("actualizar"),
                linkTo(methodOn(InventarioController.class).eliminarMovimiento(id)).withRel("eliminar")
            );

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------

    @PostMapping
    @Operation(
        summary = "Registrar movimiento de inventario",
        description = "Crea un movimiento de inventario para un producto y lo guarda en la base de datos. "
            + "El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Movimiento de inventario registrado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del movimiento creado", operationId = "obtenerMovimientoPorId"),
                    @Link(name = "listarMovimientos", description = "Enlace a la lista de todos los movimientos de inventario", operationId = "listarMovimientos"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de movimiento creado", operationId = "actualizarMovimiento"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del movimiento creado", operationId = "eliminarMovimiento")
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
                responseCode = "404", description = "Producto no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<InventarioResponse>> registrarMovimiento(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Movimiento de inventario para guardar en base de datos",
            required = true
        )
        @Valid @RequestBody InventarioRequest request
    ) {
        sanitizarInventario(request);
        Inventario inventario = requestMapper.toInventario(request);
        inventario.setId(null);
        
        //Guardar inventario
        Inventario creado = inventarioService.save(inventario);
        InventarioResponse response = new InventarioResponse(creado);

        // Generar EntityModel con links
        EntityModel<InventarioResponse> inventarioModel = EntityModel.of(response,
            linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(creado.getId())).withSelfRel(),
            linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("listarMovimientos"),
            linkTo(methodOn(InventarioController.class).actualizarMovimiento(creado.getId(), new InventarioRequest())).withRel("actualizar"),
            linkTo(methodOn(InventarioController.class).eliminarMovimiento(creado.getId())).withRel("eliminar")
        );

        return ResponseEntity.ok(inventarioModel);
    }

    //---------------------------------------------

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar datos de movimiento de inventario",
        description = "Modifica los datos del movimiento de inventario en la base de datos con el ID ingresado."
            + " El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Movimiento de inventario actualizado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del movimiento actualizado", operationId = "obtenerMovimientoPorId"),
                    @Link(name = "listarMovimientos", description = "Enlace a la lista de todos los movimientos de inventario", operationId = "listarMovimientos"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del movimiento actualizado", operationId = "eliminarMovimiento")
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
                responseCode = "404", description = "Movimiento de inventario o producto no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<InventarioResponse>> actualizarMovimiento(
        @Parameter(
            description = "ID del movimiento de inventario que se desea actualizar", 
            required = true,
            example = "1"
        )
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Movimiento de inventario con datos actualizados",
            required = true
        ) 
        @Valid @RequestBody InventarioRequest request) {
        sanitizarInventario(request);
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            Inventario inventario = requestMapper.toInventario(request);
            inventario.setId(id);
            
            // Actualizar producto
            Inventario actualizado = inventarioService.update(inventario);
            InventarioResponse response = new InventarioResponse(actualizado);

            // Generar EntityModel con links
            EntityModel<InventarioResponse> inventarioModel = EntityModel.of(response,
                linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(id)).withSelfRel(),
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("listarMovimientos"),
                linkTo(methodOn(InventarioController.class).eliminarMovimiento(id)).withRel("eliminar")
            );

            return ResponseEntity.ok(inventarioModel);
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar movimiento de inventario",
        description = "Elimina el movimiento de inventario en la base de datos con el ID ingresado. "
            + "El acceso requiere rol ADMIN.",
        responses = {
                @ApiResponse(
                responseCode = "200", description = "Movimiento de inventario eliminado exitosamente",
                content = @Content(
                    mediaType = "application/json", 
                    examples = @ExampleObject(
                        name = "Confirmacion de eliminacion",
                        description = "Respuesta con mensaje que confirma la eliminacion del movimiento de inventario",
                        value = """
                                {
                                    "message": "Movimiento de inventario eliminado exitosamente"
                                }
                                """
                    )
                ),
                links = {
                    @Link(name = "listarMovimientos", description = "Enlace a lista con todos los movimientos de inventario", operationId = "listarMovimientosDeInventario"),
                    @Link(name = "guardarMovimiento", description = "Enlace para crear nuevo movimiento de inventario", operationId = "registrarMovimiento")
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
                responseCode = "404", description = "Movimiento de inventario no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarMovimiento(
        @Parameter(
            description = "ID del movimiento de inventario que se desea eliminar",
            required = true,
            example = "1"
        )
        @PathVariable Long id
    ) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            
            EntityModel<Map<String, String>> responseModel = EntityModel.of(
                Map.of("message", "Movimiento de inventario eliminado exitosamente"),
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("listarMovimientos"),
                linkTo(methodOn(InventarioController.class).registrarMovimiento(new InventarioRequest())).withRel("guardarMovimiento")
            );
            
            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarInventario(InventarioRequest inventario){
        inventario.setTipoMovimiento(sanitizeInput(inventario.getTipoMovimiento()));
    }

}
