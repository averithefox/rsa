package com.ricedotwho.rsa.module.impl.dungeon;

import com.mojang.authlib.GameProfile;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.MathUtils;
import com.ricedotwho.rsm.utils.RotationUtils;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Secrets", id = "Secrets", category = Category.DUNGEONS)
public class SecretAura extends Module {
    private static final double CHEST_RANGE = 6.2d;
    private static final double SKULL_RANGE = 4.5d;
    private static final double CHEST_RANGE_SQ = CHEST_RANGE * CHEST_RANGE;
    private static final double SKULL_RANGE_SQ = SKULL_RANGE * SKULL_RANGE;


    private static final String REDSTONE_KEY_ID = "fed95410-aba1-39df-9b95-1d4f361eb66e";
    private static final String WITHER_ESSENCE_ID = "e0f3e929-869e-3dca-9504-54c666ee6f23";

    ModeSetting type = new ModeSetting("Type", "Triggerbot", List.of("Triggerbot", "Aura"));
    MultiSetting bigHitboxes = new MultiSetting("Hitboxes", new ArrayList<>(), List.of("Big Buttons", "Big Levers", "Big Skulls"));
    NumberSetting delay = new NumberSetting("Click Delay", 100, 4000, 150, 50);
    NumberSetting swapSlot = new NumberSetting("Swap Slot Index", 0, 7, 0, 1);
    BooleanSetting inBoss = new BooleanSetting("In Boss", true);
    BooleanSetting autoClose = new BooleanSetting("Auto Close GUI", false);
    BooleanSetting forceSkyblock = new BooleanSetting("Force Skyblock", false);

    private boolean hasRedstoneKey = false;
    private final Int2LongOpenHashMap clickedBlocks = new Int2LongOpenHashMap(5);
    private final IntOpenHashSet blocksDone = new IntOpenHashSet();
    private int clickBlockCooldown = 20;
    private int lastSlot = -1;

    public SecretAura() {
        this.registerProperty(
                type,
                bigHitboxes,
                delay,
                swapSlot,
                inBoss,
                autoClose,
                forceSkyblock
        );
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        this.clear();
    }

