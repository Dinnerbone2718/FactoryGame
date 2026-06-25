package com.factory.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.ItemStack;
import com.factory.game.Items.SmeltingRecipe;

public class SmelterUI {



    private static final int PANEL_W   = 320;
    private static final int PAD       = 18;
    private static final int LINE_GAP  = 8;
    private static final int BAR_H     = 12;
    private static final int SLOT_SIZE = 64;

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
            + BAR_H + 4                 
            + 14 + LINE_GAP               
            + PAD + 14;                 


    private boolean      visible    = false;
    private boolean      justOpened = false;
    private PlacedObject smelter    = null;
    private Inventory    playerInv  = null;

    private int[] inputSlotRect = new int[4];

    private final SmelterManager manager;



    private final ShapeRenderer shape;
    private final BitmapFont    titleFont;
    private final BitmapFont    labelFont;
    private final BitmapFont    tinyFont;
    private final GlyphLayout   layout = new GlyphLayout();

    private int panelX, panelY, sw, sh;


    public SmelterUI(SmelterManager manager) {
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



    public void open(PlacedObject smelter, Inventory playerInventory) {
        this.smelter    = smelter;
        this.playerInv  = playerInventory;
        this.visible    = true;
        this.justOpened = true;
        layout();
    }

    public void close() {
        visible   = false;
        smelter   = null;
        playerInv = null;
    }

    public boolean isVisible() { return visible; }

    public void resize(int w, int h) { sw = w; sh = h; layout(); }



    public void handleInput() {
        if (!visible) return;
        if (justOpened) { justOpened = false; return; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)
         || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        if (!Gdx.input.justTouched()) return;
        int mx = Gdx.input.getX();
        int my = sh - Gdx.input.getY();

        if (hit(mx, my, inputSlotRect[0], inputSlotRect[1],
                         inputSlotRect[2], inputSlotRect[3])) {

            Inventory inv    = manager.getOrCreateInventory(smelter);
            ItemStack inSlot = inv.getSlot(SmelterManager.INPUT_SLOT);

            for (int i = 0; i < playerInv.getSize(); i++) {
                ItemStack ps = playerInv.getSlot(i);
                if (ps == null || ps.getQuantity() <= 0) continue;

                if (CraftingManager.getSmeltRecipeFor(ps.getItem()) == null) continue;

                // Slot must be empty  same item type with room
                if (inSlot != null && inSlot.getQuantity() > 0
                        && (inSlot.getItem() != ps.getItem()
                            || inSlot.getQuantity() >= 64)) continue;

                if (manager.pushToInput(smelter, ps.getItem())) {
                    playerInv.removeItem(ps.getItem(), 1);
                    break;
                }
            }
            return;
        }

        if (!hit(mx, my, panelX, panelY, PANEL_W, PANEL_H)) close();
    }


    public void render(SpriteBatch batch) {
        if (!visible || smelter == null) return;

        batch.end();
        shape.setProjectionMatrix(batch.getProjectionMatrix());
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.09f, 0.07f, 0.05f, 0.97f);
        shape.rect(panelX, panelY, PANEL_W, PANEL_H);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.82f, 0.33f, 0.08f, 1f);
        shape.rect(panelX, panelY, PANEL_W, PANEL_H);
        shape.end();
        batch.begin();

        int cursorY = panelY + PANEL_H - PAD;
        int innerX  = panelX + PAD;
        int innerW  = PANEL_W - PAD * 2;

        titleFont.setColor(1.00f, 0.52f, 0.12f, 1f);
        layout.setText(titleFont, "Smelter");
        titleFont.draw(batch, "Smelter", innerX, cursorY);
        cursorY -= (int) layout.height + LINE_GAP + 6;

        divider(batch, innerX, cursorY, innerW);
        cursorY -= LINE_GAP + 6;

        boolean processing = manager.isProcessing(smelter);
        float   progress   = manager.getProgress(smelter);

        SmeltingRecipe activeRecipe = manager.getActiveRecipe(smelter);
        String annotation = processing
                ? (int)(progress * 100f) + "%"
                : (activeRecipe != null ? "ready" : "idle");

        drawLabeledBar(batch, innerX, cursorY, innerW, BAR_H,
                "Progress",
                processing ? progress : 0f,
                processing,
                new Color(1.00f, 0.42f, 0.08f, 1f),
                annotation);
        cursorY -= BAR_H + LINE_GAP + 18;


        divider(batch, innerX, cursorY, innerW);
        cursorY -= LINE_GAP + 8;

        labelFont.setColor(0.82f, 0.50f, 0.22f, 1f);
        labelFont.draw(batch, "Input  (click to insert)", innerX, cursorY);
        cursorY -= LINE_GAP + 6;

        Inventory inv        = manager.getOrCreateInventory(smelter);
        ItemStack inputStack = inv.getSlot(SmelterManager.INPUT_SLOT);
        boolean   hasInput   = inputStack != null && inputStack.getQuantity() > 0;

        int slotX = innerX;
        int slotY = cursorY - SLOT_SIZE;
        inputSlotRect = new int[]{ slotX, slotY, SLOT_SIZE, SLOT_SIZE };

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(hasInput ? 0.22f : 0.10f,
                       hasInput ? 0.12f : 0.10f,
                       hasInput ? 0.05f : 0.10f, 1f);
        shape.rect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(hasInput ? 0.82f : 0.30f,
                       hasInput ? 0.38f : 0.25f,
                       hasInput ? 0.08f : 0.18f, 1f);
        shape.rect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
        shape.end();
        batch.begin();

