package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.EliminadoMessageDTO;
import com.minimarket.minimarket.dto.ProductoRequest;
import com.minimarket.minimarket.dto.ProductoResponse;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.ProductoRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

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
        description = "Retorna la lista completa de productos en la base de datos. El acceso es público.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Lista de productos obtenida exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse[].class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de producto", operationId = "obtenerProductoPorId"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de producto", operationId = "actualizarProducto"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del producto", operationId = "eliminarProducto")
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
        })
    public ResponseEntity<List<EntityModel<ProductoResponse>>> listarProductos() {
        List<Producto> productos = productoService.findAll();

        // Se guarda cada producto en una lista con sus links asociados
        List<EntityModel<ProductoResponse>> lista = new ArrayList<>();
        for(Producto producto : productos){
            lista.add(EntityModel.of(new ProductoResponse(producto),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).actualizarProducto(producto.getId(), new ProductoRequest())).withRel("actualizar"),
                linkTo(methodOn(ProductoController.class).eliminarProducto(producto.getId())).withRel("eliminar")
            )
        );
        }

        return ResponseEntity.ok(lista);
    }

    //---------------------------------------------

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar producto por ID",
        description = "Busca un producto en la base de datos por su ID y retorna sus datos. El acceso es público.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Producto recuperado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del producto buscado", operationId = "obtenerProductoPorId"),
                    @Link(name = "listarProductos", description = "Enlace a la lista de todos los productos", operationId = "listarProductos"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de producto buscado", operationId = "actualizarProducto"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del producto buscado", operationId = "eliminarProducto")
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
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<ProductoResponse>> obtenerProductoPorId(
        @Parameter(description = "ID del producto buscado", required = true, example = "1") @PathVariable @PositiveOrZero Long id
    ) {
        Producto producto = productoService.findById(id);
        if (producto != null){
            ProductoResponse productoResponse = new ProductoResponse(producto);
            EntityModel<ProductoResponse> response = EntityModel.of(productoResponse,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(id)).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("listarProductos"),
                linkTo(methodOn(ProductoController.class).actualizarProducto(id, new ProductoRequest())).withRel("actualizar"),
                linkTo(methodOn(ProductoController.class).eliminarProducto(id)).withRel("eliminar")
            );

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Registrar producto",
        description = "Crea un producto y lo guarda en la base de datos. El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Producto registrado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del producto creado", operationId = "obtenerProductoPorId"),
                    @Link(name = "listarProductos", description = "Enlace a la lista de todos los productos", operationId = "listarProductos"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de producto creado", operationId = "actualizarProducto"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del producto creado", operationId = "eliminarProducto")
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
                responseCode = "404", description = "Categoria asociada a producto no encontrada",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<ProductoResponse>> guardarProducto(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Producto para guardar en base de datos", 
            required = true
        )
        @Valid @RequestBody ProductoRequest request
    ) {
        sanitizarProducto(request);
        Producto producto = requestMapper.toProducto(request);
        producto.setId(null);

        //Guardar producto
        Producto creado = productoService.save(producto);
        ProductoResponse response = new ProductoResponse(creado);

        // Generar EntityModel con links
        EntityModel<ProductoResponse> productoModel = EntityModel.of(response,
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(creado.getId())).withSelfRel(),
            linkTo(methodOn(ProductoController.class).listarProductos()).withRel("listarProductos"),
            linkTo(methodOn(ProductoController.class).actualizarProducto(creado.getId(), new ProductoRequest())).withRel("actualizar"),
            linkTo(methodOn(ProductoController.class).eliminarProducto(creado.getId())).withRel("eliminar")
        );

        return ResponseEntity.ok(productoModel);
    }

    //---------------------------------------------

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Actualizar datos de producto",
        description = "Modifica los datos del producto en la base de datos con el ID ingresado. El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Producto actualizado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del producto actualizado", operationId = "obtenerProductoPorId"),
                    @Link(name = "listarProductos", description = "Enlace a la lista de todos los productos", operationId = "listarProductos"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del producto actualizado", operationId = "eliminarProducto")
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
                responseCode = "404", description = "Producto no encontrado o categoria no encontrada",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<ProductoResponse>> actualizarProducto(
        @Parameter(description = "ID del producto modificado", required = true, example = "1") @PathVariable Long id,
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
            
            // Actualizar producto
            Producto actualizado = productoService.save(producto);
            ProductoResponse response = new ProductoResponse(actualizado);

            // Generar EntityModel con links
            EntityModel<ProductoResponse> productoModel = EntityModel.of(response,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(id)).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("listarProductos"),
                linkTo(methodOn(ProductoController.class).eliminarProducto(id)).withRel("eliminar")
            );

            return ResponseEntity.ok(productoModel);
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina el producto en la base de datos con el ID ingresado. El acceso requiere rol ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Producto eliminado exitosamente (No content)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = EliminadoMessageDTO.class)),
                links = {
                    @Link(name = "listarProductos", description = "Enlace a lista con todos los productos", operationId = "listarProductos"),
                    @Link(name = "guardarProducto", description = "Enlace para crear nuevo producto", operationId = "guardarProducto")
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
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarProducto(
        @Parameter(description = "ID del producto que se desea eliminar", required = true, example = "1") @PathVariable Long id
    ) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);

            EntityModel<Map<String, String>> responseModel = EntityModel.of(
                Map.of("message", "Producto eliminado exitosamente"),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("listarProductos"),
                linkTo(methodOn(ProductoController.class).guardarProducto(new ProductoRequest())).withRel("guardarProducto")
            );

            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build();
    }


    private void sanitizarProducto(ProductoRequest producto){
        producto.setNombre(sanitizeInput(producto.getNombre()));
    }
}
