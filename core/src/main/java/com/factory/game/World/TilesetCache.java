package com.factory.game.World;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class TilesetCache {

    private static final int TILE_W = 16;
    private static final int TILE_H = 16;


    private static final String[][] TILE_DEFS = {
        { "grass",  "tileset/Grass/Grass.png",   "11" },
        { "water",  "tileset/Water/Water.png",    "11" },
        { "forest", "tileset/Forest/Forest.png",  "11" },
        { "desert", "tileset/Desert/Desert.png",  "11" },
        { "swamp", "tileset/Swamp/Swamp.png", "11"},
        {"mountain", "tileset/Mountain/Mountain.png", "11"},
        {"badland", "tileset/Wasteland/Wasteland.png", "11"},
        {"snow", "tileset/Snow/Snow.png", "11"},

        {"cave", "tileset/Cave/Cave.png", "11"}
    };


    private static final Map<String, Texture>         textures = new HashMap<>();
    private static final Map<String, TextureRegion[]> regions  = new HashMap<>();
    private static final Map<String, TextureRegion[]> normalRegions = new HashMap<>();

    private static boolean initialised = false;


    public static void init() {
        if (initialised) return;

        for (String[] def : TILE_DEFS) {
            String type        = def[0];
            String path        = def[1];
            int    tilesPerRow = Integer.parseInt(def[2]);

            Texture tex = new Texture(path);
            textures.put(type, tex);

            int cols        = tilesPerRow;
            int rows        = tex.getHeight() / TILE_H;
            int totalTiles  = cols * rows;

            TextureRegion[] arr = new TextureRegion[totalTiles];
            for (int i = 0; i < totalTiles; i++) {
                int col = i % cols;
                int row = i / cols;
                arr[i]  = new TextureRegion(tex, col * TILE_W, row * TILE_H, TILE_W, TILE_H);
            }
            regions.put(type, arr);

            String normalPath = path.replace(".png", "_n.png");
            if (Gdx.files.internal(normalPath).exists()) {
                Texture normalTex  = new Texture(normalPath);
                textures.put(type + "_n", normalTex);
                int normalCols     = tilesPerRow;
                int normalRows     = normalTex.getHeight() / TILE_H;
                TextureRegion[] normalArr = new TextureRegion[normalCols * normalRows];
                for (int i = 0; i < normalArr.length; i++) {
                    normalArr[i] = new TextureRegion(normalTex,
                            (i % normalCols) * TILE_W,
                            (i / normalCols) * TILE_H,
                            TILE_W, TILE_H);
                }
                normalRegions.put(type, normalArr);
            }
        }

        initialised = true;
    }

    public static TextureRegion getRegion(String tileType, int index) {
        TextureRegion[] arr = regions.get(tileType);
        if (arr == null) return getFallback();
        if (index < 0 || index >= arr.length) return arr[0];
        return arr[index];
    }

    public static void dispose() {
        for (Texture tex : textures.values()) tex.dispose();
        textures.clear();
        regions.clear();
        normalRegions.clear();
        initialised = false;
    }


    public static TextureRegion getNormalRegion(String tileType, int index) {
        TextureRegion[] arr = normalRegions.get(tileType);
        if (arr == null) return null;
        if (index < 0 || index >= arr.length) return arr[0];
        return arr[index];
    }

    private static TextureRegion fallbackRegion = null;

    private static TextureRegion getFallback() {
        if (fallbackRegion == null) {
            Texture t = new Texture("tileset/grass.png");
            textures.put("__fallback__", t);
            fallbackRegion = new TextureRegion(t);
        }
        return fallbackRegion;
    }

    private TilesetCache() {} 
}