package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.awaits;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.AwaitCondition;

public class AwaitBat extends AwaitCondition<Boolean> {
    private boolean batKilled;

    public AwaitBat() {
        this.batKilled = false;
    }

    public boolean test() {
        return this.batKilled;
    }

    @Override
    public void onEnter() {
        this.batKilled = false;
    }

    @Override
    public void reset() {

    }

    protected void consume(Boolean bl) {
        this.batKilled = bl;
    }

}
