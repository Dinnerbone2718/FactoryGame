package com.factory.game.World;


public class LiquidMachineDefinition {

    public final LiquidTank.Role role;

    public final float capacity;


    public final LiquidType producedLiquid;
    public final float      productionRate;


    public final LiquidType acceptedLiquid;
    public final float      consumptionRate;


    public LiquidMachineDefinition(
            LiquidTank.Role role,
            float           capacity,
            LiquidType      producedLiquid,
            float           productionRate,
            LiquidType      acceptedLiquid,
            float           consumptionRate) {
        this.role            = role;
        this.capacity        = capacity;
        this.producedLiquid  = producedLiquid;
        this.productionRate  = productionRate;
        this.acceptedLiquid  = acceptedLiquid;
        this.consumptionRate = consumptionRate;
    }

    public static LiquidMachineDefinition storage(float capacity) {
        return new LiquidMachineDefinition(
                LiquidTank.Role.STORAGE, capacity,
                null, 0f, null, 0f);
    }

    public static LiquidMachineDefinition producer(
            float capacity, LiquidType liquid, float rate) {
        return new LiquidMachineDefinition(
                LiquidTank.Role.PRODUCER, capacity,
                liquid, rate, null, 0f);
    }

    public static LiquidMachineDefinition receiver(
            float capacity, LiquidType accepted, float consumeRate) {
        return new LiquidMachineDefinition(
                LiquidTank.Role.RECEIVER, capacity,
                null, 0f, accepted, consumeRate);
    }

    public boolean isProducer() { return producedLiquid != null && productionRate > 0f; }
    public boolean isReceiver() { return role == LiquidTank.Role.RECEIVER; }
    public boolean isStorage()  { return role == LiquidTank.Role.STORAGE;  }
}