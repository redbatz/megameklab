package org.redbat.roguetech.megameklab.data.component.value;

import com.fasterxml.jackson.databind.JsonNode;
import org.redbat.roguetech.megameklab.data.component.type.Component;

public interface ComponentValuePopulator {
    void populateValues(Component component, JsonNode node);
}
