package org.redbat.roguetech.megameklab.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redbat.roguetech.megameklab.data.category.Category;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIMapping;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIType;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIGroup;
import org.redbat.roguetech.megameklab.data.component.ComponentType;
import org.redbat.roguetech.megameklab.data.repository.CategoryRepository;
import org.redbat.roguetech.megameklab.data.repository.CategoryGUIMappingsRepository;
import org.redbat.roguetech.megameklab.file.JsonLoader;
import org.redbat.roguetech.megameklab.util.CConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.redbat.roguetech.megameklab.data.category.CategoryConstants.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryGUIMappingsManager {

    public static void load() {
        loadCategoryMappings();
    }

    private static void loadCategoryMappings() {
        log.info("Loading category UI mappings");
        Path path = Paths.get(CConfig.getParam(CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY));
        if (!Files.isDirectory(path)) {
            log.error("Configured path is not a directory");
            return;
        }
        Path customFiltersFile = path.resolve("CustomFilters/mod.json");
        JsonNode rootNode = JsonLoader.load(customFiltersFile, false);
        JsonNode tabsNode = rootNode.get(SETTINGS_FIELD).get(TABS_FIELD);
        for (JsonNode typeNode : tabsNode) {
            handleType(typeNode);
        }
        log.info("Finished loading category UI mappings");
    }

    private static void handleType(JsonNode typeNode) {
        CategoryGUIGroup categoryGUIGroup = CategoryGUIGroup.getCategorySection(typeNode.get(CAPTION_FIELD).asText());
        for (JsonNode buttonNode : typeNode.get(BUTTONS_FIELD)) {
            JsonNode tooltipNode = buttonNode.get(TOOLTIP_FIELD);
            if (tooltipNode == null) {
                continue;
            }
            String typeName = StringUtils.removeStart(tooltipNode.asText(), SHOW_PREFIX);
            CategoryGUIType categoryGUIType = CategoryGUIType.getCategoryGroup(categoryGUIGroup, typeName);
            if (categoryGUIType == null) {
                continue;
            }
            List<Category> includeCategories = parseCategories(buttonNode, CATEGORIES_FIELD);
            List<Category> excludeCategories = parseCategories(buttonNode, NOT_CATEGORIES_FIELD);
            List<ComponentType> componentType = parseComponentTypeOverrides(buttonNode);
            CategoryGUIMapping mapping = CategoryGUIMapping.of(componentType, includeCategories, excludeCategories);
            CategoryGUIMappingsRepository.store(categoryGUIGroup, categoryGUIType, mapping);

        }
    }

    private static List<ComponentType> parseComponentTypeOverrides(JsonNode buttonNode) {
        JsonNode typesNode = buttonNode.findValue(COMPONENT_TYPES_FIELD);
        if (typesNode == null) {
            return Collections.emptyList();
        }
        List<ComponentType> types = new ArrayList<>();
        for (JsonNode jsonNode : typesNode) {
            ComponentType categoryType = ComponentType.getComponentType(jsonNode.asText());
            types.add(categoryType);
        }
        return types;
    }

    private static List<Category> parseCategories(JsonNode buttonNode, String categoriesFieldKey) {
        List<Category> includeCategories = new ArrayList<>();
        JsonNode categoriesNode = buttonNode.findValue(categoriesFieldKey);
        if (categoriesNode != null) {
            for (JsonNode categoryNode : categoriesNode) {
                Category category = CategoryRepository.find(categoryNode.asText());
                includeCategories.add(category);
            }
        }
        return includeCategories;
    }

    public static Collection<CategoryGUIMapping> get(CategoryGUIGroup categoryGUIGroup) {
        return CategoryGUIMappingsRepository.findByGroup(categoryGUIGroup);
    }
}