    @SubscribeEvent
    public void onTickEnd(ClientTickEvent.Start event) {
        clickBlockCooldown--;
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof ServerboundUseItemOnPacket)) return;
        clickBlockCooldown = 1;
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (!this.autoClose.getValue() || !Location.getArea().is(Island.Dungeon)) return;
        if (!(event.getPacket() instanceof ClientboundOpenScreenPacket openScreenPacket) || Minecraft.getInstance().getConnection() == null) return;
        String name = openScreenPacket.getTitle().getString();
        if (!name.equals("Chest") && !name.equals("Large Chest")) return;

        int windowId = openScreenPacket.getContainerId();
        Minecraft.getInstance().getConnection().send(new ServerboundContainerClosePacket(windowId));
        // This is technically supposed to stop movement inputs when it sends while closing a gui
        // But it doesn't because that would be annoying
        // And hypixel doesn't seem to care
        // It seems that the packet is normally sent off tick, so packet order shouldn't matter
        // https://github.com/GrimAnticheat/Grim/blob/2b621483e7ccd140e6631cd049bab0a09edf24af/common/src/main/java/ac/grim/grimac/checks/impl/multiactions/MultiActionsD.java#L11
        event.setCancelled(true);
    }


    @SubscribeEvent
    public void onTickStart(ClientTickEvent.Start event) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null || Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) return;
        if (!forceSkyblock.getValue() && (!Location.getArea().is(Island.Dungeon) || isRoomDisabled())) return;
        if (!forceSkyblock.getValue() && Dungeon.isInBoss() && !inBoss.getValue()) return;
        ClientLevel level = Minecraft.getInstance().level;

        Vec3 eyePos = Minecraft.getInstance().player.position().add(0.0d, EtherUtils.SNEAK_EYE_HEIGHT, 0.0d);
        Vec3 flooredEyePos = eyePos.subtract(0.5d, 0.5d, 0.5d);

        double bestDistance = Double.MAX_VALUE;
        BlockPos bestCandidate = null;

        AABB box = new AABB(eyePos, eyePos).inflate(CHEST_RANGE, CHEST_RANGE, CHEST_RANGE);
        for (BlockPos blockPos : BlockPos.betweenClosed(box)) {
            int hash = getBlockPosHash(blockPos);
            if (blocksDone.contains(hash)) continue;

            Block block = level.getBlockState(blockPos).getBlock();
            if (!isValidBlock(block) && (block != Blocks.PLAYER_HEAD || !isValidSkull(blockPos, level))) continue;
            if (Dungeon.isInBoss() && block == Blocks.PLAYER_HEAD) continue;

            if (!clickedBlocks.containsKey(hash) && delay.getValue() > 0) {
                clickedBlocks.put(hash, System.currentTimeMillis() + delay.getValue().longValue());
                continue;
            }

            long nextClickTime = clickedBlocks.get(hash);
            if (nextClickTime > System.currentTimeMillis()) continue;

            double d = flooredEyePos.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if ((block == Blocks.PLAYER_HEAD && d > SKULL_RANGE_SQ) || d > CHEST_RANGE_SQ) continue;

            if (d < bestDistance) {
                bestDistance = d;
                bestCandidate = new BlockPos(blockPos);
            }
        }

        // Don't return earlier so we can register than we have seen the clicked blocks
        if (bestCandidate == null || clickBlockCooldown > 0) return;

        BlockState blockState = level.getBlockState(bestCandidate);
        if ((blockState.getBlock() == Blocks.PLAYER_HEAD || Minecraft.getInstance().player.getInventory().getSelectedSlot() == 8) && !SwapManager.swapSlot(swapSlot.getValue().intValue())) return;

        clickedBlocks.put(getBlockPosHash(bestCandidate), System.currentTimeMillis() + 500L); // 500ms re-click delay

        AABB blockAABB = blockState.getShape(level, bestCandidate).bounds();

        Vec3 center = new Vec3((blockAABB.minX + blockAABB.maxX) * 0.5 + bestCandidate.getX(), (blockAABB.minY + blockAABB.maxY) * 0.5 + bestCandidate.getY(), (blockAABB.minZ + blockAABB.maxZ) * 0.5 + bestCandidate.getZ());
        BlockHitResult result = RotationUtils.collisionRayTrace(bestCandidate, blockAABB, eyePos, center);
        if (result == null) return;

        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            SwapManager.sendBlockC08(result.getLocation(), result.getDirection(), false, true);
            //ChatUtils.chat("Sent aura C08!");
        });
    }

    private boolean isValidBlock(Block block) {
        if (block == Blocks.AIR) return false;
        return block == Blocks.LEVER || block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || (block == Blocks.REDSTONE_BLOCK && hasRedstoneKey);
    }

    private boolean isValidSkull(BlockPos blockPos, ClientLevel level) {
        BlockEntity entity = level.getBlockEntity(blockPos);
        if (!(entity instanceof SkullBlockEntity skullBlockEntity)) return false;
        ResolvableProfile gameProfile = skullBlockEntity.getOwnerProfile();
        if (gameProfile == null) return false;
        String uuid = gameProfile.partialProfile().id().toString();
        return switch (uuid) {
            case WITHER_ESSENCE_ID, REDSTONE_KEY_ID -> true;
            default -> false;
        };
    }



    private boolean isRoomDisabled() {
        if (!Location.getArea().is(Island.Dungeon) || Dungeon.isInBoss() || Map.getCurrentRoom() == null) return false;
        return switch (Map.getCurrentRoom().getData().name()) {
            case "Water Board", "Three Weirdos" -> true;
            default -> false;
        };
    }



    // Y range : 0 to 255
    // X range: -2048 to 2047
    // Z range: -2048 to 2047
    private static int getBlockPosHash(BlockPos blockPos) {
        return (blockPos.getY() & 0xFF) | (((blockPos.getX() + 2048) & 0xFFF) << 8) | (((blockPos.getZ() + 2048) & 0xFFF) << 20);
    }

    private void clear() {
        blocksDone.clear();
        this.clickBlockCooldown = 20;
        hasRedstoneKey = false;
    }

    @Override
    public void onEnable() {
        this.clear();
    }

    @Override
    public void onDisable() {

    }

}
