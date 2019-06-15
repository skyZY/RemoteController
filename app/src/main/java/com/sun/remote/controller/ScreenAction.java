package com.sun.remote.controller;

/*
 * =====================================================================================
 * Summary:
 *
 * File: ScreenAction.java
 * Author: Yanpeng.Sun
 * Create: 2019/6/15 14:16
 * =====================================================================================
 */
public class ScreenAction{

    public ScreenSize screen;
    public Mouse mouse;

    public ScreenSize getScreen() {
        return screen;
    }

    public void setScreen(ScreenSize screen) {
        this.screen = screen;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

    @Override
    public String toString() {
        return "ScreenAction{" +
                "screen=" + screen +
                ", mouse=" + mouse +
                '}';
    }

    class ScreenSize{
        public int width;

        public int height;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public String toString() {
            return "ScreenSize{" +
                    "width=" + width +
                    ", height=" + height +
                    '}';
        }
    }

    class Mouse{

        public int keys;

        public int startLocationX;

        public int startLocationY;

        public int getKeys() {
            return keys;
        }

        public void setKeys(int keys) {
            this.keys = keys;
        }

        public int getStartLocationX() {
            return startLocationX;
        }

        public void setStartLocationX(int startLocationX) {
            this.startLocationX = startLocationX;
        }

        public int getStartLocationY() {
            return startLocationY;
        }

        public void setStartLocationY(int startLocationY) {
            this.startLocationY = startLocationY;
        }

        @Override
        public String toString() {
            return "Mouse{" +
                    "keys=" + keys +
                    ", startLocationX=" + startLocationX +
                    ", startLocationY=" + startLocationY +
                    '}';
        }
    }
}
