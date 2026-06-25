package com.factory.game.Items;

public class ItemStack {
    private final Item item;
    private int quantity;

    public ItemStack(Item item, int quantity) {
        this.item = item;
        this.quantity = Math.min(quantity, item.getMaxStackSize());
    }

    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, Math.min(quantity, item.getMaxStackSize()));
    }

    public void addQuantity(int amount) {
        setQuantity(quantity + amount);
    }

    public void removeQuantity(int amount) {
        setQuantity(quantity - amount);
    }

    public boolean canStackWith(ItemStack other) {
        if (other == null) return false;
        return this.item == other.item && this.item.isStackable();
    }

    public int getRemainingCapacity() {
        return item.getMaxStackSize() - quantity;
    }

    public boolean isFull() {
        return quantity >= item.getMaxStackSize();
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    public ItemStack copy() {
        return new ItemStack(item, quantity);
    }

    @Override
    public String toString() {
        return item.getDisplayName() + " x" + quantity;
    }
}