package org.telegram.ui.Animation;

public class AnimationParam {

    public final AnimationParamType type;

    protected float maxDuration;
    protected float startDuration;
    protected float endDuration;
    protected float cp1;
    protected float cp2;

    public AnimationParam(AnimationParamType type) {
        this.type = type;
        maxDuration = 500; // TODO take it from Saved
        initDefaults(type);
    }

    private void initDefaults(AnimationParamType type) {
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

    private float interpolate(float start, float end, float f) {
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
}
