package com.ricedotwho.rsa.module.impl.movement;


import com.ricedotwho.rsa.mixins.LocalPlayerAccessor;
import com.ricedotwho.rsa.module.impl.dungeon.DungeonBreaker;
import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Getter
@ModuleInfo(aliases = "No Rotate", id = "NoRotate", category = Category.MOVEMENT)
public class NoRotate extends Module {
    private boolean shouldNoRotate = false;

    private final BooleanSetting teleportItem = new BooleanSetting("Teleport Items", true);
    private final BooleanSetting outbounds = new BooleanSetting("Outbounds", false);
    private final BooleanSetting alwaysNoRotate = new BooleanSetting("Always No Rotate", false);

    private static final List<Block> ignored = Arrays.asList(
            Blocks.HOPPER,
            Blocks.ANVIL,
            Blocks.DAMAGED_ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.CHEST, // copper chest ?
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.LEVER
    );

    public NoRotate() {
        this.registerProperty(
                teleportItem,
                outbounds,
                alwaysNoRotate
        );
    }

    // maybe this should have a timeout
    @SubscribeEvent
    public void onUseItem(PacketEvent.Send event) {
        if (!this.teleportItem.getValue() || (Dungeon.isInBoss() && (Location.getFloor() == Floor.F7 || Location.getFloor() == Floor.M7))) return;
        if (event.getPacket() instanceof ServerboundUseItemPacket packet) {
            ItemStack stack = mc.player.getItemBySlot(packet.getHand().asEquipmentSlot());
            if (isHoldingTpItem(stack)) {
                shouldNoRotate = true;
            }
        } else if (event.getPacket() instanceof ServerboundUseItemOnPacket packet) {
            ItemStack stack = mc.player.getItemBySlot(packet.getHand().asEquipmentSlot());
            Block block =  mc.level.getBlockState(packet.getHitResult().getBlockPos()).getBlock();
            if (!ignored.contains(block) && isHoldingTpItem(stack)) {
                shouldNoRotate = true;
            }
        }
    }

    @SubscribeEvent
    public void onEnterBoss(DungeonEvent.EnterBoss event) {
        reset();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    private boolean shouldNoRotate() {
        return this.alwaysNoRotate.getValue()
                || (this.teleportItem.getValue() && shouldNoRotate
                || this.outbounds.getValue() && !Dungeon.isStarted() && Location.getArea().is(Island.Dungeon)
        );
    }

    // Is there a reason to not use PacketEvent?
    public void onHandleMovePlayer(ClientboundPlayerPositionPacket packet, Connection connection, CallbackInfo ci) {
        if (!this.isEnabled() || !shouldNoRotate()) return;
        shouldNoRotate = false;
        // Thanks noob for the code

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        PositionMoveRotation startPos = PositionMoveRotation.of(player);
        PositionMoveRotation newPos = PositionMoveRotation.calculateAbsolute(startPos, packet.change(), packet.relatives());

        player.setPos(newPos.position());
        player.setDeltaMovement(newPos.deltaMovement());

        PositionMoveRotation oldPlayerPos = new PositionMoveRotation(player.oldPosition(), Vec3.ZERO, player.yRotO, player.xRotO);
        PositionMoveRotation newOldPlayerPos = PositionMoveRotation.calculateAbsolute(oldPlayerPos, packet.change(), packet.relatives());

        player.setOldPosAndRot(newOldPlayerPos.position(), player.yRotO, player.xRotO); // i would prefer to just set position here, but fun is private

        connection.send(new ServerboundAcceptTeleportationPacket(packet.id()));
        connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), newPos.yRot(), newPos.xRot(), false, false));

        ((LocalPlayerAccessor) player).setYRotLast(newPos.yRot());
        ((LocalPlayerAccessor) player).setXRotLast(newPos.xRot());

        ci.cancel();
    }

    @Override
    public void reset() {
        shouldNoRotate = false;
    }

    private boolean isHoldingTpItem(ItemStack item) {
        String sbId = ItemUtils.getID(item);
        if (Utils.equalsOneOf(sbId, "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID", "ETHERWARP_CONDUIT", "ASPECT_OF_THE_LEECH_1", "ASPECT_OF_THE_LEECH_2", "ASPECT_OF_THE_LEECH_3")) return true;
        return Utils.equalsOneOf(sbId, "NECRON_BLADE", "SCYLLA", "HYPERION", "VALKYRIE", "ASTRAEA") && ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").size() == 3;
    }
}
