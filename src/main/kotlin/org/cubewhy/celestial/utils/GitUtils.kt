package org.cubewhy.celestial.utils

import java.io.IOException
import java.util.*

object GitUtils {
    val info: Properties = Properties()

    init {
        try {
            GitUtils::class.java.classLoader.getResourceAsStream("git.properties").use { inputStream ->
                if (inputStream != null) {
                    info.load(inputStream)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    val buildVersion: String
        get() = info.getProperty("git.build.version")

    @JvmStatic
    val branch: String
        get() = info.getProperty("git.branch")

    @JvmStatic
    val remote: String
        get() = info.getProperty("git.remote.origin.url")

    @JvmStatic
    fun getCommitId(shortID: Boolean): String {
        return if (shortID) info.getProperty("git.commit.id.abbrev") else info.getProperty("git.commit.id")
    }

    @JvmStatic
    val buildUser: String
        get() = info.getProperty("git.build.user.name")

    @JvmStatic
    val buildUserEmail: String
        get() = info.getProperty("git.build.user.email")

    @JvmStatic
    val commitMessage: String
        get() = info.getProperty("git.commit.message.full")

    @JvmStatic
    val commitTime: String
        get() = info.getProperty("git.commit.time")
}
