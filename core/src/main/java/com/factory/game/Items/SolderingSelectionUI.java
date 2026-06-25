package com.factory.game.Items;

import java.util.List;
import java.util.Map;

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

public class SolderingSelectionUI {

    private static final int SLOT_SIZE    = 52;
    private static final int SLOT_PAD     = 8;
    private static final int PANEL_W      = 480;
    private static final int TITLE_H      = 42;
    private static final int INFO_PANEL_W = 310;

    private boolean         visible      = false;
    private int             hoveredIndex = -1;
    private SolderingRecipe chosenRecipe = null;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont    font;
    private final Inventory     inventory;

    private int panelX, panelY, panelH;
    private int infoPanelX, infoPanelY;
    private List<SolderingRecipe> currentRecipes;

    public SolderingSelectionUI(Inventory inventory) {
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
        currentRecipes = CraftingManager.getAvailableSolderingRecipes(inventory);

        visible      = true;
        hoveredIndex = -1;
        chosenRecipe = null;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        int rows = currentRecipes.size();
        panelH   = TITLE_H + rows * (SLOT_SIZE + SLOT_PAD) + SLOT_PAD + 24;
        panelX   = (sw - PANEL_W) / 2;
        panelY   = (sh - panelH) / 2;

        infoPanelX = panelX + PANEL_W + 12;
        infoPanelY = panelY;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    public void close() {
        visible      = false;
        hoveredIndex = -1;
    }

    public boolean isVisible() { return visible; }

    public SolderingRecipe pollChosen() {
        SolderingRecipe r = chosenRecipe;
        chosenRecipe      = null;
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
                    hov ? 1.00f : 0.22f,
                    hov ? 0.88f : 0.72f,
                    hov ? 1.00f : 0.90f,
                    1f);
            shapeRenderer.rect(pos[0], pos[1], SLOT_SIZE, SLOT_SIZE);
        }
        shapeRenderer.end();

        if (hoveredIndex >= 0) {
            SolderingRecipe recipe   = currentRecipes.get(hoveredIndex);
            int             inputCnt = recipe.getInputs().size();
            int             infoH   = TITLE_H + inputCnt * 28 + 16 + 24;
            infoPanelY = Math.max(panelY, panelY + panelH - infoH);

            if (infoPanelX + INFO_PANEL_W > sw - 8)
                infoPanelX = panelX - INFO_PANEL_W - 12;

            drawPanel(infoPanelX, infoPanelY, INFO_PANEL_W, infoH);
        }

        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(0.35f, 0.90f, 1.00f, 1f);
        font.draw(batch, "What to Solder?", panelX + 14, panelY + panelH - 12);

        for (int i = 0; i < currentRecipes.size(); i++) {
            SolderingRecipe recipe = currentRecipes.get(i);
            Item            out    = recipe.getOutputItem();
            int[]           pos    = slotPos(i);
            boolean         hov    = (i == hoveredIndex);

            TextureRegion tex = ItemTextureCache.getTexture(out);
            if (tex != null)
                batch.draw(tex, pos[0] + 4, pos[1] + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);

            int labelX = pos[0] + SLOT_SIZE + 10;
            int labelY = pos[1] + SLOT_SIZE - 8;

            font.getData().setScale(1.20f);
            font.setColor(hov ? new Color(0.55f, 0.95f, 1.00f, 1f) : Color.WHITE);
            font.draw(batch, recipe.getName(), labelX, labelY);

            font.getData().setScale(1.00f);
            font.setColor(0.45f, 0.65f, 0.78f, 1f);
            font.draw(batch, out.getDisplayName() + " x" + recipe.getOutputQuantity(),
                    labelX, labelY - 20);
        }

        if (hoveredIndex >= 0) {
            SolderingRecipe recipe   = currentRecipes.get(hoveredIndex);
            int             inputCnt = recipe.getInputs().size();
            int             infoH   = TITLE_H + inputCnt * 28 + 16 + 24;
            int             curInfoY = Math.max(panelY, panelY + panelH - infoH);

            font.getData().setScale(1.25f);
            font.setColor(0.35f, 0.90f, 1.00f, 1f);
            font.draw(batch, "Ingredients", infoPanelX + 10, curInfoY + infoH - 12);

            int rowY = curInfoY + infoH - TITLE_H - 6;

            for (Map.Entry<Item, Integer> entry : recipe.getInputs().entrySet()) {
                Item in     = entry.getKey();
                int  needed = entry.getValue();
                int  owned  = inventory.countItem(in);

                TextureRegion tex = ItemTextureCache.getTexture(in);
                if (tex != null)
                    batch.draw(tex, infoPanelX + 10, rowY - 22, 22, 22);

                boolean enough = owned >= needed;
                font.getData().setScale(1.00f);
                font.setColor(enough ? Color.WHITE : new Color(1f, 0.45f, 0.40f, 1f));
                font.draw(batch, in.getDisplayName(), infoPanelX + 38, rowY);

                font.getData().setScale(0.95f);
                font.setColor(enough
                        ? new Color(0.55f, 0.85f, 0.55f, 1f)
                        : new Color(1f, 0.45f, 0.40f, 1f));
                font.draw(batch,
                        owned + " / " + needed,
                        infoPanelX + INFO_PANEL_W - 60, rowY);

                rowY -= 28;
            }

            font.getData().setScale(0.95f);
            font.setColor(0.40f, 0.62f, 0.75f, 1f);
            font.draw(batch,
                    "Output: " + recipe.getOutputItem().getDisplayName()
                            + " x" + recipe.getOutputQuantity(),
                    infoPanelX + 10, curInfoY + 22);
        }

        font.getData().setScale(1.05f);
        font.setColor(0.35f, 0.50f, 0.62f, 0.90f);
        font.draw(batch, "ESC to cancel", panelX + 14, panelY + 18);

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
        shapeRenderer.setColor(0.05f, 0.08f, 0.12f, 0.97f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.18f, 0.68f, 0.88f, 1f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.12f, 0.45f, 0.60f, 1f);
        shapeRenderer.line(x + 6, y + h - TITLE_H, x + w - 6, y + h - TITLE_H);
        shapeRenderer.end();
    }
}