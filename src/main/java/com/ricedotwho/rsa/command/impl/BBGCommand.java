package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.ricedotwho.rsa.module.impl.dungeon.BloodBlink;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.AutoP3;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.AlignRing;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.MovementPredictor;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.RoomType;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@CommandInfo(name = "bbg", description = "Auto P3 command")
public class BBGCommand extends Command {
    private Vec3 lastMovement = null;

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("center")
                        .executes(this::center)
                )
                .then(literal("add")
                        .executes(this::addRing)
                )
                .then(literal("test")
                        .executes(this::test)
                );
    }

    private int addRing(CommandContext<ClientSuggestionProvider> ctx) {
        RSM.getModule(AutoP3.class).addRing(new AlignRing(Minecraft.getInstance().player.position()));
        return 1;
    }

    private int center(CommandContext<ClientSuggestionProvider> ctx) {
        if (Minecraft.getInstance().player == null) return 0;
        Vec3 initialVelocity = Minecraft.getInstance().player.getDeltaMovement();
        Vec2 initialDisplacement = MovementPredictor.getDisplacementVector(new Vec2((float) initialVelocity.x, (float) initialVelocity.z));

        Vec3 position = Minecraft.getInstance().player.position().add(initialDisplacement.x, 0d, initialDisplacement.y);
        Vec3 target = new Vec3(Mth.floor(position.x) + 0.5d, position.y, Mth.floor(position.z) + 0.5d);
        Vec3 delta = target.subtract(position);
        double deltaLength = delta.length();
        double displacement = MovementPredictor.getDisplacementFromInput(Minecraft.getInstance().player.getSpeed() * 10, true);



        ChatUtils.chat("Guess : " + displacement);
        if (deltaLength > 2 * displacement) {
            AutoP3.chat("Too far!");
            return 0;
        }

        AutoP3.chat("Centering!");
        if (deltaLength < 0.01) return 1;


        double yaw = (float) Math.atan2(-delta.z, delta.x);
        double theta = Math.acos(deltaLength / (2 * displacement));

        AutoP3 autoP3 = RSM.getModule(AutoP3.class);
        autoP3.queueYaw((float) -Math.toDegrees(yaw + theta) - 90f, true);
        autoP3.queueYaw((float) -Math.toDegrees(yaw - theta) - 90f, true);
        return 1;
    }

    private int test(CommandContext<ClientSuggestionProvider> ctx) {
        if (lastMovement != null && Minecraft.getInstance().player != null) {
            ChatUtils.chat("Delta : " + Minecraft.getInstance().player.position().subtract(lastMovement).length());
            ChatUtils.chat("Guess : " + MovementPredictor.getDisplacementMagnitude(new Vec2(1f, 1f)));
        }
        if (Minecraft.getInstance().player != null) {
            lastMovement = Minecraft.getInstance().player.position();
            Minecraft.getInstance().player.setDeltaMovement(1f, Minecraft.getInstance().player.getDeltaMovement().y, 1f);
        }
        return 1;
    }

    private int test1(CommandContext<ClientSuggestionProvider> ctx) {
        AutoP3 autoP3 = RSM.getModule(AutoP3.class);
        if (lastMovement != null && Minecraft.getInstance().player != null) {
            ChatUtils.chat("Delta : " + Minecraft.getInstance().player.position().subtract(lastMovement).length());
            ChatUtils.chat("Guess : " + MovementPredictor.getDisplacementFromInput(Minecraft.getInstance().player.getSpeed() * 10, true));
        }
        if (Minecraft.getInstance().player != null)
            lastMovement = Minecraft.getInstance().player.position();
        autoP3.queueYaw(0f, false);
        return 1;
    }
}