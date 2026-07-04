package com.minimarket.minimarket.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.minimarket.minimarket.entity.Usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
DTO para respuestas que incluyan objetos Usuario. Por seguridad, este DTO omite el 
atributo password, correspondiente a la contrasena del usuario.
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para objetos Usuario")
public class UsuarioResponse {
    @Schema(description = "ID del usuario")
    private Long id;
    @Schema(description = "Nombre de usuario")
    private String username;
    @Schema(description = "Rol(es) del usuario")
    private Set<String> roles;

    // Constructor a partir de usuario
    public UsuarioResponse(Usuario usuario){
        id = usuario.getId();
        username = usuario.getUsername();
        roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toSet());
    }
}
