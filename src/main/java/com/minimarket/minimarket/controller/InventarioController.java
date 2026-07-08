package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.dto.InventarioRequest;
import com.minimarket.minimarket.dto.InventarioResponse;
import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.mapper.InventarioRequestMapper;
import com.minimarket.minimarket.service.InventarioService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minimarket.minimarket.security.util.InputSanitizer.*;


@RestController
@RequestMapping("/api/inventario")
@Tag(
    name = "Inventario", 
    description = "API para gestionar movimientos de inventario en base de datos y realizar consultas.")
@SecurityRequirement(name = "bearerAuth")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InventarioRequestMapper requestMapper;

    @GetMapping
    public List<InventarioResponse> listarMovimientosDeInventario() {
        return inventarioService.findAll().stream()
            .map(InventarioResponse::new)
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponse> obtenerMovimientoPorId(
        @PathVariable Long id
    ) {
        Inventario inventario = inventarioService.findById(id);
        return (inventario != null) ? ResponseEntity.ok(new InventarioResponse(inventario)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public InventarioResponse registrarMovimiento(
        @Valid @RequestBody InventarioRequest request
    ) {
        sanitizarInventario(request);
        Inventario inventario = requestMapper.toInventario(request);
        inventario.setId(null);
        return new InventarioResponse(inventarioService.save(inventario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioResponse> actualizarMovimiento(
            @PathVariable Long id, 
            @Valid @RequestBody InventarioRequest request) {
        sanitizarInventario(request);
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            Inventario inventario = requestMapper.toInventario(request);
            inventario.setId(id);
            return ResponseEntity.ok(new InventarioResponse(inventarioService.save(inventario)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(
        @PathVariable Long id
    ) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarInventario(InventarioRequest inventario){
        inventario.setTipoMovimiento(sanitizeInput(inventario.getTipoMovimiento()));
    }

}
