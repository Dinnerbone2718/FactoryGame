package com.factory.game.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;


public class InventoryUI extends InputAdapter {

    private static final int SLOT_SIZE    = 48;
    private static final int SLOT_PADDING = 4;
    private static final int COLS         = 6;
    private static final int ROWS         = 8;

    private static final int RECIPE_HEIGHT  = 60;
    private static final int RECIPE_SPACING = 4;
    private static final int REC_PANEL_W    = 680;
    private static final int SCROLLBAR_W    = 10;
    private static final int SCROLLBAR_PAD  = 4;

    private static final int PANEL_GAP = 14;
    private static final int TITLE_H   = 40;

    private static final int BTN_W = 56;
    private static final int BTN_H = 26;

    private enum SortMode { DEFAULT, CRAFTABLE_FIRST, NAME_AZ }
    private SortMode recipeSort = SortMode.DEFAULT;

    private final ShapeRenderer shapeRenderer;
    private final BitmapFont    font;
    private final Inventory     inventory;

    private boolean visible       = false;
    private int     hoveredRecipe = -1;

    private ItemStack cursorItem       = null;
    private int       cursorSourceSlot = -1;
    private boolean   mouseWasDown     = false;

    private int lastHeldSlot = -1;
    private int selectedSlot = -1;

    private int invX, invY, invW, invH;
    private int recX, recY, recH;
    private int clipX, clipY, clipW, clipH;

    private int tabW;
    private int tabDefaultX,   tabDefaultY;
    private int tabCraftableX, tabCraftableY;
    private int tabNameAZX,    tabNameAZY;
    private int btnSortInvX,   btnSortInvY;

    private int scrollOffset = 0;
    private int maxScroll    = 0;


    public InventoryUI(Inventory inventory) {
        this.inventory     = inventory;
        this.shapeRenderer = new ShapeRenderer();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size  = 16;
        parameter.color = Color.WHITE;
        this.font = generator.generateFont(parameter);
        generator.dispose();

        updateLayout();
    }


    private void updateLayout() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        invW = COLS * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING;
        invH = ROWS * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING + TITLE_H;

        recH = Math.max(invH + 20, (int)(sh * 0.85f));

        int totalW = invW + PANEL_GAP + REC_PANEL_W;
        int startX = (sw - totalW) / 2;

        recX = startX + invW + PANEL_GAP;
        recY = (sh - recH) / 2;
        invX = startX;
        invY = recY + recH - invH;

        clipX = recX + 8;
        clipY = recY + 8;
        clipW = REC_PANEL_W - SCROLLBAR_W - SCROLLBAR_PAD * 2 - 16;
        clipH = recH - TITLE_H - 16;

        tabW = REC_PANEL_W / 3;
        int tabBarY   = recY + recH - TITLE_H;
        tabDefaultX   = recX;
        tabCraftableX = recX + tabW;
        tabNameAZX    = recX + tabW * 2;
        tabDefaultY = tabCraftableY = tabNameAZY = tabBarY;

