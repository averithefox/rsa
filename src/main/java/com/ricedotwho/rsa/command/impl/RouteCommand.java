package com.ricedotwho.rsa.command.impl;

import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.Ring;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.RingAction;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.world.phys.Vec3;

@CommandInfo(aliases = {"route"}, description = "Handles creating autoroutes")
public class RouteCommand extends Command {

    @Override
    public void execute(String[] args, String message) {
        if (mc.player == null) return;

        if (!Loc.getArea().is(Island.Dungeon)) {
            ChatUtils.chat("Cant use autoroutes outside of dungeons");
            return;
        }

        if (args.length == 0) {
            ChatUtils.chat("Invalid arguments (example: route <add, remove> <action> <wx> <h> <wz>)");
            return;
        }

        Room room = Map.getCurrentRoom();
        if (room == null) {
            ChatUtils.chat("Couldn't find current room");
            return;
        }

        AutoRoutes routesModule = RSM.getModule(AutoRoutes.class);

        Pos pos = new Pos(mc.player.getX(), mc.player.getY(), mc.player.getX());
        Pos relativePos = RoomUtils.getRelativePosition(pos, room);
        if (relativePos == null) {
            ChatUtils.chat("Failed to get relative position");
            return;
        }

        Rotation rot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
        Rotation relativeRot = RoomUtils.getRelativeYaw(rot);

        switch (args[0].toLowerCase()) {
            case "add" -> {
                String actionName = args[1].toLowerCase();

                RingAction action = routesModule.getAction(actionName);
                if (action == null) {
                    ChatUtils.chat("Couldn't find an action named: {}", actionName);
                    return;
                }

                Ring ring = new Ring(
                        room.getData().name(),
                        action,
                        relativePos,
                        relativeRot
                );

                if (args.length >= 4) {
                    try {
                        String widthX = args[2];
                        String height = args[3];
                        String widthZ = args[4];

                        ring.widthX = Double.parseDouble(widthX);
                        ring.height = Double.parseDouble(height);
                        ring.widthZ = Double.parseDouble(widthZ);
                    } catch (NumberFormatException error) {
                        ChatUtils.chat("Failed to parse size arguments");
                        RSA.getLogger().error("Failed to parse size arguments", error);
                    }
                }

                routesModule.rings.add(ring);
                ChatUtils.chat("Added a %s ring", ring.action.name);
            }
            case "remove" -> {
                Ring ring = routesModule.getClosestRing(room, relativePos.x, relativePos.y, relativePos.z);
                if (ring == null) {
                    ChatUtils.chat("Couldn't find any rings");
                    return;
                }

                routesModule.rings.remove(ring);
                ChatUtils.chat("Removed a {} ring", ring.action.name);
            }
        }
    }

}