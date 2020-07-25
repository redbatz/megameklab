package org.redbat.roguetech.megameklab.data.component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redbat.roguetech.megameklab.data.component.type.*;
import org.redbat.roguetech.megameklab.data.component.value.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComponentFactory {

    public static Component createComponent(ComponentType componentType) {
        switch (componentType) {
            case AMMUNITION_BOX:
                return new AmmunitionBox();
            case HEAT_SINK:
                return new HeatSink();
            case JUMP_JET:
                return new JumpJet();
            case UPGRADE:
                return new Upgrade();
            case WEAPON:
                return new Weapon();
            default:
                throw new IllegalStateException("Unknown component type");
        }
    }

    public static ComponentValuePopulator getComponentValuePopulator(ComponentType componentType) {
        switch (componentType) {
            case AMMUNITION_BOX:
                return new AmmunitionBoxValuePopulator();
            case HEAT_SINK:
                return new HeatSinkValuePopulator();
            case JUMP_JET:
                return new JumpJetValuePopulator();
            case UPGRADE:
                return new UpgradeValuePopulator();
            case WEAPON:
                return new WeaponValuePopulator();
            default:
                throw new IllegalStateException("Unknown component type");
        }
    }
}
