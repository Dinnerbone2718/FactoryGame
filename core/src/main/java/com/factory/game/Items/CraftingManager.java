package com.factory.game.Items;

import com.factory.game.World.DistilleryRecipe;
import com.factory.game.World.LiquidType;
import com.factory.game.World.MixingRecipe;
import java.util.ArrayList;
import java.util.List;

public class CraftingManager {

    private static final List<CraftingRecipe> recipes = new ArrayList<>();
    private static final List<SiftingRecipe> siftRecipes = new ArrayList<>();
    private static final List<CrushingRecipe> crushRecipes = new ArrayList<>();
    private static final List<SmeltingRecipe> smeltRecipes = new ArrayList<>();
    private static final List<FishingRecipe> fishRecipes = new ArrayList<>();
    private static final List<SolderingRecipe> solderingRecipes =
        new ArrayList<>();
    private static final List<PlanterRecipe> planterRecipes = new ArrayList<>();
    public static final List<MixingRecipe> mixingRecipes = new ArrayList<>();
    public static final List<DistilleryRecipe> distilleryRecipes =
        new ArrayList<>();
    private static final List<GoblinoRecipe> goblinoRecipes = new ArrayList<>();
    private static final List<GoblinoBarterRecipe> goblinoBarterRecipes =
        new ArrayList<>();
    private static final List<GoblinoGiftRecipe> goblinoGiftRecipes =
        new ArrayList<>();
    private static final List<FoodManager> foodRecipes = new ArrayList<>();

    static {
        foodRecipes.add(new FoodManager(Item.FISH, 20, 0));
        foodRecipes.add(new FoodManager(Item.DFISH1, 20, 0));
        foodRecipes.add(new FoodManager(Item.DFISH2, 25, 0));
        foodRecipes.add(new FoodManager(Item.DFISH3, 30, 5));
        foodRecipes.add(new FoodManager(Item.DFISH4, 40, 10));
        foodRecipes.add(new FoodManager(Item.QUALE, 15, 10));
        foodRecipes.add(new FoodManager(Item.SODA, 5, 50));
    }

    static {
        siftRecipes.add(
            new SiftingRecipe("Raw Ore", Item.RAW_ORE)
                .addOutput(Item.RAW_OREMINUM, 35)
                .addOutput(Item.RAW_ROCKITE, 35)
                .addOutput(Item.RAW_EARTHEL, 25)
                .addOutput(Item.RAW_STONEDON, 5)
        );

        siftRecipes.add(
            new SiftingRecipe("Sand", Item.SAND)
                .addOutput(Item.SCRAP, 70)
                .addOutput(Item.SEASHELL, 10)
                .addOutput(Item.CLAY, 5)
                .addOutput(Item.BONE, 5)
                .addOutput(Item.MAP, 5)
                .addOutput(Item.COROSERITE, 5)
        );
    }

