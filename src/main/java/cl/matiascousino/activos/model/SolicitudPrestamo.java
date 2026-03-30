package cl.matiascousino.activos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitudes_prestamo")
public class SolicitudPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_solicitante", nullable = false, length = 150)
    private String nombreSolicitante;

    @Column(name = "rut_solicitante", nullable = false, length = 12)
    private String rutSolicitante;

    @Column(name = "curso_asignatura", nullable = false, length = 100)
    private String cursoAsignatura;

    @Enumerated(EnumType.STRING)
    @Column(name = "taller", nullable = false)
    private Taller taller;

    // Reemplazamos ActivoFijo por un String simple
    @Column(name = "producto_solicitado", nullable = false, length = 150)
    private String productoSolicitado;

    @Column(name = "cantidad_producto", nullable = false)
    private Integer cantidadProducto;

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDate fechaDevolucion;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPrestamo estado;

    public enum Taller {
        ELECTRONICA,
        TELECOMUNICACIONES
    }

    public enum EstadoPrestamo {
        PENDIENTE,
        APROBADO,
        DEVUELTO,
        CANCELADO,
        ATRASADO
    }

    @PrePersist
    public void prePersist() {
        this.fechaSolicitud = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPrestamo.PENDIENTE;
        }
    }

    // ===== Getters y Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombreSolicitante() { return nombreSolicitante; }
    public void setNombreSolicitante(String nombreSolicitante) { this.nombreSolicitante = nombreSolicitante; }
    
    public String getRutSolicitante() { return rutSolicitante; }
    public void setRutSolicitante(String rutSolicitante) { this.rutSolicitante = rutSolicitante; }
    
    public String getCursoAsignatura() { return cursoAsignatura; }
    public void setCursoAsignatura(String cursoAsignatura) { this.cursoAsignatura = cursoAsignatura; }
    
    public Taller getTaller() { return taller; }
    public void setTaller(Taller taller) { this.taller = taller; }
    
    public String getProductoSolicitado() { return productoSolicitado; }
    public void setProductoSolicitado(String productoSolicitado) { this.productoSolicitado = productoSolicitado; }
    
    public Integer getCantidadProducto() { return cantidadProducto; }
    public void setCantidadProducto(Integer cantidadProducto) { this.cantidadProducto = cantidadProducto; }
    
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    
    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
}