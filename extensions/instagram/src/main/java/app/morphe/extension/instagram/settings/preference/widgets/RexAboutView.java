/*
 * Copyright (C) 2026 xboyrex <https://github.com/xboyrex/rex-ig-lite>
 * GNU General Public License v3.0
 */

package app.morphe.extension.instagram.settings.preference.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import app.morphe.extension.instagram.settings.ActivityHook;
import app.morphe.extension.instagram.utils.InstaUtils;
import app.morphe.extension.rex.RexUtils;

public final class RexAboutView {

    private static final int SURFACE_CARD = 0xFF1A1A1A;
    private static final int BORDER_STROKE = 0xFF2A2E33;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_SECTION = 0xFF9AA3AE;
    private static final int TEXT_BLUE = 0xFF2D95E8;

    private RexAboutView() {
    }

    public static View createAboutScreen(final Context context) {
        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(Color.BLACK);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        int hPad = dp(context, 14);
        container.setPadding(hPad, dp(context, 6), hPad, dp(context, 36));

        // 1. Minimal Top Title Card: Rex IG Lite V3.13.0
        LinearLayout titleCard = createMinimalCard(context);
        TextView titleText = new TextView(context);
        titleText.setText("Rex IG Lite V3.13.0");
        titleText.setTextColor(TEXT_WHITE);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21f);
        titleText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleCard.addView(titleText);
        container.addView(titleCard, createCardLayoutParams(context));

        // 2. Team Card
        LinearLayout teamCard = createMinimalCard(context);
        addSectionTitle(context, teamCard, "Team");
        addInlineLinkRow(
                context,
                teamCard,
                "Owner & Modder",
                "xboyrex (Mayank)",
                true,
                () -> RexUtils.openUrl("https://github.com/xboyrex", false)
        );
        container.addView(teamCard, createCardLayoutParams(context));

        // 3. Project & Updates Card
        LinearLayout projectCard = createMinimalCard(context);
        addSectionTitle(context, projectCard, "Project & Updates");
        addInlineLinkRow(
                context,
                projectCard,
                "GitHub Repository",
                "xboyrex/rex-ig-lite (Tap to open)",
                true,
                () -> RexUtils.openUrl("https://github.com/xboyrex/rex-ig-lite", false)
        );
        addInlineLinkRow(
                context,
                projectCard,
                "Morphe Source",
                "Add to Morphe Manager (Tap to add)",
                false,
                () -> RexUtils.openUrl("https://morphe.software/add-source?github=xboyrex/rex-ig-lite", false)
        );
        addInlineLinkRow(
                context,
                projectCard,
                "Changelog",
                "View v3.13.0 release notes (Tap to view)",
                false,
                () -> RexDialogs.showChangelogDialog(context)
        );
        addInlineLinkRow(
                context,
                projectCard,
                "Backup & Restore",
                "Export, import or reset settings (Tap to open)",
                false,
                () -> showBackupRestoreDialog(context)
        );
        container.addView(projectCard, createCardLayoutParams(context));

        // 4. Support Us Card
        LinearLayout supportCard = createMinimalCard(context);
        addSectionTitle(context, supportCard, "Support Us");
        addInlineLinkRow(
                context,
                supportCard,
                "Star on GitHub",
                "xboyrex/rex-ig-lite (Tap to open)",
                true,
                () -> RexUtils.openUrl("https://github.com/xboyrex/rex-ig-lite", false)
        );
        addInlineLinkRow(
                context,
                supportCard,
                "Support Developer",
                "xboyrex (Tap to open)",
                false,
                () -> RexDialogs.showSupportDialog(context)
        );
        container.addView(supportCard, createCardLayoutParams(context));

