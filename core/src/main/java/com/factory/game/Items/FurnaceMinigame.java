package com.factory.game.Items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.World.LiquidType;


public class FurnaceMinigame {

    public static class FurnaceResult {
        public final LiquidType liquid;   
        public final float      amount;
        public final boolean    success;

        public FurnaceResult(LiquidType liquid, float amount, boolean success) {
            this.liquid  = liquid;
            this.amount  = amount;
            this.success = success;
        }
    }

    private static final float TOTAL_TIME          = 15f;
    private static final float COUNTDOWN_DURATION  = 3f;
    private static final float RESULT_DISPLAY_TIME = 1.8f;
    private static final int   MAX_LIVES           = 3;

    private static final float PLAYER_W     = 38f;
    private static final float PLAYER_H     = 16f;
    private static final float PLAYER_SPEED = 195f;   

    private static final float BLOCK_W             = 30f;   
    private static final float BLOCK_H             = 22f;
    private static final float BASE_BLOCK_SPEED    = 175f; 
    private static final float BLOCK_SPEED_RAMP    = 10f;   
    private static final float BASE_SPAWN_INTERVAL = 0.75f;
    private static final float MIN_SPAWN_INTERVAL  = 0.22f; 

    private static final float INVINCIBLE_TIME = 0.85f;

    private enum State { INACTIVE, COUNTDOWN, RUNNING, SHOWING_RESULT }
    private State state = State.INACTIVE;

    private SmeltingRecipe activeRecipe  = null;
    private FurnaceResult  pendingResult = null;
    private boolean        resultSuccess = false;

    private float countdownTimer  = 0f;
    private float timeElapsed     = 0f;
    private int   lives           = MAX_LIVES;

    private float playerX   = 0f;
    private float playerDir = 1f;  

    private static class FallingBlock {
        float x, y;  
        FallingBlock(float x, float y) { this.x = x; this.y = y; }
    }
    private final List<FallingBlock> blocks = new ArrayList<>();
    private float spawnTimer = 0f;

    private float   flashTimer      = 0f;
    private float   invincibleTimer = 0f;
    private float   resultTimer     = 0f;

    private float arenaW = 480f;
    private float arenaH = 220f;

    private final Random        rng           = new Random();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont    font;

    public FurnaceMinigame() {
        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 18;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }

    public void start(SmeltingRecipe recipe) {
        activeRecipe     = recipe;
        state            = State.COUNTDOWN;
        countdownTimer   = COUNTDOWN_DURATION;
        timeElapsed      = 0f;
        lives            = MAX_LIVES;
        playerX          = arenaW * 0.5f - PLAYER_W * 0.5f;
        playerDir        = 1f;
        spawnTimer       = 0.5f;  
        flashTimer       = 0f;
        invincibleTimer  = 0f;
        blocks.clear();
        pendingResult    = null;
        resultSuccess    = false;
    }

    public boolean isActive() {
        return state != State.INACTIVE;
    }


    public FurnaceResult pollResult() {
        if (state == State.INACTIVE && pendingResult != null) {
            FurnaceResult r = pendingResult;
            pendingResult   = null;
            return r;
        }
        return null;
    }

    public void update(float delta) {
        if (state == State.INACTIVE) return;

        flashTimer      = Math.max(0f, flashTimer      - delta);
        invincibleTimer = Math.max(0f, invincibleTimer - delta);

        if (state == State.COUNTDOWN) {
            countdownTimer -= delta;
            if (countdownTimer <= 0f) state = State.RUNNING;
            return;
        }

        if (state == State.RUNNING) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                playerDir = -playerDir;
            }

            playerX += playerDir * PLAYER_SPEED * delta;
            if (playerX <= 0f) {
                playerX   = 0f;
                playerDir = 1f;  
            }
            if (playerX >= arenaW - PLAYER_W) {
                playerX   = arenaW - PLAYER_W;
                playerDir = -1f; 
            }

            spawnTimer -= delta;
            float progress      = timeElapsed / TOTAL_TIME;  
            float spawnInterval = Math.max(MIN_SPAWN_INTERVAL,
                    BASE_SPAWN_INTERVAL - progress * (BASE_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL));
            if (spawnTimer <= 0f) {
                float bx = rng.nextFloat() * (arenaW - BLOCK_W);
                blocks.add(new FallingBlock(bx, arenaH + BLOCK_H));
                spawnTimer = spawnInterval;
            }

            float blockSpeed = BASE_BLOCK_SPEED + BLOCK_SPEED_RAMP * timeElapsed;
            Iterator<FallingBlock> it = blocks.iterator();
            while (it.hasNext()) {
                FallingBlock b = it.next();
                b.y -= blockSpeed * delta;


                if (b.y <= 0f) {
                    it.remove();
                    continue;
                }

                if (invincibleTimer <= 0f) {
                    boolean hx = b.x < playerX + PLAYER_W && b.x + BLOCK_W > playerX;
                    boolean hy = b.y < PLAYER_H            && b.y + BLOCK_H > 0f;
                    if (hx && hy) {
                        lives--;
                        flashTimer      = 0.40f;
                        invincibleTimer = INVINCIBLE_TIME;
                        it.remove();
                        if (lives <= 0) {
                            finishGame(false);
                            return;
                        }
                        continue;
                    }
                }
            }

