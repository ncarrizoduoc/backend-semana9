package com.minimarket.minimarket.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@Tag(name = "Hola Mundo", description = "API de bienvenida")
public class HolaMundoController {

    @GetMapping("/public/hola")
    @Operation(
        summary = "Bienvenida",
        description = "Retorna un saludo estandar. El acceso es público",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Saludo generado exitosamente",
                content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(
                        name = "Saludo",
                        description = "Respuesta con mensaje de saludo",
                        value = "¡Hola Mundo!"
                    )
                )

            )
        }
    )
    public String holaMundo() {
        return "¡Hola Mundo!";
    }
}
