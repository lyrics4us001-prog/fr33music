package com.fr33music.crash

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            saveCrashReport(thread, throwable)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar el reporte de crash en last_crash.txt", e)
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrashReport(thread: Thread, throwable: Throwable) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTraceString = sw.toString()

        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (_: Exception) {
            null
        }

        val versionName = packageInfo?.versionName ?: "N/A"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode ?: -1L
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toLong() ?: -1L
        }

        val report = buildString {
            appendLine("==================================================")
            appendLine("             FR33MUSIC CRASH REPORT               ")
            appendLine("==================================================")
            appendLine("Fecha y hora:      $currentDate")
            appendLine("Fabricante:        ${Build.MANUFACTURER}")
            appendLine("Modelo:            ${Build.MODEL} (${Build.DEVICE})")
            appendLine("Versión Android:   ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Versión App:       $versionName (Build $versionCode)")
            appendLine("Hilo:              ${thread.name} (id: ${thread.id})")
            appendLine("--------------------------------------------------")
            appendLine("DETALLE DE LA EXCEPCIÓN:")
            appendLine("Tipo:    ${throwable.javaClass.name}")
            appendLine("Mensaje: ${throwable.message ?: "Sin mensaje"}")
            appendLine("--------------------------------------------------")
            appendLine("STACK TRACE COMPLETO:")
            appendLine(stackTraceString)
            appendLine("==================================================")
        }

        val crashFile = File(context.filesDir, CRASH_FILE_NAME)
        crashFile.writeText(report)
        Log.e(TAG, "Crash report guardado exitosamente en: ${crashFile.absolutePath}")
    }

    companion object {
        private const val TAG = "CrashHandler"
        const val CRASH_FILE_NAME = "last_crash.txt"
    }
}
