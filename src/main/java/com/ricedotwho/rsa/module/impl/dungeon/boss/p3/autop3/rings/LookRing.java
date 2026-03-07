package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.AutoP3;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.MutableInput;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.RingType;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public class LookRing extends Ring implements Accessor {
    @Getter
    private final float yaw;
    @Getter
    private final float pitch;

    @Override
    public RingType getType() {
        return RingType.LOOK;
    }

    public LookRing(Vec3 pos) {
        super(pos, 0.5, RingType.LOOK.getRenderSizeOffset());
        this.yaw = mc.gameRenderer.getMainCamera().yaw();
        this.pitch = mc.gameRenderer.getMainCamera().getXRot();
    }

    public LookRing(Pos min, Pos max, float yaw, float pitch) {
        super(min, max, RingType.LOOK.getRenderSizeOffset());
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public boolean run() {
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
        return true;
    }

    @Override
    public Colour getColour() {
        return Colour.GREEN;
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3) {
        return false;
    }

    @Override
    public JsonObject serialize() {
        JsonObject obj = super.serialize();
        obj.addProperty("yaw", this.yaw);
        obj.addProperty("pitch", this.pitch);
        return obj;
    }
}
