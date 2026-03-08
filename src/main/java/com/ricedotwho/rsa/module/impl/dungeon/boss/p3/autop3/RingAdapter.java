package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.Argument;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.ArgumentManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.RingArgType;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type.*;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.rings.*;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.SubAction;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.SubActionManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.SubActionType;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.type.EdgeAction;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.type.JumpAction;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.type.LookAction;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.type.StopAction;
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
        RingType type = EnumUtils.getEnum(RingType.class, obj.get("type").getAsString().toUpperCase());
        Pos min = gson.fromJson(obj.get("min").getAsJsonObject(), posType);
        Pos max = gson.fromJson(obj.get("max").getAsJsonObject(), posType);

        ArgumentManager args = null;
        if (obj.has("args")) {
            args = deserializeArguments(obj.get("args"));
        }

        SubActionManager sub = null;
        if (obj.has("sub")) {
            sub = deserializeSubActions(obj.get("sub"));
        }

        return switch (type) {
            case BONZO -> new BonzoRing(min, max, obj.get("yaw").getAsFloat(), obj.get("pitch").getAsFloat(), args, sub);
            case FAST_BONZO -> new FastBonzoRing(min, max, obj.get("yaw").getAsFloat(), obj.get("pitch").getAsFloat(), args, sub);
            case JUMP -> new JumpRing(min, max, args, sub);
            case LOOK -> new LookRing(min, max, obj.get("yaw").getAsFloat(), obj.get("pitch").getAsFloat(), args, sub);
            case STOP -> new StopRing(min, max, args, sub);
            case WALK -> new WalkRing(min, max, obj.get("yaw").getAsFloat(), args, sub);
            case ALIGN -> new AlignRing(min, max, args, sub);
            case FAST_ALIGN -> new FastAlign(min, max, args, sub);
            case EDGE -> new EdgeRing(min, max, args, sub);
            case MOVEMENT -> new MovementRing(min, max, obj.get("route").getAsString(), args, sub);
            case BOOM -> new BoomRing(min, max, gson.fromJson(obj.get("target").getAsJsonObject(), posType), args, sub);
            case LEAP -> new LeapRing(min, max, args, sub);
            case null -> throw new IllegalStateException("Unexpected value: " + obj.get("type"));
        };
    }

    @Override
    public JsonElement serialize(Ring src, Type typeOfSrc, JsonSerializationContext context) {
        return src.serialize();
    }

    public ArgumentManager deserializeArguments(JsonElement json) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        HashMap<RingArgType, Argument> map = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            RingArgType type = EnumUtils.getEnum(RingArgType.class, entry.getKey());
            Argument condition = switch (type) {
                case GROUND -> new GroundArg();
                case LEAP -> new LeapArg(entry.getValue().getAsInt());
                case TERM -> new TermArg();
                case TRIGGER -> new TriggerArg();
                case DELAY -> new DelayArg(entry.getValue().getAsLong());
            };
            map.put(type, condition);
        }

        return new ArgumentManager(map);
    }

    public SubActionManager deserializeSubActions(JsonElement json) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        HashMap<SubActionType, SubAction> map = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            SubActionType type = EnumUtils.getEnum(SubActionType.class, entry.getKey());
            SubAction action = switch (type) {
                case LOOK -> {
                    JsonObject o = entry.getValue().getAsJsonObject();
                    yield new LookAction(o.get("yaw").getAsFloat(), o.get("pitch").getAsFloat());
                }
                case JUMP -> new JumpAction();
                case EDGE -> new EdgeAction();
                case STOP -> new StopAction();
            };
            map.put(type, action);
        }

        return new SubActionManager(map);
    }
}

