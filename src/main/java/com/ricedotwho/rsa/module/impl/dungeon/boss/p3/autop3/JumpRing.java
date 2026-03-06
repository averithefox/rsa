package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public class JumpRing extends Ring {
    public JumpRing(Vec3 pos) {
        super(pos, 0.5, RingType.JUMP.getRenderSizeOffset());
    }


    @Override
    public boolean run() {
        return true;
    }

    @Override
    public Colour getColour() {
        return Colour.ORANGE;
    }

    @Override
    public int getPriority() {
        return 60;
    }

    @Override
    public boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3) {
        mutableInput.jump(true);
        return true;
    }
}
