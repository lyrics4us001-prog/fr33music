package com.fr33music.core.domain.model

sealed class PlaybackError(
    override val message: String?,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    data class SourceError(
        override val message: String? = "Error al reproducir la fuente de audio",
        override val cause: Throwable? = null
    ) : PlaybackError(message, cause)

    data class StreamResolveFailed(
        val track: Track,
        val reason: String
    ) : PlaybackError("No se pudo obtener el stream para ${track.title}: $reason")

    object EmptyQueue : PlaybackError("La cola de reproducción está vacía")

    data class Unknown(
        override val message: String? = "Error desconocido en el reproductor",
        override val cause: Throwable? = null
    ) : PlaybackError(message, cause)
}
