package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.*;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandInfo(name = "bbg", description = "Auto P3 command")
public class BBGCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("center")
                        .executes(this::center)
                )
                .then(literal("remove")
                        .executes(this::removeRing)
                )
                .then(literal("add")
                        .then(argument("ring", BBGCommand.RingArgumentType.ringArgument())
                            .executes(this::addRing)
                        )
                );
    }

    private int addRing(CommandContext<ClientSuggestionProvider> ctx) {
        RingType type = BBGCommand.RingArgumentType.getRing(ctx, "ring");

        RSM.getModule(AutoP3.class).addRing(type.supply(Minecraft.getInstance().player.position()));
        return 1;
    }

    private int removeRing(CommandContext<ClientSuggestionProvider> ctx) {
        if (Minecraft.getInstance().player == null) return 0;

        Vec3 position = Minecraft.getInstance().player.position();
        RSM.getModule(AutoP3.class).removeNearest(position);
        return 1;
    }

    private int center(CommandContext<ClientSuggestionProvider> ctx) {
        if (Minecraft.getInstance().player == null || !Minecraft.getInstance().isSingleplayer()) return 0;

        Vec3 position = Minecraft.getInstance().player.position();
        Vec3 target = new Vec3(Mth.floor(position.x) + 0.5d, position.y, Mth.floor(position.z) + 0.5d);
        Minecraft.getInstance().player.setPos(target);
        return 1;
    }

//    private int test(CommandContext<ClientSuggestionProvider> ctx) {
//        if (lastMovement != null && Minecraft.getInstance().player != null) {
//            ChatUtils.chat("Delta : " + Minecraft.getInstance().player.position().subtract(lastMovement).length());
//            ChatUtils.chat("Guess : " + MovementPredictor.getDisplacementMagnitude(new Vec2(1f, 1f)));
//        }
//        if (Minecraft.getInstance().player != null) {
//            lastMovement = Minecraft.getInstance().player.position();
//            Minecraft.getInstance().player.setDeltaMovement(1f, Minecraft.getInstance().player.getDeltaMovement().y, 1f);
//        }
//        return 1;
//    }
//
//    private int test1(CommandContext<ClientSuggestionProvider> ctx) {
//        AutoP3 autoP3 = RSM.getModule(AutoP3.class);
//        if (lastMovement != null && Minecraft.getInstance().player != null) {
//            ChatUtils.chat("Delta : " + Minecraft.getInstance().player.position().subtract(lastMovement).length());
//            ChatUtils.chat("Guess : " + MovementPredictor.getDisplacementFromInput(Minecraft.getInstance().player.getSpeed() * 10, true));
//        }
//        if (Minecraft.getInstance().player != null)
//            lastMovement = Minecraft.getInstance().player.position();
//        autoP3.queueYaw(0f, false);
//        return 1;
//    }

    private static class RingArgumentType implements ArgumentType<RingType> {
        private static final Collection<String> EXAMPLES = Stream.of(RingType.ALIGN, RingType.WALK)
                .map(RingType::getName)
                .collect(Collectors.toList());
        private static final RingType[] VALUES = RingType.values();
        private static final DynamicCommandExceptionType INVALID_NODE_EXCEPTION = new DynamicCommandExceptionType(
                ring -> Component.literal("Invalid ring type : " + ring)
        );

        public RingType parse(StringReader stringReader) throws CommandSyntaxException {
            String string = stringReader.readUnquotedString();
            RingType ring = RingType.byName(string);
            if (ring == null) {
                throw INVALID_NODE_EXCEPTION.createWithContext(stringReader, string);
            } else {
                return ring;
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return context.getSource() instanceof SharedSuggestionProvider
                    ? SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(RingType::getName), builder)
                    : Suggestions.empty();
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static BBGCommand.RingArgumentType ringArgument() {
            return new BBGCommand.RingArgumentType();
        }

        public static RingType getRing(CommandContext<ClientSuggestionProvider> context, String name) {
            return context.getArgument(name, RingType.class);
        }
    }
}