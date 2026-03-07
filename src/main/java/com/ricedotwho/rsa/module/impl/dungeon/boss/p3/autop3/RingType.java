package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.mojang.datafixers.util.Function4;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.ArgumentManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings.*;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.SubActionManager;
import com.ricedotwho.rsm.data.Pos;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.Arrays;

public enum RingType {
    ALIGN("align", AlignRing::new, 0f),
    FAST_ALIGN("fastalign", FastAlign::new, 0.01f),
    STOP("stop", StopRing::new, 0.02f),
    WALK("walk", WalkRing::new, 0.03f),
    JUMP("jump", JumpRing::new, 0.04f),
    LOOK("look", LookRing::new, 0.05f),
    EDGE("edge", EdgeRing::new, 0.06f);

    @Getter
    private final String name;
    private final Function4<Pos, Pos, ArgumentManager, SubActionManager, Ring> factory;
    @Getter
    private final float renderSizeOffset;

    RingType(String s, Function4<Pos, Pos, ArgumentManager, SubActionManager, Ring> factory, float renderSizeOffset) {
        this.name = s;
        this.renderSizeOffset = renderSizeOffset;
        this.factory = factory;
    }

    public Ring supply(Pos min, Pos max, ArgumentManager manager, SubActionManager actions) {
        if (this.factory == null || Minecraft.getInstance().player == null) return null;
        return this.factory.apply(min, max, manager, actions);
    }

    public static RingType byName(String name) {
        return Arrays.stream(RingType.values()).filter(n -> n.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
