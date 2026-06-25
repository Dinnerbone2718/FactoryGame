package com.factory.game.Items;

import java.util.HashMap;
import java.util.Map;

public class CraftingRecipe {
    private final Map<Item, Integer> inputs;
    private final Item outputItem;
    private final int outputQuantity;
    private final String name;

    public CraftingRecipe(String name, Item outputItem, int outputQuantity) {
        this.name = name;
        this.outputItem = outputItem;
        this.outputQuantity = outputQuantity;
        this.inputs = new HashMap<>();
    }

    public CraftingRecipe addInput(Item item, int quantity) {
        inputs.put(item, quantity);
        return this;
    }

    public boolean canCraft(Inventory inventory) {
        for (Map.Entry<Item, Integer> entry : inputs.entrySet()) {
            if (!inventory.hasItem(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean craft(Inventory inventory) {
        if (!canCraft(inventory)) return false;

        for (Map.Entry<Item, Integer> entry : inputs.entrySet()) {
            if (!inventory.removeItem(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return inventory.addItem(outputItem, outputQuantity);
    }

    public Map<Item, Integer> getInputs() { return new HashMap<>(inputs); }
    public Item getOutputItem() { return outputItem; }
    public int getOutputQuantity() { return outputQuantity; }
    public String getName() { return name; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name + ": ");
        for (Map.Entry<Item, Integer> entry : inputs.entrySet()) {
            sb.append(entry.getKey().getDisplayName())
              .append(" x").append(entry.getValue())
              .append(" + ");
        }
        sb.delete(sb.length() - 3, sb.length());
        sb.append(" → ").append(outputItem.getDisplayName())
          .append(" x").append(outputQuantity);
        return sb.toString();
    }
}