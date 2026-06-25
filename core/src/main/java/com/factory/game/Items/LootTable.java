package com.factory.game.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootTable {
    private final List<LootEntry> entries;
    private final int rolls;
    private final Random random = new Random();

    public LootTable(List<LootEntry> entries, int rolls) {
        this.entries = entries;
        this.rolls = rolls;
    }

    public List<Item> generate() {
        List<Item> result = new ArrayList<>();
        int totalWeight = entries.stream().mapToInt(LootEntry::getWeight).sum();

        for (int i = 0; i < rolls; i++) {
            int roll = random.nextInt(totalWeight);
            int cumulative = 0;
            for (LootEntry entry : entries) {
                cumulative += entry.getWeight();
                if (roll < cumulative) {
                    int count = entry.getMinCount() + random.nextInt(entry.getMaxCount() - entry.getMinCount() + 1);
                    for (int c = 0; c < count; c++) result.add(entry.getItem());
                    break;
                }
            }
        }
        return result;
    }
}
