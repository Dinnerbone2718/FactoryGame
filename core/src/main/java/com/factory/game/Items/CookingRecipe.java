package com.factory.game.Items;

public class CookingRecipe {

    private final String name;
    private final Item inputItem;
    private final Item outputItem;
    private final int outputQuantity;

    public CookingRecipe(
        String name,
        Item inputItem,
        Item outputItem,
        int outputQuantity
    ) {
        this.name = name;
        this.inputItem = inputItem;
        this.outputItem = outputItem;
        this.outputQuantity = outputQuantity;
    }

    public String getName() {
        return name;
    }

    public Item getInputItem() {
        return inputItem;
    }

    public Item getOutputItem() {
        return outputItem;
    }

    public int getOutputQuantity() {
        return outputQuantity;
    }

    @Override
    public String toString() {
        return (
            name +
            ": " +
            inputItem.getDisplayName() +
            " x1 -> " +
            outputItem.getDisplayName() +
            " x" +
            outputQuantity
        );
    }
}
