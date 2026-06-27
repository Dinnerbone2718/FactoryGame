package com.factory.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.EnumMap;
import java.util.Map;

public final class PlacedObjectCache {

    private static final int TILE_W = 16;
    private static final int TILE_H = 16;

    public enum RenderMode {
        FIXED {
            @Override
            public int pickTileIndex(
                boolean top,
                boolean bottom,
                boolean left,
                boolean right,
                boolean tl,
                boolean tr,
                boolean bl,
                boolean br
            ) {
                return 0;
            }
        },

        RANDOM {
            @Override
            public int pickTileIndex(
                boolean top,
                boolean bottom,
                boolean left,
                boolean right,
                boolean tl,
                boolean tr,
                boolean bl,
                boolean br
            ) {
                return 0;
            }
        },

        ANIMATED {
            @Override
            public int pickTileIndex(
                boolean top,
                boolean bottom,
                boolean left,
                boolean right,
                boolean tl,
                boolean tr,
                boolean bl,
                boolean br
            ) {
                return 0;
            }
        },

        WALL {
            @Override
            public int pickTileIndex(
                boolean top,
                boolean bottom,
                boolean left,
                boolean right,
                boolean tl,
                boolean tr,
                boolean bl,
                boolean br
            ) {
                int mask = 0;
                if (top) mask |= 1;
                if (right) mask |= 2;
                if (bottom) mask |= 4;
                if (left) mask |= 8;
                switch (mask) {
                    case 0:
                        return 15;
                    case 1:
                        return 2;
                    case 2:
                        return 3;
                    case 3:
                        return 12;
                    case 4:
                        return 0;
                    case 5:
                        return 1;
                    case 6:
                        return 6;
                    case 7:
                        return 9;
                    case 8:
                        return 5;
                    case 9:
                        return 14;
                    case 10:
                        return 4;
                    case 11:
                        return 13;
                    case 12:
                        return 8;
                    case 13:
                        return 11;
                    case 14:
                        return 7;
                    case 15:
                        return 10;
                    default:
                        return 15;
                }
            }
        },

        BLOB_47 {
            @Override
            public int pickTileIndex(
                boolean top,
                boolean bottom,
                boolean left,
                boolean right,
                boolean tl,
                boolean tr,
                boolean bl,
                boolean br
            ) {
                boolean useNW = tl && top && left;
                boolean useNE = tr && top && right;
                boolean useSE = br && bottom && right;
                boolean useSW = bl && bottom && left;

                int mask = 0;
                if (useNW) mask |= 1;
                if (top) mask |= 2;
                if (useNE) mask |= 4;
                if (right) mask |= 8;
                if (useSE) mask |= 16;
                if (bottom) mask |= 32;
                if (useSW) mask |= 64;
                if (left) mask |= 128;

                switch (mask) {
                    case 0:
                    case 2:
                    case 8:
                    case 32:
                    case 128:
                    case 34:
                    case 136:
                    case 10:
                    case 40:
                    case 160:
                    case 130:
                    case 42:
                    case 168:
                    case 162:
                    case 138:
                        return 3;
                    case 14:
                    case 46:
                    case 142:
                        return 22;
                    case 56:
                    case 58:
                    case 184:
                        return 0;
                    case 224:
                    case 232:
                    case 226:
                        return 2;
                    case 131:
                    case 163:
                    case 139:
                        return 24;
                    case 62:
                        return 11;
                    case 248:
                        return 1;
                    case 227:
                    case 235:
                        return 13;
                    case 143:
                    case 175:
                        return 23;
                    case 170:
                        return 3;
                    case 171:
                        return 24;
                    case 174:
                        return 22;
                    case 186:
                        return 0;
                    case 234:
                        return 2;
                    case 187:
                        return 9;
                    case 190:
                        return 11;
                    case 238:
                        return 20;
                    case 250:
                        return 1;
                    case 191:
                        return 17;
                    case 239:
                        return 16;
                    case 251:
                        return 27;
                    case 254:
                        return 28;
                    case 255:
                        return 12;
                    default:
                        return 3;
                }
            }
        },