    static {
        recipes.add(
            new CraftingRecipe("Wood Fence", Item.WALL, 1)
                .addInput(Item.STICK, 2)
                .addInput(Item.WOOD, 1)
        );

        recipes.add(
            new CraftingRecipe("Wood Wall", Item.WOOD_WALL, 1)
                .addInput(Item.STICK, 2)
                .addInput(Item.WOOD, 2)
        );

        recipes.add(
            new CraftingRecipe("Stone Fence", Item.STONE_WALL, 1)
                .addInput(Item.STONE, 2)
                .addInput(Item.STICK, 1)
        );

        recipes.add(
            new CraftingRecipe("Stone Wall", Item.STONE_WALL_FULL, 1)
                .addInput(Item.STONE, 2)
                .addInput(Item.STICK, 2)
        );

        recipes.add(
            new CraftingRecipe("Sticks", Item.STICK, 5).addInput(Item.WOOD, 1)
        );

        recipes.add(
            new CraftingRecipe("Wood Axe", Item.WOOD_AXE, 1)
                .addInput(Item.STICK, 5)
                .addInput(Item.WOOD, 3)
        );

        recipes.add(
            new CraftingRecipe("Stone Axe", Item.STONE_AXE, 1)
                .addInput(Item.STICK, 5)
                .addInput(Item.STONE, 10)
        );

        recipes.add(
            new CraftingRecipe("Wood Pickaxe", Item.WOOD_PICKAXE, 1)
                .addInput(Item.STICK, 5)
                .addInput(Item.WOOD, 3)
        );

        recipes.add(
            new CraftingRecipe("Stone Pickaxe", Item.STONE_PICKAXE, 1)
                .addInput(Item.STICK, 5)
                .addInput(Item.STONE, 10)
        );

        recipes.add(
            new CraftingRecipe("Lantern", Item.LANTERN, 4)
                .addInput(Item.WOOD, 1)
                .addInput(Item.STICK, 3)
                .addInput(Item.COAL, 4)
                .addInput(Item.STONE, 2)
        );

        recipes.add(
            new CraftingRecipe("Campfire", Item.CAMPFIRE, 1)
                .addInput(Item.WOOD, 3)
                .addInput(Item.STICK, 4)
                .addInput(Item.COAL, 2)
        );

        recipes.add(
            new CraftingRecipe("Ore Fishing Rod", Item.ORE_FISHING_ROD, 1)
                .addInput(Item.FISHING_ROD, 1)
                .addInput(Item.RAW_OREMINUM, 3)
                .addInput(Item.RAW_ROCKITE, 3)
                .addInput(Item.RAW_EARTHEL, 3)
                .addInput(Item.RAW_STONEDON, 3)
        );

        recipes.add(
            new CraftingRecipe(
                "Wrench and Screwdriver",
                Item.WRENCH_AND_SCREW,
                1
            )
                .addInput(Item.STICK, 4)
                .addInput(Item.SCRAP, 3)
        );

        recipes.add(
            new CraftingRecipe("Basic Pipe", Item.BASIC_PIPE, 4).addInput(
                Item.SCRAP,
                2
            )
        );

        recipes.add(
            new CraftingRecipe("Filter Pipe", Item.FILTER_PIPE, 4)
                .addInput(Item.SCRAP, 2)
                .addInput(Item.CIRCUIT_BOARD, 1)
                .addInput(Item.COROSERITE, 1)
        );

        recipes.add(
            new CraftingRecipe("Item Pipe", Item.ITEM_PIPE, 4)
                .addInput(Item.SCRAP, 2)
                .addInput(Item.OREMINUM_INGOT, 1)
        );

        recipes.add(
            new CraftingRecipe("Quale Seeds", Item.QUALE_SEEDS, 1).addInput(
                Item.QUALE,
                2
            )
        );

        recipes.add(
            new CraftingRecipe("Fishing Rod", Item.FISHING_ROD, 1)
                .addInput(Item.STICK, 2)
                .addInput(Item.STRING, 1)
        );

        recipes.add(
            new CraftingRecipe("Sift", Item.SIFT, 1)
                .addInput(Item.PAPER, 1)
                .addInput(Item.STRING, 2)
                .addInput(Item.STICK, 4)
        );

        recipes.add(
            new CraftingRecipe("Paper", Item.PAPER, 1).addInput(Item.GRASS, 4)
        );

        recipes.add(
            new CraftingRecipe("String", Item.STRING, 3).addInput(Item.QUALE, 1)
        );

        recipes.add(
            new CraftingRecipe("Wood Planks", Item.WOOD_PLANKS, 4).addInput(
                Item.WOOD,
                2
            )
        );

        recipes.add(
            new CraftingRecipe("Refined Wood", Item.REFINED_WOOD, 2)
                .addInput(Item.WOOD_PLANKS, 2)
                .addInput(Item.COAL, 1)
        );

        recipes.add(
            new CraftingRecipe("Wood Floor", Item.WOOD_FLOOR, 4).addInput(
                Item.WOOD_PLANKS,
                2
            )
        );

        recipes.add(
            new CraftingRecipe("Stone Floor", Item.STONE_FLOOR, 4).addInput(
                Item.STONE,
                3
            )
        );

        recipes.add(
            new CraftingRecipe("Chair", Item.CHAIR, 1)
                .addInput(Item.WOOD, 2)
                .addInput(Item.STICK, 3)
        );

        recipes.add(
            new CraftingRecipe("Table", Item.TABLE, 1)
                .addInput(Item.WOOD, 3)
                .addInput(Item.STICK, 4)
        );

        recipes.add(
            new CraftingRecipe("Sign", Item.SIGN, 1)
                .addInput(Item.WOOD, 1)
                .addInput(Item.STICK, 2)
        );

        recipes.add(
            new CraftingRecipe("Decorative Barrel", Item.DECO_BARREL, 1)
                .addInput(Item.WOOD, 4)
                .addInput(Item.STICK, 2)
        );

        recipes.add(
            new CraftingRecipe("Book Shelf", Item.BOOK_SHELF, 1)
                .addInput(Item.WOOD, 4)
                .addInput(Item.PAPER, 3)
                .addInput(Item.STICK, 2)
        );

        recipes.add(
            new CraftingRecipe("Globe", Item.GLOBE, 1)
                .addInput(Item.WOOD, 2)
                .addInput(Item.STONE, 2)
                .addInput(Item.PAPER, 4)
        );

        recipes.add(
            new CraftingRecipe("Lamp Post", Item.LAMP_POST, 1)
                .addInput(Item.STICK, 4)
                .addInput(Item.STONE, 4)
                .addInput(Item.COAL, 2)
                .addInput(Item.WOOD, 2)
        );

        recipes.add(
            new CraftingRecipe("Toilet", Item.TOILET, 1)
                .addInput(Item.STONE, 6)
                .addInput(Item.CLAY, 4)
        );

        recipes.add(
            new CraftingRecipe("Fishbowl", Item.FISHBOWL, 1)
                .addInput(Item.CLAY, 4)
                .addInput(Item.SAND, 3)
        );

        recipes.add(
            new CraftingRecipe("Raft", Item.RAFT, 1)
                .addInput(Item.WOOD, 10)
                .addInput(Item.STICK, 8)
                .addInput(Item.STRING, 4)
        );

        recipes.add(
            new CraftingRecipe("Clock", Item.CLOCK, 1)
                .addInput(Item.OREMINUM_INGOT, 2)
                .addInput(Item.SCRAP, 4)
                .addInput(Item.WOOD, 2)
        );

        recipes.add(
            new CraftingRecipe("Flashlight", Item.FLASHLIGHT, 1)
                .addInput(Item.OREMINUM_INGOT, 2)
                .addInput(Item.CIRCUIT_BOARD, 1)
                .addInput(Item.COAL, 2)
                .addInput(Item.STICK, 1)
        );

        recipes.add(
            new CraftingRecipe(
                "Empty Circuit Board",
                Item.EMPTY_CIRCUIT_BOARD,
                2
            )
                .addInput(Item.SCRAP, 4)
                .addInput(Item.REFINED_WOOD, 2)
        );

        recipes.add(
            new CraftingRecipe("Capacitor", Item.CAPACITOR, 3)
                .addInput(Item.SCRAP, 2)
                .addInput(Item.OREMINUM_INGOT, 1)
        );

        recipes.add(
            new CraftingRecipe("Resistor", Item.RESISTOR, 4)
                .addInput(Item.SCRAP, 3)
                .addInput(Item.COAL, 1)
        );

        recipes.add(
            new CraftingRecipe("Microchip", Item.MICROCHIP, 2)
                .addInput(Item.OREMINUM_INGOT, 1)
                .addInput(Item.SCRAP, 2)
                .addInput(Item.COROSERITE, 1)
        );

        recipes.add(
            new CraftingRecipe("Well", Item.WELL, 1)
                .addInput(Item.STONE, 8)
                .addInput(Item.WOOD, 4)
                .addInput(Item.SAND, 4)
        );

        recipes.add(
            new CraftingRecipe("Barrel", Item.BARREL, 1)
                .addInput(Item.WOOD, 8)
                .addInput(Item.STICK, 4)
        );

        recipes.add(
            new CraftingRecipe("Storage Crate", Item.STORAGE_CRATE, 1)
                .addInput(Item.WOOD, 6)
                .addInput(Item.STICK, 4)
                .addInput(Item.STONE, 2)
        );

        recipes.add(
            new CraftingRecipe("Planter", Item.PLANTER, 1)
                .addInput(Item.WOOD, 6)
                .addInput(Item.STONE, 4)
                .addInput(Item.CLAY, 3)
        );

        recipes.add(
            new CraftingRecipe("Trash", Item.TRASH, 1)
                .addInput(Item.WOOD, 2)
                .addInput(Item.SCRAP, 4)
        );

        recipes.add(
            new CraftingRecipe("Crushing Pot", Item.CRUSHING_POT, 1)
                .addInput(Item.STONE, 8)
                .addInput(Item.WOOD, 4)
                .addInput(Item.STICK, 4)
        );

        recipes.add(
            new CraftingRecipe("Furnace", Item.FURNACE, 1)
                .addInput(Item.STONE, 12)
                .addInput(Item.COAL, 4)
                .addInput(Item.STICK, 4)
        );

        recipes.add(
            new CraftingRecipe("Cast", Item.CAST, 1)
                .addInput(Item.STONE, 10)
                .addInput(Item.CLAY, 6)
        );

        recipes.add(
            new CraftingRecipe("Smelter", Item.SMELTER, 1)
                .addInput(Item.STONE, 8)
                .addInput(Item.COAL, 8)
                .addInput(Item.SCRAP, 6)
                .addInput(Item.CIRCUIT_BOARD, 2)
        );

        recipes.add(
            new CraftingRecipe("Tank", Item.TANK, 1)
                .addInput(Item.OREMINUM_INGOT, 4)
                .addInput(Item.SCRAP, 6)
        );

        recipes.add(
            new CraftingRecipe("Soldering Table", Item.SOLDERING_TABLE, 1)
                .addInput(Item.WOOD, 6)
                .addInput(Item.SCRAP, 6)
                .addInput(Item.OREMINUM_INGOT, 2)
                .addInput(Item.REFINED_WOOD, 2)
        );

        recipes.add(
            new CraftingRecipe("Crusher", Item.CRUSHER, 1)
                .addInput(Item.OREMINUM_INGOT, 4)
                .addInput(Item.STONE, 4)
                .addInput(Item.SCRAP, 6)
                .addInput(Item.CIRCUIT_BOARD, 1)
        );

        recipes.add(
            new CraftingRecipe("Mixer", Item.MIXER, 1)
                .addInput(Item.OREMINUM_INGOT, 4)
                .addInput(Item.SCRAP, 6)
                .addInput(Item.CIRCUIT_BOARD, 1)
        );

        recipes.add(
            new CraftingRecipe("Distillery", Item.DISTILLERY, 1)
                .addInput(Item.OREMINUM_INGOT, 6)
                .addInput(Item.CIRCUIT_BOARD, 2)
                .addInput(Item.STONE, 4)
                .addInput(Item.SCRAP, 4)
        );

        recipes.add(
            new CraftingRecipe("Drill", Item.DRILL, 1)
                .addInput(Item.OREMINUM_INGOT, 6)
                .addInput(Item.CIRCUIT_BOARD, 2)
                .addInput(Item.STONE_PICKAXE, 1)
                .addInput(Item.SCRAP, 4)
        );

        recipes.add(
            new CraftingRecipe("Chunk Loader", Item.CHUNK_LOADER, 1)
                .addInput(Item.COMPOSITE_INGOT, 4)
                .addInput(Item.CIRCUIT_BOARD_ADVANCED, 2)
                .addInput(Item.ROCKITE_INGOT, 4)
        );
    }

