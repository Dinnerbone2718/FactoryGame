package com.factory.game.Items;

public class GoblinoRecipe {
    private final Item   outputItem;
    private final int    cost;
    private final String name;
    private final int    outputQuantity;

    public GoblinoRecipe(String name, Item outputItem, int cost, int outputQuantity) {
        this.name           = name;
        this.cost           = cost;
        this.outputItem     = outputItem;
        this.outputQuantity = outputQuantity;
    }

    public Item   getOutputItem()     { return outputItem;     }
    public int    getCost()           { return cost;           }
    public String getName()           { return name;           }
    public int    getOutputQuantity() { return outputQuantity; } // ← added

    public boolean canCraft(Inventory inventory) {
        return inventory.hasItem(Item.GOBLINO_COIN, cost);
    }

    public boolean craft(Inventory inventory) {
        if (!canCraft(inventory)) return false;
        if (!inventory.removeItem(Item.GOBLINO_COIN, cost)) return false;
        return inventory.addItem(outputItem, outputQuantity);
    }

    @Override
    public String toString() {
        return name + ": Cost: " + cost + " → " + outputItem.getDisplayName() + " x" + outputQuantity;
    }
}