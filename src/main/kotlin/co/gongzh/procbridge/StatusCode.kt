package co.gongzh.procbridge

internal enum class StatusCode(var rawValue: Int) {
    REQUEST(0), GOOD_RESPONSE(1), BAD_RESPONSE(2);

    companion object {
        fun fromRawValue(rawValue: Int): StatusCode? {
            for (sc in entries) {
                if (sc.rawValue == rawValue) {
                    return sc
                }
            }
            return null
        }
    }
}
