package com.factory.game.World;

import com.factory.game.Items.DropTable;
import com.factory.game.Items.ItemClass;


public class HarvestDefinition {

    private final ItemClass requiredToolClass; 
    private final int       hitsRequired;
    private final DropTable dropTable;

    public HarvestDefinition(ItemClass requiredToolClass, int hitsRequired, DropTable dropTable) {
        if (hitsRequired < 1) throw new IllegalArgumentException("hitsRequired must be >= 1");
        this.requiredToolClass = requiredToolClass;
        this.hitsRequired      = hitsRequired;
        this.dropTable         = dropTable;
    }


    public boolean canHarvest(ItemClass toolClass) {
        return requiredToolClass == null || requiredToolClass == toolClass;
    }

    public ItemClass getRequiredToolClass() { return requiredToolClass; }
    public int       getHitsRequired()      { return hitsRequired; }
    public DropTable getDropTable()         { return dropTable; }
}