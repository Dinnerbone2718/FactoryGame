package com.factory.game.Items;

import java.util.Arrays;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class FishingMinigame {

    public static class FishResult {
        public final Item    item;
        public final int     qty;
        public final boolean success;

        public FishResult(Item item, int qty, boolean success) {
            this.item    = item;
            this.qty     = qty;
            this.success = success;
        }
    }

    public enum Difficulty {
        SIMPLE  ( 5, 25.0f,  100f,    10f,   320f,   0.30f,  5.5f,    1,  210f, "Simple"),
        MEDIUM  ( 8, 20.0f,  130f,    18f,   280f,   0.45f,  4.0f,    2,   250f, "Medium"),
        COMPLEX (8, 15.0f,  165f,    25f,   245f,   0.62f,  3.2f,    2,   300f, "Complex");

        public final int    hitsRequired;
        public final float  totalTime;
        public final float  fishBaseSpeed;
        public final float  fishSpeedBonus;
        public final float  hookSpeed;
        public final float  cooldownOnMiss;
        public final float  jukeInterval;
        public final int    obstacleCount;
        public final float  obstacleGapWidth;
        public final String label;

        Difficulty(int hitsRequired, float totalTime, float fishBaseSpeed,
                   float fishSpeedBonus, float hookSpeed, float cooldownOnMiss,
                   float jukeInterval, int obstacleCount, float obstacleGapWidth, String label) {
            this.hitsRequired     = hitsRequired;
            this.totalTime        = totalTime;
            this.fishBaseSpeed    = fishBaseSpeed;
            this.fishSpeedBonus   = fishSpeedBonus;
            this.hookSpeed        = hookSpeed;
            this.cooldownOnMiss   = cooldownOnMiss;
            this.jukeInterval     = jukeInterval;
            this.obstacleCount    = obstacleCount;
            this.obstacleGapWidth = obstacleGapWidth;
            this.label            = label;
        }
    }

    private static final float COUNTDOWN_DURATION  = 3.0f;
    private static final float RESULT_DISPLAY_TIME = 1.8f;

    private static final float ARENA_W = 480f;
    private static final float ARENA_H = 200f;

    private static final float HOOK_W = 20f;

    private static final float CAST_SPEED      = 400f;
    private static final float MISS_FLASH_TIME = 0.30f;
    private static final float HIT_FLASH_TIME  = 0.35f;
    private static final float OBS_FLASH_TIME  = 0.35f;

    private static final float FISH_BODY_W = 26f;
    private static final float FISH_TAIL_W = 10f;
    private static final float FISH_W      = FISH_BODY_W + FISH_TAIL_W;
    private static final float FISH_H      = 20f;

    private static final float HOOK_SPRITE_W = 16f;
    private static final float HOOK_SPRITE_H = 24f;

    private static final int   MAX_OBS = 3;
    private static final float OBS_H   = 12f;

    private static final Color COL_OVERLAY      = new Color(0.00f, 0.00f, 0.00f, 0.62f);
    private static final Color COL_PANEL_BG     = new Color(0.04f, 0.08f, 0.14f, 0.97f);
    private static final Color COL_PANEL_EDGE   = new Color(0.20f, 0.45f, 0.70f, 1.00f);
    private static final Color COL_PANEL_INNER  = new Color(0.12f, 0.30f, 0.52f, 0.40f);

    private static final Color COL_ARENA_BG     = new Color(0.04f, 0.10f, 0.24f, 1.00f);
    private static final Color COL_ARENA_FLOOR  = new Color(0.06f, 0.15f, 0.32f, 1.00f);
    private static final Color COL_ARENA_STRIPE = new Color(0.08f, 0.18f, 0.36f, 0.35f);
    private static final Color COL_ARENA_EDGE   = new Color(0.20f, 0.40f, 0.65f, 1.00f);

    private static final Color COL_HOOK_ZONE    = new Color(0.60f, 0.88f, 1.00f, 0.12f);
    private static final Color COL_LINE         = new Color(0.75f, 0.92f, 1.00f, 0.85f);
    private static final Color COL_LINE_FADE    = new Color(0.75f, 0.92f, 1.00f, 0.05f);
    private static final Color COL_LURE_COOL    = new Color(0.42f, 0.45f, 0.55f, 1.00f);

    private static final Color COL_OBS_FILL     = new Color(0.28f, 0.18f, 0.10f, 1.00f);
    private static final Color COL_OBS_EDGE     = new Color(0.52f, 0.38f, 0.22f, 1.00f);
    private static final Color COL_OBS_HILIGHT  = new Color(0.60f, 0.48f, 0.30f, 0.55f);
    private static final Color COL_GAP_GUIDE    = new Color(0.25f, 0.65f, 1.00f, 0.05f);

    private static final Color COL_HITSBAR_BG   = new Color(0.06f, 0.10f, 0.20f, 1.00f);
    private static final Color COL_HITSBAR_FILL = new Color(0.18f, 0.65f, 0.95f, 1.00f);
    private static final Color COL_HITSBAR_DONE = new Color(0.18f, 0.95f, 0.40f, 1.00f);
    private static final Color COL_TIMEBAR_BG   = new Color(0.08f, 0.06f, 0.10f, 1.00f);
    private static final Color COL_TIMEBAR_OK   = new Color(0.25f, 0.55f, 0.90f, 1.00f);
    private static final Color COL_TIMEBAR_LOW  = new Color(0.95f, 0.25f, 0.20f, 1.00f);
    private static final Color COL_TIMEBAR_EDGE = new Color(0.20f, 0.35f, 0.55f, 0.70f);

    private static final Color COL_TITLE        = new Color(0.35f, 0.80f, 1.00f, 1.00f);
    private static final Color COL_SUBTITLE     = new Color(0.50f, 0.70f, 0.90f, 1.00f);
    private static final Color COL_HITS_LABEL   = new Color(0.45f, 0.65f, 0.85f, 1.00f);
    private static final Color COL_INSTR        = new Color(0.75f, 0.88f, 1.00f, 1.00f);
    private static final Color COL_READY        = new Color(0.55f, 0.75f, 0.95f, 1.00f);
    private static final Color COL_SUCCESS      = new Color(0.20f, 1.00f, 0.42f, 1.00f);
    private static final Color COL_FAIL         = new Color(1.00f, 0.38f, 0.32f, 1.00f);
    private static final Color COL_DIFF_SIMPLE  = new Color(0.35f, 0.90f, 0.45f, 1.00f);
    private static final Color COL_DIFF_MEDIUM  = new Color(0.95f, 0.82f, 0.25f, 1.00f);
    private static final Color COL_DIFF_COMPLEX = new Color(1.00f, 0.42f, 0.32f, 1.00f);

    private static final Color COL_MISS_FLASH   = new Color(0.95f, 0.15f, 0.10f, 1.00f);
    private static final Color COL_HIT_FLASH    = new Color(0.15f, 0.95f, 0.50f, 1.00f);
    private static final Color COL_OBS_FLASH    = new Color(0.90f, 0.52f, 0.05f, 1.00f);

    private enum State { INACTIVE, COUNTDOWN, RUNNING, SHOWING_RESULT }
    private State state = State.INACTIVE;

    private FishingRecipe activeRecipe  = null;
    private Difficulty    difficulty    = Difficulty.MEDIUM;
    private FishResult    pendingResult = null;
    private boolean       resultSuccess = false;

    private float countdownTimer = 0f;
    private float timeElapsed    = 0f;
    private float resultTimer    = 0f;
    private int   hitsLanded     = 0;

    private float   hookX        = 0f;
    private boolean casting      = false;
    private float   castY        = 0f;
    private float   castCooldown = 0f;

    private float fishX        = 0f;
    private float fishDir      = 1f;
    private float fishJukeTimer = 0f;

    private final float[]   obsY       = new float[MAX_OBS];
    private final float[]   obsGapX    = new float[MAX_OBS];
    private final float[]   obsGapW    = new float[MAX_OBS];
    private final boolean[]   obsDir    = new boolean[MAX_OBS];
    private final boolean[] obsCleared = new boolean[MAX_OBS];
    private int             obsCount   = 0;

    private float missFlash = 0f;
    private float hitFlash  = 0f;
    private float obsFlash  = 0f;

    private float arenaX = 0f;
    private float arenaY = 0f;

    private final Random        rng           = new Random();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont    font;
    private final Texture       fishTexture;
    private final Texture       hookTexture;



    public FishingMinigame() {
        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 18;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();

        fishTexture = new Texture(Gdx.files.internal("minigame/fish.png"));
        fishTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        hookTexture = new Texture(Gdx.files.internal("minigame/hook.png"));
        hookTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void start(FishingRecipe recipe) {
        activeRecipe   = recipe;
        difficulty     = recipe.getDifficulty();
        state          = State.COUNTDOWN;
        countdownTimer = COUNTDOWN_DURATION;
        timeElapsed    = 0f;
        hitsLanded     = 0;
        hookX          = ARENA_W * 0.5f - HOOK_W * 0.5f;
        fishX          = rng.nextFloat() * (ARENA_W - FISH_W);
        fishDir        = rng.nextBoolean() ? 1f : -1f;
        fishJukeTimer  = difficulty.jukeInterval;
        casting        = false;
        castY          = 0f;
        castCooldown   = 0f;
        missFlash      = 0f;
        hitFlash       = 0f;
        obsFlash       = 0f;
        pendingResult  = null;
        resultSuccess  = false;
        spawnObstacles();
    }

    public boolean isActive() { return state != State.INACTIVE; }

    public FishResult pollResult() {
        if (state == State.INACTIVE && pendingResult != null) {
            FishResult r  = pendingResult;
            pendingResult = null;
            return r;
        }
        return null;
    }

    public void update(float delta) {
        if (state == State.INACTIVE) return;

        missFlash    = Math.max(0f, missFlash    - delta);
        hitFlash     = Math.max(0f, hitFlash     - delta);
        obsFlash     = Math.max(0f, obsFlash     - delta);
        castCooldown = Math.max(0f, castCooldown - delta);

        if (state == State.COUNTDOWN) {
            countdownTimer -= delta;
            if (countdownTimer <= 0f) state = State.RUNNING;
            return;
        }

        if (state == State.RUNNING) {
            updateHook(delta);
            updateFish(delta);
            updateCast(delta);
            timeElapsed += delta;
            if (timeElapsed >= difficulty.totalTime) finishGame(false);

        } else if (state == State.SHOWING_RESULT) {
            resultTimer -= delta;
            if (resultTimer <= 0f) state = State.INACTIVE;
        }
    }

    private void updateHook(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  hookX -= difficulty.hookSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) hookX += difficulty.hookSpeed * delta;
        hookX = MathUtils.clamp(hookX, 0f, ARENA_W - HOOK_W);
    }

    private void updateFish(float delta) {
        fishJukeTimer -= delta;
        if (fishJukeTimer <= 0f) {
            fishDir       = rng.nextBoolean() ? 1f : -1f;
            fishJukeTimer = difficulty.jukeInterval * (0.55f + rng.nextFloat() * 0.90f);
        }

        float speed = difficulty.fishBaseSpeed + hitsLanded * difficulty.fishSpeedBonus;
        fishX += fishDir * speed * delta;

        if (fishX <= 0f) {
            fishX   = 0f;
            fishDir = 1f;
            fishJukeTimer = difficulty.jukeInterval * (0.40f + rng.nextFloat() * 0.60f);
        } else if (fishX >= ARENA_W - FISH_W) {
            fishX   = ARENA_W - FISH_W;
            fishDir = -1f;
            fishJukeTimer = difficulty.jukeInterval * (0.40f + rng.nextFloat() * 0.60f);
        }
    }

    private void updateCast(float delta) {
        if (casting) {
            castY -= CAST_SPEED * delta;

            for (int i = 0; i < obsCount; i++) {
                if (!obsCleared[i] && castY <= obsY[i] + OBS_H) {
                    obsCleared[i] = true;
                    float hookCenter = hookX + HOOK_W * 0.5f;
                    boolean inGap = hookCenter >= obsGapX[i]
                                 && hookCenter <= obsGapX[i] + obsGapW[i];
                    if (!inGap) {
                        obsFlash     = OBS_FLASH_TIME;
                        missFlash    = MISS_FLASH_TIME;
                        castCooldown = difficulty.cooldownOnMiss * 1.8f;
                        casting      = false;
                        castY        = 0f;
                        return;
                    }
                }
            }

            if (castY <= FISH_H) {
                resolveCast();
            }

        } else if (castCooldown <= 0f && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            casting = true;
            castY   = ARENA_H;
            Arrays.fill(obsCleared, false);
        }
    }

    private void resolveCast() {
        boolean overlap = (hookX + HOOK_W > fishX) && (hookX < fishX + FISH_W);
        if (overlap) {
            hitsLanded++;
            hitFlash      = HIT_FLASH_TIME;
            fishDir       = -fishDir;
            fishX         = MathUtils.clamp(fishX + fishDir * FISH_W * 0.5f, 0f, ARENA_W - FISH_W);
            fishJukeTimer = difficulty.jukeInterval * (0.30f + rng.nextFloat() * 0.40f);
            spawnObstacles();
        } else {
            missFlash    = MISS_FLASH_TIME;
            castCooldown = difficulty.cooldownOnMiss;
        }
        casting = false;
        castY   = 0f;

        if (hitsLanded >= difficulty.hitsRequired) finishGame(true);
    }

    private void finishGame(boolean success) {
        resultSuccess = success;
        pendingResult = success
                ? new FishResult(activeRecipe.roll(rng), 1, true)
                : new FishResult(null, 0, false);
        state       = State.SHOWING_RESULT;
        resultTimer = RESULT_DISPLAY_TIME;
    }

    private void spawnObstacles() {
        obsCount = difficulty.obstacleCount;
        float margin  = 28f;
        float usable  = ARENA_H - FISH_H - margin * 2f;
        float section = usable / (obsCount + 1);
        for (int i = 0; i < obsCount; i++) {
            obsY[i]    = FISH_H + margin + section * (i + 1)
                       + (rng.nextFloat() - 0.5f) * section * 0.35f;
            float maxLeft = ARENA_W - difficulty.obstacleGapWidth - 20f;
            obsGapX[i] = 10f + rng.nextFloat() * maxLeft;
            obsGapW[i] = difficulty.obstacleGapWidth;
        }
        Arrays.fill(obsCleared, false);


        boolean dir = false;
        for (int i = 0; i < MAX_OBS; i++) {
            dir = !(dir);
            obsDir[i] = dir;
        }


    }

    public void render(SpriteBatch batch) {
        if (state == State.INACTIVE) return;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_OVERLAY);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();

        float panelW = 600f, panelH = 440f;
        float panelX = (sw - panelW) * 0.5f;
        float panelY = (sh - panelH) * 0.5f;

        float botPad   = 18f;
        float instrH   = 24f;
        float gapD     = 16f;
        float hitsBarH = 16f;
        float hitsBarW = panelW - 100f;
        float hitsBarX = panelX + 50f;
        float hitsBarY = panelY + botPad + instrH + gapD;
        float timeBarH = 8f;
        float gapC     = 36f;
        float timeBarY = hitsBarY + hitsBarH + gapC;

        arenaX = panelX + (panelW - ARENA_W) * 0.5f;
        arenaY = timeBarY + timeBarH + 36f;

        float subtitleY = arenaY + ARENA_H + 18f;
        float titleY    = subtitleY + 26f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_PANEL_BG);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_PANEL_EDGE);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_PANEL_INNER);
        shapeRenderer.rect(panelX + 3f, panelY + 3f, panelW - 6f, panelH - 6f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_ARENA_BG);
        shapeRenderer.rect(arenaX, arenaY, ARENA_W, ARENA_H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_ARENA_FLOOR);
        shapeRenderer.rect(arenaX, arenaY, ARENA_W, FISH_H + 8f);
        shapeRenderer.end();

        for (int i = 1; i <= 4; i++) {
            float lineY = arenaY + i * (ARENA_H / 5f);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(COL_ARENA_STRIPE);
            shapeRenderer.rect(arenaX, lineY, ARENA_W, 2f);
            shapeRenderer.end();
        }

        if (obsFlash > 0f) {
            float a = (obsFlash / OBS_FLASH_TIME) * 0.42f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(COL_OBS_FLASH.r, COL_OBS_FLASH.g, COL_OBS_FLASH.b, a);
            shapeRenderer.rect(arenaX, arenaY, ARENA_W, ARENA_H);
            shapeRenderer.end();
        } else if (missFlash > 0f) {
            float a = (missFlash / MISS_FLASH_TIME) * 0.35f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(COL_MISS_FLASH.r, COL_MISS_FLASH.g, COL_MISS_FLASH.b, a);
            shapeRenderer.rect(arenaX, arenaY, ARENA_W, ARENA_H);
            shapeRenderer.end();
        }
        if (hitFlash > 0f) {
            float a = (hitFlash / HIT_FLASH_TIME) * 0.38f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(COL_HIT_FLASH.r, COL_HIT_FLASH.g, COL_HIT_FLASH.b, a);
            shapeRenderer.rect(arenaX, arenaY, ARENA_W, ARENA_H);
            shapeRenderer.end();
        }

        drawObstacles();

        if (state == State.RUNNING || state == State.SHOWING_RESULT) {
            drawFish(batch);
        }

        if (state == State.RUNNING) {
            drawHook(batch);
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_ARENA_EDGE);
        shapeRenderer.rect(arenaX, arenaY, ARENA_W, ARENA_H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_HITSBAR_BG);
        shapeRenderer.rect(hitsBarX, hitsBarY, hitsBarW, hitsBarH);
        shapeRenderer.end();

        float hitFill = (float) hitsLanded / difficulty.hitsRequired;
        if (hitFill > 0f) {
            Color barCol = hitsLanded >= difficulty.hitsRequired ? COL_HITSBAR_DONE : COL_HITSBAR_FILL;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(barCol);
            shapeRenderer.rect(hitsBarX, hitsBarY, hitsBarW * hitFill, hitsBarH);
            shapeRenderer.end();
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_PANEL_EDGE);
        shapeRenderer.rect(hitsBarX, hitsBarY, hitsBarW, hitsBarH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_TIMEBAR_BG);
        shapeRenderer.rect(hitsBarX, timeBarY, hitsBarW, timeBarH);
        shapeRenderer.end();

        float timeLeft  = Math.max(0f, difficulty.totalTime - timeElapsed);
        float timeFill  = timeLeft / difficulty.totalTime;
        Color timeColor = timeLeft > 5f ? COL_TIMEBAR_OK : COL_TIMEBAR_LOW;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(timeColor);
        shapeRenderer.rect(hitsBarX, timeBarY, hitsBarW * timeFill, timeBarH);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_TIMEBAR_EDGE);
        shapeRenderer.rect(hitsBarX, timeBarY, hitsBarW, timeBarH);
        shapeRenderer.end();

        batch.begin();
        GlyphLayout layout = new GlyphLayout();

        font.getData().setScale(1.5f);
        font.setColor(COL_TITLE);
        layout.setText(font, "Fishing");
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                titleY + layout.height * 0.5f);

        font.getData().setScale(0.95f);
        font.setColor(difficultyColor());
        font.draw(batch, "[" + difficulty.label + "]", panelX + 14f, panelY + panelH - 10f);

        font.getData().setScale(1.0f);
        font.setColor(COL_SUBTITLE);
        String sub = (activeRecipe != null) ? "Rod: " + activeRecipe.getRodItem().getDisplayName() : "";
        layout.setText(font, sub);
        font.draw(batch, layout, panelX + (panelW - layout.width) * 0.5f, subtitleY);

        font.getData().setScale(0.95f);
        font.setColor(COL_HITS_LABEL);
        layout.setText(font, "Hooks: " + hitsLanded + " / " + difficulty.hitsRequired);
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                hitsBarY + hitsBarH + 18f);

        if (state == State.RUNNING) {
            font.getData().setScale(0.88f);
            font.setColor(timeColor);
            layout.setText(font, String.format("%.1fs remaining", timeLeft));
            font.draw(batch, layout,
                    panelX + (panelW - layout.width) * 0.5f,
                    timeBarY + timeBarH + 18f);
        }

        font.getData().setScale(1.1f);
        String instrText;
        if (state == State.COUNTDOWN) {
            instrText = "Get ready...";
            font.setColor(COL_READY);
        } else if (state == State.RUNNING) {
            instrText = "Left / Right to move  |  Space to cast";
            font.setColor(COL_INSTR);
        } else if (resultSuccess) {
            String name = (pendingResult != null && pendingResult.item != null)
                    ? pendingResult.item.getDisplayName() : "something";
            instrText = "Caught: " + name + "!";
            font.setColor(COL_SUCCESS);
        } else {
            instrText = "Ran out of time!";
            font.setColor(COL_FAIL);
        }
        layout.setText(font, instrText);
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                panelY + botPad + instrH);

        if (state == State.COUNTDOWN) {
            int   countNum = (int) Math.ceil(countdownTimer);
            float frac     = countdownTimer - (countNum - 1);
            float pulse    = 1.0f - frac * 0.35f;
            font.getData().setScale(4.5f * pulse);
            Color numColor;
            if      (countNum == 3) numColor = new Color(0.20f, 0.85f, 1.00f, 1f);
            else if (countNum == 2) numColor = new Color(0.10f, 0.65f, 0.95f, 1f);
            else                   numColor = new Color(0.05f, 0.45f, 0.90f, 1f);
            font.setColor(numColor.r, numColor.g, numColor.b, Math.min(1f, frac + 0.3f));
            layout.setText(font, String.valueOf(countNum));
            font.draw(batch, layout,
                    panelX + (panelW - layout.width) * 0.5f,
                    arenaY + ARENA_H * 0.5f + layout.height * 0.5f);
            font.getData().setScale(1.2f);
            font.setColor(Color.WHITE);
        }
    }

    private void drawFish(SpriteBatch batch) {
        float fx = arenaX + fishX;
        float fy = arenaY;
        boolean facingRight = fishDir > 0f;

        batch.begin();
        if (facingRight) {
            batch.draw(fishTexture, fx, fy, FISH_W, FISH_H);
        } else {
            batch.draw(fishTexture, fx + FISH_W, fy, -FISH_W, FISH_H);
        }
        batch.end();
    }

    private void drawHook(SpriteBatch batch) {
        float hx       = arenaX + hookX + HOOK_W * 0.5f;
        float hookTopY = arenaY + ARENA_H;


        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_HOOK_ZONE);
        shapeRenderer.rect(arenaX + hookX, arenaY, HOOK_W, ARENA_H);
        shapeRenderer.end();

        float lureTopY;
        if (casting) {
            lureTopY = arenaY + castY;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.rect(hx - 1f, lureTopY, 2f, hookTopY - lureTopY,
                    COL_LINE, COL_LINE, COL_LINE_FADE, COL_LINE_FADE);
            shapeRenderer.end();

        } else {
            lureTopY = hookTopY - 10f;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.rect(hx - 1f, lureTopY, 2f, 10f,
                    COL_LINE, COL_LINE, COL_LINE_FADE, COL_LINE_FADE);
            shapeRenderer.end();
        }

        batch.begin();
        if (!casting && castCooldown > 0f) {
            batch.setColor(COL_LURE_COOL);
        }
        batch.draw(hookTexture, hx - HOOK_SPRITE_W * 0.5f, lureTopY - HOOK_SPRITE_H, HOOK_SPRITE_W, HOOK_SPRITE_H);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawObstacles() {
        for (int i = 0; i < obsCount; i++) {
            float obsScreenY = arenaY + obsY[i];
            float leftSegW   = obsGapX[i];
            float rightSegX  = obsGapX[i] + obsGapW[i];
            float rightSegW  = ARENA_W - rightSegX;
            boolean obsDirection = obsDir[i];


            if (obsDirection == true){
                obsGapX[i] += 1f;
                if (obsGapX[i] + obsGapW[i] >= ARENA_W - 10f){
                    obsDir[i] = false;
                }
            } else {
                obsGapX[i] -= 1f;
                if (obsGapX[i] <= 10f){
                    obsDir[i] = true;
                }
            }
            

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(COL_GAP_GUIDE);
            shapeRenderer.rect(arenaX + obsGapX[i], arenaY, obsGapW[i], ARENA_H);
            shapeRenderer.end();

            if (leftSegW > 0f) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(COL_OBS_FILL);
                shapeRenderer.rect(arenaX, obsScreenY, leftSegW, OBS_H);
                shapeRenderer.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(COL_OBS_HILIGHT);
                shapeRenderer.rect(arenaX, obsScreenY + OBS_H - 2f, leftSegW, 2f);
                shapeRenderer.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(COL_OBS_EDGE);
                shapeRenderer.rect(arenaX, obsScreenY, leftSegW, OBS_H);
                shapeRenderer.end();
            }

            if (rightSegW > 0f) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(COL_OBS_FILL);
                shapeRenderer.rect(arenaX + rightSegX, obsScreenY, rightSegW, OBS_H);
                shapeRenderer.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(COL_OBS_HILIGHT);
                shapeRenderer.rect(arenaX + rightSegX, obsScreenY + OBS_H - 2f, rightSegW, 2f);
                shapeRenderer.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(COL_OBS_EDGE);
                shapeRenderer.rect(arenaX + rightSegX, obsScreenY, rightSegW, OBS_H);
                shapeRenderer.end();
            }
        }
    }

    private Color difficultyColor() {
        switch (difficulty) {
            case SIMPLE:  return COL_DIFF_SIMPLE;
            case COMPLEX: return COL_DIFF_COMPLEX;
            default:      return COL_DIFF_MEDIUM;
        }
    }

    public void resize(int w, int h) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        fishTexture.dispose();
        hookTexture.dispose();
    }
}