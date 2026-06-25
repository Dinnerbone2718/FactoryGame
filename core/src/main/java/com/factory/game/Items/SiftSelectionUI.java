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


public class SiftSelectionUI {

    private static final int SLOT_SIZE    = 52;
    private static final int SLOT_PAD     = 8;
    private static final int PANEL_W      = 460;
    private static final int TITLE_H      = 42;
    private static final int ODDS_PANEL_W = 320;

    private boolean        visible        = false;
    private int            hoveredIndex   = -1;
    private SiftingRecipe  chosenRecipe   = null;   

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont    font;
    private final Inventory     inventory;

    private int panelX, panelY, panelH;
    private int oddsPanelX, oddsPanelY;
    private List<SiftingRecipe> currentRecipes;

    public SiftSelectionUI(Inventory inventory) {
        this.inventory = inventory;

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 16;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }


    public void open() {
        currentRecipes = CraftingManager.getAvailableSiftRecipes(inventory);

        visible      = true;
        hoveredIndex = -1;
        chosenRecipe = null;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        int rows   = currentRecipes.size();
        panelH     = TITLE_H + rows * (SLOT_SIZE + SLOT_PAD) + SLOT_PAD + 24;
        panelX     = (sw - PANEL_W) / 2;
        panelY     = (sh - panelH) / 2;
        oddsPanelX = panelX + PANEL_W + 12;
        oddsPanelY = panelY;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    public void close() {
        visible      = false;
        hoveredIndex = -1;
    }

    public boolean isVisible() { return visible; }


    public SiftingRecipe pollChosen() {
        SiftingRecipe r = chosenRecipe;
        chosenRecipe    = null;
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
            int[] pos    = slotPos(i);
            boolean hov  = (i == hoveredIndex);
            shapeRenderer.setColor(hov ? 1f : 0.38f, hov ? 0.88f : 0.38f, hov ? 0.2f : 0.42f, 1f);
            shapeRenderer.rect(pos[0], pos[1], SLOT_SIZE, SLOT_SIZE);
        }
        shapeRenderer.end();

        if (hoveredIndex >= 0) {
            SiftingRecipe recipe = currentRecipes.get(hoveredIndex);
            int oddsH = TITLE_H + recipe.getOutputs().size() * 28 + 16;
            oddsPanelY = panelY + panelH - oddsH;   

            if (oddsPanelX + ODDS_PANEL_W > sw - 8) {
                oddsPanelX = panelX - ODDS_PANEL_W - 12;
            }

            drawPanel(oddsPanelX, oddsPanelY, ODDS_PANEL_W, oddsH);
        }

        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(0.88f, 0.88f, 1f, 1f);
        font.draw(batch, "What to Sift?", panelX + 14, panelY + panelH - 12);

        for (int i = 0; i < currentRecipes.size(); i++) {
            SiftingRecipe recipe = currentRecipes.get(i);
            Item          item   = recipe.getInputItem();
            int[]         pos    = slotPos(i);
            boolean       hov    = (i == hoveredIndex);

            TextureRegion tex = ItemTextureCache.getTexture(item);
            if (tex != null) batch.draw(tex, pos[0] + 4, pos[1] + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);

            int labelX = pos[0] + SLOT_SIZE + 10;
            int labelY = pos[1] + SLOT_SIZE - 10;

            font.getData().setScale(1.2f);
            font.setColor(hov ? new Color(1f, 0.95f, 0.4f, 1f) : Color.WHITE);
            font.draw(batch, item.getDisplayName(), labelX, labelY);

            font.getData().setScale(1.05f);
            font.setColor(0.7f, 0.7f, 0.85f, 1f);
            font.draw(batch, "x" + inventory.countItem(item) + " owned", labelX, labelY - 20);
        }

        if (hoveredIndex >= 0) {
            SiftingRecipe recipe = currentRecipes.get(hoveredIndex);
            int oddsH    = TITLE_H + recipe.getOutputs().size() * 28 + 16;
            oddsPanelY   = panelY + panelH - oddsH;

            font.getData().setScale(1.3f);
            font.setColor(0.85f, 0.85f, 1f, 1f);
            font.draw(batch, "Possible Results", oddsPanelX + 10, oddsPanelY + oddsH - 12);

            for (int i = 0; i < recipe.getOutputs().size(); i++) {
                Item  out    = recipe.getOutputs().get(i);
                float chance = recipe.getChance(i);

                int iconX = oddsPanelX + 10;
                int rowY  = oddsPanelY + oddsH - TITLE_H - 10 - i * 28;

                TextureRegion tex = ItemTextureCache.getTexture(out);
                if (tex != null) batch.draw(tex, iconX, rowY - 20, 22, 22);

                font.getData().setScale(1.05f);
                font.setColor(Color.WHITE);
                font.draw(batch, out.getDisplayName(), iconX + 28, rowY);

                Color chanceColor;
                if      (chance > 30f) chanceColor = new Color(0.3f, 1f,  0.35f, 1f);
                else if (chance > 15f) chanceColor = new Color(1f,  0.9f, 0.2f,  1f);
                else                  chanceColor = new Color(1f,  0.4f, 0.4f,  1f);

                font.setColor(chanceColor);
                font.draw(batch, String.format("%.1f%%", chance),
                        oddsPanelX + ODDS_PANEL_W - 58, rowY);
            }
        }

        font.getData().setScale(1.1f);
        font.setColor(0.5f, 0.5f, 0.65f, 0.9f);
        font.draw(batch, "ESC / T  to cancel",
                panelX + 14, panelY + 20);

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
        int y = panelY + panelH - TITLE_H - SLOT_PAD - (i + 1) * (SLOT_SIZE + SLOT_PAD) + SLOT_PAD;
        return new int[]{ x, y };
    }

    private void drawPanel(int x, int y, int w, int h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.07f, 0.07f, 0.13f, 0.97f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.38f, 0.38f, 0.62f, 1f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 1f);
        shapeRenderer.line(x + 6, y + h - TITLE_H, x + w - 6, y + h - TITLE_H);
        shapeRenderer.end();
    }
}