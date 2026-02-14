package com.ricedotwho.rsa.module.impl.dungeon;

import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.SignedBytes;
import com.ricedotwho.rsa.module.impl.dungeon.terminals.*;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2d;

import java.util.Arrays;
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
    //private final BooleanSetting invWalk = new BooleanSetting("Inventory Walk", false);

    private final GroupSetting invWalkGroup = new GroupSetting("Invwalk");
    private final BooleanSetting doInvwalk = new BooleanSetting("Enabled", false);
    private final ModeSetting style = new ModeSetting("Style", "Solver", Arrays.asList("Solver", "Items"));

    private final BooleanSetting renderTitles = new BooleanSetting("Render title thing", true);
    private final BooleanSetting renderClicksLeft = new BooleanSetting("Render clicks left", true);
    private final ColourSetting titleColour = new ColourSetting("Title Colour", new Colour(96,31,158));
    private final ColourSetting remainingColour = new ColourSetting("Remaining Colour", new Colour(96,31,158));
    private final ColourSetting clicksColour = new ColourSetting("Clicks Colour", new Colour(0, 191, 0));
    private final BooleanSetting textShadow = new BooleanSetting("Text Shadow", false);

    private final BooleanSetting doMoveDelay = new BooleanSetting("Do move delay", true);
    private final NumberSetting melodyMoveDelay = new NumberSetting("Move delay", 0, 10, 6, 1);

    private final DragSetting termTitle = new DragSetting("Term Title", new Vector2d(10, 10), new Vector2d(15, 150));
    private final DragSetting clicksText = new DragSetting("Clicks Text", new Vector2d(10, 10), new Vector2d(15, 150));
    private final DragSetting gui = new DragSetting("Visualiser Gui", new Vector2d(10d, 10d), new Vector2d(100, 66)); // idk how to use ts

    // Make this the drag setting or whatever
    private final NumberSetting xPos = new NumberSetting("Render X Position", 0d, 1280d, 640d, 5d);
    private final NumberSetting yPos = new NumberSetting("Render Y Position", 0d, 768d, 384d, 4d);

    public AutoTerms() {
        registerProperty(
                firstClickDelay,
                delay,
                breakThreshold,
                invWalkGroup
        );

        invWalkGroup.add(
                doInvwalk,
                style,
                renderTitles,
                renderClicksLeft,
                titleColour,
                remainingColour,
                clicksColour,
                textShadow,
                doMoveDelay,
                melodyMoveDelay,
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
        if (isInTerm() && this.doInvwalk.getValue())
            terminalRenderer.render(event.getGfx(), xPos.getValue().floatValue(), yPos.getValue().floatValue(), this.terminal);
    }

    @SubscribeEvent
    public void render(Render3DEvent.Last event) {
        // issues with race conditions
        if (!isInTerm()) return;

        if (terminal.shouldSolve() && !terminal.isSolved()) {
            terminal.solve();
        }

        if (firstClick && (System.currentTimeMillis() - lastClickTime < firstClickDelay.getValue())) return;

        if (System.currentTimeMillis() - lastClickTime < delay.getValue()) return;

        if (System.currentTimeMillis() - lastClickTime > breakThreshold.getValue()) {
            clickedWindow = false;
        }

        // Why is there another check here?
        if (!isInTerm() || clickedWindow) return;

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

            if (doInvwalk.getValue()) event.setCancelled(true);
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            if (packet.getContainerId() == 0) return;
            terminalContainer.setItem(packet.getSlot(), packet.getStateId(), packet.getItem());
            terminal.loadSlot(packet);

            if (doInvwalk.getValue()) event.setCancelled(true);
            return;
        }


        if (isInTerm() && event.getPacket() instanceof ClientboundContainerClosePacket packet) {
            if (packet.getContainerId() != terminalContainer.containerId) {
                ChatUtils.chat("Container ID mismatch on close!");
            }

            this.close();
            if (doInvwalk.getValue()) event.setCancelled(true);
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundSetCursorItemPacket packet) {
            if (doInvwalk.getValue()) event.setCancelled(true);
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundContainerSetContentPacket packet) {
            if (packet.containerId() != 0 && doInvwalk.getValue()) event.setCancelled(true);
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

/*package com.ricedotwho.rsa.module.impl.dungeon;

import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.SignedBytes;
import com.ricedotwho.rsa.module.impl.dungeon.terminals.*;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.client.TerminalEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2d;

import java.util.Arrays;
import java.util.List;

@ModuleInfo(aliases = "AutoTerms", id = "AutoTerms", category = Category.DUNGEONS)
public class AutoTerms extends Module {
    private long lastClickTime = 0L;
    private boolean clickedWindow = false;
    private boolean firstClick = true;
    private boolean cursorContentsState;
    private Terminal terminal;

    private final NumberSetting firstClickDelay = new NumberSetting("First Click Delay", 200d, 600d, 400d, 5d);
    private final NumberSetting delay = new NumberSetting("Delay", 100d, 250d, 150d, 5d);
    private final NumberSetting breakThreshold = new NumberSetting("Break Threshold", 200d, 800d, 500d, 10d);


    private final GroupSetting invwalk = new GroupSetting("Invwalk");
    private final BooleanSetting doInvwalk = new BooleanSetting("Enabled", false);
    private final ModeSetting style = new ModeSetting("Style", "Solver", Arrays.asList("Solver", "Items"));
    private final BooleanSetting renderTitles = new BooleanSetting("Render title thing", true);
    private final BooleanSetting renderClicksLeft = new BooleanSetting("Render clicks left", true);
    private final ColourSetting titleColour = new ColourSetting("Title Colour", new Colour(96,31,158));
    private final ColourSetting remainingColour = new ColourSetting("Remaining Colour", new Colour(96,31,158));
    private final ColourSetting clicksColour = new ColourSetting("Clicks Colour", new Colour(0, 191, 0));
    private final BooleanSetting textShadow = new BooleanSetting("Text Shadow", false);

    private final BooleanSetting doMoveDelay = new BooleanSetting("Do move delay", true);
    private final NumberSetting melodyMoveDelay = new NumberSetting("Move delay", 0, 10, 6, 1);

    private final DragSetting termTitle = new DragSetting("Term Title", new Vector2d(10, 10), new Vector2d(15, 150));
    private final DragSetting clicksText = new DragSetting("Clicks Text", new Vector2d(10, 10), new Vector2d(15, 150));
    private final DragSetting gui = new DragSetting("Visualiser Gui", new Vector2d(10, 10), new Vector2d(100, 66));


    public AutoTerms() {
        registerProperty(
                firstClickDelay,
                delay,
                breakThreshold
//                invwalk,
//                termTitle,
//                clicksText,
//                gui
        );
        invwalk.add(
                doInvwalk,
                style,
                renderTitles,
                renderClicksLeft,
                titleColour,
                remainingColour,
                clicksColour,
                textShadow,
                doMoveDelay,
                melodyMoveDelay
        );
        this.cursorContentsState = false;
    }

    @SubscribeEvent
    public void onLoadWorld(WorldEvent.Load event) {
        this.terminal = null;
    }

    @SubscribeEvent
    public void render(Render3DEvent.Last event) {
        // issues with race conditions
        if (!isInTerm()) return;

        if (firstClick && (System.currentTimeMillis() - lastClickTime < firstClickDelay.getValue())) return;

        if (System.currentTimeMillis() - lastClickTime < delay.getValue()) return;

        if (System.currentTimeMillis() - lastClickTime > breakThreshold.getValue()) {
            if (cursorContentsState)
                clickedWindow = false;
            else {
                //ChatUtils.chat("Could not reset break! Cursor contents full!");
            }
        }

        // Why is there another check here?
        if (!isInTerm() || clickedWindow || !terminal.shouldSolve()) return;

        terminal.solve();
        if (!terminal.isSolved()) return;

        Solution solution = terminal.getSolution();

        if (solution.getLength() < 1) return;

        sendWindowClick0(solution.getNext());
        lastClickTime = System.currentTimeMillis();
        clickedWindow = true;
        firstClick = false;
        cursorContentsState = false; // Only click again if the server cleared our held slot, otherwise that would fuck some shit with sendWindowClick0
    }


    // Need to change this for inv walk
    private void sendWindowClick(int windowID, SolutionClick click, Player player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
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

        abstractContainerMenu.clicked(click.index(), 0, click.type(), player);
        Int2ObjectMap<HashedStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();

        for (int m = 0; m < l; m++) {
            ItemStack itemStack = list.get(m);
            ItemStack itemStack2 = nonNullList.get(m).getItem();
            if (!ItemStack.matches(itemStack, itemStack2)) {
                int2ObjectMap.put(m, HashedStack.create(itemStack2, connection.decoratedHashOpsGenenerator()));
            }
        }

        HashedStack hashedStack = HashedStack.create(abstractContainerMenu.getCarried(), connection.decoratedHashOpsGenenerator());
        connection.send(new ServerboundContainerClickPacket(windowID, abstractContainerMenu.getStateId(), Shorts.checkedCast(click.index()), SignedBytes.checkedCast(0), click.type(), int2ObjectMap, hashedStack));
    }

    private void sendWindowClick0(SolutionClick click) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().getConnection() == null) return;
        if (!isInTerm() || click.index() < 0 || click.index() >= terminal.getType().getSlotCount()) return;

        Int2ObjectMap<HashedStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
        int2ObjectMap.put(click.index(), HashedStack.EMPTY);

        Minecraft.getInstance().getConnection().send(new ServerboundContainerClickPacket(terminal.getWindowID(), 0, Shorts.checkedCast(click.index()), SignedBytes.checkedCast(0), click.type(), int2ObjectMap, HashedStack.EMPTY));
    }

    private void sendWindowClick(SolutionClick click) {
        if (click.index() < 0) return;
        if (Minecraft.getInstance().player == null) return;
        // Make some checks
        sendWindowClick(terminal.getWindowID(), click, Minecraft.getInstance().player);
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
            this.terminal = Terminal.fromPacket(packet);
            this.clickedWindow = false;
            cursorContentsState = true;
            //ChatUtils.chat("Found terminal : " + (terminal == null ? "null" : terminal.getType().toString()));
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            this.terminal.loadSlot(packet);
            return;
        }


        if (isInTerm() && event.getPacket() instanceof ClientboundContainerClosePacket packet) {
            this.terminal = null;
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundSetCursorItemPacket(ItemStack contents)) {
            if (contents.isEmpty())
                cursorContentsState = true;
            return;
        }
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof  ServerboundContainerClickPacket packet) {
            ChatUtils.chat("WindowID : " + packet.containerId());
            ChatUtils.chat("ActionID : " + packet.stateId());
            ChatUtils.chat("More Stuff : " + packet.changedSlots());
            ChatUtils.chat("Stuff : " + packet.changedSlots().values());
        }

        if (isInTerm() && event.getPacket() instanceof ServerboundContainerClosePacket packet) {
            this.terminal = null;
            return;
        }
    }

    private boolean isInTerm() {
        return this.terminal != null;
    }


    // invwalk
}
*/
