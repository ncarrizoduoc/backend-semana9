package com.minimarket.minimarket.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minimarket.minimarket.exception.ErrorResponse;
import com.minimarket.minimarket.openapi.dto.BadRequestResponse;
import com.minimarket.minimarket.security.model.JwtResponse;
import com.minimarket.minimarket.security.model.LoginRequest;
import com.minimarket.minimarket.security.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacion", description = "API para autenticacion (login) con usuario y contrasena")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(
        summary = "Autenticacion con usuario y contrasena",
        description = "Permite al usuario autenticarse con nombre de usuario y contrasena. Retorna un JWT si la autenticacion es exitosa",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Autenticacion exitosa (Retorna JWT)",
                content = @Content(
                    mediaType = "application/json", 
                    examples = @ExampleObject(
                        name = "Respuesta a autenticacion exitosa",
                        description = "Respuesta con JWT valido",
                        value = """
                                {
                                    "token": "tokenJWTValido"
                                }
                                """
                    )
                )

            ),
            @ApiResponse(
                responseCode = "400",
                description = "Solicitud incorrecta (formato de datos no valido)",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BadRequestResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "No autorizado (credenciales invalidas)",
                content = @Content(
                    mediaType = "application/json", 
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        name = "Respuesta a autenticacion fallida",
                        description = "Mensaje de error por credenciales invalidas",
                        value = """
                                {
                                    "timestamp": "2026-07-14T16:33:17.0741857",
                                    "status": 401,
                                    "error": "Unauthorized",
                                    "message": "Invalid username or password",
                                    "path": "uri=/auth/login"
                                }
                                """
                    )
                )
            )
        }
    )
    public ResponseEntity<JwtResponse> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciales de usuario (nombre de usuario y contrasena)",
            required = true)
        @Valid @RequestBody LoginRequest request
    ){
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(new JwtResponse(token));

    }


}
