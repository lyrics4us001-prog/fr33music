package com.fr33music.core.domain.model

sealed class ExtractorError(
    override val message: String?,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    data class NetworkError(
        override val message: String? = "Error de red al conectar con el servidor",
        override val cause: Throwable? = null
    ) : ExtractorError(message, cause)

    data class StreamUnavailable(
        override val message: String? = "El stream de audio no está disponible"
    ) : ExtractorError(message)

    object EmptyQuery : ExtractorError("La consulta de búsqueda está vacía")

    data class Unknown(
        override val message: String? = "Ocurrió un error inesperado al extraer el contenido",
        override val cause: Throwable? = null
    ) : ExtractorError(message, cause)
}
