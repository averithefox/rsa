package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;


@ModuleInfo(aliases = "Term Aura", id = "Term Aura", category = Category.DUNGEONS)
public class TermAura extends Module {
    private static final double AURA_RANGE = 4d; // Vanilla is 3.0F
    private static final double AURA_RANGE_SQ = AURA_RANGE * AURA_RANGE;

    private final NumberSetting delay = new NumberSetting("Delay", 50d, 5000d, 500d, 50d);
    private final BooleanSetting showArmorStands = new BooleanSetting("Show Hitboxes", true);
    private final BooleanSetting forceSkyblock = new BooleanSetting("Force Skyblock", false);

    private long lastClick = 0L;

    public TermAura() {
        registerProperty(
                delay,
                showArmorStands,
                forceSkyblock
        );
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, this::rapeArmorstands);
    }

    private void rapeArmorstands() {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null || Minecraft.getInstance().getConnection() == null) return;
        if (System.currentTimeMillis() - lastClick < delay.getValue()) return;
        if (!locationCheck()) return;
        if (AutoTerms.isInTerminal() || Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) return;

        Vec3 eyePos = Minecraft.getInstance().player.position().add(0.0d, Minecraft.getInstance().player.getEyeHeight(), 0.0d);

        double bestDistance = AURA_RANGE_SQ;
        ArmorStand bestCandidate = null;

        AABB box = new AABB(eyePos, eyePos).inflate(AURA_RANGE, AURA_RANGE, AURA_RANGE);
        for (ArmorStand stand : Minecraft.getInstance().level.getEntitiesOfClass(ArmorStand.class, box, TermAura::filterEntities)) {
            double distance = stand.getBoundingBox().distanceToSqr(eyePos);
            if (distance <= bestDistance) {
                bestCandidate = stand;
                bestDistance = distance;
            }
        }

        if (bestCandidate == null) return;


        Vec3 vec3 = clamp(bestCandidate.getBoundingBox(), eyePos).subtract(bestCandidate.getX(), bestCandidate.getY(), bestCandidate.getZ());
        Minecraft.getInstance().getConnection().send(ServerboundInteractPacket.createInteractionPacket(bestCandidate, Minecraft.getInstance().player.isShiftKeyDown(), InteractionHand.MAIN_HAND, vec3));
        lastClick = System.currentTimeMillis();
    }

    public static boolean getEntityVisibility(Entity entity) {
        if (!entity.isInvisible()) return true;
        TermAura termAura = RSM.getModule(TermAura.class);
        return termAura.isEnabled() && termAura.showArmorStands.getValue() && termAura.locationCheck();
    }

    private boolean locationCheck() {
        return forceSkyblock.getValue() || (Location.getArea().is(Island.Dungeon) && Dungeon.isInBoss() && DungeonUtils.isPhase(Phase7.P3));
    }

    private Vec3 clamp(AABB aabb, Vec3 vec3) {
        return new Vec3(clamp(vec3.x, aabb.minX, aabb.maxX), clamp(vec3.y, aabb.minY, aabb.maxY), clamp(vec3.z, aabb.minZ, aabb.maxZ));
    }

    private double clamp(double d, double min, double max) {
        return Math.min(max, Math.max(d, min));
    }


    private static boolean filterEntities(ArmorStand armorStand) {
        if (armorStand.isDeadOrDying()) return false;
        Component name = armorStand.getCustomName();
        if (name == null) return false;
        return name.getString().equals("Inactive Terminal");
    }
}
