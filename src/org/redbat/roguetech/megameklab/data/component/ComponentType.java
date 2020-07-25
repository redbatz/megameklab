package org.redbat.roguetech.megameklab.data.component;

import lombok.Getter;

public enum ComponentType {
    AMMUNITION_BOX("AmmunitionBox"), HEAT_SINK("HeatSink"), JUMP_JET("JumpJet"), UPGRADE("Upgrade"), WEAPON("Weapon");

    @Getter
    private final String name;

    ComponentType(String name) {
        this.name = name;
    }

    public static ComponentType getComponentType(String name) {
        if (name == null) {
            return null;
        }
        for (ComponentType value : values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown category type " + name);
    }
}
