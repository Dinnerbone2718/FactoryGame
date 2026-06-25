package com.factory.game.Renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.factory.game.Camera;
import com.factory.game.Main;
import com.factory.game.World.ObjectSpriteCache;

public class FootprintManager {

    private static final float FOOTPRINT_LIFETIME = 4f;
    private static final int   MAX_FOOTPRINTS     = 120;

    private static final float FOOT_SIZE          = Main.TILE_SCALE * 0.14f;

    private static class Footprint {
        float  worldX, worldY;
        float  age;
        String tileType;
        boolean leftFoot;
        int     facingRow;
    }

    private final List<Footprint> footprints = new ArrayList<>();

    public void update(float delta) {
        Iterator<Footprint> it = footprints.iterator();
        while (it.hasNext()) {
            Footprint fp = it.next();
            fp.age += delta;
            if (fp.age >= FOOTPRINT_LIFETIME) it.remove();
        }
    }

    public void spawnFootprint(float worldX, float worldY,
                               int facingRow, boolean leftFoot, String tileType) {
        if ("water".equals(tileType)) return;

        if (footprints.size() >= MAX_FOOTPRINTS) {
            footprints.remove(0);
        }

        float ts   = Main.TILE_SCALE;
        float side = leftFoot ? -1f : 1f;

        float lateralOffset = ts * 0.18f;
        float forwardOffset = ts * 0.05f;

        float ox, oy;
        switch (facingRow) {
            case 1:
                ox = side * lateralOffset;
                oy = ts * 0.12f + forwardOffset;
                break;
            case 0:
                ox = side * lateralOffset;
                oy = ts * 0.08f - forwardOffset;
                break;
            case 2:
                ox = ts * 0.15f + forwardOffset;
                oy = ts * -0.20f + side * lateralOffset;
                break;
            case 3:
                ox = ts * 0.05f - forwardOffset;
                oy = ts * -0.20f + side * lateralOffset;
                break;
            default:
                ox = side * lateralOffset;
                oy = ts * 0.10f;
                break;
        }

        Footprint fp = new Footprint();
        fp.worldX = worldX + ts * 0.5f + ox;
        fp.worldY = worldY + ts * 0.5f + oy;
        fp.age       = 0f;
        fp.tileType  = tileType;
        fp.leftFoot  = leftFoot;
        fp.facingRow = facingRow;
        footprints.add(fp);
    }

    public void draw(Batch batch, Camera cam) {
        if (footprints.isEmpty()) return;

        ShaderProgram shader = (ShaderProgram) batch.getShader();
        batch.flush();
        shader.setUniformi("u_isWater",      0);
        shader.setUniformi("u_hasNormalMap", 0);
        shader.setUniformi("u_isItem",       0);
        ObjectSpriteCache.flatNormal.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        Color prev = new Color(batch.getColor());

        for (Footprint fp : footprints) {
            float t     = fp.age / FOOTPRINT_LIFETIME;
            float alpha = (t < 0.20f) ? 1f : 1f - ((t - 0.20f) / 0.80f);
            alpha = Math.max(0f, alpha * 0.55f);
            if (alpha < 0.01f) continue;

            Color col = footprintColor(fp.tileType);
            batch.setColor(col.r, col.g, col.b, alpha);

            float sx = fp.worldX + cam.cameraX - FOOT_SIZE * 0.5f;
            float sy = fp.worldY + cam.cameraY - FOOT_SIZE * 0.5f;
            batch.draw(ObjectSpriteCache.whitePixel, sx, sy, FOOT_SIZE, FOOT_SIZE);
        }

        batch.setColor(prev);
    }

    private static Color footprintColor(String tileType) {
        if (tileType == null) return new Color(0.28f, 0.28f, 0.28f, 1f);
        switch (tileType) {
            case "grass":    return new Color(0.22f, 0.38f, 0.14f, 1f);
            case "forest":   return new Color(0.17f, 0.32f, 0.10f, 1f);
            case "desert":   return new Color(0.58f, 0.44f, 0.20f, 1f);
            case "mountain": return new Color(0.38f, 0.36f, 0.32f, 1f);
            case "snow":     return new Color(0.55f, 0.60f, 0.68f, 1f);
            case "badland":  return new Color(0.34f, 0.22f, 0.12f, 1f);
            case "swamp":    return new Color(0.18f, 0.26f, 0.12f, 1f);
            case "cave":     return new Color(0.22f, 0.20f, 0.18f, 1f);
            default:         return new Color(0.28f, 0.28f, 0.28f, 1f);
        }
    }

    public void clear() { footprints.clear(); }
}
