package com.factory.game.Items;

import com.factory.game.World.LiquidType;


public class SmeltingRecipe {

    private final String      name;
    private final Item        inputItem;
    private final LiquidType  outputLiquid;
    private final float       outputAmount;  

    public SmeltingRecipe(String name, Item inputItem,
                          LiquidType outputLiquid, float outputAmount) {
        this.name         = name;
        this.inputItem    = inputItem;
        this.outputLiquid = outputLiquid;
        this.outputAmount = outputAmount;
    }

    public String     getName()         { return name;         }
    public Item       getInputItem()    { return inputItem;    }
    public LiquidType getOutputLiquid() { return outputLiquid; }
    public float      getOutputAmount() { return outputAmount; }
}