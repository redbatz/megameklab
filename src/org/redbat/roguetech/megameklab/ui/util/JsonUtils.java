package org.redbat.roguetech.megameklab.ui.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megameklab.data.CategoryManager;
import org.redbat.roguetech.megameklab.data.category.Category;
import org.redbat.roguetech.megameklab.data.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.redbat.roguetech.megameklab.ui.util.JsonConstants.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {
    public static boolean containsAnyField(JsonNode jsonNode, String... fieldNames) {
        if (jsonNode == null) {
            return false;
        }
        for (String fieldName : fieldNames) {
            if (jsonNode.has(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasMatchingField(JsonNode jsonNode, String key, String... matchingValues) {
        if (!jsonNode.has(key)) {
            return false;
        }
        String nodeValue = jsonNode.get(key).asText();
        return Arrays.asList(matchingValues).contains(nodeValue);
    }

    public static String getValueAsString(JsonNode jsonNode, String... ids) {
        JsonNode node = getNode(jsonNode, ids);
        if (node == null) {
            return null;
        }
        return node.asText();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getNode(JsonNode jsonNode, String... ids) {
        if (!hasField(jsonNode, ids)) {
            return null;
        }
        JsonNode node = jsonNode;
        for (String id : ids) {
            node = node.get(id);
        }
        return (T) node;
    }

    public static boolean hasField(JsonNode jsonNode, String... ids) {
        JsonNode currentNode = jsonNode;
        for (String id : ids) {
            if (currentNode.has(id)) {
                currentNode = currentNode.get(id);
            } else {
                return false;
            }
        }
        return true;
    }

    public static List<Category> getCategories(JsonNode node) {
        JsonNode customNode = node.get(CUSTOM_FIELD);
        if (customNode == null) {
            return Collections.emptyList();
        }
        JsonNode categoryNode = customNode.get(CATEGORY_FIELD);
        if (categoryNode == null) {
            return Collections.emptyList();
        }
        List<Category> categories = new ArrayList<>();
        for (JsonNode idNode : categoryNode.findValues(CATEGORY_ID_FIELD)) {
            String id = idNode.asText();
            if (id.startsWith("Linked_")) {
                continue;
            }
            Category category = CategoryRepository.find(id);
            if (category == null) {
                log.info("Unknown category {}", id);
                category = CategoryManager.createManualCategory(id);
            }
            categories.add(category);
        }
        return categories;
    }
}
