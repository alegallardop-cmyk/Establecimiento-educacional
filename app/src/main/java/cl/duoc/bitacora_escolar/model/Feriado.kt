package cl.duoc.bitacora_escolar.model

// Un feriado de la API externa Nager.Date.
// Ejemplo del JSON: { "date": "2026-09-18", "localName": "Día de la Independencia", ... }
// Solo declaramos los campos que usamos; Gson ignora el resto.
data class Feriado(
    val date: String,              // formato aaaa-mm-dd
    val localName: String          // nombre en español
)
