package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.Argument;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.RingArgType;

public class GroundArg extends Argument {
    public GroundArg() {
        super(RingArgType.GROUND);
    }

    @Override
    public boolean check() {
        return mc.player != null && mc.player.onGround();
    }

    @Override
    public String stringValue() {
        return "ground";
    }

    public static GroundArg create(String arg) {
        return new GroundArg();
    }

}
