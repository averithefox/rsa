package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ConfigUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "example", description = "An example command")
public class ExampleCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(ctx -> {
                    ChatUtils.chat("Hi!");
                    return 1;
                })
                .then(literal("hi")
                        .executes(ctx -> {
                            ChatUtils.chat("...");
                            return 1;
                        })
                );
    }
}