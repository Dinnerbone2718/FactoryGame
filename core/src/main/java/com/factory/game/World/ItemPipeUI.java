package com.factory.game.World;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.Items.ItemStack;
import com.factory.game.Main;

public class ItemPipeUI {

    private static final int PANEL_W  = 300;
    private static final int PANEL_H  = 300;
    private static final int BTN_SIZE = 64;
    private static final int GAP      = BTN_SIZE + 20;
    private static final int CTR_SIZE = 56;

    private static final float OVERLAY_ARROW_SZ = 7f;

    private boolean         visible    = false;
    private PlacedObject    pipe       = null;
    private ItemPipeConfig  config     = null;
    private boolean         dirty      = false;
    private boolean         justOpened = false;

    private String          pipeKey    = null;
    private ItemPipeNetwork network    = null;

    private final ShapeRenderer shape;
    private final BitmapFont    font;
    private final BitmapFont    labelFont;
    private final BitmapFont    itemFont;
    private final GlyphLayout   layout = new GlyphLayout();

    private int panelX, panelY;
    private int sw, sh;

    public ItemPipeUI() {
        shape = new ShapeRenderer();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

        p.size  = 22;
        p.color = Color.WHITE;
        font = gen.generateFont(p);

        p.size  = 12;
        p.color = Color.WHITE;
        labelFont = gen.generateFont(p);

        p.size  = 11;
        p.color = Color.WHITE;
        itemFont = gen.generateFont(p);

        gen.dispose();
    }


    public void open(PlacedObject pipe, ItemPipeConfig config,
                     String pipeKey, ItemPipeNetwork network) {
        this.pipe       = pipe;
        this.config     = config;
        this.pipeKey    = pipeKey;
        this.network    = network;
        this.visible    = true;
        this.dirty      = false;
        this.justOpened = true;
        layout();
    }

    public void close() {
        visible  = false;
        pipe     = null;
        config   = null;
        pipeKey  = null;
        network  = null;
    }


    private void layout() {
        sw     = Gdx.graphics.getWidth();
        sh     = Gdx.graphics.getHeight();
        panelX = (sw - PANEL_W) / 2;
        panelY = (sh - PANEL_H) / 2;
        shape.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    public void resize(int w, int h) {
        sw = w; sh = h;
        layout();
    }


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

        int cx   = panelX + PANEL_W / 2;
        int cy   = panelY + PANEL_H / 2;
        int half = BTN_SIZE / 2;

        if (hitTest(mx, my, cx - half, cy + GAP - half, BTN_SIZE, BTN_SIZE)) {
            config.cycleNorth(); dirty = true;
        } else if (hitTest(mx, my, cx - half, cy - GAP - half, BTN_SIZE, BTN_SIZE)) {
            config.cycleSouth(); dirty = true;
        } else if (hitTest(mx, my, cx + GAP - half, cy - half, BTN_SIZE, BTN_SIZE)) {
            config.cycleEast(); dirty = true;
        } else if (hitTest(mx, my, cx - GAP - half, cy - half, BTN_SIZE, BTN_SIZE)) {
            config.cycleWest(); dirty = true;
        } else if (!hitTest(mx, my, panelX, panelY, PANEL_W, PANEL_H)) {
            close();
        }
    }


