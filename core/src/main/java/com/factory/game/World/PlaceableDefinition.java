package com.factory.game.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PlaceableDefinition {

    private final PlacedObject.Type placedType;
    private final int               itemCost;
    private final Set<String>       allowedTerrains;


    public PlaceableDefinition(PlacedObject.Type placedType, int itemCost) {
        this(placedType, itemCost, (String[]) null);
    }


    public PlaceableDefinition(PlacedObject.Type placedType, int itemCost,
                               String... allowedTerrains) {
        if (itemCost < 1) throw new IllegalArgumentException("itemCost must be >= 1");
        this.placedType  = placedType;
        this.itemCost    = itemCost;
        this.allowedTerrains = (allowedTerrains == null || allowedTerrains.length == 0)
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(Arrays.asList(allowedTerrains)));
    }


    public boolean canPlaceOn(String tileName) {
        if ("water".equals(tileName)) return false;
        return allowedTerrains.isEmpty() || allowedTerrains.contains(tileName);
    }

    public PlacedObject.Type getPlacedType()    { return placedType;    }
    public int               getItemCost()      { return itemCost;      }
    public Set<String>       getAllowedTerrains(){ return allowedTerrains; }
}