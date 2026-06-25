package com.factory.game.Items;


public class DropEntry {
    public final Item  item;
    public final int   minQty;
    public final int   maxQty;
    public final float chance;

    public DropEntry(Item item, int minQty, int maxQty, float chance) {
        this.item   = item;
        this.minQty = minQty;
        this.maxQty = maxQty;
        this.chance = Math.max(0f, Math.min(1f, chance));
    }

    public DropEntry(Item item, int qty) {
        this(item, qty, qty, 1.0f);
    }

    public DropEntry(Item item, int qty, float chance) {
        this(item, qty, qty, chance);
    }

    public boolean isGuaranteed() { return chance >= 1.0f; }
    public boolean isRandom()     { return minQty != maxQty; }
}