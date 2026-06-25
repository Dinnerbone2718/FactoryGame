package com.factory.game.Renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.factory.game.Camera;
import com.factory.game.Main;

public class FireParticleEmitter {

    private static final class Particle {
        float x, y;          
        float vx, vy;        
        float life, maxLife; 
        float size;          
    }

    private static final float EMIT_INTERVAL = 0.045f;
    private static final float RISE_MIN      = 68f;   
    private static final float RISE_RANGE    = 82f;   
    private static final float LATERAL_MAX   = 205f;   
    private static final float LATERAL_DAMP  = 3.5f;  
    private static final float LIFE_MIN      = 0.28f;
    private static final float LIFE_RANGE    = 0.38f;
    private static final float SIZE_MIN      = 2.2f;
    private static final float SIZE_RANGE    = 2.8f;

    private final List<Particle> particles = new ArrayList<>();
    private final Random         rng;
    private       float          emitTimer = 0f;

    private final float originX, originY;

    public FireParticleEmitter(float worldPixelX, float worldPixelY, long seed) {
        this.originX = worldPixelX;
        this.originY = worldPixelY;
        this.rng     = new Random(seed);
    }


    public void update(float delta) {
        emitTimer += delta;
        while (emitTimer >= EMIT_INTERVAL) {
            emitTimer -= EMIT_INTERVAL;
            emit();
        }

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life -= delta;
            if (p.life <= 0f) { it.remove(); continue; }
            p.x  += p.vx * delta;
            p.y  += p.vy * delta;
            p.vx *= (1f - LATERAL_DAMP * delta);
        }
    }

    private void emit() {
        Particle p = new Particle();
        float spread = Main.TILE_SCALE * 0.12f;
        p.x       = originX + (rng.nextFloat() - 0.5f) * spread;
        p.y       = originY + Main.TILE_SCALE * 0.30f;
        p.vx      = (rng.nextFloat() - 0.5f) * LATERAL_MAX;
        p.vy      = RISE_MIN + rng.nextFloat() * RISE_RANGE;
        p.maxLife = LIFE_MIN + rng.nextFloat() * LIFE_RANGE;
        p.life    = p.maxLife;
        p.size    = SIZE_MIN + rng.nextFloat() * SIZE_RANGE;
        particles.add(p);
    }

    public void draw(Batch batch, TextureRegion pixel, Camera cam) {
        if (particles.isEmpty()) return;

        ShaderProgram shader = (ShaderProgram) batch.getShader();
        batch.flush();
        shader.setUniformi("u_hasNormalMap", 0);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); 

        for (Particle p : particles) {
            float t   = p.life / p.maxLife;

            float r   = 1f;
            float g   = t > 0.55f ? 0.80f : t * (0.80f / 0.55f);
            float b   = t > 0.75f ? (t - 0.75f) * 0.6f : 0f;
            float a   = t < 0.20f ? (t / 0.20f) * 0.90f : 0.90f;
            float sz  = p.size * (0.35f + t * 0.65f);

            batch.setColor(r, g, b, a);
            batch.draw(pixel,
                p.x + cam.cameraX - sz * 0.5f,
                p.y + cam.cameraY - sz * 0.5f,
                sz, sz);
        }

        batch.flush();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }
}