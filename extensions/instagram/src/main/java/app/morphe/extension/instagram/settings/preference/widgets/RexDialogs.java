/*
 * Copyright (C) 2026 xboyrex <https://github.com/xboyrex/rex-ig-lite>
 * GNU General Public License v3.0
 */

package app.morphe.extension.instagram.settings.preference.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.instagram.common.session.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import app.morphe.extension.instagram.constants.Constants;
import app.morphe.extension.instagram.db.RexMessageDb;
import app.morphe.extension.instagram.entity.MediaData;
import app.morphe.extension.instagram.entity.UserData;
import app.morphe.extension.instagram.patches.dm.SavedMessagesHook;
import app.morphe.extension.instagram.patches.download.DownloadUtils;
import app.morphe.extension.instagram.patches.userprofile.ProfilePictureViewer;
import app.morphe.extension.instagram.settings.ActivityHook;
import app.morphe.extension.instagram.settings.Settings;
import app.morphe.extension.instagram.settings.preference.fragments.FragmentHook;
import app.morphe.extension.instagram.theme.MaterialYouTheme;
import app.morphe.extension.instagram.utils.Pref;
import app.morphe.extension.rex.RexUtils;
import app.morphe.extension.rex.downloader.MediaType;
import app.morphe.extension.rex.sharedPreference.SharedPref;
import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.Utils;

public final class RexDialogs {

    private RexDialogs() {}

    public static void showNotificationCenterDialog(final Context context) {
        final String[] options = {"Filter by Notification Type", "Filter by Keywords"};
        new AlertDialog.Builder(context)
                .setTitle("Notification Center")
                .setItems(options, (dialog, which) -> {
                    RexHaptics.tap(context);
                    if (which == 0) RexRuntimeFeatures.showNotificationTypeFilterDialog(context);
                    else if (which == 1) RexRuntimeFeatures.showNotificationKeywordFilterDialog(context);
                })
                .show();
    }

    public static void showAppLanguagePicker(final Context context) {
        final String[] options = {"Instagram App Language", "Translator Target Language"};
        new AlertDialog.Builder(context)
                .setTitle("App Language & Translator")
                .setItems(options, (dialog, which) -> {
                    RexHaptics.tap(context);
                    if (which == 1) {
                        RexRuntimeFeatures.showTranslatorLanguagePicker(context, null);
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APP_LOCALE_SETTINGS);
                            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (Throwable error) {
                            Utils.showToastShort("Unable to open Android app language settings: " + error.getMessage());
                        }
                    } else {
                        new AlertDialog.Builder(context)
                                .setTitle("Android Version Required")
                                .setMessage("Per-app language settings are available on Android 13 and newer. You can still change the in-app translator language from this menu.")
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                })
                .show();
    }

    public static Activity resolveActivity(Context context) {
        Context c = context;
        while (c instanceof ContextWrapper) {
            if (c instanceof Activity) return (Activity) c;
            Context base = ((ContextWrapper) c).getBaseContext();
            if (base == c) break;
            c = base;
        }
        return Utils.getActivity();
    }

