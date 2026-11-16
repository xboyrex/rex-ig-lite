/*
 * Copyright (C) 2026 xboyrex <https://github.com/xboyrex/rex-ig-lite>
 *
 * See the included NOTICE file for GPLv3 §7(b) terms that apply to this code.
 */

package app.morphe.extension.instagram.settings.preference.widgets;

import static app.morphe.extension.instagram.utils.IgStr.str;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import app.morphe.extension.instagram.constants.Constants;
import app.morphe.extension.instagram.constants.UI;
import app.morphe.extension.instagram.patches.customise.font.FontStorage;
import app.morphe.extension.instagram.patches.devFlags.RecommendedFlags;
import app.morphe.extension.instagram.patches.dm.SavedMessagesHook;
import app.morphe.extension.instagram.patches.download.DownloadMapping;
import app.morphe.extension.instagram.patches.focusLock.FocusLockDialogs;
import app.morphe.extension.instagram.patches.story.StoryLikeHistoryActivity;
import app.morphe.extension.instagram.patches.story.StoryMentionSettings;
import app.morphe.extension.instagram.settings.ActivityHook;
import app.morphe.extension.instagram.settings.Settings;
import app.morphe.extension.instagram.settings.SettingsStatus;
import app.morphe.extension.instagram.settings.preference.fragments.FragmentHook;
import app.morphe.extension.instagram.utils.InstaUtils;
import app.morphe.extension.rex.downloader.StorageUtils;
import app.morphe.extension.rex.sharedPreference.SharedPref;
import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.Utils;

