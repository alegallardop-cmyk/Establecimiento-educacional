package cl.duoc.bitacora_escolar.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import cl.duoc.bitacora_escolar.model.Asignacion
import cl.duoc.bitacora_escolar.model.Asignatura
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.Curso
import cl.duoc.bitacora_escolar.model.Evidencia
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.model.Registro
import cl.duoc.bitacora_escolar.model.Usuario

@Database(
    entities = [
        Usuario::class,
        Curso::class,
        Asignatura::class,
        Asignacion::class,
        Bitacora::class,
        Registro::class,
        Evidencia::class,
        Recordatorio::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun cursoDao(): CursoDao
    abstract fun bitacoraDao(): BitacoraDao
    abstract fun registroDao(): RegistroDao
    abstract fun evidenciaDao(): EvidenciaDao
    abstract fun recordatorioDao(): RecordatorioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bitacora.db"
                )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}
