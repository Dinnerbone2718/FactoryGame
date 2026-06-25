package com.factory.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.Main;
import com.factory.game.Minimap;

public class ChunkLoaderUI {

    private static final int GRID_RADIUS   = 3;
    private static final int GRID_DIAMETER = GRID_RADIUS * 2 + 1;
    private static final int CELL_SIZE     = 72;
    private static final int CELL_GAP      = 3;

    private static final int ITEM_PAD  = 28;
    private static final int TITLE_H   = 96;
    private static final int FOOTER_H  = 72;

    private static final int GRID_PANEL_W = GRID_DIAMETER * CELL_SIZE + ITEM_PAD * 2;
    private static final int GRID_PANEL_H = TITLE_H + GRID_DIAMETER * CELL_SIZE
                                          + ITEM_PAD * 2 + FOOTER_H;

    private static final int INFO_GAP      = 40;
    private static final int INFO_PANEL_W  = 340;

    private static final int BAR_H         = 20;
    private static final int BAR_LABEL_H   = 24;
    private static final int ROW_H         = BAR_LABEL_H + BAR_H + 24;
    private static final int SECTION_HDR_H = 40;
    private static final int TANK_TITLE_H  = 180;
    private static final int TANK_FOOTER_H = 120;
    private static final int TANK_SIDE_PAD = 28;

    private boolean      visible    = false;
    private boolean      dirty      = false;
    private PlacedObject loader     = null;
    private int          loaderCX, loaderCY;
    private int          hoveredCol = -1;
    private int          hoveredRow = -1;

    private final ShapeRenderer shapes;
    private final BitmapFont    font;
    private final Minimap       minimap;

    private int gridPanelX, gridPanelY;
    private int infoPanelX, infoPanelY;

    public ChunkLoaderUI(Minimap minimap) {
        this.minimap = minimap;
        this.shapes  = new ShapeRenderer();

        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 22;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }

    public void open(PlacedObject chunkLoader) {
        this.loader   = chunkLoader;
        this.loaderCX = Math.floorDiv(chunkLoader.getX(), Main.CHUNK_SIZE);
        this.loaderCY = Math.floorDiv(chunkLoader.getY(), Main.CHUNK_SIZE);
        visible       = true;
        dirty         = false;
        hoveredCol    = -1;
        hoveredRow    = -1;
        relayout();
    }

    public void close() {
        visible = false;
        loader  = null;
    }

    public boolean      isVisible()  { return visible; }
    public boolean      isDirty()    { return dirty;   }
    public void         clearDirty() { dirty = false;  }
    public PlacedObject getLoader()  { return loader;  }

