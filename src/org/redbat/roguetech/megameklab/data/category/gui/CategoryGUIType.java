package org.redbat.roguetech.megameklab.data.category.gui;

public interface CategoryGUIType {
    static CategoryGUIType getCategoryGroup(CategoryGUIGroup categoryGUIGroup, String typeName) {
        CategoryGUIType[] values = getCategoryGroup(categoryGUIGroup);
        for (CategoryGUIType value : values) {
            if (value.getId().equals(typeName)) {
                return value;
            }
        }
        return null;
    }

    static CategoryGUIType[] getCategoryGroup(CategoryGUIGroup categoryGUIGroup) {
        switch (categoryGUIGroup) {
            case WEAPON:
                return WeaponCategoryGUIType.values();
            case AMMUNITION:
                return AmmunitionCategoryGUIType.values();
            case INTERNALS:
                return InternalComponentCategoryGUIType.values();
            case EQUIPMENT:
                return EquipmentCategoryGUIType.values();
            case ALL:
                return new CategoryGUIType[]{DefaultCategoryGUIType.ALL};
            default:
                throw new IllegalStateException("Unexpected value: " + categoryGUIGroup);
        }
    }

    String getId();
    String getName();
}
