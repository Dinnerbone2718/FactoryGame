package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.factory.game.World.ObjectSpriteCache;
import java.util.Random;

public class Bubble {

    private static final float POP_DURATION = 0.2f;
    private static final float MIN_LIFETIME = 2.2f;
    private static final float MAX_LIFETIME = 4.2f;
    private static final float MIN_RISE_SPEED = 35f;
    private static final float MAX_RISE_SPEED = 70f;
    private static final float MIN_WOBBLE_AMPLITUDE = 6f;
    private static final float MAX_WOBBLE_AMPLITUDE = 16f;
    private static final float MIN_WOBBLE_FREQUENCY = 1.5f;
    private static final float MAX_WOBBLE_FREQUENCY = 3f;

    private float worldX, worldY;

    private float xSpeed, ySpeed;

    private int bubledNum = 0;

    private float age = 0f;
    private final float lifeTime;

    private boolean popped = false;
    private float popTimer = 0f;
    private float scale = 1f;

    private final float wobbleAmplitude;
    private final float wobbleFrequency;
    private final float wobblePhase;

    public boolean visible = true;

    private Texture texture;
    private TextureRegion[] frames;

    public Bubble(float startWorldX, float startWorldY) {
        this.worldX = startWorldX;
        this.worldY = startWorldY;

        Random rng = new Random();
        this.ySpeed =
            MIN_RISE_SPEED +
            rng.nextFloat() * (MAX_RISE_SPEED - MIN_RISE_SPEED);
        this.xSpeed = 0f;
        this.wobbleAmplitude =
            MIN_WOBBLE_AMPLITUDE +
            rng.nextFloat() * (MAX_WOBBLE_AMPLITUDE - MIN_WOBBLE_AMPLITUDE);
        this.wobbleFrequency =
            MIN_WOBBLE_FREQUENCY +
            rng.nextFloat() * (MAX_WOBBLE_FREQUENCY - MIN_WOBBLE_FREQUENCY);
        this.wobblePhase = rng.nextFloat() * (float) (Math.PI * 2);
        this.lifeTime =
            MIN_LIFETIME + rng.nextFloat() * (MAX_LIFETIME - MIN_LIFETIME);

        loadTexture();
        this.bubledNum = rng.nextInt(frames.length);
    }

    public Bubble(
        float startWorldX,
        float startWorldY,
        float dirX,
        float dirY
    ) {
        this(startWorldX, startWorldY);
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len > 0.0001f) {
            float speed = this.ySpeed;
            this.xSpeed = (dirX / len) * speed;
            this.ySpeed = Math.max((dirY / len) * speed, MIN_RISE_SPEED * 0.4f);
        }
    }

    public void update(float delta) {
        if (!visible) return;

        if (popped) {
            popTimer += delta;
            scale = Math.max(0f, 1f - popTimer / POP_DURATION);
            if (popTimer >= POP_DURATION) {
                visible = false;
            }
            return;
        }

        age += delta;

        worldY += ySpeed * delta;
        worldX +=
            xSpeed * delta +
            (float) Math.sin(age * wobbleFrequency + wobblePhase) *
            wobbleAmplitude *
            delta;

        if (age >= lifeTime) {
            popped = true;
            popTimer = 0f;
        }
    }

    public boolean isFinished() {
        return !visible;
    }

    private void loadTexture() {
        int COLS = 2;
        int ROWS = 3;
        texture = new Texture("spritesheets/bubbles.png");
        int fw = texture.getWidth() / COLS;
        int fh = texture.getHeight() / ROWS;
        frames = new TextureRegion[ROWS * COLS];
        for (int row = 0; row < ROWS; row++) for (
            int col = 0;
            col < COLS;
            col++
        ) frames[row * COLS + col] = new TextureRegion(
            texture,
            col * fw,
            row * fh,
            fw,
            fh
        );
    }

    public void draw(Batch batch, Camera cam) {
        if (!visible || scale <= 0f) return;
        ShaderProgram shader = (ShaderProgram) batch.getShader();

        batch.flush();
        shader.setUniformi("u_isWater", 0);
        shader.setUniformi("u_hasNormalMap", 0);
        ObjectSpriteCache.flatNormal.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        TextureRegion frame = frames[bubledNum];
        float size = Main.TILE_SCALE * scale;
        float centerOffset = (Main.TILE_SCALE - size) / 2f;
        batch.draw(
            frame,
            worldX + cam.cameraX + centerOffset,
            worldY + cam.cameraY + centerOffset,
            size,
            size
        );
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public void setPosition(float x, float y) {
        this.worldX = x;
        this.worldY = y;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
