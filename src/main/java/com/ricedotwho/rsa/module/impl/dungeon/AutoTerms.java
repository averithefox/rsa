package com.ricedotwho.rsa.module.impl.dungeon;

import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.SignedBytes;
import com.ricedotwho.rsa.component.impl.TickFreeze;
import com.ricedotwho.rsa.event.impl.RawTickEvent;
import com.ricedotwho.rsa.module.impl.dungeon.terminals.*;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
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
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2d;

import java.util.Arrays;
import java.util.List;

@Getter
@ModuleInfo(aliases = "AutoTerms", id = "AutoTerms", category = Category.DUNGEONS)
public class AutoTerms extends Module {
    private long lastClickTime = 0L;
    private boolean clickedWindow = false;
    private boolean firstClick = true;
    private Terminal terminal;

    private AbstractContainerMenu terminalContainer;
    private final ClickedSlotsTracker clickedSlotsTracker;
    private final TerminalRenderer terminalRenderer;

    private TerminalState predictedState = null;

    private final NumberSetting firstClickDelay = new NumberSetting("First Click Delay", 200d, 600d, 400d, 5d);
    private final NumberSetting delay = new NumberSetting("Delay", 100d, 250d, 150d, 5d);
    private final NumberSetting breakThreshold = new NumberSetting("Break Threshold", 200d, 800d, 500d, 10d);

    private final BooleanSetting melodySkip = new BooleanSetting("Melody Skip", true);
    private final BooleanSetting melodySkipFirst = new BooleanSetting("Don't Skip First", true);

    private final GroupSetting invWalkGroup = new GroupSetting("Invwalk");
    private final BooleanSetting doInvwalk = new BooleanSetting("Enabled", false);
    private final ModeSetting style = new ModeSetting("Style", "Items", Arrays.asList("Solver", "Items"));

    private final BooleanSetting renderTitles = new BooleanSetting("Render title thing", true);
    private final BooleanSetting renderClicksLeft = new BooleanSetting("Render clicks left", true);
    private final ColourSetting titleColour = new ColourSetting("Title Colour", new Colour(96,31,158));
    private final ColourSetting remainingColour = new ColourSetting("Remaining Colour", new Colour(96,31,158));
    private final ColourSetting clicksColour = new ColourSetting("Clicks Colour", new Colour(0, 191, 0));
    @Getter private static final ColourSetting solutionColour = new ColourSetting("Solution Colour", new Colour(0, 150, 0));
    @Getter private static final ColourSetting oppositeColour = new ColourSetting("Opposite Colour", new Colour(0, 0, 150));
    @Getter private static final ColourSetting orderColour1 = new ColourSetting("Order Colour 1", new Colour(0, 150, 0));
    @Getter private static final ColourSetting orderColour2 = new ColourSetting("Order Colour 2", new Colour(150, 150, 0));
    @Getter private static final ColourSetting orderColour3 = new ColourSetting("Order Colour 3", new Colour(150, 0, 0));
    private final NumberSetting gap = new NumberSetting("Gap", 0, 3, 1.5, 0.01);
    private final BooleanSetting textShadow = new BooleanSetting("Text Shadow", false);

    private final ModeSetting moveDelayMode = new ModeSetting("Mode Delay", "Stop Inputs", List.of("Stop Inputs", "Freeze"));
    private final NumberSetting melodyMoveDelay = new NumberSetting("Melody Move Delay", 0, 500, 300, 50);

    private final DragSetting termTitle = new DragSetting("Term Title", new Vector2d(10, 10), new Vector2d(150, 15));
    private final DragSetting clicksText = new DragSetting("Clicks Text", new Vector2d(10, 10), new Vector2d(150, 15));
    private final DragSetting gui = new DragSetting("Visualiser Gui", new Vector2d(551, 330), new Vector2d(144, 80));


    private int melodyMoveCounter = 0;


    public AutoTerms() {
        this.clickedSlotsTracker = new ClickedSlotsTracker();
        this.terminalRenderer = new TerminalRenderer();
        registerProperty(
                firstClickDelay,
                delay,
                breakThreshold,
                melodySkip,
                melodySkipFirst,
                invWalkGroup,
                gui,
                termTitle,
                clicksText
        );

        invWalkGroup.add(
                doInvwalk,
                style,
                renderTitles,
                renderClicksLeft,
                titleColour,
                remainingColour,
                clicksColour,
                solutionColour,
                oppositeColour,
                gap,
                textShadow,
                moveDelayMode,
                melodyMoveDelay
        );
    }

    @SubscribeEvent
    public void onLoadWorld(WorldEvent.Load event) {
        close();
    }

