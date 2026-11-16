/*
 * Copyright (C) 2026 xboyrex <https://github.com/xboyrex/rex-ig-lite>
 * GNU General Public License v3.0
 */

package app.morphe.extension.instagram.settings.preference.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public final class RexVectorIcons {

    private RexVectorIcons() {}

    public static int dp(Context context, float value) {
        if (context == null) return Math.round(value * 2.75f);
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()));
    }

    /** Resolves the logical rex_vec_* icon names used by preference and patch entry points. */
    public static Drawable create(Context context, String iconName, int color, int sizeDp) {
        String type = iconName == null ? "hexagon" : iconName;
        if (type.startsWith("rex_vec_")) type = type.substring("rex_vec_".length());
        switch (type) {
            case "feed": type = "instagram"; break;
            case "ui": type = "sliders"; break;
            case "dev": type = "code_slash"; break;
            case "info": type = "doc"; break;
            case "download": type = "download_tray"; break;
            default: break;
        }
        return createMenuIcon(context, type, sizeDp, color);
    }

    public static Drawable createHexagonIcon(Context context, int sizeDp, int color) {
        return createMenuIcon(context, "hexagon", sizeDp, color);
    }

    public static Drawable createDownloadIcon(Context context, int sizeDp, int color) {
        return createMenuIcon(context, "download_tray", sizeDp, color);
    }

    public static Drawable createTranslateIcon(Context context, int sizeDp, int color) {
        return createMenuIcon(context, "translate", sizeDp, color);
    }

    public static Path buildScallopedPath(float cx, float cy, float baseRadius, float waveAmp, int lobes) {
        Path path = new Path();
        int steps = 180;
        for (int i = 0; i <= steps; i++) {
            double theta = (2.0 * Math.PI * i) / steps;
            float r = (float) (baseRadius + waveAmp * Math.cos(lobes * theta));
            float x = (float) (cx + r * Math.cos(theta));
            float y = (float) (cy + r * Math.sin(theta));
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        return path;
    }

    public static Drawable createScallopedAvatar(final Context context, final Bitmap bitmap, final int sizeDp) {
        final int px = dp(context, sizeDp);
        return new Drawable() {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            public void draw(Canvas canvas) {
                Rect b = getBounds();
                float w = b.width() > 0 ? b.width() : px;
                float h = b.height() > 0 ? b.height() : px;
                float cx = b.left + w / 2f;
                float cy = b.top + h / 2f;
                float r = Math.min(w, h) * 0.44f;
                float amp = Math.min(w, h) * 0.042f;
                Path scallop = buildScallopedPath(cx, cy, r, amp, 12);

                if (bitmap != null && !bitmap.isRecycled()) {
                    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Matrix m = new Matrix();
                    float scale = Math.max(w / (float) bitmap.getWidth(), h / (float) bitmap.getHeight());
                    m.setScale(scale, scale);
                    m.postTranslate(b.left + (w - bitmap.getWidth() * scale) / 2f,
                            b.top + (h - bitmap.getHeight() * scale) / 2f);
                    shader.setLocalMatrix(m);
                    paint.setShader(shader);
                    canvas.drawPath(scallop, paint);
                } else {
                    paint.setShader(null);
                    paint.setColor(0xFF223036);
                    canvas.drawPath(scallop, paint);
                }

                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(Math.max(2f, w * 0.02f));
                borderPaint.setColor(0x4488C2CE);
                canvas.drawPath(scallop, borderPaint);
            }

            @Override public int getIntrinsicWidth() { return px; }
            @Override public int getIntrinsicHeight() { return px; }
            @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
            @Override public void setColorFilter(ColorFilter cf) { paint.setColorFilter(cf); }
            @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        };
    }

    public static Drawable createBlackInstagramBadge(final Context context, final int sizeDp) {
        final int px = dp(context, sizeDp);
        return new Drawable() {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            public void draw(Canvas canvas) {
                Rect b = getBounds();
                float w = b.width() > 0 ? b.width() : px;
                float h = b.height() > 0 ? b.height() : px;
                float cx = b.left + w / 2f;
                float cy = b.top + h / 2f;

                Path outer = buildScallopedPath(cx, cy, w * 0.43f, w * 0.045f, 7);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF0A0C0E);
                canvas.drawPath(outer, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(2f, w * 0.028f));
                paint.setColor(0xFF88C2CE);
                canvas.drawPath(outer, paint);

                float half = w * 0.21f;
                RectF camRect = new RectF(cx - half, cy - half, cx + half, cy + half);
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(Math.max(2.2f, w * 0.042f));
                canvas.drawRoundRect(camRect, w * 0.11f, w * 0.11f, paint);
                canvas.drawCircle(cx, cy, w * 0.095f, paint);

                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx + w * 0.115f, cy - w * 0.115f, w * 0.028f, paint);
            }

            @Override public int getIntrinsicWidth() { return px; }
            @Override public int getIntrinsicHeight() { return px; }
            @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
            @Override public void setColorFilter(ColorFilter cf) { paint.setColorFilter(cf); }
            @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        };
    }

    public static Drawable createMenuIcon(final Context context, final String type, final int sizeDp, final int defaultColor) {
        final int px = dp(context, sizeDp);
        return new Drawable() {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final int tintColor = defaultColor;

            @Override
            public void draw(Canvas canvas) {
                Rect b = getBounds();
                float w = b.width() > 0 ? b.width() : px;
                float h = b.height() > 0 ? b.height() : px;
                float cx = b.left + w / 2f;
                float cy = b.top + h / 2f;
                float r = Math.min(w, h) * 0.36f;

                paint.setPathEffect(null);
                paint.setColor(tintColor);
                paint.setStyle(Paint.Style.STROKE);
                float stroke = Math.max(2f, Math.min(w, h) * 0.082f);
                paint.setStrokeWidth(stroke);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);

                String t = type == null ? "hexagon" : type;
                switch (t) {
                    case "hexagon":
                    case "misc": {
                        Path hex = new Path();
                        for (int i = 0; i < 6; i++) {
                            double a = Math.toRadians(60 * i - 30);
                            float x = (float) (cx + r * Math.cos(a));
                            float y = (float) (cy + r * Math.sin(a));
                            if (i == 0) hex.moveTo(x, y);
                            else hex.lineTo(x, y);
                        }
                        hex.close();
                        canvas.drawPath(hex, paint);
                        canvas.drawCircle(cx, cy, r * 0.35f, paint);
                        break;
                    }
                    case "gear": {
                        canvas.drawCircle(cx, cy, r * 0.36f, paint);
                        canvas.drawCircle(cx, cy, r * 0.72f, paint);
                        for (int i = 0; i < 8; i++) {
                            double a = Math.toRadians(i * 45);
                            canvas.drawLine(
                                    (float) (cx + r * 0.72f * Math.cos(a)),
                                    (float) (cy + r * 0.72f * Math.sin(a)),
                                    (float) (cx + r * 0.92f * Math.cos(a)),
                                    (float) (cy + r * 0.92f * Math.sin(a)),
                                    paint);
                        }
                        break;
                    }
                    case "globe": {
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        RectF oval = new RectF(cx - r * 0.42f, cy - r * 0.82f, cx + r * 0.42f, cy + r * 0.82f);
                        canvas.drawOval(oval, paint);
                        canvas.drawLine(cx - r * 0.8f, cy, cx + r * 0.8f, cy, paint);
                        break;
                    }
                    case "code":
                    case "code_slash": {
                        // Clean </> vector icon
                        canvas.drawLine(cx - r * 0.35f, cy - r * 0.45f, cx - r * 0.82f, cy, paint);
                        canvas.drawLine(cx - r * 0.82f, cy, cx - r * 0.35f, cy + r * 0.45f, paint);
                        canvas.drawLine(cx + r * 0.35f, cy - r * 0.45f, cx + r * 0.82f, cy, paint);
                        canvas.drawLine(cx + r * 0.82f, cy, cx + r * 0.35f, cy + r * 0.45f, paint);
                        canvas.drawLine(cx + r * 0.16f, cy - r * 0.65f, cx - r * 0.16f, cy + r * 0.65f, paint);
                        break;
                    }
                    case "typing_dm": {
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        float dotR = r * 0.11f;
                        canvas.drawCircle(cx - r * 0.34f, cy, dotR, paint);
                        canvas.drawCircle(cx, cy, dotR, paint);
                        canvas.drawCircle(cx + r * 0.34f, cy, dotR, paint);
                        break;
                    }
                    case "story_ring": {
                        paint.setPathEffect(new DashPathEffect(new float[]{r * 0.36f, r * 0.22f}, 0));
                        canvas.drawCircle(cx, cy, r * 0.84f, paint);
                        paint.setPathEffect(null);
                        Path hp = new Path();
                        float hr = r * 0.42f;
                        hp.moveTo(cx, cy + hr * 0.75f);
                        hp.cubicTo(cx - hr * 1.1f, cy + hr * 0.1f, cx - hr * 0.9f, cy - hr * 0.85f, cx, cy - hr * 0.3f);
                        hp.cubicTo(cx + hr * 0.9f, cy - hr * 0.85f, cx + hr * 1.1f, cy + hr * 0.1f, cx, cy + hr * 0.75f);
                        canvas.drawPath(hp, paint);
                        break;
                    }
                    case "live_badge": {
                        RectF tv = new RectF(cx - r * 0.86f, cy - r * 0.56f, cx + r * 0.86f, cy + r * 0.64f);
                        canvas.drawRoundRect(tv, r * 0.2f, r * 0.2f, paint);
                        canvas.drawLine(cx - r * 0.3f, cy - r * 0.85f, cx, cy - r * 0.56f, paint);
                        canvas.drawLine(cx + r * 0.3f, cy - r * 0.85f, cx, cy - r * 0.56f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTextAlign(Paint.Align.CENTER);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                        paint.setTextSize(r * 0.56f);
                        Paint.FontMetrics fm = paint.getFontMetrics();
                        canvas.drawText("LIVE", cx, cy + r * 0.04f - (fm.ascent + fm.descent) / 2f, paint);
                        break;
                    }
                    case "view_once": {
                        paint.setPathEffect(new DashPathEffect(new float[]{r * 0.34f, r * 0.22f}, 0));
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        paint.setPathEffect(null);
                        canvas.drawLine(cx, cy - r * 0.36f, cx, cy + r * 0.38f, paint);
                        canvas.drawLine(cx - r * 0.2f, cy - r * 0.18f, cx, cy - r * 0.36f, paint);
                        break;
                    }
                    case "screenshot": {
                        RectF ph = new RectF(cx - r * 0.52f, cy - r * 0.85f, cx + r * 0.52f, cy + r * 0.85f);
                        canvas.drawRoundRect(ph, r * 0.16f, r * 0.16f, paint);
                        canvas.drawLine(cx - r * 0.28f, cy - r * 0.45f, cx - r * 0.1f, cy - r * 0.45f, paint);
                        canvas.drawLine(cx - r * 0.28f, cy - r * 0.45f, cx - r * 0.28f, cy - r * 0.25f, paint);
                        canvas.drawLine(cx + r * 0.28f, cy + r * 0.45f, cx + r * 0.1f, cy + r * 0.45f, paint);
                        canvas.drawLine(cx + r * 0.28f, cy + r * 0.45f, cx + r * 0.28f, cy + r * 0.25f, paint);
                        break;
                    }
                    case "replay":
                    case "refresh": {
                        RectF rf = new RectF(cx - r * 0.75f, cy - r * 0.75f, cx + r * 0.75f, cy + r * 0.75f);
                        canvas.drawArc(rf, -40, 290, false, paint);
                        canvas.drawLine(cx + r * 0.3f, cy - r * 0.8f, cx + r * 0.72f, cy - r * 0.45f, paint);
                        canvas.drawLine(cx + r * 0.72f, cy - r * 0.45f, cx + r * 0.28f, cy - r * 0.3f, paint);
                        break;
                    }
                    case "eye_off": {
                        Path eye = new Path();
                        eye.moveTo(cx - r * 0.88f, cy);
                        eye.quadTo(cx, cy - r * 0.78f, cx + r * 0.88f, cy);
                        eye.quadTo(cx, cy + r * 0.78f, cx - r * 0.88f, cy);
                        canvas.drawPath(eye, paint);
                        canvas.drawCircle(cx, cy, r * 0.26f, paint);
                        canvas.drawLine(cx - r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy + r * 0.78f, paint);
                        break;
                    }
                    case "mark_read": {
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        canvas.drawLine(cx - r * 0.34f, cy + r * 0.02f, cx - r * 0.06f, cy + r * 0.3f, paint);
                        canvas.drawLine(cx - r * 0.06f, cy + r * 0.3f, cx + r * 0.4f, cy - r * 0.24f, paint);
                        break;
                    }
                    case "camera_bolt": {
                        Path bolt = new Path();
                        bolt.moveTo(cx + r * 0.1f, cy - r * 0.85f);
                        bolt.lineTo(cx - r * 0.55f, cy + r * 0.05f);
                        bolt.lineTo(cx + r * 0.05f, cy + r * 0.05f);
                        bolt.lineTo(cx - r * 0.1f, cy + r * 0.85f);
                        bolt.lineTo(cx + r * 0.55f, cy - r * 0.05f);
                        bolt.lineTo(cx - r * 0.05f, cy - r * 0.05f);
                        bolt.close();
                        canvas.drawPath(bolt, paint);
                        break;
                    }
                    case "mic": {
                        RectF cap = new RectF(cx - r * 0.32f, cy - r * 0.85f, cx + r * 0.32f, cy + r * 0.18f);
                        canvas.drawRoundRect(cap, r * 0.32f, r * 0.32f, paint);
                        RectF stand = new RectF(cx - r * 0.62f, cy - r * 0.42f, cx + r * 0.62f, cy + r * 0.48f);
                        canvas.drawArc(stand, 0, 180, false, paint);
                        canvas.drawLine(cx, cy + r * 0.48f, cx, cy + r * 0.85f, paint);
                        canvas.drawLine(cx - r * 0.35f, cy + r * 0.85f, cx + r * 0.35f, cy + r * 0.85f, paint);
                        break;
                    }
                    case "bars": {
                        canvas.drawLine(cx - r * 0.55f, cy - r * 0.35f, cx - r * 0.55f, cy + r * 0.35f, paint);
                        canvas.drawLine(cx - r * 0.18f, cy - r * 0.72f, cx - r * 0.18f, cy + r * 0.72f, paint);
                        canvas.drawLine(cx + r * 0.18f, cy - r * 0.48f, cx + r * 0.18f, cy + r * 0.48f, paint);
                        canvas.drawLine(cx + r * 0.55f, cy - r * 0.25f, cx + r * 0.55f, cy + r * 0.25f, paint);
                        break;
                    }
                    case "megaphone": {
                        Path horn = new Path();
                        horn.moveTo(cx - r * 0.75f, cy - r * 0.2f);
                        horn.lineTo(cx + r * 0.55f, cy - r * 0.72f);
                        horn.lineTo(cx + r * 0.55f, cy + r * 0.58f);
                        horn.lineTo(cx - r * 0.75f, cy + r * 0.18f);
                        horn.close();
                        canvas.drawPath(horn, paint);
                        canvas.drawLine(cx - r * 0.42f, cy + r * 0.22f, cx - r * 0.28f, cy + r * 0.78f, paint);
                        break;
                    }
                    case "analytics": {
                        canvas.drawLine(cx - r * 0.72f, cy + r * 0.45f, cx - r * 0.22f, cy - r * 0.15f, paint);
                        canvas.drawLine(cx - r * 0.22f, cy - r * 0.15f, cx + r * 0.22f, cy + r * 0.2f, paint);
                        canvas.drawLine(cx + r * 0.22f, cy + r * 0.2f, cx + r * 0.72f, cy - r * 0.48f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(cx - r * 0.72f, cy + r * 0.45f, r * 0.16f, paint);
                        canvas.drawCircle(cx - r * 0.22f, cy - r * 0.15f, r * 0.16f, paint);
                        canvas.drawCircle(cx + r * 0.22f, cy + r * 0.2f, r * 0.16f, paint);
                        canvas.drawCircle(cx + r * 0.72f, cy - r * 0.48f, r * 0.16f, paint);
                        break;
                    }
                    case "suggested":
                    case "post_grid": {
                        RectF box = new RectF(cx - r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy + r * 0.78f);
                        canvas.drawRoundRect(box, r * 0.18f, r * 0.18f, paint);
                        canvas.drawLine(cx - r * 0.78f, cy, cx + r * 0.78f, cy, paint);
                        canvas.drawLine(cx, cy - r * 0.78f, cx, cy + r * 0.78f, paint);
                        break;
                    }
                    case "link": {
                        RectF l1 = new RectF(cx - r * 0.85f, cy - r * 0.38f, cx + r * 0.1f, cy + r * 0.38f);
                        RectF l2 = new RectF(cx - r * 0.1f, cy - r * 0.38f, cx + r * 0.85f, cy + r * 0.38f);
                        canvas.drawRoundRect(l1, r * 0.38f, r * 0.38f, paint);
                        canvas.drawRoundRect(l2, r * 0.38f, r * 0.38f, paint);
                        break;
                    }
                    case "external_link": {
                        RectF box = new RectF(cx - r * 0.78f, cy - r * 0.45f, cx + r * 0.45f, cy + r * 0.78f);
                        canvas.drawArc(box, 30, 240, false, paint);
                        canvas.drawLine(cx - r * 0.1f, cy + r * 0.1f, cx + r * 0.78f, cy - r * 0.78f, paint);
                        canvas.drawLine(cx + r * 0.25f, cy - r * 0.78f, cx + r * 0.78f, cy - r * 0.78f, paint);
                        canvas.drawLine(cx + r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy - r * 0.25f, paint);
                        break;
                    }
                    case "shield_check": {
                        Path sh = new Path();
                        sh.moveTo(cx, cy - r * 0.86f);
                        sh.lineTo(cx + r * 0.72f, cy - r * 0.52f);
                        sh.lineTo(cx + r * 0.72f, cy + r * 0.12f);
                        sh.quadTo(cx + r * 0.72f, cy + r * 0.68f, cx, cy + r * 0.88f);
                        sh.quadTo(cx - r * 0.72f, cy + r * 0.68f, cx - r * 0.72f, cy + r * 0.12f);
                        sh.lineTo(cx - r * 0.72f, cy - r * 0.52f);
                        sh.close();
                        canvas.drawPath(sh, paint);
                        canvas.drawLine(cx - r * 0.26f, cy + r * 0.04f, cx - r * 0.05f, cy + r * 0.25f, paint);
                        canvas.drawLine(cx - r * 0.05f, cy + r * 0.25f, cx + r * 0.32f, cy - r * 0.16f, paint);
                        break;
                    }
                    case "heart_off": {
                        Path hp = new Path();
                        hp.moveTo(cx, cy + r * 0.75f);
                        hp.cubicTo(cx - r * 1.1f, cy + r * 0.1f, cx - r * 0.9f, cy - r * 0.85f, cx, cy - r * 0.3f);
                        hp.cubicTo(cx + r * 0.9f, cy - r * 0.85f, cx + r * 1.1f, cy + r * 0.1f, cx, cy + r * 0.75f);
                        canvas.drawPath(hp, paint);
                        canvas.drawLine(cx - r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy + r * 0.78f, paint);
                        break;
                    }
                    case "swipe": {
                        canvas.drawLine(cx - r * 0.78f, cy - r * 0.28f, cx + r * 0.78f, cy - r * 0.28f, paint);
                        canvas.drawLine(cx + r * 0.45f, cy - r * 0.56f, cx + r * 0.78f, cy - r * 0.28f, paint);
                        canvas.drawLine(cx + r * 0.45f, cy, cx + r * 0.78f, cy - r * 0.28f, paint);
                        canvas.drawLine(cx - r * 0.78f, cy + r * 0.28f, cx + r * 0.78f, cy + r * 0.28f, paint);
                        canvas.drawLine(cx - r * 0.45f, cy, cx - r * 0.78f, cy + r * 0.28f, paint);
                        canvas.drawLine(cx - r * 0.45f, cy + r * 0.56f, cx - r * 0.78f, cy + r * 0.28f, paint);
                        break;
                    }
                    case "more_menu": {
                        canvas.drawLine(cx - r * 0.78f, cy - r * 0.48f, cx + r * 0.78f, cy - r * 0.48f, paint);
                        canvas.drawLine(cx - r * 0.78f, cy, cx + r * 0.35f, cy, paint);
                        canvas.drawLine(cx - r * 0.78f, cy + r * 0.48f, cx + r * 0.15f, cy + r * 0.48f, paint);
                        canvas.drawCircle(cx + r * 0.58f, cy + r * 0.38f, r * 0.24f, paint);
                        break;
                    }
                    case "zoom":
                    case "search": {
                        canvas.drawCircle(cx - r * 0.12f, cy - r * 0.12f, r * 0.58f, paint);
                        canvas.drawLine(cx + r * 0.3f, cy + r * 0.3f, cx + r * 0.82f, cy + r * 0.82f, paint);
                        if ("zoom".equals(t)) {
                            canvas.drawLine(cx - r * 0.34f, cy - r * 0.12f, cx + r * 0.1f, cy - r * 0.12f, paint);
                            canvas.drawLine(cx - r * 0.12f, cy - r * 0.34f, cx - r * 0.12f, cy + r * 0.1f, paint);
                        }
                        break;
                    }
                    case "fast_forward": {
                        Path p1 = new Path();
                        p1.moveTo(cx - r * 0.75f, cy - r * 0.52f);
                        p1.lineTo(cx - r * 0.05f, cy);
                        p1.lineTo(cx - r * 0.75f, cy + r * 0.52f);
                        p1.close();
                        canvas.drawPath(p1, paint);
                        Path p2 = new Path();
                        p2.moveTo(cx + r * 0.05f, cy - r * 0.52f);
                        p2.lineTo(cx + r * 0.75f, cy);
                        p2.lineTo(cx + r * 0.05f, cy + r * 0.52f);
                        p2.close();
                        canvas.drawPath(p2, paint);
                        break;
                    }
                    case "video_play": {
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        Path play = new Path();
                        play.moveTo(cx - r * 0.22f, cy - r * 0.36f);
                        play.lineTo(cx + r * 0.36f, cy);
                        play.lineTo(cx - r * 0.22f, cy + r * 0.36f);
                        play.close();
                        canvas.drawPath(play, paint);
                        break;
                    }
                    case "speaker": {
                        Path sp = new Path();
                        sp.moveTo(cx - r * 0.75f, cy - r * 0.25f);
                        sp.lineTo(cx - r * 0.38f, cy - r * 0.25f);
                        sp.lineTo(cx + r * 0.08f, cy - r * 0.65f);
                        sp.lineTo(cx + r * 0.08f, cy + r * 0.65f);
                        sp.lineTo(cx - r * 0.38f, cy + r * 0.25f);
                        sp.lineTo(cx - r * 0.75f, cy + r * 0.25f);
                        sp.close();
                        canvas.drawPath(sp, paint);
                        RectF w1 = new RectF(cx - r * 0.1f, cy - r * 0.45f, cx + r * 0.55f, cy + r * 0.45f);
                        canvas.drawArc(w1, -45, 90, false, paint);
                        break;
                    }
                    case "reels": {
                        RectF cl = new RectF(cx - r * 0.78f, cy - r * 0.72f, cx + r * 0.78f, cy + r * 0.78f);
                        canvas.drawRoundRect(cl, r * 0.22f, r * 0.22f, paint);
                        canvas.drawLine(cx - r * 0.78f, cy - r * 0.22f, cx + r * 0.78f, cy - r * 0.22f, paint);
                        Path tr = new Path();
                        tr.moveTo(cx - r * 0.18f, cy + r * 0.04f);
                        tr.lineTo(cx + r * 0.28f, cy + r * 0.28f);
                        tr.lineTo(cx - r * 0.18f, cy + r * 0.52f);
                        tr.close();
                        canvas.drawPath(tr, paint);
                        break;
                    }
                    case "skip_next": {
                        Path tr = new Path();
                        tr.moveTo(cx - r * 0.68f, cy - r * 0.58f);
                        tr.lineTo(cx + r * 0.22f, cy);
                        tr.lineTo(cx - r * 0.68f, cy + r * 0.58f);
                        tr.close();
                        canvas.drawPath(tr, paint);
                        canvas.drawLine(cx + r * 0.52f, cy - r * 0.58f, cx + r * 0.52f, cy + r * 0.58f, paint);
                        break;
                    }
                    case "story_size": {
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        canvas.drawCircle(cx, cy, r * 0.42f, paint);
                        break;
                    }
                    case "hd_upload": {
                        canvas.drawLine(cx, cy + r * 0.45f, cx, cy - r * 0.65f, paint);
                        canvas.drawLine(cx - r * 0.42f, cy - r * 0.25f, cx, cy - r * 0.68f, paint);
                        canvas.drawLine(cx + r * 0.42f, cy - r * 0.25f, cx, cy - r * 0.68f, paint);
                        canvas.drawLine(cx - r * 0.72f, cy + r * 0.78f, cx + r * 0.72f, cy + r * 0.78f, paint);
                        break;
                    }
                    case "palette": {
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(cx - r * 0.32f, cy - r * 0.22f, r * 0.13f, paint);
                        canvas.drawCircle(cx + r * 0.05f, cy - r * 0.38f, r * 0.13f, paint);
                        canvas.drawCircle(cx + r * 0.36f, cy - r * 0.12f, r * 0.13f, paint);
                        break;
                    }
                    case "font": {
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTextAlign(Paint.Align.CENTER);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                        paint.setTextSize(r * 1.25f);
                        Paint.FontMetrics fm = paint.getFontMetrics();
                        canvas.drawText("Aa", cx, cy - (fm.ascent + fm.descent) / 2f, paint);
                        break;
                    }
                    case "emoji": {
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        RectF smile = new RectF(cx - r * 0.44f, cy - r * 0.28f, cx + r * 0.44f, cy + r * 0.46f);
                        canvas.drawArc(smile, 20, 140, false, paint);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(cx - r * 0.28f, cy - r * 0.2f, r * 0.11f, paint);
                        canvas.drawCircle(cx + r * 0.28f, cy - r * 0.2f, r * 0.11f, paint);
                        break;
                    }
                    case "image": {
                        RectF img = new RectF(cx - r * 0.82f, cy - r * 0.68f, cx + r * 0.82f, cy + r * 0.68f);
                        canvas.drawRoundRect(img, r * 0.18f, r * 0.18f, paint);
                        canvas.drawCircle(cx - r * 0.36f, cy - r * 0.25f, r * 0.16f, paint);
                        Path mt = new Path();
                        mt.moveTo(cx - r * 0.68f, cy + r * 0.58f);
                        mt.lineTo(cx - r * 0.08f, cy - r * 0.05f);
                        mt.lineTo(cx + r * 0.32f, cy + r * 0.32f);
                        mt.lineTo(cx + r * 0.55f, cy + r * 0.12f);
                        mt.lineTo(cx + r * 0.75f, cy + r * 0.58f);
                        canvas.drawPath(mt, paint);
                        break;
                    }
                    case "nav_bar": {
                        RectF bar = new RectF(cx - r * 0.86f, cy - r * 0.42f, cx + r * 0.86f, cy + r * 0.42f);
                        canvas.drawRoundRect(bar, r * 0.24f, r * 0.24f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(cx - r * 0.45f, cy, r * 0.13f, paint);
                        canvas.drawCircle(cx, cy, r * 0.13f, paint);
                        canvas.drawCircle(cx + r * 0.45f, cy, r * 0.13f, paint);
                        break;
                    }
                    case "collapse": {
                        canvas.drawLine(cx - r * 0.78f, cy - r * 0.78f, cx - r * 0.18f, cy - r * 0.18f, paint);
                        canvas.drawLine(cx - r * 0.52f, cy - r * 0.18f, cx - r * 0.18f, cy - r * 0.18f, paint);
                        canvas.drawLine(cx - r * 0.18f, cy - r * 0.52f, cx - r * 0.18f, cy - r * 0.18f, paint);
                        canvas.drawLine(cx + r * 0.78f, cy + r * 0.78f, cx + r * 0.18f, cy + r * 0.18f, paint);
                        canvas.drawLine(cx + r * 0.52f, cy + r * 0.18f, cx + r * 0.18f, cy + r * 0.18f, paint);
                        canvas.drawLine(cx + r * 0.18f, cy + r * 0.52f, cx + r * 0.18f, cy + r * 0.18f, paint);
                        break;
                    }
                    case "comment": {
                        RectF msg = new RectF(cx - r * 0.82f, cy - r * 0.72f, cx + r * 0.82f, cy + r * 0.55f);
                        canvas.drawRoundRect(msg, r * 0.36f, r * 0.36f, paint);
                        canvas.drawLine(cx - r * 0.35f, cy + r * 0.55f, cx - r * 0.62f, cy + r * 0.85f, paint);
                        canvas.drawLine(cx - r * 0.62f, cy + r * 0.85f, cx - r * 0.05f, cy + r * 0.55f, paint);
                        break;
                    }
                    case "trash": {
                        canvas.drawLine(cx - r * 0.75f, cy - r * 0.52f, cx + r * 0.75f, cy - r * 0.52f, paint);
                        canvas.drawLine(cx - r * 0.28f, cy - r * 0.78f, cx + r * 0.28f, cy - r * 0.78f, paint);
                        RectF bin = new RectF(cx - r * 0.56f, cy - r * 0.52f, cx + r * 0.56f, cy + r * 0.82f);
                        canvas.drawRoundRect(bin, r * 0.14f, r * 0.14f, paint);
                        canvas.drawLine(cx - r * 0.18f, cy - r * 0.18f, cx - r * 0.18f, cy + r * 0.48f, paint);
                        canvas.drawLine(cx + r * 0.18f, cy - r * 0.18f, cx + r * 0.18f, cy + r * 0.48f, paint);
                        break;
                    }
                    case "edit_box":
                    case "saved_copies": {
                        RectF box = new RectF(cx - r * 0.78f, cy - r * 0.52f, cx + r * 0.52f, cy + r * 0.78f);
                        canvas.drawRoundRect(box, r * 0.18f, r * 0.18f, paint);
                        canvas.drawLine(cx - r * 0.15f, cy + r * 0.15f, cx + r * 0.78f, cy - r * 0.78f, paint);
                        break;
                    }
                    case "cc_box": {
                        RectF box = new RectF(cx - r * 0.84f, cy - r * 0.62f, cx + r * 0.84f, cy + r * 0.62f);
                        canvas.drawRoundRect(box, r * 0.18f, r * 0.18f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTextAlign(Paint.Align.CENTER);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                        paint.setTextSize(r * 0.78f);
                        Paint.FontMetrics fm = paint.getFontMetrics();
                        canvas.drawText("CC", cx, cy - (fm.ascent + fm.descent) / 2f, paint);
                        break;
                    }
                    case "user_folder": {
                        canvas.drawCircle(cx - r * 0.28f, cy - r * 0.35f, r * 0.34f, paint);
                        RectF arc = new RectF(cx - r * 0.85f, cy + r * 0.08f, cx + r * 0.28f, cy + r * 0.95f);
                        canvas.drawArc(arc, 195, 150, false, paint);
                        canvas.drawCircle(cx + r * 0.45f, cy + r * 0.35f, r * 0.28f, paint);
                        break;
                    }
                    case "pin": {
                        canvas.drawCircle(cx, cy - r * 0.22f, r * 0.26f, paint);
                        Path pin = new Path();
                        pin.moveTo(cx, cy + r * 0.86f);
                        pin.quadTo(cx - r * 0.68f, cy + r * 0.15f, cx - r * 0.68f, cy - r * 0.22f);
                        pin.arcTo(new RectF(cx - r * 0.68f, cy - r * 0.88f, cx + r * 0.68f, cy + r * 0.44f), 180, 180, false);
                        pin.quadTo(cx + r * 0.68f, cy + r * 0.15f, cx, cy + r * 0.86f);
                        canvas.drawPath(pin, paint);
                        break;
                    }
                    case "download":
                    case "download_tray": {
                        canvas.drawLine(cx, cy - r * 0.85f, cx, cy + r * 0.35f, paint);
                        canvas.drawLine(cx - r * 0.45f, cy - r * 0.05f, cx, cy + r * 0.4f, paint);
                        canvas.drawLine(cx + r * 0.45f, cy - r * 0.05f, cx, cy + r * 0.4f, paint);
                        canvas.drawLine(cx - r * 0.75f, cy + r * 0.8f, cx + r * 0.75f, cy + r * 0.8f, paint);
                        break;
                    }
                    case "ghost": {
                        RectF head = new RectF(cx - r * 0.75f, cy - r * 0.85f, cx + r * 0.75f, cy + r * 0.5f);
                        Path p = new Path();
                        p.addArc(head, 180, 180);
                        p.lineTo(cx + r * 0.75f, cy + r * 0.85f);
                        p.lineTo(cx + r * 0.25f, cy + r * 0.55f);
                        p.lineTo(cx - r * 0.25f, cy + r * 0.85f);
                        p.lineTo(cx - r * 0.75f, cy + r * 0.55f);
                        p.close();
                        canvas.drawPath(p, paint);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(cx - r * 0.28f, cy - r * 0.15f, r * 0.12f, paint);
                        canvas.drawCircle(cx + r * 0.28f, cy - r * 0.15f, r * 0.12f, paint);
                        break;
                    }
                    case "bell":
                    case "bell_off": {
                        RectF bell = new RectF(cx - r * 0.65f, cy - r * 0.75f, cx + r * 0.65f, cy + r * 0.35f);
                        canvas.drawArc(bell, 180, 180, false, paint);
                        canvas.drawLine(cx - r * 0.65f, cy - r * 0.2f, cx - r * 0.8f, cy + r * 0.45f, paint);
                        canvas.drawLine(cx + r * 0.65f, cy - r * 0.2f, cx + r * 0.8f, cy + r * 0.45f, paint);
                        canvas.drawLine(cx - r * 0.85f, cy + r * 0.45f, cx + r * 0.85f, cy + r * 0.45f, paint);
                        canvas.drawCircle(cx, cy + r * 0.72f, r * 0.14f, paint);
                        if ("bell_off".equals(t)) {
                            canvas.drawLine(cx - r * 0.85f, cy - r * 0.85f, cx + r * 0.85f, cy + r * 0.85f, paint);
                        }
                        break;
                    }
                    case "shield":
                    case "lock": {
                        RectF body = new RectF(cx - r * 0.7f, cy - r * 0.1f, cx + r * 0.7f, cy + r * 0.85f);
                        canvas.drawRoundRect(body, r * 0.2f, r * 0.2f, paint);
                        RectF shackle = new RectF(cx - r * 0.42f, cy - r * 0.85f, cx + r * 0.42f, cy - r * 0.05f);
                        canvas.drawArc(shackle, 180, 180, false, paint);
                        break;
                    }
                    case "gesture": {
                        canvas.drawCircle(cx, cy - r * 0.55f, r * 0.32f, paint);
                        canvas.drawLine(cx, cy - r * 0.35f, cx, cy + r * 0.75f, paint);
                        canvas.drawLine(cx - r * 0.45f, cy + r * 0.2f, cx + r * 0.45f, cy + r * 0.2f, paint);
                        break;
                    }
                    case "heart":
                    case "heart_filled": {
                        if ("heart_filled".equals(t)) paint.setStyle(Paint.Style.FILL);
                        Path hp = new Path();
                        hp.moveTo(cx, cy + r * 0.75f);
                        hp.cubicTo(cx - r * 1.1f, cy + r * 0.1f, cx - r * 0.9f, cy - r * 0.85f, cx, cy - r * 0.3f);
                        hp.cubicTo(cx + r * 0.9f, cy - r * 0.85f, cx + r * 1.1f, cy + r * 0.1f, cx, cy + r * 0.75f);
                        canvas.drawPath(hp, paint);
                        break;
                    }
                    case "sliders": {
                        canvas.drawLine(cx - r * 0.8f, cy - r * 0.45f, cx + r * 0.8f, cy - r * 0.45f, paint);
                        canvas.drawLine(cx - r * 0.8f, cy + r * 0.45f, cx + r * 0.8f, cy + r * 0.45f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(cx - r * 0.25f, cy - r * 0.45f, r * 0.24f, paint);
                        canvas.drawCircle(cx + r * 0.3f, cy + r * 0.45f, r * 0.24f, paint);
                        break;
                    }
                    case "focus": {
                        RectF phone = new RectF(cx - r * 0.55f, cy - r * 0.88f, cx + r * 0.55f, cy + r * 0.88f);
                        canvas.drawRoundRect(phone, r * 0.18f, r * 0.18f, paint);
                        canvas.drawCircle(cx, cy + r * 0.58f, r * 0.08f, paint);
                        break;
                    }
                    case "translate": {
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTextAlign(Paint.Align.CENTER);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                        paint.setTextSize(r * 1.12f);
                        Paint.FontMetrics fm = paint.getFontMetrics();
                        canvas.drawText("文A", cx, cy - (fm.ascent + fm.descent) / 2f, paint);
                        break;
                    }
                    case "at": {
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTextAlign(Paint.Align.CENTER);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                        paint.setTextSize(r * 1.45f);
                        Paint.FontMetrics fm = paint.getFontMetrics();
                        canvas.drawText("@", cx, cy - (fm.ascent + fm.descent) / 2f, paint);
                        break;
                    }
                    case "doc": {
                        RectF doc = new RectF(cx - r * 0.65f, cy - r * 0.85f, cx + r * 0.65f, cy + r * 0.85f);
                        canvas.drawRoundRect(doc, r * 0.15f, r * 0.15f, paint);
                        canvas.drawLine(cx - r * 0.35f, cy - r * 0.3f, cx + r * 0.35f, cy - r * 0.3f, paint);
                        canvas.drawLine(cx - r * 0.35f, cy + r * 0.05f, cx + r * 0.35f, cy + r * 0.05f, paint);
                        canvas.drawLine(cx - r * 0.35f, cy + r * 0.4f, cx + r * 0.15f, cy + r * 0.4f, paint);
                        break;
                    }
                    case "user":
                    case "people":
                    case "user_plus": {
                        canvas.drawCircle(cx, cy - r * 0.35f, r * 0.36f, paint);
                        RectF arc = new RectF(cx - r * 0.75f, cy + r * 0.12f, cx + r * 0.75f, cy + r * 1.1f);
                        canvas.drawArc(arc, 195, 150, false, paint);
                        if ("user_plus".equals(t)) {
                            canvas.drawLine(cx + r * 0.65f, cy - r * 0.55f, cx + r * 0.65f, cy - r * 0.05f, paint);
                            canvas.drawLine(cx + r * 0.4f, cy - r * 0.3f, cx + r * 0.9f, cy - r * 0.3f, paint);
                        }
                        break;
                    }
                    case "bookmark": {
                        Path bm = new Path();
                        bm.moveTo(cx - r * 0.6f, cy - r * 0.85f);
                        bm.lineTo(cx + r * 0.6f, cy - r * 0.85f);
                        bm.lineTo(cx + r * 0.6f, cy + r * 0.85f);
                        bm.lineTo(cx, cy + r * 0.38f);
                        bm.lineTo(cx - r * 0.6f, cy + r * 0.85f);
                        bm.close();
                        canvas.drawPath(bm, paint);
                        break;
                    }
                    case "id_card": {
                        RectF card = new RectF(cx - r * 0.85f, cy - r * 0.6f, cx + r * 0.85f, cy + r * 0.6f);
                        canvas.drawRoundRect(card, r * 0.16f, r * 0.16f, paint);
                        canvas.drawCircle(cx - r * 0.38f, cy, r * 0.22f, paint);
                        canvas.drawLine(cx + r * 0.05f, cy - r * 0.2f, cx + r * 0.55f, cy - r * 0.2f, paint);
                        canvas.drawLine(cx + r * 0.05f, cy + r * 0.2f, cx + r * 0.45f, cy + r * 0.2f, paint);
                        break;
                    }
                    case "scissors": {
                        canvas.drawCircle(cx - r * 0.45f, cy + r * 0.45f, r * 0.28f, paint);
                        canvas.drawCircle(cx - r * 0.45f, cy - r * 0.45f, r * 0.28f, paint);
                        canvas.drawLine(cx - r * 0.22f, cy - r * 0.3f, cx + r * 0.8f, cy + r * 0.55f, paint);
                        canvas.drawLine(cx - r * 0.22f, cy + r * 0.3f, cx + r * 0.8f, cy - r * 0.55f, paint);
                        break;
                    }
                    case "sun": {
                        canvas.drawCircle(cx, cy, r * 0.42f, paint);
                        for (int i = 0; i < 8; i++) {
                            double a = Math.toRadians(i * 45);
                            canvas.drawLine(
                                    (float) (cx + r * 0.62f * Math.cos(a)),
                                    (float) (cy + r * 0.62f * Math.sin(a)),
                                    (float) (cx + r * 0.88f * Math.cos(a)),
                                    (float) (cy + r * 0.88f * Math.sin(a)),
                                    paint);
                        }
                        break;
                    }
                    case "history": {
                        canvas.drawCircle(cx, cy, r * 0.82f, paint);
                        canvas.drawLine(cx, cy - r * 0.45f, cx, cy, paint);
                        canvas.drawLine(cx, cy, cx + r * 0.38f, cy + r * 0.22f, paint);
                        break;
                    }
                    case "chat_x": {
                        RectF msg = new RectF(cx - r * 0.82f, cy - r * 0.72f, cx + r * 0.82f, cy + r * 0.55f);
                        canvas.drawRoundRect(msg, r * 0.22f, r * 0.22f, paint);
                        canvas.drawLine(cx - r * 0.25f, cy - r * 0.32f, cx + r * 0.25f, cy + r * 0.18f, paint);
                        canvas.drawLine(cx + r * 0.25f, cy - r * 0.32f, cx - r * 0.25f, cy + r * 0.18f, paint);
                        break;
                    }
                    case "folder": {
                        RectF fld = new RectF(cx - r * 0.85f, cy - r * 0.45f, cx + r * 0.85f, cy + r * 0.75f);
                        canvas.drawRoundRect(fld, r * 0.16f, r * 0.16f, paint);
                        canvas.drawLine(cx - r * 0.75f, cy - r * 0.68f, cx - r * 0.1f, cy - r * 0.68f, paint);
                        break;
                    }
                    case "telegram": {
                        Path tg = new Path();
                        tg.moveTo(cx - r * 0.8f, cy + r * 0.05f);
                        tg.lineTo(cx + r * 0.78f, cy - r * 0.68f);
                        tg.lineTo(cx + r * 0.35f, cy + r * 0.75f);
                        tg.lineTo(cx - r * 0.08f, cy + r * 0.25f);
                        tg.close();
                        canvas.drawPath(tg, paint);
                        break;
                    }
                    case "youtube": {
                        paint.setStyle(Paint.Style.FILL);
                        Path play = new Path();
                        play.moveTo(cx - r * 0.45f, cy - r * 0.65f);
                        play.lineTo(cx + r * 0.68f, cy);
                        play.lineTo(cx - r * 0.45f, cy + r * 0.65f);
                        play.close();
                        canvas.drawPath(play, paint);
                        break;
                    }
                    case "github": {
                        canvas.drawCircle(cx, cy, r * 0.78f, paint);
                        canvas.drawCircle(cx, cy - r * 0.05f, r * 0.32f, paint);
                        break;
                    }
                    case "instagram": {
                        RectF ig = new RectF(cx - r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy + r * 0.78f);
                        canvas.drawRoundRect(ig, r * 0.38f, r * 0.38f, paint);
                        canvas.drawCircle(cx, cy, r * 0.36f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(cx + r * 0.44f, cy - r * 0.44f, r * 0.1f, paint);
                        break;
                    }
                    case "chevron_right": {
                        canvas.drawLine(cx - r * 0.25f, cy - r * 0.5f, cx + r * 0.28f, cy, paint);
                        canvas.drawLine(cx + r * 0.28f, cy, cx - r * 0.25f, cy + r * 0.5f, paint);
                        break;
                    }
                    default: {
                        canvas.drawCircle(cx, cy, r * 0.75f, paint);
                        canvas.drawLine(cx, cy - r * 0.25f, cx, cy + r * 0.35f, paint);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(cx, cy - r * 0.45f, r * 0.09f, paint);
                        break;
                    }
                }
            }

            @Override public int getIntrinsicWidth() { return px; }
            @Override public int getIntrinsicHeight() { return px; }
            @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
            @Override public void setColorFilter(ColorFilter cf) { paint.setColorFilter(cf); }
            @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        };
    }
}
