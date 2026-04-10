package cl.matiascousino.activos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.matiascousino.activos.model.ActivoFijo;

@Repository
public interface ActivoFijoRepository extends JpaRepository<ActivoFijo, Long> {
    
    Optional<ActivoFijo> findByNombre(String nombre);
    
    boolean existsByNombre(String nombre);
}