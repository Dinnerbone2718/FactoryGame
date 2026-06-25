package com.factory.game.World;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.factory.game.Camera;
import com.factory.game.Items.ItemClass;
import com.factory.game.Items.ItemStack;
import com.factory.game.Main;
import com.factory.game.Renderer.FireParticleEmitter;
import com.factory.game.Renderer.LightRenderer;
import com.factory.game.Renderer.LightSource;

public class WorldObject {

    public enum Type { TREE, ROCK, BUSH, GRASS, CAMPFIRE, PALMTREE, SEASHELLS, RUBBLE, CAVE, CAVE_EXIT, RAW_ORE, LANTERN, SAND_CASTLE, SNOWITE, SAND_PILE, CACTI, DEAD_BUSH, QUALE, GOBLINO_HUT }


    public final Type type;
    private final int x, y;
    private final int spriteIndex;


    private LightSource               lightSource;
    private Body                      shadowBody;
    private final FireParticleEmitter particleEmitter;

    private int   animFrame = 0;
    private float animTimer = 0f;


    private static final float SHAKE_DURATION  = 0.28f;
    private static final float SHAKE_MAGNITUDE = 5.5f;

    private float shakeTimer = 0f;


    private int hitsReceived = 0;


    public WorldObject(Type type, int x, int y, int spriteIndex) {
        this.type        = type;
        this.x           = x;
        this.y           = y;
        this.spriteIndex = spriteIndex;

        if (type == Type.CAMPFIRE) {
            long seed = ((long) x * 0xDEAD_BEEFL) ^ ((long) y * 0x1337_CAFEL);
            float cx  = x * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float cy  = y * Main.TILE_SCALE;
            particleEmitter = new FireParticleEmitter(cx, cy, seed);
        } else {
            particleEmitter = null;
        }
    }


    public HarvestResult tryHit(ItemClass toolClass) {
        HarvestDefinition def = HarvestRegistry.get(type);
        if (def == null)                return HarvestResult.NOT_HARVESTABLE;
        if (!def.canHarvest(toolClass)) return HarvestResult.WRONG_TOOL;

        hitsReceived++;
        shakeTimer = SHAKE_DURATION;

        if (hitsReceived >= def.getHitsRequired()) {
            return HarvestResult.DESTROYED;
        }
        return HarvestResult.HIT_REGISTERED;
    }

    public void resetHits() {
        hitsReceived = 0;
        shakeTimer   = 0f;
    }

    public List<ItemStack> generateDrops(Random rng) {
        HarvestDefinition def = HarvestRegistry.get(type);
        if (def == null || def.getDropTable().isEmpty()) return Collections.emptyList();
        return def.getDropTable().roll(rng);
    }

    public float getHitProgress() {
        HarvestDefinition def = HarvestRegistry.get(type);
        if (def == null || def.getHitsRequired() <= 0) return 0f;
        return (float) hitsReceived / def.getHitsRequired();
    }

    public int     getHitsReceived() { return hitsReceived; }
    public boolean isHarvestable()   { return HarvestRegistry.isHarvestable(type); }


    public void attachShadowBody(World world) {
        if (shadowBody != null) return;

        ObjectSpriteCache.SpriteConfig cfg = ObjectSpriteCache.getConfig(type);
        if (cfg == null || !cfg.hasHitbox()) return;

        float scale = Main.TILE_SCALE / 16f;
        float hw    = cfg.hitboxW    * scale * 0.5f;
        float hh    = cfg.hitboxH    * scale * 0.5f;
        float bodyX = x * Main.TILE_SCALE + cfg.hitboxOffX * scale + hw;
        float bodyY = y * Main.TILE_SCALE + cfg.hitboxOffY * scale + hh;

        BodyDef def  = new BodyDef();
        def.type     = BodyDef.BodyType.StaticBody;
        def.position.set(bodyX, bodyY);
        shadowBody   = world.createBody(def);

        PolygonShape box = new PolygonShape();
        box.setAsBox(hw, hh);

        FixtureDef fixture = new FixtureDef();
        fixture.shape    = box;
        fixture.isSensor = false;
        shadowBody.createFixture(fixture);
        box.dispose();
    }

