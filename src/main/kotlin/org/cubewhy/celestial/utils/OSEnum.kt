package org.cubewhy.celestial.utils

import java.util.*

enum class OSEnum(val os: String, val jsName: String = os.lowercase(Locale.getDefault())) {
    Any("any", "any"),
    Linux("Linux", "linux"),
    Mac_OS("Mac OS", "darwin"),
    Mac_OS_X("Mac OS X", "osx"),
    Windows("Windows", "win32"),
    FreeBSD("FreeBSD", "freebsd"),
    Darwin("Darwin", "darwin");

    val isCurrent: Boolean
        get() =
            current == this

    companion object {
        /**
         * Find a OS enum
         *
         * @param osString os.name
         */

        fun find(osString: String): OSEnum? {
            for (value in entries) {
                if (osString.contains(value.os)) {
                    return value
                }
            }
            return null
        }


        val current: OSEnum?
            get() = find(System.getProperty("os.name"))
    }
}
