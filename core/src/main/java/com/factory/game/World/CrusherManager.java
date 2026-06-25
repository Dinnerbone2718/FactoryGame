package com.factory.game.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.CrushingRecipe;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;

public final class CrusherManager {

    public static final int INPUT_SLOT        = 0;
    public static final int OUTPUT_SLOT       = 1;
    public static final int CRUSHER_INV_SLOTS = 2;

    private static final float PROCESS_DURATION = 2.0f;
    private static final int   MAX_STACK        = 64;

    private static final class CrusherState {
        float   timer      = 0f;
        boolean processing = false;
    }

    private final Map<String, CrusherState> states      = new HashMap<>();
    private final Map<String, Inventory>    inventories = new HashMap<>();

    public void update(float delta, Collection<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            for (PlacedObject obj : chunk.getPlacedObjects()) {
                if (obj.type != PlacedObject.Type.CRUSHER) continue;
                tickCrusher(delta, obj);
            }
        }
    }

    private void tickCrusher(float delta, PlacedObject crusher) {
        String       key   = keyOf(crusher);
        CrusherState state = getOrCreate(key);
        Inventory    inv   = getOrCreateInventory(crusher);

        ItemStack inputStack = inv.getSlot(INPUT_SLOT);
        if (inputStack == null || inputStack.getQuantity() <= 0) {
            state.processing = false;
            state.timer      = 0f;
            return;
        }

        CrushingRecipe recipe = CraftingManager.getCrushRecipeFor(inputStack.getItem());
        if (recipe == null) {
            state.processing = false;
            state.timer      = 0f;
            return;
        }

        ItemStack outputStack = inv.getSlot(OUTPUT_SLOT);
        if (outputStack != null && outputStack.getQuantity() > 0) {
            if (outputStack.getItem() != recipe.getOutputItem()) {
                state.processing = false;
                state.timer      = 0f;
                return;
            }
            if (outputStack.getQuantity() + recipe.getOutputQuantity() > MAX_STACK) {
                state.processing = false;
                state.timer      = 0f;
                return;
            }
        }

        state.processing = true;
        state.timer += delta;

        if (state.timer >= PROCESS_DURATION) {
            state.timer = 0f;

            int inputQty = inputStack.getQuantity() - 1;
            inv.setSlot(INPUT_SLOT, inputQty <= 0 ? null : new ItemStack(inputStack.getItem(), inputQty));

            if (outputStack != null && outputStack.getQuantity() > 0
                    && outputStack.getItem() == recipe.getOutputItem()) {
                inv.setSlot(OUTPUT_SLOT, new ItemStack(
                        recipe.getOutputItem(),
                        outputStack.getQuantity() + recipe.getOutputQuantity()));
            } else {
                inv.setSlot(OUTPUT_SLOT, new ItemStack(
                        recipe.getOutputItem(), recipe.getOutputQuantity()));
            }
        }
    }

    public Inventory getOrCreateInventory(PlacedObject crusher) {
        return inventories.computeIfAbsent(keyOf(crusher),
                k -> new Inventory(CRUSHER_INV_SLOTS));
    }

    public Inventory getInventory(PlacedObject crusher) {
        return inventories.get(keyOf(crusher));
    }

    public float getProgress(PlacedObject crusher) {
        CrusherState state = states.get(keyOf(crusher));
        if (state == null || !state.processing) return 0f;
        return Math.min(1f, state.timer / PROCESS_DURATION);
    }

    public boolean isProcessing(PlacedObject crusher) {
        CrusherState state = states.get(keyOf(crusher));
        return state != null && state.processing;
    }

    public void onCrusherRemoved(PlacedObject crusher) {
        String key = keyOf(crusher);
        states.remove(key);
        inventories.remove(key);
    }

    public ItemStack pullFromOutput(PlacedObject crusher) {
        Inventory inv = inventories.get(keyOf(crusher));
        if (inv == null) return null;
        ItemStack s = inv.getSlot(OUTPUT_SLOT);
        if (s == null || s.getQuantity() <= 0) return null;
        Item item = s.getItem();
        int  qty  = s.getQuantity() - 1;
        inv.setSlot(OUTPUT_SLOT, qty <= 0 ? null : new ItemStack(item, qty));
        return new ItemStack(item, 1);
    }

    public boolean pushToInput(PlacedObject crusher, Item item) {
        Inventory inv = getOrCreateInventory(crusher);
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

    private CrusherState getOrCreate(String key) {
        return states.computeIfAbsent(key, k -> new CrusherState());
    }

    public Map<String, Inventory> getInventories() {
        return inventories;
    }

    private static String keyOf(PlacedObject obj) {
        return obj.getX() + "," + obj.getY();
    }
}