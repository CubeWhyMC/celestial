package org.cubewhy.celestial.utils

import java.util.*

enum class OSEnum(val os: String, val jsName: String = os.lowercase(Locale.getDefault())) {
    Linux("Linux", "linux"),
    Windows("Windows", "win32"),
    MacOS("Mac OS", "darwin"),
    MacOSX("Mac OS X", "osx"),
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
