package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings.Ring;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationHandler;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationProvider;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.IntStream;

@ModuleInfo(aliases = "Auto P3", id = "AutoP3", category = Category.DUNGEONS)
public class AutoP3 extends Module implements ClientRotationProvider {
    private static final MutableComponent PREFIX = Component.literal("§6[§8byebyebalding§6] §r");

    private final BooleanSetting forceSkyblock = new BooleanSetting("Force Skyblock", false);

    private final SaveSetting<List<Ring>> data = new SaveSetting<>("Rings", "dungeon/ap3", "rings.json", ArrayList::new,
            new TypeToken<List<Ring>>() {}.getType(),
            new GsonBuilder()
                    .registerTypeHierarchyAdapter(Ring.class, new RingAdapter())
                    .setPrettyPrinting().create(),
            true, this::reload, null);

    private final List<Ring> rings;
    private boolean desync = false;
    private boolean lastDesync = false;
    private final List<Ring> activeRings;

    public AutoP3() {
        this.registerProperty(
                forceSkyblock,
                data
        );
        this.rings = new ArrayList<>();
        this.activeRings = new ArrayList<>(5);
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
    public void onWorldLoad(WorldEvent.Load event) {
        this.activeRings.clear();
    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (!dungeonCheck()) return;
        if (activeRings.isEmpty()) return;

        MutableInput mutableInput = new MutableInput(event.getClientInput());

        for (int i = 0 ; i < activeRings.size(); i++) {
            boolean bl2 = activeRings.get(i).tick(mutableInput, event.getClientInput(), this);
            if (!bl2) continue;
            activeRings.remove(i--);
        }

        if (mutableInput.isModified()) {
            event.getInputConsumer().accept(mutableInput.toInput());
        }
    }

    private void reload() {
        this.rings.clear();
        this.rings.addAll(data.getValue());
    }

    protected void onDesyncEnable() {
        ClientRotationHandler.registerProvider(this);

        if (Minecraft.getInstance().player == null) return;
        ClientRotationHandler.setYaw(Minecraft.getInstance().player.getYRot());
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if (!dungeonCheck() || mc.player == null) return;
        desync = false;

        Vec3 playerPos = mc.player.position();
        Vec3 oldPos = mc.player.oldPosition();

        List<Ring> sorted;
        synchronized (rings) {
            sorted = rings.stream().filter(r -> r.updateState(playerPos, oldPos) && (activeRings.isEmpty() || activeRings.stream().allMatch(active -> r.getPriority() >= active.getPriority()))).sorted(Comparator.comparingInt(Ring::getPriority).reversed()).toList();
        }

        if (sorted.isEmpty()) return;

        activeRings.clear();

        for (Ring ring : sorted) {
            activeRings.add(ring);
            ring.setTriggered(true);
            if (!ring.run()) break;
        }
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (!dungeonCheck()) return;
        synchronized (rings) {
            this.rings.forEach(r -> r.render(false));
        }
    }

    private boolean dungeonCheck() {
        return this.forceSkyblock.getValue() || (mc.player != null && Location.getArea().is(Island.Dungeon) && Dungeon.isInBoss());
    }

    public static void chat(Object message, Object... objects) {
        ChatUtils.chatClean(PREFIX.copy().append(String.format(message.toString(), objects)));
    }

    public void addRing(Ring ring) {
        ring.setTriggered(true); // So it doesn't activate instantly
        synchronized (rings) {
            this.rings.add(ring);
            data.setValue(List.copyOf(this.rings));
        }
        save();
    }

    public void removeNearest(Vec3 pos) {
        synchronized (rings) {
            int index = IntStream.range(0, rings.size())
                .boxed()
                .min(Comparator.comparingDouble(i -> rings.get(i).getDistanceSq(pos)))
                .orElse(-1);
            if (index < 0) return;
            rings.remove(index);
            data.setValue(List.copyOf(this.rings));
        }
        save();
    }

    public void setDesync(boolean bl) {
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

    public void save() {
        data.save();
    }

    public void load() {
        data.load();
    }
}
