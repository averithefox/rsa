package com.ricedotwho.rsa.component.impl.managers;

import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class PacketOrderManager {
    private static final ConcurrentHashMap<STATE, List<Runnable>> packets = new ConcurrentHashMap<>();
    private static final List<Predicate<Packet<?>>> receiveListeners = new ArrayList<>(4);

    private PacketOrderManager() {

    }

    public enum STATE {
        START,
        ITEM_USE,
        ATTACK
    }

    public static void onPreTickStart() {
        execute(STATE.START);
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

    public static void registerReceiveListener(Predicate<Packet<?>> listener) {
        synchronized (receiveListeners) {
            receiveListeners.add(listener);
        }
    }

    public static void onPreReceivePacket(Packet<?> packet) {
        synchronized (receiveListeners) {
            if (receiveListeners.isEmpty()) return;
            receiveListeners.removeIf(predicate -> predicate.test(packet));
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
