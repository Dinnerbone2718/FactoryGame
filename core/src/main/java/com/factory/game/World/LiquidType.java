package com.factory.game.World;

import com.badlogic.gdx.graphics.Color;
import com.factory.game.Items.Item;

public enum LiquidType {

    WATER("Water", new Color(0.20f, 0.55f, 1.00f, 0.85f)),

    MOLTEN_OREMINUM("Molten Oreminum", new Color(0.15f, 0.30f, 0.315f, 0.95f)),
    MOLTEN_ROCKITE ("Molten Rockite",  new Color(0.35f, 0.65f, 1.00f, 0.95f)),
    MOLTEN_EARTHEL ("Molten Earthel",  new Color(1.00f, 0.45f, 0.10f, 0.95f)),
    MOLTEN_STONEDON("Molten Stonedon", new Color(0.60f, 1.00f, 0.20f, 0.95f)),
    MOLTEN_SNOWITE ("Molten Snowite",  new Color(0.20f, 0.20f, 1.00f, 0.95f)),

    FERTILIZER("Fertilizer", new Color(1f, 1.00f, 1.00f, 0.95f)),

    COOLANT("Coolant", new Color(0.10f, 0.90f, 0.85f, 0.90f)),

    OIL("Oil", new Color(0.05f, 0.05f, 0.05f, 0.85f)),


    REFINED_OIL("Refined Oil", new Color(0.85f, 0.65f, 0.05f, 0.90f)),

    SLAG("Slag", new Color(0.28f, 0.22f, 0.18f, 0.95f)),

    STEAM("Steam", new Color(0.85f, 0.90f, 0.95f, 0.50f)),

    COMPOSITE_MELT("Composite Melt", new Color(0.55f, 0.42f, 0.80f, 0.95f)),

    GAS("Gas", new Color(0.90f, 0.90f, 0.90f, 0.75f));


    public final String name;
    public final Color  color;

    LiquidType(String name, Color color) {
        this.name  = name;
        this.color = new Color(color);
    }

    public Item toIngot() {
        switch (this) {
            case MOLTEN_OREMINUM: return Item.OREMINUM_INGOT;
            case MOLTEN_ROCKITE:  return Item.ROCKITE_INGOT;
            case MOLTEN_EARTHEL:  return Item.EARTHEL_INGOT;
            case MOLTEN_STONEDON: return Item.STONEDON_INGOT;
            case MOLTEN_SNOWITE:  return Item.SNOWITE_INGOT;
            case COMPOSITE_MELT: return Item.COMPOSITE_INGOT;
            default:              return null;
        }
    }
}