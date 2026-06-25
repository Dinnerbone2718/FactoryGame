package com.factory.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.factory.game.Camera;
import com.factory.game.Main;
import com.factory.game.WorldManager;
import java.util.List;
import java.util.Random;

public class Animal {

    public interface CollisionChecker {
        boolean isBlocked(float x0, float y0, float x1, float y1);
    }

    public enum Type {
        COW(
            AnimalSpriteCache.Kind.COW,
            AnimalSpriteCache.Diet.HERBIVORE,
            40f,
            20f,
            5f,
            30f,
            0.325f,
            0.0f,
            0.85f,
            0.85f,
            0,
            0,
            WorldObject.Type.GRASS,
            null,
            0f,
            0f,
            0f,
            15f,
            500f
        ),
        ARMADILLO(
            AnimalSpriteCache.Kind.ARMADILLO,
            AnimalSpriteCache.Diet.HERBIVORE,
            10f,
            20f,
            30f,
            15f,
            0.075f,
            0.0f,
            0.85f,
            0.85f,
            0,
            0,
            null,
            null,
            0f,
            0f,
            0f,
            18f,
            220f
        ),
        WOLF(
            AnimalSpriteCache.Kind.WOLF,
            AnimalSpriteCache.Diet.CARNIVORE,
            50f,
            5f,
            5f,
            40f,
            0.325f,
            0.0f,
            0.85f,
            0.85f,
            0,
            0,
            null,
            Animal.Type.COW,
            4f,
            1.5f,
            0.8f,
            5f,
            400f
        );

        public final AnimalSpriteCache.Kind spriteKind;
        public final AnimalSpriteCache.Diet diet;
        public final float speed, maxHunger, maxThirst, maxHealth;
        public final float colOffsetX, colOffsetY, colWidth, colHeight;
        public final int pathExtraX, pathExtraY;
        public final WorldObject.Type foodType;
        public final Animal.Type prey;
        public final float attackDamage, attackRange, attackCooldown;
        public final float baseVisionTiles;
        public final float baseMaxAge;

        Type(
            AnimalSpriteCache.Kind spriteKind,
            AnimalSpriteCache.Diet diet,
            float speed,
            float maxHunger,
            float maxThirst,
            float maxHealth,
            float colOffsetX,
            float colOffsetY,
            float colWidth,
            float colHeight,
            int pathExtraX,
            int pathExtraY,
            WorldObject.Type foodType,
            Animal.Type prey,
            float attackDamage,
            float attackRange,
            float attackCooldown,
            float baseVisionTiles,
            float baseMaxAge
        ) {
            this.spriteKind = spriteKind;
            this.diet = diet;
            this.speed = speed;
            this.maxHunger = maxHunger;
            this.maxThirst = maxThirst;
            this.maxHealth = maxHealth;
            this.colOffsetX = colOffsetX;
            this.colOffsetY = colOffsetY;
            this.colWidth = colWidth;
            this.colHeight = colHeight;
            this.pathExtraX = pathExtraX;
            this.pathExtraY = pathExtraY;
            this.foodType = foodType;
            this.prey = prey;
            this.attackDamage = attackDamage;
            this.attackRange = attackRange;
            this.attackCooldown = attackCooldown;
            this.baseVisionTiles = baseVisionTiles;
            this.baseMaxAge = baseMaxAge;
        }
    }

    public static final class Genes {

        public final float visionTiles;
        public final float speedMultiplier;
        public final float stomachMultiplier;
        public final float aggressionMultiplier;
        public final float reproductiveRate;
        public final float resilienceMultiplier;
        public final float maxAge;

        public Genes(
            float visionTiles,
            float speedMultiplier,
            float stomachMultiplier,
            float aggressionMultiplier,
            float reproductiveRate,
            float resilienceMultiplier,
            float maxAge
        ) {
            this.visionTiles = clamp(
                visionTiles,
                MIN_VISION_TILES,
                MAX_VISION_TILES
            );
            this.speedMultiplier = clamp(
                speedMultiplier,
                MIN_SPEED_MULT,
                MAX_SPEED_MULT
            );
            this.stomachMultiplier = clamp(
                stomachMultiplier,
                MIN_STOMACH_MULT,
                MAX_STOMACH_MULT
            );
            this.aggressionMultiplier = clamp(
                aggressionMultiplier,
                MIN_AGGRESSION_MULT,
                MAX_AGGRESSION_MULT
            );
            this.reproductiveRate = clamp(
                reproductiveRate,
                MIN_REPRO_RATE,
                MAX_REPRO_RATE
            );
            this.resilienceMultiplier = clamp(
                resilienceMultiplier,
                MIN_RESILIENCE_MULT,
                MAX_RESILIENCE_MULT
            );
            this.maxAge = clamp(maxAge, MIN_MAX_AGE, MAX_MAX_AGE);
        }
    }

    private static final float MIN_VISION_TILES = 1f;
    private static final float MAX_VISION_TILES = 65f;
    private static final float MIN_SPEED_MULT = 0.4f;
    private static final float MAX_SPEED_MULT = 2.5f;
    private static final float MIN_STOMACH_MULT = 0.4f;
    private static final float MAX_STOMACH_MULT = 2.5f;
    private static final float MIN_AGGRESSION_MULT = 0.3f;
    private static final float MAX_AGGRESSION_MULT = 2.5f;
    private static final float MIN_REPRO_RATE = 0.3f;
    private static final float MAX_REPRO_RATE = 2.5f;
    private static final float MIN_RESILIENCE_MULT = 0.3f;
    private static final float MAX_RESILIENCE_MULT = 2.5f;
    private static final float MIN_MAX_AGE = 100f;
    private static final float MAX_MAX_AGE = 1300f;

