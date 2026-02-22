package com.ricedotwho.rsa.component.impl.pathfinding;

import net.minecraft.core.BlockPos;

public record PathfindingCalculationContext(BlockPos.MutableBlockPos startBlock, int threadCount, float yawStep, float pitchStep, float newNodeCost, float heuristicThreshold) {
    public static PathfindingCalculationContext simple(BlockPos startBlock, int threadCount) {
        return new PathfindingCalculationContext(startBlock.mutable(), threadCount, 2f, 2f, 100f, 0.5f);
    }

    public BlockPos.MutableBlockPos getMutableStart() {
        return this.startBlock;
    }
}
