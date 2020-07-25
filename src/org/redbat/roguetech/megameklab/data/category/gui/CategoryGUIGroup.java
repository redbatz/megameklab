package org.redbat.roguetech.megameklab.data.category.gui;

import lombok.Getter;
import org.redbat.roguetech.megameklab.data.component.ComponentType;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Getter
public enum CategoryGUIGroup {
    ALL("All", "All"),
    WEAPON("WEAPON", "Weapon", ComponentType.WEAPON),
    AMMUNITION("AMMO", "Ammunition", ComponentType.AMMUNITION_BOX),
    INTERNALS("Internals", "Internals", ComponentType.HEAT_SINK, ComponentType.UPGRADE),
    EQUIPMENT("EQUIP", "Equipment", ComponentType.JUMP_JET, ComponentType.UPGRADE, ComponentType.HEAT_SINK);

    private final String id;
    private final String name;
    private final Set<ComponentType> baseComponentTypes;

    CategoryGUIGroup(String id, String name, ComponentType... baseComponentTypes) {

        this.id = id;
        this.name = name;
        this.baseComponentTypes = newHashSet(baseComponentTypes);
    }

    public static CategoryGUIGroup getCategorySection(String name) {
        for (CategoryGUIGroup value : values()) {
            if (value.getId().equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown Category Section " + name);
    }
}
