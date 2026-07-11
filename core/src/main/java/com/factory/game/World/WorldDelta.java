package com.factory.game.World;

import com.badlogic.gdx.Gdx;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;
import com.factory.game.Main;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldDelta {

    public enum Layer {
        PIPE,
        FLOOR,
        OBJECT,
    }

    public static Layer layerOf(PlacedObject.Type type) {
        switch (type) {
            case BASIC_PIPE:
            case FILTER_PIPE:
            case ITEM_PIPE:
            case FILTER_ITEM_PIPE:
                return Layer.PIPE;
            case WOOD_FLOOR:
            case STONE_FLOOR:
            case WOOD_PLANKS:
                return Layer.FLOOR;
            default:
                return Layer.OBJECT;
        }
    }

    public static final class PlacedRecord {

        public final int x, y;
        public final PlacedObject.Type type;
        public volatile LiquidType liquidType = null;
        public volatile float liquidAmount = 0f;
        public volatile ItemStack[] crateContents = null;
        public volatile int filterAllowedMask = 0;
        public volatile int[] itemFilterOrdinals = null;
        public volatile int devBarrelLiquidOrdinal = -1;
        public volatile int itemPipeConfigBits = -1;
        public volatile int distilleryRecipeOrdinal = -1;

        public volatile float[] mixerInputAmounts = null;
        public volatile float mixerOutputAmount = 0f;

        public volatile int mixerRecipeOrdinal = -1;

        public volatile float[] distilleryInputAmounts = null;
        public volatile float[] distilleryOutputAmounts = null;

        public volatile String[] chunkLoaderPinnedKeys = null;

        public PlacedRecord(int x, int y, PlacedObject.Type type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    private final Set<String> removedObjects = ConcurrentHashMap.newKeySet();

    private final ConcurrentHashMap<String, PlacedRecord> pipeByPos =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PlacedRecord> floorByPos =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PlacedRecord> objectByPos =
        new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, List<PlacedRecord>> placedByChunk =
        new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, PlacedRecord> mapFor(Layer layer) {
        switch (layer) {
            case PIPE:
                return pipeByPos;
            case FLOOR:
                return floorByPos;
            default:
                return objectByPos;
        }
    }

    public void markRemoved(int worldX, int worldY) {
        removedObjects.add(worldX + "," + worldY);
    }

    public boolean isRemoved(int worldX, int worldY) {
        return removedObjects.contains(worldX + "," + worldY);
    }

    public PlacedRecord addPlaced(
        int worldX,
        int worldY,
        PlacedObject.Type type
    ) {
        String posKey = worldX + "," + worldY;
        String chunkKey = chunkKeyFor(worldX, worldY);
        ConcurrentHashMap<String, PlacedRecord> layerMap = mapFor(
            layerOf(type)
        );

        PlacedRecord record = new PlacedRecord(worldX, worldY, type);
        PlacedRecord existing = layerMap.putIfAbsent(posKey, record);
        if (existing != null) return null;

        placedByChunk
            .computeIfAbsent(chunkKey, k ->
                Collections.synchronizedList(new ArrayList<>())
            )
            .add(record);
        return record;
    }

    public void removePlaced(int worldX, int worldY, PlacedObject.Type type) {
        removeFromLayer(worldX, worldY, layerOf(type));
    }

    public void removePlaced(int worldX, int worldY) {
        for (Layer layer : Layer.values()) {
            removeFromLayer(worldX, worldY, layer);
        }
    }

    private void removeFromLayer(int worldX, int worldY, Layer layer) {
        String posKey = worldX + "," + worldY;
        PlacedRecord record = mapFor(layer).remove(posKey);
        if (record == null) return;

        String chunkKey = chunkKeyFor(worldX, worldY);
        List<PlacedRecord> list = placedByChunk.get(chunkKey);
        if (list != null) list.remove(record);
    }

    public List<PlacedRecord> getPlacedForChunk(int cx, int cy) {
        List<PlacedRecord> list = placedByChunk.get(cx + "," + cy);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public void updateLiquid(
        int worldX,
        int worldY,
        LiquidType type,
        float amount
    ) {
        PlacedRecord record = objectByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.liquidType = type;
        record.liquidAmount = amount;
    }

    public void updateCrateContents(
        int worldX,
        int worldY,
        ItemStack[] contents
    ) {
        PlacedRecord record = objectByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.crateContents = contents;
    }

    public void updateDevBarrelLiquid(
        int worldX,
        int worldY,
        int liquidOrdinal
    ) {
        PlacedRecord record = objectByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.devBarrelLiquidOrdinal = liquidOrdinal;
    }

    public void updateMixerTanks(
        int worldX,
        int worldY,
        float[] inputAmounts,
        float outputAmount
    ) {
        PlacedRecord record = objectByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.mixerInputAmounts = inputAmounts;
        record.mixerOutputAmount = outputAmount;
    }

    public void updateMixerRecipe(int worldX, int worldY, int recipeOrdinal) {
        PlacedRecord record = objectByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.mixerRecipeOrdinal = recipeOrdinal;
    }

    public void updateDistilleryRecipe(
        int worldX,
        int worldY,
        int recipeOrdinal
    ) {
        PlacedRecord record = objectByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.distilleryRecipeOrdinal = recipeOrdinal;
    }

    public void updateDistilleryTanks(
        int worldX,
        int worldY,
        float[] inputAmounts,
        float[] outputAmounts
    ) {
        PlacedRecord record = objectByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.distilleryInputAmounts = inputAmounts;
        record.distilleryOutputAmounts = outputAmounts;
    }

    public void updateChunkLoaderPins(
        int worldX,
        int worldY,
        java.util.Set<String> pinnedKeys
    ) {
        PlacedRecord record = objectByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.chunkLoaderPinnedKeys = (pinnedKeys == null ||
            pinnedKeys.isEmpty())
            ? null
            : pinnedKeys.toArray(new String[0]);
    }

    public void updateItemPipeConfig(int worldX, int worldY, int bits) {
        PlacedRecord record = pipeByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.itemPipeConfigBits = bits;
    }

    public int getItemPipeConfigBits(int worldX, int worldY) {
        PlacedRecord record = pipeByPos.get(worldX + "," + worldY);
        return (record != null) ? record.itemPipeConfigBits : -1;
    }

    public void updateFilterPipe(int worldX, int worldY, int mask) {
        PlacedRecord record = pipeByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.filterAllowedMask = mask;
    }

    public void updateItemFilterPipe(
        int worldX,
        int worldY,
        int[] itemOrdinals
    ) {
        PlacedRecord record = pipeByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.itemFilterOrdinals = itemOrdinals;
    }

    public static int[] toItemFilterArray(java.util.Set<Item> items) {
        if (items == null || items.isEmpty()) return null;
        int[] arr = new int[items.size()];
        int i = 0;
        for (Item item : items) arr[i++] = item.ordinal();
        return arr;
    }

    public static java.util.Set<Item> fromItemFilterArray(int[] ordinals) {
        if (ordinals == null || ordinals.length == 0) return null;
        java.util.EnumSet<Item> result = java.util.EnumSet.noneOf(Item.class);
        Item[] vals = Item.values();
        for (int ord : ordinals) {
            if (ord >= 0 && ord < vals.length) result.add(vals[ord]);
        }
        return result.isEmpty() ? null : result;
    }

    public static int toFilterMask(java.util.Set<LiquidType> types) {
        if (types == null || types.isEmpty()) return 0;
        int mask = 0;
        for (LiquidType t : types) mask |= (1 << t.ordinal());
        return mask;
    }

    public static java.util.Set<LiquidType> fromFilterMask(int mask) {
        if (mask == 0) return null;
        java.util.EnumSet<LiquidType> result = java.util.EnumSet.noneOf(
            LiquidType.class
        );
        LiquidType[] vals = LiquidType.values();
        for (int i = 0; i < vals.length; i++) {
            if ((mask & (1 << i)) != 0) result.add(vals[i]);
        }
        return result;
    }

    private static final int MAGIC = 0xFAC70007;

    public void save(String path) {
        File file = resolveFile(path);
        file.getParentFile().mkdirs();
        try (
            DataOutputStream out = new DataOutputStream(
                new FileOutputStream(file)
            )
        ) {
            writeTo(out);
        } catch (IOException e) {
            Gdx.app.error(
                "WorldDelta",
                "Failed to save delta to " + path + ": " + e.getMessage()
            );
        }
    }

    private List<PlacedRecord> allRecords() {
        List<PlacedRecord> all = new ArrayList<>(
            pipeByPos.size() + floorByPos.size() + objectByPos.size()
        );
        all.addAll(pipeByPos.values());
        all.addAll(floorByPos.values());
        all.addAll(objectByPos.values());
        return all;
    }

    public void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(MAGIC);

        out.writeInt(removedObjects.size());
        for (String key : removedObjects) {
            String[] parts = key.split(",");
            out.writeInt(Integer.parseInt(parts[0]));
            out.writeInt(Integer.parseInt(parts[1]));
        }

        List<PlacedRecord> all = allRecords();
        out.writeInt(all.size());
        for (PlacedRecord r : all) {
            out.writeInt(r.x);
            out.writeInt(r.y);
            out.writeInt(r.type.ordinal());
            out.writeInt(r.liquidType != null ? r.liquidType.ordinal() : -1);
            out.writeFloat(r.liquidAmount);
            int crateLen = (r.crateContents != null)
                ? r.crateContents.length
                : 0;
            out.writeInt(crateLen);
            for (int j = 0; j < crateLen; j++) {
                ItemStack s = r.crateContents[j];
                out.writeInt(s != null ? s.getItem().ordinal() : -1);
                out.writeInt(s != null ? s.getQuantity() : 0);
            }
            out.writeInt(r.filterAllowedMask);
            out.writeInt(r.devBarrelLiquidOrdinal);
            out.writeInt(r.itemPipeConfigBits);
            out.writeInt(r.mixerRecipeOrdinal);
            out.writeInt(r.distilleryRecipeOrdinal);

            int inputCount = (r.mixerInputAmounts != null)
                ? r.mixerInputAmounts.length
                : 0;
            out.writeInt(inputCount);
            for (int j = 0; j < inputCount; j++) {
                out.writeFloat(r.mixerInputAmounts[j]);
            }
            out.writeFloat(r.mixerOutputAmount);

            int itemFilterLen = (r.itemFilterOrdinals != null)
                ? r.itemFilterOrdinals.length
                : 0;
            out.writeInt(itemFilterLen);
            for (int j = 0; j < itemFilterLen; j++) {
                out.writeInt(r.itemFilterOrdinals[j]);
            }

            int distInputLen = (r.distilleryInputAmounts != null)
                ? r.distilleryInputAmounts.length
                : 0;
            out.writeInt(distInputLen);
            for (int j = 0; j < distInputLen; j++) {
                out.writeFloat(r.distilleryInputAmounts[j]);
            }

            int distOutputLen = (r.distilleryOutputAmounts != null)
                ? r.distilleryOutputAmounts.length
                : 0;
            out.writeInt(distOutputLen);
            for (int j = 0; j < distOutputLen; j++) {
                out.writeFloat(r.distilleryOutputAmounts[j]);
            }

            int pinnedLen = (r.chunkLoaderPinnedKeys != null)
                ? r.chunkLoaderPinnedKeys.length
                : 0;
            out.writeInt(pinnedLen);
            for (int j = 0; j < pinnedLen; j++) {
                out.writeUTF(r.chunkLoaderPinnedKeys[j]);
            }
        }
    }

    public static WorldDelta load(String path) {
        WorldDelta delta = new WorldDelta();
        File file = resolveFile(path);
        if (!file.exists()) return delta;

        try (
            DataInputStream in = new DataInputStream(new FileInputStream(file))
        ) {
            return readFrom(in);
        } catch (IOException e) {
            Gdx.app.error(
                "WorldDelta",
                "Failed to load delta from " + path + ": " + e.getMessage()
            );
            return new WorldDelta();
        }
    }

    public static WorldDelta readFrom(DataInputStream in) throws IOException {
        WorldDelta delta = new WorldDelta();

        int magic = in.readInt();
        if (magic != MAGIC) {
            Gdx.app.error("WorldDelta", "Bad magic — ignoring save.");
            return delta;
        }

        int removedCount = in.readInt();
        for (int i = 0; i < removedCount; i++) {
            int x = in.readInt();
            int y = in.readInt();
            delta.removedObjects.add(x + "," + y);
        }

        PlacedObject.Type[] allTypes = PlacedObject.Type.values();
        LiquidType[] allLiquids = LiquidType.values();

        int placedCount = in.readInt();
        for (int i = 0; i < placedCount; i++) {
            int x = in.readInt();
            int y = in.readInt();
            int typeOrdinal = in.readInt();
            int liqOrdinal = in.readInt();
            float liqAmount = in.readFloat();

            int crateLen = in.readInt();
            Item[] allItems = Item.values();
            ItemStack[] contents = new ItemStack[crateLen];
            for (int j = 0; j < crateLen; j++) {
                int itemOrd = in.readInt();
                int qty = in.readInt();
                if (itemOrd >= 0 && itemOrd < allItems.length && qty > 0) {
                    contents[j] = new ItemStack(allItems[itemOrd], qty);
                }
            }

            int filterMask = in.readInt();
            int devBarrelOrd = in.readInt();
            int itemPipeBits = in.readInt();
            int mixerOrd = in.readInt();
            int distilleryOrd = in.readInt();

            int mixerInputCount = in.readInt();
            float[] inputAmounts = new float[mixerInputCount];
            for (int j = 0; j < mixerInputCount; j++) {
                inputAmounts[j] = in.readFloat();
            }
            float outputAmount = in.readFloat();

            int itemFilterLen = in.readInt();
            int[] itemFilterOrdinals = new int[itemFilterLen];
            for (int j = 0; j < itemFilterLen; j++) {
                itemFilterOrdinals[j] = in.readInt();
            }

            int distInputLen = in.readInt();
            float[] distInputAmounts = new float[distInputLen];
            for (int j = 0; j < distInputLen; j++) {
                distInputAmounts[j] = in.readFloat();
            }

            int distOutputLen = in.readInt();
            float[] distOutputAmounts = new float[distOutputLen];
            for (int j = 0; j < distOutputLen; j++) {
                distOutputAmounts[j] = in.readFloat();
            }

            int pinnedLen = in.readInt();
            String[] pinnedKeys = new String[pinnedLen];
            for (int j = 0; j < pinnedLen; j++) {
                pinnedKeys[j] = in.readUTF();
            }

            if (typeOrdinal >= 0 && typeOrdinal < allTypes.length) {
                delta.addPlaced(x, y, allTypes[typeOrdinal]);

                if (
                    liqOrdinal >= 0 &&
                    liqOrdinal < allLiquids.length &&
                    liqAmount > 0f
                ) {
                    delta.updateLiquid(x, y, allLiquids[liqOrdinal], liqAmount);
                }

                if (crateLen > 0) {
                    delta.updateCrateContents(x, y, contents);
                }

                if (filterMask != 0) {
                    delta.updateFilterPipe(x, y, filterMask);
                }

                if (devBarrelOrd >= 0 && devBarrelOrd < allLiquids.length) {
                    delta.updateDevBarrelLiquid(x, y, devBarrelOrd);
                }

                if (itemPipeBits >= 0) {
                    delta.updateItemPipeConfig(x, y, itemPipeBits);
                }

                if (mixerOrd >= 0) {
                    delta.updateMixerRecipe(x, y, mixerOrd);
                }

                if (mixerInputCount > 0) {
                    delta.updateMixerTanks(x, y, inputAmounts, outputAmount);
                }

                if (distilleryOrd >= 0) {
                    delta.updateDistilleryRecipe(x, y, distilleryOrd);
                }

                if (itemFilterLen > 0) {
                    delta.updateItemFilterPipe(x, y, itemFilterOrdinals);
                }

                if (distInputLen > 0 || distOutputLen > 0) {
                    delta.updateDistilleryTanks(
                        x,
                        y,
                        distInputLen > 0 ? distInputAmounts : null,
                        distOutputLen > 0 ? distOutputAmounts : null
                    );
                }

                if (pinnedLen > 0) {
                    delta.updateChunkLoaderPins(
                        x,
                        y,
                        new java.util.HashSet<>(
                            java.util.Arrays.asList(pinnedKeys)
                        )
                    );
                }
            }
        }

        return delta;
    }

    private static String chunkKeyFor(int worldX, int worldY) {
        int cx = Math.floorDiv(worldX, Main.CHUNK_SIZE);
        int cy = Math.floorDiv(worldY, Main.CHUNK_SIZE);
        return cx + "," + cy;
    }

    private static File resolveFile(String path) {
        return new File(Gdx.files.getLocalStoragePath() + path);
    }

    public boolean hasPlaced(int worldX, int worldY) {
        String key = worldX + "," + worldY;
        return (
            pipeByPos.containsKey(key) ||
            floorByPos.containsKey(key) ||
            objectByPos.containsKey(key)
        );
    }

    private static final int MAX_FOOTPRINT_SPAN = 4;

    public boolean isTileOccupied(int worldX, int worldY) {
        if (hasPlaced(worldX, worldY)) return true;

        for (int dx = -MAX_FOOTPRINT_SPAN; dx <= 0; dx++) {
            for (int dy = -MAX_FOOTPRINT_SPAN; dy <= 0; dy++) {
                if (dx == 0 && dy == 0) continue;
                String key = (worldX + dx) + "," + (worldY + dy);

                for (Layer layer : Layer.values()) {
                    PlacedRecord rec = mapFor(layer).get(key);
                    if (rec == null) continue;

                    int tw = PlacedObjectCache.getTileWidth(rec.type);
                    int th = PlacedObjectCache.getTileHeight(rec.type);
                    if (tw <= 1 && th <= 1) continue;

                    if (
                        worldX >= rec.x &&
                        worldX < rec.x + tw &&
                        worldY >= rec.y &&
                        worldY < rec.y + th
                    ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
