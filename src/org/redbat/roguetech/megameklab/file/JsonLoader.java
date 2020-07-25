package org.redbat.roguetech.megameklab.file;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megameklab.ui.util.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonLoader {

    private static JsonLoader instance;
    private final Map<Path, JsonNode> nodeMap = new ConcurrentHashMap<>();
    private final JsonMapper jsonMapper = new JsonMapper();

    private static JsonLoader getInstance() {
        if (instance == null) {
            instance = new JsonLoader();
        }
        return instance;
    }

    public static JsonNode load(Path path, boolean useCachedValue) {
        JsonLoader instance = getInstance();
        return instance.doLoad(path, useCachedValue);
    }

    private JsonNode doLoad(Path path, boolean useCachedValue) {
        if (useCachedValue && nodeMap.containsKey(path)) {
            return nodeMap.get(path);
        }
        try {
            JsonNode node = jsonMapper.readValue(Files.newBufferedReader(path), JsonNode.class);
            nodeMap.put(path, node);
            return node;
        } catch (IOException e) {
            log.debug("Unable to read JSON file {}", path.toString(), e);
            return null;
        }
    }
}
