package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.CarritoRequest;
import com.minimarket.minimarket.dto.CarritoResponse;
import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.CarritoRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.CarritoService;

import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "API para gestionar carritos en base de datos y realizar consultas.")
@SecurityRequirement(name = "bearerAuth")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private CarritoRequestMapper requestMapper;

    @GetMapping
    @Operation(
        summary = "Listar todos los carritos",
        description = "Retorna la lista completa de carritos en la base de datos. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Lista de carritos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoResponse[].class))
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
    public List<CarritoResponse> listarCarrito() {
        return carritoService.findAll().stream().map(CarritoResponse::new).toList();
    }

    @Operation(
        summary = "Buscar carrito por ID",
        description = "Busca un carrito en la base de datos por su ID y retorna sus datos. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Carrito recuperado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoResponse.class))
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
            responseCode = "404", description = "Carrito no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponse> obtenerCarritoPorId(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        return (carrito != null) ? ResponseEntity.ok(new CarritoResponse(carrito)) : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Registrar carrito",
        description = "Crea un carrito (con usuario y producto asociado) y lo guarda en la base de datos. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Carrito registrado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoResponse.class))
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
            responseCode = "404", description = "Carrito, producto o usuario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    @PostMapping
    public CarritoResponse agregarProductoAlCarrito(@Valid @RequestBody CarritoRequest request) {
        Carrito carrito = requestMapper.toCarrito(request);
        carrito.setId(null);
        return new CarritoResponse(carritoService.save(carrito));
    }

    @Operation(
        summary = "Actualizar datos de carrito",
        description = "Modifica los datos del carrito en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Carrito actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoResponse.class))
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
            responseCode = "404", description = "Carrito, producto o usuario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    @PutMapping("/{id}")
    public ResponseEntity<CarritoResponse> actualizarCarrito(@PathVariable Long id, @Valid @RequestBody CarritoRequest request) {
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            Carrito carrito = requestMapper.toCarrito(request);
            carrito.setId(id);
            return ResponseEntity.ok(new CarritoResponse(carritoService.update(carrito)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar carrito",
        description = "Elimina el carrito en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "Carrito eliminado exitosamente (No content)",
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
            responseCode = "404", description = "Carrito no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
