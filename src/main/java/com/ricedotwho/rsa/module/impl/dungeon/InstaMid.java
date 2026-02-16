package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.component.impl.TickFreeze;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.DungeonUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

@Getter
@ModuleInfo(aliases = "InstaMid", id = "InstaMid", category = Category.DUNGEONS)
public class InstaMid extends Module {

    private final NumberSetting ticks = new NumberSetting("Ticks", 100, 150, 128, 1);

    private boolean startOnNextFlying = false;
    private int airTicks = 0;

    public InstaMid() {
        this.registerProperty(
                ticks
        );
    }

    @Override
    public void reset() {
        startOnNextFlying = false;
        airTicks = 0;
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        reset();
    }

    public void onPacketSend(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket packet)
                || !Location.getArea().is(Island.Dungeon)
                || !Dungeon.isInBoss()
                || !DungeonUtils.isPhase(Phase7.P4)
                || !startOnNextFlying
                || packet.isOnGround()
                || !isOnPlatform()
        ) return;

        airTicks++;
        if (airTicks > 3) {
            startIMid();
        }
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        String unformatted = ChatFormatting.stripFormatting(event.getMessage().getString());
        if (!Location.getArea().is(Island.Dungeon)
                || !Dungeon.isInBoss()
                || !DungeonUtils.isPhase(Phase7.P4)
                || !"[BOSS] Necron: You went further than any human before, congratulations.".equals(unformatted)
                || !isOnPlatform()
        ) return;

        if (mc.player.onGround()) {
            startOnNextFlying = true;
            mc.player.jumpFromGround();
        } else {
            startIMid();
        }
    }

    private void startIMid() {
        startOnNextFlying = false;
        ChatUtils.chat("Attempting to InstaMid");
        TaskComponent.onTick(0, () -> {
                TickFreeze.freeze();
                TaskComponent.onServerTick(this.ticks.getValue().intValue(), TickFreeze::unFreeze);
        });
    }

    private boolean isOnPlatform() {
        Vec3 pos = mc.player.position();
        return pos.y() > 63 && pos.y() < 100 && (Math.pow(Math.abs(pos.x() - 54.5), 2) + Math.pow(Math.abs(pos.z() - 76.5), 2)) < 56.25;
    }
}
