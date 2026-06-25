package com.factory.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.factory.game.Camera;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;
import com.factory.game.Main;
import com.factory.game.Renderer.FireParticleEmitter;
import com.factory.game.Renderer.LightRenderer;
import com.factory.game.Renderer.LightSource;
import java.util.Map;

public class PlacedObject {

    public static boolean PIPES_VISIBLE = false;
    public static boolean ITEM_PIPES_VISIBLE = false;

    public enum Type {
        WOOD_WALL,
        CAMPFIRE,
        LANTERN,
        CRUSHING_POT,
        GRASS,
        WELL,
        BARREL,
        BASIC_PIPE,
        FURNACE,
        SEASHELL,
        STORAGE_CRATE,
        FOREST_CRATE,
        DESERT_CRATE,
        MOUNTAIN_CRATE,
        PENGUIN_PLUSH,
        POO_PET,
        CAST,
        SOLDERING_TABLE,
        FISHBOWL,
        MIXER,
        DEVBARREL,
        ITEM_PIPE,
        PLANTER,
        CRUSHER,
        STONE_WALL,
        FILTER_PIPE,
        DRILL,
        TANK,
        SMELTER,
        WOOD_WALL_FULL,
        WOOD_FLOOR,
        STONE_WALL_FULL,
        CHUNK_LOADER,
        TRASH,
        DISTILLERY,
        STONE_FLOOR,
        GLOBE,
        BOOK_SHELF,
        CHAIR,
        SIGN,
        TABLE,
        BARREL_DECO,
        WOOD_PLANKS,
        TOILET,
        LAMP_POST,
        ORE_DRILL,
        FILTER_ITEM_PIPE,
    }

    public final Type type;
    private final int x, y;

    private int tileIndex = 0;

    private int hitsReceived = 0;
    private float shakeTimer = 0f;

    private static final float SHAKE_DURATION = 0.18f;
    private static final float SHAKE_MAGNITUDE = 4f;

    private final int spriteVariant;

    private int animFrame = 0;
    private float animTimer = 0f;

    private LightSource lightSource;
    private Body shadowBody;
    private FireParticleEmitter particleEmitter;

    private final LiquidTank liquidTank;

    private static final float CAST_DURATION = 30f;
    private boolean isCasting = false;
    private float castTimer = 0f;
    private LiquidType castLiquid = null;
    private ItemStack pendingIngot = null;

    private java.util.Set<LiquidType> allowedLiquidTypes = null;
    private java.util.Set<Item> allowedItemTypes = null;

    private MixingRecipe selectedMixingRecipe = null;
    private LiquidTank[] mixerInputTanks = null;
    private LiquidTank mixerOutputTank = null;

    private DistilleryRecipe selectedDistilleryRecipe = null;
    private LiquidTank[] distilleryInputTanks = null;
    private LiquidTank[] distilleryOutputTanks = null;

    private LiquidType selectedProducedLiquid = null;

    private LiquidTank[] planterInputTanks = null;

    private static PlanterManager planterManager;

    public static void setPlanterManager(PlanterManager pm) {
        planterManager = pm;
    }

    public boolean isMixer() {
        return type == Type.MIXER;
    }

    public MixingRecipe getSelectedMixingRecipe() {
        return selectedMixingRecipe;
    }

    public LiquidTank[] getMixerInputTanks() {
        return mixerInputTanks;
    }

    public LiquidTank getMixerOutputTank() {
        return mixerOutputTank;
    }

    public boolean isDistillery() {
        return type == Type.DISTILLERY;
    }

    public DistilleryRecipe getSelectedDistilleryRecipe() {
        return selectedDistilleryRecipe;
    }

    public LiquidTank[] getDistilleryInputTanks() {
        return distilleryInputTanks;
    }

    public LiquidTank[] getDistilleryOutputTanks() {
        return distilleryOutputTanks;
    }

    private java.util.Set<String> pinnedChunkKeys = new java.util.HashSet<>();

