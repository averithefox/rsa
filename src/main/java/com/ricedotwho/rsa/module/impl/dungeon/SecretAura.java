package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.AABB;
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
    BooleanSetting inBoss = new BooleanSetting("In Boss", true);
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
                inBoss,
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
    public void onTickStart(ClientTickEvent.Start event) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null || Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) return;
        if (!forceSkyblock.getValue() && (!Location.getArea().is(Island.Dungeon) || isRoomDisabled())) return;
        if (!forceSkyblock.getValue() && Dungeon.isInBoss() && !inBoss.getValue()) return;
        ClientLevel level = Minecraft.getInstance().level;

        Vec3 eyePos = Minecraft.getInstance().player.getEyePosition();
        Vec3 flooredEyePos = eyePos.subtract(0.5d, 0.5d, 0.5d);

        double bestDistance = Double.MAX_VALUE;
        BlockPos bestCandidate = null;

        AABB box = new AABB(eyePos, eyePos).expandTowards(CHEST_RANGE, CHEST_RANGE, CHEST_RANGE);
        for (BlockPos blockPos : BlockPos.betweenClosed(box)) {
            int hash = getBlockPosHash(blockPos);
            if (blocksDone.contains(hash)) continue;

            Block block = level.getBlockState(blockPos).getBlock();
            if (!isValidBlock(block) && (block != Blocks.PLAYER_HEAD || !isValidSkull(blockPos, level))) continue;


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
                bestCandidate  = blockPos;
            }
        }

        ChatUtils.chat("Test : " + bestCandidate);
        // Don't return earlier so we can register than we have seen the clicked blocks
        if (bestCandidate == null || clickBlockCooldown > 0) return;
        clickedBlocks.put(getBlockPosHash(bestCandidate), System.currentTimeMillis() + 500L); // 500ms re-click delay

        AABB blockAABB = level.getBlockState(bestCandidate).getShape(level, bestCandidate).bounds();
        if (blockAABB == null) {
            ChatUtils.chat("Null block!");
        }
    }

    private boolean isValidBlock(Block block) {
        if (block == Blocks.AIR) return false;
        return block == Blocks.LEVER || block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || (block == Blocks.REDSTONE_BLOCK && hasRedstoneKey);
    }

    private boolean isValidSkull(BlockPos blockPos, ClientLevel level) {
        BlockEntity entity = level.getBlockEntity(blockPos);
        if (!(entity instanceof SkullBlockEntity skullBlockEntity)) return false;
        System.out.println("UUID : " + skullBlockEntity.getOwnerProfile().partialProfile().id().toString());
        return true;
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
