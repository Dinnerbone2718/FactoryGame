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

public class OreDrillUI {


    private static final int PANEL_W       = 320;
    private static final int PAD           = 18;
    private static final int LINE_GAP      = 8;
    private static final int BAR_H         = 12;
    private static final int INV_SLOT_SIZE = 56;
    private static final int INV_SLOT_GAP  = 6;
    private static final int INV_COLS      = 3;  

    private boolean      visible    = false;
    private boolean      justOpened = false;
    private PlacedObject drill      = null;
    private Inventory    playerInv  = null;

    private final OreDrillManager manager;
    private final ShapeRenderer   shape;
    private final BitmapFont      titleFont;
    private final BitmapFont      labelFont;
    private final BitmapFont      tinyFont;
    private final GlyphLayout     layout = new GlyphLayout();

    private int panelX, panelY, panelH, sw, sh;

    private final int[][] invSlotRects = new int[OreDrillManager.ORE_DRILL_INV_SLOTS][4];



    public OreDrillUI(OreDrillManager manager) {
        this.manager = manager;
        shape = new ShapeRenderer();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

        p.size = 20; p.color = Color.WHITE; titleFont = gen.generateFont(p);
        p.size = 13; p.color = Color.WHITE; labelFont = gen.generateFont(p);
        p.size = 10; p.color = Color.WHITE; tinyFont  = gen.generateFont(p);

        gen.dispose();
    }


    public void open(PlacedObject drill, Inventory playerInventory) {
        this.drill      = drill;
        this.playerInv  = playerInventory;
        this.visible    = true;
        this.justOpened = true;
        layout();
    }

    public void close()        { visible = false; drill = null; }
    public boolean isVisible() { return visible; }

    public void resize(int w, int h) { sw = w; sh = h; layout(); }


