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

    public List<AnimationParams> getConfigWrappers() {
        List<AnimationParams> configTypes = new ArrayList<>();
        if (this == SMALL_MESSAGE || this == BIG_MESSAGE) {
            configTypes.add(new AnimationParams(AnimationParamType.X));
            configTypes.add(new AnimationParams(AnimationParamType.Y));

            configTypes.add(new AnimationParams(AnimationParamType.X_REPLY));
            configTypes.add(new AnimationParams(AnimationParamType.Y_REPLY));

            configTypes.add(new AnimationParams(AnimationParamType.LEFT_BACKGROUND));
            configTypes.add(new AnimationParams(AnimationParamType.TOP_BACKGROUND));

            configTypes.add(new AnimationParams(AnimationParamType.TEXT_SCALE));

            configTypes.add(new AnimationParams(AnimationParamType.COLOR_CHANGE));

            configTypes.add(new AnimationParams(AnimationParamType.TIME_APPEARS));
        } else if (this == SINGLE_EMOJI) {

            configTypes.add(new AnimationParams(AnimationParamType.Y));
            configTypes.add(new AnimationParams(AnimationParamType.EMOJI_SCALE));
            configTypes.add(new AnimationParams(AnimationParamType.TIME_APPEARS));
        }

        return configTypes;
    }
}
