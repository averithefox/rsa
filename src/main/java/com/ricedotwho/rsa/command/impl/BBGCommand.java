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
                        .then(argument("centerType", BBGCommand.CenterArgumentType.centerArgument())
                                .executes(this::center)
                        )
                        .executes(r -> this.center(CenterType.ALL)) // Won't center pos on server dw
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
        if (Minecraft.getInstance().player == null) return 0;
        RingType type = BBGCommand.RingArgumentType.getRing(ctx, "ring");

        Ring ring = type.supply(Minecraft.getInstance().player.position());
        if (ring == null) return 0;
        RSM.getModule(AutoP3.class).addRing(ring);
        return 1;
    }

    private int removeRing(CommandContext<ClientSuggestionProvider> ctx) {
        if (Minecraft.getInstance().player == null) return 0;

        Vec3 position = Minecraft.getInstance().player.position();
        RSM.getModule(AutoP3.class).removeNearest(position);
        return 1;
    }

    private int center(CenterType centerType) {
        if (centerType == null) return 0;
        switch (centerType) {
            case ALL -> {
                centerYaw();
                centerPitch();
                centerPos();
                break;
            }

            case POS -> {
                centerPos();
                break;
            }

            case ANGLES -> {
                centerYaw();
                centerPitch();
                break;
            }

            case YAW -> {
                centerYaw();
            }

            case PITCH -> {
                centerPitch();
            }
        }
        return 1;
    }

    private int center(CommandContext<ClientSuggestionProvider> ctx) {
        CenterType centerType = CenterArgumentType.getType(ctx, "centerType");
        return center(centerType);
    }

    private void centerYaw() {
        if (Minecraft.getInstance().player == null) return;
        Minecraft.getInstance().player.setYRot((Math.round((mc.player.getYRot()) / 45f)) * 45f);
    }

    private void centerPitch() {
        if (Minecraft.getInstance().player == null) return;
        Minecraft.getInstance().player.setXRot(0f);
    }

    private void centerPos() {
        if (Minecraft.getInstance().player == null || !Minecraft.getInstance().isSingleplayer()) return;

        Vec3 position = Minecraft.getInstance().player.position();
        Vec3 target = new Vec3(Mth.floor(position.x) + 0.5d, position.y, Mth.floor(position.z) + 0.5d);
        Minecraft.getInstance().player.setPos(target);
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
        private static final DynamicCommandExceptionType INVALID_RING_EXCEPTION = new DynamicCommandExceptionType(
                ring -> Component.literal("Invalid ring type : " + ring)
        );

        public RingType parse(StringReader stringReader) throws CommandSyntaxException {
            String string = stringReader.readUnquotedString();
            RingType ring = RingType.byName(string);
            if (ring == null) {
                throw INVALID_RING_EXCEPTION.createWithContext(stringReader, string);
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

    private static class CenterArgumentType implements ArgumentType<CenterType> {
        private static final Collection<String> EXAMPLES = Stream.of(CenterType.POS, CenterType.ANGLES)
                .map(CenterType::getName)
                .collect(Collectors.toList());
        private static final CenterType[] VALUES = CenterType.values();
        private static final DynamicCommandExceptionType INVALID_CENTER_EXCEPTION = new DynamicCommandExceptionType(
                ring -> Component.literal("Invalid center type : " + ring)
        );

        public CenterType parse(StringReader stringReader) throws CommandSyntaxException {
            String string = stringReader.readUnquotedString();
            CenterType ring = CenterType.fromName(string);
            if (ring == null) {
                throw INVALID_CENTER_EXCEPTION.createWithContext(stringReader, string);
            } else {
                return ring;
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return context.getSource() instanceof SharedSuggestionProvider
                    ? SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(CenterType::getName), builder)
                    : Suggestions.empty();
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static BBGCommand.CenterArgumentType centerArgument() {
            return new BBGCommand.CenterArgumentType();
        }

        public static CenterType getType(CommandContext<ClientSuggestionProvider> context, String name) {
            return context.getArgument(name, CenterType.class);
        }
    }
}