    public static float getMinVisionTiles() {
        return MIN_VISION_TILES;
    }

    public static float getMaxVisionTiles() {
        return MAX_VISION_TILES;
    }

    public static float getMinSpeedMultiplier() {
        return MIN_SPEED_MULT;
    }

    public static float getMaxSpeedMultiplier() {
        return MAX_SPEED_MULT;
    }

    public static float getMinStomachMultiplier() {
        return MIN_STOMACH_MULT;
    }

    public static float getMaxStomachMultiplier() {
        return MAX_STOMACH_MULT;
    }

    public static float getMinAggressionMultiplier() {
        return MIN_AGGRESSION_MULT;
    }

    public static float getMaxAggressionMultiplier() {
        return MAX_AGGRESSION_MULT;
    }

    public static float getMinReproductiveRate() {
        return MIN_REPRO_RATE;
    }

    public static float getMaxReproductiveRate() {
        return MAX_REPRO_RATE;
    }

    public static float getMinResilienceMultiplier() {
        return MIN_RESILIENCE_MULT;
    }

    public static float getMaxResilienceMultiplier() {
        return MAX_RESILIENCE_MULT;
    }

    public static float getMinMaxAge() {
        return MIN_MAX_AGE;
    }

    public static float getMaxMaxAge() {
        return MAX_MAX_AGE;
    }

    private static final float DENSITY_CHECK_INTERVAL = 3.0f;
    private static final float DENSITY_RADIUS_TILES = 10f;

    private static final float VISION_MUTATION_RANGE = 20f;
    private static final float SPEED_MUTATION_RANGE = 0.5f;
    private static final float STOMACH_MUTATION_RANGE = 0.5f;
    private static final float AGGRESSION_MUTATION_RANGE = 0.5f;
    private static final float REPRO_MUTATION_RANGE = 0.50f;
    private static final float RESILIENCE_MUTATION_RANGE = 0.5f;
    private static final float MAX_AGE_MUTATION_RANGE = 35f;

    private static final float INITIAL_VISION_VARIANCE = 10f;
    private static final float INITIAL_SPEED_VARIANCE = 0.15f;
    private static final float INITIAL_STOMACH_VARIANCE = 0.25f;
    private static final float INITIAL_AGGRESSION_VARIANCE = 0.30f;
    private static final float INITIAL_REPRO_VARIANCE = 0.5f;
    private static final float INITIAL_RESILIENCE_VARIANCE = 0.25f;
    private static final float INITIAL_MAX_AGE_VARIANCE = 40f;

    private static final float VISION_ENERGY_COST_PER_TILE = 0.006f;
    private static final float SPEED_ENERGY_COST_FACTOR = 0.5f;
    private static final float STOMACH_ENERGY_COST_FACTOR = 0.35f;
    private static final float AGGRESSION_ENERGY_COST_FACTOR = 0.20f;
    private static final float RESILIENCE_ENERGY_COST_FACTOR = 0.25f;
    private static final float MIN_METABOLIC_MULTIPLIER = 0.5f;
    private static final float MAX_METABOLIC_MULTIPLIER = 3.0f;

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float mutate(float average, float range, Random rng) {
        return average + (rng.nextFloat() * 2f - 1f) * range;
    }

    public static Genes randomInitialGenes(Type type, Random rng) {
        float vision =
            type.baseVisionTiles +
            (rng.nextFloat() * 2f - 1f) * INITIAL_VISION_VARIANCE;
        float speed = 1f + (rng.nextFloat() * 2f - 1f) * INITIAL_SPEED_VARIANCE;
        float stomach =
            1f + (rng.nextFloat() * 2f - 1f) * INITIAL_STOMACH_VARIANCE;
        float aggr =
            1f + (rng.nextFloat() * 2f - 1f) * INITIAL_AGGRESSION_VARIANCE;
        float repro = 1f + (rng.nextFloat() * 2f - 1f) * INITIAL_REPRO_VARIANCE;
        float resil =
            1f + (rng.nextFloat() * 2f - 1f) * INITIAL_RESILIENCE_VARIANCE;
        float maxAge =
            type.baseMaxAge +
            (rng.nextFloat() * 2f - 1f) * INITIAL_MAX_AGE_VARIANCE;
        return new Genes(vision, speed, stomach, aggr, repro, resil, maxAge);
    }

