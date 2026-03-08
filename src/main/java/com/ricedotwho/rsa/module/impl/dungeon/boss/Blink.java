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
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.sounds.SoundEvents;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


@ModuleInfo(aliases = "Blink", id = "Blink", category = Category.MOVEMENT, hasKeybind = true)
public class Blink extends Module {

    private final DragSetting gui = new DragSetting("Blink Hud", new Vector2d(100, 100), new Vector2d(144, 80));


    private final ConcurrentLinkedQueue<Packet<?>> queue = new ConcurrentLinkedQueue<>();
    private boolean flushing = false;


    public Blink() {
        this.registerProperty(
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

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (Minecraft.getInstance().player == null || flushing) return;
        queue.add(event.getPacket());
        event.setCancelled(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.flush();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.flush();
    }

    private void flush() {
        // Assumes module is disabled
        if (Minecraft.getInstance().getConnection() == null) return;
        flushing = true;
        synchronized (queue) {
            if (queue.isEmpty()) {
                flushing = false;
                return;
            }
            queue.forEach(packet -> {
                ((IConnection) Minecraft.getInstance().getConnection().getConnection()).sendPacketImmediately(packet);
            });
            this.queue.clear();
        }
        flushing = false;
    }

}
