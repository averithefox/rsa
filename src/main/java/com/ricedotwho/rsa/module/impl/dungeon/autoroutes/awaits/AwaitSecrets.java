package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitCondition;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;

public class AwaitSecrets extends AwaitCondition<Integer> {
    @Expose
    private final int secretCount;
    private int collectedSecretCount;

    public AwaitSecrets(int count) {
        this.secretCount = count;
    }

    public boolean test(Node node) {
        return this.collectedSecretCount >= secretCount;
    }

    @Override
    public void reset() {
        this.collectedSecretCount = 0;
    }

    @Override
    public void serialize(JsonObject json) {
        json.addProperty("awaitSecrets", this.secretCount);
    }

    @Override
    public void onEnter() {

    }

    protected void consume(Integer secrets) {
        this.collectedSecretCount += secrets;
    }

}
