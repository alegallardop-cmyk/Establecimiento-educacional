package cl.duoc.bitacora_escolar.ui.docente.formbitacora

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.bitacora_escolar.data.AppDatabase
import cl.duoc.bitacora_escolar.data.BitacoraRepository
import cl.duoc.bitacora_escolar.data.CursoRepository
import cl.duoc.bitacora_escolar.data.RecordatorioRepository
import cl.duoc.bitacora_escolar.data.Sesion
import cl.duoc.bitacora_escolar.model.AsignacionConNombres
import cl.duoc.bitacora_escolar.model.Bitacora
import cl.duoc.bitacora_escolar.model.EstadoBitacora
import cl.duoc.bitacora_escolar.model.Recordatorio
import cl.duoc.bitacora_escolar.model.TipoRecordatorio
import cl.duoc.bitacora_escolar.util.fechaDeHoy
import cl.duoc.bitacora_escolar.util.nombreMes
import cl.duoc.bitacora_escolar.util.ultimoDiaDelMes
import cl.duoc.bitacora_escolar.util.validarAnio
import cl.duoc.bitacora_escolar.util.validarMes
import cl.duoc.bitacora_escolar.util.validarPregunta
import cl.duoc.bitacora_escolar.util.validarPreguntas
import cl.duoc.bitacora_escolar.util.validarTexto
import cl.duoc.bitacora_escolar.util.validarTitulo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Un solo formulario para CREAR y para EDITAR una bitácora
class FormBitacoraViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val bitacoraRepository = BitacoraRepository(database.bitacoraDao())
    private val cursoRepository = CursoRepository(database.cursoDao())
    private val recordatorioRepository = RecordatorioRepository(database.recordatorioDao())

    private val _uiState = MutableStateFlow(FormBitacoraUiState(bitacoraId = Sesion.bitacoraSeleccionada))
    val uiState: StateFlow<FormBitacoraUiState> = _uiState.asStateFlow()

    // Bitácora original cuando se edita (para conservar su estado y fechas)
    private var original: Bitacora? = null

    init {
        viewModelScope.launch {
            // El docente solo puede elegir SUS cursos y asignaturas
            val lista = cursoRepository.asignacionesDeDocente(Sesion.idUsuario())
            _uiState.update { it.copy(asignaciones = lista) }

            val id = _uiState.value.bitacoraId
            if (id != 0) {
                val datos = bitacoraRepository.obtenerPorId(id)
                // Solo el docente responsable edita, y solo si la bitácora no está cerrada
                if (datos == null || datos.docenteId != Sesion.idUsuario() ||
                    datos.bitacora.estado == EstadoBitacora.CERRADA
                ) {
                    _uiState.update { it.copy(sinAcceso = true) }
                } else {
                    original = datos.bitacora
                    _uiState.update {
                        it.copy(
                            asignacionElegida = lista.find { a -> a.asignacion.id == datos.bitacora.asignacionId },
                            mes = datos.bitacora.mes.toString(),
                            anio = datos.bitacora.anio.toString(),
                            titulo = datos.bitacora.titulo,
                            proposito = datos.bitacora.proposito,
                            instrucciones = datos.bitacora.instrucciones,
                            preguntas = datos.bitacora.preguntas.split("\n").filter { p -> p.isNotBlank() }
                        )
                    }
                }
            }
        }
    }

    fun esEdicion(): Boolean = _uiState.value.bitacoraId != 0

    fun elegirAsignacion(asignacion: AsignacionConNombres) {
        _uiState.update { it.copy(asignacionElegida = asignacion, errorAsignacion = false, errorRepetida = false) }
    }

    fun cambiarMes(mes: String) {
        _uiState.update { it.copy(mes = mes, errorMes = false, errorRepetida = false) }
    }

    fun cambiarAnio(anio: String) {
        _uiState.update { it.copy(anio = anio, errorAnio = false, errorRepetida = false) }
    }

    fun cambiarTitulo(titulo: String) {
        _uiState.update { it.copy(titulo = titulo, errorTitulo = false) }
    }

    fun cambiarProposito(proposito: String) {
        _uiState.update { it.copy(proposito = proposito, errorProposito = false) }
    }

    fun cambiarInstrucciones(instrucciones: String) {
        _uiState.update { it.copy(instrucciones = instrucciones, errorInstrucciones = false) }
    }

    fun cambiarNuevaPregunta(pregunta: String) {
        _uiState.update { it.copy(nuevaPregunta = pregunta, errorNuevaPregunta = false) }
    }

    fun agregarPregunta() {
        val pregunta = _uiState.value.nuevaPregunta
        if (!validarPregunta(pregunta)) {
            _uiState.update { it.copy(errorNuevaPregunta = true) }
            return
        }
        _uiState.update {
            it.copy(preguntas = it.preguntas + pregunta.trim(), nuevaPregunta = "", errorPreguntas = false)
        }
    }

    fun quitarPregunta(pregunta: String) {
        _uiState.update { it.copy(preguntas = it.preguntas - pregunta) }
    }

    // Acción del formulario.
    // Al crear: publicar = true la deja visible para los estudiantes.
    // Al editar: se mantiene el estado que ya tenía.
    fun guardar(publicar: Boolean) {
        val estado = _uiState.value
        val errorAsignacion = estado.asignacionElegida == null
        val errorMes = !validarMes(estado.mes)
        val errorAnio = !validarAnio(estado.anio)
        val errorTitulo = !validarTitulo(estado.titulo)
        val errorProposito = !validarTexto(estado.proposito)
        val errorInstrucciones = !validarTexto(estado.instrucciones)
        val errorPreguntas = !validarPreguntas(estado.preguntas)

        _uiState.update {
            it.copy(
                errorAsignacion = errorAsignacion,
                errorMes = errorMes,
                errorAnio = errorAnio,
                errorTitulo = errorTitulo,
                errorProposito = errorProposito,
                errorInstrucciones = errorInstrucciones,
                errorPreguntas = errorPreguntas,
                mensaje = ""
            )
        }

        val formularioValido = !errorAsignacion && !errorMes && !errorAnio &&
                !errorTitulo && !errorProposito && !errorInstrucciones && !errorPreguntas
        if (!formularioValido) return

        val elegida = estado.asignacionElegida ?: return
        val mes = estado.mes.toInt()
        val anio = estado.anio.toInt()

        viewModelScope.launch {
            try {
                // No puede haber dos bitácoras de la misma asignación en el mismo mes
                // (al editar no se cuenta a sí misma)
                val repetida = bitacoraRepository.existe(elegida.asignacion.id, mes, anio, estado.bitacoraId)
                if (repetida) {
                    _uiState.update { it.copy(errorRepetida = true) }
                    return@launch
                }

                val anterior = original
                val bitacora = if (anterior != null) {
                    // EDITAR: se copian los cambios sobre la bitácora original
                    anterior.copy(
                        mes = mes,
                        anio = anio,
                        titulo = estado.titulo.trim(),
                        proposito = estado.proposito.trim(),
                        instrucciones = estado.instrucciones.trim(),
                        preguntas = estado.preguntas.joinToString("\n")
                    )
                } else {
                    // CREAR
                    Bitacora(
                        asignacionId = elegida.asignacion.id,
                        mes = mes,
                        anio = anio,
                        titulo = estado.titulo.trim(),
                        proposito = estado.proposito.trim(),
                        instrucciones = estado.instrucciones.trim(),
                        preguntas = estado.preguntas.joinToString("\n"),
                        estado = if (publicar) EstadoBitacora.PUBLICADA else EstadoBitacora.BORRADOR,
                        fechaPublicacion = if (publicar) fechaDeHoy() else ""
                    )
                }
                bitacoraRepository.guardar(bitacora)

                if (anterior == null) {
                    // Recordatorio interno: cerrar la bitácora el último día del mes
                    recordatorioRepository.guardar(
                        Recordatorio(
                            docenteId = Sesion.idUsuario(),
                            tipo = TipoRecordatorio.CIERRE_MENSUAL,
                            descripcion = "Cerrar la bitácora de ${elegida.asignatura} ${elegida.curso} (${nombreMes(mes)} $anio)",
                            fechaProgramada = ultimoDiaDelMes(mes, anio)
                        )
                    )
                }

                val texto = if (anterior != null) "Cambios guardados correctamente"
                else if (publicar) "Bitácora creada y publicada"
                else "Bitácora guardada como borrador"
                _uiState.update { it.copy(guardada = true, mensaje = texto, mensajeEsError = false) }
            } catch (e: Exception) {
                e.printStackTrace()   // el detalle del error queda en el Logcat
                // Error de la operación (por ejemplo, la base de datos)
                _uiState.update {
                    it.copy(mensaje = "No se pudo guardar la bitácora. Intenta nuevamente.", mensajeEsError = true)
                }
            }
        }
    }
}
