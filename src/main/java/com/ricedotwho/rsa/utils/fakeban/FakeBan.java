package com.ricedotwho.rsa.utils.fakeban;

import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.utils.DiscordWebhook;
import com.ricedotwho.rsa.utils.Util;
import com.ricedotwho.rsm.utils.Utils;
import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@UtilityClass
public class FakeBan {
    public void ban(String reason, String expiry, String link) {
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

    public void ban(String reason, String expiry, String link, String banID) {
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

    public BanInfo extractBanInfo(Component component) {
        RSA.getLogger().info("Size: {}, Component: {}", component.getSiblings().size(), component);
        if (component.getSiblings().size() != 10) return null;
        Component ban = component.getSiblings().get(0);
        if (!Objects.equals(TextColor.fromLegacyFormat(ChatFormatting.RED), ban.getStyle().getColor()) || !ban.getString().contains("banned")) return null;
        String duraString = component.getSiblings().get(1).getString().trim();
        long dura = Util.getMillisFromDHMS(duraString);
        String reason = component.getSiblings().get(4).getString().trim();
        BanType type = BanType.getFromString(reason);
        String id = component.getSiblings().get(8).getString().trim();
        return new BanInfo(type, dura, id);
    }

    public record BanInfo(BanType type, long duration, String id) {
        public DiscordWebhook.EmbedObject createEmbed(@NotNull User user) {
            DiscordWebhook.EmbedObject obj = new DiscordWebhook.EmbedObject();
            obj.setTitle("Ban Detected");
            obj.addField("Player:", user.getName(), false);
            obj.addField("Reason:", Utils.capitalise(type.name().replace("_", " ").toLowerCase()), false);
            if (duration == 0) {
                obj.addField("Duration: ", "Permanent", false);
            } else {
                long unixSeconds = (System.currentTimeMillis() + duration) / 1000;
                obj.addField("Duration:", Util.millisToDHMS(duration), false);
                obj.addField("Expires at:", String.format("<t:%s:F>", unixSeconds), false);
            }
            obj.addField("Ban ID", id, false);
            return obj;
        }
    }

    public enum BanType {
        CHEATING("Cheating through the use of unfair game advantages."),
        BOOSTING("Boosting detected on one or multiple SkyBlock profiles."),
        CHAT_INFRACTION("idk man"),
        UNKNOWN("wtf yo");

        public final String reason;

        BanType(String reason) {
            this.reason = reason;
        }

        public static BanType getFromString(String reason) {
            for (BanType type : values()) {
                if (type.reason.equals(reason)) return type;
            }
            RSA.getLogger().info("Failed to find ban type for \"{}\"", reason);
            return BanType.UNKNOWN;
        }
    }
}