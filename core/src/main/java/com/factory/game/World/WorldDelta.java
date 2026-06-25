package com.factory.game.World;

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

import com.badlogic.gdx.Gdx;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;
import com.factory.game.Main;


public final class WorldDelta {


    public static final class PlacedRecord {
        public final int               x, y;
        public final PlacedObject.Type type;
        public volatile LiquidType liquidType    = null;
        public volatile float      liquidAmount  = 0f;
        public volatile ItemStack[] crateContents = null;
        public volatile int        filterAllowedMask = 0;
        public volatile int[]      itemFilterOrdinals = null;
        public volatile int        devBarrelLiquidOrdinal = -1;
        public volatile int itemPipeConfigBits = -1;
        public volatile int distilleryRecipeOrdinal = -1;

        public volatile float[] mixerInputAmounts = null;
        public volatile float   mixerOutputAmount = 0f;

        public volatile int        mixerRecipeOrdinal = -1;

        public PlacedRecord(int x, int y, PlacedObject.Type type) {
            this.x    = x;
            this.y    = y;
            this.type = type;
        }
    }


    private final Set<String>                                    removedObjects = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, PlacedRecord>        placedByPos    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<PlacedRecord>>  placedByChunk  = new ConcurrentHashMap<>();


    public void markRemoved(int worldX, int worldY) {
        removedObjects.add(worldX + "," + worldY);
    }

    public boolean isRemoved(int worldX, int worldY) {
        return removedObjects.contains(worldX + "," + worldY);
    }


    public PlacedRecord addPlaced(int worldX, int worldY, PlacedObject.Type type) {
        String posKey   = worldX + "," + worldY;
        String chunkKey = chunkKeyFor(worldX, worldY);

        PlacedRecord record   = new PlacedRecord(worldX, worldY, type);
        PlacedRecord existing = placedByPos.putIfAbsent(posKey, record);
        if (existing != null) return null;

        placedByChunk
            .computeIfAbsent(chunkKey, k -> Collections.synchronizedList(new ArrayList<>()))
            .add(record);
        return record;
    }

    public void removePlaced(int worldX, int worldY) {
        String posKey = worldX + "," + worldY;
        PlacedRecord record = placedByPos.remove(posKey);
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

    public void updateLiquid(int worldX, int worldY, LiquidType type, float amount) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.liquidType   = type;
        record.liquidAmount = amount;
    }

