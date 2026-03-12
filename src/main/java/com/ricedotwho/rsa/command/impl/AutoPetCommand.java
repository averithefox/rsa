package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.ricedotwho.rsa.module.impl.player.AutoAutoPet;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "ap", aliases = "pet", description = "Auto Auto Pet Command")
public class AutoPetCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("swap")
                        .then(argument("pet", StringArgumentType.greedyString())
                        .executes(c -> swap(c))
                        )
                );
    }

    private int swap(CommandContext<ClientSuggestionProvider> ctx) {
        String petName = ctx.getArgument("pet", String.class);
        if (petName.isEmpty()) {
            ChatUtils.chat("Please enter a pet!");
            return 0;
        }
        AutoAutoPet autoAutoPet = RSM.getModule(AutoAutoPet.class);
        if (!autoAutoPet.isEnabled()) {
            ChatUtils.chat("Please enable auto auto pet!");
            return 0;
        }
        autoAutoPet.swapTo(petName);
        return 1;
    }
}