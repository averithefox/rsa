package com.ricedotwho.rsa.module.impl.dungeon.boss;

import com.ricedotwho.rsa.IMixin.IConnection;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.FakeKeyboardInput;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.recorder.MovementRecorder;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings.BlinkRing;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.mixins.accessor.LocalPlayerAccessor;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@ModuleInfo(aliases = "BaldingBlink", id = "BaldingBlink", category = Category.MOVEMENT, hasKeybind = true)
public class BaldingBlink extends Module {
    private static BaldingBlink INSTANCE;

    private final DragSetting gui = new DragSetting("Balding Blink Hud", new Vector2d(100, 100), new Vector2d(144, 80));
    private final NumberSetting maxBlinkPacket = new NumberSetting("Max Blink Ticks", 1, 30, 17, 1);
    @Setter
    private BlinkRing currentRing;


    private final LinkedList<Packet<?>> queue = new LinkedList<>();
    @Getter
    private boolean flushing = false;
    private int packetCount = 0;


    public BaldingBlink() {
        this.registerProperty(
                maxBlinkPacket,
                gui
        );
    }

    @SubscribeEvent
    public void onRenderGui(Render2DEvent event) {
        gui.renderScaled(event.getGfx(), () -> event.getGfx().drawCenteredString(Minecraft.getInstance().font, ("Packets : " + packetCount), (int) gui.getPosition().x, (int) gui.getPosition().y, 0xFFFFFFFF), 10, 10);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        synchronized (queue) {
            this.packetCount = 0;
            queue.clear();
            if (this.isEnabled())
                this.setEnabled(false);
        }
    }

    public void flushIfInRing() {
        if (this.currentRing == null || !this.isEnabled() || this.isFlushing()) return;
        this.currentRing.flush();
    }

    public static boolean onSendPacket(Packet<?> packet) {
        if (INSTANCE == null) INSTANCE = RSM.getModule(BaldingBlink.class);
        // if the instance is still null after this, the module should probably just be disabled as well
        return INSTANCE != null && INSTANCE.onPreSendPacket(packet);
    }

    public void blinkMovement(List<MovementRecorder.PlayerInput> movements) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null || Minecraft.getInstance().getConnection() == null || packetCount < 1 || movements.isEmpty()) return;
        int tickCount = Math.min(movements.size(), packetCount);
        if (movements.size() > packetCount) {
            movements = movements.subList(0, packetCount);
        }


        LocalPlayer player = Minecraft.getInstance().player;

        LocalPlayer copy = player;
        ClientInput oldInputs = copy.input;
        copy.input = new FakeKeyboardInput(Minecraft.getInstance().options);

        boolean bl = flushing;
        flushing = true;
        for (int i = 0; i < tickCount; i++) {
            MovementRecorder.PlayerInput input = movements.get(i);
            copy.input.keyPresses = input.input();

            copy.setYRot(input.yaw);
            copy.setXRot(input.pitch);
            copy.tick();
            Minecraft.getInstance().getConnection().send(new ServerboundClientTickEndPacket());
            packetCount--;
        }
        flushing = bl;

        copy.input = oldInputs;
    }


    private boolean sentMove = false;
    private Vec3 awaited;

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket positionPacket) {
            awaited = positionPacket.change().position();
        }
    }

    private boolean onPreSendPacket(Packet<?> packet) {
        if (packet instanceof ServerboundPongPacket && (!this.isEnabled() || flushing)) {
            ChatUtils.chat(((ServerboundPongPacket) packet).getId());
        }

        if (Minecraft.getInstance().player == null || !this.isEnabled() || flushing) return false;
        if (packet instanceof ServerboundMovePlayerPacket movePlayerPacket && awaited != null) {
            if (movePlayerPacket.hasPosition() && movePlayerPacket.getX(0d) == awaited.x && movePlayerPacket.getY(0d) == awaited.y && movePlayerPacket.getZ(0d) == awaited.z) {

                this.flush();
                actuallySendImmediately(movePlayerPacket);
                this.setEnabled(false);
                awaited = null;
                return true;
            }
        }

        if (packet instanceof ServerboundPongPacket) {
            queue.add(packet);

            if (queue.size() > packetCount) {
                Packet<?> ping = queue.removeFirst();
                actuallySendImmediately(ping);
            }
            return true;
        }

        if (packetCount >= maxBlinkPacket.getValue().intValue()) return false;

        LocalPlayerAccessor accessor = (LocalPlayerAccessor) Minecraft.getInstance().player;

        if (packet instanceof ServerboundClientTickEndPacket) {
            if (!sentMove) {
                int reminder = accessor.getPositionReminder();
                if (reminder > 0) {
                    accessor.setPositionReminder(reminder - 1);
                }
                packetCount++;
                return true;
            }
            sentMove = false;
            return false;
        }



        if (packet instanceof ServerboundMovePlayerPacket movePacket) {
//            sentMove = true;
//            return false;
            if (movePacket.hasPosition()) {
                sentMove = true;
                return false;
            }
            return true;
        }

        if (packet instanceof ServerboundPlayerInputPacket) {
            sentMove = true;
            return false;
        }
        return false;
    }

    public int getChargedCount() {
        // PacketCount is 1 lower than C03 count
        return this.packetCount;
    }

    public void actuallySendImmediately(Packet<?> packet) {
        if (Minecraft.getInstance().getConnection() == null) return;

        synchronized (queue) { // Need to block, to make sure that other threads don't modify flushing
            this.flushing = true;
            ((IConnection) Minecraft.getInstance().getConnection().getConnection()).sendPacketImmediately(packet);
            this.flushing = false;
        }
    }

    public void enableFlush() {
        this.flushing = true;
    }

    public void disableFlush() {
        this.flushing = false;
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
        currentRing = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ChatUtils.chat("Packets : " + packetCount);
        List<MovementRecorder.PlayerInput> inputs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            inputs.add(new MovementRecorder.PlayerInput(0f, 0f, true, false, false, false, false, false, true));
        }

        this.blinkMovement(inputs);
        //this.queue.stream().forEach(p -> System.out.println(p.getClass()));
        this.flush();
        currentRing = null;
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