        btnSortInvX = invX + invW - BTN_W - 8;
        btnSortInvY = invY + invH - TITLE_H + (TITLE_H - BTN_H) / 2;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }


    public void toggle() {
        visible = !visible;
        if (visible) {
            updateLayout();
            scrollOffset = 0;
            mouseWasDown = false;
            if (cursorItem != null) returnCursorToSource();
            Gdx.input.setInputProcessor(this);
        } else {
            if (cursorItem != null) returnCursorToSource();
            Gdx.input.setInputProcessor(null);
        }
    }

    public boolean isVisible()    { return visible;      }
    public int  getSelectedSlot() { return lastHeldSlot; }
    public void clearSelection()  { lastHeldSlot = -1;   }


    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (!visible) return false;
        scrollOffset += (int)(amountY * 45);
        clampScroll();
        return true;
    }

    private void clampScroll() {
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }


    public void handleInput() {
        if (!visible) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            toggle();
            return;
        }

        int     mx   = Gdx.input.getX();
        int     my   = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean down = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        boolean justPressed  = down && !mouseWasDown;
        boolean justReleased = !down && mouseWasDown;

        if (justPressed) {
            if (hitArea(mx, my, tabDefaultX,   tabDefaultY,   tabW, TITLE_H)) {
                recipeSort = SortMode.DEFAULT;         scrollOffset = 0; mouseWasDown = down; return;
            }
            if (hitArea(mx, my, tabCraftableX, tabCraftableY, tabW, TITLE_H)) {
                recipeSort = SortMode.CRAFTABLE_FIRST; scrollOffset = 0; mouseWasDown = down; return;
            }
            if (hitArea(mx, my, tabNameAZX,    tabNameAZY,    tabW, TITLE_H)) {
                recipeSort = SortMode.NAME_AZ;         scrollOffset = 0; mouseWasDown = down; return;
            }
        }

        if (justPressed && hitArea(mx, my, btnSortInvX, btnSortInvY, BTN_W, BTN_H)) {
            sortInventory();
            mouseWasDown = down;
            return;
        }

        int hoveredSlot = -1;
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            int[] pos = slotScreenPos(i);
            if (mx >= pos[0] && mx < pos[0] + SLOT_SIZE && my >= pos[1] && my < pos[1] + SLOT_SIZE) {
                hoveredSlot = i;
                break;
            }
        }

        if (justPressed && hoveredSlot != -1 && cursorItem == null) {
            pickUp(hoveredSlot);
        } else if (justReleased && cursorItem != null) {
            if (hoveredSlot != -1) {
                dropAt(hoveredSlot);
            } else {
                returnCursorToSource();
            }
        }

        List<CraftingRecipe> recipes = getSortedRecipes();
        hoveredRecipe = -1;

        if (cursorItem == null && mx >= clipX && mx <= clipX + clipW) {
            for (int i = 0; i < recipes.size(); i++) {
                int ry = recipeRowY(i);
                if (ry + RECIPE_HEIGHT < clipY || ry > clipY + clipH) continue;
                if (my >= ry && my <= ry + RECIPE_HEIGHT) {
                    hoveredRecipe = i;
                    if (justPressed) {
                        CraftingRecipe recipe = recipes.get(i);
                        if (recipe.canCraft(inventory)) recipe.craft(inventory);
                    }
                    break;
                }
            }
        }

        mouseWasDown = down;
    }


    private void pickUp(int slot) {
        ItemStack stack = inventory.getSlot(slot);
        if (stack == null) return;

        boolean shiftHeld = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                         || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        cursorSourceSlot = slot;
        lastHeldSlot     = slot;
        selectedSlot     = slot;

        if (shiftHeld || stack.getQuantity() == 1) {
            cursorItem = stack;
            inventory.setSlot(slot, null);
        } else {
            cursorItem = new ItemStack(stack.getItem(), 1);
            stack.removeQuantity(1);
        }
    }

    private void dropAt(int slot) {
        ItemStack dstStack = inventory.getSlot(slot);

        if (dstStack == null) {
            inventory.setSlot(slot, cursorItem);
            cursorItem = null;
        } else if (dstStack.getItem() == cursorItem.getItem() && dstStack.getItem().isStackable()) {
            int toAdd = Math.min(cursorItem.getQuantity(), dstStack.getRemainingCapacity());
            dstStack.addQuantity(toAdd);
            cursorItem.removeQuantity(toAdd);
            if (cursorItem.isEmpty()) cursorItem = null;
        } else {
            inventory.setSlot(slot, cursorItem);
            cursorItem       = dstStack;
            cursorSourceSlot = slot;
        }
    }

    private void returnCursorToSource() {
        if (cursorItem == null) return;

        ItemStack existing = inventory.getSlot(cursorSourceSlot);
        if (existing == null) {
            inventory.setSlot(cursorSourceSlot, cursorItem);
        } else if (existing.getItem() == cursorItem.getItem() && existing.getItem().isStackable()) {
            int toAdd = Math.min(cursorItem.getQuantity(), existing.getRemainingCapacity());
            existing.addQuantity(toAdd);
            cursorItem.removeQuantity(toAdd);
            if (!cursorItem.isEmpty()) {
                int free = findFreeSlot();
                if (free != -1) inventory.setSlot(free, cursorItem);
            }
        } else {
            int free = findFreeSlot();
            if (free != -1) inventory.setSlot(free, cursorItem);
        }

        cursorItem       = null;
        cursorSourceSlot = -1;
    }

    private int findFreeSlot() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getSlot(i) == null) return i;
        }
        return -1;
    }

    private void sortInventory() {
        Map<Item, Integer> totals = new HashMap<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack s = inventory.getSlot(i);
            if (s == null) continue;
            totals.merge(s.getItem(), s.getQuantity(), Integer::sum);
            inventory.setSlot(i, null);
        }

        List<Map.Entry<Item, Integer>> entries = new ArrayList<>(totals.entrySet());
        entries.sort(Comparator.comparingInt(e -> e.getKey().ordinal()));

        int slot = 0;
        for (Map.Entry<Item, Integer> entry : entries) {
            Item item      = entry.getKey();
            int  remaining = entry.getValue();
            while (remaining > 0 && slot < inventory.getSize()) {
                int toPlace = Math.min(remaining, item.getMaxStackSize());
                inventory.setSlot(slot, new ItemStack(item, toPlace));
                remaining -= toPlace;
                slot++;
            }
        }
    }

    private boolean hitArea(int mx, int my, int bx, int by, int bw, int bh) {
        return mx >= bx && mx < bx + bw && my >= by && my < by + bh;
    }


    private List<CraftingRecipe> getSortedRecipes() {
        List<CraftingRecipe> list = new ArrayList<>(CraftingManager.getAllRecipes());
        switch (recipeSort) {
            case CRAFTABLE_FIRST:
                list.sort((a, b) -> {
                    boolean ca = a.canCraft(inventory);
                    boolean cb = b.canCraft(inventory);
                    if (ca == cb) return 0;
                    return ca ? -1 : 1;
                });
                break;
            case NAME_AZ:
                list.sort(Comparator.comparing(CraftingRecipe::getName));
                break;
            default:
                break;
        }
        return list;
    }


    public void render(SpriteBatch batch) {
        if (!visible) {
            renderHUD(batch);
            return;
        }

        List<CraftingRecipe> recipes = getSortedRecipes();
        int totalListH = recipes.size() * (RECIPE_HEIGHT + RECIPE_SPACING) + RECIPE_SPACING;
        maxScroll = Math.max(0, totalListH - clipH);
        clampScroll();

        batch.end();

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.95f);
        shapeRenderer.rect(invX, invY, invW, invH);
        shapeRenderer.rect(recX, recY, REC_PANEL_W, recH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.45f, 1f);
        shapeRenderer.rect(invX, invY, invW, invH);
        shapeRenderer.rect(recX, recY, REC_PANEL_W, recH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 1f);
        shapeRenderer.line(invX + 6, invY + invH - TITLE_H, invX + invW - 6, invY + invH - TITLE_H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            int[] pos    = slotScreenPos(i);
            boolean isSrc = (i == cursorSourceSlot && cursorItem != null);
            shapeRenderer.setColor(isSrc ? 0.5f : 0.38f,
                                   isSrc ? 0.42f : 0.38f,
                                   isSrc ? 0.1f  : 0.42f, 1f);
            shapeRenderer.rect(pos[0], pos[1], SLOT_SIZE, SLOT_SIZE);
        }
        shapeRenderer.end();

        if (invY > recY) {
            int gapPad  = 8;
            int gapPanX = invX + gapPad;
            int gapPanY = recY + gapPad;
            int gapPanW = invW - gapPad * 2;
            int gapPanH = invY - recY - gapPad * 2;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.92f);
            shapeRenderer.rect(gapPanX, gapPanY, gapPanW, gapPanH);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            ItemStack display = displayStack();
            shapeRenderer.setColor(display != null ? 1f : 0.3f,
                                   display != null ? 0.9f : 0.3f,
                                   display != null ? 0.2f : 0.45f,
                                   display != null ? 0.9f : 1f);
            shapeRenderer.rect(gapPanX, gapPanY, gapPanW, gapPanH);
            shapeRenderer.end();
        }

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(clipX, clipY, clipW, clipH);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < recipes.size(); i++) {
            int ry = recipeRowY(i);
            if (ry + RECIPE_HEIGHT < clipY || ry > clipY + clipH) continue;

            CraftingRecipe recipe   = recipes.get(i);
            boolean        canCraft = recipe.canCraft(inventory);
            boolean        hovered  = (i == hoveredRecipe);

            if (hovered) {
                shapeRenderer.setColor(canCraft ? 0.10f : 0.20f,
                                       canCraft ? 0.20f : 0.10f,
                                       0.08f, 0.92f);
            } else {
                float base = (i % 2 == 0) ? 0.11f : 0.09f;
                shapeRenderer.setColor(base, base, base + 0.05f, 0.80f);
            }
            shapeRenderer.rect(clipX + 1, ry + 1, clipW - 2, RECIPE_HEIGHT - 2);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < recipes.size(); i++) {
            int ry = recipeRowY(i);
            if (ry + RECIPE_HEIGHT < clipY || ry > clipY + clipH) continue;

            CraftingRecipe recipe   = recipes.get(i);
            boolean        canCraft = recipe.canCraft(inventory);
            boolean        hovered  = (i == hoveredRecipe);

            if (hovered) {
                shapeRenderer.setColor(canCraft ? 0.3f : 0.65f, canCraft ? 0.65f : 0.3f, 0.2f, 1f);
            } else {
                shapeRenderer.setColor(0.22f, 0.22f, 0.32f, 1f);
            }
            shapeRenderer.rect(clipX, ry, clipW, RECIPE_HEIGHT);
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        if (maxScroll > 0) {
            int sbX = recX + REC_PANEL_W - SCROLLBAR_W - SCROLLBAR_PAD;
            int sbY = clipY;
            int sbH = clipH;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.14f, 0.14f, 0.2f, 0.85f);
            shapeRenderer.rect(sbX, sbY, SCROLLBAR_W, sbH);
            shapeRenderer.end();

            float ratio  = (float) clipH / (clipH + maxScroll);
            int   thumbH = Math.max(28, (int)(sbH * ratio));
            float travel = sbH - thumbH;
            int   thumbY = sbY + (int)(travel - travel * scrollOffset / maxScroll);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.48f, 0.48f, 0.65f, 0.95f);
            shapeRenderer.rect(sbX + 2, thumbY, SCROLLBAR_W - 4, thumbH);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1f);
            shapeRenderer.rect(sbX + 2, thumbY, SCROLLBAR_W - 4, thumbH);
            shapeRenderer.end();
        }

        drawTab(tabDefaultX,   tabDefaultY,   recipeSort == SortMode.DEFAULT);
        drawTab(tabCraftableX, tabCraftableY, recipeSort == SortMode.CRAFTABLE_FIRST);
        drawTab(tabNameAZX,    tabNameAZY,    recipeSort == SortMode.NAME_AZ);

        drawSmallBtn(btnSortInvX, btnSortInvY);

        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(0.88f, 0.88f, 1f, 1f);
        font.draw(batch, "Inventory", invX + 10, invY + invH - 10);

        font.getData().setScale(0.95f);
        drawTabLabel(batch, "Default",   tabDefaultX,   tabDefaultY,   recipeSort == SortMode.DEFAULT);
        drawTabLabel(batch, "Craftable", tabCraftableX, tabCraftableY, recipeSort == SortMode.CRAFTABLE_FIRST);
        drawTabLabel(batch, "A - Z",     tabNameAZX,    tabNameAZY,    recipeSort == SortMode.NAME_AZ);

        font.getData().setScale(0.9f);
        font.setColor(0.85f, 0.85f, 0.95f, 1f);
        font.draw(batch, "Sort", btnSortInvX + 10, btnSortInvY + BTN_H - 7);

        if (invY > recY) {
            int gapPad   = 8;
            int gapPanX  = invX + gapPad;
            int gapPanY  = recY + gapPad;
            int gapPanH  = invY - recY - gapPad * 2;
            int iconSize = Math.min(48, gapPanH - 8);
            int iconX    = gapPanX + 8;
            int iconY    = gapPanY + (gapPanH - iconSize) / 2;
            int textX    = iconX + iconSize + 10;
            int textMidY = gapPanY + gapPanH / 2;

            ItemStack display = displayStack();
            if (display != null) {
                TextureRegion selTex = ItemTextureCache.getTexture(display.getItem());
                if (selTex != null) batch.draw(selTex, iconX, iconY, iconSize, iconSize);

                font.getData().setScale(1.2f);
                font.setColor(1f, 0.95f, 0.6f, 1f);
                font.draw(batch, display.getItem().getDisplayName(), textX, textMidY + 18);

                font.getData().setScale(1.1f);
                font.setColor(0.8f, 0.8f, 0.9f, 1f);
                font.draw(batch, "x" + display.getQuantity(), textX, textMidY - 4);
            } else {
                font.getData().setScale(1.1f);
                font.setColor(0.35f, 0.35f, 0.45f, 0.85f);
                font.draw(batch, "No item selected", gapPanX + 12, textMidY + 8);
            }
        }

        font.getData().setScale(1.2f);
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = inventory.getSlot(i);
            if (stack == null) continue;

            int[]         pos = slotScreenPos(i);
            TextureRegion tex = ItemTextureCache.getTexture(stack.getItem());
            if (tex != null) batch.draw(tex, pos[0] + 4, pos[1] + 4, SLOT_SIZE - 8, SLOT_SIZE - 8);

            if (stack.getQuantity() > 1) {
                font.setColor(Color.WHITE);
                font.draw(batch, String.valueOf(stack.getQuantity()), pos[0] + SLOT_SIZE - 20, pos[1] + 16);
            }
        }

        batch.flush();
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(clipX, clipY, clipW, clipH);

        final int NAME_W  = 192;
        final int ICON_SZ = 28;
        final int CELL_W  = 50;
        final int ARROW_W = 32;

        for (int i = 0; i < recipes.size(); i++) {
            int ry = recipeRowY(i);
            if (ry + RECIPE_HEIGHT < clipY - 5 || ry > clipY + clipH + 5) continue;

            CraftingRecipe recipe   = recipes.get(i);
            boolean        canCraft = recipe.canCraft(inventory);
            boolean        hovered  = (i == hoveredRecipe);

            int rowMidY = ry + RECIPE_HEIGHT / 2;
            int iconY   = rowMidY - ICON_SZ / 2;
            int textY   = rowMidY + 6;

            Color nameColor;
            if (hovered) {
                nameColor = canCraft ? new Color(0.65f, 1f, 0.65f, 1f) : new Color(1f, 0.70f, 0.35f, 1f);
            } else {
                nameColor = canCraft ? new Color(0.38f, 1f, 0.38f, 1f) : new Color(1f, 0.38f, 0.38f, 1f);
            }
            font.getData().setScale(1.05f);
            font.setColor(nameColor);
            font.draw(batch, recipe.getName(), clipX + 8, textY + 2);

            int curX = clipX + NAME_W;
            font.getData().setScale(1.0f);

            for (var entry : recipe.getInputs().entrySet()) {
                Item          item = entry.getKey();
                int           qty  = entry.getValue();
                TextureRegion tex  = ItemTextureCache.getTexture(item);
                if (tex != null) batch.draw(tex, curX, iconY, ICON_SZ, ICON_SZ);
                font.setColor(inventory.hasItem(item, qty)
                        ? new Color(0.88f, 0.88f, 0.88f, 1f)
                        : new Color(0.95f, 0.35f, 0.35f, 1f));
                font.draw(batch, "x" + qty, curX + ICON_SZ + 2, textY);
                curX += CELL_W;
            }

            font.getData().setScale(1.2f);
            font.setColor(0.50f, 0.50f, 0.68f, 1f);
            font.draw(batch, "=>", curX + 2, textY + 2);
            curX += ARROW_W;

            TextureRegion outTex = ItemTextureCache.getTexture(recipe.getOutputItem());
            if (outTex != null) batch.draw(outTex, curX, iconY, ICON_SZ, ICON_SZ);
            font.getData().setScale(1.0f);
            font.setColor(new Color(1f, 0.95f, 0.55f, 1f));
            font.draw(batch, "x" + recipe.getOutputQuantity(), curX + ICON_SZ + 2, textY);
        }

        batch.flush();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        if (maxScroll > 0 && scrollOffset < maxScroll) {
            font.getData().setScale(1.0f);
            font.setColor(0.5f, 0.5f, 0.6f, 0.8f);
            font.draw(batch, "Scroll for more", recX + REC_PANEL_W / 2 - 60, recY + 20);
        }

        if (cursorItem != null) {
            int   cmx  = Gdx.input.getX();
            int   cmy  = Gdx.graphics.getHeight() - Gdx.input.getY();
            int   half = SLOT_SIZE / 2;
            TextureRegion tex = ItemTextureCache.getTexture(cursorItem.getItem());
            batch.setColor(1f, 1f, 1f, 0.85f);
            if (tex != null) batch.draw(tex, cmx - half, cmy - half, SLOT_SIZE, SLOT_SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
            if (cursorItem.getQuantity() > 1) {
                font.getData().setScale(1.2f);
                font.setColor(Color.WHITE);
                font.draw(batch, String.valueOf(cursorItem.getQuantity()),
                        cmx - half + SLOT_SIZE - 20, cmy - half + 16);
            }
        }

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }


    private void drawTab(int bx, int by, boolean active) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(active ? 0.20f : 0.13f,
                               active ? 0.32f : 0.13f,
                               active ? 0.52f : 0.20f, 1f);
        shapeRenderer.rect(bx, by, tabW, TITLE_H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(active ? 0.50f : 0.32f,
                               active ? 0.65f : 0.32f,
                               active ? 0.90f : 0.48f, 1f);
        shapeRenderer.rect(bx, by, tabW, TITLE_H);
        shapeRenderer.end();
    }

    private void drawTabLabel(SpriteBatch batch, String label, int bx, int by, boolean active) {
        font.setColor(active ? new Color(0.80f, 0.95f, 1f, 1f)
                             : new Color(0.60f, 0.60f, 0.76f, 1f));
        font.draw(batch, label, bx, by + TITLE_H - 12, tabW, Align.center, false);
    }

    private void drawSmallBtn(int bx, int by) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.18f, 0.18f, 0.26f, 0.95f);
        shapeRenderer.rect(bx, by, BTN_W, BTN_H);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.45f, 0.45f, 0.65f, 1f);
        shapeRenderer.rect(bx, by, BTN_W, BTN_H);
        shapeRenderer.end();
    }


    public void renderHUD(SpriteBatch batch) {
        ItemStack stack = lastHeldSlot >= 0 ? inventory.getSlot(lastHeldSlot) : null;
        if (stack == null) { lastHeldSlot = -1; return; }

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        int panelW = 220, panelH = 64, margin = 16;
        int panelX = sw - panelW - margin;
        int panelY = margin;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.92f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.9f, 0.2f, 0.9f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        batch.begin();

        int iconSize = 48;
        int iconX    = panelX + 8;
        int iconY    = panelY + (panelH - iconSize) / 2;
        TextureRegion tex = ItemTextureCache.getTexture(stack.getItem());
        if (tex != null) batch.draw(tex, iconX, iconY, iconSize, iconSize);

        int textX = iconX + iconSize + 10;
        font.getData().setScale(1.2f);
        font.setColor(1f, 0.95f, 0.6f, 1f);
        font.draw(batch, stack.getItem().getDisplayName(), textX, panelY + panelH - 12);

        font.getData().setScale(1.1f);
        font.setColor(0.8f, 0.8f, 0.9f, 1f);
        font.draw(batch, "x" + stack.getQuantity(), textX, panelY + 22);

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }


    private ItemStack displayStack() {
        if (cursorItem != null) return cursorItem;
        if (lastHeldSlot >= 0) return inventory.getSlot(lastHeldSlot);
        return null;
    }

    public void resize(int width, int height) {
        updateLayout();
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }


    private int[] slotScreenPos(int i) {
        int col       = i % COLS;
        int row       = i / COLS;
        int x         = invX + SLOT_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
        int topOfGrid = invY + invH - TITLE_H;
        int y         = topOfGrid - SLOT_PADDING - row * (SLOT_SIZE + SLOT_PADDING) - SLOT_SIZE;
        return new int[]{ x, y };
    }

    private int recipeRowY(int i) {
        return clipY + clipH - RECIPE_SPACING
               - i * (RECIPE_HEIGHT + RECIPE_SPACING)
               - RECIPE_HEIGHT
               + scrollOffset;
    }
}