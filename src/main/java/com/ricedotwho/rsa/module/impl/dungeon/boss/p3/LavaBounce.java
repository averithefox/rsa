//package com.ricedotwho.rsa.module.impl.dungeon.boss.p3;
//
//import com.google.common.reflect.TypeToken;
//import com.ricedotwho.rsa.RSA;
//import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
//import com.ricedotwho.rsa.component.impl.managers.SwapManager;
//import com.ricedotwho.rsa.module.impl.dungeon.DungeonBreaker;
//import com.ricedotwho.rsa.utils.InteractUtils;
//import com.ricedotwho.rsm.component.impl.Renderer3D;
//import com.ricedotwho.rsm.component.impl.location.Floor;
//import com.ricedotwho.rsm.component.impl.location.Island;
//import com.ricedotwho.rsm.component.impl.location.Location;
//import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
//import com.ricedotwho.rsm.data.Colour;
//import com.ricedotwho.rsm.data.Keybind;
//import com.ricedotwho.rsm.data.Pos;
//import com.ricedotwho.rsm.event.api.SubscribeEvent;
//import com.ricedotwho.rsm.event.impl.client.PacketEvent;
//import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
//import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
//import com.ricedotwho.rsm.event.impl.world.WorldEvent;
//import com.ricedotwho.rsm.module.Module;
//import com.ricedotwho.rsm.module.api.Category;
//import com.ricedotwho.rsm.module.api.ModuleInfo;
//import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
//import com.ricedotwho.rsm.utils.FileUtils;
//import com.ricedotwho.rsm.utils.ItemUtils;
//import com.ricedotwho.rsm.utils.RotationUtils;
//import com.ricedotwho.rsm.utils.Utils;
//import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
//import lombok.Getter;
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.core.BlockPos;
//import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.context.UseOnContext;
//import net.minecraft.world.level.GameType;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.HitResult;
//import net.minecraft.world.phys.Vec3;
//import net.minecraft.world.phys.shapes.VoxelShape;
//import org.lwjgl.glfw.GLFW;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//@Getter
//@ModuleInfo(aliases = "Lava Bounce", id = "LavaBounce", category = Category.DUNGEONS, hasKeybind = true)
//public class LavaBounce extends Module {
//
//    public LavaBounce() {
//        this.registerProperty(
//        );
//    }
//
//    @SubscribeEvent
//    public void onTick(ClientTickEvent.Start event) {
//        if (!Location.getArea().is(Island.Dungeon) || !Dungeon.isInBoss() || !Utils.equalsOneOf(Location.getFloor(), Floor.M7, Floor.F7) || mc.level == null || mc.player == null) return;
//        BlockPos under = findLava();
//        if (under == null) return;
//        BlockState state = mc.level.getBlockState(under);
//        if (state.getShape(mc.level, under).isEmpty()) return;
//        Vec3 eyePos = mc.player.position().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
//        Vec3 top = new Vec3(under.getX() + 0.5, under.getY() + 1, under.getZ() + 0.5);
//        if (eyePos.distanceToSqr(top) > 16) return;
//
//        if (SwapManager.swapItem("SOUL_SAND", "CHEST", "ENDER_CHEST")) {
//            PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
//                //InteractUtils.interactOnBlock(under, eyePos, top, true);
//                //placeBlock(under.above());
//            });
//        }
//    }
//
//    private void placeBlock(BlockPos pos) {
//        ItemStack item = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
//        Block toPlace = switch (ItemUtils.getID(item)) {
//            case "SOUL_SAND" -> Blocks.SOUL_SAND;
//            case "CHEST" -> Blocks.CHEST;
//            case "ENDER_CHEST" -> Blocks.ENDER_CHEST;
//            default -> null;
//        };
//        if (toPlace == null) return;
//        mc.level.setBlock(pos, toPlace.defaultBlockState(), 0);
//    }
//
//    private BlockPos findLava() {
//        BlockPos.MutableBlockPos bp = mc.player.blockPosition().mutable();
//        int y = mc.player.getBlockY();
//        for (int i = y; i > 0; i--) {
//            bp.setY(i);
//            BlockState state = mc.level.getBlockState(bp);
//            if (state.is(Blocks.LAVA)) {
//                return bp.setY(i - 1).immutable();
//            } else if (!state.is(Blocks.AIR)) {
//                return null;
//            }
//        }
//        return null;
//    }
//
////    @SubscribeEvent
////    public void onRender3D(Render3DEvent.Extract event) {
////        if (!Location.getArea().is(Island.Dungeon) || !renderBlocks.getValue() || !Dungeon.isInBoss() || !Utils.equalsOneOf(Location.getFloor(), Floor.M7, Floor.F7) || data.getValue().isEmpty() || mc.level == null || mc.player == null) return;
////        for (Pos pos : data.getValue()) {
////            BlockPos bp = pos.asBlockPos();
////            BlockState state = mc.level.getBlockState(bp);
////            VoxelShape shape = state.getShape(mc.level, bp);
////            if (shape.isEmpty()) continue;
////            AABB aabb = shape.bounds().move(bp);
////            Renderer3D.addTask(new FilledBox(aabb, colour.getValue(), true));
////        }
////    }
////
////    public void addOrRemoveBlock() {
////        if (!Location.getArea().is(Island.Dungeon) || !Dungeon.isInBoss() || mc.player == null) return;
////        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
////            RSA.chat(ChatFormatting.RED + "Not looking at a block");
////            return;
////        }
////        Pos pos = new Pos(blockHitResult.getBlockPos());
////
////        if (data.getValue().contains(pos)) {
////            data.getValue().remove(pos);
////            RSA.chat(ChatFormatting.RED + "Removed " + pos.toChatString());
////        } else {
////            data.getValue().add(pos);
////            RSA.chat(ChatFormatting.GREEN + "Added " + pos.toChatString());
////        }
////        data.save();
////    }
//
//}
