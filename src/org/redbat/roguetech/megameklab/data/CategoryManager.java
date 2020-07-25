package org.redbat.roguetech.megameklab.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megameklab.data.category.Category;
import org.redbat.roguetech.megameklab.data.repository.CategoryRepository;
import org.redbat.roguetech.megameklab.file.JsonLoader;
import org.redbat.roguetech.megameklab.util.CConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.redbat.roguetech.megameklab.data.category.CategoryConstants.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryManager {

    public static void load() {
        loadCategories();
    }

    @SneakyThrows
    private static void loadCategories() {
        log.info("Loading categories");
        Path path = Paths.get(CConfig.getParam(CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY));
        if (!Files.isDirectory(path)) {
            log.error("Configured path is not a directory");
            return;
        }
        Path categoriesDirectory = path.resolve("RogueTech Core/categories");
        try (Stream<Path> files = Files.list(categoriesDirectory)) {
            for (Path filePath : files.collect(Collectors.toList())) {
                JsonNode loadedFile = JsonLoader.load(filePath, false);
                if (loadedFile == null) {
                    continue;
                }
                parseCategories(loadedFile);
            }
        }
        log.info("Finished loading {} categories", CategoryRepository.getCount());
    }

    private static void parseCategories(JsonNode loadedFile) {
        for (JsonNode categoryNode : loadedFile.get(SETTINGS_FIELD)) {
            String id = categoryNode.get(NAME_FIELD).asText();
            JsonNode nameNode = categoryNode.get(DISPLAY_NAME_FIELD);
            if (nameNode == null) {
                nameNode = categoryNode.get(DISPLAY_NAME_ALT_FIELD);
            }
            String name = nameNode.asText();
            Category category = Category.of(id, name);
            CategoryRepository.store(category);
        }
    }

    public static Category createManualCategory(String id) {
        Category category = Category.of(id, id);
        category.setManualCategory(true);
        CategoryRepository.store(category);
        return category;
    }
}
