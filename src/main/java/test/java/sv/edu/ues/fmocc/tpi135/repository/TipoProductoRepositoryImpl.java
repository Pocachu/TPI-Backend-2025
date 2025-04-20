package test.java.sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.entity.TipoProducto;
import sv.edu.ues.fmocc.tpi135.repository.TipoProductoRepository;

/**
 * Implementación de TipoProductoRepository para operaciones CRUD con JPA
 */
@ApplicationScoped
public class TipoProductoRepositoryImpl implements TipoProductoRepository {
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;
    
    @Override
    @Transactional
    public TipoProducto crear(TipoProducto tipoProducto) {
        em.persist(tipoProducto);
        em.flush();
        return tipoProducto;
    }
    
    @Override
    @Transactional
    public TipoProducto actualizar(TipoProducto tipoProducto) {
        return em.merge(tipoProducto);
    }
    
    @Override
    public Optional<TipoProducto> encontrarPorId(Integer id) {
        TipoProducto tipoProducto = em.find(TipoProducto.class, id);
        return Optional.ofNullable(tipoProducto);
    }
    
    @Override
    public List<TipoProducto> listarTodos() {
        TypedQuery<TipoProducto> query = em.createNamedQuery("TipoProducto.findAll", TipoProducto.class);
        return query.getResultList();
    }
    
    @Override
    public List<TipoProducto> buscarPorNombre(String nombre) {
        TypedQuery<TipoProducto> query = em.createNamedQuery("TipoProducto.findByNombre", TipoProducto.class);
        query.setParameter("nombre", nombre);
        return query.getResultList();
    }
    
    @Override
    public List<TipoProducto> buscarPorEstado(Boolean activo) {
        TypedQuery<TipoProducto> query = em.createNamedQuery("TipoProducto.findByActivo", TipoProducto.class);
        query.setParameter("activo", activo);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        Optional<TipoProducto> tipoProductoOpt = encontrarPorId(id);
        if (tipoProductoOpt.isPresent()) {
            em.remove(tipoProductoOpt.get());
            return true;
        }
        return false;
    }
}