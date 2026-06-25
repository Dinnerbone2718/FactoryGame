package com.factory.game;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Minimap extends InputAdapter {

    private static final int   WORLD_TILES = Main.WORLD_CHUNKS * Main.CHUNK_SIZE;
    private static final int   CORNER_SIZE = 180;
    private static final int   CORNER_PAD  = 14;
    private static final int   DOT_SIZE    = 6;

    private static final float CORNER_MIN  = 20f;
    private static final float CORNER_MAX  = 160f;

    private static final float FS_MIN      = 24f;
    private static final float FS_MAX      = WORLD_TILES;

    private static final float ZOOM_STEP   = 1.17f;

    private static final float PAN_TILES_PER_SEC = 1.8f;

    private static final int C_WATER     = rgba(0x1a, 0x78, 0xd6, 0xff);
    private static final int C_DESERT    = rgba(0xff, 0xce, 0x91, 0xff);
    private static final int C_FOREST    = rgba(0x3b, 0x7d, 0x4f, 0xff);
    private static final int C_GRASS     = rgba(0xa4, 0xc4, 0x43, 0xff);
    private static final int C_MOUNTAIN  = rgba(0xa3, 0xa7, 0xc2, 0xff);
    private static final int C_SWAMP     = rgba(0x2a, 0x59, 0x4f, 0xff);
    private static final int C_RIVER     = rgba(0x2e, 0x9c, 0xe8, 0xff);
    private static final int C_RIVERBANK = rgba(0x8b, 0x73, 0x45, 0xff);
    private static final int C_SNOW      = rgba(0xe8, 0xee, 0xf8, 0xff);
    private static final int C_BADLAND   = rgba(0x8b, 0x6f, 0x4e, 0xff);
    private static final int C_UNKNOWN   = rgba(0x18, 0x18, 0x18, 0xff);

    private static final double TERRAIN_SCALE        = 0.0025;
    private static final double BIOME_SCALE          = 0.0015;
    private static final double WATER_MAX            = 0.15;
    private static final double BEACH_MAX            = 0.17;
    private static final double MOUNTAIN_MIN         = 0.35;
    private static final double SNOW_MIN             = 0.52;
    private static final double FOREST_MIN           =  0.17;
    private static final double GRASS_MIN            = -0.15;

    private static final double RIVER_SCALE           = 0.006;
    private static final double RIVER_WARP_SCALE      = 0.0015;
    private static final double RIVER_WARP_AMP        = 60.0;
    private static final double RIVER_THRESHOLD       = 0.05;
    private static final double BANK_THRESHOLD        = 0.075;

    private static final long RIVER_SEED  = 0xCAFEBABE12345678L;
    private static final long WARP_SEED_X = 0xABCDEF1234567890L;
    private static final long WARP_SEED_Y = 0x0FEDCBA987654321L;

    private static class TexPatch {
        final int    texX, texY;
        final Pixmap pixmap;
        TexPatch(int texX, int texY, Pixmap pixmap) {
            this.texX = texX;
            this.texY = texY;
            this.pixmap = pixmap;
        }
    }

    private final ConcurrentLinkedQueue<TexPatch> pendingPatches = new ConcurrentLinkedQueue<>();

    private       Texture         mapTexture;
    private final Texture         white;

    private final ExecutorService bakePool = Executors.newSingleThreadExecutor();

    private boolean fullscreen = false;
    private boolean mWasDown   = false;

    private int screenW, screenH;

    private float cornerViewTiles = 80f;

    private float viewCX, viewCY;
    private float fsViewTiles = FS_MAX;

    private boolean dragging;
    private int     dragStartScreenX, dragStartScreenY;
    private float   dragStartViewCX,  dragStartViewCY;

    private float lastPlayerTileX, lastPlayerTileY;

    public Minimap(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;

        Pixmap blank = new Pixmap(WORLD_TILES, WORLD_TILES, Pixmap.Format.RGBA8888);
        blank.setColor(0.094f, 0.094f, 0.094f, 1f);
        blank.fill();
        mapTexture = new Texture(blank);
        mapTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        blank.dispose();

        Pixmap whitePix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        whitePix.setColor(1f, 1f, 1f, 1f);
        whitePix.fill();
        white = new Texture(whitePix);
        whitePix.dispose();
    }

    public void prebakeWorld(long seed, int noiseOffsetX, int noiseOffsetY) {
        bakePool.submit(() -> {
            int chunkSize = Main.CHUNK_SIZE;
            int stripH    = chunkSize;

            for (int stripY = 0; stripY < WORLD_TILES; stripY += stripH) {
                int rows  = Math.min(stripH, WORLD_TILES - stripY);
                Pixmap strip = new Pixmap(WORLD_TILES, rows, Pixmap.Format.RGBA8888);

                for (int wx = 0; wx < WORLD_TILES; wx++) {
                    for (int localY = 0; localY < rows; localY++) {
                        int wy = stripY + localY;
                        String biome = computeBiome(wx, wy, noiseOffsetX, noiseOffsetY, seed);
                        strip.drawPixel(wx, (rows - 1 - localY), terrainColor(biome));
                    }
                }

                int texY = WORLD_TILES - stripY - rows;
                pendingPatches.add(new TexPatch(0, texY, strip));
            }
        });
    }

    private static String computeBiome(int wx, int wy, int nox, int noy, long seed) {
        double terrain = PerlinNoise.noise(
                (wx + nox) * TERRAIN_SCALE,
                (wy + noy) * TERRAIN_SCALE,
                seed);
        double biome = PerlinNoise.noise(
                (wx + nox) * BIOME_SCALE,
                (wy + noy) * BIOME_SCALE,
                seed ^ 0x9E3779B97F4A7C15L);

        String type;
        if (terrain < WATER_MAX) {
            type = "water";
        } else if (terrain < BEACH_MAX) {
            type = "desert";
        } else if (terrain > MOUNTAIN_MIN) {
            type = (terrain > SNOW_MIN) ? "snow" : "mountain";
        } else if (biome > FOREST_MIN) {
            type = "forest";
        } else if (biome > GRASS_MIN) {
            type = "grass";
        } else{
            type = "badland";
        }

        if (!type.equals("water")) {
            double rv = riverValue(wx, wy, nox, noy, seed);
            double elevFactor = riverElevFactor(terrain);
            double effectiveRiver = RIVER_THRESHOLD * elevFactor;
            double effectiveBank  = BANK_THRESHOLD  * elevFactor;
            if (rv < effectiveRiver) {
                type = "water";
            } else if (rv < effectiveBank && !type.equals("mountain") && !type.equals("snow") && !type.equals("desert") && !type.equals("badland")) {
                type = "swamp";
            }
        }

        return type;
    }

    private static double riverElevFactor(double terrainNoise) {
        if (terrainNoise >= MOUNTAIN_MIN) return 0.0;
        if (terrainNoise <= BEACH_MAX)    return 0.0;
        double t = (terrainNoise - BEACH_MAX) / (MOUNTAIN_MIN - BEACH_MAX);
        return 1.0 - t * t;
    }

    private static double riverValue(int wx, int wy, int nox, int noy, long seed) {
        long rs = seed ^ RIVER_SEED;
        double px = (wx + nox) * RIVER_WARP_SCALE;
        double py = (wy + noy) * RIVER_WARP_SCALE;

        double warpX = PerlinNoise.noise(px,        py,        seed ^ WARP_SEED_X) * RIVER_WARP_AMP;
        double warpY = PerlinNoise.noise(px + 5.2,  py + 1.3,  seed ^ WARP_SEED_Y) * RIVER_WARP_AMP;

        return Math.abs(PerlinNoise.noise(
                (wx + nox + warpX) * RIVER_SCALE,
                (wy + noy + warpY) * RIVER_SCALE,
                rs));
    }


    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        if (fullscreen) {
            viewCX      = lastPlayerTileX;
            viewCY      = lastPlayerTileY;
            fsViewTiles = FS_MAX;
            dragging    = false;
        }
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void onChunkLoaded(int cx, int cy, String[] types) {
        int size = Main.CHUNK_SIZE;

        Pixmap patch = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        for (int tx = 0; tx < size; tx++) {
            for (int ty = 0; ty < size; ty++) {
                patch.drawPixel(tx, (size - 1 - ty), terrainColor(types[tx + ty * size]));
            }
        }

        int texX =  cx * size;
        int texY = WORLD_TILES - (cy + 1) * size;
        pendingPatches.add(new TexPatch(texX, texY, patch));
    }

    public void handleInput(boolean uiBlocking) {
        if (uiBlocking) {
            fullscreen = false;
            mWasDown   = true;
            dragging   = false;
            return;
        }

        boolean mDown = Gdx.input.isKeyPressed(Input.Keys.M);
        if (mDown && !mWasDown) {
            fullscreen = !fullscreen;
            if (fullscreen) {
                viewCX      = lastPlayerTileX;
                viewCY      = lastPlayerTileY;
                fsViewTiles = FS_MAX;
                dragging    = false;
            }
        }
        mWasDown = mDown;

        if (fullscreen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            fullscreen = false;
            dragging   = false;
        }

        if (!fullscreen) return;

        float delta    = Gdx.graphics.getDeltaTime();
        float panSpeed = fsViewTiles * PAN_TILES_PER_SEC * delta;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  viewCX -= panSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) viewCX += panSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    viewCY += panSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  viewCY -= panSpeed;

        viewCX = clamp(viewCX, 0, WORLD_TILES);
        viewCY = clamp(viewCY, 0, WORLD_TILES);

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            viewCX = lastPlayerTileX;
            viewCY = lastPlayerTileY;
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        float factor = (amountY > 0f) ? ZOOM_STEP : (1f / ZOOM_STEP);
        if (fullscreen) {
            fsViewTiles = clamp(fsViewTiles * factor, FS_MIN, FS_MAX);
        } else {
            cornerViewTiles = clamp(cornerViewTiles * factor, CORNER_MIN, CORNER_MAX);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!fullscreen || button != Input.Buttons.LEFT) return false;
        dragging         = true;
        dragStartScreenX = screenX;
        dragStartScreenY = screenY;
        dragStartViewCX  = viewCX;
        dragStartViewCY  = viewCY;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!dragging) return false;

        float margin = 0.04f;
        float areaW  = screenW * (1f - margin * 2f);
        float areaH  = screenH * (1f - margin * 2f);
        float ppt    = Math.min(areaW, areaH) / fsViewTiles;

        float dxPx = screenX - dragStartScreenX;
        float dyPx = screenY - dragStartScreenY;

        viewCX = clamp(dragStartViewCX - dxPx / ppt, 0, WORLD_TILES);
        viewCY = clamp(dragStartViewCY + dyPx / ppt, 0, WORLD_TILES);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) dragging = false;
        return false;
    }

    public void render(SpriteBatch batch, float playerWorldX, float playerWorldY) {
        int uploadsThisFrame = 0;
        TexPatch patch;
        while (uploadsThisFrame < 4 && (patch = pendingPatches.poll()) != null) {
            mapTexture.draw(patch.pixmap, patch.texX, patch.texY);
            patch.pixmap.dispose();
            uploadsThisFrame++;
        }

        lastPlayerTileX = playerWorldX / Main.TILE_SCALE;
        lastPlayerTileY = playerWorldY / Main.TILE_SCALE;

        if (fullscreen) {
            drawFullscreen(batch, lastPlayerTileX, lastPlayerTileY);
        } else {
            drawCorner(batch, lastPlayerTileX, lastPlayerTileY);
        }
    }

    public Texture getMapTexture() {
        return mapTexture;
    }

    public void resize(int w, int h) {
        screenW = w;
        screenH = h;
    }

    public void dispose() {
        bakePool.shutdownNow();
        TexPatch patch;
        while ((patch = pendingPatches.poll()) != null) patch.pixmap.dispose();
        mapTexture.dispose();
        white.dispose();
    }

    private void drawCorner(SpriteBatch batch, float tileX, float tileY) {
        float drawX = screenW - CORNER_SIZE - CORNER_PAD;
        float drawY = screenH - CORNER_SIZE - CORNER_PAD;

        int border = 2;
        batch.setColor(0f, 0f, 0f, 0.88f);
        batch.draw(white, drawX - border, drawY - border,
                CORNER_SIZE + border * 2f, CORNER_SIZE + border * 2f);

        int viewT      = Math.max(1, Math.round(cornerViewTiles));
        int playerPixY = WORLD_TILES - 1 - (int) tileY;

        int srcX = (int) tileX - viewT / 2;
        int srcY = playerPixY  - viewT / 2;
        srcX = Math.max(0, Math.min(WORLD_TILES - viewT, srcX));
        srcY = Math.max(0, Math.min(WORLD_TILES - viewT, srcY));

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(mapTexture, drawX, drawY, CORNER_SIZE, CORNER_SIZE,
                srcX, srcY, viewT, viewT, false, false);

        float dotFracX    = ((int) tileX - srcX) / (float) viewT;
        float dotFracTopY = (playerPixY  - srcY) / (float) viewT;
        float dotScreenX  = drawX + dotFracX           * CORNER_SIZE - DOT_SIZE / 2f;
        float dotScreenY  = drawY + (1f - dotFracTopY) * CORNER_SIZE - DOT_SIZE / 2f;

        batch.setColor(0f, 0f, 0f, 0.85f);
        batch.draw(white, dotScreenX - 1, dotScreenY - 1, DOT_SIZE + 2f, DOT_SIZE + 2f);
        batch.setColor(1f, 0.18f, 0.18f, 1f);
        batch.draw(white, dotScreenX, dotScreenY, DOT_SIZE, DOT_SIZE);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawFullscreen(SpriteBatch batch, float playerTileX, float playerTileY) {
        batch.setColor(0f, 0f, 0f, 0.78f);
        batch.draw(white, 0f, 0f, screenW, screenH);

        float margin = 0.04f;
        float areaX  = screenW * margin;
        float areaY  = screenH * margin;
        float areaW  = screenW * (1f - margin * 2f);
        float areaH  = screenH * (1f - margin * 2f);

        float ppt  = Math.min(areaW, areaH) / fsViewTiles;
        float visW = areaW / ppt;
        float visH = areaH / ppt;

        float capW   = Math.min(visW, WORLD_TILES);
        float capH   = Math.min(visH, WORLD_TILES);
        float drawW  = areaW * (capW / visW);
        float drawH  = areaH * (capH / visH);
        float drawX  = areaX + (areaW - drawW) * 0.5f;
        float drawY  = areaY + (areaH - drawH) * 0.5f;

        float viewPixCX = viewCX;
        float viewPixCY = WORLD_TILES - 1 - viewCY;

        float srcX = viewPixCX - capW / 2f;
        float srcY = viewPixCY - capH / 2f;
        srcX = clamp(srcX, 0, Math.max(0f, WORLD_TILES - capW));
        srcY = clamp(srcY, 0, Math.max(0f, WORLD_TILES - capH));

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(mapTexture,
                drawX, drawY, drawW, drawH,
                (int) srcX, (int) srcY, (int) capW, (int) capH,
                false, false);

        int bdr = 2;
        batch.setColor(0.55f, 0.55f, 0.55f, 0.7f);
        batch.draw(white, drawX - bdr,        drawY - bdr,        drawW + bdr * 2f, bdr);
        batch.draw(white, drawX - bdr,        drawY + drawH,      drawW + bdr * 2f, bdr);
        batch.draw(white, drawX - bdr,        drawY,              bdr,              drawH);
        batch.draw(white, drawX + drawW,      drawY,              bdr,              drawH);

        float playerPixX = playerTileX;
        float playerPixY = WORLD_TILES - 1 - playerTileY;
        float fracX      = (playerPixX - srcX) / capW;
        float fracY      = (playerPixY - srcY) / capH;

        if (fracX >= 0f && fracX <= 1f && fracY >= 0f && fracY <= 1f) {
            int   bigDot = DOT_SIZE * 2;
            float dotSX  = drawX + fracX        * drawW - bigDot / 2f;
            float dotSY  = drawY + (1f - fracY) * drawH - bigDot / 2f;

            batch.setColor(0f, 0f, 0f, 0.85f);
            batch.draw(white, dotSX - 1, dotSY - 1, bigDot + 2f, bigDot + 2f);
            batch.setColor(1f, 0.18f, 0.18f, 1f);
            batch.draw(white, dotSX, dotSY, bigDot, bigDot);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static int terrainColor(String type) {
        if (type == null) return C_UNKNOWN;
        switch (type) {
            case "water":     return C_WATER;
            case "desert":    return C_DESERT;
            case "forest":    return C_FOREST;
            case "grass":     return C_GRASS;
            case "mountain":  return C_MOUNTAIN;
            case "swamp":     return C_SWAMP;
            case "river":     return C_RIVER;
            case "riverbank": return C_RIVERBANK;
            case "snow":      return C_SNOW;
            case "badland":   return C_BADLAND;
            default:          return C_UNKNOWN;
        }
    }

    private static int rgba(int r, int g, int b, int a) {
        return (r << 24) | (g << 16) | (b << 8) | a;
    }
}