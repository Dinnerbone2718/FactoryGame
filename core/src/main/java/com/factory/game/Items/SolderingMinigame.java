package com.factory.game.Items;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SolderingMinigame {

    public static class SolderResult {
        public final Item    item;
        public final int     qty;
        public final boolean success;
        public SolderResult(Item item, int qty, boolean success) {
            this.item = item; this.qty = qty; this.success = success;
        }
    }

    public enum GateType {
        AND  ("AND",  "A AND B"),
        OR   ("OR",   "A OR B"),
        XOR  ("XOR",  "A XOR B"),
        NAND ("NAND", "NOT(A AND B)"),
        NOR  ("NOR",  "NOT(A OR B)"),
        NOT  ("NOT",  "NOT A"),
        BUFFER  ("BUFFER",  "A"),
        XNOR  ("XNOR",  "NOT (A XOR B)");

        public final String label;
        public final String formula;

        GateType(String label, String formula) {
            this.label   = label;
            this.formula = formula;
        }

        public boolean isSingleInput() { return this == NOT || this == BUFFER; }

        public int evaluate(int a, int b) {
            switch (this) {
                case AND:  return (a == 1 && b == 1) ? 1 : 0;
                case OR:   return (a == 1 || b == 1) ? 1 : 0;
                case XOR:  return (a != b)           ? 1 : 0;
                case NAND: return (a == 1 && b == 1) ? 0 : 1;
                case NOR:  return (a == 1 || b == 1) ? 0 : 1;
                case NOT:  return (a == 0)           ? 1 : 0;
                case BUFFER: return (a == 1)         ? 1 : 0;
                case XNOR: return (a == b)           ? 1 : 0;
                default:   return 0;
            }
        }
    }

    private enum Difficulty {
        SIMPLE  (5, 3,  10.0f, "Simple",  1),
        MEDIUM  (6, 4,  30.0f, "Medium",  2),
        COMPLEX (7, 5, 60.0f, "Complex", 3);

        final int    totalRounds;
        final int    passThreshold;
        final float  timePerRound;
        final String label;
        final int    gateCount;

        Difficulty(int totalRounds, int passThreshold, float timePerRound, String label, int gateCount) {
            this.totalRounds   = totalRounds;
            this.passThreshold = passThreshold;
            this.timePerRound  = timePerRound;
            this.label         = label;
            this.gateCount     = gateCount;
        }

        static Difficulty fromIngredientCount(int count) {
            if (count <= 2) return SIMPLE;
            if (count <= 4) return MEDIUM;
            return COMPLEX;
        }
    }

    private static final float FLASH_DURATION      = 0.55f;
    private static final float RESULT_DISPLAY_TIME = 2.2f;

    private enum State { INACTIVE, RUNNING, ROUND_FLASH, SHOWING_RESULT }
    private State state = State.INACTIVE;

    private SolderingRecipe activeRecipe;
    private Difficulty      difficulty;
    private int             totalRounds;
    private int             passThreshold;
    private float           timePerRound;
    private int             round;
    private int             correctCount;
    private boolean[]       roundResults;

    private GateType[] gateChain;
    private int[]      inputValues;
    private int[]      intermediates;
    private int        correctOutput;
    private float      roundTimer;
    private boolean    flashCorrect;
    private float      flashTimer;
    private int        playerAnswer;

    private SolderResult pendingResult;
    private float        resultTimer;
    private boolean      finalSuccess;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont    font;
    private final Random        rng = new Random();

    private static final GateType[] ALL_GATES  = GateType.values();
    private static final GateType[] DUAL_GATES = {
        GateType.AND, GateType.OR, GateType.XOR, GateType.NAND, GateType.NOR, GateType.XNOR
    };

    private static final Color COL_PANEL_BG    = new Color(0.04f, 0.07f, 0.11f, 0.97f);
    private static final Color COL_PANEL_EDGE  = new Color(0.18f, 0.68f, 0.88f, 1.00f);
    private static final Color COL_WIRE        = new Color(0.25f, 0.65f, 0.92f, 0.85f);
    private static final Color COL_GATE_BG     = new Color(0.09f, 0.26f, 0.40f, 1.00f);
    private static final Color COL_GATE_EDGE   = new Color(0.25f, 0.78f, 1.00f, 1.00f);
    private static final Color COL_HIGH        = new Color(0.18f, 0.92f, 0.42f, 1.00f);
    private static final Color COL_LOW         = new Color(0.82f, 0.22f, 0.22f, 1.00f);
    private static final Color COL_UNKNOWN     = new Color(0.10f, 0.20f, 0.30f, 1.00f);
    private static final Color COL_TITLE       = new Color(0.35f, 0.90f, 1.00f, 1.00f);
    private static final Color COL_SUCCESS     = new Color(0.20f, 1.00f, 0.45f, 1.00f);
    private static final Color COL_FAIL        = new Color(1.00f, 0.35f, 0.30f, 1.00f);
    private static final Color COL_DIFF_SIMPLE = new Color(0.35f, 0.90f, 0.45f, 1f);
    private static final Color COL_DIFF_MEDIUM = new Color(0.95f, 0.82f, 0.25f, 1f);
    private static final Color COL_DIFF_COMPLEX= new Color(1.00f, 0.42f, 0.32f, 1f);

    private static final float S_NW = 60f, S_NH = 42f;
    private static final float S_GW = 116f, S_GH = 70f;
    private static final float S_OW = 60f, S_OH = 42f;

    private static final float M_NW = 52f, M_NH = 36f;
    private static final float M_GW = 90f, M_GH = 56f;
    private static final float M_IW = 44f, M_IH = 30f;
    private static final float M_OW = 52f, M_OH = 36f;

    private static final float C_NW = 48f, C_NH = 34f;
    private static final float C_GW = 82f, C_GH = 52f;
    private static final float C_IW = 40f, C_IH = 28f;
    private static final float C_OW = 48f, C_OH = 34f;

    private final float[] gatePosCX = new float[3];
    private final float[] gatePosCY = new float[3];
    private float         gatePosW;
    private float         gatePosH;
    private int           hoveredGateIdx = -1;

    public SolderingMinigame() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 18;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();
    }

    public void start(SolderingRecipe recipe) {
        activeRecipe  = recipe;
        difficulty    = Difficulty.fromIngredientCount(recipe.getInputs().size());
        totalRounds   = difficulty.totalRounds;
        passThreshold = difficulty.passThreshold;
        timePerRound  = difficulty.timePerRound;
        roundResults  = new boolean[totalRounds];
        state         = State.RUNNING;
        round         = 0;
        correctCount  = 0;
        pendingResult = null;
        finalSuccess  = false;
        pickNewRound();
    }

    public boolean isActive() { return state != State.INACTIVE; }

    public SolderResult pollResult() {
        if (state == State.INACTIVE && pendingResult != null) {
            SolderResult r = pendingResult;
            pendingResult  = null;
            return r;
        }
        return null;
    }

    public void update(float delta) {
        if (state == State.INACTIVE) return;
        flashTimer = Math.max(0f, flashTimer - delta);

        if (state == State.RUNNING) {
            roundTimer -= delta;
            int answer = -1;
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT))  answer = 0;
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) answer = 1;
            boolean timedOut = roundTimer <= 0f;
            if (answer != -1 || timedOut) {
                playerAnswer        = answer;
                boolean correct     = (answer == correctOutput);
                roundResults[round] = correct;
                if (correct) correctCount++;
                flashCorrect = correct;
                flashTimer   = FLASH_DURATION;
                state        = State.ROUND_FLASH;
            }
        } else if (state == State.ROUND_FLASH) {
            if (flashTimer <= 0f) {
                round++;
                if (round >= totalRounds) finishGame();
                else { pickNewRound(); state = State.RUNNING; }
            }
        } else if (state == State.SHOWING_RESULT) {
            resultTimer -= delta;
            if (resultTimer <= 0f) state = State.INACTIVE;
        }
    }

    private void finishGame() {
        finalSuccess  = (correctCount >= passThreshold);
        pendingResult = finalSuccess
                ? new SolderResult(activeRecipe.getOutputItem(), activeRecipe.getOutputQuantity(), true)
                : null;
        state       = State.SHOWING_RESULT;
        resultTimer = RESULT_DISPLAY_TIME;
    }

    private void pickNewRound() {
        int gc        = difficulty.gateCount;
        gateChain     = new GateType[gc];
        intermediates = new int[Math.max(0, gc - 1)];

        if (gc == 1) {
            gateChain[0]   = ALL_GATES[rng.nextInt(ALL_GATES.length)];
            boolean single = gateChain[0].isSingleInput();
            inputValues    = new int[single ? 1 : 2];
            inputValues[0] = rng.nextInt(2);
            if (!single) inputValues[1] = rng.nextInt(2);
            correctOutput  = gateChain[0].evaluate(inputValues[0], single ? 0 : inputValues[1]);

        } else if (gc == 2) {
            gateChain[0]     = DUAL_GATES[rng.nextInt(DUAL_GATES.length)];
            gateChain[1]     = DUAL_GATES[rng.nextInt(DUAL_GATES.length)];
            inputValues      = new int[3];
            for (int i = 0; i < 3; i++) inputValues[i] = rng.nextInt(2);
            intermediates[0] = gateChain[0].evaluate(inputValues[0], inputValues[1]);
            correctOutput    = gateChain[1].evaluate(intermediates[0], inputValues[2]);

        } else {
            gateChain[0]     = DUAL_GATES[rng.nextInt(DUAL_GATES.length)];
            gateChain[1]     = DUAL_GATES[rng.nextInt(DUAL_GATES.length)];
            gateChain[2]     = DUAL_GATES[rng.nextInt(DUAL_GATES.length)];
            inputValues      = new int[4];
            for (int i = 0; i < 4; i++) inputValues[i] = rng.nextInt(2);
            intermediates[0] = gateChain[0].evaluate(inputValues[0], inputValues[1]);
            intermediates[1] = gateChain[1].evaluate(inputValues[2], inputValues[3]);
            correctOutput    = gateChain[2].evaluate(intermediates[0], intermediates[1]);
        }

        roundTimer   = timePerRound;
        flashTimer   = 0f;
        playerAnswer = -1;
    }

    public void render(SpriteBatch batch) {
        if (state == State.INACTIVE) return;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();

        float panelW = (difficulty == Difficulty.COMPLEX) ? 820f
                     : (difficulty == Difficulty.MEDIUM)  ? 720f
                     : 660f;
        float panelH = (difficulty == Difficulty.COMPLEX) ? 480f : 460f;
        float panelX = (sw - panelW) * 0.5f;
        float panelY = (sh - panelH) * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_PANEL_BG);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_PANEL_EDGE);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        float botPad     = 20f;
        float instrH     = 24f;
        float gapD       = 12f;
        float timerH     = 14f;
        float gapC       = 20f;
        float gateAreaH  = (difficulty == Difficulty.COMPLEX) ? 230f : 200f;
        float gapB       = 18f;
        float circleRowH = 28f;
        float gapA       = 12f;

        float instrBottom  = panelY + botPad;
        float timerBottom  = instrBottom + instrH + gapD;
        float gateAreaBot  = timerBottom + timerH + gapC;
        float gateAreaTop  = gateAreaBot + gateAreaH;
        float circleRowBot = gateAreaTop + gapB;
        float titleBottom  = circleRowBot + circleRowH + gapA;

        float gateAreaCX = panelX + panelW * 0.5f;
        float gateAreaCY = gateAreaBot + gateAreaH * 0.5f;

        float circR       = 12f;
        float circSpacing = 44f;
        float cStartX     = gateAreaCX - (totalRounds - 1) * circSpacing * 0.5f;
        float circCY      = circleRowBot + circleRowH * 0.5f;

        for (int i = 0; i < totalRounds; i++) {
            float cx = cStartX + i * circSpacing;
            Color fill;
            if (i < round || state == State.SHOWING_RESULT) {
                fill = roundResults[i] ? COL_SUCCESS : COL_FAIL;
            } else if (i == round && state != State.SHOWING_RESULT) {
                fill = new Color(0.88f, 0.84f, 0.10f, 1f);
            } else {
                fill = new Color(0.12f, 0.20f, 0.28f, 1f);
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fill);
            shapeRenderer.circle(cx, circCY, circR, 24);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(COL_PANEL_EDGE.r, COL_PANEL_EDGE.g, COL_PANEL_EDGE.b, 0.35f);
            shapeRenderer.circle(cx, circCY, circR, 24);
            shapeRenderer.end();
        }

        if (state == State.ROUND_FLASH && flashTimer > 0f) {
            float alpha = (flashTimer / FLASH_DURATION) * 0.28f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(
                    flashCorrect ? 0.10f : 0.90f,
                    flashCorrect ? 0.90f : 0.18f,
                    flashCorrect ? 0.30f : 0.18f,
                    alpha);
            shapeRenderer.rect(panelX + 20f, gateAreaBot, panelW - 40f, gateAreaH);
            shapeRenderer.end();
        }

        float timerBarW = panelW - 80f;
        float timerBarX = panelX + 40f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.12f, 0.16f, 1f);
        shapeRenderer.rect(timerBarX, timerBottom, timerBarW, timerH);
        shapeRenderer.end();
        if (state == State.RUNNING || state == State.ROUND_FLASH) {
            float ratio  = Math.max(0f, roundTimer / timePerRound);
            Color tColor = ratio > 0.40f
                    ? new Color(0.18f, 0.72f, 0.92f, 1f)
                    : new Color(0.95f, 0.38f, 0.18f, 1f);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(tColor);
            shapeRenderer.rect(timerBarX, timerBottom, timerBarW * ratio, timerH);
            shapeRenderer.end();
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.20f, 0.55f, 0.72f, 1f);
        shapeRenderer.rect(timerBarX, timerBottom, timerBarW, timerH);
        shapeRenderer.end();

        switch (difficulty) {
            case SIMPLE:  drawSimpleShapes (panelX, gateAreaCY); break;
            case MEDIUM:  drawMediumShapes (panelX, gateAreaCY); break;
            case COMPLEX: drawComplexShapes(panelX, gateAreaCY); break;
        }

        updateGatePositions(panelX, gateAreaCY);
        updateHover(sh);

        batch.begin();
        GlyphLayout layout = new GlyphLayout();

        font.getData().setScale(1.55f);
        font.setColor(COL_TITLE);
        layout.setText(font, "Soldering Table");
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                titleBottom + layout.height);

        font.getData().setScale(0.95f);
        Color diffCol;
        switch (difficulty) {
            case MEDIUM:  diffCol = COL_DIFF_MEDIUM;  break;
            case COMPLEX: diffCol = COL_DIFF_COMPLEX; break;
            default:      diffCol = COL_DIFF_SIMPLE;  break;
        }
        font.setColor(diffCol);
        font.draw(batch, "[" + difficulty.label + "]", panelX + 14f, panelY + panelH - 10f);

        font.getData().setScale(0.95f);
        font.setColor(0.40f, 0.62f, 0.78f, 1f);
        String hint = buildHintText()
                + "   (need " + passThreshold + "/" + totalRounds + ")"
                + "   " + (int) Math.ceil(Math.max(0f, roundTimer)) + "s";
        layout.setText(font, hint);
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                titleBottom - 50f);

        String instrText;
        if (state == State.SHOWING_RESULT) {
            if (finalSuccess) {
                instrText = "Soldered. Got: " + activeRecipe.getOutputItem().getDisplayName()
                        + " x" + activeRecipe.getOutputQuantity();
                font.getData().setScale(1.15f);
                font.setColor(COL_SUCCESS);
            } else {
                instrText = "Short circuit. (" + correctCount + "/" + totalRounds
                        + " correct, need " + passThreshold + ")";
                font.getData().setScale(1.10f);
                font.setColor(COL_FAIL);
            }
        } else if (state == State.ROUND_FLASH) {
            instrText = flashCorrect ? "Correct." : "Wrong. Answer was " + correctOutput;
            font.getData().setScale(1.15f);
            font.setColor(flashCorrect ? COL_SUCCESS : COL_FAIL);
        } else {
            instrText = "Left: output 0              Right: output 1";
            font.getData().setScale(1.20f);
            font.setColor(0.65f, 0.85f, 0.95f, 1f);
        }
        layout.setText(font, instrText);
        font.draw(batch, layout,
                panelX + (panelW - layout.width) * 0.5f,
                instrBottom + instrH);

        font.getData().setScale(1.0f);
        font.setColor(0.40f, 0.60f, 0.75f, 1f);
        String scoreStr = "Correct: " + correctCount + " / " + totalRounds;
        layout.setText(font, scoreStr);
        font.draw(batch, layout,
                panelX + panelW - layout.width - 14f,
                panelY + panelH - 10f);

        switch (difficulty) {
            case SIMPLE:  drawSimpleLabels (batch, layout, panelX, gateAreaCY); break;
            case MEDIUM:  drawMediumLabels (batch, layout, panelX, gateAreaCY); break;
            case COMPLEX: drawComplexLabels(batch, layout, panelX, gateAreaCY); break;
        }

        batch.end();
        drawGateTooltipShapes(sw, sh);
        batch.begin();
        drawGateTooltipLabels(batch, layout, sw, sh);

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }

    private void updateGatePositions(float panelX, float gateAreaCY) {
        switch (difficulty) {
            case SIMPLE:
                gatePosCX[0] = panelX + 330f;
                gatePosCY[0] = gateAreaCY;
                gatePosW = S_GW;
                gatePosH = S_GH;
                break;
            case MEDIUM:
                gatePosCX[0] = panelX + 185f;
                gatePosCY[0] = gateAreaCY + 38f;
                gatePosCX[1] = panelX + 490f;
                gatePosCY[1] = gateAreaCY - 18f;
                gatePosW = M_GW;
                gatePosH = M_GH;
                break;
            case COMPLEX:
                gatePosCX[0] = panelX + 195f;
                gatePosCY[0] = gateAreaCY + 62f;
                gatePosCX[1] = panelX + 195f;
                gatePosCY[1] = gateAreaCY - 62f;
                gatePosCX[2] = panelX + 510f;
                gatePosCY[2] = gateAreaCY;
                gatePosW = C_GW;
                gatePosH = C_GH;
                break;
        }
    }

    private void updateHover(int sh) {
        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();
        hoveredGateIdx = -1;
        int gc = difficulty.gateCount;
        float hw = gatePosW * 0.5f;
        float hh = gatePosH * 0.5f;
        for (int i = 0; i < gc; i++) {
            if (mx >= gatePosCX[i] - hw && mx <= gatePosCX[i] + hw
             && my >= gatePosCY[i] - hh && my <= gatePosCY[i] + hh) {
                hoveredGateIdx = i;
                break;
            }
        }
    }

    private void drawGateTooltipShapes(int sw, int sh) {
        if (hoveredGateIdx < 0 || state != State.RUNNING || !allowTooltip()) return;

        GateType g      = gateChain[hoveredGateIdx];
        boolean  single = g.isSingleInput();
        int      rows   = single ? 2 : 4;

        float rowH  = 30f;
        float ttW   = single ? 160f : 220f;
        float ttH   = 44f + rowH + rows * rowH + 14f;

        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();

        float ttX = mx + 18f;
        float ttY = my - ttH * 0.5f;
        ttX = Math.min(ttX, sw - ttW - 6f);
        ttY = Math.max(ttY, 6f);
        ttY = Math.min(ttY, sh - ttH - 6f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.03f, 0.06f, 0.10f, 0.97f);
        shapeRenderer.rect(ttX, ttY, ttW, ttH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_GATE_EDGE);
        shapeRenderer.rect(ttX, ttY, ttW, ttH);
        shapeRenderer.end();

        float dividerY = ttY + ttH - 44f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_GATE_EDGE.r, COL_GATE_EDGE.g, COL_GATE_EDGE.b, 0.45f);
        shapeRenderer.line(ttX + 8f, dividerY, ttX + ttW - 8f, dividerY);
        shapeRenderer.end();

        float colW  = ttW / (single ? 2f : 3f);
        float hdrY  = dividerY - rowH;
        for (int r = 0; r < rows; r++) {
            float rowY = hdrY - (r + 1) * rowH;
            int   a    = r / (single ? 1 : 2);
            int   b    = single ? 0 : r % 2;
            int   out  = g.evaluate(a, b);

            Color aCol  = a   == 1 ? COL_HIGH : COL_LOW;
            Color bCol  = b   == 1 ? COL_HIGH : COL_LOW;
            Color oCol  = out == 1 ? COL_HIGH : COL_LOW;

            float dotR = 5f;
            float dotX = ttX + colW * 0.5f;
            float dotY = rowY + rowH * 0.5f - dotR;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(aCol);
            shapeRenderer.rect(dotX - dotR, dotY, dotR * 2f, dotR * 2f);
            if (!single) {
                shapeRenderer.setColor(bCol);
                shapeRenderer.rect(ttX + colW * 1.5f - dotR, dotY, dotR * 2f, dotR * 2f);
            }
            shapeRenderer.setColor(oCol);
            shapeRenderer.rect(ttX + colW * (single ? 1.5f : 2.5f) - dotR, dotY, dotR * 2f, dotR * 2f);
            shapeRenderer.end();
        }
    }


    private boolean allowTooltip() {
        if (difficulty == Difficulty.SIMPLE) return true;

        return false;
    }


    private void drawGateTooltipLabels(SpriteBatch batch, GlyphLayout layout, int sw, int sh) {
        if (hoveredGateIdx < 0 || state != State.RUNNING || !allowTooltip()) return;

        GateType g      = gateChain[hoveredGateIdx];
        boolean  single = g.isSingleInput();
        int      rows   = single ? 2 : 4;

        float rowH = 30f;
        float ttW  = single ? 160f : 220f;
        float ttH  = 44f + rowH + rows * rowH + 14f;

        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();

        float ttX = mx + 18f;
        float ttY = my - ttH * 0.5f;
        ttX = Math.min(ttX, sw - ttW - 6f);
        ttY = Math.max(ttY, 6f);
        ttY = Math.min(ttY, sh - ttH - 6f);

        float colW = ttW / (single ? 2f : 3f);

        font.getData().setScale(0.95f);
        font.setColor(COL_TITLE);
        drawCenteredLabel(batch, layout, g.label + ": " + g.formula, ttX + ttW * 0.5f, ttY + ttH - 22f);

        float dividerY = ttY + ttH - 44f;
        float hdrY     = dividerY - rowH;

        font.getData().setScale(0.82f);
        font.setColor(0.60f, 0.82f, 0.95f, 1f);
        drawCenteredLabel(batch, layout, "A",   ttX + colW * 0.5f, hdrY + rowH * 0.5f + -4f);
        if (!single) drawCenteredLabel(batch, layout, "B",   ttX + colW * 1.5f, hdrY + rowH * 0.5f + -4f);
        drawCenteredLabel(batch, layout, "Out", ttX + colW * (single ? 1.5f : 2.5f), hdrY + rowH * 0.5f -4f);

        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
    }

    private String buildHintText() {
        if (difficulty == Difficulty.SIMPLE) return gateChain[0].formula;
        if (difficulty == Difficulty.MEDIUM) return gateChain[0].label + " into " + gateChain[1].label;
        return gateChain[0].label + " and " + gateChain[1].label + " into " + gateChain[2].label;
    }

    private void drawSimpleShapes(float panelX, float gateAreaCY) {
        boolean single = gateChain[0].isSingleInput();
        float gCX = panelX + 330f, gCY = gateAreaCY;
        float aCX = gCX - 220f,    aCY = single ? gCY : gCY + 24f;
        float bCX = gCX - 220f,    bCY = gCY - 24f;
        float oCX = gCX + 220f,    oCY = gCY;

        drawWireValue(aCX + S_NW * 0.5f, aCY,
                gCX - S_GW * 0.5f, single ? gCY : gCY + 16f, inputValues[0]);
        if (!single)
            drawWireValue(bCX + S_NW * 0.5f, bCY, gCX - S_GW * 0.5f, gCY - 16f, inputValues[1]);

        Color outWireCol = (state == State.ROUND_FLASH)
                ? (flashCorrect ? COL_SUCCESS : COL_FAIL) : COL_WIRE;
        drawWireColor(gCX + S_GW * 0.5f, gCY, oCX - S_OW * 0.5f, oCY, outWireCol);

        drawGateBox(gCX, gCY, S_GW, S_GH);
        drawNodeBox(aCX, aCY, S_NW, S_NH, inputValues[0]);
        if (!single) drawNodeBox(bCX, bCY, S_NW, S_NH, inputValues[1]);

        Color outCol = (state == State.ROUND_FLASH)
                ? (flashCorrect ? COL_SUCCESS : COL_FAIL) : COL_UNKNOWN;
        drawResultBox(oCX, oCY, S_OW, S_OH, outCol);
    }

    private void drawSimpleLabels(SpriteBatch batch, GlyphLayout layout, float panelX, float gateAreaCY) {
        boolean single = gateChain[0].isSingleInput();
        float gCX = panelX + 330f, gCY = gateAreaCY;
        float aCX = gCX - 220f,    aCY = single ? gCY : gCY + 24f;
        float bCX = gCX - 220f,    bCY = gCY - 24f;
        float oCX = gCX + 220f,    oCY = gCY;

        font.getData().setScale(1.45f);
        font.setColor(0.82f, 0.96f, 1.00f, 1f);
        drawCenteredLabel(batch, layout, gateChain[0].label, gCX, gCY);

        font.getData().setScale(1.30f);
        font.setColor(Color.WHITE);
        drawCenteredLabel(batch, layout, "A=" + inputValues[0], aCX, aCY);
        if (!single) drawCenteredLabel(batch, layout, "B=" + inputValues[1], bCX, bCY);

        font.getData().setScale(1.30f);
        if (state == State.ROUND_FLASH) {
            font.setColor(Color.WHITE);
            drawCenteredLabel(batch, layout, String.valueOf(correctOutput), oCX, oCY);
        } else {
            font.setColor(0.55f, 0.80f, 0.95f, 1f);
            drawCenteredLabel(batch, layout, "?", oCX, oCY);
        }
    }

    private void drawMediumShapes(float panelX, float gateAreaCY) {
        float g1CX = panelX + 185f, g1CY = gateAreaCY + 38f;
        float g2CX = panelX + 490f, g2CY = gateAreaCY - 18f;
        float aCX  = panelX + 62f,  aCY  = g1CY + 24f;
        float bCX  = panelX + 62f,  bCY  = g1CY - 24f;
        float iCX  = panelX + 338f, iCY  = (g1CY + g2CY) * 0.5f + 5f;
        float cCX  = panelX + 362f, cCY  = g2CY - 26f;
        float oCX  = panelX + 652f, oCY  = g2CY;

        drawWireValue(aCX + M_NW * 0.5f, aCY, g1CX - M_GW * 0.5f, g1CY + 14f, inputValues[0]);
        drawWireValue(bCX + M_NW * 0.5f, bCY, g1CX - M_GW * 0.5f, g1CY - 14f, inputValues[1]);

        boolean intRevealed = (state == State.ROUND_FLASH);
        Color r0Col = intRevealed ? (intermediates[0] == 1 ? COL_HIGH : COL_LOW) : COL_WIRE;
        drawWireColor(g1CX + M_GW * 0.5f, g1CY, iCX  - M_IW * 0.5f, iCY, r0Col);
        drawWireColor(iCX  + M_IW * 0.5f, iCY,  g2CX - M_GW * 0.5f, g2CY + 14f, r0Col);

        drawWireValue(cCX + M_NW * 0.5f, cCY, g2CX - M_GW * 0.5f, g2CY - 14f, inputValues[2]);

        Color outWireCol = (state == State.ROUND_FLASH)
                ? (flashCorrect ? COL_SUCCESS : COL_FAIL) : COL_WIRE;
        drawWireColor(g2CX + M_GW * 0.5f, g2CY, oCX - M_OW * 0.5f, oCY, outWireCol);

        drawGateBox(g1CX, g1CY, M_GW, M_GH);
        drawGateBox(g2CX, g2CY, M_GW, M_GH);
        drawNodeBox(aCX, aCY, M_NW, M_NH, inputValues[0]);
        drawNodeBox(bCX, bCY, M_NW, M_NH, inputValues[1]);
        drawNodeBox(cCX, cCY, M_NW, M_NH, inputValues[2]);

        Color intCol = intRevealed ? (intermediates[0] == 1 ? COL_HIGH : COL_LOW) : COL_UNKNOWN;
        drawResultBox(iCX, iCY, M_IW, M_IH, intCol);

        Color outCol = (state == State.ROUND_FLASH)
                ? (flashCorrect ? COL_SUCCESS : COL_FAIL) : COL_UNKNOWN;
        drawResultBox(oCX, oCY, M_OW, M_OH, outCol);
    }

    private void drawMediumLabels(SpriteBatch batch, GlyphLayout layout, float panelX, float gateAreaCY) {
        float g1CX = panelX + 185f, g1CY = gateAreaCY + 38f;
        float g2CX = panelX + 490f, g2CY = gateAreaCY - 18f;
        float aCX  = panelX + 62f,  aCY  = g1CY + 24f;
        float bCX  = panelX + 62f,  bCY  = g1CY - 24f;
        float iCX  = panelX + 338f, iCY  = (g1CY + g2CY) * 0.5f + 5f;
        float cCX  = panelX + 362f, cCY  = g2CY - 26f;
        float oCX  = panelX + 652f, oCY  = g2CY;
        boolean intRevealed = (state == State.ROUND_FLASH);

        font.getData().setScale(1.25f);
        font.setColor(0.82f, 0.96f, 1.00f, 1f);
        drawCenteredLabel(batch, layout, gateChain[0].label, g1CX, g1CY);
        drawCenteredLabel(batch, layout, gateChain[1].label, g2CX, g2CY);

        font.getData().setScale(1.10f);
        font.setColor(Color.WHITE);
        drawCenteredLabel(batch, layout, "A=" + inputValues[0], aCX, aCY);
        drawCenteredLabel(batch, layout, "B=" + inputValues[1], bCX, bCY);
        drawCenteredLabel(batch, layout, "C=" + inputValues[2], cCX, cCY);

        font.getData().setScale(1.00f);
        if (intRevealed) {
            font.setColor(Color.WHITE);
            drawCenteredLabel(batch, layout, String.valueOf(intermediates[0]), iCX, iCY);
        } else {
            font.setColor(0.55f, 0.80f, 0.95f, 1f);
            drawCenteredLabel(batch, layout, "?", iCX, iCY);
        }

        font.getData().setScale(1.10f);
        if (state == State.ROUND_FLASH) {
            font.setColor(Color.WHITE);
            drawCenteredLabel(batch, layout, String.valueOf(correctOutput), oCX, oCY);
        } else {
            font.setColor(0.55f, 0.80f, 0.95f, 1f);
            drawCenteredLabel(batch, layout, "?", oCX, oCY);
        }
    }

    private void drawComplexShapes(float panelX, float gateAreaCY) {
        float g1CX = panelX + 195f, g1CY = gateAreaCY + 62f;
        float g2CX = panelX + 195f, g2CY = gateAreaCY - 62f;
        float g3CX = panelX + 510f, g3CY = gateAreaCY;
        float aCX  = panelX + 58f,  aCY  = g1CY + 22f;
        float bCX  = panelX + 58f,  bCY  = g1CY - 22f;
        float cCX  = panelX + 58f,  cCY  = g2CY + 22f;
        float dCX  = panelX + 58f,  dCY  = g2CY - 22f;
        float r1CX = panelX + 355f, r1CY = g1CY - 8f;
        float r2CX = panelX + 355f, r2CY = g2CY + 8f;
        float oCX  = panelX + 710f, oCY  = g3CY;

        drawWireValue(aCX + C_NW * 0.5f, aCY, g1CX - C_GW * 0.5f, g1CY + 12f, inputValues[0]);
        drawWireValue(bCX + C_NW * 0.5f, bCY, g1CX - C_GW * 0.5f, g1CY - 12f, inputValues[1]);
        drawWireValue(cCX + C_NW * 0.5f, cCY, g2CX - C_GW * 0.5f, g2CY + 12f, inputValues[2]);
        drawWireValue(dCX + C_NW * 0.5f, dCY, g2CX - C_GW * 0.5f, g2CY - 12f, inputValues[3]);

        boolean intRevealed = (state == State.ROUND_FLASH);
        Color r1Col = intRevealed ? (intermediates[0] == 1 ? COL_HIGH : COL_LOW) : COL_WIRE;
        Color r2Col = intRevealed ? (intermediates[1] == 1 ? COL_HIGH : COL_LOW) : COL_WIRE;

        drawWireColor(g1CX + C_GW * 0.5f, g1CY,  r1CX - C_IW * 0.5f, r1CY, r1Col);
        drawWireColor(r1CX + C_IW * 0.5f, r1CY,  g3CX - C_GW * 0.5f, g3CY + 12f, r1Col);
        drawWireColor(g2CX + C_GW * 0.5f, g2CY,  r2CX - C_IW * 0.5f, r2CY, r2Col);
        drawWireColor(r2CX + C_IW * 0.5f, r2CY,  g3CX - C_GW * 0.5f, g3CY - 12f, r2Col);

        Color outWireCol = (state == State.ROUND_FLASH)
                ? (flashCorrect ? COL_SUCCESS : COL_FAIL) : COL_WIRE;
        drawWireColor(g3CX + C_GW * 0.5f, g3CY, oCX - C_OW * 0.5f, oCY, outWireCol);

        drawGateBox(g1CX, g1CY, C_GW, C_GH);
        drawGateBox(g2CX, g2CY, C_GW, C_GH);
        drawGateBox(g3CX, g3CY, C_GW, C_GH);
        drawNodeBox(aCX, aCY, C_NW, C_NH, inputValues[0]);
        drawNodeBox(bCX, bCY, C_NW, C_NH, inputValues[1]);
        drawNodeBox(cCX, cCY, C_NW, C_NH, inputValues[2]);
        drawNodeBox(dCX, dCY, C_NW, C_NH, inputValues[3]);

        Color i1Col = intRevealed ? (intermediates[0] == 1 ? COL_HIGH : COL_LOW) : COL_UNKNOWN;
        Color i2Col = intRevealed ? (intermediates[1] == 1 ? COL_HIGH : COL_LOW) : COL_UNKNOWN;
        drawResultBox(r1CX, r1CY, C_IW, C_IH, i1Col);
        drawResultBox(r2CX, r2CY, C_IW, C_IH, i2Col);

        Color outCol = (state == State.ROUND_FLASH)
                ? (flashCorrect ? COL_SUCCESS : COL_FAIL) : COL_UNKNOWN;
        drawResultBox(oCX, oCY, C_OW, C_OH, outCol);
    }

    private void drawComplexLabels(SpriteBatch batch, GlyphLayout layout, float panelX, float gateAreaCY) {
        float g1CX = panelX + 195f, g1CY = gateAreaCY + 62f;
        float g2CX = panelX + 195f, g2CY = gateAreaCY - 62f;
        float g3CX = panelX + 510f, g3CY = gateAreaCY;
        float aCX  = panelX + 58f,  aCY  = g1CY + 22f;
        float bCX  = panelX + 58f,  bCY  = g1CY - 22f;
        float cCX  = panelX + 58f,  cCY  = g2CY + 22f;
        float dCX  = panelX + 58f,  dCY  = g2CY - 22f;
        float r1CX = panelX + 355f, r1CY = g1CY - 8f;
        float r2CX = panelX + 355f, r2CY = g2CY + 8f;
        float oCX  = panelX + 710f, oCY  = g3CY;
        boolean intRevealed = (state == State.ROUND_FLASH);

        font.getData().setScale(1.10f);
        font.setColor(0.82f, 0.96f, 1.00f, 1f);
        drawCenteredLabel(batch, layout, gateChain[0].label, g1CX, g1CY);
        drawCenteredLabel(batch, layout, gateChain[1].label, g2CX, g2CY);
        drawCenteredLabel(batch, layout, gateChain[2].label, g3CX, g3CY);

        font.getData().setScale(0.95f);
        font.setColor(Color.WHITE);
        drawCenteredLabel(batch, layout, "A=" + inputValues[0], aCX, aCY);
        drawCenteredLabel(batch, layout, "B=" + inputValues[1], bCX, bCY);
        drawCenteredLabel(batch, layout, "C=" + inputValues[2], cCX, cCY);
        drawCenteredLabel(batch, layout, "D=" + inputValues[3], dCX, dCY);

        font.getData().setScale(0.90f);
        if (intRevealed) {
            font.setColor(Color.WHITE);
            drawCenteredLabel(batch, layout, String.valueOf(intermediates[0]), r1CX, r1CY);
            drawCenteredLabel(batch, layout, String.valueOf(intermediates[1]), r2CX, r2CY);
        } else {
            font.setColor(0.55f, 0.80f, 0.95f, 1f);
            drawCenteredLabel(batch, layout, "?", r1CX, r1CY);
            drawCenteredLabel(batch, layout, "?", r2CX, r2CY);
        }

        font.getData().setScale(0.95f);
        if (state == State.ROUND_FLASH) {
            font.setColor(Color.WHITE);
            drawCenteredLabel(batch, layout, String.valueOf(correctOutput), oCX, oCY);
        } else {
            font.setColor(0.55f, 0.80f, 0.95f, 1f);
            drawCenteredLabel(batch, layout, "?", oCX, oCY);
        }
    }

    private void drawGateBox(float cx, float cy, float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COL_GATE_BG);
        shapeRenderer.rect(cx - w * 0.5f, cy - h * 0.5f, w, h);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_GATE_EDGE);
        shapeRenderer.rect(cx - w * 0.5f, cy - h * 0.5f, w, h);
        shapeRenderer.end();
    }

    private void drawNodeBox(float cx, float cy, float w, float h, int value) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(value == 1 ? COL_HIGH : COL_LOW);
        shapeRenderer.rect(cx - w * 0.5f, cy - h * 0.5f, w, h);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 0.25f);
        shapeRenderer.rect(cx - w * 0.5f, cy - h * 0.5f, w, h);
        shapeRenderer.end();
    }

    private void drawResultBox(float cx, float cy, float w, float h, Color col) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(col);
        shapeRenderer.rect(cx - w * 0.5f, cy - h * 0.5f, w, h);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COL_GATE_EDGE);
        shapeRenderer.rect(cx - w * 0.5f, cy - h * 0.5f, w, h);
        shapeRenderer.end();
    }

    private void drawWireValue(float x1, float y1, float x2, float y2, int value) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(value == 1 ? COL_HIGH : COL_LOW);
        shapeRenderer.line(x1, y1, x2, y2);
        shapeRenderer.end();
    }

    private void drawWireColor(float x1, float y1, float x2, float y2, Color col) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(col);
        shapeRenderer.line(x1, y1, x2, y2);
        shapeRenderer.end();
    }

    private void drawCenteredLabel(SpriteBatch batch, GlyphLayout layout, String text, float cx, float cy) {
        layout.setText(font, text);
        font.draw(batch, layout, cx - layout.width * 0.5f, cy + layout.height * 0.5f);
    }

    public void resize(int w, int h) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}