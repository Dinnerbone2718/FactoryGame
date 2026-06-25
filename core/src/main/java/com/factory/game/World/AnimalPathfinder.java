package com.factory.game.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.factory.game.Main;

public final class AnimalPathfinder {

    private static final int MAX_NODES = 768;

    private static final int[][] DIRS = {
        { 1, 0}, {-1, 0}, { 0, 1}, { 0,-1}
    };

    private static final class Node implements Comparable<Node> {
        final int x, y;
        float g      = Float.MAX_VALUE;
        float f      = Float.MAX_VALUE;
        Node  parent = null;

        Node(int x, int y) { this.x = x; this.y = y; }

        @Override
        public int compareTo(Node o) { return Float.compare(f, o.f); }
    }

    public static List<float[]> findPath(
            float startWorldX, float startWorldY,
            float goalWorldX,  float goalWorldY,
            Animal.Type        animalType,
            Map<String, Chunk> loadedChunks,
            WorldObject.Type   passableObjectType) {

        int sx = worldToTile(startWorldX);
        int sy = worldToTile(startWorldY);
        int gx = worldToTile(goalWorldX);
        int gy = worldToTile(goalWorldY);

        if (sx == gx && sy == gy) return Collections.emptyList();

        Set<Long> blocked = buildBlockedSet(loadedChunks, passableObjectType);

        int extraX = animalType.pathExtraX;
        int extraY = animalType.pathExtraY;

        Map<Long, Node>     nodes  = new HashMap<>();
        PriorityQueue<Node> open   = new PriorityQueue<>();
        Set<Long>           closed = new HashSet<>();

        Node start = getOrCreate(nodes, sx, sy);
        start.g = 0f;
        start.f = h(sx, sy, gx, gy);
        open.add(start);

        while (!open.isEmpty() && closed.size() < MAX_NODES) {
            Node cur = open.poll();
            long ck  = encodeKey(cur.x, cur.y);
            if (closed.contains(ck)) continue;
            closed.add(ck);

            if (cur.x == gx && cur.y == gy) return buildPath(cur);

            for (int[] d : DIRS) {
                int  nx = cur.x + d[0];
                int  ny = cur.y + d[1];
                long nk = encodeKey(nx, ny);
                if (closed.contains(nk)) continue;

                boolean isGoal = (nx == gx && ny == gy);
                if (!isGoal && footprintBlocked(nx, ny, extraX, extraY, blocked)) continue;

                float tentG = cur.g + 1f;
                Node  nb    = getOrCreate(nodes, nx, ny);
                if (tentG < nb.g) {
                    nb.g      = tentG;
                    nb.f      = tentG + h(nx, ny, gx, gy);
                    nb.parent = cur;
                    open.add(nb);
                }
            }
        }

        return null;
    }

    private static Set<Long> buildBlockedSet(Map<String, Chunk> chunks,
                                              WorldObject.Type passableObjectType) {
        Set<Long> blocked = new HashSet<>();

        for (Chunk chunk : chunks.values()) {

            if (chunk.containsWater()) {
                for (Ground_Tile tile : chunk.getTiles()) {
                    if ("water".equals(tile.tileName))
                        blocked.add(encodeKey(tile.getX(), tile.getY()));
                }
            }

            for (WorldObject obj : chunk.getObjects()) {

                if (passableObjectType != null && obj.type == passableObjectType) continue;

                ObjectSpriteCache.SpriteConfig cfg = ObjectSpriteCache.getConfig(obj.type);
                if (cfg == null || !cfg.hasHitbox()) continue;
                for (int dx = 0; dx < cfg.tileWidth;  dx++)
                for (int dy = 0; dy < cfg.tileHeight; dy++)
                    blocked.add(encodeKey(obj.getX() + dx, obj.getY() + dy));
            }


            for (PlacedObject obj : chunk.getPlacedObjects()) {
                PlacedObjectCache.SpriteConfig cfg = PlacedObjectCache.getConfig(obj.type);
                if (cfg == null || !cfg.solid) continue;
                for (int dx = 0; dx < cfg.tileWidth;  dx++)
                for (int dy = 0; dy < cfg.tileHeight; dy++)
                    blocked.add(encodeKey(obj.getX() + dx, obj.getY() + dy));
            }
        }
        return blocked;
    }

    private static boolean footprintBlocked(int tx, int ty,
                                             int extraX, int extraY,
                                             Set<Long> blocked) {
        for (int dx = 0; dx <= extraX; dx++)
        for (int dy = 0; dy <= extraY; dy++)
            if (blocked.contains(encodeKey(tx + dx, ty + dy))) return true;
        return false;
    }

    private static List<float[]> buildPath(Node goal) {
        List<float[]> path = new ArrayList<>();
        for (Node n = goal; n != null; n = n.parent)
            path.add(new float[]{
                (n.x + 0.5f) * Main.TILE_SCALE,
                (n.y + 0.5f) * Main.TILE_SCALE
            });
        Collections.reverse(path);
        return path;
    }

    private static float h(int ax, int ay, int bx, int by) {
        return Math.abs(ax - bx) + Math.abs(ay - by);
    }

    private static int worldToTile(float worldPx) {
        return (int) Math.floor(worldPx / Main.TILE_SCALE);
    }

    private static long encodeKey(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFF_FFFFL);
    }

    private static Node getOrCreate(Map<Long, Node> nodes, int x, int y) {
        long k = encodeKey(x, y);
        Node n = nodes.get(k);
        if (n == null) { n = new Node(x, y); nodes.put(k, n); }
        return n;
    }

    private AnimalPathfinder() {}
}