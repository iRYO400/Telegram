package org.telegram.ui.Animation;

public class AnimationParams {

    private final static float NOT_FOUND_VAL = -1f;

    public final AnimationParamType type;

    public float maxDuration;
    public float startDuration;
    public float endDuration;
    public float cp1;
    public float cp2;

    public AnimationParams(AnimationParamType type) {
        this.type = type;
        initDefaults(type);
    }

    public AnimationParams(AnimationParamType type, float maxDuration, float startDuration, float endDuration, float cp1, float cp2) {
        this.type = type;
        this.maxDuration = maxDuration;

        if (startDuration == NOT_FOUND_VAL)
            this.startDuration = type.getStartDuration() / maxDuration;
        else
            this.startDuration = startDuration;

        if (endDuration == NOT_FOUND_VAL)
            this.endDuration = type.getEndDuration() / maxDuration;
        else
            this.endDuration = endDuration;

        if (cp1 == NOT_FOUND_VAL)
            this.cp1 = type.getCp1();
        else
            this.cp1 = cp1;

        if (cp2 == NOT_FOUND_VAL)
            this.cp2 = type.getCp2();
        else
            this.cp2 = cp2;
    }

    private void initDefaults(AnimationParamType type) {
        maxDuration = 500;
        startDuration = (type.getStartDuration() / maxDuration);
        endDuration = (type.getEndDuration() / maxDuration);
        cp1 = type.getCp1();
        cp2 = type.getCp2();
    }

    public float getCalculated(float start, float end, float time) {
        if (time >= startDuration && time <= endDuration) {
            float max = endDuration - startDuration;
            float pos = time - startDuration;
            return interpolate(start, end, pos / max);
        }
        if (time < startDuration)
            return start;
        return end;
    }

    public float interpolate(float start, float end, float f) {
        return start + getBezierCoordinateY(getXForTime(f)) * (end - start);
    }

    private static float getBezierCoordinateY(float time) {
        float c = 0.03f;
        float b = 3 * 0.98f - c;
        float a = 1 - c - b;
        return time * (c + time * (b + time * a));
    }

    private float getXForTime(float time) {
        float start = (cp1 - cp1 * 0.01f);
        float end = 1 - (cp2 - cp2 * 0.01f);
        float x = time;
        float z;
        for (int i = 1; i < 14; i++) {
            float c = 3 * start;
            float b = 3 * (end - start) - c;
            float a = 1 - c - b;
            z = x * (c + x * (b + x * a)) - time;
            if (Math.abs(z) < 1e-3)
                break;
            x -= z / (c + x * (2 * b + 3 * a * x));
        }
        return x;
    }

    public String getName() {
        return "_" + type.getName();
    }
}
