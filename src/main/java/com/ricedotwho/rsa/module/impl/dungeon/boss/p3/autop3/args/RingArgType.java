package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type.*;
import lombok.Getter;

import java.util.Set;
import java.util.function.Function;

public enum RingArgType {
    TERM(TermArg::create, Set.of("term")),
    LEAP(LeapArg::create, Set.of("leap")),
    GROUND(GroundArg::create, Set.of("ground", "g")),
    TRIGGER(TriggerArg::create, Set.of("trigger", "click", "c")),
    DELAY(DelayArg::create, Set.of("delay", "d"));

    private final Function<String, Argument> factory;
    @Getter
    private final Set<String> aliases;

    RingArgType(Function<String, Argument> factory, Set<String> aliases) {
        this.factory = factory;
        this.aliases = aliases;
    }

    public Argument create(String arg) {
        return this.factory.apply(arg);
    }

    public static RingArgType fromAliases(String string) {
        for (RingArgType type : values()) {
            if (type.getAliases().contains(string)) return type;
        }
        return null;
    }
}
