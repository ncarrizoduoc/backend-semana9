package com.minimarket.minimarket.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minimarket.minimarket.dto.VentaRequest;
import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.repository.UsuarioRepository;

@Component
public class VentaRequestMapper {

    @Autowired
    private UsuarioRepository usuarioRepo;

    public Venta toVenta(VentaRequest request){
        Usuario usuario = usuarioRepo.findById(request.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con el ID: " + request.getUsuarioId()));

        Venta venta = Venta.builder()
            .id(request.getId())
            .usuario(usuario)
            .fecha(request.getFecha())
            .detalles(new ArrayList<DetalleVenta>())
            .build();

        return venta;

    }

}