    public void render(SpriteBatch batch) {
        if (!visible || config == null) return;

        batch.end();
        shape.setProjectionMatrix(batch.getProjectionMatrix());

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.10f, 0.10f, 0.14f, 0.97f);
        shape.rect(panelX, panelY, PANEL_W, PANEL_H);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.45f, 0.45f, 0.55f, 1f);
        shape.rect(panelX, panelY, PANEL_W, PANEL_H);
        shape.end();

        int cx = panelX + PANEL_W / 2;
        int cy = panelY + PANEL_H / 2;

        drawPortButton(cx,       cy + GAP, config.getNorth(), "N");
        drawPortButton(cx,       cy - GAP, config.getSouth(), "S");
        drawPortButton(cx + GAP, cy,       config.getEast(),  "E");
        drawPortButton(cx - GAP, cy,       config.getWest(),  "W");

        int half = CTR_SIZE / 2;
        ItemStack buffered = (network != null && pipeKey != null)
                ? network.getBuffer(pipeKey) : null;

        Color boxColor = buffered != null
                ? new Color(0.18f, 0.38f, 0.22f, 1f)
                : new Color(0.20f, 0.20f, 0.25f, 1f);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(boxColor);
        shape.rect(cx - half, cy - half, CTR_SIZE, CTR_SIZE);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.55f, 0.55f, 0.65f, 1f);
        shape.rect(cx - half, cy - half, CTR_SIZE, CTR_SIZE);
        shape.end();

        batch.begin();

        font.setColor(0.85f, 0.85f, 0.92f, 1f);
        font.draw(batch, (pipe != null && pipe.isFilterItemPipe()) ? "Filter Item Pipe" : "Item Pipe",
                panelX + 12, panelY + PANEL_H - 10);

        labelFont.setColor(0.55f, 0.55f, 0.62f, 1f);
        labelFont.draw(batch, "Click to cycle:  OFF  BLUE IN  ORANGE OUT",
                panelX + 12, panelY + 20);

        if (buffered != null) {
            String name = buffered.getItem().getDisplayName();
            itemFont.setColor(0.90f, 1.00f, 0.85f, 1f);
            layout.setText(itemFont, name);
            float maxW = CTR_SIZE - 4;
            if (layout.width <= maxW) {
                itemFont.draw(batch, name,
                        cx - layout.width / 2f,
                        cy + layout.height / 2f);
            } else {
                int sp = name.indexOf(' ');
                if (sp > 0) {
                    String line1 = name.substring(0, sp);
                    String line2 = name.substring(sp + 1);
                    GlyphLayout l1 = new GlyphLayout(itemFont, line1);
                    GlyphLayout l2 = new GlyphLayout(itemFont, line2);
                    float lineH = l1.height + 2;
                    itemFont.draw(batch, line1, cx - l1.width / 2f, cy + lineH);
                    itemFont.draw(batch, line2, cx - l2.width / 2f, cy + lineH - l1.height - 3);
                } else {
                    itemFont.draw(batch, name,
                            cx - layout.width / 2f,
                            cy + layout.height / 2f);
                }
            }
        } else {
            itemFont.setColor(0.40f, 0.40f, 0.48f, 1f);
            GlyphLayout el = new GlyphLayout(itemFont, "empty");
            itemFont.draw(batch, "empty", cx - el.width / 2f, cy + el.height / 2f);
        }
    }


    public void drawAllPipeOverlays(SpriteBatch batch,
                                    Map<String, ItemPipeConfig> configs,
                                    Map<String, PlacedObject>   pipeLookup,
                                    float cameraX, float cameraY) {
        if (configs == null || pipeLookup == null) return;

        batch.end();

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        shape.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);

        float tile = Main.TILE_SCALE;
        float half = tile * 0.5f;

        for (Map.Entry<String, PlacedObject> entry : pipeLookup.entrySet()) {
            PlacedObject po = entry.getValue();
            if (po.type != PlacedObject.Type.ITEM_PIPE && po.type != PlacedObject.Type.FILTER_ITEM_PIPE) continue;

            ItemPipeConfig cfg = configs.get(entry.getKey());
            if (cfg == null) continue;

            float scx = po.getX() * tile + half + cameraX;
            float scy = po.getY() * tile + half + cameraY;

            if (scx < -tile || scx > screenW + tile) continue;
            if (scy < -tile || scy > screenH + tile) continue;

            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0f, 0f, 0f, 0.35f);
            shape.circle(scx, scy, half * 0.72f, 16);
            shape.end();

            drawOverlayArrow(scx, scy, half, cfg.getNorth(), 0);
            drawOverlayArrow(scx, scy, half, cfg.getSouth(), 1);
            drawOverlayArrow(scx, scy, half, cfg.getEast(),  2);
            drawOverlayArrow(scx, scy, half, cfg.getWest(),  3);
        }

        batch.begin();
    }

    private void drawOverlayArrow(float cx, float cy, float half,
                                  ItemPipeConfig.PortMode mode, int dir) {
        if (mode == ItemPipeConfig.PortMode.DISABLED) {
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0.35f, 0.35f, 0.40f, 0.7f);
            float[] p = portEdgePoint(cx, cy, half * 0.58f, dir);
            shape.circle(p[0], p[1], 2.5f, 8);
            shape.end();
            return;
        }

        boolean isOutput = (mode == ItemPipeConfig.PortMode.OUTPUT);

        Color col = isOutput
                ? new Color(1.00f, 0.50f, 0.10f, 0.95f)
                : new Color(0.25f, 0.65f, 1.00f, 0.95f);

        float sz = OVERLAY_ARROW_SZ;


        float[] tip  = portEdgePoint(cx, cy, half * 0.80f, dir);
        float[] base = portEdgePoint(cx, cy, half * 0.38f, dir);

        if (!isOutput) {
            float[] tmp = tip; tip = base; base = tmp;
        }

        float[] perp = perpOffset(dir, sz);

        float[] vx = { tip[0], base[0] + perp[0], base[0] - perp[0] };
        float[] vy = { tip[1], base[1] + perp[1], base[1] - perp[1] };

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(col);
        shape.triangle(vx[0], vy[0], vx[1], vy[1], vx[2], vy[2]);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0f, 0f, 0f, 0.55f);
        shape.triangle(vx[0], vy[0], vx[1], vy[1], vx[2], vy[2]);
        shape.end();
    }

    private static float[] portEdgePoint(float cx, float cy, float dist, int dir) {
        switch (dir) {
            case 0: return new float[]{ cx,          cy + dist };
            case 1: return new float[]{ cx,          cy - dist };
            case 2: return new float[]{ cx + dist,   cy        };
            default:return new float[]{ cx - dist,   cy        };
        }
    }

    private static float[] perpOffset(int dir, float sz) {
        switch (dir) {
            case 0: 
            case 1:
                return new float[]{ sz, 0 };
            default: 
                return new float[]{ 0, sz };
        }
    }


    private void drawPortButton(int cx, int cy, ItemPipeConfig.PortMode mode, String dir) {
        int half = BTN_SIZE / 2;

        Color fill;
        switch (mode) {
            case INPUT:  fill = new Color(0.18f, 0.55f, 1.00f, 1f); break;
            case OUTPUT: fill = new Color(1.00f, 0.42f, 0.12f, 1f); break;
            default:     fill = new Color(0.20f, 0.20f, 0.25f, 1f); break;
        }

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(fill);
        shape.rect(cx - half, cy - half, BTN_SIZE, BTN_SIZE);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.55f, 0.55f, 0.65f, 1f);
        shape.rect(cx - half, cy - half, BTN_SIZE, BTN_SIZE);
        shape.end();

        drawArrow(cx, cy, dir, mode);
    }

    private void drawArrow(int cx, int cy, String dir, ItemPipeConfig.PortMode mode) {
        if (mode == ItemPipeConfig.PortMode.DISABLED) return;

        float[] vx = new float[3];
        float[] vy = new float[3];
        int     sz = 17;
        boolean inward = (mode == ItemPipeConfig.PortMode.INPUT);

        switch (dir) {
            case "N":
                if (inward) {
                    vx[0]=cx;      vy[0]=cy-sz;
                    vx[1]=cx-sz;   vy[1]=cy+sz/2f;
                    vx[2]=cx+sz;   vy[2]=cy+sz/2f;
                } else {
                    vx[0]=cx;      vy[0]=cy+sz;
                    vx[1]=cx-sz;   vy[1]=cy-sz/2f;
                    vx[2]=cx+sz;   vy[2]=cy-sz/2f;
                }
                break;
            case "S":
                if (inward) {
                    vx[0]=cx;      vy[0]=cy+sz;
                    vx[1]=cx-sz;   vy[1]=cy-sz/2f;
                    vx[2]=cx+sz;   vy[2]=cy-sz/2f;
                } else {
                    vx[0]=cx;      vy[0]=cy-sz;
                    vx[1]=cx-sz;   vy[1]=cy+sz/2f;
                    vx[2]=cx+sz;   vy[2]=cy+sz/2f;
                }
                break;
            case "E":
                if (inward) {
                    vx[0]=cx-sz;      vy[0]=cy;
                    vx[1]=cx+sz/2f;   vy[1]=cy+sz;
                    vx[2]=cx+sz/2f;   vy[2]=cy-sz;
                } else {
                    vx[0]=cx+sz;      vy[0]=cy;
                    vx[1]=cx-sz/2f;   vy[1]=cy+sz;
                    vx[2]=cx-sz/2f;   vy[2]=cy-sz;
                }
                break;
            case "W":
                if (inward) {
                    vx[0]=cx+sz;      vy[0]=cy;
                    vx[1]=cx-sz/2f;   vy[1]=cy+sz;
                    vx[2]=cx-sz/2f;   vy[2]=cy-sz;
                } else {
                    vx[0]=cx-sz;      vy[0]=cy;
                    vx[1]=cx+sz/2f;   vy[1]=cy+sz;
                    vx[2]=cx+sz/2f;   vy[2]=cy-sz;
                }
                break;
        }

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(Color.WHITE);
        shape.triangle(vx[0], vy[0], vx[1], vy[1], vx[2], vy[2]);
        shape.end();
    }


    private boolean hitTest(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    public boolean        isVisible()  { return visible; }
    public boolean        isDirty()    { return dirty;   }
    public void           clearDirty() { dirty = false;  }
    public PlacedObject   getPipe()    { return pipe;    }
    public ItemPipeConfig getConfig()  { return config;  }

    public void dispose() {
        shape.dispose();
        font.dispose();
        labelFont.dispose();
        itemFont.dispose();
    }
}