    public static Genes inheritGenes(
        Animal parentA,
        Animal parentB,
        Random rng
    ) {
        float bias = 0.35f + rng.nextFloat() * 0.30f;
        float biasB = 1f - bias;

        float vision = mutate(
            parentA.visionTiles * bias + parentB.visionTiles * biasB,
            VISION_MUTATION_RANGE,
            rng
        );
        float speed = mutate(
            parentA.speedMultiplier * bias + parentB.speedMultiplier * biasB,
            SPEED_MUTATION_RANGE,
            rng
        );
        float stomach = mutate(
            parentA.stomachMultiplier * bias +
                parentB.stomachMultiplier * biasB,
            STOMACH_MUTATION_RANGE,
            rng
        );
        float aggr = mutate(
            parentA.aggressionMultiplier * bias +
                parentB.aggressionMultiplier * biasB,
            AGGRESSION_MUTATION_RANGE,
            rng
        );
        float repro = mutate(
            parentA.reproductiveRate * bias + parentB.reproductiveRate * biasB,
            REPRO_MUTATION_RANGE,
            rng
        );
        float resil = mutate(
            parentA.resilienceMultiplier * bias +
                parentB.resilienceMultiplier * biasB,
            RESILIENCE_MUTATION_RANGE,
            rng
        );

        if (rng.nextFloat() < 0.20f) vision +=
            (rng.nextFloat() * 2f - 1f) * VISION_MUTATION_RANGE * 3.5f;
        if (rng.nextFloat() < 0.20f) speed +=
            (rng.nextFloat() * 2f - 1f) * SPEED_MUTATION_RANGE * 3.5f;
        if (rng.nextFloat() < 0.20f) stomach +=
            (rng.nextFloat() * 2f - 1f) * STOMACH_MUTATION_RANGE * 3.5f;
        if (rng.nextFloat() < 0.20f) aggr +=
            (rng.nextFloat() * 2f - 1f) * AGGRESSION_MUTATION_RANGE * 3.5f;
        if (rng.nextFloat() < 0.20f) repro +=
            (rng.nextFloat() * 2f - 1f) * REPRO_MUTATION_RANGE * 3.5f;
        if (rng.nextFloat() < 0.20f) resil +=
            (rng.nextFloat() * 2f - 1f) * RESILIENCE_MUTATION_RANGE * 3.5f;

        float maxAge = mutate(
            parentA.maxAge * bias + parentB.maxAge * biasB,
            MAX_AGE_MUTATION_RANGE,
            rng
        );
        if (rng.nextFloat() < 0.20f) maxAge +=
            (rng.nextFloat() * 2f - 1f) * MAX_AGE_MUTATION_RANGE * 3.5f;

        return new Genes(vision, speed, stomach, aggr, repro, resil, maxAge);
    }

    private static final int DIR_RIGHT = 0;
    private static final int DIR_LEFT = 1;
    private static final int DIR_UP = 2;
    private static final int DIR_DOWN = 3;

    private static final float COW_RENDER_Y_OFFSET_DEFAULT = -24.0f;
    private static final float COW_RENDER_Y_OFFSET_UP = -8.0f;
    private static final float COW_RENDER_Y_OFFSET_DOWN = -8.0f;

    private static final float FRAME_DURATION = 0.18f;

    private static final float STATUS_BAR_WIDTH = 28f;
    private static final float STATUS_BAR_HEIGHT = 3f;
    private static final float STATUS_BAR_GAP = 1f;
    private static final float STATUS_BAR_Y_OFFSET = 6f;

    private static final float BAR_BG_R = 0.10f,
        BAR_BG_G = 0.10f,
        BAR_BG_B = 0.10f,
        BAR_BG_A = 0.75f;
    private static final float HUNGER_R = 0.90f,
        HUNGER_G = 0.55f,
        HUNGER_B = 0.15f;
    private static final float THIRST_R = 0.25f,
        THIRST_G = 0.55f,
        THIRST_B = 0.95f;
    private static final float HEALTH_R = 0.85f,
        HEALTH_G = 0.15f,
        HEALTH_B = 0.15f;

    private static final float IDLE_MIN = 1.0f;
    private static final float IDLE_MAX = 3.0f;
    private static final float WALK_MIN = 1.5f;
    private static final float WALK_MAX = 4.0f;

    private static final float PATH_RECOMP_INTERVAL = 3.5f;

    private static final float MAX_HAPPINESS = 100f;

    private static final float CONTENT_HUNGER_FRACTION = 0.50f;
    private static final float CONTENT_THIRST_FRACTION = 0.50f;

    private static final float HAPPINESS_GAIN_PER_SEC = MAX_HAPPINESS / 60f;
    private static final float HAPPINESS_DECAY_PER_SEC = MAX_HAPPINESS / 60f;

    private static final float DENSITY_SWEET_SPOT = 2f;
    private static final float DENSITY_PEAK_BONUS_RATE =
        HAPPINESS_GAIN_PER_SEC * 0.6f;
    private static final float DENSITY_MAX_PENALTY_RATE =
        -HAPPINESS_DECAY_PER_SEC * 1.2f;

    private static final float BREEDING_COOLDOWN_SECONDS = 10f;

    private static final float FORAGE_HUNGER_RESTORE_FRACTION = 0.80f;
    private static final float FORAGE_THIRST_RESTORE_FRACTION = 0.50f;
    private static final float HUNT_HUNGER_RESTORE_FRACTION = 0.85f;

    private enum State {
        IDLE,
        WALKING,
        THIRST,
        HUNGERY,
        HUNTING,
        MATING,
    }

    public final Type type;

    private float worldX, worldY;

    private int direction;
    private int animFrame = 0;
    private float animTimer = 0f;

    private State state;
    private float stateTimer;

    private float hunger = 0;
    private float happiness = 0;
    private float thirst = 0;
    private float timeSinceLastBirth = 0;
    private float age = 0;

    private float health;
    private float attackCooldownTimer = 0f;

    private final float visionTiles;
    private final float speedMultiplier;
    private final float stomachMultiplier;
    private final float aggressionMultiplier;
    private final float reproductiveRate;
    private final float resilienceMultiplier;
    private final float maxAge;

    private float[] water = null;
    private WorldObject food = null;
    private Animal huntTarget = null;
    private Animal mateTarget = null;

    private float densityCheckTimer = 0f;
    private int cachedNearbyCount = 0;

    private List<float[]> currentPath = null;
    private int pathIndex = 0;
    private float pathTimer = 0f;
    private State pathForState = null;
    private WorldObject pathFoodTarget = null;
    private Animal pathHuntTarget = null;
    private Animal pathMateTarget = null;
    private float pathWaterX = Float.NaN;
    private float pathWaterY = Float.NaN;

    private final Random rng;

    public Animal(Type type, float worldX, float worldY, long randomSeed) {
        this(
            type,
            worldX,
            worldY,
            randomSeed,
            new Genes(type.baseVisionTiles, 1f, 1f, 1f, 1f, 1f, type.baseMaxAge)
        );
    }

