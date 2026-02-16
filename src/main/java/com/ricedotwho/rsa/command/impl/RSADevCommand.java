package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsa.component.impl.TickFreeze;
import com.ricedotwho.rsa.utils.Util;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "rdev", description = "Handles creating autoroutes")
public class RSADevCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("tickrate")
                        .then(argument("tick rate", FloatArgumentType.floatArg(0, 20))
                                .executes(ctx -> {
                                    Util.setTickRate(FloatArgumentType.getFloat(ctx, "tick rate"));
                                    TaskComponent.onMilli(2500, () -> Util.setTickRate(20, false));
                                    return 1;
                                })
                        )
                        .then(literal("freeze")
                                .executes(ctx -> {
                                    TickFreeze.freeze(5000);
                                    return 1;
                                })
                        )
                );
    }
}