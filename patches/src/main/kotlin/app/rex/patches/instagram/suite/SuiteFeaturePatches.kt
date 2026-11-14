/*
 * Copyright (C) 2026 xboyrex <https://github.com/xboyrex/rex-ig-lite>
 * GNU General Public License v3.0
 */

package app.rex.patches.instagram.suite

import app.rex.patches.instagram.misc.extension.sharedExtensionPatch
import app.rex.patches.instagram.misc.settings.settingsPatch
import app.rex.patches.instagram.utils.Constants.COMPATIBILITY_INSTAGRAM
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val askBeforeCallPatch = bytecodePatch(
    name = "Ask before a call",
    description = "Asks for confirmation before starting an audio or video call in Direct Messages.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM)
    dependsOn(sharedExtensionPatch, settingsPatch)
    execute {}
}

@Suppress("unused")
val askBeforeFollowPatch = bytecodePatch(
    name = "Ask before a follow",
    description = "Asks for confirmation before following or requesting to follow an account.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM)
    dependsOn(sharedExtensionPatch, settingsPatch)
    execute {}
}

@Suppress("unused")
val askBeforeLikePatch = bytecodePatch(
    name = "Ask before a like",
    description = "Asks for confirmation before liking or unliking a post, reel, or comment.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM)
    dependsOn(sharedExtensionPatch, settingsPatch)
    execute {}
}

@Suppress("unused")
val pinchToZoomReelsPatch = bytecodePatch(
    name = "Pinch to zoom on Reels",
    description = "Allows two-finger pinch-to-zoom on playing Reels.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM)
    dependsOn(sharedExtensionPatch, settingsPatch)
    execute {}
}

@Suppress("unused")
val longPressToZoomPatch = bytecodePatch(
    name = "Long press to zoom",
    description = "Allows pressing and holding a feed photo or video to zoom or open xboyrex options.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM)
    dependsOn(sharedExtensionPatch, settingsPatch)
    execute {}
}

@Suppress("unused")
val startMediaWithSoundPatch = bytecodePatch(
    name = "Start videos, reels and stories with sound",
    description = "Adds runtime controls to start feed videos, reels, and stories with audio enabled.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM)
    dependsOn(sharedExtensionPatch, settingsPatch)
    execute {}
}

@Suppress("unused")
val liquidGlassNavigationPatch = bytecodePatch(
    name = "Liquid Glass UI & Navigation",
    description = "Enables Liquid Glass floating navigation bar, rounded post corners, and real-time Gaussian blur dialogs.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM)
    dependsOn(sharedExtensionPatch, settingsPatch)
    execute {}
}

@Suppress("unused")
val privateScreenModePatch = bytecodePatch(
    name = "Private screen dimmer & notification filter",
    description = "Adds runtime privacy screen dimmer overlay with adjustable opacity and keyword/type notification filtering.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSTAGRAM)
    dependsOn(sharedExtensionPatch, settingsPatch)
    execute {}
}
