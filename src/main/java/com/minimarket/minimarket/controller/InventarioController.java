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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        description = "Retorna la lista completa de movimientos de inventario en la base de datos. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Lista de movimientos de inventario obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponse[].class))
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
    public List<InventarioResponse> listarMovimientosDeInventario() {
        return inventarioService.findAll().stream()
            .map(InventarioResponse::new)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar movimiento de inventario por ID",
        description = "Busca un movimiento de inventario en la base de datos por su ID y retorna sus datos."
            + " El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Movimiento de inventario recuperado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponse.class))
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
            responseCode = "404", description = "Movimiento de inventario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<InventarioResponse> obtenerMovimientoPorId(
        @Parameter(description = "ID del movimiento de inventario buscado", required = true) @PathVariable Long id
    ) {
        Inventario inventario = inventarioService.findById(id);
        return (inventario != null) ? ResponseEntity.ok(new InventarioResponse(inventario)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(
        summary = "Registrar movimiento de inventario",
        description = "Crea un movimiento de inventario para un producto y lo guarda en la base de datos. "
            + "El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Movimiento de inventario registrado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponse.class))
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
            responseCode = "404", description = "Producto no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public InventarioResponse registrarMovimiento(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Movimiento de inventario para guardar en base de datos",
            required = true
        )
        @Valid @RequestBody InventarioRequest request
    ) {
        sanitizarInventario(request);
        Inventario inventario = requestMapper.toInventario(request);
        inventario.setId(null);
        return new InventarioResponse(inventarioService.save(inventario));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar datos de movimiento de inventario",
        description = "Modifica los datos de un movimiento de inventario en la base de datos con el ID ingresado."
            + " El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Movimiento de inventario actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponse.class))
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
            responseCode = "404", description = "Movimiento de inventario o producto no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<InventarioResponse> actualizarMovimiento(
        @Parameter(
            description = "ID del movimiento de inventario que se desea actualizar", 
            required = true
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
            return ResponseEntity.ok(new InventarioResponse(inventarioService.update(inventario)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar movimiento de inventario",
        description = "Elimina el movimiento de inventario en la base de datos con el ID ingresado. "
            + "El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "Movimiento de inventario eliminado exitosamente (No content)",
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
            responseCode = "404", description = "Movimiento de inventario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<Void> eliminarMovimiento(
        @Parameter(
            description = "ID del movimiento de inventario que se desea eliminar",
            required = true
        )
        @PathVariable Long id
    ) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarInventario(InventarioRequest inventario){
        inventario.setTipoMovimiento(sanitizeInput(inventario.getTipoMovimiento()));
    }

}