        TILESETTER {
            @Override
            public int pickTileIndex(
                boolean top,
                boolean bottom,
                boolean left,
                boolean right,
                boolean tl,
                boolean tr,
                boolean bl,
                boolean br
            ) {
                boolean useNW = tl && top && left;
                boolean useNE = tr && top && right;
                boolean useSE = br && bottom && right;
                boolean useSW = bl && bottom && left;

                int mask = 0;
                if (useNW) mask |= 1;
                if (top) mask |= 2;
                if (useNE) mask |= 4;
                if (right) mask |= 8;
                if (useSE) mask |= 16;
                if (bottom) mask |= 32;
                if (useSW) mask |= 64;
                if (left) mask |= 128;

                switch (mask) {
                    case 0:
                        return 36;
                    case 2:
                        return 25;
                    case 8:
                        return 33;
                    case 32:
                        return 3;
                    case 128:
                        return 35;
                    case 34:
                        return 14;
                    case 136:
                        return 34;
                    case 10:
                        return 37;
                    case 40:
                        return 4;
                    case 160:
                        return 7;
                    case 130:
                        return 40;
                    case 14:
                        return 22;
                    case 56:
                        return 0;
                    case 224:
                        return 2;
                    case 131:
                        return 24;
                    case 42:
                        return 48;
                    case 168:
                        return 8;
                    case 162:
                        return 51;
                    case 138:
                        return 41;
                    case 46:
                        return 15;
                    case 58:
                        return 26;
                    case 184:
                        return 6;
                    case 232:
                        return 5;
                    case 226:
                        return 29;
                    case 163:
                        return 18;
                    case 139:
                        return 38;
                    case 142:
                        return 39;
                    case 62:
                        return 11;
                    case 248:
                        return 1;
                    case 227:
                        return 13;
                    case 143:
                        return 23;
                    case 170:
                        return 52;
                    case 171:
                        return 43;
                    case 174:
                        return 42;
                    case 186:
                        return 31;
                    case 234:
                        return 32;
                    case 175:
                        return 19;
                    case 187:
                        return 9;
                    case 235:
                        return 49;
                    case 190:
                        return 50;
                    case 238:
                        return 20;
                    case 250:
                        return 30;
                    case 191:
                        return 17;
                    case 239:
                        return 16;
                    case 251:
                        return 27;
                    case 254:
                        return 28;
                    case 255:
                        return 12;
                    default:
                        return 36;
                }
            }
        },

        WANG_16 {
            @Override
            public int pickTileIndex(
                boolean top,
                boolean bottom,
                boolean left,
                boolean right,
                boolean tl,
                boolean tr,
                boolean bl,
                boolean br
            ) {
                int mask = 0;
                if (top) mask |= 1;
                if (right) mask |= 2;
                if (bottom) mask |= 4;
                if (left) mask |= 8;
                return mask;
            }
        };

        public abstract int pickTileIndex(
            boolean top,
            boolean bottom,
            boolean left,
            boolean right,
            boolean tl,
            boolean tr,
            boolean bl,
            boolean br
        );

        public boolean isAutoTile() {
            return (
                this == WALL ||
                this == BLOB_47 ||
                this == WANG_16 ||
                this == TILESETTER
            );
        }
    }

    public static final class SpriteConfig {

        public final String path;
        public final int cols, rows;
        public final RenderMode mode;
        public final int fixedIndex;
        public final float drawWidth, drawHeight;
        public final float offsetX, offsetY;
        public final float animFps;
        public final int hitboxW, hitboxH;
        public final int hitboxOffX, hitboxOffY;
        public final boolean solid;
        public final boolean isFloor;

        public final int tileWidth;
        public final int tileHeight;

        public SpriteConfig(
            String path,
            int cols,
            int rows,
            RenderMode mode,
            int fixedIndex,
            float drawWidth,
            float drawHeight,
            float offsetX,
            float offsetY,
            float animFps,
            int hitboxW,
            int hitboxH,
            int hitboxOffX,
            int hitboxOffY,
            boolean solid,
            int tileWidth,
            int tileHeight,
            boolean isFloor
        ) {
            this.path = path;
            this.cols = cols;
            this.rows = rows;
            this.mode = mode;
            this.fixedIndex = fixedIndex;
            this.drawWidth = drawWidth;
            this.drawHeight = drawHeight;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.animFps = animFps;
            this.hitboxW = hitboxW;
            this.hitboxH = hitboxH;
            this.hitboxOffX = hitboxOffX;
            this.hitboxOffY = hitboxOffY;
            this.solid = solid;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.isFloor = isFloor;
        }

