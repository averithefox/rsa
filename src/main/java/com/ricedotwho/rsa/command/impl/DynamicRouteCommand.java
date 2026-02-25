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
import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.component.impl.pathfinding.*;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.DynamicRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.*;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitClick;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitEWRaytrace;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitSecrets;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.DynamicEtherwarpNode;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.UseNode;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonInfo;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.util.freetype.FreeType;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
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
                .then(literal("stop")
                        .executes(DynamicRouteCommand::stopPathing)
                )
                .then(literal("path")
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .executes((ctx) -> path(ctx, ctx.getArgument("pos", WorldCoordinates.class)))
                        )
                )
                .then(literal("roompath")
                        .then(argument("room", StringArgumentType.greedyString())
                                .executes((ctx) -> dungeonRoomPath(ctx, ctx.getArgument("room", String.class)))
                        )
                )
                .then(literal("insta")
                        .then(argument("room1", StringArgumentType.string())
                                .then(argument("room2", StringArgumentType.string())
                                        .then(argument("room3", StringArgumentType.string())
                                                .executes((ctx) -> insta(ctx, ctx.getArgument("room1", String.class), ctx.getArgument("room2", String.class), ctx.getArgument("room3", String.class)))
                                        )
                                )
                        )
                )
                .then(literal("roomfind")
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .executes((ctx) -> dungeonPath(ctx, ctx.getArgument("pos", WorldCoordinates.class)))
                        )
                )
                .then(literal("cp")
                        .executes(DynamicRouteCommand::copyBlockPosLook)
                )
                .then(literal("remove")
                        .executes(DynamicRouteCommand::removeNode)
                );
    }

    private static int stopPathing(CommandContext<ClientSuggestionProvider> ctx) {
        boolean bl = RSM.getModule(DynamicRoutes.class).cancelPathing();
        if (bl) {
            RSA.chat("Cancelled pathing!");
            return 1;
        }

        RSA.chat("No pathing active!");
        return 0;
    }
    private static int copyBlockPosLook(CommandContext<ClientSuggestionProvider> ctx) {
        Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float yaw = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
        float pitch = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();

        Vec3 vec = EtherUtils.rayTraceBlock(61, yaw, pitch, pos);
        Vec3 viewVector = vec.subtract(pos).normalize();
        Vec3 vec2 = viewVector.scale(EtherUtils.EPSILON).add(vec);
        BlockPos ether = BlockPos.containing(vec2);

        String s = ether.getX() + " " + ether.getY() + " " + ether.getZ();
        Minecraft.getInstance().keyboardHandler.setClipboard(s);
        RSA.chat("Copied " + s);
        return 1;
    }

    private static int path(CommandContext<ClientSuggestionProvider> ctx, WorldCoordinates pos) {
        if (Minecraft.getInstance().player == null) return 0;
        BlockPos blockPos = BlockPos.containing(pos.x().value(), pos.y().value(), pos.z().value());
        BlockPos startPos = BlockPos.containing(Minecraft.getInstance().player.position().subtract(0, EtherUtils.EPSILON, 0d));
        RSM.getModule(DynamicRoutes.class).executePath(startPos, new GoalXYZ(blockPos));
        return 1;
    }



    private static int insta(CommandContext<ClientSuggestionProvider> ctx, String... roomNames) {
        if (Minecraft.getInstance().player == null) return 0;
        BlockPos startPos = BlockPos.containing(Minecraft.getInstance().player.position().subtract(0, EtherUtils.EPSILON, 0d));


        List<GoalDungeonRoom> goals = new ArrayList<>();

        for (String s : roomNames) {
            UniqueRoom uniqueRoom = DungeonInfo.getRoomByName(s);
            if (uniqueRoom == null || uniqueRoom.getTiles().isEmpty()) {
                RSA.chat("Room not loaded!");
            }

            GoalDungeonRoom goal = GoalDungeonRoom.create(uniqueRoom);
            if (goal == null) {
                RSA.chat("Failed to create goal!");
                return 0;
            }
            goals.add(goal);
        }

        RSM.getModule(DynamicRoutes.class).pathGoals(startPos, goals);
        return 1;
    }

    private static int dungeonRoomPath(CommandContext<ClientSuggestionProvider> ctx, String uniqueRoomName) {
        if (Minecraft.getInstance().player == null) return 0;

        UniqueRoom uniqueRoom = DungeonInfo.getRoomByName(uniqueRoomName);
        if (uniqueRoom == null || uniqueRoom.getTiles().isEmpty()) {
            RSA.chat("Room not loaded!");
        }

        BlockPos startPos = BlockPos.containing(Minecraft.getInstance().player.position().subtract(0, EtherUtils.EPSILON, 0d));
        GoalDungeonRoom goal = GoalDungeonRoom.create(uniqueRoom);
        if (goal == null) {
            RSA.chat("Failed to create goal!");
            return 0;
        }

        RSM.getModule(DynamicRoutes.class).executePath(startPos, goal);
        return 1;
    }

    private static int dungeonPath(CommandContext<ClientSuggestionProvider> ctx, WorldCoordinates pos) {
        if (Minecraft.getInstance().player == null) return 0;
        BlockPos blockPos = BlockPos.containing(pos.x().value(), pos.y().value(), pos.z().value());
        BlockPos startPos = BlockPos.containing(Minecraft.getInstance().player.position().subtract(0, EtherUtils.EPSILON, 0d));
        GoalDungeonXYZ goal = GoalDungeonXYZ.create(blockPos);
        if (goal == null) {
            RSA.chat("Failed to create goal!");
            return 0;
        }

        RSM.getModule(DynamicRoutes.class).executePath(startPos, goal);
        return 1;
    }


    private static int clearNodes(CommandContext<ClientSuggestionProvider> ctx) {
        if (!RSM.getModule(DynamicRoutes.class).clearNodes()) {
            RSA.chat("No nodes found!");
            return 0;
        }
        RSA.chat("Cleared all nodes!");
        return 1;
    }

    private static int removeNode(CommandContext<ClientSuggestionProvider> ctx) {
        if (!RSM.getModule(DynamicRoutes.class).removeNearest()) {
            RSA.chat("No nodes found in this room!");
            return 0;
        }
        RSA.chat("Removed node!");
        return 1;
    }

    private static int addNode(CommandContext<ClientSuggestionProvider> ctx) {
        if (Minecraft.getInstance().player == null) return 0;
        boolean bl = RSM.getModule(DynamicRoutes.class).addNode(Minecraft.getInstance().player);
        if (!bl) {
            RSA.chat("Failed to raytrace etherwarp!");
            return 0;
        }
        RSA.chat("Added " + NodeType.ETHERWARP + " node!");
        return 1;
    }
}