package cl.duoc.bitacora_escolar.util

import java.text.SimpleDateFormat
import java.util.Date

// Funciones para trabajar con fechas en formato dd/mm/aaaa.
// Todas son Kotlin básico (split, toIntOrNull, when), salvo fechaDeHoy().

private val MESES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

// Fecha actual del teléfono como texto "dd/mm/aaaa"
fun fechaDeHoy(): String {
    return SimpleDateFormat("dd/MM/yyyy").format(Date())
}

fun nombreMes(mes: Int): String {
    if (mes < 1 || mes > 12) return "Mes $mes"
    return MESES[mes - 1]
}

fun esBisiesto(anio: Int): Boolean {
    return (anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0
}

fun diasDelMes(mes: Int, anio: Int): Int {
    return when (mes) {
        2 -> if (esBisiesto(anio)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

// "05/10/2026" -> 20261005 (sirve para comparar y ordenar fechas)
fun fechaComoNumero(fecha: String): Int {
    val partes = fecha.split("/")
    if (partes.size != 3) return 0
    val dia = partes[0].toIntOrNull() ?: return 0
    val mes = partes[1].toIntOrNull() ?: return 0
    val anio = partes[2].toIntOrNull() ?: return 0
    return anio * 10000 + mes * 100 + dia
}

fun ultimoDiaDelMes(mes: Int, anio: Int): String {
    val mesTexto = if (mes < 10) "0$mes" else "$mes"
    return "${diasDelMes(mes, anio)}/$mesTexto/$anio"
}

// Devuelve (mes, año) del mes siguiente
fun mesSiguiente(mes: Int, anio: Int): Pair<Int, Int> {
    return if (mes == 12) Pair(1, anio + 1) else Pair(mes + 1, anio)
}
