package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.Argument;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.RingArgType;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import net.minecraft.util.Mth;

public class SectionArg extends Argument<Boolean> {
    private final Phase7 section;
    private boolean override = false;

    public SectionArg(int players) {
        super(RingArgType.SECTION);
        this.section = DungeonUtils.getSectionFromI(Mth.clamp(players, 0, 3));
    }

    @Override
    public boolean check() {
        if (override) {
            override = false;
            return true;
        }
        return Dungeon.getP3Section() == this.section;
    }

    @Override
    public void consume(Boolean bl) {
        override = true;
    }

    @Override
    public void reset() {
        override = false;
    }

    public static SectionArg create(String arg) {
        return new SectionArg(NumberUtils.isInteger(arg) ? Integer.parseInt(arg) : 0);
    }

    public void serialize(JsonObject json) {
        json.addProperty(getType().name(), section.name());
    }

    @Override
    public String stringValue() {
        return "section " + section.name();
    }

}