    public void handleInput() {
        if (!visible || loader == null) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();

        hoveredCol = -1;
        hoveredRow = -1;

        int gridX = gridPanelX + ITEM_PAD;
        int gridY = gridPanelY + FOOTER_H + ITEM_PAD;

        int relX = mx - gridX;
        int relY = my - gridY;

        if (relX >= 0 && relY >= 0
                && relX < GRID_DIAMETER * CELL_SIZE
                && relY < GRID_DIAMETER * CELL_SIZE) {

            int col  = relX / CELL_SIZE;
            int row  = relY / CELL_SIZE;
            int offX = col - GRID_RADIUS;
            int offY = row - GRID_RADIUS;
            int cx   = loaderCX + offX;
            int cy   = loaderCY + offY;

            boolean centre = offX == 0 && offY == 0;
            boolean oob    = cx < 0 || cx >= Main.WORLD_CHUNKS
                          || cy < 0 || cy >= Main.WORLD_CHUNKS;

            if (!centre && !oob) {
                hoveredCol = col;
                hoveredRow = row;

                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    loader.togglePinnedChunk(Chunk.key(cx, cy));
                    dirty = true;
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (!visible || loader == null) return;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        boolean    active = loader.isChunkLoaderActive();
        LiquidTank tank   = loader.getLiquidTank();
        int        pinned = loader.getPinnedChunkKeys().size();

        int gridX      = gridPanelX + ITEM_PAD;
        int gridY      = gridPanelY + FOOTER_H + ITEM_PAD;
        int bx         = infoPanelX + TANK_SIDE_PAD;
        int bw         = INFO_PANEL_W - TANK_SIDE_PAD * 2;
        int barY       = infoBarY();
        int divY       = infoPanelY + GRID_PANEL_H - TANK_TITLE_H
                         - ROW_H - SECTION_HDR_H - 4;
        int worldTiles = Main.WORLD_CHUNKS * Main.CHUNK_SIZE;
        Texture mapTex = (minimap != null) ? minimap.getMapTexture() : null;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(0, 0, sw, sh);
        shapes.end();

        drawPanel(gridPanelX, gridPanelY, GRID_PANEL_W, GRID_PANEL_H, TITLE_H);
        drawPanel(infoPanelX, infoPanelY, INFO_PANEL_W,  GRID_PANEL_H, TANK_TITLE_H);

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.14f, 0.30f, 0.22f, 0.80f);
        shapes.line(gridPanelX + 16, gridPanelY + FOOTER_H,
                    gridPanelX + GRID_PANEL_W - 16, gridPanelY + FOOTER_H);
        shapes.end();

        batch.begin();

        if (mapTex != null) {
            for (int col = 0; col < GRID_DIAMETER; col++) {
                for (int row = 0; row < GRID_DIAMETER; row++) {
                    int offX = col - GRID_RADIUS;
                    int offY = row - GRID_RADIUS;
                    int cx   = loaderCX + offX;
                    int cy   = loaderCY + offY;

                    boolean oob = cx < 0 || cx >= Main.WORLD_CHUNKS
                               || cy < 0 || cy >= Main.WORLD_CHUNKS;
                    if (oob) continue;

                    int cellX = gridX + col * CELL_SIZE + CELL_GAP;
                    int cellY = gridY + row * CELL_SIZE + CELL_GAP;
                    int cellW = CELL_SIZE - CELL_GAP * 2;
                    int cellH = CELL_SIZE - CELL_GAP * 2;

                    int srcX = cx * Main.CHUNK_SIZE;
                    int srcY = worldTiles - (cy + 1) * Main.CHUNK_SIZE;

                    batch.setColor(1f, 1f, 1f, 1f);
                    batch.draw(mapTex,
                               cellX, cellY, cellW, cellH,
                               srcX, srcY, Main.CHUNK_SIZE, Main.CHUNK_SIZE,
                               false, false);
                }
            }
        }

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (int col = 0; col < GRID_DIAMETER; col++) {
            for (int row = 0; row < GRID_DIAMETER; row++) {
                int offX = col - GRID_RADIUS;
                int offY = row - GRID_RADIUS;
                int cx   = loaderCX + offX;
                int cy   = loaderCY + offY;

                int cellX = gridX + col * CELL_SIZE + CELL_GAP;
                int cellY = gridY + row * CELL_SIZE + CELL_GAP;
                int cellW = CELL_SIZE - CELL_GAP * 2;
                int cellH = CELL_SIZE - CELL_GAP * 2;

                boolean oob      = cx < 0 || cx >= Main.WORLD_CHUNKS
                                || cy < 0 || cy >= Main.WORLD_CHUNKS;
                boolean centre   = offX == 0 && offY == 0;
                boolean isPinned = !oob && !centre
                                && loader.getPinnedChunkKeys().contains(Chunk.key(cx, cy));
                boolean hov      = col == hoveredCol && row == hoveredRow;

                if (oob) {

                    shapes.setColor(0.06f, 0.06f, 0.07f, 0.94f);
                    shapes.rect(cellX, cellY, cellW, cellH);

                } else if (centre) {

                    shapes.setColor(0.14f, 0.36f, 0.72f, 0.52f);
                    shapes.rect(cellX, cellY, cellW, cellH);

                } else if (isPinned && active) {

                    shapes.setColor(0.08f, 0.50f, 0.18f, 0.52f);
                    shapes.rect(cellX, cellY, cellW, cellH);

                    shapes.setColor(0.25f, 0.90f, 0.45f, 0.22f);
                    shapes.rect(cellX + 5, cellY + 5, cellW - 10, cellH - 10);

                } else if (isPinned) {

                    shapes.setColor(0.32f, 0.38f, 0.10f, 0.52f);
                    shapes.rect(cellX, cellY, cellW, cellH);

                } else if (hov) {

                    shapes.setColor(0.85f, 0.82f, 0.28f, 0.30f);
                    shapes.rect(cellX, cellY, cellW, cellH);
                }

            }
        }

        shapes.setColor(0.22f, 0.42f, 0.32f, 0.60f);
        for (int col = 0; col <= GRID_DIAMETER; col++) {
            shapes.rect(gridX + col * CELL_SIZE, gridY, 1, GRID_DIAMETER * CELL_SIZE);
        }
        for (int row = 0; row <= GRID_DIAMETER; row++) {
            shapes.rect(gridX, gridY + row * CELL_SIZE, GRID_DIAMETER * CELL_SIZE, 1);
        }

        shapes.setColor(0.08f, 0.08f, 0.08f, 1f);
        shapes.rect(bx, barY, bw, BAR_H);
        if (tank != null && tank.getFillRatio() > 0f) {
            LiquidType lt = tank.getType();
            Color      fc = (lt != null) ? lt.color : new Color(0.25f, 0.70f, 1.00f, 1f);
            shapes.setColor(fc.r * 0.9f, fc.g * 0.9f, fc.b * 0.9f, 0.95f);
            shapes.rect(bx, barY, bw * tank.getFillRatio(), BAR_H);
        }

        shapes.setColor(0.22f, 0.40f, 0.30f, 0.65f);
        shapes.rect(infoPanelX + TANK_SIDE_PAD, divY,
                    INFO_PANEL_W - TANK_SIDE_PAD * 2, 1);

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.45f, 0.80f, 1.00f, 1f);
        shapes.rect(gridX + GRID_RADIUS * CELL_SIZE,
                    gridY + GRID_RADIUS * CELL_SIZE,
                    CELL_SIZE, CELL_SIZE);
        shapes.end();

        batch.begin();

        font.getData().setScale(1.90f);
        font.setColor(0.48f, 0.88f, 0.65f, 1f);
        font.draw(batch, "Chunk Loader",
                gridPanelX + 28, gridPanelY + GRID_PANEL_H - 22);

        font.getData().setScale(1.00f);
        font.setColor(0.42f, 0.62f, 0.52f, 0.88f);
        font.draw(batch, "Chunk (" + loaderCX + ", " + loaderCY + ")",
                gridPanelX + 28, gridPanelY + GRID_PANEL_H - TITLE_H + 40);

        if (hoveredCol >= 0 && hoveredRow >= 0) {
            int offX     = hoveredCol - GRID_RADIUS;
            int offY     = hoveredRow - GRID_RADIUS;
            int cx       = loaderCX + offX;
            int cy       = loaderCY + offY;
            boolean isPinned = loader.getPinnedChunkKeys().contains(Chunk.key(cx, cy));
            font.getData().setScale(1.15f);
            font.setColor(isPinned
                    ? new Color(0.35f, 1.00f, 0.62f, 1f)
                    : new Color(1.00f, 0.95f, 0.42f, 1f));
            font.draw(batch,
                    (isPinned ? "Unpin" : "Pin") + " chunk (" + cx + ", " + cy + ")",
                    gridPanelX + 28, gridPanelY + FOOTER_H - 10);
        } else {
            font.getData().setScale(0.88f);
            font.setColor(0.38f, 0.38f, 0.42f, 0.88f);
            font.draw(batch, "Click a cell to toggle chunk pinning",
                    gridPanelX + 28, gridPanelY + FOOTER_H - 10);
        }

        font.getData().setScale(1.10f);
        font.setColor(0.44f, 0.35f, 0.24f, 0.85f);
        font.draw(batch, "ESC  to close", gridPanelX + 28, gridPanelY + 26);

        font.getData().setScale(1.35f);
        font.setColor(0.48f, 0.85f, 0.65f, 1f);
        font.draw(batch, "Status",
                infoPanelX + TANK_SIDE_PAD, infoPanelY + GRID_PANEL_H - 18);

        font.getData().setScale(1.12f);
        if (active) {
            font.setColor(0.35f, 1.00f, 0.62f, 1f);
            font.draw(batch, "Active",
                    infoPanelX + TANK_SIDE_PAD,
                    infoPanelY + GRID_PANEL_H - TANK_TITLE_H + 110);
        } else {
            font.setColor(0.85f, 0.28f, 0.18f, 1f);
            font.draw(batch, "Inactive",
                    infoPanelX + TANK_SIDE_PAD,
                    infoPanelY + GRID_PANEL_H - TANK_TITLE_H + 110);
            font.getData().setScale(0.90f);
            font.setColor(0.55f, 0.30f, 0.22f, 0.90f);
            font.draw(batch, "Needs gas",
                    infoPanelX + TANK_SIDE_PAD,
                    infoPanelY + GRID_PANEL_H - TANK_TITLE_H + 82);
        }

        font.getData().setScale(0.95f);
        font.setColor(0.68f, 0.68f, 0.72f, 1f);
        font.draw(batch,
                pinned + (pinned == 1 ? " chunk pinned" : " chunks pinned"),
                infoPanelX + TANK_SIDE_PAD,
                infoPanelY + GRID_PANEL_H - TANK_TITLE_H + 46);

        font.getData().setScale(0.92f);
        font.setColor(0.55f, 0.55f, 0.55f, 0.85f);
        font.draw(batch, "FUEL",
                infoPanelX + TANK_SIDE_PAD,
                infoPanelY + GRID_PANEL_H - TANK_TITLE_H - SECTION_HDR_H + 28);

        if (tank != null) {
            LiquidType lt   = tank.getType();
            Color      fc   = (lt != null) ? lt.color : new Color(0.55f, 0.55f, 0.60f, 1f);
            String     name = (lt != null) ? lt.name  : "Empty";
            font.getData().setScale(0.92f);
            font.setColor(fc.r, fc.g, fc.b, 1f);
            font.draw(batch,
                    name + " " + (int) tank.getAmount() + " / " + (int) tank.getCapacity(),
                    bx, barY + BAR_H + BAR_LABEL_H - 2);
        }

        int legendY = divY - 18;
        font.getData().setScale(0.88f);
        font.setColor(0.24f, 0.50f, 0.38f, 1f);
        font.draw(batch, "LEGEND", infoPanelX + TANK_SIDE_PAD, legendY);
        legendY -= 30;

        font.setColor(0.35f, 0.68f, 1.00f, 1f);
        font.draw(batch, "Loader chunk (always loaded)",
                infoPanelX + TANK_SIDE_PAD, legendY);
        legendY -= 28;

        font.setColor(0.20f, 0.80f, 0.35f, 1f);
        font.draw(batch, "Pinned + active",
                infoPanelX + TANK_SIDE_PAD, legendY);
        legendY -= 28;

        font.setColor(0.45f, 0.55f, 0.22f, 1f);
        font.draw(batch, "Pinned (no liquid)",
                infoPanelX + TANK_SIDE_PAD, legendY);
        legendY -= 28;

        font.setColor(0.18f, 0.28f, 0.22f, 1f);
        font.draw(batch, "Not pinned",
                infoPanelX + TANK_SIDE_PAD, legendY);
        legendY -= 28;

        font.setColor(0.10f, 0.10f, 0.12f, 1f);
        font.draw(batch, "Out of world bounds",
                infoPanelX + TANK_SIDE_PAD, legendY);

        float drainPerSec = 1.00f + pinned * 0.15f;
        font.getData().setScale(0.92f);
        font.setColor(0.44f, 0.44f, 0.50f, 0.90f);
        font.draw(batch,
                String.format("Drain rate: %.2f / s", drainPerSec),
                infoPanelX + TANK_SIDE_PAD,
                infoPanelY + TANK_FOOTER_H + 24);

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }

