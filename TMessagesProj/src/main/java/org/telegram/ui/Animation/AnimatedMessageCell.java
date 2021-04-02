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

import static org.telegram.messenger.AndroidUtilities.dp;

public class AnimatedMessageCell extends ChatMessageCell {

    private final ChatActivity chatActivity;

    public boolean isAnimating = false;
    private AnimationType animationType;
    private List<AnimationParams> animationConfigs = new ArrayList<>();
    private List<ParamsEvaluator> paramsEvaluators = new ArrayList<>();
    private final ValueAnimator animator;

    private float startTextSize;
    private float endTextSize;

    private int startBackgroundColor;
    private int endBackgroundColor;
    private int startBackgroundShadowColor;
    private int endBackgroundShadowColor;

    private int startReplyNameColor;
    private int endReplyNameColor;
    private int currentReplyNameColor;

    private int startReplyTextColor;
    private int endReplyTextColor;
    private int currentReplyTextColor;

    private int startReplyLineColor;
    private int endReplyLineColor;
    private int currentReplyLineColor;

    private final Rect rootViewRect = new Rect();
    private final Rect messageRect = new Rect();

    private ParamsEvaluator scaleTextEvaluator;
    private ParamsEvaluator leftBackgroundEvaluator;
    private ParamsEvaluator xReplyEvaluator;
    private ParamsEvaluator yReplyEvaluator;
    private ParamsEvaluator topBackgroundEvaluator;

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int yOffset = 0;

    private boolean isReply = false;

    private Rect clipTextForBigMessage;
    private ChatMessageCell messageCell;
    private final int[] messageCellPosition = new int[2];

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

