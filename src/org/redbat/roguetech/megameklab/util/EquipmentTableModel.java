/*
 * MegaMekLab - Copyright (C) 2011
 *
 * Original author - jtighe (torren@users.sourceforge.net)
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

package org.redbat.roguetech.megameklab.util;

import org.redbat.roguetech.megamek.common.*;
import org.redbat.roguetech.megamek.common.weapons.autocannons.UACWeapon;
import org.redbat.roguetech.megamek.common.weapons.gaussrifles.HAGWeapon;
import org.redbat.roguetech.megamek.common.weapons.infantry.InfantryWeapon;
import org.redbat.roguetech.megamek.common.weapons.missiles.ATMWeapon;
import org.redbat.roguetech.megamek.common.weapons.missiles.MissileWeapon;
import org.redbat.roguetech.megamek.common.weapons.missiles.ThunderBoltWeapon;
import org.redbat.roguetech.megamek.common.weapons.mortars.MekMortarWeapon;
import org.redbat.roguetech.megameklab.data.component.type.Component;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * this model was not being used by anything, so I totally redid so that it can
 * be used as the model for the equipment tab. It will be a sortable, filterable
 * table of equipment, similar to the tables in MHQ
 *
 * @author Jay lawson
 */
public class EquipmentTableModel extends AbstractTableModel {

    public final static String VARIABLE = "variable";
    public final static int COL_ID = 0;
    public final static int COL_NAME = 1;
    public final static int COL_UINAME = 2;
    public final static int COL_DAMAGE = 3;
    public final static int COL_DIVISOR = 4;
    public final static int COL_SPECIAL = 5;
    public final static int COL_HEAT = 6;
    public final static int COL_MRANGE = 7;
    public final static int COL_RANGE = 8;
    public final static int COL_SHOTS = 9;
    public final static int COL_TECH = 10;
    public final static int COL_COST = 11;
    public final static int COL_BV = 12;
    public final static int COL_TON = 13;
    public final static int COL_CRIT = 14;
    public final static int N_COL = 15;
    private static final long serialVersionUID = -5207167419079014157L;
    /**
     * Comparator for numeric columns. Non-numeric values such as "variable" or "special" are sorted
     * alphabetically and placed at the end (if in descending order). Strings ending in "kg" are parsed
     * as numbers and converted to tons.
     */
    private static final Comparator<Object> NUMBER_SORTER = (o1, o2) -> {
        double d1 = -1.0;
        double d2 = -1.0;
        try {
            if (o1 instanceof Number) {
                // Simplest processing of Integer and Double
                d1 = ((Number) o1).doubleValue();
            } else if (o1.toString().endsWith("kg")) {
                // Convert kg values to tons
                d1 = Double.parseDouble(o1.toString().replace("kg", "").trim()) / 1000.0;
            } else {
                // Handle potentially commafied number
                d1 = NumberFormat.getInstance().parse(o1.toString()).doubleValue();
            }
        } catch (NumberFormatException | ParseException ignored) {
            // Not a representation of a number; sort alphabetically
        }
        try {
            if (o2 instanceof Number) {
                d2 = ((Number) o2).doubleValue();
            } else if (o2.toString().endsWith("kg")) {
                d2 = Double.parseDouble(o2.toString().replace("kg", "").trim()) / 1000.0;
            } else {
                d2 = NumberFormat.getInstance().parse(o2.toString()).doubleValue();
            }
        } catch (NumberFormatException | ParseException ignored) {
            // Not a representation of a number; sort alphabetically
        }
        if ((d1 < 0) && (d2 < 0)) {
            return o1.toString().compareToIgnoreCase(o2.toString());
        } else {
            return Double.compare(d1, d2);
        }
    };
    /**
     * Sorter for a series of one or more values separated by slashes. This handles weapon ranges
     * and also deals with multiple damage values.
     */
    private static final Comparator<String> RANGE_DAMAGE_SORTER = (s1, s2) -> {
        String[] r1 = s1.split("/");
        String[] r2 = s2.split("/");
        int retVal = 0;
        for (int i = 0; i < Math.min(r1.length, r2.length); i++) {
            retVal = NUMBER_SORTER.compare(r1[i], r2[i]);
            if (retVal != 0) {
                break;
            }
        }
        return retVal;
    };
    /**
     * Sorter for reference column. References give the page number then the work separated by a comma.
     * Sorts by the work first, then the page number.
     */
    private static final Comparator<String> REFERENCE_SORTER = (s1, s2) -> {
        String[] r1 = s1.split(",\\s*");
        String[] r2 = s2.split(",\\s*");
        if ((r1.length > 1) && (r2.length > 1) && !r1[1].equals(r2[1])) {
            return r1[1].compareTo(r2[1]);
        }
        return NUMBER_SORTER.compare(r1[0], r2[0]);
    };
    final private ITechManager techManager;
    private List<Component> data = new ArrayList<>();
    private Entity entity;

