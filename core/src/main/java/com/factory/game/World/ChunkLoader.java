package com.factory.game.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.factory.game.Items.ItemStack;
import com.factory.game.Items.LootTableGenerator;
import com.factory.game.Main;
import com.factory.game.PerlinNoise;

public class ChunkLoader {

    public static final class RawChunkData {
        public final int           cx, cy;
        public final String[]      types;
        public final List<int[]>   objects; 

        RawChunkData(int cx, int cy, String[] types, List<int[]> objects) {
            this.cx      = cx;
            this.cy      = cy;
            this.types   = types;
            this.objects = objects;
        }
    }

    private final ExecutorService                  pool;
    private final Set<String>                      inFlight  = ConcurrentHashMap.newKeySet();
    public  final ConcurrentLinkedQueue<RawChunkData> completed = new ConcurrentLinkedQueue<>();

    public ChunkLoader() {
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        pool = Executors.newFixedThreadPool(threads);
    }

    public void request(int cx, int cy, int noiseOffsetX, int noiseOffsetY, long seed,
                        WorldDelta delta) {
        String key = Chunk.key(cx, cy);
        if (!inFlight.add(key)) return;
        pool.submit(() -> {
            try {
                String[]    types   = computeTypes(cx, cy, noiseOffsetX, noiseOffsetY, seed);
                List<int[]> objects = computeObjects(cx, cy, seed, types, delta, noiseOffsetX, noiseOffsetY);
                completed.add(new RawChunkData(cx, cy, types, objects));
            } finally {
                inFlight.remove(key);
            }
        });
    }

    public boolean isInFlight(int cx, int cy) {
        return inFlight.contains(Chunk.key(cx, cy));
    }

