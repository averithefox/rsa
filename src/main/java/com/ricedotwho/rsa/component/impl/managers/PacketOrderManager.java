package com.ricedotwho.rsa.component.impl.managers;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PacketOrderManager {
    private static final ConcurrentHashMap<STATE, List<Runnable>> packets = new ConcurrentHashMap<>();
    private PacketOrderManager() {

    }

    public enum STATE {
        ITEM_USE,
        ATTACK
    }

    public static void register(STATE state, Runnable runnable) {
        synchronized (packets) {
            if (!packets.containsKey(state)) packets.put(state, new ArrayList<>());
        }

        List<Runnable> list = packets.get(state);
        synchronized (list) {
            list.add(runnable);
        }
    }

    public static void execute(STATE state) {
        if (!packets.containsKey(state)) return;

        List<Runnable> runnables = packets.get(state);
        synchronized (runnables) {
            if (runnables.isEmpty()) return;
            runnables.forEach(Runnable::run);
            runnables.clear();
        }
    }

}
