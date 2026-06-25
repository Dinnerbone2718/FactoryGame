package com.factory.game.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;

public final class ItemPipeNetwork {

    private static final float TICK_INTERVAL = 0.5f;

    private float timer = 0f;

    private Map<String, PlacedObject>   pipeLookup;
    private Map<String, PlacedObject>   objectLookup;
    private Map<String, ItemPipeConfig> configLookup;
    private Map<String, Inventory>      crateInventories;
    private CrusherManager              crusherManager;
    private SmelterManager              smelterManager;

    private final Map<String, ItemStack> pipeBuffers = new HashMap<>();

    public void init(Map<String, PlacedObject>   pipeLookup,
                     Map<String, PlacedObject>   objectLookup,
                     Map<String, ItemPipeConfig> configLookup,
                     Map<String, Inventory>      crateInventories) {
        this.pipeLookup       = pipeLookup;
        this.objectLookup     = objectLookup;
        this.configLookup     = configLookup;
        this.crateInventories = crateInventories;
    }

    public void setCrusherManager(CrusherManager crusherManager) {
        this.crusherManager = crusherManager;
    }

    public void setSmelterManager(SmelterManager smelterManager) {
        this.smelterManager = smelterManager;
    }

    public void update(float delta) {
        timer += delta;
        if (timer < TICK_INTERVAL) return;
        timer -= TICK_INTERVAL;
        tick();
    }

    public ItemStack getBuffer(String key) {
        return pipeBuffers.get(key);
    }

    public void onPipeRemoved(String key) {
        pipeBuffers.remove(key);
    }

    private void tick() {
        if (pipeLookup == null || configLookup == null || crateInventories == null) return;
        if (pipeLookup.isEmpty()) return;

        Set<String> visited = new HashSet<>();

        for (Map.Entry<String, PlacedObject> entry : pipeLookup.entrySet()) {
            String key = entry.getKey();
            if (visited.contains(key)) continue;
            List<String> network = collectNetwork(key, visited);
            tickNetwork(network);
        }
    }

    private List<String> collectNetwork(String startKey, Set<String> globalVisited) {
        List<String> network = new ArrayList<>();
        Set<String>  local   = new HashSet<>();
        List<String> queue   = new ArrayList<>();
        queue.add(startKey);

        while (!queue.isEmpty()) {
            String k = queue.remove(queue.size() - 1);
            if (!local.add(k)) continue;
            globalVisited.add(k);
            network.add(k);

            int[] pos = parseKey(k);
            if (pos == null) continue;
            int x = pos[0], y = pos[1];

            String[] neighbors = {
                x + "," + (y + 1),
                x + "," + (y - 1),
                (x + 1) + "," + y,
                (x - 1) + "," + y
            };
            for (String n : neighbors) {
                if (!local.contains(n) && pipeLookup.containsKey(n)) {
                    queue.add(n);
                }
            }
        }
        return network;
    }

