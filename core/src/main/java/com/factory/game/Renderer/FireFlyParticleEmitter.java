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

public class FireFlyParticleEmitter {

    private static final class Particle {
        float x, y;
        float vx, vy;
        float life, maxLife;
        float size;
        float pulseOffset;
        float pulseSpeed;
        float wanderTimer;
        float blinkOnTime;
        float blinkOffTime;
        float blinkTimer;
        boolean blinkOn;
        float homeX, homeY;

        LightSource light;
    }

    private static final float EMIT_INTERVAL      = 1.5f;

    private static final float SPEED_MIN          = 6f;
    private static final float SPEED_RANGE        = 18f;
    private static final float DAMP               = 0.6f;

    private static final float WANDER_MIN         = 0.8f;
    private static final float WANDER_RANGE       = 2.2f;

    private static final float SPAWN_RADIUS_X     = 160f;
    private static final float SPAWN_RADIUS_Y     = 80f;

    private static final float LIFE_MIN           = 5f;
    private static final float LIFE_RANGE         = 6f;

    private static final float SIZE_MIN           = 1.8f;
    private static final float SIZE_RANGE         = 2.2f;

    private static final float PULSE_SPEED_MIN    = 3f;
    private static final float PULSE_SPEED_RANGE  = 4f;

    private static final float BLINK_ON_MIN       = 0.55f;
    private static final float BLINK_ON_RANGE     = 0.55f;
    private static final float BLINK_OFF_MIN      = 1.6f;
    private static final float BLINK_OFF_RANGE    = 2.6f;

    private static final float FADE_IN_FRAC       = 0.40f;
    private static final float FADE_OUT_FRAC      = 0.48f;

    private static final float FIREFLY_LIGHT_RADIUS = 48f;
    private static final int   FIREFLY_LIGHT_RAYS   = 12;


    private final List<Particle> particles = new ArrayList<>();
    private final Random         rng;
    private       float          emitTimer = 0f;
    private final float          originX, originY;

    private boolean              dying = false;

    private LightRenderer        lightRenderer;

    private final Color          scratchColor = new Color();


    public FireFlyParticleEmitter(float worldPixelX, float worldPixelY, long seed) {
        this.originX = worldPixelX;
        this.originY = worldPixelY;
        this.rng = new Random(seed ^ (long)(worldPixelX * 73856093) ^ (long)(worldPixelY * 19349663));
        this.emitTimer = rng.nextFloat() * EMIT_INTERVAL;
    }

    public void setLightRenderer(LightRenderer lightRenderer) {
        this.lightRenderer = lightRenderer;
    }

    public void startFading() {
        dying = true;
    }

    public boolean isDead() {
        return dying && particles.isEmpty();
    }

