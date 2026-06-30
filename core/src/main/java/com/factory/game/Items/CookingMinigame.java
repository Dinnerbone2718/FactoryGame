package com.factory.game.Items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class CookingMinigame {

    public static class CookResult {

        public final Item item;
        public final int qty;
        public final boolean success;

        public CookResult(Item item, int qty, boolean success) {
            this.item = item;
            this.qty = qty;
            this.success = success;
        }
    }

    private enum GameState {
        INACTIVE,
        COUNTDOWN,
        RUNNING,
        SHOWING_RESULT,
    }

    private enum PattyState {
        WAITING,
        COOKING,
    }

    private enum Outcome {
        NONE,
        UNDERCOOKED,
        PERFECT,
        OVERCOOKED,
        BURNT,
    }

    private static final int NUM_PATTIES = 3;

    private static final float COUNTDOWN_DURATION = 3.0f;
    private static final float RESULT_DISPLAY_TIME = 2.6f;

    private static final float ROUND_TIME = 45f;

    private static final float BASE_COOK_TIME = 10.0f;
    private static final float COOK_TIME_JITTER = 0.6f;

    private static final float INITIAL_STAGGER = 0.8f;

    private static final float PERFECT_MIN = 0.46f;
    private static final float PERFECT_MAX = 0.58f;
    private static final float OVERCOOK_MAX = 0.74f;

    private static final int REQUIRED_PERFECT_FLIPS = 3;

    private static final float FLASH_DURATION = 0.9f;
    private static final float JAB_DURATION = 0.18f;

    private static final int FRAME_COUNT = 5;
    private static final int FRAME_W = 20;
    private static final int FRAME_H = 16;

    private static final float PIXEL_SCALE = 6f;

    private static final float SPATULA_W = 16f;
    private static final float SPATULA_H = 32f;

    private static final float PATTY_DRAW_W = FRAME_W * PIXEL_SCALE;
    private static final float PATTY_DRAW_H = FRAME_H * PIXEL_SCALE;
    private static final float SPATULA_DRAW_W = SPATULA_W * PIXEL_SCALE;
    private static final float SPATULA_DRAW_H = SPATULA_H * PIXEL_SCALE;

    private static final float GRILL_PAD_X = 18f;
    private static final float GRILL_PAD_Y = 14f;
    private static final float SLOT_GAP = 22f;
    private static final float SIDE_PADDING = 50f;

    private static final float GRILL_BLOCK_W = PATTY_DRAW_W + GRILL_PAD_X * 2f;
    private static final float GRILL_BLOCK_H = PATTY_DRAW_H + GRILL_PAD_Y * 2f;

    private static final float DONENESS_STRIP_H = 14f;
    private static final float PIP_SIZE = 12f;
    private static final float PIP_GAP = 6f;

    private static final float SPATULA_SPEED_SLOTS = 3.0f;
    private static final float JAB_HEIGHT = 26f;
    private static final float LEAN_MAX_DEG = 14f;

    private final class PattySlot {

        final int index;
        PattyState state = PattyState.WAITING;
        Outcome lastOutcome = Outcome.NONE;

        float cookTime;
        float startDelay;
        float cookTimer;
        int perfectFlips;
        float flashTimer;

        PattySlot(int index) {
            this.index = index;
        }
    }

    private GameState state = GameState.INACTIVE;

    private CookingRecipe activeRecipe = null;
    private CookResult pendingResult = null;

    private PattySlot[] slots;

    private float countdownTimer = 0f;
    private float roundTimer = 0f;
    private float resultTimer = 0f;

    private boolean won = false;
    private String endReason = "";

    private float spatulaPos = 0f;
    private float spatulaLean = 0f;
    private float spatulaJabTimer = 0f;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont font;
    private final Texture pattySheet;
    private final TextureRegion[] pattyFrames;
    private final Texture spatulaTexture;
    private final TextureRegion spatulaRegion;

    public CookingMinigame() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
            Gdx.files.internal("JetBrainsMono-Regular.ttf")
        );
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = 18;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();

        pattySheet = new Texture(
            Gdx.files.internal("minigame/burger_patty.png")
        );
        pattySheet.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );
        pattyFrames = TextureRegion.split(pattySheet, FRAME_W, FRAME_H)[0];

        spatulaTexture = new Texture(
            Gdx.files.internal("minigame/spatula.png")
        );
        spatulaTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );
        spatulaRegion = new TextureRegion(spatulaTexture);
    }

    public void start(CookingRecipe recipe) {
        activeRecipe = recipe;
        state = GameState.COUNTDOWN;
        countdownTimer = COUNTDOWN_DURATION;
        roundTimer = ROUND_TIME;
        resultTimer = 0f;
        pendingResult = null;
        won = false;
        endReason = "";

        spatulaPos = (NUM_PATTIES - 1) * 0.5f;
        spatulaLean = 0f;
        spatulaJabTimer = 0f;

        slots = new PattySlot[NUM_PATTIES];
        for (int i = 0; i < NUM_PATTIES; i++) {
            PattySlot slot = new PattySlot(i);
            slot.cookTime = rolledCookTime();
            slot.startDelay = i * INITIAL_STAGGER;
            slots[i] = slot;
        }
    }

    private float rolledCookTime() {
        return (
            BASE_COOK_TIME +
            MathUtils.random(-COOK_TIME_JITTER, COOK_TIME_JITTER)
        );
    }

    public boolean isActive() {
        return state != GameState.INACTIVE;
    }

    public CookResult pollResult() {
        if (state == GameState.INACTIVE && pendingResult != null) {
            CookResult r = pendingResult;
            pendingResult = null;
            return r;
        }
        return null;
    }

    public void update(float delta) {
        if (state == GameState.INACTIVE) return;

        if (state == GameState.COUNTDOWN) {
            countdownTimer -= delta;
            if (countdownTimer <= 0f) state = GameState.RUNNING;
            return;
        }

        if (spatulaJabTimer > 0f) {
            spatulaJabTimer = Math.max(0f, spatulaJabTimer - delta);
        }
        for (PattySlot slot : slots) {
            if (slot.flashTimer > 0f) {
                slot.flashTimer = Math.max(0f, slot.flashTimer - delta);
            }
        }

        if (state == GameState.RUNNING) {
            roundTimer -= delta;
            handleMovement(delta);

            float elapsed = ROUND_TIME - roundTimer;
            for (PattySlot slot : slots) {
                if (slot.state == PattyState.WAITING) {
                    if (elapsed >= slot.startDelay) {
                        slot.state = PattyState.COOKING;
                    }
                } else {
                    slot.cookTimer += delta;
                    if (slot.cookTimer >= slot.cookTime) {
                        slot.lastOutcome = Outcome.BURNT;
                        slot.flashTimer = FLASH_DURATION;
                        endRound(false, "A patty burned! Round over.");
                        return;
                    }
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                attemptFlip(currentAlignedIndex());
                if (allPattiesCookedWell()) {
                    endRound(true, "All four patties cooked to perfection!");
                    return;
                }
            }

            if (roundTimer <= 0f) {
                endRound(
                    false,
                    "Time's up! Not every patty got cooked well enough."
                );
            }
        } else if (state == GameState.SHOWING_RESULT) {
            resultTimer -= delta;
            if (resultTimer <= 0f) {
                state = GameState.INACTIVE;
            }
        }
    }

    private void handleMovement(float delta) {
        float dir = 0f;
        if (
            Gdx.input.isKeyPressed(Input.Keys.A) ||
            Gdx.input.isKeyPressed(Input.Keys.LEFT)
        ) {
            dir -= 1f;
        }
        if (
            Gdx.input.isKeyPressed(Input.Keys.D) ||
            Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        ) {
            dir += 1f;
        }

        spatulaPos = MathUtils.clamp(
            spatulaPos + dir * SPATULA_SPEED_SLOTS * delta,
            0f,
            NUM_PATTIES - 1
        );

        float targetLean = dir * LEAN_MAX_DEG;
        spatulaLean = MathUtils.lerp(
            spatulaLean,
            targetLean,
            MathUtils.clamp(delta * 10f, 0f, 1f)
        );
    }

    private int currentAlignedIndex() {
        return MathUtils.clamp(Math.round(spatulaPos), 0, NUM_PATTIES - 1);
    }

    private void attemptFlip(int slotIndex) {
        PattySlot slot = slots[slotIndex];
        if (slot.state != PattyState.COOKING) return;

        float doneness = slot.cookTimer / slot.cookTime;
        Outcome outcome;
        if (doneness < PERFECT_MIN) {
            outcome = Outcome.UNDERCOOKED;
        } else if (doneness <= PERFECT_MAX) {
            outcome = Outcome.PERFECT;
        } else if (doneness <= OVERCOOK_MAX) {
            outcome = Outcome.OVERCOOKED;
        } else {
            outcome = Outcome.BURNT;
        }

        slot.lastOutcome = outcome;
        slot.flashTimer = FLASH_DURATION;
        if (outcome == Outcome.PERFECT) slot.perfectFlips++;

        slot.cookTimer = 0f;
        slot.cookTime = rolledCookTime();

        spatulaJabTimer = JAB_DURATION;
    }

    private boolean allPattiesCookedWell() {
        for (PattySlot slot : slots) {
            if (slot.perfectFlips < REQUIRED_PERFECT_FLIPS) return false;
        }
        return true;
    }

    private void endRound(boolean win, String reason) {
        won = win;
        endReason = reason;
        if (win) {
            pendingResult = new CookResult(
                activeRecipe.getOutputItem(),
                activeRecipe.getOutputQuantity() * NUM_PATTIES,
                true
            );
        }
        state = GameState.SHOWING_RESULT;
        resultTimer = RESULT_DISPLAY_TIME;
    }

    public void render(SpriteBatch batch) {
        if (state == GameState.INACTIVE) return;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.62f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();

        float rowWidth =
            NUM_PATTIES * GRILL_BLOCK_W + (NUM_PATTIES - 1) * SLOT_GAP;
        float panelW = rowWidth + SIDE_PADDING * 2f;
        float panelH = 600f;
        float panelX = (sw - panelW) * 0.5f;
        float panelY = (sh - panelH) * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.13f, 0.07f, 0.05f, 0.97f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.85f, 0.45f, 0.15f, 1f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        float spatulaCenterY = panelY + 120f;
        float gridY = panelY + 250f;
        float stripY = panelY + 392f;
        float pipsY = panelY + 440f;
        float timerBarY = panelY + 470f;
        float timerBarH = 20f;
        float subtitleY = panelY + panelH - 66f;
        float titleY = panelY + panelH - 36f;
        float instrY = panelY + 30f;

        float gridStartX = panelX + SIDE_PADDING + GRILL_PAD_X;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.10f, 0.08f, 0.06f, 1f);
        for (int i = 0; i < NUM_PATTIES; i++) {
            float slotX = slotPattyX(gridStartX, i);
            shapeRenderer.rect(
                slotX - GRILL_PAD_X,
                gridY - GRILL_PAD_Y,
                GRILL_BLOCK_W,
                GRILL_BLOCK_H
            );
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < NUM_PATTIES; i++) {
            float slotX = slotPattyX(gridStartX, i);
            shapeRenderer.setColor(0.45f, 0.28f, 0.14f, 1f);
            shapeRenderer.rect(
                slotX - GRILL_PAD_X,
                gridY - GRILL_PAD_Y,
                GRILL_BLOCK_W,
                GRILL_BLOCK_H
            );
            shapeRenderer.setColor(0.30f, 0.16f, 0.08f, 0.8f);
            for (int g = 1; g < 5; g++) {
                float lx = slotX - GRILL_PAD_X + GRILL_BLOCK_W * (g / 5f);
                shapeRenderer.line(
                    lx,
                    gridY - GRILL_PAD_Y,
                    lx,
                    gridY + PATTY_DRAW_H + GRILL_PAD_Y
                );
            }
        }
        shapeRenderer.end();

        batch.begin();
        for (PattySlot slot : slots) {
            float slotX = slotPattyX(gridStartX, slot.index);
            int displayFrame = pattyDisplayFrame(slot);
            if (slot.state == PattyState.WAITING) {
                batch.setColor(1f, 1f, 1f, 0.35f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }
            batch.draw(
                pattyFrames[displayFrame],
                slotX,
                gridY,
                PATTY_DRAW_W,
                PATTY_DRAW_H
            );
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();

        for (PattySlot slot : slots) {
            float slotX = slotPattyX(gridStartX, slot.index);
            drawDonenessStrip(slotX, stripY, slot);
            drawQuotaPips(slotX, pipsY, slot);
        }

        GlyphLayout layout = new GlyphLayout();
        batch.begin();
        for (PattySlot slot : slots) {
            float slotX = slotPattyX(gridStartX, slot.index);
            drawFlashLabel(batch, layout, slotX, gridY, slot);
        }
        batch.end();

        float timeFrac = MathUtils.clamp(roundTimer / ROUND_TIME, 0f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.18f, 0.10f, 0.08f, 1f);
        shapeRenderer.rect(
            gridStartX - GRILL_PAD_X,
            timerBarY,
            rowWidth,
            timerBarH
        );
        if (timeFrac > 0.5f) shapeRenderer.setColor(0.30f, 0.85f, 0.35f, 1f);
        else if (timeFrac > 0.25f) shapeRenderer.setColor(
            0.92f,
            0.65f,
            0.20f,
            1f
        );
        else shapeRenderer.setColor(0.95f, 0.30f, 0.30f, 1f);
        shapeRenderer.rect(
            gridStartX - GRILL_PAD_X,
            timerBarY,
            rowWidth * timeFrac,
            timerBarH
        );
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.45f, 0.28f, 0.14f, 1f);
        shapeRenderer.rect(
            gridStartX - GRILL_PAD_X,
            timerBarY,
            rowWidth,
            timerBarH
        );
        shapeRenderer.end();

        drawSpatula(batch, gridStartX, spatulaCenterY);

        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(1f, 0.70f, 0.30f, 1f);
        layout.setText(font, "~ Cooking ~");
        font.draw(
            batch,
            layout,
            panelX + (panelW - layout.width) * 0.5f,
            titleY + layout.height * 0.5f
        );

        if (activeRecipe != null) {
            font.getData().setScale(1.0f);
            font.setColor(0.75f, 0.60f, 0.45f, 1f);
            String sub =
                "Cooking:  " + activeRecipe.getOutputItem().getDisplayName();
            layout.setText(font, sub);
            font.draw(
                batch,
                layout,
                panelX + (panelW - layout.width) * 0.5f,
                subtitleY
            );
        }

        font.getData().setScale(1.1f);
        String instrText;
        if (state == GameState.COUNTDOWN) {
            instrText = "Get ready...";
            font.setColor(0.85f, 0.65f, 0.35f, 1f);
        } else if (state == GameState.RUNNING) {
            instrText =
                "[A/D] or [\u2190/\u2192] move   \u2022   [SPACE] flip   \u2022   Cooked well: " +
                countReady() +
                "/" +
                NUM_PATTIES;
            font.setColor(0.90f, 0.78f, 0.55f, 1f);
        } else {
            instrText = endReason;
            font.setColor(
                won
                    ? new Color(0.30f, 1f, 0.40f, 1f)
                    : new Color(1f, 0.40f, 0.30f, 1f)
            );
        }
        layout.setText(font, instrText);
        font.draw(
            batch,
            layout,
            panelX + (panelW - layout.width) * 0.5f,
            instrY
        );

        if (state == GameState.COUNTDOWN) {
            int countNum = (int) Math.ceil(countdownTimer);
            float frac = countdownTimer - (countNum - 1);
            float pulse = 1.0f - frac * 0.35f;

            font.getData().setScale(5.0f * pulse);
            Color numColor;
            if (countNum == 3) numColor = new Color(1f, 0.85f, 0.15f, 1f);
            else if (countNum == 2) numColor = new Color(1f, 0.55f, 0.10f, 1f);
            else numColor = new Color(1f, 0.22f, 0.18f, 1f);
            font.setColor(
                numColor.r,
                numColor.g,
                numColor.b,
                Math.min(1f, frac + 0.3f)
            );

            layout.setText(font, String.valueOf(countNum));
            font.draw(
                batch,
                layout,
                panelX + (panelW - layout.width) * 0.5f,
                gridY + PATTY_DRAW_H * 0.5f + layout.height * 0.5f
            );
        }

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }

    private float slotPattyX(float gridStartX, int index) {
        return gridStartX + index * (GRILL_BLOCK_W + SLOT_GAP);
    }

    private int pattyDisplayFrame(PattySlot slot) {
        if (slot.state == PattyState.WAITING) return 0;
        float doneness = slot.cookTimer / slot.cookTime;
        return MathUtils.clamp(
            (int) (doneness * FRAME_COUNT),
            0,
            FRAME_COUNT - 1
        );
    }

    private int countReady() {
        int count = 0;
        for (PattySlot slot : slots) {
            if (slot.perfectFlips >= REQUIRED_PERFECT_FLIPS) count++;
        }
        return count;
    }

    private String textFor(Outcome outcome) {
        switch (outcome) {
            case UNDERCOOKED:
                return "Too soon!";
            case PERFECT:
                return "Perfect!";
            case OVERCOOKED:
                return "Too late!";
            case BURNT:
                return "Burnt!";
            default:
                return "";
        }
    }

    private Color colorFor(Outcome outcome) {
        switch (outcome) {
            case UNDERCOOKED:
                return new Color(0.92f, 0.55f, 0.25f, 1f);
            case PERFECT:
                return new Color(0.30f, 0.95f, 0.40f, 1f);
            case OVERCOOKED:
                return new Color(0.85f, 0.45f, 0.15f, 1f);
            case BURNT:
                return new Color(0.95f, 0.30f, 0.30f, 1f);
            default:
                return Color.WHITE;
        }
    }

    private void drawFlashLabel(
        SpriteBatch batch,
        GlyphLayout layout,
        float slotX,
        float gridY,
        PattySlot slot
    ) {
        if (slot.flashTimer <= 0f) return;

        String text = textFor(slot.lastOutcome);
        if (text.isEmpty()) return;

        float t = 1f - (slot.flashTimer / FLASH_DURATION);
        float riseY = t * 40f;
        float alpha = slot.flashTimer / FLASH_DURATION;

        Color c = colorFor(slot.lastOutcome);
        font.getData().setScale(1.0f);
        font.setColor(c.r, c.g, c.b, alpha);
        layout.setText(font, text);
        font.draw(
            batch,
            layout,
            slotX + (PATTY_DRAW_W - layout.width) * 0.5f,
            gridY + PATTY_DRAW_H * 0.5f + riseY
        );
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }

    private void drawQuotaPips(float slotX, float pipsY, PattySlot slot) {
        float totalW =
            REQUIRED_PERFECT_FLIPS * PIP_SIZE +
            (REQUIRED_PERFECT_FLIPS - 1) * PIP_GAP;
        float startX = slotX + (PATTY_DRAW_W - totalW) * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < REQUIRED_PERFECT_FLIPS; i++) {
            float px = startX + i * (PIP_SIZE + PIP_GAP);
            if (i < slot.perfectFlips) {
                shapeRenderer.setColor(0.30f, 0.95f, 0.40f, 1f);
            } else {
                shapeRenderer.setColor(0.30f, 0.20f, 0.15f, 1f);
            }
            shapeRenderer.rect(px, pipsY, PIP_SIZE, PIP_SIZE);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.55f, 0.40f, 0.25f, 1f);
        for (int i = 0; i < REQUIRED_PERFECT_FLIPS; i++) {
            float px = startX + i * (PIP_SIZE + PIP_GAP);
            shapeRenderer.rect(px, pipsY, PIP_SIZE, PIP_SIZE);
        }
        shapeRenderer.end();
    }

    private void drawDonenessStrip(float stripX, float stripY, PattySlot slot) {
        float stripW = PATTY_DRAW_W;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.85f, 0.30f, 0.35f, 1f);
        shapeRenderer.rect(
            stripX,
            stripY,
            stripW * PERFECT_MIN,
            DONENESS_STRIP_H
        );
        shapeRenderer.setColor(0.30f, 0.85f, 0.35f, 1f);
        shapeRenderer.rect(
            stripX + stripW * PERFECT_MIN,
            stripY,
            stripW * (PERFECT_MAX - PERFECT_MIN),
            DONENESS_STRIP_H
        );
        shapeRenderer.setColor(0.92f, 0.55f, 0.25f, 1f);
        shapeRenderer.rect(
            stripX + stripW * PERFECT_MAX,
            stripY,
            stripW * (OVERCOOK_MAX - PERFECT_MAX),
            DONENESS_STRIP_H
        );
        shapeRenderer.setColor(0.42f, 0.18f, 0.10f, 1f);
        shapeRenderer.rect(
            stripX + stripW * OVERCOOK_MAX,
            stripY,
            stripW * (1f - OVERCOOK_MAX),
            DONENESS_STRIP_H
        );
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.40f, 0.25f, 0.15f, 1f);
        shapeRenderer.rect(stripX, stripY, stripW, DONENESS_STRIP_H);
        shapeRenderer.end();

        if (slot.state == PattyState.COOKING) {
            float frac = MathUtils.clamp(
                slot.cookTimer / slot.cookTime,
                0f,
                1f
            );
            float cursorX = stripX + frac * stripW;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
            shapeRenderer.rect(
                cursorX - 1.5f,
                stripY - 4f,
                3f,
                DONENESS_STRIP_H + 8f
            );
            shapeRenderer.end();
        }
    }

    private void drawSpatula(
        SpriteBatch batch,
        float gridStartX,
        float laneCenterY
    ) {
        float centerX =
            gridStartX +
            spatulaPos * (GRILL_BLOCK_W + SLOT_GAP) +
            PATTY_DRAW_W * 0.5f;

        float jabT = 1f - (spatulaJabTimer / JAB_DURATION);
        jabT = MathUtils.clamp(jabT, 0f, 1f);
        float jabPulse = (spatulaJabTimer <= 0f)
            ? 0f
            : ((jabT < 0.5f) ? (jabT / 0.5f) : ((1f - jabT) / 0.5f));

        float centerY = laneCenterY + jabPulse * JAB_HEIGHT;
        float rot = spatulaLean + jabPulse * 12f;

        float drawX = centerX - SPATULA_DRAW_W * 0.5f;
        float drawY = centerY - SPATULA_DRAW_H * 0.5f;

        batch.begin();
        batch.draw(
            spatulaRegion,
            drawX,
            drawY,
            SPATULA_DRAW_W * 0.5f,
            SPATULA_DRAW_H * 0.5f,
            SPATULA_DRAW_W,
            SPATULA_DRAW_H,
            1f,
            1f,
            rot
        );
        batch.end();
    }

    public void resize(int w, int h) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        pattySheet.dispose();
        spatulaTexture.dispose();
    }
}
