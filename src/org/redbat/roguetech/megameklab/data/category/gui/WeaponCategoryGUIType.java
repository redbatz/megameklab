package org.redbat.roguetech.megameklab.data.category.gui;

import lombok.Getter;

@Getter
public enum WeaponCategoryGUIType implements CategoryGUIType {
    AC("Autocannons", "Autocannons"),
    GAUSS("Magnetic rifles", "Magnetic Rifles"),
    ARTILLERY("Other Balistics", "MG/Artillery"),
    LASER("Lasers", "Lasers"),
    PPC("PPC", "PPC"),
    FLAMER("Flamers and Plasma", "Flamers/Plasma"),
    SRM("Direct fire Missiles", "Direct Fire Missiles"),
    LRM("Indirect fire Missiles", "Indirect Fire Missiles"),
    ATM("Multi Missile Systems", "ATM"),
    SUPPORT("AMS/Tag/Support Weapons", "AMS/Tag/Support"),
    SPECIALIST("Specialist Weapons", "Specialist");

    private final String id;
    private final String name;

    WeaponCategoryGUIType(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
