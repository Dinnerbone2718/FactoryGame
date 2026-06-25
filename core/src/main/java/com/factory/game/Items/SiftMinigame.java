package com.factory.game.Items;

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

public class SiftMinigame {

    public static class SiftResult {
        public final Item item;
        public final int  qty;
        public SiftResult(Item item, int qty) { this.item = item; this.qty = qty; }
    }

    private static final int   TOTAL_ROUNDS        = 3;
    private static final float RESULT_DISPLAY_TIME = 1.6f;

    private static final float[][] ROUND_CONFIG = {
        { 0.42f, 0.14f },   
        { 0.80f, 0.08f }, 
        { 0.90f, 0.06f },  
 
    };

    private enum State { INACTIVE, RUNNING, SHOWING_RESULT }
    private State state = State.INACTIVE;

    private int       round      = 0;
    private boolean[] hits       = new boolean[TOTAL_ROUNDS];
    private boolean   allHit     = false;

    private SiftingRecipe activeRecipe = null;

    private float barPos     = 0f;   
    private float barDir     = 1f;
    private float zoneCenter = 0.5f; 

    private float   flashTimer = 0f;
    private boolean flashHit   = false;

    private float       resultTimer   = 0f;
    private SiftResult  pendingResult = null;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont    font;
    private final Random        rng = new Random();

