package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.function.Function;

public enum RingType {
    ALIGN("align", AlignRing::new, 0f),
    FAST_ALIGN("fastalign", FastAlign::new, 0.01f),
    STOP("stop", StopRing::new, 0.02f),
    WALK("walk", WalkRing::new, 0.03f),
    JUMP("jump", JumpRing::new, 0.04f),
    BONZO("bonzo", BonzoRing::new, 0.05f),
    FAST_BONZO("fastbonzo", FastBonzoRing::new, 0.06f),
    LOOK("look", LookRing::new, 0.07f);

    @Getter
    private final String name;
    private final Function<Vec3, Ring> factory;
    @Getter
    private final float renderSizeOffset;

    RingType(String s, Function<Vec3, Ring> factory, float renderSizeOffset) {
        this.name = s;
        this.renderSizeOffset = renderSizeOffset;
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
