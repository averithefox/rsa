package com.ricedotwho.rsa.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "stopwatch", aliases = "sw", description = "Handles Stopwatches")
public class StopwatchCommand extends Command {

    private long startTime = -1;

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("start")
                        .executes(ctx -> {
                            startTime = System.currentTimeMillis();
                            RSA.chat("Stopwatch started.");
                            return 1;
                        })
                )
                .then(literal("stop")
                        .executes(ctx -> {
                            if (startTime == -1) {
                                RSA.chat("start a stopwatch first!");
                                return 0;
                            }
                            long elapsed = System.currentTimeMillis() - startTime;
                            startTime = -1;
                            RSA.chat("End Time: %s", formatTime(elapsed) + "s");
                            return 1;
                        })
                )
                .then(literal("reset")
                        .executes(ctx -> {
                            startTime = -1;
                            RSA.chat("Stopwatch reset.");
                            return 1;
                        })
                );
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long miliseconds  = ms % 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%dm %ds %dms", minutes, seconds, miliseconds);
        }
        return String.format("%d.%03d", ms / 1000, ms % 1000);
    }
}