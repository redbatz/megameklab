package org.redbat.roguetech.megameklab.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megameklab.data.component.ComponentLoader;
import org.redbat.roguetech.megameklab.data.component.ComponentType;
import org.redbat.roguetech.megameklab.data.component.type.Component;
import org.redbat.roguetech.megameklab.data.repository.ComponentRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataManager {

    public static void initialize() {
        log.info("Start loading of all data files");
        CategoryManager.load();
        CategoryGUIMappingsManager.load();
        ComponentManager.load();
        log.info("Data loading finished");
    }
}
