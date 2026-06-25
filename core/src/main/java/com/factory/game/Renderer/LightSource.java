package com.factory.game.Renderer;

import com.badlogic.gdx.graphics.Color;

import box2dLight.ConeLight;
import box2dLight.Light;


public class LightSource {

    private final Light light;

    private final Color cachedColor  = new Color();
    private       float cachedRadius;

    public LightSource(float worldPixelX, float worldPixelY,
                       Light light, Color color, float distancePx) {
        this.light        = light;
        this.cachedColor.set(color);
        this.cachedRadius = distancePx;
    }

    public void setWorldPosition(float worldPixelX, float worldPixelY) {
        light.setPosition(worldPixelX, worldPixelY);
    }

    public float getWorldPixelX() { return light.getX(); }
    public float getWorldPixelY() { return light.getY(); }

    public Color getColor()  { return cachedColor; }
    public float getRadius() { return cachedRadius; }

    public void setColor(Color color) {
        cachedColor.set(color);
        light.setColor(color);
    }

    public void setDistance(float distancePx) {
        cachedRadius = distancePx;
        light.setDistance(distancePx);
    }

    public void setActive(boolean active) { light.setActive(active); }

    public void setConeDirection(float degrees) {
        if (light instanceof ConeLight) {
            ((ConeLight) light).setDirection(degrees);
        }
    }

    void remove() { light.remove(); }
}