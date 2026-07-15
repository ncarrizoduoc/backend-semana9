package com.minimarket.minimarket.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.minimarket.minimarket.repository.CategoriaRepository;
import com.minimarket.minimarket.repository.ProductoRepository;
import com.minimarket.minimarket.repository.RolRepository;
import com.minimarket.minimarket.repository.UsuarioRepository;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;

@Component
public class DataInitializer implements ApplicationRunner{
    @Autowired
    private RolRepository rolRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoriaRepository categoriaRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @Override
    public void run(ApplicationArguments args) throws Exception{

        //Crear los roles en la base de datos
        for (RolEnum rolEnum : RolEnum.values()){
            if (rolRepo.findByNombre(rolEnum.name()).isEmpty()){
                // Crear rol y guardarlo en la base de datos
                Rol rol = Rol.builder()
                    .nombre(rolEnum.name())
                    .build();
                rolRepo.save(rol);

                // Se crea un usuario con el rol y se guarda en la base de datos
                Usuario usuario = Usuario.builder()
                    .username(rol.getNombre().toLowerCase())
                    .password(passwordEncoder.encode(rol.getNombre().toLowerCase() + "123"))
                    .roles(new HashSet<Rol>(Arrays.asList(rol)))
                    .build();
                usuarioRepo.save(usuario);
            }

        }

        // Categoria de ejemplo para facilitar pruebas de endpoints de ProductoController
        if (categoriaRepo.findAll().isEmpty()){
            Categoria categoria = Categoria.builder()
                .id(null)
                .nombre("Abarrotes")
                .productos(new ArrayList<Producto>())
                .build();
            categoriaRepo.save(categoria);
            
            // Se crea tambien un producto para facilitar pruebas de InventarioController y CarritoController
            if (productoRepo.findAll().isEmpty()){
                Producto producto = Producto.builder()
                    .id(null)
                    .nombre("Arroz")
                    .precio(2690.0)
                    .stock(10)
                    .categoria(categoria)
                    .build();
                productoRepo.save(producto);
            }
        }

    }

}