            timeElapsed += delta;
            if (timeElapsed >= TOTAL_TIME) {
                finishGame(true);
            }

        } else if (state == State.SHOWING_RESULT) {
            resultTimer -= delta;
            if (resultTimer <= 0f) state = State.INACTIVE;
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
        shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();

        float panelW = 580f, panelH = 480f;
        float panelX = (sw - panelW) * 0.5f;
        float panelY = (sh - panelH) * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.09f, 0.04f, 0.02f, 0.97f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.78f, 0.36f, 0.08f, 1f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();


        float titleTopY   = panelY + panelH - 20f;  
        float subtitleY   = panelY + panelH - 52f;  

        float timerBarX = panelX + 48f;
        float timerBarY = panelY + panelH - 82f;
        float timerBarW = panelW - 96f;
        float timerBarH = 10f;

        float livesRowY = panelY + panelH - 120f;

        arenaW = panelW - 96f;  
        arenaH = 220f;
        float arenaX = panelX + 48f;
        float arenaY = panelY + 56f;   
        float instrY = panelY + 28f;


        {
            float timeLeft = Math.max(0f, TOTAL_TIME - timeElapsed);
            float ratio    = timeLeft / TOTAL_TIME;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.16f, 0.08f, 0.04f, 1f);
            shapeRenderer.rect(timerBarX, timerBarY, timerBarW, timerBarH);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(timeLeft > 3f
                    ? new Color(0.90f, 0.52f, 0.08f, 1f)
                    : new Color(0.96f, 0.22f, 0.10f, 1f));
            shapeRenderer.rect(timerBarX, timerBarY, timerBarW * ratio, timerBarH);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.55f, 0.28f, 0.10f, 1f);
            shapeRenderer.rect(timerBarX, timerBarY, timerBarW, timerBarH);
            shapeRenderer.end();
        }

        float lifeR       = 10f;
        float lifeSpacing = 28f;
        float lifeGroupW  = MAX_LIVES * lifeSpacing - (lifeSpacing - lifeR * 2f);
        float lifeStartX  = panelX + panelW - 52f - lifeGroupW;
        for (int i = 0; i < MAX_LIVES; i++) {
            float lx    = lifeStartX + i * lifeSpacing + lifeR;
            boolean alive = (i < lives);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(alive
                    ? new Color(1f, 0.42f, 0.04f, 1f)
                    : new Color(0.22f, 0.12f, 0.08f, 1f));
            shapeRenderer.circle(lx, livesRowY, lifeR, 16);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(alive
                    ? new Color(1f, 0.75f, 0.18f, 1f)
                    : new Color(0.38f, 0.22f, 0.14f, 1f));
            shapeRenderer.circle(lx, livesRowY, lifeR, 16);
            shapeRenderer.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.06f, 0.02f, 0.01f, 1f);
        shapeRenderer.rect(arenaX, arenaY, arenaW, arenaH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.85f, 0.20f, 0.02f, 0.38f);
        shapeRenderer.rect(arenaX, arenaY, arenaW, 20f);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.55f, 0.10f, 0.65f);
        shapeRenderer.rect(arenaX, arenaY + 18f, arenaW, 2f);
        shapeRenderer.end();

        if (flashTimer > 0f) {
            float alpha = (flashTimer / 0.40f) * 0.42f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.12f, 0.08f, alpha);
            shapeRenderer.rect(arenaX, arenaY, arenaW, arenaH);
            shapeRenderer.end();
        }

        if (state == State.RUNNING || state == State.SHOWING_RESULT) {
            for (FallingBlock b : blocks) {
                float bsx = arenaX + b.x;
                float bsy = arenaY + b.y;
                if (bsy > arenaY + arenaH) continue;
                float renderY = Math.max(bsy, arenaY);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0.82f, 0.40f, 0.06f, 1f);
                shapeRenderer.rect(bsx, renderY, BLOCK_W, BLOCK_H);
                shapeRenderer.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(1f, 0.68f, 0.14f, 1f);
                shapeRenderer.rect(bsx, renderY, BLOCK_W, BLOCK_H);
                shapeRenderer.end();
            }
        }

        if (state == State.RUNNING) {
            boolean blink = invincibleTimer > 0f && ((int)(invincibleTimer * 9) % 2 == 0);
            if (!blink) {
                float psx = arenaX + playerX;
                float psy = arenaY;

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0.18f, 0.78f, 1.00f, 1f);
                shapeRenderer.rect(psx, psy, PLAYER_W, PLAYER_H);
                shapeRenderer.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(0.65f, 1f, 1f, 1f);
                shapeRenderer.rect(psx, psy, PLAYER_W, PLAYER_H);
                shapeRenderer.end();

                float arrowCX = psx + PLAYER_W * 0.5f;
                float arrowCY = psy + PLAYER_H * 0.5f;
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(1f, 1f, 1f, 0.88f);
                if (playerDir > 0) {
                    shapeRenderer.triangle(
                            arrowCX + 9f, arrowCY,
                            arrowCX - 4f, arrowCY + 5f,
                            arrowCX - 4f, arrowCY - 5f);
                } else {
                    shapeRenderer.triangle(
                            arrowCX - 9f, arrowCY,
                            arrowCX + 4f, arrowCY + 5f,
                            arrowCX + 4f, arrowCY - 5f);
                }
                shapeRenderer.end();
            }
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.65f, 0.30f, 0.08f, 1f);
        shapeRenderer.rect(arenaX, arenaY, arenaW, arenaH);
        shapeRenderer.end();

        batch.begin();
        GlyphLayout layout = new GlyphLayout();

        font.getData().setScale(1.5f);
        font.setColor(1f, 0.62f, 0.18f, 1f);
        layout.setText(font, "~ Smelting ~");
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                titleTopY);

        if (activeRecipe != null) {
            font.getData().setScale(1.0f);
            font.setColor(0.75f, 0.52f, 0.28f, 1f);
            String sub = "Smelting:  " + activeRecipe.getInputItem().getDisplayName();
            layout.setText(font, sub);
            font.draw(batch, layout,
                    panelX + (panelW - layout.width) * 0.5f,
                    subtitleY);
        }

        if (state == State.RUNNING) {
            float timeLeft = Math.max(0f, TOTAL_TIME - timeElapsed);
            font.getData().setScale(0.88f);
            font.setColor(timeLeft > 3f
                    ? new Color(0.92f, 0.72f, 0.32f, 1f)
                    : new Color(1f,    0.28f, 0.18f, 1f));
            layout.setText(font, String.format("%.1fs", timeLeft));
            font.draw(batch, layout,
                    timerBarX + timerBarW - layout.width,
                    timerBarY + timerBarH + 16f);
        }

        font.getData().setScale(0.95f);
        font.setColor(0.72f, 0.48f, 0.28f, 1f);
        layout.setText(font, "Lives:");
        font.draw(batch, layout,
                lifeStartX - layout.width - 10f,
                livesRowY + layout.height * 0.5f);

        if (state == State.COUNTDOWN) {
            int   countNum = (int) Math.ceil(countdownTimer);
            float frac     = countdownTimer - (countNum - 1);
            float pulse    = 1.0f - frac * 0.35f;
            font.getData().setScale(5.0f * pulse);
            Color numColor;
            if      (countNum == 3) numColor = new Color(1f, 0.85f, 0.15f, 1f);
            else if (countNum == 2) numColor = new Color(1f, 0.55f, 0.10f, 1f);
            else                   numColor = new Color(1f, 0.22f, 0.18f, 1f);
            font.setColor(numColor.r, numColor.g, numColor.b, Math.min(1f, frac + 0.3f));
            layout.setText(font, String.valueOf(countNum));
            font.draw(batch, layout,
                    arenaX + (arenaW - layout.width) * 0.5f,
                    arenaY + arenaH * 0.5f + layout.height * 0.5f);
            font.getData().setScale(1.2f);
            font.setColor(Color.WHITE);
        }

        String instrText;
        if (state == State.COUNTDOWN) {
            instrText = "Get ready to dodge!";
            font.setColor(0.80f, 0.65f, 0.32f, 1f);
        } else if (state == State.RUNNING) {
            instrText = "SPACE to change direction";
            font.setColor(0.86f, 0.76f, 0.50f, 1f);
        } else if (resultSuccess) {
            String liquidName = (pendingResult != null && pendingResult.liquid != null)
                    ? pendingResult.liquid.name : "";
            instrText = "Success!  Produced: " + liquidName;
            font.setColor(0.22f, 1f, 0.36f, 1f);
        } else {
            instrText = "Failed!  The ore was lost.";
            font.setColor(1f, 0.32f, 0.25f, 1f);
        }
        font.getData().setScale(1.0f);
        layout.setText(font, instrText);
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                instrY + layout.height);

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }

    private void finishGame(boolean success) {
        resultSuccess = success;
        pendingResult = success
                ? new FurnaceResult(activeRecipe.getOutputLiquid(),
                                    activeRecipe.getOutputAmount(), true)
                : new FurnaceResult(null, 0f, false);
        state       = State.SHOWING_RESULT;
        resultTimer = RESULT_DISPLAY_TIME;
    }

    public void resize(int w, int h) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}