    @SubscribeEvent
    public void onRenderGui(Render2DEvent event) {
        try {
           if (!isInTerm() || !this.doInvwalk.getValue()) return;

           float width = 9 * 16f;
           int slots = TerminalRenderer.getGuiSlotCount(this.terminalContainer.getType());
           float height = (float) (Math.floor(slots / 9f) * 16);

           if (this.style.is("Items")) {
               gui.renderScaledGFX(event.getGfx(), () -> terminalRenderer.renderItems(event.getGfx(), this.terminal), width, height);
           } else {
               gui.renderScaled(event.getGfx(), () -> terminalRenderer.renderSolver(this.gap.getValue().floatValue(), this.terminal), width, height);
           }
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    @SubscribeEvent
    public void render(Render3DEvent.Last event) {
        // issues with race conditions
        if (!isInTerm() || terminal instanceof Melody) return;

        if (terminal.shouldSolve() && !terminal.isSolved()) {
            terminal.solve();
        }

        if (!terminal.isSolved()) return;

        if (predictedState != null) {
            TerminalState newState = this.terminal.getCurrentState();
            if (!predictedState.matches(newState)) {
//                ChatUtils.chat("First click detected!");
//                ChatUtils.chat("Old : " + predictedState.getHash());
//                ChatUtils.chat("new : " + newState.getHash());
                this.firstClick = true;
                this.lastClickTime = System.currentTimeMillis();
                this.clickedSlotsTracker.clear();
            }
            this.predictedState = null;
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

    public void sendWindowClick(SolutionClick click) {
        if (Minecraft.getInstance().player == null) return;
        if (!isInTerm() || click.index() < 0 || click.index() >= terminal.getType().getSlotCount()) return;
        // Make some checks
        if (this.terminal instanceof StartsWith || this.terminal instanceof Colors)
            this.clickedSlotsTracker.clickSlot(this.terminalContainer.getSlot(click.index()));
        sendWindowClick(terminal.getWindowID(), click, Minecraft.getInstance().player, this.terminalContainer);
    }

    // This works for strafe but not for forwards and backwards for some reason
    @SubscribeEvent
    public void onPollInput(InputPollEvent event) {
        if (this.melodyMoveCounter < 1) return;

        if (Minecraft.getInstance().screen == null && !this.isInTerm()) {
            this.melodyMoveCounter = 0;
            return;
        }

        Input oldInputs = event.getClientInput();
        Input newInputs = new Input(false, false, false, false, false, oldInputs.shift(), false);
        event.getInputConsumer().accept(newInputs);

        this.melodyMoveCounter--;
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if (!isInTerm()) {
            firstClick = true;
            this.clickedSlotsTracker.clear();
            lastClickTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRawTick(RawTickEvent event) {
        if (isInTerm() && terminal instanceof Melody melody) {
            if (melody.onTickStart(this)) {
                if (this.moveDelayMode.is("Freeze")) {
                    TickFreeze.freeze(this.melodyMoveDelay.getValue().longValue(), true);
                } else {
                    this.melodyMoveCounter = (this.melodyMoveDelay.getValue().intValue() / 50);
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundOpenScreenPacket packet) {
            if (packet.getContainerId() < 1 || packet.getContainerId() > 100) return;
            if (Minecraft.getInstance().player == null) return;

            TerminalState predictionState = new TerminalState(null, 0);
            if (this.terminal != null && this.terminal.isSolved()) {
                predictionState = this.terminal.getNextState();
            }

            this.terminalContainer = packet.getType().create(packet.getContainerId(), Minecraft.getInstance().player.getInventory());

            this.terminal = Terminal.fromPacket(packet, terminalContainer);
            if (this.terminal == null) {
                this.terminalContainer = null;
                return;
            }

            this.predictedState = predictionState;
            this.clickedWindow = false;
            this.terminalRenderer.newWindow(terminalContainer);

            if (doInvwalk.getValue()) event.setCancelled(true);
            return;
        }

        if (isInTerm() && event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            if (packet.getContainerId() == 0 || packet.getContainerId() != this.terminalContainer.containerId) return;
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
        this.predictedState = null;
        this.firstClick = true;
        this.lastClickTime = System.currentTimeMillis();
        this.clickedSlotsTracker.clear();
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (isInTerm() && event.getPacket() instanceof ServerboundContainerClosePacket packet) {
            this.close();
            return;
        }
    }

    public static boolean isInTerminal() {
        return RSM.getModule(AutoTerms.class).isInTerm();
    }

    private boolean isInTerm() {
        return this.terminal != null && terminalContainer != null;
    }
}