package cl.duoc.bitacora_escolar.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Igual que en FotoMotion: crea un archivo vacío donde la cámara guardará la foto
// y entrega su URI segura (content://) gracias a FileProvider.
// Usamos filesDir (y no cacheDir) porque las fotos de evidencia deben quedar guardadas.
fun crearUriFotografia(context: Context): Uri {
    val carpeta = File(
        context.filesDir, "fotos"
    )
    carpeta.mkdirs()
    val archivo = File.createTempFile(
        "foto_", ".jpg", carpeta
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}
