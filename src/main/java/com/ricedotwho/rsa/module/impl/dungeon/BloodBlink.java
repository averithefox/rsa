package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonScanner;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Getter
@ModuleInfo(aliases = "BB", id = "BloodBlink", category = Category.DUNGEONS)
public class BloodBlink extends Module {
    private static final Vec3 SLAB_BLOCK_OFFSET = new Vec3(-10, 82.5, -13); // Sometimes y = 81.5
    private static final Vec3 MIDDLE_MAP_COORDS = new Vec3(-104.5, 0, -104.5);

    private Room bloodRoom;
    private Room startRoom;

    // Packet Order
    // C09
    // C08
    // C03 ??

    public BloodBlink() {
        this.registerProperty(
                // todo: register settings

        );
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    private int serverTickTimer = -1;
    private boolean loaded = false;
    private int state = 0;
    private boolean isLower = false;

    public boolean forceNextSneak = false;

    // Options
    public boolean waitForGround = false;
    public boolean auto = true;
    public int deathTickOffset = 0;

    @Override
    public String getName() {
        return "BloodBlink";
    }

    @SubscribeEvent
    public void WorldEventLoad(WorldEvent.Load event) {
        this.bloodRoom = null;
        this.startRoom = null;
        this.isLower = false;
        this.serverTickTimer = -1;
        reset();

        state = -1;
        loaded = true;
    }

    private void resetState() {
        state = -1;
        this.forceNextSneak = false;
    }


    public long encodeIndex(int x, int z) {
        return (long) x | (((long) z) << 32);
    }

    public long encodeIndex(Point p) {
        return encodeIndex(p.x, p.y);
    }

    private void rightClick() {
        Minecraft.getInstance().gameMode.useItem(Minecraft.getInstance().player, InteractionHand.MAIN_HAND);
    }


    @SubscribeEvent
    public void onTickStart(ClientTickEvent.Start event) {
        if (Location.getArea() != Island.Dungeon || Minecraft.getInstance().player == null) return;
        LocalPlayer player = Minecraft.getInstance().player;

        if (this.isEnabled() && isBlinking() && Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
            cancel();
            return;
        }

        if (serverTickTimer > 5) {
            switch (state) {
                case -1: {
                    if (!auto) break;
                    KeyMapping.releaseAll();
                    state = 0;
                    // Don't break here, overflow into next state
                }

                case 0: {
                    if (waitForGround && !player.onGround()) break;
                    if (startRoom == null) {
                        startRoom = ScanUtils.getRoomFromPos(player.getBlockX(), player.getBlockZ());
                    }
                    if (startRoom == null) break;

                    if (goToSlab()) {
                        state = 1;
                        forceNextSneak = true; // To align with C08
                    }
                    break;
                }

                case 1: {
                    // Can't merge with previous tick because C08 is sent before C03
                    ItemStack item = player.getInventory().getItem(player.getInventory().getSelectedSlot());
                    if (item == null || item.getItem() != Items.DIAMOND_SHOVEL) {
                        if (!SwapManager.swapItem(Items.DIAMOND_SHOVEL)) break;
                    }

                    //PacketOrderManager.register(this::rightClick, PacketOrderManager.STATE.ITEM_USE);
                    state = 2;
                    break;
                }

                case 2: {

                    break;
                }

                case 3: {
                    player.setXRot(-90.0f);
                    state = 4;
                    break;
                }
                case 4: {
                    if (pearl()) state = 5;
                    break;
                }

                case 5: {
                    // Wait for pearl S08s
                    break;
                }

                case 6: {
                    if (bloodRoom != null) {
                        // Loaded we can skip to next, since we break here it will be 1 tick behind but whatever
                        state = 17;
                        break;
                    }
                    if ((serverTickTimer % 40) < 10) {
                        if (aotv(7)) {
                            state = 7;
                        }
                    }
                    break;
                }

                case 7: {
                    // Wait for S08s
                    break;
                }

                case 8: {
                    float[] angles = EtherUtils.getYawAndPitch(MIDDLE_MAP_COORDS.add(0.0d, player.getY(), 0.0d), false, player, false);
                    player.setYRot(angles[0]);
                    player.setXRot(0.0f);
                    state = 9;
                    break;
                }

                case 9: {
                    float deltaX = (float) (player.getX() - MIDDLE_MAP_COORDS.x);
                    float deltaZ = (float) (player.getZ() - MIDDLE_MAP_COORDS.z);
                    if (aotv(Math.round(Mth.sqrt(deltaX * deltaX + deltaZ * deltaZ) / 12f))) {
                        state = 10;
                    }
                }

                case 10: {
                    // Await S08 to respawn in start room

                    break;
                }

                case 11: {
                    // Most of this should be done in case 0 anyways
                    if (waitForGround && !player.onGround()) break;
                    if (bloodRoom == null) {
                        // Still null, we didn't find it
                        ChatUtils.chat("Could not find blood!");
                        state = 31;
                        break;
                    }

                    if (startRoom == null) {
                        startRoom = ScanUtils.getRoomFromPos(player.getBlockX(), player.getBlockZ());
                    }
                    if (startRoom == null) break;

                    if (goToSlab()) {
                        state = 12;
                        forceNextSneak = true; // To align with C08
                    }
                    break;
                }

                case 12: {
                    // Can't merge with previous tick because C08 is sent before C03
                    ItemStack item = player.getInventory().getItem(player.getInventory().getSelectedSlot());
                    if (item.getItem() != Items.DIAMOND_SHOVEL) {
                        if (!SwapManager.swapItem(Items.DIAMOND_SHOVEL)) break;
                    }

                    //PacketOrderManager.register(this::rightClick, PacketOrderManager.STATE.ITEM_USE);
                    state = 13;
                    break;
                }

                case 13: {
                    // Wait for S08
                    break;
                }

                case 14: {
                    player.setXRot(-90f);
                    state = 15;
                    break;
                }

                case 15: {
                    if (pearl()) state = 16;
                    break;
                }

                case 16: {
                    // Wait for pearl S08
                    break;
                }

                case 17: {
                    if (Minecraft.getInstance().gameMode == null) break;
                    ItemStack item = player.getInventory().getItem(player.getInventory().getSelectedSlot());;
                    if (item.getItem() != Items.DIAMOND_SHOVEL) {
                        SwapManager.swapItem(Items.DIAMOND_SHOVEL);
                        break;
                    }
                    if (Dungeon.isStarted() && (serverTickTimer % 40) < 35) {
                        if (bigBlink())
                            state = 30;
                    }
                    break;
                }

                case 29: {
                    player.setXRot(-90f);
                    if (pearl()) state = 30;
                    break;
                }

                case 30: {
                    // Wait for pearl S08
                    break;
                }

                case 31: {
                    // Done
                    break;
                }

                default:
                    break;
            }
        }
    }

    private Direction getVoidRotation() {
        Direction rotation;
        int xIndex = (startRoom.getX() - DungeonScanner.startX) / DungeonScanner.roomSize;
        int zIndex = (startRoom.getZ() - DungeonScanner.startZ) / DungeonScanner.roomSize;
        if (xIndex == 0) {
            rotation = Direction.WEST;
        } else if (zIndex == 0) {
            rotation = Direction.NORTH;
        } else if (xIndex > zIndex) {
            rotation = Direction.EAST;
        } else {
            rotation = Direction.SOUTH;
        }
        return rotation;
    }

    private static Vec3 fastRotateVec(Direction direction, Vec3 vec) {
        return switch (direction) {
            case EAST -> new Vec3(-vec.z, vec.y, vec.x);
            case SOUTH -> new Vec3(-vec.x, vec.y, -vec.z);
            case WEST -> new Vec3(vec.z, vec.y, -vec.x);
            default -> vec; // North returns itself
        };
    }

    private static Vec3 fastRotateVec(Direction direction, double x, double y, double z) {
        return switch (direction) {
            case NORTH -> new Vec3(x, y, z);
            case EAST -> new Vec3(-z, y, x);
            case SOUTH -> new Vec3(-x, y, -z);
            case WEST -> new Vec3(z, y, -x);
            default -> Vec3.ZERO;
        };
    }

    private boolean bigBlink() {
        if (!SwapManager.checkServerItem(Items.DIAMOND_SHOVEL)) return false; // Need to check order because packet order gets cooked here
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;

        Direction dir = getVoidRotation();
        aotv0(4, dir.toYRot(), 0f, player);
        aotv0(9, 0f, 90f, player);

        Vec3 playerPos = Minecraft.getInstance().player.position().add(fastRotateVec(dir, 0, 0d, -48d));

        float deltaX = (float) ((bloodRoom.getX() + 0.5d) - playerPos.x());
        float deltaZ = (float) ((bloodRoom.getZ() + 0.5d) - playerPos.z());

        float[] angles = EtherUtils.getYawAndPitch(deltaX, 0.0d, deltaZ);
        aotv0(Math.round(Mth.sqrt(deltaX * deltaX + deltaZ * deltaZ) / 12f), angles[0], 0f, player);
        aotv0(5, 0f, -90f, player);

        boolean bl = SwapManager.swapItem(Items.ENDER_PEARL);
        if (!bl) {
            ChatUtils.chat("Could not swap to pearls!");
            return true;
        }
        player.setXRot(-90f);
        Minecraft.getInstance().gameMode.useItem(Minecraft.getInstance().player, InteractionHand.MAIN_HAND);
        return true;
    }

    private void aotv0(int count, float yaw, float pitch, LocalPlayer playerEntity) {
        playerEntity.setYRot(yaw);
        playerEntity.setXRot(pitch);
        for (int i = 0; i < count; i++) {
            Minecraft.getInstance().gameMode.useItem(Minecraft.getInstance().player, InteractionHand.MAIN_HAND);
        }
    }

    public boolean isBlinking() {
        return state < 30 && state > -1;
    }

    public void doBlink() {
        reset();
        state = 0; // Bypass auto
        KeyMapping.releaseAll();
    }

//    @Override
//    public void onKeyPressed(InputUtil.Key key, CancellableEventCallback callback) {
//        if (!this.isEnabled() || !isBlinking()) return;
//        GameOptions settings = Minecraft.getInstance().options;
//        if (settings == null) return;
//
//        if (key.getCode() == settings.forwardKey.getDefaultKey().getCode() || key.getCode() == settings.leftKey.getDefaultKey().getCode() || key.getCode() == settings.rightKey.getDefaultKey().getCode() || key.getCode() == settings.backKey.getDefaultKey().getCode() || key.getCode() == settings.jumpKey.getDefaultKey().getCode()) {
//            cancel();
//            return;
//        }
//    }

    private void cancel() {
        reset();
        this.state = 31; // End
        ChatUtils.chat("Cancelling blood blink!");
    }

    private Vec3 getSlabLocalPosition() {
        Direction dir = startRoom.getUniqueRoom().getRotation().toDir();
        if (dir == null) return null;
        return fastRotateVec(dir, SLAB_BLOCK_OFFSET).add(0.5, 0.0, 0.5);
    }

    private boolean aotv(int count) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().gameMode == null) return false;
        ItemStack item = Minecraft.getInstance().player.getInventory().getItem(Minecraft.getInstance().player.getInventory().getSelectedSlot());;
        if (item.getItem() != Items.DIAMOND_SHOVEL) {
            SwapManager.swapItem(Items.DIAMOND_SHOVEL);
            return false;
        }

