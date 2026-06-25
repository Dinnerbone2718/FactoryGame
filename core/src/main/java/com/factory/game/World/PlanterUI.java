package com.factory.game.World;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;

public class PlanterUI {

    private static final int PANEL_W       = 360;
    private static final int PAD           = 18;
    private static final int LINE_GAP      = 8;
    private static final int PREVIEW_W     = 192;
    private static final int PREVIEW_H     = 96;
    private static final int BAR_H         = 12;
    private static final int SEED_SIZE     = 50;
    private static final int SEED_GAP      = 8;
    private static final int SEEDS_PER_ROW = 5;

    private static final int INV_SLOT_SIZE = 44;
    private static final int INV_SLOT_GAP  = 6;
    private static final int INV_COLS      = 5;

    private int panelH = 600;

    private boolean      visible    = false;
    private boolean      justOpened = false;
    private PlacedObject planter    = null;
    private Inventory    playerInv  = null;

    private final PlanterManager manager;

    private Texture         growSheet;
    private TextureRegion[] growFrames;
    private String          loadedSheetPath = null;

    private final ShapeRenderer shape;
    private final BitmapFont    titleFont;
    private final BitmapFont    labelFont;
    private final BitmapFont    tinyFont;
    private final GlyphLayout   layout = new GlyphLayout();

    private int panelX, panelY, sw, sh;

    private int removeBtnX, removeBtnY, removeBtnW, removeBtnH;
    private int[][] seedBtnRects = new int[0][0];
    private List<Item> availableSeeds;

    private final int[][] invSlotRects = new int[PlanterManager.PLANTER_INV_SLOTS][4];

    public PlanterUI(PlanterManager manager) {
        this.manager = manager;
        shape = new ShapeRenderer();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

        p.size = 20; p.color = Color.WHITE;
        titleFont = gen.generateFont(p);

        p.size = 13; p.color = Color.WHITE;
        labelFont = gen.generateFont(p);

        p.size = 10; p.color = Color.WHITE;
        tinyFont = gen.generateFont(p);

        gen.dispose();
    }

    private void loadGrowSheet(String path) {
        if (growSheet != null) { growSheet.dispose(); growSheet = null; }
        growFrames = null;
        if (path == null) return;
        try {
            growSheet  = new Texture(Gdx.files.internal(path));
            growFrames = new TextureRegion[9];
            int fw = growSheet.getWidth()  / 3;
            int fh = growSheet.getHeight() / 3;
            for (int row = 0; row < 3; row++)
                for (int col = 0; col < 3; col++)
                    growFrames[row * 3 + col] =
                            new TextureRegion(growSheet, col * fw, row * fh, fw, fh);
        } catch (Exception e) {
            Gdx.app.error("PlanterUI", "Could not load grow sheet '" + path + "': " + e.getMessage());
            growSheet  = null;
            growFrames = null;
        }
    }

    private void refreshGrowSheet() {
        Item planted = (planter != null) ? manager.getPlantedSeed(planter) : null;
        String path = null;
        if (planted != null) {
            com.factory.game.Items.PlanterRecipe recipe =
                    com.factory.game.Items.CraftingManager.getPlanterRecipeFor(planted);
            if (recipe != null) path = recipe.getGrowSheetPath();
        }
        if (!java.util.Objects.equals(path, loadedSheetPath)) {
            loadedSheetPath = path;
            loadGrowSheet(path);
        }
    }

    public void open(PlacedObject planter, Inventory playerInventory) {
        this.planter    = planter;
        this.playerInv  = playerInventory;
        this.visible    = true;
        this.justOpened = true;
        layout();
        refreshSeeds();
        refreshGrowSheet();
    }

    public void close() { visible = false; planter = null; }
    public boolean isVisible() { return visible; }

    public void resize(int w, int h) { sw = w; sh = h; layout(); }

