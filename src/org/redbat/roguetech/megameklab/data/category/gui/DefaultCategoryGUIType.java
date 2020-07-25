package org.redbat.roguetech.megameklab.data.category.gui;

import lombok.Getter;

@Getter
public enum DefaultCategoryGUIType implements CategoryGUIType {
    ALL("All", "All"), NONE("None", "               ");

    private final String id;
    private final String name;

    DefaultCategoryGUIType(String id, String name) {

        this.id = id;
        this.name = name;
    }
}
