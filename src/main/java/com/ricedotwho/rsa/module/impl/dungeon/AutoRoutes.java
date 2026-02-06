package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.nodes.BoomNode;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomData;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.Accessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.transformer.meta.MixinInner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@ModuleInfo(aliases = "Autoroutes", id = "Autoroutes", category = Category.DUNGEONS)
public class AutoRoutes extends Module implements Accessor {

    private final HashMap<String, List<Node>> savedNodes = new HashMap<>();
    private final HashMap<RoomData, List<Node>> activeNodes = new HashMap<>();

    private int tickTime = 0;
    private boolean forceNextSneak = false;

    @SubscribeEvent
    public void onRoomEnter(DungeonEvent.ChangeRoom event) {
        Room room = event.getRoom();
        if (activeNodes.containsKey(room.getData())) return;
        List<Node> nodes = savedNodes.get(room.getData().name());
        UniqueRoom uniqueRoom = room.getUniqueRoom();
        nodes.forEach(n -> n.calculate(uniqueRoom));
        activeNodes.put(room.getData(), nodes);
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (!Location.getArea().is(Island.Dungeon) || Map.getCurrentRoom() == null) return;
        Room currentRoom = Map.getCurrentRoom();
        List<Node> nodes = this.activeNodes.get(currentRoom.getData());
        if (nodes == null || nodes.isEmpty()) return;
        nodes.forEach(Node::render);
    }

    @SubscribeEvent
    public void onClientTickStart(ClientTickEvent.Start event) {
        if (!Location.getArea().is(Island.Dungeon) || Map.getCurrentRoom() == null || Minecraft.getInstance().player == null) return;
        tickTime++;

        Room currentRoom = Map.getCurrentRoom();
        List<Node> nodes = this.activeNodes.get(currentRoom.getData());
        if (nodes == null || nodes.isEmpty()) return;


        Pos playerPos = new Pos(Minecraft.getInstance().player.position());


        while (true) {
            if (!handleQueue(playerPos, nodes)) break;
        }
    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (!this.isEnabled() || !this.forceNextSneak) return;
        Input oldInputs = event.getClientInput();

        Input newInputs = new Input(oldInputs.forward(), oldInputs.backward(), oldInputs.left(), oldInputs.right(), oldInputs.jump(), this.forceNextSneak, oldInputs.sprint());
        this.forceNextSneak = false;
        event.getInputConsumer().accept(newInputs);
    }

    public boolean clearNodes(UniqueRoom uniqueRoom) {
        if (Minecraft.getInstance().player == null || !this.activeNodes.containsKey(uniqueRoom.getMainRoom().getData())) return false;
        List<Node> nodes = activeNodes.get(uniqueRoom.getMainRoom().getData());
        if (nodes.isEmpty()) return false;
        nodes.clear();
        return true;
    }

    public boolean removeNearest(UniqueRoom uniqueRoom) {
        if (Minecraft.getInstance().player == null || !this.activeNodes.containsKey(uniqueRoom.getMainRoom().getData())) return false;
        List<Node> nodes = activeNodes.get(uniqueRoom.getMainRoom().getData());
        if (nodes.isEmpty()) return false;
        // Once again don't need to use saveNodes because they share the same list, if you're trying to remove them they should already be loaded

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

    public void addNode(Node node, UniqueRoom uniqueRoom) {
        this.savedNodes.putIfAbsent(uniqueRoom.getName(), new ArrayList<>());
        List<Node> nodes = savedNodes.get(uniqueRoom.getName());
        nodes.add(node);
        // Don't need to put into active nodes because they share the same list objects
    }

    public void setForceSneak(boolean bl) {
        this.forceNextSneak = bl;
    }

    public boolean handleQueue(Pos playerPos, List<Node> nodes) {
        List<Node> activeNodes = nodes.stream().filter(n -> n.updateNodeState(playerPos, tickTime)).sorted(Comparator.comparingInt(n -> ((Node) n).getPriority()).reversed()).toList();
        if (activeNodes.isEmpty()) return false;
//        if (playerPos == Minecraft.getInstance().player.position()) {
//            ChatUtils.chat("Node evaluation tick start!");
//        }

        Node node = activeNodes.getFirst();
        if (node instanceof BoomNode) {
            // Do boom or whatever

            node = activeNodes.stream().filter(n -> !(n instanceof BoomNode)).findFirst().orElse(null);
            if (node == null) return false;
        }

        return node.run(playerPos);
    }


}