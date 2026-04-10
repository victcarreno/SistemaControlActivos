package cl.matiascousino.activos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.matiascousino.activos.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByRut(String rut);
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByRut(String rut);
    
    boolean existsByEmail(String email);
}