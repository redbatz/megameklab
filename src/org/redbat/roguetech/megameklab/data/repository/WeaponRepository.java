package org.redbat.roguetech.megameklab.data.repository;

import org.redbat.roguetech.megameklab.data.DataRepository;
import org.redbat.roguetech.megameklab.data.type.Equipment;
import org.redbat.roguetech.megameklab.data.type.Weapon;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponRepository implements DataRepository<Weapon> {

    private final static Map<String, Weapon> dataStorage = new ConcurrentHashMap<>();

    @Override
    public void storeAll(Collection<Weapon> loadableData) {
        loadableData.forEach(data -> dataStorage.put(data.getId(), data));
    }

    @Override
    public Collection<Weapon> findAll() {
        return dataStorage.values();
    }
}
