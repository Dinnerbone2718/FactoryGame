package com.factory.game.World;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.factory.game.Camera;
import com.factory.game.Main;

public class Ground_Tile {

    public final String tileName;
    private final int   x, y;

    private final boolean autotile;
    private       int     tileIndex;

    private int     waterTileDepth = 0;
    private boolean waterLeft, waterRight, waterBottom, waterTop;


    public Ground_Tile(String name, int x, int y) {
        this.tileName  = name;
        this.x         = x;
        this.y         = y;
        this.autotile  = !name.equals("default");
        this.tileIndex = 0;
    }


    public void draw(Batch batch, Camera cam) {
        float drawX = x * Main.TILE_SCALE + cam.cameraX;
        float drawY = y * Main.TILE_SCALE + cam.cameraY;

        if (drawX + Main.TILE_SCALE < 0 || drawX > cam.VIRTUAL_WIDTH)  return;
        if (drawY + Main.TILE_SCALE < 0 || drawY > cam.VIRTUAL_HEIGHT) return;

        TextureRegion region       = TilesetCache.getRegion(tileName, tileIndex);
        TextureRegion normalRegion = TilesetCache.getNormalRegion(tileName, tileIndex);
        ShaderProgram shader = (ShaderProgram) batch.getShader();

        batch.flush();
        if (normalRegion != null) {
            normalRegion.getTexture().bind(1);
            shader.setUniformi("u_hasNormalMap", 1);
        } else {
            ObjectSpriteCache.flatNormal.bind(1);
            shader.setUniformi("u_hasNormalMap", 0);
        }

        boolean isWater = tileName.equals("water");
        shader.setUniformi("u_isWater", isWater ? 1 : 0);
        if (isWater && region != null) {
            shader.setUniformf("u_waterPixelSize",
                1f / region.getTexture().getWidth(),
                1f / region.getTexture().getHeight());
            shader.setUniformf("u_waterDepth", (float) waterTileDepth);
            shader.setUniformf("u_waterEdges",
                waterLeft   ? 1f : 0f,
                waterRight  ? 1f : 0f,
                waterBottom ? 1f : 0f,
                waterTop    ? 1f : 0f);

        }

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        batch.draw(region, drawX, drawY, Main.TILE_SCALE, Main.TILE_SCALE);
    }


    public void reupdate(Map<String, Ground_Tile> lookup) {
        if (!autotile) return;

        boolean top    = isSame(lookup, x,     y + 1);
        boolean bottom = isSame(lookup, x,     y - 1);
        boolean left   = isSame(lookup, x - 1, y    );
        boolean right  = isSame(lookup, x + 1, y    );
        boolean tl     = isSame(lookup, x - 1, y + 1);
        boolean tr     = isSame(lookup, x + 1, y + 1);
        boolean bl     = isSame(lookup, x - 1, y - 1);
        boolean br     = isSame(lookup, x + 1, y - 1);

        tileIndex = pickIndex(top, bottom, left, right, tl, tr, bl, br);

        if (tileName.equals("water")) {
            waterLeft   = left;
            waterRight  = right;
            waterBottom = bottom;
            waterTop    = top;
        }
    }

    private boolean isSame(Map<String, Ground_Tile> lookup, int tx, int ty) {
        Ground_Tile n = lookup.get(tx + "," + ty);
        return n != null && n.tileName.equals(tileName);
    }

    private int pickIndex(boolean top, boolean bottom, boolean left, boolean right,
                          boolean tl,  boolean tr,     boolean bl,   boolean br) {

        boolean useNW = tl && top && left;
        boolean useNE = tr && top && right;
        boolean useSE = br && bottom && right;
        boolean useSW = bl && bottom && left;

        int mask = 0;
        if (useNW) mask |= 1;
        if (top)   mask |= 2;
        if (useNE) mask |= 4;
        if (right) mask |= 8;
        if (useSE) mask |= 16;
        if (bottom)mask |= 32;
        if (useSW) mask |= 64;
        if (left)  mask |= 128;

        switch (mask) {
            case 0:   return 3;
            case 2:   return 3;
            case 8:   return 3;
            case 32:  return 3;
            case 128: return 3;
            case 34:  return 3;
            case 136: return 3;
            case 10:  return 3;
            case 40:  return 3;
            case 160: return 3;
            case 130: return 3;
            case 14:  return 22;
            case 56:  return 0;
            case 224: return 2;
            case 131: return 24;
            case 42:  return 3;
            case 168: return 3;
            case 162: return 3;
            case 138: return 3;
            case 46:  return 22;
            case 58:  return 0;
            case 184: return 0;
            case 232: return 2;
            case 226: return 2;
            case 163: return 24;
            case 139: return 24;
            case 142: return 22;
            case 62:  return 11;
            case 248: return 1;
            case 227: return 13;
            case 143: return 23;
            case 170: return 3;
            case 171: return 24;
            case 174: return 22;
            case 186: return 0;
            case 234: return 2;
            case 175: return 23;
            case 187: return 9;
            case 235: return 13;
            case 190: return 11;
            case 238: return 20;
            case 250: return 1;
            case 191: return 17;
            case 239: return 16;
            case 251: return 27;
            case 254: return 28;
            case 255: return 12;
            default:  return 3;
        }
    }


    public static final int FALLBACK_DEPTH = 20;

    private static final int[] BFS_DX = { -1, 1,  0, 0 };
    private static final int[] BFS_DY = {  0, 0, -1, 1 };

    public static void computeWaterDepths(Map<String, Ground_Tile> lookup) {
        Queue<Ground_Tile> queue = new ArrayDeque<>();

        for (Ground_Tile tile : lookup.values()) {
            if (!tile.tileName.equals("water")) continue;
            tile.waterTileDepth = -1;

            boolean isShore = false;
            for (int i = 0; i < 4; i++) {
                Ground_Tile nb = lookup.get((tile.x + BFS_DX[i]) + "," + (tile.y + BFS_DY[i]));
                if (nb == null || !nb.tileName.equals("water")) { isShore = true; break; }
            }
            if (isShore) {
                tile.waterTileDepth = 0;
                queue.add(tile);
            }
        }

        while (!queue.isEmpty()) {
            Ground_Tile cur = queue.poll();
            for (int i = 0; i < 4; i++) {
                Ground_Tile nb = lookup.get((cur.x + BFS_DX[i]) + "," + (cur.y + BFS_DY[i]));
                if (nb != null && nb.tileName.equals("water") && nb.waterTileDepth == -1) {
                    nb.waterTileDepth = cur.waterTileDepth + 1;
                    queue.add(nb);
                }
            }
        }

        for (Ground_Tile tile : lookup.values()) {
            if (tile.tileName.equals("water") && tile.waterTileDepth == -1)
                tile.waterTileDepth = FALLBACK_DEPTH;
        }
    }

    public int   getX()             { return x; }
    public int   getY()             { return y; }
    public int   getTileIndex()     { return tileIndex; }
    public int   getWaterTileDepth(){ return waterTileDepth; }
}