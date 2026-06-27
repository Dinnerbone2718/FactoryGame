package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;
import com.factory.game.Renderer.LightRenderer;
import com.factory.game.Renderer.LightSource;
import com.factory.game.World.ObjectSpriteCache;

public class Player {

    public interface CollisionChecker {
        boolean isBlocked(float x0, float y0, float x1, float y1);
    }

    private float worldX, worldY;
    private final Inventory inventory;

    private static final float SPEED = 220f;
    private static final float SPRINT_SPEED = 350f;

    private static final float COL_OX = 0.20f;
    private static final float COL_OY = 0.05f;
    private static final float COL_W = 0.60f;
    private static final float COL_H = 0.35f;

    private static final float RAFT_COL_OX = -0.50f;
    private static final float RAFT_COL_OY = -0.30f;
    private static final float RAFT_COL_W = 2.00f;
    private static final float RAFT_COL_H = 2.00f;

    private static final int COLS = 4;
    private static final int ROWS = 4;
    private static final float FRAME_DURATION = 0.12f;

    private static final float FLASHLIGHT_DISTANCE_TILES = 10f;
    private static final float FLASHLIGHT_CONE_DEGREES = 30f;

    private int facingRow = 0;
    private int animFrame = 0;
    private float animTimer = 0f;

    private MyVector boatVelocity = new MyVector(0f, 0f);

    private static final float FOOTPRINT_STEP_DIST = Main.TILE_SCALE * 0.50f;
    private float distSinceFootprint = 0f;
    private boolean isLeftFootNext = true;
    private boolean footprintPending = false;
    private float pendingFpX, pendingFpY;
    private int pendingFpFacing;
    private boolean pendingFpLeft;

    private boolean flashlightOn = false;

    private LightSource lightSource = null;

    private Texture texture;
    private TextureRegion[] frames;

    private Texture raftTexture;
    private boolean onRaft = false;
    private Body shadowBody;

    public Player(float startWorldX, float startWorldY) {
        this.worldX = startWorldX;
        this.worldY = startWorldY;
        this.inventory = new Inventory();

        inventory.addItem(Item.WRENCH_AND_SCREW, 1);

        inventory.addItem(Item.WOOD_FLOOR, 50);
        inventory.addItem(Item.STONE_FLOOR, 50);
        inventory.addItem(Item.STONE_WALL, 50);
        inventory.addItem(Item.WALL, 50);
        inventory.addItem(Item.BASIC_PIPE, 50);
        inventory.addItem(Item.STONE_WALL_FULL, 50);

        inventory.addItem(Item.DRILL, 1);
        inventory.addItem(Item.TANK, 1);

        inventory.addItem(Item.SMELTER, 1);

        inventory.addItem(Item.DEVBARREL, 1);

        inventory.addItem(Item.CLOCK, 1);
        inventory.addItem(Item.TRASH, 1);
        inventory.addItem(Item.GLOBE, 1);
        inventory.addItem(Item.BOOK_SHELF, 10);
        inventory.addItem(Item.MAP, 1);
        inventory.addItem(Item.WOOD_PLANKS, 10);
        inventory.addItem(Item.ANINMAL_PHONE, 1);
        inventory.addItem(Item.POO_PET, 1);
        inventory.addItem(Item.BUBBLE_WAND, 1);
        inventory.addItem(Item.SODA, 1);

        loadTexture();
    }

    private void loadTexture() {
        texture = new Texture("spritesheets/player.png");
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

        raftTexture = new Texture("other_placeables/raft.png");
    }

