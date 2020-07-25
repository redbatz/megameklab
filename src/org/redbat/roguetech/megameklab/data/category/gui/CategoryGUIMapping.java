package org.redbat.roguetech.megameklab.data.category.gui;

import lombok.Value;
import org.redbat.roguetech.megameklab.data.component.ComponentType;
import org.redbat.roguetech.megameklab.data.category.Category;

import java.util.List;

@Value(staticConstructor = "of")
public class CategoryGUIMapping {
    List<ComponentType> componentTypesOverrides;
    List<Category> includeCategories;
    List<Category> excludeCategories;
}
