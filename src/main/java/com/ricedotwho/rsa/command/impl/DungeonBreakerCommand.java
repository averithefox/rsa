package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.module.impl.dungeon.boss.BreakerAura;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "breakeraura", aliases = "ba", description = "Breaker Aura command")
public class DungeonBreakerCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("load")
                        .then(argument("config", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String config = StringArgumentType.getString(ctx, "config");
                                    BreakerAura.load(config);
                                    RSA.chat("Loaded %s", config);
                                    return 1;
                                })
                        )
                )
                .then(literal("e")
                        .executes(ctx -> {
                            BreakerAura ba = RSM.getModule(BreakerAura.class);
                            ba.getEdit().toggle();
                            RSA.chat((ba.getEdit().getValue() ? "Enabled" : "Disabled") +  " breaker aura");
                            return 1;
                        })
                );
    }
}