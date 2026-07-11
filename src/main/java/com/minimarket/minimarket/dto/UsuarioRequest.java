package com.minimarket.minimarket.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder()
@Schema(description = "DTO para ingresar objetos Usuario")
public class UsuarioRequest {
    @Schema(description = "ID del usuario", example = "0")
    private Long id;

    @Schema(description = "Nombre de usuario", example = "usuarioPrueba")
    @NotNull(message = "Debe ingresar un nombre de usuario")
    @NotBlank(message = "El nombre de usuario no puede ser un texto en blanco")
    private String username;

    @Schema(description = "Contrasena del usuario", example = "contrasenaPrueba")
    @NotNull(message = "Debe ingresar una contrasena")
    @NotBlank(message = "La contrasena no puede ser un texto en blanco")
    private String password;

    @Schema(description = "Lista de IDs de roles del usuario", example = "[1, 2]")
    private List<Long> rolesId;

}
