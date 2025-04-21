package sv.edu.ues.fmocc.tpi135.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;

/**
 * Implementación de OrdenRepository para operaciones CRUD con JPA
 */
@ApplicationScoped
public class OrdenRepositoryImpl implements OrdenRepository {
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;
    
    @Override
    @Transactional
    public Orden crear(Orden orden) {
        em.persist(orden);
        em.flush();
        return orden;
    }
    
    @Override
    @Transactional
    public Orden actualizar(Orden orden) {
        return em.merge(orden);
    }
    
    @Override
    public Optional<Orden> encontrarPorId(Long id) {
        Orden orden = em.find(Orden.class, id);
        return Optional.ofNullable(orden);
    }
    
    @Override
    public List<Orden> listarTodas() {
        TypedQuery<Orden> query = em.createNamedQuery("Orden.findAll", Orden.class);
        return query.getResultList();
    }
    
    @Override
    public List<Orden> buscarPorFecha(Date fecha) {
        TypedQuery<Orden> query = em.createNamedQuery("Orden.findByFecha", Orden.class);
        query.setParameter("fecha", fecha);
        return query.getResultList();
    }
    
    @Override
    public List<Orden> buscarPorSucursal(String sucursal) {
        TypedQuery<Orden> query = em.createNamedQuery("Orden.findBySucursal", Orden.class);
        query.setParameter("sucursal", sucursal);
        return query.getResultList();
    }
    
    @Override
    public List<Orden> buscarPorAnulada(Boolean anulada) {
        TypedQuery<Orden> query = em.createNamedQuery("Orden.findByAnulada", Orden.class);
        query.setParameter("anulada", anulada);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public boolean anular(Long id) {
        Optional<Orden> ordenOpt = encontrarPorId(id);
        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();
            orden.setAnulada(true);
            em.merge(orden);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional
    public boolean eliminar(Long id) {
        Optional<Orden> ordenOpt = encontrarPorId(id);
        if (ordenOpt.isPresent()) {
            em.remove(ordenOpt.get());
            return true;
        }
        return false;
    }
}