        if (hasInput) {
            drawSlotContents(batch, inputStack, slotX, slotY);
        } else {
            tinyFont.setColor(0.28f, 0.23f, 0.16f, 1f);
            GlyphLayout el = new GlyphLayout(tinyFont, "empty");
            tinyFont.draw(batch, "empty",
                    slotX + (SLOT_SIZE - el.width) / 2f,
                    slotY + SLOT_SIZE / 2f + el.height / 2f);
        }

        cursorY = slotY - LINE_GAP - 8;

        divider(batch, innerX, cursorY, innerW);
        cursorY -= LINE_GAP + 8;

        labelFont.setColor(0.82f, 0.50f, 0.22f, 1f);
        labelFont.draw(batch, "Output Tank", innerX, cursorY);
        cursorY -= LINE_GAP + 6;

        LiquidTank tank    = smelter.getLiquidTank();
        float      amount  = tank != null ? tank.getAmount()   : 0f;
        float      cap     = tank != null ? tank.getCapacity() : 1f;
        LiquidType liqType = tank != null ? tank.getType()     : null;
        float      ratio   = cap > 0f ? Math.min(1f, amount / cap) : 0f;
        boolean    hasLiq  = liqType != null && amount > 0.01f;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.10f, 0.08f, 0.06f, 1f);
        shape.rect(innerX, cursorY - BAR_H, innerW, BAR_H);
        if (hasLiq && ratio > 0f) {
            Color lc = liqType.color;
            shape.setColor(lc.r, lc.g, lc.b, 0.88f);
            shape.rect(innerX, cursorY - BAR_H, innerW * ratio, BAR_H);
        }
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(hasLiq ? 0.72f : 0.28f,
                       hasLiq ? 0.30f : 0.22f,
                       hasLiq ? 0.08f : 0.15f, 1f);
        shape.rect(innerX, cursorY - BAR_H, innerW, BAR_H);
        shape.end();
        batch.begin();

        cursorY -= BAR_H + 4;

        String tankStr = hasLiq
                ? liqType.name + "   " + (int) amount + " / " + (int) cap
                : "empty";
        tinyFont.setColor(hasLiq
                ? new Color(1.00f, 0.82f, 0.50f, 1f)
                : new Color(0.36f, 0.30f, 0.24f, 1f));
        tinyFont.draw(batch, tankStr, innerX, cursorY);

        tinyFont.setColor(0.28f, 0.26f, 0.36f, 1f);
        tinyFont.draw(batch, "E or ESC to close", innerX, panelY + 14);
    }

    private void divider(SpriteBatch batch, int x, int y, int w) {
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.55f, 0.25f, 0.06f, 0.50f);
        shape.rect(x, y, w, 1);
        shape.end();
        batch.begin();
    }

    private void drawSlotContents(SpriteBatch batch, ItemStack s, int sx, int sy) {
        String name = s.getItem().getDisplayName();
        tinyFont.setColor(1.00f, 0.88f, 0.62f, 1f);
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

        tinyFont.setColor(0.58f, 0.55f, 0.65f, 1f);
        String qty = "x" + s.getQuantity();
        GlyphLayout ql = new GlyphLayout(tinyFont, qty);
        tinyFont.draw(batch, qty,
                sx + SLOT_SIZE - ql.width - 2,
                sy + tinyFont.getCapHeight() + 3);
    }

    private void drawLabeledBar(SpriteBatch batch,
                                 int x, int y, int totalW, int barH,
                                 String label, float ratio, boolean active,
                                 Color fillColor, String annotation) {
        labelFont.setColor(active ? 1.00f : 0.42f,
                           active ? 0.52f : 0.40f,
                           active ? 0.12f : 0.30f, 1f);
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
        shape.setColor(0.11f, 0.09f, 0.07f, 1f);
        shape.rect(barX, barY, barW, barH);
        if (active && ratio > 0f) {
            shape.setColor(fillColor.r, fillColor.g, fillColor.b, fillColor.a);
            shape.rect(barX, barY, barW * ratio, barH);
        }
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(active ? 0.62f : 0.28f,
                       active ? 0.28f : 0.22f,
                       active ? 0.06f : 0.15f, 1f);
        shape.rect(barX, barY, barW, barH);
        shape.end();
        batch.begin();

        labelFont.draw(batch, label,
                x,
                barY + barH * 0.5f + labelGl.height * 0.5f + 1f);

        tinyFont.setColor(active ? 1.00f : 0.38f,
                          active ? 0.70f : 0.38f,
                          active ? 0.28f : 0.38f, 1f);
        tinyFont.draw(batch, annotation,
                barX + barW + 4,
                barY + barH * 0.5f + annGl.height * 0.5f + 1f);
    }

    private boolean hit(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }


    private void layout() {
        sw     = Gdx.graphics.getWidth();
        sh     = Gdx.graphics.getHeight();
        panelX = (sw - PANEL_W) / 2;
        panelY = (sh - PANEL_H) / 2;
        shape.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    public void dispose() {
        shape.dispose();
        titleFont.dispose();
        labelFont.dispose();
        tinyFont.dispose();
    }
}