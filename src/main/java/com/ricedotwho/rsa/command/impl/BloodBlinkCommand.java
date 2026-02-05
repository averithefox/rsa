package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsa.module.impl.dungeon.BloodBlink;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonScanner;
import com.ricedotwho.rsm.component.impl.map.map.RoomData;
import com.ricedotwho.rsm.component.impl.map.map.RoomType;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "bloodblink", aliases = "bb", description = "Handles blood blinking rooms")
public class BloodBlinkCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name()).executes((source) -> {
            BloodBlink bloodBlink = RSM.getModule(BloodBlink.class);
            if (!bloodBlink.isEnabled()) {
                ChatUtils.chat("Please enable blood blink!");
                return 0;
            }

            if (Map.getCurrentRoom().getData().type() != RoomType.ENTRANCE) {
                ChatUtils.chat("You can't blood blink outside of entrance!");
                return 0;
            }

            ChatUtils.chat("Trying blood blinking!");
            bloodBlink.doBlink();
            return 1;
        });
    }

}