    static {
        crushRecipes.add(
            new CrushingRecipe(
                "Crushed Oreminum",
                Item.RAW_OREMINUM,
                Item.CRUSHED_OREMINUM,
                2
            )
        );
        crushRecipes.add(
            new CrushingRecipe(
                "Crushed Rockite",
                Item.RAW_ROCKITE,
                Item.CRUSHED_ROCKITE,
                2
            )
        );
        crushRecipes.add(
            new CrushingRecipe(
                "Crushed Earthel",
                Item.RAW_EARTHEL,
                Item.CRUSHED_EARTHEL,
                2
            )
        );
        crushRecipes.add(
            new CrushingRecipe(
                "Crushed Stonedon",
                Item.RAW_STONEDON,
                Item.CRUSHED_STONEDON,
                2
            )
        );
        crushRecipes.add(
            new CrushingRecipe("Crushed Leaf", Item.LEAF, Item.CRUSHED_LEAF, 1)
        );
    }

    static {
        smeltRecipes.add(
            new SmeltingRecipe(
                "Smelt Oreminum",
                Item.CRUSHED_OREMINUM,
                LiquidType.MOLTEN_OREMINUM,
                50f
            )
        );
        smeltRecipes.add(
            new SmeltingRecipe(
                "Smelt Rockite",
                Item.CRUSHED_ROCKITE,
                LiquidType.MOLTEN_ROCKITE,
                50f
            )
        );
        smeltRecipes.add(
            new SmeltingRecipe(
                "Smelt Earthel",
                Item.CRUSHED_EARTHEL,
                LiquidType.MOLTEN_EARTHEL,
                50f
            )
        );
        smeltRecipes.add(
            new SmeltingRecipe(
                "Smelt Stonedon",
                Item.CRUSHED_STONEDON,
                LiquidType.MOLTEN_STONEDON,
                50f
            )
        );
        smeltRecipes.add(
            new SmeltingRecipe(
                "Fertilizer",
                Item.CRUSHED_LEAF,
                LiquidType.FERTILIZER,
                25f
            )
        );
        smeltRecipes.add(
            new SmeltingRecipe(
                "Smelt Snowite",
                Item.SNOWITE_PIECES,
                LiquidType.MOLTEN_SNOWITE,
                25f
            )
        );
    }

