package com.factory.game.Items;

public class GoblinoGiftRecipe {

    private final Item   giftItem;
    private final int    quantity;
    private final int    happinessGain;
    private final String responseText;

    public GoblinoGiftRecipe(Item giftItem, int quantity,
                              int happinessGain, String responseText) {
        this.giftItem      = giftItem;
        this.quantity      = quantity;
        this.happinessGain = happinessGain;
        this.responseText  = responseText;
    }

    public Item   getGiftItem()      { return giftItem;      }
    public int    getQuantity()      { return quantity;       }
    public int    getHappinessGain() { return happinessGain; }
    public String getResponseText()  { return responseText;  }

    public boolean canGift(Inventory inventory) {
        return inventory.hasItem(giftItem, quantity);
    }

    public boolean gift(Inventory inventory) {
        if (!canGift(inventory)) return false;
        return inventory.removeItem(giftItem, quantity);
    }
}