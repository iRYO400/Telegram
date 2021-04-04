package org.telegram.ui.Animation.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Animation.AnimationParams;

import static org.telegram.messenger.AndroidUtilities.dp;

@SuppressLint("ViewConstructor")
public class ChatAnimationEditor extends View
        implements IChatAnimationEditor {

    private AnimationParams params;

    public float duration;

    private final Rect baseRect = new Rect();
    private final Rect controlPoint1Rect = new Rect();
    private final Rect controlPoint2Rect = new Rect();
    private final Rect startDurationRect = new Rect();
    private final Rect endDurationRect = new Rect();

    private final Rect touchAreaRect = new Rect();

    private final int graphHeight = dp(224);
    private final int horizontalMargin = AndroidUtilities.dp(24);
    private final int verticalMargin = AndroidUtilities.dp(32);

    private final float horLineWidth = AndroidUtilities.dp(2);
    private final float pointWidth = AndroidUtilities.dp(2);
    private final float pointRadius = AndroidUtilities.dp(6);

    private final int dotButtonSize = dp(12);
    private final int durationButtonWidth = dp(8);
    private final int durationButtonHeight = dp(20);

    private final int backgroundColor = Theme.getColor(Theme.key_windowBackgroundWhite);

    private final int textValueColor = 0xff6aa8e5;
    private final int textDurationColor = 0xfff7cf46;

    private final float minDurationDelta = 0.1f;

    private float touchPosX;
    private float touchStartDuration;
    private Rect touchRect;

    private boolean isTouching = false;

    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final Path path = new Path();

    private final float[] points = new float[64];

    public ChatAnimationEditor(Context context, AnimationParams params) {
        super(context);
        this.params = params;
        this.duration = params.maxDuration;

        buttonPaint.setColor(backgroundColor);
        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setShadowLayer(2, 0, 0, 0xFF9B9B9B);

        textPaint.setTextSize(dp(12));
        setBackgroundColor(backgroundColor);
    }

    @Override
    public boolean isEditing() {
        return isTouching;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, graphHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutInternal();
    }

    private void layoutInternal() {
        touchAreaRect.set(horizontalMargin, verticalMargin, getWidth() - horizontalMargin, getHeight() - verticalMargin);
        baseRect.set(touchAreaRect.left + Math.round(touchAreaRect.width() * params.startDuration), touchAreaRect.top, touchAreaRect.left + Math.round(touchAreaRect.width() * params.endDuration), touchAreaRect.bottom);

        int durationButtonTop = touchAreaRect.top + touchAreaRect.height() / 2 - durationButtonHeight;
        int durationButtonBottom = touchAreaRect.bottom - touchAreaRect.height() / 2 + durationButtonHeight;

        startDurationRect.set(baseRect.left - durationButtonWidth, durationButtonTop, baseRect.left + durationButtonWidth, durationButtonBottom);
        endDurationRect.set(baseRect.right - durationButtonWidth, durationButtonTop, baseRect.right + durationButtonWidth, durationButtonBottom);

        int controlPoint1 = baseRect.left + Math.round(baseRect.width() * params.cp1);
        controlPoint1Rect.set(controlPoint1 - dotButtonSize, touchAreaRect.bottom - dotButtonSize, controlPoint1 + dotButtonSize, touchAreaRect.bottom + dotButtonSize);

        int controlPoint2 = baseRect.left + Math.round(baseRect.width() * (1 - params.cp2));
        controlPoint2Rect.set(controlPoint2 - dotButtonSize, touchAreaRect.top - dotButtonSize, controlPoint2 + dotButtonSize, touchAreaRect.top + dotButtonSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawEditor(canvas);
        drawButton(canvas, startDurationRect, ButtonType.START_DURATION);
        drawButton(canvas, endDurationRect, ButtonType.END_DURATION);
        drawButton(canvas, controlPoint1Rect, ButtonType.CP_1);
        drawButton(canvas, controlPoint2Rect, ButtonType.CP_2);
    }

    private void drawEditor(Canvas canvas) {
        int left = horizontalMargin;
        int top = verticalMargin;
        int right = getWidth() - horizontalMargin;
        int bottom = getHeight() - verticalMargin;

        int width = right - left;
        int height = bottom - top;

        int durationLeft = left + Math.round(width * params.startDuration);
        int durationRight = left + Math.round(width * params.endDuration);
        int durationWidth = durationRight - durationLeft;

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xffebedf0);
        paint.setStrokeWidth(horLineWidth);

        canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(left, bottom, right, bottom, paint);

        path.reset();
        path.moveTo(durationLeft, bottom);

        float vstep = (float) horLineWidth / durationWidth;

        for (float v = 0; v <= 1; v += vstep) {
            float y = params.interpolate(0, height, v);
            path.lineTo(durationLeft + durationWidth * v, bottom - y);
        }
        path.lineTo(durationRight, top);
        canvas.drawPath(path, paint);

        paint.setColor(textValueColor);
        canvas.drawLine(durationLeft + Math.round(durationWidth * (1 - params.cp2)), top, durationRight, top, paint);
        canvas.drawLine(durationLeft, bottom, durationLeft + Math.round(durationWidth * params.cp1), bottom, paint);

        float step = (height + horLineWidth * 2) / 16;
        int num = 0;
        for (int l = durationLeft; l <= durationRight; l += durationWidth) {
            for (float t = top + step; t < bottom - step; t += step) {
                points[num++] = l;
                points[num++] = t;
            }
        }

        Paint.Cap cap = paint.getStrokeCap();

        paint.setColor(textDurationColor);
        paint.setStrokeWidth(pointWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPoints(points, 0, num, paint);

        num = 0;
        points[num++] = durationLeft;
        points[num++] = top;

        points[num++] = durationRight;
        points[num++] = top;

        points[num++] = durationLeft;
        points[num++] = bottom;

        points[num++] = durationRight;
        points[num++] = bottom;

        paint.setColor(backgroundColor);
        paint.setStrokeWidth(pointRadius * 2);
        canvas.drawPoints(points, 0, num, paint);

        paint.setColor(textDurationColor);
        paint.setStrokeWidth(pointRadius);
        canvas.drawPoints(points, 0, num, paint);

        paint.setStrokeCap(cap);
    }

    private void drawButton(Canvas canvas, Rect rect, ButtonType type) {
        rectF.set(rect);
        canvas.drawRoundRect(rectF, rect.width() / 2f, rect.width() / 2f, buttonPaint);

        float x = rect.left + (rect.width()) / 2f;
        float y = rect.top + (rect.height()) / 2f;

        switch (type) {
            case START_DURATION:
                x += (rect.width() + dp(6)) / 2f;
                y += dp(3);
                break;
            case END_DURATION:
                x = rect.left - dp(40);
                y += dp(3);
                break;
            case CP_1:
                x -= rect.width() / 2f;
                y += rect.height();
                break;
            case CP_2:
                x -= rect.width() / 2f;
                y -= rect.height() / 2f - dp(2);
                break;
        }

        String text = null;
        switch (type) {
            case START_DURATION:
                text = Math.round(duration * params.startDuration) + "ms";
                break;
            case END_DURATION:
                text = Math.round(duration * params.endDuration) + "ms";
                break;
            case CP_1:
                text = Math.round(100 * params.cp1) + "%";
                break;
            case CP_2:
                text = Math.round(100 * params.cp2) + "%";
                break;
        }
        textPaint.setColor((type == ButtonType.START_DURATION || type == ButtonType.END_DURATION) ? textDurationColor : textValueColor);
        canvas.drawText(text, x, y, textPaint);
    }

    private void setStartDuration(float value) {
        value = Math.max(0, Math.min(params.endDuration - minDurationDelta, value));
        if (params.startDuration == value) return;
        params.startDuration = value;
        onChanged();
    }

    private void setEndDuration(float value) {
        value = Math.max(params.startDuration + minDurationDelta, Math.min(1, value));
        if (params.endDuration == value) return;
        params.endDuration = value;
        onChanged();
    }

    private void setStartInterpolation(float value) {
        value = Math.max(0, Math.min(1, value));
        if (params.cp1 == value) return;
        params.cp1 = value;
        onChanged();
    }

    private void setEndInterpolation(float value) {
        value = Math.max(0, Math.min(1, value));
        if (params.cp2 == value) return;
        params.cp2 = value;
        onChanged();
    }

    private void setDuration(float start, float end) {
        if (params.startDuration == start && params.endDuration == end) return;
        params.startDuration = start;
        params.endDuration = end;
        onChanged();
    }

    private void onChanged() {
        layoutInternal();
        invalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getPointerCount() == 1) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isTouching = false;
                    touchRect = null;
                    break;
                case MotionEvent.ACTION_DOWN:
                    int x = (int) e.getX();
                    int y = (int) e.getY();
                    if (startDurationRect.contains(x, y)) {
                        touchRect = startDurationRect;
                    } else if (endDurationRect.contains(x, y)) {
                        touchRect = endDurationRect;
                    } else if (controlPoint1Rect.contains(x, y)) {
                        touchRect = controlPoint1Rect;
                    } else if (controlPoint2Rect.contains(x, y)) {
                        touchRect = controlPoint2Rect;
                    } else if (baseRect.contains(x, y)) {
                        touchRect = baseRect;
                        touchPosX = e.getX();
                        touchStartDuration = params.startDuration;
                    }
                    isTouching = touchRect != null;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (touchRect != null) {
                        if (touchRect == startDurationRect)
                            setStartDuration((e.getX() - touchAreaRect.left) / touchAreaRect.width());
                        else if (touchRect == endDurationRect)
                            setEndDuration((e.getX() - touchAreaRect.left) / touchAreaRect.width());
                        else if (touchRect == controlPoint1Rect)
                            setStartInterpolation((e.getX() - baseRect.left) / baseRect.width());
                        else if (touchRect == controlPoint2Rect)
                            setEndInterpolation(1 - (e.getX() - baseRect.left) / baseRect.width());
                        else if (touchRect == baseRect) {
                            float durDelta = params.endDuration - params.startDuration;
                            float dur = Math.max(0, Math.min(1 - durDelta, touchStartDuration + (e.getX() - touchPosX) / touchAreaRect.width()));
                            setDuration(dur, dur + durDelta);
                        }

                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(e);
    }

    public void setParams(AnimationParams params) {
        this.params = params;
        this.layoutInternal();
        this.invalidate();
    }

    public enum ButtonType {
        START_DURATION,
        END_DURATION,
        CP_1,
        CP_2
    }
}