    private void tickNetwork(List<String> pipeKeys) {
        Set<String> network = new HashSet<>(pipeKeys);


        for (String pipeKey : pipeKeys) {
            ItemStack buffered = pipeBuffers.get(pipeKey);
            if (buffered == null) continue;

            ItemPipeConfig cfg = configLookup.get(pipeKey);
            if (cfg == null) continue;

            int[] pos = parseKey(pipeKey);
            if (pos == null) continue;
            int px = pos[0], py = pos[1];

            int[][]                   dirs  = { {0,1}, {0,-1}, {1,0}, {-1,0} };
            ItemPipeConfig.PortMode[] ports = {
                cfg.getNorth(), cfg.getSouth(), cfg.getEast(), cfg.getWest()
            };

            for (int i = 0; i < dirs.length; i++) {
                if (ports[i] != ItemPipeConfig.PortMode.OUTPUT) continue;

                int    nx = px + dirs[i][0];
                int    ny = py + dirs[i][1];
                String nk = nx + "," + ny;

                if (network.contains(nk)) {
                    if (!pipeBuffers.containsKey(nk)) {
                        pipeBuffers.put(nk, buffered);
                        pipeBuffers.remove(pipeKey);
                        break;
                    }
                } else {
                    PlacedObject neighbor = objectLookup.get(nk);

                    if (neighbor != null && neighbor.type == PlacedObject.Type.CRUSHER
                            && crusherManager != null) {
                        if (crusherManager.pushToInput(neighbor, buffered.getItem())) {
                            pipeBuffers.remove(pipeKey);
                            break;
                        }
                    } else if (neighbor != null && neighbor.type == PlacedObject.Type.SMELTER
                            && smelterManager != null) {
                        if (smelterManager.pushToInput(neighbor, buffered.getItem())) {
                            pipeBuffers.remove(pipeKey);
                            break;
                        }
                    } else {
                        Inventory dst = getCrateInventory(nx, ny);
                        if (dst != null && dst.addItem(buffered.getItem(), 1)) {
                            pipeBuffers.remove(pipeKey);
                            break;
                        }
                    }
                }
            }
        }


        for (String pipeKey : pipeKeys) {
            if (pipeBuffers.containsKey(pipeKey)) continue;

            ItemPipeConfig cfg = configLookup.get(pipeKey);
            if (cfg == null) continue;

            PlacedObject selfPipe = pipeLookup.get(pipeKey);
            Set<Item> allowedItems = (selfPipe != null && selfPipe.isFilterItemPipe())
                    ? selfPipe.getAllowedItemTypes() : null;

            int[] pos = parseKey(pipeKey);
            if (pos == null) continue;
            int px = pos[0], py = pos[1];

            int[][]                   dirs  = { {0,1}, {0,-1}, {1,0}, {-1,0} };
            ItemPipeConfig.PortMode[] ports = {
                cfg.getNorth(), cfg.getSouth(), cfg.getEast(), cfg.getWest()
            };

            for (int i = 0; i < dirs.length; i++) {
                if (ports[i] != ItemPipeConfig.PortMode.INPUT) continue;

                int    nx = px + dirs[i][0];
                int    ny = py + dirs[i][1];
                String nk = nx + "," + ny;

                if (network.contains(nk)) continue;

                PlacedObject neighbor = objectLookup.get(nk);
                if (neighbor != null && neighbor.type == PlacedObject.Type.CRUSHER
                        && crusherManager != null) {
                    ItemStack pulled = crusherManager.pullFromOutput(neighbor);
                    if (pulled != null) {
                        if (isAllowed(pulled.getItem(), allowedItems)) {
                            pipeBuffers.put(pipeKey, pulled);
                            break;
                        } else {
                            crusherManager.pushToInput(neighbor, pulled.getItem());
                        }
                    }
                } else {
                    Inventory src = getCrateInventory(nx, ny);
                    if (src == null) continue;
                    ItemStack first = firstAllowedItem(src, allowedItems);
                    if (first != null && src.removeItem(first.getItem(), 1)) {
                        pipeBuffers.put(pipeKey, new ItemStack(first.getItem(), 1));
                        break;
                    }
                }
            }
        }
    }

    public Map<String, ItemStack> getBuffers() {
        return pipeBuffers;
    }

    private ItemStack firstAllowedItem(Inventory inv, Set<Item> allowed) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack s = inv.getSlot(i);
            if (s != null && s.getQuantity() > 0 && isAllowed(s.getItem(), allowed)) return s;
        }
        return null;
    }

    private boolean isAllowed(Item item, Set<Item> allowed) {
        return allowed == null || allowed.isEmpty() || allowed.contains(item);
    }

    private Inventory getCrateInventory(int x, int y) {
        String       key = x + "," + y;
        PlacedObject obj = objectLookup.get(key);

        if (obj == null) {
            obj = findMultiTileObjectAt(x, y);
        }

        if (obj == null) return null;
        if (obj.type != PlacedObject.Type.STORAGE_CRATE
        && obj.type != PlacedObject.Type.FOREST_CRATE
        && obj.type != PlacedObject.Type.DESERT_CRATE
        && obj.type != PlacedObject.Type.MOUNTAIN_CRATE
        && obj.type != PlacedObject.Type.PLANTER
        && obj.type != PlacedObject.Type.ORE_DRILL) return null;

        String originKey = obj.getX() + "," + obj.getY();
        return crateInventories.get(originKey);
    }

    private PlacedObject findMultiTileObjectAt(int x, int y) {
        int radius = 3;
        for (int ox = x - radius; ox <= x; ox++) {
            for (int oy = y - radius; oy <= y; oy++) {
                String candidate = ox + "," + oy;
                PlacedObject obj = objectLookup.get(candidate);
                if (obj == null || obj.type != PlacedObject.Type.PLANTER) continue;
                int tw = PlacedObjectCache.getTileWidth(obj.type);
                int th = PlacedObjectCache.getTileHeight(obj.type);
                if (x >= obj.getX() && x < obj.getX() + tw
                && y >= obj.getY() && y < obj.getY() + th) {
                    return obj;
                }
            }
        }
        return null;
    }

    private static int[] parseKey(String key) {
        int comma = key.indexOf(',');
        if (comma < 0) return null;
        try {
            int x = Integer.parseInt(key.substring(0, comma));
            int y = Integer.parseInt(key.substring(comma + 1));
            return new int[]{ x, y };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}