package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitCondition;

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

    @Override
    public void serialize(JsonObject json) {

    }

    protected void consume(Boolean bl) {
        this.batKilled = bl;
    }

}
