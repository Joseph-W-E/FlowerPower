package com.example.jellio.flowerpower;

/**
 * Created by jellio on 9/29/16.
 */

public class Pixel {
    private int rgb;
    private int x;
    private int y;

    public Pixel(int rgb, int x, int y) {
        this.rgb = rgb;
        this.x = x;
        this.y = y;
    }

    public int getRgb() {
        return rgb;
    }

    public void setRgb(int rgb) {
        this.rgb = rgb;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
