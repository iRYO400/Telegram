package org.telegram.ui.Animation;

import java.util.ArrayList;
import java.util.List;

public enum AnimationType {
    SMALL_MESSAGE("Small message"),
    BIG_MESSAGE("Big message"),
    LINK_WITH_PREVIEW("Link with preview"),
    SINGLE_EMOJI("Single emoji"),
    STICKER("Sticker"),
    VOICE_MESSAGE("Voice message"),
    VIDEO_MESSAGE("Video message");

    private final String name;

    AnimationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<AnimationParam> getConfigWrappers() {
        List<AnimationParam> configTypes = new ArrayList<>();
        if (this == SMALL_MESSAGE) {
            configTypes.add(new AnimationParam(AnimationParamType.X));
            configTypes.add(new AnimationParam(AnimationParamType.Y));
            configTypes.add(new AnimationParam(AnimationParamType.BUBBLE_SHAPE));
            configTypes.add(new AnimationParam(AnimationParamType.TEXT_SCALE));
            configTypes.add(new AnimationParam(AnimationParamType.COLOR_CHANGE));
            configTypes.add(new AnimationParam(AnimationParamType.TIME_APPEARS));
        }

        return configTypes;
    }
}
