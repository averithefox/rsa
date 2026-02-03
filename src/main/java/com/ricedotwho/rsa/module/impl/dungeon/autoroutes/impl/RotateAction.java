package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.impl;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.RingAction;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import com.ricedotwho.rsm.utils.RotationUtils;

public class RotateAction extends RingAction {

    public RotateAction() {
        super("rotate");
    }

    @Override
    public boolean executeAction(Pos pos, Rotation rot) {
        float yaw = rot.getYaw();
        float pitch = rot.getPitch();

        mc.player.setYRot(RotationUtils.wrapAngleTo180(yaw));
        mc.player.setXRot(Math.clamp(pitch, -90, 90));

        return true;
    }

}