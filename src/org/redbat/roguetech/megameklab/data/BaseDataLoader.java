package org.redbat.roguetech.megameklab.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redbat.roguetech.megameklab.data.type.DataType;
import org.redbat.roguetech.megameklab.data.type.LoadableData;
import org.redbat.roguetech.megameklab.file.FileConstants;
import org.redbat.roguetech.megameklab.file.FileLoader;
import org.redbat.roguetech.megameklab.file.JsonLoader;
import org.redbat.roguetech.megameklab.util.CConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.*;

@Slf4j
public class BaseDataLoader {

    protected static final JsonLoader jsonLoader = new JsonLoader();

    protected static <T extends LoadableData> void doLoad(Class<T> dataClass, BiPredicate<Path, JsonNode> filter, Supplier<T> supplier, BiFunction<T, JsonNode, T> valuePopulator) {
        String dataTypeName = DataType.getDataType(dataClass).getName();
        log.info("Loading {} files", dataTypeName);
        Path path = Paths.get(CConfig.getParam(CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY));
        if (!Files.isDirectory(path)) {
            log.error("Configured path is not a directory");
            return;
        }
        Predicate<Path> actualFilter = mergeFilters(filter);
        Function<Path, T> dataCreateFunction = buildDataCreateFunction(supplier, valuePopulator);
        List<T> weapons = FileLoader.loadFiles(path, actualFilter, dataCreateFunction);
        DataManager.getRepository(dataClass).storeAll(weapons);
        log.info("Found {} {} files", weapons.size(), dataTypeName);
    }

    protected static Predicate<Path> mergeFilters(BiPredicate<Path, JsonNode> filter) {
        return path -> {
            String filename = path.getFileName().toString();
            if (!filename.endsWith(FileConstants.SUFFIX_JSON)) {
                return false;
            }
            JsonNode jsonNode = jsonLoader.load(path, true);
            if (jsonNode == null) {
                return false;
            }
            return filter.test(path, jsonNode);
        };
    }

    protected static <T extends LoadableData> Function<Path, T> buildDataCreateFunction(Supplier<T> createFunction, BiFunction<T, JsonNode, T> valuePopulator) {
        return path -> {
            JsonNode node = jsonLoader.load(path, true);
            T createdObject = createFunction.get();

            JsonNode description = node.findValue("Description");
            String id = description.get("Id").asText();

            createdObject.setId(id);
            createdObject.setInternalName(id);
            createdObject.setName(description.get("Name").asText());
            JsonNode uiName = description.get("UIName");
            if (uiName != null) {
                createdObject.setUiName(uiName.asText());
                createdObject.addLookupName(uiName.asText());
            } else {
                createdObject.setUiName(StringUtils.EMPTY);
            }
            return valuePopulator.apply(createdObject, node);
        };
    }
}
