package com.factory.game.World;

import java.util.EnumSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class FilterPipeUI {

    private boolean      visible     = false;
    private PlacedObject filterPipe  = null;
    private boolean      dirty       = false;
    private boolean      justOpened  = false;

    private final BitmapFont    font;
    private final TextureRegion whitePixel;

    private static final float PANEL_W  = 540f;
    private static final float ROW_H    = 34f;
    private static final float PAD      = 16f;
    private static final float CHECKBOX = 20f;

    private int screenW, screenH;

    public FilterPipeUI() {
        font       = new BitmapFont();
        whitePixel = ObjectSpriteCache.whitePixel;
        screenW    = Gdx.graphics.getWidth();
        screenH    = Gdx.graphics.getHeight();
    }


    public void open(PlacedObject pipe) {
        this.filterPipe = pipe;
        this.visible    = true;
        this.dirty      = false;
        this.justOpened = true;
    }

    public void close() {
        visible = false;
    }

    public boolean isVisible()   { return visible;    }
    public boolean isDirty()     { return dirty;      }
    public void    clearDirty()  { dirty = false;     }

    public PlacedObject getFilterPipe() { return filterPipe; }


    public void resize(int w, int h) {
        screenW = w;
        screenH = h;
    }


    public void handleInput() {
        if (!visible || filterPipe == null) return;

        if (justOpened) {
            justOpened = false;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)
         || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;

        LiquidType[] types  = LiquidType.values();
        float        panelH = panelHeight(types.length);
        float        panelX = (screenW - PANEL_W) / 2f;
        float        panelY = (screenH - panelH)  / 2f;

        float mx = Gdx.input.getX();
        float my = screenH - Gdx.input.getY();

        Set<LiquidType> current = filterPipe.getAllowedLiquidTypes();
        EnumSet<LiquidType> working = (current == null || current.isEmpty())
                ? EnumSet.noneOf(LiquidType.class)
                : EnumSet.copyOf(current);

        for (int i = 0; i < types.length; i++) {
            float rowY = rowTopY(panelY, panelH, i);
            float boxX = panelX + PAD;
            float boxY = rowY + (ROW_H - CHECKBOX) * 0.5f;

            if (mx >= boxX && mx <= boxX + CHECKBOX
             && my >= boxY && my <= boxY + CHECKBOX) {
                if (working.contains(types[i])) working.remove(types[i]);
                else                            working.add(types[i]);
                filterPipe.setAllowedLiquidTypes(working.isEmpty() ? null : working);
                dirty = true;
                return;
            }
        }
    }


    public void render(SpriteBatch batch) {
        if (!visible || filterPipe == null) return;

        LiquidType[] types  = LiquidType.values();
        float        panelH = panelHeight(types.length);
        float        panelX = (screenW - PANEL_W) / 2f;
        float        panelY = (screenH - panelH)  / 2f;

        drawRect(batch, panelX,     panelY,     PANEL_W,     panelH,     0.10f, 0.10f, 0.16f, 0.96f);
        drawRect(batch, panelX + 1, panelY + 1, PANEL_W - 2, panelH - 2, 0.30f, 0.30f, 0.45f, 0.30f);

        float titleY = panelY + panelH - PAD;
        font.setColor(1f, 0.80f, 0.25f, 1f);
        font.draw(batch, "Filter Pipe Configuration", panelX + PAD, titleY);

        font.setColor(0.60f, 0.60f, 0.65f, 1f);
        font.draw(batch, "Toggle which liquids may pass through.  (none = allow all)",
                panelX + PAD, titleY - 16f);
        font.draw(batch, "Press  E  to close.", panelX + PAD, titleY - 30f);

        Set<LiquidType> current = filterPipe.getAllowedLiquidTypes();

        for (int i = 0; i < types.length; i++) {
            LiquidType t    = types[i];
            boolean    on   = (current != null && current.contains(t));
            float      rowY = rowTopY(panelY, panelH, i);

            float boxX = panelX + PAD;
            float boxY = rowY + (ROW_H - CHECKBOX) * 0.5f;

            drawRect(batch, boxX,     boxY,     CHECKBOX,     CHECKBOX,     0.30f, 0.30f, 0.42f, 1f);
            if (on) {
                drawRect(batch, boxX + 3, boxY + 3, CHECKBOX - 6, CHECKBOX - 6,
                        t.color.r * 0.9f, t.color.g * 0.9f, t.color.b * 0.9f, 1f);
            }

            if (on) {
                font.setColor(t.color.r, t.color.g, t.color.b, 1f);
            } else {
                font.setColor(0.55f, 0.55f, 0.60f, 1f);
            }
            font.draw(batch, t.name, boxX + CHECKBOX + 10f, rowY + ROW_H * 0.68f);
        }

        if (current == null || current.isEmpty()) {
            float hintY = panelY + PAD * 0.8f;
            font.setColor(0.40f, 0.85f, 0.55f, 1f);
            font.draw(batch, "No filter active, all liquids pass through",
                    panelX + PAD, hintY + 14f);
        }

        font.setColor(Color.WHITE);
    }


    public void dispose() {
        font.dispose();
    }

    private float panelHeight(int typeCount) {
        return PAD + 46f + PAD * 0.5f + ROW_H * typeCount + PAD * 2f;
    }

    private float rowTopY(float panelY, float panelH, int i) {
        float headerH = PAD + 46f + PAD * 0.5f;
        return panelY + panelH - headerH - ROW_H * (i + 1);
    }

    private void drawRect(SpriteBatch batch, float x, float y, float w, float h,
                          float r, float g, float b, float a) {
        batch.setColor(r, g, b, a);
        batch.draw(whitePixel, x, y, w, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}