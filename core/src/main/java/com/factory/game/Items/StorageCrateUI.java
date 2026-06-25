package com.factory.game.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

public class StorageCrateUI {

    private static final int SLOT_SIZE    = 48;
    private static final int SLOT_PADDING = 4;
    private static final int TITLE_H      = 40;
    private static final int PANEL_GAP    = 80;

    private static final int BTN_W = 64;
    private static final int BTN_H = 28;

    private static final int PLAYER_COLS = 6;
    private static final int PLAYER_ROWS = 8;
    private static final int CRATE_COLS  = 6;
    private static final int CRATE_ROWS  = 4;
    public  static final int CRATE_SLOTS = CRATE_COLS * CRATE_ROWS;

    private final ShapeRenderer shapeRenderer;
    private final BitmapFont    font;
    private final Inventory     playerInventory;

    private Inventory crateInventory = null;
    private boolean   visible        = false;
    private boolean   dirty          = false;

    private ItemStack cursorItem       = null;
    private Inventory cursorSourceInv  = null;
    private int       cursorSourceSlot = -1;

    private boolean mouseWasDown = false;

    private int playerX, playerY, playerW, playerH;
    private int crateX,  crateY,  crateW,  crateH;
    private int gapCX;

    private int btnToCrateX,  btnToCrateY;
    private int btnToPlayerX, btnToPlayerY;
    private int btnSortPlayerX, btnSortPlayerY;
    private int btnSortCrateX,  btnSortCrateY;

    public StorageCrateUI(Inventory playerInventory) {
        this.playerInventory = playerInventory;
        this.shapeRenderer   = new ShapeRenderer();

        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter  param = new FreeTypeFontParameter();
        param.size  = 16;
        param.color = Color.WHITE;
        this.font   = gen.generateFont(param);
        gen.dispose();

        updateLayout();
    }

    private void updateLayout() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        playerW = PLAYER_COLS * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING;
        playerH = PLAYER_ROWS * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING + TITLE_H;
        crateW  = CRATE_COLS  * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING;
        crateH  = CRATE_ROWS  * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING + TITLE_H;

        int totalW = playerW + PANEL_GAP + crateW;
        int startX = (sw - totalW) / 2;

        playerX = startX;
        crateX  = startX + playerW + PANEL_GAP;
        playerY = (sh - playerH) / 2;
        crateY  = playerY + (playerH - crateH);

        gapCX = playerX + playerW + PANEL_GAP / 2;

        int midY = playerY + playerH / 2;
        btnToCrateX  = gapCX - BTN_W / 2;
        btnToCrateY  = midY + 6;
        btnToPlayerX = gapCX - BTN_W / 2;
        btnToPlayerY = midY - BTN_H - 6;

        btnSortPlayerX = playerX + playerW - BTN_W - 8;
        btnSortPlayerY = playerY + playerH - TITLE_H + (TITLE_H - BTN_H) / 2;
        btnSortCrateX  = crateX + crateW - BTN_W - 8;
        btnSortCrateY  = crateY + crateH - TITLE_H + (TITLE_H - BTN_H) / 2;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    public void open(Inventory crateInventory) {
        this.crateInventory = crateInventory;
        this.visible        = true;
        this.cursorItem     = null;
        this.mouseWasDown   = false;
        updateLayout();
    }

    public void close() {
        if (cursorItem != null) returnCursorToSource();
        visible        = false;
        crateInventory = null;
    }

    public boolean isVisible() { return visible; }
    public boolean isDirty()   { return dirty;   }
    public void clearDirty()   { dirty = false;  }

