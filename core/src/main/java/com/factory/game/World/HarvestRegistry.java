package com.factory.game.World;

import java.util.EnumMap;
import java.util.Map;

import com.factory.game.Items.DropTable;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemClass;


public final class HarvestRegistry {

    private static final Map<WorldObject.Type, HarvestDefinition> REGISTRY =
            new EnumMap<>(WorldObject.Type.class);

    static {

        REGISTRY.put(WorldObject.Type.TREE,
            new HarvestDefinition(ItemClass.AXE, 5,
                new DropTable()
                    .addDrop(Item.WOOD,  2, 5, 1.0f)
                    .addDrop(Item.LEAF, 1, 2, .5f)
                    .addDrop(Item.STICK, 1, 3, 0.5f)));

        REGISTRY.put(WorldObject.Type.PALMTREE,
            new HarvestDefinition(ItemClass.AXE, 6,
                new DropTable()
                    .addDrop(Item.WOOD,  2, 4, 1.0f)
                    .addDrop(Item.STICK, 1, 2, 0.3f)));


        REGISTRY.put(WorldObject.Type.ROCK,
            new HarvestDefinition(ItemClass.PICKAXE, 8,
                new DropTable()
                    .addDrop(Item.STONE, 3, 8, 1.0f)
                    .addDrop(Item.COAL,  1, 2, 0.3f)));


        REGISTRY.put(WorldObject.Type.RUBBLE,
            new HarvestDefinition(null, 3,
                new DropTable()
                    .addDrop(Item.STONE, 1, 3, 1.0f)));


        REGISTRY.put(WorldObject.Type.CAMPFIRE,
            new HarvestDefinition(null, 3,
                new DropTable()
                    .addDrop(Item.WOOD, 1, 3, 1.0f)));


        REGISTRY.put(WorldObject.Type.GRASS,
            new HarvestDefinition(null, 1, new DropTable()));


        REGISTRY.put(WorldObject.Type.SEASHELLS,
            new HarvestDefinition(null, 1,
                 new DropTable().addDrop(Item.SEASHELL, 1, 3, 1f)
                
                ));


        REGISTRY.put(WorldObject.Type.RAW_ORE,
            new HarvestDefinition(ItemClass.PICKAXE, 8,
                new DropTable()
                    .addDrop(Item.RAW_ORE, 1, 3, 1f)
                
                ));

        REGISTRY.put(WorldObject.Type.LANTERN,
            new HarvestDefinition(null, 3, new DropTable()
                .addDrop(Item.LANTERN, 1, 1, 1.0f)));
        
        REGISTRY.put(WorldObject.Type.SAND_CASTLE,
            new HarvestDefinition(null, 3, new DropTable()
                .addDrop(Item.SAND, 1, 1, 1.0f)));

        REGISTRY.put(WorldObject.Type.GRASS,
            new HarvestDefinition(null, 1, new DropTable()
                .addDrop(Item.GRASS, 1, 1, 1.0f)));



        REGISTRY.put(WorldObject.Type.BUSH,
            new HarvestDefinition(null, 2, new DropTable()
                .addDrop(Item.LEAF, 1, 2, 1.0f)));
        
        REGISTRY.put(WorldObject.Type.SNOWITE,
            new HarvestDefinition(null, 2,
                new DropTable()
                    .addDrop(Item.SNOWITE_PIECES, 1, 3, 1.0f)));

        REGISTRY.put(WorldObject.Type.SAND_PILE,
            new HarvestDefinition(null, 2,
                new DropTable()
                    .addDrop(Item.SAND, 2, 4, 1.0f)));

        REGISTRY.put(WorldObject.Type.DEAD_BUSH,
            new HarvestDefinition(null, 2,
                new DropTable()
                    .addDrop(Item.STICK, 1, 3, 0.5f)));

        REGISTRY.put(WorldObject.Type.CACTI,
            new HarvestDefinition(ItemClass.AXE, 2,
                new DropTable()
                    .addDrop(Item.CACTI_FLOWER, 1, 1, .25f)));


        REGISTRY.put(WorldObject.Type.QUALE,
            new HarvestDefinition(null, 1,
                new DropTable()
                    .addDrop(Item.QUALE, 1, 3, 1.0f)
                    .addDrop(Item.QUALE_SEEDS, 1, 2, 0.5f)
                ));

    }

    public static HarvestDefinition get(WorldObject.Type type) {
        return REGISTRY.get(type);
    }

    public static boolean isHarvestable(WorldObject.Type type) {
        return REGISTRY.containsKey(type);
    }

    private HarvestRegistry() {}
}