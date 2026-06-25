package com.factory.game.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DropTable {

    private final List<DropEntry> entries = new ArrayList<>();


    public DropTable addDrop(Item item, int minQty, int maxQty, float chance) {
        entries.add(new DropEntry(item, minQty, maxQty, chance));
        return this;
    }

    public DropTable addDrop(Item item, int qty) {
        entries.add(new DropEntry(item, qty));
        return this;
    }

    public DropTable addDrop(Item item, int qty, float chance) {
        entries.add(new DropEntry(item, qty, chance));
        return this;
    }


    public List<ItemStack> roll(Random rng) {
        if (entries.isEmpty()) return Collections.emptyList();

        List<ItemStack> drops = new ArrayList<>();
        for (DropEntry entry : entries) {
            if (rng.nextFloat() >= entry.chance) continue;         

            int qty = (entry.minQty == entry.maxQty)
                    ? entry.minQty
                    : entry.minQty + rng.nextInt(entry.maxQty - entry.minQty + 1);

            if (qty > 0) drops.add(new ItemStack(entry.item, qty));
        }
        return drops;
    }

    public boolean isEmpty()              { return entries.isEmpty(); }
    public List<DropEntry> getEntries()   { return Collections.unmodifiableList(entries); }
}