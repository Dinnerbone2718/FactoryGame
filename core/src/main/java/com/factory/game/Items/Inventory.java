package com.factory.game.Items;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    public static final int INVENTORY_SIZE = 48;

    private final int        size;
    private final ItemStack[] slots;

    public Inventory() {
        this(INVENTORY_SIZE);
    }

    public Inventory(int size) {
        this.size  = size;
        this.slots = new ItemStack[size];
    }

    public int getSize() { return size; }

    public ItemStack getSlot(int index) {
        if (index < 0 || index >= size) return null;
        return slots[index];
    }

    public void setSlot(int index, ItemStack stack) {
        if (index < 0 || index >= size) return;
        slots[index] = stack;
    }

    
    public boolean addItem(Item item, int quantity) {
        int remaining = quantity;

        if (item.isStackable()) {
            for (int i = 0; i < size && remaining > 0; i++) {
                ItemStack slot = slots[i];
                if (slot != null && slot.getItem() == item && !slot.isFull()) {
                    int canAdd = Math.min(remaining, slot.getRemainingCapacity());
                    slot.addQuantity(canAdd);
                    remaining -= canAdd;
                }
            }
        }

        for (int i = 0; i < size && remaining > 0; i++) {
            if (slots[i] == null) {
                int toAdd = Math.min(remaining, item.getMaxStackSize());
                slots[i] = new ItemStack(item, toAdd);
                remaining -= toAdd;
            }
        }

        return remaining == 0;
    }

    public boolean removeItem(Item item, int quantity) {
        if (!hasItem(item, quantity)) return false;

        int remaining = quantity;
        for (int i = 0; i < size && remaining > 0; i++) {
            ItemStack slot = slots[i];
            if (slot != null && slot.getItem() == item) {
                int toRemove = Math.min(remaining, slot.getQuantity());
                slot.removeQuantity(toRemove);
                remaining -= toRemove;

                if (slot.isEmpty()) {
                    slots[i] = null;
                }
            }
        }

        return true;
    }

    public boolean hasItem(Item item, int quantity) {
        int count = 0;
        for (ItemStack slot : slots) {
            if (slot != null && slot.getItem() == item) {
                count += slot.getQuantity();
            }
        }
        return count >= quantity;
    }

    public int countItem(Item item) {
        int count = 0;
        for (ItemStack slot : slots) {
            if (slot != null && slot.getItem() == item) {
                count += slot.getQuantity();
            }
        }
        return count;
    }

    public int getFirstEmptySlot() {
        for (int i = 0; i < size; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    public boolean isFull() {
        return getFirstEmptySlot() == -1;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            slots[i] = null;
        }
    }

    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack slot : slots) {
            if (slot != null) {
                items.add(slot);
            }
        }
        return items;
    }




    public int getQuantity(Item item) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack s = getSlot(i);
            if (s != null && s.getItem() == item) return s.getQuantity();
        }
        return 0;
    }



}