package sv.edu.ues.fmocc.tpi135.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Configuración principal de la aplicación JAX-RS
 */

@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // La configuración automática de JAX-RS registrará los recursos
}