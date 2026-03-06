package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.data.Colour;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public class WalkRing extends Ring {
    @Getter
    private float yaw;

    public WalkRing(Vec3 pos) {
        super(pos, 0.5, RingType.WALK.getRenderSizeOffset());
        this.yaw = Minecraft.getInstance().gameRenderer.getMainCamera().yaw();
    }

    @Override
    public boolean run() {
        return false;
    }

    @Override
    public Colour getColour() {
        return Colour.CYAN;
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3) {
        if (hasInputPressed(input)) return true;

        autoP3.setDesync(true);
        Minecraft.getInstance().player.setYRot(yaw);

        mutableInput.forward(true);
        mutableInput.sprint(true);
        return false;
    }

    private boolean hasInputPressed(Input input) {
        return input.forward() || input.backward() || input.left() || input.right() || input.jump();
    }
}
