package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.google.gson.annotations.Expose;
import com.ricedotwho.rsa.component.impl.BlockAura;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.Circle;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import com.ricedotwho.rsm.utils.render.render3d.type.RenderTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BreakNode extends Node implements Accessor {
    private static final Colour BLOCK_COLOUR = new Colour(255, 255, 0, 90);

    public BreakNode(Pos localPos, AwaitManager awaits) {
        super(localPos, awaits);

        // Blocks should be added through a keybind or by tracking what blocks the player breaks? or maybe a command too
        this.blocks = new ArrayList<>();
    }

    @Expose
    private final List<Pos> blocks;
    private List<Pos> rotated = null;

    @Override
    public void calculate(UniqueRoom room) {
        super.calculate(room);
        rotated = blocks.stream().map(pos -> RoomUtils.getRealPosition(pos, room.getMainRoom())).toList();
    }

    @Override
    public boolean run(Pos playerPos) {
        if (!SwapManager.reserveSwap("DUNGEONBREAKER")) return cancel();
        // I will assume rotated gets recreated every new instance of a room
        List<Pos> temp = new ArrayList<>(rotated);
        Pos first = temp.removeFirst();
        BlockAura.addBlock(first, true, true);
        BlockAura.addBlock(rotated, true);
        return false;
    }

    public boolean cancel() {
        this.reset();
        return false;
    }

    @Override
    public void render() {
        Renderer3D.addTask(new Circle(this.getRealPos(), true, this.getRadius(), Colour.YELLOW, 30));
        if (this.rotated == null) return;
        for (Pos pos : rotated) {
            BlockPos bp = pos.asBlockPos();
            BlockState state = mc.level.getBlockState(bp);
            VoxelShape shape = state.getShape(mc.level, bp);
            if (shape.isEmpty()) continue; // air
            Renderer3D.addTask(new FilledBox(shape.bounds(), BLOCK_COLOUR, true));
        }
    }

    @Override
    public int getPriority() {
        return 18;
    }

    @Override
    public String getName() {
        return "break";
    }
}
