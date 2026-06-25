package com.factory.game.World;


public class LiquidTank {

    public enum Role {

        PRODUCER,

        RECEIVER,

        STORAGE
    }

    private LiquidType liquidType;
    private float      amount;
    private final float capacity;
    private final Role  role;

    private final java.util.Set<LiquidType> allowedTypes;

    public LiquidTank(Role role, float capacity, java.util.Set<LiquidType> allowedTypes) {
        this.role         = role;
        this.capacity     = capacity;
        this.amount       = 0f;
        this.liquidType   = null;
        this.allowedTypes = allowedTypes;
    }

    public LiquidTank(Role role, float capacity) {
        this.role         = role;
        this.capacity     = capacity;
        this.amount       = 0f;
        this.liquidType   = null;
        this.allowedTypes = null;
    }


    public boolean canAccept(LiquidType type) {
        if (amount >= capacity) return false;
        if (allowedTypes != null && !allowedTypes.contains(type)) return false;
        return liquidType == null || liquidType == type;
    }

    public float      getAmount()    { return amount;              }
    public float      getCapacity()  { return capacity;            }
    public float      getFillRatio() { return amount / capacity;   }
    public LiquidType getType()      { return liquidType;          }
    public Role       getRole()      { return role;                }
    public boolean    isEmpty()      { return amount <= 0f;        }
    public float      getSpace()     { return capacity - amount;   }


    public float deposit(LiquidType type, float qty) {
        if (!canAccept(type) || qty <= 0f) return 0f;
        float depositable = Math.min(qty, capacity - amount);
        if (liquidType == null) liquidType = type;
        amount += depositable;
        return depositable;
    }


    public float withdraw(float qty) {
        if (qty <= 0f || isEmpty()) return 0f;
        float taken = Math.min(qty, amount);
        amount -= taken;
        if (amount <= 0f) {
            amount     = 0f;
            liquidType = null;
        }
        return taken;
    }


    public void clear() {
        amount     = 0f;
        liquidType = null;
    }
}