package com.factory.game.Items;

import java.util.HashMap;
import java.util.Map;

public final class ItemDamage {

    private static final Map<Item, Integer> itemDamage = new HashMap<>();

    static {
        itemDamage.put(Item.WOOD_AXE, 1);
        itemDamage.put(Item.STONE_AXE, 2);
        itemDamage.put(Item.WOOD_PICKAXE, 1);
        itemDamage.put(Item.STONE_PICKAXE, 2);
    }

    private ItemDamage() {}

    public static int getDamage(Item item) {
        return itemDamage.getOrDefault(item, 0);
    }

    public static boolean containsKey(Item item) {
        return itemDamage.containsKey(item);
    }
}