    public void destroyShadowBody(World world) {
        if (shadowBody == null) return;
        world.destroyBody(shadowBody);
        shadowBody = null;
    }


    public void draw(Batch batch, Camera cam) {
        ObjectSpriteCache.SpriteConfig cfg = ObjectSpriteCache.getConfig(type);

        float delta = Gdx.graphics.getDeltaTime();
        shakeTimer = Math.max(0f, shakeTimer - delta);

        int frameIndex;
        if (cfg != null && cfg.mode == ObjectSpriteCache.SelectionMode.ANIMATED) {
            animTimer += delta;
            float frameDuration = 1f / cfg.animFps;
            while (animTimer >= frameDuration) {
                animTimer -= frameDuration;
                animFrame  = (animFrame + 1) % cfg.totalFrames();
            }
            frameIndex = animFrame;
        } else {
            frameIndex = spriteIndex;
        }

        float shakeX = 0f, shakeY = 0f;
        if (shakeTimer > 0f) {
            float t     = (float) (Math.random() * 2 - 1);
            float decay = shakeTimer / SHAKE_DURATION;
            shakeX = (float) Math.sin(t * Math.PI) * SHAKE_MAGNITUDE * decay;
            shakeY = (float) Math.cos(t * Math.PI) * SHAKE_MAGNITUDE * decay * 0.5f;
        }

        float drawX = x * Main.TILE_SCALE + cam.cameraX
                    + (cfg != null ? cfg.offsetX / 16f * Main.TILE_SCALE : 0)
                    + shakeX;
        float drawY = y * Main.TILE_SCALE + cam.cameraY
                    + (cfg != null ? cfg.offsetY / 16f * Main.TILE_SCALE : 0)
                    + shakeY;
        float drawW = cfg != null ? cfg.drawWidth  / 16f * Main.TILE_SCALE : Main.TILE_SCALE;
        float drawH = cfg != null ? cfg.drawHeight / 16f * Main.TILE_SCALE : Main.TILE_SCALE;

        TextureRegion region = ObjectSpriteCache.getRegion(type, frameIndex);
        if (region == null) return;

        TextureRegion normalRegion = ObjectSpriteCache.getNormalRegion(type, frameIndex);
        ShaderProgram shader = (ShaderProgram) batch.getShader();

        batch.flush();
        shader.setUniformi("u_isWater", 0);
        if (normalRegion != null) {
            normalRegion.getTexture().bind(1);
            shader.setUniformi("u_hasNormalMap", 1);
        } else {
            ObjectSpriteCache.flatNormal.bind(1);
            shader.setUniformi("u_hasNormalMap", 0);
        }
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        if (particleEmitter != null) {
            particleEmitter.update(delta);
            particleEmitter.draw(batch, ObjectSpriteCache.whitePixel, cam);
        }

        batch.draw(region, drawX, drawY, drawW, drawH);
    }


    public void attachLight(LightRenderer lightRenderer) {
        if (lightSource != null) return;
        if (type == Type.CAMPFIRE || type == Type.LANTERN){

        float worldPixelX = x * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = y * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;

        lightSource = lightRenderer.addPointLight(
            worldPixelX, worldPixelY,
            new Color(1f, 0.45f, 0.12f, .9f),
            8f * Main.TILE_SCALE,
            64);

        }
        else if (type == Type.CAVE_EXIT){

        float worldPixelX = x * Main.TILE_SCALE + Main.TILE_SCALE * 1f;
        float worldPixelY = y * Main.TILE_SCALE + Main.TILE_SCALE * -.2f;

        lightSource = lightRenderer.addPointLight(
            worldPixelX, worldPixelY,
            new Color(.25f, 0.25f, 0.25f, .8f),
            8f * Main.TILE_SCALE,
            64);

        }

    }

    public void detachLight(LightRenderer lightRenderer) {
        if (lightSource == null) return;
        lightRenderer.removeLight(lightSource);
        lightSource = null;
    }


    public int     getX()          { return x; }
    public int     getY()          { return y; }
    public boolean hasShadowBody() { return shadowBody != null; }
}