        public SpriteConfig(
            String path,
            int cols,
            int rows,
            RenderMode mode,
            int fixedIndex,
            float drawWidth,
            float drawHeight,
            float offsetX,
            float offsetY,
            int hitboxW,
            int hitboxH,
            int hitboxOffX,
            int hitboxOffY,
            boolean solid
        ) {
            this(
                path,
                cols,
                rows,
                mode,
                fixedIndex,
                drawWidth,
                drawHeight,
                offsetX,
                offsetY,
                0f,
                hitboxW,
                hitboxH,
                hitboxOffX,
                hitboxOffY,
                solid,
                1,
                1,
                false
            );
        }

        public SpriteConfig(
            String path,
            int cols,
            int rows,
            RenderMode mode,
            int fixedIndex,
            float drawWidth,
            float drawHeight,
            float offsetX,
            float offsetY,
            float animFps,
            boolean solid
        ) {
            this(
                path,
                cols,
                rows,
                mode,
                fixedIndex,
                drawWidth,
                drawHeight,
                offsetX,
                offsetY,
                animFps,
                0,
                0,
                0,
                0,
                solid,
                1,
                1,
                false
            );
        }

        public SpriteConfig(
            String path,
            int cols,
            int rows,
            RenderMode mode,
            int fixedIndex,
            float drawWidth,
            float drawHeight,
            float offsetX,
            float offsetY,
            boolean solid
        ) {
            this(
                path,
                cols,
                rows,
                mode,
                fixedIndex,
                drawWidth,
                drawHeight,
                offsetX,
                offsetY,
                0f,
                0,
                0,
                0,
                0,
                solid,
                1,
                1,
                false
            );
        }

        public SpriteConfig(
            String path,
            int cols,
            int rows,
            RenderMode mode,
            int fixedIndex,
            float drawWidth,
            float drawHeight,
            float offsetX,
            float offsetY,
            float animFps,
            int hitboxW,
            int hitboxH,
            int hitboxOffX,
            int hitboxOffY,
            boolean solid,
            int tileWidth,
            int tileHeight
        ) {
            this(
                path,
                cols,
                rows,
                mode,
                fixedIndex,
                drawWidth,
                drawHeight,
                offsetX,
                offsetY,
                animFps,
                hitboxW,
                hitboxH,
                hitboxOffX,
                hitboxOffY,
                solid,
                tileWidth,
                tileHeight,
                false
            );
        }

        public boolean hasHitbox() {
            return hitboxW > 0 && hitboxH > 0;
        }
    }

    private static final Map<PlacedObject.Type, SpriteConfig> CONFIGS =
        new EnumMap<>(PlacedObject.Type.class);

