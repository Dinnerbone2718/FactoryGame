package com.factory.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.WorldManager;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class AnimalStatsUI {

    public static final int TOGGLE_KEY = Input.Keys.P;

    private static final float SAMPLE_INTERVAL = 1.0f;
    private static final int HISTORY_LENGTH = 300;

    private static final Color PANEL_BG = new Color(0.06f, 0.06f, 0.08f, 0.90f);
    private static final Color PANEL_BORDER = new Color(1f, 1f, 1f, 0.18f);
    private static final Color DIM_BG = new Color(0f, 0f, 0f, 0.45f);
    private static final Color GRID_COLOR = new Color(1f, 1f, 1f, 0.07f);
    private static final Color AXIS_COLOR = new Color(1f, 1f, 1f, 0.30f);
    private static final Color TRACK_COLOR = new Color(1f, 1f, 1f, 0.12f);

    private static final Color HUNGER_COLOR = new Color(
        0.95f,
        0.65f,
        0.25f,
        1f
    );
    private static final Color THIRST_COLOR = new Color(
        0.35f,
        0.65f,
        0.95f,
        1f
    );
    private static final Color HEALTH_COLOR = new Color(
        0.85f,
        0.30f,
        0.30f,
        1f
    );

    private static final Color AVG_AGE_COLOR = new Color(1.0f, 1.0f, 1.0f, 1f);

    private static final Color VISION_COLOR = new Color(
        0.40f,
        0.90f,
        0.85f,
        1f
    );
    private static final Color SPEED_COLOR = new Color(0.75f, 0.55f, 0.95f, 1f);
    private static final Color STOMACH_COLOR = new Color(
        0.95f,
        0.85f,
        0.40f,
        1f
    );
    private static final Color AGGRESSION_COLOR = new Color(
        0.95f,
        0.35f,
        0.55f,
        1f
    );
    private static final Color REPRO_COLOR = new Color(0.55f, 0.95f, 0.55f, 1f);
    private static final Color RESILIENCE_COLOR = new Color(
        0.95f,
        0.75f,
        0.35f,
        1f
    );
    private static final Color MAX_AGE_COLOR = new Color(
        0.70f,
        0.70f,
        0.95f,
        1f
    );

    private static final Map<Animal.Type, Color> SPECIES_COLOR = new EnumMap<>(
        Animal.Type.class
    );

    static {
        SPECIES_COLOR.put(Animal.Type.COW, new Color(0.95f, 0.78f, 0.35f, 1f));
        SPECIES_COLOR.put(
            Animal.Type.ARMADILLO,
            new Color(0.55f, 0.85f, 0.55f, 1f)
        );
        SPECIES_COLOR.put(Animal.Type.WOLF, new Color(0.85f, 0.40f, 0.40f, 1f));
    }

    private static final class History {

        final int[] population = new int[HISTORY_LENGTH];
        final float[] avgVision = new float[HISTORY_LENGTH];
        int writeIndex = 0;
        int filled = 0;

        int currentPopulation = 0;
        float currentVision = 0f;
        float currentSpeed = 0f;
        float currentStomach = 0f;
        float currentHunger = 0f;
        float currentThirst = 0f;
        float currentHealth = 0f;
        float currentAggression = 0f;
        float currentReproRate = 0f;
        float currentResilience = 0f;
        float currentMaxAge = 0f;
        float currentAge = 0f;

        void push(
            int pop,
            float vision,
            float speed,
            float stomach,
            float hunger,
            float thirst,
            float health,
            float aggression,
            float reproRate,
            float resilience,
            float maxAge,
            float avgAge
        ) {
            population[writeIndex] = pop;
            avgVision[writeIndex] = vision;
            writeIndex = (writeIndex + 1) % HISTORY_LENGTH;
            filled = Math.min(HISTORY_LENGTH, filled + 1);

            currentPopulation = pop;
            currentVision = vision;
            currentSpeed = speed;
            currentStomach = stomach;
            currentHunger = hunger;
            currentThirst = thirst;
            currentHealth = health;
            currentAggression = aggression;
            currentReproRate = reproRate;
            currentResilience = resilience;
            currentMaxAge = maxAge;
            currentAge = avgAge;
        }

        int oldestIndex() {
            return filled < HISTORY_LENGTH ? 0 : writeIndex;
        }
    }

    private final WorldManager worldManager;
    private final Map<Animal.Type, History> histories = new EnumMap<>(
        Animal.Type.class
    );

    private boolean visible = false;
    private float sampleTimer = 0f;
    private int focusedIndex = 0;

    private int screenWidth = 1280;
    private int screenHeight = 720;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    public AnimalStatsUI(WorldManager worldManager) {
        this.worldManager = worldManager;
        for (Animal.Type type : Animal.Type.values())
            histories.put(type, new History());
    }

    public boolean isVisible() {
        return visible;
    }

    public void toggle() {
        visible = !visible;
    }

    public void close() {
        visible = false;
    }

    public void handleInput() {
        if (!visible) return;

        if (
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(TOGGLE_KEY)
        ) {
            visible = false;
            return;
        }

        int count = Animal.Type.values().length;
        if (
            Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) ||
            Gdx.input.isKeyJustPressed(Input.Keys.D)
        ) {
            focusedIndex = (focusedIndex + 1) % count;
        }
        for (int i = 0; i < count && i < 9; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) focusedIndex =
                i;
        }
    }

    public void update(float delta) {
        sampleTimer += delta;
        if (sampleTimer < SAMPLE_INTERVAL) return;
        sampleTimer -= SAMPLE_INTERVAL;
        recordSample();
    }

    private void recordSample() {
        Map<Animal.Type, int[]> counts = new EnumMap<>(Animal.Type.class);
        Map<Animal.Type, float[]> sums = new EnumMap<>(Animal.Type.class);
        for (Animal.Type type : Animal.Type.values()) {
            counts.put(type, new int[] { 0 });
            sums.put(type, new float[11]);
        }

        for (Animal animal : worldManager.getAnimals()) {
            if (animal.isDead()) continue;
            Animal.Type type = animal.type;
            counts.get(type)[0]++;

            float[] s = sums.get(type);
            s[0] += animal.getVisionTiles();
            s[1] += animal.getSpeedMultiplier();
            s[2] += clamp01(animal.getHunger() / type.maxHunger);
            s[3] += clamp01(animal.getThirst() / type.maxThirst);
            s[4] += clamp01(animal.getHealth() / type.maxHealth);
            s[5] += animal.getStomachMultiplier();
            s[6] += animal.getAggressionMultiplier();
            s[7] += animal.getReproductiveRate();
            s[8] += animal.getResilienceMultiplier();
            s[9] += animal.getMaxAge();
            s[10] += clamp01(animal.getAge() / type.baseMaxAge);
        }

        for (Animal.Type type : Animal.Type.values()) {
            int n = counts.get(type)[0];
            float[] s = sums.get(type);
            float invN = n > 0 ? 1f / n : 0f;
            histories
                .get(type)
                .push(
                    n,
                    s[0] * invN,
                    s[1] * invN,
                    s[5] * invN,
                    s[2] * invN,
                    s[3] * invN,
                    s[4] * invN,
                    s[6] * invN,
                    s[7] * invN,
                    s[8] * invN,
                    s[9] * invN,
                    s[10] * invN
                );
        }
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void render(SpriteBatch batch) {
        if (!visible) return;

        float panelW = Math.min(900f, screenWidth * 0.85f);
        float panelH = Math.min(680f, screenHeight * 0.94f);
        float panelX = (screenWidth - panelW) * 0.5f;
        float panelY = (screenHeight - panelH) * 0.5f;

        float graphX = panelX + 24f;
        float graphY = panelY + 96f;
        float graphW = panelW * 0.60f;
        float graphH = panelH - 160f;

        float detailX = graphX + graphW + 28f;
        float detailW = (panelX + panelW - 20f) - detailX;
        float detailY = graphY;
        float detailH = graphH;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        drawBackgroundAndGraph(
            panelX,
            panelY,
            panelW,
            panelH,
            graphX,
            graphY,
            graphW,
            graphH
        );
        drawDetailPanel(detailX, detailY, detailW, detailH);

        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        drawAllText(
            panelX,
            panelY,
            panelW,
            panelH,
            graphX,
            graphY,
            graphW,
            graphH,
            detailX,
            detailW,
            batch
        );
    }

    private void drawBackgroundAndGraph(
        float panelX,
        float panelY,
        float panelW,
        float panelH,
        float graphX,
        float graphY,
        float graphW,
        float graphH
    ) {
        float maxPop = 1f;
        for (Animal.Type type : Animal.Type.values()) {
            History hist = histories.get(type);
            for (int i = 0; i < hist.filled; i++) {
                int idx = (hist.oldestIndex() + i) % HISTORY_LENGTH;
                maxPop = Math.max(maxPop, hist.population[idx]);
            }
        }
        maxPop *= 1.15f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(DIM_BG);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.setColor(PANEL_BG);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);

        shapeRenderer.setColor(GRID_COLOR);
        for (int p = 1; p <= 4; p++) {
            float gy = graphY + graphH * (p / 4f);
            shapeRenderer.rect(graphX, gy, graphW, 1f);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(PANEL_BORDER);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.setColor(AXIS_COLOR);
        shapeRenderer.line(graphX, graphY, graphX, graphY + graphH);
        shapeRenderer.line(graphX, graphY, graphX + graphW, graphY);

        for (Animal.Type type : Animal.Type.values()) {
            History hist = histories.get(type);
            if (hist.filled < 2) continue;

            shapeRenderer.setColor(SPECIES_COLOR.get(type));
            int n = hist.filled;
            for (int i = 0; i < n - 1; i++) {
                int idx0 = (hist.oldestIndex() + i) % HISTORY_LENGTH;
                int idx1 = (hist.oldestIndex() + i + 1) % HISTORY_LENGTH;

                float x0 = graphX + graphW * (i / (float) (HISTORY_LENGTH - 1));
                float x1 =
                    graphX + graphW * ((i + 1) / (float) (HISTORY_LENGTH - 1));
                float y0 = graphY + graphH * (hist.population[idx0] / maxPop);
                float y1 = graphY + graphH * (hist.population[idx1] / maxPop);

                shapeRenderer.line(x0, y0, x1, y1);
            }
        }
        shapeRenderer.end();
    }

    private void drawDetailPanel(float x, float y, float w, float h) {
        Animal.Type focused = Animal.Type.values()[focusedIndex];
        History hist = histories.get(focused);

        float barH = 22f;
        float barGap = 34f;
        float barY = y + h - barH;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawBar(x, barY, hist.currentHunger, w, barH, HUNGER_COLOR);
        drawBar(x, barY - barGap, hist.currentThirst, w, barH, THIRST_COLOR);
        drawBar(
            x,
            barY - barGap * 2,
            hist.currentHealth,
            w,
            barH,
            HEALTH_COLOR
        );

        float visionFrac = normalize(
            hist.currentVision,
            Animal.getMinVisionTiles(),
            Animal.getMaxVisionTiles()
        );
        float speedFrac = normalize(
            hist.currentSpeed,
            Animal.getMinSpeedMultiplier(),
            Animal.getMaxSpeedMultiplier()
        );
        float stomachFrac = normalize(
            hist.currentStomach,
            Animal.getMinStomachMultiplier(),
            Animal.getMaxStomachMultiplier()
        );
        float aggrFrac = normalize(
            hist.currentAggression,
            Animal.getMinAggressionMultiplier(),
            Animal.getMaxAggressionMultiplier()
        );
        float reproFrac = normalize(
            hist.currentReproRate,
            Animal.getMinReproductiveRate(),
            Animal.getMaxReproductiveRate()
        );
        float resiliFrac = normalize(
            hist.currentResilience,
            Animal.getMinResilienceMultiplier(),
            Animal.getMaxResilienceMultiplier()
        );

        drawBar(x, barY - barGap * 3.4f, visionFrac, w, barH, VISION_COLOR);
        drawBar(x, barY - barGap * 4.4f, speedFrac, w, barH, SPEED_COLOR);
        drawBar(
            x,
            barY - barGap * 5.4f,
            hist.currentAge,
            w,
            barH,
            AVG_AGE_COLOR
        );
        drawBar(x, barY - barGap * 6.4f, stomachFrac, w, barH, STOMACH_COLOR);
        drawBar(x, barY - barGap * 7.4f, aggrFrac, w, barH, AGGRESSION_COLOR);
        drawBar(x, barY - barGap * 8.4f, reproFrac, w, barH, REPRO_COLOR);
        drawBar(x, barY - barGap * 9.4f, resiliFrac, w, barH, RESILIENCE_COLOR);

        float maxAgeFrac = normalize(
            hist.currentMaxAge,
            Animal.getMinMaxAge(),
            Animal.getMaxMaxAge()
        );
        drawBar(x, barY - barGap * 10.4f, maxAgeFrac, w, barH, MAX_AGE_COLOR);

        shapeRenderer.end();

        float sparkY = y;
        float sparkH = barGap * 1.6f;
        float sparkW = w;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(TRACK_COLOR);
        shapeRenderer.rect(x, sparkY, sparkW, sparkH);

        if (hist.filled >= 2) {
            shapeRenderer.setColor(VISION_COLOR);
            int n = hist.filled;
            for (int i = 0; i < n - 1; i++) {
                int idx0 = (hist.oldestIndex() + i) % HISTORY_LENGTH;
                int idx1 = (hist.oldestIndex() + i + 1) % HISTORY_LENGTH;

                float v0 = normalize(
                    hist.avgVision[idx0],
                    Animal.getMinVisionTiles(),
                    Animal.getMaxVisionTiles()
                );
                float v1 = normalize(
                    hist.avgVision[idx1],
                    Animal.getMinVisionTiles(),
                    Animal.getMaxVisionTiles()
                );

                float x0 = x + sparkW * (i / (float) (HISTORY_LENGTH - 1));
                float x1 =
                    x + sparkW * ((i + 1) / (float) (HISTORY_LENGTH - 1));
                float y0 = sparkY + sparkH * v0;
                float y1 = sparkY + sparkH * v1;

                shapeRenderer.line(x0, y0, x1, y1);
            }
        }
        shapeRenderer.end();
    }

    private void drawBar(
        float x,
        float y,
        float fraction,
        float w,
        float h,
        Color color
    ) {
        fraction = clamp01(fraction);
        shapeRenderer.setColor(TRACK_COLOR);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, w * fraction, h);
    }

    private static float normalize(float v, float min, float max) {
        if (max <= min) return 0f;
        return Math.max(0f, Math.min(1f, (v - min) / (max - min)));
    }

    private void drawAllText(
        float panelX,
        float panelY,
        float panelW,
        float panelH,
        float graphX,
        float graphY,
        float graphW,
        float graphH,
        float detailX,
        float detailW,
        SpriteBatch batch
    ) {
        font.setColor(Color.WHITE);

        layout.setText(font, "Wildlife Population & Stats");
        font.draw(
            batch,
            "Wildlife Population & Stats",
            panelX + (panelW - layout.width) * 0.5f,
            panelY + panelH - 16f
        );

        float legendX = graphX + graphW - 150f;
        float legendY = graphY + graphH + 28f;
        int row = 0;
        for (Animal.Type type : Animal.Type.values()) {
            History hist = histories.get(type);
            font.setColor(SPECIES_COLOR.get(type));
            font.draw(
                batch,
                type.name() + ": " + hist.currentPopulation,
                legendX,
                legendY - row * 16f
            );
            row++;
        }

        font.setColor(new Color(1f, 1f, 1f, 0.6f));
        font.draw(
            batch,
            "Population",
            graphX,
            graphY + graphH + 28f - 16f * Animal.Type.values().length - 6f
        );
        font.draw(batch, "0", graphX - 14f, graphY + 4f);

        Animal.Type focused = Animal.Type.values()[focusedIndex];
        History hist = histories.get(focused);

        font.setColor(SPECIES_COLOR.get(focused));
        font.draw(
            batch,
            focused.name() + "  (pop " + hist.currentPopulation + ")",
            detailX,
            graphY + graphH + 28f
        );

        float barH = 22f;
        float barGap = 34f;
        float barY = graphY + graphH - barH;

        drawBarLabel(batch, detailX, barY, "Hunger " + pct(hist.currentHunger));
        drawBarLabel(
            batch,
            detailX,
            barY - barGap,
            "Thirst " + pct(hist.currentThirst)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 2,
            "Health " + pct(hist.currentHealth)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 3.4f,
            "Vision " +
                String.format(Locale.US, "%.1f tiles", hist.currentVision)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 4.4f,
            "Speed x" + String.format(Locale.US, "%.2f", hist.currentSpeed)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 5.4f,
            "Avg Age " + pct(hist.currentAge)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 6.4f,
            "Stomach x" + String.format(Locale.US, "%.2f", hist.currentStomach)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 7.4f,
            "Aggression x" +
                String.format(Locale.US, "%.2f", hist.currentAggression)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 8.4f,
            "Repro Rate x" +
                String.format(Locale.US, "%.2f", hist.currentReproRate)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 9.4f,
            "Resilience x" +
                String.format(Locale.US, "%.2f", hist.currentResilience)
        );
        drawBarLabel(
            batch,
            detailX,
            barY - barGap * 10.4f,
            "Max Age " + String.format(Locale.US, "%.0fs", hist.currentMaxAge)
        );

        font.setColor(new Color(1f, 1f, 1f, 0.5f));
        font.draw(
            batch,
            "Avg. vision over time",
            detailX,
            graphY + (barGap * 1.6f) + 14f
        );

        font.setColor(new Color(1f, 1f, 1f, 0.55f));
        String hint =
            "1-" +
            Animal.Type.values().length +
            " switch species    P / Esc close";
        layout.setText(font, hint);
        font.draw(
            batch,
            hint,
            panelX + (panelW - layout.width) * 0.5f,
            panelY + 18f
        );
    }

    private void drawBarLabel(
        SpriteBatch batch,
        float x,
        float barY,
        String text
    ) {
        font.setColor(Color.WHITE);
        font.draw(batch, text, x + 6f, barY + 16f);
    }

    private static String pct(float frac) {
        return String.format(Locale.US, "%.0f%%", clamp01(frac) * 100f);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