    /**
     * Attaches real-time Gaussian background blur (RenderEffect on underlying Activity decorView +
     * WindowManager FLAG_BLUR_BEHIND) and cleans it up automatically when the dialog dismisses.
     */
    public static void attachBlurBehind(final Dialog dialog, Context context) {
        if (dialog == null) return;
        final Activity host = resolveActivity(context);
        try {
            Window w = dialog.getWindow();
            if (w != null) {
                w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams lp = w.getAttributes();
                lp.dimAmount = 0.52f;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                    lp.setBlurBehindRadius(42);
                }
                w.setAttributes(lp);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && host != null && host.getWindow() != null) {
                final View decor = host.getWindow().getDecorView();
                if (decor != null) {
                    decor.setRenderEffect(RenderEffect.createBlurEffect(26f, 26f, Shader.TileMode.CLAMP));
                    dialog.setOnDismissListener(d -> {
                        try {
                            decor.setRenderEffect(null);
                        } catch (Throwable ignored) {}
                    });
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void showBlurredDialog(Context context, Dialog dialog) {
        if (dialog == null) return;
        attachBlurBehind(dialog, context);
        dialog.show();
    }

    // -------------------------------------------------------------------------
    // 1. Quick Access Menu (Matching Reference Screen 1: SETTINGS & FEATURES)
    // -------------------------------------------------------------------------
    public static void showHomeQuickMenu(final Context context) {
        try {
            final Context hostCtx = resolveActivity(context) != null ? resolveActivity(context) : context;
            final AlertDialog dialog = new AlertDialog.Builder(hostCtx).create();

            LinearLayout sheet = new LinearLayout(hostCtx);
            sheet.setOrientation(LinearLayout.VERTICAL);
            int pad = RexVectorIcons.dp(hostCtx, 18);
            sheet.setPadding(pad, RexVectorIcons.dp(hostCtx, 20), pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xF2181A1E);
            bg.setCornerRadius(RexVectorIcons.dp(hostCtx, 26));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(hostCtx, 1)), 0xFF2E333A);
            sheet.setBackground(bg);

            TextView header = new TextView(hostCtx);
            header.setText("Quick Access");
            header.setTextColor(Color.WHITE);
            header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            header.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            header.setGravity(Gravity.CENTER);
            header.setPadding(0, 0, 0, RexVectorIcons.dp(hostCtx, 14));
            sheet.addView(header);

            boolean ghostOn = Pref.getTurnOnAllGhostModes();
            boolean darkOn = Boolean.TRUE.equals(SharedPref.getBooleanPref(Settings.AMOLED_THEME)) || MaterialYouTheme.isAmoledEnabled();
            boolean dndOn = Boolean.TRUE.equals(SharedPref.getBooleanPref(Settings.DND_MODE_ENABLED));
            int deletedCount = getDeletedMessagesCount(hostCtx);

            // Section 1: SETTINGS
            addQuickAccessSectionHeader(hostCtx, sheet, "SETTINGS");
            LinearLayout settingsCard = createGroupedMenuCard(hostCtx);
            addGroupedMenuRow(hostCtx, settingsCard, "gear", "Rex IG Lite Settings", true, () -> {
                dialog.dismiss();
                FragmentHook.startSettings();
            });
            addGroupedMenuRow(hostCtx, settingsCard, "code_slash", "Developer Options", true, () -> {
                dialog.dismiss();
                showDevWarningDialog(hostCtx, () -> FragmentHook.startFragment(Constants.REX_FRAGMENT_DEV_OPTIONS));
            });
            addGroupedMenuRow(hostCtx, settingsCard, "refresh", "Restart App", false, () -> {
                dialog.dismiss();
                Utils.restartApp(hostCtx);
            });
            sheet.addView(settingsCard);

            // Section 2: FEATURES
            addQuickAccessSectionHeader(hostCtx, sheet, "FEATURES");
            LinearLayout featuresCard = createGroupedMenuCard(hostCtx);
            addGroupedMenuRow(hostCtx, featuresCard, "ghost", "Ghost Mode: (" + (ghostOn ? "Enabled" : "Disabled") + ")", true, () -> {
                boolean next = !Pref.getTurnOnAllGhostModes();
                Pref.setTurnOnAllGhostModes(next);
                Utils.showToastShort("Ghost Mode: " + (next ? "Enabled" : "Disabled"));
                dialog.dismiss();
            });
            addGroupedMenuRow(hostCtx, featuresCard, "bell_off", "DND Mode: (" + (dndOn ? "Enabled" : "Disabled") + ")", true, () -> {
                boolean next = !Boolean.TRUE.equals(SharedPref.getBooleanPref(Settings.DND_MODE_ENABLED));
                SharedPref.setBooleanPref(Settings.DND_MODE_ENABLED.key, next);
                Utils.showToastShort("DND Mode: " + (next ? "Enabled" : "Disabled"));
                dialog.dismiss();
            });
            addGroupedMenuRow(hostCtx, featuresCard, "history", "Activity & Watch History", true, () -> {
                dialog.dismiss();
                RexVault.showWatchHistoryDialog(hostCtx);
            });
            addGroupedMenuRow(hostCtx, featuresCard, "saved_copies", "Saved Copies (Bios, Captions & Profiles)", true, () -> {
                dialog.dismiss();
                showSavedCopiesDialog(hostCtx);
            });
            addGroupedMenuRow(hostCtx, featuresCard, "trash", "Deleted Messages (" + deletedCount + ")", true, () -> {
                dialog.dismiss();
                SavedMessagesHook.openDeletedMessages(hostCtx);
            });
            addGroupedMenuRow(hostCtx, featuresCard, "scissors", "Long Video Splitter", true, () -> {
                dialog.dismiss();
                RexRuntimeFeatures.showVideoSplitterDialog(hostCtx);
            });
            addGroupedMenuRow(hostCtx, featuresCard, "sun", "Dark Mode: (" + (darkOn ? "Enabled" : "Disabled") + ")", false, () -> {
                boolean next = !Boolean.TRUE.equals(SharedPref.getBooleanPref(Settings.AMOLED_THEME));
                SharedPref.setBooleanPref(Settings.AMOLED_THEME.key, next);
                Utils.showToastShort("AMOLED Dark Mode: " + (next ? "Enabled" : "Disabled"));
                dialog.dismiss();
            });
            sheet.addView(featuresCard);

            ScrollView scroll = new ScrollView(hostCtx);
            scroll.addView(sheet);
            dialog.setView(scroll);
            showBlurredDialog(hostCtx, dialog);
        } catch (Throwable e) {
            Logger.printException(() -> "showHomeQuickMenu failure", e);
            FragmentHook.startSettings();
        }
    }

    public static void showSavedCopiesDialog(final Context context) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            LinearLayout sheet = new LinearLayout(context);
            sheet.setOrientation(LinearLayout.VERTICAL);
            int pad = RexVectorIcons.dp(context, 18);
            sheet.setPadding(pad, RexVectorIcons.dp(context, 20), pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xF2181A1E);
            bg.setCornerRadius(RexVectorIcons.dp(context, 24));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF2E333A);
            sheet.setBackground(bg);

