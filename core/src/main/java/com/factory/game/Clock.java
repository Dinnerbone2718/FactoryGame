package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Clock {

    private static final int CORNER_SIZE   = 180;   
    private static final int CORNER_PAD    = 14;   

    private static final int PANEL_W       = CORNER_SIZE;  
    private static final int PANEL_H       = 72;
    private static final int PANEL_GAP     = 6;           

    private static final int CLOCK_RADIUS  = 22;
    private static final int CLOCK_PAD     = 12;        

    private static final Color BG_COLOR     = new Color(0.08f, 0.08f, 0.12f, 0.92f);
    private static final Color BORDER_COLOR = new Color(0.3f,  0.3f,  0.45f, 1.00f);
    private static final Color DIVIDER      = new Color(0.25f, 0.25f, 0.35f, 1.00f);
    private static final Color CLOCK_FACE   = new Color(0.14f, 0.14f, 0.20f, 1.00f);
    private static final Color TICK_COLOR   = new Color(0.48f, 0.48f, 0.65f, 1.00f);
    private static final Color HAND_HOUR    = new Color(0.88f, 0.88f, 1.00f, 1.00f);
    private static final Color HAND_MIN     = new Color(0.60f, 0.60f, 0.78f, 1.00f);
    private static final Color CENTER_DOT   = new Color(1.00f, 0.90f, 0.20f, 1.00f);
    private static final Color LABEL_COLOR  = new Color(0.88f, 0.88f, 1.00f, 1.00f);
    private static final Color TIME_COLOR   = new Color(1.00f, 0.95f, 0.60f, 1.00f);

    private final ShapeRenderer shapeRenderer;
    private final BitmapFont    font;

    private int screenWidth, screenHeight;

    public Clock(Main main, int screenWidth, int screenHeight) {
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;
        this.shapeRenderer = new ShapeRenderer();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size  = 14;
        parameter.color = Color.WHITE;
        this.font = generator.generateFont(parameter);
        generator.dispose();
    }


    public void render(SpriteBatch batch, float time) {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        int panelX = sw - PANEL_W - CORNER_PAD;
        int panelY = sh - CORNER_SIZE - CORNER_PAD - PANEL_GAP - PANEL_H;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(BG_COLOR);
        shapeRenderer.rect(panelX, panelY, PANEL_W, PANEL_H);

        int cxInt = panelX + CLOCK_PAD + CLOCK_RADIUS;
        int cyInt = panelY + PANEL_H / 2;
        shapeRenderer.setColor(CLOCK_FACE);
        shapeRenderer.circle(cxInt, cyInt, CLOCK_RADIUS);

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(panelX, panelY, PANEL_W, PANEL_H);

        int divX = panelX + CLOCK_PAD * 2 + CLOCK_RADIUS * 2 + 4;
        shapeRenderer.setColor(DIVIDER);
        shapeRenderer.line(divX, panelY + 8, divX, panelY + PANEL_H - 8);
        shapeRenderer.end();

        float cx = cxInt;
        float cy = cyInt;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 12; i++) {
            float angle  = (float) (i / 12.0 * 2 * Math.PI);
            float cos    = (float) Math.cos(angle);
            float sin    = (float) Math.sin(angle);
            boolean main = (i % 3 == 0);
            float outer  = CLOCK_RADIUS - 2;
            float inner  = outer - (main ? 5 : 3);
            float r      = main ? 1.5f : 1.0f;
            shapeRenderer.setColor(main ? TICK_COLOR : DIVIDER);
            shapeRenderer.circle(cx + cos * inner, cy + sin * inner, r);
        }


        float hourAngleDeg = time * 360f * 2f;        
        float hourRad      = (float) Math.toRadians(-hourAngleDeg + 90);
        float hourLen      = CLOCK_RADIUS * 0.55f;
        shapeRenderer.setColor(HAND_HOUR);
        shapeRenderer.rectLine(
                cx, cy,
                cx + (float) Math.cos(hourRad) * hourLen,
                cy + (float) Math.sin(hourRad) * hourLen,
                2.5f);

        float minAngleDeg = (time * 360f * 24f) % 360f;   
        float minRad      = (float) Math.toRadians(-minAngleDeg + 90);
        float minLen      = CLOCK_RADIUS * 0.80f;
        shapeRenderer.setColor(HAND_MIN);
        shapeRenderer.rectLine(
                cx, cy,
                cx + (float) Math.cos(minRad) * minLen,
                cy + (float) Math.sin(minRad) * minLen,
                1.5f);

        shapeRenderer.setColor(CENTER_DOT);
        shapeRenderer.circle(cx, cy, 2.5f);

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(TICK_COLOR);
        shapeRenderer.circle(cx, cy, CLOCK_RADIUS, 32);
        shapeRenderer.end();

        batch.begin();

        int textX = divX + 10;

        font.getData().setScale(0.95f);
        font.setColor(LABEL_COLOR);
        font.draw(batch, "Time of Day", textX, panelY + PANEL_H - 10);

        float totalHours = time * 24f;
        int   hour       = (int) totalHours % 24;
        int   minute     = (int) ((totalHours - (int) totalHours) * 60f);
        String timeStr   = String.format("%02d:%02d", hour, minute);

        font.getData().setScale(1.4f);
        font.setColor(TIME_COLOR);
        font.draw(batch, timeStr, textX, panelY + PANEL_H / 2 + 5);

        String period = getPeriodLabel(time);
        font.getData().setScale(0.85f);
        font.setColor(0.60f, 0.60f, 0.78f, 1f);
        font.draw(batch, period, textX, panelY + 18);

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }

    public void resize(int width, int height) {
        this.screenWidth  = width;
        this.screenHeight = height;
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }


    private static String getPeriodLabel(float t) {
        if      (t < 0.25f) return "Night";
        else if (t < 0.30f) return "Dawn";
        else if (t < 0.35f) return "Morning";
        else if (t < 0.75f) return "Day";
        else if (t < 0.80f) return "Evening";
        else if (t < 0.85f) return "Dusk";
        else                 return "Night";
    }
}