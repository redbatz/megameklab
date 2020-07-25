package org.redbat.roguetech.megameklab.data.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redbat.roguetech.megameklab.data.category.Category;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryRepository {
    private static final Map<String, Category> dataStorage = new ConcurrentHashMap<>();

    public static void storeAll(Collection<Category> loadableData) {
        loadableData.forEach(data -> dataStorage.put(data.getId(), data));
    }

    public static Collection<Category> findAll() {
        return dataStorage.values();
    }

    public static void store(Category category) {
        dataStorage.put(category.getId(), category);
    }

    public static int getCount() {
        return dataStorage.size();
    }

    public static Category find(String id) {
        return dataStorage.get(id);
    }
}
