package com.factory.game.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlanterRecipe {

    public static final float FERTILIZER_SPEED_MULT = 1.5f;

    public static final class Drop {
        public final Item item;
        public final int  weight;
        public final int  minQty;
        public final int  maxQty;

        public Drop(Item item, int weight, int minQty, int maxQty) {
            this.item    = item;
            this.weight  = weight;
            this.minQty  = minQty;
            this.maxQty  = maxQty;
        }
    }

    private final String     name;
    private final Item       seedItem;
    private final float      growDuration;
    private final float      waterPerSecond;
    private final List<Drop> drops = new ArrayList<>();
    private String growSheetPath = null;

    public PlanterRecipe(String name, Item seedItem,
                         float growDuration, float waterPerSecond) {
        this.name           = name;
        this.seedItem       = seedItem;
        this.growDuration   = growDuration;
        this.waterPerSecond = waterPerSecond;
    }

    public PlanterRecipe addDrop(Item item, int weight, int minQty, int maxQty) {
        drops.add(new Drop(item, weight, minQty, maxQty));
        return this;
    }

    public PlanterRecipe addDrop(Item item, int weight, int qty) {
        return addDrop(item, weight, qty, qty);
    }

    public PlanterRecipe setGrowSheet(String path) {
        this.growSheetPath = path;
        return this;
    }

    public String getGrowSheetPath() { return growSheetPath; }

    public ItemStack rollDrop(Random rng) {
        if (drops.isEmpty()) return null;

        int total = 0;
        for (Drop d : drops) total += d.weight;

        int roll = rng.nextInt(total);
        int acc  = 0;
        for (Drop d : drops) {
            acc += d.weight;
            if (roll < acc) {
                int qty = (d.minQty == d.maxQty)
                        ? d.minQty
                        : d.minQty + rng.nextInt(d.maxQty - d.minQty + 1);
                return new ItemStack(d.item, qty);
            }
        }
        Drop last = drops.get(drops.size() - 1);
        return new ItemStack(last.item, last.minQty);
    }

    public String     getName()          { return name;           }
    public Item       getSeedItem()      { return seedItem;       }
    public float      getGrowDuration()  { return growDuration;   }
    public float      getWaterPerSecond(){ return waterPerSecond; }
    public List<Drop> getDrops()         { return drops;          }
}
