package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Asignacion
import cl.duoc.bitacora_escolar.model.Asignatura
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.Curso
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.EstadoEnvio
import cl.duoc.bitacora_escolar.model.EstadoRevision
import cl.duoc.bitacora_escolar.model.Evidencia
import cl.duoc.bitacora_escolar.model.Perfil
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.TipoEvidencia
import cl.duoc.bitacora_escolar.model.TipoRecordatorio
import cl.duoc.bitacora_escolar.model.Usuario

// Llena la base de datos con datos FICTICIOS la primera vez que se usa la app.
// Contraseña de todas las cuentas: 12345678
object DatosDePrueba {

    suspend fun cargarSiEstaVacia(database: AppDatabase) {
        val usuarioDao = database.usuarioDao()
        if (usuarioDao.contar() > 0) return      // ya hay datos: no hacemos nada

        val cursoDao = database.cursoDao()
        val bitacoraDao = database.bitacoraDao()
        val registroDao = database.registroDao()
        val evidenciaDao = database.evidenciaDao()
        val recordatorioDao = database.recordatorioDao()

        // ---------- Cursos ----------
        val quinto = cursoDao.insertarCurso(Curso(nombre = "5° Básico A")).toInt()
        cursoDao.insertarCurso(Curso(nombre = "6° Básico A"))
        val septimo = cursoDao.insertarCurso(Curso(nombre = "7° Básico A")).toInt()
        val octavo = cursoDao.insertarCurso(Curso(nombre = "8° Básico A")).toInt()

        // ---------- Asignaturas ----------
        val lenguaje = cursoDao.insertarAsignatura(Asignatura(nombre = "Lenguaje")).toInt()
        val matematica = cursoDao.insertarAsignatura(Asignatura(nombre = "Matemática")).toInt()
        val ciencias = cursoDao.insertarAsignatura(Asignatura(nombre = "Ciencias Naturales")).toInt()
        val historia = cursoDao.insertarAsignatura(Asignatura(nombre = "Historia")).toInt()

        // ---------- Docentes ----------
        val ana = usuarioDao.insertar(
            Usuario(nombre = "Ana Fuentes", correo = "ana.fuentes@escuela.cl", password = "12345678", perfil = Perfil.DOCENTE)
        ).toInt()
        val pedro = usuarioDao.insertar(
            Usuario(nombre = "Pedro Rojas", correo = "pedro.rojas@escuela.cl", password = "12345678", perfil = Perfil.DOCENTE)
        ).toInt()

        // ---------- Quién hace qué ----------
        val lenguajeSeptimo = cursoDao.insertarAsignacion(Asignacion(docenteId = ana, cursoId = septimo, asignaturaId = lenguaje)).toInt()
        cursoDao.insertarAsignacion(Asignacion(docenteId = ana, cursoId = octavo, asignaturaId = lenguaje))
        cursoDao.insertarAsignacion(Asignacion(docenteId = ana, cursoId = septimo, asignaturaId = historia))
        val matematicaSeptimo = cursoDao.insertarAsignacion(Asignacion(docenteId = pedro, cursoId = septimo, asignaturaId = matematica)).toInt()
        cursoDao.insertarAsignacion(Asignacion(docenteId = pedro, cursoId = quinto, asignaturaId = ciencias))

        // ---------- Estudiantes ----------
        val martina = usuarioDao.insertar(
            Usuario(nombre = "Martina Soto", correo = "martina.soto@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = septimo)
        ).toInt()
        val benjamin = usuarioDao.insertar(
            Usuario(nombre = "Benjamín Lagos", correo = "benjamin.lagos@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = septimo)
        ).toInt()
        usuarioDao.insertar(Usuario(nombre = "Florencia Ruiz", correo = "florencia.ruiz@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = septimo))
        usuarioDao.insertar(Usuario(nombre = "Vicente Mora", correo = "vicente.mora@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = septimo))
        usuarioDao.insertar(Usuario(nombre = "Agustín Vera", correo = "agustin.vera@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = octavo))
        usuarioDao.insertar(Usuario(nombre = "Emilia Castro", correo = "emilia.castro@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = quinto))

        // ---------- Bitácoras ----------
        val septiembre = bitacoraDao.insertar(
            Bitacora(
                asignacionId = lenguajeSeptimo,
                mes = 9, anio = 2026,
                titulo = "Bitácora de Lenguaje - Septiembre",
                proposito = "Registrar lo aprendido en la unidad de textos narrativos.",
                instrucciones = "Después de cada clase escribe qué trabajamos, qué actividad hicimos y qué aprendiste.",
                preguntas = "¿Qué texto leímos hoy?\n¿Qué fue lo más interesante?",
                estado = EstadoBitacora.CERRADA,
                fechaPublicacion = "01/09/2026",
                fechaCierre = "30/09/2026"
            )
        ).toInt()

        val octubre = bitacoraDao.insertar(
            Bitacora(
                asignacionId = lenguajeSeptimo,
                mes = 10, anio = 2026,
                titulo = "Bitácora de Lenguaje - Octubre",
                proposito = "Reflexionar sobre la lectura de poemas y la escritura creativa.",
                instrucciones = "Completa un registro después de cada clase. Puedes agregar el enlace a una foto de tu cuaderno.",
                preguntas = "¿Qué poema trabajamos?\n¿Qué recurso literario aprendiste?\n¿Qué duda te quedó?",
                estado = EstadoBitacora.PUBLICADA,
                fechaPublicacion = "01/10/2026"
            )
        ).toInt()

        bitacoraDao.insertar(
            Bitacora(
                asignacionId = matematicaSeptimo,
                mes = 10, anio = 2026,
                titulo = "Bitácora de Matemática - Octubre",
                proposito = "Seguir el aprendizaje de números enteros.",
                instrucciones = "Registra los ejercicios que hiciste y explica el procedimiento con tus palabras.",
                preguntas = "¿Qué operación practicamos?\n¿Cómo resolviste el ejercicio más difícil?",
                estado = EstadoBitacora.PUBLICADA,
                fechaPublicacion = "01/10/2026"
            )
        )

        // ---------- Registros ----------
        registroDao.insertar(
            Registro(
                bitacoraId = septiembre, estudianteId = martina,
                fechaClase = "10/09/2026",
                contenido = "Estructura del cuento.",
                actividad = "Leímos un cuento en grupo y marcamos inicio, desarrollo y desenlace.",
                aprendizaje = "Aprendí a reconocer el conflicto de una historia.",
                estadoEnvio = EstadoEnvio.ENVIADO, estadoRevision = EstadoRevision.REVISADO,
                fechaCreacion = "10/09/2026", fechaEnvio = "10/09/2026",
                comentarioDocente = "¡Muy bien identificado el conflicto!",
                fechaRetroalimentacion = "12/09/2026", docenteRevisorId = ana
            )
        )
        val revisado = registroDao.insertar(
            Registro(
                bitacoraId = octubre, estudianteId = martina,
                fechaClase = "02/10/2026",
                contenido = "La rima en la poesía.",
                actividad = "Leímos dos poemas en voz alta y buscamos rimas.",
                aprendizaje = "Aprendí la diferencia entre rima asonante y consonante.",
                dudas = "No entendí bien qué es una estrofa libre.",
                estadoEnvio = EstadoEnvio.ENVIADO, estadoRevision = EstadoRevision.REVISADO,
                fechaCreacion = "02/10/2026", fechaEnvio = "02/10/2026",
                comentarioDocente = "Excelente. La estrofa libre la veremos la próxima clase.",
                fechaRetroalimentacion = "03/10/2026", docenteRevisorId = ana
            )
        ).toInt()
        evidenciaDao.insertar(
            Evidencia(registroId = revisado, tipo = TipoEvidencia.ENLACE, descripcion = "Página sobre la rima", enlace = "https://es.wikipedia.org/wiki/Rima")
        )
        registroDao.insertar(
            Registro(
                bitacoraId = octubre, estudianteId = benjamin,
                fechaClase = "02/10/2026",
                contenido = "Comparación y metáfora.",
                actividad = "Escribimos tres comparaciones sobre la naturaleza.",
                aprendizaje = "Una metáfora no usa la palabra 'como'.",
                estadoEnvio = EstadoEnvio.ENVIADO, estadoRevision = EstadoRevision.PENDIENTE,
                fechaCreacion = "02/10/2026", fechaEnvio = "02/10/2026"
            )
        )
        // Borrador de Martina: el docente NO lo ve
        registroDao.insertar(
            Registro(
                bitacoraId = octubre, estudianteId = martina,
                fechaClase = "05/10/2026",
                contenido = "Escritura de un caligrama.",
                actividad = "",
                aprendizaje = "",
                estadoEnvio = EstadoEnvio.BORRADOR,
                fechaCreacion = "05/10/2026"
            )
        )
        // Florencia y Vicente no han participado (sirve para el indicador "sin participación")

        // ---------- Recordatorios ----------
        recordatorioDao.insertar(
            Recordatorio(
                docenteId = ana, tipo = TipoRecordatorio.CIERRE_MENSUAL,
                descripcion = "Cerrar la bitácora de Lenguaje 7° Básico A (Octubre 2026)",
                fechaProgramada = "31/10/2026"
            )
        )
        recordatorioDao.insertar(
            Recordatorio(
                docenteId = ana, tipo = TipoRecordatorio.INICIO_SEMESTRE,
                descripcion = "Preparar las bitácoras del primer semestre 2027",
                fechaProgramada = "22/02/2027"
            )
        )
    }
}
