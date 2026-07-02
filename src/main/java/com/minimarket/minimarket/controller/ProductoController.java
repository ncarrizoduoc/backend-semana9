package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minimarket.minimarket.security.util.InputSanitizer.*;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Producto", description = "API para gestionar productos en base de datos y realizar consultas.")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(
        summary = "Listar todos los productos",
        description = "Retorna la lista completa de productos en la base de datos. El acceso es público."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Lista de productos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto[].class))
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
    public List<Producto> listarProductos() {
        return productoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar producto por ID",
        description = "Busca un producto en la base de datos por su ID y retorna sus datos. El acceso es público."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Producto recuperado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))
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
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<Producto> obtenerProductoPorId(
        @Parameter(description = "ID del producto buscado", required = true) @PathVariable @PositiveOrZero Long id
    ) {
        Producto producto = productoService.findById(id);
        return (producto != null) ? ResponseEntity.ok(producto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(
        summary = "Registrar producto",
        description = "Crea un producto y lo guarda en la base de datos. El acceso requiere rol ADMIN."
    )
    public Producto guardarProducto(@Valid @RequestBody Producto producto) {
        sanitizarProducto(producto);
        return productoService.save(producto);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Modificar datos de producto",
        description = "Modifica los datos del producto en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        sanitizarProducto(producto);
        Producto productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            producto.setId(id);
            return ResponseEntity.ok(productoService.save(producto));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina el producto en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarProducto(Producto producto){
        producto.setNombre(sanitizeInput(producto.getNombre()));
    }
}