    public void handleInput() {
        if (!visible) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        int mx       = Gdx.input.getX();
        int my       = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean down = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        boolean justPressed  = down && !mouseWasDown;
        boolean justReleased = !down && mouseWasDown;

        if (justPressed) {
            if (hitButton(mx, my, btnSortPlayerX, btnSortPlayerY)) {
                sortInventory(playerInventory);
                mouseWasDown = down;
                return;
            }
            if (hitButton(mx, my, btnSortCrateX, btnSortCrateY)) {
                sortInventory(crateInventory);
                mouseWasDown = down;
                return;
            }
            if (hitButton(mx, my, btnToCrateX, btnToCrateY)) {
                transferAll(playerInventory, crateInventory);
                mouseWasDown = down;
                return;
            }
            if (hitButton(mx, my, btnToPlayerX, btnToPlayerY)) {
                transferAll(crateInventory, playerInventory);
                mouseWasDown = down;
                return;
            }
        }

        int     hoveredSlot  = -1;
        boolean hoveredCrate = false;

        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            int[] pos = playerSlotPos(i);
            if (mx >= pos[0] && mx < pos[0] + SLOT_SIZE && my >= pos[1] && my < pos[1] + SLOT_SIZE) {
                hoveredSlot  = i;
                hoveredCrate = false;
                break;
            }
        }

        if (hoveredSlot == -1) {
            for (int i = 0; i < CRATE_SLOTS; i++) {
                int[] pos = crateSlotPos(i);
                if (mx >= pos[0] && mx < pos[0] + SLOT_SIZE && my >= pos[1] && my < pos[1] + SLOT_SIZE) {
                    hoveredSlot  = i;
                    hoveredCrate = true;
                    break;
                }
            }
        }

        if (justPressed && hoveredSlot != -1 && cursorItem == null) {
            pickUp(hoveredSlot, hoveredCrate);
        } else if (justReleased && cursorItem != null) {
            if (hoveredSlot != -1) {
                dropAt(hoveredSlot, hoveredCrate);
            } else {
                returnCursorToSource();
            }
        }

