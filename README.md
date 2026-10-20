# Bitácora Escolar · DSY1105

App Android para el caso **"Bitácora"**: bitácoras mensuales de aprendizaje por curso y asignatura (5° a 8° básico), con un perfil **docente** y uno **estudiante**. El docente crea, edita, publica y cierra bitácoras, revisa los aportes y deja retroalimentación; el estudiante registra lo que aprendió en cada clase, con fotos y enlaces como evidencia.


## Integrantes

| Nombre | Rol en el proyecto |
|---|---|
| Alen Gallardo | Base del proyecto, modelo de datos, base de datos Room, validaciones y login |
| Benjamín Leiva | Módulo docente, módulo estudiante, cámara y galería, animaciones y navegación |

## Planificación y repositorio

- **Trello:** [Tablero del proyecto](https://trello.com/invite/b/6ad6d5f67d0229f6b9b4501f/ATTI38492a0cf5091200dca157743621a751EB91C3A1/bitacora-escolar)
- **GitHub:** https://github.com/alegallardop-cmyk/Establecimiento-educacional

## Funcionalidades

**Docente**
- Dashboard con indicadores (activas, borradores, cerradas, aportes por revisar) y avisos internos.
- Crear y **editar** bitácoras (mes, año, título, propósito, instrucciones, preguntas orientadoras).
- Publicar y cerrar bitácoras. No se repite la misma asignación en el mismo mes.
- Detalle con informe mensual: participación, estudiantes sin participar y filtros por estudiante, fecha y estado.
- Revisar aportes (con sus fotos y enlaces), dejar comentario y marcarlos como revisados.
- Historial de todas sus bitácoras con búsqueda y filtro por estado.

**Estudiante**
- Dashboard con indicadores (activas, enviados, revisados, borradores) y avisos.
- Ver las bitácoras publicadas de su curso.
- Crear registros de clase, guardarlos como borrador, editarlos y enviarlos (con confirmación).
- Evidencias: foto desde la **galería** (Photo Picker) o la **cámara**, con vista previa, y enlaces.
- "Mis aportes": historial y seguimiento de sus registros (borrador, pendiente, revisado) y la retroalimentación del docente.

**Control de acceso:** cada pantalla revisa el perfil (`Sesion.esDocente()` / `esEstudiante()`), y cada ViewModel revisa que el dato le pertenezca (bitácora del docente, curso del estudiante, registro propio).

## Pantallas (10)

| Módulo | Pantallas |
|---|---|
| General | Login |
| Docente | Inicio (dashboard) · Formulario de bitácora (crear/editar) · Detalle + informe · Revisión de aporte · Historial |
| Estudiante | Inicio (dashboard) · Bitácora · Registro de clase · Mis aportes (historial y seguimiento) |

## Base de datos (Room)

8 tablas normalizadas (3FN): `usuarios`, `cursos`, `asignaturas`, `asignaciones`, `bitacoras`, `registros`, `evidencias`, `recordatorios`.

```
cursos ──< usuarios (estudiantes, cursoId)
usuarios (docente) ──< asignaciones >── cursos
                         asignaciones >── asignaturas
asignaciones ──< bitacoras ──< registros ──< evidencias
usuarios (estudiante) ──< registros
usuarios (docente) ──< recordatorios
```

- **Claves primarias:** `id` autogenerado en cada tabla.
- **Claves foráneas:** `@ForeignKey` en cada entidad hija (ej. `registros.bitacoraId → bitacoras.id`). Si se borra un registro, sus evidencias se borran solas (`CASCADE`).
- **3FN:** los nombres (curso, asignatura, estudiante) se guardan una sola vez. Para mostrarlos se usa un `JOIN` y el resultado se recibe en `BitacoraConNombres`, `RegistroConEstudiante` y `AsignacionConNombres` (`@Embedded`).
- **Índice único** en `bitacoras(asignacionId, mes, anio)`: no puede haber dos bitácoras del mismo curso y asignatura en el mismo mes.

## Estructura

```
cl/duoc/bitacora_escolar/
├── MainActivity.kt          → Tema + Navegacion()
├── model/                   → entidades Room, estados y clases "ConNombres"
├── data/                    → Dao, Repository, AppDatabase, DatosDePrueba, Sesion
├── navigation/              → Rutas.kt y Navegacion.kt
├── ui/
│   ├── theme/               → Color (paleta + colores semánticos), Type, Theme
│   ├── componentes/         → BarraSuperior y composables reutilizables
│   ├── login/
│   ├── docente/             → inicio, formbitacora, detalle, revision, historial
│   └── estudiante/          → inicio, bitacora, registro, historial
└── util/                    → Validaciones, Fechas, Indicadores, CamaraUtils
```

Cada pantalla tiene `XxxScreen` (Compose) + `XxxViewModel` (StateFlow) y, en los formularios, `XxxUiState`.
