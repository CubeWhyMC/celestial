package co.gongzh.procbridge

class ProtocolException internal constructor(message: String?) : RuntimeException(message) {
    companion object {
        const val UNRECOGNIZED_PROTOCOL: String = "unrecognized protocol"
        const val INCOMPATIBLE_VERSION: String = "incompatible protocol version"
        const val INCOMPLETE_DATA: String = "incomplete data"
        const val INVALID_STATUS_CODE: String = "invalid status code"
        const val INVALID_BODY: String = "invalid body"
    }
}
