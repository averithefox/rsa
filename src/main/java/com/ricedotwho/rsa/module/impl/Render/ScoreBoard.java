package com.ricedotwho.rsa.module.impl.Render;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ScoreboardEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter // please don't use spaces in the id
@ModuleInfo(aliases = "SB", id = "ScoreBoard", category = Category.RENDER)
public class ScoreBoard extends Module {
    private final BooleanSetting removeServerID = new BooleanSetting("ServerID", false);

    public ScoreBoard() {
        this.registerProperty(
                removeServerID
        );
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

    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.Receive event) {
//        if (event.getPacket() instanceof ClientboundSetObjectivePacket packet) {
//            ChatUtils.chat(packet.getDisplayName()+ "= Display Name"); this = SKYBLOCK in your scoreboard
//            event.setCancelled(true);
//
//            ClientboundSetObjectivePacket newPacket = new ClientboundSetObjectivePacket(new Objective())
//        }
//        if (event.getPacket() instanceof ClientboundSetScorePacket packet) {
//            ChatUtils.chat(packet.display() + "display. (setscorepacket)"); this is set to: Optional.empty when logged
//            ChatUtils.chat(packet.score() + " Score. (setscorepacket)"); this is set to your health. the #/ half of it it seems.
//        }
//        if (event.getPacket() instanceof ClientboundSetDisplayObjectivePacket packet) {
//            ChatUtils.chat(packet.getObjectiveName() + " = GetObj Name."); // sets it to: SBScoreboard, and health
//            ChatUtils.chat(packet.getSlot() + " = GetSlot."); // Sets it to: SIDEBAR, and BELOW_NAME
//            ChatUtils.chat(packet.type() + " = Type."); //Sets it to clientbound/minecraft:set_display_objective x 2
//            ChatUtils.chat(packet.getClass() + " = GetClass."); //Sets it to: class net.minecraft.class_2736 x2
//            ChatUtils.chat(packet + " Packet info"); // just says: net.minecraft.class_2736@38238966, and net.minecraft.class_2736@4aa11b44
//        }
        if(event.getPacket() instanceof ScoreboardEvent event1) {
            ChatUtils.chat(event1.getUnformatted());
        }
    }

    private final Pattern SERVER_PATTERN = Pattern.compile("\\d{2}/\\d{2}/\\d{2}\\s+(m\\d{1,4}[A-Z]{1,3})");

    @SubscribeEvent
    public void onScoreboardEvent(ScoreboardEvent event) {
        if(removeServerID.getValue()) {
            Matcher matcher = SERVER_PATTERN.matcher(event.getUnformatted());

            if(matcher.find()){
                ChatUtils.chat("ServerID FOUND! Deleting the serverid! " + event.getUnformatted());
                String serverID = matcher.group(1);
            }
        }
    }
}

// TODO: make it delete it from scoreboard