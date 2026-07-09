package com.minimarket.minimarket.service.impl;

import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.TipoMovimientoNoValidoException;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.InventarioRepository;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    private static final String MENSAJE_TIPO_NO_VALIDO = "El tipo de movimiento no es valido";

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepo;

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Inventario save(Inventario inventario) {
        Producto producto = inventario.getProducto();

        // Actualizar stock de producto segun entrada o salida de producto
        if (inventario.getTipoMovimiento().equals("Entrada")){
            producto.setStock(producto.getStock() + inventario.getCantidad());
        } else if (inventario.getTipoMovimiento().equals("Salida")){
            producto.setStock(producto.getStock() - inventario.getCantidad());
            validarStock(producto);
        } else {
            throw new TipoMovimientoNoValidoException("Error al guardar: " + MENSAJE_TIPO_NO_VALIDO);
        }
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public Inventario update(Inventario inventario){
        Inventario inventarioOriginal = inventarioRepository.findById(inventario.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No existe el inventario con el ID ingresado"));
        Producto productoOriginal = inventarioOriginal.getProducto();
        Producto producto = inventario.getProducto();
        
        // Verificar si es necesario actualizar el stock de productos
        boolean esMismoProducto = productoOriginal.getId().equals(producto.getId());
        boolean esMismaCantidad = inventarioOriginal.getCantidad().equals(inventario.getCantidad());
        boolean esMismoTipo  = inventarioOriginal.getTipoMovimiento().equals(inventario.getTipoMovimiento());
        
        // Mantener consistencia de stock en base de datos
        if (!esMismoProducto || !esMismaCantidad || !esMismoTipo){
            // Restaurar stock del producto antiguo
            if (inventarioOriginal.getTipoMovimiento().equals("Entrada")){
                productoOriginal.setStock(productoOriginal.getStock() - inventarioOriginal.getCantidad());
            } else if(inventarioOriginal.getTipoMovimiento().equals("Salida")){
                productoOriginal.setStock(productoOriginal.getStock() + inventarioOriginal.getCantidad());
            } else {
                throw new TipoMovimientoNoValidoException("Error al actualizar: " + MENSAJE_TIPO_NO_VALIDO);
            }

            if (esMismoProducto){
                producto = productoOriginal;
            }

            // Actualizar stock del producto asociado al inventario
            if (inventario.getTipoMovimiento().equals("Entrada")){
                producto.setStock(producto.getStock() + inventario.getCantidad());
            } else if(inventario.getTipoMovimiento().equals("Salida")){
                producto.setStock(producto.getStock() - inventario.getCantidad());
            } else {
                throw new TipoMovimientoNoValidoException("Error al actualizar: " + MENSAJE_TIPO_NO_VALIDO);
            }
            
            validarStock(productoOriginal);
            productoRepo.save(productoOriginal);
        }

        return inventarioRepository.save(inventario);
    }

    // Metodo que valida que el stock de un producto no sea negativo
    // Si el stock es negativo, lanza una excepcion
    public void validarStock(Producto producto){
        if (producto.getStock() < 0){
            throw new StockInsuficienteException(
                "Error al registrar movimiento de inventario: No hay stock suficiente del producto: "
                + producto.getNombre());
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Inventario inventario = inventarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("No existe el inventario con el ID ingresado"));
        
        // Revertir el stock del producto
        Producto producto = inventario.getProducto();
        if (inventario.getTipoMovimiento().equals("Entrada")){
            producto.setStock(producto.getStock() - inventario.getCantidad());
            validarStock(producto);
        } else if (inventario.getTipoMovimiento().equals("Salida")){
            producto.setStock(producto.getStock() + inventario.getCantidad());
        } else {
            throw new TipoMovimientoNoValidoException("Error al eliminar: " + MENSAJE_TIPO_NO_VALIDO);
        }
        productoRepo.save(producto);

        // Eliminar movimiento de inventario
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }
}
