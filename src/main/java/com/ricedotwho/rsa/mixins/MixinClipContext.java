package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.module.impl.dungeon.SecretHitboxes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClipContext.class)
public class MixinClipContext {
    @Redirect(method = "getBlockShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At(target = "Lnet/minecraft/world/level/ClipContext$Block;get(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", value = "INVOKE"))
    VoxelShape getBlockShape(ClipContext.Block instance, BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape shape = SecretHitboxes.getShape(blockState, blockPos);
        if (shape != null) return shape;
        else return instance.get(blockState, blockGetter, blockPos, collisionContext);
    }
}