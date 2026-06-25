package com.factory.game.Items;

public class CrushingRecipe {

    private final String name;
    private final Item   inputItem;
    private final Item   outputItem;
    private final int    outputQuantity;

    public CrushingRecipe(String name, Item inputItem, Item outputItem, int outputQuantity) {
        this.name           = name;
        this.inputItem      = inputItem;
        this.outputItem     = outputItem;
        this.outputQuantity = outputQuantity;
    }

    public String getName()           { return name;           }
    public Item   getInputItem()      { return inputItem;      }
    public Item   getOutputItem()     { return outputItem;     }
    public int    getOutputQuantity() { return outputQuantity; }
}