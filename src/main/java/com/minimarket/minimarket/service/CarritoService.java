package com.minimarket.minimarket.service;

import com.minimarket.minimarket.entity.Carrito;

import java.util.List;

public interface CarritoService {
    List<Carrito> findAll();
    Carrito findById(Long id);
    Carrito save(Carrito carrito);
    void deleteById(Long id);
    List<Carrito> findByUsuarioId(Long usuarioId);

    Carrito update(Carrito carrito);
}
