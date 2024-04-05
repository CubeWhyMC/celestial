/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.thirdparty

import kotlinx.serialization.Serializable

@Serializable
@Suppress("unused")
class LunarQTConfig {
    var cosmeticsEnabled: Boolean = false
    var freelookEnabled: Boolean = false
    var crackedEnabled: Boolean = false
    var noHitDelayEnabled: Boolean = false
    var debugModsEnabled: Boolean = false

    var fpsSpoofEnabled: Boolean = false
    var fpsSpoofMultiplier: Float = 1.0f

    var rawInputEnabled: Boolean = false

    var packFixEnabled: Boolean = false

    var customMetadataEnabled: Boolean = false
    var customMetadataURL: String = "https://lunarclient.top/api"
}