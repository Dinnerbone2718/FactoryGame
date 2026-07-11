package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PauseMenuUI extends InputAdapter {

    private enum Mode {
        MAIN,
        SAVE,
        LOAD,
    }

    private static final int PANEL_W = 420;
    private static final int TITLE_H = 56;
    private static final int BTN_H = 44;
    private static final int BTN_GAP = 14;
    private static final int SIDE_PAD = 24;

    private static final int SLOT_ROW_H = 52;
    private static final int SLOT_ROW_GAP = 10;
    private static final int SLOT_BTN_W = 90;

    private final Main main;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;

    private boolean visible = false;
    private Mode mode = Mode.MAIN;
    private String statusMessage = null;

    private int panelX, panelY, panelH;

    private int resumeX, resumeY;
    private int saveX, saveY;
    private int loadX, loadY;

    private int backX, backY;

    public PauseMenuUI(Main main) {
        this.main = main;
        this.shapeRenderer = new ShapeRenderer();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("JetBrainsMono-Regular.ttf")
        );

        FreeTypeFontParameter bodyParam = new FreeTypeFontParameter();
        bodyParam.size = 16;
        bodyParam.color = Color.WHITE;
        this.font = generator.generateFont(bodyParam);

        FreeTypeFontParameter titleParam = new FreeTypeFontParameter();
        titleParam.size = 22;
        titleParam.color = Color.WHITE;
        this.titleFont = generator.generateFont(titleParam);

        generator.dispose();

        updateLayout();
    }

    private void updateLayout() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        int rows = (mode == Mode.MAIN) ? 3 : GameSaveManager.getSlotCount();
        int rowH = (mode == Mode.MAIN) ? BTN_H : SLOT_ROW_H;
        int rowGap = (mode == Mode.MAIN) ? BTN_GAP : SLOT_ROW_GAP;

        panelH =
            TITLE_H +
            rows * rowH +
            (rows - 1) * rowGap +
            SIDE_PAD * 2 +
            ((mode != Mode.MAIN) ? (BTN_H + BTN_GAP) : 0);

        panelX = (sw / 2 - PANEL_W) / 2;
        panelY = (sh - panelH) / 2;

        int contentTop = panelY + panelH - TITLE_H - SIDE_PAD;

        if (mode == Mode.MAIN) {
            resumeX = panelX + SIDE_PAD;
            resumeY = contentTop - BTN_H;
            saveX = panelX + SIDE_PAD;
            saveY = resumeY - BTN_GAP - BTN_H;
            loadX = panelX + SIDE_PAD;
            loadY = saveY - BTN_GAP - BTN_H;
        } else {
            backX = panelX + SIDE_PAD;
            backY = panelY + SIDE_PAD;
        }

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    public void show() {
        visible = true;
        mode = Mode.MAIN;
        statusMessage = null;
        updateLayout();
        Gdx.input.setInputProcessor(this);
    }

    public void hide() {
        visible = false;
        Gdx.input.setInputProcessor(null);
    }

    public boolean isVisible() {
        return visible;
    }

    public void resize(int width, int height) {
        updateLayout();
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        titleFont.dispose();
    }

    public void handleInput() {
        if (!visible) return;
        if (!Gdx.input.justTouched()) return;

        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();
        int btnW = PANEL_W - SIDE_PAD * 2;

        if (mode == Mode.MAIN) {
            if (hitArea(mx, my, resumeX, resumeY, btnW, BTN_H)) {
                hide();
                return;
            }
            if (hitArea(mx, my, saveX, saveY, btnW, BTN_H)) {
                mode = Mode.SAVE;
                statusMessage = null;
                updateLayout();
                return;
            }
            if (hitArea(mx, my, loadX, loadY, btnW, BTN_H)) {
                mode = Mode.LOAD;
                statusMessage = null;
                updateLayout();
                return;
            }
            return;
        }

        if (hitArea(mx, my, backX, backY, btnW, BTN_H)) {
            mode = Mode.MAIN;
            statusMessage = null;
            updateLayout();
            return;
        }

        GameSaveManager.SlotInfo[] slots = GameSaveManager.listSlots();
        for (int i = 0; i < slots.length; i++) {
            int rowY = slotRowY(i);
            int actionX = panelX + PANEL_W - SIDE_PAD - SLOT_BTN_W;

            if (mode == Mode.SAVE) {
                if (hitArea(mx, my, actionX, rowY, SLOT_BTN_W, SLOT_ROW_H)) {
                    boolean ok = GameSaveManager.save(i, main);
                    statusMessage = ok
                        ? "Saved to Slot " + (i + 1)
                        : "Save failed";
                    return;
                }
            } else {
                if (
                    slots[i].exists &&
                    hitArea(mx, my, actionX, rowY, SLOT_BTN_W, SLOT_ROW_H)
                ) {
                    boolean ok = GameSaveManager.load(i, main);
                    if (ok) {
                        hide();
                    } else {
                        statusMessage = "Load failed";
                    }
                    return;
                }
            }
        }
    }

    private int slotRowY(int index) {
        int topRowY = panelY + SIDE_PAD + BTN_H + BTN_GAP;
        int slotCount = GameSaveManager.getSlotCount();
        int fromBottomIndex = slotCount - 1 - index;
        return topRowY + fromBottomIndex * (SLOT_ROW_H + SLOT_ROW_GAP);
    }

    private boolean hitArea(int mx, int my, int bx, int by, int bw, int bh) {
        return mx >= bx && mx < bx + bw && my >= by && my < by + bh;
    }

    public void render(SpriteBatch batch) {
        if (!visible) return;

        batch.end();

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        int btnW = PANEL_W - SIDE_PAD * 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.97f);
        shapeRenderer.rect(panelX, panelY, PANEL_W, panelH);

        if (mode == Mode.MAIN) {
            drawButtonFill(resumeX, resumeY, btnW, BTN_H, true);
            drawButtonFill(saveX, saveY, btnW, BTN_H, true);
            drawButtonFill(loadX, loadY, btnW, BTN_H, true);
        } else {
            drawButtonFill(backX, backY, btnW, BTN_H, true);

            GameSaveManager.SlotInfo[] slots = GameSaveManager.listSlots();
            for (int i = 0; i < slots.length; i++) {
                int rowY = slotRowY(i);
                boolean actionable = (mode == Mode.SAVE) || slots[i].exists;

                shapeRenderer.setColor(
                    actionable ? 0.16f : 0.11f,
                    actionable ? 0.16f : 0.11f,
                    actionable ? 0.22f : 0.14f,
                    1f
                );
                shapeRenderer.rect(panelX + SIDE_PAD, rowY, btnW, SLOT_ROW_H);

                int actionX = panelX + PANEL_W - SIDE_PAD - SLOT_BTN_W;
                shapeRenderer.setColor(
                    actionable ? 0.22f : 0.14f,
                    actionable ? 0.42f : 0.14f,
                    actionable ? 0.28f : 0.14f,
                    1f
                );
                shapeRenderer.rect(actionX, rowY, SLOT_BTN_W, SLOT_ROW_H);
            }
        }

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.45f, 1f);
        shapeRenderer.rect(panelX, panelY, PANEL_W, panelH);

        if (mode == Mode.MAIN) {
            drawButtonOutline(resumeX, resumeY, btnW, BTN_H);
            drawButtonOutline(saveX, saveY, btnW, BTN_H);
            drawButtonOutline(loadX, loadY, btnW, BTN_H);
        } else {
            drawButtonOutline(backX, backY, btnW, BTN_H);
            GameSaveManager.SlotInfo[] slots = GameSaveManager.listSlots();
            for (int i = 0; i < slots.length; i++) {
                int rowY = slotRowY(i);
                shapeRenderer.rect(panelX + SIDE_PAD, rowY, btnW, SLOT_ROW_H);
            }
        }

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 1f);
        shapeRenderer.line(
            panelX + 8,
            panelY + panelH - TITLE_H,
            panelX + PANEL_W - 8,
            panelY + panelH - TITLE_H
        );
        shapeRenderer.end();

        batch.begin();

        titleFont.getData().setScale(1f);
        titleFont.setColor(Color.WHITE);
        titleFont.draw(
            batch,
            "Paused",
            panelX,
            panelY + panelH - 16,
            PANEL_W,
            Align.center,
            false
        );

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);

        if (mode == Mode.MAIN) {
            drawCenteredLabel(batch, "Resume", resumeX, resumeY, btnW, BTN_H);
            drawCenteredLabel(batch, "Save Game", saveX, saveY, btnW, BTN_H);
            drawCenteredLabel(batch, "Load Game", loadX, loadY, btnW, BTN_H);
        } else {
            drawCenteredLabel(batch, "Back", backX, backY, btnW, BTN_H);

            SimpleDateFormat fmt = new SimpleDateFormat("MMM d, HH:mm");
            GameSaveManager.SlotInfo[] slots = GameSaveManager.listSlots();
            for (int i = 0; i < slots.length; i++) {
                int rowY = slotRowY(i);
                String label =
                    "Slot " +
                    (i + 1) +
                    " - " +
                    (slots[i].exists
                        ? fmt.format(new Date(slots[i].lastModified))
                        : "Empty");
                font.draw(
                    batch,
                    label,
                    panelX + SIDE_PAD + 12,
                    rowY + SLOT_ROW_H - 16
                );

                String actionLabel = (mode == Mode.SAVE)
                    ? "Save"
                    : (slots[i].exists ? "Load" : "");
                int actionX = panelX + PANEL_W - SIDE_PAD - SLOT_BTN_W;
                font.draw(
                    batch,
                    actionLabel,
                    actionX,
                    rowY + SLOT_ROW_H - 16,
                    SLOT_BTN_W,
                    Align.center,
                    false
                );
            }
        }

        if (statusMessage != null) {
            font.setColor(1f, 0.9f, 0.5f, 1f);
            font.draw(
                batch,
                statusMessage,
                panelX,
                panelY + panelH - TITLE_H - 4,
                PANEL_W,
                Align.center,
                false
            );
            font.setColor(Color.WHITE);
        }
    }

    private void drawButtonFill(int x, int y, int w, int h, boolean enabled) {
        shapeRenderer.setColor(
            enabled ? 0.18f : 0.12f,
            enabled ? 0.18f : 0.12f,
            enabled ? 0.24f : 0.16f,
            1f
        );
        shapeRenderer.rect(x, y, w, h);
    }

    private void drawButtonOutline(int x, int y, int w, int h) {
        shapeRenderer.rect(x, y, w, h);
    }

    private void drawCenteredLabel(
        SpriteBatch batch,
        String text,
        int x,
        int y,
        int w,
        int h
    ) {
        font.draw(
            batch,
            text,
            x,
            y + h - (h - font.getLineHeight()) / 2f,
            w,
            Align.center,
            false
        );
    }
}
