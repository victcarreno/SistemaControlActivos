package cl.matiascousino.activos.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.matiascousino.activos.model.ActivoFijo;
import cl.matiascousino.activos.repository.ActivoFijoRepository;

@Service
@Transactional
public class ActivoFijoService {

    private final ActivoFijoRepository repository;

    public ActivoFijoService(ActivoFijoRepository repository) {
        this.repository = repository;
    }

    public ActivoFijo guardar(ActivoFijo activo) {
        return repository.save(activo);
    }

    @Transactional(readOnly = true)
    public List<ActivoFijo> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ActivoFijo> buscarPorNombre(String nombre) {
        return repository.findByNombre(nombre);
    }

    public boolean restarCantidad(String nombre, Integer cantidad) {
        Optional<ActivoFijo> activoOpt = repository.findByNombre(nombre);
        
        if (activoOpt.isPresent()) {
            ActivoFijo activo = activoOpt.get();
            if (activo.getCantidad() >= cantidad) {
                activo.setCantidad(activo.getCantidad() - cantidad);
                repository.save(activo);
                return true;
            }
        }
        return false;
    }

    public boolean sumarCantidad(String nombre, Integer cantidad) {
        Optional<ActivoFijo> activoOpt = repository.findByNombre(nombre);
        
        if (activoOpt.isPresent()) {
            ActivoFijo activo = activoOpt.get();
            activo.setCantidad(activo.getCantidad() + cantidad);
            repository.save(activo);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Integer obtenerCantidad(String nombre) {
        return repository.findByNombre(nombre)
            .map(ActivoFijo::getCantidad)
            .orElse(0);
    }

    @Transactional(readOnly = true)
    public Optional<ActivoFijo> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> listarNombresProductos() {
        return repository.findAll()
            .stream()
            .map(ActivoFijo::getNombre)
            .distinct()
            .collect(Collectors.toList());
    }
}