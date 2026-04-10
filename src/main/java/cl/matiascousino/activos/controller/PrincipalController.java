package cl.matiascousino.activos.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.matiascousino.activos.model.ActivoFijo;
import cl.matiascousino.activos.model.SolicitudPrestamo;
import cl.matiascousino.activos.model.Usuario;
import cl.matiascousino.activos.service.ActivoFijoService;
import cl.matiascousino.activos.service.SolicitudPrestamoService;
import cl.matiascousino.activos.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class PrincipalController {

    private final SolicitudPrestamoService solicitudService;
    private final UsuarioService usuarioService;
    private final ActivoFijoService activoService;

    public PrincipalController(SolicitudPrestamoService solicitudService, 
                          UsuarioService usuarioService,
                          ActivoFijoService activoService) {  
        this.solicitudService = solicitudService;
        this.usuarioService = usuarioService;
        this.activoService = activoService;
    }

    // ── Login (GET) ───────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ── Login (POST) ──────────────────────────────────────────────────────────
    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String rut,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        
        try {
            Optional<Usuario> usuarioOpt = usuarioService.autenticar(rut, password);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                session.setAttribute("usuario", usuario);
                session.setAttribute("nombreUsuario", usuario.getNombre());
                return "redirect:/index";
            } else {
                model.addAttribute("error", "RUT o contraseña incorrectos");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al iniciar sesión: " + e.getMessage());
            return "login";
        }
    }

    // ── Registro ──────────────────────────────────────────────────────────────
    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam String rut,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (!password.equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/registro";
            }
            
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                return "redirect:/registro";
            }
            
            Usuario usuario = new Usuario();
            usuario.setRut(rut);
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setPassword(password);
            usuario.setRol("USUARIO");
            
            usuarioService.registrarUsuario(usuario);
            
            redirectAttributes.addFlashAttribute("mensaje", "Usuario registrado exitosamente. Ahora puedes iniciar sesión.");
            return "redirect:/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/registro";
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ── Mis Préstamos (solo para usuarios) ────────────────────────────────────
    @GetMapping("/mis-prestamos")
    public String misPrestamos(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        try {
            List<SolicitudPrestamo> todosLosPrestamos = solicitudService.listarTodas();
            
            List<SolicitudPrestamo> misPrestamos = todosLosPrestamos.stream()
                .filter(p -> p.getRutSolicitante().equals(usuario.getRut()))
                .collect(Collectors.toList());
            
            model.addAttribute("solicitudes", misPrestamos);
            return "mis-prestamos";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar tus préstamos: " + e.getMessage());
            return "mis-prestamos";
        }
    }

    // ── Index (requiere login) ────────────────────────────────────────────────
    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "index";
    }

    // ── Solicitar Préstamo ────────────────────────────────────────────────────
    @GetMapping("/solicitar-prestamo")
    public String solicitarPrestamo(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        // ✅ Obtener productos dinámicamente desde la BD
        List<String> productos = activoService.listarNombresProductos();
        
        // Si no hay productos, agregar algunos por defecto (opcional)
        if (productos.isEmpty()) {
            productos = List.of(
                "Multímetro Digital", "Cautín", "Osciloscopio", "Resistencias",
                "Protoboard", "Fuente de Poder", "Multitester", "Pinzas"
            );
        }
        
        // Obtener stock de cada producto
        Map<String, Integer> stockDisponibles = new HashMap<>();
        for (String prod : productos) {
            stockDisponibles.put(prod, activoService.obtenerCantidad(prod));
        }
        
        model.addAttribute("talleres", SolicitudPrestamo.Taller.values());
        model.addAttribute("solicitudes", solicitudService.listarTodas());
        model.addAttribute("productos", productos);  // ✅ Lista dinámica
        model.addAttribute("stockDisponibles", stockDisponibles);
        model.addAttribute("usuario", usuario);
        
        return "solicitar-prestamo";
    }

    @PostMapping("/solicitar-prestamo")
    public String procesarSolicitud(
            @RequestParam String nombreSolicitante,
            @RequestParam String rutSolicitante,
            @RequestParam String cursoAsignatura,
            @RequestParam SolicitudPrestamo.Taller taller,
            @RequestParam String productoSolicitado,
            @RequestParam Integer cantidadProducto,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDevolucion,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        try {
            LocalDate hoy = LocalDate.now();
            
            if (fechaDevolucion == null) {
                redirectAttributes.addFlashAttribute("error", "La fecha de devolución es obligatoria");
                return "redirect:/solicitar-prestamo";
            }
            
            if (fechaDevolucion.isBefore(hoy)) {
                redirectAttributes.addFlashAttribute("error", 
                    "⚠️ La fecha de devolución no puede ser anterior a hoy (" + hoy + "). Por favor ingrese una fecha válida.");
                return "redirect:/solicitar-prestamo";
            }
            
            if (cantidadProducto <= 0) {
                redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor a 0");
                return "redirect:/solicitar-prestamo";
            }
            
            // Verificar que el producto exista y tenga stock
            Integer stockDisponible = activoService.obtenerCantidad(productoSolicitado);
            
            if (stockDisponible == null || stockDisponible <= 0) {
                redirectAttributes.addFlashAttribute("error", 
                    "❌ Producto no disponible o sin stock: " + productoSolicitado);
                return "redirect:/solicitar-prestamo";
            }
            
            if (stockDisponible < cantidadProducto) {
                redirectAttributes.addFlashAttribute("error", 
                    "❌ No hay suficiente stock disponible. Producto: " + productoSolicitado + 
                    ", Stock actual: " + stockDisponible + ", Solicitado: " + cantidadProducto);
                return "redirect:/solicitar-prestamo";
            }
            
            if (nombreSolicitante.trim().isEmpty() || 
                rutSolicitante.trim().isEmpty() || 
                cursoAsignatura.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Todos los campos son obligatorios");
                return "redirect:/solicitar-prestamo";
            }
            
            if (nombreSolicitante.matches(".*\\d.*")) {
                redirectAttributes.addFlashAttribute("error", "El nombre no puede contener números");
                return "redirect:/solicitar-prestamo";
            }
            
            // Restar cantidad del inventario
            boolean stockRestado = activoService.restarCantidad(productoSolicitado, cantidadProducto);
            if (!stockRestado) {
                redirectAttributes.addFlashAttribute("error", "Error al actualizar el inventario");
                return "redirect:/solicitar-prestamo";
            }
            
            SolicitudPrestamo solicitud = new SolicitudPrestamo();
            solicitud.setNombreSolicitante(nombreSolicitante.trim());
            solicitud.setRutSolicitante(rutSolicitante.trim());
            solicitud.setCursoAsignatura(cursoAsignatura.trim());
            solicitud.setTaller(taller);
            solicitud.setProductoSolicitado(productoSolicitado);
            solicitud.setCantidadProducto(cantidadProducto);
            solicitud.setFechaDevolucion(fechaDevolucion);
            
            solicitudService.guardar(solicitud);
            
            redirectAttributes.addFlashAttribute("mensaje", 
                "✅ Solicitud registrada correctamente. Stock actualizado.");
            return "redirect:/solicitar-prestamo";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "❌ Error al registrar: " + e.getMessage());
            return "redirect:/solicitar-prestamo";
        }
    }

    // ── Inventario (solo ADMIN) ───────────────────────────────────────────────
    @GetMapping("/inventario")
    public String inventario(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String tipoFiltro,
            HttpSession session,
            Model model) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (!"ADMIN".equals(usuario.getRol())) {
            return "redirect:/index";
        }
        
        List<ActivoFijo> activos = activoService.listarTodos();
        
        if (buscar != null && !buscar.isEmpty()) {
            activos = activos.stream()
                .filter(a -> a.getNombre().toLowerCase().contains(buscar.toLowerCase()) ||
                            a.getUbicacion().toLowerCase().contains(buscar.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (tipoFiltro != null && !tipoFiltro.isEmpty()) {
            activos = activos.stream()
                .filter(a -> a.getTipo().equals(tipoFiltro))
                .collect(Collectors.toList());
        }
        
        model.addAttribute("activos", activos);
        model.addAttribute("buscar", buscar);
        model.addAttribute("tipoFiltro", tipoFiltro);
        model.addAttribute("usuario", usuario);
        
        return "inventario";
    }

    // ── Guardar Activo (Agregar o Modificar) ──────────────────────────────────
    @PostMapping("/inventario/guardar")
    public String guardarActivo(
            @RequestParam(required = false) Long id,
            @RequestParam String nombre,
            @RequestParam String tipo,
            @RequestParam Integer cantidad,
            @RequestParam String ubicacion,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ADMIN".equals(usuario.getRol())) {
            return "redirect:/login";
        }
        
        try {
            if (id != null && id > 0) {
                ActivoFijo activo = activoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Activo no encontrado"));
                activo.setNombre(nombre);
                activo.setTipo(tipo);
                activo.setCantidad(cantidad);
                activo.setUbicacion(ubicacion);
                activoService.guardar(activo);
                redirectAttributes.addFlashAttribute("mensaje", "✅ Activo actualizado correctamente");
            } else {
                ActivoFijo activo = new ActivoFijo();
                activo.setNombre(nombre);
                activo.setTipo(tipo);
                activo.setCantidad(cantidad);
                activo.setUbicacion(ubicacion);
                activoService.guardar(activo);
                redirectAttributes.addFlashAttribute("mensaje", "✅ Activo agregado correctamente");
            }
            return "redirect:/inventario";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
            return "redirect:/inventario";
        }
    }

    // ── Editar Activo (cargar datos en formulario) ────────────────────────────
    @GetMapping("/inventario/editar/{id}")
    public String editarActivo(
            @PathVariable Long id,
            HttpSession session,
            Model model) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ADMIN".equals(usuario.getRol())) {
            return "redirect:/login";
        }
        
        ActivoFijo activo = activoService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Activo no encontrado"));
        
        model.addAttribute("activos", activoService.listarTodos());
        model.addAttribute("activoEditando", activo);
        model.addAttribute("usuario", usuario);
        
        return "inventario";
    }

    // ── Eliminar Activo ───────────────────────────────────────────────────────
    @GetMapping("/inventario/eliminar/{id}")
    public String eliminarActivo(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ADMIN".equals(usuario.getRol())) {
            return "redirect:/login";
        }
        
        try {
            activoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "✅ Activo eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al eliminar: " + e.getMessage());
        }
        
        return "redirect:/inventario";
    }

    // ── Reportes ──────────────────────────────────────────────────────────────
    @GetMapping("/reportes")
    public String reportes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model,
            HttpSession session) {  // ✅ SIN HttpServletRequest
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        List<SolicitudPrestamo> solicitudes;
        
        if (!"ADMIN".equals(usuario.getRol())) {
            List<SolicitudPrestamo> todos = solicitudService.listarTodas();
            solicitudes = todos.stream()
                .filter(s -> s.getRutSolicitante().equals(usuario.getRut()))
                .collect(Collectors.toList());
        } else {
            if (desde != null && hasta != null) {
                if (desde.isAfter(hasta)) {
                    model.addAttribute("error", "La fecha desde no puede ser mayor que la fecha hasta");
                    solicitudes = solicitudService.listarTodas();
                } else {
                    solicitudes = solicitudService.listarPorRangoFecha(desde, hasta);
                    model.addAttribute("desde", desde);
                    model.addAttribute("hasta", hasta);
                }
            } else {
                solicitudes = solicitudService.listarTodas();
            }
        }
        
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("usuario", usuario);
        // ✅ NO agregar request al modelo
        
        return "reportes";
    }

    // ── Actualizar Estado de Préstamo ─────────────────────────────────────────
    @PostMapping("/reportes/estado")
    public String actualizarEstado(
            @RequestParam Long id,
            @RequestParam SolicitudPrestamo.EstadoPrestamo estado,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            HttpServletRequest request) {
        
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        
        try {
            SolicitudPrestamo solicitud = solicitudService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + id));
            
            // Validar que no esté ya devuelto
            if (solicitud.getEstado() == SolicitudPrestamo.EstadoPrestamo.DEVUELTO) {
                redirectAttributes.addFlashAttribute("error", "Este préstamo ya fue devuelto");
                return redirigirPorReferer(request);
            }
            
            // Si el nuevo estado es DEVUELTO, sumar la cantidad al inventario
            if (estado == SolicitudPrestamo.EstadoPrestamo.DEVUELTO) {
                boolean stockSumado = activoService.sumarCantidad(
                    solicitud.getProductoSolicitado(), 
                    solicitud.getCantidadProducto()
                );
                
                if (!stockSumado) {
                    // Si el producto no existe en inventario, crearlo con la cantidad devuelta
                    ActivoFijo nuevoActivo = new ActivoFijo();
                    nuevoActivo.setNombre(solicitud.getProductoSolicitado());
                    nuevoActivo.setTipo("General");
                    nuevoActivo.setCantidad(solicitud.getCantidadProducto());
                    nuevoActivo.setUbicacion("Bodega Central");
                    activoService.guardar(nuevoActivo);
                }
            }
            
            // Actualizar el estado de la solicitud
            solicitudService.actualizarEstado(id, estado);
            
            redirectAttributes.addFlashAttribute("mensajeReporte", 
                "Estado actualizado correctamente." + 
                (estado == SolicitudPrestamo.EstadoPrestamo.DEVUELTO ? " ✅ Stock actualizado." : ""));
            
            // ✅ Redirigir según la página de origen (usando Referer header)
            return redirigirPorReferer(request);
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return redirigirPorReferer(request);
        }
    }
    
    // ── Método auxiliar para redirección basada en Referer ───────────────────
    private String redirigirPorReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        
        if (referer != null && !referer.isEmpty()) {
            if (referer.contains("/solicitar-prestamo")) {
                return "redirect:/solicitar-prestamo";
            } else if (referer.contains("/reportes")) {
                return "redirect:/reportes";
            }
        }
        // Fallback por defecto
        return "redirect:/reportes";
    }

    // ── Imprimir Reporte Individual ───────────────────────────────────────────
    @GetMapping("/reportes/imprimir/{id}")
    public String imprimirReporteIndividual(
            @PathVariable Long id,
            Model model,
            HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        try {
            SolicitudPrestamo solicitud = solicitudService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + id));
            
            if (!"ADMIN".equals(usuario.getRol())) {
                if (!solicitud.getRutSolicitante().equals(usuario.getRut())) {
                    return "redirect:/reportes?error=acceso_denegado";
                }
            }
            
            model.addAttribute("solicitud", solicitud);
            return "reporte-print";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/reportes?error=" + e.getMessage();
        }
    }
}