    public SiftMinigame() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 18;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }


    public void start(SiftingRecipe recipe) {
        activeRecipe  = recipe;
        state         = State.RUNNING;
        round         = 0;
        hits          = new boolean[TOTAL_ROUNDS];
        allHit        = false;
        flashTimer    = 0f;
        pendingResult = null;
        barPos        = rng.nextFloat();
        barDir        = rng.nextBoolean() ? 1f : -1f;
        zoneCenter    = randomZoneCenter();
    }

    public boolean isActive() {
        return state != State.INACTIVE;
    }


    public SiftResult pollResult() {
        if (state == State.INACTIVE && pendingResult != null) {
            SiftResult r = pendingResult;
            pendingResult = null;
            return r;
        }
        return null;
    }


    public void update(float delta) {
        if (state == State.INACTIVE) return;

        flashTimer = Math.max(0f, flashTimer - delta);

        if (state == State.RUNNING) {
            float speed = ROUND_CONFIG[round][0];
            barPos += barDir * speed * delta;
            if (barPos > 1f) { barPos = 1f; barDir = -1f; }
            if (barPos < 0f) { barPos = 0f; barDir =  1f; }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                float halfW  = ROUND_CONFIG[round][1];
                boolean hit  = Math.abs(barPos - zoneCenter) <= halfW;
                hits[round]  = hit;
                flashHit     = hit;
                flashTimer   = 0.35f;
                round++;

                if (round >= TOTAL_ROUNDS) {
                    allHit = true;
                    for (boolean h : hits) if (!h) { allHit = false; break; }

                    pendingResult = allHit
                        ? new SiftResult(activeRecipe.roll(rng), 1)
                        : new SiftResult(Item.STONE, 2);

                    state       = State.SHOWING_RESULT;
                    resultTimer = RESULT_DISPLAY_TIME;
                } else {
                    zoneCenter = randomZoneCenter();
                }
            }

        } else if (state == State.SHOWING_RESULT) {
            resultTimer -= delta;
            if (resultTimer <= 0f) {
                state = State.INACTIVE;
            }
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
        shapeRenderer.setColor(0f, 0f, 0f, 0.62f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();


        float panelW = 560f, panelH = 260f;
        float panelX = (sw - panelW) * 0.5f;
        float panelY = (sh - panelH) * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.07f, 0.07f, 0.13f, 0.97f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.38f, 0.38f, 0.62f, 1f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        float topPad      = 16f;
        float titleH      = 28f;
        float gapA        = 18f;
        float circleD     = 26f;   
        float gapB        = 18f;
        float trackH      = 38f;
        float gapC        = 14f;
        float instrH      = 22f;

        float titleRowY   = panelY + panelH - topPad - titleH;         
        float circleRowCY = panelY + panelH - topPad - titleH - gapA - circleD * 0.5f;  
        float trackY      = panelY + panelH - topPad - titleH - gapA - circleD - gapB - trackH;
        float instrCY     = trackY - gapC - instrH * 0.5f;              

        float circleR  = 11f;
        float spacing  = 40f;
        float cStartX  = panelX + panelW * 0.5f - ((TOTAL_ROUNDS - 1) * spacing * 0.5f);

        for (int i = 0; i < TOTAL_ROUNDS; i++) {
            float cx = cStartX + i * spacing;
            Color fill;
            if (state == State.SHOWING_RESULT || i < round) {
                fill = hits[i]
                    ? new Color(0.18f, 0.95f, 0.28f, 1f)
                    : new Color(0.95f, 0.22f, 0.22f, 1f);
            } else if (i == round) {
                fill = new Color(1f, 0.85f, 0.15f, 1f);
            } else {
                fill = new Color(0.28f, 0.28f, 0.40f, 1f);
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fill);
            shapeRenderer.circle(cx, circleRowCY, circleR, 24);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 1f, 1f, 0.35f);
            shapeRenderer.circle(cx, circleRowCY, circleR, 24);
            shapeRenderer.end();
        }

        float trackX = panelX + 40f;
        float trackW = panelW - 80f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.13f, 0.13f, 0.20f, 1f);
        shapeRenderer.rect(trackX, trackY, trackW, trackH);
        shapeRenderer.end();

        if (state == State.RUNNING) {
            float halfW   = ROUND_CONFIG[round][1];
            float zoneX   = trackX + (zoneCenter - halfW) * trackW;
            float zoneWPx = halfW * 2f * trackW;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.12f, 0.72f, 0.28f, 0.50f);
            shapeRenderer.rect(zoneX, trackY, zoneWPx, trackH);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.25f, 1f, 0.42f, 0.90f);
            shapeRenderer.rect(zoneX, trackY, zoneWPx, trackH);
            shapeRenderer.end();
        }

        if (flashTimer > 0f) {
            float alpha = (flashTimer / 0.35f) * 0.42f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(
                flashHit ? 0.2f : 1f,
                flashHit ? 1f   : 0.2f,
                flashHit ? 0.3f : 0.2f,
                alpha);
            shapeRenderer.rect(trackX, trackY, trackW, trackH);
            shapeRenderer.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.32f, 0.32f, 0.48f, 1f);
        shapeRenderer.rect(trackX, trackY, trackW, trackH);
        shapeRenderer.end();

        if (state == State.RUNNING) {
            float indX = trackX + barPos * trackW;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.88f, 0.08f, 1f);
            shapeRenderer.rect(indX - 3f, trackY - 5f, 6f, trackH + 10f);
            shapeRenderer.end();
        }

        batch.begin();

        GlyphLayout layout = new GlyphLayout();

        font.getData().setScale(1.5f);
        font.setColor(0.88f, 0.88f, 1f, 1f);
        layout.setText(font, "~ Sifting ~");
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                titleRowY + layout.height);

        font.getData().setScale(1.1f);
        String instrText;
        if (state == State.RUNNING) {
            font.setColor(0.75f, 0.95f, 0.75f, 1f);
            instrText = "Press SPACE when inside the zone";
        } else if (allHit) {
            font.setColor(0.25f, 1f, 0.38f, 1f);
            instrText = "Success! Found: " + pendingResult.item.getDisplayName();
        } else {
            font.setColor(1f, 0.38f, 0.32f, 1f);
            instrText = "Missed! Got: Stone x2";
        }
        layout.setText(font, instrText);
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                instrCY + layout.height * 0.5f);

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }


    public void resize(int w, int h) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }


    private float randomZoneCenter() {
        return 0.20f + rng.nextFloat() * 0.60f;
    }
}