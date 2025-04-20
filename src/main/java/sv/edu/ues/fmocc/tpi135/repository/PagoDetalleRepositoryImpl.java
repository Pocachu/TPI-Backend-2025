package sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.entity.PagoDetalle;

/**
 * Implementación de PagoDetalleRepository para operaciones CRUD con JPA
 */
@ApplicationScoped
public class PagoDetalleRepositoryImpl implements PagoDetalleRepository {
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;
    
    @Override
    @Transactional
    public PagoDetalle crear(PagoDetalle pagoDetalle) {
        em.persist(pagoDetalle);
        em.flush();
        return pagoDetalle;
    }
    
    @Override
    @Transactional
    public PagoDetalle actualizar(PagoDetalle pagoDetalle) {
        return em.merge(pagoDetalle);
    }
    
    @Override
    public Optional<PagoDetalle> encontrarPorId(Long id) {
        PagoDetalle pagoDetalle = em.find(PagoDetalle.class, id);
        return Optional.ofNullable(pagoDetalle);
    }
    
    @Override
    public List<PagoDetalle> listarTodos() {
        TypedQuery<PagoDetalle> query = em.createNamedQuery("PagoDetalle.findAll", PagoDetalle.class);
        return query.getResultList();
    }
    
    @Override
    public List<PagoDetalle> buscarPorIdPago(Long idPago) {
        TypedQuery<PagoDetalle> query = em.createNamedQuery("PagoDetalle.findByIdPago", PagoDetalle.class);
        query.setParameter("idPago", idPago);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public boolean eliminar(Long id) {
        Optional<PagoDetalle> pagoDetalleOpt = encontrarPorId(id);
        if (pagoDetalleOpt.isPresent()) {
            em.remove(pagoDetalleOpt.get());
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional
    public int eliminarPorIdPago(Long idPago) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<PagoDetalle> delete = cb.createCriteriaDelete(PagoDetalle.class);
        Root<PagoDetalle> root = delete.from(PagoDetalle.class);
        delete.where(cb.equal(root.get("idPago"), idPago));
        
        return em.createQuery(delete).executeUpdate();
    }
}