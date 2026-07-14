package com.minimarket.minimarket.service.impl;

import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.DetalleVentaRepository;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.service.DetalleVentaService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetalleVentaServiceImpl implements DetalleVentaService {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ProductoRepository productoRepo;

    @Override
    public List<DetalleVenta> findAll() {
        return detalleVentaRepository.findAll();
    }

    @Override
    public DetalleVenta findById(Long id) {
        return detalleVentaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public DetalleVenta save(DetalleVenta detalleVenta) {
        Producto producto = detalleVenta.getProducto();
        producto.setStock(producto.getStock() - detalleVenta.getCantidad());
        validarStock(producto);

        return detalleVentaRepository.save(detalleVenta);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        DetalleVenta detalle = detalleVentaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("No existe el detalle de venta con el ID ingresado"));

        // Actualizar stock del producto
        Producto producto = detalle.getProducto();
        producto.setStock(producto.getStock() + detalle.getCantidad());
        productoRepo.save(producto);
        
        // Eliminar detalle de venta
        detalleVentaRepository.deleteById(id);
    }

    @Override
    public List<DetalleVenta> findByVentaId(Long ventaId) {
        return detalleVentaRepository.findByVentaId(ventaId);
    }

    public void validarStock(Producto producto){
        if (producto.getStock() < 0){
            throw new StockInsuficienteException("No hay suficiente stock del producto: " + producto.getNombre());
        }
    }
}
