package org.redbat.roguetech.megameklab.data.category.gui;

import lombok.Getter;

@Getter
public enum InternalComponentCategoryGUIType implements CategoryGUIType {
    CORE("engine cores", "Engine Core"),
    SHIELD("engine shielding", "Engine Shield"),
    COOLING_KIT("Cooling Kits", "Cooling Kits"),
    COOLING_ENGINE("Engine Cooling", "Engine Cooling"),
    HEAT_SINK("heatsinks and heat control equipment", "Heat Sinks"),
    COOLING_POD("Coolant Pods", "Coolant Pods"),
    ARMOR("Armor", "Armor"),
    STRUCTURE("Structure", "Structure"),
    GYRO("Gyro", "Gyro"),
    ACTUATORS_LEG("Leg Upgrades", "Leg Actuators"),
    ACTUATORS_ARM("Arm Upgrades", "Arm Actuators");

    private final String id;
    private final String name;

    InternalComponentCategoryGUIType(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
