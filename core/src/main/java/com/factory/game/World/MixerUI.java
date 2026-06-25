package com.factory.game.World;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MixerUI {

    private static final int RECIPE_PANEL_W = 620;
    private static final int ITEM_H         = 100;
    private static final int ITEM_PAD       = 18;
    private static final int TITLE_H        = 72;
    private static final int FOOTER_H       = 56;

    private static final int TANK_PANEL_W   = 520;
    private static final int TANK_GAP       = 36;
    private static final int BAR_H          = 34;
    private static final int BAR_LABEL_H    = 30;
    private static final int ROW_H          = BAR_LABEL_H + BAR_H + 28;
    private static final int TANK_TITLE_H   = 72;
    private static final int TANK_FOOTER_H  = 24;
    private static final int TANK_SIDE_PAD  = 28;

    private boolean      visible = false;
    private boolean      dirty   = false;
    private PlacedObject mixer   = null;
    private int          hovered = -1;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont    font;

    private int recipeX, recipeY, recipeH;
    private int tankX,   tankY,   tankH;

    public MixerUI() {
        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 22;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }

    public void open(PlacedObject mixer) {
        this.mixer = mixer;
        visible    = true;
        hovered    = -1;
        relayout();
    }

    public void close() {
        visible = false;
        mixer   = null;
        hovered = -1;
    }

    public boolean      isVisible() { return visible; }
    public boolean      isDirty()   { return dirty;   }
    public void         clearDirty(){ dirty = false;  }
    public PlacedObject getMixer()  { return mixer;   }

    public void handleInput() {
        if (!visible || mixer == null) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();
        hovered = -1;

        List<MixingRecipe> recipes = MixingRecipe.ALL;
        for (int i = 0; i < recipes.size(); i++) {
            int iy = itemY(i);
            if (mx >= recipeX + ITEM_PAD
                    && mx <= recipeX + RECIPE_PANEL_W - ITEM_PAD
                    && my >= iy
                    && my <= iy + ITEM_H) {
                hovered = i;
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    MixingRecipe picked = recipes.get(i);
                    mixer.setSelectedMixingRecipe(
                            mixer.getSelectedMixingRecipe() == picked ? null : picked);
                    dirty = true;
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (!visible || mixer == null) return;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.50f);
        shapes.rect(0, 0, sw, sh);
        shapes.end();

        drawPanel(recipeX, recipeY, RECIPE_PANEL_W, recipeH);

        MixingRecipe current = mixer.getSelectedMixingRecipe();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < MixingRecipe.ALL.size(); i++) {
            boolean isActive  = (MixingRecipe.ALL.get(i) == current);
            boolean isHovered = (i == hovered);
            int iy = itemY(i);
            if (isActive) {
                shapes.setColor(0.12f, 0.42f, 0.28f, 0.40f);
                shapes.rect(recipeX + ITEM_PAD, iy, RECIPE_PANEL_W - ITEM_PAD * 2, ITEM_H);
            } else if (isHovered) {
                shapes.setColor(0.22f, 0.22f, 0.10f, 0.30f);
                shapes.rect(recipeX + ITEM_PAD, iy, RECIPE_PANEL_W - ITEM_PAD * 2, ITEM_H);
            }
        }
        shapes.end();

        if (current != null) {
            int inputCount = current.getInputs().size();
            tankH = TANK_TITLE_H + (inputCount + 1) * ROW_H + TANK_FOOTER_H + 32;
            tankY = recipeY + recipeH - tankH;
            if (tankY < 8) tankY = 8;

            drawPanel(tankX, tankY, TANK_PANEL_W, tankH);

            LiquidTank[] inputTanks = mixer.getMixerInputTanks();
            LiquidTank   outputTank = mixer.getMixerOutputTank();

            shapes.begin(ShapeRenderer.ShapeType.Filled);

            for (int i = 0; i < inputCount; i++) {
                MixingRecipe.InputSpec spec = current.getInputs().get(i);
                int bx = tankX + TANK_SIDE_PAD;
                int bw = TANK_PANEL_W - TANK_SIDE_PAD * 2;
                int by = tankY + tankH - TANK_TITLE_H - (i + 1) * ROW_H + 4;

                shapes.setColor(0.08f, 0.08f, 0.08f, 1f);
                shapes.rect(bx, by, bw, BAR_H);

                if (inputTanks != null && i < inputTanks.length) {
                    float ratio = inputTanks[i].getFillRatio();
                    if (ratio > 0f) {
                        Color c = spec.type.color;
                        shapes.setColor(c.r * 0.9f, c.g * 0.9f, c.b * 0.9f, 0.95f);
                        shapes.rect(bx, by, bw * ratio, BAR_H);
                    }
                }
            }

            int divY = tankY + tankH - TANK_TITLE_H - inputCount * ROW_H - 2;
            shapes.setColor(0.20f, 0.35f, 0.25f, 0.60f);
            shapes.rect(tankX + TANK_SIDE_PAD, divY, TANK_PANEL_W - TANK_SIDE_PAD * 2, 2);

            int obx = tankX + TANK_SIDE_PAD;
            int obw = TANK_PANEL_W - TANK_SIDE_PAD * 2;
            int oby = tankY + TANK_FOOTER_H + 12;

            shapes.setColor(0.08f, 0.08f, 0.08f, 1f);
            shapes.rect(obx, oby, obw, BAR_H);

            if (outputTank != null) {
                float ratio = outputTank.getFillRatio();
                if (ratio > 0f) {
                    Color c = current.getOutputType().color;
                    shapes.setColor(c.r * 0.9f, c.g * 0.9f, c.b * 0.9f, 0.95f);
                    shapes.rect(obx, oby, obw * ratio, BAR_H);
                }
            }

            shapes.end();

            batch.begin();

            font.getData().setScale(1.55f);
            font.setColor(0.50f, 0.85f, 0.65f, 1f);
            font.draw(batch, "Tanks", tankX + TANK_SIDE_PAD, tankY + tankH - 18);

            for (int i = 0; i < inputCount; i++) {
                MixingRecipe.InputSpec spec = current.getInputs().get(i);
                int bx = tankX + TANK_SIDE_PAD;
                int by = tankY + tankH - TANK_TITLE_H - (i + 1) * ROW_H + 4;

                float amt = (inputTanks != null && i < inputTanks.length)
                        ? inputTanks[i].getAmount() : 0f;
                float cap = (inputTanks != null && i < inputTanks.length)
                        ? inputTanks[i].getCapacity() : spec.tankCapacity;

                Color c = spec.type.color;
                font.getData().setScale(1.10f);
                font.setColor(c.r, c.g, c.b, 1f);
                font.draw(batch,
                        spec.type.name + "  " + (int) amt + " / " + (int) cap,
                        bx, by + BAR_H + BAR_LABEL_H);
            }

            float oAmt = (outputTank != null) ? outputTank.getAmount()   : 0f;
            float oCap = (outputTank != null) ? outputTank.getCapacity() : current.getOutputCapacity();
            Color oc   = current.getOutputType().color;
            int   obx2 = tankX + TANK_SIDE_PAD;
            int   oby2 = tankY + TANK_FOOTER_H + 12;

            font.getData().setScale(1.10f);
            font.setColor(oc.r, oc.g, oc.b, 1f);
            font.draw(batch,
                    "Output: " + current.getOutputType().name + "  " + (int) oAmt + " / " + (int) oCap,
                    obx2, oby2 + BAR_H + BAR_LABEL_H);

            batch.end();
        }

        batch.begin();

        font.getData().setScale(1.80f);
        font.setColor(0.50f, 0.88f, 0.65f, 1f);
        font.draw(batch, "Mixer", recipeX + 24, recipeY + recipeH - 18);

        for (int i = 0; i < MixingRecipe.ALL.size(); i++) {
            MixingRecipe r      = MixingRecipe.ALL.get(i);
            boolean      active = (r == current);
            boolean      hov    = (i == hovered);
            int          iy     = itemY(i);

            font.getData().setScale(1.40f);
            font.setColor(
                    active ? new Color(0.35f, 1.00f, 0.58f, 1f) :
                    hov    ? new Color(1.00f, 0.95f, 0.42f, 1f) :
                             Color.WHITE);
            font.draw(batch, r.getName(), recipeX + ITEM_PAD + 20, iy + ITEM_H - 16);

            StringBuilder sb = new StringBuilder();
            for (MixingRecipe.InputSpec s : r.getInputs()) {
                if (sb.length() > 0) sb.append(" + ");
                sb.append(s.type.name);
            }
            sb.append(" -> ").append(r.getOutputType().name);

            font.getData().setScale(1.05f);
            font.setColor(0.58f, 0.50f, 0.36f, 1f);
            font.draw(batch, sb.toString(), recipeX + ITEM_PAD + 20, iy + ITEM_H - 52);
        }

        if (current == null) {
            font.getData().setScale(1.20f);
            font.setColor(0.50f, 0.50f, 0.50f, 0.80f);
            font.draw(batch, "Click a recipe to start mixing.",
                    recipeX + 24, recipeY + FOOTER_H + 8);
        }

        font.getData().setScale(1.15f);
        font.setColor(0.45f, 0.36f, 0.24f, 0.90f);
        font.draw(batch, "ESC to close", recipeX + 24, recipeY + 24);

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }

    public void resize(int w, int h) {
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        if (visible) relayout();
    }

    public void dispose() {
        shapes.dispose();
        font.dispose();
    }

    private void relayout() {
        int sw   = Gdx.graphics.getWidth();
        int sh   = Gdx.graphics.getHeight();
        int rows = MixingRecipe.ALL.size();

        recipeH = TITLE_H + rows * (ITEM_H + ITEM_PAD) + ITEM_PAD + FOOTER_H;

        int totalW = RECIPE_PANEL_W + TANK_GAP + TANK_PANEL_W;
        recipeX = (sw - totalW) / 2;
        recipeY = (sh - recipeH) / 2;
        tankX   = recipeX + RECIPE_PANEL_W + TANK_GAP;
        tankY   = recipeY;
        tankH   = recipeH;

        shapes.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    private int itemY(int i) {
        return recipeY + recipeH - TITLE_H - ITEM_PAD
                - (i + 1) * (ITEM_H + ITEM_PAD) + ITEM_PAD;
    }

    private void drawPanel(int x, int y, int w, int h) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.07f, 0.11f, 0.09f, 0.97f);
        shapes.rect(x, y, w, h);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.26f, 0.52f, 0.36f, 1f);
        shapes.rect(x, y, w, h);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.16f, 0.33f, 0.23f, 1f);
        shapes.line(x + 12, y + h - TITLE_H, x + w - 12, y + h - TITLE_H);
        shapes.end();
    }
}