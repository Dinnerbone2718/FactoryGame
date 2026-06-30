package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.World;
import com.factory.game.Items.DroppedItem;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.InventoryUI;
import com.factory.game.Items.ItemStack;
import com.factory.game.Renderer.FireFlyParticleEmitter;
import com.factory.game.Renderer.LightRenderer;
import com.factory.game.Renderer.WorldRenderer;
import com.factory.game.World.Animal;
import com.factory.game.World.CaveChunkLoader;
import com.factory.game.World.Chunk;
import com.factory.game.World.ChunkLoader;
import com.factory.game.World.ChunkLoader.RawChunkData;
import com.factory.game.World.CrusherManager;
import com.factory.game.World.GoblinoHutManager;
import com.factory.game.World.Ground_Tile;
import com.factory.game.World.ItemPipeConfig;
import com.factory.game.World.ItemPipeNetwork;
import com.factory.game.World.LiquidTank;
import com.factory.game.World.LiquidType;
import com.factory.game.World.MixingRecipe;
import com.factory.game.World.ObjectSpriteCache;
import com.factory.game.World.OreDrillManager;
import com.factory.game.World.PlacedObject;
import com.factory.game.World.PlacedObjectCache;
import com.factory.game.World.PlanterManager;
import com.factory.game.World.SmelterManager;
import com.factory.game.World.WorldDelta;
import com.factory.game.World.WorldObject;
import com.factory.game.World.WorldObjectRespawner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {

    private static final int LOAD_RADIUS = 10;
    private static final int UNLOAD_RADIUS = 11;
    private static final int RENDER_RADIUS = 3;
    private static final int MAX_PROMOTE_PER_FRAME = 4;
    private static final float CAVE_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private static final float CRUSH_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private static final float FURNACE_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private static final float STOVE_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private static final float WATER_INTERACT_RANGE = 1.8f * Main.TILE_SCALE;
    private static final float CRATE_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private static final int CRATE_SLOTS = 24;
    private static final float PICKUP_RANGE = 1.5f * Main.TILE_SCALE;
    private static final float SOLDERING_INTERACT_RANGE =
        1.2f * Main.TILE_SCALE;
    private static final float DEV_BARREL_INTERACT_RANGE =
        1.2f * Main.TILE_SCALE;
    private static final float SMELTER_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private static final float DISTILLERY_INTERACT_RANGE =
        1.2f * Main.TILE_SCALE;
    private static final float GLOBE_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private static final float GOBLINO_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;

    private static final float BREEDING_RANGE = 3f * Main.TILE_SCALE;

    private static final float CHUNK_LOADER_INTERACT_RANGE =
        1.2f * Main.TILE_SCALE;

    private static final String SURFACE_DELTA_PATH = "saves/surface.delta";
    private static final String CAVE_DELTA_PATH = "saves/cave.delta";

    private static final float DELTA_SAVE_INTERVAL = 5f;

    private final WorldRenderer renderer;
    private final Camera camera;
    private final ChunkLoader chunkLoader;
    private final CaveChunkLoader caveLoader;
    private final Color surfaceAmbient;
    private final Color caveAmbient;

    private final Map<String, Chunk> loadedChunks = new HashMap<>();
    private final Map<String, Ground_Tile> worldTileLookup = new HashMap<>();
    private final Map<String, float[]> objectHitboxes = new HashMap<>();
    private final Map<String, WorldObject> worldObjectLookup = new HashMap<>();
    private final Map<String, PlacedObject> placedObjectLookup =
        new HashMap<>();
    private final Map<String, PlacedObject> floorLookup = new HashMap<>();
    private final Map<String, PlacedObject> pipeLookup =
        new ConcurrentHashMap<>();

    private final Map<String, List<Animal>> animalsByChunk = new HashMap<>();

    private final Map<String, Float> objectRespawnTimers = new HashMap<>();
    private final Random respawnRng = new Random();

    private static final double COW_SPAWN_CHANCE = 0.015;
    private static final double AMARDILLO_SPAWN_CHANGE = 0.005f;
    private static final double WOLF_SPAWN_CHANCE = 0.0025;
    private static final long ANIMAL_SEED_SALT = 0x4E16BACEF00DL;

    private static final int MIN_POPULATION_THRESHOLD = 5;
    private static final float REPOP_CHECK_INTERVAL = 8f;
    private static final int REPOP_CHUNK_SAMPLE = 8;
    private static final int REPOP_CANDIDATE_CAP = 24;

    private float repopCheckTimer = 0f;

    private final ArrayList<DroppedItem> droppedItems = new ArrayList<>();

    private final Map<String, Chunk> savedSurfaceChunks = new HashMap<>();

    private WorldDelta surfaceDelta;
    private WorldDelta caveDelta;

    private boolean worldDirty = false;
    private boolean inCave = false;

    private static final float FILTER_PIPE_INTERACT_RANGE =
        1.2f * Main.TILE_SCALE;

    private WorldObject nearbyCaveObject = null;
    private PlacedObject nearbyCrushingPot = null;
    private PlacedObject nearbyFurnace = null;
    private PlacedObject nearbyStove = null;
    private Ground_Tile nearbyWaterTile = null;
    private PlacedObject nearbyStorageCrate = null;
    private PlacedObject nearbyFilterPipe = null;
    private PlacedObject nearbyFilterItemPipe = null;
    private PlacedObject nearbySolderingTable = null;
    private PlacedObject nearbyDevBarrel = null;
    private PlacedObject nearbyItemPipe = null;
    private PlacedObject nearbyCrusher = null;
    private PlacedObject nearbyPenguin = null;
    private PlacedObject nearbyPooObject = null;
    private PlacedObject nearbySmelter = null;
    private PlacedObject nearbyDistillery = null;
    private PlacedObject nearbyGlobe = null;
    private PlacedObject nearbyChunkLoader = null;
    private WorldObject nearbyGoblino = null;

    private final java.util.Map<String, ItemPipeConfig> itemPipeConfigs =
        new java.util.concurrent.ConcurrentHashMap<>();
    private final ItemPipeNetwork itemPipeNetwork = new ItemPipeNetwork();

    private final Map<String, Inventory> crateInventories = new HashMap<>();

    private final int noiseOffsetX;
    private final int noiseOffsetY;
    private final long seed;

    private float deltaSaveTimer = 0f;
    private boolean deltaSavePending = false;

    public boolean penguinDudeEnabled = true;
    public boolean pooDudeEnabled = true;

    private Minimap minimap;

    private static final float MIXER_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private PlacedObject nearbyMixer = null;

    private static final int MAX_BUBBLES = 40;

    private static final float PLANTER_INTERACT_RANGE = 1.2f * Main.TILE_SCALE;
    private static final float ORE_DRILL_INTERACT_RANGE =
        1.5f * Main.TILE_SCALE;

    private PlacedObject nearbyPlanter = null;
    private PlacedObject nearbyOreDrill = null;

    private final PlanterManager planterManager;
    private final CrusherManager crusherManager = new CrusherManager();
    private final SmelterManager smelterManager = new SmelterManager();
    private final OreDrillManager oreDrillManager = new OreDrillManager();
    private GoblinoHutManager goblinoHutManager = new GoblinoHutManager(0);

    private List<FireFlyParticleEmitter> fireflyEmitters = new ArrayList<>();

    private List<Bubble> bubbles = new ArrayList<>();

    public WorldManager(
        WorldRenderer renderer,
        Camera camera,
        ChunkLoader chunkLoader,
        CaveChunkLoader caveLoader,
        long seed,
        int noiseOffsetX,
        int noiseOffsetY,
        Color surfaceAmbient,
        Color caveAmbient,
        PlanterManager planterManager
    ) {
        this.renderer = renderer;
        this.camera = camera;
        this.chunkLoader = chunkLoader;
        this.caveLoader = caveLoader;
        this.seed = seed;
        this.noiseOffsetX = noiseOffsetX;
        this.noiseOffsetY = noiseOffsetY;
        this.surfaceAmbient = surfaceAmbient;
        this.caveAmbient = caveAmbient;
        this.planterManager = planterManager;

        surfaceDelta = Main.PERSIST_DATA
            ? WorldDelta.load(SURFACE_DELTA_PATH)
            : new WorldDelta();
        caveDelta = Main.PERSIST_DATA
            ? WorldDelta.load(CAVE_DELTA_PATH)
            : new WorldDelta();
        itemPipeNetwork.init(
            pipeLookup,
            placedObjectLookup,
            itemPipeConfigs,
            crateInventories
        );
        itemPipeNetwork.setCrusherManager(crusherManager);
        itemPipeNetwork.setSmelterManager(smelterManager);

        goblinoHutManager = new GoblinoHutManager(seed);
    }

    public void manageActiveChunks() {
        if (inCave) {
            manageCaveChunks();
            drainCompletedCaveChunks();
        } else {
            manageSurfaceChunks();
            drainCompletedSurfaceChunks();
        }

        if (worldDirty) {
            rebuildLookupAndAutotile();
            worldDirty = false;
        }
    }

    private void manageSurfaceChunks() {
        int camCX = cameraChunkX();
        int camCY = cameraChunkY();

        for (int dx = -LOAD_RADIUS; dx <= LOAD_RADIUS; dx++) {
            for (int dy = -LOAD_RADIUS; dy <= LOAD_RADIUS; dy++) {
                int cx = camCX + dx;
                int cy = camCY + dy;
                if (outOfBounds(cx, cy)) continue;
                String key = Chunk.key(cx, cy);
                if (!loadedChunks.containsKey(key)) {
                    chunkLoader.request(
                        cx,
                        cy,
                        noiseOffsetX,
                        noiseOffsetY,
                        seed,
                        surfaceDelta
                    );
                }
            }
        }

        for (String pinnedKey : collectPinnedChunkKeys()) {
            if (loadedChunks.containsKey(pinnedKey)) continue;
            String[] parts = pinnedKey.split(",");
            if (parts.length != 2) continue;
            try {
                int cx = Integer.parseInt(parts[0]);
                int cy = Integer.parseInt(parts[1]);
                if (!outOfBounds(cx, cy) && !chunkLoader.isInFlight(cx, cy)) {
                    chunkLoader.request(
                        cx,
                        cy,
                        noiseOffsetX,
                        noiseOffsetY,
                        seed,
                        surfaceDelta
                    );
                }
            } catch (NumberFormatException ignored) {}
        }

        unloadDistantChunks(camCX, camCY);
    }

    private void drainCompletedSurfaceChunks() {
        java.util.Set<String> pinnedKeys = collectPinnedChunkKeys();

        int promoted = 0;
        RawChunkData raw;
        while (
            promoted < MAX_PROMOTE_PER_FRAME &&
            (raw = chunkLoader.completed.poll()) != null
        ) {
            String rawKey = Chunk.key(raw.cx, raw.cy);

            if (
                !isInLoadRange(raw.cx, raw.cy) && !pinnedKeys.contains(rawKey)
            ) continue;

            if (loadedChunks.containsKey(rawKey)) continue;

            Chunk chunk = Chunk.buildFromTypes(
                raw.cx,
                raw.cy,
                raw.types,
                raw.objects
            );
            for (WorldDelta.PlacedRecord rec : surfaceDelta.getPlacedForChunk(
                raw.cx,
                raw.cy
            )) {
                PlacedObject po = new PlacedObject(rec.type, rec.x, rec.y);
                restoreFilterPipe(po, rec);
                restoreItemFilterPipe(po, rec);
                restoreDevBarrel(po, rec);
                restoreMixer(po, rec);
                restoreCrateInventory(rec);
                restoreLiquid(po, rec);
                restoreItemPipeConfig(rec);
                chunk.addPlacedObject(po, renderer.getLightRenderer());
                if (po.isPipe()) {
                    pipeLookup.put(rec.x + "," + rec.y, po);
                } else if (po.isFloor()) {
                    floorLookup.put(rec.x + "," + rec.y, po);
                } else {
                    int tw = PlacedObjectCache.getTileWidth(po.type);
                    int th = PlacedObjectCache.getTileHeight(po.type);
                    for (int dx2 = 0; dx2 < tw; dx2++) {
                        for (int dy2 = 0; dy2 < th; dy2++) {
                            placedObjectLookup.put(
                                (rec.x + dx2) + "," + (rec.y + dy2),
                                po
                            );
                        }
                    }
                }
            }
            chunk.attachBodies(
                renderer.getLightRenderer().getWorld(),
                renderer.getLightRenderer()
            );
            loadedChunks.put(rawKey, chunk);
            spawnAnimalsForChunk(chunk);
            worldDirty = true;
            if (minimap != null) minimap.onChunkLoaded(
                raw.cx,
                raw.cy,
                raw.types
            );
            promoted++;
        }
    }

    private void manageCaveChunks() {
        int camCX = cameraChunkX();
        int camCY = cameraChunkY();

        for (int dx = -LOAD_RADIUS; dx <= LOAD_RADIUS; dx++) {
            for (int dy = -LOAD_RADIUS; dy <= LOAD_RADIUS; dy++) {
                int cx = camCX + dx;
                int cy = camCY + dy;
                if (outOfBounds(cx, cy)) continue;
                String key = Chunk.key(cx, cy);
                if (
                    !loadedChunks.containsKey(key) &&
                    !caveLoader.isInFlight(cx, cy)
                ) {
                    caveLoader.request(
                        cx,
                        cy,
                        noiseOffsetX,
                        noiseOffsetY,
                        seed,
                        caveDelta
                    );
                }
            }
        }
        unloadDistantChunks(camCX, camCY);
    }

    public void spawnFirefly(float x, float y) {
        FireFlyParticleEmitter emitter = new FireFlyParticleEmitter(
            x,
            y,
            seed ^ 0xABCDEF
        );
        emitter.setLightRenderer(renderer.getLightRenderer());
        fireflyEmitters.add(emitter);
    }

    public int getFireflyCount() {
        return fireflyEmitters.size();
    }

    public void deleteFirefly() {
        if (!fireflyEmitters.isEmpty()) {
            fireflyEmitters.get(0).startFading();
        }
    }

    public void updateLampPosts(LightRenderer lightRenderer) {
        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type == PlacedObject.Type.LAMP_POST) {
                obj.updateLampPostLight(lightRenderer);
            }
        }
    }

    public void updateFireflys() {
        float delta = Gdx.graphics.getDeltaTime();
        Iterator<FireFlyParticleEmitter> it = fireflyEmitters.iterator();
        while (it.hasNext()) {
            FireFlyParticleEmitter emitter = it.next();
            emitter.update(delta);
            if (emitter.isDead()) {
                emitter.dispose();
                it.remove();
            }
        }
    }

    public void drawFireflys(Batch batch, Camera cam) {
        for (FireFlyParticleEmitter emitter : fireflyEmitters) {
            emitter.draw(batch, ObjectSpriteCache.whitePixel, cam);
        }
    }

    public void openMap(Main main, Minimap minimap) {
        minimap.setFullscreen(true);
    }

    private void drainCompletedCaveChunks() {
        int promoted = 0;
        RawChunkData raw;
        while (
            promoted < MAX_PROMOTE_PER_FRAME &&
            (raw = caveLoader.completed.poll()) != null
        ) {
            if (!isInLoadRange(raw.cx, raw.cy)) continue;
            String key = Chunk.key(raw.cx, raw.cy);
            if (loadedChunks.containsKey(key)) continue;

            Chunk chunk = Chunk.buildFromTypes(
                raw.cx,
                raw.cy,
                raw.types,
                raw.objects
            );
            for (WorldDelta.PlacedRecord rec : caveDelta.getPlacedForChunk(
                raw.cx,
                raw.cy
            )) {
                PlacedObject po = new PlacedObject(rec.type, rec.x, rec.y);
                restoreFilterPipe(po, rec);
                restoreItemFilterPipe(po, rec);
                restoreDevBarrel(po, rec);
                restoreMixer(po, rec);
                restoreCrateInventory(rec);
                restoreLiquid(po, rec);
                chunk.addPlacedObject(po, renderer.getLightRenderer());
                if (po.isPipe()) {
                    pipeLookup.put(rec.x + "," + rec.y, po);
                } else if (po.isFloor()) {
                    floorLookup.put(rec.x + "," + rec.y, po);
                } else {
                    int tw = PlacedObjectCache.getTileWidth(po.type);
                    int th = PlacedObjectCache.getTileHeight(po.type);
                    for (int dx2 = 0; dx2 < tw; dx2++) {
                        for (int dy2 = 0; dy2 < th; dy2++) {
                            placedObjectLookup.put(
                                (rec.x + dx2) + "," + (rec.y + dy2),
                                po
                            );
                        }
                    }
                }
            }
            chunk.attachBodies(
                renderer.getLightRenderer().getWorld(),
                renderer.getLightRenderer()
            );
            loadedChunks.put(key, chunk);
            worldDirty = true;
            promoted++;
        }
    }

    private void snapshotLiquids(Chunk chunk) {
        WorldDelta delta = activeDelta();
        int chunkX = chunk.getChunkX();
        int chunkY = chunk.getChunkY();

        java.util.Set<PlacedObject> visited =
            java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>()
            );

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (!visited.add(obj)) continue;
            if (
                Math.floorDiv(obj.getX(), Main.CHUNK_SIZE) != chunkX ||
                Math.floorDiv(obj.getY(), Main.CHUNK_SIZE) != chunkY
            ) continue;

            if (obj.isMixer()) {
                MixingRecipe recipe = obj.getSelectedMixingRecipe();
                int recipeOrd = (recipe != null)
                    ? MixingRecipe.ALL.indexOf(recipe)
                    : -1;
                delta.updateMixerRecipe(obj.getX(), obj.getY(), recipeOrd);

                LiquidTank[] inputs = obj.getMixerInputTanks();
                LiquidTank output = obj.getMixerOutputTank();
                if (inputs != null && output != null) {
                    float[] amounts = new float[inputs.length];
                    for (int i = 0; i < inputs.length; i++) amounts[i] =
                        inputs[i].getAmount();
                    delta.updateMixerTanks(
                        obj.getX(),
                        obj.getY(),
                        amounts,
                        output.getAmount()
                    );
                }
                continue;
            }

            LiquidTank tank = obj.getLiquidTank();
            if (tank == null) continue;
            delta.updateLiquid(
                obj.getX(),
                obj.getY(),
                tank.getType(),
                tank.getAmount()
            );

            if (obj.type == PlacedObject.Type.DEVBARREL) {
                LiquidType sel = obj.getSelectedProducedLiquid();
                delta.updateDevBarrelLiquid(
                    obj.getX(),
                    obj.getY(),
                    (sel != null) ? sel.ordinal() : -1
                );
            }
        }
    }

    private void snapshotCrates(Chunk chunk) {
        WorldDelta delta = activeDelta();
        int chunkX = chunk.getChunkX();
        int chunkY = chunk.getChunkY();

        java.util.Set<PlacedObject> visited =
            java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>()
            );

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (!visited.add(obj)) continue;
            if (
                Math.floorDiv(obj.getX(), Main.CHUNK_SIZE) != chunkX ||
                Math.floorDiv(obj.getY(), Main.CHUNK_SIZE) != chunkY
            ) continue;
            if (
                obj.type != PlacedObject.Type.STORAGE_CRATE &&
                obj.type != PlacedObject.Type.FOREST_CRATE &&
                obj.type != PlacedObject.Type.DESERT_CRATE &&
                obj.type != PlacedObject.Type.MOUNTAIN_CRATE
            ) continue;

            String key = obj.getX() + "," + obj.getY();
            Inventory inv = crateInventories.get(key);
            if (inv == null) continue;
            ItemStack[] contents = new ItemStack[inv.getSize()];
            for (int i = 0; i < inv.getSize(); i++) contents[i] = inv.getSlot(
                i
            );
            delta.updateCrateContents(obj.getX(), obj.getY(), contents);
        }
    }

    private static void restoreLiquid(
        PlacedObject po,
        WorldDelta.PlacedRecord rec
    ) {
        LiquidTank tank = po.getLiquidTank();
        if (
            tank == null || rec.liquidType == null || rec.liquidAmount <= 0f
        ) return;
        tank.deposit(rec.liquidType, rec.liquidAmount);
    }

    private static void restoreFilterPipe(
        PlacedObject po,
        WorldDelta.PlacedRecord rec
    ) {
        if (!po.isFilterPipe() || rec.filterAllowedMask == 0) return;
        po.setAllowedLiquidTypes(
            WorldDelta.fromFilterMask(rec.filterAllowedMask)
        );
    }

    private static void restoreItemFilterPipe(
        PlacedObject po,
        WorldDelta.PlacedRecord rec
    ) {
        if (!po.isFilterItemPipe() || rec.itemFilterOrdinals == null) return;
        po.setAllowedItemTypes(
            WorldDelta.fromItemFilterArray(rec.itemFilterOrdinals)
        );
    }

    private static void restoreDevBarrel(
        PlacedObject po,
        WorldDelta.PlacedRecord rec
    ) {
        if (po.type != PlacedObject.Type.DEVBARREL) return;
        if (rec.devBarrelLiquidOrdinal < 0) return;
        LiquidType[] vals = LiquidType.values();
        if (rec.devBarrelLiquidOrdinal < vals.length) {
            po.setSelectedProducedLiquid(vals[rec.devBarrelLiquidOrdinal]);
        }
    }

    private static void restoreMixer(
        PlacedObject po,
        WorldDelta.PlacedRecord rec
    ) {
        if (po.type != PlacedObject.Type.MIXER) return;
        if (
            rec.mixerRecipeOrdinal < 0 ||
            rec.mixerRecipeOrdinal >= MixingRecipe.ALL.size()
        ) return;

        MixingRecipe recipe = MixingRecipe.ALL.get(rec.mixerRecipeOrdinal);
        po.setSelectedMixingRecipe(recipe);

        LiquidTank[] inputTanks = po.getMixerInputTanks();
        if (inputTanks != null && rec.mixerInputAmounts != null) {
            for (
                int i = 0;
                i < inputTanks.length && i < rec.mixerInputAmounts.length;
                i++
            ) {
                if (rec.mixerInputAmounts[i] > 0f) {
                    inputTanks[i].deposit(
                        recipe.getInputs().get(i).type,
                        rec.mixerInputAmounts[i]
                    );
                }
            }
        }

        LiquidTank outputTank = po.getMixerOutputTank();
        if (outputTank != null && rec.mixerOutputAmount > 0f) {
            outputTank.deposit(recipe.getOutputType(), rec.mixerOutputAmount);
        }
    }

    private void restoreCrateInventory(WorldDelta.PlacedRecord rec) {
        if (
            rec.type != PlacedObject.Type.STORAGE_CRATE &&
            rec.type != PlacedObject.Type.FOREST_CRATE &&
            rec.type != PlacedObject.Type.DESERT_CRATE &&
            rec.type != PlacedObject.Type.MOUNTAIN_CRATE
        ) return;
        if (rec.crateContents == null) return;
        String key = rec.x + "," + rec.y;
        Inventory inv = crateInventories.computeIfAbsent(key, k ->
            new Inventory(CRATE_SLOTS)
        );
        for (
            int i = 0;
            i < rec.crateContents.length && i < inv.getSize();
            i++
        ) {
            inv.setSlot(i, rec.crateContents[i]);
        }
    }

    private void restoreItemPipeConfig(WorldDelta.PlacedRecord rec) {
        if (rec.type != PlacedObject.Type.ITEM_PIPE) return;
        if (rec.itemPipeConfigBits < 0) return;
        itemPipeConfigs.put(
            rec.x + "," + rec.y,
            ItemPipeConfig.decode(rec.itemPipeConfigBits)
        );
    }

    public void onItemPipeRemoved(int tileX, int tileY) {
        itemPipeConfigs.remove(tileX + "," + tileY);
        itemPipeNetwork.onPipeRemoved(tileX + "," + tileY);
    }

    private void unloadDistantChunks(int camCX, int camCY) {
        List<String> toRemove = new ArrayList<>();
        java.util.Set<String> pinnedKeys = collectPinnedChunkKeys();

        for (Map.Entry<String, Chunk> entry : loadedChunks.entrySet()) {
            Chunk c = entry.getValue();
            int dx = c.getChunkX() - camCX;
            int dy = c.getChunkY() - camCY;
            if (Math.abs(dx) > UNLOAD_RADIUS || Math.abs(dy) > UNLOAD_RADIUS) {
                if (!pinnedKeys.contains(entry.getKey())) {
                    toRemove.add(entry.getKey());
                }
            }
        }

        if (toRemove.isEmpty()) return;

        World b2 = renderer.getLightRenderer().getWorld();
        for (String key : toRemove) {
            Chunk chunk = loadedChunks.get(key);

            snapshotLiquids(chunk);
            snapshotCrates(chunk);

            evictChunkFromLookups(chunk);

            chunk.destroyBodies(b2, renderer.getLightRenderer());
            loadedChunks.remove(key);
            animalsByChunk.remove(key);
            objectRespawnTimers.keySet().removeIf(k -> k.startsWith(key + "|"));
        }
        worldDirty = true;
    }

    private void evictChunkFromLookups(Chunk chunk) {
        int baseX = chunk.getChunkX() * Main.CHUNK_SIZE;
        int baseY = chunk.getChunkY() * Main.CHUNK_SIZE;
        for (int x = baseX; x < baseX + Main.CHUNK_SIZE; x++) {
            for (int y = baseY; y < baseY + Main.CHUNK_SIZE; y++) {
                String k = x + "," + y;
                placedObjectLookup.remove(k);
                floorLookup.remove(k);
                pipeLookup.remove(k);
            }
        }
    }

    private void spawnAnimalsForChunk(Chunk chunk) {
        List<Animal> animals = null;

        for (Ground_Tile tile : chunk.getTiles()) {
            if ("grass".equals(tile.tileName)) {
                int worldX = tile.getX();
                int worldY = tile.getY();

                long tileSeed =
                    seed ^
                    ((long) worldX * 374761393L) ^
                    ((long) worldY * 668265263L) ^
                    ANIMAL_SEED_SALT;

                Random rng = new Random(tileSeed);
                if (rng.nextDouble() <= WOLF_SPAWN_CHANCE) {
                    float px = worldX * Main.TILE_SCALE;
                    float py = worldY * Main.TILE_SCALE;

                    if (animals == null) animals = new ArrayList<>();

                    animals.add(
                        new Animal(
                            Animal.Type.WOLF,
                            px,
                            py,
                            tileSeed ^ 0x9E3779B97F4A7C15L,
                            Animal.randomInitialGenes(Animal.Type.WOLF, rng)
                        )
                    );
                    continue;
                } else if (rng.nextDouble() <= COW_SPAWN_CHANCE) {
                    float px = worldX * Main.TILE_SCALE;
                    float py = worldY * Main.TILE_SCALE;

                    if (animals == null) animals = new ArrayList<>();

                    animals.add(
                        new Animal(
                            Animal.Type.COW,
                            px,
                            py,
                            tileSeed ^ 0x9E3779B97F4A7C15L,
                            Animal.randomInitialGenes(Animal.Type.COW, rng)
                        )
                    );
                    continue;
                }
            } else if ("badland".equals(tile.tileName)) {
                int worldX = tile.getX();
                int worldY = tile.getY();

                long tileSeed =
                    seed ^
                    ((long) worldX * 374761393L) ^
                    ((long) worldY * 668265263L) ^
                    ANIMAL_SEED_SALT;

                Random rng = new Random(tileSeed);
                if (rng.nextDouble() >= AMARDILLO_SPAWN_CHANGE) continue;

                float px = worldX * Main.TILE_SCALE;
                float py = worldY * Main.TILE_SCALE;

                if (animals == null) animals = new ArrayList<>();
                animals.add(
                    new Animal(
                        Animal.Type.ARMADILLO,
                        px,
                        py,
                        tileSeed ^ 0x9E3779B97F4A7C15L,
                        Animal.randomInitialGenes(Animal.Type.ARMADILLO, rng)
                    )
                );
            }
        }

        if (animals != null) {
            animalsByChunk.put(chunk.key(), animals);
        }
    }

    public void updateAnimals(float delta) {
        for (List<Animal> animals : animalsByChunk.values()) {
            for (Animal animal : animals) {
                animal.update(delta, this::isBlockedAt, this);
            }

            animals.removeIf(animal -> animal.shouldBeDead());
        }

        updateBreeding();

        repopCheckTimer -= delta;
        if (repopCheckTimer <= 0f) {
            repopCheckTimer = REPOP_CHECK_INTERVAL;
            restockAnimalPopulations();
        }
    }

    private void updateBreeding() {
        List<Animal> animals = getAnimals();
        if (animals.size() < 2) return;

        Set<Animal> alreadyBred = new HashSet<>();

        for (int i = 0; i < animals.size(); i++) {
            Animal a = animals.get(i);
            if (alreadyBred.contains(a) || !a.canReproduce()) continue;

            for (int j = i + 1; j < animals.size(); j++) {
                Animal b = animals.get(j);
                if (
                    alreadyBred.contains(b) ||
                    b.type != a.type ||
                    !b.canReproduce()
                ) continue;

                float dx = a.getWorldX() - b.getWorldX();
                float dy = a.getWorldY() - b.getWorldY();
                if (
                    dx * dx + dy * dy > BREEDING_RANGE * BREEDING_RANGE
                ) continue;

                float babyX = (a.getWorldX() + b.getWorldX()) * 0.5f;
                float babyY = (a.getWorldY() + b.getWorldY()) * 0.5f;
                Random mutationRng = new Random(
                    System.nanoTime() ^
                        ((long) a.hashCode() << 32) ^
                        b.hashCode()
                );
                Animal.Genes babyGenes = Animal.inheritGenes(a, b, mutationRng);
                spawnAnimal(a.type, babyX, babyY, babyGenes);

                a.onBred();
                b.onBred();
                alreadyBred.add(a);
                alreadyBred.add(b);
                break;
            }
        }
    }

    private void restockAnimalPopulations() {
        if (loadedChunks.isEmpty()) return;

        Map<Animal.Type, Integer> counts = new EnumMap<>(Animal.Type.class);
        for (Animal.Type type : Animal.Type.values()) counts.put(type, 0);
        for (Animal animal : getAnimals()) {
            if (animal.isDead()) continue;
            counts.merge(animal.type, 1, Integer::sum);
        }

        List<Chunk> chunkPool = new ArrayList<>(loadedChunks.values());

        for (Animal.Type type : Animal.Type.values()) {
            if (counts.get(type) >= MIN_POPULATION_THRESHOLD) continue;

            float[] spot = findSpawnSpot(type, chunkPool);
            if (spot == null) continue;
            spawnAnimal(
                type,
                spot[0],
                spot[1],
                Animal.randomInitialGenes(type, respawnRng)
            );
        }
    }

    private static String spawnTileNameFor(Animal.Type type) {
        return type == Animal.Type.ARMADILLO ? "badland" : "grass";
    }

    private float[] findSpawnSpot(Animal.Type type, List<Chunk> chunkPool) {
        if (chunkPool.isEmpty()) return null;

        String wantedTile = spawnTileNameFor(type);
        List<float[]> candidates = new ArrayList<>();

        int chunksToSample = Math.min(chunkPool.size(), REPOP_CHUNK_SAMPLE);
        for (int i = 0; i < chunksToSample; i++) {
            Chunk chunk = chunkPool.get(respawnRng.nextInt(chunkPool.size()));

            for (Ground_Tile tile : chunk.getTiles()) {
                if (!wantedTile.equals(tile.tileName)) continue;

                float px = tile.getX() * Main.TILE_SCALE;
                float py = tile.getY() * Main.TILE_SCALE;
                if (
                    isBlockedAt(
                        px,
                        py,
                        px + Main.TILE_SCALE,
                        py + Main.TILE_SCALE
                    )
                ) continue;

                candidates.add(new float[] { px, py });
            }

            if (candidates.size() >= REPOP_CANDIDATE_CAP) break;
        }

        if (candidates.isEmpty()) return null;
        return candidates.get(respawnRng.nextInt(candidates.size()));
    }

    public List<Animal> getAnimals() {
        if (animalsByChunk.isEmpty()) return Collections.emptyList();
        List<Animal> all = new ArrayList<>();
        for (List<Animal> animals : animalsByChunk.values())
            all.addAll(animals);
        return all;
    }

    public void updateObjectRespawn(float delta) {
        Map<WorldObject.Type, WorldObjectRespawner.RespawnConfig> configs =
            WorldObjectRespawner.all();
        if (configs.isEmpty()) return;

        for (Chunk chunk : loadedChunks.values()) {
            for (Map.Entry<
                WorldObject.Type,
                WorldObjectRespawner.RespawnConfig
            > entry : configs.entrySet()) {
                WorldObject.Type type = entry.getKey();
                WorldObjectRespawner.RespawnConfig cfg = entry.getValue();

                String timerKey = chunk.key() + "|" + type.name();
                float timer =
                    objectRespawnTimers.getOrDefault(timerKey, 0f) - delta;

                if (timer > 0f) {
                    objectRespawnTimers.put(timerKey, timer);
                    continue;
                }

                objectRespawnTimers.put(timerKey, cfg.checkInterval);
                attemptRespawnInChunk(chunk, type, cfg);
            }
        }
    }

    private void attemptRespawnInChunk(
        Chunk chunk,
        WorldObject.Type type,
        WorldObjectRespawner.RespawnConfig cfg
    ) {
        if (cfg.maxPerChunk >= 0) {
            int current = 0;
            for (WorldObject obj : chunk.getObjects()) {
                if (obj.type == type) current++;
            }
            if (current >= cfg.maxPerChunk) return;
        }

        ObjectSpriteCache.SpriteConfig spriteCfg = ObjectSpriteCache.getConfig(
            type
        );
        int tw = (spriteCfg != null) ? spriteCfg.tileWidth : 1;
        int th = (spriteCfg != null) ? spriteCfg.tileHeight : 1;

        for (Ground_Tile tile : chunk.getTiles()) {
            if (!cfg.groundTileName.equals(tile.tileName)) continue;
            if (respawnRng.nextDouble() >= cfg.spawnChance) continue;

            int tx = tile.getX();
            int ty = tile.getY();
            if (isFootprintOccupied(tx, ty, tw, th)) continue;

            spawnRespawnedObject(chunk, type, tx, ty, spriteCfg);
        }
    }

    private boolean isFootprintOccupied(int tx, int ty, int tw, int th) {
        for (int dx = 0; dx < tw; dx++) {
            for (int dy = 0; dy < th; dy++) {
                String key = (tx + dx) + "," + (ty + dy);
                if (worldObjectLookup.containsKey(key)) return true;
                if (placedObjectLookup.containsKey(key)) return true;
            }
        }
        return false;
    }

    private void spawnRespawnedObject(
        Chunk chunk,
        WorldObject.Type type,
        int tx,
        int ty,
        ObjectSpriteCache.SpriteConfig cfg
    ) {
        int spriteIndex = ObjectSpriteCache.resolveSpriteIndex(
            type,
            respawnRng
        );
        WorldObject obj = new WorldObject(type, tx, ty, spriteIndex);

        chunk.addObject(obj);
        obj.attachShadowBody(renderer.getLightRenderer().getWorld());
        obj.attachLight(renderer.getLightRenderer());

        int tw = (cfg != null) ? cfg.tileWidth : 1;
        int th = (cfg != null) ? cfg.tileHeight : 1;
        for (int dx = 0; dx < tw; dx++) {
            for (int dy = 0; dy < th; dy++) {
                worldObjectLookup.put((tx + dx) + "," + (ty + dy), obj);
            }
        }

        if (cfg != null && cfg.hasHitbox()) {
            float scale = Main.TILE_SCALE / 16f;
            float hx = tx * Main.TILE_SCALE + cfg.hitboxOffX * scale;
            float hy = ty * Main.TILE_SCALE + cfg.hitboxOffY * scale;
            registerHitbox(hx, hy, cfg.hitboxW * scale, cfg.hitboxH * scale);
        }
    }

    private boolean isInLoadRange(int cx, int cy) {
        return (
            Math.abs(cx - cameraChunkX()) <= UNLOAD_RADIUS &&
            Math.abs(cy - cameraChunkY()) <= UNLOAD_RADIUS
        );
    }

    private boolean outOfBounds(int cx, int cy) {
        return (
            cx < 0 ||
            cx >= Main.WORLD_CHUNKS ||
            cy < 0 ||
            cy >= Main.WORLD_CHUNKS
        );
    }

    private void rebuildLookupAndAutotile() {
        worldTileLookup.clear();
        for (Chunk chunk : loadedChunks.values()) {
            for (Ground_Tile tile : chunk.getTiles()) {
                worldTileLookup.put(tile.getX() + "," + tile.getY(), tile);
            }
        }
        for (Chunk chunk : loadedChunks.values()) {
            chunk.reupdate(worldTileLookup);
        }

        Ground_Tile.computeWaterDepths(worldTileLookup);

        objectHitboxes.clear();
        worldObjectLookup.clear();
        for (Chunk chunk : loadedChunks.values()) {
            for (WorldObject obj : chunk.getObjects()) {
                ObjectSpriteCache.SpriteConfig cfg =
                    ObjectSpriteCache.getConfig(obj.type);
                int tw = (cfg != null) ? cfg.tileWidth : 1;
                int th = (cfg != null) ? cfg.tileHeight : 1;

                for (int dx = 0; dx < tw; dx++) {
                    for (int dy = 0; dy < th; dy++) {
                        worldObjectLookup.put(
                            (obj.getX() + dx) + "," + (obj.getY() + dy),
                            obj
                        );
                    }
                }

                if (cfg == null || !cfg.hasHitbox()) continue;
                float scale = Main.TILE_SCALE / 16f;
                float hx =
                    obj.getX() * Main.TILE_SCALE + cfg.hitboxOffX * scale;
                float hy =
                    obj.getY() * Main.TILE_SCALE + cfg.hitboxOffY * scale;
                float hw = cfg.hitboxW * scale;
                float hh = cfg.hitboxH * scale;
                registerHitbox(hx, hy, hw, hh);
            }
        }

        placedObjectLookup.clear();
        floorLookup.clear();
        pipeLookup.clear();
        for (Chunk chunk : loadedChunks.values()) {
            for (PlacedObject obj : chunk.getPlacedObjects()) {
                if (obj.isPipe()) {
                    pipeLookup.put(obj.getX() + "," + obj.getY(), obj);
                } else if (obj.isFloor()) {
                    floorLookup.put(obj.getX() + "," + obj.getY(), obj);
                } else {
                    int tw = PlacedObjectCache.getTileWidth(obj.type);
                    int th = PlacedObjectCache.getTileHeight(obj.type);
                    for (int dx = 0; dx < tw; dx++) {
                        for (int dy = 0; dy < th; dy++) {
                            placedObjectLookup.put(
                                (obj.getX() + dx) + "," + (obj.getY() + dy),
                                obj
                            );
                        }
                    }
                }
            }
        }

        for (PlacedObject obj : placedObjectLookup.values()) {
            obj.reupdate(placedObjectLookup);
        }
        for (PlacedObject obj : floorLookup.values()) {
            obj.reupdate(floorLookup);
        }
        for (PlacedObject obj : placedObjectLookup.values()) {
            if (!obj.isSolid()) continue;
            float[] hbCorners = computePlacedHitbox(obj);
            registerHitbox(
                hbCorners[0],
                hbCorners[1],
                hbCorners[2] - hbCorners[0],
                hbCorners[3] - hbCorners[1]
            );
        }

        for (PlacedObject pipe : pipeLookup.values()) {
            pipe.reupdate(pipeLookup);
        }
    }

    private void registerHitbox(float hx, float hy, float hw, float hh) {
        float[] hbData = new float[] { hx, hy, hx + hw, hy + hh };
        int minTX = (int) Math.floor(hx / Main.TILE_SCALE);
        int maxTX = (int) Math.floor((hx + hw - 0.01f) / Main.TILE_SCALE);
        int minTY = (int) Math.floor(hy / Main.TILE_SCALE);
        int maxTY = (int) Math.floor((hy + hh - 0.01f) / Main.TILE_SCALE);
        for (int tx = minTX; tx <= maxTX; tx++) {
            for (int ty = minTY; ty <= maxTY; ty++) {
                objectHitboxes.put(tx + "," + ty, hbData);
            }
        }
    }

    public void unregisterHitbox(float hx, float hy, float hw, float hh) {
        int minTX = (int) Math.floor(hx / Main.TILE_SCALE);
        int maxTX = (int) Math.floor((hx + hw - 0.01f) / Main.TILE_SCALE);
        int minTY = (int) Math.floor(hy / Main.TILE_SCALE);
        int maxTY = (int) Math.floor((hy + hh - 0.01f) / Main.TILE_SCALE);
        for (int tx = minTX; tx <= maxTX; tx++) {
            for (int ty = minTY; ty <= maxTY; ty++) {
                objectHitboxes.remove(tx + "," + ty);
            }
        }
    }

    public float[] computePlacedHitbox(PlacedObject obj) {
        PlacedObjectCache.SpriteConfig cfg = PlacedObjectCache.getConfig(
            obj.type
        );
        float hx, hy, hw, hh;
        if (cfg != null && cfg.hasHitbox()) {
            float scale = Main.TILE_SCALE / 16f;
            hx = obj.getX() * Main.TILE_SCALE + cfg.hitboxOffX * scale;
            hy = obj.getY() * Main.TILE_SCALE + cfg.hitboxOffY * scale;
            hw = cfg.hitboxW * scale;
            hh = cfg.hitboxH * scale;
        } else {
            hx = obj.getX() * Main.TILE_SCALE;
            hy = obj.getY() * Main.TILE_SCALE;
            hw = Main.TILE_SCALE;
            hh = Main.TILE_SCALE;
        }
        return new float[] { hx, hy, hx + hw, hy + hh };
    }

    public void findNearbyCrusher() {
        nearbyCrusher = null;
        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);
        float bestDistSq = CRUSH_INTERACT_RANGE * CRUSH_INTERACT_RANGE;
        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.CRUSHER) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyCrusher = obj;
            }
        }
    }

    public PlacedObject getNearbyCrusher() {
        return nearbyCrusher;
    }

    public void findNearbySmelter() {
        nearbySmelter = null;
        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);
        float bestDistSq = SMELTER_INTERACT_RANGE * SMELTER_INTERACT_RANGE;
        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.SMELTER) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbySmelter = obj;
            }
        }
    }

    public PlacedObject getNearbySmelter() {
        return nearbySmelter;
    }

    public SmelterManager getSmelterManager() {
        return smelterManager;
    }

    public void findNearbyCaveObject() {
        nearbyCaveObject = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        WorldObject.Type targetType = inCave
            ? WorldObject.Type.CAVE_EXIT
            : WorldObject.Type.CAVE;

        float bestDistSq = CAVE_INTERACT_RANGE * CAVE_INTERACT_RANGE;

        for (Chunk chunk : loadedChunks.values()) {
            if (!chunk.isVisible(camera)) continue;
            for (WorldObject obj : chunk.getObjects()) {
                if (obj.type != targetType) continue;
                float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE;
                float oy =
                    obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
                float dx = px - ox;
                float dy = py - oy;
                float dsq = dx * dx + dy * dy;
                if (dsq < bestDistSq) {
                    bestDistSq = dsq;
                    nearbyCaveObject = obj;
                }
            }
        }
    }

    public void findNearbyCrushingPot() {
        nearbyCrushingPot = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = CRUSH_INTERACT_RANGE * CRUSH_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.CRUSHING_POT) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyCrushingPot = obj;
            }
        }
    }

    public void findNearbyFurnace() {
        nearbyFurnace = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = FURNACE_INTERACT_RANGE * FURNACE_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.FURNACE) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyFurnace = obj;
            }
        }
    }

    public void findNearbyStove() {
        nearbyStove = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = STOVE_INTERACT_RANGE * STOVE_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.STOVE) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyStove = obj;
            }
        }
    }

    public void findNearbyWaterTile() {
        nearbyWaterTile = null;

        if (inCave) return;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        int centerTX = (int) Math.floor(px / Main.TILE_SCALE);
        int centerTY = (int) Math.floor(py / Main.TILE_SCALE);

        float bestDistSq = WATER_INTERACT_RANGE * WATER_INTERACT_RANGE;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                Ground_Tile tile = worldTileLookup.get(
                    (centerTX + dx) + "," + (centerTY + dy)
                );
                if (tile == null || !tile.tileName.equals("water")) continue;
                float tx =
                    tile.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
                float ty =
                    tile.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
                float ddx = px - tx;
                float ddy = py - ty;
                float dsq = ddx * ddx + ddy * ddy;
                if (dsq < bestDistSq) {
                    bestDistSq = dsq;
                    nearbyWaterTile = tile;
                }
            }
        }
    }

    public void findNearbyStorageCrate() {
        nearbyStorageCrate = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = CRATE_INTERACT_RANGE * CRATE_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (
                obj.type != PlacedObject.Type.STORAGE_CRATE &&
                obj.type != PlacedObject.Type.FOREST_CRATE &&
                obj.type != PlacedObject.Type.DESERT_CRATE &&
                obj.type != PlacedObject.Type.MOUNTAIN_CRATE
            ) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyStorageCrate = obj;
            }
        }
    }

    public void findNearbyFilterPipe() {
        nearbyFilterPipe = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq =
            FILTER_PIPE_INTERACT_RANGE * FILTER_PIPE_INTERACT_RANGE;

        for (PlacedObject obj : pipeLookup.values()) {
            if (!obj.isFilterPipe()) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyFilterPipe = obj;
            }
        }
    }

    public void findNearbyFilterItemPipe() {
        nearbyFilterItemPipe = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq =
            FILTER_PIPE_INTERACT_RANGE * FILTER_PIPE_INTERACT_RANGE;

        for (PlacedObject obj : pipeLookup.values()) {
            if (!obj.isFilterItemPipe()) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyFilterItemPipe = obj;
            }
        }
    }

    public void findNearbySolderingTable() {
        nearbySolderingTable = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = SOLDERING_INTERACT_RANGE * SOLDERING_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.SOLDERING_TABLE) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbySolderingTable = obj;
            }
        }
    }

    public void findNearbyDevBarrel() {
        nearbyDevBarrel = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq =
            DEV_BARREL_INTERACT_RANGE * DEV_BARREL_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.DEVBARREL) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyDevBarrel = obj;
            }
        }
    }

    public void findNearbyDistillery() {
        nearbyDistillery = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq =
            DISTILLERY_INTERACT_RANGE * DISTILLERY_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.DISTILLERY) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyDistillery = obj;
            }
        }
    }

    public PlacedObject getNearbyDistillery() {
        return nearbyDistillery;
    }

    public void findNearbyGlobe() {
        nearbyGlobe = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = GLOBE_INTERACT_RANGE * GLOBE_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.GLOBE) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyGlobe = obj;
            }
        }
    }

    public PlacedObject getNearbyGlobe() {
        return nearbyGlobe;
    }

    public Inventory getOrCreateCrateInventory(int worldX, int worldY) {
        return crateInventories.computeIfAbsent(worldX + "," + worldY, k ->
            new Inventory(CRATE_SLOTS)
        );
    }

    public void removeCrateInventory(int worldX, int worldY) {
        crateInventories.remove(worldX + "," + worldY);
    }

    public void handleCaveTransition(Player player) {
        if (nearbyCaveObject == null) return;
        if (!inCave) enterCave(
            nearbyCaveObject.getX(),
            nearbyCaveObject.getY(),
            player
        );
        else exitCave(nearbyCaveObject.getX(), nearbyCaveObject.getY(), player);
    }

    private void enterCave(int surfaceCaveX, int surfaceCaveY, Player player) {
        World b2 = renderer.getLightRenderer().getWorld();

        savedSurfaceChunks.clear();
        for (Chunk chunk : loadedChunks.values()) {
            snapshotLiquids(chunk);
            snapshotCrates(chunk);
            chunk.destroyBodies(b2, renderer.getLightRenderer());
            savedSurfaceChunks.put(chunk.key(), chunk);
        }
        loadedChunks.clear();
        clearLookups();

        inCave = true;
        worldDirty = true;

        float destX = surfaceCaveX * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float destY = surfaceCaveY * Main.TILE_SCALE - Main.TILE_SCALE * 0.5f;
        player.setPosition(destX, destY);
        camera.followPlayer(destX, destY);

        renderer.getLightRenderer().setSunAmbient(caveAmbient);
    }

    private void exitCave(int caveExitX, int caveExitY, Player player) {
        World b2 = renderer.getLightRenderer().getWorld();

        for (Chunk chunk : loadedChunks.values()) {
            snapshotLiquids(chunk);
            snapshotCrates(chunk);
            chunk.destroyBodies(b2, renderer.getLightRenderer());
        }
        loadedChunks.clear();
        loadedChunks.putAll(savedSurfaceChunks);
        savedSurfaceChunks.clear();

        for (Chunk chunk : loadedChunks.values()) {
            chunk.attachBodies(b2, renderer.getLightRenderer());
        }
        clearLookups();

        inCave = false;
        worldDirty = true;

        float destX = caveExitX * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float destY = caveExitY * Main.TILE_SCALE - Main.TILE_SCALE * 0.5f;
        player.setPosition(destX, destY);
        camera.followPlayer(destX, destY);

        renderer.getLightRenderer().setSunAmbient(surfaceAmbient);
    }

    private void clearLookups() {
        worldTileLookup.clear();
        objectHitboxes.clear();
        worldObjectLookup.clear();
        placedObjectLookup.clear();
        pipeLookup.clear();
    }

    public boolean isBlockedAt(float px0, float py0, float px1, float py1) {
        int tileX0 = (int) Math.floor(px0 / Main.TILE_SCALE);
        int tileY0 = (int) Math.floor(py0 / Main.TILE_SCALE);
        int tileX1 = (int) Math.floor(px1 / Main.TILE_SCALE);
        int tileY1 = (int) Math.floor(py1 / Main.TILE_SCALE);

        for (int tx = tileX0; tx <= tileX1; tx++) {
            for (int ty = tileY0; ty <= tileY1; ty++) {
                String key = tx + "," + ty;

                Ground_Tile tile = worldTileLookup.get(key);
                if (tile == null) return true;
                if (tile.tileName.equals("water")) return true;
                if (tile.tileName.equals("cave")) return true;

                float[] hb = objectHitboxes.get(key);
                if (hb == null) continue;
                if (
                    px1 > hb[0] && px0 < hb[2] && py1 > hb[1] && py0 < hb[3]
                ) return true;
            }
        }
        return false;
    }

    public boolean isBlockedAtOnRaft(
        float px0,
        float py0,
        float px1,
        float py1
    ) {
        int tileX0 = (int) Math.floor(px0 / Main.TILE_SCALE);
        int tileY0 = (int) Math.floor(py0 / Main.TILE_SCALE);
        int tileX1 = (int) Math.floor(px1 / Main.TILE_SCALE);
        int tileY1 = (int) Math.floor(py1 / Main.TILE_SCALE);

        for (int tx = tileX0; tx <= tileX1; tx++) {
            for (int ty = tileY0; ty <= tileY1; ty++) {
                Ground_Tile tile = worldTileLookup.get(tx + "," + ty);
                if (tile == null) return true;
                if (!tile.tileName.equals("water")) return true;
            }
        }
        return false;
    }

    public boolean isNearShore(float playerWorldX, float playerWorldY) {
        int tileX = (int) Math.floor(
            (playerWorldX + Main.TILE_SCALE * 0.5f) / Main.TILE_SCALE
        );
        int tileY = (int) Math.floor(
            (playerWorldY + Main.TILE_SCALE * 0.5f) / Main.TILE_SCALE
        );

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;
                Ground_Tile tile = worldTileLookup.get(
                    (tileX + dx) + "," + (tileY + dy)
                );
                if (
                    tile != null &&
                    !tile.tileName.equals("water") &&
                    !tile.tileName.equals("cave")
                ) return true;
            }
        }
        return false;
    }

    public float[] findNearestShorePosition(
        float playerWorldX,
        float playerWorldY
    ) {
        int tileX = (int) Math.floor(
            (playerWorldX + Main.TILE_SCALE * 0.5f) / Main.TILE_SCALE
        );
        int tileY = (int) Math.floor(
            (playerWorldY + Main.TILE_SCALE * 0.5f) / Main.TILE_SCALE
        );

        for (int radius = 1; radius <= 3; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (
                        Math.abs(dx) != radius && Math.abs(dy) != radius
                    ) continue;
                    int nx = tileX + dx;
                    int ny = tileY + dy;
                    String key = nx + "," + ny;
                    Ground_Tile tile = worldTileLookup.get(key);
                    if (
                        tile != null &&
                        !tile.tileName.equals("water") &&
                        !tile.tileName.equals("cave") &&
                        !objectHitboxes.containsKey(key)
                    ) {
                        return new float[] {
                            nx * Main.TILE_SCALE,
                            ny * Main.TILE_SCALE,
                        };
                    }
                }
            }
        }
        return null;
    }

    public void updateDroppedItems(float delta) {
        for (DroppedItem item : droppedItems) {
            item.update(delta);
        }
    }

    public void checkItemPickup(Player player, InventoryUI inventoryUI) {
        Iterator<DroppedItem> it = droppedItems.iterator();
        while (it.hasNext()) {
            DroppedItem item = it.next();
            if (item.canPickup(player.getWorldX(), player.getWorldY())) {
                ItemStack stack = item.getItemStack();
                if (
                    player
                        .getInventory()
                        .addItem(stack.getItem(), stack.getQuantity())
                ) {
                    it.remove();
                }
            }
        }
    }

    public WorldDelta activeDelta() {
        return inCave ? caveDelta : surfaceDelta;
    }

    public void saveDeltasAsync() {
        deltaSavePending = true;
    }

    public void tickDeltaSave(float delta) {
        if (!Main.PERSIST_DATA || !deltaSavePending) return;
        deltaSaveTimer -= delta;
        if (deltaSaveTimer > 0f) return;
        deltaSaveTimer = DELTA_SAVE_INTERVAL;
        deltaSavePending = false;
        Thread t = new Thread(
            () -> {
                surfaceDelta.save(SURFACE_DELTA_PATH);
                caveDelta.save(CAVE_DELTA_PATH);
            },
            "delta-save"
        );
        t.setDaemon(true);
        t.start();
    }

    public void removeFromObjectLookup(int tileX, int tileY, WorldObject obj) {
        ObjectSpriteCache.SpriteConfig cfg = ObjectSpriteCache.getConfig(
            obj.type
        );
        int tw = (cfg != null) ? cfg.tileWidth : 1;
        int th = (cfg != null) ? cfg.tileHeight : 1;
        for (int dx = 0; dx < tw; dx++) {
            for (int dy = 0; dy < th; dy++) {
                worldObjectLookup.remove((tileX + dx) + "," + (tileY + dy));
            }
        }
    }

    public void removeWorldObject(WorldObject obj) {
        int chunkX = Math.floorDiv(obj.getX(), Main.CHUNK_SIZE);
        int chunkY = Math.floorDiv(obj.getY(), Main.CHUNK_SIZE);
        Chunk chunk = loadedChunks.get(Chunk.key(chunkX, chunkY));
        if (chunk != null) chunk.removeObject(obj);

        obj.destroyShadowBody(renderer.getLightRenderer().getWorld());
        obj.detachLight(renderer.getLightRenderer());

        removeFromObjectLookup(obj.getX(), obj.getY(), obj);

        ObjectSpriteCache.SpriteConfig cfg = ObjectSpriteCache.getConfig(
            obj.type
        );
        if (cfg != null && cfg.hasHitbox()) {
            float scale = Main.TILE_SCALE / 16f;
            float hx = obj.getX() * Main.TILE_SCALE + cfg.hitboxOffX * scale;
            float hy = obj.getY() * Main.TILE_SCALE + cfg.hitboxOffY * scale;
            unregisterHitbox(hx, hy, cfg.hitboxW * scale, cfg.hitboxH * scale);
        }
    }

    public void reupdatePlacedObjectAndNeighbours(int tx, int ty) {
        for (int nx = tx - 1; nx <= tx + 1; nx++) {
            for (int ny = ty - 1; ny <= ty + 1; ny++) {
                PlacedObject obj = placedObjectLookup.get(nx + "," + ny);
                if (obj != null) obj.reupdate(placedObjectLookup);
                PlacedObject floor = floorLookup.get(nx + "," + ny);
                if (floor != null) floor.reupdate(floorLookup);
            }
        }
    }

    public void reupdatePipeAndNeighbours(int tx, int ty) {
        int[][] offsets = {
            { 0, 0 },
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 },
        };
        for (int[] off : offsets) {
            PlacedObject pipe = pipeLookup.get(
                (tx + off[0]) + "," + (ty + off[1])
            );
            if (pipe != null) pipe.reupdate(pipeLookup);
        }
    }

    public int cameraChunkX() {
        float centreTileX =
            (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f) / Main.TILE_SCALE;
        return (int) Math.floor(centreTileX / Main.CHUNK_SIZE);
    }

    public int cameraChunkY() {
        float centreTileY =
            (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f) / Main.TILE_SCALE;
        return (int) Math.floor(centreTileY / Main.CHUNK_SIZE);
    }

    public void dispose() {
        if (Main.PERSIST_DATA) {
            for (Chunk chunk : loadedChunks.values()) {
                snapshotCrates(chunk);
                snapshotLiquids(chunk);
            }
            surfaceDelta.save(SURFACE_DELTA_PATH);
            caveDelta.save(CAVE_DELTA_PATH);
        }
        World b2World = renderer.getLightRenderer().getWorld();
        for (Chunk chunk : loadedChunks.values()) {
            chunk.destroyBodies(b2World, renderer.getLightRenderer());
        }
        chunkLoader.dispose();
        caveLoader.dispose();
    }

    public void setMinimap(Minimap minimap) {
        this.minimap = minimap;
    }

    public Map<String, Chunk> getLoadedChunks() {
        return loadedChunks;
    }

    public List<Chunk> getChunksForRendering() {
        int camCX = cameraChunkX();
        int camCY = cameraChunkY();

        List<Chunk> visible = new ArrayList<>();
        for (Chunk chunk : loadedChunks.values()) {
            int dx = chunk.getChunkX() - camCX;
            int dy = chunk.getChunkY() - camCY;
            if (
                Math.abs(dx) <= RENDER_RADIUS && Math.abs(dy) <= RENDER_RADIUS
            ) {
                visible.add(chunk);
            }
        }
        return visible;
    }

    public Map<String, WorldObject> getWorldObjectLookup() {
        return worldObjectLookup;
    }

    public Map<String, PlacedObject> getPlacedObjectLookup() {
        return placedObjectLookup;
    }

    public Map<String, PlacedObject> getFloorLookup() {
        return floorLookup;
    }

    public Map<String, PlacedObject> getPipeLookup() {
        return pipeLookup;
    }

    public Map<String, Ground_Tile> getWorldTileLookup() {
        return worldTileLookup;
    }

    public Map<String, float[]> getObjectHitboxes() {
        return objectHitboxes;
    }

    public ArrayList<DroppedItem> getDroppedItems() {
        return droppedItems;
    }

    public WorldObject getNearbyCaveObject() {
        return nearbyCaveObject;
    }

    public PlacedObject getNearbyCrushingPot() {
        return nearbyCrushingPot;
    }

    public PlacedObject getNearbyFurnace() {
        return nearbyFurnace;
    }

    public PlacedObject getNearbyStove() {
        return nearbyStove;
    }

    public Ground_Tile getNearbyWaterTile() {
        return nearbyWaterTile;
    }

    public PlacedObject getNearbyStorageCrate() {
        return nearbyStorageCrate;
    }

    public PlacedObject getNearbyFilterPipe() {
        return nearbyFilterPipe;
    }

    public PlacedObject getNearbyFilterItemPipe() {
        return nearbyFilterItemPipe;
    }

    public PlacedObject getNearbySolderingTable() {
        return nearbySolderingTable;
    }

    public PlacedObject getNearbyDevBarrel() {
        return nearbyDevBarrel;
    }

    public boolean isInCave() {
        return inCave;
    }

    public String getTileTypeAt(float worldPixelX, float worldPixelY) {
        int tileX = (int) Math.floor(worldPixelX / Main.TILE_SCALE);
        int tileY = (int) Math.floor(worldPixelY / Main.TILE_SCALE);
        Ground_Tile tile = worldTileLookup.get(tileX + "," + tileY);
        return tile != null ? tile.tileName : null;
    }

    public void findNearbyMixer() {
        nearbyMixer = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = MIXER_INTERACT_RANGE * MIXER_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.MIXER) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyMixer = obj;
            }
        }
    }

    public PlacedObject getNearbyMixer() {
        return nearbyMixer;
    }

    public CrusherManager getCrusherManager() {
        return crusherManager;
    }

    public PlacedObject getNearbyItemPipe() {
        return nearbyItemPipe;
    }

    public void updateItemPipes(float delta) {
        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.PLANTER) continue;
            String key = obj.getX() + "," + obj.getY();
            crateInventories.put(
                key,
                planterManager.getOrCreatePlanterInventory(obj)
            );
        }

        java.util.Set<PlacedObject> visitedDrills =
            java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>()
            );
        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.ORE_DRILL) continue;
            if (!visitedDrills.add(obj)) continue;
            String key = obj.getX() + "," + obj.getY();
            crateInventories.put(
                key,
                oreDrillManager.getOrCreateInventory(obj)
            );
        }

        crusherManager.update(delta, loadedChunks.values());
        smelterManager.update(delta, loadedChunks.values());
        itemPipeNetwork.update(delta);
    }

    public java.util.Map<String, ItemPipeConfig> getItemPipeConfigs() {
        return itemPipeConfigs;
    }

    public ItemPipeNetwork getItemPipeNetwork() {
        return itemPipeNetwork;
    }

    public void findNearbyItemPipe() {
        nearbyItemPipe = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = MIXER_INTERACT_RANGE * MIXER_INTERACT_RANGE;

        for (PlacedObject obj : pipeLookup.values()) {
            if (
                obj.type != PlacedObject.Type.ITEM_PIPE &&
                obj.type != PlacedObject.Type.FILTER_ITEM_PIPE
            ) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyItemPipe = obj;
            }
        }
    }

    public void findNearbyPlanter() {
        nearbyPlanter = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = PLANTER_INTERACT_RANGE * PLANTER_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.PLANTER) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyPlanter = obj;
            }
        }
    }

    public PlacedObject getNearbyPlanter() {
        return nearbyPlanter;
    }

    public PlanterManager getPlanterManager() {
        return planterManager;
    }

    public void findNearbyOreDrill() {
        nearbyOreDrill = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = ORE_DRILL_INTERACT_RANGE * ORE_DRILL_INTERACT_RANGE;

        java.util.Set<PlacedObject> visited =
            java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>()
            );

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (!visited.add(obj)) continue;
            if (obj.type != PlacedObject.Type.ORE_DRILL) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyOreDrill = obj;
            }
        }
    }

    public PlacedObject getNearbyOreDrill() {
        return nearbyOreDrill;
    }

    public OreDrillManager getOreDrillManager() {
        return oreDrillManager;
    }

    public void findNearbyPenguin() {
        nearbyPenguin = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = PLANTER_INTERACT_RANGE * PLANTER_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.PENGUIN_PLUSH) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyPenguin = obj;
            }
        }
    }

    public void findNearbyPooDude() {
        nearbyPooObject = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);

        float bestDistSq = PLANTER_INTERACT_RANGE * PLANTER_INTERACT_RANGE;

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (obj.type != PlacedObject.Type.POO_PET) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyPooObject = obj;
            }
        }
    }

    public PlacedObject getNearbyPenguin() {
        return nearbyPenguin;
    }

    public PlacedObject getNearbyPooGuy() {
        return nearbyPooObject;
    }

    public Map<String, Inventory> getCrateInventories() {
        return crateInventories;
    }

    public void findNearbyChunkLoader() {
        nearbyChunkLoader = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);
        float bestDistSq =
            CHUNK_LOADER_INTERACT_RANGE * CHUNK_LOADER_INTERACT_RANGE;

        java.util.Set<PlacedObject> visited =
            java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>()
            );

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (!visited.add(obj)) continue;
            if (obj.type != PlacedObject.Type.CHUNK_LOADER) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyChunkLoader = obj;
            }
        }
    }

    public PlacedObject getNearbyChunkLoader() {
        return nearbyChunkLoader;
    }

    public void findNearbyGoblino() {
        nearbyGoblino = null;

        float px = (-camera.cameraX + camera.VIRTUAL_WIDTH / 2f);
        float py = (-camera.cameraY + camera.VIRTUAL_HEIGHT / 2f);
        float bestDistSq = GOBLINO_INTERACT_RANGE * GOBLINO_INTERACT_RANGE;

        java.util.Set<WorldObject> visited =
            java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>()
            );

        for (WorldObject obj : worldObjectLookup.values()) {
            if (!visited.add(obj)) continue;
            if (obj.type != WorldObject.Type.GOBLINO_HUT) continue;
            float ox = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float oy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = px - ox;
            float dy = py - oy;
            float dsq = dx * dx + dy * dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                nearbyGoblino = obj;
            }
        }
    }

    public WorldObject getNearbyGoblino() {
        return nearbyGoblino;
    }

    private java.util.Set<String> collectPinnedChunkKeys() {
        java.util.Set<String> pinned = new java.util.HashSet<>();
        java.util.Set<PlacedObject> visited =
            java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>()
            );

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (!visited.add(obj)) continue;
            if (obj.type != PlacedObject.Type.CHUNK_LOADER) continue;
            if (!obj.isChunkLoaderActive()) continue;

            int lCX = Math.floorDiv(obj.getX(), Main.CHUNK_SIZE);
            int lCY = Math.floorDiv(obj.getY(), Main.CHUNK_SIZE);
            pinned.add(Chunk.key(lCX, lCY));

            pinned.addAll(obj.getPinnedChunkKeys());
        }
        return pinned;
    }

    public void updateChunkLoaders(float delta) {
        final float BASE_DRAIN = 1.00f;
        final float DRAIN_PER_PINNED = 0.15f;

        java.util.Set<PlacedObject> visited =
            java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>()
            );

        for (PlacedObject obj : placedObjectLookup.values()) {
            if (!visited.add(obj)) continue;
            if (obj.type != PlacedObject.Type.CHUNK_LOADER) continue;
            LiquidTank tank = obj.getLiquidTank();
            if (tank == null || tank.getAmount() <= 0f) continue;

            float drain =
                (BASE_DRAIN +
                    obj.getPinnedChunkKeys().size() * DRAIN_PER_PINNED) *
                delta;
            tank.withdraw(Math.min(drain, tank.getAmount()));
        }
    }

    public GoblinoHutManager getGoblinoHutManager() {
        return goblinoHutManager;
    }

    public void spawnAnimal(Animal.Type type, float worldX, float worldY) {
        spawnAnimal(
            type,
            worldX,
            worldY,
            new Animal.Genes(type.baseVisionTiles, 1f, 1f, 1f, 1f, 1f, 1f)
        );
    }

    public void spawnAnimal(
        Animal.Type type,
        float worldX,
        float worldY,
        Animal.Genes genes
    ) {
        int tileX = (int) Math.floor(worldX / Main.TILE_SCALE);
        int tileY = (int) Math.floor(worldY / Main.TILE_SCALE);
        int chunkX = Math.floorDiv(tileX, Main.CHUNK_SIZE);
        int chunkY = Math.floorDiv(tileY, Main.CHUNK_SIZE);
        Animal animal = new Animal(
            type,
            worldX,
            worldY,
            System.nanoTime(),
            genes
        );
        animalsByChunk
            .computeIfAbsent(Chunk.key(chunkX, chunkY), k -> new ArrayList<>())
            .add(animal);
    }

    public void spawnBubble(Player player) {
        if (bubbles.size() >= MAX_BUBBLES) {
            bubbles.remove(0).dispose();
        }
        bubbles.add(
            new Bubble(
                player.getWorldX(),
                player.getWorldY(),
                player.getFacingDirX(),
                player.getFacingDirY()
            )
        );
    }

    public void updateBubbles(float delta) {
        Iterator<Bubble> it = bubbles.iterator();
        while (it.hasNext()) {
            Bubble bubble = it.next();
            bubble.update(delta);
            if (bubble.isFinished()) {
                bubble.dispose();
                it.remove();
            }
        }
    }

    public List<Bubble> getBubbles() {
        return bubbles;
    }
}
