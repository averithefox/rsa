package com.ricedotwho.rsa.module.impl.dungeon.autoroutes;

import lombok.Getter;

import java.util.Arrays;

public enum AwaitType {
    CLICK("awaitClick"),
    SECRETS("secrets"),
    BAT("bat");

    @Getter
    private final String name;

    AwaitType(String s) {
        this.name = s;
    }


    public static AwaitType byName(String name) {
        return Arrays.stream(AwaitType.values()).filter(n -> n.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
