package com.ricedotwho.rsa.module.impl.dungeon.boss;

import com.ricedotwho.rsa.IMixin.IConnection;
import com.ricedotwho.rsa.utils.SoundPlayer;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.sounds.SoundEvents;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


@ModuleInfo(aliases = "Velocity Buffer", id = "Velocity Buffer", category = Category.MOVEMENT, hasKeybind = true)
public class VelocityBuffer extends Module {
    private static VelocityBuffer INSTANCE;

    private final KeybindSetting popKey = new KeybindSetting("Queue Pop Key", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, this::popQueue));
    private final DragSetting gui = new DragSetting("Velocity Buffer Hud", new Vector2d(100, 100), new Vector2d(144, 80));
    private int bufferedCount = 0;


    private static final Set<Class<? extends Packet<?>>> PACKET_SET = Set.of(
            ClientboundPingPacket.class
//            ClientboundKeepAlivePacket.class,
//            ClientboundBundlePacket.class, // ???
//            ClientboundBundleDelimiterPacket.class // ???
    );

    private final ConcurrentLinkedQueue<Packet<?>> queue = new ConcurrentLinkedQueue<>();


    public VelocityBuffer() {
        this.registerProperty(
                popKey,
                gui
        );
    }

    @SubscribeEvent
    public void onRenderGui(Render2DEvent event) {
        if (queue.isEmpty()) return;
        gui.renderScaled(event.getGfx(), () -> event.getGfx().drawCenteredString(Minecraft.getInstance().font, "Buffered Packets : " + bufferedCount, 0, 0, 0), 10, 10);
    }


    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        synchronized (queue) {
            queue.clear();
            this.bufferedCount = 0;
            this.setEnabled(false);
        }
    }

    public static boolean onReceivePacketPre(Packet<?> packet) {
        if (INSTANCE == null) INSTANCE = RSM.getModule(VelocityBuffer.class);
        return INSTANCE.onReceivePacket(packet);
    }

    private boolean onReceivePacket(Packet<?> packet) {
        if (Minecraft.getInstance().player == null || !this.isEnabled()) return false;
        if (packet instanceof ClientboundPlayerPositionPacket) {
            this.onKeyToggle();
            return false;
        }

        if (isMotionPacket(packet, Minecraft.getInstance().player)) {
            queue.add(packet);
            bufferedCount++;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 0.5f));
            //ChatUtils.chat("Added to queue!");
            return true;
        }
        if (!PACKET_SET.contains(packet.getClass())) return false;

        synchronized (queue) {
            if (queue.isEmpty()) return false;
            queue.add(packet);
        }
        return true;
    }

    @Override
    public void onEnable() {
        synchronized (queue) {
            this.queue.clear();
        }
//        for (int i = 0; i < 10; i++) {
//            System.out.println(" ");
//        }
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
                this.receivePacket(packet);

                if (isMotionPacket(packet, Minecraft.getInstance().player)) {
                    bufferedCount--;
                    if (queue.stream().anyMatch(p -> isMotionPacket(p, Minecraft.getInstance().player))) break;
                    // Ik this gets called twice, but I want to make sure it still gets called if the implementation of onDisable changes
                    flush();

                    if (this.isEnabled())
                        this.onKeyToggle();
                    break;
                }
            }
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 2f, 2f));
        //ChatUtils.chat("Popped from queue!");
    }


    private void receivePacket(Packet<?> packet) {
        if (Minecraft.getInstance().getConnection() == null) return;
        ((IConnection) Minecraft.getInstance().getConnection().getConnection()).receivePacket(packet);
    }

    private boolean isMotionPacket(Packet<?> packet, LocalPlayer player) {
        return packet instanceof ClientboundSetEntityMotionPacket motionPacket && motionPacket.getId() == player.getId();
    }

    private void flush() {
        synchronized (queue) {
            if (!queue.isEmpty())
                queue.forEach(this::receivePacket);
            this.queue.clear();
        }
        this.bufferedCount = 0;
    }

}
