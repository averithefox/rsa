package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public class WalkRing extends Ring {
    @Getter
    private float yaw;

    private static final Input FORWARDS = new Input(true, false, false, false, false, false, true);

    public WalkRing(Vec3 pos) {
        super(pos, 0.5);
        this.yaw = Minecraft.getInstance().gameRenderer.getMainCamera().yaw();
    }

    @Override
    public void run() {

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
    public boolean tick(InputPollEvent event, AutoP3 autoP3) {
        if (hasInputPressed(event.getClientInput())) return true;

        autoP3.setDesync(true);
        Minecraft.getInstance().player.setYRot(yaw);

        event.getInputConsumer().accept(FORWARDS);
        return false;
    }

    private boolean hasInputPressed(Input input) {
        return input.forward() || input.backward() || input.left() || input.right() || input.jump();
    }
}
