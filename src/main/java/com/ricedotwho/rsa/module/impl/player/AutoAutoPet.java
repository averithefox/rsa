package com.ricedotwho.rsa.module.impl.player;

import com.ricedotwho.rsa.module.impl.player.pet.LocationPetRule;
import com.ricedotwho.rsa.module.impl.player.pet.PetRule;
import com.ricedotwho.rsa.utils.GuiUtils;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(aliases = "Auto Auto Pet", id = "AutoAutoPet", category = Category.PLAYER)
public class AutoAutoPet extends Module {
    private boolean swapping;
    private boolean awaitingOpen;
    private boolean clicked;
    private boolean foundPet;
    private String swapID;
    private AbstractContainerMenu container;

    private BooleanSetting yap = new BooleanSetting("Feedback", true);
    private List<PetRule> petRules;


    public AutoAutoPet() {
        this.petRules = new ArrayList<>();
        registerProperty(
            yap
        );
        clear();
    }

    public void addPetRule(PetRule petRule) {
        this.petRules.add(petRule);
        RSM.getInstance().getEventBus().register(petRule);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        this.swapping = false;
        //addPetRule(new LocationPetRule("sheep", this::swapTo, Island.Hub));
        clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        swapping = false;
        clear();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        swapping = false;
        clear();
    }

    public void swapTo(String swapID) {
        if (Minecraft.getInstance().getConnection() == null || swapping) return;
        this.swapID = swapID.toLowerCase();
        this.swapping = true;
        Minecraft.getInstance().getConnection().sendCommand("pet");
    }

    private void clear() {
        this.foundPet = false;
        awaitingOpen = false;
        clicked = false;
        this.container = null;
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (Minecraft.getInstance().player == null || !Location.isHypixel() || !Location.isInSkyblock()) return;
        if (swapping && event.getPacket() instanceof ClientboundOpenScreenPacket openScreenPacket && !swapID.isEmpty()) { // Only check for swapping here so we don't get errors if swap changes during other sections
            clear();
            if (openScreenPacket.getContainerId() < 1 || openScreenPacket.getContainerId() > 100) return;
            this.clicked = false;
            if (openScreenPacket.getTitle().getString().equals("Pets")) {
                this.container = openScreenPacket.getType().create(openScreenPacket.getContainerId(), Minecraft.getInstance().player.getInventory());
                this.awaitingOpen = true;
                event.setCancelled(true);
            } else {
                this.container = null;
                this.awaitingOpen = false;
                // Don't reset swap, could just have opened a different gui
            }
            return;
        }

        if (event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            if (packet.getContainerId() < 1 || packet.getContainerId() > 100 || mc.player == null || this.container == null || this.container.containerId != packet.getContainerId()) return;
            event.setCancelled(true);
            container.setItem(packet.getSlot(), packet.getStateId(), packet.getItem());
            if (!awaitingOpen) return;

            if (clicked || packet.getSlot() < 10) return; // First pet slot
            if (this.swapping && packet.getSlot() > 43) { // Last pet slot
                this.swapping = false;
                if (!foundPet)
                    ChatUtils.chat("Failed to find pet " + swapID + "!");
                close();
                return;
            }


            ItemStack item = packet.getItem();
            if (!item.getItem().equals(Items.PLAYER_HEAD)) return;

            if (!ChatFormatting.stripFormatting(item.getHoverName().getString()).toLowerCase().contains(swapID) && !ItemUtils.getID(item).equals(swapID)) return;
            foundPet = true;
            if (((ItemLore) item.getOrDefault(DataComponents.LORE, CustomData.EMPTY)).lines().stream().anyMatch(p -> p.getString().equals("Click to despawn!"))) return;
            GuiUtils.sendWindowClick(packet.getSlot(), mc.player, this.container);
            if (yap.getValue()) ChatUtils.chat(Component.literal("Swapping to ").append(item.getDisplayName()));

            awaitingOpen = false;
            swapping = false;
        }

        if (event.getPacket() instanceof ClientboundContainerClosePacket packet) {
            if (this.container != null && packet.getContainerId() == container.containerId) {
                reset();
                event.setCancelled(true);
            }
        }
    }

    private void close() {
        if (container == null || mc.getConnection() == null) return;
        mc.getConnection().send(new ServerboundContainerClosePacket(this.container.containerId));
        clear();
    }
}
