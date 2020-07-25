package org.redbat.roguetech.megameklab.data.category.gui;

import lombok.Getter;

@Getter
public enum AmmunitionCategoryGUIType implements CategoryGUIType {
    AC("Autocannons and Riffle ammo", "AC/Rifle"),
    RAC("RAC, UAC and LBX ammo", "RAC/UAC/LBX"),
    GAUSS("Gauss ammo", "Gauss"),
    OTHER_BALLISTIC("MG and Artillery ammo", "MG/Artillery"),
    SRM("SRM and SSRM missiles", "SRM"),
    MRM("MRM and HMRM missiles", "MRM"),
    LRM("LRM and SLRM missiles", "LRM"),
    ATM("ATM and iATM missiles", "ATM"),
    ROCKET("Artillery and Thunderbolt rockets", "Arrow IV/Thunderbolt"),
    ENERGY("Chemical Lasers, Flamers and Plasma fuel/ammo", "Chemical Lasers/Flamers/Plasma"),
    SUPPORT("Support weapons ammo", "Support");

    private final String id;
    private final String name;

    AmmunitionCategoryGUIType(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
