package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.ProductoRequest;
import com.minimarket.minimarket.dto.ProductoResponse;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.ProductoRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Autowired
    private ProductoRequestMapper requestMapper;

    @GetMapping
    @Operation(
        summary = "Listar todos los productos",
        description = "Retorna la lista completa de productos en la base de datos. El acceso es público."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Lista de productos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse[].class))
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
    public List<ProductoResponse> listarProductos() {
        return productoService.findAll().stream().map(ProductoResponse :: new).toList();
    }


    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar producto por ID",
        description = "Busca un producto en la base de datos por su ID y retorna sus datos. El acceso es público."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Producto recuperado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class))
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
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(
        @Parameter(description = "ID del producto buscado", required = true) @PathVariable @PositiveOrZero Long id
    ) {
        Producto producto = productoService.findById(id);
        return (producto != null) ? ResponseEntity.ok(new ProductoResponse(producto)) : ResponseEntity.notFound().build();
    }


    @PostMapping
    @Operation(
        summary = "Registrar producto",
        description = "Crea un producto y lo guarda en la base de datos. El acceso requiere rol ADMIN."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Producto registrado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class))
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
            responseCode = "404", description = "Categoria asociada a producto no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ProductoResponse guardarProducto(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Producto para guardar en base de datos", 
            required = true
        )
        @Valid @RequestBody ProductoRequest request
    ) {
        sanitizarProducto(request);
        Producto producto = requestMapper.toProducto(request);
        producto.setId(null);
        return new ProductoResponse(productoService.save(producto));
    }


    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar datos de producto",
        description = "Modifica los datos del producto en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Producto actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class))
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
            responseCode = "404", description = "Producto no encontrado o categoria no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<ProductoResponse> actualizarProducto(
        @Parameter(description = "ID del producto modificado", required = true) @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Producto con datos actualizados", 
            required = true
        )
        @Valid @RequestBody ProductoRequest request
    ) {
        sanitizarProducto(request);
        Producto productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            Producto producto = requestMapper.toProducto(request);
            producto.setId(id);
            return ResponseEntity.ok(new ProductoResponse(productoService.save(producto)));
        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina el producto en la base de datos con el ID ingresado. El acceso requiere rol ADMIN."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "Producto eliminado exitosamente (No content)",
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
            responseCode = "404", description = "Producto no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )}
    )
    public ResponseEntity<Void> eliminarProducto(
        @Parameter(description = "ID del producto que se desea eliminar", required = true) @PathVariable Long id
    ) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }


    private void sanitizarProducto(ProductoRequest producto){
        producto.setNombre(sanitizeInput(producto.getNombre()));
    }
}
