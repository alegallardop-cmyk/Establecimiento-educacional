package cl.duoc.bitacora_escolar.data

import cl.duoc.bitacora_escolar.model.Asignacion
import cl.duoc.bitacora_escolar.model.Asignatura
import cl.duoc.bitacora_escolar.model.Curso
import cl.duoc.bitacora_escolar.model.Perfil
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.model.TipoRecordatorio
import cl.duoc.bitacora_escolar.model.Usuario

// Llena la base de datos con datos FICTICIOS la primera vez que se usa la app.
// Contraseña de todas las cuentas: 12345678
object DatosDePrueba {

    suspend fun cargarSiEstaVacia(usuarioDao: UsuarioDao, cursoDao: CursoDao, recordatorioDao: RecordatorioDao) {
        if (usuarioDao.contar() > 0) return      // ya hay datos: no hacemos nada

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
        cursoDao.insertarAsignacion(Asignacion(docenteId = ana, cursoId = septimo, asignaturaId = lenguaje))
        cursoDao.insertarAsignacion(Asignacion(docenteId = ana, cursoId = octavo, asignaturaId = lenguaje))
        cursoDao.insertarAsignacion(Asignacion(docenteId = ana, cursoId = septimo, asignaturaId = historia))
        cursoDao.insertarAsignacion(Asignacion(docenteId = pedro, cursoId = septimo, asignaturaId = matematica))
        cursoDao.insertarAsignacion(Asignacion(docenteId = pedro, cursoId = quinto, asignaturaId = ciencias))

        // ---------- Estudiantes ----------
        usuarioDao.insertar(Usuario(nombre = "Martina Soto", correo = "martina.soto@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = septimo))
        usuarioDao.insertar(Usuario(nombre = "Benjamín Lagos", correo = "benjamin.lagos@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = septimo))
        usuarioDao.insertar(Usuario(nombre = "Florencia Ruiz", correo = "florencia.ruiz@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = septimo))
        usuarioDao.insertar(Usuario(nombre = "Vicente Mora", correo = "vicente.mora@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = septimo))
        usuarioDao.insertar(Usuario(nombre = "Agustín Vera", correo = "agustin.vera@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = octavo))
        usuarioDao.insertar(Usuario(nombre = "Emilia Castro", correo = "emilia.castro@escuela.cl", password = "12345678", perfil = Perfil.ESTUDIANTE, cursoId = quinto))

        // Las bitácoras y los registros de prueba NO están aquí:
        // los carga el microservicio (DataInitializer del backend) en MySQL.
        // Usan asignacionId = 1 (Lenguaje 7°A de Ana) y 4 (Matemática 7°A de Pedro).

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
