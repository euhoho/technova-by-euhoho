package com.grupo3.technova.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration le dice a Spring que esta clase contiene configuración.
// Spring la lee al arrancar y aplica todo lo que encuentre dentro.
@Configuration
public class CorsConfig {

    // @Bean le dice a Spring que este método devuelve un objeto que él debe gestionar.
    // Spring lo ejecuta al arrancar y registra el resultado automáticamente.
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // Acepta peticiones de cualquier origen.
                        .allowedOrigins("*")
                        // Métodos HTTP permitidos
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        // Acepta cualquier cabecera HTTP
                        .allowedHeaders("*");
            }
        };
    }
}