        if (!SwapManager.checkServerItem(Items.DIAMOND_SHOVEL)) return false; // Need to check order because packet order gets cooked here

        for (int i = 0; i < count; i++) {
            Minecraft.getInstance().gameMode.useItem(Minecraft.getInstance().player, InteractionHand.MAIN_HAND);
        }
        return true;
    }

    private boolean pearl() {
        if (!SwapManager.checkClientItem(Items.ENDER_PEARL)) {
            if (!SwapManager.swapItem(Items.ENDER_PEARL)) return false; // C09 is sent before C08 so should swap in time
        }
        //PacketOrderManager.register(this::rightClick, PacketOrderManager.STATE.ITEM_USE);
        return true;
    }


    private boolean goToSlab() {
//        if (Minecraft.getInstance().player == null || !loaded) return false;
//        Vec3 slab = getSlabLocalPosition();
//        if (slab == null) return false;
//        Block block = startRoom.getLocalBlock(BlockPos.ofFloored(slab));
//        if (block == null) return false;
//        if (block == Blocks.AIR) {
//            isLower = true;
//            slab = slab.add(0.0, -1.0, 0.0);
//        }
//
//        float[] angles = EtherUtils.getYawAndPitch(slab.add(startRoom.getX(), 0.0d, startRoom.getZ()), true, Minecraft.getInstance().player, true);
//        Minecraft.getInstance().player.setYRot(angles[0]);
//        Minecraft.getInstance().player.setXRot(angles[1]);
        return true;
    }

