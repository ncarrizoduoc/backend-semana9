package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.DetalleVentaRequest;
import com.minimarket.minimarket.dto.DetalleVentaResponse;
import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.DetalleVentaRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.DetalleVentaService;

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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detalle-ventas")
@Tag(name = "Detalle Venta", description = "API para gestionar detalles de venta en base de datos y realizar consultas.")
@SecurityRequirement(name = "bearerAuth")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private DetalleVentaRequestMapper requestMapper;

    @GetMapping
    @Operation(
        summary = "Listar todos los detalles de venta",
        description = "Retorna la lista completa de detalles de venta en la base de datos. El acceso requiere rol CAJERO.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Lista de detalles de venta obtenida exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVentaResponse[].class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de detalle de venta", operationId = "obtenerDetalleVentaPorId"),
                    @Link(name = "crearDetalle", description = "Enlace para crear nuevo detalle de venta", operationId = "guardarDetalleVenta"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de detalle de venta", operationId = "actualizarDetalleVenta"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del detalle de venta", operationId = "eliminarDetalleVenta")
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
    public ResponseEntity<List<EntityModel<DetalleVentaResponse>>> listarDetalleVentas() {
        List<DetalleVenta> detalles =  detalleVentaService.findAll();

        List<EntityModel<DetalleVentaResponse>> responseModel = new ArrayList<>();
        for (DetalleVenta detalle : detalles){
            responseModel.add(EntityModel.of(new DetalleVentaResponse(detalle),
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(detalle.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).guardarDetalleVenta(new DetalleVentaRequest())).withRel("crearDetalle"),
                linkTo(methodOn(DetalleVentaController.class).actualizarDetalleVenta(detalle.getId(), new DetalleVentaRequest())).withRel("actualizar"),
                linkTo(methodOn(DetalleVentaController.class).eliminarDetalleVenta(detalle.getId())).withRel("eliminar")
            ));
        }
        
        return ResponseEntity.ok(responseModel);
    }

    //---------------------------------------------


    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar detalle de venta por ID",
        description = "Busca un detalle de venta en la base de datos por su ID y retorna sus datos."
            + " El acceso requiere rol CAJERO.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Detalle de venta recuperado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVentaResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del detalle de venta buscado", operationId = "obtenerDetalleVentaPorId"),
                    @Link(name = "listarDetalles", description = "Enlace a la lista de todos los detalles de venta", operationId = "listarDetalleVentas"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de detalle de venta buscado", operationId = "actualizarDetalleVenta"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del detalle de venta buscado", operationId = "eliminarDetalleVenta")
                }
            ),
            @ApiResponse(
                responseCode = "400", description = "Solicitud incorrecta (no se ha ingresado un objeto DetalleVentaRequest valido)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
            ),
            @ApiResponse(
                responseCode = "403", description = "Prohibido",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "404", description = "Detalle de venta no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<DetalleVentaResponse>> obtenerDetalleVentaPorId(
        @Parameter(description = "ID del detalle de venta buscado", required = true, example = "1") @PathVariable Long id
    ) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null){
            DetalleVentaResponse detalleVentaResponse = new DetalleVentaResponse(detalleVenta);
            EntityModel<DetalleVentaResponse> responseModel = EntityModel.of(detalleVentaResponse,
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(id)).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("listarDetalles"),
                linkTo(methodOn(DetalleVentaController.class).actualizarDetalleVenta(id, new DetalleVentaRequest())).withRel("actualizar"),
                linkTo(methodOn(DetalleVentaController.class).eliminarDetalleVenta(id)).withRel("eliminar")
            );
            
            return ResponseEntity.ok(responseModel);
        }
        
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------


    @PostMapping
    @Operation(
        summary = "Registrar detalle de venta",
        description = "Crea un detalle de venta para un producto y lo guarda en la base de datos. "
            + "El acceso requiere rol CAJERO.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Detalle de venta registrado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVentaResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del detalle de venta buscado", operationId = "obtenerDetalleVentaPorId"),
                    @Link(name = "listarDetalles", description = "Enlace a la lista de todos los detalles de venta", operationId = "listarDetalleVentas"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de detalle de venta buscado", operationId = "actualizarDetalleVenta"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del detalle de venta buscado", operationId = "eliminarDetalleVenta")
                }
            ),
            @ApiResponse(
                responseCode = "400", description = "Solicitud incorrecta (no se ha ingresado un objeto DetalleVentaRequest valido)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
            ),
            @ApiResponse(
                responseCode = "403", description = "Prohibido",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "404", description = "Venta o Producto no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<DetalleVentaResponse>> guardarDetalleVenta(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Detalle de venta para guardar en base de datos", required = true)
        @Valid @RequestBody DetalleVentaRequest request
    ) {
        DetalleVenta detalle = requestMapper.toDetalleVenta(request);
        detalle.setId(null);
        DetalleVenta creado = detalleVentaService.save(detalle);
        DetalleVentaResponse response = new DetalleVentaResponse(creado);

        EntityModel<DetalleVentaResponse> responseModel = EntityModel.of(response,
            linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(creado.getId())).withSelfRel(),
            linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("listarDetalles"),
            linkTo(methodOn(DetalleVentaController.class).actualizarDetalleVenta(creado.getId(), new DetalleVentaRequest())).withRel("actualizar"),
            linkTo(methodOn(DetalleVentaController.class).eliminarDetalleVenta(creado.getId())).withRel("eliminar")
        );

        return ResponseEntity.ok(responseModel);
    }

    //---------------------------------------------


    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar datos de detalle de venta",
        description = "Modifica los datos del detalle de venta en la base de datos con el ID ingresado."
            + " El acceso requiere rol CAJERO.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Detalle de venta actualizado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVentaResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del detalle de venta", operationId = "obtenerDetalleVentaPorId"),
                    @Link(name = "listarDetalles", description = "Enlace a la lista de todos los detalles de venta", operationId = "listarDetalleVentas"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del detalle de venta actualizado", operationId = "eliminarDetalleVenta")
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
                responseCode = "404", description = "Detalle de venta, Venta o Producto no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<DetalleVentaResponse>> actualizarDetalleVenta(
        @Parameter(
            description = "ID del detalle de venta que se desea actualizar", 
            required = true, 
            example = "1") 
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos actualizados del detalle de venta",
            required = true)
        @Valid @RequestBody DetalleVentaRequest request
    ) {
        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente != null) {
            DetalleVenta detalle = requestMapper.toDetalleVenta(request);
            detalle.setId(id);
            DetalleVenta actualizado = detalleVentaService.update(detalle);
            DetalleVentaResponse response = new DetalleVentaResponse(actualizado); 
            
            // Generar EntityModel con links
            EntityModel<DetalleVentaResponse> responseModel = EntityModel.of(response,
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(id)).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("listarDetalles"),
                linkTo(methodOn(DetalleVentaController.class).eliminarDetalleVenta(id)).withRel("eliminar")
            );
            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------


    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar detalle de venta",
        description = "Elimina el detalle de venta en la base de datos con el ID ingresado. "
            + "El acceso requiere rol CAJERO.",
        responses = {
                @ApiResponse(
                responseCode = "200",
                description = "Detalle de venta eliminado exitosamente",
                content = @Content(
                    mediaType = "application/json", 
                    examples = @ExampleObject(
                        name = "Confirmacion de eliminacion",
                        description = "Respuesta con mensaje que confirma la eliminacion del detalle de venta",
                        value = """
                                {
                                    "message": "Detalle de venta eliminado exitosamente"
                                }
                                """
                    )
                ),
                links = {
                    @Link(name = "listarDetalles", description = "Enlace a la lista de todos los detalles de venta", operationId = "listarDetalleVentas"),
                    @Link(name = "crearDetalle", description = "Enlace para crear nuevo detalle de venta", operationId = "guardarDetalleVenta")
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
                responseCode = "404", description = "Detalle de venta no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarDetalleVenta(
        @Parameter(
            description = "ID del detalle de venta que se desea eliminar",
            required = true,
            example = "1")
        @PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null) {
            detalleVentaService.deleteById(id);

            EntityModel<Map<String, String>> responseModel = EntityModel.of(
                Map.of("message", "Detalle de venta eliminado exitosamente"),
                    linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("listarDetalles"),
                    linkTo(methodOn(DetalleVentaController.class).guardarDetalleVenta(new DetalleVentaRequest())).withRel("crearDetalle")
            );

            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build();
    }
}
