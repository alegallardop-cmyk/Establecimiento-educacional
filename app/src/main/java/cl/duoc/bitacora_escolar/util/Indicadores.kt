package cl.duoc.bitacora_escolar.util

import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.RegistroConEstudiante
import cl.duoc.bitacora_escolar.model.Usuario

// Indicadores de participación de una bitácora (solo los ve el docente)
data class Indicadores(
    val totalEstudiantes: Int,
    val participaron: Int,
    val sinParticipacion: Int,
    val totalAportes: Int,
    val revisados: Int,
    val pendientes: Int
)

// Solo cuentan los registros ENVIADOS
fun calcularIndicadores(estudiantes: List<Usuario>, registros: List<Registro>): Indicadores {
    val enviados = registros.filter { it.estadoEnvio == EstadoEnvio.ENVIADO }
    val participaron = estudiantes.count { estudiante ->
        enviados.any { it.estudianteId == estudiante.id }
    }
    val revisados = enviados.count { it.estadoRevision == EstadoRevision.REVISADO }
    return Indicadores(
        totalEstudiantes = estudiantes.size,
        participaron = participaron,
        sinParticipacion = estudiantes.size - participaron,
        totalAportes = enviados.size,
        revisados = revisados,
        pendientes = enviados.size - revisados
    )
}

fun estudiantesSinParticipacion(estudiantes: List<Usuario>, registros: List<Registro>): List<Usuario> {
    return estudiantes.filter { estudiante ->
        registros.none { it.estudianteId == estudiante.id && it.estadoEnvio == EstadoEnvio.ENVIADO }
    }
}

// Filtros del docente. Un texto vacío o un estado vacío = no filtrar por eso.
// Recibe RegistroConEstudiante porque el nombre viene de la tabla usuarios (JOIN).
fun filtrarRegistros(
    registros: List<RegistroConEstudiante>,
    nombreEstudiante: String,
    fecha: String,
    estadoRevision: String
): List<RegistroConEstudiante> {
    return registros.filter { aporte ->
        (nombreEstudiante.isBlank() || aporte.estudiante.contains(nombreEstudiante.trim(), ignoreCase = true)) &&
                (fecha.isBlank() || aporte.registro.fechaClase == fecha.trim()) &&
                (estadoRevision.isBlank() || aporte.registro.estadoRevision == estadoRevision)
    }
}

// Orden cronológico por fecha de la clase
fun ordenarPorFecha(registros: List<Registro>): List<Registro> {
    return registros.sortedBy { fechaComoNumero(it.fechaClase) }
}
