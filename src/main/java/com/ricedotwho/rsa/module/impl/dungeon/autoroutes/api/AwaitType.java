package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.awaits.AwaitClick;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.nodes.BatNode;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.nodes.EtherwarpNode;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

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
