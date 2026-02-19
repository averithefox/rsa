package com.ricedotwho.rsa.module.impl.dungeon.boss.p3;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autoterms.AutoTerms;
import com.ricedotwho.rsa.utils.InteractUtils;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


@ModuleInfo(aliases = "Term Aura", id = "Term Aura", category = Category.DUNGEONS)
public class TermAura extends Module {
    private static final double AURA_RANGE = 4d; // Vanilla is 3.0F
    private static final double AURA_RANGE_SQ = AURA_RANGE * AURA_RANGE;

    private final NumberSetting delay = new NumberSetting("Delay", 50d, 5000d, 500d, 50d);
    private final BooleanSetting showArmorStands = new BooleanSetting("Show Hitboxes", true);
    private final BooleanSetting forceSkyblock = new BooleanSetting("Force Skyblock", false);

    private long lastClick = 0L;

    public TermAura() {
        registerProperty(
                delay,
                showArmorStands,
                forceSkyblock
        );
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, this::rapeArmorstands);
    }

    private void rapeArmorstands() {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null || Minecraft.getInstance().getConnection() == null) return;
        if (System.currentTimeMillis() - lastClick < delay.getValue()) return;
        if (!locationCheck()) return;
        if (AutoTerms.isInTerminal() || Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) return;

        Vec3 eyePos = Minecraft.getInstance().player.position().add(0.0d, Minecraft.getInstance().player.getEyeHeight(), 0.0d);

        double bestDistance = AURA_RANGE_SQ;
        ArmorStand bestCandidate = null;

        Vec3 retardedPos = Minecraft.getInstance().player.position().add(0, -2, 0);

        AABB box = new AABB(retardedPos, retardedPos).inflate(AURA_RANGE, AURA_RANGE, AURA_RANGE);
        for (ArmorStand stand : Minecraft.getInstance().level.getEntitiesOfClass(ArmorStand.class, box, TermAura::filterEntities)) {
            double distance = stand.position().distanceToSqr(retardedPos);
            if (distance <= bestDistance) {
                bestCandidate = stand;
                bestDistance = distance;
            }
        }

        if (bestCandidate == null) return;
        ChatUtils.chat(bestDistance);


        Vec3 vec3 = clamp(bestCandidate.getBoundingBox(), eyePos).subtract(bestCandidate.getX(), bestCandidate.getY(), bestCandidate.getZ());
//        Minecraft.getInstance().getConnection().send(ServerboundInteractPacket.createInteractionPacket(bestCandidate, Minecraft.getInstance().player.isShiftKeyDown(), InteractionHand.MAIN_HAND, vec3));

        // so this should be how vailla does it, can't check if there's flags bcs of reach flagging hitboxes, and grim ignores armourstands for PacketOrderC
        InteractUtils.interactOnEntity(bestCandidate, vec3);

        lastClick = System.currentTimeMillis();
    }

    public static boolean getEntityVisibility(Entity entity) {
        if (!entity.isInvisible()) return true;
        TermAura termAura = RSM.getModule(TermAura.class);
        return termAura.isEnabled() && termAura.showArmorStands.getValue() && termAura.locationCheck();
    }

    private boolean locationCheck() {
        return forceSkyblock.getValue() || (Location.getArea().is(Island.Dungeon) && Dungeon.isInBoss() && DungeonUtils.isPhase(Phase7.P3));
    }

    private Vec3 clamp(AABB aabb, Vec3 vec3) {
        return new Vec3(clamp(vec3.x, aabb.minX, aabb.maxX), clamp(vec3.y, aabb.minY, aabb.maxY), clamp(vec3.z, aabb.minZ, aabb.maxZ));
    }

    private double clamp(double d, double min, double max) {
        return Math.min(max, Math.max(d, min));
    }


    private static boolean filterEntities(ArmorStand armorStand) {
        if (armorStand.isDeadOrDying()) return false;
        Component name = armorStand.getCustomName();
        if (name == null) return false;
        return name.getString().equals("Inactive Terminal");
    }
}
/*
 [STDERR]: java.util.NoSuchElementException
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at java.base/java.util.ArrayList.getFirst(ArrayList.java:439)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//com.ricedotwho.rsa.module.impl.dungeon.terminals.Solution.getNext(Solution.java:17)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//com.ricedotwho.rsa.module.impl.dungeon.terminals.StartsWith.getNextState(StartsWith.java:31)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autoterms.AutoTerms.onReceivePacket(AutoTerms.java:220)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//com.ricedotwho.rsm.event.api.EventBus.invoke(EventBus.java:100)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//com.ricedotwho.rsm.event.api.EventBus.post(EventBus.java:88)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//com.ricedotwho.rsm.event.Event.post(Event.java:26)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//net.minecraft.network.Connection.handler$bka000$rsm$handlePacket(Connection.java:1689)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//net.minecraft.network.Connection.genericsFtw(Connection.java)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//net.minecraft.network.Connection.channelRead0(Connection.java:176)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//net.minecraft.network.Connection.channelRead0(Connection.java)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.SimpleChannelInboundHandler.channelRead(SimpleChannelInboundHandler.java:99)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:107)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:107)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.flow.FlowControlHandler.dequeue(FlowControlHandler.java:202)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.flow.FlowControlHandler.channelRead(FlowControlHandler.java:164)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:107)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.handler.timeout.IdleStateHandler.channelRead(IdleStateHandler.java:289)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1357)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:868)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:799)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:501)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:399)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:998)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at knot//io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
[22:23:15] [Netty Epoll Client IO #0/INFO] (Minecraft) [STDERR]: 	at java.base/java.lang.Thread.run(Thread.java:1583)
 */
