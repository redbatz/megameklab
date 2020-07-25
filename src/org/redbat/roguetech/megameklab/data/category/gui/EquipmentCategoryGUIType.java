package org.redbat.roguetech.megameklab.data.category.gui;

import lombok.Getter;

@Getter
public enum EquipmentCategoryGUIType implements CategoryGUIType {
    JUMP_JET("JumpJets", "Jump Jet"),
    BC("BattleComputers", "Battle Computers"),
    EWS("Electronic Warfare", "EWS"),
    GENERIC("other equipment", "Other"),
    COCKPIT("Cockpits", "Cockpit"),
    FCS("FCS", "FCS"),
    SENSOR("Sensors", "Sensors"),
    MELEE("Melee Gear", "Melee"),
    SHIELDS("Shields", "Shields"),
    SPECIALIST("Specialist Gear", "Specialist");

    private final String id;
    private final String name;

    EquipmentCategoryGUIType(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