    static {
        CONFIGS.put(
            PlacedObject.Type.WOOD_WALL,
            new SpriteConfig(
                "tileset/Wall/wood.png",
                3,
                0,
                RenderMode.WALL,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                14,
                1,
                1,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.STONE_WALL,
            new SpriteConfig(
                "tileset/Wall/stone.png",
                3,
                0,
                RenderMode.WALL,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                14,
                1,
                1,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.CRUSHING_POT,
            new SpriteConfig(
                "machines/model/crushing_pot.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                12,
                10,
                2,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.CAMPFIRE,
            new SpriteConfig(
                "spritesheets/campfire.png",
                5,
                1,
                RenderMode.ANIMATED,
                0,
                16f,
                16f,
                0f,
                0f,
                8f,
                12,
                8,
                2,
                0,
                true,
                1,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.LANTERN,
            new SpriteConfig(
                "spritesheets/lantern.png",
                3,
                3,
                RenderMode.ANIMATED,
                0,
                16f,
                16f,
                0f,
                0f,
                6f,
                12,
                8,
                2,
                0,
                true,
                1,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.GRASS,
            new SpriteConfig(
                "spritesheets/weed.png",
                4,
                4,
                RenderMode.RANDOM,
                0,
                16f,
                16f,
                0f,
                0f,
                false
            )
        );

        CONFIGS.put(
            PlacedObject.Type.SEASHELL,
            new SpriteConfig(
                "spritesheets/seashells.png",
                4,
                4,
                RenderMode.RANDOM,
                0,
                16f,
                16f,
                0f,
                0f,
                false
            )
        );

        CONFIGS.put(
            PlacedObject.Type.WELL,
            new SpriteConfig(
                "machines/model/well.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                20f,
                0f,
                0f,
                14,
                10,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.BARREL,
            new SpriteConfig(
                "machines/model/barrel.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                8,
                4,
                4,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.DEVBARREL,
            new SpriteConfig(
                "machines/model/barrel.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                8,
                4,
                4,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.BASIC_PIPE,
            new SpriteConfig(
                "tileset/Pipe/basic_pipe.png",
                3,
                0,
                RenderMode.WALL,
                0,
                16f,
                16f,
                0f,
                0f,
                false
            )
        );

        CONFIGS.put(
            PlacedObject.Type.FURNACE,
            new SpriteConfig(
                "machines/model/furnace.png",
                2,
                2,
                RenderMode.ANIMATED,
                0,
                16f,
                16f,
                0f,
                0f,
                6f,
                14,
                10,
                1,
                0,
                true,
                1,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.CAST,
            new SpriteConfig(
                "machines/model/cast.png",
                2,
                2,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                10,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.STORAGE_CRATE,
            new SpriteConfig(
                "machines/model/storage_crate.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                8,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.FOREST_CRATE,
            new SpriteConfig(
                "machines/model/forest_crate.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                8,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.DESERT_CRATE,
            new SpriteConfig(
                "machines/model/desert_crate.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                8,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.MOUNTAIN_CRATE,
            new SpriteConfig(
                "machines/model/mountain_crate.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                8,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.PENGUIN_PLUSH,
            new SpriteConfig(
                "other_placeables/penguin_plush.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                4,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.POO_PET,
            new SpriteConfig(
                "other_placeables/poo_pet.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                4,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.FILTER_PIPE,
            new SpriteConfig(
                "tileset/Pipe/filter_pipe.png",
                3,
                0,
                RenderMode.WALL,
                0,
                16f,
                16f,
                0f,
                0f,
                false
            )
        );

        CONFIGS.put(
            PlacedObject.Type.SOLDERING_TABLE,
            new SpriteConfig(
                "machines/model/soldering_table.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                10,
                10,
                3,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.MIXER,
            new SpriteConfig(
                "machines/model/mixer.png",
                2,
                1,
                RenderMode.ANIMATED,
                0,
                16f,
                24f,
                0f,
                0f,
                6f,
                14,
                6,
                1,
                0,
                true,
                1,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.FISHBOWL,
            new SpriteConfig(
                "other_placeables/fishbowl.png",
                8,
                4,
                RenderMode.ANIMATED,
                0,
                16f,
                16f,
                0f,
                0f,
                16f,
                12,
                6,
                2,
                0,
                true,
                1,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.ITEM_PIPE,
            new SpriteConfig(
                "tileset/Pipe/transport_pipe.png",
                3,
                0,
                RenderMode.WALL,
                0,
                16f,
                16f,
                0f,
                0f,
                false
            )
        );

        CONFIGS.put(
            PlacedObject.Type.PLANTER,
            new SpriteConfig(
                "machines/farming/pot.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                32f,
                16f,
                0f,
                0f,
                16f,
                30,
                6,
                1,
                0,
                true,
                2,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.CRUSHER,
            new SpriteConfig(
                "machines/model/auto_crusher.png",
                4,
                5,
                RenderMode.ANIMATED,
                0,
                16f,
                16f,
                0f,
                0f,
                12f,
                14,
                6,
                1,
                0,
                true,
                1,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.WOOD_FLOOR,
            new SpriteConfig(
                "tileset/Floor/wood_floor.png",
                3,
                0,
                RenderMode.WALL,
                0,
                16f,
                16f,
                0f,
                0f,
                0f,
                0,
                0,
                0,
                0,
                false,
                1,
                1,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.STONE_FLOOR,
            new SpriteConfig(
                "tileset/Floor/stone_floor.png",
                3,
                0,
                RenderMode.WALL,
                0,
                16f,
                16f,
                0f,
                0f,
                0f,
                0,
                0,
                0,
                0,
                false,
                1,
                1,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.DRILL,
            new SpriteConfig(
                "machines/model/drill.png",
                5,
                1,
                RenderMode.ANIMATED,
                0,
                32f,
                32f,
                0f,
                0f,
                4f,
                30,
                10,
                1,
                0,
                true,
                2,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.TANK,
            new SpriteConfig(
                "machines/model/tank.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                32f,
                32f,
                0f,
                0f,
                4f,
                30,
                20,
                1,
                0,
                true,
                2,
                2
            )
        );

        CONFIGS.put(
            PlacedObject.Type.SMELTER,
            new SpriteConfig(
                "machines/model/smelter.png",
                3,
                3,
                RenderMode.ANIMATED,
                0,
                32f,
                32f,
                0f,
                0f,
                8f,
                30,
                12,
                1,
                0,
                true,
                2,
                2
            )
        );

        CONFIGS.put(
            PlacedObject.Type.WOOD_WALL_FULL,
            new SpriteConfig(
                "tileset/wall/wood_wall.png",
                11,
                0,
                RenderMode.TILESETTER,
                0,
                16f,
                16f,
                0f,
                0f,
                0f,
                14,
                14,
                1,
                1,
                true,
                1,
                1,
                false
            )
        );

        CONFIGS.put(
            PlacedObject.Type.STONE_WALL_FULL,
            new SpriteConfig(
                "tileset/wall/stone_wall.png",
                11,
                0,
                RenderMode.TILESETTER,
                0,
                16f,
                16f,
                0f,
                0f,
                0f,
                14,
                14,
                1,
                1,
                true,
                1,
                1,
                false
            )
        );

        CONFIGS.put(
            PlacedObject.Type.CHUNK_LOADER,
            new SpriteConfig(
                "machines/model/chunk_loader.png",
                3,
                3,
                RenderMode.ANIMATED,
                0,
                32f,
                32f,
                0f,
                0f,
                8f,
                30,
                12,
                1,
                0,
                true,
                2,
                2
            )
        );

        CONFIGS.put(
            PlacedObject.Type.TRASH,
            new SpriteConfig(
                "machines/model/trash.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                8,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.DISTILLERY,
            new SpriteConfig(
                "machines/model/distillery.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                32f,
                32f,
                0f,
                0f,
                4f,
                30,
                20,
                1,
                0,
                true,
                2,
                2
            )
        );

        CONFIGS.put(
            PlacedObject.Type.GLOBE,
            new SpriteConfig(
                "other_placeables/globe.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                20f,
                0f,
                0f,
                14,
                8,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.BOOK_SHELF,
            new SpriteConfig(
                "other_placeables/book_shelf.png",
                2,
                1,
                RenderMode.RANDOM,
                0,
                16f,
                24f,
                0f,
                0f,
                14,
                8,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.CHAIR,
            new SpriteConfig(
                "other_placeables/chair.png",
                1,
                1,
                RenderMode.RANDOM,
                0,
                16f,
                24f,
                0f,
                0f,
                10,
                8,
                3,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.SIGN,
            new SpriteConfig(
                "other_placeables/sign.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                6,
                1,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.TABLE,
            new SpriteConfig(
                "other_placeables/table.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                12,
                6,
                2,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.BARREL_DECO,
            new SpriteConfig(
                "other_placeables/barrel_deco.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                12,
                6,
                2,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.WOOD_PLANKS,
            new SpriteConfig(
                "tileset/Floor/wood_planks.png",
                3,
                3,
                RenderMode.RANDOM,
                0,
                16f,
                16f,
                0f,
                0f,
                0f,
                0,
                0,
                0,
                0,
                false,
                1,
                1,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.TOILET,
            new SpriteConfig(
                "other_placeables/toilet.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                10,
                6,
                3,
                0,
                true
            )
        );

        CONFIGS.put(
            PlacedObject.Type.LAMP_POST,
            new SpriteConfig(
                "machines/model/lamp_post.png",
                3,
                3,
                RenderMode.ANIMATED,
                0,
                16f,
                24f,
                0f,
                0f,
                8f,
                14,
                12,
                1,
                0,
                true,
                1,
                1
            )
        );

        CONFIGS.put(
            PlacedObject.Type.ORE_DRILL,
            new SpriteConfig(
                "machines/model/ore_drill.png",
                3,
                4,
                RenderMode.ANIMATED,
                0,
                32f,
                32f,
                0f,
                0f,
                1f,
                30,
                12,
                1,
                0,
                true,
                2,
                2
            )
        );

        CONFIGS.put(
            PlacedObject.Type.FILTER_ITEM_PIPE,
            new SpriteConfig(
                "tileset/Pipe/filter_transport_pipe.png",
                3,
                0,
                RenderMode.WALL,
                0,
                16f,
                16f,
                0f,
                0f,
                false
            )
        );

        CONFIGS.put(
            PlacedObject.Type.STOVE,
            new SpriteConfig(
                "machines/model/oven.png",
                1,
                1,
                RenderMode.FIXED,
                0,
                16f,
                16f,
                0f,
                0f,
                14,
                10,
                1,
                0,
                true
            )
        );
    }

    private static final Map<PlacedObject.Type, Texture> textures =
        new EnumMap<>(PlacedObject.Type.class);
    private static final Map<PlacedObject.Type, TextureRegion[]> regions =
        new EnumMap<>(PlacedObject.Type.class);

    private static boolean initialised = false;

    public static int getTileWidth(PlacedObject.Type type) {
        SpriteConfig cfg = CONFIGS.get(type);
        return (cfg != null) ? cfg.tileWidth : TILE_W;
    }

    public static int getTileHeight(PlacedObject.Type type) {
        SpriteConfig cfg = CONFIGS.get(type);
        return (cfg != null) ? cfg.tileHeight : TILE_H;
    }

    public static void init() {
        if (initialised) return;

        for (Map.Entry<
            PlacedObject.Type,
            SpriteConfig
        > entry : CONFIGS.entrySet()) {
            PlacedObject.Type type = entry.getKey();
            SpriteConfig cfg = entry.getValue();

            if (!Gdx.files.internal(cfg.path).exists()) {
                Gdx.app.error(
                    "PlacedObjectCache",
                    "Sprite not found: " +
                        cfg.path +
                        " — " +
                        type +
                        " will be invisible."
                );
                continue;
            }

            Texture tex = new Texture(cfg.path);
            int rows = (cfg.rows > 0) ? cfg.rows : (tex.getHeight() / TILE_H);
            int tileW = tex.getWidth() / cfg.cols;
            int tileH = tex.getHeight() / rows;
            int total = cfg.cols * rows;

            TextureRegion[] arr = new TextureRegion[total];
            for (int i = 0; i < total; i++) {
                int col = i % cfg.cols;
                int row = i / cfg.cols;
                arr[i] = new TextureRegion(
                    tex,
                    col * tileW,
                    row * tileH,
                    tileW,
                    tileH
                );
            }

            textures.put(type, tex);
            regions.put(type, arr);
        }

        PlanterSpriteCache.init();

        initialised = true;
    }

    public static void dispose() {
        for (Texture t : textures.values()) t.dispose();
        textures.clear();
        regions.clear();
        PlanterSpriteCache.dispose();
        initialised = false;
    }

    public static SpriteConfig getConfig(PlacedObject.Type type) {
        return CONFIGS.get(type);
    }

    public static TextureRegion getRegion(PlacedObject.Type type, int index) {
        TextureRegion[] arr = regions.get(type);
        if (arr == null) return null;
        if (index < 0 || index >= arr.length) return arr[0];
        return arr[index];
    }

    public static int getTotalFrames(PlacedObject.Type type) {
        TextureRegion[] arr = regions.get(type);
        return (arr != null) ? arr.length : 1;
    }
}
