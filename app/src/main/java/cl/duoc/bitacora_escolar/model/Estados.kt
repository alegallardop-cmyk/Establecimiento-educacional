package cl.duoc.bitacora_escolar.model

// Valores fijos que se guardan en la base de datos (como texto).
// Usamos "object" (una sola instancia) con constantes para no escribir el texto a mano cada vez.

object Perfil {
    const val DOCENTE = "DOCENTE"
    const val ESTUDIANTE = "ESTUDIANTE"
}

object EstadoBitacora {
    const val BORRADOR = "BORRADOR"     // creada, los estudiantes aún no la ven
    const val PUBLICADA = "PUBLICADA"   // activa: los estudiantes pueden registrar
    const val CERRADA = "CERRADA"       // terminó el mes: solo lectura
}

object EstadoEnvio {
    const val BORRADOR = "BORRADOR"     // guardado por el estudiante, solo él lo ve
    const val ENVIADO = "ENVIADO"       // enviado al docente
}

object EstadoRevision {
    const val PENDIENTE = "PENDIENTE"
    const val REVISADO = "REVISADO"
}

object TipoEvidencia {
    const val FOTO = "FOTO"             // foto de la galería o de la cámara
    const val ENLACE = "ENLACE"         // un enlace (Drive, video, documento)
}

object TipoRecordatorio {
    const val CIERRE_MENSUAL = "Cierre mensual"
    const val NUEVA_BITACORA = "Crear siguiente bitácora"
    const val INICIO_SEMESTRE = "Inicio de semestre"
}

object EstadoRecordatorio {
    const val PENDIENTE = "PENDIENTE"
    const val COMPLETADO = "COMPLETADO"
}
