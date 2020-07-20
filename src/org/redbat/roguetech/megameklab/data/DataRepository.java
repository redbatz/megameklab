package org.redbat.roguetech.megameklab.data;

import org.redbat.roguetech.megameklab.data.type.LoadableData;

import java.util.Collection;

public interface DataRepository<T extends LoadableData> {
    void storeAll(Collection<T> loadableData);

    Collection<T> findAll();
}
