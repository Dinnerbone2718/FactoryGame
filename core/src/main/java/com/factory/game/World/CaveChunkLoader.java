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

import com.factory.game.Main;
import com.factory.game.PerlinNoise;

public class CaveChunkLoader {

    private final ExecutorService                                 pool;
    private final Set<String>                                    inFlight  = ConcurrentHashMap.newKeySet();
    public  final ConcurrentLinkedQueue<ChunkLoader.RawChunkData> completed = new ConcurrentLinkedQueue<>();


    public CaveChunkLoader() {
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        pool = Executors.newFixedThreadPool(threads);
    }

    public void request(int cx, int cy, int noiseOffsetX, int noiseOffsetY, long seed,
                        WorldDelta delta) {
        String key = Chunk.key(cx, cy);
        if (!inFlight.add(key)) return;
        pool.submit(() -> {
            try {
                String[]    surface   = computeSurfaceBiomes(cx, cy, noiseOffsetX, noiseOffsetY, seed);
                String[]    caveTypes = computeCaveTypes(surface);
                List<int[]> objects   = computeCaveObjects(cx, cy, seed, caveTypes, surface, delta);
                completed.add(new ChunkLoader.RawChunkData(cx, cy, caveTypes, objects));
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
    private static final double FOREST_BIOME_MIN     = 0.17;
    private static final double GRASS_BIOME_MIN      = -0.15;

    private static final double RIVER_SCALE          = 0.006;
    private static final double RIVER_WARP_SCALE     = 0.0015;
    private static final double RIVER_WARP_AMP       = 60.0;
    private static final double RIVER_THRESHOLD      = 0.05;
    private static final double VALLEY_CARVE_RANGE   = 0.18;
    private static final double VALLEY_CARVE_DEPTH   = 0.07;

    private static final int    SMOOTH_RADIUS        = 2;

    private static final long   RIVER_SEED           = 0xCAFEBABE12345678L;
    private static final long   WARP_SEED_X          = 0xABCDEF1234567890L;
    private static final long   WARP_SEED_Y          = 0x0FEDCBA987654321L;

    private static final float  CAVE_CHANCE          = 0.0007f;

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

    private static double riverValue(int worldX, int worldY, int nox, int noy, long seed) {
        long   rs    = seed ^ RIVER_SEED;
        double px    = (worldX + nox) * RIVER_WARP_SCALE;
        double py    = (worldY + noy) * RIVER_WARP_SCALE;
        double warpX = PerlinNoise.noise(px,       py,       seed ^ WARP_SEED_X) * RIVER_WARP_AMP;
        double warpY = PerlinNoise.noise(px + 5.2, py + 1.3, seed ^ WARP_SEED_Y) * RIVER_WARP_AMP;
        return Math.abs(PerlinNoise.noise(
                (worldX + nox + warpX) * RIVER_SCALE,
                (worldY + noy + warpY) * RIVER_SCALE,
                rs));
    }

    private static double riverElevFactor(double terrainNoise) {
        if (terrainNoise >= MOUNTAIN_TERRAIN_MIN) return 0.0;
        if (terrainNoise <= BEACH_MAX)            return 0.0;
        double t = (terrainNoise - BEACH_MAX) / (MOUNTAIN_TERRAIN_MIN - BEACH_MAX);
        return 1.0 - t * t;
    }

    private static String surfaceBiomeAt(int worldX, int worldY, int nox, int noy, long seed) {
        double terrainNoise = smoothedTerrainNoise(worldX, worldY, nox, noy, seed);
        double biomeNoise   = PerlinNoise.noise(
                (worldX + nox) * BIOME_SCALE,
                (worldY + noy) * BIOME_SCALE,
                seed ^ 0x9E3779B97F4A7C15L);
        double rv = riverValue(worldX, worldY, nox, noy, seed);
        if (terrainNoise > BEACH_MAX && terrainNoise < MOUNTAIN_TERRAIN_MIN) {
            double proximity = Math.max(0.0, 1.0 - rv / VALLEY_CARVE_RANGE);
            double carve     = proximity * proximity * VALLEY_CARVE_DEPTH;
            terrainNoise     = Math.max(BEACH_MAX + 0.005, terrainNoise - carve);
        }
        String type;
        if      (terrainNoise < WATER_MAX)          type = "water";
        else if (terrainNoise < BEACH_MAX)           type = "desert";
        else if (terrainNoise > MOUNTAIN_TERRAIN_MIN) type = (terrainNoise > SNOW_TERRAIN_MIN) ? "snow" : "mountain";
        else if (biomeNoise   > FOREST_BIOME_MIN)   type = "forest";
        else if (biomeNoise   > GRASS_BIOME_MIN)    type = "grass";
        else                                         type = "badland";
        if (!type.equals("water")) {
            if (rv < RIVER_THRESHOLD * riverElevFactor(terrainNoise)) type = "water";
        }
        return type;
    }

    private static String[] computeSurfaceBiomes(int cx, int cy, int nox, int noy, long seed) {
        int      size   = Main.CHUNK_SIZE;
        String[] biomes = new String[size * size];
        for (int tx = 0; tx < size; tx++) {
            for (int ty = 0; ty < size; ty++) {
                int worldX = cx * size + tx;
                int worldY = cy * size + ty;
                biomes[tx + ty * size] = surfaceBiomeAt(worldX, worldY, nox, noy, seed);
            }
        }
        return biomes;
    }

    private static String[] computeCaveTypes(String[] surfaceBiomes) {
        String[] types = new String[surfaceBiomes.length];
        for (int i = 0; i < surfaceBiomes.length; i++) {
            types[i] = surfaceBiomes[i].equals("water") ? "cave" : "mountain";
        }
        return types;
    }

    private static List<int[]> computeCaveObjects(int cx, int cy, long seed,
                                                   String[] caveTypes, String[] surfaceBiomes,
                                                   WorldDelta delta) {
        List<int[]>  objects  = new ArrayList<>();
        List<int[]>  huts     = new ArrayList<>();
        List<int[]>  pending  = new ArrayList<>();
        Set<String>  occupied = new HashSet<>();
        int          size     = Main.CHUNK_SIZE;

        for (int tx = 0; tx < size; tx++) {
            for (int ty = 0; ty < size; ty++) {
                int    worldX   = cx * size + tx;
                int    worldY   = cy * size + ty;
                String caveType = caveTypes[tx + ty * size];

                if (caveType.equals("cave")) continue;

                String surface  = surfaceBiomes[tx + ty * size];
                long   tileSeed = seed ^ ((long) worldX * 374761393L) ^ ((long) worldY * 668265263L);

                if (surface.equals("mountain") && new Random(tileSeed).nextFloat() < CAVE_CHANCE) {
                    if (!delta.isRemoved(worldX, worldY)) {
                        objects.add(new int[]{
                            worldX, worldY,
                            WorldObject.Type.CAVE_EXIT.ordinal(),
                            0
                        });
                    }
                    continue;
                }

                Random           rng     = new Random(tileSeed);
                WorldObject.Type objType = null;
                if      (rng.nextFloat() < 0.05f)   objType = WorldObject.Type.RAW_ORE;
                else if (rng.nextFloat() < 0.01f)   objType = WorldObject.Type.ROCK;
                else if (rng.nextFloat() < 0.005f)  objType = WorldObject.Type.LANTERN;
                else if (rng.nextFloat() < 0.0001f)  objType = WorldObject.Type.GOBLINO_HUT;

                if (objType == null || delta.isRemoved(worldX, worldY)) continue;

                int spriteIndex = ObjectSpriteCache.resolveSpriteIndex(objType, rng);

                if (objType == WorldObject.Type.GOBLINO_HUT) {
                    huts.add(new int[]{worldX, worldY, objType.ordinal(), spriteIndex});
                    occupied.add(worldX       + "," + worldY);   
                    occupied.add((worldX + 1) + "," + worldY); 
                    occupied.add((worldX - 1) + "," + worldY);   
                    occupied.add((worldX + 2) + "," + worldY);   
                } else {
                    pending.add(new int[]{worldX, worldY, objType.ordinal(), spriteIndex});
                }
            }
        }

        for (int[] hut : huts) {
            objects.add(hut);
            int hx = hut[0];
            int hy = hut[1];

            int leftX = hx - 1;
            if (!delta.isRemoved(leftX, hy)) {
                Random lr = new Random(seed ^ ((long) leftX * 374761393L) ^ ((long) hy * 668265263L));
                objects.add(new int[]{
                    leftX, hy,
                    WorldObject.Type.LANTERN.ordinal(),
                    ObjectSpriteCache.resolveSpriteIndex(WorldObject.Type.LANTERN, lr)
                });
            }

            int rightX = hx + 2;
            if (!delta.isRemoved(rightX, hy)) {
                Random rr = new Random(seed ^ ((long) rightX * 374761393L) ^ ((long) hy * 668265263L));
                objects.add(new int[]{
                    rightX, hy,
                    WorldObject.Type.LANTERN.ordinal(),
                    ObjectSpriteCache.resolveSpriteIndex(WorldObject.Type.LANTERN, rr)
                });
            }
        }

        for (int[] obj : pending) {
            if (!occupied.contains(obj[0] + "," + obj[1])) {
                objects.add(obj);
            }
        }

        return objects;
    }
}