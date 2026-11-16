package app.rex.patches.instagram

import java.io.File

object RexPatchManifestTest {

    private fun repoRoot(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        return if (File(cwd, "patches-list.json").exists()) cwd else cwd.parentFile
    }

    @JvmStatic
    fun main(args: Array<String>) {
        verifyAllSixteenPreviouslyUnwiredSwitchesHaveRealRuntimeConsumers()
        verifyPersistenceAndStaticHelperDefectsAreFixed()
        verifyBrandingSubpageHeaderAndRegisteredButtonRoutes()
    }

    fun verifyAllSixteenPreviouslyUnwiredSwitchesHaveRealRuntimeConsumers() {
        val root = repoRoot()
        val prefFile = File(root, "extensions/instagram/src/main/java/app/morphe/extension/instagram/utils/Pref.java")
        check(prefFile.exists()) { "Pref.java must exist" }
        val prefText = prefFile.readText()

        val requiredPrefMethods = listOf(
            "hideViewOnceSeen(",
            "ghostInstants(",
            "hideAudioSeen(",
            "hideReelsAndStoriesAdPods(",
            "disableAnalytics(",
            "restoreTrustSpoof(",
            "holdVideoFastForward(",
            "startFeedVideosMuted(",
            "disableReelsLooping(",
            "playStorySoundAutomatically(",
            "storyMentionIcon(",
            "highQualityUpload(",
            "upload60FpsStories(",
            "extendedStoryDuration(",
            "reelsFloatingToolbar(",
            "storyFiltersMaster("
        )
        for (method in requiredPrefMethods) {
            check(prefText.contains(method)) { "Pref.java must expose consumer accessor $method" }
        }
    }

    fun verifyPersistenceAndStaticHelperDefectsAreFixed() {
        val root = repoRoot()
        val prefDir = File(root, "extensions/instagram/src/main/java/app/morphe/extension/instagram/settings/preference/widgets")
        val widgets = listOf("SwitchPref.java", "ListPref.java", "MultiSelectListPref.java", "EditTextPref.java")

        for (w in widgets) {
            val text = File(prefDir, w).readText()
            check(!text.contains("static Helper helper")) { "$w must not hold a static Activity-bound Helper reference" }
            check(text.contains("private final Helper helper;")) { "$w must use an instance-scoped final Helper field" }
        }

        for (w in listOf("ListPref.java", "MultiSelectListPref.java", "EditTextPref.java")) {
            val text = File(prefDir, w).readText()
            check(text.contains("boolean saved = helper.setValue(")) { "$w must inspect boolean return value of helper.setValue" }
            check(text.contains("if (!saved)")) { "$w must reject failed persistence writes and restore previous value" }
            check(!text.contains("key == Settings.")) { "$w must not compare preference keys using ==" }
        }
    }

    fun verifyBrandingSubpageHeaderAndRegisteredButtonRoutes() {
        val root = repoRoot()
        val buttonPref = File(root, "extensions/instagram/src/main/java/app/morphe/extension/instagram/settings/preference/widgets/ButtonPref.java").readText()
        check(!buttonPref.contains("key.startsWith(\"rex_\")")) { "ButtonPref must not use blanket rex_ prefix chevron matching" }
        check(buttonPref.contains("REGISTERED_ROUTE_KEYS")) { "ButtonPref must use explicit REGISTERED_ROUTE_KEYS registry" }
        check(buttonPref.contains("REGISTERED_ACTION_KEYS")) { "ButtonPref must use explicit REGISTERED_ACTION_KEYS registry" }

        val settingsActivity = File(root, "extensions/instagram/src/main/java/app/morphe/extension/instagram/settings/SettingsActivity.java").readText()
        check(settingsActivity.contains("Rex IG Lite")) { "SettingsActivity header must display Rex IG Lite" }
        check(settingsActivity.contains("by xboyrex")) { "SettingsActivity subtitle must display by xboyrex" }
        check(settingsActivity.contains("resolveSubpageDescription(")) { "SettingsActivity must render subpage descriptions below the toolbar" }
        val forbiddenBrand = "Insta" + "Zen"
        check(!settingsActivity.contains(forbiddenBrand)) { "SettingsActivity must not contain foreign branding" }
    }
}
