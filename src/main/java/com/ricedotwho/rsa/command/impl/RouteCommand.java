package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.Ring;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.RingAction;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.phys.Vec3;

@CommandInfo(name = "route", aliases = "r", description = "Handles creating autoroutes")
public class RouteCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .requires(src -> {
                    if (!Location.getArea().is(Island.Dungeon)) {
                        ChatUtils.chat("Cannot use AutoRoutes outside of Dungeons!");
                        return false;
                    }

                    Room room = Map.getCurrentRoom();
                    if (room == null) {
                        ChatUtils.chat("Couldn't find current room");
                        return false;
                    }

                    return true;
                })
                .then(literal("add")
                        .then(argument("type", StringArgumentType.word())
                                .then(argument("widthX", DoubleArgumentType.doubleArg(0))
                                        .then(argument("height", DoubleArgumentType.doubleArg(0))
                                                .then(argument("widthZ", DoubleArgumentType.doubleArg(0))
                                                        .executes(ctx -> {
                                                            Room room = Map.getCurrentRoom();

                                                            AutoRoutes routesModule = RSM.getModule(AutoRoutes.class);
                                                            String actionName = StringArgumentType.getString(ctx, "type");
                                                            RingAction action = routesModule.getAction(actionName);
                                                            if (action == null) {
                                                                ChatUtils.chat("Couldn't find an action named: {}", actionName);
                                                                return 1;
                                                            }

                                                            Pos pos = new Pos(mc.player.getX(), mc.player.getY(), mc.player.getX());
                                                            Pos relativePos = RoomUtils.getRelativePosition(pos, room);
                                                            if (relativePos == null) {
                                                                ChatUtils.chat("Failed to get relative position");
                                                                return 1;
                                                            }

                                                            Rotation rot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
                                                            Rotation relativeRot = RoomUtils.getRelativeYaw(rot);

                                                            Ring ring = new Ring(
                                                                    room.getData().name(),
                                                                    action,
                                                                    relativePos,
                                                                    relativeRot
                                                            );

                                                            ring.widthX = DoubleArgumentType.getDouble(ctx, "widthX");
                                                            ring.height = DoubleArgumentType.getDouble(ctx, "height");
                                                            ring.widthZ = DoubleArgumentType.getDouble(ctx, "widthZ");

                                                            routesModule.rings.add(ring);
                                                            ChatUtils.chat("Added a %s ring", ring.action.name);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(literal("remove")
                        .executes(ctx -> {
                            AutoRoutes routesModule = RSM.getModule(AutoRoutes.class);
                            Room room = Map.getCurrentRoom();
                            Pos pos = new Pos(mc.player.getX(), mc.player.getY(), mc.player.getX());
                            Pos relativePos = RoomUtils.getRelativePosition(pos, room);
                            if (relativePos == null) {
                                ChatUtils.chat("Failed to get relative position");
                                return 1;
                            }

                            Ring ring = routesModule.getClosestRing(room, relativePos.x, relativePos.y, relativePos.z);
                            if (ring == null) {
                                ChatUtils.chat("Couldn't find any rings");
                                return 1;
                            }

                            routesModule.rings.remove(ring);
                            ChatUtils.chat("Removed a {} ring", ring.action.name);
                            return 1;
                        })
                );
    }

}