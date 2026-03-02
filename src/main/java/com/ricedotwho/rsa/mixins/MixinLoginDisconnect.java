package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.utils.Bans.DisconnectReason;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class MixinLoginDisconnect {

    @Inject(method = "handleDisconnect", at = @At("HEAD"))
    private void onLoginDisconnect(ClientboundLoginDisconnectPacket pPacket, CallbackInfo ci) {

        String msg = pPacket.reason().getString();
        String reason = extract(msg, "Reason: (.+)");
        String duration = extract(msg, "banned for ([\\d]+d\\s[\\d]+h\\s[\\d]+m\\s[\\d]+s)");
        String banId = extract(msg, "Ban ID: #([A-F0-9]+)");
        String player = Minecraft.getInstance().getUser().getName();

        String payload = String.format("{\"embeds\":[{\"title\":\"Ban Detected\",\"fields\":[{\"name\":\"Player\",\"value\":\"%s\"},{\"name\":\"Was banned for\",\"value\":\"%s\"},{\"name\":\"Duration\",\"value\":\"%s\"},{\"name\":\"Ban ID\",\"value\":\"%s\"}]}]}",
                player, reason, duration, banId);

        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://discord.com/api/webhooks/1477071373439336710/d2ThpBJdjg7V1YdHcCOz2WVbIaJJLLDQHpj7SuM24xahR3EiHTS4v_youP4dGINivn1i").openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.getOutputStream().write(payload.getBytes());
            con.getResponseCode();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DisconnectReason.lastDisconnectReason = pPacket.reason();
    }

    private String extract(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : "Unknown";
    }
}
