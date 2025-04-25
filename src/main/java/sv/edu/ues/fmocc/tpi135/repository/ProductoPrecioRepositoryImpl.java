package sv.edu.ues.fmocc.tpi135.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.TemporalType;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.entity.ProductoPrecio;

/**
 * Implementación de ProductoPrecioRepository para operaciones CRUD con JPA
 */
@ApplicationScoped
public class ProductoPrecioRepositoryImpl implements ProductoPrecioRepository {
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;
    
    @Override
    @Transactional
    public ProductoPrecio crear(ProductoPrecio productoPrecio) {
        em.persist(productoPrecio);
        em.flush();
        return productoPrecio;
    }
    
    @Override
    @Transactional
    public ProductoPrecio actualizar(ProductoPrecio productoPrecio) {
        return em.merge(productoPrecio);
    }
    
    @Override
    public Optional<ProductoPrecio> encontrarPorId(Long id) {
        ProductoPrecio productoPrecio = em.find(ProductoPrecio.class, id);
        return Optional.ofNullable(productoPrecio);
    }
    
    @Override
    public List<ProductoPrecio> listarTodos() {
        TypedQuery<ProductoPrecio> query = em.createNamedQuery("ProductoPrecio.findAll", ProductoPrecio.class);
        return query.getResultList();
    }
    
    @Override
    public List<ProductoPrecio> buscarPorIdProducto(Long idProducto) {
        TypedQuery<ProductoPrecio> query = em.createNamedQuery("ProductoPrecio.findByIdProducto", ProductoPrecio.class);
        query.setParameter("idProducto", idProducto);
        return query.getResultList();
    }
    
    @Override
    public List<ProductoPrecio> buscarVigentesPorFecha(Date fecha) {
        TypedQuery<ProductoPrecio> query = em.createNamedQuery("ProductoPrecio.findVigentesByFecha", ProductoPrecio.class);
        query.setParameter("fecha", fecha, TemporalType.DATE);
        return query.getResultList();
    }
    
    @Override
    public Optional<ProductoPrecio> buscarPrecioVigente(Long idProducto, Date fecha) {
        TypedQuery<ProductoPrecio> query = em.createNamedQuery("ProductoPrecio.findVigente", ProductoPrecio.class);
        query.setParameter("idProducto", idProducto);
        query.setParameter("fecha", fecha, TemporalType.DATE);
        query.setMaxResults(1);
        
        List<ProductoPrecio> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultList.get(0));
    }
    
    @Override
    @Transactional
    public boolean eliminar(Long id) {
        Optional<ProductoPrecio> productoPrecioOpt = encontrarPorId(id);
        if (productoPrecioOpt.isPresent()) {
            em.remove(productoPrecioOpt.get());
            return true;
        }
        return false;
    }
}