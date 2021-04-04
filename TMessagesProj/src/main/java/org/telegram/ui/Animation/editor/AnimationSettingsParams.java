package org.telegram.ui.Animation.editor;

import org.telegram.ui.Animation.AnimationParams;
import org.telegram.ui.Animation.AnimationType;

import java.util.List;

public class AnimationSettingsParams {

    private final AnimationType animationType;
    private final long maxDuration;
    private final List<AnimationParams> animationParamsList;

    public AnimationSettingsParams(AnimationType animationType, long maxDuration, List<AnimationParams> animationParamsList) {
        this.animationType = animationType;
        this.maxDuration = maxDuration;
        this.animationParamsList = animationParamsList;
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public String getMaxDurationText() {
        return maxDuration + "ms";
    }

    public List<AnimationParams> getAnimationParamsList() {
        return animationParamsList;
    }
}
