package org.redbat.roguetech.megameklab.data.type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.redbat.roguetech.megamek.common.EquipmentType;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class LoadableData extends EquipmentType {

    private String id;
    private String uiName;

    public void setName(String name) {
        this.name = name;
        this.addLookupName(name);
    }

}
