package com.factory.game.Items;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.World.LiquidType;


public class FurnaceSelectionUI {

    private static final int SLOT_SIZE    = 52;
    private static final int SLOT_PAD     = 8;
    private static final int PANEL_W      = 460;
    private static final int TITLE_H      = 42;
    private static final int INFO_PANEL_W = 300;

    private boolean        visible      = false;
    private int            hoveredIndex = -1;
    private SmeltingRecipe chosenRecipe = null;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont    font;
    private final Inventory     inventory;

    private int panelX, panelY, panelH;
    private int infoPanelX, infoPanelY;
    private List<SmeltingRecipe> currentRecipes;

    public FurnaceSelectionUI(Inventory inventory) {
        this.inventory = inventory;
        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 16;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }

    public void open() {
        currentRecipes = CraftingManager.getAvailableSmeltRecipes(inventory);

        visible      = true;
        hoveredIndex = -1;
        chosenRecipe = null;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        int rows   = currentRecipes.size();
        panelH     = TITLE_H + rows * (SLOT_SIZE + SLOT_PAD) + SLOT_PAD + 24;
        panelX     = (sw - PANEL_W) / 2;
        panelY     = (sh - panelH) / 2;
        infoPanelX = panelX + PANEL_W + 12;
        infoPanelY = panelY;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    public void close() {
        visible      = false;
        hoveredIndex = -1;
    }

    public boolean isVisible() { return visible; }

    public SmeltingRecipe pollChosen() {
        SmeltingRecipe r = chosenRecipe;
        chosenRecipe     = null;
        return r;
    }

    public void handleInput() {
        if (!visible) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();
        hoveredIndex = -1;

        for (int i = 0; i < currentRecipes.size(); i++) {
            int[] pos = slotPos(i);
            if (mx >= pos[0] && mx <= pos[0] + SLOT_SIZE
                    && my >= pos[1] && my <= pos[1] + SLOT_SIZE) {
                hoveredIndex = i;
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    chosenRecipe = currentRecipes.get(i);
                    close();
                }
                break;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (!visible) return;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();

        drawPanel(panelX, panelY, PANEL_W, panelH);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < currentRecipes.size(); i++) {
            int[]   pos = slotPos(i);
            boolean hov = (i == hoveredIndex);
            shapeRenderer.setColor(
                    hov ? 1f    : 0.58f,
                    hov ? 0.72f : 0.35f,
                    hov ? 0.08f : 0.10f,
                    1f);
            shapeRenderer.rect(pos[0], pos[1], SLOT_SIZE, SLOT_SIZE);
        }
        shapeRenderer.end();

        if (hoveredIndex >= 0) {
            int infoH = TITLE_H + 48 + 16;
            infoPanelY = panelY + panelH - infoH;
            if (infoPanelX + INFO_PANEL_W > sw - 8)
                infoPanelX = panelX - INFO_PANEL_W - 12;
            drawPanel(infoPanelX, infoPanelY, INFO_PANEL_W, infoH);
        }

        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(1f, 0.72f, 0.30f, 1f);
        font.draw(batch, "What to Smelt?", panelX + 14, panelY + panelH - 12);

        for (int i = 0; i < currentRecipes.size(); i++) {
            SmeltingRecipe recipe = currentRecipes.get(i);
            Item           item   = recipe.getInputItem();
            int[]          pos    = slotPos(i);
            boolean        hov    = (i == hoveredIndex);

            TextureRegion tex = ItemTextureCache.getTexture(item);
            if (tex != null)
                batch.draw(tex, pos[0] + 4, pos[1] + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);

            int labelX = pos[0] + SLOT_SIZE + 10;
            int labelY = pos[1] + SLOT_SIZE - 10;

            font.getData().setScale(1.2f);
            font.setColor(hov ? new Color(1f, 0.90f, 0.30f, 1f) : Color.WHITE);
            font.draw(batch, item.getDisplayName(), labelX, labelY);

            font.getData().setScale(1.05f);
            font.setColor(0.68f, 0.50f, 0.32f, 1f);
            font.draw(batch, "x" + inventory.countItem(item) + " owned", labelX, labelY - 20);
        }

        if (hoveredIndex >= 0) {
            SmeltingRecipe recipe = currentRecipes.get(hoveredIndex);
            LiquidType     out    = recipe.getOutputLiquid();
            int            infoH  = TITLE_H + 48 + 16;
            infoPanelY = panelY + panelH - infoH;

            font.getData().setScale(1.3f);
            font.setColor(1f, 0.72f, 0.30f, 1f);
            font.draw(batch, "Output Liquid", infoPanelX + 10, infoPanelY + infoH - 12);

            batch.end();
            int swatchX = infoPanelX + 10;
            int swatchY = infoPanelY + infoH - TITLE_H - 14;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(out.color);
            shapeRenderer.rect(swatchX, swatchY - 18, 22, 22);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 1f, 1f, 0.45f);
            shapeRenderer.rect(swatchX, swatchY - 18, 22, 22);
            shapeRenderer.end();
            batch.begin();

            font.getData().setScale(1.05f);
            font.setColor(Color.WHITE);
            font.draw(batch, out.name, infoPanelX + 38, swatchY);

            font.getData().setScale(1.0f);
            font.setColor(0.68f, 0.58f, 0.40f, 1f);
            font.draw(batch, String.format("%.0f units on success", recipe.getOutputAmount()),
                    infoPanelX + 10, swatchY - 22);
        }

        font.getData().setScale(1.1f);
        font.setColor(0.50f, 0.36f, 0.22f, 0.9f);
        font.draw(batch, "ESC  to cancel", panelX + 14, panelY + 20);

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

    private int[] slotPos(int i) {
        int x = panelX + SLOT_PAD;
        int y = panelY + panelH - TITLE_H - SLOT_PAD
                - (i + 1) * (SLOT_SIZE + SLOT_PAD) + SLOT_PAD;
        return new int[]{ x, y };
    }

    private void drawPanel(int x, int y, int w, int h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.10f, 0.05f, 0.03f, 0.97f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.65f, 0.30f, 0.10f, 1f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.42f, 0.20f, 0.10f, 1f);
        shapeRenderer.line(x + 6, y + h - TITLE_H, x + w - 6, y + h - TITLE_H);
        shapeRenderer.end();
    }
}