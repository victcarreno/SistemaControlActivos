package cl.matiascousino.activos.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.matiascousino.activos.model.SolicitudPrestamo;
import cl.matiascousino.activos.repository.SolicitudPrestamoRepository;

@Service
@Transactional
public class SolicitudPrestamoService {

    private final SolicitudPrestamoRepository repository;

    public SolicitudPrestamoService(SolicitudPrestamoRepository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("null")
    public SolicitudPrestamo guardar(SolicitudPrestamo solicitud) {
        return repository.save(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudPrestamo> listarTodas() {
        return repository.findAllByOrderByFechaSolicitudDesc();
    }

    @Transactional(readOnly = true)
    public List<SolicitudPrestamo> listarPorRangoFecha(LocalDate desde, LocalDate hasta) {
        return repository.findByFechaDevolucionBetweenOrderByFechaSolicitudDesc(desde, hasta);
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Optional<SolicitudPrestamo> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @SuppressWarnings("null")
    public SolicitudPrestamo actualizarEstado(Long id, SolicitudPrestamo.EstadoPrestamo nuevoEstado) {
        SolicitudPrestamo solicitud = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));
        solicitud.setEstado(nuevoEstado);
        return repository.save(solicitud);
    }
}
