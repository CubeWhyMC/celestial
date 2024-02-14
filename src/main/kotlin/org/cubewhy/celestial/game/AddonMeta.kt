/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game

import kotlinx.serialization.Serializable

// template: https://github.com/CubeWhyMC/LunarMod-Example/blob/master/src/main/resources/addon.meta.json

@Serializable
data class AddonMeta(
    val name: String,
    val version: String,
    val description: String,
    val authors: Array<String>,
    val website: String?,
    val repository: String?,
    val dependencies: Array<String>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddonMeta

        if (name != other.name) return false
        if (version != other.version) return false
        if (description != other.description) return false
        if (!authors.contentEquals(other.authors)) return false
        if (website != other.website) return false
        if (repository != other.repository) return false
        if (dependencies != null) {
            if (other.dependencies == null) return false
            if (!dependencies.contentEquals(other.dependencies)) return false
        } else if (other.dependencies != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + authors.contentHashCode()
        result = 31 * result + (website?.hashCode() ?: 0)
        result = 31 * result + (repository?.hashCode() ?: 0)
        result = 31 * result + (dependencies?.contentHashCode() ?: 0)
        return result
    }
}
