package com.factory.game.Renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.factory.game.Camera;

import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;


public class LightRenderer {

    private final World      b2World;
    private final RayHandler rayHandler;

    private DirectionalLight        sunLight;
    private final List<LightSource> lights   = new ArrayList<>();
    private final Matrix4           combined = new Matrix4();


    public LightRenderer(int width, int height) {
        b2World    = new World(new Vector2(0, 0), true);
        rayHandler = new RayHandler(b2World, width / 4, height / 4);
        
        rayHandler.setBlurNum(1);
        
        RayHandler.useDiffuseLight(true);
        
        rayHandler.setAmbientLight(DayNightCycle.DEFAULT_AMBIENT);
        addSunLight(DayNightCycle.DEFAULT_SUN_COLOR, DayNightCycle.DEFAULT_SUN_ANGLE);
    }

    public World getWorld() { return b2World; }


    public void setSunAmbient(Color color) { rayHandler.setAmbientLight(color); }
    public void setSunAmbient(float r, float g, float b, float a) { rayHandler.setAmbientLight(r, g, b, a); }

    public void setSunLight(Color color, float angle) {
        if (sunLight != null) { sunLight.setColor(color); sunLight.setDirection(angle); }
        else                  { addSunLight(color, angle); }
    }

    public void removeSunLight() {
        if (sunLight != null) { sunLight.remove(); sunLight = null; }
    }


    public LightSource addPointLight(float worldPixelX, float worldPixelY,
                                     Color color, float distancePx, int rays) {
        PointLight pl = new PointLight(rayHandler, rays, color, distancePx,
                                       worldPixelX, worldPixelY);
        pl.setSoftnessLength(distancePx * 0.3f);
        LightSource src = new LightSource(worldPixelX, worldPixelY, pl, color, distancePx);
        lights.add(src);
        return src;
    }

    public LightSource addConeLight(float worldPixelX, float worldPixelY,
                                    Color color, float distancePx, int rays,
                                    float directionDegrees, float coneDegrees) {
        ConeLight cl = new ConeLight(rayHandler, rays, color, distancePx,
                                     worldPixelX, worldPixelY,
                                     directionDegrees, coneDegrees);
        cl.setSoftnessLength(distancePx * 0.08f);
        LightSource src = new LightSource(worldPixelX, worldPixelY, cl, color, distancePx);
        lights.add(src);
        return src;
    }

    public void removeLight(LightSource src) {
        src.remove();
        lights.remove(src);
    }


    public void render(Camera cam) {
        combined.setToOrtho2D(-cam.cameraX, -cam.cameraY,
                               cam.VIRTUAL_WIDTH, cam.VIRTUAL_HEIGHT);
        rayHandler.setCombinedMatrix(combined);
        rayHandler.updateAndRender();
    }


    public void resize(int width, int height) { rayHandler.resizeFBO(width / 4, height / 4); }

    public void dispose() { rayHandler.dispose(); b2World.dispose(); }

    public List<LightSource> getLightSources() {
        return Collections.unmodifiableList(lights);
    }

    private void addSunLight(Color color, float angle) {
        sunLight = new DirectionalLight(rayHandler, 64, color, angle);
    }
}