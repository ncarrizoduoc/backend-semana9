package com.minimarket.minimarket.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.minimarket.minimarket.repository.CategoriaRepository;
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

    @Override
    public void run(ApplicationArguments args) throws Exception{

        //Crear los roles en la base de datos
        for (RolEnum rolEnum : RolEnum.values()){
            if (rolRepo.findByNombre(rolEnum.name()).isEmpty()){
                // Crear rol y guardarlo en la base de datos
                Rol rol = new Rol();
                rol.setNombre(rolEnum.name());
                rolRepo.save(rol);

                // Se crea un usuario con el rol y se guarda en la base de datos
                Usuario usuario = new Usuario();
                usuario.setUsername(rol.getNombre().toLowerCase());
                usuario.setPassword(passwordEncoder.encode(rol.getNombre().toLowerCase() + "123"));
                Set<Rol> roles = new HashSet<Rol>(Arrays.asList(rol));
                usuario.setRoles(roles);
                usuarioRepo.save(usuario);
            }

        }

        // Categoria de ejemplo para facilitar pruebas de endpoints de ProductoController
        if (categoriaRepo.findAll().isEmpty()){
            Categoria categoria = new Categoria(null, "Abbarrotes", new ArrayList<Producto>());
            categoriaRepo.save(categoria);
        }

    }

}
