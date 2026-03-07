package com.ricedotwho.rsa.module.impl.dungeon.boss;

import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


@ModuleInfo(aliases = "Velocity Buffer", id = "Velocity Buffer", category = Category.MOVEMENT, hasKeybind = true)
public class VelocityBuffer extends Module {
    private final KeybindSetting popKey = new KeybindSetting("Queue Pop Key", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, this::popQueue));


    private static final Set<Class<? extends Packet<?>>> PACKET_SET = Set.of(
            ClientboundPingPacket.class
    );

    private final ConcurrentLinkedQueue<Packet<?>> queue = new ConcurrentLinkedQueue<>();


    public VelocityBuffer() {
        this.registerProperty(
                popKey
        );
    }


    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        synchronized (queue) {
            queue.clear();
            this.setEnabled(false);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        // Be careful here, there are threading issues which can cause order to be fucked
        // If we disable before we flush, packets can be sent before we have time to flush
        // If we flush before we disable, packets can get stuck in queue and once again only sent in the flush after we disable (hence above issue)

        if (Minecraft.getInstance().player == null) return;
        Packet<?> packet = event.getPacket();
        if (isMotionPacket(packet, Minecraft.getInstance().player)) {
            queue.add(packet);
            RSA.chat("Added to queue!");
            event.setCancelled(true);
            return;
        }
        if (!PACKET_SET.contains(packet.getClass())) return;

        synchronized (queue) {
            if (queue.isEmpty()) return;
            queue.add(packet);
        }
        event.setCancelled(true);
    }

    @Override
    public void onEnable() {
        ChatUtils.chat("OnEnable!");
        flush();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        flush();
        super.onDisable();
    }

    private void popQueue() {
        if (Minecraft.getInstance().player == null) return;
        synchronized (queue) {
            if (queue.isEmpty()) return;
            while (!queue.isEmpty()) {
                Packet<?> packet = queue.poll();
                ((Packet<ClientPacketListener>) packet).handle(Minecraft.getInstance().getConnection()); // prob should not put here, so we can still detect packets through events | u could just call the event bus, idt it matters tho

                if (isMotionPacket(packet, Minecraft.getInstance().player)) {
                    if (queue.stream().anyMatch(p -> isMotionPacket(p, Minecraft.getInstance().player))) break;
                    // Ik this gets called twice, but I want to make sure it still gets called if the implementation of onDisable changes
                    flush(); // Flush before ??
                    this.setEnabled(false);
                    break;
                }
            }
        }
        RSA.chat("Popped from queue!");
    }

    private boolean isMotionPacket(Packet<?> packet, LocalPlayer player) {
        return packet instanceof ClientboundSetEntityMotionPacket motionPacket && motionPacket.getId() == player.getId();
    }

    private void flush() {
        synchronized (queue) {
            if (!queue.isEmpty())
                queue.forEach(q -> ((Packet<ClientPacketListener>) q).handle(Minecraft.getInstance().getConnection()));
            this.queue.clear();
            ChatUtils.chat("Flushed!");
        }
    }

}
