package com.factory.game.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootTableGenerator {
    private static final Map<String, LootTable> tables = new HashMap<>();

    static {
        tables.put("grass_crate",    buildGrassCrate());
        tables.put("forest_crate",   buildForestCrate());
        tables.put("desert_crate",   buildDesertCrate());
        tables.put("mountain_crate", buildMountainCrate());
    }

    public static LootTable getTable(String name) {
        return tables.get(name);
    }

    public static ItemStack[] generateContents(String tableName) {
        LootTable table = tables.get(tableName);
        if (table == null) return new ItemStack[9];

        List<Item> loot = table.generate();
        ItemStack[] slots = new ItemStack[9];

        for (Item item : loot) {
            boolean merged = false;
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != null && slots[i].getItem() == item && !slots[i].isFull()) {
                    slots[i].addQuantity(1);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] == null) {
                        slots[i] = new ItemStack(item, 1);
                        break;
                    }
                }
            }
        }
        return slots;
    }


    private static LootTable buildGrassCrate() {
        List<LootEntry> entries = List.of(
            new LootEntry(Item.STICK,        90, 4, 12),
            new LootEntry(Item.STONE,        80, 3, 10),
            new LootEntry(Item.COAL,         60, 2,  6),
            new LootEntry(Item.BONE,         50, 1,  4),
            new LootEntry(Item.SCRAP,        50, 2,  6),
            new LootEntry(Item.GRASS,        70, 3,  8),
            new LootEntry(Item.LEAF,         60, 3,  8),
            new LootEntry(Item.WOOD,         40, 2,  6),
            new LootEntry(Item.LANTERN,      15, 1,  1),
            new LootEntry(Item.SIFT,         10, 1,  1),
            new LootEntry(Item.WOOD_AXE,      5, 1,  1),
            new LootEntry(Item.PENGUIN_PLUSH, 2, 1,  1)
        );
        return new LootTable(entries, 6);
    }

    private static LootTable buildForestCrate() {
        List<LootEntry> entries = List.of(
            new LootEntry(Item.WOOD,          90, 6, 18),
            new LootEntry(Item.STICK,         85, 4, 12),
            new LootEntry(Item.LEAF,          80, 5, 15),
            new LootEntry(Item.COAL,          55, 2,  7),
            new LootEntry(Item.CRUSHED_LEAF,  45, 2,  6),
            new LootEntry(Item.BONE,          40, 1,  4),
            new LootEntry(Item.GRASS,         60, 3, 10),
            new LootEntry(Item.REFINED_WOOD,  20, 1,  3),
            new LootEntry(Item.LANTERN,       25, 1,  2),
            new LootEntry(Item.WOOD_AXE,      15, 1,  1),
            new LootEntry(Item.GOBLINO_COIN, 15, 1,  1),
            new LootEntry(Item.WOOD_PICKAXE,  10, 1,  1),
            new LootEntry(Item.FISHING_ROD,    8, 1,  1),
            new LootEntry(Item.CAMPFIRE,       5, 1,  1)
        );
        return new LootTable(entries, 6);
    }

    private static LootTable buildDesertCrate() {
        List<LootEntry> entries = List.of(
            new LootEntry(Item.SAND,          90, 6, 18),
            new LootEntry(Item.SEASHELL,      80, 3,  9),
            new LootEntry(Item.BONE,          65, 2,  6),
            new LootEntry(Item.CLAY,          55, 2,  7),
            new LootEntry(Item.SCRAP,         50, 2,  6),
            new LootEntry(Item.COAL,          35, 1,  4),
            new LootEntry(Item.STONE,         40, 2,  6),
            new LootEntry(Item.FISHBOWL,      20, 1,  2),
            new LootEntry(Item.FISH,          25, 1,  3),
            new LootEntry(Item.SIFT,          18, 1,  1),
            new LootEntry(Item.GOBLINO_COIN, 15, 1,  1),
            new LootEntry(Item.LANTERN,       12, 1,  1),
            new LootEntry(Item.WOOD_AXE,       5, 1,  1),
            new LootEntry(Item.PENGUIN_PLUSH,  2, 1,  1)
        );
        return new LootTable(entries, 6);
    }

    private static LootTable buildMountainCrate() {
        List<LootEntry> entries = List.of(
            new LootEntry(Item.STONE,         90, 6, 18),
            new LootEntry(Item.RAW_ORE,       75, 2,  8),
            new LootEntry(Item.COAL,          70, 3,  9),
            new LootEntry(Item.SCRAP,         55, 2,  7),
            new LootEntry(Item.BONE,          45, 1,  4),
            new LootEntry(Item.RAW_OREMINUM,  25, 1,  3),
            new LootEntry(Item.RAW_ROCKITE,   20, 1,  3),
            new LootEntry(Item.STICK,         40, 2,  6),
            new LootEntry(Item.LANTERN,       30, 1,  2),
            new LootEntry(Item.STONE_PICKAXE, 18, 1,  1),
            new LootEntry(Item.GOBLINO_COIN, 15, 1,  1),
            new LootEntry(Item.STONE_AXE,     15, 1,  1),
            new LootEntry(Item.WOOD_PICKAXE,   8, 1,  1)
        );
        return new LootTable(entries, 7);
    }
}