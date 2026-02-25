package com.ricedotwho.rsa.module.impl.dungeon.boss.p3;

import com.ricedotwho.rsm.component.impl.camera.ClientRotationHandler;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationProvider;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Input;

import java.util.*;

@ModuleInfo(aliases = "Auto P3", id = "AutoP3", category = Category.DUNGEONS)
public class AutoP3 extends Module implements ClientRotationProvider {
    private static final MutableComponent PREFIX = Component.literal("§6[§8byebyegoldor§6] §r");
    public static final double UNIT_VECTOR_LENGTH = 0.210139989d;
    private final Queue<Float> yaws;
    private boolean bl = false;

    public AutoP3() {
        this.yaws = new LinkedList<>();
    }

    public void test() {

    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (yaws.isEmpty() || Minecraft.getInstance().player == null) {
            bl = false;
            return;
        }
        if (!bl) {
            ClientRotationHandler.registerProvider(this);
            ClientRotationHandler.setYaw(Minecraft.getInstance().player.getYRot());
            //ChatUtils.chat("registering!");
        }
        bl = true;

        if (Minecraft.getInstance().player.getDeltaMovement().x != 0 || Minecraft.getInstance().player.getDeltaMovement().z != 0) return;

        Minecraft.getInstance().player.setYRot(yaws.poll());
        event.getInputConsumer().accept(new Input(true, false, false, false, false, false, false));
    }

    public void queueYaw(float yaw) {
        yaws.add(yaw);
    }

    public static void chat(Object message, Object... objects) {
        ChatUtils.chatClean(PREFIX.copy().append(String.format(message.toString(), objects)));
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
