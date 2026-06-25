package com.factory.game.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;
import com.factory.game.Items.SmeltingRecipe;

public final class SmelterManager {

    public static final int INPUT_SLOT        = 0;
    public static final int SMELTER_INV_SLOTS = 1;

    private static final float PROCESS_DURATION = 3.0f;
    private static final int   MAX_STACK        = 64;

    private static final class SmelterState {
        float   timer      = 0f;
        boolean processing = false;
    }

    private final Map<String, SmelterState> states      = new HashMap<>();
    private final Map<String, Inventory>    inventories = new HashMap<>();


    public void update(float delta, Collection<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            for (PlacedObject obj : chunk.getPlacedObjects()) {
                if (obj.type != PlacedObject.Type.SMELTER) continue;
                tickSmelter(delta, obj);
            }
        }
    }


    private void tickSmelter(float delta, PlacedObject smelter) {
        String       key   = keyOf(smelter);
        SmelterState state = getOrCreateState(key);
        Inventory    inv   = getOrCreateInventory(smelter);

        ItemStack inputStack = inv.getSlot(INPUT_SLOT);
        if (inputStack == null || inputStack.getQuantity() <= 0) {
            halt(state);
            return;
        }

        SmeltingRecipe recipe = CraftingManager.getSmeltRecipeFor(inputStack.getItem());
        if (recipe == null) {
            halt(state);
            return;
        }

        LiquidTank tank = smelter.getLiquidTank();
        if (tank == null) {
            halt(state);
            return;
        }

        if (!tank.isEmpty() && tank.getType() != recipe.getOutputLiquid()) {
            halt(state);
            return;
        }

        if (tank.getAmount() + recipe.getOutputAmount() > tank.getCapacity()) {
            halt(state);
            return;
        }

        state.processing = true;
        state.timer     += delta;

        if (state.timer >= PROCESS_DURATION) {
            state.timer = 0f;

            int remaining = inputStack.getQuantity() - 1;
            inv.setSlot(INPUT_SLOT,
                    remaining <= 0 ? null : new ItemStack(inputStack.getItem(), remaining));

            tank.deposit(recipe.getOutputLiquid(), recipe.getOutputAmount());
        }
    }

    private static void halt(SmelterState state) {
        state.processing = false;
        state.timer      = 0f;
    }


    public SmeltingRecipe getActiveRecipe(PlacedObject smelter) {
        Inventory inv = inventories.get(keyOf(smelter));
        if (inv == null) return null;
        ItemStack s = inv.getSlot(INPUT_SLOT);
        if (s == null || s.getQuantity() <= 0) return null;
        return CraftingManager.getSmeltRecipeFor(s.getItem());
    }

    public float getProgress(PlacedObject smelter) {
        SmelterState state = states.get(keyOf(smelter));
        if (state == null || !state.processing) return 0f;
        return Math.min(1f, state.timer / PROCESS_DURATION);
    }

    public boolean isProcessing(PlacedObject smelter) {
        SmelterState state = states.get(keyOf(smelter));
        return state != null && state.processing;
    }

    public Inventory getOrCreateInventory(PlacedObject smelter) {
        return inventories.computeIfAbsent(keyOf(smelter),
                k -> new Inventory(SMELTER_INV_SLOTS));
    }

    public Inventory getInventory(PlacedObject smelter) {
        return inventories.get(keyOf(smelter));
    }


    public boolean pushToInput(PlacedObject smelter, Item item) {
        if (CraftingManager.getSmeltRecipeFor(item) == null) return false;

        Inventory inv = getOrCreateInventory(smelter);
        ItemStack s   = inv.getSlot(INPUT_SLOT);
        if (s == null || s.getQuantity() == 0) {
            inv.setSlot(INPUT_SLOT, new ItemStack(item, 1));
            return true;
        }
        if (s.getItem() == item && s.getQuantity() < MAX_STACK) {
            inv.setSlot(INPUT_SLOT, new ItemStack(item, s.getQuantity() + 1));
            return true;
        }
        return false;
    }

    public void onSmelterRemoved(PlacedObject smelter) {
        String key = keyOf(smelter);
        states.remove(key);
        inventories.remove(key);
    }

    public Map<String, Inventory> getInventories() {
        return inventories;
    }


    private SmelterState getOrCreateState(String key) {
        return states.computeIfAbsent(key, k -> new SmelterState());
    }

    private static String keyOf(PlacedObject obj) {
        return obj.getX() + "," + obj.getY();
    }
}