    public PlacedObject(Type type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;

        this.spriteVariant = Math.abs((x * 1664525) ^ (y * 1013904223));

        if (type == Type.CAMPFIRE) {
            long seed = ((long) x * 0xDEAD_BEEFL) ^ ((long) y * 0x1337_CAFEL);
            float cx = x * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float cy = y * Main.TILE_SCALE;
            particleEmitter = new FireParticleEmitter(cx, cy, seed);
        }

        LiquidMachineDefinition def = LiquidMachineRegistry.get(type);
        if (def != null && type == Type.CAST) {
            java.util.EnumSet<LiquidType> molten = java.util.EnumSet.of(
                LiquidType.MOLTEN_OREMINUM,
                LiquidType.MOLTEN_ROCKITE,
                LiquidType.MOLTEN_EARTHEL,
                LiquidType.MOLTEN_STONEDON,
                LiquidType.MOLTEN_SNOWITE
            );
            this.liquidTank = new LiquidTank(def.role, def.capacity, molten);
        } else {
            if (def != null && type != Type.PLANTER) {
                if (def.acceptedLiquid != null) {
                    this.liquidTank = new LiquidTank(
                        def.role,
                        def.capacity,
                        java.util.EnumSet.of(def.acceptedLiquid)
                    );
                } else {
                    this.liquidTank = new LiquidTank(def.role, def.capacity);
                }
            } else {
                this.liquidTank = null;
            }
        }

        if (type == Type.PLANTER) {
            planterInputTanks = new LiquidTank[] {
                new LiquidTank(
                    LiquidTank.Role.RECEIVER,
                    50f,
                    java.util.EnumSet.of(LiquidType.WATER)
                ),
                new LiquidTank(
                    LiquidTank.Role.RECEIVER,
                    50f,
                    java.util.EnumSet.of(LiquidType.FERTILIZER)
                ),
            };
        }
    }

    public LiquidTank[] getPlanterInputTanks() {
        return planterInputTanks;
    }

    public LiquidTank getLiquidTank() {
        return liquidTank;
    }

    public boolean isLiquidMachine() {
        return liquidTank != null;
    }

    public boolean isChunkLoaderActive() {
        return (
            type == Type.CHUNK_LOADER &&
            liquidTank != null &&
            liquidTank.getAmount() > 0f
        );
    }

    public java.util.Set<String> getPinnedChunkKeys() {
        return pinnedChunkKeys;
    }

    public void setPinnedChunkKeys(java.util.Set<String> keys) {
        pinnedChunkKeys = (keys != null && !keys.isEmpty())
            ? new java.util.HashSet<>(keys)
            : new java.util.HashSet<>();
    }

    public void togglePinnedChunk(String chunkKey) {
        if (!pinnedChunkKeys.remove(chunkKey)) {
            pinnedChunkKeys.add(chunkKey);
        }
    }

    public boolean isFloor() {
        return (
            type == Type.WOOD_FLOOR ||
            type == Type.STONE_FLOOR ||
            type == Type.WOOD_PLANKS
        );
    }

    public boolean isPipe() {
        return (
            type == Type.BASIC_PIPE ||
            type == Type.FILTER_PIPE ||
            type == Type.ITEM_PIPE ||
            type == Type.FILTER_ITEM_PIPE
        );
    }

    public boolean isFilterPipe() {
        return type == Type.FILTER_PIPE;
    }

    public boolean isFilterItemPipe() {
        return type == Type.FILTER_ITEM_PIPE;
    }

    public java.util.Set<LiquidType> getAllowedLiquidTypes() {
        return allowedLiquidTypes;
    }

    public void setAllowedLiquidTypes(java.util.Set<LiquidType> types) {
        this.allowedLiquidTypes = (types == null || types.isEmpty())
            ? null
            : types;
    }

    public java.util.Set<Item> getAllowedItemTypes() {
        return allowedItemTypes;
    }

    public void setAllowedItemTypes(java.util.Set<Item> types) {
        this.allowedItemTypes = (types == null || types.isEmpty())
            ? null
            : types;
    }

    public LiquidType getSelectedProducedLiquid() {
        return selectedProducedLiquid;
    }

    public void setSelectedProducedLiquid(LiquidType liquid) {
        if (liquid != selectedProducedLiquid) {
            selectedProducedLiquid = liquid;
            if (liquidTank != null) liquidTank.clear();
        }
    }