    public boolean stealParams(ChatMessageCell messageCell) {
        animationType = defineAnimationType(messageCell.getMessageObject());
        if (animationType == AnimationType.BIG_MESSAGE || animationType == AnimationType.SMALL_MESSAGE || animationType == AnimationType.SINGLE_EMOJI) {
            this.isAnimating = true;
            this.messageCell = messageCell;
            this.chatActivity.getChatListItemAnimator().setAdditionalMoveDuration(1000); //TODO from Settings
            setMessageObject(messageCell.getMessageObject(), null, messageCell.isPinnedBottom(), messageCell.isPinnedTop());
            this.messageRect.set(messageCell.getLeft(), messageCell.getTop(), messageCell.getRight(), messageCell.getBottom());

            if (messageCell.getMessageObject().replyMessageObject != null) {
                isReply = true;
                yOffset = chatActivity.getChatActivityEnterView().getTopViewHeight();
            }
            setEnterViewRect(chatActivity.getChatActivityEnterView());

            animator.setDuration(1000); //TODO from settings
            animator.start();
            return true;
        } else {
            reset();
            return false;
        }
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

    @Override
    public void setMessageObject(MessageObject messageObject, MessageObject.GroupedMessages groupedMessages, boolean bottomNear, boolean topNear) {
        super.setMessageObject(messageObject, groupedMessages, bottomNear, topNear);
        animationConfigs = defineAnimationConfigs(animationType);
        paramsEvaluators = defineAnimationDataPoints(animationConfigs);
    }

    private List<AnimationParams> defineAnimationConfigs(AnimationType animationType) {
        return animationType.getConfigWrappers();
    }

    private List<ParamsEvaluator> defineAnimationDataPoints(List<AnimationParams> animationConfigs) {
        List<ParamsEvaluator> paramsEvaluators = new ArrayList<>();
        for (AnimationParams animationConfig : animationConfigs) {
            paramsEvaluators.add(new ParamsEvaluator(animationConfig));
        }
        return paramsEvaluators;
    }

    private void setEnterViewRect(ChatActivityEnterView chatActivityEnterView) {
        chatActivityEnterView.takeBounds(rootViewRect);
    }

    private void onAnimationStarted() {
        for (ParamsEvaluator paramsEvaluator : paramsEvaluators) {
            switch (paramsEvaluator.params.type) {
                case X:
                    paramsEvaluator.startValue = chatActivity.getChatActivityEnterView().getMessageEditText().getLeft() - dp(11);
                    paramsEvaluator.endValue = getBackgroundDrawableLeft() + dp(11) + getExtraTextX();
                    break;
                case Y:
                    paramsEvaluator.startValue = rootViewRect.bottom - messageRect.height();
                    paramsEvaluator.endValue = messageCellPosition[1];
//                    paramsEvaluator.endValue = messageRect.top + chatActivity.getChatListView().getTop() + yOffset;
                    break;
                case X_REPLY:
                    xReplyEvaluator = paramsEvaluator;
                    paramsEvaluator.startValue = chatActivity.getChatActivityEnterView().getMessageEditText().getLeft() - dp(11);
                    paramsEvaluator.endValue = getBackgroundDrawableLeft() + dp(11) + getExtraTextX();
                    break;
                case Y_REPLY: // used only if isReply == true
                    yReplyEvaluator = paramsEvaluator;
                    if (animationType == AnimationType.BIG_MESSAGE) { // reply with BM
                        paramsEvaluator.startValue = messageRect.height() - (rootViewRect.bottom - chatActivity.chatActivityEnterViewAnimateFromTop) - dp(40);
                        paramsEvaluator.endValue = replyStartY;
                    } else { // reply with SM
                        if (replyStartY <= 0)
                            paramsEvaluator.startValue = replyStartY;
                        else
                            paramsEvaluator.startValue = replyStartY - dp(16);
                        paramsEvaluator.endValue = replyStartY;
                    }
                    break;
                case LEFT_BACKGROUND:
                    leftBackgroundEvaluator = paramsEvaluator;
                    paramsEvaluator.startValue = chatActivity.getChatActivityEnterView().getMessageEditText().getLeft() - dp(11);
                    paramsEvaluator.endValue = getBackgroundDrawableLeft();
                    break;
                case TOP_BACKGROUND:
                    topBackgroundEvaluator = paramsEvaluator;
                    if (animationType == AnimationType.BIG_MESSAGE && !isReply) { // BM only
                        paramsEvaluator.startValue = messageRect.height() - (rootViewRect.bottom - chatActivity.chatActivityEnterViewAnimateFromTop);
                        paramsEvaluator.endValue = 0;
                    } else if (animationType != AnimationType.BIG_MESSAGE && isReply) { // reply with SM
                        paramsEvaluator.startValue = -dp(12);
                        paramsEvaluator.endValue = 0;
                    } else if (animationType == AnimationType.BIG_MESSAGE) { // reply with BM
                        paramsEvaluator.startValue = messageRect.height() - (rootViewRect.bottom - chatActivity.chatActivityEnterViewAnimateFromTop) - dp(48);
                        paramsEvaluator.endValue = 0;
                    }
                    break;
                case COLOR_CHANGE:
                    startBackgroundColor = Theme.getColor(Theme.key_windowBackgroundWhite);
                    endBackgroundColor = Theme.getColor(Theme.key_chat_outBubble);
                    startBackgroundShadowColor = Theme.getColor(Theme.key_windowBackgroundWhite);
                    endBackgroundShadowColor = 0xFF9B9B9B;
                    if (isReply) {
                        startReplyNameColor = Theme.getColor(Theme.key_chat_replyPanelName);
                        startReplyTextColor = Theme.getColor(Theme.key_chat_replyPanelMessage);
                        startReplyLineColor = 0x009B9B9B;

                        endReplyNameColor = Theme.getColor(Theme.key_chat_outReplyNameText);
                        endReplyTextColor = Theme.getColor(Theme.key_chat_outReplyMessageText);
                        endReplyLineColor = Theme.getColor(Theme.key_chat_outReplyLine);
                    }
                    paramsEvaluator.startValue = 0;
                    paramsEvaluator.endValue = 1;
                    break;
                case TEXT_SCALE:
                    scaleTextEvaluator = paramsEvaluator;
                    startTextSize = chatActivity.getChatActivityEnterView().getMessageEditText().getTextSize();
                    endTextSize = Theme.chat_msgTextPaint.getTextSize();
                    paramsEvaluator.startValue = 0;
                    paramsEvaluator.endValue = 1;
                    break;
                case TIME_APPEARS:
                    paramsEvaluator.startValue = 0;
                    paramsEvaluator.endValue = 1;
                    break;
            }
        }
    }

    private void onUpdateAnimation(float animatedValue) {
        if (animatedValue <= 0)
            return;
        int x = getLeft();
        int y = getTop();
        setMessageCellPosition();
        for (ParamsEvaluator paramsEvaluator : paramsEvaluators) {
            paramsEvaluator.setCurrentValue(animatedValue);
            switch (paramsEvaluator.params.type) {
                case X:
                    x = (int) paramsEvaluator.currentValue;
                    break;
                case Y:
                    paramsEvaluator.endValue = messageCellPosition[1];
                    y = (int) paramsEvaluator.currentValue;
                    break;
                case LEFT_BACKGROUND:
                    paramsEvaluator.endValue = getBackgroundDrawableLeft() + dp(0.25f);
                    break;
                case X_REPLY:
                    paramsEvaluator.endValue = getBackgroundDrawableLeft() + dp(11) + getExtraTextX();
                    break;
                case Y_REPLY:
                    paramsEvaluator.endValue = replyStartY;
                    break;
                case COLOR_CHANGE:
                    backgroundPaint.setColor(getColor(startBackgroundColor, endBackgroundColor, paramsEvaluator.currentValue));
                    backgroundPaint.setShadowLayer(1, 0, 1, getColor(startBackgroundShadowColor, endBackgroundShadowColor, paramsEvaluator.currentValue));
                    if (isReply) {
                        currentReplyNameColor = getColor(startReplyNameColor, endReplyNameColor, paramsEvaluator.currentValue);
                        currentReplyTextColor = getColor(startReplyTextColor, endReplyTextColor, paramsEvaluator.currentValue);
                        currentReplyLineColor = getColor(startReplyLineColor, endReplyLineColor, paramsEvaluator.currentValue);
                    }
                    break;
                case TIME_APPEARS:
                    setTimeAlpha(paramsEvaluator.currentValue);
                    break;
                case TEXT_SCALE:
                    break;
            }
        }
        Log.d("Bootya", "animatedValue " + animatedValue + " x: " + x + " y: " + y + " messageRect.width() " + messageRect.width() + " messageRect.height() " + messageRect.height());
        layout(0, y, messageRect.width(), y + messageRect.height());
        if (getVisibility() != View.VISIBLE)
            setVisibility(View.VISIBLE);
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void setDrawableBoundsInner(Drawable drawable, int x, int y, int w, int h) {
        if (isOnDrawIntercepted && (animationType == AnimationType.SMALL_MESSAGE || animationType == AnimationType.BIG_MESSAGE)) {
            if (drawable != null) {
                float tempX = x;
                if (leftBackgroundEvaluator != null) {
                    tempX = leftBackgroundEvaluator.currentValue;
                }

                float tempY = y;
                if (topBackgroundEvaluator != null) {
                    if (animationType == AnimationType.BIG_MESSAGE && !isReply) // BM only
                        tempY = topBackgroundEvaluator.currentValue;
                    else if (animationType != AnimationType.BIG_MESSAGE && isReply) // reply with SM
                        tempY = topBackgroundEvaluator.currentValue;
                    else if (animationType == AnimationType.BIG_MESSAGE) // reply with BM
                        tempY = topBackgroundEvaluator.currentValue;
                }
                drawable.setBounds((int) tempX, (int) tempY, messageCell.getBackgroundDrawableRight(), messageCell.getBackgroundDrawableBottom());
            }
        } else {
            super.setDrawableBoundsInner(drawable, x, y, w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (scaleTextEvaluator != null)
            Theme.chat_msgTextPaint.setTextSize(getValue((int) startTextSize, (int) endTextSize, (int) scaleTextEvaluator.currentValue));
        super.onDraw(canvas);
        if (scaleTextEvaluator != null)
            Theme.chat_msgTextPaint.setTextSize(endTextSize);
    }

    @Override
    protected void canvasDrawTimeLayout(Canvas canvas, StaticLayout timeLayout, float drawTimeX, float drawTimeY) {
        canvas.translate(drawTimeX, messageRect.height() - timeLayout.getHeight() - AndroidUtilities.dp(isPinnedBottom() || isPinnedTop() ? 7.5f : 6.5f));
        timeLayout.draw(canvas);
    }

    @Override
    protected void canvasDrawTextBlock(Canvas canvas, StaticLayout textLayout) {
        if (animationType == AnimationType.BIG_MESSAGE) {
            if (topBackgroundEvaluator != null) {
                if (!isReply) {
                    clipTextForBigMessage.set(messageRect.left, messageRect.top, messageRect.right, (int) topBackgroundEvaluator.currentValue - dp(6.5f));
                    canvas.clipRect(clipTextForBigMessage);
                } else {
                    clipTextForBigMessage.set(messageRect.left, messageRect.top, messageRect.right, (int) topBackgroundEvaluator.currentValue - dp(6.5f));
                    canvas.clipRect(clipTextForBigMessage);
                }
            }
        }
        textLayout.draw(canvas);
    }

    @Override
    protected void canvasDrawReplyLine(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        float tempLeft = left;
        float tempRight = right;
        if (leftBackgroundEvaluator != null) {
            tempLeft = leftBackgroundEvaluator.currentValue + dp(12);
            tempRight = tempLeft + dp(2);
            Theme.chat_replyLinePaint.setColor(currentReplyLineColor);
        }
        super.canvasDrawReplyLine(canvas, tempLeft, top, tempRight, bottom, paint);
    }

    @Override
    protected void canvasDrawReplayName(Canvas canvas, StaticLayout replyNameLayout, float dx, float dy) {
        float tempX = dx;
        float tempY = dy;
        if (xReplyEvaluator != null) {
            tempX = xReplyEvaluator.currentValue + dp(11 + (needReplyImage ? 44 : 0));
            Theme.chat_replyNamePaint.setColor(currentReplyNameColor);
        }
        if (yReplyEvaluator != null) {
            tempY = yReplyEvaluator.currentValue;
        }
        super.canvasDrawReplayName(canvas, replyNameLayout, tempX, tempY);
    }

    @Override
    protected void canvasDrawReplyText(Canvas canvas, StaticLayout replyTextLayout, float dx, float dy) {
        float tempX = dx;
        float tempY = dy;
        if (xReplyEvaluator != null) {
            tempX = xReplyEvaluator.currentValue + dp(11 + (needReplyImage ? 44 : 0));
            Theme.chat_replyTextPaint.setColor(currentReplyTextColor);
        }
        if (yReplyEvaluator != null) {
            tempY = yReplyEvaluator.currentValue + dp(19);
        }
        super.canvasDrawReplyText(canvas, replyTextLayout, tempX, tempY);
    }

    @Override
    protected void drawBackgroundDrawable(Canvas canvas) {
        getCurrentBackgroundDrawable().draw(canvas, backgroundPaint);
    }

    public static int getValue(int start, int end, float f) {
        return Math.round(start * (1 - f) + end * f);
    }

    private void reset() {
        setVisibility(View.GONE);
        layout(0, 0, 0, 0);
        isAnimating = false;
        isReply = false;
        scaleTextEvaluator = null;
        leftBackgroundEvaluator = null;
        topBackgroundEvaluator = null;
        xReplyEvaluator = null;
        yReplyEvaluator = null;
        yOffset = 0;
    }

    private void setMessageCellPosition() {
        if (messageCell != null)
            messageCell.getLocationOnScreen(messageCellPosition);
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
