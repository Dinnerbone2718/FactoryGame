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


public class DistilleryUI {


    private static final int RECIPE_PANEL_W = 640;
    private static final int ITEM_H         = 100;
    private static final int ITEM_PAD       = 18;
    private static final int TITLE_H        = 72;
    private static final int FOOTER_H       = 56;

    private static final int TANK_PANEL_W  = 480;
    private static final int TANK_GAP      = 24;
    private static final int BAR_H         = 16;
    private static final int BAR_LABEL_H   = 18;
    private static final int ROW_H         = BAR_LABEL_H + BAR_H + 20;
    private static final int SECTION_HDR_H = 12;
    private static final int DIVIDER_H     = 1;
    private static final int DIVIDER_PAD   = 2;
    private static final int TANK_TITLE_H  = 100;
    private static final int TANK_FOOTER_H = 80;
    private static final int TANK_SIDE_PAD = 22;


    private boolean      visible  = false;
    private boolean      dirty    = false;
    private PlacedObject distillery = null;
    private int          hovered  = -1;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont    font;

    private int recipeX, recipeY, recipeH;
    private int tankX,   tankY,   tankH;



    public DistilleryUI() {
        FreeTypeFontGenerator     gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 22;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }


    public void open(PlacedObject distillery) {
        this.distillery = distillery;
        visible         = true;
        hovered         = -1;
        relayout();
    }

    public void close() {
        visible    = false;
        distillery = null;
        hovered    = -1;
    }

    public boolean      isVisible()    { return visible;    }
    public boolean      isDirty()      { return dirty;      }
    public void         clearDirty()   { dirty = false;     }
    public PlacedObject getDistillery(){ return distillery; }


