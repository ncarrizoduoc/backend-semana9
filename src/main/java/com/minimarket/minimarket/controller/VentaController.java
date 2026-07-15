package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.VentaRequest;
import com.minimarket.minimarket.dto.VentaResponse;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.VentaRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
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

@RestController
@RequestMapping("/api/ventas")
@Tag(
    name = "Venta",
    description = "API para guardar ventas en base de datos y realizar consultas.")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRequestMapper requestMapper;

    @GetMapping
    @Operation(
        summary = "Listar todas las ventas",
        description = "Retorna la lista completa de ventas en la base de datos. El acceso requiere rol CAJERO.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Lista de ventas obtenida exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaResponse[].class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de venta", operationId = "obtenerVentaPorId"),
                    @Link(name = "Crear venta", description = "Enlace para guardar nueva venta", operationId = "guardarVenta")
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
    public ResponseEntity<List<EntityModel<VentaResponse>>> listarVentas() {
        List<Venta> ventas =  ventaService.findAll();

        List<EntityModel<VentaResponse>> lista = new ArrayList<>();
        for (Venta venta : ventas){
            lista.add(EntityModel.of(new VentaResponse(venta),
            linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel(),
            linkTo(methodOn(VentaController.class).guardarVenta(new VentaRequest())).withRel("Crear venta")
        ));
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar venta por ID",
        description = "Busca una venta en la base de datos por su ID y retorna sus datos."
            + " El acceso requiere rol CAJERO.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Venta recuperada exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de la venta buscada", operationId = "obtenerVentaPorId"),
                    @Link(name = "Listar ventas", description = "Enlace a la lista de todas las ventas", operationId = "listarVentas"),
                    @Link(name = "Crear venta", description = "Enlace para guardar nueva venta", operationId = "guardarVenta")
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
                responseCode = "404", description = "Venta no encontrada",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<VentaResponse>> obtenerVentaPorId(
        @Parameter(description = "ID de la venta buscada", required = true, example = "1") @PathVariable Long id
    ) {
        Venta venta = ventaService.findById(id);
        if (venta != null){
            VentaResponse ventaResponse = new VentaResponse(venta);
            EntityModel<VentaResponse> response = EntityModel.of(ventaResponse,
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(ventaResponse.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas()).withRel("Listar ventas"),
                linkTo(methodOn(VentaController.class).guardarVenta(new VentaRequest())).withRel("Crear venta")
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Registrar venta",
        description = "Crea una venta y la guarda en la base de datos. "
            + "El acceso requiere rol CAJERO.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Venta registrada exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de la venta creada", operationId = "obtenerVentaPorId"),
                    @Link(name = "Listar ventas", description = "Enlace a la lista de todas las ventas", operationId = "listarVentas"),
                }
            ),
            @ApiResponse(
                responseCode = "400", description = "Solicitud incorrecta (el objeto ingresado no corresponde a un VentaRequest valido)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestResponse.class))
            ),
            @ApiResponse(
                responseCode = "403", description = "Prohibido",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "404", description = "Usuario no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<VentaResponse>> guardarVenta(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Venta para guardar en base de datos", required = true
        )
        @Valid @RequestBody VentaRequest request
    ) {
        Venta venta = requestMapper.toVenta(request);
        venta.setId(null);
        
        // Guardar venta
        Venta creado = ventaService.save(venta);
        VentaResponse ventaResponse = new VentaResponse(creado); 

        // Generar EntityModel con links
        EntityModel<VentaResponse> response = EntityModel.of(ventaResponse,
            linkTo(methodOn(VentaController.class).obtenerVentaPorId(ventaResponse.getId())).withSelfRel(),
            linkTo(methodOn(VentaController.class).listarVentas()).withRel("Listar ventas")
        );
        
        return ResponseEntity.ok(response);
    }

}
