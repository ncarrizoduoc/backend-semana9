package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.CategoriaRequest;
import com.minimarket.minimarket.dto.CategoriaResponse;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.service.CategoriaService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minimarket.minimarket.security.util.InputSanitizer.*;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categoria", description = "API para gestionar categorias en base de datos y realizar consultas.")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaResponse> listarCategorias() {
        return categoriaService.findAll().stream().map(CategoriaResponse::new).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        return (categoria != null) ? ResponseEntity.ok(new CategoriaResponse(categoria)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public CategoriaResponse guardarCategoria(@Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = request.toCategoria();
        sanitizarCategoria(categoria);
        categoria.setId(null);
        return new CategoriaResponse(categoriaService.save(categoria));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Long id, @Valid @RequestBody Categoria categoria) {
        sanitizarCategoria(categoria);
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            categoria.setId(id);
            return ResponseEntity.ok(categoriaService.save(categoria));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarCategoria(Categoria categoria){
        categoria.setNombre(sanitizeInput(categoria.getNombre()));
    }
}
