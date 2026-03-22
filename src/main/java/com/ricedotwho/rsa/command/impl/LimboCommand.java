package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ricedotwho.rsa.IMixin.IConnection;
import com.ricedotwho.rsa.module.impl.player.autopet.AutoPet;
import com.ricedotwho.rsa.module.impl.player.autopet.pet.ChatPetRule;
import com.ricedotwho.rsa.module.impl.player.autopet.pet.IslandPetRule;
import com.ricedotwho.rsa.module.impl.player.autopet.pet.PetRule;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandInfo(name = "limbo", aliases = "limbo", description = "Sends an invalid slot packet to send you to limbo.")
public class LimboCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .executes(this::limbo);
    }

    private int limbo(CommandContext<ClientSuggestionProvider> clientSuggestionProviderCommandContext) {
        if (Minecraft.getInstance().getConnection() == null) return 0;
        // nneed to send immediately because swap manager blocks
        ((IConnection) Minecraft.getInstance().getConnection().getConnection()).sendPacketImmediately(new ServerboundSetCarriedItemPacket(9));
        return 1;
    }

}