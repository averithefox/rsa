package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.component.impl.TickFreeze;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p5.Relics;
import com.ricedotwho.rsa.utils.Util;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "rdev", description = "Developer")
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
                )
                .then(literal("iszero")
                        .executes(ctx -> {
                            RSA.chat("Zero: %s", Util.isZero());
                            return 1;
                        })
                )
                .then(literal("reliclook")
                        .then(argument("relic", StringArgumentType.string())
                                .executes(ctx -> {
                                    RSM.getModule(Relics.class).test(StringArgumentType.getString(ctx, "relic"));
                                    return 1;
                                })
                        )
                );
    }
}