    public Animal(
        Type type,
        float worldX,
        float worldY,
        long randomSeed,
        Genes genes
    ) {
        this.type = type;
        this.worldX = worldX;
        this.worldY = worldY;
        this.rng = new Random(randomSeed);
        this.direction = rng.nextInt(4);
        this.state = State.IDLE;
        this.stateTimer = rng.nextFloat() * IDLE_MAX;
        this.densityCheckTimer = rng.nextFloat() * DENSITY_CHECK_INTERVAL;
        this.health = type.maxHealth;
        this.visionTiles = genes.visionTiles;
        this.speedMultiplier = genes.speedMultiplier;
        this.stomachMultiplier = genes.stomachMultiplier;
        this.aggressionMultiplier = genes.aggressionMultiplier;
        this.reproductiveRate = genes.reproductiveRate;
        this.resilienceMultiplier = genes.resilienceMultiplier;
        this.maxAge = genes.maxAge;
    }

    private int countNearbyConspecifics(WorldManager worldManager) {
        float radius = DENSITY_RADIUS_TILES * Main.TILE_SCALE;
        float radius2 = radius * radius;
        int count = 0;
        for (Animal other : worldManager.getAnimals()) {
            if (other == this || other.type != type || other.isDead()) continue;
            float dx = other.getWorldX() - worldX;
            float dy = other.getWorldY() - worldY;
            if (dx * dx + dy * dy <= radius2) count++;
        }
        return count;
    }

    private float computeDensityHappinessRate() {
        float t = cachedNearbyCount / DENSITY_SWEET_SPOT;
        float shape = (t <= 1f) ? t : (2f - t);
        float rate = shape * DENSITY_PEAK_BONUS_RATE;
        return Math.max(rate, DENSITY_MAX_PENALTY_RATE);
    }

    public void findNearbyWater(WorldManager worldManager) {
        this.water = WorldSearch.findNearestWaterTile(
            getWorldX(),
            getWorldY(),
            worldManager.getLoadedChunks(),
            visionTiles * Main.TILE_SCALE
        );
    }

    public void findNearbyFood(WorldManager worldManager) {
        if (type.foodType == null) return;
        this.food = WorldSearch.findNearestWorldObject(
            getWorldX(),
            getWorldY(),
            type.foodType,
            worldManager.getLoadedChunks(),
            visionTiles * Main.TILE_SCALE
        );
    }

    public void findNearbyPrey(WorldManager worldManager) {
        if (type.prey == null) return;

        float maxDistPx = visionTiles * Main.TILE_SCALE;
        float maxDist2 = maxDistPx * maxDistPx;

        Animal nearest = null;
        float bestDist2 = maxDist2;

        for (Animal other : worldManager.getAnimals()) {
            if (
                other == this || other.type != type.prey || other.isDead()
            ) continue;

            float dx = other.getWorldX() - worldX;
            float dy = other.getWorldY() - worldY;
            float d2 = dx * dx + dy * dy;

            if (d2 < bestDist2) {
                bestDist2 = d2;
                nearest = other;
            }
        }

        huntTarget = nearest;
    }

    public void findNearbyMate(WorldManager worldManager) {
        float maxDistPx = visionTiles * Main.TILE_SCALE;
        float maxDist2 = maxDistPx * maxDistPx;

        Animal nearest = null;
        float bestDist2 = maxDist2;

        for (Animal other : worldManager.getAnimals()) {
            if (
                other == this ||
                other.type != type ||
                other.isDead() ||
                !other.canReproduce()
            ) continue;

            float dx = other.getWorldX() - worldX;
            float dy = other.getWorldY() - worldY;
            float d2 = dx * dx + dy * dy;

            if (d2 < bestDist2) {
                bestDist2 = d2;
                nearest = other;
            }
        }

        mateTarget = nearest;
    }

    private float computeMetabolicMultiplier() {
        float visionCost =
            (visionTiles - type.baseVisionTiles) * VISION_ENERGY_COST_PER_TILE;
        float speedCost = (speedMultiplier - 1f) * SPEED_ENERGY_COST_FACTOR;
        float stomachCost =
            (stomachMultiplier - 1f) * STOMACH_ENERGY_COST_FACTOR;
        float aggressionCost =
            (aggressionMultiplier - 1f) * AGGRESSION_ENERGY_COST_FACTOR;
        float resilienceCost =
            (resilienceMultiplier - 1f) * RESILIENCE_ENERGY_COST_FACTOR;
        return clamp(
            1f +
                visionCost +
                speedCost +
                stomachCost +
                aggressionCost +
                resilienceCost,
            MIN_METABOLIC_MULTIPLIER,
            MAX_METABOLIC_MULTIPLIER
        );
    }

