package com.minimarket.minimarket.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minimarket.minimarket.dto.DetalleVentaRequest;
import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.repository.VentaRepository;

@Component
public class DetalleVentaRequestMapper {

    @Autowired
    private VentaRepository ventaRepo;

    @Autowired
    private ProductoRepository productoRepo;

    public DetalleVenta toDetalleVenta(DetalleVentaRequest request){
        Venta venta = ventaRepo.findById(request.getVentaId())
            .orElseThrow(() -> new ResourceNotFoundException("No existe la venta con ID: " + request.getVentaId()));

        Producto producto = productoRepo.findById(request.getProductoId())
            .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con ID: " + request.getProductoId()));

        DetalleVenta detalle = DetalleVenta.builder()
            .id(request.getId())
            .venta(venta)
            .producto(producto)
            .cantidad(request.getCantidad())
            .precio(request.getPrecio())
            .build();
        
        return detalle;

    }

}
