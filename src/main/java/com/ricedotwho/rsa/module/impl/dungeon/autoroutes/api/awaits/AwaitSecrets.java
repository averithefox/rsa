package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.awaits;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.AwaitCondition;
import com.ricedotwho.rsm.utils.ChatUtils;

public class AwaitSecrets extends AwaitCondition<Integer> {
    private final int secretCount;
    private int collectedSecretCount;

    public AwaitSecrets(int count) {
        this.secretCount = count;
    }

    public boolean test() {
        return this.collectedSecretCount >= secretCount;
    }

    @Override
    public void reset() {
        this.collectedSecretCount = 0;
    }

    @Override
    public void onEnter() {

    }

    protected void consume(Integer secrets) {
        this.collectedSecretCount += secrets;
    }

}
