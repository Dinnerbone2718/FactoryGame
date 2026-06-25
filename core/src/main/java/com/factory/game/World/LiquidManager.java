package com.factory.game.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.factory.game.Items.ItemStack;
import com.factory.game.Main;

public class LiquidManager {

    private static final float FLOW_RATE       = 10f;
    private static final float DEV_BARREL_RATE = 50f;

    public static final class CastDrop {
        public final ItemStack ingot;
        public final float     worldPixelX;
        public final float     worldPixelY;

        CastDrop(ItemStack ingot, float worldPixelX, float worldPixelY) {
            this.ingot       = ingot;
            this.worldPixelX = worldPixelX;
            this.worldPixelY = worldPixelY;
        }
    }

    private final List<CastDrop> pendingCastDrops = new ArrayList<>();

    public List<CastDrop> pollCastDrops() {
        List<CastDrop> result = new ArrayList<>(pendingCastDrops);
        pendingCastDrops.clear();
        return result;
    }

    public void update(float delta, Collection<Chunk> chunks) {

        Map<String, PlacedObject> machineLookup = new HashMap<>();
        Map<String, PlacedObject> pipeLookup    = new HashMap<>();
        Map<String, PlacedObject> mixerLookup   = new HashMap<>();
        Map<String, PlacedObject> distilleryLookup = new HashMap<>();

        
        for (Chunk chunk : chunks) {
            for (PlacedObject obj : chunk.getPlacedObjects()) {
                String key = obj.getX() + "," + obj.getY();
                if (obj.isPipe()) {
                    pipeLookup.put(key, obj);
                } else if (obj.isMixer()) {
                    mixerLookup.put(key, obj);
                } else if (obj.isLiquidMachine() || obj.type == PlacedObject.Type.PLANTER) {
                    int tw = PlacedObjectCache.getTileWidth(obj.type);
                    int th = PlacedObjectCache.getTileHeight(obj.type);
                    for (int dx = 0; dx < tw; dx++) {
                        for (int dy = 0; dy < th; dy++) {
                            machineLookup.put((obj.getX() + dx) + "," + (obj.getY() + dy), obj);
                        }
                    }
                } else if (obj.type == PlacedObject.Type.DISTILLERY) {
                    int tw = PlacedObjectCache.getTileWidth(obj.type);
                    int th = PlacedObjectCache.getTileHeight(obj.type);
                    for (int dx = 0; dx < tw; dx++) {
                        for (int dy = 0; dy < th; dy++) {
                            distilleryLookup.put((obj.getX() + dx) + "," + (obj.getY() + dy), obj);
                        }
                    }
                }

                if (obj.type == PlacedObject.Type.CAST) {
                    obj.updateCast(delta);
                    ItemStack ingot = obj.pollPendingIngot();
                    if (ingot != null) {
                        float wx = obj.getX() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
                        float wy = obj.getY() * Main.TILE_SCALE + Main.TILE_SCALE * 0.5f;
                        pendingCastDrops.add(new CastDrop(ingot, wx, wy));
                    }
                }
            }
        }

        tickProducers(delta, machineLookup);
        tickReceivers(delta, machineLookup);
        handleTrash(machineLookup);

        for (PlacedObject mixer : mixerLookup.values()) {
            mixer.updateMixerProcessing(delta);
        }

        for (PlacedObject distillery : distilleryLookup.values()) {
            distillery.updateDistilleryProcessing(delta);
        }

        if (!pipeLookup.isEmpty()) {
            flowAllNetworks(delta, pipeLookup, machineLookup, mixerLookup, distilleryLookup);
        }
    }

    private void tickReceivers(float delta, Map<String, PlacedObject> machineLookup) {
        for (PlacedObject obj : machineLookup.values()) {
            LiquidMachineDefinition def = LiquidMachineRegistry.get(obj.type);
            if (def == null || !def.isReceiver() || def.consumptionRate <= 0f) continue;
            LiquidTank tank = obj.getLiquidTank();
            if (tank == null || tank.isEmpty()) continue;
            tank.withdraw(def.consumptionRate * delta);
        }
    }

    private void tickProducers(float delta, Map<String, PlacedObject> machineLookup) {
        for (PlacedObject obj : machineLookup.values()) {
            LiquidTank tank = obj.getLiquidTank();
            if (tank == null || tank.getRole() != LiquidTank.Role.PRODUCER) continue;

            if (obj.type == PlacedObject.Type.DEVBARREL) {
                LiquidType selected = obj.getSelectedProducedLiquid();
                if (selected != null) {
                    tank.deposit(selected, DEV_BARREL_RATE * delta);
                }
                continue;
            }

            LiquidMachineDefinition def = LiquidMachineRegistry.get(obj.type);
            if (def != null && def.isProducer()) {
                tank.deposit(def.producedLiquid, def.productionRate * delta);
            }
        }
    }

