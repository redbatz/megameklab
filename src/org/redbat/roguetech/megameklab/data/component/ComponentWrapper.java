package org.redbat.roguetech.megameklab.data.component;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;
import org.redbat.roguetech.megameklab.data.component.type.Component;

import java.nio.file.Path;

@Value(staticConstructor = "of")
public class ComponentWrapper {
    Component component;
    JsonNode jsonNode;
    Path path;
}