    static {
        fishRecipes.add(
            new FishingRecipe(Item.FISHING_ROD)
                .setDifficulty(FishingMinigame.Difficulty.SIMPLE)
                .addOutput(Item.FISH, 65)
                .addOutput(Item.SEASHELL, 20)
                .addOutput(Item.SCRAP, 16)
                .addOutput(Item.DYLAN_ROD, 2)
                .addOutput(Item.MAP, 1)
                .addOutput(Item.PENGUIN_PLUSH, 1)
        );

        fishRecipes.add(
            new FishingRecipe(Item.ORE_FISHING_ROD)
                .setDifficulty(FishingMinigame.Difficulty.MEDIUM)
                .addOutput(Item.FISH, 65)
                .addOutput(Item.RAW_EARTHEL, 5)
                .addOutput(Item.RAW_OREMINUM, 5)
                .addOutput(Item.RAW_STONEDON, 5)
                .addOutput(Item.RAW_ROCKITE, 5)
                .addOutput(Item.COROSERITE, 5)
                .addOutput(Item.SCRAP, 10)
        );

        fishRecipes.add(
            new FishingRecipe(Item.DYLAN_ROD)
                .setDifficulty(FishingMinigame.Difficulty.COMPLEX)
                .addOutput(Item.DFISH1, 25)
                .addOutput(Item.DFISH2, 25)
                .addOutput(Item.DFISH3, 25)
                .addOutput(Item.DFISH4, 25)
        );
    }