    public void updateCast(float delta) {
        if (type != Type.CAST || liquidTank == null) return;

        if (
            !isCasting &&
            liquidTank.getAmount() >= liquidTank.getCapacity() - 0.01f
        ) {
            castLiquid = liquidTank.getType();
            isCasting = true;
            castTimer = CAST_DURATION;
        }

        if (isCasting) {
            castTimer -= delta;
            if (castTimer <= 0f) {
                liquidTank.withdraw(liquidTank.getAmount());
                if (castLiquid != null) {
                    Item ingot = castLiquid.toIngot();
                    if (ingot != null) {
                        pendingIngot = new ItemStack(ingot, 1);
                    }
                }
                isCasting = false;
                castTimer = 0f;
                castLiquid = null;
            }
        }
    }

    private int getCastFrameIndex() {
        if (!isCasting) return 0;
        float elapsed = CAST_DURATION - castTimer;
        if (elapsed < 10f) return 1;
        if (elapsed < 20f) return 2;
        return 3;
    }

    public ItemStack pollPendingIngot() {
        ItemStack result = pendingIngot;
        pendingIngot = null;
        return result;
    }

    public void attachLight(LightRenderer lightRenderer) {
        if (lightSource != null) return;

        if (type == Type.PENGUIN_PLUSH) {
            float worldPixelX = x * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float worldPixelY = y * Main.TILE_SCALE + Main.TILE_SCALE * 0.25f;
            lightSource = lightRenderer.addPointLight(
                worldPixelX,
                worldPixelY,
                new com.badlogic.gdx.graphics.Color(1f, 0.05f, 0.05f, 0.01f),
                4f * Main.TILE_SCALE,
                64
            );
        }

        if (type == Type.LAMP_POST && isLampPostPowered()) {
            float worldPixelX = x * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float worldPixelY = y * Main.TILE_SCALE + Main.TILE_SCALE * 0.75f;
            lightSource = lightRenderer.addPointLight(
                worldPixelX,
                worldPixelY,
                new com.badlogic.gdx.graphics.Color(1f, 0.95f, 0.8f, 0.9f),
                15f * Main.TILE_SCALE,
                64
            );
        }

        if (type != Type.CAMPFIRE && type != Type.LANTERN) return;

        float worldPixelX = x * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = y * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        lightSource = lightRenderer.addPointLight(
            worldPixelX,
            worldPixelY,
            new com.badlogic.gdx.graphics.Color(1f, 0.45f, 0.12f, 0.9f),
            8f * Main.TILE_SCALE,
            64
        );
    }

    public void detachLight(LightRenderer lightRenderer) {
        if (lightSource == null) return;
        lightRenderer.removeLight(lightSource);
        lightSource = null;
    }

    private boolean isLampPostPowered() {
        return liquidTank != null && liquidTank.getAmount() > 0f;
    }

    public void updateLampPostLight(LightRenderer lightRenderer) {
        if (type != Type.LAMP_POST) return;
        boolean powered = isLampPostPowered();
        if (powered && lightSource == null) {
            float worldPixelX = x * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float worldPixelY = y * Main.TILE_SCALE + Main.TILE_SCALE * 0.75f;
            lightSource = lightRenderer.addPointLight(
                worldPixelX,
                worldPixelY,
                new com.badlogic.gdx.graphics.Color(1f, 0.95f, 0.8f, 0.9f),
                15f * Main.TILE_SCALE,
                64
            );
        } else if (!powered && lightSource != null) {
            lightRenderer.removeLight(lightSource);
            lightSource = null;
        }
    }

    public boolean isSolid() {
        PlacedObjectCache.SpriteConfig cfg = PlacedObjectCache.getConfig(type);
        return cfg != null && cfg.solid;
    }

    public boolean hasShadowBody() {
        return shadowBody != null;
    }

    public void attachShadowBody(com.badlogic.gdx.physics.box2d.World world) {
        if (shadowBody != null) return;

        float half = Main.TILE_SCALE * 0.5f;
        float cx = x * Main.TILE_SCALE + half;
        float cy = y * Main.TILE_SCALE + half;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(cx, cy);
        shadowBody = world.createBody(def);

        PolygonShape box = new PolygonShape();
        box.setAsBox(half, half);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = box;
        fixture.isSensor = false;
        shadowBody.createFixture(fixture);
        box.dispose();
    }

