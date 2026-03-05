package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.component.impl.camera.ClientRotationHandler;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationProvider;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.IntStream;

@ModuleInfo(aliases = "Auto P3", id = "AutoP3", category = Category.DUNGEONS)
public class AutoP3 extends Module implements ClientRotationProvider {
    private static final MutableComponent PREFIX = Component.literal("§6[§8byebyebalding§6] §r");

    private final BooleanSetting forceSkyblock = new BooleanSetting("Force Skyblock", false);
    private final List<Ring> rings;
    private boolean desync = false;
    private boolean lastDesync = false;
    private Ring currentNode;

    public AutoP3() {
        this.registerProperty(
                forceSkyblock
        );
        //this.yaws = new LinkedList<>();
        this.rings = new ArrayList<>();
    }

    @SubscribeEvent
    public void onTickEnd(ClientTickEvent.End event) {
        if (!desync && lastDesync && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setYRot(ClientRotationHandler.getClientYaw());
            Minecraft.getInstance().player.setXRot(ClientRotationHandler.getClientPitch());
        }
        lastDesync = desync;
    }

    @SubscribeEvent
    public void onWorldLoad() {
        this.currentNode = null;
    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (!dungeonCheck()) return;
        if (currentNode != null && currentNode.tick(event, this)) currentNode = null;
    }

    protected void onDesyncEnable() {
        ClientRotationHandler.registerProvider(this);

        if (Minecraft.getInstance().player == null) return;
        ClientRotationHandler.setYaw(Minecraft.getInstance().player.getYRot());
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if (!dungeonCheck() || Minecraft.getInstance().player == null) return;
        desync = false;

        Vec3 playerPos = Minecraft.getInstance().player.position();

        Ring ring;
        synchronized (rings) {
            ring = rings.stream().filter(r -> r.updateState(playerPos) && (currentNode == null || r.getPriority() >= currentNode.getPriority())).max(Comparator.comparingInt(Ring::getPriority)).orElse(null);
        }

        if (ring == null) return;
        currentNode = ring;
        ring.setTriggered(true);
        ring.run();
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (!dungeonCheck()) return;
        synchronized (rings) {
            this.rings.forEach(r -> r.render(false));
        }
    }

    private boolean dungeonCheck() {
        return this.forceSkyblock.getValue() || (Minecraft.getInstance().player != null && Location.getArea().is(Island.Dungeon) && Dungeon.isInBoss());
    }

    public static void chat(Object message, Object... objects) {
        ChatUtils.chatClean(PREFIX.copy().append(String.format(message.toString(), objects)));
    }

    public void addRing(Ring ring) {
        ring.setTriggered(true); // So it doesn't activate instantly
        synchronized (rings) {
            this.rings.add(ring);
        }
    }

    public void removeNearest(Vec3 pos) {
        synchronized (rings) {
            int index = IntStream.range(0, rings.size())
                .boxed()
                .min(Comparator.comparingDouble(i -> rings.get(i).getDistanceSq(pos)))
                .orElse(-1);
            if (index < 0) return;
            rings.remove(index);
        }
    }

    protected void setDesync(boolean bl) {
        if (bl && !desync && !lastDesync) onDesyncEnable();
        this.desync = bl;
    }

    protected boolean getDesync() {
        return this.desync;
    }

    protected boolean getLastDesync() {
        return this.lastDesync;
    }

    @Override
    public boolean isClientRotationActive() {
        return this.isEnabled() && desync;
    }

    @Override
    public boolean allowClientKeyInputs() {
        return true;
    }
}
