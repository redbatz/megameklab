package org.redbat.roguetech.megameklab.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataLoader {

    public static void load() {
        log.info("Start loading of all data files");
        EquipmentLoader.load();
        WeaponLoader.load();
        log.info("Data loading finished");
    }

}
