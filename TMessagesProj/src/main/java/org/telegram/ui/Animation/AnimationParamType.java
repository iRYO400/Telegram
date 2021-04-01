package org.telegram.ui.Animation;

public enum AnimationParamType {
    X("X Position", 0, 500, 1, .33f),
    Y("Y Position", 0, 500, 1, .33f),
    Y_EXPANDING_BIG_MESSAGE("Depend on Y Position", Y.startDuration, Y.endDuration, Y.cp1, Y.cp2),
    BUBBLE_SHAPE("Bubble shape", 0, 250, 1, .33f),
    TEXT_SCALE("Text scale", 0, 250, 1, .33f),
    COLOR_CHANGE("Color change", 0, 250, 1, .33f),
    TIME_APPEARS("Time appears", 0, 500, 1, .33f);

    private final String name;
    private final long startDuration;
    private final long endDuration;
    private final float cp1;
    private final float cp2;

    AnimationParamType(String name, long startDuration, long endDuration, float cp1, float cp2) {
        this.name = name;
        this.startDuration = startDuration;
        this.endDuration = endDuration;
        this.cp1 = cp1;
        this.cp2 = cp2;
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
}
