package org.redbat.roguetech.megameklab.data.category;

import lombok.Data;

@Data(staticConstructor = "of")
public class Category {

    private final String id;
    private final String name;
    private boolean manualCategory = false;

}
