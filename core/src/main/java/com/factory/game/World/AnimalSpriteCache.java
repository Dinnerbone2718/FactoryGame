package com.factory.game.World;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class AnimalSpriteCache {

    public enum Kind {
        COW("animals/cow.png", 1.5f, 2.0f),
        ARMADILLO("animals/armadillo.png", 1, 1),
        WOLF("animals/wolf.png", 1.5f, 2.0f)
        ;

        public final String path;
        public final float  drawScaleW;
        public final float  drawScaleH;

        Kind(String path, float drawScaleW, float drawScaleH) {
            this.path       = path;
            this.drawScaleW = drawScaleW;
            this.drawScaleH = drawScaleH;
        }
    }

    public enum Diet {

        HERBIVORE("Herbivore", 1),
        OMNIVORE("Omnivore", 2),
        CARNIVORE("Carnivore", 3);

        public final String name;
        public final int option;


        Diet(String name, int option){
            this.name   = name;
            this.option = option;
        }
    }

    

    public static final int ROWS = 4;
    public static final int COLS = 4;

    private static final Map<Kind, Texture>           textures = new HashMap<>();
    private static final Map<Kind, TextureRegion[][]> regions  = new HashMap<>();

    private static Texture       statusBarPixelTexture;
    private static TextureRegion statusBarPixel;

    private static boolean initialised = false;

    public static void init() {
        if (initialised) return;

        for (Kind kind : Kind.values()) {
            Texture tex = new Texture(kind.path);
            textures.put(kind, tex);

            int frameW = tex.getWidth()  / COLS;
            int frameH = tex.getHeight() / ROWS;

            TextureRegion[][] frames = new TextureRegion[ROWS][COLS];
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    frames[row][col] = new TextureRegion(
                        tex, col * frameW, row * frameH, frameW, frameH);
                }
            }

            regions.put(kind, frames);
        }

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        statusBarPixelTexture = new Texture(pixmap);
        pixmap.dispose();
        statusBarPixel = new TextureRegion(statusBarPixelTexture);

        initialised = true;
    }

    public static TextureRegion[][] getFrames(Kind kind) {
        return regions.get(kind);
    }

    public static TextureRegion getStatusBarPixel() {
        return statusBarPixel;
    }

    public static void dispose() {
        for (Texture tex : textures.values()) tex.dispose();
        textures.clear();
        regions.clear();

        if (statusBarPixelTexture != null) {
            statusBarPixelTexture.dispose();
            statusBarPixelTexture = null;
            statusBarPixel        = null;
        }

        initialised = false;
    }

    private AnimalSpriteCache() {}
}