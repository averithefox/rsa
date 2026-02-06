package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.nodes;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.NodeRunState;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Pos;

public class BoomNode extends Node {
    public BoomNode(Pos localPos) {
        super(localPos);
    }


    @Override
    public boolean run(Pos playerPos) {
        return false;
    }

    @Override
    public void render() {
        
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
