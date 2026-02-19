package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.DynamicEtherwarpNode;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.EtherwarpNode;
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
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@ModuleInfo(aliases = "Dynamicroutes", id = "Dynamicroutes", category = Category.MOVEMENT)
public class DynamicRoutes extends Module {
    private final UniqueRoom EMPTY_UNIQUE;

    @Getter
    private final List<Node> nodes = new ArrayList<>();

    private static final BooleanSetting centerOnly = new BooleanSetting("Center Only", false);
    private final BooleanSetting editMode = new BooleanSetting("Edit Mode", false);

    private final DefaultGroupSetting render = new DefaultGroupSetting("Render", this);
    private static final BooleanSetting nodeDepth = new BooleanSetting("Node Depth", true);

    @Getter
    private static final ColourSetting nodeColor = new ColourSetting("Color", Colour.ORANGE);

    private int tickTime = 0;

    @Getter
    private boolean isRouting = false;

    public DynamicRoutes() {
        this.registerProperty(
                editMode,
                centerOnly,
                render
        );
        render.add(nodeDepth, nodeColor);

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
       if (node == null) return false;

       addNode(node);
       return true;
    }

    public void addNode(Node node) {
        node.calculate(EMPTY_UNIQUE);
        this.nodes.add(node);
    }

    public boolean handleQueue(Pos playerPos, List<Node> nodes) {
        // Don't need to sort, they should all be EWs
        List<Node> activeNodes = nodes.stream().filter(n -> !n.isTriggered() && !n.hasRanThisTick(tickTime) && n.isInNode(playerPos)).toList(); // .sorted(Comparator.comparingInt(n -> ((Node) n).getPriority()).reversed())
        if (activeNodes.isEmpty()) return false;

        this.isRouting = true;

        Node node = activeNodes.getFirst();

        node.preTrigger(tickTime);
        return node.run(playerPos);
    }
}