    public void update(float delta, CollisionChecker cc) {
        if (shadowBody != null) {
            shadowBody.setTransform(
                worldX + Main.TILE_SCALE * .5f,
                worldY + Main.TILE_SCALE * .5f,
                0f
            );
        }

        if (onRaft) {
            animFrame = 0;
            animTimer = 0f;

            float ix = 0f,
                iy = 0f;
            if (
                Gdx.input.isKeyPressed(Input.Keys.D) ||
                Gdx.input.isKeyPressed(Input.Keys.RIGHT)
            ) {
                ix += 1;
                facingRow = 2;
            }
            if (
                Gdx.input.isKeyPressed(Input.Keys.A) ||
                Gdx.input.isKeyPressed(Input.Keys.LEFT)
            ) {
                ix -= 1;
                facingRow = 3;
            }
            if (
                Gdx.input.isKeyPressed(Input.Keys.W) ||
                Gdx.input.isKeyPressed(Input.Keys.UP)
            ) {
                iy += 1;
                facingRow = 1;
            }
            if (
                Gdx.input.isKeyPressed(Input.Keys.S) ||
                Gdx.input.isKeyPressed(Input.Keys.DOWN)
            ) {
                iy -= 1;
                facingRow = 0;
            }

            boolean sprinting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
            boolean inputing = (ix != 0f || iy != 0f);

            if (inputing) {
                float len = (float) Math.sqrt(ix * ix + iy * iy);
                float accelBase = (sprinting ? SPRINT_SPEED : SPEED);
                float accel = accelBase * 0.02f;
                boatVelocity.setX(
                    boatVelocity.getX() + (ix / len) * accel * delta
                );
                boatVelocity.setY(
                    boatVelocity.getY() + (iy / len) * accel * delta
                );
            }

            float friction = 0.99f;
            boatVelocity.setX(boatVelocity.getX() * friction);
            boatVelocity.setY(boatVelocity.getY() * friction);

            float maxSpeed = sprinting ? SPRINT_SPEED : SPEED;
            boatVelocity.setX(
                Math.max(-maxSpeed, Math.min(maxSpeed, boatVelocity.getX()))
            );
            boatVelocity.setY(
                Math.max(-maxSpeed, Math.min(maxSpeed, boatVelocity.getY()))
            );

            if (Math.abs(boatVelocity.getX()) < 0.05f) boatVelocity.setX(0f);
            if (Math.abs(boatVelocity.getY()) < 0.05f) boatVelocity.setY(0f);

            float nextWX = worldX + boatVelocity.getX();
            if (!collidesAt(nextWX, worldY, cc)) {
                worldX = nextWX;
            } else if (boatVelocity.getX() != 0f) {
                float bounce = 0.95f;
                boatVelocity.setX(-boatVelocity.getX() * bounce);
                worldX += Math.signum(boatVelocity.getX()) * 0.01f;
            }

            float nextWY = worldY + boatVelocity.getY();
            if (!collidesAt(worldX, nextWY, cc)) {
                worldY = nextWY;
            } else if (boatVelocity.getY() != 0f) {
                float bounce = 0.95f;
                boatVelocity.setY(-boatVelocity.getY() * bounce);
                worldY += Math.signum(boatVelocity.getY()) * 0.01f;
            }

            if (Math.abs(boatVelocity.getX()) > Math.abs(boatVelocity.getY())) {
                facingRow = (boatVelocity.getX() > 0) ? 2 : 3;
            } else if (Math.abs(boatVelocity.getY()) > 0) {
                facingRow = (boatVelocity.getY() > 0) ? 1 : 0;
            }
        } else {
            boatVelocity.setX(0);
            boatVelocity.setY(0);

            float dx = 0,
                dy = 0;
            boolean spriting = false;

            if (
                Gdx.input.isKeyPressed(Input.Keys.D) ||
                Gdx.input.isKeyPressed(Input.Keys.RIGHT)
            ) {
                dx += 1;
                facingRow = 2;
            }
            if (
                Gdx.input.isKeyPressed(Input.Keys.A) ||
                Gdx.input.isKeyPressed(Input.Keys.LEFT)
            ) {
                dx -= 1;
                facingRow = 3;
            }
            if (
                Gdx.input.isKeyPressed(Input.Keys.W) ||
                Gdx.input.isKeyPressed(Input.Keys.UP)
            ) {
                dy += 1;
                facingRow = 1;
            }
            if (
                Gdx.input.isKeyPressed(Input.Keys.S) ||
                Gdx.input.isKeyPressed(Input.Keys.DOWN)
            ) {
                dy -= 1;
                facingRow = 0;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                spriting = true;
            }

            boolean moving = (dx != 0 || dy != 0);
            float prevX = worldX,
                prevY = worldY;

            if (moving) {
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                float moveX;
                float moveY;
                if (!spriting) {
                    moveX = (dx / len) * SPEED * delta;
                    moveY = (dy / len) * SPEED * delta;
                } else {
                    animTimer += delta * (5f / 8f);
                    moveX = (dx / len) * SPRINT_SPEED * delta;
                    moveY = (dy / len) * SPRINT_SPEED * delta;
                }

                if (!collidesAt(worldX + moveX, worldY, cc)) worldX += moveX;
                if (!collidesAt(worldX, worldY + moveY, cc)) worldY += moveY;

                float distMoved = (float) Math.sqrt(
                    (worldX - prevX) * (worldX - prevX) +
                        (worldY - prevY) * (worldY - prevY)
                );
                if (distMoved > 0.01f) {
                    distSinceFootprint += distMoved;
                    if (distSinceFootprint >= FOOTPRINT_STEP_DIST) {
                        distSinceFootprint -= FOOTPRINT_STEP_DIST;
                        footprintPending = true;
                        pendingFpX = worldX;
                        pendingFpY = worldY;
                        pendingFpFacing = facingRow;
                        pendingFpLeft = isLeftFootNext;
                        isLeftFootNext = !isLeftFootNext;
                    }
                }

                animTimer += delta;
                while (animTimer >= FRAME_DURATION) {
                    animTimer -= FRAME_DURATION;
                    animFrame = (animFrame + 1) % COLS;
                }
            } else {
                animFrame = 0;
                animTimer = 0f;
            }
        }
    }

