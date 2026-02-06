package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.nodes.EtherwarpNode;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Arrays;
import java.util.function.BiFunction;

public enum NodeType {
    ETHERWARP("ew", EtherwarpNode::supply),
    BOOM("boom", null),
    AOTV("aotv", null);

    @Getter
    private final String name;
    private final BiFunction<UniqueRoom, LocalPlayer, Node> factory;

    NodeType(String s, BiFunction<UniqueRoom, LocalPlayer, Node> factory) {
        this.name = s;
        this.factory = factory;
    }

    public Node supply(UniqueRoom fullRoom) {
        if (this.factory == null || Minecraft.getInstance().player == null) return null;
        return this.factory.apply(fullRoom, Minecraft.getInstance().player);
    }

    public static NodeType byName(String name) {
        return Arrays.stream(NodeType.values()).filter(n -> n.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