    private void layout() {
        sw = Gdx.graphics.getWidth();
        sh = Gdx.graphics.getHeight();

        int h = PAD;
        h += 20 + LINE_GAP + 6;         
        h += 1  + LINE_GAP + 6;        
        h += BAR_H + LINE_GAP + 18;   
        h += BAR_H + LINE_GAP + 18;      
        h += 14 + LINE_GAP;          
        h += 1  + LINE_GAP + 8;         
        h += 14 + LINE_GAP + 6;          
        int invRows = (int) Math.ceil((double) OreDrillManager.ORE_DRILL_INV_SLOTS / INV_COLS);
        h += (INV_SLOT_SIZE + INV_SLOT_GAP) * invRows;
        h += LINE_GAP + 1 + LINE_GAP;    
        h += 14 + PAD;                  

        panelH = h;
        panelX = (sw - PANEL_W) / 2;
        panelY = (sh - panelH) / 2;
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

        Inventory drillInv = manager.getInventory(drill);
        if (drillInv != null) {
            for (int i = 0; i < invSlotRects.length; i++) {
                int[] r = invSlotRects[i];
                if (r[2] == 0) continue;
                if (hit(mx, my, r[0], r[1], r[2], r[3])) {
                    ItemStack s = drillInv.getSlot(i);
                    if (s != null && s.getQuantity() > 0 && playerInv != null) {
                        if (drillInv.removeItem(s.getItem(), 1)) {
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
        if (!visible || drill == null) return;

        batch.end();
        shape.setProjectionMatrix(batch.getProjectionMatrix());
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.08f, 0.09f, 0.10f, 0.97f);
        shape.rect(panelX, panelY, PANEL_W, panelH);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.75f, 0.45f, 0.10f, 1f);
        shape.rect(panelX, panelY, PANEL_W, panelH);
        shape.end();
        batch.begin();

        int cursorY = panelY + panelH - PAD;
        int innerX  = panelX + PAD;
        int innerW  = PANEL_W - PAD * 2;

        titleFont.setColor(0.95f, 0.65f, 0.15f, 1f);
        layout.setText(titleFont, "Ore Drill");
        titleFont.draw(batch, "Ore Drill", innerX, cursorY);
        cursorY -= (int) layout.height + LINE_GAP + 6;

        drawSeparator(batch, innerX, cursorY, innerW);
        cursorY -= LINE_GAP + 6;

        float   gasRatio = manager.getGasRatio(drill);
        boolean gasOk    = manager.hasGas(drill);
        String  gasAnn   = gasRatio > 0f ? (int)(gasRatio * 100f) + "%" : "empty";
        drawLabeledBar(batch, innerX, cursorY, innerW, BAR_H,
                "Gas", gasRatio, gasRatio > 0f,
                new Color(0.35f, 0.65f, 1.00f, 1f),
                gasAnn);
        cursorY -= BAR_H + LINE_GAP + 18;

        float  progress = manager.getMineProgress(drill);
        String mineAnn  = gasOk ? (int)(progress * 100f) + "%" : "no gas";
        drawLabeledBar(batch, innerX, cursorY, innerW, BAR_H,
                "Mining", gasOk ? progress : 0f, gasOk,
                new Color(0.90f, 0.52f, 0.10f, 1f),
                mineAnn);
        cursorY -= BAR_H + LINE_GAP + 18;

        if (gasOk) {
            labelFont.setColor(0.80f, 0.62f, 0.22f, 1f);
            labelFont.draw(batch, "Mining ores...", innerX, cursorY);
        } else {
            labelFont.setColor(0.42f, 0.38f, 0.32f, 1f);
            labelFont.draw(batch, "Waiting for gas...", innerX, cursorY);
        }
        cursorY -= 14 + LINE_GAP;

        drawSeparator(batch, innerX, cursorY, innerW);
        cursorY -= LINE_GAP + 8;

        labelFont.setColor(0.60f, 0.40f, 0.12f, 1f);
        labelFont.draw(batch, "Output  (click to take)", innerX, cursorY);
        cursorY -= 14 + LINE_GAP + 6;

        Inventory drillInv = manager.getOrCreateInventory(drill);
        int invRow = 0, invCol = 0;
        int invStartY = cursorY - INV_SLOT_SIZE;

        for (int i = 0; i < OreDrillManager.ORE_DRILL_INV_SLOTS; i++) {
            int sx = innerX + invCol * (INV_SLOT_SIZE + INV_SLOT_GAP);
            int sy = invStartY - invRow * (INV_SLOT_SIZE + INV_SLOT_GAP);

            invSlotRects[i] = new int[]{ sx, sy, INV_SLOT_SIZE, INV_SLOT_SIZE };

            ItemStack s    = drillInv.getSlot(i);
            boolean   full = s != null && s.getQuantity() > 0;

            batch.end();
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(full ? 0.18f : 0.10f,
                           full ? 0.13f : 0.09f,
                           full ? 0.05f : 0.07f, 1f);
            shape.rect(sx, sy, INV_SLOT_SIZE, INV_SLOT_SIZE);
            shape.end();
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(full ? 0.65f : 0.28f,
                           full ? 0.42f : 0.22f,
                           full ? 0.10f : 0.14f, 1f);
            shape.rect(sx, sy, INV_SLOT_SIZE, INV_SLOT_SIZE);
            shape.end();
            batch.begin();

            if (full) {
                String iname = s.getItem().getDisplayName();
                GlyphLayout igl = new GlyphLayout(tinyFont, iname);
                tinyFont.setColor(1.00f, 0.85f, 0.55f, 1f);

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

                tinyFont.setColor(0.60f, 0.48f, 0.25f, 1f);
                String qStr = "x" + s.getQuantity();
                GlyphLayout ql = new GlyphLayout(tinyFont, qStr);
                tinyFont.draw(batch, qStr,
                        sx + INV_SLOT_SIZE - ql.width - 2,
                        sy + tinyFont.getCapHeight() + 3);
            }

            invCol++;
            if (invCol >= INV_COLS) { invCol = 0; invRow++; }
        }

        tinyFont.setColor(0.32f, 0.26f, 0.18f, 1f);
        tinyFont.draw(batch, "E or ESC to close", innerX, panelY + 14);
    }


    private void drawSeparator(SpriteBatch batch, int x, int y, int w) {
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.55f, 0.35f, 0.08f, 0.65f);
        shape.rect(x, y, w, 1);
        shape.end();
        batch.begin();
    }

    private void drawLabeledBar(SpriteBatch batch,
                                 int x, int y, int totalW, int barH,
                                 String label, float ratio, boolean active,
                                 Color fillColor, String annotation) {

        labelFont.setColor(active ? 0.90f : 0.42f,
                           active ? 0.65f : 0.42f,
                           active ? 0.18f : 0.42f, 1f);
        GlyphLayout labelGl = new GlyphLayout(labelFont, label);
        int labelW = (int) labelGl.width + 10;

        GlyphLayout annGl = new GlyphLayout(tinyFont, annotation);
        int annW = (int) annGl.width + 6;

        int barX = x + labelW;
        int barW = totalW - labelW - annW;
        int barY = y - barH;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.11f, 0.09f, 0.07f, 1f);
        shape.rect(barX, barY, barW, barH);
        if (active && ratio > 0f) {
            shape.setColor(fillColor.r, fillColor.g, fillColor.b, fillColor.a);
            shape.rect(barX, barY, barW * ratio, barH);
        }
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(active ? 0.52f : 0.22f,
                       active ? 0.36f : 0.22f,
                       active ? 0.08f : 0.22f, 1f);
        shape.rect(barX, barY, barW, barH);
        shape.end();
        batch.begin();

        labelFont.draw(batch, label, x,
                barY + barH * 0.5f + labelGl.height * 0.5f + 1);

        tinyFont.setColor(active ? 0.88f : 0.40f,
                          active ? 0.70f : 0.40f,
                          active ? 0.28f : 0.40f, 1f);
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