package org.redbat.roguetech.megameklab.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megameklab.data.component.ComponentLoader;
import org.redbat.roguetech.megameklab.data.component.ComponentType;
import org.redbat.roguetech.megameklab.data.component.ComponentWrapper;
import org.redbat.roguetech.megameklab.data.component.type.Component;
import org.redbat.roguetech.megameklab.data.model.Location;
import org.redbat.roguetech.megameklab.data.repository.ComponentRepository;
import org.redbat.roguetech.megameklab.ui.util.JsonUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.redbat.roguetech.megameklab.ui.util.JsonConstants.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComponentManager {

    public static void load() {
        log.info("Start loading of all data files");
        List<ComponentWrapper> data = ComponentLoader.load();
        applyLinks(data);
        ComponentRepository.storeAll(data.stream().map(ComponentWrapper::getComponent).collect(Collectors.toList()));
        log.info("Data loading finished");
    }

    private static void applyLinks(List<ComponentWrapper> data) {
        log.debug("Parsing {} components for links", data.size());
        Map<String, Component> components = data.stream().map(ComponentWrapper::getComponent).collect(Collectors.toMap(Component::getId, Function.identity()));
        for (ComponentWrapper componentWrapper : data) {
            JsonNode jsonNode = componentWrapper.getJsonNode();
            ArrayNode linksNode = JsonUtils.getNode(jsonNode, CUSTOM_FIELD, LINKED_FIELD, LINKS_FIELD);
            if (linksNode == null) {
                return;
            }
            Component component = componentWrapper.getComponent();
            for (JsonNode linkNode : linksNode) {
                String componentDefId = linkNode.get(COMPONENT_DEF_ID_FIELD).asText();
                Location location = Location.getLocation(linkNode.get(LOCATION_FIELD).asText());
                Component linkedComponent = components.get(componentDefId);
                if (linkedComponent == null) {
                    log.error("Unable to find linked component {}", componentDefId);
                    continue;
                }
                linkedComponent.setLinked(true);
                component.addLinkedComponent(location, linkedComponent);
            }
        }
    }

    public static Collection<Component> getAll(ComponentType... types) {
        if (types == null || types.length == 0) {
            return ComponentRepository.findAll();
        }
        return Arrays.stream(types)
                .map(ComponentRepository::findAllByType)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