    private void flowAllNetworks(float delta,
                                  Map<String, PlacedObject> pipeLookup,
                                  Map<String, PlacedObject> machineLookup,
                                  Map<String, PlacedObject> mixerLookup,
                                  Map<String, PlacedObject> distilleryLookup) {

        Set<String>       visited  = new HashSet<>();
        List<Set<String>> networks = new ArrayList<>();

        for (String startKey : pipeLookup.keySet()) {
            if (visited.contains(startKey)) continue;
            Set<String>   net   = new LinkedHashSet<>();
            Queue<String> queue = new ArrayDeque<>();
            queue.add(startKey);
            visited.add(startKey);
            while (!queue.isEmpty()) {
                String key = queue.poll();
                net.add(key);
                int[] pos = parseKey(key);
                enqueueIfPipe(pos[0] + 1, pos[1], pipeLookup, visited, queue);
                enqueueIfPipe(pos[0] - 1, pos[1], pipeLookup, visited, queue);
                enqueueIfPipe(pos[0], pos[1] + 1, pipeLookup, visited, queue);
                enqueueIfPipe(pos[0], pos[1] - 1, pipeLookup, visited, queue);
            }
            networks.add(net);
        }

        float maxTransfer = FLOW_RATE * delta;

        for (Set<String> network : networks) {
            flowNetwork(network, machineLookup, mixerLookup, pipeLookup, distilleryLookup, maxTransfer);
        }
    }

    private void flowNetwork(Set<String> network,
                              Map<String, PlacedObject> machineLookup,
                              Map<String, PlacedObject> mixerLookup,
                              Map<String, PlacedObject> pipeLookup,
                                Map<String, PlacedObject> distilleryLookup,
                              float maxTransfer) {

        Set<LiquidType> typesToRoute = gatherAvailableLiquidTypes(network, machineLookup, mixerLookup, distilleryLookup);
        if (typesToRoute.isEmpty()) return;

        for (LiquidType liquidType : typesToRoute) {
            Set<String> passable = buildPassableSet(network, pipeLookup, liquidType);
            if (passable.isEmpty()) continue;

            Set<String> componentVisited = new HashSet<>();
            for (String startKey : passable) {
                if (componentVisited.contains(startKey)) continue;

                Set<String>   component = new LinkedHashSet<>();
                Queue<String> queue     = new ArrayDeque<>();
                queue.add(startKey);
                componentVisited.add(startKey);
                while (!queue.isEmpty()) {
                    String key = queue.poll();
                    component.add(key);
                    int[] pos = parseKey(key);
                    for (int[] d : DIRS) {
                        String nkey = (pos[0] + d[0]) + "," + (pos[1] + d[1]);
                        if (!componentVisited.contains(nkey) && passable.contains(nkey)) {
                            componentVisited.add(nkey);
                            queue.add(nkey);
                        }
                    }
                }

                flowComponent(component, machineLookup, mixerLookup, distilleryLookup, liquidType, maxTransfer);
            }
        }
    }

    private Set<LiquidType> gatherAvailableLiquidTypes(Set<String> network,
                                                        Map<String, PlacedObject> machineLookup,
                                                        Map<String, PlacedObject> mixerLookup,
                                                        Map<String, PlacedObject> distilleryLookup) {
        Set<LiquidType> types   = new HashSet<>();
        Set<String>     checked = new HashSet<>();

        for (String pipeKey : network) {
            int[] pos = parseKey(pipeKey);
            for (int[] d : DIRS) {
                String adjKey = (pos[0] + d[0]) + "," + (pos[1] + d[1]);
                if (!checked.add(adjKey)) continue;

                PlacedObject machine = machineLookup.get(adjKey);
                if (machine != null) {
                    LiquidTank tank = machine.getLiquidTank();
                    if (tank != null && !tank.isEmpty()
                            && tank.getRole() != LiquidTank.Role.RECEIVER) {
                        types.add(tank.getType());
                    }
                }

                PlacedObject mixer = mixerLookup.get(adjKey);
                if (mixer != null) {
                    LiquidTank out = mixer.getMixerOutputTank();
                    if (out != null && !out.isEmpty() && out.getType() != null) {
                        types.add(out.getType());
                    }
                }

                PlacedObject distillery = distilleryLookup.get(adjKey);
                if (distillery != null) {
                    LiquidTank[] outputs = distillery.getDistilleryOutputTanks();
                    if (outputs != null)
                        for (LiquidTank t : outputs)
                            if (t != null && !t.isEmpty() && t.getType() != null)
                                types.add(t.getType());
                }


            }
        }
        return types;
    }

    private Set<String> buildPassableSet(Set<String> network,
                                          Map<String, PlacedObject> pipeLookup,
                                          LiquidType liquidType) {
        Set<String> passable = new HashSet<>();
        for (String pipeKey : network) {
            PlacedObject pipe = pipeLookup.get(pipeKey);
            if (pipe == null || !pipe.isFilterPipe()) {
                passable.add(pipeKey);
            } else {
                Set<LiquidType> allowed = pipe.getAllowedLiquidTypes();
                if (allowed == null || allowed.isEmpty() || allowed.contains(liquidType)) {
                    passable.add(pipeKey);
                }
            }
        }
        return passable;
    }

