package com.factory.game.World;

import com.factory.game.Items.Item;
import java.util.EnumMap;
import java.util.Map;

public final class PlaceableRegistry {

    private static final Map<Item, PlaceableDefinition> REGISTRY =
        new EnumMap<>(Item.class);

    static {
        REGISTRY.put(
            Item.WALL,
            new PlaceableDefinition(PlacedObject.Type.WOOD_WALL, 1)
        );

        REGISTRY.put(
            Item.STONE_WALL,
            new PlaceableDefinition(PlacedObject.Type.STONE_WALL, 1)
        );

        REGISTRY.put(
            Item.LANTERN,
            new PlaceableDefinition(PlacedObject.Type.LANTERN, 1)
        );

        REGISTRY.put(
            Item.CAMPFIRE,
            new PlaceableDefinition(PlacedObject.Type.CAMPFIRE, 1)
        );

        REGISTRY.put(
            Item.CRUSHING_POT,
            new PlaceableDefinition(PlacedObject.Type.CRUSHING_POT, 1)
        );

        REGISTRY.put(
            Item.GRASS,
            new PlaceableDefinition(PlacedObject.Type.GRASS, 1)
        );

        REGISTRY.put(
            Item.SEASHELL,
            new PlaceableDefinition(PlacedObject.Type.SEASHELL, 1)
        );

        REGISTRY.put(
            Item.WELL,
            new PlaceableDefinition(PlacedObject.Type.WELL, 1)
        );

        REGISTRY.put(
            Item.BARREL,
            new PlaceableDefinition(PlacedObject.Type.BARREL, 1)
        );

        REGISTRY.put(
            Item.DEVBARREL,
            new PlaceableDefinition(PlacedObject.Type.DEVBARREL, 1)
        );

        REGISTRY.put(
            Item.BASIC_PIPE,
            new PlaceableDefinition(PlacedObject.Type.BASIC_PIPE, 1)
        );

        REGISTRY.put(
            Item.FILTER_PIPE,
            new PlaceableDefinition(PlacedObject.Type.FILTER_PIPE, 1)
        );

        REGISTRY.put(
            Item.FURNACE,
            new PlaceableDefinition(PlacedObject.Type.FURNACE, 1)
        );

        REGISTRY.put(
            Item.CAST,
            new PlaceableDefinition(PlacedObject.Type.CAST, 1)
        );

        REGISTRY.put(
            Item.STORAGE_CRATE,
            new PlaceableDefinition(PlacedObject.Type.STORAGE_CRATE, 1)
        );

        REGISTRY.put(
            Item.PENGUIN_PLUSH,
            new PlaceableDefinition(PlacedObject.Type.PENGUIN_PLUSH, 1)
        );

        REGISTRY.put(
            Item.POO_PET,
            new PlaceableDefinition(PlacedObject.Type.POO_PET, 1)
        );

        REGISTRY.put(
            Item.SOLDERING_TABLE,
            new PlaceableDefinition(PlacedObject.Type.SOLDERING_TABLE, 1)
        );

        REGISTRY.put(
            Item.FISHBOWL,
            new PlaceableDefinition(PlacedObject.Type.FISHBOWL, 1)
        );

        REGISTRY.put(
            Item.MIXER,
            new PlaceableDefinition(PlacedObject.Type.MIXER, 1)
        );

        REGISTRY.put(
            Item.PLANTER,
            new PlaceableDefinition(PlacedObject.Type.PLANTER, 1)
        );

        REGISTRY.put(
            Item.ITEM_PIPE,
            new PlaceableDefinition(PlacedObject.Type.ITEM_PIPE, 1)
        );

        REGISTRY.put(
            Item.CRUSHER,
            new PlaceableDefinition(PlacedObject.Type.CRUSHER, 1)
        );

        REGISTRY.put(
            Item.WOOD_FLOOR,
            new PlaceableDefinition(PlacedObject.Type.WOOD_FLOOR, 1)
        );

        REGISTRY.put(
            Item.STONE_FLOOR,
            new PlaceableDefinition(PlacedObject.Type.STONE_FLOOR, 1)
        );

        REGISTRY.put(
            Item.DRILL,
            new PlaceableDefinition(PlacedObject.Type.DRILL, 1)
        );

        REGISTRY.put(
            Item.TANK,
            new PlaceableDefinition(PlacedObject.Type.TANK, 1)
        );

        REGISTRY.put(
            Item.SMELTER,
            new PlaceableDefinition(PlacedObject.Type.SMELTER, 1)
        );

        REGISTRY.put(
            Item.CHUNK_LOADER,
            new PlaceableDefinition(PlacedObject.Type.CHUNK_LOADER, 1)
        );

        REGISTRY.put(
            Item.WOOD_WALL,
            new PlaceableDefinition(PlacedObject.Type.WOOD_WALL_FULL, 1)
        );

        REGISTRY.put(
            Item.STONE_WALL_FULL,
            new PlaceableDefinition(PlacedObject.Type.STONE_WALL_FULL, 1)
        );

        REGISTRY.put(
            Item.TRASH,
            new PlaceableDefinition(PlacedObject.Type.TRASH, 1)
        );

        REGISTRY.put(
            Item.DISTILLERY,
            new PlaceableDefinition(PlacedObject.Type.DISTILLERY, 1)
        );

        REGISTRY.put(
            Item.GLOBE,
            new PlaceableDefinition(PlacedObject.Type.GLOBE, 1)
        );

        REGISTRY.put(
            Item.BOOK_SHELF,
            new PlaceableDefinition(PlacedObject.Type.BOOK_SHELF, 1)
        );

        REGISTRY.put(
            Item.CHAIR,
            new PlaceableDefinition(PlacedObject.Type.CHAIR, 1)
        );

        REGISTRY.put(
            Item.SIGN,
            new PlaceableDefinition(PlacedObject.Type.SIGN, 1)
        );

        REGISTRY.put(
            Item.TABLE,
            new PlaceableDefinition(PlacedObject.Type.TABLE, 1)
        );

        REGISTRY.put(
            Item.DECO_BARREL,
            new PlaceableDefinition(PlacedObject.Type.BARREL_DECO, 1)
        );

        REGISTRY.put(
            Item.WOOD_PLANKS,
            new PlaceableDefinition(PlacedObject.Type.WOOD_PLANKS, 1)
        );

        REGISTRY.put(
            Item.TOILET,
            new PlaceableDefinition(PlacedObject.Type.TOILET, 1)
        );

        REGISTRY.put(
            Item.LAMP_POST,
            new PlaceableDefinition(PlacedObject.Type.LAMP_POST, 1)
        );

        REGISTRY.put(
            Item.ORE_DRILL,
            new PlaceableDefinition(PlacedObject.Type.ORE_DRILL, 1)
        );

        REGISTRY.put(
            Item.FILTER_ITEM_PIPE,
            new PlaceableDefinition(PlacedObject.Type.FILTER_ITEM_PIPE, 1)
        );

        REGISTRY.put(
            Item.STOVE,
            new PlaceableDefinition(PlacedObject.Type.STOVE, 1)
        );
    }

    public static PlaceableDefinition get(Item item) {
        return REGISTRY.get(item);
    }

    public static boolean isPlaceable(Item item) {
        return REGISTRY.containsKey(item);
    }

    private PlaceableRegistry() {}
}