    public void resize(int w, int h) {
        shapes.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        if (visible) relayout();
    }

    public void dispose() {
        shapes.dispose();
        font.dispose();
    }

    private void relayout() {
        int sw     = Gdx.graphics.getWidth();
        int sh     = Gdx.graphics.getHeight();
        int totalW = GRID_PANEL_W + INFO_GAP + INFO_PANEL_W;

        gridPanelX = (sw - totalW) / 2 - 60;
        gridPanelY = (sh - GRID_PANEL_H) / 2;
        infoPanelX = gridPanelX + GRID_PANEL_W + INFO_GAP;
        infoPanelY = gridPanelY;

        shapes.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    private int infoBarY() {
        int topOfRows = infoPanelY + GRID_PANEL_H - TANK_TITLE_H - SECTION_HDR_H - ROW_H;
        return topOfRows + (ROW_H - BAR_H - BAR_LABEL_H);
    }

    private void drawPanel(int x, int y, int w, int h, int panelTitleH) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.10f, 0.09f, 0.97f);
        shapes.rect(x, y, w, h);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.24f, 0.50f, 0.38f, 1f);
        shapes.rect(x, y, w, h);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.14f, 0.30f, 0.22f, 1f);
        shapes.line(x + 16, y + h - panelTitleH, x + w - 16, y + h - panelTitleH);
        shapes.end();
    }
}