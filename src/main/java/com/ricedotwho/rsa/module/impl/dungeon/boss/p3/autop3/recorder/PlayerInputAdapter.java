package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.recorder;

import com.google.gson.*;

import java.lang.reflect.Type;

public class PlayerInputAdapter implements JsonDeserializer<MovementRecorder.PlayerInput>, JsonSerializer<MovementRecorder.PlayerInput> {

    @Override
    public MovementRecorder.PlayerInput deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        return new MovementRecorder.PlayerInput(
                obj.get("yaw").getAsFloat(),
                obj.get("pitch").getAsFloat(),
                obj.get("forward").getAsBoolean(),
                obj.get("backward").getAsBoolean(),
                obj.get("left").getAsBoolean(),
                obj.get("right").getAsBoolean(),
                obj.get("jump").getAsBoolean(),
                obj.get("sneak").getAsBoolean(),
                obj.get("sprint").getAsBoolean()
        );
    }

    @Override
    public JsonElement serialize(MovementRecorder.PlayerInput src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("yaw", src.yaw());
        obj.addProperty("pitch", src.pitch());
        obj.addProperty("forward", src.forward());
        obj.addProperty("backward", src.back());
        obj.addProperty("left", src.left());
        obj.addProperty("right", src.right());
        obj.addProperty("jump", src.jump());
        obj.addProperty("sneak", src.sneak());
        obj.addProperty("sprint", src.sprint());
        return obj;
    }
}

