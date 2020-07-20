package org.redbat.roguetech.megameklab.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megameklab.data.type.Equipment;
import org.redbat.roguetech.megameklab.ui.util.JsonConstants;

import java.nio.file.Path;

@Slf4j
public class EquipmentLoader {

    public static void load() {
        BaseDataLoader.doLoad(Equipment.class, EquipmentLoader::fileFilter, Equipment::new, EquipmentLoader::valuePopulator);
    }

    private static Equipment valuePopulator(Equipment equipment, JsonNode jsonNode) {
        return equipment;
    }

    private static boolean fileFilter(Path path, JsonNode jsonNode) {
        JsonNode weaponSubType = jsonNode.findValue(JsonConstants.WEAPON_SUB_TYPE_FIELD);
        if (weaponSubType != null) {
            return false;
        }
        JsonNode customNode = jsonNode.get(JsonConstants.CUSTOM_FIELD);
        if (customNode == null) {
            return false;
        }
        JsonNode categoryNode = customNode.findValue(JsonConstants.CATEGORY_FIELD);
        return categoryNode != null;
    }
}
