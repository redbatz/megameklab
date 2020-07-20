package org.redbat.roguetech.megameklab.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redbat.roguetech.megameklab.data.type.Equipment;
import org.redbat.roguetech.megameklab.data.type.LoadableData;
import org.redbat.roguetech.megameklab.data.type.Weapon;
import org.redbat.roguetech.megameklab.file.FileConstants;
import org.redbat.roguetech.megameklab.ui.util.JsonConstants;

import java.nio.file.Path;
import java.util.function.BiFunction;

@Slf4j
public class WeaponLoader extends BaseDataLoader {

    public static void load() {
        BaseDataLoader.doLoad(Weapon.class, WeaponLoader::fileFilter, Weapon::new, WeaponLoader::valuePopulator);
    }

    private static Weapon valuePopulator(Weapon weapon, JsonNode jsonNode) {
        return weapon;
    }

    private static boolean fileFilter(Path path, JsonNode jsonNode) {
        JsonNode weaponSubType = jsonNode.findValue(JsonConstants.WEAPON_SUB_TYPE_FIELD);
        return weaponSubType != null;
    }

}
