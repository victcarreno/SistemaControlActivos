package cl.matiascousino.activos.repository;

import cl.matiascousino.activos.model.SolicitudPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SolicitudPrestamoRepository extends JpaRepository<SolicitudPrestamo, Long> {

    // Buscar por estado
    List<SolicitudPrestamo> findByEstadoOrderByFechaSolicitudDesc(SolicitudPrestamo.EstadoPrestamo estado);

    // Buscar por rango de fechas de solicitud
    List<SolicitudPrestamo> findByFechaDevolucionBetweenOrderByFechaSolicitudDesc(
            LocalDate desde, LocalDate hasta);

    // Todas ordenadas por fecha descendente
    List<SolicitudPrestamo> findAllByOrderByFechaSolicitudDesc();
}
