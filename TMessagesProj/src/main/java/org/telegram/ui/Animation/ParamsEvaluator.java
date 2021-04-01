package org.telegram.ui.Animation;

public class ParamsEvaluator {

    public final AnimationParams params;

    public float startValue;
    public float endValue;
    public float currentValue;

    public ParamsEvaluator(AnimationParams params) {
        this.params = params;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = params.getCalculated(startValue, endValue, currentValue);
    }

    @Override
    public String toString() {
        return "{" +
                "config=" + params.type +
                ", startValue=" + startValue +
                ", endValue=" + endValue +
                ", currentValue=" + currentValue +
                '}';
    }
}