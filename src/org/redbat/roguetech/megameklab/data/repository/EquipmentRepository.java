package org.redbat.roguetech.megameklab.data.repository;

import org.redbat.roguetech.megameklab.data.DataRepository;
import org.redbat.roguetech.megameklab.data.type.Equipment;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EquipmentRepository implements DataRepository<Equipment> {

    private final static Map<String, Equipment> dataStorage = new ConcurrentHashMap<>();

    @Override
    public void storeAll(Collection<Equipment> loadableData) {
        loadableData.forEach(data -> dataStorage.put(data.getId(), data));
    }

    @Override
    public Collection<Equipment> findAll() {
        return dataStorage.values();
    }
}
