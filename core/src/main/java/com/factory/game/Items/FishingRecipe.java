package com.factory.game.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishingRecipe {

    private final Item          rodItem;
    private final List<Item>    outputs     = new ArrayList<>();
    private final List<Integer> weights     = new ArrayList<>();
    private       int           totalWeight = 0;

    private FishingMinigame.Difficulty difficulty = FishingMinigame.Difficulty.MEDIUM;

    public FishingRecipe(Item rodItem) {
        this.rodItem = rodItem;
    }

    public FishingRecipe addOutput(Item item, int weight) {
        outputs.add(item);
        weights.add(weight);
        totalWeight += weight;
        return this;
    }


    public FishingRecipe setDifficulty(FishingMinigame.Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public Item roll(Random rng) {
        int r          = rng.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < outputs.size(); i++) {
            cumulative += weights.get(i);
            if (r < cumulative) return outputs.get(i);
        }
        return outputs.get(outputs.size() - 1);
    }

    public float getChance(int i) {
        return totalWeight == 0 ? 0f : weights.get(i) * 100f / totalWeight;
    }

    public Item                        getRodItem()   { return rodItem;     }
    public List<Item>                  getOutputs()   { return outputs;     }
    public List<Integer>               getWeights()   { return weights;     }
    public int                         getTotalWeight(){ return totalWeight; }
    public FishingMinigame.Difficulty  getDifficulty(){ return difficulty;  }
}