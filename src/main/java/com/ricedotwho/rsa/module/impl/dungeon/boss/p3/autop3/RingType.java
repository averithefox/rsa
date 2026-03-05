package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.mojang.datafixers.util.Function4;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.*;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.function.Function;

public enum RingType {
    ALIGN("align", AlignRing::new),
    WALK("walk", WalkRing::new),
    STOP("stop", StopRing::new);

    @Getter
    private final String name;
    private final Function<Vec3, Ring> factory;

    RingType(String s, Function<Vec3, Ring> factory) {
        this.name = s;
        this.factory = factory;
    }

    public Ring supply(Vec3 pos) {
        if (this.factory == null || Minecraft.getInstance().player == null) return null;
        return this.factory.apply(pos);
    }

    public static RingType byName(String name) {
        return Arrays.stream(RingType.values()).filter(n -> n.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
