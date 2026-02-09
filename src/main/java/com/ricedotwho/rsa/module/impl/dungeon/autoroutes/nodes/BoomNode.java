package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsm.data.Pos;

public class BoomNode extends Node {
    public BoomNode(Pos localPos, AwaitManager awaits) {
        super(localPos, awaits);
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

    @Override
    public String getName() {
        return "boom";
    }
}
