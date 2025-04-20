package test.java.sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.entity.Combo;
import sv.edu.ues.fmocc.tpi135.repository.ComboRepository;

/**
 * Implementación de ComboRepository para operaciones CRUD con JPA
 */
@ApplicationScoped
public class ComboRepositoryImpl implements ComboRepository {
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;
    
    @Override
    @Transactional
    public Combo crear(Combo combo) {
        em.persist(combo);
        em.flush();
        return combo;
    }
    
    @Override
    @Transactional
    public Combo actualizar(Combo combo) {
        return em.merge(combo);
    }
    
    @Override
    public Optional<Combo> encontrarPorId(Long id) {
        Combo combo = em.find(Combo.class, id);
        return Optional.ofNullable(combo);
    }
    
    @Override
    public List<Combo> listarTodos() {
        TypedQuery<Combo> query = em.createNamedQuery("Combo.findAll", Combo.class);
        return query.getResultList();
    }
    
    @Override
    public List<Combo> buscarPorNombre(String nombre) {
        TypedQuery<Combo> query = em.createNamedQuery("Combo.findByNombre", Combo.class);
        query.setParameter("nombre", nombre);
        return query.getResultList();
    }
    
    @Override
    public List<Combo> buscarPorEstado(Boolean activo) {
        TypedQuery<Combo> query = em.createNamedQuery("Combo.findByActivo", Combo.class);
        query.setParameter("activo", activo);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public boolean eliminar(Long id) {
        Optional<Combo> comboOpt = encontrarPorId(id);
        if (comboOpt.isPresent()) {
            em.remove(comboOpt.get());
            return true;
        }
        return false;
    }
}