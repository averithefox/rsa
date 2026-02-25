package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoroutesFileManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitClick;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitSecrets;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.BatNode;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.BreakNode;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomData;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Keybind;
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
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@ModuleInfo(aliases = "Auto Routes", id = "Autoroutes", category = Category.DUNGEONS)
public class AutoRoutes extends Module implements Accessor {

    @Getter
    private final HashMap<String, List<Node>> savedNodes = new HashMap<>();
    private final HashMap<RoomData, List<Node>> activeNodes = new HashMap<>();
    private final HashMap<String, List<Node>> redoMap = new HashMap<>();

    @Getter private static final BooleanSetting centerOnly = new BooleanSetting("Center Only", false);
    private final BooleanSetting editMode = new BooleanSetting("Edit Mode", false);
    private final KeybindSetting triggerBind = new KeybindSetting("Trigger Bind", new Keybind(GLFW.GLFW_MOUSE_BUTTON_1, true, this::onTrigger));
    private final KeybindSetting addBlockBind = new KeybindSetting("Add Block Bind", new Keybind(GLFW.GLFW_KEY_SEMICOLON, true, this::addBlockToInNode));

    // uhh surely this won't cause issues...
    private final DefaultGroupSetting render = new DefaultGroupSetting("Render", this);
    @Getter private static final BooleanSetting startDepth = new BooleanSetting("Start Depth", false);
    @Getter private static final BooleanSetting nodeDepth = new BooleanSetting("Node Depth", true);
    @Getter private static final ColourSetting startColour = new ColourSetting("Start", Colour.GREEN);
    @Getter private static final ColourSetting etherwarpColour = new ColourSetting("Etherwarp", Colour.CYAN);
    @Getter private static final ColourSetting breakColour = new ColourSetting("Break", Colour.YELLOW);
    @Getter private static final ColourSetting boomColour = new ColourSetting("Boom", Colour.RED);
    @Getter private static final ColourSetting batColour = new ColourSetting("Bat", Colour.BLUE);
    @Getter private static final ColourSetting aotvColour = new ColourSetting("Aotv", Colour.MAGENTA);
    @Getter private static final ColourSetting useColour = new ColourSetting("Use", Colour.WHITE); // idk

    private int tickTime = 0;
    private boolean forceNextNotSneak = false;
    private Node inNode;
    @Getter
    private boolean isRouting = false;
    private boolean receivedS08;
    private byte crouchDataShiftRegister = 0;
    public int lastBlockC08 = 0;

    private Class<? extends Node> lastType = null;

