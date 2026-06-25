package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.factory.game.World.ObjectSpriteCache;

public class PenguinDude {

    private float worldX, worldY;

    private static final float SPEED = 220f;
    private static final float SPRINT_SPEED = 350f;

    private static final float COL_OX = 0.20f;
    private static final float COL_OY = 0.05f;
    private static final float COL_W = 0.60f;
    private static final float COL_H = 0.35f;

    private static final int COLS = 4;
    private static final int ROWS = 3;
    private static final float FRAME_DURATION = 0.05f;

    private static final int WALK_ROW_DOWN = 0;
    private static final int WALK_ROW_UP = 1;
    private static final int SIT_ROW = 2;

    private static final int SIT_COL_DOWN = 0;
    private static final int SIT_COL_RIGHT = 1;
    private static final int SIT_COL_LEFT = 2;
    private static final int SIT_COL_UP = 3;

    private static final float IDLE_SIT_DELAY = 2.5f;

    private static final float LEASH_DISTANCE = Main.TILE_SCALE * 6f;

    private static final int BOAT_FRAME_DOWN = 0;
    private static final int BOAT_FRAME_RIGHT = 1;
    private static final int BOAT_FRAME_LEFT = 2;
    private static final int BOAT_FRAME_UP = 3;

    private int facingRow = WALK_ROW_DOWN;
    private int animFrame = 0;
    private float animTimer = 0f;
    private int boatFrame = BOAT_FRAME_DOWN;

    private float idleTimer = 0f;
    private boolean isSitting = false;
    private int sitCol = SIT_COL_DOWN;

    public boolean visible = true;

    private Texture texture;
    private TextureRegion[] frames;

    private Texture boat_texture;
    private TextureRegion[] boat_frames;

    private boolean boatVisible = false;

    public PenguinDude(float startWorldX, float startWorldY) {
        this.worldX = startWorldX;
        this.worldY = startWorldY;
        loadTexture();
    }

    private void loadTexture() {
        texture = new Texture("spritesheets/pengiun_dude.png");
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

        boat_texture = new Texture("spritesheets/pengiun_dude_raft.png");
        int boat_fw = boat_texture.getWidth() / COLS;
        int boat_fh = boat_texture.getHeight();
        boat_frames = new TextureRegion[COLS];
        for (int col = 0; col < COLS; col++) boat_frames[col] =
            new TextureRegion(boat_texture, col * boat_fw, 0, boat_fw, boat_fh);
    }

