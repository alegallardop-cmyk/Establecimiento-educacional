package cl.duoc.bitacora_escolar.navigation

// Nombres de todas las pantallas (destinos) en un solo lugar: 10 pantallas
object Rutas {
    const val LOGIN = "login"

    // Módulo docente
    const val DOCENTE_INICIO = "docente_inicio"          // dashboard
    const val FORM_BITACORA = "form_bitacora"            // crear y editar
    const val DETALLE_BITACORA = "detalle_bitacora"      // detalle + informe
    const val REVISION = "revision"                      // revisar un aporte
    const val HISTORIAL_DOCENTE = "historial_docente"    // todas sus bitácoras

    // Módulo estudiante
    const val ESTUDIANTE_INICIO = "estudiante_inicio"    // dashboard
    const val BITACORA_ESTUDIANTE = "bitacora_estudiante"
    const val REGISTRO = "registro"                      // crear, editar borrador, ver
    const val HISTORIAL_ESTUDIANTE = "historial_estudiante" // seguimiento de sus aportes
}
