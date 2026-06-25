package com.factory.game.World;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class PlanterSpriteCache {

    public static final int SHEET_COLS  = 3;
    public static final int SHEET_ROWS  = 3;
    public static final int FRAME_COUNT = 9;
    public static final int CELL_W      = 32;
    public static final int CELL_H      = 16;

    private static final Map<String, Texture>         textures = new HashMap<>();
    private static final Map<String, TextureRegion[]> regions  = new HashMap<>();

    private PlanterSpriteCache() {}

    public static void init() {
        for (com.factory.game.Items.PlanterRecipe recipe :
                com.factory.game.Items.CraftingManager.getAllPlanterRecipes()) {
            String path = recipe.getGrowSheetPath();
            if (path == null || regions.containsKey(path)) continue;
            load(path);
        }
    }

    private static void load(String path) {
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("PlanterSpriteCache",
                    "Grow sheet not found: " + path + " — will fall back to pot sprite.");
            return;
        }
        Texture tex = new Texture(path);
        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            int col = i % SHEET_COLS;
            int row = i / SHEET_COLS;
            frames[i] = new TextureRegion(tex, col * CELL_W, row * CELL_H, CELL_W, CELL_H);
        }
        textures.put(path, tex);
        regions.put(path, frames);
    }

    public static void dispose() {
        for (Texture t : textures.values()) t.dispose();
        textures.clear();
        regions.clear();
    }

    public static TextureRegion getFrame(String sheetPath, int growFrame) {
        if (sheetPath == null) return null;
        TextureRegion[] frames = regions.get(sheetPath);
        if (frames == null) return null;
        return frames[Math.max(0, Math.min(FRAME_COUNT - 1, growFrame))];
    }
}
