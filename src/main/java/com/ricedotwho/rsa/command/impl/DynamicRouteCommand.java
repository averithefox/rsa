package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.DynamicRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.*;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitClick;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitEWRaytrace;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitSecrets;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.UseNode;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandInfo(name = "dynamicroute", aliases = {"dr"}, description = "Handles creating dynamic routes.")
public class DynamicRouteCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .executes(DynamicRouteCommand::addNode)
                )
                .then(literal("clear")
                        .executes(DynamicRouteCommand::clearNodes)
                )
                .then(literal("remove")
                        .executes(DynamicRouteCommand::removeNode)
                );
    }


    private static int clearNodes(CommandContext<ClientSuggestionProvider> ctx) {
        if (!RSM.getModule(DynamicRoutes.class).clearNodes()) {
            ChatUtils.chat("No nodes found!");
            return 0;
        }
        ChatUtils.chat("Cleared all nodes!");
        return 1;
    }

    private static int removeNode(CommandContext<ClientSuggestionProvider> ctx) {
        if (!RSM.getModule(DynamicRoutes.class).removeNearest()) {
            ChatUtils.chat("No nodes found in this room!");
            return 0;
        }
        ChatUtils.chat("Removed node!");
        return 1;
    }

    private static int addNode(CommandContext<ClientSuggestionProvider> ctx) {
        if (Minecraft.getInstance().player == null) return 0;
        boolean bl = RSM.getModule(DynamicRoutes.class).addNode(Minecraft.getInstance().player);
        if (!bl) {
            ChatUtils.chat("Failed to raytrace etherwarp!");
            return 0;
        }
        ChatUtils.chat("Added " + NodeType.ETHERWARP + " node!");
        return 1;
    }
}