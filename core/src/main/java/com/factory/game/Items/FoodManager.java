package com.factory.game.Items;

public class FoodManager {

    private float hungerRestoration = 0;
    private float thirstRestoration = 0;
    private Item item = null;
    private boolean isFood = false;
    private boolean isLiquid = false;

    public FoodManager(Item item, float hunger, float thirst) {
        this.item = item;

        if (hunger > 0) {
            isFood = true;
            hungerRestoration = hunger;
        }

        if (thirst > 0) {
            isLiquid = true;
            thirstRestoration = thirst;
        }
    }

    public float getHungerValue() {
        return hungerRestoration;
    }

    public float getThirstValue() {
        return thirstRestoration;
    }

    public boolean getIsFood() {
        return isFood;
    }

    public boolean getIsLiquid() {
        return isLiquid;
    }

    public Item getItem() {
        return item;
    }
}
