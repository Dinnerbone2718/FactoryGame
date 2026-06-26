package com.factory.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.FoodManager;
import com.factory.game.Items.Item;

public class Hunger {

    private static final int HUNGER_TICKS = 20;
    private static final float MAX_HUNGER = 100f;
    private static final float DEPLETION_PER_SECOND = MAX_HUNGER / HUNGER_TICKS;

    private static final int FRAME_COUNT = 5;
    private static final int ICON_COUNT = 5;
    private static final float ICON_SIZE = 32f;
    private static final float ICON_GAP = 4f;
    private static final float MARGIN = 16f;
    private static final float LOW_HUNGER_THRESHOLD = 25f;

    private static final float HUNGER_PER_ICON = MAX_HUNGER / ICON_COUNT;

    private float currentHunger = MAX_HUNGER;

    private final float virtualWidth;
    private final float virtualHeight;

    private Texture texture;
    private TextureRegion[] frames;

    public Hunger(float virtualWidth, float virtualHeight) {
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;
        loadTexture();
    }

    private void loadTexture() {
        texture = new Texture("ui/burger.png");
        int fw = texture.getWidth() / FRAME_COUNT;
        int fh = texture.getHeight();
        frames = new TextureRegion[FRAME_COUNT];
        for (int col = 0; col < FRAME_COUNT; col++) {
            frames[col] = new TextureRegion(texture, col * fw, 0, fw, fh);
        }
    }

    public void update(float delta) {
        currentHunger = Math.max(
            0f,
            currentHunger - DEPLETION_PER_SECOND * delta
        );
    }

    public boolean eat(Item item) {
        FoodManager foodManager = CraftingManager.getFoodManagerFor(item);
        if (foodManager == null || !foodManager.getIsFood()) return false;

        currentHunger = Math.min(
            MAX_HUNGER,
            currentHunger + foodManager.getHungerValue()
        );
        return true;
    }

    public void render(SpriteBatch batch, float totalTime) {
        if (frames == null) return;

        boolean low = currentHunger <= LOW_HUNGER_THRESHOLD;
        if (low) {
            float pulse = 0.7f + 0.3f * (float) Math.sin(totalTime * 6f);
            batch.setColor(1f, 0.55f, 0.55f, pulse);
        } else {
            batch.setColor(1f, 1f, 1f, 1f);
        }

        float y = virtualHeight - MARGIN - ICON_SIZE;

        for (int i = 0; i < ICON_COUNT; i++) {
            float sliceFloor = i * HUNGER_PER_ICON;
            float sliceFill = Math.max(
                0f,
                Math.min(HUNGER_PER_ICON, currentHunger - sliceFloor)
            );
            float frac = sliceFill / HUNGER_PER_ICON;

            int frameIndex = Math.round(frac * (frames.length - 1));
            frameIndex = Math.max(0, Math.min(frames.length - 1, frameIndex));
            frameIndex = (frames.length - 1) - frameIndex;
            TextureRegion frame = frames[frameIndex];

            float x = MARGIN + i * (ICON_SIZE + ICON_GAP);
            batch.draw(frame, x, y, ICON_SIZE, ICON_SIZE);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public float getCurrentHunger() {
        return currentHunger;
    }

    public float getMaxHunger() {
        return MAX_HUNGER;
    }

    public float getHungerPercent() {
        return currentHunger / MAX_HUNGER;
    }

    public boolean isStarving() {
        return currentHunger <= 0f;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
