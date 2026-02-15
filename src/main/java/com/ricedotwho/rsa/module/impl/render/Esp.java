package com.ricedotwho.rsa.module.impl.render;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@Getter
@ModuleInfo(aliases = "Esp", id = "Esp", category = Category.RENDER)
public class Esp extends Module {

    //basically 1:1 Ported from Hyper's esp
    private final BooleanSetting
            drawBoxes = new BooleanSetting("Draw Boxes", true, () -> true),
            drawFilledBox = new BooleanSetting("Draw Filled", true, () -> true),
            showStarredMobs = new BooleanSetting("Starred Mobs", true, () -> true),
            onlyShowInCurrentRoom = new BooleanSetting("Current Room Only", true, () -> true),
            drawBloodMobs = new BooleanSetting("Blood Mobs", true, () -> true),
            withers = new BooleanSetting("Withers", true, () -> true),
            debugVault = new BooleanSetting("Debug: Vault", false, () -> true);

    // Tracked entities
    private final Set<Integer> starredMobs = new HashSet<>();
    private final Set<Integer> bloodMobs = new HashSet<>();
    private final Set<Integer> bloodNames = new HashSet<>();
    private final Set<Integer> vaultStands = new HashSet<>();
    
    private int wither = -1;
    private double witherDistance = Double.MAX_VALUE;
    private int tick = 0;
    
    public float updateInterval = 10;

    public Esp() {
        // Blood mob names
        addName("Revoker");
        addName("Psycho");
        addName("Reaper");
        addName("Cannibal");
        addName("Mute");
        addName("Ooze");
        addName("Putrid");
        addName("Freak");
        addName("Leech");
        addName("Tear");
        addName("Parasite");
        addName("Flamer");
        addName("Skull");
        addName("Mr. Dead");
        addName("Vader");
        addName("Frost");
        addName("Walker");
        addName("Wandering Soul");
        addName("Bonzo");
        addName("Scarf");
        addName("Livid");
        addName("Spirit Bear");
        
        this.registerProperty(
                drawBoxes,
                drawFilledBox,
                showStarredMobs,
                onlyShowInCurrentRoom,
                drawBloodMobs,
                withers,
                debugVault
        );
    }
    
    private void addName(String name) {
        bloodNames.add(name.hashCode());
    }

    @Override
    public void onEnable() {
        starredMobs.clear();
        bloodMobs.clear();
        tick = 0;
    }

    @Override
    public void onDisable() {
        starredMobs.clear();
        bloodMobs.clear();
        wither = -1;
    }

    @Override
    public void reset() {
        starredMobs.clear();
        bloodMobs.clear();
        wither = -1;
        witherDistance = Double.MAX_VALUE;
        tick = 0;
    }

    @SubscribeEvent
    public void onRender3dEvent(Render3DEvent.Extract event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        
        if (!(Location.getArea() == Island.Dungeon)) return;

        // Update tracked entities
        tick++;
        if (tick % updateInterval == 0) {
            updateTrackedEntities(level);
        }

        float partialTicks = event.getContext().tickCounter().getGameTimeDeltaPartialTick(false);

        // Render starred mobs
        if (showStarredMobs.getValue() && !starredMobs.isEmpty()) {
            handleRender(starredMobs, 0xFFD600FF, 0x1A790091, partialTicks);
        }

        // Render blood mobs
        if (drawBloodMobs.getValue() && !bloodMobs.isEmpty()) {
            handleRender(bloodMobs, 0xFFFF0000, 0x1A720000, partialTicks);
        }

        // Render wither
        if (withers.getValue() && wither != -1) {
            Entity entity = level.getEntity(wither);
            if (entity != null) {
                renderEntityBox(entity, 0xFF0066FF, 0x1A003688, partialTicks);
            } else {
                wither = -1;
            }
        }
    }