    private void layout() {
        sw = Gdx.graphics.getWidth();
        sh = Gdx.graphics.getHeight();

        int h = PAD + 14;
        int invRows = (int) Math.ceil((double) PlanterManager.PLANTER_INV_SLOTS / INV_COLS);
        h += (INV_SLOT_SIZE + INV_SLOT_GAP) * invRows + LINE_GAP + 14 + LINE_GAP + 8 + 1 + LINE_GAP;
        int seedCount = (availableSeeds != null) ? availableSeeds.size() : 0;
        int seedRows  = Math.max(1, (int) Math.ceil((double) seedCount / SEEDS_PER_ROW));
        h += (SEED_SIZE + SEED_GAP) * seedRows + LINE_GAP + 10 + 14 + LINE_GAP;
        h += LINE_GAP + 22;
        h += BAR_H + LINE_GAP + 18;
        h += BAR_H + LINE_GAP + 18;
        h += BAR_H + LINE_GAP + 18;
        h += PREVIEW_H + LINE_GAP + 6;
        h += 20 + LINE_GAP + 6 + LINE_GAP + 6 + PAD;

        panelH = h;
        panelX = (sw - PANEL_W) / 2;
        panelY = (sh - panelH) / 2;
        shape.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    private void refreshSeeds() {
        availableSeeds = CraftingManager.getPlantableSeeds(playerInv);
        seedBtnRects   = new int[availableSeeds.size()][4];
        layout();
    }

    public void handleInput() {
        if (!visible) return;
        if (justOpened) { justOpened = false; return; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)
         || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close(); return;
        }

        if (!Gdx.input.justTouched()) return;
        int mx = Gdx.input.getX();
        int my = sh - Gdx.input.getY();

        if (hit(mx, my, removeBtnX, removeBtnY, removeBtnW, removeBtnH)) {
            Item planted = manager.getPlantedSeed(planter);
            if (planted != null) {
                manager.removeSeed(planter);
                playerInv.addItem(planted, 1);
                refreshSeeds();
                refreshGrowSheet();
            }
            return;
        }

        for (int i = 0; i < seedBtnRects.length; i++) {
            int[] r = seedBtnRects[i];
            if (hit(mx, my, r[0], r[1], r[2], r[3])) {
                if (manager.getPlantedSeed(planter) == null) {
                    Item seed = availableSeeds.get(i);
                    if (playerInv.removeItem(seed, 1)) {
                        manager.plantSeed(planter, seed);
                        refreshSeeds();
                        refreshGrowSheet();
                    }
                }
                return;
            }
        }

        Inventory planterInv = manager.getPlanterInventory(planter);
        if (planterInv != null) {
            for (int i = 0; i < invSlotRects.length; i++) {
                int[] r = invSlotRects[i];
                if (r[2] == 0) continue;
                if (hit(mx, my, r[0], r[1], r[2], r[3])) {
                    ItemStack s = planterInv.getSlot(i);
                    if (s != null && s.getQuantity() > 0) {
                        if (planterInv.removeItem(s.getItem(), 1)) {
                            playerInv.addItem(s.getItem(), 1);
                        }
                    }
                    return;
                }
            }
        }

        if (!hit(mx, my, panelX, panelY, PANEL_W, panelH)) close();
    }

