package com.ls.library_verticaltextview;

public final class WidthAndHeight {
    private float width;
    private float height;

    public WidthAndHeight() {
    }

    public WidthAndHeight(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void setWidthAndHeight(float width, float height){
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "WidthAndHeight{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
