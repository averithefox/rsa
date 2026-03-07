package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitCondition;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitType;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitClick;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitEWRaytrace;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitSecrets;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings.*;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.FileUtils;
import org.apache.commons.lang3.EnumUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RingAdapter implements JsonDeserializer<Ring>, JsonSerializer<Ring> {
    private static final Type posType = new TypeToken<Pos>() {}.getType();
    private static final Gson gson = FileUtils.getGson();

    @Override
    public Ring deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        RingType type = EnumUtils.getEnum(RingType.class, obj.get("type").getAsString().toUpperCase(), null);
        Pos min = gson.fromJson(obj.get("min").getAsJsonObject(), posType);
        Pos max = gson.fromJson(obj.get("max").getAsJsonObject(), posType);

        return switch (type) {
            case JUMP -> new JumpRing(min, max);
            case LOOK -> new LookRing(min, max, obj.get("yaw").getAsFloat(), obj.get("pitch").getAsFloat());
            case STOP -> new StopRing(min, max);
            case WALK -> new WalkRing(min, max, obj.get("yaw").getAsFloat());
            case ALIGN -> new AlignRing(min, max);
            case FAST_ALIGN -> new FastAlign(min, max);
            case null -> throw new IllegalStateException("Unexpected value: " + obj.get("type"));
        };
    }

    @Override
    public JsonElement serialize(Ring src, Type typeOfSrc, JsonSerializationContext context) {
        return src.serialize();
    }
}

