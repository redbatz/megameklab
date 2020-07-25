package org.redbat.roguetech.megameklab.data.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.redbat.roguetech.megameklab.data.component.type.Component;
import org.redbat.roguetech.megameklab.data.component.value.ComponentValuePopulator;
import org.redbat.roguetech.megameklab.file.FileConstants;
import org.redbat.roguetech.megameklab.file.FileLoader;
import org.redbat.roguetech.megameklab.file.JsonLoader;
import org.redbat.roguetech.megameklab.ui.util.JsonConstants;
import org.redbat.roguetech.megameklab.ui.util.JsonUtils;
import org.redbat.roguetech.megameklab.util.CConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static org.redbat.roguetech.megameklab.ui.util.JsonConstants.*;
import static org.redbat.roguetech.megameklab.ui.util.JsonUtils.getCategories;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentLoader {

    private static final Set<String> PATH_BLACKLIST = newHashSet("ExperimentalWeapons", "Turrets");
    private static final String LOOTABLE_PREFIX = "lootable_";
    private static final String DEPRECATED_SUFFIX = "(Deprecated)";

    public static List<ComponentWrapper> load() {
        log.info("Loading components");
        Path path = Paths.get(CConfig.getParam(CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY));
        if (!Files.isDirectory(path)) {
            log.error("Configured path is not a directory");
            return Collections.emptyList();
        }
        List<ComponentWrapper> data = FileLoader.loadFiles(path, ComponentLoader::componentFileFilter, ComponentLoader::createComponent);
        log.info("Found {} components", data.size());
        return data;
    }

    private static ComponentWrapper createComponent(Path path) {
        JsonNode node = JsonLoader.load(path, true);
        ComponentType componentType = ComponentType.getComponentType(node.get(COMPONENT_TYPE_FIELD).asText());

        Component component = ComponentFactory.createComponent(componentType);

        JsonNode description = node.get(DESCRIPTION_FIELD);
        String id = description.get(ID_FIELD).asText();
        String name = description.get(NAME_FIELD).asText();

        component.setId(id);
        component.setInternalName(id);
        component.setName(name);
        component.setCategories(getCategories(node));
        component.setComponentType(componentType);
        component.setTags(getTags(node));
        if (id.startsWith(LOOTABLE_PREFIX)) {
            component.setLootable(true);
        }
        if (name.endsWith(DEPRECATED_SUFFIX)) {
            component.setDeprecated(true);
        }
        JsonNode uiName = description.get(UI_NAME_FIELD);
        if (uiName != null) {
            component.setUiName(uiName.asText());
            component.addLookupName(uiName.asText());
            if (uiName.asText().endsWith(DEPRECATED_SUFFIX)) {
                component.setDeprecated(true);
            }
        } else {
            component.setUiName(StringUtils.EMPTY);
        }
        
        ComponentValuePopulator componentValuePopulator = ComponentFactory.getComponentValuePopulator(componentType);
        componentValuePopulator.populateValues(component, node);
        return ComponentWrapper.of(component, node, path);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static List<String> getTags(JsonNode node) {
        ArrayNode arrayNode = JsonUtils.getNode(node, COMPONENT_TAGS_FIELD, ITEMS_FIELD);
        if (arrayNode == null) {
            return Collections.emptyList();
        }
        return Streams.stream(arrayNode.iterator()).map(JsonNode::asText).collect(Collectors.toList());
    }

    private static boolean componentFileFilter(Path path) {
        String filename = path.getFileName().toString();
        if (!filename.endsWith(FileConstants.SUFFIX_JSON)) {
            return false;
        }
        for (Path subPath : path.toAbsolutePath()) {
            String name = subPath.toString();
            if (PATH_BLACKLIST.contains(name)) {
                return false;
            }
        }
        JsonNode jsonNode = JsonLoader.load(path, false);
        if (jsonNode == null) {
            return false;
        }
        if (!JsonUtils.hasMatchingField(jsonNode, COMPONENT_TYPE_FIELD, ComponentType.WEAPON.getName(), ComponentType.AMMUNITION_BOX.getName(), ComponentType.HEAT_SINK.getName(), ComponentType.JUMP_JET.getName(), ComponentType.UPGRADE.getName())) {
            return false;
        }
        return true;
    }


}
