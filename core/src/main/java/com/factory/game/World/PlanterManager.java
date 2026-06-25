package com.factory.game.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;
import com.factory.game.Items.PlanterRecipe;
import com.factory.game.Main;

public final class PlanterManager {

    public static final class HarvestedCrop {
        public final ItemStack crop;
        public final float     worldPixelX;
        public final float     worldPixelY;
        public final String    targetPipeKey;

        HarvestedCrop(ItemStack crop, float wx, float wy, String pipeKey) {
            this.crop          = crop;
            this.worldPixelX   = wx;
            this.worldPixelY   = wy;
            this.targetPipeKey = pipeKey;
        }
    }

    private static final int[][] DIRS = { {1,0},{-1,0},{0,1},{0,-1} };

    public static final int PLANTER_INV_SLOTS = 9;

    private final List<HarvestedCrop> pendingHarvests = new ArrayList<>();
    private final Random              rng             = new Random();

    private final Map<String, PlanterState> states = new HashMap<>();

    private final Map<String, Inventory> planterInventories = new HashMap<>();

    private static final class PlanterState {
        Item          plantedSeed  = null;
        PlanterRecipe recipe       = null;
        float         growTimer    = 0f;
        int           growFrame    = 0;
        float         lastWaterAmount      = 0f;
        float         lastWaterCapacity    = 1f;
        float         lastFertAmount       = 0f;
        float         lastFertCapacity     = 1f;
        boolean       hasWaterSource       = false;
        boolean       hasFertSource        = false;
    }

    public List<HarvestedCrop> pollHarvests() {
        List<HarvestedCrop> result = new ArrayList<>(pendingHarvests);
        pendingHarvests.clear();
        return result;
    }

    public void update(float delta,
                       Collection<Chunk> chunks,
                       Map<String, PlacedObject> machineLookup,
                       Map<String, PlacedObject> pipeLookup,
                       Map<String, ItemStack>    itemPipeBuffers,
                     Map<String, Inventory>    crateInventories) {

        for (Chunk chunk : chunks) {
            for (PlacedObject obj : chunk.getPlacedObjects()) {
                if (obj.type != PlacedObject.Type.PLANTER) continue;
                tickPlanter(delta, obj, pipeLookup, itemPipeBuffers, crateInventories);
            }
        }
    }

    public boolean plantSeed(PlacedObject planter, Item seedItem) {
        PlanterRecipe recipe = CraftingManager.getPlanterRecipeFor(seedItem);
        if (recipe == null) return false;

        String key = keyOf(planter);
        PlanterState state = getOrCreate(key);
        state.plantedSeed = seedItem;
        state.recipe      = recipe;
        state.growTimer   = 0f;
        state.growFrame   = 0;
        return true;
    }

    public void removeSeed(PlacedObject planter) {
        PlanterState state = states.get(keyOf(planter));
        if (state == null) return;
        state.plantedSeed = null;
        state.recipe      = null;
        state.growTimer   = 0f;
        state.growFrame   = 0;
    }

    public int getGrowFrame(PlacedObject planter) {
        PlanterState state = states.get(keyOf(planter));
        return (state != null) ? state.growFrame : 0;
    }

    public Item getPlantedSeed(PlacedObject planter) {
        PlanterState state = states.get(keyOf(planter));
        return (state != null) ? state.plantedSeed : null;
    }

    public float getGrowProgress(PlacedObject planter) {
        PlanterState state = states.get(keyOf(planter));
        if (state == null || state.recipe == null) return 0f;
        return Math.min(1f, state.growTimer / state.recipe.getGrowDuration());
    }

    public float getWaterRatio(PlacedObject planter) {
        PlanterState state = states.get(keyOf(planter));
        if (state == null || state.lastWaterCapacity <= 0f) return 0f;
        return Math.min(1f, state.lastWaterAmount / state.lastWaterCapacity);
    }

    public boolean hasWaterSource(PlacedObject planter) {
        PlanterState state = states.get(keyOf(planter));
        return state != null && state.hasWaterSource;
    }

    public float getFertilizerRatio(PlacedObject planter) {
        PlanterState state = states.get(keyOf(planter));
        if (state == null || state.lastFertCapacity <= 0f) return 0f;
        return Math.min(1f, state.lastFertAmount / state.lastFertCapacity);
    }

    public boolean hasFertilizerSource(PlacedObject planter) {
        PlanterState state = states.get(keyOf(planter));
        return state != null && state.hasFertSource;
    }

    public void onPlanterRemoved(PlacedObject planter) {
        String key = keyOf(planter);
        states.remove(key);
        planterInventories.remove(key);
    }

    public Inventory getOrCreatePlanterInventory(PlacedObject planter) {
        return planterInventories.computeIfAbsent(keyOf(planter),
                k -> new Inventory(PLANTER_INV_SLOTS));
    }

    public Inventory getPlanterInventory(PlacedObject planter) {
        return planterInventories.get(keyOf(planter));
    }

