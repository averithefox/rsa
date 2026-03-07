package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.RingType;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import net.minecraft.world.phys.Vec3;

public class FastAlign extends AlignRing {
    private final Colour colour = Colour.GREEN.darker();
    public FastAlign(Vec3 pos) {
        super(pos, RingType.FAST_ALIGN.getRenderSizeOffset());
    }

    public FastAlign(Pos min, Pos max) {
        super(min, max, RingType.FAST_ALIGN.getRenderSizeOffset());
    }

    @Override
    public RingType getType() {
        return RingType.FAST_ALIGN;
    }

    @Override
    protected double getPrecision() {
        return 0.25d * 0.25d; // The velocity will carry and it will end up much better than 0.25
    }

    @Override
    public Colour getColour() {
        return colour;
    }
}
