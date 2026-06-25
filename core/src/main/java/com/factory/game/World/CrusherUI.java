package com.factory.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.ItemStack;

public class CrusherUI {

    private static final int PANEL_W    = 300;
    private static final int PAD        = 18;
    private static final int LINE_GAP   = 8;
    private static final int BAR_H      = 12;
    private static final int SLOT_SIZE  = 64;

    private static final int PANEL_H =
            PAD
            + 20 + LINE_GAP + 6
            + 1  + LINE_GAP + 6
            + BAR_H + LINE_GAP + 18
            + 1  + LINE_GAP + 8
            + 14 + LINE_GAP + 6
            + SLOT_SIZE + LINE_GAP + 8
            + 1  + LINE_GAP + 8
            + 14 + LINE_GAP + 6
            + SLOT_SIZE + LINE_GAP
            + PAD + 14;

    private boolean      visible    = false;
    private boolean      justOpened = false;
    private PlacedObject crusher    = null;
    private Inventory    playerInv  = null;

    private final CrusherManager manager;

    private final ShapeRenderer shape;
    private final BitmapFont    titleFont;
    private final BitmapFont    labelFont;
    private final BitmapFont    tinyFont;
    private final GlyphLayout   layout = new GlyphLayout();

    private int panelX, panelY, sw, sh;

    private int[] inputSlotRect  = new int[4];
    private int[] outputSlotRect = new int[4];

    public CrusherUI(CrusherManager manager) {
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

    public void open(PlacedObject crusher, Inventory playerInventory) {
        this.crusher   = crusher;
        this.playerInv = playerInventory;
        this.visible   = true;
        this.justOpened = true;
        layout();
    }

    public void close() {
        visible = false;
        crusher = null;
    }

    public boolean isVisible() { return visible; }

    public void resize(int w, int h) { sw = w; sh = h; layout(); }

    private void layout() {
        sw     = Gdx.graphics.getWidth();
        sh     = Gdx.graphics.getHeight();
        panelX = (sw - PANEL_W) / 2;
        panelY = (sh - PANEL_H) / 2;
        shape.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
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

        if (hit(mx, my, outputSlotRect[0], outputSlotRect[1], outputSlotRect[2], outputSlotRect[3])) {
            Inventory inv = manager.getInventory(crusher);
            if (inv != null) {
                ItemStack s = inv.getSlot(CrusherManager.OUTPUT_SLOT);
                if (s != null && s.getQuantity() > 0) {
                    ItemStack pulled = manager.pullFromOutput(crusher);
                    if (pulled != null) {
                        playerInv.addItem(pulled.getItem(), pulled.getQuantity());
                    }
                }
            }
            return;
        }

        if (!hit(mx, my, panelX, panelY, PANEL_W, PANEL_H)) close();
    }

    public void render(SpriteBatch batch) {
        if (!visible || crusher == null) return;

        batch.end();
        shape.setProjectionMatrix(batch.getProjectionMatrix());
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.09f, 0.10f, 0.11f, 0.97f);
        shape.rect(panelX, panelY, PANEL_W, PANEL_H);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.75f, 0.50f, 0.15f, 1f);
        shape.rect(panelX, panelY, PANEL_W, PANEL_H);
        shape.end();
        batch.begin();

        int cursorY = panelY + PANEL_H - PAD;
        int innerX  = panelX + PAD;
        int innerW  = PANEL_W - PAD * 2;

