package com.ricedotwho.rsa.module.impl.player.pet;

import lombok.Getter;

import java.util.function.Consumer;

public abstract class PetRule {
    @Getter
    private final String id;
    protected final Consumer<String> callback;

    public PetRule(String id, Consumer<String> callback) {
        this.id = id;
        this.callback = callback;
    }

}
