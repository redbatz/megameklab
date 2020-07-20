package org.redbat.roguetech.megameklab.file;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megameklab.ui.util.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JsonLoader {

    private final JsonMapper jsonMapper = new JsonMapper();

    private final static Map<Path, JsonNode> nodeMap = new ConcurrentHashMap<>();

    public JsonNode load(Path path, boolean useCachedValue) {
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
