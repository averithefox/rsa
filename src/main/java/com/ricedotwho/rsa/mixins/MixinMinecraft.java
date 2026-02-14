package com.ricedotwho.rsa.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.player.CancelInteract;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 600) // Low prio for SwapManager
public abstract class MixinMinecraft {
    @Shadow
    private @Nullable Overlay overlay;
    @Shadow
    @Nullable
    public Screen screen;

    @Shadow
    protected abstract void handleKeybinds();

    @Unique
    private boolean bla = false;
    @Unique
    private boolean blu = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        SwapManager.onPreTickStart(); // Must be called first, unless you have a good reason don't change the order
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V"))
    public void onHandleKeyBinds(Minecraft instance) {
        // Need to cancel it
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showDebugScreen()Z"))
    public void onGetShowDebugScreen(CallbackInfo ci) { // Right before onHandleKeyBinds
        if (this.overlay == null && (screen == null || (!(this.screen instanceof AbstractContainerScreen<?>) && Minecraft.getInstance().player != null))) {
            Profiler.get().popPush("Keybindings");
            // Needed to still call the packet order
            this.handleKeybinds();
        }
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    public void onHandleKeybinds(CallbackInfo ci) {
        bla = true;
        blu = true;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 14), method = "handleKeybinds")
    public void onHandleInputEvent(CallbackInfo ci) {
        if (bla) {
            PacketOrderManager.execute(PacketOrderManager.STATE.ATTACK);
            bla = false;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 15), method = "handleKeybinds")
    public void onHandleInputEvent2(CallbackInfo ci) {
        if (blu) {
            PacketOrderManager.execute(PacketOrderManager.STATE.ITEM_USE);
            blu = false;
            // Need bl because called in whileLoop
        }
    }

    @Redirect(
            method = "startUseItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
            )
    )
    private InteractionResult skipBlockUse(
            MultiPlayerGameMode gameMode,
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (CancelInteract.shouldCancelInteract(hit, player, player.getItemBySlot(hand.asEquipmentSlot()))) {
            return InteractionResult.PASS;
        }

        return gameMode.useItemOn(player, hand, hit);
    }
}
