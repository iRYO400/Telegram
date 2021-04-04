package org.telegram.ui.Animation;

import java.util.ArrayList;
import java.util.List;

public enum AnimationType {
    SMALL_MESSAGE("Short Text"),
    BIG_MESSAGE("Long Text"),
    LINK_WITH_PREVIEW("Link"),
    SINGLE_EMOJI("Emoji");
//    VOICE_MESSAGE("Voice message"),
//    VIDEO_MESSAGE("Video message");

    private final String name;

    AnimationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
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

            configTypes.add(new AnimationParams(AnimationParamType.X));
            configTypes.add(new AnimationParams(AnimationParamType.Y));

            configTypes.add(new AnimationParams(AnimationParamType.EMOJI_SCALE));

            configTypes.add(new AnimationParams(AnimationParamType.TIME_APPEARS));
        }

        return configTypes;
    }

    public List<AnimationParams> getConfigWrappersForSettings() {
        List<AnimationParams> configTypes = new ArrayList<>();
        if (this == SMALL_MESSAGE || this == BIG_MESSAGE) {
            configTypes.add(new AnimationParams(AnimationParamType.X));
            configTypes.add(new AnimationParams(AnimationParamType.Y));

            configTypes.add(new AnimationParams(AnimationParamType.TEXT_SCALE));

            configTypes.add(new AnimationParams(AnimationParamType.COLOR_CHANGE));

            configTypes.add(new AnimationParams(AnimationParamType.TIME_APPEARS));
        } else if (this == SINGLE_EMOJI) {

            configTypes.add(new AnimationParams(AnimationParamType.X));
            configTypes.add(new AnimationParams(AnimationParamType.Y));

            configTypes.add(new AnimationParams(AnimationParamType.EMOJI_SCALE));

            configTypes.add(new AnimationParams(AnimationParamType.TIME_APPEARS));
        }

        return configTypes;
    }
}
