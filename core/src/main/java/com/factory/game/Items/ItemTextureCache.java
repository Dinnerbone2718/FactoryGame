package com.factory.game.Items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class ItemTextureCache {
    private static final Map<Item, TextureRegion> textures = new EnumMap<>(Item.class);
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        for (Item item : Item.values()) {
            String path = item.getTexturePath();
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(path);
                textures.put(item, new TextureRegion(tex));
            } else {
                Gdx.app.error("ItemTextureCache", "Missing texture: " + path);
            }
        }

        initialized = true;
    }

    public static TextureRegion getTexture(Item item) {
        return textures.get(item);
    }

    public static void dispose() {
        for (TextureRegion region : textures.values()) {
            region.getTexture().dispose();
        }
        textures.clear();
        initialized = false;
    }
}