    private void flowComponent(Set<String> component,
                                Map<String, PlacedObject> machineLookup,
                                Map<String, PlacedObject> mixerLookup,
                                Map<String, PlacedObject> distilleryLookup,
                                LiquidType liquidType,
                                float maxTransfer) {

        List<LiquidTank> producers = new ArrayList<>();
        List<LiquidTank> receivers = new ArrayList<>();
        List<LiquidTank> storage   = new ArrayList<>();
        Set<String>      seen      = new HashSet<>();

        for (String pipeKey : component) {
            int[] pos = parseKey(pipeKey);
            for (int[] d : DIRS) {
                gatherAdjacentTanks(pos[0] + d[0], pos[1] + d[1],
                                    machineLookup, mixerLookup, distilleryLookup,
                                    seen, producers, receivers, storage);
            }
        }

        for (LiquidTank src : producers) {
            if (src.isEmpty() || src.getType() != liquidType) continue;
            for (LiquidTank dst : receivers) {
                if (src.isEmpty()) break;
                if (!dst.canAccept(liquidType)) continue;
                transfer(src, dst, liquidType, maxTransfer);
            }
            for (LiquidTank dst : storage) {
                if (src.isEmpty()) break;
                if (!dst.canAccept(liquidType)) continue;
                transfer(src, dst, liquidType, maxTransfer);
            }
        }

        for (LiquidTank src : storage) {
            if (src.isEmpty() || src.getType() != liquidType) continue;
            for (LiquidTank dst : receivers) {
                if (src.isEmpty()) break;
                if (!dst.canAccept(liquidType)) continue;
                transfer(src, dst, liquidType, maxTransfer);
            }
        }
    }

    private static final int[][] DIRS = {{1,0},{-1,0},{0,1},{0,-1}};

    private void gatherAdjacentTanks(int x, int y,
                                    Map<String, PlacedObject> machineLookup,
                                    Map<String, PlacedObject> mixerLookup,
                                    Map<String, PlacedObject> distilleryLookup,
                                    Set<String> seen,
                                    List<LiquidTank> producers,
                                    List<LiquidTank> receivers,
                                    List<LiquidTank> storage) {
        String key = x + "," + y;

        PlacedObject machine = machineLookup.get(key);
        String machineSeenKey = (machine != null)
                ? machine.getX() + "," + machine.getY()
                : key;
        
        if (machine != null) {
            if (seen.add(machineSeenKey)) {   
                LiquidTank tank = machine.getLiquidTank();
                if (tank != null) {
                    switch (tank.getRole()) {
                        case PRODUCER: producers.add(tank); break;
                        case RECEIVER: receivers.add(tank); break;
                        case STORAGE:  storage.add(tank);   break;
                    }
                }
                if (machine.type == PlacedObject.Type.PLANTER) {
                    LiquidTank[] inputs = machine.getPlanterInputTanks();
                    if (inputs != null) {
                        for (LiquidTank t : inputs) {
                            if (t != null) receivers.add(t);
                        }
                    }
                }
            }
        }

        PlacedObject mixer = mixerLookup.get(key);
        if (mixer != null && seen.add(key + "_mixer")) {
            LiquidTank[] inputs = mixer.getMixerInputTanks();
            if (inputs != null) {
                for (LiquidTank t : inputs) {
                    if (t != null) receivers.add(t);
                }
            }
            LiquidTank out = mixer.getMixerOutputTank();
            if (out != null) producers.add(out);
        }

        PlacedObject distillery = distilleryLookup.get(key);
        String distillerySeenKey = (distillery != null)
                ? distillery.getX() + "," + distillery.getY() + "_distillery"
                : key + "_distillery";
        if (distillery != null && seen.add(distillerySeenKey)) {
            LiquidTank[] inputs = distillery.getDistilleryInputTanks();
            if (inputs != null)
                for (LiquidTank t : inputs) if (t != null) receivers.add(t);

            LiquidTank[] outputs = distillery.getDistilleryOutputTanks();
            if (outputs != null)
                for (LiquidTank t : outputs) if (t != null) producers.add(t);
        }


    }

    private void transfer(LiquidTank src, LiquidTank dst, LiquidType type, float max) {
        float want   = Math.min(src.getAmount(), max);
        float actual = dst.deposit(type, want);
        src.withdraw(actual);
    }


    private void handleTrash(Map<String, PlacedObject> machineLookup) {
        for (PlacedObject obj : machineLookup.values()){
            if (obj.type != PlacedObject.Type.TRASH) continue;
            LiquidTank tank = obj.getLiquidTank();
            if (tank != null && !tank.isEmpty()) {
                tank.clear();
            }
        }

    }


    private void enqueueIfPipe(int x, int y,
                                Map<String, PlacedObject> pipeLookup,
                                Set<String> visited,
                                Queue<String> queue) {
        String key = x + "," + y;
        if (!visited.contains(key) && pipeLookup.containsKey(key)) {
            visited.add(key);
            queue.add(key);
        }
    }

    private static int[] parseKey(String key) {
        int comma = key.indexOf(',');
        return new int[]{
            Integer.parseInt(key.substring(0, comma)),
            Integer.parseInt(key.substring(comma + 1))
        };
    }
}