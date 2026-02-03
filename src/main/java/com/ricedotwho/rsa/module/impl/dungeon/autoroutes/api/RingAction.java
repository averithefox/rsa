package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api;

import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class RingAction implements Accessor {

    public final String name;

    public abstract boolean executeAction(Pos pos, Rotation rot);

}