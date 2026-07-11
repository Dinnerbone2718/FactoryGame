package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;
import com.factory.game.World.Animal;
import com.factory.game.World.GoblinoHutManager;
import com.factory.game.World.PlanterManager;
import com.factory.game.World.WorldDelta;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GameSaveManager {

    private static final int MAGIC = 0x53415645;
    private static final int VERSION = 1;
    private static final int SLOT_COUNT = 5;
    private static final String SAVE_DIR = "saves/";

    public static final class SlotInfo {

        public final int slot;
        public final boolean exists;
        public final long lastModified;

        SlotInfo(int slot, boolean exists, long lastModified) {
            this.slot = slot;
            this.exists = exists;
            this.lastModified = lastModified;
        }
    }

    public static int getSlotCount() {
        return SLOT_COUNT;
    }

    public static SlotInfo[] listSlots() {
        SlotInfo[] infos = new SlotInfo[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            File file = slotFile(i);
            infos[i] = new SlotInfo(
                i,
                file.exists(),
                file.exists() ? file.lastModified() : 0L
            );
        }
        return infos;
    }

    private static File slotFile(int slot) {
        return new File(
            Gdx.files.getLocalStoragePath() + SAVE_DIR + "slot" + slot + ".sav"
        );
    }

    public static boolean save(int slot, Main main) {
        File file = slotFile(slot);
        file.getParentFile().mkdirs();
        try (
            DataOutputStream out = new DataOutputStream(
                new FileOutputStream(file)
            )
        ) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);

            Player player = main.getPlayer();
            out.writeFloat(player.getWorldX());
            out.writeFloat(player.getWorldY());
            out.writeBoolean(player.isOnRaft());
            out.writeBoolean(player.isFlashlightOn());
            writeInventory(out, player.getInventory());

            out.writeFloat(main.getHunger().getCurrentHunger());

            WorldManager worldManager = main.getWorldManager();
            worldManager.flushLiveStateToDeltas();
            worldManager.getSurfaceDelta().writeTo(out);
            worldManager.getCaveDelta().writeTo(out);

            Set<String> spawnedChunks = worldManager.getAnimalSpawnedChunks();
            out.writeInt(spawnedChunks.size());
            for (String key : spawnedChunks) out.writeUTF(key);

            List<Animal> animals = worldManager.getAnimals();
            out.writeInt(animals.size());
            for (Animal a : animals) {
                out.writeInt(a.type.ordinal());
                out.writeFloat(a.getWorldX());
                out.writeFloat(a.getWorldY());
                out.writeFloat(a.getHealth());
                out.writeFloat(a.getAge());
                Animal.Genes genes = a.snapshotGenes();
                out.writeFloat(genes.visionTiles);
                out.writeFloat(genes.speedMultiplier);
                out.writeFloat(genes.stomachMultiplier);
                out.writeFloat(genes.aggressionMultiplier);
                out.writeFloat(genes.reproductiveRate);
                out.writeFloat(genes.resilienceMultiplier);
                out.writeFloat(genes.maxAge);
            }

            PlanterManager planterManager = worldManager.getPlanterManager();
            Map<String, float[]> growth = planterManager.getGrowthSnapshot();
            out.writeInt(growth.size());
            for (Map.Entry<String, float[]> e : growth.entrySet()) {
                out.writeUTF(e.getKey());
                float[] v = e.getValue();
                out.writeInt((int) v[0]);
                out.writeFloat(v[1]);
                out.writeInt((int) v[2]);
            }

            Map<String, Inventory> planterInvs =
                planterManager.getPlanterInventoriesSnapshot();
            out.writeInt(planterInvs.size());
            for (Map.Entry<String, Inventory> e : planterInvs.entrySet()) {
                out.writeUTF(e.getKey());
                writeInventory(out, e.getValue());
            }

            GoblinoHutManager goblinoHutManager =
                worldManager.getGoblinoHutManager();
            Map<String, Integer> happiness =
                goblinoHutManager.getHappinessSnapshot();
            out.writeInt(happiness.size());
            for (Map.Entry<String, Integer> e : happiness.entrySet()) {
                out.writeUTF(e.getKey());
                out.writeInt(e.getValue());
            }

            return true;
        } catch (IOException e) {
            Gdx.app.error(
                "GameSaveManager",
                "Failed to save slot " + slot + ": " + e.getMessage()
            );
            return false;
        }
    }

    public static boolean load(int slot, Main main) {
        File file = slotFile(slot);
        if (!file.exists()) return false;

        try (
            DataInputStream in = new DataInputStream(new FileInputStream(file))
        ) {
            int magic = in.readInt();
            if (magic != MAGIC) {
                Gdx.app.error("GameSaveManager", "Bad magic in slot " + slot);
                return false;
            }
            in.readInt();

            float px = in.readFloat();
            float py = in.readFloat();
            boolean onRaft = in.readBoolean();
            boolean flashlightOn = in.readBoolean();
            int invCount = in.readInt();
            int[] invOrdinals = new int[invCount];
            int[] invQtys = new int[invCount];
            for (int i = 0; i < invCount; i++) {
                invOrdinals[i] = in.readInt();
                invQtys[i] = in.readInt();
            }

            float hunger = in.readFloat();

            WorldDelta surfaceDelta = WorldDelta.readFrom(in);
            WorldDelta caveDelta = WorldDelta.readFrom(in);

            int spawnedChunkCount = in.readInt();
            Set<String> spawnedChunks = new HashSet<>();
            for (int i = 0; i < spawnedChunkCount; i++) spawnedChunks.add(
                in.readUTF()
            );

            int animalCount = in.readInt();
            List<Animal> animals = new ArrayList<>();
            Animal.Type[] animalTypes = Animal.Type.values();
            for (int i = 0; i < animalCount; i++) {
                int typeOrd = in.readInt();
                float ax = in.readFloat();
                float ay = in.readFloat();
                float health = in.readFloat();
                float age = in.readFloat();
                float visionTiles = in.readFloat();
                float speedMultiplier = in.readFloat();
                float stomachMultiplier = in.readFloat();
                float aggressionMultiplier = in.readFloat();
                float reproductiveRate = in.readFloat();
                float resilienceMultiplier = in.readFloat();
                float maxAge = in.readFloat();

                if (typeOrd < 0 || typeOrd >= animalTypes.length) continue;
                Animal.Genes genes = new Animal.Genes(
                    visionTiles,
                    speedMultiplier,
                    stomachMultiplier,
                    aggressionMultiplier,
                    reproductiveRate,
                    resilienceMultiplier,
                    maxAge
                );
                Animal animal = new Animal(
                    animalTypes[typeOrd],
                    ax,
                    ay,
                    System.nanoTime(),
                    genes
                );
                animal.setHealth(health);
                animal.setAge(age);
                animals.add(animal);
            }

            int growthCount = in.readInt();
            Map<String, float[]> growth = new HashMap<>();
            for (int i = 0; i < growthCount; i++) {
                String key = in.readUTF();
                int seedOrd = in.readInt();
                float growTimer = in.readFloat();
                int growFrame = in.readInt();
                growth.put(key, new float[] { seedOrd, growTimer, growFrame });
            }

            int planterInvCount = in.readInt();
            Map<String, Inventory> planterInvs = new HashMap<>();
            for (int i = 0; i < planterInvCount; i++) {
                String key = in.readUTF();
                planterInvs.put(key, readInventory(in));
            }

            int happinessCount = in.readInt();
            Map<String, Integer> happiness = new HashMap<>();
            for (int i = 0; i < happinessCount; i++) {
                String key = in.readUTF();
                happiness.put(key, in.readInt());
            }

            WorldManager worldManager = main.getWorldManager();
            worldManager.applyLoadedWorld(
                surfaceDelta,
                caveDelta,
                animals,
                spawnedChunks
            );
            worldManager.getPlanterManager().restoreGrowthSnapshot(growth);
            worldManager
                .getPlanterManager()
                .restorePlanterInventories(planterInvs);
            worldManager.getGoblinoHutManager().restoreHappiness(happiness);

            Player player = main.getPlayer();
            player.setPosition(px, py);
            player.setOnRaft(onRaft);
            if (flashlightOn) {
                player.setFlashlightOn(
                    main.getLightRenderer(),
                    main.getCamera()
                );
            } else {
                player.setFlashlightOff();
            }
            Inventory inv = player.getInventory();
            Item[] items = Item.values();
            int slots = Math.min(invCount, inv.getSize());
            for (int i = 0; i < slots; i++) {
                int ord = invOrdinals[i];
                int qty = invQtys[i];
                if (ord >= 0 && ord < items.length && qty > 0) {
                    inv.setSlot(i, new ItemStack(items[ord], qty));
                } else {
                    inv.setSlot(i, null);
                }
            }
            for (int i = slots; i < inv.getSize(); i++) inv.setSlot(i, null);

            main.getHunger().setCurrentHunger(hunger);

            return true;
        } catch (IOException e) {
            Gdx.app.error(
                "GameSaveManager",
                "Failed to load slot " + slot + ": " + e.getMessage()
            );
            return false;
        }
    }

    private static void writeInventory(DataOutputStream out, Inventory inv)
        throws IOException {
        out.writeInt(inv.getSize());
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack s = inv.getSlot(i);
            out.writeInt(s != null ? s.getItem().ordinal() : -1);
            out.writeInt(s != null ? s.getQuantity() : 0);
        }
    }

    private static Inventory readInventory(DataInputStream in)
        throws IOException {
        int size = in.readInt();
        Inventory inv = new Inventory(size);
        Item[] items = Item.values();
        for (int i = 0; i < size; i++) {
            int ord = in.readInt();
            int qty = in.readInt();
            if (ord >= 0 && ord < items.length && qty > 0) {
                inv.setSlot(i, new ItemStack(items[ord], qty));
            }
        }
        return inv;
    }

    private GameSaveManager() {}
}
