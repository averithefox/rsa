package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public class StopRing extends Ring {
    public StopRing(Vec3 pos) {
        super(pos, 0.5, RingType.STOP.getRenderSizeOffset());
    }


    @Override
    public boolean run() {
        return false;
    }

    @Override
    public Colour getColour() {
        return Colour.RED;
    }

    @Override
    public int getPriority() {
        return 75;
    }

    @Override
    public boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3) {
        return true;
    }
}
