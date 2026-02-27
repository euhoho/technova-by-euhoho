package com.grupo3.technova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;

// @SpringBootApplication es una anotación que agrupa tres cosas:
// 1. Le dice a Spring que esta es la clase principal de la aplicación.
// 2. Activa el escaneo automático de componentes. Spring busca todas las clases con @RestController, @Repository, @Configuration, etc. y las registra.
// 3. Activa la autoconfiguración. Spring configura automáticamente el DataSource, el servidor web, etc, usando el application.properties.
@SpringBootApplication
public class DemoApplication {

    // Punto de entrada de la aplicación — el main que arranca todo.
    // SpringApplication.run() lanza el servidor web y deja la API escuchando peticiones.
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        // Verificamos la conexión a la BD nada más arrancar
        try {
            DataSource dataSource = context.getBean(DataSource.class);
            Connection con = dataSource.getConnection();
            con.close();
            System.out.println("Conexión a MySQL establecida correctamente");
        } catch (Exception e) {
            System.out.println("Error al conectar con MySQL: " + e.getMessage());
        }
    }
}