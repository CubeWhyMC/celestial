package org.cubewhy.celestial.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class GitUtils {
    private GitUtils() {
    }

    public static final Properties info = new Properties();

    static {
        try {
            try (InputStream inputStream = GitUtils.class.getClassLoader().getResourceAsStream("git.properties")) {
                if (inputStream != null) {
                    info.load(inputStream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getBuildVersion() {
        return info.getProperty("git.build.version");
    }

    public static String getBranch() {
        return info.getProperty("git.branch");
    }

    public static String getRemote() {
        return info.getProperty("git.remote.origin.url");
    }

    public static String getCommitId(boolean shortID) {
        return shortID ? info.getProperty("git.commit.id.abbrev") : info.getProperty("git.commit.id");
    }

    public static String getBuildUser() {
        return info.getProperty("git.build.user.name");
    }
}
