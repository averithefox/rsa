package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.mojang.datafixers.util.Function5;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.ArgumentManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings.*;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.SubActionManager;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.Map;

public enum RingType {
    ALIGN("align", AlignRing::new, 0f),
    FAST_ALIGN("fastalign", FastAlign::new, 0.01f),
    STOP("stop", StopRing::new, 0.02f),
    WALK("walk", WalkRing::new, 0.03f),
    JUMP("jump", JumpRing::new, 0.04f),
    BONZO("bonzo", BonzoRing::new, 0.05f),
    FAST_BONZO("fastbonzo", FastBonzoRing::new, 0.06f),
    EDGE("edge", EdgeRing::new, 0.06f),
    MOVEMENT("movement", MovementRing::new, 0.07f),
    LOOK("look", LookRing::new, 0.08f),
    BOOM("boom", BoomRing::new, 0.009f),
    LEAP("leap", LeapRing::new, 0.010f),
    USE("use", UseRing::new, 0.011f),
    CHAT("chat", ChatRing::new, 0.012f),
    COMMAND("command", CommandRing::new, 0.013f),
    BLINK("blink", BlinkRing::new, 0.014f);

    @Getter
    private final String name;
    private final Function5<Pos, Pos, ArgumentManager, SubActionManager, Map<String, Object>, Ring> factory;
    @Getter
    private final float renderSizeOffset;

    RingType(String s, Function5<Pos, Pos, ArgumentManager, SubActionManager, Map<String, Object>, Ring> factory, float renderSizeOffset) {
        this.name = s;
        this.renderSizeOffset = renderSizeOffset;
        this.factory = factory;
    }

    public Ring supply(Pos min, Pos max, ArgumentManager manager, SubActionManager actions, Map<String, Object> extraData) {
        if (this.factory == null || Minecraft.getInstance().player == null) return null;
        return this.factory.apply(min, max, manager, actions, extraData);
    }

    public static RingType byName(String name) {
        return Arrays.stream(RingType.values()).filter(n -> n.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
