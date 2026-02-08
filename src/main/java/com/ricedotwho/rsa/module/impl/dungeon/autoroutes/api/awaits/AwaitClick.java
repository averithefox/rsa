package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.awaits;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.AwaitCondition;
import com.ricedotwho.rsm.utils.ChatUtils;

public class AwaitClick extends AwaitCondition<Boolean> {
    private boolean clicked;

    public AwaitClick() {
        this.clicked = false;
    }

    public boolean test() {
        return this.clicked;
    }

    @Override
    public void onEnter() {
        this.clicked = false;
    }

    @Override
    public void reset() {

    }

    protected void consume(Boolean bl) {
        this.clicked = bl;
    }

}
