package org.telegram.ui.Animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.view.View;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatActivityEnterView;

import java.util.ArrayList;
import java.util.List;

public class AnimatedMessageCell extends ChatMessageCell {

    private final ChatActivity chatActivity;

    private AnimationType animationType = AnimationType.SMALL_MESSAGE;
    private List<AnimationParam> animationConfigs = new ArrayList<>();
    private List<AnimationParamWrapper> animationParamWrappers = new ArrayList<>();
    private final ValueAnimator animator;

    private float startTextSize;
    private float endTextSize;

    private int startBackgroundColor;
    private int endBackgroundColor;

    private final Rect rootViewRect = new Rect();
    private final Rect messageRect = new Rect();

    private AnimationParamWrapper textPosData;
    private AnimationParamWrapper xPosData;
    private AnimationParamWrapper yPosData;

    private final Paint backgroundPaint = new Paint();

    private Rect clipTextForBigMessage;
    private ChatMessageCell messageCell; //TODO replace to backgroundDrawableRight/Bottom

    public AnimatedMessageCell(Context context, ChatActivity chatActivity) {
        super(context);
        this.chatActivity = chatActivity;
        this.isOnDrawIntercepted = true;

        animator = ObjectAnimator.ofFloat(0, 1);
        animator.addUpdateListener(animation ->
                onUpdateAnimation((float) animation.getAnimatedValue())
        );
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reset();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                onAnimationStarted();
            }
        });

        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    public void stealParams(ChatMessageCell messageCell) {
        this.messageCell = messageCell;
        setMessageObject(messageCell.getMessageObject(), null, messageCell.isPinnedBottom(), messageCell.isPinnedTop());
        this.messageRect.set(messageCell.getLeft(), messageCell.getTop(), messageCell.getRight(), messageCell.getBottom());

        setEnterViewRect(chatActivity.getChatActivityEnterView());

        animator.setDuration(1000); //TODO from settings
        animator.start();
    }

    @Override
    public void setMessageObject(MessageObject messageObject, MessageObject.GroupedMessages groupedMessages, boolean bottomNear, boolean topNear) {
        super.setMessageObject(messageObject, groupedMessages, bottomNear, topNear);
        animationType = defineAnimationType(messageObject);
        animationConfigs = defineAnimationConfigs(animationType);
        animationParamWrappers = defineAnimationDataPoints(animationConfigs);
    }

    private AnimationType defineAnimationType(MessageObject messageObject) {
        switch (messageObject.type) {
            case 15:
                return AnimationType.SINGLE_EMOJI;
            case 5:
                return AnimationType.VOICE_MESSAGE;
            case 2:
                return AnimationType.VIDEO_MESSAGE;
        }
        if (messageObject.linkDescription != null) {
            return AnimationType.LINK_WITH_PREVIEW;
        } else if (messageObject.linesCount > chatActivity.getChatActivityEnterView().getMessageEditText().getMaxLines()) {
            clipTextForBigMessage = new Rect();
            return AnimationType.BIG_MESSAGE;
        } else {
            return AnimationType.SMALL_MESSAGE;
        }
    }

    private List<AnimationParam> defineAnimationConfigs(AnimationType animationType) {
        return animationType.getConfigWrappers();
    }

    private List<AnimationParamWrapper> defineAnimationDataPoints(List<AnimationParam> animationConfigs) {
        List<AnimationParamWrapper> animationParamWrappers = new ArrayList<>();
        for (AnimationParam animationConfig : animationConfigs) {
            animationParamWrappers.add(new AnimationParamWrapper(animationConfig));
        }
        return animationParamWrappers;
    }

    private void setEnterViewRect(ChatActivityEnterView chatActivityEnterView) {
        chatActivityEnterView.takeBounds(rootViewRect);
    }

    private void onAnimationStarted() {
        for (AnimationParamWrapper animationParamWrapper : animationParamWrappers) {
            switch (animationParamWrapper.config.type) {
                case X:
                    xPosData = animationParamWrapper;
                    animationParamWrapper.startValue = chatActivity.getChatActivityEnterView().getMessageEditText().getLeft() - AndroidUtilities.dp(11);
                    animationParamWrapper.endValue = getBackgroundDrawableLeft() + AndroidUtilities.dp(0.25f);
                    break;
                case Y:
                    animationParamWrapper.startValue = rootViewRect.bottom - messageRect.height();
                    animationParamWrapper.endValue = messageRect.top + chatActivity.getChatListView().getTop();
                    break;
                case Y_EXPANDING_BIG_MESSAGE:
                    yPosData = animationParamWrapper;
                    animationParamWrapper.startValue = messageRect.height() - (rootViewRect.bottom - chatActivity.chatActivityEnterViewAnimateFromTop);
                    animationParamWrapper.endValue = 0;
                    break;
                case COLOR_CHANGE:
                    startBackgroundColor = Theme.getColor(Theme.key_windowBackgroundWhite);
                    endBackgroundColor = Theme.getColor(Theme.key_chat_outBubble);
                    animationParamWrapper.startValue = 0;
                    animationParamWrapper.endValue = 1;
                    break;
                case TEXT_SCALE:
                    textPosData = animationParamWrapper;
                    startTextSize = chatActivity.getChatActivityEnterView().getMessageEditText().getTextSize();
                    endTextSize = Theme.chat_msgTextPaint.getTextSize();
                    animationParamWrapper.startValue = 0;
                    animationParamWrapper.endValue = 1;
                    break;
                case TIME_APPEARS:
                    animationParamWrapper.startValue = 0;
                    animationParamWrapper.endValue = 1;
                    break;
            }
        }
        setVisibility(View.VISIBLE);
        onUpdateAnimation(0);
    }

    private void onUpdateAnimation(float animatedValue) {
        if (animatedValue <= 0)
            return;

        int x = getLeft();
        int y = getTop();

        for (AnimationParamWrapper animationParamWrapper : animationParamWrappers) {
            animationParamWrapper.setCurrentValue(animatedValue);
            switch (animationParamWrapper.config.type) {
                case X:
                    x = (int) animationParamWrapper.currentValue;
                    break;
                case Y:
                    y = (int) animationParamWrapper.currentValue;
                    break;
                case COLOR_CHANGE:
                    backgroundPaint.setColor(getColor(startBackgroundColor, endBackgroundColor, animationParamWrapper.currentValue));
                    break;
                case TIME_APPEARS:
                    setTimeAlpha(animationParamWrapper.currentValue);
                    break;
                case TEXT_SCALE:
                    break;
            }
        }
        Log.d("Bootya", "animatedValue " + animatedValue + " x " + x + " y " + y + " messageRect.width() " + messageRect.width() + " messageRect.height() " + messageRect.height());
        layout(0, y, messageRect.width(), y + messageRect.height());
        invalidate();
    }

    @Override
    public void setDrawableBoundsInner(Drawable drawable, int x, int y, int w, int h) {
        if (isOnDrawIntercepted) {
            if (drawable != null) {
                float tempX = x;
                if (xPosData != null) {
                    Log.d("Bootya", "setDrawableBoundsInner, x " + xPosData.toString());
                    tempX = xPosData.currentValue;
                }

                float tempY = y;
                if (yPosData != null) {
                    tempY = yPosData.currentValue;
                    Log.d("Bootya", "setDrawableBoundsInner, y " + y + " yPos " + yPosData.toString());
                }
                drawable.setBounds((int) tempX, (int) tempY, messageCell.getBackgroundDrawableRight(), messageCell.getBackgroundDrawableBottom());
            }
        } else {
            super.setDrawableBoundsInner(drawable, x, y, w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (textPosData != null)
            Theme.chat_msgTextPaint.setTextSize(getValue(startTextSize, endTextSize, textPosData.currentValue));
        super.onDraw(canvas);
        if (textPosData != null)
            Theme.chat_msgTextPaint.setTextSize(endTextSize);
    }

    @Override
    protected void canvasDrawTextBlock(Canvas canvas, StaticLayout textLayout) {
        if (yPosData != null) {
            clipTextForBigMessage.set(messageRect.left, messageRect.top, messageRect.right, (int) yPosData.currentValue - AndroidUtilities.dp(6.5f));
            canvas.clipRect(clipTextForBigMessage);
            textLayout.draw(canvas);
        }
    }

    @Override
    protected void drawBackgroundDrawable(Canvas canvas) {
        getCurrentBackgroundDrawable().draw(canvas, backgroundPaint);
    }

    public static int getValue(int start, int end, float f) {
        return Math.round(start * (1 - f) + end * f);
    }

    public static float getValue(float start, float end, float f) {
        return start * (1 - f) + end * f;
    }

    private void reset() {
        layout(0, 0, 0, 0);
        textPosData = null;
        xPosData = null;
        yPosData = null;
        setVisibility(View.GONE);
    }

    public static int getColor(int start, int end, float value) {
        float tempValue = Math.max(Math.min(value, 1), 0);
        float f2 = 1.0f - tempValue;
        return Color.argb(
                (int) (Color.alpha(end) * tempValue + Color.alpha(start) * f2),
                (int) (Color.red(end) * tempValue + Color.red(start) * f2),
                (int) (Color.green(end) * tempValue + Color.green(start) * f2),
                (int) (Color.blue(end) * tempValue + Color.blue(start) * f2)
        );
    }
}
