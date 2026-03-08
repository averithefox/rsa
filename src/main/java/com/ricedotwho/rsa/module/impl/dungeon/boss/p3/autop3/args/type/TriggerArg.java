package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.Argument;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.RingArgType;

public class TriggerArg extends Argument {
    public TriggerArg() {
        super(RingArgType.TRIGGER);
    }

    @Override
    public boolean check() {
        return false;
    }

    @Override
    public String stringValue() {
        return "trigger";
    }

    public static TriggerArg create(String ignored) {
        return new TriggerArg();
    }
}
