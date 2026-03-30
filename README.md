# Sistema de Control de Activos — CEMC
## Spring Boot + MySQL

---

## Requisitos
- Java 17 o superior
- Maven 3.8 o superior
- MySQL 8 instalado y corriendo en tu PC

---

## Pasos para ejecutar el proyecto

### 1. Crear la base de datos en MySQL

Abre MySQL Workbench (o la terminal de MySQL) y ejecuta:

```sql
CREATE DATABASE `matiascousiño`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

> Las comillas invertidas ` `` ` son necesarias porque el nombre tiene ñ.

---

### 2. Revisar el archivo de conexión

El archivo `src/main/resources/application.properties` ya tiene la conexión configurada.
Solo asegúrate de poner tu contraseña de MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/matiascousiño?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Santiago
spring.datasource.username=root
spring.datasource.password=root   ← cambia esto por tu contraseña
```

---

### 3. Ejecutar el proyecto

```bash
mvn spring-boot:run
```

Al iniciar, Hibernate crea automáticamente la tabla `solicitudes_prestamo` en tu base de datos.

Abre en el navegador: **http://localhost:8080**

---

### 4. (Opcional) Cargar datos de prueba

Si quieres ver la página de reportes con datos de ejemplo, ejecuta en MySQL:

```sql
USE `matiascousiño`;

INSERT INTO solicitudes_prestamo (nombre_solicitante, taller, materiales, fecha_devolucion, fecha_solicitud, estado)
VALUES
  ('Maria Torres', 'Electronica', 'Osciloscopio', '2025-10-21', '2025-10-18 09:00:00', 'DEVUELTO'),
  ('Carlos Diaz', 'Telecomunicaciones', 'Cautin', '2025-10-25', '2025-10-20 10:30:00', 'PENDIENTE');
```

---

## Estructura del proyecto

```
SistemaControlActivos/
├── pom.xml                                      ← dependencias Maven
└── src/main/
    ├── java/cl/matiascousino/activos/
    │   ├── SistemaControlActivosApplication.java ← clase principal
    │   ├── controller/
    │   │   └── PrincipalController.java          ← maneja las URLs
    │   ├── model/
    │   │   └── SolicitudPrestamo.java            ← tabla de la BD
    │   ├── repository/
    │   │   └── SolicitudPrestamoRepository.java  ← consultas a la BD
    │   └── service/
    │       └── SolicitudPrestamoService.java     ← lógica del sistema
    └── resources/
        ├── application.properties               ← conexión a MySQL
        ├── db/init.sql                          ← datos de prueba
        ├── static/css/style.css
        ├── static/img/logo.png
        └── templates/
            ├── index.html
            ├── inventario.html
            ├── reportes.html
            └── solicitar-prestamo.html
```
