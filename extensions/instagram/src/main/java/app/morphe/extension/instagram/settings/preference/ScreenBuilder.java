/*
 * Copyright (C) 2026 rex <https://github.com/xboyrex/rex-ig-lite>
 *
 * See the included NOTICE file for GPLv3 §7(b) terms that apply to this code.
 */

package app.morphe.extension.instagram.settings.preference;

import static app.morphe.extension.instagram.utils.IgStr.str;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import app.morphe.extension.instagram.constants.Constants;
import app.morphe.extension.instagram.db.RexMessageDb;
import app.morphe.extension.instagram.patches.Links;
import app.morphe.extension.instagram.patches.customise.font.FontStorage;
import app.morphe.extension.instagram.patches.devFlags.Flag;
import app.morphe.extension.instagram.patches.devFlags.RecommendedFlags;
import app.morphe.extension.instagram.patches.dm.SavedMessagesHook;
import app.morphe.extension.instagram.patches.focusLock.FocusLock;
import app.morphe.extension.instagram.patches.focusLock.FocusLockTargets;
import app.morphe.extension.instagram.settings.Settings;
import app.morphe.extension.instagram.settings.SettingsStatus;
import app.morphe.extension.instagram.settings.preference.fragments.FragmentHook;
import app.morphe.extension.instagram.settings.preference.widgets.ButtonPref;
import app.morphe.extension.instagram.settings.preference.widgets.CategoryPref;
import app.morphe.extension.instagram.settings.preference.widgets.FocusLockDurationPreference;
import app.morphe.extension.instagram.settings.preference.widgets.InstagramPreferenceStyle;
import app.morphe.extension.instagram.settings.preference.widgets.SwitchPref;
import app.morphe.extension.instagram.theme.MaterialYouTheme;
import app.morphe.extension.instagram.ui.RexDialogs;
import app.morphe.extension.instagram.ui.RexVectorIcons;
import app.morphe.extension.instagram.utils.Pref;
import app.morphe.extension.rex.RexUtils;
import app.morphe.extension.rex.downloader.StorageUtils;
import app.morphe.extension.rex.settings.BooleanSetting;
import app.morphe.extension.rex.sharedPreference.SharedPref;
import app.morphe.extension.shared.Utils;

public class ScreenBuilder {
    private final Context context;
    private final PreferenceScreen screen;
    private final Helper helper;

    public ScreenBuilder(Context context, PreferenceScreen screen, Helper helper) {
        this.context = context;
        this.screen = screen;
        this.helper = helper;
    }

    private void addPreference(Preference pref) {
        addPreference(null, pref);
    }

    private void addPreference(PreferenceCategory category, Preference pref) {
        if (category != null) {
            category.addPreference(pref);
        } else {
            screen.addPreference(pref);
        }
    }

    private PreferenceCategory addCategory(String title) {
        CategoryPref preferenceCategory = new CategoryPref(context);
        preferenceCategory.setFirstCategory(screen.getPreferenceCount() == 0);
        preferenceCategory.setTitle(title);
        screen.addPreference(preferenceCategory);
        return preferenceCategory;
    }

    /**
     * Root Settings Page matching Screenshots 063050 & 063059
     */
    public void buildSettingsPage() {
        PreferenceCategory categories = addCategory("Categories");

        // 1. Premium
        addPreference(categories, helper.categoryPreference(
                "Premium",
                "Premium Features Enabled • Lifetime Access",
                Constants.REX_FRAGMENT_PREMIUM,
                RexVectorIcons.IC_DIAMOND
        ));

        // 2. View Blocked Profiles Secretly 👑
        ButtonPref blockedPref = helper.actionButtonPreference(
                "View Blocked Profiles Secretly 👑",
                "Bypass the block and anonymously see posts, stories & follower counts of accounts that blocked you.",
                RexVectorIcons.IC_SPARKLES,
                () -> RexDialogs.showSecretBlockedProfileDialog(context)
        );
        blockedPref.setPressedHighlightEnabled(true);
        addPreference(categories, blockedPref);

        // 3. Ghost Mode
        addPreference(categories, helper.categoryPreference(
                "Ghost Mode",
                "Watch stories, lives, and DMs secretly",
                Constants.REX_FRAGMENT_GHOST,
                RexVectorIcons.IC_GHOST
        ));

        // 4. Notification
        addPreference(categories, helper.categoryPreference(
                "Notification",
                "Customize Notification Behaviors",
                Constants.REX_FRAGMENT_NOTIFICATION,
                RexVectorIcons.IC_BELL
        ));

        // 5. Privacy
        addPreference(categories, helper.categoryPreference(
                "Privacy",
                "Remove tracking from links and block data collection",
                Constants.REX_FRAGMENT_PRIVACY,
                RexVectorIcons.IC_SHIELD_OUTLINE
        ));

        // 6. Gestures
        addPreference(categories, helper.categoryPreference(
                "Gestures",
                "Customize swipe and tap gestures",
                Constants.REX_FRAGMENT_GESTURES,
                RexVectorIcons.IC_HAND
        ));

        // 7. Feed and Stories
        addPreference(categories, helper.categoryPreference(
                "Feed and Stories",
                "Auto-play, story rings, and unfollower indicators",
                Constants.REX_FRAGMENT_FEED_STORIES,
                RexVectorIcons.IC_LAYERS
        ));

        // 8. UI Tweaks
        addPreference(categories, helper.categoryPreference(
                "UI Tweaks",
                "Customize buttons, navigation bar, and layout",
                Constants.REX_FRAGMENT_UI_TWEAKS,
                RexVectorIcons.IC_PALETTE
        ));

        // 9. Focus Mode
        addPreference(categories, helper.categoryPreference(
                "Focus Mode",
                "Hide reels, stories, and distractions",
                Constants.REX_FRAGMENT_FOCUS_MODE,
                RexVectorIcons.IC_SPARKLES
        ));

        // 10. Miscellaneous
        addPreference(categories, helper.categoryPreference(
                "Miscellaneous",
                "Saved data, Watch History, Translation and other tools",
                Constants.REX_FRAGMENT_MISC,
                RexVectorIcons.IC_SLIDERS
        ));

        // 11. Downloads
        addPreference(categories, helper.categoryPreference(
                "Downloads",
                "Configure media saving, folders, and filenames",
                Constants.REX_FRAGMENT_DOWNLOAD_MEDIA,
                RexVectorIcons.IC_DOWNLOAD
        ));

        // 12. Developer Options
        addPreference(categories, helper.categoryPreference(
                "Developer Options",
                "Unlock MetaConfig, internal settings, and flags",
                Constants.REX_FRAGMENT_DEV_OPTIONS,
                RexVectorIcons.IC_WRENCH
        ));

        // Info Section
        PreferenceCategory infoCat = addCategory("Info");

        addPreference(infoCat, helper.categoryPreference(
                "About RexPrime",
                "Credits, social links, and app configuration",
                Constants.REX_FRAGMENT_ABOUT,
                RexVectorIcons.IC_INFO
        ));

        ButtonPref changelogBtn = helper.actionButtonPreference(
                "Changelogs",
                "Tap to view what's new in this update",
                RexVectorIcons.IC_SPARKLES,
                () -> RexDialogs.showChangelogsDialog(context)
        );
        changelogBtn.setPressedHighlightEnabled(true);
        addPreference(infoCat, changelogBtn);
    }

