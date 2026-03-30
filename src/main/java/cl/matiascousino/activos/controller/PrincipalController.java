package cl.matiascousino.activos.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.matiascousino.activos.model.SolicitudPrestamo;
import cl.matiascousino.activos.service.SolicitudPrestamoService;

@Controller
public class PrincipalController {

    private final SolicitudPrestamoService solicitudService;

    public PrincipalController(SolicitudPrestamoService solicitudService) {
        this.solicitudService = solicitudService;
    }

    // ── Inicio ──────────────────────────────────────────────────────────────
    @GetMapping({"/", "/index"})
    public String inicio() {
        return "index";
    }

    // ── Inventario ─────────────
    @GetMapping("/inventario")
    public String inventario() {
        return "inventario";
    }

    // ── Solicitar Préstamo ──────────────────────────────────────────────────
    @GetMapping("/solicitar-prestamo")
    public String solicitarPrestamo(Model model) {
        model.addAttribute("talleres", SolicitudPrestamo.Taller.values());
        model.addAttribute("solicitudes", solicitudService.listarTodas());
        // Lista fija de productos (reemplaza a ActivoFijo)
        model.addAttribute("productos", List.of(
            "Multímetro Digital", "Cautín", "Osciloscopio", "Resistencias",
            "Protoboard", "Fuente de Poder", "Multitester", "Pinzas"
        ));
        return "solicitar-prestamo";
    }

    @PostMapping("/solicitar-prestamo")
    public String procesarSolicitud(
            @RequestParam String nombreSolicitante,
            @RequestParam String rutSolicitante,
            @RequestParam String cursoAsignatura,
            @RequestParam SolicitudPrestamo.Taller taller,
            @RequestParam String productoSolicitado,  // String en lugar de ActivoFijo
            @RequestParam Integer cantidadProducto,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDevolucion,
            RedirectAttributes redirectAttributes) {

        SolicitudPrestamo solicitud = new SolicitudPrestamo();
        solicitud.setNombreSolicitante(nombreSolicitante);
        solicitud.setRutSolicitante(rutSolicitante);
        solicitud.setCursoAsignatura(cursoAsignatura);
        solicitud.setTaller(taller);
        solicitud.setProductoSolicitado(productoSolicitado);
        solicitud.setCantidadProducto(cantidadProducto);
        solicitud.setFechaDevolucion(fechaDevolucion);

        solicitudService.guardar(solicitud);

        redirectAttributes.addFlashAttribute("mensaje", 
                "Solicitud de " + nombreSolicitante + " registrada correctamente.");
        return "redirect:/solicitar-prestamo";
    }

    // ── Reportes ────────────────────────────────────────────────────────────
    @GetMapping("/reportes")
    public String reportes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        List<SolicitudPrestamo> solicitudes;

        if (desde != null && hasta != null) {
            solicitudes = solicitudService.listarPorRangoFecha(desde, hasta);
            model.addAttribute("desde", desde);
            model.addAttribute("hasta", hasta);
        } else {
            solicitudes = solicitudService.listarTodas();
        }

        model.addAttribute("solicitudes", solicitudes);
        return "reportes";
    }

    @PostMapping("/reportes/estado")
    public String actualizarEstado(
            @RequestParam Long id,
            @RequestParam SolicitudPrestamo.EstadoPrestamo estado,
            RedirectAttributes redirectAttributes) {

        solicitudService.actualizarEstado(id, estado);
        redirectAttributes.addFlashAttribute("mensajeReporte", "Estado actualizado correctamente.");
        return "redirect:/reportes";
    }

    // ── Login ───────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
}