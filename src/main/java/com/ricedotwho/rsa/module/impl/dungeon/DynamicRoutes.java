package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.component.impl.pathfinding.EtherwarpPathfinder;
import com.ricedotwho.rsa.component.impl.pathfinding.Goal;
import com.ricedotwho.rsa.component.impl.pathfinding.Path;
import com.ricedotwho.rsa.component.impl.pathfinding.PathfindingCalculationContext;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.DynamicEtherwarpNode;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.EtherwarpNode;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomRotation;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.sun.jna.platform.linux.Udev;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Consumer;

@ModuleInfo(aliases = "Dynamicroutes", id = "Dynamicroutes", category = Category.MOVEMENT)
public class DynamicRoutes extends Module {
    private final UniqueRoom EMPTY_UNIQUE;

    @Getter
    private final List<Node> nodes = new ArrayList<>();

    private static final BooleanSetting centerOnly = new BooleanSetting("Center Only", false);
    private final BooleanSetting oneUse = new BooleanSetting("Delete After Use", true);
    private final BooleanSetting editMode = new BooleanSetting("Edit Mode", false);

    private final DefaultGroupSetting render = new DefaultGroupSetting("Render", this);
    private static final BooleanSetting nodeDepth = new BooleanSetting("Node Depth", true);

    @Getter
    private static final ColourSetting nodeColor = new ColourSetting("Color", Colour.ORANGE);

    private final DefaultGroupSetting pathfinder = new DefaultGroupSetting("Pathfinding", this);
    private final NumberSetting heuristicThreshold = new NumberSetting("Heuristic Threshold", 0.1, 5, 0.5, 0.1);
    private final NumberSetting threadCount = new NumberSetting("Thead Count", 1d, 64d, 8d, 1d);
    private final NumberSetting nodeCost = new NumberSetting("Node Cost", 1d, 10000d, 500d, 1d);
    private final NumberSetting yawStep = new NumberSetting("Yaw Step", 0.1d, 10d, 4d, 0.1d);
    private final NumberSetting pitchStep = new NumberSetting("Pitch Step", 0.1d, 10d, 2d, 0.1d);

    private EtherwarpPathfinder currentPathfinder;
    private Thread pathfinderThread;
    private final List<Goal> pathQueue;


    private int tickTime = 0;

    @Getter
    private boolean isRouting = false;

    public DynamicRoutes() {
        this.registerProperty(
                editMode,
                centerOnly,
                oneUse,
                render,
                pathfinder
        );
        this.pathQueue = new ArrayList<>();
        render.add(nodeDepth, nodeColor);
        pathfinder.add(threadCount, heuristicThreshold, nodeCost, yawStep, pitchStep);

        EMPTY_UNIQUE = UniqueRoom.emptyUnique();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        this.nodes.clear();
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (nodes.isEmpty()) return;
        nodes.forEach(n -> n.render(nodeDepth.getValue()));
    }

    @SubscribeEvent
    public void onClientTickStart(ClientTickEvent.Start event) {
        this.isRouting = false;
        tickTime++;

        if (hasGuiOpen()) return;
        if (this.editMode.getValue() || Minecraft.getInstance().player == null) return;

        if (nodes.isEmpty()) return;

        Pos playerPos = new Pos(Minecraft.getInstance().player.position());

        nodes.forEach(n -> n.updateNodeState(playerPos, tickTime));

        while (true) {
            if (!handleQueue(playerPos, nodes)) break;
        }
    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (!this.isRouting() || hasGuiOpen()) return;
        Input oldInputs = event.getClientInput();

        Input newInputs = new Input(oldInputs.forward(), oldInputs.backward(), oldInputs.left(), oldInputs.right(), oldInputs.jump(), true, oldInputs.sprint());
        event.getInputConsumer().accept(newInputs);
    }

    public void pathGoals(BlockPos startPos, List<? extends Goal> goals) {
        if ((!pathQueue.isEmpty()) || goals.isEmpty()) return;
        this.pathQueue.addAll(goals);
        pathNextQueued(startPos);
    }

    private void pathNextQueued(BlockPos pos) {
        if (pathQueue.isEmpty()) return;
        Goal goal = pathQueue.removeFirst();
        PathfindingCalculationContext ctx = new PathfindingCalculationContext(pos.mutable(), this.threadCount.getValue().intValue(), this.yawStep.getValue().floatValue(), this.pitchStep.getValue().floatValue(), this.nodeCost.getValue().floatValue(), this.heuristicThreshold.getValue().floatValue());
        executePath(new EtherwarpPathfinder(ctx, goal), (path) -> this.pathNextQueued(path.getEndNode().getPos()));
    }

    public void executePath(BlockPos startPos, Goal goal) {
        PathfindingCalculationContext ctx = new PathfindingCalculationContext(startPos.mutable(), this.threadCount.getValue().intValue(), this.yawStep.getValue().floatValue(), this.pitchStep.getValue().floatValue(), this.nodeCost.getValue().floatValue(), this.heuristicThreshold.getValue().floatValue());
        executePath(new EtherwarpPathfinder(ctx, goal), null);
    }

    public void executePath(EtherwarpPathfinder pathfinder, Consumer<Path> callback) {
        if (this.currentPathfinder != null) {
            ChatUtils.chat("Pathfinder already active!");
            return;
        }

        this.currentPathfinder = pathfinder;
        this.pathfinderThread = new Thread(() -> {
            Path path = pathfinder.calculate();
            if (path == null) return;

            path.consumeNodes(this::addNode, DynamicEtherwarpNode::fromBlockPos);
            this.currentPathfinder = null;
            if (callback != null) callback.accept(path);
        });
        this.pathfinderThread.start();
    }


    public boolean isPathing() {
        return this.currentPathfinder != null;
    }

    public boolean cancelPathing() {
        this.pathQueue.clear();
        if (this.currentPathfinder == null) return false;
        this.currentPathfinder.cancel();
        this.currentPathfinder = null;
        return true;
    }

    private boolean hasGuiOpen() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>;
    }

    public boolean clearNodes() {
        nodes.clear();
        return true;
    }

    public boolean removeNearest() {
        if (Minecraft.getInstance().player == null) return false;
        if (nodes.isEmpty()) return false;

        int bestIndex = -1;
        double bestDistance = Double.MAX_VALUE;
        Vec3 playerPos = Minecraft.getInstance().player.position();
        for (int i = 0; i < nodes.size(); i++) {
            double d = nodes.get(i).getRealPos().squaredDistanceTo(playerPos);
            if (d >= bestDistance) continue;
            bestIndex = i;
            bestDistance = d;
        }

        if (bestIndex < 0) return false;
        nodes.remove(bestIndex);
        return true;
    }

    public boolean addNode(LocalPlayer player) {
       Node node = DynamicEtherwarpNode.supply(EMPTY_UNIQUE, player);

       addNode(node);
       return true;
    }

    public void addNode(Node node) {
        node.calculate(EMPTY_UNIQUE);
        this.nodes.add(node);
    }

    public boolean handleQueue(Pos playerPos, List<Node> nodes) {
        // Don't need to sort, they should all be EWs
        // .sorted(Comparator.comparingInt(n -> ((Node) n).getPriority()).reversed())

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.isTriggered() || node.hasRanThisTick(tickTime) || !node.isInNode(playerPos)) continue;

            this.isRouting = true;

            node.preTrigger(tickTime);
            boolean bl = node.run(playerPos);
            if (bl && this.oneUse.getValue()) {
                nodes.remove(i);
            }
            return bl;
        }
        return false;
    }
}