    public void update(float delta, Player player, Player.CollisionChecker cc) {
        if (!player.isOnRaft()) {
            boatVisible = false;
            float tpx = player.getWorldX();
            float tpy = player.getWorldY();

            float toPlayerX = tpx - worldX;
            float toPlayerY = tpy - worldY;

            float dx = 0,
                dy = 0;

            if (tpx < worldX - Main.TILE_SCALE * 3.5f) {
                dx = -1;
            } else if (tpx > worldX + Main.TILE_SCALE * 3f) {
                dx = 1;
            }

            if (tpy < worldY - Main.TILE_SCALE * 2.5f) {
                dy = -1;
                facingRow = WALK_ROW_DOWN;
            } else if (tpy > worldY + Main.TILE_SCALE * 2f) {
                dy = 1;
                facingRow = WALK_ROW_UP;
            } else {
                facingRow = WALK_ROW_DOWN;
            }

            boolean inStopZone = (dx == 0 && dy == 0);

            if (inStopZone) {
                idleTimer += delta;

                if (idleTimer >= IDLE_SIT_DELAY && !isSitting) {
                    isSitting = true;
                    sitCol = directionToSitCol(toPlayerX, toPlayerY);
                }

                if (isSitting) {
                    sitCol = directionToSitCol(toPlayerX, toPlayerY);
                }

                animFrame = 0;
                animTimer = 0f;
                return;
            }

            isSitting = false;
            idleTimer = 0f;

            float length = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;

            float moveX = dx * SPEED * delta;
            float moveY = dy * SPEED * delta;

            float distSq = toPlayerX * toPlayerX + toPlayerY * toPlayerY;
            boolean useCollision = distSq < LEASH_DISTANCE * LEASH_DISTANCE;

            if (useCollision) {
                if (!collidesAt(worldX + moveX, worldY, cc)) worldX += moveX;
                if (!collidesAt(worldX, worldY + moveY, cc)) worldY += moveY;
            } else {
                worldX += moveX;
                worldY += moveY;
            }

            animTimer += delta;
            while (animTimer >= FRAME_DURATION) {
                animTimer -= FRAME_DURATION;
                animFrame = (animFrame + 1) % COLS;
            }
        } else {
            boatVisible = true;
            float tpx = player.getWorldX();
            float tpy = player.getWorldY();

            float toPlayerX = tpx - worldX;
            float toPlayerY = tpy - worldY;

            float dx = 0,
                dy = 0;

            if (tpx < worldX - Main.TILE_SCALE * 3.5f) {
                dx = -1;
                boatFrame = BOAT_FRAME_LEFT;
            } else if (tpx > worldX + Main.TILE_SCALE * 3f) {
                dx = 1;
                boatFrame = BOAT_FRAME_RIGHT;
            }

            if (tpy < worldY - Main.TILE_SCALE * 2.5f) {
                dy = -1;
                if (dx == 0) boatFrame = BOAT_FRAME_DOWN;
            } else if (tpy > worldY + Main.TILE_SCALE * 2f) {
                dy = 1;
                if (dx == 0) boatFrame = BOAT_FRAME_UP;
            }

            boolean inStopZone = (dx == 0 && dy == 0);

            if (inStopZone) {
                idleTimer += delta;
                animFrame = 0;
                animTimer = 0f;
                return;
            }

            isSitting = false;
            idleTimer = 0f;

            float length = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;

            worldX += dx * SPEED * delta;
            worldY += dy * SPEED * delta;

            animTimer += delta;
            while (animTimer >= FRAME_DURATION) {
                animTimer -= FRAME_DURATION;
                animFrame = (animFrame + 1) % COLS;
            }
        }
    }

    private int directionToSitCol(float toPlayerX, float toPlayerY) {
        if (Math.abs(toPlayerX) >= Math.abs(toPlayerY)) {
            return toPlayerX >= 0 ? SIT_COL_RIGHT : SIT_COL_LEFT;
        } else {
            return toPlayerY >= 0 ? SIT_COL_UP : SIT_COL_DOWN;
        }
    }

    private boolean collidesAt(float px, float py, Player.CollisionChecker cc) {
        float ts = Main.TILE_SCALE;
        float x0 = px + COL_OX * ts;
        float y0 = py + COL_OY * ts;
        float x1 = x0 + COL_W * ts;
        float y1 = y0 + COL_H * ts;
        return cc.isBlocked(x0, y0, x1, y1);
    }

    public void draw(Batch batch, Camera cam) {
        if (!visible) return;
        ShaderProgram shader = (ShaderProgram) batch.getShader();

        batch.flush();
        shader.setUniformi("u_isWater", 0);
        shader.setUniformi("u_hasNormalMap", 0);
        ObjectSpriteCache.flatNormal.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        if (boatVisible) {
            TextureRegion frame = boat_frames[boatFrame];
            batch.draw(
                frame,
                worldX + cam.cameraX,
                worldY + cam.cameraY,
                Main.TILE_SCALE * (.75f),
                Main.TILE_SCALE * (.75f)
            );
        } else {
            TextureRegion frame = isSitting
                ? frames[SIT_ROW * COLS + sitCol]
                : frames[facingRow * COLS + animFrame];
            batch.draw(
                frame,
                worldX + cam.cameraX,
                worldY + cam.cameraY,
                Main.TILE_SCALE / 2,
                Main.TILE_SCALE / 2
            );
        }
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public boolean isSitting() {
        return isSitting;
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
        if (boat_texture != null) {
            boat_texture.dispose();
            boat_texture = null;
        }
    }
}