    public void update(
        float delta,
        CollisionChecker cc,
        WorldManager worldManager
    ) {
        timeSinceLastBirth += delta;
        age += delta;

        float metabolicMultiplier = computeMetabolicMultiplier();
        thirst += (delta / 45f) * metabolicMultiplier;
        hunger += (delta / 45f) * metabolicMultiplier;

        boolean isWellFed = hunger / type.maxHunger < CONTENT_HUNGER_FRACTION;
        boolean isHydrated = thirst / type.maxThirst < CONTENT_THIRST_FRACTION;

        densityCheckTimer -= delta;
        if (densityCheckTimer <= 0f) {
            densityCheckTimer = DENSITY_CHECK_INTERVAL + rng.nextFloat() * 1.0f;
            cachedNearbyCount = countNearbyConspecifics(worldManager);
        }

        float densityRate = computeDensityHappinessRate();

        if (isWellFed && isHydrated) {
            happiness = clamp(
                happiness + (HAPPINESS_GAIN_PER_SEC + densityRate) * delta,
                0f,
                MAX_HAPPINESS
            );
        } else {
            happiness = clamp(
                happiness + (-HAPPINESS_DECAY_PER_SEC + densityRate) * delta,
                0f,
                MAX_HAPPINESS
            );
        }

        boolean isThirsty = thirst > type.maxThirst * 0.5f;
        boolean isHungry = hunger > type.maxHunger * 0.5f;

        if (isThirsty) {
            if (water == null) findNearbyWater(worldManager);
            state = State.THIRST;
        } else {
            water = null;
        }

        if (
            type.diet == AnimalSpriteCache.Diet.CARNIVORE && type.prey != null
        ) {
            if (isHungry) {
                if (huntTarget == null || huntTarget.isDead()) findNearbyPrey(
                    worldManager
                );
                if (thirst / type.maxThirst < hunger / type.maxHunger) {
                    state = State.HUNTING;
                }
            } else {
                huntTarget = null;
            }
        } else {
            if (isHungry) {
                if (food == null) findNearbyFood(worldManager);
                if (thirst / type.maxThirst < hunger / type.maxHunger) {
                    state = State.HUNGERY;
                }
            } else {
                food = null;
            }
        }

        if (!isThirsty && !isHungry && canReproduce()) {
            if (
                mateTarget == null ||
                mateTarget.isDead() ||
                !mateTarget.canReproduce()
            ) {
                findNearbyMate(worldManager);
            }
            if (mateTarget != null) {
                state = State.MATING;
            }
        } else {
            mateTarget = null;
        }

        stateTimer -= delta;
        if (
            stateTimer <= 0f &&
            state != State.HUNGERY &&
            state != State.THIRST &&
            state != State.HUNTING &&
            state != State.MATING
        ) {
            if (state == State.IDLE) {
                state = State.WALKING;
                direction = rng.nextInt(4);
                stateTimer = WALK_MIN + rng.nextFloat() * (WALK_MAX - WALK_MIN);
            } else {
                state = State.IDLE;
                stateTimer = IDLE_MIN + rng.nextFloat() * (IDLE_MAX - IDLE_MIN);
            }
        }

        if (state == State.HUNGERY && food != null) {
            float colCX =
                worldX +
                (type.colOffsetX + type.colWidth * 0.5f) * Main.TILE_SCALE;
            float colCY =
                worldY +
                (type.colOffsetY + type.colHeight * 0.5f) * Main.TILE_SCALE;
            float fdx = (food.getX() + 0.5f) * Main.TILE_SCALE - colCX;
            float fdy = (food.getY() + 0.5f) * Main.TILE_SCALE - colCY;

            float eatReach = 2.0f * Main.TILE_SCALE;

            if (fdx * fdx + fdy * fdy <= eatReach * eatReach) {
                worldManager.removeWorldObject(food);

                float hungerRestored =
                    type.maxHunger *
                    FORAGE_HUNGER_RESTORE_FRACTION *
                    stomachMultiplier;
                float thirstRestored =
                    type.maxThirst *
                    FORAGE_THIRST_RESTORE_FRACTION *
                    stomachMultiplier;
                hunger = Math.max(0f, hunger - hungerRestored);
                thirst = Math.max(0f, thirst - thirstRestored);
                food = null;
                currentPath = null;
                pathIndex = 0;
                pathFoodTarget = null;
                pathTimer = 0f;
                pathForState = null;
                state = State.IDLE;
                stateTimer = IDLE_MIN + rng.nextFloat() * (IDLE_MAX - IDLE_MIN);
            } else {
                pathTimer -= delta;
                boolean needsRecomp =
                    food != pathFoodTarget ||
                    currentPath == null ||
                    pathForState != State.HUNGERY ||
                    pathTimer <= 0f;
                if (needsRecomp) {
                    float goalX = (food.getX() + 0.5f) * Main.TILE_SCALE;
                    float goalY = (food.getY() + 0.5f) * Main.TILE_SCALE;

                    float startCX =
                        worldX +
                        (type.colOffsetX + type.colWidth * 0.5f) *
                        Main.TILE_SCALE;
                    float startCY =
                        worldY +
                        (type.colOffsetY + type.colHeight * 0.5f) *
                        Main.TILE_SCALE;
                    currentPath = AnimalPathfinder.findPath(
                        startCX,
                        startCY,
                        goalX,
                        goalY,
                        type,
                        worldManager.getLoadedChunks(),
                        type.foodType
                    );
                    pathIndex = 0;
                    pathFoodTarget = food;
                    pathForState = State.HUNGERY;
                    pathTimer = PATH_RECOMP_INTERVAL;
                }
            }
        } else if (
            state == State.HUNTING && huntTarget != null && !huntTarget.isDead()
        ) {
            float colCX =
                worldX +
                (type.colOffsetX + type.colWidth * 0.5f) * Main.TILE_SCALE;
            float colCY =
                worldY +
                (type.colOffsetY + type.colHeight * 0.5f) * Main.TILE_SCALE;
            float tdx = huntTarget.getColliderCenterX() - colCX;
            float tdy = huntTarget.getColliderCenterY() - colCY;

            float biteReach = type.attackRange * Main.TILE_SCALE;

            attackCooldownTimer -= delta;

            if (tdx * tdx + tdy * tdy <= biteReach * biteReach) {
                currentPath = null;
                pathTimer = 0f;

                if (attackCooldownTimer <= 0f) {
                    huntTarget.takeDamage(
                        type.attackDamage * aggressionMultiplier
                    );
                    attackCooldownTimer =
                        type.attackCooldown / aggressionMultiplier;
                }

                if (huntTarget.isDead()) {
                    float hungerRestored =
                        type.maxHunger *
                        HUNT_HUNGER_RESTORE_FRACTION *
                        stomachMultiplier;
                    hunger = Math.max(0, hunger - hungerRestored);
                    huntTarget = null;
                    currentPath = null;
                    pathIndex = 0;
                    pathHuntTarget = null;
                    pathTimer = 0f;
                    pathForState = null;
                    attackCooldownTimer = 0f;
                    state = State.IDLE;
                    stateTimer =
                        IDLE_MIN + rng.nextFloat() * (IDLE_MAX - IDLE_MIN);
                }
            } else {
                pathTimer -= delta;
                boolean needsRecomp =
                    huntTarget != pathHuntTarget ||
                    currentPath == null ||
                    pathForState != State.HUNTING ||
                    pathTimer <= 0f;
                if (needsRecomp) {
                    float goalX = huntTarget.getColliderCenterX();
                    float goalY = huntTarget.getColliderCenterY();

                    currentPath = AnimalPathfinder.findPath(
                        colCX,
                        colCY,
                        goalX,
                        goalY,
                        type,
                        worldManager.getLoadedChunks(),
                        null
                    );
                    pathIndex = 0;
                    pathHuntTarget = huntTarget;
                    pathForState = State.HUNTING;
                    pathTimer = PATH_RECOMP_INTERVAL;
                }
            }
        } else if (state == State.THIRST && water != null) {
            float colCX =
                worldX +
                (type.colOffsetX + type.colWidth * 0.5f) * Main.TILE_SCALE;
            float colCY =
                worldY +
                (type.colOffsetY + type.colHeight * 0.5f) * Main.TILE_SCALE;
            float wdx = water[0] - colCX;
            float wdy = water[1] - colCY;

            float drinkReach = 2.0f * Main.TILE_SCALE;

            if (wdx * wdx + wdy * wdy <= drinkReach * drinkReach) {
                thirst = 0f;
                water = null;
                currentPath = null;
                pathIndex = 0;
                pathTimer = 0f;
                pathForState = null;
                pathWaterX = Float.NaN;
                pathWaterY = Float.NaN;
                state = State.IDLE;
                stateTimer = IDLE_MIN + rng.nextFloat() * (IDLE_MAX - IDLE_MIN);
            } else {
                pathTimer -= delta;
                boolean needsRecomp =
                    pathForState != State.THIRST ||
                    currentPath == null ||
                    water[0] != pathWaterX ||
                    water[1] != pathWaterY ||
                    pathTimer <= 0f;
                if (needsRecomp) {
                    float startCX =
                        worldX +
                        (type.colOffsetX + type.colWidth * 0.5f) *
                        Main.TILE_SCALE;
                    float startCY =
                        worldY +
                        (type.colOffsetY + type.colHeight * 0.5f) *
                        Main.TILE_SCALE;
                    currentPath = AnimalPathfinder.findPath(
                        startCX,
                        startCY,
                        water[0],
                        water[1],
                        type,
                        worldManager.getLoadedChunks(),
                        null
                    );
                    pathIndex = 0;
                    pathWaterX = water[0];
                    pathWaterY = water[1];
                    pathForState = State.THIRST;
                    pathTimer = PATH_RECOMP_INTERVAL;
                }
            }
        } else if (state == State.MATING) {
            if (
                mateTarget == null ||
                mateTarget.isDead() ||
                !mateTarget.canReproduce()
            ) {
                mateTarget = null;
                currentPath = null;
                pathIndex = 0;
                pathTimer = 0f;
                pathForState = null;
                state = State.IDLE;
                stateTimer = IDLE_MIN + rng.nextFloat() * (IDLE_MAX - IDLE_MIN);
            } else {
                pathTimer -= delta;
                boolean needsRecomp =
                    mateTarget != pathMateTarget ||
                    currentPath == null ||
                    pathForState != State.MATING ||
                    pathTimer <= 0f;
                if (needsRecomp) {
                    float colCX =
                        worldX +
                        (type.colOffsetX + type.colWidth * 0.5f) *
                        Main.TILE_SCALE;
                    float colCY =
                        worldY +
                        (type.colOffsetY + type.colHeight * 0.5f) *
                        Main.TILE_SCALE;
                    float goalX = mateTarget.getColliderCenterX();
                    float goalY = mateTarget.getColliderCenterY();

                    currentPath = AnimalPathfinder.findPath(
                        colCX,
                        colCY,
                        goalX,
                        goalY,
                        type,
                        worldManager.getLoadedChunks(),
                        null
                    );
                    pathIndex = 0;
                    pathMateTarget = mateTarget;
                    pathForState = State.MATING;
                    pathTimer = PATH_RECOMP_INTERVAL;
                }
            }
        } else {
            currentPath = null;
            pathIndex = 0;
            pathFoodTarget = null;
            pathHuntTarget = null;
            pathMateTarget = null;
            pathWaterX = Float.NaN;
            pathWaterY = Float.NaN;
            pathForState = null;
        }

        if (
            state == State.WALKING ||
            state == State.HUNGERY ||
            state == State.THIRST ||
            state == State.HUNTING ||
            state == State.MATING
        ) {
            float dx = 0f,
                dy = 0f;

            if (state == State.WALKING) {
                switch (direction) {
                    case DIR_RIGHT:
                        dx = 1f;
                        break;
                    case DIR_LEFT:
                        dx = -1f;
                        break;
                    case DIR_UP:
                        dy = 1f;
                        break;
                    case DIR_DOWN:
                        dy = -1f;
                        break;
                }
            } else if (currentPath != null && pathIndex < currentPath.size()) {
                float colCX =
                    worldX +
                    (type.colOffsetX + type.colWidth * 0.5f) * Main.TILE_SCALE;
                float colCY =
                    worldY +
                    (type.colOffsetY + type.colHeight * 0.5f) * Main.TILE_SCALE;

                float[] wp = currentPath.get(pathIndex);
                float wpDx = wp[0] - colCX;
                float wpDy = wp[1] - colCY;

                float reach = 0.45f * Main.TILE_SCALE;
                if (wpDx * wpDx + wpDy * wpDy < reach * reach) {
                    pathIndex++;
                    if (pathIndex < currentPath.size()) {
                        wp = currentPath.get(pathIndex);
                        wpDx = wp[0] - colCX;
                        wpDy = wp[1] - colCY;
                    } else {
                        wpDx = 0f;
                        wpDy = 0f;
                    }
                }

                float len = (float) Math.sqrt(wpDx * wpDx + wpDy * wpDy);
                if (len > 0.001f) {
                    dx = wpDx / len;
                    dy = wpDy / len;
                }
            } else {
                float targetX = worldX,
                    targetY = worldY;
                if (state == State.HUNGERY && food != null) {
                    targetX = (food.getX() + 0.5f) * Main.TILE_SCALE;
                    targetY = (food.getY() + 0.5f) * Main.TILE_SCALE;
                } else if (state == State.THIRST && water != null) {
                    targetX = water[0];
                    targetY = water[1];
                } else if (state == State.HUNTING && huntTarget != null) {
                    targetX = huntTarget.getColliderCenterX();
                    targetY = huntTarget.getColliderCenterY();
                } else if (state == State.MATING && mateTarget != null) {
                    targetX = mateTarget.getColliderCenterX();
                    targetY = mateTarget.getColliderCenterY();
                }

                float colCX =
                    worldX +
                    (type.colOffsetX + type.colWidth * 0.5f) * Main.TILE_SCALE;
                float colCY =
                    worldY +
                    (type.colOffsetY + type.colHeight * 0.5f) * Main.TILE_SCALE;
                float rdx = targetX - colCX;
                float rdy = targetY - colCY;
                float len = (float) Math.sqrt(rdx * rdx + rdy * rdy);
                if (len > 0.001f) {
                    dx = rdx / len;
                    dy = rdy / len;
                }
            }

            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0.01f) direction = DIR_RIGHT;
                else if (dx < -0.01f) direction = DIR_LEFT;
            } else if (Math.abs(dy) > Math.abs(dx)) {
                if (dy > 0.01f) direction = DIR_UP;
                else if (dy < -0.01f) direction = DIR_DOWN;
            }

