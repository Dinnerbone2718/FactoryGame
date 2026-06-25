package com.factory.game.Items;

public enum Item {
    //NATURAL STUFF
    WOOD("Wood", "items/wood.png", true, 50, null),
    STICK("Stick", "items/stick.png", true, 50, null),
    STONE("Stone", "items/stone.png", true, 50, null),
    COAL("Coal", "items/coal.png", true, 50, null),
    SAND("Sand", "items/sand.png", true, 50, null),
    SEASHELL("Seashell", "items/seashell.png", true, 50, null),
    GRASS("Grass", "items/grass_weed.png", true, 50, null),
    LEAF("Leaf", "items/leaf.png", true, 50, null),
    CACTI_FLOWER("Cacti Flower", "items/cacti_flower.png", true, 50, null),

    //BUILDABLES
    WALL("Wood Fence", "items/wall.png", true, 50, null),
    STONE_WALL("Stone Fence", "items/stone_wall.png", true, 50, null),
    WOOD_WALL("Wood Wall", "items/wood_wall.png", true, 50, null),
    STONE_WALL_FULL("Stone Wall", "items/stone_wall_full.png", true, 50, null),
    GLOBE("Globe", "items/globe.png", true, 50, null),
    BOOK_SHELF("Book Shelf", "items/book_shelf.png", true, 50, null),
    CHAIR("Chair", "items/chair.png", true, 50, null),
    SIGN("Sign", "items/sign.png", true, 50, null),
    TABLE("Table", "items/table.png", true, 50, null),
    DECO_BARREL("Decorative Barrel", "items/barrel_deco.png", true, 50, null),
    WOOD_PLANKS("Wood Planks", "items/wood_planks.png", true, 50, null),
    TOILET("Toilet", "items/toilet.png", true, 50, null),
    LAMP_POST("Lamp Post", "items/lamp_post.png", true, 50, null),

    //UNIQUE WORLD THINGS
    RAFT("Raft", "items/raft.png", false, 1, null),
    MAP("Map", "items/map.png", false, 1, null),
    CLOCK("Clock", "items/clock.png", false, 1, null),
    FLASHLIGHT("Flashlight", "items/flashlight.png", false, 1, null),
    PLAYER_GUIDE("Player Guide", "items/player_guide.png", false, 1, null),
    GOBLINO_COIN(
        "Goblino Coin",
        "items/goblino.png",
        true,
        Integer.MAX_VALUE,
        null
    ),
    ANINMAL_PHONE("Animal Phone", "items/animal_phone.png", false, 1, null),
    BUBBLE_WAND("Bubble Wand", "items/bubble_wand.png", false, 1, null),

    //ORE
    RAW_ORE("Raw Ore", "items/raw_ore.png", true, 50, null),
    RAW_OREMINUM("Raw Oreminum", "items/raw_oreminum.png", true, 50, null),
    RAW_ROCKITE("Raw Rockite", "items/raw_rockite.png", true, 50, null),
    RAW_EARTHEL("Raw Earthel", "items/raw_earthel.png", true, 50, null),
    RAW_STONEDON("Raw Stonedon", "items/raw_stonedon.png", true, 50, null),
    COROSERITE("Coroserite", "items/coroserite.png", true, 50, null),
    SNOWITE_PIECES(
        "Snowite Pieces",
        "items/snowite_pieces.png",
        true,
        50,
        null
    ),

    CRUSHED_OREMINUM(
        "Crushed Oreminum",
        "items/crushed_oreminum.png",
        true,
        50,
        null
    ),
    CRUSHED_ROCKITE(
        "Crushed Rockite",
        "items/crushed_rockite.png",
        true,
        50,
        null
    ),
    CRUSHED_EARTHEL(
        "Crushed Earthel",
        "items/crushed_earthel.png",
        true,
        50,
        null
    ),
    CRUSHED_STONEDON(
        "Crushed Stonedon",
        "items/crushed_stonedon.png",
        true,
        50,
        null
    ),

    OREMINUM_INGOT(
        "Oreminum Ingot",
        "items/oreminum_ingot.png",
        true,
        50,
        null
    ),
    ROCKITE_INGOT("Rockite Ingot", "items/rockite_ingot.png", true, 50, null),
    EARTHEL_INGOT("Earthel Ingot", "items/earthel_ingot.png", true, 50, null),
    STONEDON_INGOT(
        "Stonedon Ingot",
        "items/stonedon_ingot.png",
        true,
        50,
        null
    ),
    SNOWITE_INGOT("Snowite Ingot", "items/snowite_ingot.png", true, 50, null),
    COMPOSITE_INGOT(
        "Composite Ingot",
        "items/composite_ingot.png",
        true,
        50,
        null
    ),

    //CIRCUT BOARD PARTS
    CAPACITOR("Capacitor", "items/capacitor.png", true, 50, null),
    EMPTY_CIRCUIT_BOARD(
        "Empty Circuit Board",
        "items/empty_circuit_board.png",
        true,
        50,
        null
    ),
    MICROCHIP("Microchip", "items/microchip.png", true, 50, null),
    RESISTOR("Resistor", "items/resistor.png", true, 50, null),
    CIRCUIT_BOARD("Circuit Board", "items/circuit_board.png", true, 50, null),
    CIRCUIT_BOARD_ADVANCED(
        "Advanced Circuit Board",
        "items/circuit_board_advanced.png",
        true,
        50,
        null
    ),

    //LIGHTING
    LANTERN("Lantern", "items/lantern.png", true, 50, null),
    CAMPFIRE("Campfire", "items/campfire.png", false, 1, null),

