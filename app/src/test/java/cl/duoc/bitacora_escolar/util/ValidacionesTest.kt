package cl.duoc.bitacora_escolar.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de las reglas de validación (funciones puras: entra un texto, sale true o false)
class ValidacionesTest {

    @Test
    fun correo_valido_e_invalido() {
        assertTrue(validarCorreo("ana.fuentes@escuela.cl"))
        assertFalse(validarCorreo(""))
        assertFalse(validarCorreo("ana.fuentes.escuela.cl"))      // sin @
        assertFalse(validarCorreo("ana@escuela.com"))             // no termina en .cl
    }

    @Test
    fun password_minimo_8_caracteres() {
        assertTrue(validarPassword("12345678"))
        assertFalse(validarPassword("1234567"))
    }

    @Test
    fun textos_titulo_pregunta_y_comentario() {
        assertTrue(validarTexto("Algo"))
        assertFalse(validarTexto("   "))
        assertTrue(validarTitulo("Bitácora de octubre"))
        assertFalse(validarTitulo("Bit"))
        assertFalse(validarTitulo("a".repeat(81)))
        assertTrue(validarPregunta("¿Qué aprendí?"))
        assertFalse(validarPregunta("¿Qu"))
        assertTrue(validarPreguntas(listOf("¿Qué aprendí?")))
        assertFalse(validarPreguntas(emptyList()))
        assertTrue(validarComentario("Muy bien"))
        assertFalse(validarComentario(" ok "))
    }

    @Test
    fun mes_y_anio() {
        assertTrue(validarMes("1"))
        assertTrue(validarMes("12"))
        assertFalse(validarMes("13"))
        assertFalse(validarMes("abc"))
        assertTrue(validarAnio("2026"))
        assertFalse(validarAnio("2019"))
        assertFalse(validarAnio("dos mil"))
    }

    @Test
    fun fecha_con_formato_dd_mm_aaaa() {
        assertTrue(validarFecha("29/02/2024"))      // año bisiesto
        assertFalse(validarFecha("29/02/2026"))     // 2026 no es bisiesto
        assertFalse(validarFecha("31/04/2026"))     // abril tiene 30 días
        assertFalse(validarFecha("1/10/2026"))      // falta el 0
        assertFalse(validarFecha("10-10-2026"))
        assertFalse(validarFecha("aa/bb/cccc"))
        assertFalse(validarFecha("10/13/2026"))
    }

    @Test
    fun fecha_no_futura_y_en_periodo() {
        assertTrue(fechaNoFutura("10/10/2026", "19/11/2026"))
        assertFalse(fechaNoFutura("20/11/2026", "19/11/2026"))
        assertTrue(fechaEnPeriodo("15/10/2026", 10, 2026))
        assertFalse(fechaEnPeriodo("15/11/2026", 10, 2026))
        assertFalse(fechaEnPeriodo("15-10-2026", 10, 2026))
    }

    @Test
    fun enlaces_y_maximo_de_evidencias() {
        assertTrue(validarEnlace("https://drive.google.com/archivo"))
        assertFalse(validarEnlace("drive.google.com"))
        assertFalse(validarEnlace("https://mi enlace.cl"))
        assertTrue(puedeAgregarEvidencia(4))
        assertFalse(puedeAgregarEvidencia(5))
    }
}
