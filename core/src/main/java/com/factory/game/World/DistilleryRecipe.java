package com.factory.game.World;

import java.util.ArrayList;
import java.util.List;

import com.factory.game.Items.CraftingManager;


public class DistilleryRecipe {

    public static final List<DistilleryRecipe> ALL = CraftingManager.distilleryRecipes;


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

    public static class OutputSpec {
        public final LiquidType type;
        public final float      ratePerSecond;
        public final float      tankCapacity;

        public OutputSpec(LiquidType type, float ratePerSecond, float tankCapacity) {
            this.type          = type;
            this.ratePerSecond = ratePerSecond;
            this.tankCapacity  = tankCapacity;
        }
    }



    private final String           name;
    private final List<InputSpec>  inputs  = new ArrayList<>();
    private final List<OutputSpec> outputs = new ArrayList<>();

    public DistilleryRecipe(String name) {
        this.name = name;
    }

    public DistilleryRecipe addInput(LiquidType type, float ratePerSecond, float tankCapacity) {
        if (inputs.size() >= 3)
            throw new IllegalStateException("Distillery recipes support at most 3 inputs.");
        inputs.add(new InputSpec(type, ratePerSecond, tankCapacity));
        return this;
    }

    public DistilleryRecipe addOutput(LiquidType type, float ratePerSecond, float tankCapacity) {
        if (outputs.size() >= 3)
            throw new IllegalStateException("Distillery recipes support at most 3 outputs.");
        outputs.add(new OutputSpec(type, ratePerSecond, tankCapacity));
        return this;
    }



    public String           getName()    { return name;    }
    public List<InputSpec>  getInputs()  { return inputs;  }
    public List<OutputSpec> getOutputs() { return outputs; }
}