public class ButtonPref extends Preference {
    public static final Set<String> REGISTERED_ROUTE_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            Constants.REX_FRAGMENT_SETTINGS,
            Constants.REX_FRAGMENT_GHOST,
            Constants.REX_FRAGMENT_NOTIFICATION,
            Constants.REX_FRAGMENT_PRIVACY,
            Constants.REX_FRAGMENT_GESTURES,
            Constants.REX_FRAGMENT_FEED_AND_STORIES,
            Constants.REX_FRAGMENT_UI_TWEAKS,
            Constants.REX_FRAGMENT_DISTRACTION_FREE,
            Constants.REX_FRAGMENT_MISC,
            Constants.REX_FRAGMENT_DOWNLOAD_MEDIA,
            Constants.REX_FRAGMENT_DEV_OPTIONS,
            Constants.REX_FRAGMENT_ABOUT,
            Constants.REX_FRAGMENT_ADS,
            Constants.REX_FRAGMENT_STORY,
            Constants.REX_FRAGMENT_LINKS,
            Constants.REX_FRAGMENT_NAV_BTNS,
            Constants.REX_FRAGMENT_ACTION_BAR,
            Constants.REX_FRAGMENT_DM,
            Constants.REX_FRAGMENT_FILTER_CONTENT,
            Constants.REX_FRAGMENT_REC_FLAGS,
            Constants.REX_FRAGMENT_FOCUS_LOCK,
            Constants.REX_FRAGMENT_INSTANTS,
            Constants.REX_FRAGMENT_FONT
    )));

    public static final Set<String> REGISTERED_ACTION_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "rex_export_dev_overrides",
            "rex_import_dev_overrides",
            "rex_import_id_mapping",
            "rex_export_pref",
            "rex_import_pref",
            "rex_download_set_path",
            "rex_reset_download_path",
            "rex_pref_add_font",
            "rex_reset_pref",
            "rex_focus_lock_action",
            "rex_pref_delete_font",
            "rex_export_experiment_list",
            "rex_export_experiment_mappings",
            "rex_download_id_mapping",
            "rex_view_saved_instants",
            "view_deleted_messages",
            "rex_notification_center_btn",
            "rex_chat_wallpaper_picker",
            "rex_saved_captions_btn",
            "rex_saved_bios_btn",
            "rex_saved_profiles_btn",
            "rex_watch_history_btn",
            "rex_video_splitter_btn",
            "rex_story_like_history_btn",
            "rex_story_mention_settings_btn",
            "rex_app_language_btn",
            "rex_rec_flags_refresh_file",
            "rex_enable_all_ghost",
            "rex_disable_all_ghost",
            "rex_enable_all_distractions",
            "rex_disable_all_distractions",
            "rex_quick_access_sheet",
            "rex_clear_cache_btn",
            "rex_creative_fonts_btn",
            "rex_private_screen_opacity_btn",
            "rex_passcode_lock_btn",
            "rex_hidden_chats_list_btn",
            "rex_whatsapp_type_btn",
            "rex_last_active_format_btn",
            "rex_emoji_type_btn",
            "rex_font_type_btn",
            Constants.REX_APP_VERSION_PREFERENCE,
            Constants.REX_PATCH_VERSION_PREFERENCE
    )));

    public static final Set<String> PRIMARY_ACTION_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "rex_enable_all_ghost",
            "rex_enable_all_distractions",
            "rex_export_pref",
            "rex_import_pref",
            "rex_rec_flags_refresh_file",
            "rex_quick_access_sheet"
    )));

    public static final Set<String> DESTRUCTIVE_ACTION_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "rex_reset_pref",
            "rex_pref_delete_font",
            "rex_reset_download_path",
            "rex_disable_all_ghost",
            "rex_disable_all_distractions",
            "rex_clear_cache_btn"
    )));

    private final Context context;
    private boolean pressedHighlightEnabled;

    public ButtonPref(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ButtonPref(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ButtonPref(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public static boolean isFragmentNavigation(String key) {
        return key != null && REGISTERED_ROUTE_KEYS.contains(key);
    }

    public static boolean isRegisteredAction(String key) {
        if (key == null) return false;
        return REGISTERED_ACTION_KEYS.contains(key) || SettingsStatus.FLAGS.containsKey(key);
    }

    public static boolean isRegisteredActionOrRoute(String key) {
        return isFragmentNavigation(key) || isRegisteredAction(key);
    }

    public static boolean isPrimaryActionKey(String key) {
        return key != null && PRIMARY_ACTION_KEYS.contains(key);
    }

    public static boolean isDestructiveActionKey(String key) {
        return key != null && DESTRUCTIVE_ACTION_KEYS.contains(key);
    }

    private void init() {
        setOnPreferenceClickListener(preference -> {
            try {
                RexHaptics.tap(context);
                String key = getKey();
                if (key == null) return false;

                if (!isRegisteredActionOrRoute(key)) {
                    Utils.showToastShort("Action unavailable: " + key);
                    return false;
                }

                if (key.equals("rex_export_dev_overrides")
                        || key.equals("rex_import_dev_overrides")
                        || key.equals("rex_import_id_mapping")
                        || key.equals("rex_export_pref")
                        || key.equals("rex_import_pref")
                        || key.equals("rex_download_set_path")
                        || key.equals("rex_pref_add_font")) {
                    ActivityHook.launchFragment((Activity) context, key);

                } else if (key.equals("rex_reset_download_path")) {
                    RexDialogs.showConfirmDialog(
                            context,
                            "Reset Download Path",
                            "Reset custom download folder back to default (/storage/emulated/0/Download/xboyrex)?",
                            "Reset Path",
                            true,
                            () -> {
                                StorageUtils.saveCustomPath("");
                                Utils.showToastShort("Download path reset to default");
                                Runnable recreate = recreateActivityOnComplete(context);
                                if (recreate != null) recreate.run();
                            }
                    );

                } else if (key.equals("rex_reset_pref")) {
                    InstaUtils.showResetSettingsDialog(context);

                } else if (key.equals("rex_focus_lock_action")) {
                    FocusLockDialogs.onActionPressed(context, ButtonPref.this);

                } else if (key.equals("rex_pref_delete_font")) {
                    RexDialogs.showConfirmDialog(
                            context,
                            "Delete Custom Font",
                            "Remove the imported custom font file and revert to default typography?",
                            "Delete Font",
                            true,
                            () -> {
                                FontStorage.DeleteResult result = FontStorage.delete();
                                Utils.showToastShort(str(
                                        result == FontStorage.DeleteResult.DELETED ? "rex_pref_delete_font_success" :
                                                result == FontStorage.DeleteResult.NOT_FOUND ? "rex_pref_delete_font_warn" :
                                                        "rex_pref_delete_font_fail"));
                            }
                    );

                } else if (key.equals("rex_export_experiment_list")) {
                    InstaUtils.decompileExperiments(false);

                } else if (key.equals("rex_export_experiment_mappings")) {
                    InstaUtils.decompileExperiments(true);

                } else if (key.equals("rex_download_id_mapping")) {
                    DownloadMapping.downloadMapping();

                } else if (key.equals("rex_view_saved_instants")) {
                    ActivityHook.launchActivity(context,
                            app.morphe.extension.instagram.patches.instants.InstantsVaultActivity.class);

                } else if (key.equals("view_deleted_messages")) {
                    SavedMessagesHook.openDeletedMessages(context);

                } else if (key.equals("rex_notification_center_btn")) {
                    RexDialogs.showNotificationCenterDialog(context);

                } else if (key.equals("rex_chat_wallpaper_picker")) {
                    RexRuntimeFeatures.pickChatWallpaper(context);

                } else if (key.equals("rex_saved_captions_btn")) {
                    RexVault.showSavedCaptionsDialog(context);

                } else if (key.equals("rex_saved_bios_btn")) {
                    RexVault.showSavedBiosDialog(context);

                } else if (key.equals("rex_saved_profiles_btn")) {
                    RexVault.showSavedProfilesDialog(context);

                } else if (key.equals("rex_watch_history_btn")) {
                    RexVault.showWatchHistoryDialog(context);

                } else if (key.equals("rex_video_splitter_btn")) {
                    RexRuntimeFeatures.showVideoSplitterDialog(context);

                } else if (key.equals("rex_story_like_history_btn")) {
                    StoryLikeHistoryActivity.open(context);

                } else if (key.equals("rex_story_mention_settings_btn")) {
                    StoryMentionSettings.showSettings(context);

                } else if (key.equals("rex_app_language_btn")) {
                    RexDialogs.showAppLanguagePicker(context);

                } else if (key.equals("rex_quick_access_sheet")) {
                    RexDialogs.showHomeQuickMenu(context);

                } else if (key.equals("rex_clear_cache_btn")) {
                    RexDialogs.showConfirmDialog(
                            context,
                            "Clear Cache & Temporary Media",
                            "Clear cached temporary media files and freed storage now?",
                            "Clear Cache",
                            true,
                            () -> RexRuntimeFeatures.clearTemporaryCache(context)
                    );

                } else if (key.equals("rex_creative_fonts_btn")) {
                    RexRuntimeFeatures.showCreativeFontsDialog(context);

                } else if (key.equals("rex_private_screen_opacity_btn")) {
                    RexRuntimeFeatures.showOpacityDialog(context);

                } else if (key.equals("rex_passcode_lock_btn")) {
                    RexRuntimeFeatures.showPasscodeSetupDialog(context);

                } else if (key.equals("rex_hidden_chats_list_btn")) {
                    RexRuntimeFeatures.showHiddenChatsListDialog(context);

                } else if (key.equals("rex_whatsapp_type_btn")) {
                    RexRuntimeFeatures.showWhatsAppTypePicker(context, recreateActivityOnComplete(context));

                } else if (key.equals("rex_last_active_format_btn")) {
                    RexRuntimeFeatures.showLastActiveFormatPicker(context, recreateActivityOnComplete(context));

                } else if (key.equals("rex_emoji_type_btn")) {
                    RexRuntimeFeatures.showEmojiTypePicker(context, recreateActivityOnComplete(context));

                } else if (key.equals("rex_font_type_btn")) {
                    RexRuntimeFeatures.showFontTypePicker(context, recreateActivityOnComplete(context));

                } else if (key.equals("rex_enable_all_ghost")) {
                    setGhostGroupState(true);
                    Utils.showToastShort("Enabled all Ghost Mode switches");
                    Runnable r = recreateActivityOnComplete(context);
                    if (r != null) r.run();

                } else if (key.equals("rex_disable_all_ghost")) {
                    setGhostGroupState(false);
                    Utils.showToastShort("Disabled all Ghost Mode switches");
                    Runnable r = recreateActivityOnComplete(context);
                    if (r != null) r.run();

                } else if (key.equals("rex_enable_all_distractions")) {
                    setDistractionGroupState(true);
                    Utils.showToastShort("Enabled all Distraction-Free filters");
                    Runnable r = recreateActivityOnComplete(context);
                    if (r != null) r.run();

                } else if (key.equals("rex_disable_all_distractions")) {
                    setDistractionGroupState(false);
                    Utils.showToastShort("Disabled all Distraction-Free filters");
                    Runnable r = recreateActivityOnComplete(context);
                    if (r != null) r.run();

                } else if (key.equals(Constants.REX_APP_VERSION_PREFERENCE)
                        || key.equals(Constants.REX_PATCH_VERSION_PREFERENCE)) {
                    CharSequence t = getTitle();
                    if (t != null) {
                        Utils.setClipboard(t.toString());
                        Utils.showToastShort("Copied: " + t);
                    }

                } else if (key.equals(Constants.REX_FRAGMENT_DEV_OPTIONS)) {
                    RexDialogs.showDevWarningDialog(context, () -> FragmentHook.startFragment(Constants.REX_FRAGMENT_DEV_OPTIONS));

                } else if (isFragmentNavigation(key)) {
                    FragmentHook.startFragment(key);

                } else if (key.equals("rex_rec_flags_refresh_file")) {
                    RecommendedFlags.downloadRecommendedFlagsFile(recreateActivityOnComplete(context));

                } else if (SettingsStatus.FLAGS.containsKey(key)) {
                    Boolean active = SettingsStatus.FLAGS.get(key);
                    Utils.showToastShort(key + ": " + (Boolean.TRUE.equals(active) ? "Active" : "Inactive"));
                }
            } catch (Exception e) {
                Utils.showToastShort(e.getMessage());
                Logger.printException(() -> "Preference button onclick failure", e);
            }
            return true;
        });
    }

    public static void setGhostGroupState(boolean enabled) {
        SharedPref.setBooleanPref(Settings.TURN_ON_ALL_GHOST_MODES.key, enabled);
        SharedPref.setBooleanPref(Settings.VIEW_DM_ANONYMOUSLY.key, enabled);
        SharedPref.setBooleanPref(Settings.MARK_AS_READ_AFTER_REPLY.key, enabled);
        SharedPref.setBooleanPref(Settings.DISABLE_TYPING_STATUS.key, enabled);
        SharedPref.setBooleanPref(Settings.DISABLE_SCREENSHOT_DETECTION.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_VIEW_ONCE_SEEN.key, enabled);
        SharedPref.setBooleanPref(Settings.UNLIMITED_REPLAYS.key, enabled);
        SharedPref.setBooleanPref(Settings.GHOST_INSTANTS.key, enabled);
        SharedPref.setBooleanPref(Settings.VIEW_STORIES_ANONYMOUSLY.key, enabled);
        SharedPref.setBooleanPref(Settings.VIEW_LIVE_ANONYMOUSLY.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_AUDIO_SEEN.key, enabled);
    }

    public static void setDistractionGroupState(boolean enabled) {
        SharedPref.setBooleanPref(Settings.DISABLE_STORIES.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_STORIES_TRAY.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_HIDDEN_REQUESTS.key, enabled);
        SharedPref.setBooleanPref(Settings.DISABLE_HIGHLIGHTS.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_NOTES_TRAY.key, enabled);
        SharedPref.setBooleanPref(Settings.DISABLE_EXPLORE.key, enabled);
        SharedPref.setBooleanPref(Settings.DISABLE_COMMENTS.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_FEED_POSTS.key, enabled);
        SharedPref.setBooleanPref(Settings.LIMIT_FOLLOWING_FEED.key, enabled);
        SharedPref.setBooleanPref(Settings.DISABLE_DISCOVER_PEOPLE.key, enabled);
        SharedPref.setBooleanPref(Settings.DISABLE_REELS_SCROLLING.key, enabled);
        SharedPref.setBooleanPref(Settings.DISABLE_SWIPE_TO_CREATE.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_GROUP_CREATION_BUTTON_ON_SHARESHEET.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_REELS_FOLLOW_BUTTON.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_LIKES.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_SHARE_BUTTON.key, enabled);
        SharedPref.setBooleanPref(Settings.HIDE_RESHARE_BUTTON.key, enabled);
    }

    private static Runnable recreateActivityOnComplete(Context context) {
        if (!(context instanceof Activity)) return null;
        WeakReference<Activity> activityReference = new WeakReference<>((Activity) context);
        return () -> {
            Activity activity = activityReference.get();
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                activity.recreate();
            }
        };
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return InstagramPreferenceStyle.createPreferenceView(
                context,
                InstagramPreferenceStyle.TRAILING_CHEVRON,
                iconResourceNameForKey(getKey())
        );
    }

    @Override
    protected void onBindView(View view) {
        String key = getKey();
        if (!isRegisteredActionOrRoute(key)) {
            setEnabled(false);
        }
        InstagramPreferenceStyle.bindText(this, view);
        InstagramPreferenceStyle.bindIcon(view, iconResourceNameForKey(key));
        InstagramPreferenceStyle.setTrailingVisible(view, hasVisibleTrail(key));
        InstagramPreferenceStyle.setPressedHighlightEnabled(view, pressedHighlightEnabled);
        InstagramPreferenceStyle.applyActionRoleStyling(view, key, isEnabled());
    }

    public static boolean hasVisibleTrail(String key) {
        return isRegisteredActionOrRoute(key)
                && !Constants.REX_APP_VERSION_PREFERENCE.equals(key)
                && !Constants.REX_PATCH_VERSION_PREFERENCE.equals(key)
                && !SettingsStatus.FLAGS.containsKey(key);
    }

    public void setPressedHighlightEnabled(boolean enabled) {
        if (pressedHighlightEnabled != enabled) {
            pressedHighlightEnabled = enabled;
            notifyChanged();
        }
    }

    public static String iconResourceNameForKey(String key) {
        if (key == null) return "rex_vec_hexagon";

        // 1. Root Category Navigation Cards
        if (key.equals(Constants.REX_FRAGMENT_GHOST)) return "rex_vec_ghost";
        if (key.equals(Constants.REX_FRAGMENT_NOTIFICATION)) return "rex_vec_bell";
        if (key.equals(Constants.REX_FRAGMENT_PRIVACY) || key.equals(Constants.REX_FRAGMENT_ADS)) return "rex_vec_shield";
        if (key.equals(Constants.REX_FRAGMENT_GESTURES)) return "rex_vec_gesture";
        if (key.equals(Constants.REX_FRAGMENT_FEED_AND_STORIES) || key.equals(Constants.REX_FRAGMENT_STORY)) return "rex_vec_feed";
        if (key.equals(Constants.REX_FRAGMENT_UI_TWEAKS)) return "rex_vec_ui";
        if (key.equals(Constants.REX_FRAGMENT_DISTRACTION_FREE) || key.equals(Constants.REX_FRAGMENT_FOCUS_LOCK)) return "rex_vec_focus";
        if (key.equals(Constants.REX_FRAGMENT_MISC)) return "rex_vec_misc";
        if (key.equals(Constants.REX_FRAGMENT_DOWNLOAD_MEDIA)) return "rex_vec_download";
        if (key.equals(Constants.REX_FRAGMENT_DEV_OPTIONS) || key.equals(Constants.REX_FRAGMENT_REC_FLAGS)) return "rex_vec_code_slash";
        if (key.equals(Constants.REX_FRAGMENT_ABOUT)) return "rex_vec_info";

        // 2. Ghost Mode Subpage — Distinct Vector Icons per Row (Matching Reference Screen 2)
        if (key.equals(Settings.DISABLE_TYPING_STATUS.key)) return "rex_vec_typing_dm";
        if (key.equals(Settings.VIEW_STORIES_ANONYMOUSLY.key)) return "rex_vec_story_ring";
        if (key.equals(Settings.VIEW_LIVE_ANONYMOUSLY.key)) return "rex_vec_live_badge";
        if (key.equals(Settings.HIDE_VIEW_ONCE_SEEN.key)) return "rex_vec_view_once";
        if (key.equals(Settings.DISABLE_SCREENSHOT_DETECTION.key)) return "rex_vec_screenshot";
        if (key.equals(Settings.UNLIMITED_REPLAYS.key)) return "rex_vec_replay";
        if (key.equals(Settings.VIEW_DM_ANONYMOUSLY.key)) return "rex_vec_eye_off";
        if (key.equals(Settings.MARK_AS_READ_AFTER_REPLY.key)) return "rex_vec_mark_read";
        if (key.equals(Settings.GHOST_INSTANTS.key)) return "rex_vec_camera_bolt";
        if (key.equals(Settings.HIDE_AUDIO_SEEN.key)) return "rex_vec_mic";
        if (key.equals(Settings.TURN_ON_ALL_GHOST_MODES.key)) return "rex_vec_ghost";
        if (key.equals("rex_last_active_format_btn")) return "rex_vec_history";
        if (key.equals("rex_enable_all_ghost") || key.equals("rex_disable_all_ghost")
                || key.equals("rex_enable_all_distractions") || key.equals("rex_disable_all_distractions")) {
            return "rex_vec_bars";
        }

        // 3. Notification Subpage — Distinct Vector Icons
        if (key.equals(Settings.DND_MODE.key) || key.equals(Settings.HIDDEN_CHATS_MUTE.key)) return "rex_vec_bell_off";
        if (key.equals("rex_notification_center_btn")) return "rex_vec_bell";
        if (key.equals(Settings.INBOX_LOCK_NOTIFICATIONS.key)) return "rex_vec_lock";

        // 4. Privacy Subpage — Distinct Vector Icons (Matching Reference Screen 3 & 8)
        if (key.equals(Settings.DISABLE_ADS.key) || key.equals(Settings.HIDE_REELS_AND_STORIES_AD_PODS.key)) return "rex_vec_megaphone";
        if (key.equals(Settings.DISABLE_ANALYTICS.key)) return "rex_vec_analytics";
        if (key.equals(Settings.HIDE_SUGGESTED_CONTENT.key)) return "rex_vec_suggested";
        if (key.equals(Settings.SANITIZE_SHARE_LINKS.key) || key.equals(Settings.CUSTOM_SHARING_DOMAIN.key)
                || key.equals(Constants.REX_FRAGMENT_LINKS)) return "rex_vec_link";
        if (key.equals(Settings.OPEN_LINKS_EXTERNALLY.key)) return "rex_vec_external_link";
        if (key.equals(Settings.INBOX_LOCK.key) || key.equals("rex_passcode_lock_btn")) return "rex_vec_lock";
        if (key.equals(Settings.HIDDEN_CHATS.key) || key.equals("rex_hidden_chats_list_btn")
                || key.equals(Settings.HIDDEN_CHATS_TAPS.key)) return "rex_vec_eye_off";
        if (key.equals(Settings.PRIVATE_SCREEN_MODE.key) || key.equals("rex_private_screen_opacity_btn")) return "rex_vec_sun";
        if (key.equals(Settings.RESTORE_TRUST_SPOOF.key)) return "rex_vec_shield_check";

        // 5. Gestures Subpage — Distinct Vector Icons
        if (key.contains("double_tap")) return "rex_vec_heart_off";
        if (key.equals(Settings.DISABLE_SWIPE_TO_CREATE.key)
                || key.equals(Settings.DISABLE_DISAPPEARING_MESSAGES_GESTURE.key)) return "rex_vec_swipe";
        if (key.equals(Settings.LONG_PRESS_POST_OPTIONS.key)) return "rex_vec_more_menu";
        if (key.equals(Settings.IMPROVE_IMAGE_VIEWING.key)) return "rex_vec_zoom";
        if (key.equals(Settings.HOLD_VIDEO_FAST_FORWARD.key)) return "rex_vec_fast_forward";

        // 6. Feed and Stories Subpage — Distinct Vector Icons
        if (key.equals(Settings.DISABLE_VIDEO_AUTOPLAY.key)) return "rex_vec_video_play";
        if (key.equals(Settings.START_FEED_VIDEOS_MUTED.key)
                || key.equals(Settings.PLAY_STORY_SOUND_AUTOMATICALLY.key)) return "rex_vec_speaker";
        if (key.equals(Settings.RESUME_REELS_AFTER_SCROLLING.key)
                || key.equals(Settings.DISABLE_REELS_LOOPING.key)) return "rex_vec_reels";
        if (key.equals(Settings.DISABLE_STORY_FLIPPING.key)) return "rex_vec_skip_next";
        if (key.equals(Settings.LOOP_STORY.key)) return "rex_vec_replay";
        if (key.equals(Settings.VIEW_STORY_MENTIONS.key) || key.equals(Settings.STORY_MENTION_ICON.key)
                || key.equals("rex_story_mention_settings_btn")) return "rex_vec_at";
        if (key.equals("rex_story_like_history_btn")) return "rex_vec_story_ring";
        if (key.equals(Settings.HIDE_STORY_FOLLOW_BUTTON.key)) return "rex_vec_user_plus";
        if (key.equals(Settings.CUSTOMISE_STORY_TIMESTAMP.key)) return "rex_vec_history";
        if (key.equals(Settings.CUSTOMISE_STORY_RING_SIZE.key)) return "rex_vec_story_size";
        if (key.equals(Settings.HIGH_QUALITY_UPLOAD.key) || key.equals(Settings.UPLOAD_60FPS_STORIES.key)
                || key.equals(Settings.EXTENDED_STORY_DURATION.key)) return "rex_vec_hd_upload";

        // 7. UI Tweaks Subpage — Distinct Vector Icons (Matching Reference Screen 4)
        if (key.equals(Settings.AMOLED_THEME.key)) return "rex_vec_sun";
        if (key.equals(Settings.MATERIAL_YOU_THEME.key)) return "rex_vec_palette";
        if (key.equals("rex_app_language_btn")) return "rex_vec_globe";
        if (key.equals(Constants.REX_FRAGMENT_FONT) || key.equals(Settings.USE_SYSTEM_FONT.key)
                || key.equals("rex_pref_add_font") || key.equals("rex_font_type_btn")
                || key.equals("rex_creative_fonts_btn")) return "rex_vec_font";
        if (key.equals("rex_pref_delete_font")) return "rex_vec_trash";
        if (key.equals("rex_emoji_type_btn")) return "rex_vec_emoji";
        if (key.equals(Settings.CHANGE_LIKE_ANIMATION.key)) return "rex_vec_heart";
        if (key.equals(Settings.CUSTOM_CHAT_WALLPAPER.key) || key.equals("rex_chat_wallpaper_picker")) return "rex_vec_image";
        if (key.equals(Constants.REX_FRAGMENT_ACTION_BAR) || key.equals(Settings.REELS_FLOATING_TOOLBAR.key)) return "rex_vec_sliders";
        if (key.equals(Constants.REX_FRAGMENT_NAV_BTNS)) return "rex_vec_nav_bar";
        if (key.equals(Settings.REMOVE_EMPTY_BOTTOM_SPACE.key)) return "rex_vec_collapse";
        if (key.equals(Settings.STORY_FILTERS_MASTER.key) || key.equals(Constants.REX_FRAGMENT_FILTER_CONTENT)) return "rex_vec_story_ring";

        // 8. Distraction-Free & Focus Mode Subpage (Matching Reference Screen 14)
        if (key.equals("rex_focus_lock_action")) return "rex_vec_focus";
        if (key.equals(Settings.DISABLE_REELS_SCROLLING.key) || key.equals(Settings.HIDE_REELS_FOLLOW_BUTTON.key)) return "rex_vec_reels";
        if (key.equals(Settings.HIDE_FEED_POSTS.key) || key.equals(Settings.LIMIT_FOLLOWING_FEED.key)) return "rex_vec_post_grid";
        if (key.equals(Settings.DISABLE_STORIES.key) || key.equals(Settings.HIDE_STORIES_TRAY.key)
                || key.equals(Settings.DISABLE_HIGHLIGHTS.key)) return "rex_vec_story_ring";
        if (key.equals(Settings.HIDE_NOTES_TRAY.key) || key.equals(Settings.HIDE_HIDDEN_REQUESTS.key)) return "rex_vec_typing_dm";
        if (key.equals(Settings.DISABLE_EXPLORE.key) || key.equals(Settings.DISABLE_DISCOVER_PEOPLE.key)) return "rex_vec_search";
        if (key.equals(Settings.DISABLE_COMMENTS.key)) return "rex_vec_comment";
        if (key.equals(Settings.HIDE_LIKES.key)) return "rex_vec_heart_off";
        if (key.equals(Settings.HIDE_SHARE_BUTTON.key) || key.equals(Settings.HIDE_RESHARE_BUTTON.key)
                || key.equals(Settings.HIDE_GROUP_CREATION_BUTTON_ON_SHARESHEET.key)) return "rex_vec_external_link";
        if (key.equals(Settings.HIDE_SAVE_BUTTON_SURFACES.key)) return "rex_vec_bookmark";

        // 9. Miscellaneous & Vaults Subpage (Matching Reference Screens 6, 8, 13)
        if (key.equals(Settings.FOLLOW_BACK_INDICATOR.key) || key.equals(Settings.FOLLOW_BACK_COLOR_INDICATOR.key)) return "rex_vec_user_plus";
        if (key.equals(Settings.FOLLOW_LIST_NON_FOLLOWER_BADGE.key)) return "rex_vec_people";
        if (key.equals(Settings.ENABLE_MORE_OPTIONS_ON_POST.key)) return "rex_vec_more_menu";
        if (key.equals(Settings.COMMENT_COPY_BUTTON.key) || key.equals(Settings.COMMENT_SAVE_MEDIA_BUTTON.key)) return "rex_vec_comment";
        if (key.equals(Settings.ENABLE_MARK_CHAT_AS_READ.key)) return "rex_vec_mark_read";
        if (key.equals(Settings.SAVE_DELETED_MESSAGES.key) || key.equals("view_deleted_messages")) return "rex_vec_trash";
        if (key.equals(Settings.EPHEMERAL_MEDIA_CAPTION.key) || key.equals("rex_saved_captions_btn")) return "rex_vec_cc_box";
        if (key.equals("rex_saved_bios_btn")) return "rex_vec_edit_box";
        if (key.equals("rex_saved_profiles_btn")) return "rex_vec_bookmark";
        if (key.equals("rex_watch_history_btn") || key.equals(Settings.DISABLE_WATCH_HISTORY.key)) return "rex_vec_history";
        if (key.equals(Settings.COPY_CAPTION_WITHOUT_HASHTAGS.key)) return "rex_vec_doc";
        if (key.equals(Settings.REPOST_WITH_CAPTION_WA.key) || key.equals("rex_whatsapp_type_btn")) return "rex_vec_external_link";
        if (key.equals("rex_video_splitter_btn")) return "rex_vec_scissors";
        if (key.equals("rex_clear_cache_btn")) return "rex_vec_trash";
        if (key.equals(Settings.INSTANTS_DOWNLOAD.key) || key.equals("rex_view_saved_instants")
                || key.equals(Constants.REX_FRAGMENT_INSTANTS)) return "rex_vec_camera_bolt";

        // 10. Downloads Subpage (Matching Reference Screen 5)
        if (key.equals(Settings.ENABLE_DOWNLOAD.key) || key.equals(Settings.ENABLE_DIRECT_DOWNLOAD.key)) return "rex_vec_download";
        if (key.equals(Settings.DOWNLOAD_USERNAME_FOLDER.key)) return "rex_vec_user_folder";
        if (key.equals(Settings.PREFER_DASH_FORMAT.key) || key.equals(Settings.DASH_AUDIO_AAC_LC.key)) return "rex_vec_bars";
        if (key.equals(Settings.EMBED_DOWNLOAD_METADATA.key) || key.equals(Settings.DOWNLOAD_FILE_NAME_TEMPLATE.key)) return "rex_vec_doc";
        if (key.equals("rex_download_set_path")) return "rex_vec_pin";
        if (key.equals("rex_reset_download_path")) return "rex_vec_refresh";
        if (key.equals(Settings.DOWNLOAD_WITH_EXTERNAL_DOWNLOADER.key)
                || key.equals(Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME.key)) return "rex_vec_external_link";

        // 11. Developer Options & Backup Subpage (Matching Reference Screen 15)
        if (key.equals("rex_export_pref") || key.equals("rex_export_dev_overrides")
                || key.equals("rex_export_experiment_list") || key.equals("rex_export_experiment_mappings")) return "rex_vec_hd_upload";
        if (key.equals("rex_import_pref") || key.equals("rex_import_dev_overrides")
                || key.equals("rex_import_id_mapping") || key.equals("rex_download_id_mapping")) return "rex_vec_download";
        if (key.equals("rex_reset_pref")) return "rex_vec_trash";
        if (key.equals("rex_rec_flags_refresh_file")) return "rex_vec_refresh";
        if (key.equals(Settings.DEVELOPER_OPTIONS.key) || key.equals(Settings.DIRECTLY_OPEN_METACONFIG.key)
                || key.equals(Settings.REX_DEBUG.key)) return "rex_vec_code_slash";
        if (key.equals(Constants.REX_FRAGMENT_DM)) return UI.DRAWABLE_SHARE_TO_DIRECT;
        return "rex_vec_gear";
    }
}
