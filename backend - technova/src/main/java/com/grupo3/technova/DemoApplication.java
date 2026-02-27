package com.grupo3.technova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// @SpringBootApplication es una anotación que agrupa tres cosas:
// 1. Le dice a Spring que esta es la clase principal de la aplicación.
// 2. Activa el escaneo automático de componentes. Spring busca todas las clases con @RestController, @Repository, @Configuration, etc. y las registra.
// 3. Activa la autoconfiguración. Spring configura automáticamente el DataSource, el servidor web, etc, usando el application.properties.
@SpringBootApplication
public class DemoApplication {

	// Punto de entrada de la aplicación — el main que arranca todo.
    // SpringApplication.run() lanza el servidor web y deja la API escuchando peticiones.
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
