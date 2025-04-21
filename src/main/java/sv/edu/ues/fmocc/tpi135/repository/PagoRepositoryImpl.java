package sv.edu.ues.fmocc.tpi135.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.entity.Pago;
import sv.edu.ues.fmocc.tpi135.repository.PagoRepository;

/**
 * Implementación de PagoRepository para operaciones CRUD con JPA
 */
@ApplicationScoped
public class PagoRepositoryImpl implements PagoRepository {
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;
    
    @Override
    @Transactional
    public Pago crear(Pago pago) {
        em.persist(pago);
        em.flush();
        return pago;
    }
    
    @Override
    @Transactional
    public Pago actualizar(Pago pago) {
        return em.merge(pago);
    }
    
    @Override
    public Optional<Pago> encontrarPorId(Long id) {
        Pago pago = em.find(Pago.class, id);
        return Optional.ofNullable(pago);
    }
    
    @Override
    public List<Pago> listarTodos() {
        TypedQuery<Pago> query = em.createNamedQuery("Pago.findAll", Pago.class);
        return query.getResultList();
    }
    
    @Override
    public List<Pago> buscarPorIdOrden(Long idOrden) {
        TypedQuery<Pago> query = em.createNamedQuery("Pago.findByIdOrden", Pago.class);
        query.setParameter("idOrden", idOrden);
        return query.getResultList();
    }
    
    @Override
    public List<Pago> buscarPorFecha(Date fecha) {
        TypedQuery<Pago> query = em.createNamedQuery("Pago.findByFecha", Pago.class);
        query.setParameter("fecha", fecha);
        return query.getResultList();
    }
    
    @Override
    public List<Pago> buscarPorMetodoPago(String metodoPago) {
        TypedQuery<Pago> query = em.createNamedQuery("Pago.findByMetodoPago", Pago.class);
        query.setParameter("metodoPago", metodoPago);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public boolean eliminar(Long id) {
        Optional<Pago> pagoOpt = encontrarPorId(id);
        if (pagoOpt.isPresent()) {
            em.remove(pagoOpt.get());
            return true;
        }
        return false;
    }
}