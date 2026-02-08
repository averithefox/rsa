package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api;

public abstract class AwaitCondition<T> {

    public AwaitCondition() {

    }

    public abstract boolean test();
    protected abstract void consume(T event);

    public abstract void onEnter();
    public abstract void reset();

}