    private boolean collidesAt(float px, float py, CollisionChecker cc) {
        float ts = Main.TILE_SCALE;
        float ox = onRaft ? RAFT_COL_OX : COL_OX;
        float oy = onRaft ? RAFT_COL_OY : COL_OY;
        float w = onRaft ? RAFT_COL_W : COL_W;
        float h = onRaft ? RAFT_COL_H : COL_H;
        float x0 = px + ox * ts;
        float y0 = py + oy * ts;
        float x1 = x0 + w * ts;
        float y1 = y0 + h * ts;
        return cc.isBlocked(x0, y0, x1, y1);
    }

    public void draw(Batch batch, Camera cam) {
        ShaderProgram shader = (ShaderProgram) batch.getShader();

        batch.flush();
        shader.setUniformi("u_isWater", 0);
        shader.setUniformi("u_hasNormalMap", 0);
        ObjectSpriteCache.flatNormal.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        if (onRaft && raftTexture != null) {
            float raftW = Main.TILE_SCALE * 2.0f;
            float raftH = Main.TILE_SCALE * 2.0f;
            float raftX =
                worldX + cam.cameraX + Main.TILE_SCALE * 0.5f - raftW * 0.5f;
            float raftY = worldY + cam.cameraY - raftH * 0.15f;
            batch.draw(raftTexture, raftX, raftY, raftW, raftH);
        }

        TextureRegion frame = frames[facingRow * COLS + animFrame];
        float drawY = onRaft
            ? worldY + cam.cameraY + Main.TILE_SCALE * 0.25f
            : worldY + cam.cameraY;
        batch.draw(
            frame,
            worldX + cam.cameraX,
            drawY,
            Main.TILE_SCALE,
            Main.TILE_SCALE
        );
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public float getRenderY() {
        return worldY;
    }

    public int getFacingRow() {
        return facingRow;
    }

    public float getFacingDirX() {
        if (facingRow == 2) return 1f;
        if (facingRow == 3) return -1f;
        return 0f;
    }

    public float getFacingDirY() {
        if (facingRow == 1) return 1f;
        if (facingRow == 0) return -1f;
        return 0f;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isOnRaft() {
        return onRaft;
    }

    public void setOnRaft(boolean val) {
        onRaft = val;
    }

    public boolean getInventoryMap() {
        return inventory.hasItem(Item.MAP, 1);
    }

    public boolean getInventoryClock() {
        return inventory.hasItem(Item.CLOCK, 1);
    }

    public boolean getInventoryFlashlight() {
        return inventory.hasItem(Item.FLASHLIGHT, 1);
    }

    public boolean isFlashlightOn() {
        return flashlightOn;
    }

    private float mouseAngle(Camera cam) {
        float mouseScreenX = Gdx.input.getX();
        float mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float mouseWorldX = mouseScreenX - cam.cameraX;
        float mouseWorldY = mouseScreenY - cam.cameraY;

        float cx = worldX + Main.TILE_SCALE * 0.5f;
        float cy = worldY + Main.TILE_SCALE * 0.5f;

        return (float) Math.toDegrees(
            Math.atan2(mouseWorldY - cy, mouseWorldX - cx)
        );
    }

    public void updateLightSourcePosition(Camera cam) {
        if (lightSource != null) {
            float cx = worldX + Main.TILE_SCALE * 0.5f;
            float cy = worldY + Main.TILE_SCALE * 0.5f;
            lightSource.setWorldPosition(cx, cy);
            lightSource.setConeDirection(mouseAngle(cam));
        }
    }

    public void setFlashlightOn(LightRenderer lightRenderer, Camera cam) {
        if (lightSource == null) {
            float cx = worldX + Main.TILE_SCALE * 0.5f;
            float cy = worldY + Main.TILE_SCALE * 0.5f;
            lightSource = lightRenderer.addConeLight(
                cx,
                cy,
                new com.badlogic.gdx.graphics.Color(1f, 0.95f, 0.85f, 1f),
                FLASHLIGHT_DISTANCE_TILES * Main.TILE_SCALE,
                128,
                mouseAngle(cam),
                FLASHLIGHT_CONE_DEGREES
            );
        }
        lightSource.setActive(true);
        flashlightOn = true;
    }

    public void setFlashlightOff() {
        if (lightSource != null) {
            lightSource.setActive(false);
        }
        flashlightOn = false;
    }

    public void setPosition(float x, float y) {
        this.worldX = x;
        this.worldY = y;
    }

    public void createShadowBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(worldX, worldY);

        shadowBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(Main.TILE_SCALE * .22f, Main.TILE_SCALE * .12f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        shadowBody.createFixture(fixtureDef);
        shape.dispose();
    }

    public boolean hasPendingFootprint() {
        return footprintPending;
    }

    public float getPendingFpX() {
        return pendingFpX;
    }

    public float getPendingFpY() {
        return pendingFpY;
    }

    public int getPendingFpFacing() {
        return pendingFpFacing;
    }

    public boolean getPendingFpLeft() {
        return pendingFpLeft;
    }

    public void clearPendingFootprint() {
        footprintPending = false;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        if (raftTexture != null) {
            raftTexture.dispose();
            raftTexture = null;
        }

        if (shadowBody != null) {
            shadowBody.getWorld().destroyBody(shadowBody);
            shadowBody = null;
        }
    }
}
