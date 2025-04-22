package sv.edu.ues.fmocc.tpi135.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import sv.edu.ues.fmocc.tpi135.controller.*;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        // Registrar todos los controladores
        resources.add(ProductoController.class);
        resources.add(TipoProductoController.class);
        resources.add(ComboController.class);
        resources.add(OrdenController.class);
        resources.add(PagoController.class);
        // Agregar más controladores según sea necesario
        return resources;
    }
}