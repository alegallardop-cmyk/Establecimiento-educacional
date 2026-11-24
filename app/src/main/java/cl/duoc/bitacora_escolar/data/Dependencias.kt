package cl.duoc.bitacora_escolar.data

import android.content.Context
import cl.duoc.bitacora_escolar.data.remote.RetrofitClient

// Crea los repositorios UNA sola vez para toda la app.
// MainActivity llama a iniciar() al abrir la app (Room necesita el Context).
// Los ViewModel los reciben como parámetro con valor por defecto:
//   class LoginViewModel(private val repository: UsuarioRepository = Dependencias.usuarioRepository)
// Así, en las pruebas unitarias se les puede pasar un repositorio con datos falsos.
object Dependencias {

    private lateinit var database: AppDatabase

    fun iniciar(context: Context) {
        database = AppDatabase.getInstance(context)
    }

    // Room (datos locales)
    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(database.usuarioDao(), database.cursoDao(), database.recordatorioDao())
    }

    val cursoRepository: CursoRepository by lazy {
        CursoRepository(database.cursoDao())
    }

    val recordatorioRepository: RecordatorioRepository by lazy {
        RecordatorioRepository(database.recordatorioDao())
    }

    // Microservicio (Retrofit) + Room para los nombres
    val bitacoraRepository: BitacoraRepository by lazy {
        BitacoraRepository(RetrofitClient.bitacoraApi, database.cursoDao())
    }

    val registroRepository: RegistroRepository by lazy {
        RegistroRepository(RetrofitClient.registroApi, database.usuarioDao())
    }
}