    // Player inputs are sent after C08s and keybinding events, in level.tickEntities

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
                centerOnly,
                triggerBind,
                addBlockBind,
                render
        );
        render.add(startDepth, nodeDepth, startColour, etherwarpColour, breakColour, boomColour, batColour, aotvColour);
        this.inNode = null;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        this.inNode = null;
        this.receivedS08 = false;
        this.activeNodes.clear();
        this.crouchDataShiftRegister = 0;
        this.lastBlockC08 = 0;
    }

    @SubscribeEvent
    public void onRoomEnter(DungeonEvent.ChangeRoom event) {
        if(event.unique == null || event.room == null || event.oldRoom == null) return;
        Room room = event.getRoom();
        this.inNode = null;
        if (activeNodes.containsKey(room.getData())) return;
        cacheRoomNodes(room);
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (!Location.getArea().is(Island.Dungeon) || Map.getCurrentRoom() == null) return;
        Room currentRoom = Map.getCurrentRoom();
        List<Node> nodes = this.activeNodes.get(currentRoom.getData());
        if (nodes == null || nodes.isEmpty()) return;
        nodes.forEach(n -> n.render(nodeDepth.getValue() && (!n.isStart() || startDepth.getValue())));
    }

    @SubscribeEvent
    public void onClientTickStart(ClientTickEvent.Start event) {
        lastBlockC08--;
        this.isRouting = false;
        if (!Location.getArea().is(Island.Dungeon)) return;
        tickTime++;

        if (hasGuiOpen()) return;
        if (this.editMode.getValue() || Map.getCurrentRoom() == null || Minecraft.getInstance().player == null) return;

        Room currentRoom = Map.getCurrentRoom();
        List<Node> nodes = this.activeNodes.get(currentRoom.getData());
        if (nodes == null || nodes.isEmpty()) {
            inNode = null;
            return;
        }
        Pos playerPos = new Pos(Minecraft.getInstance().player.position());


        nodes.forEach(n -> n.updateNodeState(playerPos, tickTime));

        this.lastType = null;

        while (true) {
            if (!handleQueue(playerPos, nodes)) break;
        }
    }

    public boolean willBeCrouchingForEtherwarpEvaluation() {
        return ((this.crouchDataShiftRegister >> 1) & 1) == 1;
    }

    @SubscribeEvent
    public void onPollInputs(InputPollEvent event) {
        if (!this.isRouting() || !Location.getArea().is(Island.Dungeon) || hasGuiOpen()) return;
        Input oldInputs = event.getClientInput();

//        ChatUtils.chat("Poll Input: " + this.forceNextNotSneak);

        Input newInputs = new Input(oldInputs.forward(), oldInputs.backward(), oldInputs.left(), oldInputs.right(), oldInputs.jump(), !this.forceNextNotSneak, oldInputs.sprint());
        this.forceNextNotSneak = false;
        event.getInputConsumer().accept(newInputs);
    }

    private void cacheRoomNodes(Room room) {
        List<Node> nodes = savedNodes.get(room.getData().name());
        if (nodes == null || nodes.isEmpty()) return;
        UniqueRoom uniqueRoom = room.getUniqueRoom();
        nodes.forEach(n -> n.calculate(uniqueRoom));
        activeNodes.put(room.getData(), nodes);
    }

    public void reload() {
        this.activeNodes.clear();
        this.inNode = null;
        this.receivedS08 = false;
        if (!Location.getArea().is(Island.Dungeon)) return;
        Room room = Map.getCurrentRoom();
        if (room == null || room.getUniqueRoom() == null) return;
        cacheRoomNodes(room);
    }

    private boolean hasGuiOpen() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>;
    }

    public boolean clearNodes(UniqueRoom uniqueRoom) {
        if (Minecraft.getInstance().player == null || !this.activeNodes.containsKey(uniqueRoom.getMainRoom().getData())) return false;
        List<Node> nodes = activeNodes.get(uniqueRoom.getMainRoom().getData());
        if (nodes.isEmpty()) return false;
        nodes.clear();
        AutoroutesFileManager.save();
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
        AutoroutesFileManager.save();
        return true;
    }

    public boolean undoNode(UniqueRoom uniqueRoom) {
        if (Minecraft.getInstance().player == null || !this.activeNodes.containsKey(uniqueRoom.getMainRoom().getData())) return false;
        List<Node> nodes = activeNodes.get(uniqueRoom.getMainRoom().getData());
        if (nodes.isEmpty()) return false;

        if (!redoMap.containsKey(uniqueRoom.getName())) {
            redoMap.put(uniqueRoom.getName(), new ArrayList<>());
        }

        Node node = nodes.removeLast();

        redoMap.get(uniqueRoom.getName()).add(node);
        AutoroutesFileManager.save();

        RSA.chat("Undid %s at %s", node.getName(), node.getRealPos().toChatString());
        return true;
    }

    public boolean redoNode(UniqueRoom uniqueRoom) {
        if (Minecraft.getInstance().player == null || !this.activeNodes.containsKey(uniqueRoom.getMainRoom().getData())) return false;
        List<Node> nodes = activeNodes.get(uniqueRoom.getMainRoom().getData());
        if (!redoMap.containsKey(uniqueRoom.getName())) return false;
        List<Node> redo = redoMap.get(uniqueRoom.getName());
        if (redo.isEmpty()) return false;
        Node node = redo.removeLast();
        nodes.add(node);
        AutoroutesFileManager.save();

        RSA.chat("Redid %s at %s", node.getName(), node.getRealPos().toChatString());
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
        AutoroutesFileManager.save();
    }

    public void setForceSneak(boolean bl) {
        this.forceNextNotSneak = bl;
    }

    public void onTrigger() {
        if (!this.isEnabled() || !Location.getArea().is(Island.Dungeon) || Map.getCurrentRoom() == null) return;

        if (this.inNode instanceof BatNode) this.inNode.setTriggered(true);
        if (this.inNode == null || !this.inNode.hasAwaits()) return;
        this.inNode.getAwaitManager().consume(AwaitClick.class, true);
        this.inNode.getAwaitManager().consume(AwaitSecrets.class, 100); // Skip secret
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (!Location.getArea().is(Island.Dungeon)  || Minecraft.getInstance().level == null) return;
        if (event.getPacket() instanceof ServerboundPlayerInputPacket(Input input)) {
            this.crouchDataShiftRegister <<= 1;
            this.crouchDataShiftRegister |= (byte) (input.shift() ? 1 : 0);
            return;
        }


        if (this.inNode == null || Map.getCurrentRoom() == null) return;
        if (!this.inNode.hasAwaits() || !this.inNode.getAwaitManager().hasAwait(AwaitSecrets.class)) return;
        if (!(event.getPacket() instanceof ServerboundUseItemOnPacket useItemOnPacket)) return;
        Block block = Minecraft.getInstance().level.getBlockState(useItemOnPacket.getHitResult().getBlockPos()).getBlock();
        if (block != Blocks.CHEST && block != Blocks.TRAPPED_CHEST && block != Blocks.PLAYER_HEAD && block != Blocks.LEVER) return;
        this.inNode.getAwaitManager().consume(AwaitSecrets.class, 1);
        this.lastBlockC08 = 2; // Hypixel voids C08s sometimes after secret auraing
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (!Location.getArea().is(Island.Dungeon) || Map.getCurrentRoom() == null) return;

        if (this.inNode != null && event.getPacket() instanceof ClientboundTakeItemEntityPacket packet) {
            if (Minecraft.getInstance().level == null) return;
            Entity entity = Minecraft.getInstance().level.getEntity(packet.getItemId());
            if (!(entity instanceof ItemEntity itemEntity)) return;
            String name = ChatFormatting.stripFormatting(itemEntity.getItem().getDisplayName().getString());
            if (!SECRET_NAMES.contains(name)) return;
            //ChatUtils.chat("Picked up secret!");
            //ChatUtils.chat(this.inNode.getRealPos());
            if (!this.inNode.hasAwaits() || !this.inNode.getAwaitManager().hasAwait(AwaitSecrets.class)) return; // Move earlier
            this.inNode.getAwaitManager().consume(AwaitSecrets.class, 1);
        }
    }

    private void trySetInNode(Node node) {
        if (this.inNode == node) return;

        this.inNode = node;
        if (node.hasAwaits()) node.getAwaitManager().onEnterNode();
    }

    public boolean handleQueue(Pos playerPos, List<Node> nodes) {
        List<Node> activeNodes = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (!node.isInNode(playerPos)) continue;
            this.isRouting = true;
            if (node.isTriggered() || node.hasRanThisTick(tickTime)) continue;
            activeNodes.add(node);
        }

        if (activeNodes.isEmpty()) {
            this.inNode = null;
            return false;
        }

        activeNodes.sort(Comparator.comparingInt(n -> ((Node) n).getPriority()).reversed());

        Node node = activeNodes.getFirst();
        trySetInNode(node);

        if (node.shouldAwait() || lastBlockC08 > 0 || (lastType != null && lastType != node.getClass())) return false;

        node.preTrigger(tickTime);
        boolean bl = node.run(playerPos);
        if (bl) lastType = node.getClass();
        return bl;
    }

    private void addBlockToInNode() {
        Room currentRoom = Map.getCurrentRoom();
        if (!Location.getArea().is(Island.Dungeon) || currentRoom == null || this.activeNodes.isEmpty() || mc.player == null || !this.activeNodes.containsKey(currentRoom.getData())) return;
        Pos playerPos = new Pos(mc.player.position());
        Optional<BreakNode> opt = this.activeNodes.get(currentRoom.getData())
                .stream().filter(n -> n.isInNode(playerPos) && n instanceof BreakNode).map(n -> (BreakNode) n).findFirst();
        if (opt.isEmpty()) {
            RSA.chat("Not in break node");
            return;
        }
        opt.get().addOrRemoveBlock();
    }

}