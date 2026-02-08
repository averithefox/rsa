package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.awaits.AwaitClick;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.awaits.AwaitSecrets;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.nodes.BoomNode;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomData;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@ModuleInfo(aliases = "Autoroutes", id = "Autoroutes", category = Category.DUNGEONS)
public class AutoRoutes extends Module implements Accessor {

    private final HashMap<String, List<Node>> savedNodes = new HashMap<>();
    private final HashMap<RoomData, List<Node>> activeNodes = new HashMap<>();

    private BooleanSetting teleportOnly = new BooleanSetting("Teleport Only", true);
    private BooleanSetting editMode = new BooleanSetting("Edit Mode", false);

    private int tickTime = 0;
    private boolean forceNextSneak = false;
    private Node inNode;
    private boolean receivedS08;

    private static final Set<String> SECRET_NAMES  = Set.of(
            "[Health Potion VIII Splash Potion]",
            "[Healing Potion 8 Splash Potion]",
            "[Healing Potion VIII Splash Potion]",
            "[Healing VIII Splash Potion]",
            "[Healing 8 Splash Potion]",
            "[Decoy]",
            "[Inflatable Jerry]",
            "[Spirit Leap]",
            "[Trap]",
            "[Training Weights]",
            "[Defuse Kit]",
            "[Dungeon Chest Key]",
            "[Treasure Talisman]",
            "[Revive Stone]",
            "[Architect's First Draft]"
    );

    public AutoRoutes() {
        this.registerProperty(
                editMode,
                teleportOnly
        );
        this.inNode = null;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        this.inNode = null;
        this.receivedS08 = false;
    }

    @SubscribeEvent
    public void onRoomEnter(DungeonEvent.ChangeRoom event) {
        Room room = event.getRoom();
        this.inNode = null;
        if (activeNodes.containsKey(room.getData())) return;
        List<Node> nodes = savedNodes.get(room.getData().name());
        if (nodes == null || nodes.isEmpty()) return;
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
        if (!Location.getArea().is(Island.Dungeon)) return;
        tickTime++;

        if (teleportOnly.getValue() && !this.receivedS08 && this.inNode == null || hasGuiOpen()) return;
        if (this.editMode.getValue() || Map.getCurrentRoom() == null || Minecraft.getInstance().player == null) return;

        Room currentRoom = Map.getCurrentRoom();
        List<Node> nodes = this.activeNodes.get(currentRoom.getData());
        if (nodes == null || nodes.isEmpty()) {
            inNode = null;
            return;
        }


        Pos playerPos = new Pos(Minecraft.getInstance().player.position());


        while (true) {
            if (!handleQueue(playerPos, nodes)) break;
        }
    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (!this.isEnabled() || !this.forceNextSneak || !Location.getArea().is(Island.Dungeon) || hasGuiOpen()) return;
        Input oldInputs = event.getClientInput();

        Input newInputs = new Input(oldInputs.forward(), oldInputs.backward(), oldInputs.left(), oldInputs.right(), oldInputs.jump(), this.forceNextSneak, oldInputs.sprint());
        this.forceNextSneak = false;
        event.getInputConsumer().accept(newInputs);
    }

    private boolean hasGuiOpen() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>;
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
        node.calculate(uniqueRoom);
        nodes.add(node); // Don't add to active nodes list because they share the same list objects
        if (!activeNodes.containsKey(uniqueRoom.getMainRoom().getData())) {
            // But we might need to add the list in the first place
            activeNodes.put(uniqueRoom.getMainRoom().getData(), nodes);
        }
    }

    public void setForceSneak(boolean bl) {
        this.forceNextSneak = bl;
    }

    public void onAttack() {
        if (!this.isEnabled() || !Location.getArea().is(Island.Dungeon) || Map.getCurrentRoom() == null) return;

        if (this.inNode == null || !this.inNode.hasAwaits()) return;
        this.inNode.getAwaits().consume(AwaitClick.class, true);
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (!Location.getArea().is(Island.Dungeon) || Map.getCurrentRoom() == null || Minecraft.getInstance().level == null || this.inNode == null) return;
        if (!this.inNode.hasAwaits() || !this.inNode.getAwaits().hasAwait(AwaitSecrets.class)) return;
        if (!(event.getPacket() instanceof ServerboundUseItemOnPacket useItemOnPacket)) return;
        Block block = Minecraft.getInstance().level.getBlockState(useItemOnPacket.getHitResult().getBlockPos()).getBlock();
        if (block != Blocks.CHEST && block != Blocks.TRAPPED_CHEST && block != Blocks.PLAYER_HEAD && block != Blocks.LEVER) return;
        this.inNode.getAwaits().consume(AwaitSecrets.class, 1);
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (!Location.getArea().is(Island.Dungeon) || Map.getCurrentRoom() == null) return;

        if (this.inNode != null && event.getPacket() instanceof ClientboundTakeItemEntityPacket packet && this.inNode.hasAwaits()) {
            if (!this.inNode.getAwaits().hasAwait(AwaitSecrets.class)) return;
            if (Minecraft.getInstance().level == null) return;
            Entity entity = Minecraft.getInstance().level.getEntity(packet.getItemId());
            if (!(entity instanceof ItemEntity itemEntity)) return;
            String name = ChatFormatting.stripFormatting(itemEntity.getItem().getDisplayName().getString());
            if (!SECRET_NAMES.contains(name)) return;
            this.inNode.getAwaits().consume(AwaitSecrets.class, 1);
        }

        if (teleportOnly.getValue() && event.getPacket() instanceof ClientboundPlayerPositionPacket S08) {
            this.receivedS08 = true; // Maybe should check if the S08 is in a node in the first place but it shouldn't really matter
        }

    }

    private void trySetInNode(Node node) {
        if (this.inNode == node) return;

        this.inNode = node;
        if (node.hasAwaits()) node.getAwaits().onEnterNode();
    }


    public boolean handleQueue(Pos playerPos, List<Node> nodes) {
        List<Node> activeNodes = nodes.stream().filter(n -> n.updateNodeState(playerPos, tickTime)).sorted(Comparator.comparingInt(n -> ((Node) n).getPriority()).reversed()).toList();
        if (activeNodes.isEmpty()) {
            this.inNode = null;
            return false;
        }

        Node node = activeNodes.getFirst();
        if (node instanceof BoomNode) {
            // Do boom or whatever
            trySetInNode(node);

            node = activeNodes.stream().filter(n -> !(n instanceof BoomNode)).findFirst().orElse(null);
            if (node == null) return false;
        }

        trySetInNode(node);

        if (node.shouldAwait()) {
            node.reset();
            return false;
        }
        return node.run(playerPos);
    }


}