    private void updateTrackedEntities(ClientLevel level) {
        starredMobs.clear();
        bloodMobs.clear();
        wither = -1;
        witherDistance = Double.MAX_VALUE;

        for (Entity entity : level.entitiesForRendering()) {
            // Starred mobs (armor stands with ✯ name)
            if (showStarredMobs.getValue() && entity instanceof ArmorStand stand) {
                if (!isValidStarredEntity(stand)) continue;
                Entity mob = getMobEntity(stand, level);
                if (mob != null) {
                    starredMobs.add(mob.getId());
                    stand.setCustomNameVisible(true);
                    mob.setInvisible(false);
                }
                continue;
            }

            // Shadow Assassin (player entities with specific name)
            if (showStarredMobs.getValue() && entity instanceof Player && !(entity instanceof LocalPlayer)) {
                String name = entity.getName().getString().trim();
                if (name.hashCode() == -0x277A5F7B) { // Shadow Assassin
                    starredMobs.add(entity.getId());
                    entity.setInvisible(false);
                }
                continue;
            }

            // Fels (enderman with specific name)
            if (showStarredMobs.getValue() && entity instanceof EnderMan) {
                if (entity.getName().getString().hashCode() == -0x3BEF85AA) {
                    entity.setInvisible(false);
                }
                continue;
            }

            // Blood mobs - players
            if (drawBloodMobs.getValue() && entity instanceof Player && !(entity instanceof LocalPlayer)) {
                String name = entity.getName().getString().trim();
                if (bloodNames.contains(name.hashCode())) {
                    bloodMobs.add(entity.getId());
                    entity.setInvisible(false);
                }
                continue;
            }

            // Blood mobs - giants
            if (drawBloodMobs.getValue() && entity instanceof Giant) {
                bloodMobs.add(entity.getId());
                entity.setInvisible(false);
                continue;
            }

            // Withers
            if (withers.getValue() && entity instanceof WitherBoss && !entity.isInvisible()) {
                LocalPlayer Player = Minecraft.getInstance().player;
                float maxHealth = getSBMaxHealth((LivingEntity) entity);
                if (maxHealth > 400f) {
                    if (wither == -1) {
                        wither = entity.getId();
                        continue;
                    }
                    
                    double dist = entity.distanceToSqr(Player);
                    if (dist < witherDistance) {
                        witherDistance = dist;
                        wither = entity.getId();
                    }
                }
            }
        }
    }

    private void handleRender(Set<Integer> entityIds, int outlineColor, int fillColor, float partialTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        List<Integer> toRemove = new ArrayList<>();
        for (int entityId : entityIds) {
            Entity entity = level.getEntity(entityId);
            if (entity == null) {
                toRemove.add(entityId);
                continue;
            }
            renderEntityBox(entity, outlineColor, fillColor, partialTicks);
        }
        
        toRemove.forEach(entityIds::remove);
    }

    private void renderEntityBox(Entity entity, int outlineColor, int fillColor, float partialTicks) {
        Vec3 interpolatedPos = entity.getPosition(partialTicks);
        
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();

        AABB boundingBox = new AABB(
                interpolatedPos.x - width / 2,
                interpolatedPos.y,
                interpolatedPos.z - width / 2,
                interpolatedPos.x + width / 2,
                interpolatedPos.y + height,
                interpolatedPos.z + width / 2
        );

        if (drawFilledBox.getValue()) {
            Renderer3D.addTask(new com.ricedotwho.rsm.utils.render.render3d.type.FilledBox(boundingBox, new Colour(fillColor), false));
        }
        if (drawBoxes.getValue()) {
            Renderer3D.addTask(new com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox(boundingBox, new Colour(outlineColor), false));
        }
    }

    private boolean isValidStarredEntity(ArmorStand entity) {
        if (!entity.hasCustomName()) return false;
        String name = StringUtil.stripColor(Objects.requireNonNull(entity.getCustomName()).getString());
        return name.contains("✯ ") && name.endsWith("❤");
    }

    private Entity getMobEntity(ArmorStand stand, ClientLevel level) {
        AABB searchBox = stand.getBoundingBox().move(0.0, -1.0, 0.0);
        
        return level.getEntities(stand, searchBox)
                .stream()
                .filter(e -> e instanceof LivingEntity 
                        && !(e instanceof ArmorStand) 
                        && !(e instanceof LocalPlayer)
                        && !(e instanceof WitherBoss && e.isInvisible()))
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(stand)))
                .orElse(null);
    }

    private float getSBMaxHealth(LivingEntity entity) {
        if (entity == null) return 0f;
        return entity.getMaxHealth();
    }
}
