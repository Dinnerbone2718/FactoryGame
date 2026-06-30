package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.factory.game.Items.CookingMinigame;
import com.factory.game.Items.CookingRecipe;
import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.CrushingMinigame;
import com.factory.game.Items.CrushingSelectionUI;
import com.factory.game.Items.DroppedItem;
import com.factory.game.Items.FishingMinigame;
import com.factory.game.Items.FishingRecipe;
import com.factory.game.Items.FoodManager;
import com.factory.game.Items.FurnaceSelectionUI;
import com.factory.game.Items.InventoryUI;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemClass;
import com.factory.game.Items.ItemStack;
import com.factory.game.Items.SiftMinigame;
import com.factory.game.Items.SiftSelectionUI;
import com.factory.game.Items.SolderingMinigame;
import com.factory.game.Items.SolderingSelectionUI;
import com.factory.game.Items.StorageCrateUI;
import com.factory.game.Renderer.WorldRenderer;
import com.factory.game.World.AnimalStatsUI;
import com.factory.game.World.Chunk;
import com.factory.game.World.ChunkLoaderUI;
import com.factory.game.World.CrusherUI;
import com.factory.game.World.DevBarrelUI;
import com.factory.game.World.DistilleryUI;
import com.factory.game.World.FilterPipeUI;
import com.factory.game.World.GoblinoHutUI;
import com.factory.game.World.Ground_Tile;
import com.factory.game.World.HarvestDefinition;
import com.factory.game.World.HarvestResult;
import com.factory.game.World.ItemFilterPipeUI;
import com.factory.game.World.ItemPipeConfig;
import com.factory.game.World.ItemPipeUI;
import com.factory.game.World.LiquidType;
import com.factory.game.World.MixerUI;
import com.factory.game.World.ObjectSpriteCache;
import com.factory.game.World.PlaceableDefinition;
import com.factory.game.World.PlaceableRegistry;
import com.factory.game.World.PlacedHarvestRegistry;
import com.factory.game.World.PlacedObject;
import com.factory.game.World.PlacedObjectCache;
import com.factory.game.World.PlanterUI;
import com.factory.game.World.SmelterUI;
import com.factory.game.World.WorldObject;
import java.util.List;
import java.util.Random;

public class InteractionHandler {

    public static final float HARVEST_RANGE = 3.5f * Main.TILE_SCALE;
    private static final float HARVEST_COOLDOWN = 0.45f;
    private static final float PLACE_RANGE = 3.5f * Main.TILE_SCALE;
    private static final float WRONG_TOOL_MESSAGE_DURATION = 2.0f;

    private final Player player;
    private final Camera camera;
    private final WorldRenderer renderer;
    private final InventoryUI inventoryUI;
    private final SiftMinigame siftMinigame;
    private final SiftSelectionUI siftSelectionUI;
    private final CrushingMinigame crushingMinigame;
    private final CrushingSelectionUI crushingSelectionUI;
    private final FurnaceSelectionUI furnaceSelectionUI;
    private final MixerUI mixerUI;
    private final FishingMinigame fishingMinigame;
    private final CookingMinigame cookingMinigame;
    private final StorageCrateUI storageCrateUI;
    private final FilterPipeUI filterPipeUI;
    private final ItemFilterPipeUI itemFilterPipeUI;
    private final DevBarrelUI devBarrelUI;
    private final SolderingMinigame solderingMinigame;
    private final SolderingSelectionUI solderingSelectionUI;
    private final WorldManager world;
    private final Texture ePromptTexture;
    private final Texture tPromptTexture;
    private final ItemPipeUI itemPipeUI;
    private final PlanterUI planterUI;
    private final CrusherUI crusherUI;
    private final SmelterUI smelterUI;
    private final DistilleryUI distilleryUI;
    private final Minimap minimap;
    private final ChunkLoaderUI chunkLoaderUI;
    private final GoblinoHutUI goblinoHutUI;
    private final AnimalStatsUI animalStatsUI;
    private final Hunger hunger;

    private final Random harvestRng = new Random();
    private float harvestCooldownTimer = 0f;

    private WorldObject activeHarvestTarget = null;
    private PlacedObject activePlacedHarvestTarget = null;

    private String wrongToolMessage = null;
    private float wrongToolMessageTime = 0f;

