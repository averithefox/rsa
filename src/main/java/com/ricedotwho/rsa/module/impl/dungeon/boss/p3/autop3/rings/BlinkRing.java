package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.Blink;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.AutoP3;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.RingType;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.ArgumentManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.recorder.MovementRecorder;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.SubActionManager;
import com.ricedotwho.rsa.module.impl.render.Freecam;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.MutableInput;
import com.ricedotwho.rsm.data.Pos;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class BlinkRing extends Ring {
    private final String route;
    private final int size;
    private int ticks = -1;
    private Vec3 endPos;
    private Vec3 endVelo;


    public BlinkRing(Pos min, Pos max, ArgumentManager manage, SubActionManager actions, Map<String, Object> extra) {
        this(min, max, (String) extra.getOrDefault("route", MovementRecorder.getData().getFileName()), manage, actions, (int) extra.getOrDefault("blink", 17));
    }

    public BlinkRing(Pos min, Pos max, String route, ArgumentManager manage, SubActionManager actions, int length) {
        super(min, max, RingType.BLINK.getRenderSizeOffset(), manage, actions);
        this.size = Mth.clamp(1, length, 17);
        this.route = route;
        this.endPos = null;
        this.endVelo = null;
    }

    @Override
    public RingType getType() {
        return RingType.BLINK;
    }

    @Override
    public boolean run() {
        if (Minecraft.getInstance().player == null) return false;
        Blink blink = RSM.getModule(Blink.class);
        if (!blink.isEnabled()) {
            blink.onKeyToggle();
            blink.setCurrentRing(this);
        }

        if (RSM.getModule(AutoP3.class).getFreecamBlink().getValue()) {
            Freecam freecam = RSM.getModule(Freecam.class);
            if (!freecam.isEnabled()) freecam.setEnabled(true);
        }

        ticks = 0;
        MovementRecorder.playRecording(this.route);
        return false;
    }

    @Override
    public Colour getColour() {
        return Colour.pink;
    }

    @Override
    public int getPriority() {
        return 40;
    }

    private void cancel() {
        Blink blink = RSM.getModule(Blink.class);
        if (blink.isEnabled()) {
            blink.clearMovements();
            blink.onKeyToggle();
        }
    }

    private void flush() {
        Blink blink = RSM.getModule(Blink.class);
        if (blink.isEnabled())
            blink.onKeyToggle();
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setPos(endPos);
            Minecraft.getInstance().player.setDeltaMovement(endVelo);

            if (RSM.getModule(AutoP3.class).getFreecamBlink().getValue()) {
                Freecam freecam = RSM.getModule(Freecam.class);
                if (freecam.isEnabled()) freecam.setEnabled(false);
            }
            MovementRecorder.resumeRecording();
        }
        ticks = -1;
    }

    public boolean isDonePlaying() {
        return ticks > size;
    }

    public void flushNext() {
        PacketOrderManager.register(PacketOrderManager.STATE.START, this::flush);
    }

    @Override
    public boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3) {
        if (Minecraft.getInstance().player == null || ticks < 0) return true;
        ticks++;

        if (ticks <= size + 1) {
            endPos = Minecraft.getInstance().player.position();
            endVelo = Minecraft.getInstance().player.getDeltaMovement();
        }

        if (ticks == size + 1) {
            Minecraft.getInstance().player.setPos(endPos);
            Minecraft.getInstance().player.setDeltaMovement(0d, 0d, 0d);
            MovementRecorder.pauseRecording();
        }

        return false;
    }

    @Override
    public JsonObject serialize() {
        JsonObject obj = super.serialize();
        obj.addProperty("route", this.route);
        obj.addProperty("size", this.size);
        return obj;
    }

    @Override
    public void feedback() {
        //AutoP3.modMessage("Blinking!");
    }
}
