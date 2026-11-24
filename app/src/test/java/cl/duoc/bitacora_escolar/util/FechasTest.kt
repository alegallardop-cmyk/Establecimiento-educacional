package cl.duoc.bitacora_escolar.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FechasTest {

    @Test
    fun nombre_del_mes() {
        assertEquals("Enero", nombreMes(1))
        assertEquals("Diciembre", nombreMes(12))
        assertEquals("Mes 13", nombreMes(13))
    }

    @Test
    fun anios_bisiestos_y_dias_del_mes() {
        assertTrue(esBisiesto(2024))
        assertTrue(esBisiesto(2000))
        assertFalse(esBisiesto(1900))
        assertFalse(esBisiesto(2026))
        assertEquals(29, diasDelMes(2, 2024))
        assertEquals(28, diasDelMes(2, 2026))
        assertEquals(30, diasDelMes(11, 2026))
        assertEquals(31, diasDelMes(10, 2026))
    }

    @Test
    fun fecha_como_numero_para_ordenar() {
        assertEquals(20261005, fechaComoNumero("05/10/2026"))
        assertEquals(0, fechaComoNumero("05-10-2026"))
        assertEquals(0, fechaComoNumero("aa/10/2026"))
        assertEquals(0, fechaComoNumero("05/bb/2026"))
        assertEquals(0, fechaComoNumero("05/10/cccc"))
    }

    @Test
    fun ultimo_dia_y_mes_siguiente() {
        assertEquals("28/02/2026", ultimoDiaDelMes(2, 2026))
        assertEquals("31/10/2026", ultimoDiaDelMes(10, 2026))
        assertEquals(Pair(1, 2027), mesSiguiente(12, 2026))
        assertEquals(Pair(11, 2026), mesSiguiente(10, 2026))
    }

    @Test
    fun fecha_de_hoy_tiene_formato_dd_mm_aaaa() {
        assertTrue(validarFecha(fechaDeHoy()))
    }

    @Test
    fun fecha_de_la_api_de_feriados_pasa_a_dd_mm_aaaa() {
        assertEquals("18/09/2026", fechaIsoATexto("2026-09-18"))
        assertEquals("sin fecha", fechaIsoATexto("sin fecha"))
    }
}
