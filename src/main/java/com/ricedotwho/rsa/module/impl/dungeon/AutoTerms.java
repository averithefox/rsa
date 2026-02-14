package com.ricedotwho.rsa.module.impl.dungeon;

import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.SignedBytes;
import com.ricedotwho.rsa.module.impl.dungeon.terminals.*;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@ModuleInfo(aliases = "AutoTerms", id = "AutoTerms", category = Category.DUNGEONS)
public class AutoTerms extends Module {
    private long lastClickTime = 0L;
    private boolean clickedWindow = false;
    private boolean firstClick = true;
    private Terminal terminal;
    private AbstractContainerMenu terminalContainer;
    private final TerminalRenderer terminalRenderer;

    private final NumberSetting firstClickDelay = new NumberSetting("First Click Delay", 200d, 600d, 400d, 5d);
    private final NumberSetting delay = new NumberSetting("Delay", 100d, 250d, 150d, 5d);
    private final NumberSetting breakThreshold = new NumberSetting("Break Threshold", 200d, 800d, 500d, 10d);
    private final BooleanSetting invWalk = new BooleanSetting("Inventory Walk", false);

    private final NumberSetting xPos = new NumberSetting("Render X Position", 0d, 1280d, 640d, 5d);
    private final NumberSetting yPos = new NumberSetting("Render Y Position", 0d, 768d, 384d, 4d);

    public AutoTerms() {
        registerProperty(
                firstClickDelay,
                delay,
                breakThreshold,
                invWalk,
                xPos,
                yPos
        );
        this.terminalRenderer = new TerminalRenderer();
    }

    @SubscribeEvent
    public void onLoadWorld(WorldEvent.Load event) {
        this.terminal = null;
    }

    @SubscribeEvent
    public void onRenderGui(Render2DEvent event) {
        if (isInTerm() && this.invWalk.getValue())
            terminalRenderer.render(event.getGfx(), xPos.getValue().intValue(), yPos.getValue().intValue());
    }

    @SubscribeEvent
    public void render(Render3DEvent.Last event) {
        // issues with race conditions
        if (!isInTerm()) return;

        if (firstClick && (System.currentTimeMillis() - lastClickTime < firstClickDelay.getValue())) return;

        if (System.currentTimeMillis() - lastClickTime < delay.getValue()) return;

        if (System.currentTimeMillis() - lastClickTime > breakThreshold.getValue()) {
            clickedWindow = false;
        }

        // Why is there another check here?
        if (!isInTerm() || clickedWindow || !terminal.shouldSolve()) return;

        terminal.solve();
        if (!terminal.isSolved()) return;

        Solution solution = terminal.getSolution();

        if (solution.getLength() < 1) return;

        sendWindowClick(solution.getNext());
        lastClickTime = System.currentTimeMillis();
        clickedWindow = true;
        firstClick = false;
    }


    // Need to change this for inv walk
    private static void sendWindowClick(int windowID, SolutionClick click, Player player, AbstractContainerMenu abstractContainerMenu) {
        if (windowID != abstractContainerMenu.containerId) {
            ChatUtils.chat("Window ID mismatch!");
            return;
        }

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        NonNullList<Slot> nonNullList = abstractContainerMenu.slots;
        int l = nonNullList.size();
        List<ItemStack> list = Lists.newArrayListWithCapacity(l);

        for (Slot slot : nonNullList) {
            list.add(slot.getItem().copy());
        }

        abstractContainerMenu.clicked(click.index(), click.button(), click.type(), player);
//        ChatUtils.chat("ClickType : " + click.type().name());
//        ChatUtils.chat("Button : " + click.button());
        Int2ObjectMap<HashedStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();

        for (int m = 0; m < l; m++) {
            ItemStack itemStack = list.get(m);
            ItemStack itemStack2 = nonNullList.get(m).getItem();
            if (!ItemStack.matches(itemStack, itemStack2)) {
                int2ObjectMap.put(m, HashedStack.create(itemStack2, connection.decoratedHashOpsGenenerator()));
            }
        }

        HashedStack hashedStack = HashedStack.create(abstractContainerMenu.getCarried(), connection.decoratedHashOpsGenenerator());
        connection.send(new ServerboundContainerClickPacket(windowID, abstractContainerMenu.getStateId(), Shorts.checkedCast(click.index()), SignedBytes.checkedCast(click.button()), click.type(), int2ObjectMap, hashedStack));
    }

    private void sendWindowClick(SolutionClick click) {
        if (Minecraft.getInstance().player == null) return;
        if (!isInTerm() || click.index() < 0 || click.index() >= terminal.getType().getSlotCount()) return;
        // Make some checks
        sendWindowClick(terminal.getWindowID(), click, Minecraft.getInstance().player, this.terminalContainer);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if (isInTerm()) return;
        firstClick = true;
        lastClickTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundOpenScreenPacket packet) {
            if (Minecraft.getInstance().player == null || packet.getContainerId() == 0) return;
            this.terminalContainer = packet.getType().create(packet.getContainerId(), Minecraft.getInstance().player.getInventory());

            this.terminal = Terminal.fromPacket(packet, terminalContainer);
            if (this.terminal == null) {
                this.terminalContainer = null;
                return;
            }

            this.clickedWindow = false;
            this.terminalRenderer.newWindow(terminalContainer);

            if (invWalk.getValue()) event.setCancelled(true);
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            if (packet.getContainerId() == 0) return;
            terminalContainer.setItem(packet.getSlot(), packet.getStateId(), packet.getItem());
            terminal.loadSlot(packet);

            if (invWalk.getValue()) event.setCancelled(true);
            return;
        }


        if (isInTerm() && event.getPacket() instanceof ClientboundContainerClosePacket packet) {
            if (packet.getContainerId() != terminalContainer.containerId) {
                ChatUtils.chat("Container ID mismatch on close!");
            }

            this.close();
            if (invWalk.getValue()) event.setCancelled(true);
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundSetCursorItemPacket packet) {
            if (invWalk.getValue()) event.setCancelled(true);
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundContainerSetContentPacket packet) {
            if (packet.containerId() != 0 && invWalk.getValue()) event.setCancelled(true);
            return;
        }
    }

    private void close() {
        this.terminal = null;
        this.terminalRenderer.close();
        this.terminalContainer = null;
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
//        if (event.getPacket() instanceof  ServerboundContainerClickPacket packet) {
//            ChatUtils.chat("WindowID : " + packet.containerId());
//            ChatUtils.chat("ActionID : " + packet.stateId());
//            ChatUtils.chat("Changed Slots : " + packet.changedSlots());
//            ChatUtils.chat("Carried Item : " + packet.carriedItem());
//        }

        if (isInTerm() && event.getPacket() instanceof ServerboundContainerClosePacket packet) {
            this.close();
            return;
        }
    }

    private boolean isInTerm() {
        return this.terminal != null && terminalContainer != null;
    }
}
