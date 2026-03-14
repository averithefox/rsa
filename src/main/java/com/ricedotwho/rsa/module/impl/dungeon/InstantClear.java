package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.utils.Util;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonInfo;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomState;
import com.ricedotwho.rsm.component.impl.map.map.RoomType;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.RotationUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ModuleInfo(aliases = {"Instant Clear", "Insta Clear"}, id = "InstaClear", category = Category.DUNGEONS)
public class InstantClear extends Module {

    private final float
            MAP_LEFT = -225,
            MAP_RIGHT = 25,
            MAP_TOP = -225,
            MAP_BOTTOM = 25;

    private Room targetRoom;
    private State state = State.NONE;
    private Consumer<ClientboundPlayerPositionPacket> onTeleport;
    private int deathTicks = 0;
    private boolean oddPearl;

    @Override
    public void onEnable() {
        this.resetState();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @SubscribeEvent
    public void onClientTickEvent(ClientTickEvent.Start event) {
        if (targetRoom == null && Dungeon.isStarted()) {
            findRoom();
        }

        if (targetRoom == null || state == State.WAITING) return;

        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        Room currentRoom = Map.getCurrentRoom();

        int roofHeight = currentRoom == null ? 100 : currentRoom.getRoofHeight();
        int bottomDepth = currentRoom == null ? 20 : currentRoom.getBottom();

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        boolean isAbove = playerY >= roofHeight;
        boolean isBelow = playerY <= bottomDepth;
        boolean isOutside = isOutside(playerX, playerZ);

        // pearl out
        if (!isAbove && !isBelow && !isOutside) {
            if (targetRoom.equals(currentRoom)) {
                ChatUtils.chat("Reached target room.");
                setState(State.NONE);
                targetRoom = null;
                return;
            }

            int distance = getCeilingDistance(playerX, playerY, playerZ, level);
            state = distance > 5 ? State.TELEPORTING : State.PEARLING;

            double roofDist = roofHeight - playerY;
            if (roofDist <= 2 && oddPearl && deathTicks < 37) {
                return;
            }

            switch (state) {
                case PEARLING -> pearl(player.getYRot(), -90.0F);
                case TELEPORTING -> teleport((int) Math.ceil(distance / 12F), player.getYRot(), -90.0F, player);
            }

            return;
        }

        // teleport outside the map
        if (isAbove && !isOutside) {
            if (playerY % 2 == 1 && deathTicks < 37) {
                return;
            }

            setState(State.TELEPORTING);

            Vec2 targetPos = getOutsideVec((float) playerX, (float) playerZ);
            teleportTo(player, targetPos);

            return;
        }

        // teleport below the map
        if (!isBelow) {
            setState(State.TELEPORTING);

            double distance = playerY - bottomDepth;
            int teleports = (int) Math.ceil(distance / 12) + 1;
            teleport(teleports, player.getYRot(), 90.0F, player);

            return;
        }

        // teleport to the room
        state = targetRoom.equals(currentRoom) ? State.PEARLING : State.TELEPORTING;

        switch (state) {
            case TELEPORTING -> {
                Vec2 targetPos = new Vec2(targetRoom.getX(), targetRoom.getZ());
                teleportTo(player, targetPos);
            }
            case PEARLING -> {
                int distance = getCeilingDistance(playerX, playerY, playerZ, level);
                state = distance > 5 ? State.TELEPORTING : State.PEARLING;

                switch (state) {
                    case PEARLING -> pearl(player.getYRot(), -90.0F);
                    case TELEPORTING -> teleport((int) Math.ceil(distance / 12F), player.getYRot(), -90.0F, player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerTickEvent(ServerTickEvent event) {
        deathTicks--;
        if (deathTicks < 0) deathTicks = 40;
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket packet) {
            double y = packet.change().position().y;
            oddPearl = y % 2 == 0;

            if (state == State.WAITING) {
                TaskComponent.onServerTick(() -> {
                    setState(State.NONE);
                    if (onTeleport != null) {
                        onTeleport.accept(packet);
                        onTeleport = null;
                    }
                });
            }
        }

        if (event.getPacket() instanceof ClientboundSetTimePacket packet) {
            deathTicks = (int) (40 - (packet.gameTime() % 40));
        }
    }

    @SubscribeEvent
    public void onWorldEventLoad(WorldEvent.Load event) {
        resetState();
    }

    private void resetState() {
        targetRoom = null;
        setState(State.NONE);
        onTeleport = null;
        deathTicks = 0;
    }

    private void teleportTo(LocalPlayer player, Vec2 vec) {
        double diffX = vec.x - player.getX();
        double diffZ = vec.y - player.getZ();
        float outsideYaw = (float) RotationUtils.wrapAngleTo180((double) ((float) Math.atan2(diffZ, diffX) * 180.0F) / Math.PI - 90.0D);

        double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
        int teleports = (int) Math.ceil(distance / 12);

        teleport(teleports, outsideYaw, 3.0F, player);
    }

    private void teleport(int teleports, float yaw, float pitch, LocalPlayer player) {
        if (teleports <= 0) return;

        setState(State.WAITING);
        onTeleport = packet -> setState(State.TELEPORTING);

        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            if (!SwapManager.swapItem("ASPECT_OF_THE_VOID") || player.getLastSentInput().shift()) {
                return;
            }

            for (int i = 0; i < teleports; i++) {
                if (!SwapManager.sendAirC08(yaw, pitch, true, false)) {
                    setState(State.NONE);
                    onTeleport = null;
                    break;
                }
            }
        });
    }

    private void pearl(float yaw, float pitch) {
        setState(State.WAITING);
        onTeleport = packet -> setState(State.PEARLING);

        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            if (!SwapManager.swapItem("ENDER_PEARL")) {
                return;
            }

            if (!SwapManager.sendAirC08(yaw, pitch, true, false)) {
                setState(State.NONE);
                onTeleport = null;
            }
        });
    }

    private void findRoom() {
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        List<String> orderedRooms = BloodBlink.getRooms();

        Set<UniqueRoom> rooms = DungeonInfo.getUniqueRooms()
                .stream()
                .filter(r -> {
                    Room mainRoom = r.getMainRoom();
                    if (mainRoom == null || mainRoom.getState().equals(RoomState.CLEARED)) return false;
                    RoomType type = mainRoom.getData().type();
                    return type == RoomType.NORMAL || type == RoomType.RARE;
                })
                .sorted(Comparator.comparingDouble(r -> orderedRooms.indexOf(r.getName())))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (rooms.isEmpty()) return;

        for (UniqueRoom room : rooms) {
            boolean isEmpty = room.getDoors()
                    .stream()
                    .allMatch(d -> Util.equalsOneOf(d.getState(), RoomState.UNDISCOVERED, RoomState.UNOPENED)) ||
                    room.getTiles().stream().allMatch(tile -> {
                        int topX = tile.getX();
                        int topY = tile.getZ();
                        int bottomX = topX + 32;
                        int bottomY = topY + 32;

                        AABB bounds = new AABB(
                                bottomX, tile.getBottom(), bottomY,
                                topX, tile.getRoofHeight(), topY
                        );

                        List<Entity> entities = level.getEntities(null, bounds);
                        return entities.isEmpty();
                    });

            if (!isEmpty) continue;

            targetRoom = room.getMainRoom();
            ChatUtils.chat("Targeted room: " + room.getName());

            break;
        }
    }

    private int getCeilingDistance(double x, double y, double z, ClientLevel level) {
        int distance = 255;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = (int) y; i < 255; i++) {
            BlockState blockState = level.getBlockState(pos.set(x, i, z));
            Block block = blockState.getBlock();

            if (block != Blocks.AIR) {
                distance = (int) (i - y);
                break;
            }
        }

        return distance;
    }

    private void setState(State state) {
        this.state = state;
    }

    private Vec2 getOutsideVec(float x, float z) {
        float leftDist = x - MAP_LEFT;
        float rightDist = MAP_RIGHT - x;
        float topDist = z - MAP_TOP;
        float bottomDist = MAP_BOTTOM - z;

        float dx = Math.min(leftDist, rightDist);
        float dz = Math.min(topDist, bottomDist);

        if (dx < dz) {
            return leftDist < rightDist ? new Vec2(MAP_LEFT, z) : new Vec2(MAP_RIGHT, z);
        } else {
            return topDist < bottomDist ? new Vec2(x, MAP_TOP) : new Vec2(x, MAP_BOTTOM);
        }
    }

    private boolean isOutside(double x, double z) {
        return x < MAP_TOP || x > MAP_BOTTOM || z < MAP_LEFT || z > MAP_RIGHT;
    }

    private enum State {
        TELEPORTING,
        PEARLING,
        WAITING,
        NONE
    }

}