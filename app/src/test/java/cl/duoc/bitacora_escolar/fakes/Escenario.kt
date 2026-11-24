package cl.duoc.bitacora_escolar.fakes

import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.CursoRepository
import cl.duoc.bitacora_escolar.data.DatosDePrueba
import cl.duoc.bitacora_escolar.data.FeriadosRepository
import cl.duoc.bitacora_escolar.data.RecordatorioRepository
import cl.duoc.bitacora_escolar.data.RegistroRepository
import cl.duoc.bitacora_escolar.data.UsuarioRepository
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Evidencia
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.TipoEvidencia
import kotlinx.coroutines.runBlocking

// Arma todo lo que necesita una prueba (ARRANGE):
//  - Room falso con los MISMOS datos de DatosDePrueba (ids: ana=1, pedro=2, martina=3, benjamin=4,
//    florencia=5, vicente=6, agustin=7, emilia=8; 7° Básico A = curso 3; asignación 1 = Lenguaje 7°A de Ana,
//    asignación 4 = Matemática 7°A de Pedro)
//  - Microservicio falso con 4 bitácoras y 4 registros
//  - Los repositorios reales, conectados a esos datos falsos
class Escenario {

    val usuarioDao = FakeUsuarioDao()
    val cursoDao = FakeCursoDao()
    val recordatorioDao = FakeRecordatorioDao()

    init {
        runBlocking { DatosDePrueba.cargarSiEstaVacia(usuarioDao, cursoDao, recordatorioDao) }
    }

    val bitacoraApi = FakeBitacoraApi(bitacorasIniciales())
    val registroApi = FakeRegistroApi(registrosIniciales())
    val feriadosApi = FakeFeriadosApi()

    val usuarioRepository = UsuarioRepository(usuarioDao, cursoDao, recordatorioDao)
    val cursoRepository = CursoRepository(cursoDao)
    val recordatorioRepository = RecordatorioRepository(recordatorioDao)
    val bitacoraRepository = BitacoraRepository(bitacoraApi, cursoDao)
    val registroRepository = RegistroRepository(registroApi, usuarioDao)
    val feriadosRepository = FeriadosRepository(feriadosApi)

    fun usuario(id: Int) = usuarioDao.usuarios.first { it.id == id }

    // Simula que el servidor se apaga
    fun apagarServidor() {
        bitacoraApi.sinConexion = true
        registroApi.sinConexion = true
    }

    companion object {
        const val ANA = 1
        const val PEDRO = 2
        const val MARTINA = 3
        const val BENJAMIN = 4
        const val AGUSTIN = 7
        const val SEPTIMO_A = 3

        // Bitácoras: 1 = Lenguaje sept. CERRADA, 2 = Lenguaje oct. PUBLICADA,
        //            3 = Matemática oct. PUBLICADA (de Pedro), 4 = Lenguaje nov. BORRADOR
        fun bitacorasIniciales(): List<Bitacora> = listOf(
            Bitacora(1, 1, 9, 2026, "Bitácora de Lenguaje - Septiembre", "Textos narrativos", "Escribe después de cada clase", "¿Qué leímos?", EstadoBitacora.CERRADA, "01/09/2026", "30/09/2026"),
            Bitacora(2, 1, 10, 2026, "Bitácora de Lenguaje - Octubre", "Poemas", "Completa un registro por clase", "¿Qué poema trabajamos?\n¿Qué aprendiste?", EstadoBitacora.PUBLICADA, "01/10/2026"),
            Bitacora(3, 4, 10, 2026, "Bitácora de Matemática - Octubre", "Números enteros", "Explica el procedimiento", "¿Qué operación practicamos?", EstadoBitacora.PUBLICADA, "01/10/2026"),
            Bitacora(4, 1, 11, 2026, "Bitácora de Lenguaje - Noviembre", "Teatro", "Registra cada ensayo", "¿Qué personaje te tocó?", EstadoBitacora.BORRADOR)
        )

        // Registros: 1 = Martina sept. revisado, 2 = Martina oct. revisado (con un enlace),
        //            3 = Benjamín oct. enviado pendiente, 4 = Martina oct. BORRADOR
        fun registrosIniciales(): List<Registro> = listOf(
            Registro(1, 1, MARTINA, "10/09/2026", "Estructura del cuento", "Leímos un cuento", "El conflicto",
                estadoEnvio = EstadoEnvio.ENVIADO, estadoRevision = EstadoRevision.REVISADO,
                comentarioDocente = "¡Muy bien!", docenteRevisorId = ANA),
            Registro(2, 2, MARTINA, "02/10/2026", "La rima", "Leímos poemas", "Rima asonante y consonante",
                estadoEnvio = EstadoEnvio.ENVIADO, estadoRevision = EstadoRevision.REVISADO,
                comentarioDocente = "Excelente", docenteRevisorId = ANA,
                evidencias = listOf(Evidencia(1, TipoEvidencia.ENLACE, "Página sobre la rima", "https://es.wikipedia.org/wiki/Rima"))),
            Registro(3, 2, BENJAMIN, "02/10/2026", "Comparación y metáfora", "Escribimos comparaciones", "La metáfora no usa 'como'",
                estadoEnvio = EstadoEnvio.ENVIADO, estadoRevision = EstadoRevision.PENDIENTE),
            Registro(4, 2, MARTINA, "05/10/2026", "Escritura de un caligrama", "", "",
                estadoEnvio = EstadoEnvio.BORRADOR)
        )
    }
}
