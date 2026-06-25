package com.factory.game.Items;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.factory.game.Camera;
import com.factory.game.Main;
import com.factory.game.World.ObjectSpriteCache;

public class DroppedItem {
    private final ItemStack itemStack;

    private float worldX;
    private float worldY;
    private float vx;
    private float vy;
    private float scale;
    private final long spawnTime;

    private static final float PICKUP_RADIUS = 32f;
    private static final float ITEM_SIZE     = 24f;
    private static final float FRICTION      = 7f;
    private static final float SCALE_SPEED   = 8f;
    private static final float BURST_SPEED   = 90f;
    private static final float THROW_SPEED   = 110f;

    private static final Random rng = new Random();



    private final BitmapFont    font;

    public DroppedItem(ItemStack itemStack, float worldX, float worldY, float vx, float vy) {
        this.itemStack = itemStack.copy();
        this.worldX    = worldX;
        this.worldY    = worldY;
        this.vx        = vx;
        this.vy        = vy;
        this.scale     = 0f;
        this.spawnTime = System.currentTimeMillis();



        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 16;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();


    }

    public DroppedItem(ItemStack itemStack, float worldX, float worldY) {
        this(itemStack, worldX, worldY, 0f, 0f);
    }

    public static DroppedItem spawnBurst(ItemStack itemStack, float centerX, float centerY) {
        float angle = rng.nextFloat() * (float) (Math.PI * 2);
        float speed = BURST_SPEED * (0.6f + rng.nextFloat() * 0.6f);
        return new DroppedItem(
            itemStack, centerX, centerY,
            (float) Math.cos(angle) * speed,
            (float) Math.sin(angle) * speed
        );
    }


    public static DroppedItem spawnThrow(ItemStack itemStack,
                                         float worldX,  float worldY,
                                         float dirX,    float dirY) {
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len > 0.001f) { dirX /= len; dirY /= len; }
        float spread = (rng.nextFloat() - 0.5f) * 0.3f;
        float cos    = (float) Math.cos(spread);
        float sin    = (float) Math.sin(spread);
        float rx     = dirX * cos - dirY * sin;
        float ry     = dirX * sin + dirY * cos;
        return new DroppedItem(itemStack, worldX, worldY, rx * THROW_SPEED, ry * THROW_SPEED);
    }


    public void update(float delta) {
        if (scale < 1f) scale = Math.min(1f, scale + SCALE_SPEED * delta);

        if (Math.abs(vx) > 0.5f || Math.abs(vy) > 0.5f) {
            worldX += vx * delta;
            worldY += vy * delta;

            float speed    = (float) Math.sqrt(vx * vx + vy * vy);
            float newSpeed = Math.max(0f, speed - FRICTION * speed * delta);
            float ratio    = newSpeed / speed;
            vx *= ratio;
            vy *= ratio;
        } else {
            vx = 0f;
            vy = 0f;
        }
    }

    public boolean canPickup(float playerX, float playerY) {
        float dx            = worldX - (playerX + Main.TILE_SCALE / 2f);
        float dy            = worldY - (playerY + Main.TILE_SCALE / 2f);
        float distSq        = dx * dx + dy * dy;
        long  timeSinceSpawn = System.currentTimeMillis() - spawnTime;
        return distSq <= PICKUP_RADIUS * PICKUP_RADIUS && timeSinceSpawn > 500;
    }


    public void draw(Batch batch, Camera cam) {
        TextureRegion region = ItemTextureCache.getTexture(itemStack.getItem());
        if (region == null) return;

        ShaderProgram shader = (ShaderProgram) batch.getShader();
        batch.flush();

        shader.setUniformi("u_isWater",      0);
        shader.setUniformi("u_hasNormalMap", 0);
        shader.setUniformi("u_isItem",       1);

        shader.setUniformf("u_texPixelSize",
            1f / region.getTexture().getWidth(),
            1f / region.getTexture().getHeight());

        ObjectSpriteCache.flatNormal.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        float drawSize = ITEM_SIZE * scale;
        float bob      = (float) Math.sin(System.currentTimeMillis() / 1000f * 2f) * 3f * scale;
        float drawX    = worldX + cam.cameraX - drawSize / 2f;
        float drawY    = worldY + cam.cameraY - drawSize / 2f + bob;

        batch.draw(region, drawX, drawY, drawSize, drawSize);

        batch.flush();
        shader.setUniformi("u_isItem", 0);





        if (itemStack.getQuantity() > 1){
        font.getData().setScale(1.5f);
        font.setColor(0f, 0.0f, 0.0f, 1f);
        font.draw(batch, String.valueOf(itemStack.getQuantity() + "x"), drawX + drawSize * .5f, drawY + drawSize * .5f);
        }


    }


    public ItemStack getItemStack() { return itemStack; }
    public float getWorldX()        { return worldX;    }
    public float getWorldY()        { return worldY;    }
    public float getRenderY()       { return worldY;    }
}