    public void render(SpriteBatch batch) {
        if (!visible || planter == null) return;

        batch.end();
        shape.setProjectionMatrix(batch.getProjectionMatrix());
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.09f, 0.11f, 0.13f, 0.97f);
        shape.rect(panelX, panelY, PANEL_W, panelH);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.35f, 0.70f, 0.35f, 1f);
        shape.rect(panelX, panelY, PANEL_W, panelH);
        shape.end();
        batch.begin();

        int cursorY = panelY + panelH - PAD;
        int innerX  = panelX + PAD;
        int innerW  = PANEL_W - PAD * 2;
        int cx      = panelX + PANEL_W / 2;

        titleFont.setColor(0.55f, 0.95f, 0.55f, 1f);
        layout.setText(titleFont, "Planter");
        titleFont.draw(batch, "Planter", innerX, cursorY);
        cursorY -= (int) layout.height + LINE_GAP + 6;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.30f, 0.50f, 0.30f, 0.6f);
        shape.rect(innerX, cursorY, innerW, 1);
        shape.end();
        batch.begin();
        cursorY -= LINE_GAP + 6;

        Item    planted  = manager.getPlantedSeed(planter);
        boolean hasCrop  = planted != null;
        int     frameIdx = Math.max(0, Math.min(8, manager.getGrowFrame(planter)));

        int previewX = cx - PREVIEW_W / 2;
        int previewY = cursorY - PREVIEW_H;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.10f, 0.16f, 0.10f, 1f);
        shape.rect(previewX, previewY, PREVIEW_W, PREVIEW_H);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.28f, 0.52f, 0.28f, 1f);
        shape.rect(previewX, previewY, PREVIEW_W, PREVIEW_H);
        shape.end();
        batch.begin();

        if (hasCrop && growFrames != null) {
            batch.draw(growFrames[frameIdx], previewX, previewY, PREVIEW_W, PREVIEW_H);
        }

        cursorY = previewY - LINE_GAP - 6;

        float progress = manager.getGrowProgress(planter);
        drawLabeledBar(batch, innerX, cursorY, innerW, BAR_H,
                "Growth",
                hasCrop ? progress : 0f,
                hasCrop,
                new Color(0.25f, 0.78f, 0.30f, 1f),
                hasCrop ? (int)(progress * 100f) + "%" : "no seed");
        cursorY -= BAR_H + LINE_GAP + 18;

        boolean hasWater   = manager.hasWaterSource(planter);
        float   waterRatio = manager.getWaterRatio(planter);
        drawLabeledBar(batch, innerX, cursorY, innerW, BAR_H,
                "Water",
                waterRatio,
                hasWater,
                new Color(0.20f, 0.55f, 1.00f, 1f),
                hasWater ? (int)(waterRatio * 100f) + "%" : "not connected");
        cursorY -= BAR_H + LINE_GAP + 18;

        boolean hasFert   = manager.hasFertilizerSource(planter);
        float   fertRatio = manager.getFertilizerRatio(planter);
        drawLabeledBar(batch, innerX, cursorY, innerW, BAR_H,
                "Fertilizer",
                fertRatio,
                hasFert,
                new Color(0.55f, 0.90f, 0.30f, 1f),
                hasFert ? (int)(fertRatio * 100f) + "%" : "not connected");
        cursorY -= BAR_H + LINE_GAP + 18;

        labelFont.setColor(0.75f, 0.90f, 0.75f, 1f);
        String seedLabel = hasCrop
                ? "Growing: " + planted.getDisplayName()
                : "No seed planted";
        labelFont.draw(batch, seedLabel, innerX, cursorY);

        removeBtnW = 0;
        if (hasCrop) {
            int rbW = 62, rbH = 20;
            int rbX = panelX + PANEL_W - PAD - rbW;
            int rbY = cursorY - rbH + 2;
            removeBtnX = rbX; removeBtnY = rbY;
            removeBtnW = rbW; removeBtnH = rbH;

            batch.end();
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0.50f, 0.15f, 0.15f, 1f);
            shape.rect(rbX, rbY, rbW, rbH);
            shape.end();
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(0.80f, 0.35f, 0.35f, 1f);
            shape.rect(rbX, rbY, rbW, rbH);
            shape.end();
            batch.begin();

            tinyFont.setColor(1f, 0.80f, 0.80f, 1f);
            GlyphLayout gl = new GlyphLayout(tinyFont, "Remove");
            tinyFont.draw(batch, "Remove",
                    rbX + (rbW - gl.width) / 2f,
                    rbY + rbH - 3);
        }

        cursorY -= LINE_GAP + 22;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.30f, 0.50f, 0.30f, 0.6f);
        shape.rect(innerX, cursorY, innerW, 1);
        shape.end();
        batch.begin();
        cursorY -= LINE_GAP + 8;

        labelFont.setColor(0.40f, 0.60f, 0.40f, 1f);
        labelFont.draw(batch, "Plant a seed", innerX, cursorY);
        cursorY -= LINE_GAP + 10;

        refreshSeeds();

        int col    = 0;
        int seedY  = cursorY - SEED_SIZE;
        int seedX0 = innerX;

        for (int i = 0; i < availableSeeds.size(); i++) {
            Item seed = availableSeeds.get(i);
            int  bx   = seedX0 + col * (SEED_SIZE + SEED_GAP);
            int  by   = seedY;

            seedBtnRects[i] = new int[]{ bx, by, SEED_SIZE, SEED_SIZE };

            boolean canPlant = !hasCrop;
            batch.end();
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(canPlant ? 0.16f : 0.11f,
                           canPlant ? 0.27f : 0.14f,
                           canPlant ? 0.16f : 0.11f, 1f);
            shape.rect(bx, by, SEED_SIZE, SEED_SIZE);
            shape.end();
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(canPlant ? 0.40f : 0.25f,
                           canPlant ? 0.65f : 0.35f,
                           canPlant ? 0.40f : 0.25f, 1f);
            shape.rect(bx, by, SEED_SIZE, SEED_SIZE);
            shape.end();
            batch.begin();

            tinyFont.setColor(canPlant ? 0.85f : 0.42f,
                              canPlant ? 1.00f : 0.50f,
                              canPlant ? 0.85f : 0.42f, 1f);
            String name = seed.getDisplayName();
            GlyphLayout gl = new GlyphLayout(tinyFont, name);
            if (gl.width > SEED_SIZE - 4) {
                int sp = name.indexOf(' ');
                if (sp > 0) {
                    String l1 = name.substring(0, sp);
                    String l2 = name.substring(sp + 1);
                    GlyphLayout g1 = new GlyphLayout(tinyFont, l1);
                    GlyphLayout g2 = new GlyphLayout(tinyFont, l2);
                    tinyFont.draw(batch, l1, bx + (SEED_SIZE - g1.width) / 2f, by + SEED_SIZE * 0.65f);
                    tinyFont.draw(batch, l2, bx + (SEED_SIZE - g2.width) / 2f, by + SEED_SIZE * 0.40f);
                } else {
                    tinyFont.draw(batch, name, bx + 2, by + SEED_SIZE / 2f + gl.height / 2f);
                }
            } else {
                tinyFont.draw(batch, name,
                        bx + (SEED_SIZE - gl.width) / 2f,
                        by + SEED_SIZE / 2f + gl.height / 2f);
            }

            tinyFont.setColor(0.55f, 0.55f, 0.65f, 1f);
            String qStr = "x" + playerInv.getQuantity(seed);
            GlyphLayout ql = new GlyphLayout(tinyFont, qStr);
            tinyFont.draw(batch, qStr,
                    bx + SEED_SIZE - ql.width - 2,
                    by + tinyFont.getCapHeight() + 3);

            col++;
            if (col >= SEEDS_PER_ROW) { col = 0; seedY -= SEED_SIZE + SEED_GAP; }
        }

        if (availableSeeds.isEmpty()) {
            labelFont.setColor(0.38f, 0.38f, 0.45f, 1f);
            labelFont.draw(batch, "No seeds in inventory", innerX, seedY + SEED_SIZE);
        }

        int seedRowsUsed = availableSeeds.isEmpty() ? 1
                : (int) Math.ceil((double) availableSeeds.size() / SEEDS_PER_ROW);
        cursorY = seedY - (seedRowsUsed - 1) * (SEED_SIZE + SEED_GAP) - SEED_SIZE - LINE_GAP - 14;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.30f, 0.50f, 0.30f, 0.6f);
        shape.rect(innerX, cursorY, innerW, 1);
        shape.end();
        batch.begin();
        cursorY -= LINE_GAP + 8;

        labelFont.setColor(0.45f, 0.70f, 0.45f, 1f);
        labelFont.draw(batch, "Output  (click to take)", innerX, cursorY);
        cursorY -= LINE_GAP + 6;

        Inventory planterInv = manager.getOrCreatePlanterInventory(planter);
        int invSlots  = PlanterManager.PLANTER_INV_SLOTS;
        int invRow    = 0;
        int invCol    = 0;
        int invStartY = cursorY - INV_SLOT_SIZE;

        for (int i = 0; i < invSlots; i++) {
            int sx = innerX + invCol * (INV_SLOT_SIZE + INV_SLOT_GAP);
            int sy = invStartY - invRow * (INV_SLOT_SIZE + INV_SLOT_GAP);

            invSlotRects[i] = new int[]{ sx, sy, INV_SLOT_SIZE, INV_SLOT_SIZE };

            ItemStack s    = planterInv.getSlot(i);
            boolean   full = s != null && s.getQuantity() > 0;

            batch.end();
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(full ? 0.14f : 0.10f,
                           full ? 0.22f : 0.13f,
                           full ? 0.14f : 0.10f, 1f);
            shape.rect(sx, sy, INV_SLOT_SIZE, INV_SLOT_SIZE);
            shape.end();
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(full ? 0.38f : 0.22f,
                           full ? 0.60f : 0.30f,
                           full ? 0.38f : 0.22f, 1f);
            shape.rect(sx, sy, INV_SLOT_SIZE, INV_SLOT_SIZE);
            shape.end();
            batch.begin();

            if (full) {
                String iname = s.getItem().getDisplayName();
                GlyphLayout igl = new GlyphLayout(tinyFont, iname);
                tinyFont.setColor(0.85f, 1.00f, 0.85f, 1f);
                if (igl.width <= INV_SLOT_SIZE - 4) {
                    tinyFont.draw(batch, iname,
                            sx + (INV_SLOT_SIZE - igl.width) / 2f,
                            sy + INV_SLOT_SIZE * 0.65f);
                } else {
                    int sp = iname.indexOf(' ');
                    if (sp > 0) {
                        String l1 = iname.substring(0, sp);
                        String l2 = iname.substring(sp + 1);
                        GlyphLayout g1 = new GlyphLayout(tinyFont, l1);
                        GlyphLayout g2 = new GlyphLayout(tinyFont, l2);
                        tinyFont.draw(batch, l1, sx + (INV_SLOT_SIZE - g1.width) / 2f, sy + INV_SLOT_SIZE * 0.72f);
                        tinyFont.draw(batch, l2, sx + (INV_SLOT_SIZE - g2.width) / 2f, sy + INV_SLOT_SIZE * 0.48f);
                    } else {
                        tinyFont.draw(batch, iname, sx + 2, sy + INV_SLOT_SIZE * 0.65f);
                    }
                }

                tinyFont.setColor(0.60f, 0.60f, 0.70f, 1f);
                String qStr = "x" + s.getQuantity();
                GlyphLayout ql = new GlyphLayout(tinyFont, qStr);
                tinyFont.draw(batch, qStr,
                        sx + INV_SLOT_SIZE - ql.width - 2,
                        sy + tinyFont.getCapHeight() + 3);
            }

            invCol++;
            if (invCol >= INV_COLS) { invCol = 0; invRow++; }
        }

        tinyFont.setColor(0.30f, 0.30f, 0.38f, 1f);
        tinyFont.draw(batch, "E or ESC to close", innerX, panelY + 14);
    }

    public void drawPlanterHoverBars(SpriteBatch batch,
                                      PlacedObject planter,
                                      float leftTileScreenX,  float leftTileScreenY,
                                      float rightTileScreenX, float rightTileScreenY,
                                      float tileSize) {

        boolean hasWater = manager.hasWaterSource(planter);
        float   waterRatio = manager.getWaterRatio(planter);
        boolean hasFert  = manager.hasFertilizerSource(planter);
        float   fertRatio  = manager.getFertilizerRatio(planter);

        batch.end();
        shape.setProjectionMatrix(batch.getProjectionMatrix());

        int barH = 6;
        int barW = (int) tileSize;

        drawHoverBar(leftTileScreenX, leftTileScreenY + tileSize + 4,
                     barW, barH,
                     waterRatio, hasWater,
                     0.20f, 0.55f, 1.00f);

        drawHoverBar(rightTileScreenX, rightTileScreenY + tileSize + 4,
                     barW, barH,
                     fertRatio, hasFert,
                     0.55f, 0.90f, 0.25f);

        batch.begin();

        drawHoverLabel(batch, leftTileScreenX,  leftTileScreenY  + tileSize + 4 + barH,
                       barW, hasWater ? (int)(waterRatio * 100f) + "%" : "no water",
                       hasWater ? 0.60f : 0.38f, hasWater ? 0.82f : 0.42f, hasWater ? 1.00f : 0.42f);

        drawHoverLabel(batch, rightTileScreenX, rightTileScreenY + tileSize + 4 + barH,
                       barW, hasFert ? (int)(fertRatio * 100f) + "%" : "no fert",
                       hasFert ? 0.65f : 0.38f, hasFert ? 1.00f : 0.42f, hasFert ? 0.35f : 0.42f);
    }

    private void drawHoverBar(float x, float y, int w, int h,
                               float ratio, boolean active,
                               float r, float g, float b) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.08f, 0.10f, 0.08f, 0.82f);
        shape.rect(x, y, w, h);
        if (active && ratio > 0f) {
            shape.setColor(r, g, b, 0.90f);
            shape.rect(x, y, w * ratio, h);
        }
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(active ? r * 0.7f : 0.22f,
                       active ? g * 0.7f : 0.26f,
                       active ? b * 0.7f : 0.20f, 1f);
        shape.rect(x, y, w, h);
        shape.end();
    }

    private void drawHoverLabel(SpriteBatch batch,
                                 float barX, float barTopY,
                                 int barW, String text,
                                 float r, float g, float b) {
        tinyFont.setColor(r, g, b, 1f);
        GlyphLayout gl = new GlyphLayout(tinyFont, text);
        tinyFont.draw(batch, text,
                barX + (barW - gl.width) / 2f,
                barTopY + gl.height + 2);
    }

    private void drawLabeledBar(SpriteBatch batch,
                                 int x, int y, int totalW, int barH,
                                 String label, float ratio, boolean active,
                                 Color fillColor, String annotation) {

        labelFont.setColor(active ? 0.78f : 0.40f,
                           active ? 0.92f : 0.45f,
                           active ? 0.78f : 0.40f, 1f);
        GlyphLayout labelGl = new GlyphLayout(labelFont, label);
        int labelW = (int) labelGl.width + 10;

        tinyFont.setColor(1f, 1f, 1f, 1f);
        GlyphLayout annGl = new GlyphLayout(tinyFont, annotation);
        int annW = (int) annGl.width + 6;

        int barX = x + labelW;
        int barW = totalW - labelW - annW;
        int barY = y - barH;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.12f, 0.14f, 0.16f, 1f);
        shape.rect(barX, barY, barW, barH);

        if (active && ratio > 0f) {
            shape.setColor(fillColor.r, fillColor.g, fillColor.b, fillColor.a);
            shape.rect(barX, barY, barW * ratio, barH);
        }
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(active ? 0.38f : 0.22f,
                       active ? 0.52f : 0.26f,
                       active ? 0.38f : 0.22f, 1f);
        shape.rect(barX, barY, barW, barH);
        shape.end();
        batch.begin();

        labelFont.draw(batch, label,
                x,
                barY + barH * 0.5f + labelGl.height * 0.5f + 1);

        tinyFont.setColor(active ? 0.85f : 0.38f,
                          active ? 0.85f : 0.38f,
                          active ? 0.90f : 0.38f, 1f);
        tinyFont.draw(batch, annotation,
                barX + barW + 4,
                barY + barH * 0.5f + annGl.height * 0.5f + 1);
    }

    private boolean hit(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    public void dispose() {
        shape.dispose();
        titleFont.dispose();
        labelFont.dispose();
        tinyFont.dispose();
        if (growSheet != null) growSheet.dispose();
    }
}