    static {
        solderingRecipes.add(
            new SolderingRecipe("Circuit Board", Item.CIRCUIT_BOARD, 1)
                .addInput(Item.EMPTY_CIRCUIT_BOARD, 1)
                .addInput(Item.CAPACITOR, 1)
                .addInput(Item.RESISTOR, 1)
                .addInput(Item.MICROCHIP, 1)
        );

        solderingRecipes.add(
            new SolderingRecipe(
                "Advanced Circuit Board",
                Item.CIRCUIT_BOARD_ADVANCED,
                1
            )
                .addInput(Item.CIRCUIT_BOARD, 2)
                .addInput(Item.MICROCHIP, 2)
                .addInput(Item.COROSERITE, 1)
                .addInput(Item.COMPOSITE_INGOT, 1)
        );
    }

    static {
        planterRecipes.add(
            new PlanterRecipe("Quale Seeds", Item.QUALE_SEEDS, 60f, 1f)
                .addDrop(Item.QUALE, 90, 2, 4)
                .addDrop(Item.QUALE_SEEDS, 10, 1, 3)
                .setGrowSheet("machines/farming/quale_plant.png")
        );

        planterRecipes.add(
            new PlanterRecipe("Cacti Flower", Item.CACTI_FLOWER, 120f, 0.1f)
                .addDrop(Item.CACTI_FLOWER, 90, 2, 4)
                .addDrop(Item.STICK, 10, 1, 3)
                .setGrowSheet("machines/farming/cacti.png")
        );

        planterRecipes.add(
            new PlanterRecipe("Grass", Item.GRASS, 20f, 0.5f)
                .addDrop(Item.GRASS, 100, 3, 5)
                .setGrowSheet("machines/farming/grass.png")
        );
    }

