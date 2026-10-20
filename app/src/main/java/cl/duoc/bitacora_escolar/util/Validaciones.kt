package cl.duoc.bitacora_escolar.util

// Reglas de validación. Igual que en clases: cada función devuelve Boolean (true = válido).
// No saben nada de la pantalla, por eso se pueden probar con pruebas unitarias.

const val MAXIMO_EVIDENCIAS = 5

// ---------- Login ----------
fun validarCorreo(correo: String): Boolean {
    return correo.isNotBlank() &&
            correo.contains("@") &&
            correo.trim().endsWith(".cl")
}

fun validarPassword(password: String): Boolean {
    return password.length >= 8
}

// ---------- Textos ----------
fun validarTexto(texto: String): Boolean {
    return texto.isNotBlank()
}

fun validarTitulo(titulo: String): Boolean {
    return titulo.trim().length in 5..80
}

fun validarPregunta(pregunta: String): Boolean {
    return pregunta.trim().length >= 5
}

fun validarPreguntas(preguntas: List<String>): Boolean {
    return preguntas.isNotEmpty()
}

fun validarComentario(comentario: String): Boolean {
    return comentario.trim().length >= 5
}

// ---------- Mes y año ----------
fun validarMes(mes: String): Boolean {
    val numero = mes.toIntOrNull()
    return numero != null && numero in 1..12
}

fun validarAnio(anio: String): Boolean {
    val numero = anio.toIntOrNull()
    return numero != null && numero in 2020..2100
}

// ---------- Fechas (dd/mm/aaaa) ----------
fun validarFecha(fecha: String): Boolean {
    val partes = fecha.trim().split("/")
    if (partes.size != 3) return false
    if (partes[0].length != 2 || partes[1].length != 2 || partes[2].length != 4) return false

    val dia = partes[0].toIntOrNull()
    val mes = partes[1].toIntOrNull()
    val anio = partes[2].toIntOrNull()
    if (dia == null || mes == null || anio == null) return false
    if (mes !in 1..12) return false
    return dia in 1..diasDelMes(mes, anio)
}

// La fecha de la clase no puede ser después de hoy
fun fechaNoFutura(fecha: String, hoy: String): Boolean {
    return fechaComoNumero(fecha) <= fechaComoNumero(hoy)
}

// La fecha de la clase debe ser del mes y año de la bitácora
fun fechaEnPeriodo(fecha: String, mes: Int, anio: Int): Boolean {
    val partes = fecha.trim().split("/")
    if (partes.size != 3) return false
    return partes[1].toIntOrNull() == mes && partes[2].toIntOrNull() == anio
}

// ---------- Evidencias ----------
fun validarEnlace(enlace: String): Boolean {
    val url = enlace.trim()
    return (url.startsWith("http://") || url.startsWith("https://")) &&
            !url.contains(" ") &&
            url.contains(".")
}

fun puedeAgregarEvidencia(cantidadActual: Int): Boolean {
    return cantidadActual < MAXIMO_EVIDENCIAS
}