    public InteractionHandler(
        Player player,
        Camera camera,
        WorldRenderer renderer,
        InventoryUI inventoryUI,
        SiftMinigame siftMinigame,
        SiftSelectionUI siftSelectionUI,
        CrushingMinigame crushingMinigame,
        CrushingSelectionUI crushingSelectionUI,
        FurnaceSelectionUI furnaceSelectionUI,
        FishingMinigame fishingMinigame,
        CookingMinigame cookingMinigame,
        StorageCrateUI storageCrateUI,
        FilterPipeUI filterPipeUI,
        ItemFilterPipeUI itemFilterPipeUI,
        DevBarrelUI devBarrelUI,
        SolderingMinigame solderingMinigame,
        SolderingSelectionUI solderingSelectionUI,
        MixerUI mixerUI,
        WorldManager worldManager,
        Texture ePromptTexture,
        Texture tPromptTexture,
        ItemPipeUI itemPipeUI,
        PlanterUI planterUI,
        CrusherUI crusherUI,
        SmelterUI smelterUI,
        DistilleryUI distilleryUI,
        Minimap minimap,
        ChunkLoaderUI chunkLoaderUI,
        GoblinoHutUI goblinoHutUI,
        AnimalStatsUI animalStatsUI,
        Hunger hunger
    ) {
        this.player = player;
        this.camera = camera;
        this.renderer = renderer;
        this.inventoryUI = inventoryUI;
        this.siftMinigame = siftMinigame;
        this.siftSelectionUI = siftSelectionUI;
        this.crushingMinigame = crushingMinigame;
        this.crushingSelectionUI = crushingSelectionUI;
        this.furnaceSelectionUI = furnaceSelectionUI;
        this.mixerUI = mixerUI;
        this.fishingMinigame = fishingMinigame;
        this.cookingMinigame = cookingMinigame;
        this.storageCrateUI = storageCrateUI;
        this.filterPipeUI = filterPipeUI;
        this.itemFilterPipeUI = itemFilterPipeUI;
        this.devBarrelUI = devBarrelUI;
        this.solderingMinigame = solderingMinigame;
        this.solderingSelectionUI = solderingSelectionUI;
        this.world = worldManager;
        this.ePromptTexture = ePromptTexture;
        this.tPromptTexture = tPromptTexture;
        this.itemPipeUI = itemPipeUI;
        this.planterUI = planterUI;
        this.crusherUI = crusherUI;
        this.smelterUI = smelterUI;
        this.distilleryUI = distilleryUI;
        this.minimap = minimap;
        this.chunkLoaderUI = chunkLoaderUI;
        this.goblinoHutUI = goblinoHutUI;
        this.animalStatsUI = animalStatsUI;
        this.hunger = hunger;
    }