    public void handleInput() {
        if (!visible || distillery == null) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();
        hovered = -1;

        List<DistilleryRecipe> recipes = DistilleryRecipe.ALL;
        for (int i = 0; i < recipes.size(); i++) {
            int iy = itemY(i);
            if (mx >= recipeX + ITEM_PAD
                    && mx <= recipeX + RECIPE_PANEL_W - ITEM_PAD
                    && my >= iy
                    && my <= iy + ITEM_H) {
                hovered = i;
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    DistilleryRecipe picked = recipes.get(i);
                    distillery.setSelectedDistilleryRecipe(
                            distillery.getSelectedDistilleryRecipe() == picked ? null : picked);
                    dirty = true;
                }
            }
        }
    }



    public void render(SpriteBatch batch) {
        if (!visible || distillery == null) return;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.52f);
        shapes.rect(0, 0, sw, sh);
        shapes.end();

        drawPanel(recipeX, recipeY, RECIPE_PANEL_W, recipeH);

        DistilleryRecipe current = distillery.getSelectedDistilleryRecipe();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < DistilleryRecipe.ALL.size(); i++) {
            boolean isActive  = (DistilleryRecipe.ALL.get(i) == current);
            boolean isHovered = (i == hovered);
            int iy = itemY(i);
            if (isActive) {
                shapes.setColor(0.12f, 0.38f, 0.30f, 0.42f);
                shapes.rect(recipeX + ITEM_PAD, iy, RECIPE_PANEL_W - ITEM_PAD * 2, ITEM_H);
            } else if (isHovered) {
                shapes.setColor(0.22f, 0.22f, 0.10f, 0.28f);
                shapes.rect(recipeX + ITEM_PAD, iy, RECIPE_PANEL_W - ITEM_PAD * 2, ITEM_H);
            }
        }
        shapes.end();

        if (current != null) {
            int inputCount  = current.getInputs().size();
            int outputCount = current.getOutputs().size();

            int bodyH = SECTION_HDR_H + inputCount  * ROW_H
                      + DIVIDER_PAD + DIVIDER_H + DIVIDER_PAD
                      + SECTION_HDR_H + outputCount * ROW_H;
            tankH = TANK_TITLE_H + bodyH + TANK_FOOTER_H + 8;
            tankY = recipeY + (recipeH - tankH) / 2;
            if (tankY < 8) tankY = 8;

            drawPanel(tankX, tankY, TANK_PANEL_W, tankH);

            LiquidTank[] inputTanks  = distillery.getDistilleryInputTanks();
            LiquidTank[] outputTanks = distillery.getDistilleryOutputTanks();

            int bw = TANK_PANEL_W - TANK_SIDE_PAD * 2;

            shapes.begin(ShapeRenderer.ShapeType.Filled);

            for (int i = 0; i < inputCount; i++) {
                DistilleryRecipe.InputSpec spec = current.getInputs().get(i);
                int bx = tankX + TANK_SIDE_PAD;
                int by = inputBarY(i, inputCount, outputCount);

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

            int divY = dividerY(inputCount, outputCount);
            shapes.setColor(0.22f, 0.40f, 0.30f, 0.65f);
            shapes.rect(tankX + TANK_SIDE_PAD, divY, bw, DIVIDER_H);

            for (int i = 0; i < outputCount; i++) {
                DistilleryRecipe.OutputSpec spec = current.getOutputs().get(i);
                int bx = tankX + TANK_SIDE_PAD;
                int by = outputBarY(i, inputCount, outputCount);

                shapes.setColor(0.08f, 0.08f, 0.08f, 1f);
                shapes.rect(bx, by, bw, BAR_H);

                if (outputTanks != null && i < outputTanks.length) {
                    float ratio = outputTanks[i].getFillRatio();
                    if (ratio > 0f) {
                        Color c = spec.type.color;
                        shapes.setColor(c.r * 0.9f, c.g * 0.9f, c.b * 0.9f, 0.95f);
                        shapes.rect(bx, by, bw * ratio, BAR_H);
                    }
                }
            }

            shapes.end();

            batch.begin();

            font.getData().setScale(1.20f);
            font.setColor(0.48f, 0.85f, 0.65f, 1f);
            font.draw(batch, "Distillery Tanks", tankX + TANK_SIDE_PAD, tankY + tankH - 12);

            font.getData().setScale(0.85f);
            font.setColor(0.55f, 0.55f, 0.55f, 0.85f);
            font.draw(batch, "INPUTS", tankX + TANK_SIDE_PAD, tankY + tankH - TANK_TITLE_H + 22);

            for (int i = 0; i < inputCount; i++) {
                DistilleryRecipe.InputSpec spec = current.getInputs().get(i);
                int bx = tankX + TANK_SIDE_PAD;
                int by = inputBarY(i, inputCount, outputCount);

                float amt = (inputTanks != null && i < inputTanks.length)
                        ? inputTanks[i].getAmount() : 0f;
                float cap = (inputTanks != null && i < inputTanks.length)
                        ? inputTanks[i].getCapacity() : spec.tankCapacity;

                Color c = spec.type.color;
                font.getData().setScale(0.88f);
                font.setColor(c.r, c.g, c.b, 1f);
                font.draw(batch,
                        spec.type.name + "  " + (int) amt + " / " + (int) cap,
                        bx, by + BAR_H + BAR_LABEL_H - 2);
            }

            font.getData().setScale(0.85f);
            font.setColor(0.55f, 0.55f, 0.55f, 0.85f);
            font.draw(batch, "OUTPUTS", tankX + TANK_SIDE_PAD,
                    outputSectionHeaderY(inputCount, outputCount) + SECTION_HDR_H - 4);

            for (int i = 0; i < outputCount; i++) {
                DistilleryRecipe.OutputSpec spec = current.getOutputs().get(i);
                int bx = tankX + TANK_SIDE_PAD;
                int by = outputBarY(i, inputCount, outputCount);

                float amt = (outputTanks != null && i < outputTanks.length)
                        ? outputTanks[i].getAmount() : 0f;
                float cap = (outputTanks != null && i < outputTanks.length)
                        ? outputTanks[i].getCapacity() : spec.tankCapacity;

                Color c = spec.type.color;
                font.getData().setScale(0.88f);
                font.setColor(c.r, c.g, c.b, 1f);
                font.draw(batch,
                        spec.type.name + "  " + (int) amt + " / " + (int) cap,
                        bx, by + BAR_H + BAR_LABEL_H - 2);
            }

            batch.end();
        }

        batch.begin();

        font.getData().setScale(1.80f);
        font.setColor(0.48f, 0.88f, 0.65f, 1f);
        font.draw(batch, "Distillery", recipeX + 24, recipeY + recipeH - 18);

        for (int i = 0; i < DistilleryRecipe.ALL.size(); i++) {
            DistilleryRecipe r      = DistilleryRecipe.ALL.get(i);
            boolean          active = (r == current);
            boolean          hov    = (i == hovered);
            int              iy     = itemY(i);

            font.getData().setScale(1.40f);
            font.setColor(
                    active ? new Color(0.35f, 1.00f, 0.62f, 1f) :
                    hov    ? new Color(1.00f, 0.95f, 0.42f, 1f) :
                             Color.WHITE);
            font.draw(batch, r.getName(), recipeX + ITEM_PAD + 20, iy + ITEM_H - 16);


        }

        if (current == null) {
            font.getData().setScale(1.18f);
            font.setColor(0.48f, 0.48f, 0.48f, 0.82f);
            font.draw(batch, "Click a recipe to start distilling.",
                    recipeX + 24, recipeY + FOOTER_H + 8);
        }

        font.getData().setScale(1.15f);
        font.setColor(0.44f, 0.35f, 0.24f, 0.90f);
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
        int rows = DistilleryRecipe.ALL.size();

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

    private int inputSectionHeaderY(int inputCount, int outputCount) {

        return tankY + tankH - TANK_TITLE_H - SECTION_HDR_H;
    }

    private int inputBarY(int i, int inputCount, int outputCount) {
        int topOfInputRows = inputSectionHeaderY(inputCount, outputCount) - ROW_H;
        return topOfInputRows - i * ROW_H + (ROW_H - BAR_H - BAR_LABEL_H);
    }

    private int dividerY(int inputCount, int outputCount) {
        int bottomOfOutputRows = tankY + TANK_FOOTER_H;
        int topOfOutputRows = bottomOfOutputRows + outputCount * ROW_H;
        return topOfOutputRows + DIVIDER_PAD + SECTION_HDR_H + DIVIDER_PAD;
    }

    private int outputSectionHeaderY(int inputCount, int outputCount) {
        int bottomOfOutputRows = tankY + TANK_FOOTER_H;
        int topOfOutputRows = bottomOfOutputRows + outputCount * ROW_H;
        return topOfOutputRows + DIVIDER_PAD;
    }

    private int outputBarY(int i, int inputCount, int outputCount) {
        int bottomOfOutputRows = tankY + TANK_FOOTER_H;
        return bottomOfOutputRows + (outputCount - 1 - i) * ROW_H;
    }

    private void drawPanel(int x, int y, int w, int h) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.10f, 0.09f, 0.97f);
        shapes.rect(x, y, w, h);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.24f, 0.50f, 0.38f, 1f);
        shapes.rect(x, y, w, h);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.14f, 0.30f, 0.22f, 1f);
        shapes.line(x + 12, y + h - TANK_TITLE_H, x + w - 12, y + h - TANK_TITLE_H);
        shapes.end();
    }
}