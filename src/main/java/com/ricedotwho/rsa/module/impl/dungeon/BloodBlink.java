package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonScanner;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomRotation;
import com.ricedotwho.rsm.component.impl.map.map.RoomType;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

@Getter
@ModuleInfo(aliases = "BB", id = "BloodBlink", category = Category.DUNGEONS)
public class BloodBlink extends Module {
    private static final Pos SLAB_BLOCK_OFFSET = new Pos(-9.5, 82, -12.5); // Sometimes y = 81.5
    private static final Vec3 MIDDLE_MAP_COORDS = new Vec3(-104.5, 0, -104.5);

    private Room bloodRoom;
    private Room startRoom;

    // Packet Order
    // C09
    // C08
    // C03 ??

    private int serverTickTimer = -1;
    private int serverTotalTickTimer = 0;
    private int state = 0;
    private boolean isLower = false;

    public boolean forceNextSneak = false;

    // Options
    private final BooleanSetting waitForGround = new BooleanSetting("Wait For Ground", false);
    private final BooleanSetting auto = new BooleanSetting("Auto Blink", true);
    private final NumberSetting deathTickOffset = new NumberSetting("Death Tick Offset", 0.0d, 20.0d, 0.0d, 1.0d);

    public BloodBlink() {
        this.registerProperty(
                waitForGround,
                auto,
                deathTickOffset
        );
    }

    @Override
    public void onEnable() {
        this.resetState();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @SubscribeEvent
    public void WorldEventLoad(WorldEvent.Load event) {
        this.bloodRoom = null;
        this.startRoom = null;
        this.serverTickTimer = -1;
        this.serverTotalTickTimer = 0;
        resetState();

        state = -1;
    }

    public void resetState() {
        state = -1;
        this.isLower = false;
        this.forceNextSneak = false;
    }


    public long encodeIndex(int x, int z) {
        return (long) x | (((long) z) << 32);
    }

    public long encodeIndex(Point p) {
        return encodeIndex(p.x, p.y);
    }


    @SubscribeEvent
    public void onTickStart(ClientTickEvent.Start event) {
        if (Location.getArea() != Island.Dungeon || Minecraft.getInstance().player == null) return;
        LocalPlayer player = Minecraft.getInstance().player;

        if (this.isEnabled() && this.isBlinking() && Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
            cancel();
            return;
        }
        if (serverTotalTickTimer <= 2) return;

        switch (state) {
            case -1: {
                if (!auto.getValue()) break;
                KeyMapping.releaseAll();
                state = 0;
                // Don't break here, overflow into next state
            }

            case 0: {
                if (bloodRoom == null && Dungeon.isStarted()) {
                    ChatUtils.chat("Cannot blood blink while run has started and blood has not been loaded!");
                    state = 31;
                    break;
                }

                if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) break;
                forceNextSneak = true; // Must be setup for etherwarp



                if (waitForGround.getValue() && !player.onGround()) break;
                if (startRoom == null) {
                    startRoom = ScanUtils.getRoomFromPos(player.getBlockX(), player.getBlockZ());
                }
                if (startRoom == null || startRoom.getUniqueRoom() == null) break;
                if (startRoom.getUniqueRoom().getRotation() == RoomRotation.UNKNOWN) break;

                PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
                    if (!SwapManager.swapItem(Items.DIAMOND_SHOVEL)) return;
                    if (!Minecraft.getInstance().player.getLastSentInput().shift()) return;

                    Pos slab = RoomUtils.getRealPosition(SLAB_BLOCK_OFFSET, startRoom);


                    Block block = Minecraft.getInstance().level.getBlockState(slab.asBlockPos()).getBlock();
                    if (block == Blocks.AIR) {
                        isLower = true;
                        slab.selfAdd(0.0, -1.0, 0.0);
                    }

                    float[] angles = EtherUtils.getYawAndPitch(slab.asVec3(), true, Minecraft.getInstance().player, true);
                    SwapManager.sendAirC08(angles[0], angles[1], true, false);
                    state = 2;
                });
                break;
            }

