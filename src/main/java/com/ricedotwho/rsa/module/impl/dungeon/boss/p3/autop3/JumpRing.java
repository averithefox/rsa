package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import net.minecraft.world.phys.Vec3;

public class JumpRing extends Ring {
    public JumpRing(Vec3 pos) {
        super(pos, 0.5);
    }


    @Override
    public void run() {

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
    public boolean tick(InputPollEvent event, AutoP3 autoP3) {
        return true;
    }
}