    /**
     * Premium Screen (Screenshot 063132)
     */
    public void premiumSection() {
        PreferenceCategory statusCat = addCategory("Active Plan Status");
        addPreference(statusCat, helper.actionButtonPreference(
                "RexPrime Active",
                "All pro features, secret profile tools, and high-speed media downloads are unlocked for your account.",
                RexVectorIcons.IC_DIAMOND,
                () -> RexUtils.toast("RexPrime Lifetime Access is active! 👑")
        ));
        if (SettingsStatus.unlockPlusBenefits) {
            addPreference(statusCat, helper.switchPreference(
                    str("rex_unlock_plus_benefits"),
                    str("rex_unlock_plus_benefits_desc"),
                    Settings.UNLOCK_PLUS_BENEFITS,
                    RexVectorIcons.IC_SPARKLES
            ));
        }

        PreferenceCategory featCat = addCategory("Exclusive Features");
        addPreference(featCat, helper.actionButtonPreference(
                "Ghost Mode Plus",
                "View stories, lives, and messages without leaving any seen trace.",
                RexVectorIcons.IC_GHOST,
                () -> FragmentHook.startFragment(Constants.REX_FRAGMENT_GHOST)
        ));
        addPreference(featCat, helper.actionButtonPreference(
                "Secret Profile Viewer",
                "Bypass blocks and view public account data anonymously.",
                RexVectorIcons.IC_EYE_OFF,
                () -> RexDialogs.showSecretBlockedProfileDialog(context)
        ));
        addPreference(featCat, helper.actionButtonPreference(
                "Ad-Free Experience",
                "Zero sponsored posts, story ads, or promotional interruptions.",
                RexVectorIcons.IC_SHIELD_OUTLINE,
                () -> FragmentHook.startFragment(Constants.REX_FRAGMENT_PRIVACY)
        ));
        addPreference(featCat, helper.actionButtonPreference(
                "High-Speed Media Downloader",
                "One-tap HD downloads for reels, posts, stories, and profile pictures.",
                RexVectorIcons.IC_DOWNLOAD,
                () -> FragmentHook.startFragment(Constants.REX_FRAGMENT_DOWNLOAD_MEDIA)
        ));

        PreferenceCategory subCat = addCategory("Subscription Details");
        addPreference(subCat, helper.buttonPreference(
                "Plan Type",
                "Lifetime Access (Unlocked by Mayank)",
                "rex_plan_type",
                RexVectorIcons.IC_STAR
        ));
        addPreference(subCat, helper.linkButtonPreference(
                "Developer",
                "Mayank (@xboyrex) • OnyxHub7",
                RexVectorIcons.IC_USER,
                RexVectorIcons.IC_LINK_CHAIN,
                () -> RexUtils.openUrl(Constants.AUTHOR_URL, true)
        ));
    }

    /**
     * Ghost Mode Screen (Screenshot 063218)
     */
    public void ghostSection() {
        addPreference(helper.switchPreference(
                "Turn on all Ghost modes",
                "Enable all ghost mode features at once",
                Settings.TURN_ON_ALL_GHOST_MODES,
                RexVectorIcons.IC_GHOST
        ));
        addPreference(helper.switchPreference(
                "Hide Story View",
                "Watch others' stories without them knowing",
                Settings.VIEW_STORIES_ANONYMOUSLY,
                RexVectorIcons.IC_EYE_OFF
        ));
        addPreference(helper.switchPreference(
                "Hide Live View",
                "Watch live broadcasts anonymously",
                Settings.VIEW_LIVE_ANONYMOUSLY,
                RexVectorIcons.IC_BROADCAST
        ));
        addPreference(helper.switchPreference(
                "Hide Screenshot Taken",
                "Take screenshots in DMs without notifying the other person",
                Settings.DISABLE_SCREENSHOT_DETECTION,
                RexVectorIcons.IC_CAMERA_OFF
        ));
        addPreference(helper.switchPreference(
                "Hide Typing Status",
                "Don't show 'Typing...' indicator in direct messages",
                Settings.DISABLE_TYPING_STATUS,
                RexVectorIcons.IC_KEYBOARD
        ));
        addPreference(helper.switchPreference(
                "Hide Message Seen",
                "Read direct messages without sending read receipts",
                Settings.VIEW_DM_ANONYMOUSLY,
                RexVectorIcons.IC_CHECKS
        ));
        addPreference(helper.switchPreference(
                "Mark Chat as Read",
                "Adds an option to manually mark chats as read when 'Hide Message Seen' is active",
                Settings.ENABLE_MARK_CHAT_AS_READ,
                RexVectorIcons.IC_MAIL_CHECK
        ));
        addPreference(helper.switchPreference(
                "Hide Story Replies",
                "Hides the 'Send message' reply bar at the bottom of stories to prevent accidental replies",
                Settings.HIDE_STORY_REPLIES,
                RexVectorIcons.IC_MESSAGE_OFF
        ));
        addPreference(helper.switchPreference(
                "Hide Call Buttons",
                "Hides audio and video call buttons in DMs to prevent accidental calls",
                Settings.HIDE_CALL_BUTTONS,
                RexVectorIcons.IC_PHONE_OFF
        ));
        addPreference(helper.switchPreference(
                "Ghost Instants",
                "View disappearing Instants without notifying the sender",
                Settings.GHOST_INSTANTS,
                RexVectorIcons.IC_SPARKLES
        ));
        if (SettingsStatus.unlimitedReplaysOnEphemeralMedia) {
            addPreference(helper.switchPreference(
                    str("rex_unlimited_replays"),
                    str("rex_unlimited_replays_desc"),
                    Settings.UNLIMITED_REPLAYS,
                    RexVectorIcons.IC_INFINITY
            ));
        }
        if (SettingsStatus.inboxLock) {
            addPreference(helper.switchPreference(
                    str("rex_inbox_lock"),
                    str("rex_inbox_lock_desc"),
                    Settings.INBOX_LOCK,
                    RexVectorIcons.IC_LOCK
            ));
            addPreference(helper.switchPreference(
                    str("rex_inbox_lock_notifications"),
                    str("rex_inbox_lock_notifications_desc"),
                    Settings.INBOX_LOCK_NOTIFICATIONS,
                    RexVectorIcons.IC_BELL
            ));
        }
        if (SettingsStatus.instantsDownload) {
            addPreference(helper.buttonPreference(
                    str("rex_instants_title"),
                    "Download and view saved disappearing Instants",
                    Constants.REX_FRAGMENT_INSTANTS,
                    RexVectorIcons.IC_SPARKLES
            ));
        }
    }

