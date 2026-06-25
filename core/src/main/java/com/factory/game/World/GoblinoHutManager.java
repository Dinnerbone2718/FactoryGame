package com.factory.game.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.GoblinoBarterRecipe;
import com.factory.game.Items.GoblinoRecipe;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;


public class GoblinoHutManager {

    private static final int MIN_TRADES = 2;
    private static final int MAX_TRADES = 4;


    public static final int HAPPINESS_TIER_1 = 10;
    public static final int HAPPINESS_TIER_2 = 25;


    public static final int ANGER_TIER_1 = 8;


    public static final int ANGER_TIER_2 = 20;


    public static final int NEGOTIATION_ANGER_PENALTY = 8;


    private final long                                   worldSeed;
    private final Map<String, List<GoblinoRecipe>>       recipeCache  = new HashMap<>();
    private final Map<String, List<GoblinoBarterRecipe>> barterCache  = new HashMap<>();
    private final Map<String, GoblinoMood>               moodCache    = new HashMap<>();

    private final Map<String, Integer> happinessMap = new HashMap<>();


    public GoblinoHutManager(long worldSeed) {
        this.worldSeed = worldSeed;
    }



    public List<GoblinoRecipe> getRecipes(WorldObject hut) {
        String key = hut.getX() + "," + hut.getY();
        return recipeCache.computeIfAbsent(key, k -> generateRecipes(hut.getX(), hut.getY()));
    }

    public List<GoblinoBarterRecipe> getBarterRecipes(WorldObject hut) {
        String key = hut.getX() + "," + hut.getY();
        return barterCache.computeIfAbsent(key, k -> generateBarterRecipes(hut.getX(), hut.getY()));
    }


    public GoblinoMood getMood(WorldObject hut) {
        String      key  = hut.getX() + "," + hut.getY();
        GoblinoMood base = moodCache.computeIfAbsent(key, k -> generateMood(hut.getX(), hut.getY()));
        int         hp   = happinessMap.getOrDefault(key, 0);

        int steps;
        if      (hp >=  HAPPINESS_TIER_2) steps =  2;
        else if (hp >=  HAPPINESS_TIER_1) steps =  1;
        else if (hp <= -ANGER_TIER_2)     steps = -2;
        else if (hp <= -ANGER_TIER_1)     steps = -1;
        else                              steps =  0;

        GoblinoMood[] moods      = GoblinoMood.values();
        int           newOrdinal = Math.max(0, Math.min(moods.length - 1, base.ordinal() - steps));
        return moods[newOrdinal];
    }

    public int getHappiness(WorldObject hut) {
        return happinessMap.getOrDefault(hut.getX() + "," + hut.getY(), 0);
    }


    public int addHappiness(WorldObject hut, int amount) {
        String key      = hut.getX() + "," + hut.getY();
        int    newVal   = happinessMap.merge(key, amount, Integer::sum);
        int    upperCap =  HAPPINESS_TIER_2 + 10;
        int    lowerCap = -(ANGER_TIER_2    + 10);
        if (newVal > upperCap) newVal = upperCap;
        if (newVal < lowerCap) newVal = lowerCap;
        happinessMap.put(key, newVal);
        return newVal;
    }



    public int getEffectiveCost(GoblinoRecipe recipe, WorldObject hut) {
        float mult = getMood(hut).costMultiplier;
        return Math.max(1, Math.round(recipe.getCost() * mult));
    }


    public NegotiationResult attemptNegotiation(WorldObject hut,
                                                 GoblinoRecipe recipe,
                                                 int offeredCoins,
                                                 Inventory inventory) {
        GoblinoMood currentMood   = getMood(hut);
        int         effectiveCost = getEffectiveCost(recipe, hut);
        int         minAcceptable = Math.max(1, Math.round(effectiveCost * currentMood.negotiationFloor));

        if (offeredCoins >= minAcceptable && inventory.hasItem(Item.GOBLINO_COIN, offeredCoins)) {
            inventory.removeItem(Item.GOBLINO_COIN, offeredCoins);
            inventory.addItem(recipe.getOutputItem(), recipe.getOutputQuantity());
            return NegotiationResult.SUCCESS;
        } else {
            addHappiness(hut, -NEGOTIATION_ANGER_PENALTY);
            return NegotiationResult.FAILED_ANGERED;
        }
    }



    public void evict(int hx, int hy) {
        String key = hx + "," + hy;
        recipeCache.remove(key);
        barterCache.remove(key);
        moodCache.remove(key);

    }



    private List<GoblinoRecipe> generateRecipes(int hx, int hy) {
        long   seed       = worldSeed ^ ((long) hx * 0xDEAD_BEEFL) ^ ((long) hy * 0xCAFE_BABEL);
        Random rng        = new Random(seed);
        int    tradeCount = MIN_TRADES + rng.nextInt(MAX_TRADES - MIN_TRADES + 1);

        List<GoblinoRecipe> pool = CraftingManager.getAllGoblinoRecipes();
        Collections.shuffle(pool, rng);
        return new ArrayList<>(pool.subList(0, Math.min(tradeCount, pool.size())));
    }

    private List<GoblinoBarterRecipe> generateBarterRecipes(int hx, int hy) {
        long   seed       = worldSeed ^ ((long) hx * 0xA1B2_C3D4L) ^ ((long) hy * 0xE5F6_7890L);
        Random rng        = new Random(seed);
        int    tradeCount = MIN_TRADES + rng.nextInt(MAX_TRADES - MIN_TRADES + 1);

        List<GoblinoBarterRecipe> pool = CraftingManager.getAllGoblinoBarterRecipes();
        Collections.shuffle(pool, rng);
        return new ArrayList<>(pool.subList(0, Math.min(tradeCount, pool.size())));
    }

    private GoblinoMood generateMood(int hx, int hy) {
        long        seed  = worldSeed ^ ((long) hx * 0xFEED_FACEL) ^ ((long) hy * 0xBEEF_CAFEL);
        GoblinoMood[] moods = GoblinoMood.values();
        return moods[new Random(seed).nextInt(moods.length)];
    }
}