            float effSpeed = getEffectiveSpeed();
            float moveX = dx * effSpeed * delta;
            float moveY = dy * effSpeed * delta;

            boolean moved = false;
            if (moveX != 0f && !collidesAt(worldX + moveX, worldY, cc)) {
                worldX += moveX;
                moved = true;
            }
            if (moveY != 0f && !collidesAt(worldX, worldY + moveY, cc)) {
                worldY += moveY;
                moved = true;
            }

            if (!moved) {
                boolean slid = false;
                if (
                    (state == State.HUNGERY ||
                        state == State.THIRST ||
                        state == State.HUNTING ||
                        state == State.MATING) &&
                    (Math.abs(dx) > 0.001f || Math.abs(dy) > 0.001f)
                ) {
                    float slideSpeed = effSpeed * delta * 0.6f;

                    float cwX = dy * slideSpeed;
                    float cwY = -dx * slideSpeed;
                    float ccwX = -dy * slideSpeed;
                    float ccwY = dx * slideSpeed;

                    if (cwX != 0f && !collidesAt(worldX + cwX, worldY, cc)) {
                        worldX += cwX;
                        slid = true;
                    } else if (
                        cwY != 0f && !collidesAt(worldX, worldY + cwY, cc)
                    ) {
                        worldY += cwY;
                        slid = true;
                    } else if (
                        ccwX != 0f && !collidesAt(worldX + ccwX, worldY, cc)
                    ) {
                        worldX += ccwX;
                        slid = true;
                    } else if (
                        ccwY != 0f && !collidesAt(worldX, worldY + ccwY, cc)
                    ) {
                        worldY += ccwY;
                        slid = true;
                    }
                }

                if (slid) {
                    animTimer += delta;
                    while (animTimer >= FRAME_DURATION) {
                        animTimer -= FRAME_DURATION;
                        animFrame = (animFrame + 1) % AnimalSpriteCache.COLS;
                    }
                } else {
                    if (
                        state == State.HUNGERY ||
                        state == State.THIRST ||
                        state == State.HUNTING ||
                        state == State.MATING
                    ) {
                        currentPath = null;
                        pathTimer = 0f;
                    } else {
                        state = State.IDLE;
                        stateTimer = 0.4f + rng.nextFloat() * 0.6f;
                    }
                    animFrame = 0;
                    animTimer = 0f;
                }
            } else {
                animTimer += delta;
                while (animTimer >= FRAME_DURATION) {
                    animTimer -= FRAME_DURATION;
                    animFrame = (animFrame + 1) % AnimalSpriteCache.COLS;
                }
            }
        } else {
            animFrame = 0;
            animTimer = 0f;
        }
    }

    private boolean collidesAt(float px, float py, CollisionChecker cc) {
        float ts = Main.TILE_SCALE;
        float x0 = px + type.colOffsetX * ts;
        float y0 = py + type.colOffsetY * ts;
        float x1 = x0 + type.colWidth * ts;
        float y1 = y0 + type.colHeight * ts;
        return cc.isBlocked(x0, y0, x1, y1);
    }

    public void draw(Batch batch, Camera cam) {
        TextureRegion[][] frames = AnimalSpriteCache.getFrames(type.spriteKind);
        if (frames == null) return;

        TextureRegion frame = frames[direction][animFrame];
        ShaderProgram shader = (ShaderProgram) batch.getShader();

        batch.flush();
        shader.setUniformi("u_isWater", 0);
        shader.setUniformi("u_hasNormalMap", 0);
        ObjectSpriteCache.flatNormal.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        float drawW = Main.TILE_SCALE * type.spriteKind.drawScaleW;
        float drawH = Main.TILE_SCALE * type.spriteKind.drawScaleH;

        batch.draw(
            frame,
            worldX + cam.cameraX,
            getRenderOffsetY() + cam.cameraY,
            drawW,
            drawH
        );

        drawStatusBars(batch, cam, drawW, drawH);
    }

    public boolean shouldBeDead() {
        return (
            health <= 0f ||
            (hunger / type.maxHunger > .98f) ||
            (thirst / type.maxThirst > .98f) ||
            age > maxAge
        );
    }

    public boolean isDead() {
        return health <= 0f;
    }

    public void takeDamage(float amount) {
        health = Math.max(0f, health - amount / resilienceMultiplier);
    }

    public float getColliderCenterX() {
        return (
            worldX + (type.colOffsetX + type.colWidth * 0.5f) * Main.TILE_SCALE
        );
    }

    public float getColliderCenterY() {
        return (
            worldY + (type.colOffsetY + type.colHeight * 0.5f) * Main.TILE_SCALE
        );
    }

    private void drawStatusBars(
        Batch batch,
        Camera cam,
        float drawW,
        float drawH
    ) {
        TextureRegion pixel = AnimalSpriteCache.getStatusBarPixel();
        if (pixel == null) return;

        float barX = worldX + cam.cameraX + (drawW - STATUS_BAR_WIDTH) * 0.5f;
        float baseY =
            getRenderOffsetY() + cam.cameraY + drawH + STATUS_BAR_Y_OFFSET;

        float hungerPct = clamp01(hunger / type.maxHunger);
        float thirstPct = clamp01(thirst / type.maxThirst);

        boolean showHealth = health < type.maxHealth;
        float healthPct = clamp01(health / type.maxHealth);

        float hungerY = baseY + STATUS_BAR_HEIGHT + STATUS_BAR_GAP;
        if (showHealth) {
            float healthY = hungerY + STATUS_BAR_HEIGHT + STATUS_BAR_GAP;
            drawBar(
                batch,
                pixel,
                barX,
                healthY,
                healthPct,
                HEALTH_R,
                HEALTH_G,
                HEALTH_B
            );
        }

        drawBar(
            batch,
            pixel,
            barX,
            hungerY,
            hungerPct,
            HUNGER_R,
            HUNGER_G,
            HUNGER_B
        );
        drawBar(
            batch,
            pixel,
            barX,
            baseY,
            thirstPct,
            THIRST_R,
            THIRST_G,
            THIRST_B
        );

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawBar(
        Batch batch,
        TextureRegion pixel,
        float x,
        float y,
        float pct,
        float r,
        float g,
        float b
    ) {
        batch.setColor(BAR_BG_R, BAR_BG_G, BAR_BG_B, BAR_BG_A);
        batch.draw(pixel, x, y, STATUS_BAR_WIDTH, STATUS_BAR_HEIGHT);

        if (pct > 0f) {
            batch.setColor(r, g, b, 1f);
            batch.draw(pixel, x, y, STATUS_BAR_WIDTH * pct, STATUS_BAR_HEIGHT);
        }
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public float getHunger() {
        return hunger;
    }

    public float getThirst() {
        return thirst;
    }

    public float getHealth() {
        return health;
    }

    public float getHappiness() {
        return happiness;
    }

    public float getMaxHappiness() {
        return MAX_HAPPINESS;
    }

    public float getVisionTiles() {
        return visionTiles;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public float getStomachMultiplier() {
        return stomachMultiplier;
    }

    public float getAggressionMultiplier() {
        return aggressionMultiplier;
    }

    public float getReproductiveRate() {
        return reproductiveRate;
    }

    public float getResilienceMultiplier() {
        return resilienceMultiplier;
    }

    public float getMaxAge() {
        return maxAge;
    }

    public float getAge() {
        return age;
    }

    public float getEffectiveSpeed() {
        return type.speed * speedMultiplier;
    }

    public boolean canReproduce() {
        return (
            happiness >= MAX_HAPPINESS &&
            timeSinceLastBirth >= BREEDING_COOLDOWN_SECONDS / reproductiveRate
        );
    }

    public void onBred() {
        happiness = 0f;
        timeSinceLastBirth = 0f;
    }

    public float getRenderY() {
        if (
            this.type != Animal.Type.COW && this.type != Animal.Type.WOLF
        ) return worldY;
        switch (direction) {
            case DIR_UP:
                return worldY - COW_RENDER_Y_OFFSET_UP;
            case DIR_DOWN:
                return worldY - COW_RENDER_Y_OFFSET_DOWN;
            default:
                return worldY - COW_RENDER_Y_OFFSET_DEFAULT;
        }
    }

    public float getRenderOffsetY() {
        if (
            this.type != Animal.Type.COW && this.type != Animal.Type.WOLF
        ) return worldY;
        switch (direction) {
            case DIR_UP:
                return worldY - 8;
            case DIR_DOWN:
                return worldY - 8;
            default:
                return worldY - 20f;
        }
    }
}
