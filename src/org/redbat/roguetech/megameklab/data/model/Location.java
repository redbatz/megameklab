package org.redbat.roguetech.megameklab.data.model;

import lombok.Getter;

@Getter
public enum Location {
    HEAD("Head", "HD", "Head"),
    LEFT_ARM("LeftArm", "LA", "Left Arm"),
    LEFT_TORSO("LeftTorso", "LT", "Left Torso"),
    CENTER_TORSO("CenterTorso", "CT", "Center Torso"),
    RIGHT_TORSO("RightTorso", "RT", "Right Torso"),
    RIGHT_ARM("RightArm", "RA", "Right Arm"),
    LEFT_LEG("LeftLeg", "LL", "Left Leg"),
    RIGHT_LEG("RightLeg", "RL", "Right Leg");

    private final String id;
    private final String shortName;
    private final String fullName;

    Location(String id, String shortName, String fullName) {
        this.id = id;
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public static Location getLocation(String id) {
        for (Location value : values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        throw new IllegalStateException("Unknown location " + id);
    }
}
