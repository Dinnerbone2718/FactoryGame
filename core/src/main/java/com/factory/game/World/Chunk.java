package com.factory.game.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.World;
import com.factory.game.Camera;
import com.factory.game.Main;
import com.factory.game.Renderer.LightRenderer;

public class Chunk implements Comparable<Chunk> {

    private final int chunkX, chunkY;
    private final List<Ground_Tile>  tiles         = new ArrayList<>();
    private final List<WorldObject>  objects       = new ArrayList<>();
    private final List<PlacedObject> placedObjects = new ArrayList<>();

    private boolean containsWater = false;

    private final EnumSet<WorldObject.Type> presentObjectTypes =
            EnumSet.noneOf(WorldObject.Type.class);

    public Chunk(int chunkX, int chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        
    }


    public static Chunk buildFromTypes(int chunkX, int chunkY,
                                       String[] types, List<int[]> rawObjects) {
        return buildFromTypes(chunkX, chunkY, types, rawObjects, null);
    }

    public static Chunk buildFromTypes(int chunkX, int chunkY,
                                       String[] types, List<int[]> rawObjects,
                                       WorldDelta delta) {
        Chunk chunk = new Chunk(chunkX, chunkY);
        int   size  = Main.CHUNK_SIZE;

        for (int tx = 0; tx < size; tx++) {
            for (int ty = 0; ty < size; ty++) {
                int    worldX = chunkX * size + tx;
                int    worldY = chunkY * size + ty;
                String type   = types[tx + ty * size];
                chunk.addTile(new Ground_Tile(type, worldX, worldY));
            }
        }

        for (int[] o : rawObjects) {

            if (delta != null && delta.hasPlaced(o[0], o[1])) continue;

            WorldObject.Type type        = WorldObject.Type.values()[o[2]];
            int              spriteIndex = o[3];
            WorldObject      obj         = new WorldObject(type, o[0], o[1], spriteIndex);
            chunk.objects.add(obj);
            chunk.presentObjectTypes.add(type);
        }

        return chunk;
    }


    public void attachBodies(World world, LightRenderer lightRenderer) {
        for (WorldObject obj : objects) {
            obj.attachShadowBody(world);
            obj.attachLight(lightRenderer);
        }
        for (PlacedObject obj : placedObjects) {
            obj.attachLight(lightRenderer);
        }
    }

    public void destroyBodies(World world, LightRenderer lightRenderer) {
        for (WorldObject obj : objects) {
            obj.destroyShadowBody(world);
            obj.detachLight(lightRenderer);
        }
        for (PlacedObject obj : placedObjects) {
            obj.detachLight(lightRenderer);
        }
    }


    public boolean removeObject(WorldObject obj) {
        return objects.remove(obj);
    }

    public void addObject(WorldObject obj) {
        objects.add(obj);
        presentObjectTypes.add(obj.type);
    }

    public void addTile(Ground_Tile tile) {
        tiles.add(tile);
        if ("water".equals(tile.tileName)) containsWater = true;
    }

    public List<Ground_Tile> getTiles()   { return tiles;         }
    public List<WorldObject> getObjects() { return objects;       }
    public boolean containsWater()        { return containsWater; }


    public boolean containsObjectType(WorldObject.Type type) {
        return presentObjectTypes.contains(type);
    }

    public void reupdate(Map<String, Ground_Tile> worldLookup) {
        for (Ground_Tile tile : tiles) tile.reupdate(worldLookup);
    }

    public void addPlacedObject(PlacedObject obj, LightRenderer lightRenderer) {

        objects.removeIf(wo -> wo.getX() == obj.getX() && wo.getY() == obj.getY());
        placedObjects.add(obj);
        obj.attachLight(lightRenderer);
    }

    public boolean removePlacedObject(PlacedObject obj, LightRenderer lightRenderer) {
        obj.detachLight(lightRenderer);
        return placedObjects.remove(obj);
    }

    public List<PlacedObject> getPlacedObjects() { return placedObjects; }



    public List<PlacedObject> getVisiblePlacedObjects(Camera cam) {
        if (!isVisible(cam)) return Collections.emptyList();

        List<PlacedObject> floors = new ArrayList<>();
        List<PlacedObject> pipes  = new ArrayList<>();
        List<PlacedObject> others = new ArrayList<>();

        for (PlacedObject obj : placedObjects) {
            if (obj.isFloor()) {
                floors.add(obj);
            } else if (obj.isPipe()) {
                if (PlacedObject.PIPES_VISIBLE) {
                    pipes.add(obj);
                } else if ((obj.type == PlacedObject.Type.ITEM_PIPE || obj.type == PlacedObject.Type.FILTER_ITEM_PIPE) && PlacedObject.ITEM_PIPES_VISIBLE) {
                    pipes.add(obj);
                }
            } else {
                others.add(obj);
            }
        }

        floors.addAll(pipes);
        floors.addAll(others);
        return floors;
    }


    public boolean isVisible(Camera cam) {
        int   originX = chunkX * Main.CHUNK_SIZE * Main.TILE_SCALE;
        int   originY = chunkY * Main.CHUNK_SIZE * Main.TILE_SCALE;
        int   size    = Main.CHUNK_SIZE * Main.TILE_SCALE;
        float screenX = originX + cam.cameraX;
        float screenY = originY + cam.cameraY;
        return !(screenX + size < 0 || screenX > cam.VIRTUAL_WIDTH
              || screenY + size < 0 || screenY > cam.VIRTUAL_HEIGHT);
    }

    public void drawTiles(Batch batch, Camera cam) {
        if (!isVisible(cam)) return;
        for (Ground_Tile tile : tiles) tile.draw(batch, cam);
    }

    public List<WorldObject> getVisibleObjects(Camera cam) {
        if (!isVisible(cam)) return Collections.emptyList();
        return objects;
    }


    public String key()                      { return key(chunkX, chunkY); }
    public static String key(int cx, int cy) { return cx + "," + cy;       }
    public int getChunkX()                   { return chunkX;               }
    public int getChunkY()                   { return chunkY;               }

    @Override
    public int compareTo(Chunk other) {
        return Integer.compare(this.chunkX + this.chunkY,
                               other.getChunkX() + other.getChunkY());
    }
}