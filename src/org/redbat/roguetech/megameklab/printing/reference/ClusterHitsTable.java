/*
 * MegaMekLab - Copyright (C) 2020 - The MegaMek Team
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */
package org.redbat.roguetech.megameklab.printing.reference;

import org.redbat.roguetech.megamek.common.*;
import org.redbat.roguetech.megameklab.printing.PrintEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Table showing the relevant columns of the cluster hits table
 */
public class ClusterHitsTable extends ReferenceTable {
    private final Set<Integer> clusterSizes = new TreeSet<>();
    private boolean hasATM;
    private boolean hasHAG;

    public ClusterHitsTable(PrintEntity sheet) {
        super(sheet);
        calculateClusterSizes(sheet.getEntity());
        if (!clusterSizes.isEmpty()) {
            List<Double> offsets = new ArrayList<>();
            double spacing = 0.9 / (clusterSizes.size() + 1);
            for (double o = 0.05 + spacing / 2.0; o <= 0.95; o += spacing) {
                offsets.add(o);
            }
            setColOffsets(offsets);
            List<String> headers = new ArrayList<>();
            headers.add(bundle.getString("dieRoll2d6"));
            for (int size : clusterSizes) {
                headers.add(String.valueOf(size));
            }
            setHeaders(headers);
            addRows();
            addNotes(sheet.getEntity());
        }
    }

    private void calculateClusterSizes(Entity entity) {
        for (Mounted mounted : entity.getIndividualWeaponList()) {
            if (mounted.getType() instanceof WeaponType) {
                final WeaponType weapon = (WeaponType) mounted.getType();
                switch (weapon.getAmmoType()) {
                    case AmmoType.T_AC_LBX:
                    case AmmoType.T_EXLRM:
                    case AmmoType.T_IATM:
                    case AmmoType.T_LRM:
                    case AmmoType.T_LRM_IMP:
                    case AmmoType.T_LRM_PRIMITIVE:
                    case AmmoType.T_LRM_TORPEDO:
                    case AmmoType.T_MML:
                    case AmmoType.T_MRM:
                    case AmmoType.T_NLRM:
                    case AmmoType.T_ROCKET_LAUNCHER:
                    case AmmoType.T_SBGAUSS:
                    case AmmoType.T_SRM:
                    case AmmoType.T_SRM_ADVANCED:
                    case AmmoType.T_SRM_IMP:
                    case AmmoType.T_SRM_PRIMITIVE:
                    case AmmoType.T_SRM_TORPEDO:
                        clusterSizes.add(weapon.getRackSize());
                        break;
                    case AmmoType.T_ATM:
                        hasATM = true;
                        clusterSizes.add(weapon.getRackSize());
                        break;
                    case AmmoType.T_HAG:
                        hasHAG = true;
                        clusterSizes.add(weapon.getRackSize());
                        break;
                    case AmmoType.T_AC_ROTARY:
                        for (int i = 2; i <= 6; i++) {
                            clusterSizes.add(i);
                        }
                        break;
                    case AmmoType.T_AC_ULTRA:
                    case AmmoType.T_AC_ULTRA_THB:
                        clusterSizes.add(2);
                        break;
                }
            }
        }
        if (entity instanceof BattleArmor) {
            for (int i = 2; i <= ((BattleArmor) entity).getTroopers(); i++) {
                clusterSizes.add(i);
            }
        }
    }

    private void addRows() {
        for (int roll = 2; roll <= 12; roll++) {
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(roll));
            for (int size : clusterSizes) {
                row.add(String.valueOf(Compute.calculateClusterHitTableAmount(roll, size)));
            }
            addRow(row);
        }
    }

    private void addNotes(Entity entity) {
        if (hasATM || entity.hasWorkingMisc(MiscType.F_ARTEMIS)) {
            addNote(bundle.getString("artemisIV.note"));
        } else if (entity.hasWorkingMisc(MiscType.F_ARTEMIS_V)) {
            addNote(bundle.getString("artemisV.note"));
        } else if (entity.hasWorkingMisc(MiscType.F_ARTEMIS_PROTO)) {
            addNote(bundle.getString("artemisProto.note"));
        }
        if (entity.hasWorkingMisc(MiscType.F_APOLLO)) {
            addNote(bundle.getString("apollo.note"));
        }
        if (hasHAG) {
            addNote(bundle.getString("hag.note"));
        }
    }

    /**
     * @return Whether the unit has any weapons that use the cluster hits table
     */
    public boolean required() {
        return !clusterSizes.isEmpty();
    }
}