            TextView header = new TextView(context);
            header.setText("Saved Copies");
            header.setTextColor(Color.WHITE);
            header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f);
            header.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            header.setPadding(RexVectorIcons.dp(context, 4), 0, 0, RexVectorIcons.dp(context, 10));
            sheet.addView(header);

            addQuickAccessSectionHeader(context, sheet, "SAVED DATA");
            LinearLayout card = createGroupedMenuCard(context);
            addGroupedMenuRow(context, card, "edit_box", "Saved Bio", true, () -> {
                dialog.dismiss();
                RexVault.showSavedBiosDialog(context);
            });
            addGroupedMenuRow(context, card, "cc_box", "Saved Captions", true, () -> {
                dialog.dismiss();
                RexVault.showSavedCaptionsDialog(context);
            });
            addGroupedMenuRow(context, card, "people", "Saved Profiles", false, () -> {
                dialog.dismiss();
                RexVault.showSavedProfilesDialog(context);
            });
            sheet.addView(card);

            dialog.setView(sheet);
            showBlurredDialog(context, dialog);
        } catch (Throwable e) {
            RexVault.showSavedCaptionsDialog(context);
        }
    }

    private static void addQuickAccessSectionHeader(Context context, LinearLayout parent, String text) {
        TextView label = new TextView(context);
        label.setText(text);
        label.setTextColor(0xFF9AA3AE);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        label.setLetterSpacing(0.06f);
        label.setPadding(RexVectorIcons.dp(context, 6), RexVectorIcons.dp(context, 10), 0, RexVectorIcons.dp(context, 6));
        parent.addView(label);
    }

    private static LinearLayout createGroupedMenuCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF23262B);
        bg.setCornerRadius(RexVectorIcons.dp(context, 18));
        bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF333840);
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = RexVectorIcons.dp(context, 6);
        card.setLayoutParams(lp);
        return card;
    }

    private static void addGroupedMenuRow(
            Context context,
            LinearLayout card,
            String iconType,
            String label,
            boolean showDividerBelow,
            Runnable onClick
    ) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int hPad = RexVectorIcons.dp(context, 16);
        int vPad = RexVectorIcons.dp(context, 14);
        row.setPadding(hPad, vPad, hPad, vPad);

        ImageView icon = new ImageView(context);
        int iconPx = RexVectorIcons.dp(context, 22);
        icon.setImageDrawable(RexVectorIcons.createMenuIcon(context, iconType, 22, Color.WHITE));
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(iconPx, iconPx);
        iLp.setMarginEnd(RexVectorIcons.dp(context, 14));
        row.addView(icon, iLp);

        TextView text = new TextView(context);
        text.setText(label);
        text.setTextColor(Color.WHITE);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageView chevron = new ImageView(context);
        chevron.setImageDrawable(RexVectorIcons.createMenuIcon(context, "chevron_right", 18, 0xFF9AA3AE));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                RexVectorIcons.dp(context, 18),
                RexVectorIcons.dp(context, 18)
        );
        row.addView(chevron, cLp);

        row.setOnClickListener(v -> {
            RexHaptics.tap(v);
            if (onClick != null) onClick.run();
        });
        card.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        if (showDividerBelow) {
            View div = new View(context);
            div.setBackgroundColor(0xFF333840);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Math.max(1, RexVectorIcons.dp(context, 1))
            );
            card.addView(div, dLp);
        }
    }

    private static int getDeletedMessagesCount(Context context) {
        try {
            return RexMessageDb.getInstance(context).getDeletedMessages().size();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    // -------------------------------------------------------------------------
    // 2. Profile Smart More Options Popup (Image 14764 + HD Avatar Download & Link Tools)
    // -------------------------------------------------------------------------
    public static void showProfilePopup(final Context context, final UserData userData, final Callable<String> refreshUrl) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            int pad = RexVectorIcons.dp(context, 10);
            card.setPadding(pad, pad, pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF1A1A1A);
            bg.setCornerRadius(RexVectorIcons.dp(context, 22));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF2E333A);
            card.setBackground(bg);

            final String username = safeCall(() -> userData.getUsername(), "");
            final String fullName = safeCall(() -> userData.getFullName(), "");
            final String userId = safeCall(() -> userData.getUserId(), "");
            final String bio = safeCall(() -> userData.getBio(), "");
            final boolean isPrivate = safeBool(() -> userData.isPrivate(), false);
            String[] counts = extractFollowerFollowingCounts(context, userData);
            final String followersStr = counts[0];
            final String followingStr = counts[1];
            final String cleanProfileLink = "https://www.instagram.com/" + username + "/";

            addIconMenuRow(context, card, "download_tray", "Download Profile Picture (HD)", () -> {
                dialog.dismiss();
                downloadProfilePictureHd(context, userData, refreshUrl, username);
            });
            addIconMenuRow(context, card, "user", "View Profile Picture", () -> {
                dialog.dismiss();
                ProfilePictureViewer.show(context, userData, refreshUrl);
            });
            addIconMenuRow(context, card, "doc", "Copy Bio", () -> {
                dialog.dismiss();
                if (bio != null && !bio.isEmpty()) {
                    Utils.setClipboard(bio);
                    Utils.showToastShort("Bio copied to clipboard");
                } else {
                    Utils.showToastShort("Bio is empty");
                }
            });
            addIconMenuRow(context, card, "bookmark", "Save Bio", () -> {
                dialog.dismiss();
                RexVault.saveBio(context, username, bio);
            });
            addIconMenuRow(context, card, "translate", "Translate Bio", () -> {
                dialog.dismiss();
                RexRuntimeFeatures.translateText(context, bio, "Bio");
            });
            addIconMenuRow(context, card, "at", "Copy Username", () -> {
                dialog.dismiss();
                if (username != null && !username.isEmpty()) {
                    Utils.setClipboard(username);
                    Utils.showToastShort("Copied @" + username);
                }
            });
            if (fullName != null && !fullName.isEmpty()) {
                addIconMenuRow(context, card, "edit_box", "Copy Full Name", () -> {
                    dialog.dismiss();
                    Utils.setClipboard(fullName);
                    Utils.showToastShort("Copied full name: " + fullName);
                });
            }
            addIconMenuRow(context, card, "link", "Copy Profile Link", () -> {
                dialog.dismiss();
                Utils.setClipboard(cleanProfileLink);
                Utils.showToastShort("Copied clean profile link");
            });
            addIconMenuRow(context, card, "external_link", "Share Profile Link", () -> {
                dialog.dismiss();
                shareText(context, cleanProfileLink);
            });
            addIconMenuRow(context, card, "id_card", "Copy User ID", () -> {
                dialog.dismiss();
                if (userId != null && !userId.isEmpty()) {
                    Utils.setClipboard(userId);
                    Utils.showToastShort("Copied User ID: " + userId);
                }
            });
            addIconMenuRow(context, card, "lock", isPrivate ? "Private Profile" : "Public Profile", () -> {
                dialog.dismiss();
                Utils.setClipboard(cleanProfileLink);
                Utils.showToastShort((isPrivate ? "Private Profile" : "Public Profile") + " • Link copied");
            });
            addIconMenuRow(context, card, "people", "Followers: " + followersStr, () -> {
                dialog.dismiss();
                Utils.setClipboard(followersStr);
                Utils.showToastShort("Followers: " + followersStr + " (Copied)");
            });
            addIconMenuRow(context, card, "people", "Following: " + followingStr, () -> {
                dialog.dismiss();
                Utils.setClipboard(followingStr);
                Utils.showToastShort("Following: " + followingStr + " (Copied)");
            });
            addIconMenuRow(context, card, "user_plus", "Add to Saved Profiles", () -> {
                dialog.dismiss();
                RexVault.saveProfile(context, username, fullName, userId);
            });

            ScrollView scroll = new ScrollView(context);
            scroll.addView(card);
            dialog.setView(scroll);
            showBlurredDialog(context, dialog);
        } catch (Throwable e) {
            Logger.printException(() -> "showProfilePopup failure", e);
            Utils.showToastShort(e.getMessage());
        }
    }

    private static void downloadProfilePictureHd(
            final Context context,
            final UserData userData,
            final Callable<String> refreshUrl,
            final String username
    ) {
        Utils.runOnBackgroundThread(() -> {
            String hdUrl = null;
            try {
                if (refreshUrl != null) {
                    hdUrl = refreshUrl.call();
                }
            } catch (Throwable ignored) {}
            if (hdUrl == null || hdUrl.isEmpty()) {
                try {
                    hdUrl = userData.getProfilePictureUrl();
                } catch (Throwable ignored) {}
            }
            final String finalUrl = hdUrl;
            Utils.runOnMainThread(() -> {
                if (finalUrl == null || finalUrl.isEmpty()) {
                    Utils.showToastShort("Unable to resolve profile picture URL");
                    return;
                }
                try {
                    String safeUser = (username == null || username.isEmpty()) ? "profile" : username;
                    String fileName = safeUser + "_profile_hd_" + (System.currentTimeMillis() / 1000L) + ".jpg";
                    DownloadUtils.downloadMediaUrl(context, finalUrl, DownloadUtils.getSubfolderName(safeUser), fileName);
                } catch (Throwable e) {
                    Utils.showToastShort("Profile picture download failed: " + e.getMessage());
                }
            });
        });
    }

    private static void shareText(Context context, String text) {
        try {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            Intent chooser = Intent.createChooser(sendIntent, "Share via");
            if (!(context instanceof Activity)) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(chooser);
        } catch (Throwable e) {
            Utils.setClipboard(text);
            Utils.showToastShort("Link copied to clipboard");
        }
    }

    private static String[] extractFollowerFollowingCounts(Context context, UserData userData) {
        String followers = "—";
        String following = "—";
        try {
            Activity act = resolveActivity(context);
            if (act != null && act.getWindow() != null) {
                View decor = act.getWindow().getDecorView();
                List<String> texts = new ArrayList<>();
                collectVisibleTexts(decor, texts);
                for (int i = 0; i < texts.size(); i++) {
                    String t = texts.get(i).trim();
                    String lower = t.toLowerCase();
                    if ((lower.equals("followers") || lower.equals("follower")) && i > 0) {
                        String prev = texts.get(i - 1).trim();
                        if (prev.matches(".*[0-9].*")) followers = prev;
                    } else if (lower.endsWith(" followers") || lower.endsWith(" follower")) {
                        followers = t.replaceAll("(?i)\\s*followers?$", "").trim();
                    }
                    if (lower.equals("following") && i > 0) {
                        String prev = texts.get(i - 1).trim();
                        if (prev.matches(".*[0-9].*")) following = prev;
                    } else if (lower.endsWith(" following")) {
                        following = t.replaceAll("(?i)\\s*following$", "").trim();
                    }
                }
            }
        } catch (Throwable ignored) {}
        return new String[]{followers, following};
    }

    private static void collectVisibleTexts(View view, List<String> out) {
        if (view == null || view.getVisibility() != View.VISIBLE) return;
        if (view instanceof TextView) {
            CharSequence cs = ((TextView) view).getText();
            if (cs != null && cs.length() > 0 && cs.length() < 40) {
                out.add(cs.toString());
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                collectVisibleTexts(vg.getChildAt(i), out);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 3. Post / Reel Smart Options Sheet (With Carousel Download All & Link Tools)
    // -------------------------------------------------------------------------
    public static void showPostOptionsSheet(
            final Context context,
            final UserSession userSession,
            final Object mediaObject,
            final int currentMediaIndex
    ) {
        try {
            final int pos = Math.max(0, currentMediaIndex);
            final MediaData mediaData = new MediaData(mediaObject, userSession);
            final String username = safeCall(() -> mediaData.getUserData().getUsername(), "instagram");
            final String rawCaption = safeCall(() -> mediaData.getDescriptionText(), "");
            final String shortcode = safeCall(() -> mediaData.getShortcode(), "");
            final String postLink = shortcode.isEmpty() ? "" : ("https://www.instagram.com/p/" + shortcode + "/");
            int carouselCount = 1;
            try {
                carouselCount = Math.max(1, mediaData.getCarouselSize());
            } catch (Throwable ignored) {}
            final int totalSlides = carouselCount;

            RexVault.recordWatchedMedia(context, username, rawCaption.isEmpty() ? ("Post " + shortcode) : rawCaption, postLink);

            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            int pad = RexVectorIcons.dp(context, 10);
            card.setPadding(pad, pad, pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF1A1A1A);
            bg.setCornerRadius(RexVectorIcons.dp(context, 22));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF2E333A);
            card.setBackground(bg);

            addIconMenuRow(context, card, "download_tray", "Download", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Download");
            });
            if (totalSlides > 1) {
                addIconMenuRow(context, card, "download_tray", "Download All " + totalSlides + " Posts", () -> {
                    dialog.dismiss();
                    handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Download All Carousel");
                });
            }
            addIconMenuRow(context, card, "image", "Download Cover Photo", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Download Cover Photo");
            });
            addIconMenuRow(context, card, "bars", "Download Audio (.mp3)", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Download Audio (.mp3)");
            });
            addIconMenuRow(context, card, "link", "Copy Clean Post Link", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Copy Clean Post Link");
            });
            addIconMenuRow(context, card, "at", "Copy Author Username (@" + username + ")", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Copy Author Username");
            });
            addIconMenuRow(context, card, "doc", "Copy Caption", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Copy Caption");
            });
            addIconMenuRow(context, card, "bookmark", "Save Caption", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Save Caption");
            });
            addIconMenuRow(context, card, "translate", "Translate Caption", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Translate Caption");
            });
            addIconMenuRow(context, card, "cc_box", "Copy Hashtags", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Copy Hashtags");
            });
            addIconMenuRow(context, card, "zoom", "Cover Image to Text (OCR)", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Cover Image to Text (OCR)");
            });
            addIconMenuRow(context, card, "external_link", "Open by Phone Apps", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Open by Phone Apps");
            });
            addIconMenuRow(context, card, "post_grid", "Repost as IG Post", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Repost as IG Post");
            });
            addIconMenuRow(context, card, "story_ring", "Repost as IG Story", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Repost as IG Story");
            });
            addIconMenuRow(context, card, "external_link", "Repost as WhatsApp Story", () -> {
                dialog.dismiss();
                handlePostOptionClick(context, userSession, mediaObject, mediaData, pos, username, rawCaption, postLink, "Repost as WhatsApp Story");
            });

            ScrollView scroll = new ScrollView(context);
            scroll.addView(card);
            dialog.setView(scroll);
            showBlurredDialog(context, dialog);
        } catch (Throwable e) {
            Logger.printException(() -> "showPostOptionsSheet failure", e);
            Utils.showToastShort("Options error: " + e.getMessage());
        }
    }

    private static void handlePostOptionClick(
            Context context,
            UserSession userSession,
            Object mediaObject,
            MediaData mediaData,
            int pos,
            String username,
            String rawCaption,
            String postLink,
            String option
    ) {
        try {
            switch (option) {
                case "Cover Image to Text (OCR)":
                    RexRuntimeFeatures.extractOcrFromMedia(context, mediaData, pos);
                    break;
                case "Download":
                    DownloadUtils.showDownloadOptions(context, userSession, mediaObject, pos);
                    break;
                case "Download All Carousel":
                    DownloadUtils.downloadMedia(context, mediaData, -1, MediaType.ANY);
                    break;
                case "Download Cover Photo":
                    DownloadUtils.downloadMedia(context, mediaData, pos, MediaType.IMAGE);
                    break;
                case "Download Audio (.mp3)":
                    DownloadUtils.downloadMedia(context, mediaData, pos, MediaType.AUDIO);
                    break;
                case "Copy Clean Post Link":
                    if (postLink != null && !postLink.isEmpty()) {
                        Utils.setClipboard(postLink);
                        Utils.showToastShort("Copied clean post link");
                    } else {
                        String fallbackLink = safeCall(() -> mediaData.getMediaAt(pos).getMediaLink(), "");
                        if (!fallbackLink.isEmpty()) {
                            Utils.setClipboard(fallbackLink);
                            Utils.showToastShort("Copied media link");
                        } else {
                            Utils.showToastShort("Post link unavailable");
                        }
                    }
                    break;
                case "Copy Author Username":
                    if (username != null && !username.isEmpty()) {
                        Utils.setClipboard(username);
                        Utils.showToastShort("Copied @" + username);
                    }
                    break;
                case "Open by Phone Apps": {
                    MediaData current = mediaData.getMediaAt(pos);
                    ActivityHook.handleUrlIntent(current.isVideo(), current.getMediaLink());
                    break;
                }
                case "Repost as IG Post":
                    RexRuntimeFeatures.repostMedia(context, mediaData, pos, "IG_POST");
                    break;
                case "Repost as IG Story":
                    RexRuntimeFeatures.repostMedia(context, mediaData, pos, "IG_STORY");
                    break;
                case "Repost as WhatsApp Story":
                    RexRuntimeFeatures.repostMedia(context, mediaData, pos, "WHATSAPP");
                    break;
                case "Copy Caption": {
                    String cleaned = RexRuntimeFeatures.stripHashtagsIfConfigured(rawCaption);
                    if (cleaned == null || cleaned.isEmpty()) {
                        Utils.showToastShort("Caption is empty");
                    } else {
                        Utils.setClipboard(cleaned);
                        Utils.showToastShort("Caption copied to clipboard");
                    }
                    break;
                }
                case "Save Caption":
                    RexVault.saveCaption(context, username, rawCaption, postLink);
                    break;
                case "Translate Caption":
                    RexRuntimeFeatures.translateText(context, rawCaption, "Caption");
                    break;
                case "Copy Hashtags": {
                    String tags = RexRuntimeFeatures.extractHashtags(rawCaption);
                    if (tags.isEmpty()) {
                        Utils.showToastShort("No hashtags found in caption");
                    } else {
                        Utils.setClipboard(tags);
                        Utils.showToastShort("Copied hashtags: " + tags);
                    }
                    break;
                }
            }
        } catch (Throwable e) {
            Logger.printException(() -> "handlePostOptionClick error", e);
            Utils.showToastShort(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 4. Changelogs Dialog
    // -------------------------------------------------------------------------
    public static void showChangelogDialog(final Context context) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            int pad = RexVectorIcons.dp(context, 22);
            root.setPadding(pad, pad, pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF1A1A1A);
            bg.setCornerRadius(RexVectorIcons.dp(context, 26));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF2E333A);
            root.setBackground(bg);

            TextView title = new TextView(context);
            title.setText("Changelogs");
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            root.addView(title);

            TextView meta = new TextView(context);
            meta.setText("Release Date: 16/11/2026\n" +
                    "Base: 453.0.0.57.79 & 452.1.0.53.64 / Universal\n" +
                    "Version: 3.13.0 (AMOLED Release)");
            meta.setTextColor(0xFFD5DEE5);
            meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            meta.setPadding(0, RexVectorIcons.dp(context, 10), 0, RexVectorIcons.dp(context, 14));
            root.addView(meta);

            ScrollView scroll = new ScrollView(context);
            TextView body = new TextView(context);
            body.setText(
                    "Added: Hold Home Button (Bottom Tab Bar) or Top Logo to open Quick Access Sheet.\n" +
                    "Added: Tactile Haptic Feedback across all switches, buttons, menus, and long-press actions.\n" +
                    "Added: Distinct custom vector icons across every Ghost Mode, Privacy, Gesture, Feed, and Download setting.\n" +
                    "Added: Minimal AMOLED About screen with direct GitHub, Morphe Source, and official channel links.\n" +
                    "Added: HD Profile Picture download, Clean Profile/Post Link copy & share, and Carousel Download All in More Options.\n" +
                    "Added: Auto Mark as Seen after Reply & Anti-Delete Messages local vault.\n" +
                    "Fixed: Card border rendering and rounded corner stability during fast ListView scrolling."
            );
            body.setTextColor(0xFFCFD8DF);
            body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            body.setLineSpacing(RexVectorIcons.dp(context, 3), 1.0f);
            scroll.addView(body);
            root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

            LinearLayout footer = new LinearLayout(context);
            footer.setOrientation(LinearLayout.HORIZONTAL);
            footer.setGravity(Gravity.END);
            footer.setPadding(0, RexVectorIcons.dp(context, 14), 0, 0);

            TextView close = new TextView(context);
            close.setText("Close");
            close.setTextColor(Color.WHITE);
            close.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            close.setTypeface(Typeface.DEFAULT_BOLD);
            int p = RexVectorIcons.dp(context, 10);
            close.setPadding(p, p, p, p);
            close.setOnClickListener(v -> {
                RexHaptics.tap(v);
                dialog.dismiss();
            });
            footer.addView(close);
            root.addView(footer);

            dialog.setView(root);
            showBlurredDialog(context, dialog);
        } catch (Throwable ignored) {}
    }

    // -------------------------------------------------------------------------
    // 5. Support Developer Dialog
    // -------------------------------------------------------------------------
    public static void showSupportDialog(final Context context) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setGravity(Gravity.CENTER_HORIZONTAL);
            int pad = RexVectorIcons.dp(context, 22);
            root.setPadding(pad, pad, pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF1A1A1A);
            bg.setCornerRadius(RexVectorIcons.dp(context, 26));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF2E333A);
            root.setBackground(bg);

            TextView title = new TextView(context);
            title.setText("Support Developer");
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setGravity(Gravity.CENTER);
            root.addView(title);

            TextView body = new TextView(context);
            body.setText(
                    "We're dedicated to providing a high-quality, reliable, and continuously improving AMOLED Instagram experience through xboyrex.\n\n" +
                    "Your support helps us maintain updates, implement new features, fix bugs, and ensure long-term development.\n\n" +
                    "If you've found value in Rex IG Lite, please consider starring our repository on GitHub or joining our community channels.\n\n" +
                    "Thank you for being part of our journey."
            );
            body.setTextColor(0xFFD5DEE5);
            body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            body.setGravity(Gravity.CENTER);
            body.setPadding(0, RexVectorIcons.dp(context, 14), 0, RexVectorIcons.dp(context, 18));
            root.addView(body);

            LinearLayout footer = new LinearLayout(context);
            footer.setOrientation(LinearLayout.HORIZONTAL);
            footer.setGravity(Gravity.END);
            footer.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            int p = RexVectorIcons.dp(context, 12);
            TextView close = new TextView(context);
            close.setText("Close");
            close.setTextColor(0xFF9AA3AE);
            close.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            close.setTypeface(Typeface.DEFAULT_BOLD);
            close.setPadding(p, p, p, p);
            close.setOnClickListener(v -> {
                RexHaptics.tap(v);
                dialog.dismiss();
            });
            footer.addView(close);

            TextView star = new TextView(context);
            star.setText("Star on GitHub");
            star.setTextColor(0xFF2D95E8);
            star.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            star.setTypeface(Typeface.DEFAULT_BOLD);
            star.setPadding(p, p, p, p);
            star.setOnClickListener(v -> {
                RexHaptics.tap(v);
                dialog.dismiss();
                RexUtils.openUrl("https://github.com/xboyrex/rex-ig-lite", false);
            });
            footer.addView(star);
            root.addView(footer);

            dialog.setView(root);
            showBlurredDialog(context, dialog);
        } catch (Throwable ignored) {}
    }

    // -------------------------------------------------------------------------
    // 6. Developer Options Warning Dialog
    // -------------------------------------------------------------------------
    public static void showDevWarningDialog(final Context context, final Runnable onProceed) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            int pad = RexVectorIcons.dp(context, 22);
            root.setPadding(pad, pad, pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF1A1A1A);
            bg.setCornerRadius(RexVectorIcons.dp(context, 26));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF2E333A);
            root.setBackground(bg);

            TextView title = new TextView(context);
            title.setText("Developer Options Warning");
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            root.addView(title);

            TextView body = new TextView(context);
            body.setText(
                    "You are about to enter Developer Options. Changing internal MetaConfig flags without proper knowledge may cause unexpected behavior.\n\n" +
                    "• Proceed only if you know what you are modifying.\n" +
                    "• You can export or reset overrides at any time."
            );
            body.setTextColor(0xFFD5DEE5);
            body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            body.setPadding(0, RexVectorIcons.dp(context, 12), 0, RexVectorIcons.dp(context, 16));
            root.addView(body);

            LinearLayout footer = new LinearLayout(context);
            footer.setOrientation(LinearLayout.HORIZONTAL);
            footer.setGravity(Gravity.END);

            int p = RexVectorIcons.dp(context, 12);
            TextView cancel = new TextView(context);
            cancel.setText("Cancel");
            cancel.setTextColor(0xFF9AA3AE);
            cancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            cancel.setTypeface(Typeface.DEFAULT_BOLD);
            cancel.setPadding(p, p, p, p);
            cancel.setOnClickListener(v -> {
                RexHaptics.tap(v);
                dialog.dismiss();
            });
            footer.addView(cancel);

            TextView proceed = new TextView(context);
            proceed.setText("Proceed");
            proceed.setTextColor(0xFF2D95E8);
            proceed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            proceed.setTypeface(Typeface.DEFAULT_BOLD);
            proceed.setPadding(p, p, p, p);
            proceed.setOnClickListener(v -> {
                RexHaptics.tap(v);
                dialog.dismiss();
                if (onProceed != null) onProceed.run();
            });
            footer.addView(proceed);
            root.addView(footer);

            dialog.setView(root);
            showBlurredDialog(context, dialog);
        } catch (Throwable e) {
            if (onProceed != null) onProceed.run();
        }
    }

    private static void addIconMenuRow(Context context, LinearLayout parent, String iconType, String label, Runnable onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int hPad = RexVectorIcons.dp(context, 14);
        int vPad = RexVectorIcons.dp(context, 12);
        row.setPadding(hPad, vPad, hPad, vPad);

        ImageView icon = new ImageView(context);
        int iconPx = RexVectorIcons.dp(context, 22);
        icon.setImageDrawable(RexVectorIcons.createMenuIcon(context, iconType, 22, Color.WHITE));
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(iconPx, iconPx);
        iLp.setMarginEnd(RexVectorIcons.dp(context, 14));
        row.addView(icon, iLp);

        TextView text = new TextView(context);
        text.setText(label);
        text.setTextColor(Color.WHITE);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        row.setOnClickListener(v -> {
            RexHaptics.tap(v);
            if (onClick != null) onClick.run();
        });
        parent.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private static String safeCall(Callable<String> c, String fallback) {
        try {
            String r = c.call();
            return r != null ? r : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static boolean safeBool(Callable<Boolean> c, boolean fallback) {
        try {
            Boolean r = c.call();
            return r != null ? r : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static void showConfirmDialog(
            final Context context,
            final String titleText,
            final String messageText,
            final String confirmLabel,
            final boolean destructive,
            final Runnable onConfirm
    ) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            int pad = RexVectorIcons.dp(context, 22);
            root.setPadding(pad, pad, pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF1A1A1A);
            bg.setCornerRadius(RexVectorIcons.dp(context, 24));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF2E333A);
            root.setBackground(bg);

            TextView title = new TextView(context);
            title.setText(titleText);
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            root.addView(title);

            TextView body = new TextView(context);
            body.setText(messageText);
            body.setTextColor(0xFF9AA3AE);
            body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            body.setPadding(0, RexVectorIcons.dp(context, 12), 0, RexVectorIcons.dp(context, 18));
            root.addView(body);

            LinearLayout footer = new LinearLayout(context);
            footer.setOrientation(LinearLayout.HORIZONTAL);
            footer.setGravity(Gravity.END);

            int p = RexVectorIcons.dp(context, 12);
            TextView cancel = new TextView(context);
            cancel.setText("Cancel");
            cancel.setTextColor(0xFF9AA3AE);
            cancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            cancel.setTypeface(Typeface.DEFAULT_BOLD);
            cancel.setPadding(p, p, p, p);
            cancel.setOnClickListener(v -> {
                RexHaptics.tap(v);
                dialog.dismiss();
            });
            footer.addView(cancel);

            TextView confirm = new TextView(context);
            confirm.setText(confirmLabel);
            confirm.setTextColor(destructive ? 0xFFFF4438 : 0xFF2D95E8);
            confirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            confirm.setTypeface(Typeface.DEFAULT_BOLD);
            confirm.setPadding(p, p, p, p);
            confirm.setOnClickListener(v -> {
                RexHaptics.tap(v);
                dialog.dismiss();
                if (onConfirm != null) onConfirm.run();
            });
            footer.addView(confirm);
            root.addView(footer);

            dialog.setView(root);
            showBlurredDialog(context, dialog);
        } catch (Throwable e) {
            if (onConfirm != null) onConfirm.run();
        }
    }

    public static void showBackupRestoreDialog(final Context context) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            int pad = RexVectorIcons.dp(context, 20);
            root.setPadding(pad, pad, pad, pad);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF1A1A1A);
            bg.setCornerRadius(RexVectorIcons.dp(context, 24));
            bg.setStroke(Math.max(2, RexVectorIcons.dp(context, 1)), 0xFF2E333A);
            root.setBackground(bg);

            TextView title = new TextView(context);
            title.setText("Backup, Restore & Reset");
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setPadding(0, 0, 0, RexVectorIcons.dp(context, 12));
            root.addView(title);

            Activity act = resolveActivity(context);
            addIconMenuRow(context, root, "hd_upload", "Export Settings Backup (.json)", () -> {
                dialog.dismiss();
                if (act != null) ActivityHook.launchFragment(act, "rex_export_pref");
            });
            addIconMenuRow(context, root, "download_tray", "Import Settings Backup (.json)", () -> {
                dialog.dismiss();
                if (act != null) ActivityHook.launchFragment(act, "rex_import_pref");
            });
            addIconMenuRow(context, root, "trash", "Reset All Settings to Default", () -> {
                dialog.dismiss();
                app.morphe.extension.instagram.utils.InstaUtils.showResetSettingsDialog(context);
            });

            dialog.setView(root);
            showBlurredDialog(context, dialog);
        } catch (Throwable e) {
            Utils.showToastShort("Backup dialog error: " + e.getMessage());
        }
    }
}