    public ItemStack pullFromInventory(PlacedObject planter) {
        Inventory inv = planterInventories.get(keyOf(planter));
        if (inv == null) return null;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack s = inv.getSlot(i);
            if (s != null && s.getQuantity() > 0) {
                inv.removeItem(s.getItem(), 1);
                return new ItemStack(s.getItem(), 1);
            }
        }
        return null;
    }

    private void tickPlanter(float delta,
                              PlacedObject planter,
                              Map<String, PlacedObject> pipeLookup,
                              Map<String, ItemStack>    itemPipeBuffers,
                              Map<String, Inventory>    crateInventories) {

        String       key   = keyOf(planter);
        PlanterState state = getOrCreate(key);

        if (state.recipe == null || state.plantedSeed == null) return;

        LiquidTank[] tanks = planter.getPlanterInputTanks();
        LiquidTank waterTank = (tanks != null && tanks.length > 0) ? tanks[0] : null;
        LiquidTank fertTank  = (tanks != null && tanks.length > 1) ? tanks[1] : null;

        if (waterTank != null) {
            state.lastWaterAmount   = waterTank.getAmount();
            state.lastWaterCapacity = waterTank.getCapacity();
            state.hasWaterSource    = waterTank.getAmount() > 0f;
        } else {
            state.lastWaterAmount   = 0f;
            state.lastWaterCapacity = 1f;
            state.hasWaterSource    = false;
        }

        if (fertTank != null) {
            state.lastFertAmount   = fertTank.getAmount();
            state.lastFertCapacity = fertTank.getCapacity();
            state.hasFertSource    = fertTank.getAmount() > 0f;
        } else {
            state.lastFertAmount   = 0f;
            state.lastFertCapacity = 1f;
            state.hasFertSource    = false;
        }

        float waterNeeded = state.recipe.getWaterPerSecond() * delta;
        float waterGot    = 0f;
        if (waterTank != null) {
            waterGot = waterTank.withdraw(Math.min(waterNeeded, waterTank.getAmount()));
        }
        if (waterGot < waterNeeded * 0.01f) {
            return;
        }

        float fertNeeded = state.recipe.getWaterPerSecond() * delta * 0.5f;
        float fertGot    = 0f;
        if (fertTank != null) {
            fertGot = fertTank.withdraw(Math.min(fertNeeded, fertTank.getAmount()));
        }
        boolean hasFertilizer = fertGot >= fertNeeded * 0.01f;

        float advance = delta * (hasFertilizer ? PlanterRecipe.FERTILIZER_SPEED_MULT : 1f);
        state.growTimer += advance;

        float duration = state.recipe.getGrowDuration();
        state.growFrame = Math.min(8, (int)(state.growTimer / duration * 9f));

        if (state.growTimer >= duration) {
            harvest(planter, state, pipeLookup, itemPipeBuffers, crateInventories);
            state.growTimer = 0f;
            state.growFrame = 0;
        }
    }

    private void harvest(PlacedObject planter,
                        PlanterState state,
                        Map<String, PlacedObject> pipeLookup,
                        Map<String, ItemStack>    itemPipeBuffers,
                        Map<String, Inventory>    crateInventories) {

        ItemStack drop = state.recipe.rollDrop(rng);
        if (drop == null) return;

        float wx = planter.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
        float wy = planter.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;

        int px = planter.getX();
        int py = planter.getY();
        int tw = PlacedObjectCache.getTileWidth(PlacedObject.Type.PLANTER);
        int th = PlacedObjectCache.getTileHeight(PlacedObject.Type.PLANTER);

        Inventory ownInv = getOrCreatePlanterInventory(planter);
        if (ownInv.addItem(drop.getItem(), drop.getQuantity())) {
            return;
        }

        for (int fx = px; fx < px + tw; fx++) {
            for (int fy = py; fy < py + th; fy++) {
                for (int[] d : DIRS) {
                    String nk = (fx + d[0]) + "," + (fy + d[1]);
                    PlacedObject pipe = pipeLookup.get(nk);
                    if (pipe == null || pipe.type != PlacedObject.Type.ITEM_PIPE) continue;
                    if (itemPipeBuffers.containsKey(nk)) continue;
                    pendingHarvests.add(new HarvestedCrop(drop, wx, wy, nk));
                    itemPipeBuffers.put(nk, drop);
                    return;
                }
            }
        }

        for (int fx = px; fx < px + tw; fx++) {
            for (int fy = py; fy < py + th; fy++) {
                for (int[] d : DIRS) {
                    String nk = (fx + d[0]) + "," + (fy + d[1]);
                    Inventory inv = crateInventories.get(nk);
                    if (inv != null && inv.addItem(drop.getItem(), drop.getQuantity())) {
                        return;
                    }
                }
            }
        }

        pendingHarvests.add(new HarvestedCrop(drop, wx, wy, null));
    }

    private PlanterState getOrCreate(String key) {
        return states.computeIfAbsent(key, k -> new PlanterState());
    }

    private static String keyOf(PlacedObject obj) {
        return obj.getX() + "," + obj.getY();
    }
}