        // 5. Official Channels Card
        LinearLayout channelsCard = createMinimalCard(context);
        addSectionTitle(context, channelsCard, "Official Channels");
        addInlineLinkRow(
                context,
                channelsCard,
                "YouTube",
                "Subscribe for tutorials (@mayankdroid)",
                true,
                () -> RexUtils.openUrl("https://youtube.com/@mayankdroid", false)
        );
        addInlineLinkRow(
                context,
                channelsCard,
                "YouTube (OnyxDroid)",
                "Subscribe for updates (@onyxdroid89)",
                false,
                () -> RexUtils.openUrl("https://youtube.com/@onyxdroid89", false)
        );
        addInlineLinkRow(
                context,
                channelsCard,
                "Telegram",
                "Join for latest updates (@MayankDroid7)",
                false,
                () -> RexUtils.openUrl("https://t.me/MayankDroid7", false)
        );
        addInlineLinkRow(
                context,
                channelsCard,
                "Telegram Hub",
                "Join OnyxHub7 (@OnyxHub7)",
                false,
                () -> RexUtils.openUrl("https://t.me/OnyxHub7", false)
        );
        addInlineLinkRow(
                context,
                channelsCard,
                "Instagram",
                "Follow @mayank.zx7",
                false,
                () -> RexUtils.openUrl("https://www.instagram.com/mayank.zx7", false)
        );
        container.addView(channelsCard, createCardLayoutParams(context));

        scroll.addView(container);
        return scroll;
    }

    private static void showBackupRestoreDialog(final Context context) {
        final String[] items = {
                "Export Settings Backup (.json)",
                "Import Settings Backup (.json)",
                "Reset All Settings to Default"
        };
        new android.app.AlertDialog.Builder(InstagramPreferenceStyle.dialogContext(context))
                .setTitle("Backup & Restore")
                .setItems(items, (dialog, which) -> {
                    RexHaptics.tap(context);
                    Activity act = RexDialogs.resolveActivity(context);
                    if (which == 0 && act != null) {
                        ActivityHook.launchFragment(act, "rex_export_pref");
                    } else if (which == 1 && act != null) {
                        ActivityHook.launchFragment(act, "rex_import_pref");
                    } else if (which == 2) {
                        InstaUtils.showResetSettingsDialog(context);
                    }
                })
                .show();
    }

    private static LinearLayout createMinimalCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        int hPad = dp(context, 18);
        int vPad = dp(context, 16);
        card.setPadding(hPad, vPad, hPad, vPad);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(SURFACE_CARD);
        bg.setCornerRadius(dp(context, 16));
        bg.setStroke(Math.max(2, dp(context, 1)), BORDER_STROKE);
        card.setBackground(bg);
        return card;
    }

    private static LinearLayout.LayoutParams createCardLayoutParams(Context context) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = dp(context, 12);
        return lp;
    }

    private static void addSectionTitle(Context context, LinearLayout card, String text) {
        TextView header = new TextView(context);
        header.setText(text);
        header.setTextColor(TEXT_SECTION);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        header.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        header.setPadding(0, 0, 0, dp(context, 8));
        card.addView(header);
    }

    private static void addInlineLinkRow(
            Context context,
            LinearLayout card,
            String roleLabel,
            String valueLabel,
            boolean firstRow,
            Runnable onClick
    ) {
        TextView row = new TextView(context);
        String prefix = roleLabel + " - ";
        SpannableStringBuilder ssb = new SpannableStringBuilder(prefix + valueLabel);
        ssb.setSpan(new ForegroundColorSpan(TEXT_WHITE), 0, prefix.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, roleLabel.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(TEXT_BLUE), prefix.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        row.setText(ssb);
        row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        int topPad = firstRow ? dp(context, 2) : dp(context, 12);
        row.setPadding(0, topPad, 0, dp(context, 2));
        if (onClick != null) {
            row.setOnClickListener(v -> {
                RexHaptics.tap(v);
                onClick.run();
            });
        }
        card.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
    }

    private static int dp(Context context, float value) {
        return RexVectorIcons.dp(context, value);
    }
}
