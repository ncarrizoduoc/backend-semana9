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
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

}