        titleFont.setColor(1.00f, 0.72f, 0.20f, 1f);
        layout.setText(titleFont, "Crusher");
        titleFont.draw(batch, "Crusher", innerX, cursorY);
        cursorY -= (int) layout.height + LINE_GAP + 6;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.60f, 0.40f, 0.10f, 0.6f);
        shape.rect(innerX, cursorY, innerW, 1);
        shape.end();
        batch.begin();
        cursorY -= LINE_GAP + 6;

        boolean processing = manager.isProcessing(crusher);
        float   progress   = manager.getProgress(crusher);

        drawLabeledBar(batch, innerX, cursorY, innerW, BAR_H,
                "Progress",
                processing ? progress : 0f,
                processing,
                new Color(1.00f, 0.60f, 0.10f, 1f),
                processing ? (int)(progress * 100f) + "%" : "idle");
        cursorY -= BAR_H + LINE_GAP + 18;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.60f, 0.40f, 0.10f, 0.6f);
        shape.rect(innerX, cursorY, innerW, 1);
        shape.end();
        batch.begin();
        cursorY -= LINE_GAP + 8;

        labelFont.setColor(0.85f, 0.65f, 0.30f, 1f);
        labelFont.draw(batch, "Input", innerX, cursorY);
        cursorY -= LINE_GAP + 6;

        Inventory inv = manager.getOrCreateInventory(crusher);
        ItemStack inputStack = inv.getSlot(CrusherManager.INPUT_SLOT);
        boolean   hasInput   = inputStack != null && inputStack.getQuantity() > 0;

        int slotX = innerX;
        int slotY = cursorY - SLOT_SIZE;
        inputSlotRect = new int[]{ slotX, slotY, SLOT_SIZE, SLOT_SIZE };

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(hasInput ? 0.18f : 0.10f,
                       hasInput ? 0.14f : 0.10f,
                       hasInput ? 0.10f : 0.10f, 1f);
        shape.rect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(hasInput ? 0.75f : 0.30f,
                       hasInput ? 0.50f : 0.25f,
                       hasInput ? 0.10f : 0.18f, 1f);
        shape.rect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
        shape.end();
        batch.begin();

        if (hasInput) {
            drawSlotLabel(batch, inputStack, slotX, slotY);
        } else {
            tinyFont.setColor(0.30f, 0.28f, 0.22f, 1f);
            GlyphLayout el = new GlyphLayout(tinyFont, "empty");
            tinyFont.draw(batch, "empty",
                    slotX + (SLOT_SIZE - el.width) / 2f,
                    slotY + SLOT_SIZE / 2f + el.height / 2f);
        }

        cursorY = slotY - LINE_GAP - 8;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.60f, 0.40f, 0.10f, 0.6f);
        shape.rect(innerX, cursorY, innerW, 1);
        shape.end();
        batch.begin();
        cursorY -= LINE_GAP + 8;

        labelFont.setColor(0.85f, 0.65f, 0.30f, 1f);
        labelFont.draw(batch, "Output  (click to take)", innerX, cursorY);
        cursorY -= LINE_GAP + 6;

        ItemStack outputStack = inv.getSlot(CrusherManager.OUTPUT_SLOT);
        boolean   hasOutput   = outputStack != null && outputStack.getQuantity() > 0;

        int outSlotX = innerX;
        int outSlotY = cursorY - SLOT_SIZE;
        outputSlotRect = new int[]{ outSlotX, outSlotY, SLOT_SIZE, SLOT_SIZE };

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(hasOutput ? 0.12f : 0.10f,
                       hasOutput ? 0.20f : 0.10f,
                       hasOutput ? 0.12f : 0.10f, 1f);
        shape.rect(outSlotX, outSlotY, SLOT_SIZE, SLOT_SIZE);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(hasOutput ? 0.35f : 0.22f,
                       hasOutput ? 0.65f : 0.28f,
                       hasOutput ? 0.35f : 0.18f, 1f);
        shape.rect(outSlotX, outSlotY, SLOT_SIZE, SLOT_SIZE);
        shape.end();
        batch.begin();

        if (hasOutput) {
            drawSlotLabel(batch, outputStack, outSlotX, outSlotY);
        } else {
            tinyFont.setColor(0.25f, 0.32f, 0.25f, 1f);
            GlyphLayout el = new GlyphLayout(tinyFont, "empty");
            tinyFont.draw(batch, "empty",
                    outSlotX + (SLOT_SIZE - el.width) / 2f,
                    outSlotY + SLOT_SIZE / 2f + el.height / 2f);
        }

        tinyFont.setColor(0.30f, 0.30f, 0.38f, 1f);
        tinyFont.draw(batch, "E or ESC to close", innerX, panelY + 14);
    }

    private void drawSlotLabel(SpriteBatch batch, ItemStack s, int sx, int sy) {
        String name = s.getItem().getDisplayName();
        tinyFont.setColor(1.00f, 0.90f, 0.70f, 1f);
        GlyphLayout gl = new GlyphLayout(tinyFont, name);
        if (gl.width <= SLOT_SIZE - 4) {
            tinyFont.draw(batch, name,
                    sx + (SLOT_SIZE - gl.width) / 2f,
                    sy + SLOT_SIZE * 0.65f);
        } else {
            int sp = name.indexOf(' ');
            if (sp > 0) {
                String l1 = name.substring(0, sp);
                String l2 = name.substring(sp + 1);
                GlyphLayout g1 = new GlyphLayout(tinyFont, l1);
                GlyphLayout g2 = new GlyphLayout(tinyFont, l2);
                tinyFont.draw(batch, l1, sx + (SLOT_SIZE - g1.width) / 2f, sy + SLOT_SIZE * 0.72f);
                tinyFont.draw(batch, l2, sx + (SLOT_SIZE - g2.width) / 2f, sy + SLOT_SIZE * 0.48f);
            } else {
                tinyFont.draw(batch, name, sx + 2, sy + SLOT_SIZE * 0.65f);
            }
        }

        tinyFont.setColor(0.60f, 0.60f, 0.70f, 1f);
        String qStr = "x" + s.getQuantity();
        GlyphLayout ql = new GlyphLayout(tinyFont, qStr);
        tinyFont.draw(batch, qStr,
                sx + SLOT_SIZE - ql.width - 2,
                sy + tinyFont.getCapHeight() + 3);
    }

    private void drawLabeledBar(SpriteBatch batch,
                                 int x, int y, int totalW, int barH,
                                 String label, float ratio, boolean active,
                                 Color fillColor, String annotation) {
        labelFont.setColor(active ? 1.00f : 0.42f,
                           active ? 0.70f : 0.40f,
                           active ? 0.20f : 0.35f, 1f);
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
        shape.setColor(0.12f, 0.11f, 0.09f, 1f);
        shape.rect(barX, barY, barW, barH);
        if (active && ratio > 0f) {
            shape.setColor(fillColor.r, fillColor.g, fillColor.b, fillColor.a);
            shape.rect(barX, barY, barW * ratio, barH);
        }
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(active ? 0.60f : 0.28f,
                       active ? 0.38f : 0.24f,
                       active ? 0.10f : 0.18f, 1f);
        shape.rect(barX, barY, barW, barH);
        shape.end();
        batch.begin();

        labelFont.draw(batch, label,
                x,
                barY + barH * 0.5f + labelGl.height * 0.5f + 1);

        tinyFont.setColor(active ? 1.00f : 0.38f,
                          active ? 0.80f : 0.38f,
                          active ? 0.40f : 0.38f, 1f);
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
    }
}