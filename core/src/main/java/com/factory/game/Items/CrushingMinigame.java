package com.factory.game.Items;

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
import com.badlogic.gdx.math.MathUtils;

public class CrushingMinigame {

    public static class CrushResult {
        public final Item    item;
        public final int     qty;
        public final boolean success;

        public CrushResult(Item item, int qty, boolean success) {
            this.item    = item;
            this.qty     = qty;
            this.success = success;
        }
    }

    private static final float TOTAL_TIME       = 10.0f;
    private static final float REQUIRED_IN_ZONE = 6.0f;
    private static final float ZONE_HALF_SIZE   = 0.12f;
    private static final float BASE_ZONE_SPEED  = .8f;
    private static final float ZONE_SPEED_RAMP  = 0.18f;
    private static final float LIFT_ACCEL       = 1.8f;
    private static final float GRAVITY          = 2.4f;
    private static final float MAX_SPEED        = 1.1f;
    private static final float RESULT_DISPLAY_TIME = 1.8f;

    private enum State { INACTIVE, COUNTDOWN, RUNNING, SHOWING_RESULT }
    private State state = State.INACTIVE;

    private CrushingRecipe activeRecipe = null;

    private float linePos      = 0.5f;
    private float lineVelocity = 0f;

    private float zonePos   = 0.5f;
    private float zonePhase = 0f;

    private float timeElapsed    = 0f;
    private float timeInZone     = 0f;
    private float countdownTimer = 0f;
    private static final float COUNTDOWN_DURATION = 3.0f;

    private boolean lastInZone = false;
    private float   flashTimer = 0f;
    private boolean flashIsEntry = false;   

    private float       resultTimer   = 0f;
    private CrushResult pendingResult = null;
    private boolean     resultSuccess = false;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont    font;

    public CrushingMinigame() {
        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 18;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }

    public void start(CrushingRecipe recipe) {
        activeRecipe    = recipe;
        state           = State.COUNTDOWN;
        countdownTimer  = COUNTDOWN_DURATION;
        linePos         = 0.0f;                     
        lineVelocity    = 0f;
        zonePhase       = -MathUtils.PI / 2f;          
        zonePos         = 0.15f;                    
        timeElapsed     = 0f;
        timeInZone      = 0f;
        lastInZone      = false;
        flashTimer      = 0f;
        pendingResult   = null;
        resultSuccess   = false;
    }

    public boolean isActive() {
        return state != State.INACTIVE;
    }


    public CrushResult pollResult() {
        if (state == State.INACTIVE && pendingResult != null) {
            CrushResult r = pendingResult;
            pendingResult = null;
            return r;
        }
        return null;
    }

