/*
 * Copyright (C) 2026 xboyrex <https://github.com/xboyrex/rex-ig-lite>
 */
package app.morphe.extension.instagram.patches.userprofile;

import static app.morphe.extension.instagram.utils.IgStr.str;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.morphe.extension.rex.ObjectBrowser;
import app.morphe.extension.rex.RexUtils;
import app.morphe.extension.instagram.entity.InstagramButton;
import app.morphe.extension.instagram.entity.InstagramButtonStyleEnum;
import app.morphe.extension.instagram.entity.ProfileInfo;
import app.morphe.extension.instagram.entity.UserData;
import app.morphe.extension.instagram.ui.RexDialogs;
import app.morphe.extension.instagram.ui.RexVectorIcons;
import app.morphe.extension.instagram.utils.Pref;
import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.Utils;
import app.morphe.extension.shared.ui.Dim;

import com.instagram.igds.components.button.IgdsButton;

public class ProfileMoreOption {

    private static final Pattern BIO_LINK = Pattern.compile(
            "(?:https?://)?(?:[\\w-]+\\.)+[a-z]{2,}(?:/[^\\s]*)?",
            Pattern.CASE_INSENSITIVE);

    public static void moreOptionsDailogueBox(Context context, UserData userData) {
        try {
            Activity activity = RexDialogs.resolveActivity(context);
            Context ctx = activity != null ? activity : context;

            LinearLayout card = new LinearLayout(ctx);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(RexDialogs.roundedCardBackground(ctx, 0xFF0E1014, 0xFF22262E, 22f));
            card.setPadding(0, RexDialogs.dp(ctx, 10), 0, RexDialogs.dp(ctx, 10));

            Dialog dialog = new Dialog(ctx);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(card);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.gravity = Gravity.END | Gravity.TOP;
                lp.y = RexDialogs.dp(ctx, 56);
                lp.x = RexDialogs.dp(ctx, 10);
                lp.width = RexDialogs.dp(ctx, 255);
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.dimAmount = 0.55f;
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setAttributes(lp);
            }

            String username = safe(() -> userData.getUsername(), "user");
            String fullName = safe(() -> userData.getFullName(), "");
            String userId = safe(() -> userData.getUserId(), "");
            String bio = safe(() -> userData.getBio(), "");
            boolean isPrivate = false;
            try {
                isPrivate = Boolean.TRUE.equals(userData.isVerified());
            } catch (Throwable ignored) {}
            int followersCount = 0;
            try {
                Boolean fb = userData.getUserFriendshipStatus().getFollowBackStatus();
                followersCount = Boolean.TRUE.equals(fb) ? 1 : 0;
            } catch (Throwable ignored) {}

            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_DOC_BIO, "Copy Bio", () -> {
                dialog.dismiss();
                copyOrToast(bio);
            }));

            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_USER_CIRCLE, "View Profile Picture", () -> {
                dialog.dismiss();
                try {
                    ProfilePictureViewer.show(ctx, userData);
                } catch (Exception e) {
                    Utils.showToastShort(e.getMessage());
                }
            }));

            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_BOOKMARK_EDIT, "Save Bio", () -> {
                dialog.dismiss();
                if (bio != null && !bio.isEmpty()) {
                    RexDialogs.addSavedItem(RexDialogs.KEY_SAVED_BIOS, "@" + username + ": " + bio);
                    Utils.showToastShort("Bio saved!");
                } else {
                    Utils.showToastShort("Bio is empty");
                }
            }));

            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_TRANSLATE, "Translate Bio", () -> {
                dialog.dismiss();
                if (bio != null && !bio.isEmpty()) {
                    RexUtils.openUrl("https://translate.google.com/?sl=auto&tl=en&text=" + android.net.Uri.encode(bio), false);
                } else {
                    Utils.showToastShort("Bio is empty");
                }
            }));

            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_AT_SIGN, "Copy Username", () -> {
                dialog.dismiss();
                copyOrToast(username);
            }));

            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_ID_CARD, "Copy User ID", () -> {
                dialog.dismiss();
                copyOrToast(userId);
            }));

            final boolean finalIsPrivate = isPrivate;
            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_LOCK,
                    finalIsPrivate ? "Verified Profile" : "Standard Profile", () -> {
                        dialog.dismiss();
                        Utils.showToastShort(finalIsPrivate ? "This profile is Verified" : "This profile is Standard");
                    }));

            final int fCountFinal = followersCount;
            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_PEOPLE,
                    "Followers: " + fCountFinal, () -> {
                        dialog.dismiss();
                        copyOrToast(String.valueOf(fCountFinal));
                    }));

            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_PEOPLE,
                    "Following: " + (fullName.isEmpty() ? "@" + username : fullName), () -> {
                        dialog.dismiss();
                        String links = linksFrom(bio);
                        if (links != null) {
                            copyOrToast(links);
                        } else {
                            copyOrToast(fullName.isEmpty() ? username : fullName);
                        }
                    }));

            card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_USER_PLUS, "Add to Saved Profiles", () -> {
                dialog.dismiss();
                RexDialogs.addSavedItem(RexDialogs.KEY_SAVED_PROFILES,
                        "@" + username + (fullName.isEmpty() ? "" : " (" + fullName + ")"));
                Utils.showToastShort("Added @" + username + " to Saved Profiles");
            }));

            if (Pref.rexDebug()) {
                card.addView(RexDialogs.createPopupMenuItem(ctx, RexVectorIcons.IC_DEV_BRACES, str("rex_debug"), () -> {
                    dialog.dismiss();
                    ObjectBrowser.browseObject(ctx, userData);
                }));
            }

            RexDialogs.applySmoothEntrance(card);
            dialog.show();
        } catch (Exception e) {
            Logger.printException(() -> "Error at moreOptionsDailogueBox", e);
            Utils.showToastShort(e.getMessage());
        }
    }

    private interface SafeSupplier {
        String get() throws Exception;
    }

    private static String safe(SafeSupplier s, String fallback) {
        try {
            String v = s.get();
            return v != null ? v : fallback;
        } catch (Throwable t) {
            return fallback;
        }
    }

    private static void copyOrToast(String text) {
        if (text != null && !text.isEmpty()) {
            Utils.setClipboard(text);
            Utils.showToastShort(str("rex_copied"));
        } else {
            Utils.showToastShort("Nothing to copy");
        }
    }

    private static String linksFrom(String bio) {
        if (bio == null || bio.isEmpty()) return null;
        Matcher matcher = BIO_LINK.matcher(bio);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String link = matcher.group();
            if (out.indexOf(link) >= 0) continue;
            if (out.length() > 0) out.append('\n');
            out.append(link);
        }
        return out.length() == 0 ? null : out.toString();
    }

    public static void addProfileMoreOptionsButton(ViewGroup viewGroup, ProfileInfo profileInfo) {
        try {
            UserData userData = profileInfo.getUserData();
            Context context = viewGroup.getContext();
            InstagramButton button = new InstagramButton(context);
            button.setText(str("rex_more_profile_options"));
            button.setStyle(InstagramButtonStyleEnum.SECONDARY);
            button.setOnClickListener(() -> moreOptionsDailogueBox(context, userData));

            int marginPx = Dim.dp12;
            button.setMargins(marginPx, Dim.dp6, marginPx, Dim.dp8);

            IgdsButton igdsButton = button.getIgdsButton();
            viewGroup.addView(igdsButton);
            igdsButton.bringToFront();
            viewGroup.requestLayout();
            viewGroup.invalidate();
        } catch (Exception e) {
            Logger.printException(() -> "Failed to add profile more button: ", e);
        }
    }
}
