package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitCondition;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.EtherwarpNode;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.EtherUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class AwaitEWRaytrace extends AwaitCondition<EtherwarpNode> {


    public AwaitEWRaytrace() {

    }

    public boolean test(Node node) {
        if (!(node instanceof EtherwarpNode etherwarpNode)) return true;
        Pos eyePos = etherwarpNode.getRealPos().add(0d, EtherUtils.SNEAK_EYE_HEIGHT, 0d);
        Pos viewVector = etherwarpNode.getRealTargetPos().subtract(eyePos).normalize();
        float[] angles = EtherUtils.getYawAndPitch(viewVector.x, viewVector.y, viewVector.z);

        Vec3 vec = EtherUtils.rayTraceBlock(61, angles[0], angles[1], eyePos.asVec3());
        viewVector = viewVector.multiply(EtherUtils.EPSILON).selfAdd(vec.x, vec.y, vec.z);
        BlockPos blockPos = BlockPos.containing(viewVector.x, viewVector.y, viewVector.z);
        return blockPos.equals(BlockPos.containing(etherwarpNode.getRealTargetPos().asVec3()));
    }



    @Override
    public void onEnter() {

    }

    @Override
    public void reset() {

    }

    public void serialize(JsonObject json) {
        json.addProperty("awaitEWRaytrace", true);
    }


    protected void consume(EtherwarpNode node) {
        // Never called
    }

}
