package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.subactions.SubAction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public record ArgumentManager(HashMap<RingArgType, Argument> args) {
    public ArgumentManager() {
        this(new HashMap<>());
    }

    public boolean check() {
        for (Argument arg : args.values()) {
            if (!arg.check()) return true;
        }
        return false;
    }

    public Collection<Argument> getArgs() {
        return args.values();
    }

    public void addArg(Argument argument) {
        args.put(argument.getType(), argument);
    }

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        for (Argument arg : args.values()) {
            arg.serialize(obj);
        }
        return obj;
    }

    public String getList(String before) {
        if (args.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(before);
        if (!before.isBlank()) sb.append(", ");
        else sb.append("(");
        List<Argument> args = getArgs().stream().toList();
        for (int i = 0; i < args.size(); i++) {
            Argument arg = args.get(i);
            boolean last = i == args.size() - 1;
            sb.append(arg.stringValue());
            if (!last) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
