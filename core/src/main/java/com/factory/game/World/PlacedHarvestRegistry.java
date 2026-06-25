package com.factory.game.World;

import com.factory.game.Items.DropTable;
import com.factory.game.Items.Item;
import java.util.EnumMap;
import java.util.Map;

public final class PlacedHarvestRegistry {

    private static final Map<PlacedObject.Type, HarvestDefinition> REGISTRY =
        new EnumMap<>(PlacedObject.Type.class);

    static {
        REGISTRY.put(
            PlacedObject.Type.WOOD_WALL,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.WALL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.STONE_WALL,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.STONE_WALL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.CAMPFIRE,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.WOOD, 1, 3, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.LANTERN,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.LANTERN, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.CRUSHING_POT,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.CRUSHING_POT, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.GRASS,
            new HarvestDefinition(
                null,
                1,
                new DropTable().addDrop(Item.GRASS, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.SEASHELL,
            new HarvestDefinition(
                null,
                1,
                new DropTable().addDrop(Item.SEASHELL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.WELL,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.WELL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.BARREL,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.BARREL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.DEVBARREL,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.DEVBARREL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.BASIC_PIPE,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.BASIC_PIPE, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.FURNACE,
            new HarvestDefinition(
                null,
                5,
                new DropTable().addDrop(Item.FURNACE, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.CAST,
            new HarvestDefinition(
                null,
                5,
                new DropTable().addDrop(Item.CAST, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.STORAGE_CRATE,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.STORAGE_CRATE, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.PENGUIN_PLUSH,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.PENGUIN_PLUSH, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.POO_PET,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.POO_PET, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.FILTER_PIPE,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.FILTER_PIPE, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.SOLDERING_TABLE,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.SOLDERING_TABLE, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.MIXER,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.MIXER, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.FISHBOWL,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.FISHBOWL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.ITEM_PIPE,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.ITEM_PIPE, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.PLANTER,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.PLANTER, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.CRUSHER,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.CRUSHER, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.WOOD_FLOOR,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.WOOD_FLOOR, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.STONE_FLOOR,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.STONE_FLOOR, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.DRILL,
            new HarvestDefinition(
                null,
                5,
                new DropTable().addDrop(Item.DRILL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.TANK,
            new HarvestDefinition(
                null,
                5,
                new DropTable().addDrop(Item.TANK, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.SMELTER,
            new HarvestDefinition(
                null,
                5,
                new DropTable().addDrop(Item.SMELTER, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.WOOD_WALL_FULL,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.WOOD_WALL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.STONE_WALL_FULL,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.STONE_WALL_FULL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.CHUNK_LOADER,
            new HarvestDefinition(
                null,
                5,
                new DropTable().addDrop(Item.CHUNK_LOADER, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.TRASH,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.TRASH, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.DISTILLERY,
            new HarvestDefinition(
                null,
                5,
                new DropTable().addDrop(Item.DISTILLERY, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.GLOBE,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.GLOBE, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.BOOK_SHELF,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.BOOK_SHELF, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.CHAIR,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.CHAIR, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.SIGN,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.SIGN, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.TABLE,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.TABLE, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.BARREL_DECO,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.DECO_BARREL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.WOOD_PLANKS,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.WOOD_PLANKS, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.TOILET,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.TOILET, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.LAMP_POST,
            new HarvestDefinition(
                null,
                2,
                new DropTable().addDrop(Item.LAMP_POST, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.ORE_DRILL,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.ORE_DRILL, 1, 1, 1.0f)
            )
        );

        REGISTRY.put(
            PlacedObject.Type.FILTER_ITEM_PIPE,
            new HarvestDefinition(
                null,
                3,
                new DropTable().addDrop(Item.FILTER_ITEM_PIPE, 1, 1, 1.0f)
            )
        );
    }

    public static HarvestDefinition get(PlacedObject.Type type) {
        return REGISTRY.get(type);
    }

    public static boolean isHarvestable(PlacedObject.Type type) {
        return REGISTRY.containsKey(type);
    }

    private PlacedHarvestRegistry() {}
}