    //TOOLS
    SIFT("Sift", "items/sift.png", false, 1, ItemClass.SIFT),
    WOOD_AXE("Wood Axe", "items/wood_axe.png", false, 1, ItemClass.AXE),
    STONE_AXE("Stone Axe", "items/stone_axe.png", false, 1, ItemClass.AXE),
    WOOD_PICKAXE(
        "Wood Pickaxe",
        "items/wood_pickaxe.png",
        false,
        1,
        ItemClass.PICKAXE
    ),
    STONE_PICKAXE(
        "Stone Pickaxe",
        "items/stone_pickaxe.png",
        false,
        1,
        ItemClass.PICKAXE
    ),
    FISHING_ROD(
        "Fishing Rod",
        "items/fishing_rod.png",
        false,
        1,
        ItemClass.FISHING_ROD
    ),
    ORE_FISHING_ROD(
        "Ore Fishing Rod",
        "items/ore_fishing_rod.png",
        false,
        1,
        ItemClass.FISHING_ROD
    ),
    DYLAN_ROD(
        "Anglerfish Rod",
        "items/dylan_rod.png",
        false,
        1,
        ItemClass.FISHING_ROD
    ),

    WRENCH_AND_SCREW(
        "Wrench and Screwdriver",
        "items/wrench_and_screw.png",
        false,
        1,
        ItemClass.TOOL
    ),

    //ECT
    SCRAP("Scrap", "items/scrap.png", true, 50, null),
    CRUSHED_LEAF("Crushed Leaf", "items/crushed_leaf.png", true, 50, null),
    PENGUIN_PLUSH("Penguin Plush", "items/penguin_plush.png", true, 50, null),
    POO_PET("Poo Pet", "items/poo_pet.png", true, 50, null),
    REFINED_WOOD("Refined Wood", "items/refined_wood.png", true, 50, null),
    BONE("Bone", "items/bone.png", true, 50, null),
    CLAY("Clay", "items/clay.png", true, 50, null),
    FISHBOWL("Fishbowl", "items/fishbowl.png", true, 50, null),
    PAPER("Paper", "items/paper.png", true, 50, null),
    STRING("String", "items/string.png", true, 50, null),

    //FISH
    FISH("Fish", "items/fish.png", true, 50, null),
    DFISH1("Evil Goldfish", "items/dylan_fish1.png", true, 50, null),
    DFISH2("Blue Tang", "items/dylan_fish2.png", true, 50, null),
    DFISH3("Pygmy Swordfish", "items/dylan_fish3.png", true, 50, null),
    DFISH4("The Amazing Mahi Mahi", "items/dylan_fish4.png", true, 50, null),

    //MACHINES
    DISTILLERY("Distillery", "items/distillery.png", false, 1, null),
    WELL("Well", "items/well.png", false, 1, null),
    BARREL("Barrel", "items/barrel.png", false, 1, null),
    CRUSHING_POT("Crushing Pot", "items/crushing_pot.png", false, 1, null),
    FURNACE("Furnace", "items/furnace.png", false, 1, null),
    CAST("Cast", "items/cast.png", false, 1, null),
    STORAGE_CRATE("Storage Crate", "items/storage_crate.png", false, 1, null),
    SOLDERING_TABLE(
        "Soldering Table",
        "items/soldering_table.png",
        false,
        1,
        null
    ),
    MIXER("Mixer", "items/mixer.png", false, 1, null),
    PLANTER("Planter", "items/pot.png", false, 1, null),
    CRUSHER("Crusher", "items/crusher.png", false, 1, null),
    DRILL("Drill", "items/drill.png", false, 1, null),
    TANK("Tank", "items/tank.png", false, 1, null),
    SMELTER("Smelter", "items/smelter.png", false, 1, null),
    CHUNK_LOADER("Chunk Loader", "items/chunk_loader.png", false, 1, null),
    TRASH("Trash", "items/trash.png", false, 1, null),
    ORE_DRILL("Ore Drill", "items/ore_drill.png", false, 1, null),

    DEVBARREL("Dev Barrel", "items/barrel.png", false, 1, null),

    //FLOOR
    WOOD_FLOOR("Wood Floor", "items/wood_floor.png", true, 50, null),
    STONE_FLOOR("Stone Floor", "items/stone_floor.png", true, 50, null),

    //TRANSPORT
    BASIC_PIPE("Basic Pipe", "items/basic_pipe.png", true, 50, null),
    FILTER_PIPE("Filter Pipe", "items/filter_pipe.png", true, 50, null),
    ITEM_PIPE("Item Pipe", "items/transport_pipe.png", true, 50, null),
    FILTER_ITEM_PIPE(
        "Filter Item Pipe",
        "items/filter_transport_pipe.png",
        true,
        50,
        null
    ),

    //FARMING
    QUALE("Quale", "items/quale.png", true, 50, null),
    QUALE_SEEDS("Quale Seed", "items/quale_seed.png", true, 50, null);

    private final String displayName;
    private final String texturePath;
    private final boolean stackable;
    private final int maxStackSize;
    private final ItemClass toolClass;

    Item(
        String displayName,
        String texturePath,
        boolean stackable,
        int maxStackSize,
        ItemClass toolClass
    ) {
        this.displayName = displayName;
        this.texturePath = texturePath;
        this.stackable = stackable;
        this.maxStackSize = maxStackSize;
        this.toolClass = toolClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public boolean isStackable() {
        return stackable;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public ItemClass getToolClass() {
        return toolClass;
    }

    public boolean isTool() {
        return toolClass != null;
    }
}
