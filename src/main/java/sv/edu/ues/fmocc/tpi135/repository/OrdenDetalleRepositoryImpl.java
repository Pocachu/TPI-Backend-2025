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
import sv.edu.ues.fmocc.tpi135.entity.OrdenDetalle;
import sv.edu.ues.fmocc.tpi135.entity.OrdenDetallePK;
import sv.edu.ues.fmocc.tpi135.repository.OrdenDetalleRepository;

/**
 * Implementación de OrdenDetalleRepository para operaciones CRUD con JPA
 */
@ApplicationScoped
public class OrdenDetalleRepositoryImpl implements OrdenDetalleRepository {
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;
    
    @Override
    @Transactional
    public OrdenDetalle crear(OrdenDetalle ordenDetalle) {
        em.persist(ordenDetalle);
        em.flush();
        return ordenDetalle;
    }
    
    @Override
    @Transactional
    public OrdenDetalle actualizar(OrdenDetalle ordenDetalle) {
        return em.merge(ordenDetalle);
    }
    
    @Override
    public Optional<OrdenDetalle> encontrarPorId(OrdenDetallePK pk) {
        OrdenDetalle ordenDetalle = em.find(OrdenDetalle.class, pk);
        return Optional.ofNullable(ordenDetalle);
    }
    
    @Override
    public Optional<OrdenDetalle> encontrarPorId(Long idOrden, Long idProductoPrecio) {
        OrdenDetallePK pk = new OrdenDetallePK(idOrden, idProductoPrecio);
        return encontrarPorId(pk);
    }
    
    @Override
    public List<OrdenDetalle> listarTodos() {
        TypedQuery<OrdenDetalle> query = em.createNamedQuery("OrdenDetalle.findAll", OrdenDetalle.class);
        return query.getResultList();
    }
    
    @Override
    public List<OrdenDetalle> buscarPorIdOrden(Long idOrden) {
        TypedQuery<OrdenDetalle> query = em.createNamedQuery("OrdenDetalle.findByIdOrden", OrdenDetalle.class);
        query.setParameter("idOrden", idOrden);
        return query.getResultList();
    }
    
    @Override
    public List<OrdenDetalle> buscarPorIdProductoPrecio(Long idProductoPrecio) {
        TypedQuery<OrdenDetalle> query = em.createNamedQuery("OrdenDetalle.findByIdProductoPrecio", OrdenDetalle.class);
        query.setParameter("idProductoPrecio", idProductoPrecio);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public boolean eliminar(OrdenDetallePK pk) {
        Optional<OrdenDetalle> ordenDetalleOpt = encontrarPorId(pk);
        if (ordenDetalleOpt.isPresent()) {
            em.remove(ordenDetalleOpt.get());
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional
    public boolean eliminar(Long idOrden, Long idProductoPrecio) {
        OrdenDetallePK pk = new OrdenDetallePK(idOrden, idProductoPrecio);
        return eliminar(pk);
    }
    
    @Override
    @Transactional
    public int eliminarPorIdOrden(Long idOrden) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<OrdenDetalle> delete = cb.createCriteriaDelete(OrdenDetalle.class);
        Root<OrdenDetalle> root = delete.from(OrdenDetalle.class);
        delete.where(cb.equal(root.get("idOrden"), idOrden));
        
        return em.createQuery(delete).executeUpdate();
    }
}