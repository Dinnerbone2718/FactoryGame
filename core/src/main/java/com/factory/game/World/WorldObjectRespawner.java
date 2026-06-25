package com.factory.game.World;

import java.util.EnumMap;
import java.util.Map;


public final class WorldObjectRespawner {

    public static final class RespawnConfig {

        public final String groundTileName;

        public final double spawnChance;

        public final float checkInterval;

        public final int maxPerChunk;

        public RespawnConfig(String groundTileName, double spawnChance,
                              float checkInterval, int maxPerChunk) {
            this.groundTileName = groundTileName;
            this.spawnChance    = spawnChance;
            this.checkInterval  = checkInterval;
            this.maxPerChunk    = maxPerChunk;
        }
    }

    private static final Map<WorldObject.Type, RespawnConfig> CONFIGS =
            new EnumMap<>(WorldObject.Type.class);
    static {


        CONFIGS.put(WorldObject.Type.GRASS, new RespawnConfig("grass", 0.05, 250f, 25));
    }

    public static RespawnConfig getConfig(WorldObject.Type type) {
        return CONFIGS.get(type);
    }

    public static boolean isRespawnable(WorldObject.Type type) {
        return CONFIGS.containsKey(type);
    }

    public static Map<WorldObject.Type, RespawnConfig> all() {
        return CONFIGS;
    }

    private WorldObjectRespawner() {}
}