package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class AwaitManager {
    private final HashMap<Class<? extends AwaitCondition<?>>, AwaitCondition<?>> awaits;

    public AwaitManager(Collection<AwaitCondition<?>> awaits) {
        if (awaits.isEmpty()) {
            this.awaits = null;
            return;
        }

        this.awaits = new HashMap<>();
        for (AwaitCondition<?> await : awaits) {
            this.awaits.putIfAbsent((Class<? extends AwaitCondition<?>>) await.getClass(), await);
        }
    }

    public AwaitManager(AwaitCondition<?>... conditions) {
        if (conditions == null || conditions.length < 1) {
            this.awaits = null;
            return;
        }

        this.awaits = new HashMap<>();
        for (AwaitCondition<?> await : conditions) {
            awaits.putIfAbsent((Class<? extends AwaitCondition<?>>) await.getClass(), await);
        }
    }

    public void onEnterNode() {
        this.getAwaits().forEach(AwaitCondition::onEnter);
    }

    protected void resetAwaits() {
        this.getAwaits().forEach(AwaitCondition::reset);
    }

    public boolean shouldAwait() {
        return this.hasAwaits() && this.awaits.values().stream().anyMatch(await -> !await.test());
    }

    public Collection<AwaitCondition<?>> getAwaits() {
        return awaits == null ? Collections.emptyList() : awaits.values();
    }

    public boolean hasAwaits() {
        return this.awaits != null && !this.awaits.isEmpty();
    }

    public boolean hasAwait(Class<? extends AwaitCondition<?>> clzz) {
        return this.awaits.containsKey(clzz);
    }

    public  <T> void consume(Class<? extends AwaitCondition<T>> clzz, T value) {
        AwaitCondition<T> condition = getAwait(clzz);
        if (condition == null) return;
        condition.consume(value);
    }

    public <T extends AwaitCondition<?>> T getAwait(Class<T> clzz) {
        AwaitCondition<?> await = awaits.get(clzz);
        if (await == null) return null;
        return clzz.cast(await);
    }


}