    public void update(float delta) {
        if (state == State.INACTIVE) return;
        flashTimer = Math.max(0f, flashTimer - delta);

        if (state == State.COUNTDOWN) {
            countdownTimer -= delta;
            if (countdownTimer <= 0f) {
                state = State.RUNNING;
            }
            return;
        }

        if (state == State.RUNNING) {
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                lineVelocity += LIFT_ACCEL * delta;
            } else {
                lineVelocity -= GRAVITY * delta;
            }
            lineVelocity = MathUtils.clamp(lineVelocity, -MAX_SPEED, MAX_SPEED);
            linePos     += lineVelocity * delta;

            if (linePos <= 0f) { linePos = 0f; lineVelocity = 0f; }
            if (linePos >= 1f) { linePos = 1f; lineVelocity = 0f; }

            float speed = BASE_ZONE_SPEED + timeElapsed * ZONE_SPEED_RAMP;
            zonePhase += speed * delta;
            zonePos = 0.15f + 0.70f * (0.5f + 0.5f * MathUtils.sin(zonePhase));

            boolean inZone = Math.abs(linePos - zonePos) <= ZONE_HALF_SIZE;
            if (inZone) timeInZone += delta;

            if (inZone != lastInZone) {
                flashTimer   = 0.28f;
                flashIsEntry = inZone;
                lastInZone   = inZone;
            }

            timeElapsed += delta;
            if (timeElapsed >= TOTAL_TIME) {
                finishGame();
            }

        } else if (state == State.SHOWING_RESULT) {
            resultTimer -= delta;
            if (resultTimer <= 0f) {
                state = State.INACTIVE;
            }
        }
    }

    private void finishGame() {
        resultSuccess = (timeInZone >= REQUIRED_IN_ZONE);
        if (resultSuccess) {
            pendingResult = new CrushResult(
                    activeRecipe.getOutputItem(),
                    activeRecipe.getOutputQuantity(),
                    true);
        } 
        state       = State.SHOWING_RESULT;
        resultTimer = RESULT_DISPLAY_TIME;
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
        shapeRenderer.setColor(0f, 0f, 0f, 0.62f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();

        float panelW = 560f, panelH = 430f;
        float panelX = (sw - panelW) * 0.5f;
        float panelY = (sh - panelH) * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.07f, 0.07f, 0.13f, 0.97f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.55f, 0.35f, 0.20f, 1f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();


        float instrY      = panelY + 18f;
        float progressH   = 16f;
        float progressW   = panelW - 90f;
        float progressX   = panelX + 45f;
        float progressY   = panelY + 56f;

        float trackW      = 68f;
        float trackH      = 200f;
        float trackX      = panelX + (panelW - trackW) * 0.5f;
        float trackY      = progressY + progressH + 50f;   
        float subtitleY   = trackY + trackH + 22f;
        float titleY      = subtitleY + 28f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.13f, 0.13f, 0.20f, 1f);
        shapeRenderer.rect(trackX, trackY, trackW, trackH);
        shapeRenderer.end();

        if (state == State.RUNNING) {
            
            float zoneCY    = trackY + zonePos * trackH;
            float zoneHalfPx = ZONE_HALF_SIZE * trackH;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.12f, 0.72f, 0.28f, 0.45f);
            shapeRenderer.rect(trackX, zoneCY - zoneHalfPx, trackW, zoneHalfPx * 2f);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.25f, 1f, 0.42f, 0.88f);
            shapeRenderer.rect(trackX, zoneCY - zoneHalfPx, trackW, zoneHalfPx * 2f);
            shapeRenderer.end();
        }

        if (flashTimer > 0f) {
            float alpha = (flashTimer / 0.28f) * 0.38f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(
                    flashIsEntry ? 0.2f : 1f,
                    flashIsEntry ? 1f   : 0.35f,
                    flashIsEntry ? 0.3f : 0.2f,
                    alpha);
            shapeRenderer.rect(trackX, trackY, trackW, trackH);
            shapeRenderer.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.40f, 0.28f, 0.18f, 1f);
        shapeRenderer.rect(trackX, trackY, trackW, trackH);
        shapeRenderer.end();

        if (state == State.RUNNING) {
            float indY   = trackY + linePos * trackH;
            boolean inZ  = Math.abs(linePos - zonePos) <= ZONE_HALF_SIZE;
            Color lineCol = inZ
                    ? new Color(0.3f, 1f, 0.45f, 1f)     
                    : new Color(1f, 0.82f, 0.08f, 1f);   
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(lineCol);
            shapeRenderer.rect(trackX - 8f, indY - 5f, trackW + 16f, 10f);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(lineCol.r * 0.7f, lineCol.g * 0.7f, lineCol.b * 0.7f, 1f);
            shapeRenderer.rect(trackX - 8f, indY - 5f, 5f, 10f);
            shapeRenderer.rect(trackX + trackW + 3f, indY - 5f, 5f, 10f);
            shapeRenderer.end();
        }

        if (state == State.RUNNING) {
            float timeLeft   = Math.max(0f, TOTAL_TIME - timeElapsed);
            float timeBarH   = (timeLeft / TOTAL_TIME) * trackH;
            float timeBarX   = trackX + trackW + 14f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.15f, 0.10f, 0.08f, 1f);
            shapeRenderer.rect(timeBarX, trackY, 10f, trackH);
            shapeRenderer.end();
            Color tColor = timeLeft > 2f
                    ? new Color(0.85f, 0.65f, 0.15f, 1f)
                    : new Color(0.95f, 0.25f, 0.20f, 1f);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(tColor);
            shapeRenderer.rect(timeBarX, trackY, 10f, timeBarH);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.40f, 0.28f, 0.18f, 1f);
            shapeRenderer.rect(timeBarX, trackY, 10f, trackH);
            shapeRenderer.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.13f, 0.13f, 0.20f, 1f);
        shapeRenderer.rect(progressX, progressY, progressW, progressH);
        shapeRenderer.end();

        float fillRatio = Math.min(1f, timeInZone / REQUIRED_IN_ZONE);
        Color fillColor = fillRatio >= 1f
                ? new Color(0.18f, 0.95f, 0.28f, 1f)
                : new Color(0.88f, 0.60f, 0.10f, 1f);
        if (fillRatio > 0f) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fillColor);
            shapeRenderer.rect(progressX, progressY, progressW * fillRatio, progressH);
            shapeRenderer.end();
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.40f, 0.28f, 0.18f, 1f);
        shapeRenderer.rect(progressX, progressY, progressW, progressH);
        shapeRenderer.end();

        batch.begin();
        GlyphLayout layout = new GlyphLayout();

        font.getData().setScale(1.5f);
        font.setColor(1f, 0.82f, 0.50f, 1f);
        layout.setText(font, "~ Crushing ~");
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                titleY + layout.height * 0.5f);

        if (activeRecipe != null) {
            font.getData().setScale(1.0f);
            font.setColor(0.70f, 0.60f, 0.45f, 1f);
            String sub = "Crushing:  " + activeRecipe.getInputItem().getDisplayName();
            layout.setText(font, sub);
            font.draw(batch, layout,
                    panelX + (panelW - layout.width) * 0.5f,
                    subtitleY);
        }

        if (state == State.RUNNING) {
            float timeLeft = Math.max(0f, TOTAL_TIME - timeElapsed);
            font.getData().setScale(0.95f);
            font.setColor(timeLeft > 2f
                    ? new Color(0.85f, 0.70f, 0.40f, 1f)
                    : new Color(1f,    0.35f, 0.30f, 1f));
            layout.setText(font, String.format("%.1fs", timeLeft));
            font.draw(batch, layout,
                    trackX + trackW + 28f,
                    trackY + trackH * 0.5f + layout.height * 0.5f);
        }

        font.getData().setScale(0.95f);
        font.setColor(0.65f, 0.55f, 0.40f, 1f);
        String progLabel = String.format("%.1f / %.1fs  in zone", timeInZone, REQUIRED_IN_ZONE);
        layout.setText(font, progLabel);
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                progressY + progressH + 16f);

        font.getData().setScale(1.1f);
        String instrText;
        if (state == State.COUNTDOWN) {
            instrText = "Get ready...";
            font.setColor(0.75f, 0.65f, 0.40f, 1f);
        } else if (state == State.RUNNING) {
            instrText = "Hold  SPACE  to lift - keep line in the zone";
            font.setColor(0.85f, 0.78f, 0.55f, 1f);
        } else if (resultSuccess) {
            instrText = "Crushed! Got: "
                    + pendingResult.item.getDisplayName()
                    + "  x" + pendingResult.qty;
            font.setColor(0.25f, 1f, 0.38f, 1f);
        } else {
            instrText = "Not enough time in zone!";
            font.setColor(1f, 0.38f, 0.32f, 1f);
        }
        layout.setText(font, instrText);
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                instrY);

        if (state == State.COUNTDOWN) {
            int countNum = (int) Math.ceil(countdownTimer);   
            float frac   = countdownTimer - (float)(countNum - 1); 
            float pulse  = 1.0f - frac * 0.35f;           

            font.getData().setScale(5.0f * pulse);
            Color numColor;
            if      (countNum == 3) numColor = new Color(1f,  0.85f, 0.15f, 1f);
            else if (countNum == 2) numColor = new Color(1f,  0.55f, 0.10f, 1f);
            else                   numColor = new Color(1f,  0.22f, 0.18f, 1f);
            font.setColor(numColor.r, numColor.g, numColor.b, Math.min(1f, frac + 0.3f));

            layout.setText(font, String.valueOf(countNum));
            font.draw(batch, layout,
                    trackX + (trackW - layout.width) * 0.5f,
                    trackY + trackH * 0.5f + layout.height * 0.5f);

            font.getData().setScale(1.2f);
            font.setColor(Color.WHITE);
        }


    }

    public void resize(int w, int h) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}