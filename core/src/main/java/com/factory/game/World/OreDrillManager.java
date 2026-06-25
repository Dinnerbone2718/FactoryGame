package com.factory.game.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;


public final class OreDrillManager {


    public  static final int   ORE_DRILL_INV_SLOTS = 9;

    public  static final float MINE_DURATION        = 12f;

    private static final float GAS_PER_ORE          = 10f;


    private static final Object[][] WEIGHTED_ORES = {
        { Item.STONE,         40 }, 
        { Item.RAW_OREMINUM,    20 },
        { Item.RAW_ROCKITE,     15 },
        { Item.RAW_EARTHEL,     12 },
        { Item.RAW_STONEDON,     8 },
        { Item.COROSERITE,       3 },   
        { Item.SNOWITE_PIECES,   2 },   
    };

    private static final int TOTAL_WEIGHT;
    static {
        int w = 0;
        for (Object[] entry : WEIGHTED_ORES) w += (int) entry[1];
        TOTAL_WEIGHT = w;
    }



    private static final class DrillState {
        float mineTimer = 0f;
    }

    private final Map<String, DrillState> states           = new HashMap<>();
    private final Map<String, Inventory>  drillInventories = new HashMap<>();
    private final Random                  rng              = new Random();


    public void update(float delta, Collection<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            for (PlacedObject obj : chunk.getPlacedObjects()) {
                if (obj.type != PlacedObject.Type.ORE_DRILL) continue;
                tickDrill(delta, obj);
            }
        }
    }


    public float getMineProgress(PlacedObject drill) {
        if (!hasGas(drill)) return 0f;
        DrillState state = states.get(keyOf(drill));
        return (state == null) ? 0f : Math.min(1f, state.mineTimer / MINE_DURATION);
    }

    public float getGasRatio(PlacedObject drill) {
        LiquidTank tank = drill.getLiquidTank();
        if (tank == null || tank.getCapacity() <= 0f) return 0f;
        return Math.min(1f, tank.getAmount() / tank.getCapacity());
    }

    public boolean hasGas(PlacedObject drill) {
        LiquidTank tank = drill.getLiquidTank();
        return tank != null && tank.getAmount() >= GAS_PER_ORE;
    }


    public Inventory getOrCreateInventory(PlacedObject drill) {
        return drillInventories.computeIfAbsent(keyOf(drill),
                k -> new Inventory(ORE_DRILL_INV_SLOTS));
    }


    public Inventory getInventory(PlacedObject drill) {
        return drillInventories.get(keyOf(drill));
    }

    public void onDrillRemoved(PlacedObject drill) {
        String key = keyOf(drill);
        states.remove(key);
        drillInventories.remove(key);
    }


    private void tickDrill(float delta, PlacedObject drill) {
        LiquidTank tank = drill.getLiquidTank();
        if (tank == null || tank.getAmount() < GAS_PER_ORE) return;

        String     key   = keyOf(drill);
        DrillState state = states.computeIfAbsent(key, k -> new DrillState());

        state.mineTimer += delta;
        if (state.mineTimer < MINE_DURATION) return;

        state.mineTimer -= MINE_DURATION;

        tank.withdraw(GAS_PER_ORE);


        getOrCreateInventory(drill).addItem(rollOre(), 1);
    }

    private Item rollOre() {
        int roll       = rng.nextInt(TOTAL_WEIGHT);
        int cumulative = 0;
        for (Object[] entry : WEIGHTED_ORES) {
            cumulative += (int) entry[1];
            if (roll < cumulative) return (Item) entry[0];
        }
        return Item.STONE; 
    }

    private static String keyOf(PlacedObject obj) {
        return obj.getX() + "," + obj.getY();
    }
}