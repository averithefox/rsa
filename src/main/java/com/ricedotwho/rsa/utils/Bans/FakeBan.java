package com.ricedotwho.rsa.utils.Bans;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.*;

public class FakeBan {
    public static void ban(String reason, String expiry, String link) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Component message = Component.literal("You are temporarily banned for ")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal(expiry).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" from this server!\n").withStyle(ChatFormatting.RED))
                .append(Component.literal("Reason: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(reason + "\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Find out more: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(link + "\n").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("Ban ID: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("RSA ON TOP\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Sharing your Ban ID may affect the processing of your appeal!").withStyle(ChatFormatting.GRAY));
        // FakeBan.ban("reason", "permanent", "https://tinyurl.com/hypixelBans");
        mc.player.connection.onDisconnect(new DisconnectionDetails(message));
    }

    public static void ban(String reason, String expiry, String link, String banID) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Component message = Component.literal("You are temporarily banned for ")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal(expiry).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" from this server!\n").withStyle(ChatFormatting.RED))
                .append(Component.literal("Reason: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(reason + "\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Find out more: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(link + "\n").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("Ban ID: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(banID + "\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Sharing your Ban ID may affect the processing of your appeal!").withStyle(ChatFormatting.GRAY));
        // FakeBan.ban("reason", "permanent", "https://tinyurl.com/hypixelBans", "rsa on top");
        mc.player.connection.onDisconnect(new DisconnectionDetails(message));
    }
}