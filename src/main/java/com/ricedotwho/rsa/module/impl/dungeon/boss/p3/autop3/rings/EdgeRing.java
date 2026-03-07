package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings;

import com.ricedotwho.rsa.component.impl.Edge;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.AutoP3;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.MutableInput;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.RingType;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.ArgumentManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type.GroundArg;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.SubActionManager;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import net.minecraft.world.entity.player.Input;

public class EdgeRing extends Ring {

    public EdgeRing(Pos min, Pos max, ArgumentManager manager, SubActionManager actions) {
        super(min, max, RingType.JUMP.getRenderSizeOffset(), manager, actions);
        this.getArgManager().addArg(new GroundArg());
    }

    @Override
    public RingType getType() {
        return RingType.EDGE;
    }

    @Override
    public boolean run() {
        Edge.edge();
        return true;
    }

    @Override
    public Colour getColour() {
        return Colour.BLACK;
    }

    @Override
    public int getPriority() {
        return 60;
    }

    @Override
    public boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3) {
        return false;
    }

    @Override
    public void feedback() {
        AutoP3.modMessage("Edging");
    }

}