    public void dispose() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(2, TimeUnit.SECONDS)) pool.shutdownNow();
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static final double TERRAIN_SCALE        = 0.0025;
    private static final double BIOME_SCALE          = 0.0015;
    private static final double WATER_MAX            = 0.15;
    private static final double BEACH_MAX            = 0.17;
    private static final double MOUNTAIN_TERRAIN_MIN = 0.35;
    private static final double SNOW_TERRAIN_MIN     = 0.52;
    private static final double FOREST_BIOME_MIN     =  0.17;
    private static final double GRASS_BIOME_MIN     = -.15;

    private static final double RIVER_SCALE          = 0.006;
    private static final double RIVER_WARP_SCALE     = 0.0015;
    private static final double RIVER_WARP_AMP       = 60.0;
    private static final double RIVER_THRESHOLD      = 0.05;
    private static final double BANK_THRESHOLD       = 0.075;


    private static final double VALLEY_CARVE_RANGE = 0.18;  
    private static final double VALLEY_CARVE_DEPTH = 0.07;  


    private static final int    SMOOTH_RADIUS      = 2;

    private static final long RIVER_SEED  = 0xCAFEBABE12345678L;
    private static final long WARP_SEED_X = 0xABCDEF1234567890L;
    private static final long WARP_SEED_Y = 0x0FEDCBA987654321L;


    static double riverElevFactor(double terrainNoise) {
        if (terrainNoise >= MOUNTAIN_TERRAIN_MIN) return 0.0;  
        if (terrainNoise <= BEACH_MAX)            return 0.0; 
        double t = (terrainNoise - BEACH_MAX) / (MOUNTAIN_TERRAIN_MIN - BEACH_MAX); 
        return 1.0 - t * t;  
    }


    private static double smoothedTerrainNoise(int worldX, int worldY, int nox, int noy, long seed) {
        double sum = 0.0, wSum = 0.0;
        for (int dx = -SMOOTH_RADIUS; dx <= SMOOTH_RADIUS; dx++) {
            for (int dy = -SMOOTH_RADIUS; dy <= SMOOTH_RADIUS; dy++) {
                double dist = Math.sqrt(dx * dx + dy * dy);
                double w    = 1.0 / (1.0 + dist * dist); 
                sum  += w * PerlinNoise.noise(
                        (worldX + dx + nox) * TERRAIN_SCALE,
                        (worldY + dy + noy) * TERRAIN_SCALE, seed);
                wSum += w;
            }
        }
        return sum / wSum;
    }

    static double riverValue(int worldX, int worldY, int nox, int noy, long seed) {
        long rs = seed ^ RIVER_SEED;
        double px = (worldX + nox) * RIVER_WARP_SCALE;
        double py = (worldY + noy) * RIVER_WARP_SCALE;

        double warpX = PerlinNoise.noise(px,        py,        seed ^ WARP_SEED_X) * RIVER_WARP_AMP;
        double warpY = PerlinNoise.noise(px + 5.2,  py + 1.3,  seed ^ WARP_SEED_Y) * RIVER_WARP_AMP;

        return Math.abs(PerlinNoise.noise(
                (worldX + nox + warpX) * RIVER_SCALE,
                (worldY + noy + warpY) * RIVER_SCALE,
                rs));
    }

    private static String[] computeTypes(int cx, int cy, int nox, int noy, long seed) {
        int      size  = Main.CHUNK_SIZE;
        String[] types = new String[size * size];

        for (int tx = 0; tx < size; tx++) {
            for (int ty = 0; ty < size; ty++) {
                int worldX = cx * size + tx;
                int worldY = cy * size + ty;


                double terrainNoise = smoothedTerrainNoise(worldX, worldY, nox, noy, seed);

                double biomeNoise = PerlinNoise.noise(
                        (worldX + nox) * BIOME_SCALE,
                        (worldY + noy) * BIOME_SCALE, seed ^ 0x9E3779B97F4A7C15L);

                double rv = riverValue(worldX, worldY, nox, noy, seed);
                if (terrainNoise > BEACH_MAX && terrainNoise < MOUNTAIN_TERRAIN_MIN) {
                    double proximity = Math.max(0.0, 1.0 - rv / VALLEY_CARVE_RANGE);
                    double carve     = proximity * proximity * VALLEY_CARVE_DEPTH; 
                    terrainNoise = Math.max(BEACH_MAX + 0.005, terrainNoise - carve);
                }

                String type;
                if (terrainNoise < WATER_MAX) {
                    type = "water";
                } else if (terrainNoise < BEACH_MAX) {
                    type = "desert";
                } else if (terrainNoise > MOUNTAIN_TERRAIN_MIN) {
                    type = (terrainNoise > SNOW_TERRAIN_MIN) ? "snow" : "mountain";
                } else if (biomeNoise > FOREST_BIOME_MIN) {
                    type = "forest";
                } else if (biomeNoise > GRASS_BIOME_MIN) {
                    type = "grass";
                } else {
                    type = "badland";
                }

                if (!type.equals("water")) {
                    double elevFactor     = riverElevFactor(terrainNoise);
                    double effectiveRiver = RIVER_THRESHOLD * elevFactor;
                    double effectiveBank  = BANK_THRESHOLD  * elevFactor;
                    if (rv < effectiveRiver) {
                        type = "water";
                    } else if (rv < effectiveBank && !type.equals("mountain") && !type.equals("snow") && !type.equals("desert") && !type.equals("badland")) {
                        type = "swamp";
                    }
                }

                types[tx + ty * size] = type;
            }
        }
        return types;
    }

    private static boolean spawnStruct(String terrain, int crateX, int crateY, long seed,
                                        List<int[]> objectCords, Set<String> structureTileSet,
                                        WorldDelta delta,
                                        int cx, int cy, String[] types, int nox, int noy) {

        long structSeed = seed ^ ((long) crateX * 0xBEEF1337L) ^ ((long) crateY * 0xDEAD4321L);
        int  variant    = (int)(Math.abs(structSeed) % 2);

        int[][] offsets;
        PlacedObject.Type[] structTypes;

        switch (terrain) {

            case "forest": {
                if (variant == 0) {

                    offsets     = new int[][]{ {-2,2},{-1,2},{0,2},{1,2},{2,2},
                                               {-2,1},{1,1},{2,1},
                                               {-2,0},{2,0},
                                               {-1,-1} };
                    structTypes = new PlacedObject.Type[]{
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                            PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.BARREL,    PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.CAMPFIRE,  PlacedObject.Type.LANTERN,
                        PlacedObject.Type.BARREL
                    };
                } else {

                    offsets     = new int[][]{ {-2,2},{-1,2},{0,2},{1,2},{2,2},
                                               {-2,1},{0,1},{2,1},
                                               {-2,0},{1,0},{2,0},
                                               {-1,-1},
                                               {1,-1} };
                    structTypes = new PlacedObject.Type[]{
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                            PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.BARREL,    PlacedObject.Type.WOOD_WALL, PlacedObject.Type.LANTERN,
                        PlacedObject.Type.CAMPFIRE,
                        PlacedObject.Type.BARREL
                    };
                }
                break;
            }

            case "grass": {
                if (variant == 0) {

                    offsets     = new int[][]{ {-2,2},{-1,2},{0,2},{1,2},{2,2},
                                               {-2,1},{2,1},
                                               {-2,0},{1,0},{2,0},
                                               {-1,-1},
                                               {0,-2} };
                    structTypes = new PlacedObject.Type[]{
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                            PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.LANTERN,   PlacedObject.Type.BARREL,    PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.CAMPFIRE,
                        PlacedObject.Type.WELL
                    };
                } else {

                    offsets     = new int[][]{ {-2,2},{-1,2},{0,2},{1,2},
                                               {-2,1},{1,1},
                                               {2,0},
                                               {-2,-1},{1,-1},{2,-1},
                                               {-1,-2} };
                    structTypes = new PlacedObject.Type[]{
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                            PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.BARREL,    PlacedObject.Type.LANTERN,   PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.CAMPFIRE
                    };
                }
                break;
            }

            case "desert": {
                if (variant == 0) {

                    offsets     = new int[][]{ {-1,2},{0,2},{1,2},
                                               {-2,1},{2,1},
                                               {-2,0},{2,0},
                                               {-1,-1},{1,-1},
                                               {0,-2} };
                    structTypes = new PlacedObject.Type[]{
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.BARREL,    PlacedObject.Type.BARREL,
                        PlacedObject.Type.FISHBOWL,  PlacedObject.Type.LANTERN,
                        PlacedObject.Type.BARREL
                    };
                } else {

                    offsets     = new int[][]{ {-2,2},{-1,2},{0,2},{1,2},{2,2},
                                               {-2,1},{2,1},
                                               {-2,0},{2,0},
                                               {-1,-1},{1,-1},
                                               {1,-2} };
                    structTypes = new PlacedObject.Type[]{
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                            PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.LANTERN,   PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.BARREL,    PlacedObject.Type.FISHBOWL,
                        PlacedObject.Type.BARREL
                    };
                }
                break;
            }

            case "mountain": {
                if (variant == 0) {

                    offsets     = new int[][]{ {-2,2},{-1,2},{0,2},{1,2},{2,2},
                                               {-2,1},{0,1},{2,1},
                                               {-2,0},{1,0},{2,0},
                                               {-1,-1},
                                               {-1,-2} };
                    structTypes = new PlacedObject.Type[]{
                        PlacedObject.Type.WOOD_WALL,    PlacedObject.Type.WOOD_WALL,  PlacedObject.Type.WOOD_WALL,
                            PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL,    PlacedObject.Type.FURNACE,    PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.BARREL,       PlacedObject.Type.CRUSHING_POT, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.LANTERN,
                        PlacedObject.Type.BARREL
                    };
                } else {

                    offsets     = new int[][]{ {-2,2},{-1,2},{0,2},{1,2},{2,2},
                                               {-2,1},{-1,1},{1,1},{2,1},
                                               {-2,0},{2,0},
                                               {-1,-1},{1,-1},
                                               {0,-2} };
                    structTypes = new PlacedObject.Type[]{
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                            PlacedObject.Type.WOOD_WALL, PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.WOOD_WALL, PlacedObject.Type.CAST,    PlacedObject.Type.FURNACE,
                            PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.BARREL,    PlacedObject.Type.WOOD_WALL,
                        PlacedObject.Type.LANTERN,   PlacedObject.Type.BARREL,
                        PlacedObject.Type.CRUSHING_POT
                    };
                }
                break;
            }

            default: return false;
        }

        Set<String> footprint = new HashSet<>();
        footprint.add(crateX + "," + crateY);
        for (int[] offset : offsets) {
            footprint.add((crateX + offset[0]) + "," + (crateY + offset[1]));
        }

        objectCords.removeIf(obj -> footprint.contains(obj[0] + "," + obj[1]));

        structureTileSet.addAll(footprint);

        for (int i = 0; i < offsets.length; i++) {
            int x = crateX + offsets[i][0];
            int y = crateY + offsets[i][1];
            if (!delta.hasPlaced(x, y)) {
                delta.addPlaced(x, y, structTypes[i]);
            }

        }
        return true;
    }

    private static boolean footprintOverlapsRiver(int crateX, int crateY,
                                                   int[][] offsets,
                                                   int cx, int cy, String[] types,
                                                   int nox, int noy, long seed) {
        int size = Main.CHUNK_SIZE;
        int[][] all = new int[offsets.length + 1][2];
        all[0] = new int[]{0, 0};
        System.arraycopy(offsets, 0, all, 1, offsets.length);

        for (int[] off : all) {
            int wx = crateX + off[0];
            int wy = crateY + off[1];
            int lx = wx - cx * size;
            int ly = wy - cy * size;

            String t;
            if (lx >= 0 && lx < size && ly >= 0 && ly < size) {
                t = types[lx + ly * size];
            } else {
                double terrainNoise = smoothedTerrainNoise(wx, wy, nox, noy, seed);
                double biomeNoise = PerlinNoise.noise(
                        (wx + nox) * BIOME_SCALE,   (wy + noy) * BIOME_SCALE,
                        seed ^ 0x9E3779B97F4A7C15L);

                double rvOob = riverValue(wx, wy, nox, noy, seed);
                if (terrainNoise > BEACH_MAX && terrainNoise < MOUNTAIN_TERRAIN_MIN) {
                    double prox = Math.max(0.0, 1.0 - rvOob / VALLEY_CARVE_RANGE);
                    terrainNoise = Math.max(BEACH_MAX + 0.005, terrainNoise - prox * prox * VALLEY_CARVE_DEPTH);
                }

                if      (terrainNoise < WATER_MAX)                                  t = "water";
                else if (terrainNoise < BEACH_MAX)                                  t = "desert";
                else if (terrainNoise > MOUNTAIN_TERRAIN_MIN)                       t = "mountain";
                else if (biomeNoise   > FOREST_BIOME_MIN)                           t = "forest";
                else                                                                t = "grass";

                if (!t.equals("water") && !t.equals("mountain") && !t.equals("snow")) {
                    double elevFactor = riverElevFactor(terrainNoise);
                    if      (rvOob < RIVER_THRESHOLD * elevFactor) t = "water";
                    else if (rvOob < BANK_THRESHOLD  * elevFactor) t = "swamp";
                }
            }

            if ("swamp".equals(t)) return true;
        }
        return false;
    }

    public static boolean isLandAt(int worldTileX, int worldTileY, int nox, int noy, long seed) {
        double terrainNoise = smoothedTerrainNoise(worldTileX, worldTileY, nox, noy, seed);
        if (terrainNoise < WATER_MAX) return false;
        double rv = riverValue(worldTileX, worldTileY, nox, noy, seed);
        if (terrainNoise > BEACH_MAX && terrainNoise < MOUNTAIN_TERRAIN_MIN) {
            double prox = Math.max(0.0, 1.0 - rv / VALLEY_CARVE_RANGE);
            terrainNoise = Math.max(BEACH_MAX + 0.005, terrainNoise - prox * prox * VALLEY_CARVE_DEPTH);
        }
        return rv >= RIVER_THRESHOLD * riverElevFactor(terrainNoise);
    }


    private static List<int[]> computeObjects(int cx, int cy, long seed, String[] types,
                                               WorldDelta delta, int nox, int noy) {
        List<int[]> objects = new ArrayList<>();
        int         size    = Main.CHUNK_SIZE;

        Set<String> structureTileSet = ConcurrentHashMap.newKeySet();

        for (int tx = 0; tx < size; tx++) {
            for (int ty = 0; ty < size; ty++) {
                int    worldX  = cx * size + tx;
                int    worldY  = cy * size + ty;
                String terrain = types[tx + ty * size];

                long   tileSeed = seed ^ ((long) worldX * 374761393L) ^ ((long) worldY * 668265263L);
                Random rng      = new Random(tileSeed);

                if (structureTileSet.contains(worldX + "," + worldY)) continue;

                WorldObject.Type objType = null;

                if (terrain.equals("forest") && rng.nextFloat() < 0.002f){
                    objType = WorldObject.Type.ROCK;
                }

                else if (terrain.equals("forest") && rng.nextFloat() < 0.25f) {
                    objType = WorldObject.Type.GRASS;}

                else if (terrain.equals("forest") && rng.nextFloat() < 0.35f) {
                    objType = WorldObject.Type.TREE;}

                if (terrain.equals("desert") && rng.nextFloat() < 0.01f) {
                    objType = WorldObject.Type.SAND_CASTLE;}

                else if (terrain.equals("desert") && rng.nextFloat() < 0.02f) {
                    objType = WorldObject.Type.PALMTREE;}

                else if (terrain.equals("desert") && rng.nextFloat() < 0.05f) {
                    objType = WorldObject.Type.SAND_PILE;}

                else if (terrain.equals("desert") && rng.nextFloat() < 0.15f) {
                    objType = WorldObject.Type.SEASHELLS;}

                if (terrain.equals("grass") && rng.nextFloat() < 0.02f) {
                    objType = WorldObject.Type.BUSH;}

                else if (terrain.equals("grass") && rng.nextFloat() < 0.35f) {
                    objType = WorldObject.Type.GRASS;}

                if (terrain.equals("mountain") && rng.nextFloat() < 0.0007){
                    objType = WorldObject.Type.CAVE;
                }

                else if (terrain.equals("mountain") && rng.nextFloat() < 0.05f){
                    objType = WorldObject.Type.RUBBLE;
                }

                else if (terrain.equals("mountain") && rng.nextFloat() < 0.15f){
                    objType = WorldObject.Type.ROCK;
                }

                if (terrain.equals("snow") && rng.nextFloat() < 0.02f) {
                    objType = WorldObject.Type.ROCK;
                }

                else if (terrain.equals("snow") && rng.nextFloat() < 0.1f) {
                    objType = WorldObject.Type.SNOWITE;
                }

                if (terrain.equals("badland") && rng.nextFloat() < 0.01f) {
                    objType = WorldObject.Type.ROCK;
                }
                else if (terrain.equals("badland") && rng.nextFloat() < 0.02f) {
                    objType = WorldObject.Type.CACTI;
                }
                else if (terrain.equals("badland") && rng.nextFloat() < 0.005f) {
                    objType = WorldObject.Type.RAW_ORE;
                }
                else if (terrain.equals("badland") && rng.nextFloat() < 0.02f) {
                    objType = WorldObject.Type.DEAD_BUSH;
                }

                if (terrain.equals("swamp") && rng.nextFloat() < 0.1f) {
                    objType = WorldObject.Type.QUALE;
                }



                if (objType != null && !delta.isRemoved(worldX, worldY) && !delta.hasPlaced(worldX, worldY)) {
                    int spriteIndex = ObjectSpriteCache.resolveSpriteIndex(objType, rng);
                    objects.add(new int[]{ worldX, worldY, objType.ordinal(), spriteIndex });
                }

                if (objType == null
                        && !delta.isRemoved(worldX, worldY)
                        && !delta.hasPlaced(worldX, worldY)) {

                    float crateChance;
                    String crateTable;
                    PlacedObject.Type crateType;

                    switch (terrain) {
                        case "forest":   crateChance = 0.0008f; crateTable = "forest_crate";   crateType = PlacedObject.Type.FOREST_CRATE;   break;
                        case "grass":    crateChance = 0.0006f; crateTable = "grass_crate";    crateType = PlacedObject.Type.FOREST_CRATE;   break;
                        case "desert":   crateChance = 0.0005f; crateTable = "desert_crate";   crateType = PlacedObject.Type.DESERT_CRATE;   break;
                        case "mountain": crateChance = 0.0010f; crateTable = "mountain_crate"; crateType = PlacedObject.Type.MOUNTAIN_CRATE; break;
                        case "snow":     crateChance = 0.0008f; crateTable = "mountain_crate"; crateType = PlacedObject.Type.MOUNTAIN_CRATE; break;
                        case "swamp":    crateChance = 0.0004f; crateTable = "grass_crate";    crateType = PlacedObject.Type.FOREST_CRATE;   break;
                        default:          crateChance = 0f;      crateTable = null;              crateType = null;                             break;
                    }

                    if (crateType != null && rng.nextFloat() < crateChance) {
                        String structTerrain = terrain.equals("swamp") ? "grass"
                                             : terrain.equals("snow")  ? "mountain"
                                             : terrain;

                        boolean placed = spawnStruct(structTerrain, worldX, worldY, seed,
                                objects, structureTileSet, delta,
                                cx, cy, types, nox, noy);
                        if (placed) {
                            WorldDelta.PlacedRecord crateRecord = delta.addPlaced(worldX, worldY, crateType);
                            if (crateRecord != null) {
                                ItemStack[] contents = LootTableGenerator.generateContents(crateTable);
                                delta.updateCrateContents(worldX, worldY, contents);
                            }
                        }
                    }
                }
            }
        }

        objects.removeIf(obj -> delta.hasPlaced(obj[0], obj[1]));

        return objects;
    }
}