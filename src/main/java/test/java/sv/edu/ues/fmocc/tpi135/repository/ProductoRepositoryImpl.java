package test.java.sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.entity.Producto;
import sv.edu.ues.fmocc.tpi135.repository.ProductoRepository;

/**
 * Implementación de ProductoRepository para operaciones CRUD con JPA
 */
@ApplicationScoped
public class ProductoRepositoryImpl implements ProductoRepository {
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;
    
    @Override
    @Transactional
    public Producto crear(Producto producto) {
        em.persist(producto);
        em.flush();
        return producto;
    }
    
    @Override
    @Transactional
    public Producto actualizar(Producto producto) {
        return em.merge(producto);
    }
    
    @Override
    public Optional<Producto> encontrarPorId(Long id) {
        Producto producto = em.find(Producto.class, id);
        return Optional.ofNullable(producto);
    }
    
    @Override
    public List<Producto> listarTodos() {
        TypedQuery<Producto> query = em.createNamedQuery("Producto.findAll", Producto.class);
        return query.getResultList();
    }
    
    @Override
    public List<Producto> buscarPorNombre(String nombre) {
        TypedQuery<Producto> query = em.createNamedQuery("Producto.findByNombre", Producto.class);
        query.setParameter("nombre", nombre);
        return query.getResultList();
    }
    
    @Override
    public List<Producto> buscarPorEstado(Boolean activo) {
        TypedQuery<Producto> query = em.createNamedQuery("Producto.findByActivo", Producto.class);
        query.setParameter("activo", activo);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public boolean eliminar(Long id) {
        Optional<Producto> productoOpt = encontrarPorId(id);
        if (productoOpt.isPresent()) {
            em.remove(productoOpt.get());
            return true;
        }
        return false;
    }
}