package com.minimarket.minimarket.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minimarket.minimarket.dto.InventarioRequest;
import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.repository.ProductoRepository;

import static com.minimarket.minimarket.config.CONSTANTS.*;

@Component
public class InventarioRequestMapper {

    @Autowired
    private ProductoRepository productoRepo;

    public Inventario toInventario(InventarioRequest request){
        Inventario inventario = new Inventario();
        inventario.setId(request.getId());
        inventario.setCantidad(request.getCantidad());
        inventario.setTipoMovimiento(request.getTipoMovimiento());
        inventario.setFechaMovimiento(request.getFechaMovimiento());
        
        // Obtener producto por su ID y asociarlo al inventario
        Long productoId = request.getProductoId();
        if (productoRepo.existsById(productoId)){
            Producto producto = productoRepo.findById(productoId).get();
            inventario.setProducto(producto);
        } else {
            throw new ResourceNotFoundException("No existe el producto con el ID ingresado");
        }

        return inventario;

    }

}