        mouseWasDown = down;
    }

    private boolean hitButton(int mx, int my, int bx, int by) {
        return mx >= bx && mx < bx + BTN_W && my >= by && my < by + BTN_H;
    }

    private void pickUp(int slot, boolean fromCrate) {
        Inventory inv   = fromCrate ? crateInventory : playerInventory;
        ItemStack stack = inv.getSlot(slot);
        if (stack == null) return;

        boolean shiftHeld = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                         || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        cursorSourceInv  = inv;
        cursorSourceSlot = slot;

        if (shiftHeld || stack.getQuantity() == 1) {
            cursorItem = stack;
            inv.setSlot(slot, null);
        } else {
            cursorItem = new ItemStack(stack.getItem(), 1);
            stack.removeQuantity(1);
        }

        dirty = true;
    }

    private void dropAt(int slot, boolean toCrate) {
        Inventory dstInv   = toCrate ? crateInventory : playerInventory;
        ItemStack dstStack = dstInv.getSlot(slot);

        if (dstStack == null) {
            dstInv.setSlot(slot, cursorItem);
            cursorItem = null;
        } else if (dstStack.getItem() == cursorItem.getItem() && dstStack.getItem().isStackable()) {
            int toAdd = Math.min(cursorItem.getQuantity(), dstStack.getRemainingCapacity());
            dstStack.addQuantity(toAdd);
            cursorItem.removeQuantity(toAdd);
            if (cursorItem.isEmpty()) cursorItem = null;
        } else {
            dstInv.setSlot(slot, cursorItem);
            cursorItem       = dstStack;
            cursorSourceInv  = dstInv;
            cursorSourceSlot = slot;
        }

        dirty = true;
    }

    private void returnCursorToSource() {
        if (cursorItem == null) return;

        ItemStack existing = cursorSourceInv.getSlot(cursorSourceSlot);
        if (existing == null) {
            cursorSourceInv.setSlot(cursorSourceSlot, cursorItem);
        } else if (existing.getItem() == cursorItem.getItem() && existing.getItem().isStackable()) {
            int toAdd = Math.min(cursorItem.getQuantity(), existing.getRemainingCapacity());
            existing.addQuantity(toAdd);
            cursorItem.removeQuantity(toAdd);
            if (!cursorItem.isEmpty()) {
                int free = findFreeSlot(cursorSourceInv);
                if (free != -1) cursorSourceInv.setSlot(free, cursorItem);
            }
        } else {
            int free = findFreeSlot(cursorSourceInv);
            if (free != -1) cursorSourceInv.setSlot(free, cursorItem);
        }

        cursorItem       = null;
        cursorSourceSlot = -1;
        cursorSourceInv  = null;
        dirty = true;
    }

    private void transferAll(Inventory src, Inventory dst) {
        for (int i = 0; i < src.getSize(); i++) {
            ItemStack stack = src.getSlot(i);
            if (stack == null) continue;

            if (stack.getItem().isStackable()) {
                for (int j = 0; j < dst.getSize() && stack.getQuantity() > 0; j++) {
                    ItemStack dstSlot = dst.getSlot(j);
                    if (dstSlot != null && dstSlot.getItem() == stack.getItem() && !dstSlot.isFull()) {
                        int toAdd = Math.min(stack.getQuantity(), dstSlot.getRemainingCapacity());
                        dstSlot.addQuantity(toAdd);
                        stack.removeQuantity(toAdd);
                    }
                }
            }

            if (stack.getQuantity() > 0) {
                int free = findFreeSlot(dst);
                if (free != -1) {
                    dst.setSlot(free, stack);
                    src.setSlot(i, null);
                }
            } else {
                src.setSlot(i, null);
            }
        }
        dirty = true;
    }

    private void sortInventory(Inventory inv) {
        Map<Item, Integer> totals = new HashMap<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack s = inv.getSlot(i);
            if (s == null) continue;
            totals.merge(s.getItem(), s.getQuantity(), Integer::sum);
            inv.setSlot(i, null);
        }

        List<Map.Entry<Item, Integer>> entries = new ArrayList<>(totals.entrySet());
        entries.sort(Comparator.comparingInt(e -> e.getKey().ordinal()));

        int slot = 0;
        for (Map.Entry<Item, Integer> entry : entries) {
            Item item      = entry.getKey();
            int  remaining = entry.getValue();
            while (remaining > 0 && slot < inv.getSize()) {
                int toPlace = Math.min(remaining, item.getMaxStackSize());
                inv.setSlot(slot, new ItemStack(item, toPlace));
                remaining -= toPlace;
                slot++;
            }
        }

        dirty = true;
    }

    private int findFreeSlot(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getSlot(i) == null) return i;
        }
        return -1;
    }

    public void render(SpriteBatch batch) {
        if (!visible || crateInventory == null) return;

        batch.end();

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.95f);
        shapeRenderer.rect(playerX, playerY, playerW, playerH);
        shapeRenderer.rect(crateX,  crateY,  crateW,  crateH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.45f, 1f);
        shapeRenderer.rect(playerX, playerY, playerW, playerH);
        shapeRenderer.rect(crateX,  crateY,  crateW,  crateH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 1f);
        shapeRenderer.line(playerX + 6, playerY + playerH - TITLE_H, playerX + playerW - 6, playerY + playerH - TITLE_H);
        shapeRenderer.line(crateX  + 6, crateY  + crateH  - TITLE_H, crateX  + crateW  - 6, crateY  + crateH  - TITLE_H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            int[] pos = playerSlotPos(i);
            shapeRenderer.setColor(0.38f, 0.38f, 0.42f, 1f);
            shapeRenderer.rect(pos[0], pos[1], SLOT_SIZE, SLOT_SIZE);
        }
        for (int i = 0; i < CRATE_SLOTS; i++) {
            int[] pos = crateSlotPos(i);
            shapeRenderer.setColor(0.38f, 0.38f, 0.42f, 1f);
            shapeRenderer.rect(pos[0], pos[1], SLOT_SIZE, SLOT_SIZE);
        }
        shapeRenderer.end();

        drawButton(btnSortPlayerX, btnSortPlayerY, false);
        drawButton(btnSortCrateX,  btnSortCrateY,  false);
        drawButton(btnToCrateX,    btnToCrateY,    false);
        drawButton(btnToPlayerX,   btnToPlayerY,   false);

        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(0.88f, 0.88f, 1f, 1f);
        font.draw(batch, "Inventory",     playerX + 10, playerY + playerH - 10);
        font.draw(batch, "Storage Crate", crateX  + 10, crateY  + crateH  - 10);

        font.getData().setScale(1.0f);
        font.setColor(0.85f, 0.85f, 0.95f, 1f);
        drawButtonLabel(batch, "Sort", btnSortPlayerX, btnSortPlayerY);
        drawButtonLabel(batch, "Sort", btnSortCrateX,  btnSortCrateY);
        drawButtonLabel(batch, ">> All", btnToCrateX,  btnToCrateY);
        drawButtonLabel(batch, "<< All", btnToPlayerX, btnToPlayerY);

        font.getData().setScale(1.2f);
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack     stack = playerInventory.getSlot(i);
            if (stack == null) continue;
            int[]         pos   = playerSlotPos(i);
            TextureRegion tex   = ItemTextureCache.getTexture(stack.getItem());
            if (tex != null) batch.draw(tex, pos[0] + 4, pos[1] + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);
            if (stack.getQuantity() > 1) {
                font.setColor(Color.WHITE);
                font.draw(batch, String.valueOf(stack.getQuantity()), pos[0] + SLOT_SIZE - 20, pos[1] + 16);
            }
        }

        for (int i = 0; i < CRATE_SLOTS; i++) {
            ItemStack     stack = crateInventory.getSlot(i);
            if (stack == null) continue;
            int[]         pos   = crateSlotPos(i);
            TextureRegion tex   = ItemTextureCache.getTexture(stack.getItem());
            if (tex != null) batch.draw(tex, pos[0] + 4, pos[1] + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);
            if (stack.getQuantity() > 1) {
                font.setColor(Color.WHITE);
                font.draw(batch, String.valueOf(stack.getQuantity()), pos[0] + SLOT_SIZE - 20, pos[1] + 16);
            }
        }

        if (cursorItem != null) {
            int mx   = Gdx.input.getX();
            int my   = Gdx.graphics.getHeight() - Gdx.input.getY();
            int half = SLOT_SIZE / 2;
            TextureRegion tex = ItemTextureCache.getTexture(cursorItem.getItem());
            batch.setColor(1f, 1f, 1f, 0.85f);
            if (tex != null) batch.draw(tex, mx - half, my - half, SLOT_SIZE, SLOT_SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
            if (cursorItem.getQuantity() > 1) {
                font.setColor(Color.WHITE);
                font.getData().setScale(1.2f);
                font.draw(batch, String.valueOf(cursorItem.getQuantity()), mx - half + SLOT_SIZE - 20, my - half + 16);
            }
        }

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }

    private void drawButton(int bx, int by, boolean hovered) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hovered ? 0.28f : 0.18f, hovered ? 0.28f : 0.18f, hovered ? 0.38f : 0.26f, 0.95f);
        shapeRenderer.rect(bx, by, BTN_W, BTN_H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.45f, 0.45f, 0.65f, 1f);
        shapeRenderer.rect(bx, by, BTN_W, BTN_H);
        shapeRenderer.end();
    }

    private void drawButtonLabel(SpriteBatch batch, String label, int bx, int by) {
        font.draw(batch, label, bx + 6, by + BTN_H - 8);
    }

    public void resize(int width, int height) {
        updateLayout();
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }

    private int[] playerSlotPos(int i) {
        int col       = i % PLAYER_COLS;
        int row       = i / PLAYER_COLS;
        int x         = playerX + SLOT_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
        int topOfGrid = playerY + playerH - TITLE_H;
        int y         = topOfGrid - SLOT_PADDING - row * (SLOT_SIZE + SLOT_PADDING) - SLOT_SIZE;
        return new int[]{ x, y };
    }

    private int[] crateSlotPos(int i) {
        int col       = i % CRATE_COLS;
        int row       = i / CRATE_COLS;
        int x         = crateX + SLOT_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
        int topOfGrid = crateY + crateH - TITLE_H;
        int y         = topOfGrid - SLOT_PADDING - row * (SLOT_SIZE + SLOT_PADDING) - SLOT_SIZE;
        return new int[]{ x, y };
    }
}