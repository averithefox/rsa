package com.ricedotwho.rsa.component.impl.pathfinding;

import com.mojang.datafixers.util.Function4;
import com.ricedotwho.rsa.module.impl.dungeon.DynamicRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.DynamicEtherwarpNode;
import com.ricedotwho.rsm.RSM;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class Path {
    private final BlockPos start;
    private final PathNode startNode;
    private final PathNode endNode;
    private final Goal goal;

    public Path(BlockPos start, PathNode startNode, PathNode endNode, Goal goal) {
        this.start = start;
        this.startNode = startNode;
        this.endNode = endNode;
        this.goal = goal;
    }

    public BlockPos getStart() {
        return start;
    }

    public PathNode getStartNode() {
        return startNode;
    }

    public PathNode getEndNode() {
        return endNode;
    }

    public int length() {
        int count = 0;
        PathNode node = endNode;
        while (node.getParent() != null) {
            count++;
            node = node.getParent();
        }
        return count;
    }

    public<T> void consumeNodes(Consumer<T> consumer, Function4<BlockPos, Float, Float, Boolean, T> provider) {
        PathNode node = this.getEndNode();
        PathNode last = null;
        boolean isLast = true;

        while (node != null) {
            if (last != null) {
                consumer.accept(provider.apply(node.getPos(), last.getYaw(), last.getPitch(), isLast));
                isLast = false;
            }
            last = node;
            node = node.getParent();
        }

    }

    public Goal getGoal() {
        return goal;
    }
}
