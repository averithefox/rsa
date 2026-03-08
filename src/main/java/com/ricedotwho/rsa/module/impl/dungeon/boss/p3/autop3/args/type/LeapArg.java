package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.Argument;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.RingArgType;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.utils.NumberUtils;
import net.minecraft.util.Mth;

public class LeapArg extends Argument {
    private final int players;

    public LeapArg(int players) {
        super(RingArgType.LEAP);
        this.players = Mth.clamp(players, 0, 5);
    }

    @Override
    public boolean check() {
        return Dungeon.getPlayersLeapt() >= players;
    }

    public static LeapArg create(String arg) {
        return new LeapArg(NumberUtils.isInteger(arg) ? Integer.parseInt(arg) : 1);
    }

    public void serialize(JsonObject json) {
        json.addProperty(getType().name(), players);
    }

    @Override
    public String stringValue() {
        return "leap " + players;
    }

}