    static {
        mixingRecipes.add(
            new MixingRecipe("Coolant", LiquidType.COOLANT, 4f, 100f)
                .addInput(LiquidType.WATER, 6f, 80f)
                .addInput(LiquidType.MOLTEN_SNOWITE, 2f, 40f)
        );

        mixingRecipes.add(
            new MixingRecipe(
                "Fertilizer Solution",
                LiquidType.FERTILIZER,
                3f,
                100f
            )
                .addInput(LiquidType.WATER, 5f, 80f)
                .addInput(LiquidType.MOLTEN_EARTHEL, 2f, 40f)
        );
    }

    static {
        distilleryRecipes.add(
            new DistilleryRecipe("Oil Refining")
                .addInput(LiquidType.OIL, 3f, 100f)
                .addInput(LiquidType.WATER, 1f, 40f)
                .addOutput(LiquidType.REFINED_OIL, 2f, 100f)
                .addOutput(LiquidType.SLAG, 0.5f, 40f)
        );

        distilleryRecipes.add(
            new DistilleryRecipe("Cryo Distillation")
                .addInput(LiquidType.WATER, 4f, 100f)
                .addInput(LiquidType.MOLTEN_SNOWITE, 2f, 60f)
                .addOutput(LiquidType.COOLANT, 3f, 100f)
                .addOutput(LiquidType.STEAM, 2f, 60f)
        );

        distilleryRecipes.add(
            new DistilleryRecipe("Metal Alloying")
                .addInput(LiquidType.MOLTEN_OREMINUM, 2f, 80f)
                .addInput(LiquidType.MOLTEN_ROCKITE, 2f, 80f)
                .addInput(LiquidType.MOLTEN_EARTHEL, 1f, 40f)
                .addOutput(LiquidType.COMPOSITE_MELT, 3f, 100f)
                .addOutput(LiquidType.SLAG, 1f, 40f)
        );

        distilleryRecipes.add(
            new DistilleryRecipe("Bio Synthesis")
                .addInput(LiquidType.WATER, 3f, 80f)
                .addInput(LiquidType.MOLTEN_EARTHEL, 1f, 40f)
                .addInput(LiquidType.OIL, 1f, 40f)
                .addOutput(LiquidType.FERTILIZER, 2f, 80f)
                .addOutput(LiquidType.REFINED_OIL, 0.5f, 40f)
                .addOutput(LiquidType.SLAG, 0.5f, 30f)
        );
    }

    static {
        goblinoRecipes.add(new GoblinoRecipe("Coal", Item.COAL, 20, 100));
        goblinoRecipes.add(new GoblinoRecipe("Scrap", Item.SCRAP, 20, 100));
        goblinoRecipes.add(new GoblinoRecipe("Wood", Item.WOOD, 20, 100));
        goblinoRecipes.add(new GoblinoRecipe("Stone", Item.STONE, 20, 100));
        goblinoRecipes.add(
            new GoblinoRecipe("Raw Oreminum", Item.RAW_OREMINUM, 10, 10)
        );
        goblinoRecipes.add(
            new GoblinoRecipe("Raw Rockite", Item.RAW_ROCKITE, 10, 10)
        );
        goblinoRecipes.add(
            new GoblinoRecipe("Raw Earthel", Item.RAW_EARTHEL, 20, 10)
        );
        goblinoRecipes.add(
            new GoblinoRecipe("Raw Stonedon", Item.RAW_STONEDON, 30, 10)
        );
    }