    public void handleInput() {
        if (
            siftMinigame.isActive() ||
            crushingMinigame.isActive() ||
            fishingMinigame.isActive() ||
            cookingMinigame.isActive() ||
            solderingMinigame.isActive()
        ) return;

        if (animalStatsUI.isVisible()) {
            animalStatsUI.handleInput();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            if (siftSelectionUI.isVisible()) {
                siftSelectionUI.close();
                return;
            }

            if (player.isOnRaft()) {
                if (world.isNearShore(player.getWorldX(), player.getWorldY())) {
                    float[] shore = world.findNearestShorePosition(
                        player.getWorldX(),
                        player.getWorldY()
                    );
                    if (shore != null) {
                        player.setOnRaft(false);
                        player.setPosition(shore[0], shore[1]);
                    }
                }
                return;
            }

            if (
                isHoldingAnimalViewer() &&
                !inventoryUI.isVisible() &&
                !animalStatsUI.isVisible()
            ) {
                animalStatsUI.toggle();
                return;
            }

            if (
                !inventoryUI.isVisible() &&
                isHoldingRaft() &&
                world.getNearbyWaterTile() != null
            ) {
                Ground_Tile water = world.getNearbyWaterTile();
                float waterWorldX = water.getX() * Main.TILE_SCALE;
                float waterWorldY = water.getY() * Main.TILE_SCALE;
                float dirX = waterWorldX - player.getWorldX();
                float dirY = waterWorldY - player.getWorldY();
                float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                if (len > 0) {
                    dirX /= len;
                    dirY /= len;
                }
                player.setOnRaft(true);
                player.setPosition(
                    waterWorldX + dirX * Main.TILE_SCALE * 2f,
                    waterWorldY + dirY * Main.TILE_SCALE * 2f
                );
                return;
            }

            if (!inventoryUI.isVisible() && isHoldingSift()) {
                siftSelectionUI.open();
                return;
            }

            if (
                !inventoryUI.isVisible() &&
                isHoldingFishingRod() &&
                world.getNearbyWaterTile() != null
            ) {
                Item rod = getEquippedItem();
                if (rod != null) {
                    FishingRecipe recipe = CraftingManager.getFishingRecipeFor(
                        rod
                    );
                    if (recipe != null) {
                        fishingMinigame.start(recipe);
                        return;
                    }
                }
            }

            if (!inventoryUI.isVisible() && isHoldingFlashlight()) {
                if (player.isFlashlightOn()) {
                    player.setFlashlightOff();
                } else {
                    player.setFlashlightOn(renderer.getLightRenderer(), camera);
                }
                return;
            }

            if (!inventoryUI.isVisible() && isHoldingBubbleWand()) {
                world.spawnBubble(player);
                return;
            }

            if (!inventoryUI.isVisible() && isHoldingFood()) {
                eatSelectedFood();
                return;
            }

            if (
                world.getNearbyItemPipe() != null &&
                isHoldingWrench() &&
                !inventoryUI.isVisible()
            ) {
                PlacedObject pipe = world.getNearbyItemPipe();
                String key = pipe.getX() + "," + pipe.getY();
                ItemPipeConfig cfg = world
                    .getItemPipeConfigs()
                    .computeIfAbsent(key, k -> new ItemPipeConfig());
                itemPipeUI.open(pipe, cfg, key, world.getItemPipeNetwork());
                return;
            }
        }

        if (crusherUI.isVisible()) {
            crusherUI.handleInput();
            return;
        }

        if (distilleryUI.isVisible()) {
            return;
        }

        if (chunkLoaderUI.isVisible()) {
            chunkLoaderUI.handleInput();
            return;
        }

        if (smelterUI.isVisible()) {
            smelterUI.handleInput();
            return;
        }

        if (siftSelectionUI.isVisible()) return;

        if (mixerUI.isVisible()) {
            return;
        }
        if (itemPipeUI.isVisible()) {
            itemPipeUI.handleInput();
            return;
        }

        if (planterUI.isVisible()) {
            planterUI.handleInput();
            return;
        }

        if (crushingSelectionUI.isVisible()) return;
        if (furnaceSelectionUI.isVisible()) return;
        if (solderingSelectionUI.isVisible()) return;
        if (storageCrateUI.isVisible()) {
            if (
                Gdx.input.isKeyJustPressed(Input.Keys.E)
            ) storageCrateUI.close();
            return;
        }
        if (itemFilterPipeUI.isVisible()) {
            return;
        }
        if (filterPipeUI.isVisible()) {
            return;
        }
        if (devBarrelUI.isVisible()) {
            return;
        }

        if (goblinoHutUI.isVisible()) {
            goblinoHutUI.handleInput();
            return;
        }

        if (player.isOnRaft()) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (world.getNearbyCaveObject() != null) {
                world.handleCaveTransition(player);
                return;
            }

            if (world.getNearbyPlanter() != null && !inventoryUI.isVisible()) {
                planterUI.open(world.getNearbyPlanter(), player.getInventory());
                return;
            }

            if (
                world.getNearbyDistillery() != null && !inventoryUI.isVisible()
            ) {
                distilleryUI.open(world.getNearbyDistillery());
                return;
            }

            if (world.getNearbyCrushingPot() != null) {
                crushingSelectionUI.open();
                return;
            }
            if (world.getNearbyFurnace() != null) {
                furnaceSelectionUI.open();
                return;
            }
            if (world.getNearbyStove() != null && !inventoryUI.isVisible()) {
                List<CookingRecipe> available =
                    CraftingManager.getAvailableCookRecipes(
                        player.getInventory()
                    );
                if (!available.isEmpty()) {
                    CookingRecipe recipe = available.get(0);
                    player.getInventory().removeItem(recipe.getInputItem(), 1);
                    cookingMinigame.start(recipe);
                }
                return;
            }
            if (world.getNearbyStorageCrate() != null) {
                PlacedObject crate = world.getNearbyStorageCrate();
                storageCrateUI.open(
                    world.getOrCreateCrateInventory(crate.getX(), crate.getY())
                );
                return;
            }
            if (
                world.getNearbyFilterPipe() != null &&
                isHoldingFilterPipe() &&
                !inventoryUI.isVisible()
            ) {
                filterPipeUI.open(world.getNearbyFilterPipe());
                return;
            }
            if (
                world.getNearbyFilterItemPipe() != null &&
                isHoldingFilterItemPipe() &&
                !inventoryUI.isVisible()
            ) {
                itemFilterPipeUI.open(world.getNearbyFilterItemPipe());
                return;
            }
            if (
                world.getNearbyDevBarrel() != null && !inventoryUI.isVisible()
            ) {
                devBarrelUI.open(world.getNearbyDevBarrel());
                return;
            }
            if (world.getNearbySolderingTable() != null) {
                solderingSelectionUI.open();
                return;
            }

            if (world.getNearbyMixer() != null && !inventoryUI.isVisible()) {
                mixerUI.open(world.getNearbyMixer());
                return;
            }

            if (world.getNearbyPenguin() != null && !inventoryUI.isVisible()) {
                world.penguinDudeEnabled = !world.penguinDudeEnabled;
                return;
            }

            if (world.getNearbyPooGuy() != null && !inventoryUI.isVisible()) {
                world.pooDudeEnabled = !world.pooDudeEnabled;
                return;
            }

            if (world.getNearbyCrusher() != null && !inventoryUI.isVisible()) {
                crusherUI.open(world.getNearbyCrusher(), player.getInventory());
                return;
            }

            if (world.getNearbySmelter() != null && !inventoryUI.isVisible()) {
                smelterUI.open(world.getNearbySmelter(), player.getInventory());
                return;
            }

            if (world.getNearbyGlobe() != null && !inventoryUI.isVisible()) {
                minimap.setFullscreen(true);
                return;
            }

            if (
                world.getNearbyChunkLoader() != null && !inventoryUI.isVisible()
            ) {
                chunkLoaderUI.open(world.getNearbyChunkLoader());
                return;
            }

            if (world.getNearbyGoblino() != null && !inventoryUI.isVisible()) {
                goblinoHutUI.open(
                    world.getNearbyGoblino(),
                    player.getInventory()
                );
                return;
            }

            inventoryUI.toggle();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            dropSelectedItem();
        }
    }

    public void pollCookingResult() {
        CookingMinigame.CookResult result = cookingMinigame.pollResult();
        if (result != null && result.success) {
            player.getInventory().addItem(result.item, result.qty);
        }
    }

    public void tickChunkLoaderUI() {
        if (!chunkLoaderUI.isDirty()) return;
        chunkLoaderUI.clearDirty();

        PlacedObject cl = chunkLoaderUI.getLoader();
        if (cl == null) return;
    }

    public void tickDevBarrelUI() {
        if (!devBarrelUI.isDirty()) return;
        devBarrelUI.clearDirty();

        PlacedObject barrel = devBarrelUI.getDevBarrel();
        if (barrel == null) return;

        LiquidType selected = barrel.getSelectedProducedLiquid();
        int ordinal = (selected != null) ? selected.ordinal() : -1;
        world
            .activeDelta()
            .updateDevBarrelLiquid(barrel.getX(), barrel.getY(), ordinal);
        world.saveDeltasAsync();
    }

    public void handleHarvestInput(float delta) {
        harvestCooldownTimer = Math.max(0f, harvestCooldownTimer - delta);
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) return;
        if (harvestCooldownTimer > 0f) return;

        int mouseScreenX = Gdx.input.getX();
        int mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float worldMouseX = mouseScreenX - camera.cameraX;
        float worldMouseY = mouseScreenY - camera.cameraY;

        int tileX = (int) Math.floor(worldMouseX / Main.TILE_SCALE);
        int tileY = (int) Math.floor(worldMouseY / Main.TILE_SCALE);

        float playerCX = player.getWorldX() + Main.TILE_SCALE * 0.5f;
        float playerCY = player.getWorldY() + Main.TILE_SCALE * 0.5f;
        float objCX = tileX * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float objCY = tileY * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float dx = playerCX - objCX;
        float dy = playerCY - objCY;
        if (dx * dx + dy * dy > HARVEST_RANGE * HARVEST_RANGE) return;

        ItemClass toolClass = resolveEquippedToolClass();

        PlacedObject placed = world
            .getPlacedObjectLookup()
            .get(tileX + "," + tileY);

        if (placed == null && isHoldingPipe()) {
            placed = world.getPipeLookup().get(tileX + "," + tileY);
        }

        if (placed == null) {
            placed = world.getFloorLookup().get(tileX + "," + tileY);
        }

        if (placed != null) {
            if (
                activePlacedHarvestTarget != null &&
                activePlacedHarvestTarget != placed
            ) {
                activePlacedHarvestTarget.resetHits();
            }
            activePlacedHarvestTarget = placed;

            HarvestResult result = placed.tryHit(toolClass);
            switch (result) {
                case NOT_HARVESTABLE:
                    break;
                case WRONG_TOOL: {
                    HarvestDefinition pDef = PlacedHarvestRegistry.get(
                        placed.type
                    );
                    if (pDef != null && pDef.getRequiredToolClass() != null) {
                        wrongToolMessage =
                            "Requires a " +
                            pDef.getRequiredToolClass().name().toLowerCase() +
                            "!";
                        wrongToolMessageTime = WRONG_TOOL_MESSAGE_DURATION;
                    }
                    break;
                }
                case HIT_REGISTERED:
                    harvestCooldownTimer = HARVEST_COOLDOWN;
                    break;
                case DESTROYED:
                    harvestCooldownTimer = HARVEST_COOLDOWN;
                    activePlacedHarvestTarget = null;
                    destroyPlacedObject(placed.getX(), placed.getY(), placed);
                    break;
            }
            return;
        }

        WorldObject obj = world.getWorldObjectLookup().get(tileX + "," + tileY);
        if (obj == null) return;

        if (activeHarvestTarget != null && activeHarvestTarget != obj) {
            activeHarvestTarget.resetHits();
        }
        activeHarvestTarget = obj;

        HarvestResult result = obj.tryHit(toolClass);
        switch (result) {
            case NOT_HARVESTABLE:
                break;
            case WRONG_TOOL: {
                com.factory.game.World.HarvestDefinition def =
                    com.factory.game.World.HarvestRegistry.get(obj.type);
                if (def != null && def.getRequiredToolClass() != null) {
                    wrongToolMessage =
                        "Requires a " +
                        def.getRequiredToolClass().name().toLowerCase() +
                        "!";
                    wrongToolMessageTime = WRONG_TOOL_MESSAGE_DURATION;
                }
                break;
            }
            case HIT_REGISTERED:
                harvestCooldownTimer = HARVEST_COOLDOWN;
                break;
            case DESTROYED:
                harvestCooldownTimer = HARVEST_COOLDOWN;
                activeHarvestTarget = null;
                destroyWorldObject(tileX, tileY, obj);
                break;
        }

        if (activeHarvestTarget != null) {
            String key =
                activeHarvestTarget.getX() + "," + activeHarvestTarget.getY();
            if (!world.getWorldObjectLookup().containsKey(key)) {
                activeHarvestTarget = null;
            }
        }
    }

    private void destroyPlacedObject(int tileX, int tileY, PlacedObject obj) {
        world.activeDelta().removePlaced(tileX, tileY);
        world.saveDeltasAsync();

        if (
            obj.type == PlacedObject.Type.STORAGE_CRATE ||
            obj.type == PlacedObject.Type.FOREST_CRATE ||
            obj.type == PlacedObject.Type.DESERT_CRATE ||
            obj.type == PlacedObject.Type.MOUNTAIN_CRATE
        ) {
            world.removeCrateInventory(tileX, tileY);
        }

        if (obj.type == PlacedObject.Type.PLANTER) {
            world.getPlanterManager().onPlanterRemoved(obj);
        }

        if (obj.type == PlacedObject.Type.CRUSHER) {
            world.getCrusherManager().onCrusherRemoved(obj);
        }

        if (obj.type == PlacedObject.Type.SMELTER) {
            world.getSmelterManager().onSmelterRemoved(obj);
        }

        spawnDrops(obj.generateDrops(harvestRng), tileX, tileY);

        int chunkX = Math.floorDiv(tileX, Main.CHUNK_SIZE);
        int chunkY = Math.floorDiv(tileY, Main.CHUNK_SIZE);
        Chunk chunk = world.getLoadedChunks().get(Chunk.key(chunkX, chunkY));
        if (chunk != null) chunk.removePlacedObject(
            obj,
            renderer.getLightRenderer()
        );

        if (obj.isPipe()) {
            world.getPipeLookup().remove(tileX + "," + tileY);
            if (obj.type == PlacedObject.Type.ITEM_PIPE) {
                world.onItemPipeRemoved(tileX, tileY);
            }
        } else if (obj.isFloor()) {
            world.getFloorLookup().remove(tileX + "," + tileY);
            world.getPlacedObjectLookup().remove((tileX) + "," + (tileY));
        } else {
            int tw = PlacedObjectCache.getTileWidth(obj.type);
            int th = PlacedObjectCache.getTileHeight(obj.type);
            for (int dx2 = 0; dx2 < tw; dx2++) {
                for (int dy2 = 0; dy2 < th; dy2++) {
                    world
                        .getPlacedObjectLookup()
                        .remove((tileX + dx2) + "," + (tileY + dy2));
                }
            }
        }

        World b2World = renderer.getLightRenderer().getWorld();
        obj.destroyShadowBody(b2World);

        float[] hb = world.computePlacedHitbox(obj);
        world.unregisterHitbox(hb[0], hb[1], hb[2] - hb[0], hb[3] - hb[1]);

        world.reupdatePlacedObjectAndNeighbours(tileX, tileY);
    }

    private void destroyWorldObject(int tileX, int tileY, WorldObject obj) {
        world.activeDelta().markRemoved(tileX, tileY);
        world.saveDeltasAsync();

        spawnDrops(obj.generateDrops(harvestRng), tileX, tileY);

        World b2World = renderer.getLightRenderer().getWorld();
        obj.destroyShadowBody(b2World);
        obj.detachLight(renderer.getLightRenderer());

        int chunkX = Math.floorDiv(tileX, Main.CHUNK_SIZE);
        int chunkY = Math.floorDiv(tileY, Main.CHUNK_SIZE);
        Chunk chunk = world.getLoadedChunks().get(Chunk.key(chunkX, chunkY));
        if (chunk != null) chunk.removeObject(obj);

        world.removeFromObjectLookup(tileX, tileY, obj);

        ObjectSpriteCache.SpriteConfig cfg = ObjectSpriteCache.getConfig(
            obj.type
        );
        if (cfg != null && cfg.hasHitbox()) {
            float scale = Main.TILE_SCALE / 16f;
            float hx = tileX * Main.TILE_SCALE + cfg.hitboxOffX * scale;
            float hy = tileY * Main.TILE_SCALE + cfg.hitboxOffY * scale;
            float hw = cfg.hitboxW * scale;
            float hh = cfg.hitboxH * scale;
            world.unregisterHitbox(hx, hy, hw, hh);
        }
    }

    private void spawnDrops(List<ItemStack> drops, int tileX, int tileY) {
        float dropX = tileX * Main.TILE_SCALE + Main.TILE_SCALE / 2f;
        float dropY = tileY * Main.TILE_SCALE + Main.TILE_SCALE / 2f;
        for (ItemStack drop : drops) {
            world
                .getDroppedItems()
                .add(DroppedItem.spawnBurst(drop, dropX, dropY));
        }
    }

    public void handlePlacementInput() {
        if (isHoldingWrench()) return;
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) return;

        int selectedSlot = inventoryUI.getSelectedSlot();
        if (selectedSlot < 0) return;

        ItemStack equipped = player.getInventory().getSlot(selectedSlot);
        if (equipped == null) return;

        PlaceableDefinition def = PlaceableRegistry.get(equipped.getItem());
        if (def == null) return;

        int mouseScreenX = Gdx.input.getX();
        int mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float worldMouseX = mouseScreenX - camera.cameraX;
        float worldMouseY = mouseScreenY - camera.cameraY;

        int tileX = (int) Math.floor(worldMouseX / Main.TILE_SCALE);
        int tileY = (int) Math.floor(worldMouseY / Main.TILE_SCALE);

        float playerCX = player.getWorldX() + Main.TILE_SCALE * 0.5f;
        float playerCY = player.getWorldY() + Main.TILE_SCALE * 0.5f;
        float tileCX = tileX * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float tileCY = tileY * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float dx = playerCX - tileCX;
        float dy = playerCY - tileCY;
        if (dx * dx + dy * dy > PLACE_RANGE * PLACE_RANGE) return;

        boolean placingPipe =
            def.getPlacedType() == PlacedObject.Type.BASIC_PIPE ||
            def.getPlacedType() == PlacedObject.Type.FILTER_PIPE ||
            def.getPlacedType() == PlacedObject.Type.ITEM_PIPE ||
            def.getPlacedType() == PlacedObject.Type.FILTER_ITEM_PIPE;

        boolean placingFloor =
            def.getPlacedType() == PlacedObject.Type.WOOD_FLOOR ||
            def.getPlacedType() == PlacedObject.Type.STONE_FLOOR ||
            def.getPlacedType() == PlacedObject.Type.WOOD_PLANKS;

        int tw = PlacedObjectCache.getTileWidth(def.getPlacedType());
        int th = PlacedObjectCache.getTileHeight(def.getPlacedType());

        for (int dx2 = 0; dx2 < tw; dx2++) {
            for (int dy2 = 0; dy2 < th; dy2++) {
                String occupiedKey = (tileX + dx2) + "," + (tileY + dy2);

                Ground_Tile occupiedTile = world
                    .getWorldTileLookup()
                    .get(occupiedKey);
                if (
                    occupiedTile == null ||
                    !def.canPlaceOn(occupiedTile.tileName)
                ) return;

                if (placingPipe) {
                    if (world.getPipeLookup().containsKey(occupiedKey)) return;
                    if (
                        world.getWorldObjectLookup().containsKey(occupiedKey)
                    ) return;
                } else if (placingFloor) {
                    if (world.getFloorLookup().containsKey(occupiedKey)) return;
                    if (
                        world.getPlacedObjectLookup().containsKey(occupiedKey)
                    ) return;
                    if (
                        world.getWorldObjectLookup().containsKey(occupiedKey)
                    ) return;
                } else {
                    if (
                        world.getPlacedObjectLookup().containsKey(occupiedKey)
                    ) return;
                    if (
                        world.getWorldObjectLookup().containsKey(occupiedKey)
                    ) return;
                }
            }
        }

        if (
            !player
                .getInventory()
                .removeItem(equipped.getItem(), def.getItemCost())
        ) return;

        int chunkX = Math.floorDiv(tileX, Main.CHUNK_SIZE);
        int chunkY = Math.floorDiv(tileY, Main.CHUNK_SIZE);
        Chunk chunk = world.getLoadedChunks().get(Chunk.key(chunkX, chunkY));
        if (chunk == null) {
            player
                .getInventory()
                .addItem(equipped.getItem(), def.getItemCost());
            return;
        }

        PlacedObject placed = new PlacedObject(
            def.getPlacedType(),
            tileX,
            tileY
        );
        chunk.addPlacedObject(placed, renderer.getLightRenderer());

        world.activeDelta().addPlaced(tileX, tileY, placed.type);
        world.saveDeltasAsync();

        if (placingPipe) {
            world.getPipeLookup().put(tileX + "," + tileY, placed);
        } else if (placingFloor) {
            world.getFloorLookup().put(tileX + "," + tileY, placed);
        } else {
            for (int dx2 = 0; dx2 < tw; dx2++) {
                for (int dy2 = 0; dy2 < th; dy2++) {
                    world
                        .getPlacedObjectLookup()
                        .put((tileX + dx2) + "," + (tileY + dy2), placed);
                }
            }
        }

        placed.attachShadowBody(renderer.getLightRenderer().getWorld());

        if (placed.isSolid()) {
            float[] hb = world.computePlacedHitbox(placed);
            world.unregisterHitbox(hb[0], hb[1], hb[2] - hb[0], hb[3] - hb[1]);
            world
                .getObjectHitboxes()
                .put(
                    tileX + "," + tileY,
                    new float[] { hb[0], hb[1], hb[2], hb[3] }
                );
        }

        if (placingPipe) {
            world.reupdatePipeAndNeighbours(tileX, tileY);
        } else {
            world.reupdatePlacedObjectAndNeighbours(tileX, tileY);
        }
    }

    private void eatSelectedFood() {
        int selectedSlot = inventoryUI.getSelectedSlot();
        if (selectedSlot < 0) return;

        ItemStack stack = player.getInventory().getSlot(selectedSlot);
        if (stack == null) return;

        boolean ate = hunger.eat(stack.getItem());
        if (!ate) return;

        player.getInventory().removeItem(stack.getItem(), 1);

        if (player.getInventory().getSlot(selectedSlot) == null) {
            inventoryUI.clearSelection();
        }
    }

    private void dropSelectedItem() {
        int selectedSlot = inventoryUI.getSelectedSlot();
        if (selectedSlot < 0) return;

        ItemStack stack = player.getInventory().getSlot(selectedSlot);
        if (stack == null) return;

        int dropAmount = Math.min(1, stack.getQuantity());
        player.getInventory().removeItem(stack.getItem(), dropAmount);

        int mouseScreenX = Gdx.input.getX();
        int mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float worldMouseX = mouseScreenX - camera.cameraX;
        float worldMouseY = mouseScreenY - camera.cameraY;
        float playerCX = player.getWorldX() + Main.TILE_SCALE / 2f;
        float playerCY = player.getWorldY() + Main.TILE_SCALE / 2f;

        world
            .getDroppedItems()
            .add(
                DroppedItem.spawnThrow(
                    new ItemStack(stack.getItem(), dropAmount),
                    playerCX,
                    playerCY,
                    worldMouseX - playerCX,
                    worldMouseY - playerCY
                )
            );

        if (player.getInventory().getSlot(selectedSlot) == null) {
            inventoryUI.clearSelection();
        }
    }

    private ItemClass resolveEquippedToolClass() {
        int selectedSlot = inventoryUI.getSelectedSlot();
        if (selectedSlot >= 0) {
            ItemStack equipped = player.getInventory().getSlot(selectedSlot);
            if (equipped != null && equipped.getItem().getToolClass() != null) {
                return equipped.getItem().getToolClass();
            }
        }
        return ItemClass.HAND;
    }

    private Item getEquippedItem() {
        int selectedSlot = inventoryUI.getSelectedSlot();
        if (selectedSlot < 0) return null;
        ItemStack stack = player.getInventory().getSlot(selectedSlot);
        return stack != null ? stack.getItem() : null;
    }

    public boolean isHoldingSift() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.SIFT;
    }

    public boolean isHoldingWrench() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.WRENCH_AND_SCREW;
    }

    public boolean isHoldingPipe() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return (
            stack != null &&
            (stack.getItem() == Item.BASIC_PIPE ||
                stack.getItem() == Item.FILTER_PIPE ||
                stack.getItem() == Item.ITEM_PIPE ||
                stack.getItem() == Item.FILTER_ITEM_PIPE)
        );
    }

    public boolean isHoldingFilterPipe() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.FILTER_PIPE;
    }

    public boolean isHoldingFilterItemPipe() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.FILTER_ITEM_PIPE;
    }

    public boolean isHoldingFishingRod() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return (
            stack != null &&
            stack.getItem().getToolClass() == ItemClass.FISHING_ROD
        );
    }

    public boolean isHoldingRaft() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.RAFT;
    }

    public boolean isHoldingFlashlight() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.FLASHLIGHT;
    }

    public boolean isHoldingAnimalViewer() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.ANINMAL_PHONE;
    }

    public boolean isHoldingFood() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        if (stack == null) return false;
        FoodManager foodManager = CraftingManager.getFoodManagerFor(
            stack.getItem()
        );
        return foodManager != null && foodManager.getIsFood();
    }

    public void drawEPrompt(SpriteBatch batch, float totalTime) {
        WorldObject nearby = world.getNearbyCaveObject();
        if (nearby == null) return;

        float worldPixelX =
            nearby.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 1.0f;
        float worldPixelY = nearby.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawCrushingEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject pot = world.getNearbyCrushingPot();
        if (pot == null) return;

        float worldPixelX =
            pot.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = pot.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 0.82f, 0.40f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawFurnaceEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject furnace = world.getNearbyFurnace();
        if (furnace == null) return;

        float worldPixelX =
            furnace.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = furnace.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 0.52f, 0.08f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawStoveEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject stove = world.getNearbyStove();
        if (stove == null) return;

        float worldPixelX =
            stove.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = stove.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 0.65f, 0.25f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawSiftTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawWrenchTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawFishingTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.40f, 0.80f, 1f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawStorageCrateEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject crate = world.getNearbyStorageCrate();
        if (crate == null) return;

        float worldPixelX =
            crate.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = crate.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.55f, 0.85f, 1f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawFilterPipeEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject pipe = world.getNearbyFilterPipe();
        if (pipe == null) return;

        float worldPixelX =
            pipe.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = pipe.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.25f, 0.90f, 0.75f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawFilterItemPipeEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject pipe = world.getNearbyFilterItemPipe();
        if (pipe == null) return;

        float worldPixelX =
            pipe.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = pipe.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.25f, 0.90f, 0.75f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawDevBarrelEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject barrel = world.getNearbyDevBarrel();
        if (barrel == null) return;

        float worldPixelX =
            barrel.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = barrel.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 0.50f, 0.10f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawSolderingEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject table = world.getNearbySolderingTable();
        if (table == null) return;

        float worldPixelX =
            table.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = table.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.30f, 0.90f, 1.00f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawRaftMountTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.30f, 0.75f, 1f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawRaftDismountTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 0.85f, 0.25f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawDistilleryEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject distillery = world.getNearbyDistillery();
        if (distillery == null) return;

        float worldPixelX =
            distillery.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY =
            distillery.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.55f, 0.25f, 1.00f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawMixerEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject mixer = world.getNearbyMixer();
        if (mixer == null) return;

        float worldPixelX =
            mixer.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = mixer.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.40f, 1.00f, 0.65f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawCrusherEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject mixer = world.getNearbyCrusher();
        if (mixer == null) return;

        float worldPixelX =
            mixer.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = mixer.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.40f, 1.00f, 0.65f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawSmelterEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject smelter = world.getNearbySmelter();
        if (smelter == null) return;
        float worldPixelX =
            smelter.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = smelter.getY() * Main.TILE_SCALE + Main.TILE_SCALE;
        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;
        batch.setColor(1f, 0.52f, 0.08f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawPenguinEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject mixer = world.getNearbyPenguin();
        if (mixer == null) return;

        float worldPixelX =
            mixer.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = mixer.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.40f, 1.00f, 0.65f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawPooGuyEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject mixer = world.getNearbyPooGuy();
        if (mixer == null) return;

        float worldPixelX =
            mixer.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = mixer.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.40f, 1.00f, 0.65f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawFlashlightTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawEatTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawChunkLoaderEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject cl = world.getNearbyChunkLoader();
        if (cl == null) return;

        float worldPixelX =
            cl.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = cl.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.30f, 0.90f, 1.00f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void tickItemPipeUI() {
        if (!itemPipeUI.isDirty()) return;
        itemPipeUI.clearDirty();
        PlacedObject pipe = itemPipeUI.getPipe();
        if (pipe == null) return;
        ItemPipeConfig cfg = itemPipeUI.getConfig();
        world
            .activeDelta()
            .updateItemPipeConfig(pipe.getX(), pipe.getY(), cfg.encode());
        world.saveDeltasAsync();
    }

    public void drawItemPipeWrenchPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject pipe = world.getNearbyItemPipe();
        if (pipe == null) return;
        float worldPixelX =
            pipe.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float worldPixelY = pipe.getY() * Main.TILE_SCALE + Main.TILE_SCALE;
        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;
        batch.setColor(1f, 0.85f, 0.25f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public String getWrongToolMessage() {
        return wrongToolMessage;
    }

    public float getWrongToolMessageTime() {
        return wrongToolMessageTime;
    }

    public void handleWrenchClick() {
        if (!isHoldingWrench()) return;
        if (itemPipeUI.isVisible() || inventoryUI.isVisible()) return;
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) return;

        int mouseScreenX = Gdx.input.getX();
        int mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float worldMouseX = mouseScreenX - camera.cameraX;
        float worldMouseY = mouseScreenY - camera.cameraY;

        int tileX = (int) Math.floor(worldMouseX / Main.TILE_SCALE);
        int tileY = (int) Math.floor(worldMouseY / Main.TILE_SCALE);

        PlacedObject po = world.getPipeLookup().get(tileX + "," + tileY);
        if (
            po == null ||
            (po.type != PlacedObject.Type.ITEM_PIPE &&
                po.type != PlacedObject.Type.FILTER_ITEM_PIPE)
        ) return;

        float localX =
            (worldMouseX - tileX * Main.TILE_SCALE) / Main.TILE_SCALE;
        float localY =
            (worldMouseY - tileY * Main.TILE_SCALE) / Main.TILE_SCALE;

        float dNorth = 1f - localY;
        float dSouth = localY;
        float dEast = 1f - localX;
        float dWest = localX;

        float min = Math.min(Math.min(dNorth, dSouth), Math.min(dEast, dWest));

        String key = tileX + "," + tileY;
        ItemPipeConfig cfg = world
            .getItemPipeConfigs()
            .computeIfAbsent(key, k -> new ItemPipeConfig());

        if (min == dNorth) cfg.cycleNorth();
        else if (min == dSouth) cfg.cycleSouth();
        else if (min == dEast) cfg.cycleEast();
        else cfg.cycleWest();

        world.activeDelta().updateItemPipeConfig(tileX, tileY, cfg.encode());
        world.saveDeltasAsync();
    }

    public void drawPlanterEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject p = world.getNearbyPlanter();
        if (p == null) return;

        int planterTileW =
            com.factory.game.World.PlacedObjectCache.getTileWidth(
                PlacedObject.Type.PLANTER
            );
        float worldPixelX =
            p.getX() * Main.TILE_SCALE + Main.TILE_SCALE * planterTileW * 0.5f;
        float worldPixelY = p.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.35f, 0.90f, 0.40f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawGlobeEPrompt(SpriteBatch batch, float totalTime) {
        PlacedObject p = world.getNearbyGlobe();
        if (p == null) return;

        int globeTileW = com.factory.game.World.PlacedObjectCache.getTileWidth(
            PlacedObject.Type.GLOBE
        );
        float worldPixelX =
            p.getX() * Main.TILE_SCALE + Main.TILE_SCALE * globeTileW * 0.5f;
        float worldPixelY = p.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.35f, 0.90f, 0.40f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawGoblinoEPrompt(SpriteBatch batch, float totalTime) {
        WorldObject p = world.getNearbyGoblino();
        if (p == null) return;

        int goblinoTileW = 2;
        float worldPixelX =
            p.getX() * Main.TILE_SCALE + Main.TILE_SCALE * goblinoTileW * 0.5f;
        float worldPixelY = p.getY() * Main.TILE_SCALE + Main.TILE_SCALE;

        float screenX = worldPixelX + camera.cameraX;
        float screenY = worldPixelY + camera.cameraY;
        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.35f, 0.90f, 0.40f, 0.92f);
        batch.draw(
            ePromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawAnimalViewerTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.40f, 0.80f, 1f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public boolean isHoldingBubbleWand() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.BUBBLE_WAND;
    }

    public void drawBubbleWandTPrompt(SpriteBatch batch, float totalTime) {
        float screenX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float screenY = player.getWorldY() + camera.cameraY + Main.TILE_SCALE;

        float iconSize = 24f;
        float bob = (float) Math.sin(totalTime * 3.5) * 4f;

        batch.setColor(0.40f, 0.80f, 1f, 0.92f);
        batch.draw(
            tPromptTexture,
            screenX - iconSize * 0.5f,
            screenY + 6f + bob,
            iconSize,
            iconSize
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
