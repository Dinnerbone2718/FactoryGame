package com.factory.game.Items;

public class LootEntry {
    private final Item item;
    private final int weight;
    private final int minCount;
    private final int maxCount;

    public LootEntry(Item item, int weight, int minCount, int maxCount) {
        this.item = item;
        this.weight = weight;
        this.minCount = minCount;
        this.maxCount = maxCount;
    }


    public int getWeight() {
        return weight;
    }

    public Item getItem() {
        return item;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }
   
}