    static {
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe("Stone Trade", Item.STONE, 10, Item.COAL, 6)
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Sandy Exchange",
                Item.SAND,
                10,
                Item.CLAY,
                5
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe("Scrap Deal", Item.COAL, 5, Item.SCRAP, 8)
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Ore Appraisal",
                Item.RAW_ORE,
                12,
                Item.RAW_OREMINUM,
                6
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Ore Refinement",
                Item.RAW_OREMINUM,
                6,
                Item.RAW_ROCKITE,
                4
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Fish Market",
                Item.FISH,
                50,
                Item.GOBLINO_COIN,
                1
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Bone Recycling",
                Item.BONE,
                5,
                Item.SCRAP,
                3
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Scrap to Wood",
                Item.SCRAP,
                8,
                Item.WOOD,
                5
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Stone Prospecting",
                Item.STONE,
                24,
                Item.RAW_ORE,
                4
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Grass Weaving",
                Item.GRASS,
                8,
                Item.PAPER,
                3
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Composite Deal",
                Item.OREMINUM_INGOT,
                30,
                Item.COMPOSITE_INGOT,
                1
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Evil Fish Premium",
                Item.DFISH1,
                1,
                Item.GOBLINO_COIN,
                15
            )
        );
        goblinoBarterRecipes.add(
            new GoblinoBarterRecipe(
                "Plush Payout",
                Item.PENGUIN_PLUSH,
                1,
                Item.GOBLINO_COIN,
                20
            )
        );
    }

    static {
        goblinoGiftRecipes.add(
            new GoblinoGiftRecipe(Item.FISH, 1, 3, "Mmm tasty!")
        );
        goblinoGiftRecipes.add(
            new GoblinoGiftRecipe(Item.DFISH1, 1, 4, "Goblino is delighted.")
        );
        goblinoGiftRecipes.add(
            new GoblinoGiftRecipe(Item.DFISH4, 1, 8, "THE AMAZING MAHI MAHI!")
        );
        goblinoGiftRecipes.add(
            new GoblinoGiftRecipe(
                Item.PENGUIN_PLUSH,
                1,
                12,
                "BEST GIFT EVER!! Goblino clutches it with both hands!"
            )
        );
        goblinoGiftRecipes.add(
            new GoblinoGiftRecipe(Item.QUALE, 1, 1, "Yummers....")
        );
        goblinoGiftRecipes.add(
            new GoblinoGiftRecipe(
                Item.CACTI_FLOWER,
                1,
                3,
                "Pretty! But ouchy..."
            )
        );
    }

    public static List<CraftingRecipe> getAllRecipes() {
        return new ArrayList<>(recipes);
    }

    public static List<SiftingRecipe> getAllSiftRecipes() {
        return new ArrayList<>(siftRecipes);
    }

    public static List<CrushingRecipe> getAllCrushRecipes() {
        return new ArrayList<>(crushRecipes);
    }

    public static List<SmeltingRecipe> getAllSmeltRecipes() {
        return new ArrayList<>(smeltRecipes);
    }

    public static List<FishingRecipe> getAllFishingRecipes() {
        return new ArrayList<>(fishRecipes);
    }

    public static List<SolderingRecipe> getAllSolderingRecipes() {
        return new ArrayList<>(solderingRecipes);
    }

    public static List<PlanterRecipe> getAllPlanterRecipes() {
        return new ArrayList<>(planterRecipes);
    }

    public static List<MixingRecipe> getAllMixingRecipes() {
        return new ArrayList<>(mixingRecipes);
    }

    public static List<DistilleryRecipe> getAllDistilleryRecipes() {
        return new ArrayList<>(distilleryRecipes);
    }

    public static List<CraftingRecipe> getAvailableRecipes(
        Inventory inventory
    ) {
        List<CraftingRecipe> available = new ArrayList<>();
        for (CraftingRecipe recipe : recipes) {
            if (recipe.canCraft(inventory)) available.add(recipe);
        }
        return available;
    }

    public static boolean craftRecipe(
        CraftingRecipe recipe,
        Inventory inventory
    ) {
        return recipe.craft(inventory);
    }

    public static List<SiftingRecipe> getAvailableSiftRecipes(
        Inventory inventory
    ) {
        List<SiftingRecipe> available = new ArrayList<>();
        for (SiftingRecipe r : siftRecipes) {
            if (inventory.hasItem(r.getInputItem(), 1)) available.add(r);
        }
        return available;
    }

    public static SiftingRecipe getSiftRecipeFor(Item item) {
        for (SiftingRecipe r : siftRecipes) {
            if (r.getInputItem() == item) return r;
        }
        return null;
    }

    public static List<CrushingRecipe> getAvailableCrushRecipes(
        Inventory inventory
    ) {
        List<CrushingRecipe> available = new ArrayList<>();
        for (CrushingRecipe r : crushRecipes) {
            if (inventory.hasItem(r.getInputItem(), 1)) available.add(r);
        }
        return available;
    }

    public static CrushingRecipe getCrushRecipeFor(Item item) {
        for (CrushingRecipe r : crushRecipes) {
            if (r.getInputItem() == item) return r;
        }
        return null;
    }

    public static List<SmeltingRecipe> getAvailableSmeltRecipes(
        Inventory inventory
    ) {
        List<SmeltingRecipe> available = new ArrayList<>();
        for (SmeltingRecipe r : smeltRecipes) {
            if (inventory.hasItem(r.getInputItem(), 1)) available.add(r);
        }
        return available;
    }

    public static SmeltingRecipe getSmeltRecipeFor(Item item) {
        for (SmeltingRecipe r : smeltRecipes) {
            if (r.getInputItem() == item) return r;
        }
        return null;
    }

    public static List<FishingRecipe> getAvailableSolderingRecipes2(
        Inventory inventory
    ) {
        return new ArrayList<>(fishRecipes);
    }

    public static FishingRecipe getFishingRecipeFor(Item rod) {
        for (FishingRecipe r : fishRecipes) {
            if (r.getRodItem() == rod) return r;
        }
        return null;
    }

    public static List<SolderingRecipe> getAvailableSolderingRecipes(
        Inventory inventory
    ) {
        List<SolderingRecipe> available = new ArrayList<>();
        for (SolderingRecipe r : solderingRecipes) {
            if (r.canCraft(inventory)) available.add(r);
        }
        return available;
    }

    public static List<PlanterRecipe> getAllPlanterRecipesAlt() {
        return new ArrayList<>(planterRecipes);
    }

    public static PlanterRecipe getPlanterRecipeFor(Item seedItem) {
        for (PlanterRecipe r : planterRecipes) {
            if (r.getSeedItem() == seedItem) return r;
        }
        return null;
    }

    public static List<Item> getPlantableSeeds(Inventory inventory) {
        List<Item> result = new ArrayList<>();
        for (PlanterRecipe r : planterRecipes) {
            if (inventory.hasItem(r.getSeedItem(), 1)) result.add(
                r.getSeedItem()
            );
        }
        return result;
    }

    public static MixingRecipe getMixingRecipeFor(String name) {
        for (MixingRecipe r : mixingRecipes) {
            if (r.getName().equals(name)) return r;
        }
        return null;
    }

    public static MixingRecipe getMixingRecipeForOutput(LiquidType outputType) {
        for (MixingRecipe r : mixingRecipes) {
            if (r.getOutputType() == outputType) return r;
        }
        return null;
    }

    public static DistilleryRecipe getDistilleryRecipeFor(String name) {
        for (DistilleryRecipe r : distilleryRecipes) {
            if (r.getName().equals(name)) return r;
        }
        return null;
    }

    public static DistilleryRecipe getDistilleryRecipeByIndex(int index) {
        if (index < 0 || index >= distilleryRecipes.size()) return null;
        return distilleryRecipes.get(index);
    }

    public static int getDistilleryRecipeIndex(DistilleryRecipe recipe) {
        return distilleryRecipes.indexOf(recipe);
    }

    public static List<GoblinoRecipe> getAllGoblinoRecipes() {
        return new ArrayList<>(goblinoRecipes);
    }

    public static List<GoblinoBarterRecipe> getAllGoblinoBarterRecipes() {
        return new ArrayList<>(goblinoBarterRecipes);
    }

    public static List<GoblinoGiftRecipe> getAllGoblinoGiftRecipes() {
        return new ArrayList<>(goblinoGiftRecipes);
    }

    public static FoodManager getFoodManagerFor(Item item) {
        for (FoodManager f : foodRecipes) {
            if (f.getItem() == item) return f;
        }
        return null;
    }
}
