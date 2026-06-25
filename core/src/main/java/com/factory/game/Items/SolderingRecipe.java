package com.factory.game.Items;

import java.util.LinkedHashMap;
import java.util.Map;

public class SolderingRecipe {

    private final String             name;
    private final Map<Item, Integer> inputs;
    private final Item               outputItem;
    private final int                outputQuantity;

    public SolderingRecipe(String name, Item outputItem, int outputQuantity) {
        this.name           = name;
        this.outputItem     = outputItem;
        this.outputQuantity = outputQuantity;
        this.inputs         = new LinkedHashMap<>();
    }

    public SolderingRecipe addInput(Item item, int quantity) {
        inputs.merge(item, quantity, Integer::sum);
        return this;
    }

    public boolean canCraft(Inventory inventory) {
        for (Map.Entry<Item, Integer> entry : inputs.entrySet()) {
            if (!inventory.hasItem(entry.getKey(), entry.getValue())) return false;
        }
        return true;
    }

    public boolean craft(Inventory inventory) {
        if (!canCraft(inventory)) return false;
        for (Map.Entry<Item, Integer> entry : inputs.entrySet()) {
            if (!inventory.removeItem(entry.getKey(), entry.getValue())) return false;
        }
        return inventory.addItem(outputItem, outputQuantity);
    }

    public String             getName()           { return name;           }
    public Map<Item, Integer> getInputs()         { return inputs;         }
    public Item               getOutputItem()     { return outputItem;     }
    public int                getOutputQuantity() { return outputQuantity; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name + ": ");
        for (Map.Entry<Item, Integer> e : inputs.entrySet()) {
            sb.append(e.getKey().getDisplayName()).append(" x").append(e.getValue()).append(" + ");
        }
        if (!inputs.isEmpty()) sb.delete(sb.length() - 3, sb.length());
        sb.append(" to ").append(outputItem.getDisplayName()).append(" x").append(outputQuantity);
        return sb.toString();
    }
}