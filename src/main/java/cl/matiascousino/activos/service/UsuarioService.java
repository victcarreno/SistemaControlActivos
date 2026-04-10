package cl.matiascousino.activos.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.matiascousino.activos.model.Usuario;
import cl.matiascousino.activos.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // Validar que el RUT no exista
        if (repository.existsByRut(usuario.getRut())) {
            throw new RuntimeException("El RUT ya está registrado");
        }
        
        // Validar que el email no exista
        if (repository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // ✅ Encriptar contraseña con BCrypt
        String passwordEncriptado = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptado);
        
        return repository.save(usuario);
    }

    public Optional<Usuario> autenticar(String rut, String password) {
        Optional<Usuario> usuarioOpt = repository.findByRut(rut);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // ✅ Verificar contraseña encriptada
            if (passwordEncoder.matches(password, usuario.getPassword()) && usuario.getActivo()) {
                return usuarioOpt;
            }
        }
        
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorRut(String rut) {
        return repository.findByRut(rut);
    }
}