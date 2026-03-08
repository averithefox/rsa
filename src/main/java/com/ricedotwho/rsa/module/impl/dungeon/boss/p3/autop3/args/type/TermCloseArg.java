package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.Argument;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.RingArgType;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;

public class TermCloseArg extends Argument<Boolean> {
    private boolean closed = false;

    public TermCloseArg() {
        super(RingArgType.TERM);
    }

    @Override
    public boolean check() {
        return closed;
    }

    @Override
    public void consume(Boolean event) {
        closed = true;
    }

    @Override
    public void reset() {
        closed = false;
    }

    @Override
    public String stringValue() {
        return "term close";
    }

    public static TermCloseArg create(String ignored) {
        return new TermCloseArg();
    }

}
