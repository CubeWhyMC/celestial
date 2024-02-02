package co.gongzh.procbridge

class ServerException : RuntimeException {
    internal constructor(message: String?) : super(message ?: UNKNOWN_SERVER_ERROR)

    internal constructor(cause: Throwable) : super(cause.message, cause)

    companion object {
        private const val UNKNOWN_SERVER_ERROR = "unknown server error"
    }
}
