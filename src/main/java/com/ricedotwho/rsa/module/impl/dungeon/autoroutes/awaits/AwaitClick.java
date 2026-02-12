package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitCondition;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;

public class AwaitClick extends AwaitCondition<Boolean> {
    private boolean clicked;

    public AwaitClick() {
        this.clicked = false;
    }

    public boolean test(Node node) {
        return this.clicked;
    }

    @Override
    public void onEnter() {
        this.clicked = false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void serialize(JsonObject json) {
        json.addProperty("awaitClick", true);
    }

    protected void consume(Boolean bl) {
        this.clicked = bl;
    }

}
