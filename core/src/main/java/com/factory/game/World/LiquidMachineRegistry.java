package com.factory.game.World;

import java.util.EnumMap;
import java.util.Map;

public final class LiquidMachineRegistry {

    private static final Map<PlacedObject.Type, LiquidMachineDefinition> REGISTRY =
            new EnumMap<>(PlacedObject.Type.class);

    static {
        REGISTRY.put(PlacedObject.Type.WELL,
            LiquidMachineDefinition.producer(50f, LiquidType.WATER, 2f));

        REGISTRY.put(PlacedObject.Type.FURNACE,
            new LiquidMachineDefinition(
                LiquidTank.Role.PRODUCER, 100f,
                null, 0f,
                null, 0f));

        REGISTRY.put(PlacedObject.Type.BARREL,
            LiquidMachineDefinition.storage(200f));

        REGISTRY.put(PlacedObject.Type.CAST,
            LiquidMachineDefinition.receiver(25f, null, 0f));


        REGISTRY.put(PlacedObject.Type.DEVBARREL,
            new LiquidMachineDefinition(
                LiquidTank.Role.PRODUCER, 500f,
                null, 50f,
                null, 0f));
        
        REGISTRY.put(PlacedObject.Type.DRILL,
            LiquidMachineDefinition.producer(50f, LiquidType.OIL, 2f));

        REGISTRY.put(PlacedObject.Type.TANK,
            LiquidMachineDefinition.storage(2000f));


        REGISTRY.put(PlacedObject.Type.SMELTER,
            new LiquidMachineDefinition(
                LiquidTank.Role.PRODUCER, 200f,
                null, 0f,
                null, 0f));


        REGISTRY.put(PlacedObject.Type.CHUNK_LOADER,
        LiquidMachineDefinition.receiver(100f, LiquidType.OIL, 10f));


        REGISTRY.put(PlacedObject.Type.TRASH,
        LiquidMachineDefinition.storage(1000f));

        REGISTRY.put(PlacedObject.Type.LAMP_POST,
        LiquidMachineDefinition.receiver(1f, LiquidType.GAS, 0.5f));

        REGISTRY.put(PlacedObject.Type.CHUNK_LOADER,
        LiquidMachineDefinition.receiver(5f, LiquidType.GAS, 2f));

        REGISTRY.put(PlacedObject.Type.ORE_DRILL,
            LiquidMachineDefinition.receiver(100f, LiquidType.GAS, 0f));


    }



    
    public static LiquidMachineDefinition get(PlacedObject.Type type) {
        return REGISTRY.get(type);
    }

    public static boolean isLiquidMachine(PlacedObject.Type type) {
        return REGISTRY.containsKey(type);
    }

    private LiquidMachineRegistry() {}
}