    public void update(float delta) {
        if (!dying) {
            emitTimer += delta;
            while (emitTimer >= EMIT_INTERVAL) {
                emitTimer -= EMIT_INTERVAL;
                emit();
            }
        }

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life -= delta;
            if (p.life <= 0f) {
                removeParticleLight(p);
                it.remove();
                continue;
            }

            p.wanderTimer -= delta;
            if (p.wanderTimer <= 0f) {
                float angle = (float)(rng.nextFloat() * 2.0 * Math.PI);
                float spd   = SPEED_MIN + rng.nextFloat() * SPEED_RANGE;
                p.vx = (float) Math.cos(angle) * spd;
                p.vy = (float) Math.sin(angle) * spd + 3f;
                p.wanderTimer = WANDER_MIN + rng.nextFloat() * WANDER_RANGE;
            }

            float pullX = (p.homeX - p.x) * 0.015f;
            float pullY = (p.homeY - p.y) * 0.015f;
            p.vx += pullX;
            p.vy += pullY;

            p.x  += p.vx * delta;
            p.y  += p.vy * delta;
            p.vx *= (1f - DAMP * delta);
            p.vy *= (1f - DAMP * delta);

            p.pulseOffset += p.pulseSpeed * delta;

            boolean wasBlinkOn = p.blinkOn;
            p.blinkTimer -= delta;
            if (p.blinkTimer <= 0f) {
                p.blinkOn    = !p.blinkOn;
                p.blinkTimer = p.blinkOn ? p.blinkOnTime : p.blinkOffTime;
            }

            syncParticleLight(p, wasBlinkOn);
        }
    }

    private void syncParticleLight(Particle p, boolean wasBlinkOn) {
        if (lightRenderer == null) return;

        if (p.blinkOn) {

            float lifeFrac  = p.life / p.maxLife;
            float fadeIn    = Math.min(1f, (1f - lifeFrac) / FADE_IN_FRAC);
            float fadeOut   = Math.min(1f, lifeFrac / FADE_OUT_FRAC);
            float lifeEnv   = fadeIn * fadeOut;
            float blinkFrac = 1f - (p.blinkTimer / p.blinkOnTime);
            float blinkEnv  = Math.max(0f, (float) Math.sin(blinkFrac * Math.PI));
            float pulse     = 0.5f + 0.5f * (float) Math.sin(p.pulseOffset);
            float alpha     = lifeEnv * blinkEnv * (0.65f + 0.35f * pulse);

            float hueShift = (float) Math.sin(p.pulseOffset * 0.3f) * 0.12f;
            float r = 0.55f + pulse * 0.45f;
            float g = 0.85f + hueShift;
            float b = 0.05f + pulse * 0.10f;

            scratchColor.set(r, g, b, alpha);

            if (p.light == null) {
                p.light = lightRenderer.addPointLight(
                    p.x, p.y,
                    new Color(scratchColor),
                    FIREFLY_LIGHT_RADIUS, FIREFLY_LIGHT_RAYS);
            } else {
                p.light.setWorldPosition(p.x, p.y);
                p.light.setColor(scratchColor);
            }
        } else if (wasBlinkOn) {
            removeParticleLight(p);
        }
    }

    private void removeParticleLight(Particle p) {
        if (p.light != null && lightRenderer != null) {
            lightRenderer.removeLight(p.light);
            p.light = null;
        }
    }

    private void emit() {
        Particle p = new Particle();

        float angle  = rng.nextFloat() * 2f * (float) Math.PI;
        float radius = (float) Math.sqrt(rng.nextFloat());
        p.x = originX + (float) Math.cos(angle) * radius * SPAWN_RADIUS_X;
        p.y = originY + (float) Math.sin(angle) * radius * SPAWN_RADIUS_Y;
        p.homeX = p.x;
        p.homeY = p.y;

        p.vx = (rng.nextFloat() - 0.5f) * 8f;
        p.vy = (rng.nextFloat() - 0.5f) * 8f;

        p.maxLife     = LIFE_MIN + rng.nextFloat() * LIFE_RANGE;
        p.life        = p.maxLife;
        p.size        = SIZE_MIN + rng.nextFloat() * SIZE_RANGE;
        p.pulseOffset = rng.nextFloat() * 2f * (float) Math.PI;
        p.pulseSpeed  = PULSE_SPEED_MIN + rng.nextFloat() * PULSE_SPEED_RANGE;
        p.wanderTimer = rng.nextFloat() * WANDER_MIN;

        p.blinkOnTime  = BLINK_ON_MIN  + rng.nextFloat() * BLINK_ON_RANGE;
        p.blinkOffTime = BLINK_OFF_MIN + rng.nextFloat() * BLINK_OFF_RANGE;
        p.blinkOn      = rng.nextBoolean();
        p.blinkTimer   = p.blinkOn ? p.blinkOnTime : p.blinkOffTime;

        p.light = null;

        particles.add(p);
    }

    public void draw(Batch batch, TextureRegion pixel, Camera cam) {
        if (particles.isEmpty()) return;

        ShaderProgram shader = (ShaderProgram) batch.getShader();
        batch.flush();
        shader.setUniformi("u_hasNormalMap", 0);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for (Particle p : particles) {
            if (!p.blinkOn) continue;

            float lifeFrac = p.life / p.maxLife;

            float fadeIn  = Math.min(1f, (1f - lifeFrac) / FADE_IN_FRAC);
            float fadeOut = Math.min(1f, lifeFrac / FADE_OUT_FRAC);
            float lifeEnv = fadeIn * fadeOut;

            float blinkFrac = 1f - (p.blinkTimer / p.blinkOnTime);
            float blinkEnv  = (float) Math.sin(blinkFrac * Math.PI);
            blinkEnv        = Math.max(0f, blinkEnv);

            float pulse = 0.5f + 0.5f * (float) Math.sin(p.pulseOffset);

            float alpha = lifeEnv * blinkEnv * (0.65f + 0.35f * pulse);
            if (alpha < 0.01f) continue;

            float hueShift = (float) Math.sin(p.pulseOffset * 0.3f) * 0.12f;
            float r = 0.55f + pulse * 0.45f;
            float g = 0.85f + hueShift;
            float b = 0.05f + pulse * 0.10f;

            float sz = p.size * (0.8f + 0.2f * pulse);

            batch.setColor(r, g, b, alpha);
            batch.draw(pixel,
                p.x + cam.cameraX - sz * 0.5f,
                p.y + cam.cameraY - sz * 0.5f,
                sz, sz);
        }

        batch.flush();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        if (lightRenderer != null) {
            for (Particle p : particles) {
                removeParticleLight(p);
            }
        }
        particles.clear();
    }
}