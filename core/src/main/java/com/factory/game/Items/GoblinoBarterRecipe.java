package com.factory.game.Items;

public class GoblinoBarterRecipe {

    private final String name;
    private final Item   inputItem;
    private final int    inputQuantity;
    private final Item   outputItem;
    private final int    outputQuantity;

    public GoblinoBarterRecipe(String name,
                                Item inputItem,  int inputQuantity,
                                Item outputItem, int outputQuantity) {
        this.name           = name;
        this.inputItem      = inputItem;
        this.inputQuantity  = inputQuantity;
        this.outputItem     = outputItem;
        this.outputQuantity = outputQuantity;
    }

    public String getName()           { return name;           }
    public Item   getInputItem()      { return inputItem;      }
    public int    getInputQuantity()  { return inputQuantity;  }
    public Item   getOutputItem()     { return outputItem;     }
    public int    getOutputQuantity() { return outputQuantity; }

    public boolean canBarter(Inventory inventory) {
        return inventory.hasItem(inputItem, inputQuantity);
    }

    public boolean barter(Inventory inventory) {
        if (!canBarter(inventory)) return false;
        if (!inventory.removeItem(inputItem, inputQuantity)) return false;
        return inventory.addItem(outputItem, outputQuantity);
    }

    @Override
    public String toString() {
        return name + ": " + inputItem.getDisplayName() + " \u00d7" + inputQuantity
             + " \u2192 " + outputItem.getDisplayName() + " \u00d7" + outputQuantity;
    }
}