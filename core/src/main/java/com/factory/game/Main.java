package com.factory.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.CrushingMinigame;
import com.factory.game.Items.CrushingSelectionUI;
import com.factory.game.Items.DroppedItem;
import com.factory.game.Items.FishingMinigame;
import com.factory.game.Items.FurnaceMinigame;
import com.factory.game.Items.FurnaceSelectionUI;
import com.factory.game.Items.InventoryUI;
import com.factory.game.Items.ItemTextureCache;
import com.factory.game.Items.SiftMinigame;
import com.factory.game.Items.SiftSelectionUI;
import com.factory.game.Items.SiftingRecipe;
import com.factory.game.Items.SmeltingRecipe;
import com.factory.game.Items.SolderingMinigame;
import com.factory.game.Items.SolderingRecipe;
import com.factory.game.Items.SolderingSelectionUI;
import com.factory.game.Items.StorageCrateUI;
import com.factory.game.Renderer.DayNightCycle;
import com.factory.game.Renderer.WorldRenderer;
import com.factory.game.World.AnimalSpriteCache;
import com.factory.game.World.AnimalStatsUI;
import com.factory.game.World.CaveChunkLoader;
import com.factory.game.World.Chunk;
import com.factory.game.World.ChunkLoader;
import com.factory.game.World.ChunkLoaderUI;
import com.factory.game.World.CrusherUI;
import com.factory.game.World.DevBarrelUI;
import com.factory.game.World.DistilleryUI;
import com.factory.game.World.FilterPipeUI;
import com.factory.game.World.GoblinoHutManager;
import com.factory.game.World.GoblinoHutUI;
import com.factory.game.World.ItemFilterPipeUI;
import com.factory.game.World.ItemPipeUI;
import com.factory.game.World.LiquidManager;
import com.factory.game.World.MixerUI;
import com.factory.game.World.MixingRecipe;
import com.factory.game.World.ObjectSpriteCache;
import com.factory.game.World.OreDrillUI;
import com.factory.game.World.PlacedObject;
import com.factory.game.World.PlacedObjectCache;
import com.factory.game.World.PlanterManager;
import com.factory.game.World.PlanterUI;
import com.factory.game.World.SmelterUI;
import com.factory.game.World.TilesetCache;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Main extends ApplicationAdapter {

    public static final boolean PERSIST_DATA = false;
    public static final int CHUNK_SIZE = 8;
    public static final int WORLD_CHUNKS = 2048;
    public static final int TILE_SCALE = 48;

    private Camera camera;
    private WorldRenderer renderer;
    private Player player;
    private PenguinDude penguinDude;
    private PooDude pooDude;
    private InventoryUI inventoryUI;
    private SiftMinigame siftMinigame;
    private SiftSelectionUI siftSelectionUI;
    private CrushingMinigame crushingMinigame;
    private CrushingSelectionUI crushingSelectionUI;
    private FurnaceMinigame furnaceMinigame;
    private FurnaceSelectionUI furnaceSelectionUI;
    private FishingMinigame fishingMinigame;
    private StorageCrateUI storageCrateUI;
    private FilterPipeUI filterPipeUI;
    private ItemFilterPipeUI itemFilterPipeUI;
    private DevBarrelUI devBarrelUI;
    private SolderingMinigame solderingMinigame;
    private SolderingSelectionUI solderingSelectionUI;
    private WorldManager worldManager;
    private InteractionHandler interactionHandler;
    private LiquidManager liquidManager;
    private Texture ePromptTexture;
    private Texture tPromptTexture;
    private float totalTime = 0f;
    private Minimap minimap;
    private MixerUI mixerUI;
    private ItemPipeUI itemPipeUI;
    private PlanterUI planterUI;
    private CrusherUI crusherUI;
    private DevConsole devConsole;
    private SmelterUI smelterUI;
    private DistilleryUI distilleryUI;
    private ChunkLoaderUI chunkLoaderUI;
    private GoblinoHutManager goblinoHutManager;
    private GoblinoHutUI goblinoHutUI;
    private OreDrillUI oreDrillUI;
    private AnimalStatsUI animalStatsUI;

    private Clock clock;
    private Hunger hunger;

    private final DayNightCycle dayNightCycle = new DayNightCycle();

    public static boolean CREATIVE_MODE = true;

    public static Float SPEED_MULT = 1f;

    @Override
    public void create() {
        TilesetCache.init();
        ObjectSpriteCache.init();
        ItemTextureCache.init();
        PlacedObjectCache.init();
        AnimalSpriteCache.init();

        camera = new Camera();
        renderer = new WorldRenderer();

        long seed = 2L;
        Random rng = new Random(seed);
        int noiseOffsetX = rng.nextInt(10000);
        int noiseOffsetY = rng.nextInt(10000);

        ePromptTexture = new Texture(Gdx.files.internal("ui/E.png"));
        tPromptTexture = new Texture(Gdx.files.internal("ui/T.png"));

        int centerTileX = (WORLD_CHUNKS / 4) * CHUNK_SIZE;
        int centerTileY = (WORLD_CHUNKS / 4) * CHUNK_SIZE;
        int spawnTileX = centerTileX;
        int spawnTileY = centerTileY;

        outer: for (int radius = 0; radius <= 500; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (
                        Math.abs(dx) != radius && Math.abs(dy) != radius
                    ) continue;
                    int tx = centerTileX + dx;
                    int ty = centerTileY + dy;
                    if (
                        ChunkLoader.isLandAt(
                            tx,
                            ty,
                            noiseOffsetX,
                            noiseOffsetY,
                            seed
                        )
                    ) {
                        spawnTileX = tx;
                        spawnTileY = ty;
                        break outer;
                    }
                }
            }
        }

        float spawnX = spawnTileX * TILE_SCALE;
        float spawnY = spawnTileY * TILE_SCALE;
        player = new Player(spawnX, spawnY);
        player.createShadowBody(renderer.getLightRenderer().getWorld());

        penguinDude = new PenguinDude(spawnX, spawnY);
        pooDude = new PooDude(spawnX, spawnY);

        minimap = new Minimap(camera.VIRTUAL_WIDTH, camera.VIRTUAL_HEIGHT);

        inventoryUI = new InventoryUI(player.getInventory());
        siftMinigame = new SiftMinigame();
        siftSelectionUI = new SiftSelectionUI(player.getInventory());
        crushingMinigame = new CrushingMinigame();
        crushingSelectionUI = new CrushingSelectionUI(player.getInventory());
        furnaceMinigame = new FurnaceMinigame();
        furnaceSelectionUI = new FurnaceSelectionUI(player.getInventory());
        fishingMinigame = new FishingMinigame();
        storageCrateUI = new StorageCrateUI(player.getInventory());
        filterPipeUI = new FilterPipeUI();
        itemFilterPipeUI = new ItemFilterPipeUI();
        devBarrelUI = new DevBarrelUI();
        solderingMinigame = new SolderingMinigame();
        solderingSelectionUI = new SolderingSelectionUI(player.getInventory());
        liquidManager = new LiquidManager();
        itemPipeUI = new ItemPipeUI();
        mixerUI = new MixerUI();
        distilleryUI = new DistilleryUI();
        chunkLoaderUI = new ChunkLoaderUI(minimap);
        goblinoHutManager = new GoblinoHutManager(seed);
        goblinoHutUI = new GoblinoHutUI(goblinoHutManager);

        clock = new Clock(this, camera.VIRTUAL_WIDTH, camera.VIRTUAL_HEIGHT);
        hunger = new Hunger(camera.VIRTUAL_WIDTH, camera.VIRTUAL_HEIGHT);

        PlanterManager planterManager = new PlanterManager();
        planterUI = new PlanterUI(planterManager);

        PlacedObject.setPlanterManager(planterManager);

        camera.followPlayer(spawnX, spawnY);
        renderer
            .getLightRenderer()
            .setSunAmbient(DayNightCycle.SURFACE_AMBIENT);

        worldManager = new WorldManager(
            renderer,
            camera,
            new ChunkLoader(),
            new CaveChunkLoader(),
            seed,
            noiseOffsetX,
            noiseOffsetY,
            DayNightCycle.SURFACE_AMBIENT,
            DayNightCycle.CAVE_AMBIENT,
            planterManager
        );

        crusherUI = new CrusherUI(worldManager.getCrusherManager());
        smelterUI = new SmelterUI(worldManager.getSmelterManager());
        oreDrillUI = new OreDrillUI(worldManager.getOreDrillManager());

        animalStatsUI = new AnimalStatsUI(worldManager);

        worldManager.setMinimap(minimap);

        interactionHandler = new InteractionHandler(
            player,
            camera,
            renderer,
            inventoryUI,
            siftMinigame,
            siftSelectionUI,
            crushingMinigame,
            crushingSelectionUI,
            furnaceSelectionUI,
            fishingMinigame,
            storageCrateUI,
            filterPipeUI,
            itemFilterPipeUI,
            devBarrelUI,
            solderingMinigame,
            solderingSelectionUI,
            mixerUI,
            worldManager,
            ePromptTexture,
            tPromptTexture,
            itemPipeUI,
            planterUI,
            crusherUI,
            smelterUI,
            distilleryUI,
            minimap,
            chunkLoaderUI,
            goblinoHutUI,
            animalStatsUI,
            hunger
        );

        Gdx.input.setInputProcessor(minimap);

        minimap.prebakeWorld(seed, noiseOffsetX, noiseOffsetY);

        if (CREATIVE_MODE) {
            devConsole = new DevConsole(player, dayNightCycle, worldManager);
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime() * (SPEED_MULT);
        totalTime += delta;

        if (!worldManager.isInCave()) {
            dayNightCycle.clearCave();
            dayNightCycle.tick();
            renderer.applyDayNight(dayNightCycle);

            float t =
                (float) dayNightCycle.getTick() /
                DayNightCycle.DAY_LENGTH_TICKS;
            if (t > .85f || t < 0.15f) {
                int randomNum = new Random().nextInt(20);
                float x_offset = new Random().nextInt(6000) - 3000;
                float y_offset = new Random().nextInt(6000) - 3000;
                if (randomNum < 1 && worldManager.getFireflyCount() < 25) {
                    worldManager.spawnFirefly(
                        player.getWorldX() + x_offset,
                        player.getWorldY() + y_offset
                    );
                }

                if (worldManager.getFireflyCount() > 15) {
                    worldManager.deleteFirefly();
                }
            } else {
                int randomNum = new Random().nextInt(100);

                if (randomNum < 50) {
                    worldManager.deleteFirefly();
                }
            }

            worldManager.updateLampPosts(renderer.getLightRenderer());
            worldManager.updateFireflys();
        } else {
            dayNightCycle.setCave();
            renderer.applyDayNight(dayNightCycle);
        }

        PlacedObject.PIPES_VISIBLE = interactionHandler.isHoldingPipe();

        PlacedObject.ITEM_PIPES_VISIBLE = interactionHandler.isHoldingWrench();

        siftMinigame.update(delta);
        crushingMinigame.update(delta);
        furnaceMinigame.update(delta);
        fishingMinigame.update(delta);
        solderingMinigame.update(delta);
        worldManager.tickDeltaSave(delta);
        worldManager.updateItemPipes(delta);
        worldManager.updateBubbles(delta);
        goblinoHutUI.update(delta);

        if (CREATIVE_MODE && devConsole != null) {
            devConsole.handleInput();
        }

        if (devConsole == null || !devConsole.isVisible()) {
            if (
                worldManager.getNearbyOreDrill() != null &&
                !oreDrillUI.isVisible() &&
                com.badlogic.gdx.Gdx.input.isKeyJustPressed(
                    com.badlogic.gdx.Input.Keys.E
                )
            ) {
                oreDrillUI.open(
                    worldManager.getNearbyOreDrill(),
                    player.getInventory()
                );
            } else if (!oreDrillUI.isVisible()) {
                interactionHandler.handleInput();
            }
        }

        boolean uiBlocking =
            inventoryUI.isVisible() ||
            siftSelectionUI.isVisible() ||
            siftMinigame.isActive() ||
            crushingSelectionUI.isVisible() ||
            crushingMinigame.isActive() ||
            furnaceSelectionUI.isVisible() ||
            furnaceMinigame.isActive() ||
            fishingMinigame.isActive() ||
            storageCrateUI.isVisible() ||
            filterPipeUI.isVisible() ||
            itemFilterPipeUI.isVisible() ||
            devBarrelUI.isVisible() ||
            solderingSelectionUI.isVisible() ||
            crushingSelectionUI.isVisible() ||
            (devConsole != null && devConsole.isVisible()) ||
            solderingMinigame.isActive() ||
            mixerUI.isVisible() ||
            distilleryUI.isVisible() ||
            chunkLoaderUI.isVisible() ||
            goblinoHutUI.isVisible() ||
            oreDrillUI.isVisible();

        if (!uiBlocking) {
            worldManager.manageActiveChunks();
            worldManager.findNearbyCaveObject();
            worldManager.findNearbyCrushingPot();
            worldManager.findNearbyFurnace();
            worldManager.findNearbyWaterTile();
            worldManager.findNearbyStorageCrate();
            worldManager.findNearbyFilterPipe();
            worldManager.findNearbyFilterItemPipe();
            worldManager.findNearbySolderingTable();
            worldManager.findNearbyDevBarrel();
            worldManager.findNearbyMixer();
            worldManager.findNearbyItemPipe();
            worldManager.findNearbyPlanter();
            worldManager.findNearbyCrusher();
            worldManager.findNearbySmelter();
            worldManager.findNearbyPenguin();
            worldManager.findNearbyPooDude();
            worldManager.findNearbyDistillery();
            worldManager.findNearbyGlobe();
            worldManager.findNearbyChunkLoader();
            worldManager.findNearbyGoblino();
            worldManager.findNearbyOreDrill();

            player.update(
                delta,
                player.isOnRaft()
                    ? worldManager::isBlockedAtOnRaft
                    : worldManager::isBlockedAt
            );

            hunger.update(delta);

            penguinDude.visible = false;

            if (worldManager.penguinDudeEnabled) {
                penguinDude.visible = true;
                penguinDude.update(delta, player, worldManager::isBlockedAt);
            } else {
                penguinDude.setPosition(player.getWorldX(), player.getWorldY());
            }

            pooDude.visible = false;
            if (worldManager.pooDudeEnabled) {
                pooDude.visible = true;
                pooDude.update(delta, player, worldManager::isBlockedAt);
            } else {
                pooDude.setPosition(player.getWorldX(), player.getWorldY());
            }

            if (!player.isOnRaft() && player.hasPendingFootprint()) {
                String tileType = worldManager.getTileTypeAt(
                    player.getPendingFpX(),
                    player.getPendingFpY()
                );
                renderer
                    .getFootprintManager()
                    .spawnFootprint(
                        player.getPendingFpX(),
                        player.getPendingFpY(),
                        player.getPendingFpFacing(),
                        player.getPendingFpLeft(),
                        tileType
                    );
                player.clearPendingFootprint();
            }

            worldManager.updateDroppedItems(delta);
            worldManager.updateAnimals(delta);
            animalStatsUI.update(delta);
            worldManager.updateObjectRespawn(delta);
            worldManager.checkItemPickup(player, inventoryUI);

            if (!player.isOnRaft() && !animalStatsUI.isVisible()) {
                interactionHandler.handleHarvestInput(delta);
                interactionHandler.handlePlacementInput();
                interactionHandler.handleWrenchClick();
            }
        } else if (inventoryUI.isVisible()) {
            inventoryUI.handleInput();
        } else if (itemPipeUI.isVisible()) {
            itemPipeUI.handleInput();
            interactionHandler.tickItemPipeUI();
        } else if (siftSelectionUI.isVisible()) {
            siftSelectionUI.handleInput();
            SiftingRecipe chosenSift = siftSelectionUI.pollChosen();
            if (chosenSift != null) {
                player.getInventory().removeItem(chosenSift.getInputItem(), 1);
                siftMinigame.start(chosenSift);
            }
        } else if (crushingSelectionUI.isVisible()) {
            crushingSelectionUI.handleInput();
            com.factory.game.Items.CrushingRecipe chosenCrush =
                crushingSelectionUI.pollChosen();
            if (chosenCrush != null) {
                player.getInventory().removeItem(chosenCrush.getInputItem(), 1);
                crushingMinigame.start(chosenCrush);
            }
        } else if (furnaceSelectionUI.isVisible()) {
            furnaceSelectionUI.handleInput();
            SmeltingRecipe chosenSmelt = furnaceSelectionUI.pollChosen();
            if (chosenSmelt != null) {
                player.getInventory().removeItem(chosenSmelt.getInputItem(), 1);
                furnaceMinigame.start(chosenSmelt);
            }
        } else if (storageCrateUI.isVisible()) {
            storageCrateUI.handleInput();
            if (storageCrateUI.isDirty()) {
                storageCrateUI.clearDirty();
                worldManager.saveDeltasAsync();
            }
        } else if (oreDrillUI.isVisible()) {
            oreDrillUI.handleInput();
        } else if (filterPipeUI.isVisible()) {
            filterPipeUI.handleInput();
            if (filterPipeUI.isDirty()) {
                filterPipeUI.clearDirty();
                com.factory.game.World.PlacedObject pipe =
                    filterPipeUI.getFilterPipe();
                if (pipe != null) {
                    int mask = com.factory.game.World.WorldDelta.toFilterMask(
                        pipe.getAllowedLiquidTypes()
                    );
                    worldManager
                        .activeDelta()
                        .updateFilterPipe(pipe.getX(), pipe.getY(), mask);
                    worldManager.saveDeltasAsync();
                }
            }
        } else if (itemFilterPipeUI.isVisible()) {
            itemFilterPipeUI.handleInput();
            if (itemFilterPipeUI.isDirty()) {
                itemFilterPipeUI.clearDirty();
                com.factory.game.World.PlacedObject pipe =
                    itemFilterPipeUI.getFilterPipe();
                if (pipe != null) {
                    int[] ordinals =
                        com.factory.game.World.WorldDelta.toItemFilterArray(
                            pipe.getAllowedItemTypes()
                        );
                    worldManager
                        .activeDelta()
                        .updateItemFilterPipe(
                            pipe.getX(),
                            pipe.getY(),
                            ordinals
                        );
                    worldManager.saveDeltasAsync();
                }
            }
        } else if (devBarrelUI.isVisible()) {
            devBarrelUI.handleInput();
            interactionHandler.tickDevBarrelUI();
        } else if (solderingSelectionUI.isVisible()) {
            solderingSelectionUI.handleInput();
            SolderingRecipe chosenSolder = solderingSelectionUI.pollChosen();
            if (chosenSolder != null) {
                for (java.util.Map.Entry<
                    com.factory.game.Items.Item,
                    Integer
                > entry : chosenSolder.getInputs().entrySet()) {
                    player
                        .getInventory()
                        .removeItem(entry.getKey(), entry.getValue());
                }
                solderingMinigame.start(chosenSolder);
            }
        }

        if (mixerUI.isVisible()) {
            mixerUI.handleInput();
            if (mixerUI.isDirty()) {
                mixerUI.clearDirty();
                PlacedObject mixer = mixerUI.getMixer();
                if (mixer != null) {
                    MixingRecipe recipe = mixer.getSelectedMixingRecipe();
                    int ordinal = (recipe == null)
                        ? -1
                        : MixingRecipe.ALL.indexOf(recipe);
                    worldManager
                        .activeDelta()
                        .updateMixerRecipe(mixer.getX(), mixer.getY(), ordinal);
                    worldManager.saveDeltasAsync();
                }
            }
        }

        if (distilleryUI.isVisible()) {
            distilleryUI.handleInput();
            if (distilleryUI.isDirty()) {
                distilleryUI.clearDirty();
                PlacedObject distillery = distilleryUI.getDistillery();
                if (distillery != null) {
                    com.factory.game.World.DistilleryRecipe recipe =
                        distillery.getSelectedDistilleryRecipe();
                    int ordinal = (recipe == null)
                        ? -1
                        : com.factory.game.World.DistilleryRecipe.ALL.indexOf(
                              recipe
                          );
                    worldManager
                        .activeDelta()
                        .updateDistilleryRecipe(
                            distillery.getX(),
                            distillery.getY(),
                            ordinal
                        );
                    worldManager.saveDeltasAsync();
                }
            }
        }

        if (crusherUI.isVisible()) {
            crusherUI.handleInput();
        }

        Map<String, PlacedObject> machineLookup = new HashMap<>();
        for (Chunk chunk : worldManager.getLoadedChunks().values()) {
            for (PlacedObject obj : chunk.getPlacedObjects()) {
                if (obj.isLiquidMachine()) {
                    machineLookup.put(obj.getX() + "," + obj.getY(), obj);
                }
            }
        }

        worldManager
            .getPlanterManager()
            .update(
                delta,
                worldManager.getLoadedChunks().values(),
                machineLookup,
                worldManager.getPipeLookup(),
                worldManager.getItemPipeNetwork().getBuffers(),
                worldManager.getCrateInventories()
            );

        worldManager
            .getOreDrillManager()
            .update(delta, worldManager.getLoadedChunks().values());

        for (PlanterManager.HarvestedCrop crop : worldManager
            .getPlanterManager()
            .pollHarvests()) {
            if (crop.targetPipeKey == null) {
                worldManager
                    .getDroppedItems()
                    .add(
                        DroppedItem.spawnBurst(
                            crop.crop,
                            crop.worldPixelX,
                            crop.worldPixelY
                        )
                    );
            }
        }

        liquidManager.update(delta, worldManager.getLoadedChunks().values());

        List<LiquidManager.CastDrop> castDrops = liquidManager.pollCastDrops();
        for (LiquidManager.CastDrop drop : castDrops) {
            worldManager
                .getDroppedItems()
                .add(
                    new DroppedItem(
                        drop.ingot,
                        drop.worldPixelX,
                        drop.worldPixelY,
                        0f,
                        -300f
                    )
                );
        }

        camera.followPlayer(player.getWorldX(), player.getWorldY());

        Color sky = dayNightCycle.getSkyColor();
        ScreenUtils.clear(sky.r, sky.g, sky.b, 1f);

        renderer.render(
            worldManager.getChunksForRendering(),
            camera,
            player,
            worldManager.getDroppedItems(),
            delta,
            penguinDude,
            pooDude,
            worldManager.getAnimals(),
            worldManager.getBubbles()
        );
        renderer.getLightRenderer().render(camera);

        boolean liquidHovered = renderer.isLiquidMachineHovered();

        SpriteBatch batch = renderer.getBatch();
        ShaderProgram oldShader = batch.getShader();
        batch.setShader(null);
        batch.begin();

        worldManager.drawFireflys(renderer.getBatch(), camera);

        inventoryUI.render(batch);

        if (
            player.isOnRaft() &&
            worldManager.isNearShore(player.getWorldX(), player.getWorldY())
        ) {
            interactionHandler.drawRaftDismountTPrompt(batch, totalTime);
        } else if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingRaft() &&
            worldManager.getNearbyWaterTile() != null
        ) {
            interactionHandler.drawRaftMountTPrompt(batch, totalTime);
        }

        if (!inventoryUI.isVisible() && !uiBlocking && !player.isOnRaft()) {
            int mouseScreenX = Gdx.input.getX();
            int mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();
            float worldMouseX = mouseScreenX - camera.cameraX;
            float worldMouseY = mouseScreenY - camera.cameraY;

            float tileX = (int) Math.floor(worldMouseX / Main.TILE_SCALE);
            float tileY = (int) Math.floor(worldMouseY / Main.TILE_SCALE);

            float playerCX = player.getWorldX() + Main.TILE_SCALE * 0.5f;
            float playerCY = player.getWorldY() + Main.TILE_SCALE * 0.5f;
            float objCX = tileX * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float objCY = tileY * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
            float dx = playerCX - objCX;
            float dy = playerCY - objCY;

            float red = 1f;
            float green = 1f;
            float blue = 1f;

            if (
                dx * dx + dy * dy >
                interactionHandler.HARVEST_RANGE *
                interactionHandler.HARVEST_RANGE
            ) {
                red = 1f;
                green = 0f;
                blue = 0f;
            } else {
                red = .8f;
                green = 1f;
                blue = .8f;
            }

            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.input.getY();

            tileX =
                (float) Math.floor((mouseX - camera.cameraX) / TILE_SCALE) *
                    TILE_SCALE +
                camera.cameraX;
            tileY =
                (float) Math.floor(
                        (camera.VIRTUAL_HEIGHT - mouseY - camera.cameraY) /
                            TILE_SCALE
                    ) *
                    TILE_SCALE +
                camera.cameraY;

            batch.end();

            ShapeRenderer shapeRenderer = new ShapeRenderer();

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(red, green, blue, 0.1f);
            shapeRenderer.rect(tileX, tileY, TILE_SCALE, TILE_SCALE);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

            batch.begin();
        }

        if (!liquidHovered) {
            if (
                worldManager.getNearbyCaveObject() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyCrushingPot() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawCrushingEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyFurnace() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawFurnaceEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyStorageCrate() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawStorageCrateEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyFilterPipe() != null &&
                !uiBlocking &&
                !player.isOnRaft() &&
                interactionHandler.isHoldingFilterPipe()
            ) {
                interactionHandler.drawFilterPipeEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyFilterItemPipe() != null &&
                !uiBlocking &&
                !player.isOnRaft() &&
                interactionHandler.isHoldingFilterItemPipe()
            ) {
                interactionHandler.drawFilterItemPipeEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyDevBarrel() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawDevBarrelEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbySolderingTable() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawSolderingEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyMixer() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawMixerEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyPlanter() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawPlanterEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyCrusher() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawCrusherEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbySmelter() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawSmelterEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyPenguin() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawPenguinEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyPooGuy() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawPooGuyEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyDistillery() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawDistilleryEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyGlobe() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawGlobeEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyChunkLoader() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawChunkLoaderEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyGoblino() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                interactionHandler.drawGoblinoEPrompt(batch, totalTime);
            } else if (
                worldManager.getNearbyOreDrill() != null &&
                !uiBlocking &&
                !player.isOnRaft()
            ) {
                PlacedObject od = worldManager.getNearbyOreDrill();
                float promptX =
                    od.getX() * Main.TILE_SCALE +
                    Main.TILE_SCALE +
                    camera.cameraX;
                float promptY =
                    od.getY() * Main.TILE_SCALE +
                    Main.TILE_SCALE * 2f +
                    camera.cameraY +
                    4;
                batch.draw(ePromptTexture, promptX - 16, promptY, 32, 32);
            }
        }

        if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingSift() &&
            !CraftingManager.getAvailableSiftRecipes(
                player.getInventory()
            ).isEmpty()
        ) {
            interactionHandler.drawSiftTPrompt(batch, totalTime);
        }

        if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingFishingRod() &&
            worldManager.getNearbyWaterTile() != null
        ) {
            interactionHandler.drawFishingTPrompt(batch, totalTime);
        }

        if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingFishingRod() &&
            worldManager.getNearbyWaterTile() != null
        ) {
            interactionHandler.drawFishingTPrompt(batch, totalTime);
        }

        if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingAnimalViewer()
        ) {
            interactionHandler.drawAnimalViewerTPrompt(batch, totalTime);
        }

        if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingWrench() &&
            worldManager.getNearbyItemPipe() != null
        ) {
            itemPipeUI.drawAllPipeOverlays(
                batch,
                worldManager.getItemPipeConfigs(),
                worldManager.getPipeLookup(),
                camera.cameraX,
                camera.cameraY
            );

            interactionHandler.drawWrenchTPrompt(batch, totalTime);
        }

        if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingBubbleWand()
        ) {
            interactionHandler.drawBubbleWandTPrompt(batch, totalTime);
        }

        if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingFlashlight()
        ) {
            interactionHandler.drawFlashlightTPrompt(batch, totalTime);
        }

        if (
            !inventoryUI.isVisible() &&
            !uiBlocking &&
            !player.isOnRaft() &&
            interactionHandler.isHoldingFood()
        ) {
            interactionHandler.drawEatTPrompt(batch, totalTime);
        }

        siftSelectionUI.render(batch);
        siftMinigame.render(batch);
        crushingSelectionUI.render(batch);
        crushingMinigame.render(batch);
        furnaceSelectionUI.render(batch);
        furnaceMinigame.render(batch);
        fishingMinigame.render(batch);
        storageCrateUI.render(batch);
        filterPipeUI.render(batch);
        itemFilterPipeUI.render(batch);
        devBarrelUI.render(batch);
        solderingSelectionUI.render(batch);
        solderingMinigame.render(batch);
        mixerUI.render(batch);
        planterUI.render(batch);
        smelterUI.render(batch);
        distilleryUI.render(batch);
        chunkLoaderUI.render(batch);
        goblinoHutUI.render(batch);
        oreDrillUI.render(batch);

        if (
            (player.getInventoryMap() || minimap.isFullscreen()) &&
            !inventoryUI.isVisible() &&
            !uiBlocking
        ) {
            minimap.handleInput(uiBlocking || inventoryUI.isVisible());
            minimap.render(batch, player.getWorldX(), player.getWorldY());
        }

        if (
            player.getInventoryClock() &&
            !inventoryUI.isVisible() &&
            !uiBlocking
        ) {
            clock.render(batch, dayNightCycle.getTimeOfDay());
        }

        if (player.getInventoryFlashlight() && player.isFlashlightOn()) {
            player.updateLightSourcePosition(camera);
        }

        hunger.render(batch, totalTime);

        itemPipeUI.render(batch);
        crusherUI.render(batch);

        if (CREATIVE_MODE && devConsole != null) devConsole.render(batch);

        animalStatsUI.render(batch);

        batch.end();
        batch.setShader(oldShader);

        SiftMinigame.SiftResult siftResult = siftMinigame.pollResult();
        if (siftResult != null) player
            .getInventory()
            .addItem(siftResult.item, siftResult.qty);

        CrushingMinigame.CrushResult crushResult =
            crushingMinigame.pollResult();
        if (crushResult != null) player
            .getInventory()
            .addItem(crushResult.item, crushResult.qty);

        FurnaceMinigame.FurnaceResult furnaceResult =
            furnaceMinigame.pollResult();
        if (furnaceResult != null && furnaceResult.success) {
            PlacedObject furnace = worldManager.getNearbyFurnace();
            if (furnace != null && furnace.getLiquidTank() != null) {
                furnace
                    .getLiquidTank()
                    .deposit(furnaceResult.liquid, furnaceResult.amount);
            }
        }

        FishingMinigame.FishResult fishResult = fishingMinigame.pollResult();
        if (fishResult != null && fishResult.success) {
            player.getInventory().addItem(fishResult.item, fishResult.qty);
        }

        SolderingMinigame.SolderResult solderResult =
            solderingMinigame.pollResult();
        if (solderResult != null && solderResult.success) {
            player.getInventory().addItem(solderResult.item, solderResult.qty);
        }

        if (
            Gdx.input.getInputProcessor() != minimap && !inventoryUI.isVisible()
        ) {
            Gdx.input.setInputProcessor(minimap);
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.resize(width, height);
        renderer.resize(width, height);
        inventoryUI.resize(width, height);
        siftMinigame.resize(width, height);
        siftSelectionUI.resize(width, height);
        crushingMinigame.resize(width, height);
        crushingSelectionUI.resize(width, height);
        furnaceMinigame.resize(width, height);
        furnaceSelectionUI.resize(width, height);
        fishingMinigame.resize(width, height);
        storageCrateUI.resize(width, height);
        filterPipeUI.resize(width, height);
        itemFilterPipeUI.resize(width, height);
        devBarrelUI.resize(width, height);
        solderingMinigame.resize(width, height);
        mixerUI.resize(width, height);
        solderingSelectionUI.resize(width, height);
        camera.followPlayer(player.getWorldX(), player.getWorldY());
        minimap.resize(width, height);
        itemPipeUI.resize(width, height);
        planterUI.resize(width, height);
        crusherUI.resize(width, height);
        smelterUI.resize(width, height);
        chunkLoaderUI.resize(width, height);
        goblinoHutUI.resize(width, height);
        oreDrillUI.resize(width, height);
        animalStatsUI.resize(width, height);
        if (CREATIVE_MODE && devConsole != null) devConsole.resize(
            width,
            height
        );
    }

    @Override
    public void dispose() {
        worldManager.dispose();
        renderer.dispose();
        ePromptTexture.dispose();
        tPromptTexture.dispose();
        TilesetCache.dispose();
        ObjectSpriteCache.dispose();
        ItemTextureCache.dispose();
        PlacedObjectCache.dispose();
        inventoryUI.dispose();
        siftMinigame.dispose();
        siftSelectionUI.dispose();
        crushingMinigame.dispose();
        crushingSelectionUI.dispose();
        mixerUI.dispose();
        furnaceMinigame.dispose();
        furnaceSelectionUI.dispose();
        fishingMinigame.dispose();
        storageCrateUI.dispose();
        filterPipeUI.dispose();
        itemFilterPipeUI.dispose();
        devBarrelUI.dispose();
        solderingMinigame.dispose();
        solderingSelectionUI.dispose();
        player.dispose();
        if (penguinDude != null) penguinDude.dispose();
        if (pooDude != null) pooDude.dispose();
        itemPipeUI.dispose();
        minimap.dispose();
        planterUI.dispose();
        crusherUI.dispose();
        smelterUI.dispose();
        distilleryUI.dispose();
        goblinoHutUI.dispose();
        oreDrillUI.dispose();
        animalStatsUI.dispose();
        hunger.dispose();
        if (CREATIVE_MODE && devConsole != null) devConsole.dispose();
    }
}
