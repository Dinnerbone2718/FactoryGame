package com.factory.game.World;

import java.util.ArrayList;
import java.util.List;

import com.factory.game.Items.CraftingManager;

public class MixingRecipe {

    public static final List<MixingRecipe> ALL = CraftingManager.mixingRecipes;

    public static class InputSpec {
        public final LiquidType type;
        public final float      ratePerSecond;
        public final float      tankCapacity;

        public InputSpec(LiquidType type, float ratePerSecond, float tankCapacity) {
            this.type          = type;
            this.ratePerSecond = ratePerSecond;
            this.tankCapacity  = tankCapacity;
        }
    }

    private final String          name;
    private final List<InputSpec> inputs         = new ArrayList<>();
    private final LiquidType      outputType;
    private final float           outputRate;
    private final float           outputCapacity;

    public MixingRecipe(String name, LiquidType outputType, float outputRate, float outputCapacity) {
        this.name           = name;
        this.outputType     = outputType;
        this.outputRate     = outputRate;
        this.outputCapacity = outputCapacity;
    }

    public MixingRecipe addInput(LiquidType type, float ratePerSecond, float tankCapacity) {
        inputs.add(new InputSpec(type, ratePerSecond, tankCapacity));
        return this;
    }

    public String          getName()           { return name;           }
    public List<InputSpec> getInputs()         { return inputs;         }
    public LiquidType      getOutputType()     { return outputType;     }
    public float           getOutputRate()     { return outputRate;     }
    public float           getOutputCapacity() { return outputCapacity; }
}