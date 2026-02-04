package com.ricedotwho.rsa.component.impl.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PacketOrderManager {
    private static final HashMap<STATE, List<Runnable>> packets = new HashMap<>();
    private PacketOrderManager() {

    }

    public enum STATE {
        ITEM_USE
    }

    public static void register(Runnable runnable, STATE state) {
        if (!packets.containsKey(state)) packets.put(state, new ArrayList<>());
        packets.get(state).add(runnable);
    }

    public static void execute(STATE state) {
        if (!packets.containsKey(state)) return;
        List<Runnable> runnables = packets.get(state);
        if (runnables.isEmpty()) return;
        runnables.forEach(Runnable::run);
        runnables.clear();
    }

}
