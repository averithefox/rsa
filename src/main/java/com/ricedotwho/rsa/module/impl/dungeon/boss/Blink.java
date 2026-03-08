package com.ricedotwho.rsa.module.impl.dungeon.boss;

import com.ricedotwho.rsa.IMixin.IConnection;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.sounds.SoundEvents;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


@ModuleInfo(aliases = "Blink", id = "Blink", category = Category.MOVEMENT, hasKeybind = true)
public class Blink extends Module {
    private static Blink INSTANCE;

    private final DragSetting gui = new DragSetting("Blink Hud", new Vector2d(100, 100), new Vector2d(144, 80));
    private final NumberSetting maxBlinkPacket = new NumberSetting("Max Blink Ticks", 1, 30, 17, 1);
    private final BooleanSetting flushOldSetting = new BooleanSetting("Flush Old", false);

    @Getter
    @Setter
    private boolean flushOld = true;

    private final ConcurrentLinkedQueue<Packet<?>> queue = new ConcurrentLinkedQueue<>();
    private boolean flushing = false;
    private int packetCount = 0;


    public Blink() {
        this.registerProperty(
                maxBlinkPacket,
                flushOldSetting,
                gui
        );
    }

//    @SubscribeEvent
//    public void onRenderGui(Render2DEvent event) {
//        if (queue.isEmpty()) return;
//        gui.renderScaled(event.getGfx(), () -> event.getGfx().drawCenteredString(Minecraft.getInstance().font, "Buffered Packets : " + bufferedCount, 0, 0, 0xFFFFFFFF), 10, 10);
//    }


    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        synchronized (queue) {
            queue.clear();
            if (this.isEnabled())
                this.setEnabled(false);
        }
    }

    public static boolean onSendPacket(Packet<?> packet) {
        if (INSTANCE == null) INSTANCE = RSM.getModule(Blink.class);
        return INSTANCE.onPreSendPacket(packet);
    }

    private boolean onPreSendPacket(Packet<?> packet) {
        if (Minecraft.getInstance().player == null || !this.isEnabled()) return false;
        synchronized (queue) {
            if (flushing) return false;
//            if (packet instanceof ServerboundUseItemOnPacket || packet instanceof ServerboundUseItemPacket || packet instanceof ServerboundInteractPacket) {
//                return false;
//            }

            if (packet instanceof ServerboundClientTickEndPacket) {
                packetCount++;
                if (flushOldSetting.getValue()) {
                    while (packetCount >= maxBlinkPacket.getValue().intValue()) {
                        this.flushTick();
                        packetCount--;
                    }
                } else {
                    if (packetCount >= maxBlinkPacket.getValue().intValue()) {
                        this.onKeyToggle();
                        return false;
                    }
                }
            }
            queue.add(packet);
            return true;
        }
    }

    public void actuallySend(Packet<?> packet) {
        if (Minecraft.getInstance().getConnection() == null) return;

        synchronized (queue) { // Need to block, to make sure that other threads don't modify flushing
            this.flushing = true;
            Minecraft.getInstance().getConnection().send(packet);
            this.flushing = true;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.flush();
        this.packetCount = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ChatUtils.chat("Packets : " + this.queue.stream().filter(p -> p instanceof ServerboundMovePlayerPacket).count());
        this.flush();
        this.packetCount = 0;
    }

    private void flushTick() {
        if (Minecraft.getInstance().getConnection() == null) return;
        synchronized (queue) {
            flushing = true;
            if (queue.isEmpty()) {
                flushing = false;
                this.setEnabled(false);
                return;
            }

            while (!queue.isEmpty()) {
                Packet<?> packet = queue.poll();
                ((IConnection) Minecraft.getInstance().getConnection().getConnection()).sendPacketImmediately(packet);
                if (packet instanceof ServerboundClientTickEndPacket) {
                    flushing = false;
                    return;
                }
            }
            flushing = false;
        }
    }

    private void flush() {
        if (Minecraft.getInstance().getConnection() == null) return;
        synchronized (queue) {
            flushing = true;
            if (queue.isEmpty()) {
                flushing = false;
                return;
            }
            queue.forEach(packet -> {
                ((IConnection) Minecraft.getInstance().getConnection().getConnection()).sendPacketImmediately(packet);
            });
            this.queue.clear();
            flushing = false;
        }
    }

}
