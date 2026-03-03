package com.ricedotwho.rsa.module.impl.dungeon.boss.p3;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.Ring;
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

import java.util.*;

@ModuleInfo(aliases = "Auto P3", id = "AutoP3", category = Category.DUNGEONS)
public class AutoP3 extends Module implements ClientRotationProvider {
    private static final MutableComponent PREFIX = Component.literal("§6[§8byebyegoldor§6] §r");
    public static final double UNIT_VECTOR_LENGTH = 0.210139989d;

    private final BooleanSetting forceSkyblock = new BooleanSetting("Force Skyblock", false);
    private final List<Ring> rings;
    private final Queue<Float> yaws;
    private boolean bl = false;

    public AutoP3() {
        this.registerProperty(
                forceSkyblock
        );
        this.yaws = new LinkedList<>();
        this.rings = new ArrayList<>();
    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (!dungeonCheck()) return;
        if (yaws.isEmpty() || Minecraft.getInstance().player == null) {
            bl = false;
            return;
        }
        if (!bl) {
            ClientRotationHandler.registerProvider(this);
            ClientRotationHandler.setYaw(Minecraft.getInstance().player.getYRot());
            //ChatUtils.chat("registering!");
        }
        if (!Minecraft.getInstance().player.getLastSentInput().shift()) {
            event.getInputConsumer().accept(new Input(false, false, false, false, false, true, false));
            return;
        }
        bl = true;

        if (Minecraft.getInstance().player.getDeltaMovement().x != 0 || Minecraft.getInstance().player.getDeltaMovement().z != 0) return;

        Minecraft.getInstance().player.setYRot(yaws.poll());
        event.getInputConsumer().accept(new Input(true, false, false, false, false, true, false));
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if (!dungeonCheck() || Minecraft.getInstance().player == null) return;

        Vec3 playerPos = Minecraft.getInstance().player.position();
        Ring ring = rings.stream().filter(r -> r.updateState(playerPos)).max(Comparator.comparingInt(Ring::getPriority)).orElse(null);
        if (ring == null) return;
        ring.setTriggered(true);
        ring.run();
    }


    private static double getVelocity(int tickIndex, double walkSpeed) {
        // I plotted some velocities and solved for the exponential function
        // https://www.desmos.com/calculator/tpikdildj1
        return Math.pow(0.546000082, tickIndex) * 0.098 * walkSpeed; // Don't mind the constants
    }

    // Walkspeed should be 1 when normal, may need to mult by 10
    // this function assumes it is normalized
    public static double getDisplacement(int movementTicks, double walkSpeed, boolean sneaking) {
        double lnBase = Math.log(0.546000082);
        if (sneaking) walkSpeed = walkSpeed * 0.3;

        double upper = getVelocity(movementTicks, walkSpeed);
        double lower = getVelocity(0, walkSpeed);

        return ((upper - lower) / lnBase) / 0.75;
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (!dungeonCheck()) return;
        this.rings.forEach(r -> r.render(false));
    }

    private boolean dungeonCheck() {
        return this.forceSkyblock.getValue() || (Minecraft.getInstance().player != null && Location.getArea().is(Island.Dungeon) && Dungeon.isInBoss());
    }

    public void queueYaw(float yaw) {
        yaws.add(yaw);
    }

    public static void chat(Object message, Object... objects) {
        ChatUtils.chatClean(PREFIX.copy().append(String.format(message.toString(), objects)));
    }

    public void addRing(Ring ring) {
        this.rings.add(ring);
    }

    @Override
    public boolean isClientRotationActive() {
        return this.isEnabled() && bl;
    }

    @Override
    public boolean allowClientKeyInputs() {
        return false;
    }
}