    public EquipmentTableModel(Entity e, ITechManager techManager) {
        entity = e;
        this.techManager = techManager;
    }

    private static String getDamageString(WeaponType wtype, boolean isAero) {
        // Aeros should print AV instead
        if (isAero) {
            int[] attackValue = new int[RangeType.RANGE_EXTREME + 1];
            attackValue[RangeType.RANGE_SHORT] = (int) wtype.getShortAV();
            attackValue[RangeType.RANGE_MEDIUM] = (int) wtype.getMedAV();
            attackValue[RangeType.RANGE_LONG] = (int) wtype.getLongAV();
            attackValue[RangeType.RANGE_EXTREME] = (int) wtype.getExtAV();
            boolean allEq = true;
            for (int i = 2; i <= wtype.maxRange; i++) {
                if (attackValue[i - 1] != attackValue[i]) {
                    allEq = false;
                    break;
                }
            }
            StringBuilder avString = new StringBuilder();
            avString.append(attackValue[RangeType.RANGE_SHORT]);
            if (!allEq) {
                for (int i = 2; i <= wtype.maxRange; i++) {
                    avString.append('/').append(attackValue[i]);
                }
            }
            return avString.toString();
        }
        // Damage for non-Aeros
        if (wtype instanceof InfantryWeapon) {
            return Double
                    .toString(((InfantryWeapon) wtype).getInfantryDamage());
        }

        if (wtype.getDamage() == WeaponType.DAMAGE_VARIABLE) {
            return wtype.getDamage(wtype.getShortRange()) + "/"
                    + wtype.getDamage(wtype.getMediumRange()) + "/"
                    + wtype.getDamage(wtype.getLongRange());
        } else if (wtype.getDamage() == WeaponType.DAMAGE_BY_CLUSTERTABLE) {
            if (wtype instanceof HAGWeapon) {
                return wtype.getRackSize() + "";
            } else if (wtype instanceof MekMortarWeapon) {
                return "Special";
            } else if (wtype instanceof MissileWeapon) {
                int dmg;
                if (wtype instanceof ThunderBoltWeapon) {
                    switch (wtype.getAmmoType()) {
                        case AmmoType.T_TBOLT_5:
                            return "5";
                        case AmmoType.T_TBOLT_10:
                            return "10";
                        case AmmoType.T_TBOLT_15:
                            return "15";
                        case AmmoType.T_TBOLT_20:
                            return "20";
                        default:
                            return "0";
                    }
                } else if ((wtype instanceof ATMWeapon)
                        || (wtype.getAmmoType() == AmmoType.T_SRM)
                        || (wtype.getAmmoType() == AmmoType.T_SRM_STREAK)) {
                    dmg = 2;
                } else {
                    dmg = 1;
                }
                return dmg + "/msl";
            }
            return "Cluster";
        } else if (wtype.getDamage() == WeaponType.DAMAGE_ARTILLERY) {
            return wtype.getRackSize() + "A";
        } else if (wtype instanceof UACWeapon) {
            return wtype.getDamage() + "/Shot";
        } else if (wtype.getDamage() < 0) {
            return "Special";
        } else {
            return Integer.toString(wtype.getDamage());
        }
    }

