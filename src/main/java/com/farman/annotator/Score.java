package com.farman.annotator;

public class Score {
    private float precision;
    private float recall;
    private int precisionCount;
    private int recallCount;

    public Score(float precision, float recall, int precisionCount, int recallCount) {
        this.precision = precision;
        this.recall = recall;
        this.precisionCount = precisionCount;
        this.recallCount = recallCount;
    }

    public float getPrecision() {
        return precision;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

    public float getRecall() {
        return recall;
    }

    public void setRecall(float recall) {
        this.recall = recall;
    }

    public int getPrecisionCount() {
        return precisionCount;
    }

    public void setPrecisionCount(int precisionCount) {
        this.precisionCount = precisionCount;
    }

    public int getRecallCount() {
        return recallCount;
    }

    public void setRecallCount(int recallCount) {
        this.recallCount = recallCount;
    }
}
