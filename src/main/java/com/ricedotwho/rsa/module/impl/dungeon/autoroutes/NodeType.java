package com.ricedotwho.rsa.module.impl.dungeon.autoroutes;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.BatNode;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.BoomNode;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.EtherwarpNode;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Arrays;

public enum NodeType {
    ETHERWARP("ew", EtherwarpNode::supply),
    BOOM("boom", BoomNode::supply),
    BAT("bat", BatNode::supply),
    AOTV("aotv", null);

    @Getter
    private final String name;
    private final TriFunction<UniqueRoom, LocalPlayer, AwaitManager, Node> factory;

    NodeType(String s, TriFunction<UniqueRoom, LocalPlayer, AwaitManager, Node> factory) {
        this.name = s;
        this.factory = factory;
    }

    public Node supply(UniqueRoom fullRoom, AwaitManager awaits) {
        if (this.factory == null || Minecraft.getInstance().player == null) return null;
        return this.factory.apply(fullRoom, Minecraft.getInstance().player, awaits);
    }

    public static NodeType byName(String name) {
        return Arrays.stream(NodeType.values()).filter(n -> n.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
