package com.ricedotwho.rsa.module.impl.dungeon.boss;

import com.ricedotwho.rsa.IMixin.IConnection;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings.BlinkRing;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

import java.util.LinkedList;
import java.util.stream.IntStream;


@ModuleInfo(aliases = "Blink", id = "Blink", category = Category.MOVEMENT, hasKeybind = true)
public class Blink extends Module {
    private static Blink INSTANCE;
    private Vec3 lastMove;

    private final DragSetting gui = new DragSetting("Blink Hud", new Vector2d(100, 100), new Vector2d(144, 80));
    private final NumberSetting maxBlinkPacket = new NumberSetting("Max Blink Ticks", 1, 30, 17, 1);
    @Setter
    private BlinkRing currentRing;


    private final LinkedList<Packet<?>> queue = new LinkedList<>();
    @Getter
    private boolean flushing = false;
    private int packetCount = 0;


    public Blink() {
        this.registerProperty(
                maxBlinkPacket,
                gui
        );
    }

    @SubscribeEvent
    public void onRenderGui(Render2DEvent event) {
        if (queue.isEmpty()) return;
        gui.renderScaled(event.getGfx(), () -> event.getGfx().drawCenteredString(Minecraft.getInstance().font, "Blinking", (int) gui.getPosition().x, (int) gui.getPosition().y, 0xFFFFFFFF), 10, 10);
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundPongPacket) {
            System.out.println(((ServerboundPongPacket) event.getPacket()).getId());
        }
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket movePlayerPacket)) return;
        if (!movePlayerPacket.hasPosition()) return;
        lastMove = new Vec3(movePlayerPacket.getX(0d), movePlayerPacket.getY(0d), movePlayerPacket.getZ(0d));
    }

    private boolean inputEquals(Input input1, Input input2) {
        return input1.shift() == input2.shift()
                && input1.forward() == input2.forward()
                && input1.backward() == input2.backward()
                && input1.left() == input2.left()
                && input1.right() == input2.right()
                && input1.jump() == input2.jump()
                && input1.sprint() == input2.sprint();
    }


    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        synchronized (queue) {
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
        if (INSTANCE == null) INSTANCE = RSM.getModule(Blink.class);
        // if the instance is still null after this, the module should probably just be disabled as well
        return INSTANCE != null && INSTANCE.onPreSendPacket(packet);
    }

    // Holy schizo
    private boolean onPreSendPacket(Packet<?> packet) {
        if (Minecraft.getInstance().player == null || !this.isEnabled()) return false;
        synchronized (queue) {
            if (flushing) return false;
//            if (packet instanceof ServerboundUseItemOnPacket || packet instanceof ServerboundUseItemPacket || packet instanceof ServerboundInteractPacket) {
//                return false;
//            }
            boolean bl = true;
            if (currentRing != null && (packetCount >= maxBlinkPacket.getValue().intValue() || currentRing.isDonePlaying())) {
                if (packet instanceof ServerboundMovePlayerPacket || packet instanceof ServerboundPlayerInputPacket) return true;
                if (packet instanceof ServerboundAcceptTeleportationPacket) {
                    if (this.isEnabled()) this.onKeyToggle();
                    return false;
                }
                if (packet instanceof ServerboundPongPacket) {
                    queue.add(packet);
                    int firstPong = -1;
                    int index = 0;
                    for (Packet<?> p : queue) {
                        if (p instanceof ServerboundPongPacket) {
                            firstPong = index;
                            break;
                        }
                        index++;
                    }

                    if (firstPong == -1) {
                        if (this.isEnabled()) this.onKeyToggle();
                        return false;
                    }

                    Packet<?> ping = queue.remove(firstPong);
                    actuallySendImmediately(ping);
                    return true;
                }
                // This should probably check for other packets...
                // Cancel blink if we get a velocity or teleport
                // Let autoterms through aand shit
            }

            if (packet instanceof ServerboundClientTickEndPacket) {
                packetCount++;
                if (currentRing != null) {
                    if (packetCount >= maxBlinkPacket.getValue().intValue()) {
                        bl = false;
                        packetCount--;
                    }
                } else {
                    if (packetCount >= maxBlinkPacket.getValue().intValue()) {
                        this.onKeyToggle();
                        return false;
                    }
                }
            }
            if (bl) {
                queue.add(packet);
                return true;
            }
            return false;
        }
    }

    public Vec3 getServerPosition() {
        return lastMove;
    }

    public int getChargedCount() {
        // PacketCount is 1 lower than C03 count
        return this.packetCount;
    }

    public void clearMovements() {
        queue.removeIf(p -> (p instanceof ServerboundPlayerInputPacket || p instanceof ServerboundMovePlayerPacket));
    }

//    public boolean replaceMovements(List<BlinkTick> ticks) {
//        if (Minecraft.getInstance().player == null) return false;
//        synchronized (queue) {
//            if (queue.isEmpty() || !this.isEnabled() || flushing) return false;
//            if (ticks.size() - 1 > getChargedCount() || ticks.isEmpty()) return false;
//            queue.removeIf(p -> (p instanceof ServerboundPlayerInputPacket || p instanceof ServerboundMovePlayerPacket));
//
//            int j = 0;
//
//            for (int i = 0; i < queue.size(); i++) {
//                int next = i + 1;
//                if (next >= queue.size()) continue;
//                Packet<?> nextPacket = queue.get(next);
//                if (!(nextPacket instanceof ServerboundClientTickEndPacket)) continue;
//
//                if (j >= ticks.size()) {
//                    j++;
//
//                    continue;
//                }
//                BlinkTick blinkTick = ticks.get(j++);
//                if (blinkTick.getInput() != null) {
//                    ServerboundPlayerInputPacket inputPacket = new ServerboundPlayerInputPacket(blinkTick.getInput());
//                    queue.add(i++, inputPacket);
//                }
//
//                queue.add(i++, blinkTick.getMovePacket());
//            }
//
//            if (j >= ticks.size()) return true;
//
//            BlinkTick blinkTick = ticks.get(j);
//
//            if (blinkTick.getInput() != null) {
//                ServerboundPlayerInputPacket inputPacket = new ServerboundPlayerInputPacket(blinkTick.getInput());
//                queue.add(inputPacket);
//            }
//
//            queue.add(blinkTick.getMovePacket());
//            return true;
//        }
//    }

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
        if (Minecraft.getInstance().player != null)
            lastMove = Minecraft.getInstance().player.position();
        this.packetCount = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ChatUtils.chat("Packets : " + this.queue.stream().filter(p -> p instanceof ServerboundMovePlayerPacket).count());
        //this.queue.stream().forEach(p -> System.out.println(p.getClass()));
        this.flush();
        currentRing = null;
        lastMove = null;
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
