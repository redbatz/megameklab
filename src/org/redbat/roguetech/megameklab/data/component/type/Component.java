package org.redbat.roguetech.megameklab.data.component.type;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.redbat.roguetech.megamek.common.EquipmentType;
import org.redbat.roguetech.megameklab.data.component.ComponentType;
import org.redbat.roguetech.megameklab.data.category.Category;
import org.redbat.roguetech.megameklab.data.model.Location;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Component extends EquipmentType {

    private String id;
    private String uiName;
    private List<Category> categories;
    private List<String> tags;
    private ComponentType componentType;
    private boolean lootable = false;
    private boolean linked = false;
    private boolean deprecated = false;
    private Multimap<Location, Component> linkedComponents = MultimapBuilder.hashKeys().arrayListValues().build();

    public void setName(String name) {
        this.name = name;
        this.addLookupName(name);
    }

    public void addLinkedComponent(Location location, Component linkedComponent) {
        linkedComponents.put(location, linkedComponent);
    }
}