            case 2: {
                // Wait for aotv S08s
                break;
            }

            case 4: {
                pearl(player.getYRot(), -90f, () -> state = 5);
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
                    PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
                        if (!SwapManager.swapItem(Items.DIAMOND_SHOVEL)) return;
                        float playerYaw = player.getYRot();

                        float[] angles = EtherUtils.getYawAndPitch(MIDDLE_MAP_COORDS.add(0.0d, player.getY(), 0.0d), false, player, false);
                        float deltaX = (float) (player.getX() - MIDDLE_MAP_COORDS.x);
                        float deltaZ = (float) (player.getZ() - MIDDLE_MAP_COORDS.z);

                        aotv0(7, playerYaw, -90f);
                        aotv0(Math.round(Mth.sqrt(deltaX * deltaX + deltaZ * deltaZ) / 12f), angles[0], 0.0f);
                        state = 10;
                    });
                }
                break;
            }

            case 10: {
                // Await S08 to respawn in start room
                break;
            }

            case 11: {
                if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) break;
                forceNextSneak = true; // Must be setup for etherwarp

                if (waitForGround.getValue() && !player.onGround()) break;
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
                if (startRoom.getUniqueRoom().getRotation() == RoomRotation.UNKNOWN) break;

                PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
                    if (!SwapManager.swapItem(Items.DIAMOND_SHOVEL)) return;
                    if (!Minecraft.getInstance().player.getLastSentInput().shift()) return;

                    Pos slab = RoomUtils.getRealPosition(SLAB_BLOCK_OFFSET, startRoom);

                    Block block = Minecraft.getInstance().level.getBlockState(slab.asBlockPos()).getBlock();
                    if (block == Blocks.AIR) {
                        isLower = true;
                        slab.selfAdd(0.0, -1.0, 0.0);
                    }

                    float[] angles = EtherUtils.getYawAndPitch(slab.asVec3(), true, Minecraft.getInstance().player, true);
                    SwapManager.sendAirC08(angles[0], angles[1], true, false);
                    state = 13;
                });

                break;
            }

            case 13: {
                // Wait for S08
                break;
            }

            case 15: {
                pearl(player.getYRot(), -90f, () -> state = 16);
                break;
            }

            case 16: {
                // Wait for pearl S08
                break;
            }

            case 17: {
                SwapManager.swapItem(Items.DIAMOND_SHOVEL);

                if (Dungeon.isStarted() && (serverTickTimer % 40) < 35) {
                    PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
                        if (!SwapManager.swapItem(Items.DIAMOND_SHOVEL)) return;

                        float playerYaw = player.getYRot();

                        Direction dir = getVoidRotation();
                        aotv0(4, dir.toYRot(), 0f);
                        aotv0(10, playerYaw, 90f);

                        Vec3 playerPos = Minecraft.getInstance().player.position().add(fastRotateVec(dir, 0, 0d, -48d)); // We don't care about the Y

                        float deltaX = (float) ((bloodRoom.getX() + 0.5d) - playerPos.x());
                        float deltaZ = (float) ((bloodRoom.getZ() + 0.5d) - playerPos.z());

                        float[] angles = EtherUtils.getYawAndPitch(deltaX, 0.0d, deltaZ);
                        aotv0(Math.round(Mth.sqrt(deltaX * deltaX + deltaZ * deltaZ) / 12f), angles[0], 0f);
                        aotv0(5, playerYaw, -90f);

                        // If we can't pearl
                        state = 29;
                        // Can't use pearl() because concurrentModification exception
                        if (!SwapManager.swapItem(Items.ENDER_PEARL)) return;
                        if (!SwapManager.sendAirC08(player.getYRot(), -90f, true, true)) {
                            ChatUtils.chat("Pearl failed!");
                            return;
                        }
                        state = 30;
                    });
                }
                break;
            }

            case 29: {
                pearl(player.getYRot(), -90f, () -> state = 30);
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

    private static Vec3 fastRotateVec(Direction direction, double x, double y, double z) {
        return switch (direction) {
            case NORTH -> new Vec3(x, y, z);
            case EAST -> new Vec3(-z, y, x);
            case SOUTH -> new Vec3(-x, y, -z);
            case WEST -> new Vec3(z, y, -x);
            default -> Vec3.ZERO;
        };
    }

    private void aotv0(int count, float yaw, float pitch) {
        for (int i = 0; i < count; i++) {
            SwapManager.sendAirC08(yaw, pitch, true, false);
        }
    }

    public boolean isBlinking() {
        return state < 30 && state > -1;
    }

    public void doBlink() {
        resetState();
        state = 0; // Bypass auto
        KeyMapping.releaseAll();
    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (!this.isEnabled() || !this.isBlinking()) return;
        Input input = event.getClientInput();
        if (input.forward() || input.backward() || input.left() || input.right()) {
            this.cancel();
            return;
        }

        Input newInputs = new Input(false, false, false, false, false, this.forceNextSneak, false);
        this.forceNextSneak = false;
        event.getInputConsumer().accept(newInputs);
    }

    private void cancel() {
        reset();
        this.state = 31; // End
        ChatUtils.chat("Cancelling blood blink!");
    }

    private void pearl(float yaw, float pitch, Runnable succeed) {
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            if (!SwapManager.swapItem(Items.ENDER_PEARL)) return;
            if (!SwapManager.sendAirC08(yaw, pitch, true, false)) return;
            if (succeed != null) succeed.run();
        });
    }

    @SubscribeEvent
    public void onLoadRoom(DungeonEvent.RoomLoad event) {
        if (event.getRoom().getData().type() == RoomType.BLOOD) {
            this.bloodRoom = event.getRoom();
            ChatUtils.chat("Found blood at : " + bloodRoom.getX() + ", " + bloodRoom.getZ());
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        Packet<?> packet = event.getPacket();
        if (serverTickTimer > -1 && packet instanceof ClientboundPingPacket) {
            serverTickTimer++;
            serverTotalTickTimer++;
            return;
        }

        if (packet instanceof ClientboundSetTimePacket timePacket) {
            long time = timePacket.gameTime();
            this.serverTickTimer = (int) (time + deathTickOffset.getValue()) % 40;
        }

        if (packet instanceof ClientboundPlayerPositionPacket s08) {
            switch (state) {
                case 2: {
                    state = 4;
                    break;
                }

                case 13: {
                    state = 15;
                    break;
                }

                case 5: {
                    if (s08.change().position().y <= (isLower ? 97.0 : 98.0))
                        state = 4;
                    else
                        state = 6;
                    break;
                }


                case 16: {
                    if (s08.change().position().y <= (isLower ? 97.0 : 98.0))
                        state = 15;
                    else
                        state = 17;
                    break;
                }

                case 30: {
                    Vec3 pos = s08.change().position();
                    if (!isInRoom(Mth.floor(pos.x()), Mth.floor(pos.z()), bloodRoom) || pos.y < 65d || pos.y > 73d) break; // 66 bedrock block blood, this stops aotv S08s from getting considered as pearls
                    //System.out.println("Found pearl S08!");
                    if (pos.y <= 67.0)
                        state = 29;
                    else
                        state = 31;
                    break;
                }

                case 10: {
                    double y = s08.change().position().y;
                    if (y == 76.5 || y == 75.5) // respawn Y, should be reliable *enough
                        state = 11;
                    break;
                }
            }
        }
    }

    private boolean isInRoom(int posX, int posZ, Room room) {
        return Mth.abs(room.getX() - posX) < 16 && Mth.abs(room.getZ() - posZ) < 16;
    }
}
