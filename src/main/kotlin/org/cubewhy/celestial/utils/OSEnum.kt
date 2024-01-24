package org.cubewhy.celestial.utils

import java.util.*

enum class OSEnum(val os: String, @JvmField val jsName: String = os.lowercase(Locale.getDefault())) {
    Any("any", "any"),
    Linux("Linux", "linux"),
    Mac_OS("Mac OS", "macos"),
    Mac_OS_X("Mac OS X", "osx"),
    Windows("Windows", "win32"),
    OS2("OS/2"),
    Solaris("Solaris"),
    SunOS("SunOS", "sunos"),
    MPEiX("MPE/iX"),
    HP_UX("HP-UX"),
    AIX("AIX"),
    OS390("OS/390"),
    FreeBSD("FreeBSD", "freebsd"),
    Irix("Irix"),
    Digital_Unix("Digital Unix"),
    NetWare_411("NetWare"),
    OSF1("OSF1"),
    OpenVMS("OpenVMS"),
    Aix("Aix", "aix"),
    Darwin("Darwin", "darwin"),
    Others("Others");

    companion object {
        /**
         * Find a OS enum
         *
         * @param osString os.name
         */
        @JvmStatic
        fun find(osString: String): OSEnum? {
            for (value in entries) {
                if (osString.contains(value.os)) {
                    return value
                }
            }
            return null
        }

        @JvmStatic
        val current: OSEnum?
            get() = find(System.getProperty("os.name"))
    }
}
