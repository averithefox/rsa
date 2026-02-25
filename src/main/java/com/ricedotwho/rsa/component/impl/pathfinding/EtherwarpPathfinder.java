package com.ricedotwho.rsa.component.impl.pathfinding;

import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsa.component.impl.pathfinding.openset.BinaryHeapOpenSet;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static java.lang.Math.cos;

public class EtherwarpPathfinder {
    public final static double MIN_IMPROVEMENT = 1d;

    private final Goal goal;
    private final PathfindingCalculationContext context;
    private boolean solved = false;
    private Path path;

    private PathNode bestNode;
    private CachedPath bestCachedPath;

    private final BinaryHeapOpenSet nodes;
    private final HashSet<Integer> processing;
    private final HashMap<Long, PathNode> cache;


    public EtherwarpPathfinder(PathfindingCalculationContext context, Goal goal) {
        this.goal = goal;
        this.context = context;
        this.cache = new HashMap<>();
        this.nodes = new BinaryHeapOpenSet(context.newNodeCost());
        this.processing = new HashSet<>();
    }

    public Path calculate() {
        if (solved) return path;

        if (!goal.isPossible()) {
            RSA.chat("Goal is impossible!");
            return null;
        }

        long time = System.currentTimeMillis();

        PathNode startNode = new PathNode(context.startBlock(), null, goal);

        // Important for recognition of new nodes, removing may lead to worse performance
        startNode.setYaw(Float.MAX_VALUE);
        startNode.setPitch(Float.MAX_VALUE);

        this.nodes.insert(startNode);
        this.bestNode = startNode;

        for (int i = 0; i < context.threadCount() - 1; i ++) {
            Thread thread = new Thread(this::run);
            thread.start();
        }

        this.run();

        RSA.chat("Found path! Took " + (System.currentTimeMillis() - time) + "ms!");
        this.path = new Path(context.startBlock(), startNode, bestNode, goal);
        this.solved = true;
        return this.path;
    }

    private void run() {
        while (!this.isComplete()) { //!this.isDone()
            checkNode(getLowest());
        }
    }

    private void checkNode(PathNode checkNode) {
        if (checkNode == null) return;
        double moveCost = checkNode.getMoveCost(context.newNodeCost());
        if (goal.test(checkNode.getPos())) {
            RSA.chat("Found valid route length " + checkNode.getIndex());
            if (!isComplete() || moveCost < getBestNodeMoveCost()) setBestNode(checkNode);
        }

        if (isComplete() && moveCost >= getBestNodeMoveCost()) {
            finishNode(checkNode);
            return;
        }

        if (isComplete() && checkNode.getHeuristicCost() >= getBestHeuristicByIndex(checkNode.getIndex()) * context.heuristicThreshold()) {
            finishNode(checkNode);
            return;
        }

        double newCost = moveCost + context.newNodeCost();

        consumeRaycastBlocks(checkNode, (neighborNode, yaw, pitch) -> {
            if (!neighborNode.hasBeenScanned() || neighborNode.getMoveCost(context.newNodeCost()) - newCost > MIN_IMPROVEMENT) {
                neighborNode.updateParent(checkNode);
                neighborNode.setYaw(yaw);
                neighborNode.setPitch(pitch);

                // If was in heap move it up
                if (neighborNode.isOpen()) {
                    updateNodes(neighborNode);
                } else {
                    // Else add it back
                    insertNodes(neighborNode);
                }
                if (!isComplete() && getBestNodeHeuristic() - neighborNode.getHeuristicCost() > MIN_IMPROVEMENT) {
                    if (neighborNode.getMoveCost(context.newNodeCost()) < getBestNodeMoveCost())
                        setBestNode(neighborNode);
                }

            }
        });
        finishNode(checkNode);
    }

    public void cancel() {
        this.solved = true;
    }

    private synchronized void updateNodes(PathNode node) {
        nodes.update(node);
    }

    private synchronized void insertNodes(PathNode node) {
        nodes.insert(node);
    }


    private synchronized double getBestHeuristicByIndex(int index) {
        PathNode node = bestCachedPath.getByIndex(index);
        return node == null ? Double.MAX_VALUE : node.getHeuristicCost();
    }

    private synchronized PathNode getLowest() {
        if (nodes.isEmpty()) return null;
        PathNode lowest = nodes.removeLowest();
        processing.add(lowest.hashCode());
        return lowest;
    }

    private synchronized boolean isDone() {
        return nodes.isEmpty() && processing.isEmpty();
    }

    private synchronized void finishNode(PathNode node) {
        if (!processing.contains(node.hashCode())) {
            //System.err.println("Found node not in processing!");
            return;
        }
        processing.remove(node.hashCode());
    }

    private synchronized boolean isComplete() {
        return this.solved;
    }

    private synchronized double getBestNodeMoveCost() {
        return bestNode.getMoveCost(context.newNodeCost());
    }

    private synchronized double getBestNodeHeuristic() {
        return this.bestNode.getHeuristicCost();
    }

    private synchronized void setBestNode(PathNode node) {
        if (this.solved) {
            if (node.getMoveCost(context.newNodeCost()) < bestNode.getMoveCost(context.newNodeCost())) {
                bestNode = node;
                bestCachedPath = new CachedPath(node);
            }
            return;
        }

        if (goal.test(node.getPos())) {
            bestNode = node;
            bestCachedPath = new CachedPath(node);
            this.solved = true;
            return;
        }

        if (bestNode.getHeuristicCost() - node.getHeuristicCost() > MIN_IMPROVEMENT) {
            if (bestNode.getMoveCost(context.newNodeCost()) < getBestNodeMoveCost())
                this.bestNode = node;
        }
    }

    public synchronized PathNode getNodeAt(BlockPos pos, long hashcode, PathNode parent) {
        PathNode node = cache.get(hashcode);
        if (node == null) {
            node = new PathNode(pos, parent, goal);
            cache.put(hashcode, node);
        }

//        if (!node.getPos().equals(pos)) {
//            ChatUtils.chat("HASHCODE ERROR!");
//            ChatUtils.chat("Hash1 : " + hashcode + ", Hash2 " + node.hashCode() + " pos 1 : " + pos + " pos 2 : " + node.getPos());
//        }

        return node;
    }


    private void consumeRaycastBlocks(PathNode parent, TriConsumer<PathNode, Float, Float> consumer) {
        HashSet<Integer> blockPosCache = new HashSet<>();
        // The first node might be wrong, because the player isin't actually at +0.05 offset
        // This might break since we aren't using vec3 anymore for DynEtherNode
        Vec3 eyePos = new Vec3(parent.getPos().getX() + 0.5, parent.getPos().getY() + 1.05 + EtherUtils.SNEAK_EYE_HEIGHT, parent.getPos().getZ() + 0.5);

        for (float pitch = -90f; pitch <= 90f; pitch += context.pitchStep()) {
            float pitchRadians = (float) Math.toRadians(pitch);
            float yawStepAtThisPitch = context.yawStep() / Math.max(0.01f, (float) cos(pitchRadians)); // avoid div/0

            for (float yaw = 0f; yaw < 360f; yaw += yawStepAtThisPitch) {
                BlockPos etherPos = EtherUtils.fastGetEtherFromOrigin(eyePos, yaw, pitch, 61);
                if (etherPos == null) continue;
                int hash = PathNode.hashCode(etherPos);
                if (!blockPosCache.add(hash)) continue;
                //ChatUtils.chat("Found new node!");

                PathNode node = getNodeAt(etherPos, hash, parent);
                consumer.accept(node, yaw, pitch);
            }
        }
    }


}
