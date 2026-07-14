package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.CarritoRequest;
import com.minimarket.minimarket.dto.CarritoResponse;
import com.minimarket.minimarket.dto.EliminadoMessageDTO;
import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.mapper.CarritoRequestMapper;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.service.CarritoService;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


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
        summary = "Listar todos los productos en el carrito",
        description = "Retorna la lista completa de carritos en la base de datos. "
            + "El acceso requiere rol ADMIN o CLIENTE.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Lista de carritos obtenida exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoResponse[].class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos de carrito", operationId = "obtenerCarritoPorId"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de carrito", operationId = "actualizarCarrito"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del carrito", operationId = "eliminarProductoDelCarrito")
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
    public ResponseEntity<List<EntityModel<CarritoResponse>>> listarCarrito() {
        List<Carrito> carritos = carritoService.findAll();

        // Se guarda cada carrito en una lista con sus links asociados
        List<EntityModel<CarritoResponse>> lista = new ArrayList<>();
        for(Carrito carrito : carritos){
            lista.add(EntityModel.of(new CarritoResponse(carrito),
                linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(carrito.getId())).withSelfRel(),
                linkTo(methodOn(CarritoController.class).actualizarCarrito(carrito.getId(), new CarritoRequest())).withRel("actualizar"),
                linkTo(methodOn(CarritoController.class).eliminarProductoDelCarrito(carrito.getId())).withRel("eliminar")
            )
        );
        }

        return ResponseEntity.ok(lista);
    }

    //---------------------------------------------


    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar carrito por ID",
        description = "Busca un carrito en la base de datos por su ID y retorna sus datos. "
            + "El acceso requiere rol ADMIN o CLIENTE.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Carrito recuperado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del carrito buscado", operationId = "obtenerCarritoPorId"),
                    @Link(name = "listarCarritos", description = "Enlace a la lista de todos los carritos", operationId = "listarCarrito"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de producto buscado", operationId = "actualizarCarrito"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del producto buscado", operationId = "eliminarProductoDelCarrito")
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
                responseCode = "404", description = "Carrito no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<CarritoResponse>> obtenerCarritoPorId(
        @Parameter(description = "ID del carrito buscado", required = true, example = "1") @PathVariable Long id
    ) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null){
            CarritoResponse carritoResponse = new CarritoResponse(carrito);
            EntityModel<CarritoResponse> response = EntityModel.of(carritoResponse,
                linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(id)).withSelfRel(),
                linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("listarCarritos"),
                linkTo(methodOn(CarritoController.class).actualizarCarrito(id, new CarritoRequest())).withRel("actualizar"),
                linkTo(methodOn(CarritoController.class).eliminarProductoDelCarrito(id)).withRel("eliminar")
            );

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------

    @PostMapping
    @Operation(
        summary = "Agregar producto al carrito",
        description = "Crea un carrito (con usuario y producto asociado) y lo guarda en la base de datos. "
            + "El acceso requiere rol ADMIN o CLIENTE.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Carrito registrado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del carrito creado", operationId = "obtenerCarritoPorId"),
                    @Link(name = "listarCarritos", description = "Enlace a la lista de todos los carritos", operationId = "listarCarrito"),
                    @Link(name = "actualizar", description = "Enlace a actualizacion de carrito creado", operationId = "actualizarCarrito"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del carrito creado", operationId = "eliminarProductoDelCarrito")
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
                responseCode = "404", description = "Carrito, producto o usuario no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<CarritoResponse>> agregarProductoAlCarrito(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Carrito para guardar en base de datos",
            required = true
        )
        @Valid @RequestBody CarritoRequest request
    ) {
        Carrito carrito = requestMapper.toCarrito(request);
        carrito.setId(null);
        
        //Guardar carrito
        Carrito creado = carritoService.save(carrito);
        CarritoResponse response = new CarritoResponse(creado);

        // Generar EntityModel con links
        EntityModel<CarritoResponse> carritoModel = EntityModel.of(response,
            linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(creado.getId())).withSelfRel(),
            linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("listarCarritos"),
            linkTo(methodOn(CarritoController.class).actualizarCarrito(creado.getId(), new CarritoRequest())).withRel("actualizar"),
            linkTo(methodOn(CarritoController.class).eliminarProductoDelCarrito(creado.getId())).withRel("eliminar")
        );

        return ResponseEntity.ok(carritoModel);
    }

    //---------------------------------------------

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar datos de carrito",
        description = "Modifica los datos del carrito en la base de datos con el ID ingresado. "
            + "El acceso requiere rol ADMIN o CLIENTE.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Carrito actualizado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoResponse.class)),
                links = {
                    @Link(name = "self", description = "Enlace a datos del carrito actualizado", operationId = "obtenerCarritoPorId"),
                    @Link(name = "listarCarritos", description = "Enlace a la lista de todos los carritos", operationId = "listarCarrito"),
                    @Link(name = "eliminar", description = "Enlace a eliminacion del carrito actualizado", operationId = "eliminarProductoDelCarrito")
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
                responseCode = "404", description = "Carrito, producto o usuario no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor (o stock de producto insuficiente)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<CarritoResponse>> actualizarCarrito(
        @Parameter(description = "ID del carrito modificado", required = true, example = "1") @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos actualizados de carrito",
            required = true
        )
        @Valid @RequestBody CarritoRequest request
    ) {
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            Carrito carrito = requestMapper.toCarrito(request);
            carrito.setId(id);
            
            // Actualizar producto
            Carrito actualizado = carritoService.update(carrito);
            CarritoResponse response = new CarritoResponse(actualizado);

            // Generar EntityModel con links
            EntityModel<CarritoResponse> carritoModel = EntityModel.of(response,
                linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(id)).withSelfRel(),
                linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("listarCarrito"),
                linkTo(methodOn(CarritoController.class).eliminarProductoDelCarrito(id)).withRel("eliminar")
            );

            return ResponseEntity.ok(carritoModel);
        }
        return ResponseEntity.notFound().build();
    }

    //---------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar producto del carrito",
        description = "Elimina el carrito en la base de datos con el ID ingresado. El acceso requiere rol ADMIN o CLIENTE.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Carrito eliminado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = EliminadoMessageDTO.class)),
                links = {
                    @Link(name = "listarCarritos", description = "Enlace a lista con todos los productos", operationId = "listarCarrito"),
                    @Link(name = "guardarCarrito", description = "Enlace para crear nuevo producto", operationId = "agregarProductoAlCarrito")
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
                responseCode = "404", description = "Carrito no encontrado",
                content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )}
    )
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarProductoDelCarrito(
        @Parameter(description = "ID del carrito que se desea eliminar", required = true, example = "1") @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            
            EntityModel<Map<String, String>> responseModel = EntityModel.of(
                Map.of("message", "Carrito eliminado exitosamente"),
                linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("listarProductos"),
                linkTo(methodOn(CarritoController.class).agregarProductoAlCarrito(new CarritoRequest())).withRel("guardarCarrito")
            );
            
            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build();
    }
}
