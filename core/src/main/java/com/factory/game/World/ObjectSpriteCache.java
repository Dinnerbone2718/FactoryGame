package com.factory.game.World;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class ObjectSpriteCache {

    public enum SelectionMode {
        RANDOM,
        FIXED,
        ANIMATED
    }

    public static final class SpriteConfig {
        public final String        path;
        public final int           cols, rows;
        public final SelectionMode mode;
        public final int           fixedIndex;
        public final float         drawWidth, drawHeight;
        public final float         offsetX,   offsetY;
        public final float         animFps;
        public final int           hitboxW,   hitboxH;
        public final int           hitboxOffX, hitboxOffY;
        public final int           tileWidth,  tileHeight;

        public SpriteConfig(String path, int cols, int rows,
                            SelectionMode mode, int fixedIndex,
                            int drawWidth, int drawHeight,
                            int offsetX,   int offsetY,
                            float animFps,
                            int hitboxW,   int hitboxH,
                            int hitboxOffX, int hitboxOffY,
                            int tileWidth,  int tileHeight) {
            this.path        = path;
            this.cols        = cols;
            this.rows        = rows;
            this.mode        = mode;
            this.fixedIndex  = fixedIndex;
            this.drawWidth   = drawWidth;
            this.drawHeight  = drawHeight;
            this.offsetX     = offsetX;
            this.offsetY     = offsetY;
            this.animFps     = animFps;
            this.hitboxW     = hitboxW;
            this.hitboxH     = hitboxH;
            this.hitboxOffX  = hitboxOffX;
            this.hitboxOffY  = hitboxOffY;
            this.tileWidth   = tileWidth;
            this.tileHeight  = tileHeight;
        }

        public SpriteConfig(String path, int cols, int rows,
                            SelectionMode mode, int fixedIndex,
                            int drawWidth, int drawHeight,
                            int offsetX,   int offsetY,
                            float animFps,
                            int hitboxW,   int hitboxH,
                            int hitboxOffX, int hitboxOffY) {
            this(path, cols, rows, mode, fixedIndex,
                 drawWidth, drawHeight, offsetX, offsetY, animFps,
                 hitboxW, hitboxH, hitboxOffX, hitboxOffY,
                 1, 1);
        }

        public SpriteConfig(String path, int cols, int rows,
                            SelectionMode mode, int fixedIndex,
                            int drawWidth, int drawHeight,
                            int offsetX,   int offsetY,
                            float animFps) {
            this(path, cols, rows, mode, fixedIndex,
                 drawWidth, drawHeight, offsetX, offsetY, animFps,
                 0, 0, 0, 0,
                 1, 1);
        }

        public SpriteConfig(String path, int cols, int rows,
                            SelectionMode mode, int fixedIndex,
                            int drawWidth, int drawHeight,
                            int offsetX,   int offsetY) {
            this(path, cols, rows, mode, fixedIndex,
                 drawWidth, drawHeight, offsetX, offsetY, 0f,
                 0, 0, 0, 0,
                 1, 1);
        }

        public boolean hasHitbox() { return hitboxW > 0 && hitboxH > 0; }
        public int totalFrames()   { return cols * rows; }
    }


    private static final Map<WorldObject.Type, SpriteConfig> CONFIGS =
            new EnumMap<>(WorldObject.Type.class);
    static {
        CONFIGS.put(WorldObject.Type.TREE, new SpriteConfig(
            "spritesheets/tree.png", 1, 1, SelectionMode.FIXED, 0,
            16, 24,
            0,  0,
            0f,
            8, 6, 4, 0,
            1, 1));

        CONFIGS.put(WorldObject.Type.GRASS, new SpriteConfig(
            "spritesheets/weed.png", 4, 4, SelectionMode.RANDOM,
            0, 16, 16, 0, 0));

        CONFIGS.put(WorldObject.Type.CAMPFIRE, new SpriteConfig(
            "spritesheets/campfire.png", 5, 1, SelectionMode.ANIMATED,
            0, 16, 16, 0, 0,
            8f,
            12, 8, 2, 0,
            1, 1));

        CONFIGS.put(WorldObject.Type.ROCK, new SpriteConfig(
            "spritesheets/rock.png", 4, 4, SelectionMode.RANDOM,
            0, 16, 16, 0, 0,
            0f,
            12, 4, 2, 0,
            1, 1));

        CONFIGS.put(WorldObject.Type.PALMTREE, new SpriteConfig(
            "spritesheets/palm_tree.png", 1, 1, SelectionMode.FIXED, 0,
            16, 24,
            0,  0,
            0,
            8, 6, 4, 0,
            1, 1));

        CONFIGS.put(WorldObject.Type.SEASHELLS, new SpriteConfig(
            "spritesheets/seashells.png", 4, 4, SelectionMode.RANDOM, 0,
            16, 16,
            0,  0));

        CONFIGS.put(WorldObject.Type.RUBBLE, new SpriteConfig(
            "spritesheets/rubble.png", 4, 4, SelectionMode.RANDOM, 0,
            16, 16,
            0,  0));

        CONFIGS.put(WorldObject.Type.CAVE, new SpriteConfig(
            "spritesheets/cave.png", 1, 1, SelectionMode.FIXED, 0,
            32, 32,
            0,  0,
            0,
            32, 4, 0, 0,
            2, 1));

        CONFIGS.put(WorldObject.Type.CAVE_EXIT, new SpriteConfig(
            "spritesheets/cave.png", 1, 1, SelectionMode.FIXED, 0,
            32, 32,
            0,  0,
            0,
            32, 4, 0, 0,
            2, 1));

        CONFIGS.put(WorldObject.Type.RAW_ORE, new SpriteConfig(
            "spritesheets/raw_ore.png", 4, 4, SelectionMode.RANDOM,
            0, 16, 16, 0, 0,
            0f,
            12, 4, 2, 0,
            1, 1));


        CONFIGS.put(WorldObject.Type.LANTERN, new SpriteConfig(
            "spritesheets/lantern.png", 3, 3, SelectionMode.ANIMATED,
            0, 16, 16, 0, 0,
            6f,
            12, 8, 2, 0,
            1, 1));

        CONFIGS.put(WorldObject.Type.SAND_CASTLE, new SpriteConfig(
            "spritesheets/sandcastle.png", 2, 2, SelectionMode.RANDOM, 0,
            16, 16,
            0,  0,
            0f,
            12, 4, 2, 0,
            1, 1));

        CONFIGS.put(WorldObject.Type.BUSH, new SpriteConfig(
            "spritesheets/bush.png", 2, 2, SelectionMode.RANDOM, 0,
            16, 16,
            0,  0,
            0f,
            10, 4, 3, 0,
            2, 1));

        CONFIGS.put(WorldObject.Type.SAND_PILE, new SpriteConfig(
            "spritesheets/sand_pile.png", 1, 1, SelectionMode.FIXED, 0,
            16, 16,
            0,  0,
            0f,
            12, 4, 2, 0,
            1, 1));

        CONFIGS.put(WorldObject.Type.SNOWITE, new SpriteConfig(
            "spritesheets/snowite.png", 3, 3, SelectionMode.RANDOM, 0,
            16, 16,
            0,  0));

        CONFIGS.put(WorldObject.Type.CACTI, new SpriteConfig(
            "spritesheets/cacti.png", 3, 2, SelectionMode.RANDOM, 0,
            16, 24,
            0,  0,
            0f,
            12, 4, 2, 0,
            1, 1));


        CONFIGS.put(WorldObject.Type.DEAD_BUSH, new SpriteConfig(
            "spritesheets/dead_bush.png", 2, 2, SelectionMode.RANDOM, 0,
            16, 16,
            0,  0,
            0f,
            6, 4, 5, 0,
            1, 1));



        CONFIGS.put(WorldObject.Type.QUALE, new SpriteConfig(
            "spritesheets/quale.png", 3, 3, SelectionMode.RANDOM, 0,
            16, 16,
            0,  0,
            0f,
            10, 4, 3, 0,
            1, 1));


        CONFIGS.put(WorldObject.Type.GOBLINO_HUT, new SpriteConfig(
            "spritesheets/goblino.png", 1, 1, SelectionMode.FIXED, 0,
            32, 32,
            0,  0,
            0f,
            30, 20, 1, 0,
            2, 2));

    }


    public static SpriteConfig getConfig(WorldObject.Type type) {
        return CONFIGS.get(type);
    }

    private static final Map<WorldObject.Type, TextureRegion[]> regions  =
            new EnumMap<>(WorldObject.Type.class);
    private static final Map<WorldObject.Type, Texture>         textures =
            new EnumMap<>(WorldObject.Type.class);
    private static final Map<WorldObject.Type, TextureRegion[]> normalRegions  =
            new EnumMap<>(WorldObject.Type.class);
    private static final Map<WorldObject.Type, Texture>         normalTextures =
            new EnumMap<>(WorldObject.Type.class);

    private static boolean initialised = false;

    public static Texture flatNormal;

    public static Texture       whitePixelTex;
    public static TextureRegion whitePixel;

    public static void init() {
        if (initialised) return;

        Pixmap npx = new Pixmap(1, 1, Pixmap.Format.RGB888);
        npx.setColor(0.5f, 0.5f, 1f, 1f);
        npx.fill();
        flatNormal = new Texture(npx);
        npx.dispose();

        Pixmap wpx = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        wpx.setColor(Color.WHITE);
        wpx.fill();
        whitePixelTex = new Texture(wpx);
        wpx.dispose();
        whitePixel = new TextureRegion(whitePixelTex);

        for (Map.Entry<WorldObject.Type, SpriteConfig> entry : CONFIGS.entrySet()) {
            WorldObject.Type type = entry.getKey();
            SpriteConfig     cfg  = entry.getValue();

            if (!Gdx.files.internal(cfg.path).exists()) continue;

            Texture tex   = new Texture(cfg.path);
            int     total = cfg.totalFrames();
            int     tileW = tex.getWidth()  / cfg.cols;
            int     tileH = tex.getHeight() / cfg.rows;

            TextureRegion[] arr = new TextureRegion[total];
            for (int i = 0; i < total; i++) {
                int col = i % cfg.cols;
                int row = i / cfg.cols;
                arr[i]  = new TextureRegion(tex, col * tileW, row * tileH, tileW, tileH);
            }

            textures.put(type, tex);
            regions.put(type, arr);

            String normalPath = cfg.path.replace(".png", "_n.png");
            if (Gdx.files.internal(normalPath).exists()) {
                Texture normalTex   = new Texture(normalPath);
                int     normalTileW = normalTex.getWidth()  / cfg.cols;
                int     normalTileH = normalTex.getHeight() / cfg.rows;
                TextureRegion[] normalArr = new TextureRegion[cfg.totalFrames()];
                for (int i = 0; i < normalArr.length; i++) {
                    normalArr[i] = new TextureRegion(normalTex,
                            (i % cfg.cols) * normalTileW,
                            (i / cfg.cols) * normalTileH,
                            normalTileW, normalTileH);
                }
                normalTextures.put(type, normalTex);
                normalRegions.put(type, normalArr);
            }
        }

        initialised = true;
    }


    public static int resolveSpriteIndex(WorldObject.Type type, Random rng) {
        SpriteConfig cfg = CONFIGS.get(type);
        if (cfg == null) return 0;
        switch (cfg.mode) {
            case FIXED:    return cfg.fixedIndex;
            case ANIMATED: return 0;
            default:       return rng.nextInt(cfg.totalFrames());
        }
    }

    public static TextureRegion getRegion(WorldObject.Type type, int index) {
        TextureRegion[] arr = regions.get(type);
        if (arr == null) {
            Gdx.app.error("ObjectSpriteCache", "No regions loaded for type: " + type);
            return null;
        }
        if (index < 0 || index >= arr.length) return arr[0];
        return arr[index];
    }

    public static TextureRegion getNormalRegion(WorldObject.Type type, int index) {
        TextureRegion[] arr = normalRegions.get(type);
        if (arr == null) return null;
        if (index < 0 || index >= arr.length) return arr[0];
        return arr[index];
    }

    public static void dispose() {
        if (flatNormal     != null) { flatNormal.dispose();     flatNormal     = null; }
        if (whitePixelTex  != null) { whitePixelTex.dispose();  whitePixelTex  = null; whitePixel = null; }
        for (Texture tex : textures.values())       tex.dispose();
        for (Texture tex : normalTextures.values()) tex.dispose();
        textures.clear();
        normalTextures.clear();
        regions.clear();
        normalRegions.clear();
        initialised = false;
    }

    private ObjectSpriteCache() {}
}