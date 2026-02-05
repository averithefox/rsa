package com.ricedotwho.rsa.module.impl.movement;


import com.ricedotwho.rsa.mixins.LocalPlayerAccessor;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Getter
@ModuleInfo(aliases = "No Rotate", id = "No Rotate", category = Category.MOVEMENT)
public class NoRotate extends Module {

    public NoRotate() {
        this.registerProperty(

        );
    }

    public void onHandleMovePlayer(ClientboundPlayerPositionPacket packet, Connection connection, CallbackInfo ci) {
        if (!this.isEnabled()) return;
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
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void reset() {

    }
}