//
//    @Override
//    public void onLoadRoom(Room room) {
//        if (Arrays.stream(room.getRoomData().cores()).anyMatch(i -> i == 1898104308 || i == 698844034)) {
//            this.bloodRoom = room;
//            ChatUtils.chat("Found blood at : " + room.getX() + ", " + room.getZ());
//        }
//    }
//
//    @SubscribeEvent
//    public void onReceivePacket(PacketEvent.Receive event) {
//        Packet<?> packet = event.getPacket();
//        if (serverTickTimer > -1 && packet instanceof CommonPingS2CPacket) {
//            serverTickTimer++;
//            return;
//        }
//
//        if (packet instanceof WorldTimeUpdateS2CPacket timePacket) {
//            long time = timePacket.time();
//            this.serverTickTimer = (int) (time + deathTickOffset) % 40;
//        }
//
//        if (packet instanceof PlayerPositionLookS2CPacket s08) {
//            System.out.println(s08.change().position().y);
//            switch (state) {
//                case 2: {
//                    state = 3;
//                    break;
//                }
//                case 13: {
//                    state = 14;
//                    break;
//                }
//
//                case 5: {
//                    if (s08.change().position().getY() <= (isLower ? 97.0 : 98.0))
//                        state = 4;
//                    else
//                        state = 6;
//                    break;
//                }
//
//
//                case 16: {
//                    if (s08.change().position().getY() <= (isLower ? 97.0 : 98.0))
//                        state = 15;
//                    else
//                        state = 17;
//                    break;
//                }
//
//                case 30: {
//                    Vec3 pos = s08.change().position();
//                    if (DungeonScan.getRoomFromPos(BlockPos.ofFloored(pos)) != bloodRoom || pos.getY() < 65d || pos.getY() > 73d) break; // 66 bedrock block blood, this stops aotv S08s from getting considered as pearls
//                    if (pos.getY() <= 67.0)
//                        state = 29;
//                    else
//                        state = 31;
//                    break;
//                }
//
//                case 7: {
//                    state = 8; // May get called many times but should be received at same time so should be fine.
//                    break;
//                }
//
//                case 10: {
//                    double y = s08.change().position().getY();
//                    if (y == 76.5 || y == 75.5) // respawn Y, should be reliable *enough
//                        state = 11;
//                    break;
//                }
//            }
//        }
//    }
}
