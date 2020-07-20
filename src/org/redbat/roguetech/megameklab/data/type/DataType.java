package org.redbat.roguetech.megameklab.data.type;

import lombok.Getter;

@Getter
public enum DataType {
    EQUIPMENT(Equipment.class, "Equipment"), WEAPON(Weapon.class, "Weapon");

    private final Class<? extends LoadableData> dataClass;
    private final String name;

    DataType(Class<? extends LoadableData> dataClass, String name) {
        this.dataClass = dataClass;
        this.name = name;
    }

    public static DataType getDataType(Class<?> clazz) {
        for (DataType dataType : values()) {
            if (dataType.getDataClass().equals(clazz)) {
                return dataType;
            }
        }
        throw new IllegalArgumentException("Illegal data class");
    }
}
