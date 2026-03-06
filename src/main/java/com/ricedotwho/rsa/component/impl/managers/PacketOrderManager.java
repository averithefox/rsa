package com.ricedotwho.rsa.component.impl.managers;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@UtilityClass
public class PacketOrderManager {
    private final HashMap<STATE, ConcurrentLinkedQueue<Runnable>> packets = new HashMap<>();

    public enum STATE {
        ITEM_USE,
        ATTACK
    }

    public void register(STATE state, Runnable runnable) {
        if (!packets.containsKey(state)) packets.put(state, new ConcurrentLinkedQueue<>());
        packets.get(state).add(runnable);
    }

    public void execute(STATE state) {
        ConcurrentLinkedQueue<Runnable> tasks = packets.get(state);
        if (tasks == null) return;
        Runnable r;
        while ((r = tasks.poll()) != null) {
            r.run();
        }
    }

}
