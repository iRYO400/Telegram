package org.telegram.ui.Animation;

public class AnimationParamWrapper {

    public final AnimationParam config;

    public float startValue;
    public float endValue;
    public float currentValue;

    public AnimationParamWrapper(AnimationParam config) {
        this.config = config;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = config.getCalculated(startValue, endValue, currentValue);
    }

    @Override
    public String toString() {
        return "{" +
                "config=" + config.type +
                ", startValue=" + startValue +
                ", endValue=" + endValue +
                ", currentValue=" + currentValue +
                '}';
    }
}