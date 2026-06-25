package com.factory.game.World;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.factory.game.Main;

public final class WorldSearch {

    private static final int[][] DIRS = {
        { 1, 0}, {-1, 0}, { 0, 1}, { 0,-1},
        { 1, 1}, { 1,-1}, {-1, 1}, {-1,-1}
    };


    private static int pixToChunk(float worldPix) {
        return Math.floorDiv((int) worldPix, Main.CHUNK_SIZE * Main.TILE_SCALE);
    }


    private static float chunkMinDist(float wx, float wy, int cx, int cy) {
        float chunkPx = (float)(Main.CHUNK_SIZE * Main.TILE_SCALE);

        float minX = cx * chunkPx;
        float maxX = minX + chunkPx;
        float minY = cy * chunkPx;
        float maxY = minY + chunkPx;

        float dx = Math.max(0f, Math.max(minX - wx, wx - maxX));
        float dy = Math.max(0f, Math.max(minY - wy, wy - maxY));
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static float dist2(float ax, float ay, float bx, float by) {
        float dx = ax - bx, dy = ay - by;
        return dx * dx + dy * dy;
    }


    public static float[] findNearestWaterTile(float worldX, float worldY,
                                                Map<String, Chunk> loadedChunks,
                                                float maxDistPx) {
        int startCX = pixToChunk(worldX);
        int startCY = pixToChunk(worldY);

        PriorityQueue<int[]> pq = new PriorityQueue<>(
            Comparator.comparingDouble(c -> chunkMinDist(worldX, worldY, c[0], c[1])));
        Set<String> visited = new HashSet<>();

        pq.offer(new int[]{startCX, startCY});
        visited.add(Chunk.key(startCX, startCY));

        float   bestDist2 = maxDistPx * maxDistPx;
        float[] result    = null;

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int   cx   = curr[0];
            int   cy   = curr[1];

            float minD = chunkMinDist(worldX, worldY, cx, cy);

            if (minD * minD >= bestDist2) break;

            Chunk chunk = loadedChunks.get(Chunk.key(cx, cy));

            if (chunk != null && chunk.containsWater()) {
                for (Ground_Tile tile : chunk.getTiles()) {
                    if (!"water".equals(tile.tileName)) continue;

                    float px = (tile.getX() + 0.5f) * Main.TILE_SCALE;
                    float py = (tile.getY() + 0.5f) * Main.TILE_SCALE;

                    float d2 = dist2(worldX, worldY, px, py);
                    if (d2 < bestDist2) {
                        bestDist2 = d2;
                        result    = new float[]{px, py};
                    }
                }
            }

            for (int[] dir : DIRS) {
                int    nx = cx + dir[0];
                int    ny = cy + dir[1];
                String nk = Chunk.key(nx, ny);
                if (!visited.contains(nk)
                        && chunkMinDist(worldX, worldY, nx, ny) < maxDistPx) {
                    visited.add(nk);
                    pq.offer(new int[]{nx, ny});
                }
            }
        }

        return result;
    }

    public static WorldObject findNearestWorldObject(float worldX, float worldY,
                                                      WorldObject.Type type,
                                                      Map<String, Chunk> loadedChunks,
                                                      float maxDistPx) {
        int startCX = pixToChunk(worldX);
        int startCY = pixToChunk(worldY);

        PriorityQueue<int[]> pq = new PriorityQueue<>(
            Comparator.comparingDouble(c -> chunkMinDist(worldX, worldY, c[0], c[1])));
        Set<String> visited = new HashSet<>();

        pq.offer(new int[]{startCX, startCY});
        visited.add(Chunk.key(startCX, startCY));

        float       bestDist2 = maxDistPx * maxDistPx;
        WorldObject result    = null;

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int   cx   = curr[0];
            int   cy   = curr[1];

            float minD = chunkMinDist(worldX, worldY, cx, cy);
            if (minD * minD >= bestDist2) break;

            Chunk chunk = loadedChunks.get(Chunk.key(cx, cy));

            if (chunk != null && chunk.containsObjectType(type)) {
                for (WorldObject obj : chunk.getObjects()) {
                    if (obj.type != type) continue;

                    float px = (obj.getX() + 0.5f) * Main.TILE_SCALE;
                    float py = (obj.getY() + 0.5f) * Main.TILE_SCALE;

                    float d2 = dist2(worldX, worldY, px, py);
                    if (d2 < bestDist2) {
                        bestDist2 = d2;
                        result    = obj;
                    }
                }
            }

            for (int[] dir : DIRS) {
                int    nx = cx + dir[0];
                int    ny = cy + dir[1];
                String nk = Chunk.key(nx, ny);
                if (!visited.contains(nk)
                        && chunkMinDist(worldX, worldY, nx, ny) < maxDistPx) {
                    visited.add(nk);
                    pq.offer(new int[]{nx, ny});
                }
            }
        }

        return result;
    }

    private WorldSearch() {}
}