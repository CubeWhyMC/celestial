package org.cubewhy.celestial.utils;

public enum OSEnum {
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

    public final String os;
    public final String jsName;

    OSEnum(String os) {
        this(os, os.toLowerCase());
    }

    OSEnum(String os, String jsName) {
        this.os = os;
        this.jsName = jsName;
    }

    /**
     * Find a OS enum
     *
     * @param osString os.name
     * */
    public static OSEnum find(String osString) {
        for (OSEnum value : values()) {
            if (osString.contains(value.os)) {
                return value;
            }
        }
        return null;
    }
}
