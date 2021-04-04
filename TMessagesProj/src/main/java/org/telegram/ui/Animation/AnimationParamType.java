package org.telegram.ui.Animation;

public enum AnimationParamType {
    X("X Position", 0, 250, 1, .33f, true),
    Y("Y Position", 0, 250, 1, .33f, true),

    X_REPLY("X of Reply", X.startDuration, X.endDuration, X.cp1, X.cp2, false),
    Y_REPLY("Y of Reply", Y.startDuration, Y.endDuration, Y.cp1, Y.cp2, false),

    LEFT_BACKGROUND("Left Background", X.startDuration, X.endDuration, X.cp1, X.cp2, false),
    TOP_BACKGROUND("Top background", Y.startDuration, Y.endDuration, Y.cp1, Y.cp2, false),

    BUBBLE_SHAPE("Bubble shape", 0, 250, 1, .33f, false),

    TEXT_SCALE("Text scale", 0, 250, 1, .33f, true),

    COLOR_CHANGE("Color change", 0, 250, 1, .33f, true),

    TIME_APPEARS("Time appears", 0, 500, 1, .33f, true),

    EMOJI_SCALE("Emoji Appears", 100, 250, 1, .33f, true);

    private final String name;
    private final long startDuration;
    private final long endDuration;
    private final float cp1;
    private final float cp2;
    private final boolean isEditable;

    AnimationParamType(String name, long startDuration, long endDuration, float cp1, float cp2, boolean isEditable) {
        this.name = name;
        this.startDuration = startDuration;
        this.endDuration = endDuration;
        this.cp1 = cp1;
        this.cp2 = cp2;
        this.isEditable = isEditable;
    }

    public String getName() {
        return name;
    }

    public long getStartDuration() {
        return startDuration;
    }

    public long getEndDuration() {
        return endDuration;
    }

    public float getCp1() {
        return cp1;
    }

    public float getCp2() {
        return cp2;
    }

    public boolean isEditable() {
        return isEditable;
    }
}