    public void destroyShadowBody(com.badlogic.gdx.physics.box2d.World world) {
        if (shadowBody == null) return;
        world.destroyBody(shadowBody);
        shadowBody = null;
    }

    public void reupdate(Map<String, PlacedObject> lookup) {
        PlacedObjectCache.SpriteConfig cfg = PlacedObjectCache.getConfig(type);
        if (cfg == null || !cfg.mode.isAutoTile()) {
            tileIndex = 0;
            return;
        }

        boolean top = isSameType(lookup, x, y + 1);
        boolean bottom = isSameType(lookup, x, y - 1);
        boolean left = isSameType(lookup, x - 1, y);
        boolean right = isSameType(lookup, x + 1, y);
        boolean tl = isSameType(lookup, x - 1, y + 1);
        boolean tr = isSameType(lookup, x + 1, y + 1);
        boolean bl = isSameType(lookup, x - 1, y - 1);
        boolean br = isSameType(lookup, x + 1, y - 1);

        tileIndex = cfg.mode.pickTileIndex(
            top,
            bottom,
            left,
            right,
            tl,
            tr,
            bl,
            br
        );
    }

    private boolean isSameType(
        Map<String, PlacedObject> lookup,
        int tx,
        int ty
    ) {
        PlacedObject neighbour = lookup.get(tx + "," + ty);
        return (
            (neighbour != null && (neighbour.type == this.type)) ||
            (neighbour != null &&
                neighbour.type == Type.FILTER_PIPE &&
                this.type == Type.BASIC_PIPE) ||
            (neighbour != null &&
                neighbour.type == Type.BASIC_PIPE &&
                this.type == Type.FILTER_PIPE) ||
            (neighbour != null &&
                neighbour.type == Type.FILTER_ITEM_PIPE &&
                this.type == Type.ITEM_PIPE) ||
            (neighbour != null &&
                neighbour.type == Type.ITEM_PIPE &&
                this.type == Type.FILTER_ITEM_PIPE) ||
            (neighbour != null && neighbour.isFloor() && this.isFloor()) ||
            (neighbour != null &&
                neighbour.type == Type.STONE_WALL &&
                this.type == Type.WOOD_WALL) ||
            (neighbour != null &&
                neighbour.type == Type.WOOD_WALL &&
                this.type == Type.STONE_WALL)
        );
    }

