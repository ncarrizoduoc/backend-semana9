package com.minimarket.minimarket.service.impl;

import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.VentaRepository;
import com.minimarket.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta save(Venta venta) {
        verificarStock(venta); // Se verifica que haya suficiente stock de productos
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    // Metodo que calcula el valor total de una venta
    public Double calcularTotal(Venta venta){
        Double total = Double.valueOf(0);
        if (!venta.getDetalles().isEmpty()){
            // Se multiplica el precio de cada producto por la cantidad comprada y se suma al total
            for (DetalleVenta detalle : venta.getDetalles()){
                Double totalProducto = detalle.getPrecio() * detalle.getCantidad();
                total += totalProducto;
            }
        }
        return total;
    }

    // Metodo que verifica si hay stock suficiente de los productos en una venta
    private void verificarStock(Venta venta){
        // Se itera sobre la lista de detalles de ventas para validar los stocks de cada producto en la venta
        for (DetalleVenta detalle : venta.getDetalles()){
            Producto producto = detalle.getProducto();
            
            int stockDisponible = producto.getStock();
            int stockVenta = detalle.getCantidad();
            
            // Si no hay stock suficiente del producto, lanza una excepcion
            if (stockVenta > stockDisponible){
                throw new StockInsuficienteException("No hay suficiente stock del producto: " + producto.getNombre());
            }
        }
    }
}
