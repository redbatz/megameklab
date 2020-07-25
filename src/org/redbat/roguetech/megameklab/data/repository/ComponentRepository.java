package org.redbat.roguetech.megameklab.data.repository;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redbat.roguetech.megameklab.data.component.ComponentType;
import org.redbat.roguetech.megameklab.data.component.type.*;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentRepository {

    private static final Table<String, ComponentType, Component> dataStorage = Tables.synchronizedTable(HashBasedTable.create());

    public static void storeAll(Collection<Component> loadableData) {
        loadableData.forEach(data -> dataStorage.put(data.getId(), data.getComponentType(), data));
    }

    public static Collection<Component> findAll() {
        return dataStorage.values();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Component> Collection<T> findAllByType(ComponentType componentType) {
        return (Collection<T>) dataStorage.column(componentType);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> Class<T> getDataClass(ComponentType componentType) {
        switch (componentType) {

            case AMMUNITION_BOX:
                return (Class<T>) AmmunitionBox.class;
            case HEAT_SINK:
                return (Class<T>) HeatSink.class;
            case JUMP_JET:
                return (Class<T>) JumpJet.class;
            case UPGRADE:
                return (Class<T>) Upgrade.class;
            case WEAPON:
                return (Class<T>) Weapon.class;
            default:
                throw new IllegalStateException("Unexpected value: " + componentType);
        }
    }
}
