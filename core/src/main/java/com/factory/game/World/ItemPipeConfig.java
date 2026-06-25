package com.factory.game.World;

public final class ItemPipeConfig {

    public enum PortMode {
        DISABLED,
        INPUT,
        OUTPUT;

        public PortMode next() {
            PortMode[] vals = values();
            return vals[(ordinal() + 1) % vals.length];
        }
    }

    private PortMode north = PortMode.DISABLED;
    private PortMode south = PortMode.DISABLED;
    private PortMode east  = PortMode.DISABLED;
    private PortMode west  = PortMode.DISABLED;

    public PortMode getNorth() { return north; }
    public PortMode getSouth() { return south; }
    public PortMode getEast()  { return east;  }
    public PortMode getWest()  { return west;  }

    public void cycleNorth() { north = north.next(); }
    public void cycleSouth() { south = south.next(); }
    public void cycleEast()  { east  = east.next();  }
    public void cycleWest()  { west  = west.next();  }

    public void setNorth(PortMode m) { north = m; }
    public void setSouth(PortMode m) { south = m; }
    public void setEast(PortMode m)  { east  = m; }
    public void setWest(PortMode m)  { west  = m; }

    public int encode() {
        return (north.ordinal())
             | (south.ordinal() << 2)
             | (east.ordinal()  << 4)
             | (west.ordinal()  << 6);
    }

    public static ItemPipeConfig decode(int bits) {
        PortMode[] modes = PortMode.values();
        ItemPipeConfig cfg = new ItemPipeConfig();
        cfg.north = modes[(bits)      & 0x3];
        cfg.south = modes[(bits >> 2) & 0x3];
        cfg.east  = modes[(bits >> 4) & 0x3];
        cfg.west  = modes[(bits >> 6) & 0x3];
        return cfg;
    }
}