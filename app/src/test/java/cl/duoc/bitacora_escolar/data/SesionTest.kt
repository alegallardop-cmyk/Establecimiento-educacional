package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Perfil
import cl.duoc.bitacora_escolar.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionTest {

    @Test
    fun sin_sesion_no_hay_perfil() {
        Sesion.cerrar()
        assertEquals(0, Sesion.idUsuario())
        assertFalse(Sesion.esDocente())
        assertFalse(Sesion.esEstudiante())
    }

    @Test
    fun docente_y_cerrar_sesion() {
        // Arrange
        Sesion.usuario = Usuario(1, "Ana Fuentes", "ana.fuentes@escuela.cl", "12345678", Perfil.DOCENTE)
        Sesion.bitacoraSeleccionada = 2
        Sesion.registroSeleccionado = 3

        // Assert
        assertEquals(1, Sesion.idUsuario())
        assertTrue(Sesion.esDocente())
        assertFalse(Sesion.esEstudiante())

        // Act
        Sesion.cerrar()

        // Assert
        assertNull(Sesion.usuario)
        assertEquals(0, Sesion.bitacoraSeleccionada)
        assertEquals(0, Sesion.registroSeleccionado)
    }

    @Test
    fun estudiante() {
        Sesion.usuario = Usuario(3, "Martina Soto", "martina.soto@escuela.cl", "12345678", Perfil.ESTUDIANTE, cursoId = 3)
        assertTrue(Sesion.esEstudiante())
        assertFalse(Sesion.esDocente())
        Sesion.cerrar()
    }
}
