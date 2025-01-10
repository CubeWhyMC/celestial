/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game

import kotlinx.serialization.Serializable

val LAUNCHER_FEATURE_FLAGS = mutableListOf(
    "ServersCTA",
    "PlaywireRamp",
    "SocialMessaging",
    "LaunchCancelling",
    "SelectModpack",
    "ServerRecommendedModpack",
    "EmbeddedBrowserOpens",
    "MissionControl",
    "HomeAdOverwolf",
    "MissionControlAdOverwolf",
    "Radio",
    "RadioPremium",
    "CustomizableQuickPlay",
    "CommunityServers",
    "NotificationsInbox",
    "ProfileModsExploreCTA",
    "MissionControlChat",
    "LoaderVersionSetting",
    "OverwolfOverlay",
    "InstallVCRedistributable"
)

val DEFAULT_LAUNCHER_FEATURE_FLAGS = mutableListOf(
    "ServersCTA",
    "PlaywireRamp",
    "SocialMessaging",
    "LaunchCancelling",
    "SelectModpack",
    "ServerRecommendedModpack",
    "EmbeddedBrowserOpens",
//    "MissionControl",
//    "Radio",
//    "RadioPremium",
)

// todo GUI

@Serializable
data class LauncherFeatureFlags(
    var enabledFlags: List<String> = DEFAULT_LAUNCHER_FEATURE_FLAGS
) {
    fun toJson(): LauncherFeatureFlagsJson {
        return LauncherFeatureFlagsJson(this.enabledFlags, calcDisabled())
    }

    private fun calcDisabled() = LAUNCHER_FEATURE_FLAGS.filterNot { enabledFlags.contains(it) }
}

@Serializable
data class LauncherFeatureFlagsJson(
    val enabled: List<String>,
    val disabled: List<String>
)