    public void updateItemPipeConfig(int worldX, int worldY, int bits) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.itemPipeConfigBits = bits;
    }

    public int getItemPipeConfigBits(int worldX, int worldY) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        return (record != null) ? record.itemPipeConfigBits : -1;
    }


    public void updateCrateContents(int worldX, int worldY, ItemStack[] contents) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.crateContents = contents;
    }

    public void updateFilterPipe(int worldX, int worldY, int mask) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.filterAllowedMask = mask;
    }

    public void updateItemFilterPipe(int worldX, int worldY, int[] itemOrdinals) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.itemFilterOrdinals = itemOrdinals;
    }

    public static int[] toItemFilterArray(java.util.Set<Item> items) {
        if (items == null || items.isEmpty()) return null;
        int[] arr = new int[items.size()];
        int   i   = 0;
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

    public void updateDevBarrelLiquid(int worldX, int worldY, int liquidOrdinal) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.devBarrelLiquidOrdinal = liquidOrdinal;
    }


    public void updateMixerTanks(int worldX, int worldY, float[] inputAmounts, float outputAmount) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.mixerInputAmounts = inputAmounts;
        record.mixerOutputAmount = outputAmount;
    }

    public void updateMixerRecipe(int worldX, int worldY, int recipeOrdinal) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.mixerRecipeOrdinal = recipeOrdinal;
    }


    public static int toFilterMask(java.util.Set<LiquidType> types) {
        if (types == null || types.isEmpty()) return 0;
        int mask = 0;
        for (LiquidType t : types) mask |= (1 << t.ordinal());
        return mask;
    }

    public static java.util.Set<LiquidType> fromFilterMask(int mask) {
        if (mask == 0) return null;
        java.util.EnumSet<LiquidType> result = java.util.EnumSet.noneOf(LiquidType.class);
        LiquidType[] vals = LiquidType.values();
        for (int i = 0; i < vals.length; i++) {
            if ((mask & (1 << i)) != 0) result.add(vals[i]);
        }
        return result;
    }


    public void updateDistilleryRecipe(int worldX, int worldY, int recipeOrdinal) {
        PlacedRecord record = placedByPos.get(worldX + "," + worldY);
        if (record == null) return;
        record.distilleryRecipeOrdinal = recipeOrdinal;
    }


    private static final int MAGIC = 0xFAC70007;

    public void save(String path) {
        File file = resolveFile(path);
        file.getParentFile().mkdirs();
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {

            out.writeInt(MAGIC);

            out.writeInt(removedObjects.size());
            for (String key : removedObjects) {
                String[] parts = key.split(",");
                out.writeInt(Integer.parseInt(parts[0]));
                out.writeInt(Integer.parseInt(parts[1]));
            }

            out.writeInt(placedByPos.size());
            for (PlacedRecord r : placedByPos.values()) {
                out.writeInt(r.x);
                out.writeInt(r.y);
                out.writeInt(r.type.ordinal());
                out.writeInt(r.liquidType != null ? r.liquidType.ordinal() : -1);
                out.writeFloat(r.liquidAmount);
                int crateLen = (r.crateContents != null) ? r.crateContents.length : 0;
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


                int inputCount = (r.mixerInputAmounts != null) ? r.mixerInputAmounts.length : 0;
                out.writeInt(inputCount);
                for (int j = 0; j < inputCount; j++) {
                    out.writeFloat(r.mixerInputAmounts[j]);
                }
                out.writeFloat(r.mixerOutputAmount);

                int itemFilterLen = (r.itemFilterOrdinals != null) ? r.itemFilterOrdinals.length : 0;
                out.writeInt(itemFilterLen);
                for (int j = 0; j < itemFilterLen; j++) {
                    out.writeInt(r.itemFilterOrdinals[j]);
                }

            }





        } catch (IOException e) {
            Gdx.app.error("WorldDelta", "Failed to save delta to " + path + ": " + e.getMessage());
        }
    }

    public static WorldDelta load(String path) {
        WorldDelta delta = new WorldDelta();
        File file = resolveFile(path);
        if (!file.exists()) return delta;

        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {

            int magic = in.readInt();
            if (magic != MAGIC) {
                Gdx.app.error("WorldDelta", "Bad magic in " + path + " — ignoring save.");
                return delta;
            }

            int removedCount = in.readInt();
            for (int i = 0; i < removedCount; i++) {
                int x = in.readInt();
                int y = in.readInt();
                delta.removedObjects.add(x + "," + y);
            }

            PlacedObject.Type[] allTypes   = PlacedObject.Type.values();
            LiquidType[]        allLiquids = LiquidType.values();


            int placedCount = in.readInt();
            for (int i = 0; i < placedCount; i++) {

                int   x           = in.readInt();
                int   y           = in.readInt();
                int   typeOrdinal = in.readInt();
                int   liqOrdinal  = in.readInt();
                float liqAmount   = in.readFloat();


                int crateLen = in.readInt();
                Item[] allItems = Item.values();
                ItemStack[] contents = new ItemStack[crateLen];
                for (int j = 0; j < crateLen; j++) {
                    int itemOrd = in.readInt();
                    int qty     = in.readInt();
                    if (itemOrd >= 0 && itemOrd < allItems.length && qty > 0) {
                        contents[j] = new ItemStack(allItems[itemOrd], qty);
                    }
                }

                int filterMask      = in.readInt();
                int devBarrelOrd    = in.readInt();
                int itemPipeBits = in.readInt();
                int mixerOrd        = in.readInt();
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


                if (typeOrdinal >= 0 && typeOrdinal < allTypes.length) {

                    delta.addPlaced(x, y, allTypes[typeOrdinal]);

                    if (liqOrdinal >= 0 && liqOrdinal < allLiquids.length && liqAmount > 0f) {
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

                }
            }

        } catch (IOException e) {
            Gdx.app.error("WorldDelta", "Failed to load delta from " + path + ": " + e.getMessage());
            return new WorldDelta();
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
        return placedByPos.containsKey(worldX + "," + worldY);
    }
}