    public void draw(Batch batch, Camera cam) {
        float delta = Gdx.graphics.getDeltaTime();

        shakeTimer = Math.max(0f, shakeTimer - delta);
        float shakeX = 0f;
        if (shakeTimer > 0f) {
            float decay = shakeTimer / SHAKE_DURATION;
            shakeX = (float) (Math.random() * 2 - 1) * SHAKE_MAGNITUDE * decay;
        }

        PlacedObjectCache.SpriteConfig cfg = PlacedObjectCache.getConfig(type);
        if (cfg == null) return;

        ShaderProgram shader = (ShaderProgram) batch.getShader();
        batch.flush();
        shader.setUniformi("u_isWater", 0);
        shader.setUniformi("u_hasNormalMap", 0);
        ObjectSpriteCache.flatNormal.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        float drawX = x * Main.TILE_SCALE + cam.cameraX + shakeX;
        float drawY = y * Main.TILE_SCALE + cam.cameraY;

        int frameIndex;
        switch (cfg.mode) {
            case ANIMATED:
                animTimer += delta;
                float frameDuration = 1f / cfg.animFps;
                while (animTimer >= frameDuration) {
                    animTimer -= frameDuration;
                    animFrame =
                        (animFrame + 1) %
                        PlacedObjectCache.getTotalFrames(type);
                }
                frameIndex = animFrame;
                break;
            case RANDOM:
                frameIndex =
                    spriteVariant % PlacedObjectCache.getTotalFrames(type);
                break;
            case WALL:
            case BLOB_47:
            case WANG_16:
            case TILESETTER:
                frameIndex = tileIndex;
                break;
            default:
                frameIndex = (type == Type.CAST)
                    ? getCastFrameIndex()
                    : cfg.fixedIndex;
                break;
        }

        if (particleEmitter != null) {
            particleEmitter.update(delta);
            particleEmitter.draw(batch, ObjectSpriteCache.whitePixel, cam);
        }

        if (type == Type.PLANTER && planterManager != null) {
            com.factory.game.Items.Item seed = planterManager.getPlantedSeed(
                this
            );
            TextureRegion toDraw = null;

            if (seed != null) {
                com.factory.game.Items.PlanterRecipe recipe =
                    com.factory.game.Items.CraftingManager.getPlanterRecipeFor(
                        seed
                    );
                if (recipe != null && recipe.getGrowSheetPath() != null) {
                    int growFrame = planterManager.getGrowFrame(this);
                    toDraw = PlanterSpriteCache.getFrame(
                        recipe.getGrowSheetPath(),
                        growFrame
                    );
                }
            }

            if (toDraw == null) toDraw = PlacedObjectCache.getRegion(type, 0);
            if (toDraw == null) return;

            float drawW = (cfg.drawWidth / 16f) * Main.TILE_SCALE;
            float drawH = (cfg.drawHeight / 16f) * Main.TILE_SCALE;
            batch.draw(
                toDraw,
                drawX + (cfg.offsetX / 16f) * Main.TILE_SCALE,
                drawY + (cfg.offsetY / 16f) * Main.TILE_SCALE,
                drawW,
                drawH
            );
            return;
        }

        TextureRegion region = PlacedObjectCache.getRegion(type, frameIndex);
        if (region == null) return;

        float drawW = (cfg.drawWidth / 16f) * Main.TILE_SCALE;
        float drawH = (cfg.drawHeight / 16f) * Main.TILE_SCALE;
        batch.draw(
            region,
            drawX + (cfg.offsetX / 16f) * Main.TILE_SCALE,
            drawY + (cfg.offsetY / 16f) * Main.TILE_SCALE,
            drawW,
            drawH
        );
    }

    public HarvestResult tryHit(com.factory.game.Items.ItemClass toolClass) {
        HarvestDefinition def = PlacedHarvestRegistry.get(type);
        if (def == null) return HarvestResult.NOT_HARVESTABLE;
        if (!def.canHarvest(toolClass)) return HarvestResult.WRONG_TOOL;

        hitsReceived++;
        shakeTimer = SHAKE_DURATION;

        return (hitsReceived >= def.getHitsRequired())
            ? HarvestResult.DESTROYED
            : HarvestResult.HIT_REGISTERED;
    }

    public void resetHits() {
        hitsReceived = 0;
        shakeTimer = 0f;
    }

    public java.util.List<com.factory.game.Items.ItemStack> generateDrops(
        java.util.Random rng
    ) {
        HarvestDefinition def = PlacedHarvestRegistry.get(type);
        if (
            def == null || def.getDropTable().isEmpty()
        ) return java.util.Collections.emptyList();
        return def.getDropTable().roll(rng);
    }

    public float getHitProgress() {
        HarvestDefinition def = PlacedHarvestRegistry.get(type);
        if (def == null || def.getHitsRequired() <= 0) return 0f;
        return (float) hitsReceived / def.getHitsRequired();
    }

