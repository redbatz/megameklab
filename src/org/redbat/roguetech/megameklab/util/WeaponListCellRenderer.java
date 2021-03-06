/*
 * MegaMekLab - Copyright (C) 2009
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

/*
 * Thanks to Lost in space of the Solaris Sunk Works Project for the code snippet and idea.
 */

package org.redbat.roguetech.megameklab.util;

import org.redbat.roguetech.megamek.common.Entity;
import org.redbat.roguetech.megamek.common.EquipmentType;
import org.redbat.roguetech.megamek.common.Mounted;

import javax.swing.*;
import java.awt.Component;

public class WeaponListCellRenderer extends DefaultListCellRenderer {

    private Entity unit = null;

    /**
     *
     */
    private static final long serialVersionUID = 1599368063832366744L;

    public WeaponListCellRenderer(Entity unit) {
        this.unit = unit;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);

        EquipmentType eq = EquipmentType.get(value.toString());


        if (eq == null) {
            return label;
        }

        label.setText(UnitUtil.getCritName(unit, eq));
        label.setName(value.toString());

        label.setToolTipText(UnitUtil.getToolTipInfo(unit, new Mounted(unit, eq)));
        return label;
    }

}