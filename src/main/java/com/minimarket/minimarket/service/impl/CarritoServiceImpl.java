package com.minimarket.minimarket.service.impl;

import com.minimarket.minimarket.entity.Carrito;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.exception.ResourceNotFoundException;
import com.minimarket.minimarket.exception.StockInsuficienteException;
import com.minimarket.minimarket.repository.CarritoRepository;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepo;

    @Override
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    public Carrito findById(Long id) {
        return carritoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Carrito save(Carrito carrito) {
        // Validar si hay suficiente stock del producto para el carrito
        validarStock(carrito);

        // Actualizar stock de producto
        Producto producto = carrito.getProducto();
        producto.setStock(producto.getStock() - carrito.getCantidad());
        productoRepo.save(producto);

        // Actualizar carrito
        carrito.setProducto(producto);
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito update(Carrito carrito){
        // Se obtiene el stock original del producto en el carrito
        Carrito carritoOriginal = carritoRepository.findById(carrito.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No existe el carrito con el ID ingresado"));
        
        Producto productoOriginal = carritoOriginal.getProducto();
        Producto productoNuevo = productoRepo.findById(carrito.getProducto().getId())
            .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con el ID ingresado"));

        // Se compara ID para saber si cambio el producto asociado al carrito
        boolean esMismoProducto = productoOriginal.getId().equals(productoNuevo.getId());

        productoOriginal.setStock(productoOriginal.getStock() + carritoOriginal.getCantidad());
        productoRepo.save(productoOriginal);

        if(esMismoProducto){
            carrito.setProducto(productoOriginal);
        } else {
            carrito.setProducto(productoNuevo);
        }
        return this.save(carrito);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Carrito carrito = carritoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("No existe el carrito con el ID ingresado"));
        
        // Revertir stock de producto
        Producto producto = carrito.getProducto();
        producto.setStock(producto.getStock() + carrito.getCantidad());
        productoRepo.save(producto);

        // Eliminar producto
        carritoRepository.deleteById(id);
    }

    @Override
    public List<Carrito> findByUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    // Metodo que valida que haya stock suficiente de un producto agregado al carrito
    private void validarStock(Carrito carrito){
        Producto producto = carrito.getProducto();
        // Si no hay stock suficiente, se lanza una excepcion
        if (producto.getStock() < carrito.getCantidad()){
            throw new StockInsuficienteException("Error al agregar al carrito: No hay stock suficiente del producto: "
                + producto.getNombre());
        }
    }
}
