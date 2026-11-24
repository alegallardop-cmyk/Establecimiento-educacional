package cl.duoc.bitacora_escolar.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import cl.duoc.bitacora_escolar.model.Asignacion
import cl.duoc.bitacora_escolar.model.Asignatura
import cl.duoc.bitacora_escolar.model.Curso
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.model.Usuario

// Room guarda lo LOCAL: usuarios (login), cursos, asignaturas, asignaciones y recordatorios.
// Las bitácoras, registros y evidencias ahora están en el microservicio (Spring Boot + MySQL).
@Database(
    entities = [
        Usuario::class,
        Curso::class,
        Asignatura::class,
        Asignacion::class,
        Recordatorio::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun cursoDao(): CursoDao
    abstract fun recordatorioDao(): RecordatorioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bitacora_escolar.db"   // nombre nuevo: la base cambió (ya no tiene bitácoras ni registros)
                )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}
