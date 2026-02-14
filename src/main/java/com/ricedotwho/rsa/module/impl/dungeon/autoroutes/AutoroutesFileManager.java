package com.ricedotwho.rsa.module.impl.dungeon.autoroutes;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitClick;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitEWRaytrace;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.awaits.AwaitSecrets;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes.*;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class AutoroutesFileManager {
    private static AutoRoutes autoRoutes;
    public static Gson gson;

    private static File autoRoutesDir;
    private static File routesFile;


    public static void save() {
        try {
            if (!routesFile.exists() && !routesFile.createNewFile()) return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        JsonObject jsonObject = new JsonObject();
        autoRoutes.getSavedNodes().forEach((name, nodes) -> {
            jsonObject.add(name, saveRoom(nodes));
        });

        try {
            FileWriter writer = new FileWriter(routesFile, false);
            writer.write(gson.toJson(jsonObject));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private static JsonArray saveRoom(List<Node> nodes) {
        JsonArray array = new JsonArray(nodes.size());
        nodes.forEach(node -> {
            array.add(node.serialize());
        });
        return array;
    }

    public static boolean load() {
        if (!routesFile.isFile()) return false;

        String json;
        try {
            json = new String(Files.readAllBytes(routesFile.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Map<String, List<Node>> nodes;
        try {
            nodes = deserialize(json);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (nodes == null || nodes.isEmpty()) return false;

        autoRoutes.getSavedNodes().clear();
        autoRoutes.getSavedNodes().putAll(nodes);
        autoRoutes.reload();
        return true;
    }

    // This is terrible
    // Ik
    // But idc, I hate this
    private static Map<String, List<Node>> deserialize(String json) throws JsonParseException {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Map<String, List<Node>> loaded = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String roomName = entry.getKey();
            JsonArray nodeArray = entry.getValue().getAsJsonArray();

            List<Node> nodes = new ArrayList<>();

            for (JsonElement nodeEl : nodeArray) {
                nodes.add(deserializeNode(nodeEl.getAsJsonObject()));
            }

            loaded.put(roomName, nodes);
        }
        return loaded;
    }

    private static Node deserializeNode(JsonObject jsonObject) {
        AwaitManager awaits = deserializeAwaits(jsonObject);
        Pos localPos = deserializePosition(jsonObject.getAsJsonObject("localPos"));
        float radius = jsonObject.get("radius").getAsFloat();
        boolean start = jsonObject.get("start").getAsBoolean();

        String type = jsonObject.get("type").getAsString();
        switch (type) {
            case "etherwarp" -> {
                return new EtherwarpNode(localPos, deserializePosition(jsonObject.getAsJsonObject("localTarget")), awaits, start);
            }

            case "bat" -> {
                return new BatNode(localPos, jsonObject.get("yaw").getAsFloat(), jsonObject.get("pitch").getAsFloat(), awaits, start);
            }

            case "boom" -> {
                return new BoomNode(localPos, deserializePosition(jsonObject.getAsJsonObject("target")), awaits, start);
            }

            case "aotv" -> {
                return new AotvNode(localPos, deserializePosition(jsonObject.getAsJsonObject("rotationVec")), awaits, start);
            }

            case "break" -> {
                return new BreakNode(localPos, gson.fromJson(jsonObject.getAsJsonObject("blocks"), new TypeToken<ArrayList<Pos>>() {}.getType()), start);
            }

            case "use" -> {
                return new UseNode(localPos, deserializePosition(jsonObject.getAsJsonObject("rotationVec")), jsonObject.get("itemID").getAsString(), jsonObject.get("sneak").getAsBoolean(), awaits, start);
            }
        }
        throw new IllegalStateException("Invalid node type!");
    }

    private static Pos deserializePosition(JsonObject jsonObject) {
        return new Pos(jsonObject.get("x").getAsDouble(), jsonObject.get("y").getAsDouble(), jsonObject.get("z").getAsDouble());
    }

    private static AwaitManager deserializeAwaits(JsonObject jsonObject) {
        if (!jsonObject.has("awaits")) return null;
        List<AwaitCondition<?>> conditions = new ArrayList<>();
        for (Map.Entry<String, JsonElement> map : jsonObject.getAsJsonObject("awaits").entrySet()) {
            String name = map.getKey();
            switch (name) {
                case "awaitClick" -> {
                    conditions.add(new AwaitClick());
                    break;
                }
                case "awaitSecrets" -> {
                    conditions.add(new AwaitSecrets(map.getValue().getAsInt()));
                    break;
                }
                case "awaitEWRaytrace" -> {
                    conditions.add(new AwaitEWRaytrace());
                    break;
                }
            }
        }
        return new AwaitManager(conditions);
    }

    public static void init(AutoRoutes routes) {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(AwaitCondition.class, new AwaitConditionSerializer())
                .create();
        autoRoutes = routes;
        autoRoutesDir = FileUtils.getCategoryFolder("routes");
        routesFile = new File(autoRoutesDir, "routes.json");
        createBackup();
    }

    private static void createBackup() {
        if (!routesFile.isFile()) return;

        File backUpDir = new File(autoRoutesDir, "backup");
        if (!backUpDir.mkdir()) return;
        List<Long> timeStamps = new ArrayList<>();

        for (File file : backUpDir.listFiles()) {
            String name = file.getName();
            if (!name.endsWith(".json.bak")) continue;
            String timeString = name.substring(0, name.length() - 9);
            if (timeString.isEmpty()) continue;
            try {
                timeStamps.add(Long.parseLong(timeString));
            } catch (NumberFormatException e) {
                continue;
            }
        }

        pruneBackups(backUpDir, timeStamps, 9);

        File newBackup = new File(backUpDir, System.currentTimeMillis() + ".json.bak");
        try {
            Files.copy(routesFile.toPath(), newBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to make routes backup!");
            return;
        }
        System.out.println("Made autoroutes backup!");
    }

    private static void pruneBackups(File backUpDir, List<Long> timeStamps, int maxSize) {
        if (timeStamps.size() <= maxSize) return;

        // Increasing order
        timeStamps.sort(Long::compareTo);

        for (int i = 0; i < timeStamps.size() - maxSize; i++) {
            Long ts = timeStamps.get(i);
            File file = new File(backUpDir, ts + ".json.bak");

            if (file.exists() && !file.delete()) {
                System.err.println("Failed to delete old backup: " + file.getName());
            }
        }
    }

    public static class AwaitConditionSerializer implements JsonSerializer<AwaitCondition<?>> {

        private static final Gson DELEGATE = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        @Override
        public JsonElement serialize(
                AwaitCondition<?> src,
                Type typeOfSrc,
                JsonSerializationContext context
        ) {
            return DELEGATE.toJsonTree(src);
        }
    }


}
