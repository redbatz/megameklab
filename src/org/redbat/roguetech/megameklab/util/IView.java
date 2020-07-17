/*
 * MegaMekLab - Copyright (C) 2008
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package org.redbat.roguetech.megameklab.util;

import org.redbat.roguetech.megamek.common.*;
import org.redbat.roguetech.megameklab.ui.EntitySource;

import javax.swing.*;

public class IView extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -6741722012756653309L;
    protected EntitySource eSource;

    public IView(EntitySource eSource) {
        this.eSource = eSource;
    }

    public Entity getEntity() {
        return eSource.getEntity();
    }

    public Mech getMech() {
        return (Mech) eSource.getEntity();
    }
    
    public Protomech getProtomech() {
        return (Protomech) eSource.getEntity();
    }

    public Tank getTank() {
        return (Tank) eSource.getEntity();
    }

    public VTOL getVTOL() {
        return (VTOL) eSource.getEntity();
    }

    public Aero getAero() {
        return (Aero) eSource.getEntity();
    }
    
    public SmallCraft getSmallCraft() {
        return (SmallCraft) eSource.getEntity();
    }
    
    public Jumpship getJumpship() {
        return (Jumpship) eSource.getEntity();
    }

    public BattleArmor getBattleArmor() {
        return (BattleArmor) eSource.getEntity();
    }

    public Infantry getInfantry() {
    	return (Infantry) eSource.getEntity();
    }
}