    /**
     * Notification & DM Screen (Screenshot 063214)
     */
    public void notificationSection() {
        PreferenceCategory dmCat = addCategory("Direct Messages");

        addPreference(dmCat, helper.switchPreference(
                "Save Deleted Messages",
                "Keep a local log of unsent messages and notify you when someone deletes a message",
                Settings.SAVE_DELETED_MESSAGES,
                RexVectorIcons.IC_DOWNLOAD
        ));
        addPreference(dmCat, helper.buttonPreference(
                "Deleted Messages Viewer",
                "View all messages that were unsent by others",
                "view_deleted_messages",
                RexVectorIcons.IC_EYE
        ));
        addPreference(dmCat, helper.actionButtonPreference(
                "Clear Saved Messages",
                "Delete all locally saved unsent messages",
                RexVectorIcons.IC_TRASH,
                () -> {
                    new AlertDialog.Builder(InstagramPreferenceStyle.dialogContext(context))
                            .setTitle("Clear Saved Messages")
                            .setMessage("Are you sure you want to delete all locally saved unsent messages?")
                            .setPositiveButton("Clear", (d, w) -> {
                                try {
                                    RexMessageDb db = RexMessageDb.getInstance(context);
                                    List<String[]> rows = db.getDeletedMessages();
                                    ArrayList<String> ids = new ArrayList<>();
                                    for (String[] r : rows) {
                                        if (r != null && r.length > 0) ids.add(r[0]);
                                    }
                                    db.deleteSaved(ids);
                                    RexUtils.toast("Cleared all saved deleted messages");
                                } catch (Exception e) {
                                    RexUtils.toast("Cleared saved messages");
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        ));

        PreferenceCategory shareCat = addCategory("Share & Activity Alerts");
        addPreference(shareCat, helper.switchPreference(
                "Reels Share Notification",
                "Show a confirmation toast when a Reel link is copied or shared",
                Settings.REELS_SHARE_NOTIFICATION,
                RexVectorIcons.IC_FILM
        ));
        addPreference(shareCat, helper.switchPreference(
                "Story Share Notification",
                "Show a notification when a story is shared externally",
                Settings.STORY_SHARE_NOTIFICATION,
                RexVectorIcons.IC_HISTORY
        ));
        addPreference(shareCat, helper.switchPreference(
                "Post Share Notification",
                "Show a confirmation alert when a post is shared",
                Settings.POST_SHARE_NOTIFICATION,
                RexVectorIcons.IC_SHARE
        ));
        addPreference(shareCat, helper.switchPreference(
                "Screenshot Notification",
                "Show a status toast confirming when screenshot detection is blocked",
                Settings.SCREENSHOT_NOTIFICATION,
                RexVectorIcons.IC_CAMERA_OFF
        ));
        addPreference(shareCat, helper.switchPreference(
                "Story Download Notification",
                "Show a progress and completion notification when downloading stories",
                Settings.STORY_DOWNLOAD_NOTIFICATION,
                RexVectorIcons.IC_BELL
        ));
    }

    public void dmSection() {
        notificationSection();
    }

    /**
     * Privacy Screen (Screenshot 063228)
     */
    public void privacySection() {
        addPreference(helper.switchPreference(
                "Disable Ads",
                "Hides ads in stories, reels, feed, and everywhere else",
                Settings.DISABLE_ADS,
                RexVectorIcons.IC_SHIELD_OUTLINE
        ));
        addPreference(helper.switchPreference(
                "Hide Suggested Content",
                "Hides suggested reels, threads, and posts from the main feed",
                Settings.HIDE_SUGGESTED_CONTENT,
                RexVectorIcons.IC_SPARKLES
        ));
        addPreference(helper.switchPreference(
                "Open Links Externally",
                "Opens links in your default browser instead of Instagram's in-app browser",
                Settings.OPEN_LINKS_EXTERNALLY,
                RexVectorIcons.IC_EXTERNAL_LINK
        ));
        addPreference(helper.switchPreference(
                "Sanitize Share Links",
                "Strips tracking parameters (like igsh) from copied and shared URLs",
                Settings.SANITIZE_SHARE_LINKS,
                RexVectorIcons.IC_LINK_CHAIN
        ));
        addPreference(helper.editTextPreference(
                "Custom Sharing Domain",
                Links.customSharingDomainSummary(Pref.customSharingDomain()),
                Settings.CUSTOM_SHARING_DOMAIN,
                RexVectorIcons.IC_GLOBE
        ));
        addPreference(helper.switchPreference(
                "Disable Analytics",
                "Blocks Instagram telemetry and analytics tracking requests",
                Settings.DISABLE_ANALYTICS,
                RexVectorIcons.IC_CHART
        ));
        addPreference(helper.buttonPreference(
                str("rex_delete_analytics_cache"),
                "Delete cached analytics logs from device storage",
                "rex_delete_analytics_cache",
                RexVectorIcons.IC_TRASH
        ));
    }

    public void buildAdsSection() {
        privacySection();
    }

    public void linksSection() {
        privacySection();
    }

    /**
     * Gestures Screen (Screenshot 063232)
     */
    public void gesturesSection() {
        PreferenceCategory likeCat = addCategory("Disable Double Tap to Like");
        addPreference(likeCat, helper.forcedSwitchPreference(
                "Post Like",
                "Disables double-tap to like on feed posts",
                Settings.DISABLE_DOUBLE_TAP_LIKE_POST,
                FocusLock.isForced(Settings.DISABLE_DOUBLE_TAP_LIKE_POST),
                str("rex_focus_lock_enforced"),
                RexVectorIcons.IC_HEART
        ));
        addPreference(likeCat, helper.forcedSwitchPreference(
                "Reel Like",
                "Disables double-tap to like on reels",
                Settings.DISABLE_DOUBLE_TAP_LIKE_REEL,
                FocusLock.isForced(Settings.DISABLE_DOUBLE_TAP_LIKE_REEL),
                str("rex_focus_lock_enforced"),
                RexVectorIcons.IC_FILM
        ));
        addPreference(likeCat, helper.forcedSwitchPreference(
                "Comment Like",
                "Disables double-tap to like on comments",
                Settings.DISABLE_DOUBLE_TAP_LIKE_COMMENT,
                FocusLock.isForced(Settings.DISABLE_DOUBLE_TAP_LIKE_COMMENT),
                str("rex_focus_lock_enforced"),
                RexVectorIcons.IC_MESSAGE_CIRCLE
        ));
        addPreference(likeCat, helper.forcedSwitchPreference(
                "Message Like",
                "Disables double-tap to like on direct messages",
                Settings.DISABLE_DOUBLE_TAP_LIKE_MESSAGE,
                FocusLock.isForced(Settings.DISABLE_DOUBLE_TAP_LIKE_MESSAGE),
                str("rex_focus_lock_enforced"),
                RexVectorIcons.IC_SEND
        ));

        PreferenceCategory navCat = addCategory("Playback & Navigation Gestures");
        addPreference(navCat, helper.switchPreference(
                "Swipe Camera Gesture",
                "Enable or disable swiping right on the home feed to open the camera",
                Settings.SWIPE_CAMERA_GESTURE,
                RexVectorIcons.IC_SWIPE_LEFT
        ));
        addPreference(navCat, helper.switchPreference(
                "Reels Tap & Hold Speed",
                "Tap and hold on the edge of a reel to play at 2x speed",
                Settings.REELS_TAP_HOLD_SPEED,
                RexVectorIcons.IC_FAST_FORWARD
        ));
        addPreference(navCat, helper.switchPreference(
                "Reels Lock Controls",
                "Lock reel UI overlays when tapping the screen",
                Settings.REELS_LOCK_CONTROLS,
                RexVectorIcons.IC_LOCK
        ));
        addPreference(navCat, helper.switchPreference(
                "Story Auto-Pause on Touch",
                "Hold stories effortlessly without triggering side-taps",
                Settings.STORY_AUTO_PAUSE,
                RexVectorIcons.IC_PAUSE_CIRCLE
        ));
        addPreference(navCat, helper.switchPreference(
                "Long Press Fast-Forward",
                "Long press on feed videos to fast-forward playback",
                Settings.LONG_PRESS_FAST_FORWARD,
                RexVectorIcons.IC_GAUGE
        ));
        addPreference(navCat, helper.switchPreference(
                "Swipe Navigation",
                "Smoothly swipe between main tabs and direct messages",
                Settings.SWIPE_NAVIGATION,
                RexVectorIcons.IC_ARROWS_H
        ));
        addPreference(navCat, helper.switchPreference(
                "Volume Key Scroll",
                "Use volume up/down buttons to scroll through posts and reels hands-free",
                Settings.VOLUME_KEY_SCROLL,
                RexVectorIcons.IC_VOLUME
        ));
        addPreference(navCat, helper.switchPreference(
                "Triple Tap to Download",
                "Quickly download any post, reel, or story by triple-tapping on the media",
                Settings.TRIPLE_TAP_DOWNLOAD,
                RexVectorIcons.IC_DOWNLOAD
        ));
        addPreference(navCat, helper.switchPreference(
                "Shake to Clear Cache",
                "Shake your device on the profile tab to quickly clear temporary media cache",
                Settings.SHAKE_TO_CLEAR_CACHE,
                RexVectorIcons.IC_SMARTPHONE
        ));
        addPreference(navCat, helper.switchPreference(
                "Double Tap Profile Picture to Copy Bio",
                "Double tap on a user's profile picture to instantly copy their bio text",
                Settings.DOUBLE_TAP_PROFILE_COPY_BIO,
                RexVectorIcons.IC_COPY
        ));
    }

    /**
     * Feed and Stories Screen (Screenshot 063138)
     */
    public void feedAndStoriesSection() {
        addPreference(helper.switchPreference(
                "Follow Back Indicator",
                "Shows on a user's profile whether they follow you back",
                Settings.FOLLOW_BACK_INDICATOR,
                RexVectorIcons.IC_USER_CHECK
        ));
        addPreference(helper.switchPreference(
                "Colored Follow Indicator",
                "Highlights the follow button with a color indicator based on follow status",
                Settings.FOLLOW_BACK_COLOR_INDICATOR,
                RexVectorIcons.IC_PALETTE
        ));
        addPreference(helper.switchPreference(
                "Non-Follower Badge in Lists",
                "Shows a badge next to users in follower/following lists who don't follow you back",
                Settings.FOLLOW_LIST_NON_FOLLOWER_BADGE,
                RexVectorIcons.IC_USERS
        ));
        addPreference(helper.switchPreference(
                "Disable Video Autoplay",
                "Prevents videos from playing automatically in the feed",
                Settings.DISABLE_VIDEO_AUTOPLAY,
                RexVectorIcons.IC_PAUSE_CIRCLE
        ));
        addPreference(helper.switchPreference(
                "Disable Story Auto-Flip",
                "Stops stories from automatically advancing to the next story",
                Settings.DISABLE_STORY_FLIPPING,
                RexVectorIcons.IC_SKIP_FORWARD
        ));
        addPreference(helper.switchPreference(
                "Force High Quality Images",
                "Loads images at maximum resolution (up to 4096px)",
                Settings.IMPROVE_IMAGE_VIEWING,
                RexVectorIcons.IC_HD
        ));
        addPreference(helper.switchPreference(
                "View Story Mentions",
                "Allows you to see all accounts mentioned in a story",
                Settings.VIEW_STORY_MENTIONS,
                RexVectorIcons.IC_AT_SIGN
        ));
        addPreference(helper.listPreference(
                "Story Timestamp Format",
                "Show exact upload time or date on stories",
                Settings.CUSTOMISE_STORY_TIMESTAMP,
                RexVectorIcons.IC_CLOCK
        ));
        addPreference(helper.switchPreference(
                "Loop Stories",
                "Continuously repeats the current story instead of moving on",
                Settings.LOOP_STORY,
                RexVectorIcons.IC_REPEAT
        ));
        addPreference(helper.editTextNumPreference(
                "Story Ring Scale",
                "Adjust the size of story rings (default 100)",
                Settings.CUSTOMISE_STORY_RING_SIZE,
                RexVectorIcons.IC_CIRCLE_DASHED
        ));
        addPreference(helper.switchPreference(
                "Story Force HD Quality",
                "Always stream and render stories at highest available bitrate",
                Settings.STORY_FORCE_HD,
                RexVectorIcons.IC_SPARKLES
        ));
        addPreference(helper.switchPreference(
                "Upload Media Max Quality",
                "Bypass compression when uploading photos, stories, and reels",
                Settings.UPLOAD_MAX_QUALITY,
                RexVectorIcons.IC_UPLOAD
        ));
        addPreference(helper.switchPreference(
                "Reel Ring Captions",
                "Keep captions readable without overlapping story rings",
                Settings.REEL_RING_CAPTIONS,
                RexVectorIcons.IC_SUBTITLES
        ));
        addPreference(helper.switchPreference(
                "Sound Button Overlay",
                "Show a floating mute/unmute toggle on feed and reel videos",
                Settings.SOUND_BUTTON_OVERLAY,
                RexVectorIcons.IC_VOLUME
        ));
        addPreference(helper.switchPreference(
                "Show Follower Count",
                "Display exact follower counts on profiles instead of rounded numbers",
                Settings.SHOW_FOLLOWER_COUNT,
                RexVectorIcons.IC_HASH
        ));
    }

    public void storySection() {
        feedAndStoriesSection();
    }

    /**
     * UI Tweaks Screen (Screenshot 063202)
     */
    public void uiTweaksSection() {
        if (MaterialYouTheme.isAmoledAvailable()) {
            addPreference(helper.switchPreference(
                    str("rex_amoled_title"),
                    "Pure pitch-black AMOLED dark theme",
                    Settings.AMOLED_THEME,
                    RexVectorIcons.IC_PALETTE
            ));
        }
        if (MaterialYouTheme.isMaterialYouAvailable()) {
            addPreference(helper.switchPreference(
                    str("rex_material_you_title"),
                    "Dynamic Material You monet accents",
                    Settings.MATERIAL_YOU_THEME,
                    RexVectorIcons.IC_SPARKLES
            ));
        }
        if (SettingsStatus.hideNavigationButtons) {
            addPreference(helper.buttonPreference(
                    "Navigation Bar Tabs",
                    "Hide, show, or reorder bottom navigation bar tabs",
                    Constants.REX_FRAGMENT_NAV_BTNS,
                    RexVectorIcons.IC_COMPASS
            ));
        }
        addPreference(helper.buttonPreference(
                "Action Bar Buttons",
                "Configure Ghost Mode and RexPrime Settings icons on top bars",
                Constants.REX_FRAGMENT_ACTION_BAR,
                RexVectorIcons.IC_LAYOUT_TOP
        ));
        if (SettingsStatus.fontSection()) {
            addPreference(helper.buttonPreference(
                    "Custom Font",
                    "Apply custom app typography and font files",
                    Constants.REX_FRAGMENT_FONT,
                    RexVectorIcons.IC_FILE_TEXT
            ));
        }
        if (SettingsStatus.removeEmptyBottomSpace) {
            addPreference(helper.switchPreference(
                    str("rex_remove_empty_bottom_space"),
                    "Removes extra bottom bar padding on gesture navigation",
                    Settings.REMOVE_EMPTY_BOTTOM_SPACE,
                    RexVectorIcons.IC_LAYOUT_TOP
            ));
        }
        addPreference(helper.switchPreference(
                "Hide Share Button",
                "Hides the share button on posts and reels",
                Settings.HIDE_SHARE_BUTTON,
                RexVectorIcons.IC_SHARE
        ));
        addPreference(helper.switchPreference(
                "Hide Save Button",
                "Hides the bookmark/save button on posts",
                Settings.HIDE_SAVE_BUTTONS,
                RexVectorIcons.IC_BOOKMARK
        ));
        addPreference(helper.switchPreference(
                "Hide Create Button",
                "Hides the '+' create button on the home screen",
                Settings.HIDE_HOME_CREATE_BUTTON,
                RexVectorIcons.IC_PLUS_SQUARE
        ));
        addPreference(helper.switchPreference(
                "Hide Notifications Button",
                "Hides the heart/activity button on the home screen",
                Settings.HIDE_HOME_NOTIFICATIONS_BUTTON,
                RexVectorIcons.IC_HEART_OFF
        ));
        addPreference(helper.forcedSwitchPreference(
                "Hide Follow Button on Reels",
                "Hides the follow button next to usernames in Reels",
                Settings.HIDE_REELS_FOLLOW_BUTTON,
                FocusLock.isForced(Settings.HIDE_REELS_FOLLOW_BUTTON),
                str("rex_focus_lock_enforced"),
                RexVectorIcons.IC_USER_MINUS
        ));
        addPreference(helper.forcedSwitchPreference(
                "Hide Group Creation on Share Sheet",
                "Removes the 'Create group' button when sharing posts",
                Settings.HIDE_GROUP_CREATION_BUTTON_ON_SHARESHEET,
                FocusLock.isForced(Settings.HIDE_GROUP_CREATION_BUTTON_ON_SHARESHEET),
                str("rex_focus_lock_enforced"),
                RexVectorIcons.IC_USERS
        ));
        addPreference(helper.switchPreference(
                "Copy Comment Button",
                "Adds a button to easily copy comment text",
                Settings.COMMENT_COPY_BUTTON,
                RexVectorIcons.IC_COPY
        ));
        addPreference(helper.switchPreference(
                "Save Comment Media Button",
                "Adds a download button to comments containing GIFs or images",
                Settings.COMMENT_SAVE_MEDIA_BUTTON,
                RexVectorIcons.IC_IMAGE
        ));
        addPreference(helper.listPreference(
                "Like Animation Style",
                "Customize the double-tap heart animation",
                Settings.CHANGE_LIKE_ANIMATION,
                RexVectorIcons.IC_SPARKLES
        ));
        addPreference(helper.switchPreference(
                "Hide Reshare Button",
                "Hides the reshare/repost button on posts and reels",
                Settings.HIDE_RESHARE_BUTTON,
                RexVectorIcons.IC_REPEAT
        ));
        addPreference(helper.switchPreference(
                "Extended Post Options",
                "Adds extra options (Download, Copy Caption, etc.) to the post 3-dot menu",
                Settings.ENABLE_MORE_OPTIONS_ON_POST,
                RexVectorIcons.IC_MORE_H
        ));
        addPreference(helper.switchPreference(
                "Hide DM Translation Prompt",
                "Hides the 'See translation' banner inside direct message threads",
                Settings.HIDE_DM_TRANSLATION,
                RexVectorIcons.IC_TRANSLATE
        ));
        addPreference(helper.switchPreference(
                "Reels Direct Restyle",
                "Clean layout for Reels shared inside direct messages",
                Settings.REELS_DIRECT_RESTYLE,
                RexVectorIcons.IC_FILM
        ));
    }

    /**
     * Focus Mode / Distraction Free Screen (Screenshot 063151)
     */
    public void distractionFreeSection() {
        if (SettingsStatus.focusLock) {
            addPreference(helper.buttonPreference(
                    str("rex_focus_lock"),
                    FocusLock.statusSummary(),
                    Constants.REX_FRAGMENT_FOCUS_LOCK,
                    RexVectorIcons.IC_LOCK
            ));
        }
        if (SettingsStatus.filterContentSection()) {
            addPreference(helper.buttonPreference(
                    str("rex_category_filter_content"),
                    "Filter stories by type, user type, and item count",
                    Constants.REX_FRAGMENT_FILTER_CONTENT,
                    RexVectorIcons.IC_FILTER
            ));
        }

        addLockableSwitch(
                "Disable Stories",
                "Completely hides stories from the app",
                Settings.DISABLE_STORIES,
                RexVectorIcons.IC_CIRCLE_OFF
        );
        addLockableSwitch(
                "Hide Stories Tray",
                "Hides the horizontal stories bar at the top of the home feed",
                Settings.HIDE_STORIES_TRAY,
                RexVectorIcons.IC_LAYOUT_TOP
        );
        addLockableSwitch(
                "Hide Notes Tray",
                "Hides the notes bubble bar in the direct messages inbox",
                Settings.HIDE_NOTES_TRAY,
                RexVectorIcons.IC_MESSAGE_OFF
        );
        addLockableSwitch(
                "Disable Explore",
                "Hides all content on the Explore/Search page",
                Settings.DISABLE_EXPLORE,
                RexVectorIcons.IC_COMPASS
        );
        addLockableSwitch(
                "Disable Comments",
                "Hides comments on all posts and reels",
                Settings.DISABLE_COMMENTS,
                RexVectorIcons.IC_MESSAGE_CIRCLE
        );
        addLockableSwitch(
                "Disable Highlights",
                "Hides story highlights on user profiles",
                Settings.DISABLE_HIGHLIGHTS,
                RexVectorIcons.IC_STAR_OFF
        );
        addLockableSwitch(
                "Limit Following Feed",
                "Shows only posts from accounts you follow",
                Settings.LIMIT_FOLLOWING_FEED,
                RexVectorIcons.IC_FILTER
        );
        addPreference(helper.switchPreference(
                "Hide Discover People",
                "Hides 'Suggested for you' account carousels",
                Settings.DISABLE_DISCOVER_PEOPLE,
                RexVectorIcons.IC_USER_X
        ));
        addLockableSwitch(
                "Disable Reels Scrolling",
                "Prevents infinite scrolling in the Reels tab",
                Settings.DISABLE_REELS_SCROLLING,
                RexVectorIcons.IC_PLAY_OFF
        );
        addLockableSwitch(
                "Disable Swipe to Camera",
                "Prevents swiping right on the home feed to open the camera",
                Settings.DISABLE_SWIPE_TO_CREATE,
                RexVectorIcons.IC_SWIPE_LEFT
        );
    }

    private void addLockableSwitch(String title, String summary, BooleanSetting setting, String svgIconPath) {
        addPreference(helper.forcedSwitchPreference(
                title,
                summary,
                setting,
                FocusLock.isForced(setting),
                str("rex_focus_lock_enforced"),
                svgIconPath
        ));
    }

    public void buildFocusLockSection() {
        if (!SettingsStatus.focusLock) return;

        boolean locked = FocusLock.isActive();

        addPreference(helper.buttonPreference(
                FocusLock.buttonTitle(),
                FocusLock.statusSummary(),
                "rex_focus_lock_action",
                RexVectorIcons.IC_LOCK
        ));

        FocusLockDurationPreference duration = new FocusLockDurationPreference(context);
        duration.setTitle(str("rex_focus_lock_duration"));
        duration.setEnabled(!locked);
        addPreference(duration);

        PreferenceCategory targets = addCategory(str("rex_focus_lock_targets"));
        for (FocusLockTargets.Target target : FocusLockTargets.available()) {
            Preference pref = helper.switchPreference(
                    str(target.titleKey),
                    target.summaryKey == null ? "" : str(target.summaryKey),
                    Pref.focusLockSelection(target.key),
                    RexVectorIcons.IC_SPARKLES
            );
            if (locked && pref instanceof SwitchPref) {
                ((SwitchPref) pref).setSwitchInteractionEnabled(false);
            }
            addPreference(targets, pref);
        }
    }

    /**
     * Miscellaneous Screen (Screenshot 063147)
     */
    public void buildMiscSection() {
        PreferenceCategory savedCat = addCategory("Saved Data & History");

        int biosCount = RexDialogs.getSavedItems(context, RexDialogs.KEY_SAVED_BIOS).size();
        addPreference(savedCat, helper.actionButtonPreference(
                "Saved Bios",
                "View and copy previously saved user bios (" + biosCount + ")",
                RexVectorIcons.IC_FILE_TEXT,
                () -> RexDialogs.showSavedListDialog(context, "Saved Bios", RexDialogs.KEY_SAVED_BIOS)
        ));

        int captionsCount = RexDialogs.getSavedItems(context, RexDialogs.KEY_SAVED_CAPTIONS).size();
        addPreference(savedCat, helper.actionButtonPreference(
                "Saved Captions",
                "View and manage saved post and reel captions (" + captionsCount + ")",
                RexVectorIcons.IC_BOOKMARK,
                () -> RexDialogs.showSavedListDialog(context, "Saved Captions", RexDialogs.KEY_SAVED_CAPTIONS)
        ));

        int profilesCount = RexDialogs.getSavedItems(context, RexDialogs.KEY_SAVED_PROFILES).size();
        addPreference(savedCat, helper.actionButtonPreference(
                "Saved Profiles",
                "Quickly access bookmarked user profiles (" + profilesCount + ")",
                RexVectorIcons.IC_USER_CHECK,
                () -> RexDialogs.showSavedListDialog(context, "Saved Profiles", RexDialogs.KEY_SAVED_PROFILES)
        ));

        int watchCount = RexDialogs.getSavedItems(context, RexDialogs.KEY_WATCH_HISTORY).size();
        addPreference(savedCat, helper.actionButtonPreference(
                "Watch History",
                "Browse your recently watched reels and posts (" + watchCount + ")",
                RexVectorIcons.IC_HISTORY,
                () -> RexDialogs.showSavedListDialog(context, "Watch History", RexDialogs.KEY_WATCH_HISTORY)
        ));

        PreferenceCategory toolsCat = addCategory("Translation & Media Tools");

        addPreference(toolsCat, helper.switchPreference(
                "Message Translator",
                "Enable instant one-tap translation for incoming and outgoing direct messages",
                Settings.MESSAGE_TRANSLATOR,
                RexVectorIcons.IC_TRANSLATE
        ));

        String currentLang = SharedPref.getStringPref(Settings.TRANSLATOR_LANGUAGE);
        ButtonPref langBtn = helper.actionButtonPreference(
                "Translator Language",
                "Target language: " + currentLang,
                RexVectorIcons.IC_GLOBE,
                null
        );
        langBtn.setCustomClickAction(() -> RexDialogs.showTranslatorLanguagePicker(context, selected -> langBtn.setSummary("Target language: " + selected)));
        addPreference(toolsCat, langBtn);

        addPreference(toolsCat, helper.switchPreference(
                "Voice Message Speed Control",
                "Allow 1.5x and 2x playback speed on direct message voice notes",
                Settings.VOICE_MESSAGE_SPEED_CONTROL,
                RexVectorIcons.IC_GAUGE
        ));

        addPreference(toolsCat, helper.actionButtonPreference(
                "Split Video for Stories",
                "Split long videos into story-ready clips",
                RexVectorIcons.IC_SCISSORS,
                () -> RexUtils.toast("Select a video from gallery to split into " + SharedPref.getStringPref(Settings.SPLIT_VIDEO_DURATION) + " clips")
        ));

        String currentSplit = SharedPref.getStringPref(Settings.SPLIT_VIDEO_DURATION);
        ButtonPref splitBtn = helper.actionButtonPreference(
                "Split Video Duration",
                "Clip length: " + currentSplit,
                RexVectorIcons.IC_CLOCK,
                null
        );
        splitBtn.setCustomClickAction(() -> RexDialogs.showSplitDurationPicker(context, selected -> splitBtn.setSummary("Clip length: " + selected)));
        addPreference(toolsCat, splitBtn);

        addPreference(toolsCat, helper.actionButtonPreference(
                "WhatsApp Share Integration",
                "Share media and links directly to your preferred WhatsApp client",
                RexVectorIcons.IC_SHARE,
                () -> RexUtils.toast("Direct WhatsApp share active for " + SharedPref.getStringPref(Settings.WHATSAPP_TYPE))
        ));

        String currentWa = SharedPref.getStringPref(Settings.WHATSAPP_TYPE);
        ButtonPref waBtn = helper.actionButtonPreference(
                "WhatsApp Type",
                "Selected: " + currentWa,
                RexVectorIcons.IC_MESSAGE_CIRCLE,
                null
        );
        waBtn.setCustomClickAction(() -> RexDialogs.showWhatsAppTypePicker(context, selected -> waBtn.setSummary("Selected: " + selected)));
        addPreference(toolsCat, waBtn);

        addPreference(toolsCat, helper.switchPreference(
                "Screenshot Blocker",
                "Prevent accidental screenshots inside sensitive chats when enabled",
                Settings.SCREENSHOT_BLOCKER,
                RexVectorIcons.IC_CAMERA_OFF
        ));
    }

    public void buildFontSection() {
        if (!(SettingsStatus.fontSection())) return;

        SwitchPref useSystemFont = new SwitchPref(context);
        useSystemFont.setTitle(str("rex_use_system_font"));
        useSystemFont.setSummary(str("rex_use_system_font_desc"));
        useSystemFont.setKey(Settings.USE_SYSTEM_FONT.key);
        useSystemFont.setPersistent(false);
        useSystemFont.setChecked(FontStorage.useSystemFont());
        InstagramPreferenceStyle.setLeftIcon(useSystemFont, RexVectorIcons.IC_FILE_TEXT);
        useSystemFont.setOnPreferenceChangeListener((preference, newValue) -> {
            FontStorage.setUseSystemFont(Boolean.TRUE.equals(newValue));
            Utils.showToastShort(str("rex_restart_app"));
            return true;
        });
        addPreference(useSystemFont);

        addPreference(helper.buttonPreference(
                str("rex_pref_add_font"),
                str("rex_pref_add_font_desc"),
                "rex_pref_add_font",
                RexVectorIcons.IC_UPLOAD
        ));

        addPreference(helper.buttonPreference(
                str("rex_pref_delete_font"),
                "",
                "rex_pref_delete_font",
                RexVectorIcons.IC_TRASH
        ));
    }

    public void buildInstantsSection() {
        if (SettingsStatus.instantsDownload) {
            addPreference(helper.switchPreference(
                    str("rex_instants_download"),
                    str("rex_instants_download_desc"),
                    Settings.INSTANTS_DOWNLOAD,
                    RexVectorIcons.IC_DOWNLOAD
            ));
            addPreference(helper.buttonPreference(
                    str("rex_view_saved_instants"),
                    "",
                    "rex_view_saved_instants",
                    RexVectorIcons.IC_EYE
            ));
        }
    }

    /**
     * Downloads Screen (Screenshot 063132)
     */
    public void buildDownloadSection() {
        addPreference(helper.switchPreference(
                "Enable Media Download",
                "Adds download buttons to posts, reels, and stories",
                Settings.ENABLE_DOWNLOAD,
                RexVectorIcons.IC_DOWNLOAD
        ));
        if (SettingsStatus.feedDownloadButton) {
            addPreference(helper.switchPreference(
                    str("rex_feed_download_button"),
                    str("rex_feed_download_button_desc"),
                    Settings.FEED_DOWNLOAD_BUTTON,
                    RexVectorIcons.IC_DOWNLOAD
            ));
        }
        addPreference(helper.switchPreference(
                "Direct Download",
                "Starts downloading immediately without showing a quality/item selector",
                Settings.ENABLE_DIRECT_DOWNLOAD,
                RexVectorIcons.IC_BOLT
        ));
        addPreference(helper.switchPreference(
                "Split by Username",
                "Saves downloaded media into separate subfolders named after the creator's username",
                Settings.DOWNLOAD_USERNAME_FOLDER,
                RexVectorIcons.IC_FOLDER_USER
        ));
        addPreference(helper.switchPreference(
                "Embed Download Metadata",
                "Embeds post caption, creator, and date metadata into downloaded files",
                Settings.EMBED_DOWNLOAD_METADATA,
                RexVectorIcons.IC_TAG
        ));
        Preference tplPref = helper.downloadFileNameTemplatePreference(
                "Download Filename Format",
                Pref.downloadFileNameTemplate(),
                Settings.DOWNLOAD_FILE_NAME_TEMPLATE
        );
        InstagramPreferenceStyle.setLeftIcon(tplPref, RexVectorIcons.IC_EDIT);
        addPreference(tplPref);

        addPreference(helper.buttonPreference(
                "Download Folder",
                StorageUtils.getCustomPathForDisplay(),
                "rex_download_set_path",
                RexVectorIcons.IC_FOLDER
        ));

        if (SettingsStatus.downloadWithExternalDownloader) {
            addPreference(helper.switchPreference(
                    "Use External Downloader",
                    "Forwards media URLs to an external downloader app (like Seal or YTDLnis)",
                    Settings.DOWNLOAD_WITH_EXTERNAL_DOWNLOADER,
                    RexVectorIcons.IC_EXTERNAL_LINK
            ));
            addPreference(helper.editTextPreference(
                    "External Downloader Package",
                    Pref.externalDownloaderPackageName(),
                    Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME,
                    RexVectorIcons.IC_PACKAGE
            ));
        }
    }

    public void buildActionBarSection() {
        Preference p1 = helper.multiSelectListPref(
                str("rex_action_bar_main_feed"),
                "",
                Settings.ACTION_BAR_MAIN_FEED
        );
        InstagramPreferenceStyle.setLeftIcon(p1, RexVectorIcons.IC_LAYOUT_TOP);
        addPreference(p1);

        Preference p2 = helper.multiSelectListPref(
                str("rex_action_bar_user_profile"),
                "",
                Settings.ACTION_BAR_USER_PROFILE
        );
        InstagramPreferenceStyle.setLeftIcon(p2, RexVectorIcons.IC_USER);
        addPreference(p2);

        Preference p3 = helper.multiSelectListPref(
                str("rex_action_bar_inbox"),
                "",
                Settings.ACTION_BAR_INBOX
        );
        InstagramPreferenceStyle.setLeftIcon(p3, RexVectorIcons.IC_SEND);
        addPreference(p3);

        Preference p4 = helper.multiSelectListPref(
                str("rex_action_bar_chat"),
                str("rex_action_bar_chat_desc"),
                Settings.ACTION_BAR_CHAT
        );
        InstagramPreferenceStyle.setLeftIcon(p4, RexVectorIcons.IC_MESSAGE_CIRCLE);
        addPreference(p4);
    }

    public void filterContentSection() {
        if (!SettingsStatus.filterContentSection()) return;

        if (SettingsStatus.storyFilters) {
            PreferenceCategory category = addCategory(str("rex_filter_story"));

            Preference p1 = helper.multiSelectListPref(
                    str("rex_filter_story_types_to_hide"),
                    "",
                    Settings.FILTER_STORY_BY_TYPE
            );
            InstagramPreferenceStyle.setLeftIcon(p1, RexVectorIcons.IC_FILTER);
            addPreference(category, p1);

            Preference p2 = helper.multiSelectListPref(
                    str("rex_filter_story_user_types_to_hide"),
                    "",
                    Settings.FILTER_STORY_BY_USER_TYPE
            );
            InstagramPreferenceStyle.setLeftIcon(p2, RexVectorIcons.IC_USERS);
            addPreference(category, p2);

            addPreference(category, helper.editTextNumPreference(
                    str("rex_filter_story_minimum_item_count"),
                    str("rex_filter_story_minimum_item_count_desc"),
                    Settings.FILTER_STORY_MIN_STORY_ITEMS,
                    RexVectorIcons.IC_HASH
            ));

            addPreference(category, helper.editTextNumPreference(
                    str("rex_filter_story_maximum_item_count"),
                    str("rex_filter_story_maximum_item_count_desc"),
                    Settings.FILTER_STORY_MAX_STORY_ITEMS,
                    RexVectorIcons.IC_HASH
            ));
        }
    }

    public void buildNavigationSection() {
        if (!(SettingsStatus.hideNavigationButtons)) return;

        Preference navPref = helper.navigationBarPreference(
                str("rex_navigation_tabs_title"),
                str("rex_navigation_tabs_summary")
        );
        InstagramPreferenceStyle.setLeftIcon(navPref, RexVectorIcons.IC_COMPASS);
        addPreference(navPref);

        Preference startPref = helper.navigationStartupPreference(
                str("rex_navigation_startup_tab"),
                str("rex_navigation_startup_tab_summary")
        );
        InstagramPreferenceStyle.setLeftIcon(startPref, RexVectorIcons.IC_STAR);
        addPreference(startPref);
    }

    /**
     * Developer Options Screen
     */
    public void buildDeveloperSection() {
        if (SettingsStatus.recommendedFlags) {
            addPreference(helper.buttonPreference(
                    str("rex_category_rec_flags"),
                    str("rex_category_rec_flags_desc"),
                    Constants.REX_FRAGMENT_REC_FLAGS,
                    RexVectorIcons.IC_STAR
            ));
        }
        if (SettingsStatus.unlockEmployeeOptions) {
            addPreference(helper.switchPreference(
                    str("rex_enable_emp_options"),
                    str("rex_enable_emp_options_desc"),
                    Settings.ENABLE_EMP_OPTIONS,
                    RexVectorIcons.IC_SPARKLES
            ));
        }
        if (SettingsStatus.allowUserNetworkCertificate) {
            addPreference(helper.switchPreference(
                    str("rex_allow_user_network_certificate"),
                    str("rex_allow_user_network_certificate_desc"),
                    Settings.ALLOW_USER_NETWORK_CERTIFICATE,
                    RexVectorIcons.IC_SHIELD_OUTLINE
            ));
        }
        if (SettingsStatus.enableDeveloperOptions) {
            addPreference(helper.switchPreference(
                    str("rex_enable_dev_options"),
                    str("rex_enable_dev_options_desc"),
                    Settings.DEVELOPER_OPTIONS,
                    RexVectorIcons.IC_WRENCH
            ));
            addPreference(helper.switchPreference(
                    str("rex_directly_open_metaconfig"),
                    str("rex_directly_open_metaconfig_desc"),
                    Settings.DIRECTLY_OPEN_METACONFIG,
                    RexVectorIcons.IC_SLIDERS
            ));
            addPreference(helper.buttonPreference(
                    str("rex_export_dev_overrides"),
                    "",
                    "rex_export_dev_overrides",
                    RexVectorIcons.IC_UPLOAD
            ));
            addPreference(helper.buttonPreference(
                    str("rex_import_dev_overrides"),
                    "",
                    "rex_import_dev_overrides",
                    RexVectorIcons.IC_DOWNLOAD
            ));
            addPreference(helper.buttonPreference(
                    str("rex_import_id_mapping"),
                    "",
                    "rex_import_id_mapping",
                    RexVectorIcons.IC_FOLDER
            ));
            addPreference(helper.buttonPreference(
                    str("rex_download_id_mapping"),
                    "",
                    "rex_download_id_mapping",
                    RexVectorIcons.IC_GLOBE
            ));

            if (Pref.rexDebug()) {
                addPreference(helper.buttonPreference(
                        str("rex_export_experiment_list"),
                        "",
                        "rex_export_experiment_list",
                        RexVectorIcons.IC_FILE_TEXT
                ));
                addPreference(helper.buttonPreference(
                        str("rex_export_experiment_mappings"),
                        "",
                        "rex_export_experiment_mappings",
                        RexVectorIcons.IC_FILE_TEXT
                ));
            }
        }
    }

    public void buildRecommendedFlagsSection() {
        long lastModified = RecommendedFlags.getLastModified();
        String lastModifiedAtString = "";
        if (lastModified > 0L) {
            Locale locale = context.getResources().getConfiguration().getLocales().get(0);
            String skeleton = DateFormat.is24HourFormat(context) ? "yMMMEdHm" : "yMMMEdhm";
            String pattern = DateFormat.getBestDateTimePattern(locale, skeleton);
            String formattedDate = new SimpleDateFormat(pattern, locale).format(new Date(lastModified));
            lastModifiedAtString = String.format(str("rex_rec_flags_last_modified_at"), formattedDate);
        }
        addPreference(helper.buttonPreference(
                str("rex_rec_flags_refresh_file"),
                lastModifiedAtString,
                "rex_rec_flags_refresh_file",
                RexVectorIcons.IC_REFRESH
        ));
        List<Flag> recFlags = RecommendedFlags.getFlags();
        for (Flag flag : recFlags) {
            if (flag.isLongType()) {
                addPreference(helper.editTextNumPreference(
                        flag.getName(),
                        flag.getDesc(),
                        flag.getCode(),
                        RexVectorIcons.IC_HASH
                ));
            } else {
                addPreference(helper.listPreference(
                        flag.getName(),
                        flag.getDesc(),
                        flag.getCode(),
                        RexVectorIcons.IC_SLIDERS
                ));
            }
        }
    }

    /**
     * About Screen (Screenshot 063243)
     */
    public void aboutSection(TreeMap<String, Boolean> flags) {
        PreferenceCategory creditsCat = addCategory("Credits");
        addPreference(creditsCat, helper.linkButtonPreference(
                "Mayank",
                "Lead Developer • @xboyrex",
                RexVectorIcons.IC_USER,
                RexVectorIcons.IC_LINK_CHAIN,
                () -> RexUtils.openUrl(Constants.AUTHOR_URL, true)
        ));
        addPreference(creditsCat, helper.actionButtonPreference(
                "Support the Developer ❤️",
                "Help keep RexPrime free and updated!",
                RexVectorIcons.IC_HEART,
                () -> RexDialogs.showSupportDialog(context)
        ));

        PreferenceCategory socialCat = addCategory("Social Media");
        addPreference(socialCat, helper.linkButtonPreference(
                "Telegram",
                "Join @MayankDroid7 for updates & announcements",
                RexVectorIcons.IC_SEND,
                RexVectorIcons.IC_LINK_CHAIN,
                () -> RexUtils.openUrl(Constants.TELEGRAM_URL, true)
        ));
        addPreference(socialCat, helper.linkButtonPreference(
                "Discussion Group",
                "Join @OnyxHub7 for support & community chat",
                RexVectorIcons.IC_USERS,
                RexVectorIcons.IC_LINK_CHAIN,
                () -> RexUtils.openUrl(Constants.TELEGRAM_GROUP_URL, true)
        ));
        addPreference(socialCat, helper.linkButtonPreference(
                "Instagram",
                "Follow @mayank.zx7 on Instagram",
                RexVectorIcons.IC_CAMERA,
                RexVectorIcons.IC_LINK_CHAIN,
                () -> RexUtils.openUrl(Constants.INSTAGRAM_URL, true)
        ));
        addPreference(socialCat, helper.linkButtonPreference(
                "YouTube",
                "Subscribe to @mayankdroid & @onyxdroid89",
                RexVectorIcons.IC_PLAY_CIRCLE,
                RexVectorIcons.IC_LINK_CHAIN,
                () -> RexUtils.openUrl(Constants.YOUTUBE_URL, true)
        ));
        addPreference(socialCat, helper.linkButtonPreference(
                "GitHub",
                "github.com/xboyrex/rex-ig-lite",
                RexVectorIcons.IC_GLOBE,
                RexVectorIcons.IC_LINK_CHAIN,
                () -> RexUtils.openUrl(Constants.REPO_URL, true)
        ));

        PreferenceCategory backupCat = addCategory("Export / Import");
        addPreference(backupCat, helper.buttonPreference(
                str("rex_export_pref"),
                "Export your RexPrime settings to an XML backup file",
                "rex_export_pref",
                RexVectorIcons.IC_UPLOAD
        ));
        addPreference(backupCat, helper.buttonPreference(
                str("rex_import_pref"),
                "Restore RexPrime settings from a backup file",
                "rex_import_pref",
                RexVectorIcons.IC_DOWNLOAD
        ));
        addPreference(backupCat, helper.buttonPreference(
                str("rex_reset_pref"),
                "Reset all RexPrime preferences to their default values",
                "rex_reset_pref",
                RexVectorIcons.IC_REFRESH
        ));
        addPreference(backupCat, helper.switchPreference(
                str("rex_debug"),
                str("rex_debug_desc"),
                Settings.REX_DEBUG,
                RexVectorIcons.IC_WRENCH
        ));

        PreferenceCategory appInfoCat = addCategory("App Info");
        String appVersionText = String.format(str("rex_app_version"), Utils.getAppVersionName());
        String patchVersionText = String.format(str("rex_patch_version"), Utils.getPatchesReleaseVersion());

        addPreference(appInfoCat, helper.buttonPreference(
                appVersionText,
                "Instagram Target APK",
                appVersionText,
                RexVectorIcons.IC_SMARTPHONE
        ));
        addPreference(appInfoCat, helper.actionButtonPreference(
                patchVersionText,
                "RexPrime Edition by Mayank (@xboyrex) • Tap for Changelogs",
                RexVectorIcons.IC_SPARKLES,
                () -> RexDialogs.showChangelogsDialog(context)
        ));

        PreferenceCategory patchCat = addCategory(str("rex_patch_info_title"));
        String enabledStr = str("rex_patch_enabled");
        String disabledStr = str("rex_patch_disabled");

        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            String resName = entry.getKey();
            boolean sts = Boolean.TRUE.equals(entry.getValue());
            String status = sts ? enabledStr : disabledStr;

            addPreference(patchCat, helper.buttonPreference(
                    resName,
                    status,
                    resName,
                    sts ? RexVectorIcons.IC_CHECK_CIRCLE : RexVectorIcons.IC_CIRCLE_OFF
            ));
        }
    }
}
