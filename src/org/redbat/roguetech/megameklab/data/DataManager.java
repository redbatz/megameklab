package org.redbat.roguetech.megameklab.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redbat.roguetech.megameklab.data.repository.EquipmentRepository;
import org.redbat.roguetech.megameklab.data.repository.WeaponRepository;
import org.redbat.roguetech.megameklab.data.type.DataType;
import org.redbat.roguetech.megameklab.data.type.LoadableData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataManager {


    private final static Map<Class<? extends LoadableData>, DataRepository<?>> REPO_MAP = new HashMap<>();

    static {
        REPO_MAP.put(DataType.EQUIPMENT.getDataClass(), new EquipmentRepository());
        REPO_MAP.put(DataType.WEAPON.getDataClass(), new WeaponRepository());
    }

    public static void initialize() {
        DataLoader.load();
    }

    static <T extends LoadableData> DataRepository<T> getRepository(Class<T> dataClass) {
        DataRepository<?> dataRepository = REPO_MAP.get(dataClass);
        if (dataRepository == null) {
            throw new IllegalArgumentException("Unknown data class " + dataClass.getSimpleName());
        }
        return (DataRepository<T>) dataRepository;
    }

    public static <T extends LoadableData> Collection<T> getAll(DataType dataType) {
        return (Collection<T>) getRepository(dataType.getDataClass()).findAll();
    }
}