    public float getRenderY() {
        return y * Main.TILE_SCALE;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setSelectedMixingRecipe(MixingRecipe recipe) {
        if (recipe == selectedMixingRecipe) return;
        selectedMixingRecipe = recipe;

        if (recipe == null) {
            mixerInputTanks = null;
            mixerOutputTank = null;
            return;
        }

        java.util.List<MixingRecipe.InputSpec> specs = recipe.getInputs();
        mixerInputTanks = new LiquidTank[specs.size()];
        for (int i = 0; i < specs.size(); i++) {
            MixingRecipe.InputSpec s = specs.get(i);
            java.util.Set<LiquidType> allowed = java.util.EnumSet.of(s.type);
            mixerInputTanks[i] = new LiquidTank(
                LiquidTank.Role.RECEIVER,
                s.tankCapacity,
                allowed
            );
        }
        mixerOutputTank = new LiquidTank(
            LiquidTank.Role.PRODUCER,
            recipe.getOutputCapacity()
        );
    }

    public void updateMixerProcessing(float delta) {
        if (type != Type.MIXER || selectedMixingRecipe == null) return;
        if (mixerInputTanks == null || mixerOutputTank == null) return;

        java.util.List<MixingRecipe.InputSpec> specs =
            selectedMixingRecipe.getInputs();

        float fraction = 1f;
        for (int i = 0; i < specs.size(); i++) {
            float want = specs.get(i).ratePerSecond * delta;
            if (want <= 0f) continue;
            float have = mixerInputTanks[i].getAmount();
            if (have < want) fraction = Math.min(fraction, have / want);
        }

        float outWant = selectedMixingRecipe.getOutputRate() * delta * fraction;
        float outSpace = mixerOutputTank.getSpace();
        if (outSpace <= 0f || outWant <= 0f || fraction <= 1e-6f) return;
        if (outWant > outSpace) fraction *= outSpace / outWant;
        if (fraction <= 1e-6f) return;

        for (int i = 0; i < specs.size(); i++) {
            mixerInputTanks[i].withdraw(
                specs.get(i).ratePerSecond * delta * fraction
            );
        }
        mixerOutputTank.deposit(
            selectedMixingRecipe.getOutputType(),
            selectedMixingRecipe.getOutputRate() * delta * fraction
        );
    }

    public void setSelectedDistilleryRecipe(DistilleryRecipe recipe) {
        if (recipe == selectedDistilleryRecipe) return;
        selectedDistilleryRecipe = recipe;

        if (recipe == null) {
            distilleryInputTanks = null;
            distilleryOutputTanks = null;
            return;
        }

        java.util.List<DistilleryRecipe.InputSpec> inputSpecs =
            recipe.getInputs();
        java.util.List<DistilleryRecipe.OutputSpec> outputSpecs =
            recipe.getOutputs();

        distilleryInputTanks = new LiquidTank[inputSpecs.size()];
        for (int i = 0; i < inputSpecs.size(); i++) {
            DistilleryRecipe.InputSpec s = inputSpecs.get(i);
            java.util.Set<LiquidType> allowed = java.util.EnumSet.of(s.type);
            distilleryInputTanks[i] = new LiquidTank(
                LiquidTank.Role.RECEIVER,
                s.tankCapacity,
                allowed
            );
        }

        distilleryOutputTanks = new LiquidTank[outputSpecs.size()];
        for (int i = 0; i < outputSpecs.size(); i++) {
            DistilleryRecipe.OutputSpec s = outputSpecs.get(i);
            distilleryOutputTanks[i] = new LiquidTank(
                LiquidTank.Role.PRODUCER,
                s.tankCapacity
            );
        }
    }

    public void updateDistilleryProcessing(float delta) {
        if (type != Type.DISTILLERY || selectedDistilleryRecipe == null) return;
        if (
            distilleryInputTanks == null || distilleryOutputTanks == null
        ) return;

        java.util.List<DistilleryRecipe.InputSpec> inputSpecs =
            selectedDistilleryRecipe.getInputs();
        java.util.List<DistilleryRecipe.OutputSpec> outputSpecs =
            selectedDistilleryRecipe.getOutputs();

        float fraction = 1f;
        for (int i = 0; i < inputSpecs.size(); i++) {
            float want = inputSpecs.get(i).ratePerSecond * delta;
            if (want <= 0f) continue;
            float have = distilleryInputTanks[i].getAmount();
            if (have < want) fraction = Math.min(fraction, have / want);
        }
        if (fraction <= 1e-6f) return;

        for (int i = 0; i < outputSpecs.size(); i++) {
            float produce = outputSpecs.get(i).ratePerSecond * delta * fraction;
            float space = distilleryOutputTanks[i].getSpace();
            if (space <= 0f) return;
            if (produce > space) fraction *= (space / produce);
        }
        if (fraction <= 1e-6f) return;

        for (int i = 0; i < inputSpecs.size(); i++) {
            distilleryInputTanks[i].withdraw(
                inputSpecs.get(i).ratePerSecond * delta * fraction
            );
        }

        for (int i = 0; i < outputSpecs.size(); i++) {
            DistilleryRecipe.OutputSpec spec = outputSpecs.get(i);
            distilleryOutputTanks[i].deposit(
                spec.type,
                spec.ratePerSecond * delta * fraction
            );
        }
    }
}
