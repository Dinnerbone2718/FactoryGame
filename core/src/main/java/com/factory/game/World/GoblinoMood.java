package com.factory.game.World;

public enum GoblinoMood {

    HAPPY (0.75f, "Happy", "25% discount!",  "goblino/goblino_happy.png", 0.60f),
    SAD   (1.20f, "Sad",   "+20% costs",     "goblino/goblino_sad.png",   0.85f),
    ANGRY (1.60f, "Angry", "+60% costs",     "goblino/goblino_angry.png", 0.97f);

    public final float  costMultiplier;
    public final String label;
    public final String priceNote;
    public final String texturePath;


    public final float negotiationFloor;

    GoblinoMood(float  costMultiplier,
                String label,
                String priceNote,
                String texturePath,
                float  negotiationFloor) {
        this.costMultiplier   = costMultiplier;
        this.label            = label;
        this.priceNote        = priceNote;
        this.texturePath      = texturePath;
        this.negotiationFloor = negotiationFloor;
    }
}