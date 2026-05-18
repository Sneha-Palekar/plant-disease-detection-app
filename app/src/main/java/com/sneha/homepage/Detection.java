package com.sneha.homepage;

import android.graphics.RectF;

public class Detection {
    private RectF boundingBox;
    private float confidence;
    private int classId;
    private String className;

    public Detection(RectF boundingBox, float confidence, int classId, String className) {
        this.boundingBox = boundingBox;
        this.confidence = confidence;
        this.classId = classId;
        this.className = className;
    }

    public RectF getBoundingBox() { return boundingBox; }
    public float getConfidence() { return confidence; }
    public int getClassId() { return classId; }
    public String getClassName() { return className; }
}