package com.ricedotwho.rsa.module.impl.other.Checks;

import com.ricedotwho.rsa.module.impl.other.AntiCheat;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class invWalk {
    public static boolean startChecking;
    private static Pattern playerName = Pattern.compile("^(\\w+)\\s+activated a terminal");
    public static String username;

    private static Map<BlockPos, Entity> inactiveTerminals = new HashMap<>();
    private static List<String> termCompleter = new ArrayList<>();
    private static List<Double> TermPos = new ArrayList<>();
    private static List<Double> PlayerPos = new ArrayList<>();

    @SubscribeEvent
    public static void setRunning(){
        if(AntiCheat.termWalking.getValue()){
            startChecking = true;
        }
    }

    @SubscribeEvent
    public static void Check1(){
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if(player == null || level == null) return;

        AABB searchBox = player.getBoundingBox().inflate(192);
        List<Entity> entities = level.getEntities(null, searchBox);

        Set<BlockPos> currentInactive = new HashSet<>();

        for(Entity entity : entities) {
            String name = entity.getName().getString();
            BlockPos pos = entity.blockPosition();

            if(entity instanceof ArmorStand) {
                if (name.contains("Inactive Terminal")) {
                    currentInactive.add(pos);
                    inactiveTerminals.putIfAbsent(pos, entity);
                }
                else if (name.contains("Terminal Active")) {
                    if (inactiveTerminals.containsKey(pos)) {
                        double TermX = entity.getX();
                        double TermY = entity.getY();
                        double TermZ = entity.getZ();
                        TermPos.add(TermX);
                        TermPos.add(TermY);
                        TermPos.add(TermZ);
                        inactiveTerminals.remove(pos);
                    }else{TermPos.clear();}
                }
            }

            if(entity instanceof Player) {
                if (!termCompleter.isEmpty() && name.contains(termCompleter.getFirst())) {
                    double playerx = entity.getX();
                    double playery = entity.getY();
                    double playerz = entity.getZ();
                    PlayerPos.add(playerx);
                    PlayerPos.add(playery);
                    PlayerPos.add(playerz);
                    termCompleter.removeFirst();
                }
            }
        }

        if(!PlayerPos.isEmpty() && !TermPos.isEmpty()) {
            double xOffset = PlayerPos.getFirst() - TermPos.getFirst();
            double yOffset = PlayerPos.get(1) - TermPos.get(1);
            double zOffset = PlayerPos.get(2) - TermPos.get(2);
            PlayerPos.clear();
            TermPos.clear();

            if(xOffset > 7 || xOffset < -7){
                ChatUtils.chat("§b" + username + " §7Failed InvWalk Check §4§lxOffSet§r§7: §8" + xOffset);
                xOffset = 0;
                username = null;
            }else if(yOffset > 13 || yOffset < -13) {
                ChatUtils.chat("§b" + username + " §7Failed InvWalk Check §4§lyOffSet§r§7: §8" + yOffset);
                yOffset = 0;
                username = null;
            }else if(zOffset > 7 || zOffset < -7) {
                ChatUtils.chat("§b" + username + " §7Failed InvWalk Check §4§lzOffSet§r§7: §8" + zOffset);
                zOffset = 0;
                username = null;
            }
            PlayerPos.clear();
            TermPos.clear();
        }

        termCompleter.clear();
        inactiveTerminals.keySet().retainAll(currentInactive);
    }

    @SubscribeEvent
    public static void terminalCompletedMsg(ChatEvent event){
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        String unformatted = StringUtil.stripColor(event.getMessage().getString());
        Matcher matcher = playerName.matcher(unformatted);

        if(matcher.find()){
            termCompleter.add(matcher.group(1));
            username = matcher.group(1);
        }
    }

    @AllArgsConstructor
    private enum EntityType {
        PartyPlayer(Player.class, e -> Dungeon.getPlayer((Player) e) != null),
        INACTIVETERMINAL(ArmorStand.class, e -> e.getName().getString().contains("Inactive Terminal")),
        ACTIVETERMINAL(ArmorStand.class, e -> e.getName().getString().contains("Terminal Active"));

        private Class<? extends Entity> entityClass;
        private Predicate<Entity> isValid;

        public static boolean isValidEntity(Entity entity) {
            EntityType type = Arrays.stream(values())
                    .filter(t -> t.entityClass.isInstance(entity))
                    .findFirst()
                    .orElse(null);

            return type != null &&
                    type.isValid.test(entity);
        }
    }
}