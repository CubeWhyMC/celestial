package org.cubewhy.celestial.utils;

public enum CrashReportType {
    LAUNCHER("launcher"),
    GAME("game");

    public final String jsonName;

    CrashReportType(String jsonName) {
        this.jsonName = jsonName;
    }
}
