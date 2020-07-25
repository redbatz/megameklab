package org.redbat.roguetech.megameklab.data.repository;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIMapping;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIType;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIGroup;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryGUIMappingsRepository {
    private static final Table<CategoryGUIGroup, CategoryGUIType, CategoryGUIMapping> dataStorage = Tables.synchronizedTable(HashBasedTable.create());

    public static void store(CategoryGUIGroup categoryGUIGroup, CategoryGUIType categoryGUIType, CategoryGUIMapping value) {
        dataStorage.put(categoryGUIGroup, categoryGUIType, value);
    }

    public static CategoryGUIMapping find(CategoryGUIGroup categoryGUIGroup, CategoryGUIType categoryGUIType) {
        return dataStorage.get(categoryGUIGroup, categoryGUIType);
    }

    public static Collection<CategoryGUIMapping> findByGroup(CategoryGUIGroup categoryGUIGroup) {
        return dataStorage.row(categoryGUIGroup).values();
    }
}
