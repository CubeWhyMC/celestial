package co.gongzh.procbridge

class ServerException internal constructor(cause: Throwable) : RuntimeException(cause.message, cause) {

    companion object {
        private const val UNKNOWN_SERVER_ERROR = "unknown server error"
    }
}