    /**
     * Converts an entry in the tech advancement table to an integer year for sorting.
     *
     * @param date The date entry
     * @return The year represented
     */
    private static int parseDate(String date) {
        if (date.startsWith("PS")) {
            return 1950;
        } else if (date.startsWith("ES")) {
            return 2100;
        } else if (date.equals("-")) {
            return 0;
        } else {
            try {
                return Integer.parseInt(date.replaceAll("[^0-9]", "").trim());
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return N_COL;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Component component;
        if (data.isEmpty()) {
            return "";
        } else {
            component = data.get(row);
        }
        DecimalFormat formatter = new DecimalFormat();

        if (col == COL_ID) {
            return component.getId();
        } else if (col == COL_NAME) {
            return component.getName();
        } else if (col == COL_UINAME) {
            return component.getUiName();
        } else if (col == COL_DAMAGE) {
            return "-";
        } else if (col == COL_DIVISOR) {
//            if (mtype != null && mtype.hasFlag(MiscType.F_ARMOR_KIT)){
//                if ((mtype.getSubType() & MiscType.S_ENCUMBERING) == 0) {
//                    return String.valueOf(mtype.getDamageDivisor());
//                } else {
//                    return mtype.getDamageDivisor() + "E";
//                }
//            } else {
            return "-";
        } else if (col == COL_SPECIAL) {
            String special = "";
            if (component.hasFlag(MiscType.F_ARMOR_KIT)) {
                if ((component.getSubType() & MiscType.S_DEST) != 0) {
                    special += "DEST ";
                }
                if ((component.getSubType() & MiscType.S_SNEAK_CAMO) != 0) {
                    special += "Camo ";
                }
                if ((component.getSubType() & MiscType.S_SNEAK_IR) != 0) {
                    special += "IR ";
                }
                if ((component.getSubType() & MiscType.S_SNEAK_ECM) != 0) {
                    special += "ECM ";
                }
                if ((component.getSubType() & MiscType.S_SPACE_SUIT) != 0) {
                    special += "SPC ";
                }
            }
            return special;
        } else if (col == COL_HEAT) {
            int heat = component.getHeat();
            if (heat == 0) {
                return "-";
            } else {
                return Integer.toString(heat);
            }
        } else if (col == COL_SHOTS) {
            //if (component instanceof AmmunitionBox) {
            //return Integer.toString(component.getShots());
            //} else {
            return "-";
            //}
        } else if (col == COL_RANGE) {
//            if (null != wtype) {
//                if (entity instanceof Aero) {
//                    switch (wtype.maxRange) {
//                        case RangeType.RANGE_SHORT:
//                            return "Short";
//                        case RangeType.RANGE_MEDIUM:
//                            return "Medium";
//                        case RangeType.RANGE_LONG:
//                            return "Long";
//                        case RangeType.RANGE_EXTREME:
//                            return "Extreme";
//                    }
//                }
//                if (wtype instanceof InfantryWeapon) {
//                    return ((InfantryWeapon) wtype).getInfantryRange() + "";
//                }
//                return wtype.getShortRange() + "/" + wtype.getMediumRange()
//                        + "/" + wtype.getLongRange();
//            } else {
            return "-";
//            }
        } else if (col == COL_MRANGE) {
//            if (null != wtype) {
//                int minRange = wtype.getMinimumRange();
//                if (minRange < 0) {
//                    minRange = 0;
//                }
//                return Integer.toString(minRange);
//            } else {
            return "-";
//            }
        } else if (col == COL_TON) {
//            final double weight = component.getTonnage(entity);
//            if ((atype != null) && (entity.hasETypeFlag(Entity.ETYPE_BATTLEARMOR)
//                    || entity.hasETypeFlag(Entity.ETYPE_PROTOMECH))) {
//                return String.format("%.2f kg/shot", atype.getKgPerShot());
//            } else if (component.isVariableTonnage()) {
//                return VARIABLE;
//            } else if (TestEntity.usesKgStandard(entity) || ((weight > 0.0) && (weight < 0.1))) {
//                return String.format("%.0f kg", component.getTonnage(entity) * 1000);
//            } else {
//                return formatter.format(weight);
//            }
            return "-";
        } else if (col == COL_CRIT) {
//            if (component.isVariableCriticals()
//                    && (entity.isSupportVehicle() || (entity instanceof Mech))) {
//                // Only Mechs and support vehicles require multiple slots for equipment
//                return "variable";
//            } else if (entity.isSupportVehicle()) {
//                return component.getSupportVeeSlots(entity);
//            } else if (entity instanceof Tank) {
//                return component.getTankSlots(entity);
//            } else if (entity.hasETypeFlag(Entity.ETYPE_PROTOMECH)) {
//                return TestProtomech.requiresSlot(component)? 1 : 0;
//            }
//            return component.getCriticals(entity);
            return "-";
        } else if (col == COL_COST) {
            return "-";
//            if (component.isVariableCost()) {
//                return "variable";
//            }
//            return formatter.format(component
//                    .getCost(entity, false, Entity.LOC_NONE));
        } else if (col == COL_BV) {
//            if (component.isVariableBV()) {
//                return "variable";
//            }
//            return component.getBV(entity);
            return "-";
        } else if (col == COL_TECH) {
//            switch(component.getTechBase()) {
//            case TechAdvancement.TECH_BASE_ALL:
//                return "All";
//            case TechAdvancement.TECH_BASE_IS:
//                return "IS";
//            case TechAdvancement.TECH_BASE_CLAN:
//                return "Clan";
//            }
            return "-";
        }
        return "?";
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case COL_ID:
                return "Id";
            case COL_NAME:
                return "Name";
            case COL_UINAME:
                return "UIName";

            case COL_DAMAGE:
                return "Damage";
            case COL_DIVISOR:
                return "Divisor";
            case COL_SPECIAL:
                return "Special";
            case COL_HEAT:
                return "Heat";
            case COL_MRANGE:
                return "Min R";
            case COL_RANGE:
                return "Range";
            case COL_TON:
                return "Ton";
            case COL_CRIT:
                if (entity instanceof Tank) {
                    return "Slots";
                }
                return "Crit";
            case COL_TECH:
                return "Base";
            case COL_COST:
                return "Cost";
            case COL_SHOTS:
                return "Shots";
            case COL_BV:
                return "BV";
            default:
                return "?";
        }
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public int getColumnWidth(int c) {
        switch (c) {
            case COL_ID:
                return 250;
            case COL_NAME:
            case COL_UINAME:
                return 150;
            /*
             * case COL_DATES: return 100;
             */
            case COL_RANGE:
            case COL_COST:
                return 50;
            /*
             * case COL_TRATING: case COL_COST: return 20;
             */
            case COL_TON:
            case COL_CRIT:
            case COL_MRANGE:
                return 5;
            default:
                return 30;
        }
    }

    private int getAlignment(int col) {
        if (col == COL_ID || col == COL_NAME || col == COL_UINAME) {
            return SwingConstants.LEFT;
        }
        return SwingConstants.CENTER;
    }

    public Comparator<?> getSorter(int col) {
        switch (col) {
            case COL_DAMAGE:
            case COL_RANGE:
                return RANGE_DAMAGE_SORTER;
            case COL_HEAT:
            case COL_MRANGE:
            case COL_TON:
            case COL_CRIT:
            case COL_COST:
            case COL_SHOTS:
            case COL_BV:
                return NUMBER_SORTER;
            default:
                return Comparator.naturalOrder();
        }
    }

    public EquipmentType getType(int i) {
        if (i >= data.size()) {
            return null;
        }
        return data.get(i);
    }

    // fill table with values
    public void setData(Collection<org.redbat.roguetech.megameklab.data.component.type.Component> data) {
        this.data = new ArrayList<>(data);
        fireTableDataChanged();
    }

    public EquipmentTableModel.Renderer getRenderer() {
        return new EquipmentTableModel.Renderer();
    }

    public class Renderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 9054581142945717303L;

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table,
                                                                Object value, boolean isSelected, boolean hasFocus, int row,
                                                                int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            int actualCol = table.convertColumnIndexToModel(column);
            int actualRow = table.convertRowIndexToModel(row);
            setHorizontalAlignment(getAlignment(actualCol));
            setForeground(UIManager.getColor("Label.foreground"));
            setToolTipText(value.toString());
            return this;
        }
    }
}
