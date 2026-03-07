package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.Argument;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.RingArgType;
import com.ricedotwho.rsm.component.impl.Terminals;

public class TermArg extends Argument {
    public TermArg() {
        super(RingArgType.TERM);
    }

    @Override
    public boolean check() {
        return Terminals.isInTerminal();
    }

    @Override
    public String stringValue() {
        return "term";
    }

    public static TermArg create(String ignored) {
        return new TermArg();
    }

}
