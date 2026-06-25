package com.factory.game.World;

import java.util.EnumSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemTextureCache;


/**
 * Inventory-style item picker used to configure the whitelist on a
 * {@link PlacedObject.Type#FILTER_ITEM_PIPE}. Click an item icon to toggle
 * whether the filter pipe is allowed to pull that item in on its INPUT ports.
 * An empty selection means "allow everything" (matches FilterPipeUI's
 * liquid-filter convention).
 */
public class ItemFilterPipeUI {

    private static final int   COLS      = 12;
    private static final float SLOT_SIZE = 40f;
    private static final float SLOT_PAD  = 4f;
    private static final float PANEL_PAD = 16f;
    private static final float HEADER_H  = 64f;

    private boolean      visible    = false;
    private PlacedObject filterPipe = null;
    private boolean      dirty      = false;
    private boolean      justOpened = false;

    private final BitmapFont    font;
    private final TextureRegion whitePixel;

    private int screenW, screenH;

    private final Item[] allItems = Item.values();
    private final int    rows     = (allItems.length + COLS - 1) / COLS;

    public ItemFilterPipeUI() {
        font       = new BitmapFont();
        whitePixel = ObjectSpriteCache.whitePixel;
        screenW    = Gdx.graphics.getWidth();
        screenH    = Gdx.graphics.getHeight();
    }


    public void open(PlacedObject pipe) {
        this.filterPipe = pipe;
        this.visible    = true;
        this.dirty      = false;
        this.justOpened = true;
    }

    public void close() {
        visible = false;
    }

    public boolean isVisible()  { return visible;    }
    public boolean isDirty()    { return dirty;      }
    public void    clearDirty() { dirty = false;     }

    public PlacedObject getFilterPipe() { return filterPipe; }


    public void resize(int w, int h) {
        screenW = w;
        screenH = h;
    }


    private float panelW() {
        return PANEL_PAD * 2 + COLS * (SLOT_SIZE + SLOT_PAD) - SLOT_PAD;
    }

    private float panelH() {
        return PANEL_PAD * 2 + HEADER_H + rows * (SLOT_SIZE + SLOT_PAD) - SLOT_PAD;
    }

    private float[] slotPos(int index, float panelX, float panelY, float panelH) {
        int col = index % COLS;
        int row = index / COLS;
        float x = panelX + PANEL_PAD + col * (SLOT_SIZE + SLOT_PAD);
        float top = panelY + panelH - HEADER_H - PANEL_PAD;
        float y = top - (row + 1) * (SLOT_SIZE + SLOT_PAD) + SLOT_PAD;
        return new float[]{ x, y };
    }


    public void handleInput() {
        if (!visible || filterPipe == null) return;

        if (justOpened) {
            justOpened = false;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)
         || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;

        float panelW = panelW();
        float panelH = panelH();
        float panelX = (screenW - panelW) / 2f;
        float panelY = (screenH - panelH) / 2f;

        float mx = Gdx.input.getX();
        float my = screenH - Gdx.input.getY();

        if (mx < panelX || mx > panelX + panelW || my < panelY || my > panelY + panelH) {
            close();
            return;
        }

        Set<Item> current = filterPipe.getAllowedItemTypes();
        EnumSet<Item> working = (current == null || current.isEmpty())
                ? EnumSet.noneOf(Item.class)
                : EnumSet.copyOf(current);

        for (int i = 0; i < allItems.length; i++) {
            float[] pos = slotPos(i, panelX, panelY, panelH);
            if (mx >= pos[0] && mx <= pos[0] + SLOT_SIZE
             && my >= pos[1] && my <= pos[1] + SLOT_SIZE) {
                Item item = allItems[i];
                if (working.contains(item)) working.remove(item);
                else                         working.add(item);
                filterPipe.setAllowedItemTypes(working.isEmpty() ? null : working);
                dirty = true;
                return;
            }
        }
    }


    public void render(SpriteBatch batch) {
        if (!visible || filterPipe == null) return;

        float panelW = panelW();
        float panelH = panelH();
        float panelX = (screenW - panelW) / 2f;
        float panelY = (screenH - panelH) / 2f;

        drawRect(batch, panelX,     panelY,     panelW,     panelH,     0.10f, 0.10f, 0.16f, 0.96f);
        drawRect(batch, panelX + 1, panelY + 1, panelW - 2, panelH - 2, 0.30f, 0.30f, 0.45f, 0.30f);

        float titleY = panelY + panelH - PANEL_PAD;
        font.setColor(0.25f, 0.90f, 0.75f, 1f);
        font.draw(batch, "Filter Item Pipe Configuration", panelX + PANEL_PAD, titleY);

        font.setColor(0.60f, 0.60f, 0.65f, 1f);
        font.draw(batch, "Click items to toggle which may be pulled in.  (none = allow all)",
                panelX + PANEL_PAD, titleY - 16f);
        font.draw(batch, "Press  E  to close.", panelX + PANEL_PAD, titleY - 30f);

        Set<Item> current = filterPipe.getAllowedItemTypes();

        for (int i = 0; i < allItems.length; i++) {
            Item    item = allItems[i];
            boolean on   = (current != null && current.contains(item));
            float[] pos  = slotPos(i, panelX, panelY, panelH);

            drawRect(batch, pos[0], pos[1], SLOT_SIZE, SLOT_SIZE,
                    on ? 0.18f : 0.20f, on ? 0.45f : 0.20f, on ? 0.28f : 0.25f, 1f);

            TextureRegion tex = ItemTextureCache.getTexture(item);
            if (tex != null) {
                batch.setColor(1f, 1f, 1f, on ? 1f : 0.40f);
                batch.draw(tex, pos[0] + 3, pos[1] + 3, SLOT_SIZE - 6, SLOT_SIZE - 6);
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }

        if (current == null || current.isEmpty()) {
            float hintY = panelY + PANEL_PAD * 0.8f;
            font.setColor(0.40f, 0.85f, 0.55f, 1f);
            font.draw(batch, "No filter active, all items may be pulled in",
                    panelX + PANEL_PAD, hintY + 14f);
        }

        font.setColor(Color.WHITE);
    }


    public void dispose() {
        font.dispose();
    }

    private void drawRect(SpriteBatch batch, float x, float y, float w, float h,
                          float r, float g, float b, float a) {
        batch.setColor(r, g, b, a);
        batch.draw(whitePixel, x, y, w, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}