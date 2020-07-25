/*
 * MegaMekLab - Copyright (C) 2008
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

import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megamek.common.*;
import org.redbat.roguetech.megamek.common.annotations.Nullable;
import org.redbat.roguetech.megamek.common.loaders.EntityLoadingException;
import org.redbat.roguetech.megamek.common.verifier.*;
import org.redbat.roguetech.megamek.common.weapons.*;
import org.redbat.roguetech.megamek.common.weapons.autocannons.ACWeapon;
import org.redbat.roguetech.megamek.common.weapons.autocannons.HVACWeapon;
import org.redbat.roguetech.megamek.common.weapons.autocannons.LBXACWeapon;
import org.redbat.roguetech.megamek.common.weapons.autocannons.UACWeapon;
import org.redbat.roguetech.megamek.common.weapons.battlearmor.CLBALBX;
import org.redbat.roguetech.megamek.common.weapons.bayweapons.BayWeapon;
import org.redbat.roguetech.megamek.common.weapons.defensivepods.BPodWeapon;
import org.redbat.roguetech.megamek.common.weapons.defensivepods.MPodWeapon;
import org.redbat.roguetech.megamek.common.weapons.flamers.VehicleFlamerWeapon;
import org.redbat.roguetech.megamek.common.weapons.gaussrifles.GaussWeapon;
import org.redbat.roguetech.megamek.common.weapons.gaussrifles.HAGWeapon;
import org.redbat.roguetech.megamek.common.weapons.infantry.InfantryRifleAutoRifleWeapon;
import org.redbat.roguetech.megamek.common.weapons.infantry.InfantryWeapon;
import org.redbat.roguetech.megamek.common.weapons.lasers.CLChemicalLaserWeapon;
import org.redbat.roguetech.megamek.common.weapons.lasers.EnergyWeapon;
import org.redbat.roguetech.megamek.common.weapons.lrms.LRMWeapon;
import org.redbat.roguetech.megamek.common.weapons.lrms.LRTWeapon;
import org.redbat.roguetech.megamek.common.weapons.lrms.StreakLRMWeapon;
import org.redbat.roguetech.megamek.common.weapons.mgs.MGWeapon;
import org.redbat.roguetech.megamek.common.weapons.missiles.MRMWeapon;
import org.redbat.roguetech.megamek.common.weapons.missiles.RLWeapon;
import org.redbat.roguetech.megamek.common.weapons.missiles.ThunderBoltWeapon;
import org.redbat.roguetech.megamek.common.weapons.other.ISC3M;
import org.redbat.roguetech.megamek.common.weapons.other.ISC3MBS;
import org.redbat.roguetech.megamek.common.weapons.ppc.CLPlasmaCannon;
import org.redbat.roguetech.megamek.common.weapons.ppc.ISPlasmaRifle;
import org.redbat.roguetech.megamek.common.weapons.ppc.PPCWeapon;
import org.redbat.roguetech.megamek.common.weapons.srms.SRMWeapon;
import org.redbat.roguetech.megamek.common.weapons.srms.SRTWeapon;
import org.redbat.roguetech.megamek.common.weapons.srms.StreakSRMWeapon;
import org.redbat.roguetech.megamek.common.weapons.tag.CLLightTAG;
import org.redbat.roguetech.megamek.common.weapons.tag.CLTAG;
import org.redbat.roguetech.megamek.common.weapons.tag.ISTAG;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
public class UnitUtil {

    public static int TECH_INTRO = 0;
    public static int TECH_STANDARD = 1;
    public static int TECH_ADVANCED = 2;
    public static int TECH_EXPERIMENTAL = 3;
    public static int TECH_UNOFFICAL = 4;

    private static Font rsFont = null;
    private static Font rsBoldFont = null;

    /**
     * Removes a piece of equipment from the Entity
     *
     * @param unit  The Entity
     * @param mount The equipment
     */
    public static void removeMounted(Entity unit, Mounted mount) {
        UnitUtil.removeCriticals(unit, mount);

        // Some special checks for BA
        if (unit instanceof BattleArmor) {
            // If we're removing a DWP and it has an attached weapon, we need
            //  to detach the weapon
            if (mount.getType().hasFlag(MiscType.F_DETACHABLE_WEAPON_PACK)
                    && (mount.getLinked() != null)) {
                Mounted link = mount.getLinked();
                link.setDWPMounted(false);
                link.setLinked(null);
                link.setLinkedBy(null);
            }
            // If we are removing a weapon that is mounted in an DWP, we need
            //  to clear the mounted status of the DWP
            if ((mount.getLinkedBy() != null)
                    && mount.getLinkedBy().getType().hasFlag(
                    MiscType.F_DETACHABLE_WEAPON_PACK)) {
                Mounted dwp = mount.getLinkedBy();
                dwp.setLinked(null);
                dwp.setLinkedBy(null);
            }
            // If we're removing an APM and it has an attached weapon, we need
            //  to detach the weapon
            if (mount.getType().hasFlag(MiscType.F_AP_MOUNT)
                    && (mount.getLinked() != null)) {
                Mounted link = mount.getLinked();
                link.setAPMMounted(false);
                link.setLinked(null);
                link.setLinkedBy(null);
            }
            // If we are removing a weapon that is mounted in an APM, we need
            //  to clear the mounted status of the AP Mount
            if ((mount.getLinkedBy() != null)
                    && mount.getLinkedBy().getType().hasFlag(
                    MiscType.F_AP_MOUNT)) {
                Mounted apm = mount.getLinkedBy();
                apm.setLinked(null);
                apm.setLinkedBy(null);
            }
        }
        // We will need to reset the equipment numbers of the bay ammo and weapons
        Map<Mounted, List<Mounted>> bayWeapons = new HashMap<>();
        Map<Mounted, List<Mounted>> bayAmmo = new HashMap<>();
        for (Mounted bay : unit.getWeaponBayList()) {
            List<Mounted> list = bay.getBayWeapons().stream()
                    .map(n -> unit.getEquipment(n)).collect(Collectors.toList());
            bayWeapons.put(bay, list);
            list = bay.getBayAmmo().stream()
                    .map(n -> unit.getEquipment(n)).collect(Collectors.toList());
            bayAmmo.put(bay, list);
        }

        unit.getEquipment().remove(mount);
        if (mount.getType() instanceof MiscType) {
            unit.getMisc().remove(mount);
        } else if (mount.getType() instanceof AmmoType) {
            unit.getAmmo().remove(mount);
        } else {
            unit.getWeaponList().remove(mount);
            unit.getTotalWeaponList().remove(mount);
        }
        if (bayWeapons.containsKey(mount)) {
            bayWeapons.get(mount).forEach(w -> {
                removeCriticals(unit, w);
                changeMountStatus(unit, w, Entity.LOC_NONE, Entity.LOC_NONE, false);
            });
            bayAmmo.get(mount).forEach(a -> {
                removeCriticals(unit, a);
                Mounted moveTo = UnitUtil.findUnallocatedAmmo(unit, a.getType());
                if (null != moveTo) {
                    moveTo.setShotsLeft(moveTo.getBaseShotsLeft() + a.getBaseShotsLeft());
                    UnitUtil.removeMounted(unit, a);
                }
                changeMountStatus(unit, a, Entity.LOC_NONE, Entity.LOC_NONE, false);
            });
            bayWeapons.remove(mount);
            bayAmmo.remove(mount);
        }
        for (Mounted bay : bayWeapons.keySet()) {
            bay.getBayWeapons().clear();
            for (Mounted w : bayWeapons.get(bay)) {
                if (mount != w) {
                    bay.getBayWeapons().add(unit.getEquipmentNum(w));
                }
            }
        }
        for (Mounted bay : bayAmmo.keySet()) {
            bay.getBayAmmo().clear();
            for (Mounted a : bayAmmo.get(bay)) {
                if (mount != a) {
                    bay.getBayAmmo().add(unit.getEquipmentNum(a));
                }
            }
        }
        // Remove ammo added for a one-shot launcher
        if ((mount.getType() instanceof WeaponType) && mount.isOneShot()) {
            List<Mounted> osAmmo = new ArrayList<>();
            for (Mounted ammo = mount.getLinked(); ammo != null; ammo = ammo.getLinked()) {
                osAmmo.add(ammo);
            }
            osAmmo.forEach(m -> {
                unit.getEquipment().remove(m);
                unit.getAmmo().remove(m);
            });
        }
        // It's possible that the equipment we are removing was linked to
        // something else, and so the linkedBy state may be set.  We should
        // remove it.  Using getLinked could be unreliable, so we'll brute force
        // it
        // An example of this would be removing a linked Artemis IV FCS
        for (Mounted m : unit.getEquipment()) {
            if (mount.equals(m.getLinkedBy())) {
                m.setLinkedBy(null);
            }
        }
        if ((mount.getType() instanceof MiscType)
                && (mount.getType().hasFlag(MiscType.F_HEAD_TURRET)
                || mount.getType().hasFlag(MiscType.F_SHOULDER_TURRET)
                || mount.getType().hasFlag(MiscType.F_QUAD_TURRET))) {
            for (Mounted m : unit.getEquipment()) {
                if (m.getLocation() == mount.getLocation()) {
                    m.setMechTurretMounted(false);
                }
            }
        }
        if ((mount.getType() instanceof MiscType)
                && mount.getType().hasFlag(MiscType.F_SPONSON_TURRET)) {
            for (Mounted m : unit.getEquipment()) {
                m.setSponsonTurretMounted(false);
            }
        }
        if ((mount.getType() instanceof MiscType)
                && mount.getType().hasFlag(MiscType.F_PINTLE_TURRET)) {
            for (Mounted m : unit.getEquipment()) {
                if (m.getLocation() == mount.getLocation()) {
                    m.setPintleTurretMounted(false);
                }
            }
        }
    }

    public static void addMounted(Entity unit, Mounted mounted, int loc,
                                  boolean rearMounted) throws LocationFullException {
        unit.addEquipment(mounted, loc, rearMounted);
        mounted.setOmniPodMounted(canPodMount(unit, mounted));
    }

    /**
     * Tells if param EQ is a targetting computer.
     *
     * @param eq Mounted that might be a targetting computer
     * @return True if is a targetting computer false if not.
     */
    public static boolean isTargettingComputer(Mounted eq) {
        if ((eq.getType() instanceof MiscType)
                && eq.getType().hasFlag(MiscType.F_TARGCOMP)) {
            return true;
        }

        return false;
    }

    /**
     * Reset all the Crits and Mounts on the Unit.
     *
     * @param unit
     */
    public static void resetCriticalsAndMounts(Mech unit) {
        for (int location = Mech.LOC_HEAD; location <= Mech.LOC_LLEG; location++) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                CriticalSlot cs = unit.getCritical(location, slot);

                if ((cs != null)
                        && (cs.getType() == CriticalSlot.TYPE_EQUIPMENT)) {
                    cs = null;
                    unit.setCritical(location, slot, cs);
                }
            }
        }

        for (Mounted mount : unit.getEquipment()) {
            mount.setLocation(Entity.LOC_NONE, false);
        }

    }

    /**
     * Check to see if the unit is using Clan TC
     *
     * @param unit
     * @return
     */
    public static boolean hasClanTC(Mech unit) {

        for (Mounted mount : unit.getMisc()) {
            if (mount.getType().hasFlag(MiscType.F_TARGCOMP)
                    && TechConstants.isClan(mount.getType().getTechLevel(
                    unit.getTechLevelYear()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates TC Crits and Mounts based on weapons on a unit or if the TC has
     * been removed.
     *
     * @param unit
     */
    public static Mounted updateTC(Entity unit, EquipmentType tc) {
        UnitUtil.removeTC(unit);
        return UnitUtil.createTCMounts(unit, tc);
    }

    /**
     * Creates TC Mount.
     *
     * @param unit
     */
    public static Mounted createTCMounts(Entity unit, EquipmentType tc) {
        Mounted mount = null;
        try {
            mount = unit.addEquipment(tc, Entity.LOC_NONE);
        } catch (Exception ex) {
        }
        return mount;
    }

    /**
     * Checks to see if unit can use the techlevel
     *
     * @param unit
     * @param tech
     * @return Boolean if the tech level is legal for the passed unit
     */
    public static boolean isLegal(Entity unit, ITechnology tech) {
        if (unit.isMixedTech()) {
            if (!tech.isAvailableIn(unit.getTechLevelYear())) {
                return false;
            }
        } else {
            if (tech.getTechBase() != ITechnology.TECH_BASE_ALL
                    && unit.isClan() != tech.isClan()) {
                return false;
            }
            if (!tech.isAvailableIn(unit.getTechLevelYear(), unit.isClan())) {
                return false;
            }
        }
        return TechConstants.convertFromNormalToSimple(tech.getTechLevel(unit.getTechLevelYear(),
                unit.isClan())) <= TechConstants.convertFromNormalToSimple(unit.getTechLevel());
    }

    /**
     * Checks if the unit has laser heatsinks.
     *
     * @param unit
     * @return
     */
    public static boolean hasLaserHeatSinks(Mech unit) {

        if (!unit.hasDoubleHeatSinks()) {
            return false;
        }

        for (Mounted mounted : unit.getMisc()) {
            if (mounted.getType().hasFlag(MiscType.F_LASER_HEAT_SINK)) {
                return true;
            }
        }

        return false;
    }

    /***
     * Checks for Clan DHS
     *
     * @param unit
     * @return
     */
    public static boolean hasClanDoubleHeatSinks(Mech unit) {

        if (!unit.hasDoubleHeatSinks()) {
            return false;
        }

        for (Mounted mounted : unit.getMisc()) {
            if (mounted.getType().hasFlag(MiscType.F_LASER_HEAT_SINK)) {
                return false;
            }

            if (mounted.getType().hasFlag(MiscType.F_DOUBLE_HEAT_SINK)) {
                if (mounted.getType().getInternalName()
                        .equals("CLDoubleHeatSink")) {
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /**
     * Checks if EquipmentType is a Mech Physical weapon
     *
     * @param eq
     * @return
     */
    public static boolean isPhysicalWeapon(EquipmentType eq) {

        if ((eq instanceof MiscType)
                && ((eq.hasFlag(MiscType.F_CLUB)
                || eq.hasFlag(MiscType.F_HAND_WEAPON) || eq
                .hasFlag(MiscType.F_TALON)))) {
            if (eq.hasFlag(MiscType.F_CLUB)
                    && ((eq.hasSubType(MiscType.S_CLUB) || eq
                    .hasSubType(MiscType.S_TREE_CLUB)))) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Removes the specified number of heat sinks from the mek Heat sinks are
     * removed first fwith LOC_NONE above the free crit limit then they are
     * removed with a location, and lastly they are removed below the free crit
     * limit
     *
     * @param unit
     */
    public static void removeHeatSinks(Mech unit, int number) {
        final String METHOD_NAME = "removeHeatSinks(Mech, int)";

        Vector<Mounted> toRemove = new Vector<Mounted>();
        int base = UnitUtil.getCriticalFreeHeatSinks(unit,
                unit.hasCompactHeatSinks());
        boolean splitCompact = false;
        if (unit.hasCompactHeatSinks()) {
            // first check to see if there is a single compact heat sink outside
            // of
            // the engine and remove this first if so
            Mounted mount = UnitUtil.getSingleCompactHeatSink(unit);
            if ((null != mount) && (number > 0)) {
                UnitUtil.removeMounted(unit, mount);
                number--;
            }
            // if number is now uneven, then note that we will need to split a
            // compact
            if ((number % 2) == 1) {
                splitCompact = true;
                number--;
            }
        }
        Vector<Mounted> unassigned = new Vector<Mounted>();
        Vector<Mounted> assigned = new Vector<Mounted>();
        Vector<Mounted> free = new Vector<Mounted>();
        for (Mounted m : unit.getMisc()) {
            if (UnitUtil.isHeatSink(m)) {
                if (m.getLocation() == Entity.LOC_NONE) {
                    if (base > 0) {
                        free.add(m);
                        base--;
                    } else {
                        unassigned.add(m);
                    }
                } else {
                    assigned.add(m);
                }
            }
        }
        toRemove.addAll(unassigned);
        toRemove.addAll(assigned);
        toRemove.addAll(free);
        if (unit.hasCompactHeatSinks()) {
            // need to do some number magic here. The unassigned and assigned
            // slots
            // should each contain two heat sinks, but if we dip into the free
            // then we
            // are looking at one heat sink.
            int numberDouble = Math.min(number / 2, unassigned.size()
                    + assigned.size());
            int numberSingle = Math.max(0, number - (2 * numberDouble));
            number = numberDouble + numberSingle;
        }
        number = Math.min(number, toRemove.size());
        for (int i = 0; i < number; i++) {
            Mounted eq = toRemove.get(i);
            UnitUtil.removeMounted(unit, eq);
        }
        if (splitCompact) {
            Mounted eq = toRemove.get(number);
            int loc = eq.getLocation();
            // remove singleCompact mount and replace with a double
            UnitUtil.removeMounted(unit, eq);
            if (!eq.getType().hasFlag(MiscType.F_HEAT_SINK)) {
                try {
                    UnitUtil.addMounted(unit,
                            new Mounted(unit, EquipmentType
                                    .get("IS1 Compact Heat Sink")), loc, false);
                } catch (Exception ex) {
                    log.error("Error", ex);
                }
            }

        }
    }

    /**
     * adds all heat sinks to the mech
     *
     * @param unit
     * @param hsAmount
     * @param hsType
     */
    public static void addHeatSinkMounts(Mech unit, int hsAmount, String hsType) {
        addHeatSinkMounts(unit, hsAmount, EquipmentType.get(UnitUtil.getHeatSinkType(hsType, unit.isClan())));
    }

    /**
     * adds all heat sinks to the mech
     *
     * @param unit
     * @param hsAmount
     * @param sinkType
     */
    public static void addHeatSinkMounts(Mech unit, int hsAmount, EquipmentType sinkType) {
        final String METHOD_NAME = "addHeatSinkMounts(Mech, int, String)";

        if (sinkType.hasFlag(MiscType.F_COMPACT_HEAT_SINK)) {
            UnitUtil.addCompactHeatSinkMounts(unit, hsAmount);
        } else {
            for (; hsAmount > 0; hsAmount--) {
                try {
                    unit.addEquipment(new Mounted(unit, sinkType),
                            Entity.LOC_NONE, false);
                } catch (Exception ex) {
                    log.error("Error", ex);
                }
            }
        }
    }

    public static void addCompactHeatSinkMounts(Mech unit, int hsAmount) {
        final String METHOD_NAME = "addCompactHeatSinkMounts(Mech, int)";

        // first we need to figure out how many single compacts we need to add
        // for the engine, if any
        int currentSinks = UnitUtil.countActualHeatSinks(unit);
        int engineCompacts = Math.min(hsAmount,
                UnitUtil.getCriticalFreeHeatSinks(unit, true));
        int engineToAdd = Math.max(0, engineCompacts - currentSinks);
        unit.addEngineSinks("IS1 Compact Heat Sink", engineToAdd);
        int restHS = hsAmount - engineToAdd;
        Mounted singleCompact = getSingleCompactHeatSink(unit);
        if ((restHS % 2) == 1) {
            if (null == singleCompact) {
                try {
                    unit.addEquipment(new Mounted(unit, EquipmentType
                                    .get("IS1 Compact Heat Sink")),
                            Entity.LOC_NONE, false);
                } catch (Exception ex) {
                    log.error("Error", ex);
                }
            } else {
                int loc = singleCompact.getLocation();
                // remove singleCompact mount and replace with a double
                UnitUtil.removeMounted(unit, singleCompact);
                try {
                    addMounted(unit,
                            new Mounted(unit, EquipmentType.get(UnitUtil
                                    .getHeatSinkType("Compact", unit.isClan()))),
                            loc, false);
                } catch (Exception ex) {
                    log.error("Error", ex);
                }
            }
            restHS -= 1;
        }
        for (; restHS > 0; restHS -= 2) {
            try {
                unit.addEquipment(new Mounted(unit, EquipmentType.get(UnitUtil
                                .getHeatSinkType("Compact", unit.isClan()))),
                        Entity.LOC_NONE, false);
            } catch (Exception ex) {
                log.error("Error", ex);
            }
        }
    }

    /**
     * get the single non-compact heat sink that is a non-engine sink, if it
     * exits
     *
     * @param unit
     */
    public static Mounted getSingleCompactHeatSink(Mech unit) {
        int base = UnitUtil.getCriticalFreeHeatSinks(unit, true);
        for (Mounted m : unit.getMisc()) {
            if (m.getType().hasFlag(MiscType.F_COMPACT_HEAT_SINK)
                    && m.getType().hasFlag(MiscType.F_HEAT_SINK)) {
                if (base <= 0) {
                    return m;
                } else {
                    base--;
                }
            }
        }
        return null;
    }

    public static String getHeatSinkType(String type, boolean clan) {
        String heatSinkType;

        if (type.startsWith("(Clan)")) {
            clan = true;
            type = type.substring(7).trim();
        } else if (type.startsWith("(IS)")) {
            clan = false;
            type = type.substring(4).trim();
        }

        if (clan) {
            if (type.equals("Single")) {
                heatSinkType = EquipmentTypeLookup.SINGLE_HS;
            } else if (type.equals("Double")) {
                heatSinkType = EquipmentTypeLookup.CLAN_DOUBLE_HS;
            } else {
                heatSinkType = EquipmentTypeLookup.LASER_HS;
            }
        } else {
            if (type.equals("Single")) {
                heatSinkType = EquipmentTypeLookup.SINGLE_HS;
            } else if (type.equals("Double")) {
                heatSinkType = EquipmentTypeLookup.IS_DOUBLE_HS;
            } else {
                heatSinkType = EquipmentTypeLookup.COMPACT_HS_2;
            }
        }

        return heatSinkType;
    }

    public static boolean hasSameHeatSinkType(Mech unit, String type) {
        // this seems like a total hack, but at present we apparently have no
        // good static integer codes for this on entity
        String heatSinkType = UnitUtil.getHeatSinkType(type, unit.isClan());
        for (Mounted mounted : unit.getMisc()) {
            if (type.equals("Compact")
                    && mounted.getType().hasFlag(MiscType.F_COMPACT_HEAT_SINK)) {
                return true;
            }
            if (mounted.getType().hasFlag(MiscType.F_HEAT_SINK)
                    || mounted.getType().hasFlag(MiscType.F_DOUBLE_HEAT_SINK)
                    || mounted.getType().hasFlag(MiscType.F_LASER_HEAT_SINK)) {
                return mounted.getType().getInternalName().equals(heatSinkType);
            }
        }
        return false;
    }

    /**
     * updates the heat sinks.
     *
     * @param unit
     * @param hsAmount
     * @param hsType
     */
    public static void updateHeatSinks(Mech unit, int hsAmount, String hsType) {
        // if we have the same type of heat sink, then we should not remove the
        // existing heat sinks
        int currentSinks = UnitUtil.countActualHeatSinks(unit);
        if (UnitUtil.hasSameHeatSinkType(unit, hsType)) {
            if (hsAmount < currentSinks) {
                UnitUtil.removeHeatSinks(unit, currentSinks - hsAmount);
            } else if (hsAmount > currentSinks) {
                UnitUtil.addHeatSinkMounts(unit, hsAmount - currentSinks,
                        hsType);
            }
        } else {
            UnitUtil.removeHeatSinks(unit, hsAmount);
            UnitUtil.addHeatSinkMounts(unit, hsAmount, hsType);
        }
        unit.resetSinks();
    }

    /**
     * This will cycle through the heat sinks and make sure that enough of them
     * are set LOC_NONE based on the basechassisheat sinks
     *
     * @param unit
     */
    public static void updateAutoSinks(Mech unit, boolean compact) {
        int base = UnitUtil.getCriticalFreeHeatSinks(unit, compact);
        List<Mounted> unassigned = new ArrayList<>();
        List<Mounted> assigned = new ArrayList<>();
        for (Mounted m : unit.getMisc()) {
            if (UnitUtil.isHeatSink(m)) {
                if (m.getLocation() == Entity.LOC_NONE) {
                    unassigned.add(m);
                } else if (!m.getType().hasFlag(MiscType.F_IS_DOUBLE_HEAT_SINK_PROTOTYPE)) {
                    // Prototype double heat sinks can never be integrated into the engine
                    assigned.add(m);
                }
            }
        }
        int needed = base - unassigned.size();
        if (needed <= 0) {
            return;
        }
        for (Mounted m : assigned) {
            if (needed <= 0) {
                return;
            }
            UnitUtil.removeCriticals(unit, m);
            m.setLocation(Entity.LOC_NONE);
            needed--;
        }
        // There may be more crit-free heatsinks, but if the 'mech doesn't
        // have that many heatsinks, the additional space is unused.
    }

    /**
     * Sets the corresponding critical slots to null for the Mounted object.
     *
     * @param unit
     * @param eq
     */
    public static void removeCriticals(Entity unit, Mounted eq) {

        if (eq.getLocation() == Entity.LOC_NONE) {
            return;
        }
        for (int loc = 0; loc < unit.locations(); loc++) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(loc); slot++) {
                CriticalSlot cs = unit.getCritical(loc, slot);
                if ((cs != null)
                        && (cs.getType() == CriticalSlot.TYPE_EQUIPMENT)) {
                    if (cs.getMount().equals(eq)) {
                        // If there are two pieces of equipment in this slot,
                        // remove first one, and replace it with the second
                        if (cs.getMount2() != null) {
                            cs.setMount(cs.getMount2());
                            cs.setMount2(null);
                        } else { // If it's the only Mounted, clear the slot
                            cs = null;
                            unit.setCritical(loc, slot, cs);
                        }
                    } else if ((cs.getMount2() != null)
                            && cs.getMount2().equals(eq)) {
                        cs.setMount2(null);
                    }
                }
            }
        }
    }

    /**
     * checks if Mounted is a heat sink
     *
     * @param eq
     * @return
     */
    public static boolean isHeatSink(Mounted eq) {
        return ((eq.getType() != null) && isHeatSink(eq.getType()));
    }

    /**
     * Checks if EquipmentType is a heat sink
     *
     * @param eq
     * @return
     */
    public static boolean isHeatSink(EquipmentType eq) {
        return isHeatSink(eq, false);
    }

    public static boolean isHeatSink(EquipmentType eq, boolean ignoreprototype) {
        if ((eq instanceof MiscType)
                && (eq.hasFlag(MiscType.F_HEAT_SINK)
                || eq.hasFlag(MiscType.F_LASER_HEAT_SINK)
                || eq.hasFlag(MiscType.F_DOUBLE_HEAT_SINK) || (eq
                .hasFlag(MiscType.F_IS_DOUBLE_HEAT_SINK_PROTOTYPE) && !ignoreprototype))) {
            return true;
        }

        return false;
    }

    /**
     * Return the number of critical-space free heatsinks that the given entity
     * can have.
     *
     * @param unit    The unit mounting the heatsinks
     * @param compact Whether the heatsinks are compact or not
     * @return T he number of critical-free heat sinks.
     */
    public static int getCriticalFreeHeatSinks(Entity unit, boolean compact) {
        int engineHSCapacity = unit.getEngine().integralHeatSinkCapacity(
                compact);

        if (unit.isOmni()) {
            engineHSCapacity = Math.min(engineHSCapacity, unit.getEngine()
                    .getBaseChassisHeatSinks(compact));
        }

        return engineHSCapacity;
    }

    public static boolean isJumpJet(Mounted m) {
        return (m.getType() instanceof MiscType) &&
                (m.getType().hasFlag(MiscType.F_JUMP_JET)
                        || m.getType().hasFlag(MiscType.F_JUMP_BOOSTER));
    }

    /**
     * @param type The value returned by {@link Mech#getJumpType()}
     * @return The {@link EquipmentType} lookup key for the jump jet
     */
    public static String getJumpJetType(int type) {
        if (type == Mech.JUMP_IMPROVED) {
            return EquipmentTypeLookup.IMPROVED_JUMP_JET;
        } else if (type == Mech.JUMP_PROTOTYPE) {
            return EquipmentTypeLookup.PROTOTYPE_JUMP_JET;
        } else if (type == Mech.JUMP_BOOSTER) {
            return EquipmentTypeLookup.MECH_JUMP_BOOSTER;
        } else if (type == Mech.JUMP_PROTOTYPE_IMPROVED) {
            return EquipmentTypeLookup.PROTOTYPE_IMPROVED_JJ;
        }
        return EquipmentTypeLookup.JUMP_JET;
    }

    /**
     * Removes all jump jets from the mek
     *
     * @param unit
     */
    public static void removeJumpJets(Mech unit, int number) {
        Vector<Mounted> toRemove = new Vector<Mounted>();
        ArrayList<Mounted> misceq = unit.getMisc();
        for (Mounted eq : misceq) {
            if (UnitUtil.isJumpJet(eq)) {
                toRemove.add(eq);
                if (toRemove.size() >= number) {
                    break;
                }
            }
        }
        for (Mounted eq : toRemove) {
            UnitUtil.removeMounted(unit, eq);
        }
    }

    /**
     * updates the Jump Jets.
     *
     * @param unit
     * @param jjAmount
     * @param jjType
     */
    public static void updateJumpJets(Mech unit, int jjAmount, int jjType) {
        final String METHOD_NAME = "updateJumpJets(Mech, int, int)";

        unit.setOriginalJumpMP(jjAmount);
        int ctype = unit.getJumpType();
        if (jjType == ctype) {
            int currentJJ = (int) unit.getMisc().stream().filter(m -> m.getType()
                    .hasFlag(MiscType.F_JUMP_JET))
                    .count();
            if (jjAmount < currentJJ) {
                UnitUtil.removeJumpJets(unit, currentJJ - jjAmount);
                return;
            } else if (jjAmount > currentJJ) {
                jjAmount = jjAmount - currentJJ;
            } else {
                return; // No change, get the fuck outta here!
            }
        } else {
            UnitUtil.removeJumpJets(unit, unit.getJumpMP());
        }
        // if this is the same jump jet type, then only remove if too many
        // and add if too low
        if (jjType == Mech.JUMP_BOOSTER) {
            UnitUtil.removeJumpJets(unit, unit.getJumpMP());
            createSpreadMounts(
                    unit,
                    EquipmentType.get(UnitUtil.getJumpJetType(jjType)));
        } else {
            while (jjAmount > 0) {
                try {
                    unit.addEquipment(
                            new Mounted(unit, EquipmentType.get(UnitUtil
                                    .getJumpJetType(jjType))),
                            Entity.LOC_NONE, false);
                } catch (Exception ex) {
                    log.error("Error", ex);
                }
                jjAmount--;
            }
        }
    }

    /**
     * Checks whether equipment can be linked to a weapon to enhance it (e.g. Artemis, PPC Capacitor, etc).
     *
     * @param type The equipment to check
     * @return true if the equipment is a MiscType that can be linked to a weapon.
     */
    public static boolean isWeaponEnhancement(EquipmentType type) {
        return (type instanceof MiscType)
                && (type.hasFlag(MiscType.F_ARTEMIS)
                || type.hasFlag(MiscType.F_ARTEMIS_V)
                || type.hasFlag(MiscType.F_ARTEMIS_PROTO)
                || type.hasFlag(MiscType.F_APOLLO)
                || type.hasFlag(MiscType.F_PPC_CAPACITOR)
                || type.hasFlag(MiscType.F_RISC_LASER_PULSE_MODULE));
    }

    /**
     * Removes all enhancements (TSM and MASC) from the mek
     *
     * @param unit
     */
    public static void removeEnhancements(Mech unit) {
        ConcurrentLinkedQueue<Mounted> equipmentList = new ConcurrentLinkedQueue<Mounted>(
                unit.getMisc());
        for (Mounted eq : equipmentList) {
            if (UnitUtil.isTSM(eq.getType()) || UnitUtil.isMASC(eq.getType())
                    || ((eq.getType() instanceof MiscType) && eq.getType().hasFlag(MiscType.F_SCM))) {
                UnitUtil.removeCriticals(unit, eq);
            }
        }
        for (Mounted eq : equipmentList) {
            if (UnitUtil.isTSM(eq.getType()) || UnitUtil.isMASC(eq.getType())
                    || ((eq.getType() instanceof MiscType) && eq.getType().hasFlag(MiscType.F_SCM))) {
                unit.getMisc().remove(eq);
                unit.getEquipment().remove(eq);
            }
        }
    }

    /**
     * tells if EquipmentType is TSM or TargetComp
     *
     * @param eq
     * @return
     */
    public static boolean isTSM(EquipmentType eq) {
        return (eq instanceof MiscType)
                && (eq.hasFlag(MiscType.F_TSM) || eq
                .hasFlag((MiscType.F_INDUSTRIAL_TSM)));
    }

    /**
     * tells if EquipmentType is MASC
     *
     * @param eq
     * @return
     */
    public static boolean isMASC(EquipmentType eq) {
        return (eq instanceof MiscType)
                && (eq.hasFlag(MiscType.F_MASC) && !eq.hasSubType(MiscType.S_SUPERCHARGER));
    }

    public static boolean isPrintableEquipment(EquipmentType eq) {
        return UnitUtil.isPrintableEquipment(eq, false);
    }

    /**
     * simple method to let us know if eq should be printed on the weapons and
     * equipment section of the Record sheet.
     *
     * @param eq     The equipment
     * @param isMech Whether the equipment is mounted on a mech
     * @return Whether the equipment should be shown on the record sheet
     */
    public static boolean isPrintableEquipment(EquipmentType eq, boolean isMech) {

        if (UnitUtil.isArmorOrStructure(eq)) {
            return false;
        }

        if (UnitUtil.isFixedLocationSpreadEquipment(eq)
                && !(eq instanceof MiscType) && eq.hasFlag(MiscType.F_TALON)) {
            return false;
        }

        if (UnitUtil.isJumpJet(eq)) {
            return false;
        }
        if (!eq.isHittable() && isMech) {
            return false;
        }

        if ((eq instanceof MiscType)
                && (eq.hasFlag(MiscType.F_CASE)
                || eq.hasFlag(MiscType.F_ARTEMIS)
                || eq.hasFlag(MiscType.F_ARTEMIS_PROTO)
                || eq.hasFlag(MiscType.F_ARTEMIS_V)
                || eq.hasFlag(MiscType.F_APOLLO)
                || eq.hasFlag(MiscType.F_PPC_CAPACITOR)
                || (eq.hasFlag(MiscType.F_MASC) && isMech)
                || eq.hasFlag(MiscType.F_HARJEL)
                || eq.hasFlag(MiscType.F_MASS)
                || eq.hasFlag(MiscType.F_CHASSIS_MODIFICATION)
                || eq.hasFlag(MiscType.F_SPONSON_TURRET))
                || eq.hasFlag(MiscType.F_EXTERNAL_STORES_HARDPOINT)
                || eq.hasFlag(MiscType.F_BASIC_FIRECONTROL)
                || eq.hasFlag(MiscType.F_ADVANCED_FIRECONTROL)) {
            return false;
        }

        if (UnitUtil.isHeatSink(eq)) {
            return false;
        }

        return true;

    }

    /**
     * tells is EquipementType is an equipment that uses crits/mounted and is
     * spread across multiple locations
     *
     * @param eq
     * @return
     */
    public static boolean isFixedLocationSpreadEquipment(EquipmentType eq) {
        return (eq instanceof MiscType)
                && (eq.hasFlag(MiscType.F_JUMP_BOOSTER)
                || eq.hasFlag(MiscType.F_BA_MANIPULATOR)
                || eq.hasFlag(MiscType.F_PARTIAL_WING)
                || eq.hasFlag(MiscType.F_NULLSIG)
                || eq.hasFlag(MiscType.F_VOIDSIG)
                || eq.hasFlag(MiscType.F_ENVIRONMENTAL_SEALING)
                || eq.hasFlag(MiscType.F_TRACKS)
                || eq.hasFlag(MiscType.F_TALON)
                || (eq.hasFlag(MiscType.F_STEALTH) && eq
                .hasFlag(MiscType.F_MECH_EQUIPMENT))
                || eq.hasFlag(MiscType.F_CHAMELEON_SHIELD)
                || eq.hasFlag(MiscType.F_BLUE_SHIELD)
                || eq.hasFlag(MiscType.F_MAST_MOUNT)
                || eq.hasFlag(MiscType.F_SCM)
                || (eq.hasFlag(MiscType.F_JUMP_JET) && eq.hasFlag(MiscType.F_PROTOMECH_EQUIPMENT))
                || (eq.hasFlag(MiscType.F_UMU) && eq.hasFlag(MiscType.F_PROTOMECH_EQUIPMENT))
                || (eq.hasFlag(MiscType.F_MAGNETIC_CLAMP) && eq.hasFlag(MiscType.F_PROTOMECH_EQUIPMENT))
                || (eq.hasFlag(MiscType.F_MASC) && eq.hasFlag(MiscType.F_PROTOMECH_EQUIPMENT)));
    }

    /**
     * Checks to see if something is a Jump Jet
     *
     * @param eq
     * @return
     */
    public static boolean isJumpJet(EquipmentType eq) {
        if ((eq instanceof MiscType)
                && (eq.hasFlag(MiscType.F_JUMP_JET)
                || eq.hasFlag(MiscType.F_UMU)
                || eq.hasFlag(MiscType.F_BA_VTOL))) {
            return true;
        }

        return false;
    }

    public static boolean isArmorOrStructure(EquipmentType eq) {

        return UnitUtil.isArmor(eq) || UnitUtil.isStructure(eq);
    }

    /**
     * tells if the EquipmentType is a type of armor
     *
     * @param eq
     * @return
     */
    public static boolean isArmor(EquipmentType eq) {
        return Arrays.asList(EquipmentType.armorNames).contains(eq.getName());
    }

    /**
     * tells if the EquipmentType is a type of armor
     *
     * @param eq
     * @return
     */
    public static boolean isStructure(EquipmentType eq) {
        for (String armor : EquipmentType.structureNames) {
            if (eq.getName().equals(armor)) {
                return true;
            }
        }

        return false;
    }

    /**
     * simple method to let us know if eq should be printed on the weapons and
     * equipment section of the Record sheet.
     *
     * @param eq     The equipment
     * @param entity The Entity it's mounted on
     * @return Whether the equipment should be shown on the record sheet
     */
    public static boolean isPrintableEquipment(EquipmentType eq, Entity entity) {
        if (entity instanceof BattleArmor) {
            return isPrintableBAEquipment(eq);
        }
        return isPrintableEquipment(eq, entity instanceof Mech);
    }

    /**
     * simple method to let us know if eq should be printed on the weapons and
     * equipment section of the Record sheet.
     *
     * @param eq
     * @return
     */
    public static boolean isPrintableBAEquipment(EquipmentType eq) {

        if (UnitUtil.isArmorOrStructure(eq)) {
            return false;
        }

        if (UnitUtil.isJumpJet(eq)) {
            return false;
        }

        if ((eq instanceof MiscType)
                && ((eq.hasFlag(MiscType.F_AP_MOUNT) && !eq.hasFlag(MiscType.F_BA_MANIPULATOR))
                || eq.hasFlag(MiscType.F_FIRE_RESISTANT)
                || eq.hasFlag(MiscType.F_STEALTH)
                || eq.hasFlag(MiscType.F_ARTEMIS)
                || eq.hasFlag(MiscType.F_ARTEMIS_V)
                || eq.hasFlag(MiscType.F_APOLLO)
                || eq.hasFlag(MiscType.F_HARJEL)
                || eq.hasFlag(MiscType.F_MASS)
                || eq.hasFlag(MiscType.F_DETACHABLE_WEAPON_PACK))) {
            return false;
        }

        if (UnitUtil.isHeatSink(eq)) {
            return false;
        }

        if ((eq instanceof LegAttack) || (eq instanceof SwarmAttack)
                || (eq instanceof StopSwarmAttack)
                || (eq instanceof InfantryRifleAutoRifleWeapon)
                || (eq instanceof SwarmWeaponAttack)) {
            return false;
        }

        return true;
    }

    public static boolean isBAMultiMount(EquipmentType equip) {
        if ((equip instanceof WeaponType)
                && (equip.hasFlag(WeaponType.F_TASER)
                || (((WeaponType) equip).getAmmoType()
                == AmmoType.T_NARC))) {
            return true;
        }
        return false;
    }

    public static void resizeMount(Mounted mount, double newSize) {
        mount.setSize(newSize);
        if (mount.getLocation() == Entity.LOC_NONE) {
            return;
        }
        final Entity entity = mount.getEntity();
        final int loc = mount.getLocation();
        int start = -1;
        for (int slot = 0; slot < entity.getNumberOfCriticals(loc); slot++) {
            CriticalSlot crit = entity.getCritical(loc, slot);
            if ((crit != null) && (crit.getType() == CriticalSlot.TYPE_EQUIPMENT)
                    && crit.getMount().equals(mount)) {
                start = slot;
                break;
            }
        }
        removeCriticals(entity, mount);
        compactCriticals(entity, loc);
        if ((start < 0) || (entity.getEmptyCriticals(loc) < mount.getCriticals())) {
            changeMountStatus(entity, mount, Entity.LOC_NONE, Entity.LOC_NONE, false);
        } else {
            // If the number of criticals increases, we may need to shift existing criticals
            // to make room. Since we checked for sufficient space and compacted the existing
            // criticals we can be assured of not overrunning the array.
            List<CriticalSlot> toAdd = new ArrayList<>();
            for (int i = 0; i < mount.getCriticals(); i++) {
                toAdd.add(new CriticalSlot(mount));
            }
            int slot = start;
            while (!toAdd.isEmpty()) {
                CriticalSlot cs = entity.getCritical(loc, slot);
                if (cs != null) {
                    toAdd.add(cs);
                }
                entity.setCritical(loc, slot, toAdd.get(0));
                toAdd.remove(0);
                slot++;
            }
        }
    }

    /**
     * Changes the location for a Mounted instance.  Note: for BattleArmor, this
     * effects which suit the equipment is placed on (as that is what
     * Mounted.location means for BA), but not where on the suit
     * it's located (ie, BAMountLocation isn't affected).  BattleArmor should
     * change this outside of this method.
     *
     * @param unit              The unit being modified
     * @param eq                The equipment mount to move
     * @param location          The location to move the mount to
     * @param secondaryLocation The secondary location for split equipment, otherwise {@link Entity#LOC_NONE Entity.LOC_NONE}
     * @param rear              Whether to mount with a rear facing
     */
    public static void changeMountStatus(Entity unit, Mounted eq, int location,
                                         int secondaryLocation, boolean rear) {
        if (location != eq.getLocation()) {
            if (eq.getLinked() != null) {
                eq.getLinked().setLinkedBy(null);
                eq.setLinked(null);
            }
            if (eq.getLinkedBy() != null) {
                eq.getLinkedBy().setLinked(null);
                eq.setLinkedBy(null);
            }
        }
        eq.setLocation(location, rear);
        eq.setSecondLocation(secondaryLocation, rear);
        eq.setSplit(secondaryLocation > -1);
        // If we're adding it to a location on the unit, check equipment linkages
        if (location > Entity.LOC_NONE) {
            try {
                MechFileParser.postLoadInit(unit);
            } catch (EntityLoadingException ignored) {
                // Exception thrown for not having equipment to link to yet, which is acceptable here
            }
        }
    }

    public static void compactCriticals(Entity unit, int loc) {
        int firstEmpty = -1;
        for (int slot = 0; slot < unit.getNumberOfCriticals(loc); slot++) {
            CriticalSlot cs = unit.getCritical(loc, slot);

            if ((cs == null) && (firstEmpty == -1)) {
                firstEmpty = slot;
            }
            if ((firstEmpty != -1) && (cs != null)) {
                // move this to the first empty slot
                unit.setCritical(loc, firstEmpty, cs);
                // mark the old slot empty
                unit.setCritical(loc, slot, null);
                // restart just after the moved slot's new location
                slot = firstEmpty;
                firstEmpty = -1;
            }
        }
    }

    /**
     * Find unallocated ammo of the same type. Used by large aerospace units when removing ammo
     * from a location to find the group to add it to.
     *
     * @param unit The Entity
     * @param at   The type of armor to match
     * @return An unallocated non-oneshot ammo mount of the same type, or null if there is not one.
     */
    public static Mounted findUnallocatedAmmo(Entity unit, EquipmentType at) {
        for (Mounted m : unit.getAmmo()) {
            if ((m.getLocation() == Entity.LOC_NONE)
                    && (m.getType() == at)
                    && ((m.getLinkedBy() == null)
                    || !m.getLinkedBy().getType().hasFlag(WeaponType.F_ONESHOT))) {
                return m;
            }
        }
        return null;
    }

    /**
     * Checks whether the equipment is eligible for pod mounting in an omni unit, either because the
     * equipment itself can never be pod-mounted (such as armor, structure, or myomer enhancements),
     * or the number of fixed heat sinks have not been assigned locations.
     *
     * @param unit
     * @param eq
     * @return
     */
    public static boolean canPodMount(Entity unit, Mounted eq) {
        if (!unit.isOmni() || eq.getType().isOmniFixedOnly()) {
            return false;
        }

        if (eq.getType() instanceof MiscType && unit instanceof Mech
                && (eq.getType().hasFlag(MiscType.F_HEAT_SINK)
                || eq.getType().hasFlag(MiscType.F_DOUBLE_HEAT_SINK)
                || eq.getType().hasFlag(MiscType.F_IS_DOUBLE_HEAT_SINK_PROTOTYPE))
                && unit.hasEngine()) {
            int needed = Math.max(0, unit.getEngine().getWeightFreeEngineHeatSinks() -
                    UnitUtil.getCriticalFreeHeatSinks(unit, ((Mech) unit).hasCompactHeatSinks()));
            long fixed = unit.getMisc().stream().filter(m ->
                    (m.getType().hasFlag(MiscType.F_HEAT_SINK)
                            || m.getType().hasFlag(MiscType.F_DOUBLE_HEAT_SINK)
                            || m.getType().hasFlag(MiscType.F_IS_DOUBLE_HEAT_SINK_PROTOTYPE))
                            && m.getLocation() != Entity.LOC_NONE && !m.isOmniPodMounted()).count();
            //Do not count this heat among the fixed, since we are checking whether we can change it to pod-mounted
            if (eq.getLocation() != Entity.LOC_NONE && !eq.isOmniPodMounted()) {
                fixed--;
            }
            return fixed >= needed;
        }
        return true;
    }

    /**
     * Removes all pod-mounted equipment from an omni unit
     *
     * @param unit
     */
    public static void resetBaseChassis(Entity unit) {
        if (!unit.isOmni()) {
            return;
        }
        List<Mounted> pods = unit.getEquipment().stream()
                .filter(Mounted::isOmniPodMounted)
                .collect(Collectors.toList());
        for (Mounted m : pods) {
            UnitUtil.removeMounted(unit, m);
            if (m.getType() instanceof MiscType
                    && m.getType().hasFlag(MiscType.F_JUMP_JET)) {
                unit.setOriginalJumpMP(unit.getOriginalJumpMP() - 1);
            }
        }
        List<Transporter> transporters = unit.getTransports().stream()
                .filter(unit::isPodMountedTransport).collect(Collectors.toList());
        transporters.forEach(unit::removeTransporter);
    }

    public static boolean hasTargComp(Entity unit) {

        for (Mounted mount : unit.getEquipment()) {
            if ((mount.getType() instanceof MiscType)
                    && mount.getType().hasFlag(MiscType.F_TARGCOMP)) {
                return true;
            }
        }

        return false;
    }

    public static Mounted getTargComp(Entity unit) {
        for (Mounted misc : unit.getMisc()) {
            if (misc.getType().hasFlag(MiscType.F_TARGCOMP)) {
                return misc;
            }
        }
        return null;
    }

    public static int[] getHighestContinuousNumberOfCritsArray(Mech unit) {
        int[] critSpaces = new int[]{0, 0, 0, 0, 0, 0, 0, 0};

        for (int loc = 0; loc <= Mech.LOC_LLEG; loc++) {
            critSpaces[loc] = UnitUtil.getHighestContinuousNumberOfCrits(unit,
                    loc);
        }

        return critSpaces;
    }

    public static int getHighestContinuousNumberOfCrits(Entity unit,
                                                        int location) {
        int highestNumberOfCrits = 0;
        int currentCritCount = 0;

        // Handle locations without crits
        if ((location == Entity.LOC_DESTROYED)
                || (location == Entity.LOC_NONE)) {
            return 0;
        }

        for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
            if (unit.getCritical(location, slot) == null) {
                currentCritCount++;
            } else {
                currentCritCount = 0;
            }
            highestNumberOfCrits = Math.max(currentCritCount,
                    highestNumberOfCrits);
        }

        return highestNumberOfCrits;
    }

    /**
     * This method will return the number of contiguous criticals in the given
     * location, starting at the given critical slot
     *
     * @param unit         Unit to check critical slots on
     * @param location     The location on the unit to check slots on
     * @param startingSlot The critical slot to start at
     * @return
     */
    public static int getContiguousNumberOfCrits(Entity unit, int location,
                                                 int startingSlot) {

        int numCritSlots = unit.getNumberOfCriticals(location);
        int contiguousCrits = 0;

        for (int slot = startingSlot; slot < numCritSlots; slot++) {
            if (unit.getCritical(location, slot) == null) {
                contiguousCrits++;
            } else {
                break;
            }
        }
        return contiguousCrits;
    }

    public static double getUnallocatedAmmoTonnage(Entity unit) {
        double tonnage = 0;

        for (Mounted mount : unit.getAmmo()) {
            if ((mount.getLocation() == Entity.LOC_NONE) && !mount.isOneShotAmmo()) {
                int slots = 1;
                if (unit.usesWeaponBays()) {
                    slots = (int) Math.ceil(mount.getUsableShotsLeft() / (double) ((AmmoType) mount.getType()).getShots());
                }
                tonnage += slots * mount.getTonnage();
            }
        }

        return tonnage;
    }

    public static double getMaximumArmorTonnage(Entity unit) {

        double armorPerTon = 16.0 * EquipmentType.getArmorPointMultiplier(
                unit.getArmorType(1), unit.getArmorTechLevel(1));
        double armorWeight = 0;

        if (unit.getArmorType(1) == EquipmentType.T_ARMOR_HARDENED) {
            armorPerTon = 8.0;
        }
        if (unit instanceof Mech) {
            double points = (unit.getTotalInternal() * 2);
            // Add in extra armor points for head
            if (unit.isSuperHeavy()) {
                points += 4;
            } else {
                points += 3;
            }
            armorWeight = points / armorPerTon;
            armorWeight = Math.ceil(armorWeight * 2.0) / 2.0;
        } else if (unit instanceof Protomech) {
            double points = TestProtomech.maxArmorFactor((Protomech) unit);
            return points * TestProtomech.ProtomechArmor.getArmor((Protomech) unit).getWtPerPoint();
        } else if (unit instanceof Tank) {
            double points = Math.floor((unit.getWeight() * 3.5) + 40);
            armorWeight = points / armorPerTon;
            armorWeight = Math.ceil(armorWeight * 2.0) / 2.0;
        } else if (unit instanceof BattleArmor) {
            armorWeight = (unit.getWeightClass() * 4) + 2;
        } else if (unit instanceof SmallCraft) {
            return TestSmallCraft.maxArmorWeight((SmallCraft) unit);
        }
        return armorWeight;
    }

    /**
     * NOTE: only use for non-patchwork armor
     *
     * @param unit
     * @param armorTons
     * @return
     */
    public static int getArmorPoints(Entity unit, double armorTons) {
        int raw = (int) Math.floor(UnitUtil.getRawArmorPoints(unit, armorTons));
        return Math.min(raw, UnitUtil.getMaximumArmorPoints(unit));
    }

    public static int getMaximumArmorPoints(Entity unit) {
        int points = 0;
        if (unit.hasETypeFlag(Entity.ETYPE_MECH)) {
            int headPoints = 3;
            if (unit.getWeightClass() == EntityWeightClass.WEIGHT_SUPER_HEAVY) {
                headPoints = 4;
            }
            points = (unit.getTotalInternal() * 2) + headPoints;
        } else if (unit.hasETypeFlag(Entity.ETYPE_PROTOMECH)) {
            points = TestProtomech.maxArmorFactor((Protomech) unit);
        } else if (unit.hasETypeFlag(Entity.ETYPE_TANK)) {
            points = (int) Math.floor((unit.getWeight() * 3.5) + 40);
        } else if (unit.hasETypeFlag(Entity.ETYPE_BATTLEARMOR)) {
            points = (unit.getWeightClass() * 4) + 2;
        }
        return points;
    }

    /**
     * Computes the total number of armor points available to the unit for a given tonnage of armor.
     * This does not round down the calculation or take into account any maximum number of armor
     * points or tonnage allowed to the unit.
     * <p>
     * NOTE: only use for non-patchwork armor
     *
     * @param unit
     * @param armorTons
     * @return the number of armor points available for the armor tonnage
     */
    public static double getRawArmorPoints(Entity unit, double armorTons) {
        if (unit.hasETypeFlag(Entity.ETYPE_PROTOMECH)) {
            return Math.round(armorTons /
                    EquipmentType.getProtomechArmorWeightPerPoint(unit.getArmorType(Protomech.LOC_TORSO)));
        }
        return armorTons * UnitUtil.getArmorPointsPerTon(unit,
                unit.getArmorType(1), unit.getArmorTechLevel(1));
    }

    /**
     * Calculate the number of armor points per ton of armor for the given unit.
     *
     * @param en        The unit
     * @param at        The armor type constant
     * @param techLevel The {@link TechConstants} constant for the armor
     * @return The number of armor points per ton
     */
    public static double getArmorPointsPerTon(Entity en, int at, int techLevel) {
        return getArmorPointsPerTon(en, at, TechConstants.isClan(techLevel));
    }

    /**
     * Calculate the number of armor points per ton of armor for the given unit.
     *
     * @param en
     * @param at
     * @param clanArmor
     * @return
     */
    // TODO: aerospace and support vehicle armor
    public static double getArmorPointsPerTon(Entity en, int at, boolean clanArmor) {
        if (at == EquipmentType.T_ARMOR_HARDENED) {
            return 8.0;
        } else {
            return 16.0 * EquipmentType.getArmorPointMultiplier(at, clanArmor);
        }
    }


    /**
     * NOTE: only use for non-patchwork armor
     *
     * @param unit
     * @param armorTons
     * @return
     */
    public static int getArmorPoints(Entity unit, int loc, double armorTons) {
        double armorPerTon = 16.0 * EquipmentType.getArmorPointMultiplier(
                unit.getArmorType(loc), unit.getArmorTechLevel(loc));
        if (unit.getArmorType(loc) == EquipmentType.T_ARMOR_HARDENED) {
            armorPerTon = 8.0;
        }
        return Math.min((int) Math.floor(armorPerTon * armorTons),
                UnitUtil.getMaximumArmorPoints(unit, loc));
    }

    public static int getMaximumArmorPoints(Entity unit, int loc) {
        if ((unit instanceof Mech) && (loc == Mech.LOC_HEAD)) {
            if (unit.isSuperHeavy()) {
                return 12;
            } else {
                return 9;
            }
        } else if (unit instanceof Mech) {
            return unit.getInternal(loc) * 2;
        } else if (unit instanceof Tank) {
            if ((unit instanceof VTOL) && (loc == VTOL.LOC_ROTOR)) {
                return 2;
            }
            return (int) Math.floor((unit.getWeight() * 3.5) + 40);
        } else if (unit instanceof Protomech) {
            return TestProtomech.maxArmorFactor((Protomech) unit, loc);
        } else {
            return 0;
        }
    }

    public static void compactCriticals(Entity unit) {
        for (int loc = 0; loc < unit.locations(); loc++) {
            if (unit instanceof Mech) {
                UnitUtil.compactCriticals((Mech) unit, loc);
            } else {
                UnitUtil.compactCriticals(unit, loc);
            }
        }
    }

    private static void compactCriticals(Mech mech, int loc) {
        if (loc == Mech.LOC_HEAD) {
            // This location has an empty slot inbetween systems crits
            // which will mess up parsing if compacted.
            return;
        }
        int firstEmpty = -1;
        for (int slot = 0; slot < mech.getNumberOfCriticals(loc); slot++) {
            CriticalSlot cs = mech.getCritical(loc, slot);

            if ((cs == null) && (firstEmpty == -1)) {
                firstEmpty = slot;
            }
            if ((firstEmpty != -1) && (cs != null)) {
                // move this to the first empty slot
                mech.setCritical(loc, firstEmpty, cs);
                // mark the old slot empty
                mech.setCritical(loc, slot, null);
                // restart just after the moved slot's new location
                slot = firstEmpty;
                firstEmpty = -1;
            }
        }
    }

    /**
     * Determine the maximum number of armor points that can be mounted in a location.
     *
     * @param entity
     * @param location
     * @return The maximum number of armor points for the location, or null if there is no maximum.
     */
    public static @Nullable
    Integer getMaxArmor(Entity entity, int location) {
        if ((location < 0) || (location >= entity.locations())) {
            return 0;
        }
        if (entity.hasETypeFlag(Entity.ETYPE_MECH)) {
            if (location == Mech.LOC_HEAD) {
                return (entity.getWeightClass() == EntityWeightClass.WEIGHT_SUPER_HEAVY) ? 12 : 9;
            } else {
                return entity.getOInternal(location) * 2;
            }
        } else if (entity.hasETypeFlag(Entity.ETYPE_PROTOMECH)) {
            return TestProtomech.maxArmorFactor((Protomech) entity, location);
        } else if ((entity instanceof VTOL) && (location == VTOL.LOC_ROTOR)) {
            return 2;
        }
        return null;
    }

    public static void compactCriticals(Mech unit) {
        for (int loc = 0; loc < unit.locations(); loc++) {
            UnitUtil.compactCriticals(unit, loc);
        }
    }

    public static boolean hasSwitchableAmmo(WeaponType weapon) {

        if (weapon instanceof StreakLRMWeapon) {
            return false;
        }
        if (weapon instanceof StreakSRMWeapon) {
            return false;
        }
        if (weapon instanceof EnergyWeapon) {
            return false;
        }

        if (weapon instanceof GaussWeapon) {
            return false;
        }

        if (weapon instanceof UACWeapon) {
            return false;
        }

        if (weapon instanceof HVACWeapon) {
            return false;
        }

        if (weapon instanceof HAGWeapon) {
            return false;
        }

        if (weapon instanceof MGWeapon) {
            return false;
        }

        if (UnitUtil.isAMS(weapon)) {
            return false;
        }

        if (weapon instanceof ThunderBoltWeapon) {
            return false;
        }

        if (weapon instanceof CLChemicalLaserWeapon) {
            return false;
        }

        if (weapon instanceof MPodWeapon) {
            return false;
        }

        if (weapon instanceof BPodWeapon) {
            return false;
        }

        if (weapon instanceof ISPlasmaRifle) {
            return false;
        }

        if (weapon instanceof CLPlasmaCannon) {
            return false;
        }
        if (weapon instanceof VehicleFlamerWeapon) {
            return false;
        }
        if (!(weapon instanceof AmmoWeapon)) {
            return false;
        }
        if (weapon instanceof CLBALBX) {
            return false;
        }
        return true;
    }

    public static boolean isAMS(WeaponType weapon) {
        return weapon.hasFlag(WeaponType.F_AMS);
    }

    /**
     * create a Mounted and corresponding CriticalSlots for the passed in
     * <code>EquipmentType</code> on the passed in <code>Mech</code>
     *
     * @param unit
     * @param equip
     * @return
     */
    public static Mounted createSpreadMounts(Mech unit, EquipmentType equip) {
        final String METHOD_NAME = "createSpreadMounts(Mech, EquipmentType)";

        // how many non-spreadable contiguous blocks of crits?
        int blocks = 0;
        boolean isMisc = equip instanceof MiscType;

        blocks = equip.getCriticals(unit);

        List<Integer> locations = new ArrayList<Integer>();

        if (isMisc) {
            if ((equip.hasFlag(MiscType.F_INDUSTRIAL_TSM) || equip
                    .hasFlag(MiscType.F_TSM))) {
                // all crits user placeable
                for (int i = 0; i < equip.getCriticals(unit); i++) {
                    locations.add(Entity.LOC_NONE);
                }
            } else if (equip.hasFlag(MiscType.F_ENVIRONMENTAL_SEALING)) {
                // 1 crit in each location
                for (int i = 0; i < unit.locations(); i++) {
                    locations.add(i);
                }
            } else if (equip.hasFlag(MiscType.F_STEALTH)) {
                // 2 in arms, legs, side torsos
                locations.add(Mech.LOC_LLEG);
                locations.add(Mech.LOC_RLEG);
                locations.add(Mech.LOC_LARM);
                locations.add(Mech.LOC_RARM);
                locations.add(Mech.LOC_LT);
                locations.add(Mech.LOC_RT);
                blocks = 6;
                // Need to account for the center leg
                if (unit instanceof TripodMech) {
                    locations.add(Mech.LOC_CLEG);
                    blocks++;
                }
            } else if (equip.hasFlag(MiscType.F_SCM)) {
                // 1 in arms, legs, side torsos
                locations.add(Mech.LOC_LLEG);
                locations.add(Mech.LOC_RLEG);
                locations.add(Mech.LOC_LARM);
                locations.add(Mech.LOC_RARM);
                locations.add(Mech.LOC_LT);
                locations.add(Mech.LOC_RT);
                blocks = 6;
            } else if ((equip.hasFlag(MiscType.F_TRACKS)
                    || equip.hasFlag(MiscType.F_TALON) || equip
                    .hasFlag(MiscType.F_JUMP_BOOSTER))) {
                // 1 block in each leg
                locations.add(Mech.LOC_LLEG);
                locations.add(Mech.LOC_RLEG);
                if (unit instanceof QuadMech) {
                    locations.add(Mech.LOC_LARM);
                    locations.add(Mech.LOC_RARM);
                }
                blocks = (unit instanceof BipedMech ? 2 : 4);
                // Need to account for the center leg
                if (unit instanceof TripodMech) {
                    locations.add(Mech.LOC_CLEG);
                    blocks = 3;
                }
            } else if (equip.hasFlag(MiscType.F_PARTIAL_WING)) {
                // one block in each side torso
                locations.add(Mech.LOC_LT);
                locations.add(Mech.LOC_RT);
                blocks = 2;
            } else if ((equip.hasFlag(MiscType.F_VOIDSIG)
                    || equip.hasFlag(MiscType.F_NULLSIG) || equip
                    .hasFlag(MiscType.F_BLUE_SHIELD))) {
                // Need to account for the center leg
                if (unit instanceof TripodMech) {
                    blocks++;
                }
                // 1 crit in each location, except the head
                for (int i = Mech.LOC_CT; i < unit.locations(); i++) {
                    locations.add(i);
                }
            } else if (equip.hasFlag(MiscType.F_CHAMELEON_SHIELD)) {
                // Need to account for the center leg
                if (unit instanceof TripodMech) {
                    blocks++;
                }
                // 1 crit in each location except head and CT
                for (int i = Mech.LOC_RT; i < unit.locations(); i++) {
                    locations.add(i);
                }
            }
        }

        boolean firstBlock = true;
        Mounted mount = new Mounted(unit, equip);
        for (; blocks > 0; blocks--) {
            // how many crits per block?
            int crits = UnitUtil.getCritsUsed(unit, equip);
            for (int i = 0; i < crits; i++) {
                try {
                    if (firstBlock || (locations.get(0) == Entity.LOC_NONE)) {
                        // create only one mount per equipment, for BV and stuff
                        addMounted(unit, mount, locations.get(0), false);
                        if (firstBlock) {
                            firstBlock = false;
                        }
                        if (locations.get(0) == Entity.LOC_NONE) {
                            // only user-placable spread stuff gets location
                            // none
                            // for those, we need to create a mount for each
                            // crit,
                            // otherwise we can't correctly let the user place
                            // them
                            // luckily, that only affects TSM, so BV works out
                            // correctly
                            mount = new Mounted(unit, equip);
                        }
                    } else {
                        CriticalSlot cs = new CriticalSlot(mount);
                        if (!unit.addCritical(locations.get(0), cs)) {
                            UnitUtil.removeCriticals(unit, mount);
                            JOptionPane.showMessageDialog(
                                    null,
                                    "No room for equipment",
                                    mount.getName()
                                            + " does not fit into "
                                            + unit.getLocationName(locations
                                            .get(0)),
                                    JOptionPane.INFORMATION_MESSAGE);
                            unit.getMisc().remove(mount);
                            unit.getEquipment().remove(mount);
                            return null;
                        }
                    }
                } catch (LocationFullException lfe) {
                    log.error("Error", lfe);
                    JOptionPane.showMessageDialog(
                            null,
                            lfe.getMessage(),
                            mount.getName() + " does not fit into "
                                    + unit.getLocationName(locations.get(0)),
                            JOptionPane.INFORMATION_MESSAGE);
                    unit.getMisc().remove(mount);
                    unit.getEquipment().remove(mount);
                    return null;
                }
            }
            locations.remove(0);
        }
        return mount;
    }

    public static Font deriveFont(float pointSize) {
        return UnitUtil.deriveFont(false, pointSize);
    }

    public static Font deriveFont(boolean boldFont, float pointSize) {

        UnitUtil.loadFonts();

        if (boldFont) {
            return rsBoldFont.deriveFont(pointSize);
        }

        return rsFont.deriveFont(pointSize);
    }

    public static void loadFonts() {
        Font font = Font.decode(CConfig.getParam(CConfig.RS_FONT, "Eurostile"));
        // If the font is not installed, use system default sans
        if (null == font) {
            font = Font.decode(Font.SANS_SERIF);
        }
        // If that still doesn't work, get the default dialog font
        if (null == font) {
            font = Font.decode(null);
        }
        rsFont = font.deriveFont(Font.PLAIN, 8);
        rsBoldFont = font.deriveFont(Font.BOLD, 8);
    }

    public static boolean hasAmmo(Entity unit, int location) {

        for (Mounted mount : unit.getEquipment()) {

            if (mount.getType().isExplosive(mount)
                    && ((mount.getLocation() == location) || (mount
                    .getSecondLocation() == location))) {
                return true;
            }
        }

        return false;
    }

    public static int getBAAmmoCount(Entity ba, WeaponType weapon, int location) {
        int ammoCount = 0;

        for (Mounted mount : ba.getAmmo()) {
            if (mount.getLocation() != location) {
                continue;
            }
            AmmoType ammo = (AmmoType) mount.getType();

            if ((ammo.getRackSize() == weapon.getRackSize())
                    && (ammo.getAmmoType() == weapon.getAmmoType())) {
                ammoCount += mount.getUsableShotsLeft();
            }
        }

        return ammoCount;
    }

    public static String getCritName(Entity unit, EquipmentType eq) {
        String name = eq.getInternalName();
        if (unit.isMixedTech()
                && (eq.getTechLevel(unit.getTechLevelYear()) != TechConstants.T_ALLOWED_ALL)
                && (eq.getTechLevel(unit.getTechLevelYear()) != TechConstants.T_TECH_UNKNOWN)) {

            if (unit.isClan()
                    && !TechConstants.isClan(eq.getTechLevel(unit
                    .getTechLevelYear()))) {
                name = name + " (IS)";
            }

            if (!unit.isClan()
                    && TechConstants.isClan(eq.getTechLevel(unit
                    .getTechLevelYear()))) {
                name = name + " (Clan)";
            }
        }
        return name;
    }

    public static String getToolTipInfo(Entity unit, Mounted eq) {
        DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols();
        unusualSymbols.setDecimalSeparator('.');
        unusualSymbols.setGroupingSeparator(',');
        DecimalFormat myFormatter = new DecimalFormat("#,##0", unusualSymbols);
        StringBuilder sb = new StringBuilder("<HTML>");
        sb.append(eq.getName());
        if ((eq.getType().hasFlag(MiscType.F_DETACHABLE_WEAPON_PACK)
                || eq.getType().hasFlag(MiscType.F_AP_MOUNT))
                && (eq.getLinked() != null)) {
            sb.append(" (attached " + eq.getLinked().getName()
                    + ")");
        }
        if (eq.isSquadSupportWeapon()) {
            sb.append(" (squad support weapon)");
        }

        sb.append("<br>Crits: ");
        sb.append(eq.getCriticals());
        sb.append("<br>Mass: ");
        if (TestEntity.usesKgStandard(unit)) {
            sb.append(Math.round(eq.getTonnage() * 1000));
            sb.append(" Kg");
        } else {
            sb.append(eq.getTonnage());
            sb.append(" tons");
        }

        if (eq.getType() instanceof WeaponType) {
            sb.append("<br>Heat: ");
            sb.append(eq.getType().getHeat());

        }
        sb.append("<Br>Cost: ");

        double cost = eq.getType().getCost(unit, false, eq.getLocation());

        sb.append(myFormatter.format(cost));
        sb.append(" CBills");

        if (eq.isRearMounted()) {
            sb.append("<br>Rear Facing");
        }
        if (eq.isMechTurretMounted()) {
            sb.append("<br>Turret mounted");
        }
        if (eq.isArmored()) {
            sb.append("<br>Armored");
        }
        if ((unit instanceof BattleArmor)

                && eq.getType().hasFlag(WeaponType.F_INF_SUPPORT)) {
            sb.append("<br>* Infantry support weapons must be held in an " +
                    "Armored Glove");
        } else if ((unit instanceof BattleArmor)
                && eq.getType().hasFlag(WeaponType.F_INFANTRY)) {
            sb.append("<br>* Infantry weapons must be mounted in AP Mounts");
        }

        sb.append("</html>");
        return sb.toString();
    }

    public static boolean isPreviousCritEmpty(Entity unit, CriticalSlot cs,
                                              int slot, int location) {
        if (slot == 0) {
            return false;
        }
        if (unit instanceof Mech) {
            if ((slot > 0) && (unit.getCritical(location, slot - 1) != null)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLastCrit(Entity unit, CriticalSlot cs, int slot,
                                     int location) {
        if (unit instanceof Mech) {
            return UnitUtil.isLastMechCrit((Mech) unit, cs, slot, location);
        }
        return true;
    }

    public static boolean isLastMechCrit(Mech unit, CriticalSlot cs, int slot,
                                         int location) {

        if (cs == null) {
            return true;
        }
        // extra check for the last crit in a location, it shouldn't get a
        // border
        if ((slot + 1) >= unit.getNumberOfCriticals(location)) {
            return false;
        }

        int lastIndex = 0;
        if (cs.getType() == CriticalSlot.TYPE_SYSTEM) {

            for (int position = 0; position < unit
                    .getNumberOfCriticals(location); position++) {
                if ((cs.getIndex() == Mech.SYSTEM_ENGINE) && (slot >= 3)
                        && (position < 3)) {
                    position = 3;
                }
                CriticalSlot crit = unit.getCritical(location, position);

                if ((crit != null)
                        && (crit.getType() == CriticalSlot.TYPE_SYSTEM)
                        && (crit.getIndex() == cs.getIndex())) {
                    lastIndex = position;
                } else if (position > slot) {
                    break;
                }
            }

        } else {
            CriticalSlot nextCrit = unit.getCritical(location, slot + 1);
            if (nextCrit == null) {
                return true;
            } else if ((nextCrit.getMount() == null)
                    || !nextCrit.getMount().equals(cs.getMount())) {
                return true;
            } else {
                return false;
            }

            /*
             * Mounted originalMount = cs.getMount(); Mounted testMount = null;
             *
             * if (originalMount == null) { originalMount =
             * cs.getMount(); }
             *
             * if (originalMount == null) { return true; }
             *
             * int numberOfCrits = slot -
             * (originalMount.getType().getCriticals(unit) - 1);
             *
             * if (numberOfCrits < 0) { return false; } for (int position =
             * slot; position >= numberOfCrits; position--) { CriticalSlot crit
             * = unit.getCritical(location, position);
             *
             * if ((crit == null) || (crit.getType() !=
             * CriticalSlot.TYPE_EQUIPMENT)) { return false; }
             *
             * if ((testMount = crit.getMount()) == null) { testMount =
             * crit.getMount(); }
             *
             * if (testMount == null) { return false; }
             *
             * if (!testMount.equals(originalMount)) { return false; }
             *
             * } return true;
             */
        }

        return slot == lastIndex;
    }

    /**
     * Finds all the critical slots in the location containing the mount and sets or clears the
     * armored component flag in accordance with the flag on the mount.
     *
     * @param unit     The unit the equipment is mounted on
     * @param mount    The mount
     * @param location The location to check
     */
    public static void updateCritsArmoredStatus(Entity unit, Mounted mount, int location) {
        for (int position = 0; position < unit.getNumberOfCriticals(location); position++) {
            CriticalSlot cs = unit.getCritical(location, position);
            if ((cs == null) || (cs.getType() == CriticalSlot.TYPE_SYSTEM)) {
                continue;
            }

            if (mount.equals(cs.getMount())) {
                cs.setArmored(mount.isArmored());
            }
        }
    }

    /**
     * Sets the armored component flag on all critical slots occupied by an equipment mount to
     * be the same as the flag on the mount.
     *
     * @param unit  The unit the equipment is on
     * @param mount The equipment mount
     */
    public static void updateCritsArmoredStatus(Entity unit, Mounted mount) {
        /* Several types of equipment have multiple fixed locations. These
         * are always mounted in the primary location and added to critical
         * slots in the other location(s). Examples are partial wing (both side torsos)
         * and mech tracks (all legs). Rather than dealing with each piece of equipment
         * individually and risking missing one, just check everywhere.
         */
        if (isFixedLocationSpreadEquipment(mount.getType())) {
            for (int loc = 0; loc < unit.locations(); loc++) {
                updateCritsArmoredStatus(unit, mount, loc);
            }
        } else {
            updateCritsArmoredStatus(unit, mount, mount.getLocation());

            if ((mount.isSplitable() || mount.getType().isSpreadable())
                    && (mount.getSecondLocation() != Entity.LOC_NONE)) {
                updateCritsArmoredStatus(unit, mount, mount.getSecondLocation());
            }
        }
    }

    public static void updateCritsArmoredStatus(Entity unit, CriticalSlot cs,
                                                int location) {

        if ((cs == null) || (cs.getType() == CriticalSlot.TYPE_EQUIPMENT)) {
            return;
        }

        if (cs.getIndex() <= Mech.SYSTEM_GYRO) {
            for (int loc = Mech.LOC_HEAD; loc <= Mech.LOC_LT; loc++) {
                for (int slot = 0; slot < unit.getNumberOfCriticals(loc); slot++) {
                    CriticalSlot newCrit = unit.getCritical(loc, slot);

                    if ((newCrit != null)
                            && (newCrit.getType() == CriticalSlot.TYPE_SYSTEM)
                            && (newCrit.getIndex() == cs.getIndex())) {
                        newCrit.setArmored(cs.isArmored());
                    }
                }
            }
        } else {
            // actuators
            for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                CriticalSlot newCrit = unit.getCritical(location, slot);

                if ((newCrit != null)
                        && (newCrit.getType() == CriticalSlot.TYPE_SYSTEM)
                        && (newCrit.getIndex() == cs.getIndex())) {
                    newCrit.setArmored(cs.isArmored());
                }
            }
        }
    }

    public static boolean isArmorable(CriticalSlot cs) {
        if (cs == null) {
            return false;
        }

        if (cs.getType() == CriticalSlot.TYPE_SYSTEM) {
            return true;
        }

        Mounted mount = cs.getMount();
        return ((mount != null) && isArmorable(mount.getType()));
    }

    public static boolean isArmorable(EquipmentType eq) {
        if (eq instanceof AmmoType) {
            // The prohibition against armoring ammo bins presumably only applies to actual
            // ammo bins and not equipment that we've implemented as ammo because it's explody and gets used up.
            return ((AmmoType) eq).getAmmoType() == AmmoType.T_COOLANT_POD;
        }
        return eq.isHittable();
    }

    /**
     * Returns the units tech type.
     *
     * @param unit
     * @return
     */
    public static int getUnitTechType(Entity unit) {
        switch (unit.getTechLevel()) {
            case TechConstants.T_INTRO_BOXSET:
                return UnitUtil.TECH_INTRO;
            case TechConstants.T_IS_TW_NON_BOX:
            case TechConstants.T_IS_TW_ALL:
            case TechConstants.T_CLAN_TW:
                return UnitUtil.TECH_STANDARD;
            case TechConstants.T_IS_ADVANCED:
            case TechConstants.T_CLAN_ADVANCED:
                return UnitUtil.TECH_ADVANCED;
            case TechConstants.T_IS_EXPERIMENTAL:
            case TechConstants.T_CLAN_EXPERIMENTAL:
                return UnitUtil.TECH_EXPERIMENTAL;
            case TechConstants.T_IS_UNOFFICIAL:
            case TechConstants.T_CLAN_UNOFFICIAL:
                return UnitUtil.TECH_UNOFFICAL;
        }
        return UnitUtil.TECH_INTRO;
    }

    public static void updateLoadedUnit(Entity unit) {

        // Check for illegal armor tech levels and set to the tech level of the unit.
        for (int loc = 0; loc < unit.locations(); loc++) {
            if (unit.getArmorType(loc) >= 0) {
                if (unit.getArmorTechLevel(loc) < 0) {
                    unit.setArmorTechLevel(unit.getTechLevel());
                }
            }
        }

        if (unit instanceof Mech) {
            UnitUtil.updateLoadedMech((Mech) unit);
        }
    }

    public static void updateLoadedMech(Mech unit) {
        UnitUtil.removeOneShotAmmo(unit);
        UnitUtil.removeClanCase(unit);
        UnitUtil.expandUnitMounts(unit);
        UnitUtil.checkArmor(unit);
    }

    /**
     * Expands crits that are a single mount by have multiple spreadable crits
     * Such as TSM, Endo Steel, Reactive armor.
     *
     * @param unit
     */
    public static void expandUnitMounts(Mech unit) {
        for (int location = 0; location < unit.locations(); location++) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                CriticalSlot cs = unit.getCritical(location, slot);
                if ((cs == null) || (cs.getType() == CriticalSlot.TYPE_SYSTEM)) {
                    continue;
                }
                Mounted mount = cs.getMount();

                if (!UnitUtil.isFixedLocationSpreadEquipment(mount.getType())
                        && (UnitUtil.isTSM(mount.getType()) || UnitUtil
                        .isArmorOrStructure(mount.getType()))) {
                    Mounted newMount = new Mounted(unit, mount.getType());
                    newMount.setLocation(location, mount.isRearMounted());
                    newMount.setArmored(mount.isArmored());
                    cs.setMount(newMount);
                    cs.setArmored(mount.isArmored());
                    unit.getEquipment().remove(mount);
                    unit.getMisc().remove(mount);
                    unit.getEquipment().add(newMount);
                    unit.getMisc().add(newMount);
                }
            }
        }
    }

    public static void removeOneShotAmmo(Entity unit) {
        ArrayList<Mounted> ammoList = new ArrayList<Mounted>();

        for (Mounted mount : unit.getAmmo()) {
            if (mount.getLocation() == Entity.LOC_NONE) {
                ammoList.add(mount);
            }
        }

        for (Mounted mount : ammoList) {
            int index = unit.getEquipment().indexOf(mount);
            unit.getEquipment().remove(mount);
            unit.getAmmo().remove(mount);

            for (int location = 0; location <= Mech.LOC_LLEG; location++) {
                for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                    CriticalSlot cs = unit.getCritical(location, slot);
                    if ((cs == null)
                            || (cs.getType() == CriticalSlot.TYPE_SYSTEM)) {
                        continue;
                    }

                    if (cs.getIndex() >= index) {
                        cs.setIndex(cs.getIndex() - 1);
                    }
                }
            }
        }
    }

    public static void removeClanCase(Entity unit) {
        ArrayList<Mounted> caseList = new ArrayList<Mounted>();

        for (Mounted mount : unit.getMisc()) {
            if (mount.getType().getInternalName().equals("CLCASE")) {
                caseList.add(mount);
            }
        }

        for (Mounted mount : caseList) {
            int index = unit.getEquipment().indexOf(mount);
            unit.getEquipment().remove(mount);
            unit.getMisc().remove(mount);

            for (int location = 0; location <= Mech.LOC_LLEG; location++) {
                for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                    CriticalSlot cs = unit.getCritical(location, slot);
                    if ((cs == null)
                            || (cs.getType() == CriticalSlot.TYPE_SYSTEM)) {
                        continue;
                    }

                    if (cs.getIndex() >= index) {
                        cs.setIndex(cs.getIndex() - 1);
                    }
                }
            }
        }
    }

    public static void checkArmor(Entity unit) {

        if (!(unit instanceof Mech)) {
            return;
        }

        boolean foundError = false;

        Mech mech = (Mech) unit;

        // Check all the mechs locations to see if any armor is greater than can
        // be in there.
        for (int location = 0; location < mech.locations(); location++) {
            // Head armor has a max of 9
            if (location == Mech.LOC_HEAD) {
                int armor = mech.getArmor(location);

                if ((armor > 9) && !mech.isSuperHeavy()) {
                    foundError = true;
                    mech.initializeArmor(9, location);
                } else if (armor > 12) {
                    foundError = true;
                    mech.initializeArmor(9, location);
                }
            } else {
                int armor = mech.getArmor(location);
                if (mech.hasRearArmor(location)) {
                    armor += mech.getArmor(location, true);
                }
                int totalArmor = mech.getInternal(location) * 2;
                // Armor on the location is greater than what can be there.
                if (armor > totalArmor) {
                    foundError = true;
                    int armorOverage = armor - totalArmor;

                    // check for locations with rear armor first and remove the
                    // extra armor from the rear first.
                    if (mech.hasRearArmor(location)) {
                        int rearArmor = mech.getArmor(location, true);
                        if (rearArmor >= armorOverage) {
                            mech.initializeRearArmor(rearArmor - armorOverage,
                                    location);
                            armorOverage = 0;
                        } else {
                            armorOverage -= rearArmor;
                            mech.initializeRearArmor(0, location);
                        }
                    }

                    // Any armor overage left remove it from the front. Min 0
                    // armor in the location.
                    armor = mech.getArmor(location);
                    armor = Math.max(0, armor - armorOverage);
                    mech.initializeArmor(armor, location);
                }
            }
        }

        if (foundError) {
            JOptionPane
                    .showMessageDialog(
                            null,
                            "Too much armor found on this unit.\n\rMegaMekLab has automatically corrected the problem.\n\rIt is suggested you check the armor allocation.",
                            "Too much armor", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static boolean isUnitWeapon(EquipmentType eq, Entity unit) {
        if (unit instanceof Tank) {
            return UnitUtil.isTankWeapon(eq, unit);
        }

        if (unit instanceof BattleArmor) {
            return UnitUtil.isBattleArmorWeapon(eq, unit);
        }

        return UnitUtil.isMechWeapon(eq, unit);
    }

    public static boolean isMechWeapon(EquipmentType eq, Entity unit) {
        if (eq instanceof InfantryWeapon) {
            return false;
        }

        if (UnitUtil.isHeatSink(eq) || UnitUtil.isArmorOrStructure(eq)
                || UnitUtil.isJumpJet(eq)
                || UnitUtil.isMechEquipment(eq, (Mech) unit)) {
            return false;
        }

        if (eq instanceof AmmoType) {
            return false;
        }

        if (eq instanceof WeaponType) {

            WeaponType weapon = (WeaponType) eq;

            if (!weapon.hasFlag(WeaponType.F_MECH_WEAPON)) {
                return false;
            }

            if (weapon.getTonnage(unit) <= 0) {
                return false;
            }

            if (weapon.isCapital() || weapon.isSubCapital()) {
                return false;
            }

            if (((weapon instanceof LRMWeapon) || (weapon instanceof LRTWeapon))
                    && (weapon.getRackSize() != 5)
                    && (weapon.getRackSize() != 10)
                    && (weapon.getRackSize() != 15)
                    && (weapon.getRackSize() != 20)) {
                return false;
            }
            if (((weapon instanceof SRMWeapon) || (weapon instanceof SRTWeapon))
                    && (weapon.getRackSize() != 2)
                    && (weapon.getRackSize() != 4)
                    && (weapon.getRackSize() != 6)) {
                return false;
            }
            if ((weapon instanceof MRMWeapon) && (weapon.getRackSize() < 10)) {
                return false;
            }

            if ((weapon instanceof RLWeapon) && (weapon.getRackSize() < 10)) {
                return false;
            }

            if (weapon.hasFlag(WeaponType.F_ENERGY)
                    || (weapon.hasFlag(WeaponType.F_PLASMA) && (weapon
                    .getAmmoType() == AmmoType.T_PLASMA))) {

                if (weapon.hasFlag(WeaponType.F_ENERGY)
                        && weapon.hasFlag(WeaponType.F_PLASMA)
                        && (weapon.getAmmoType() == AmmoType.T_NA)) {
                    return false;
                }
            }

            if ((unit instanceof LandAirMech)
                    && (weapon.getAmmoType() == AmmoType.T_GAUSS_HEAVY
                    || weapon.getAmmoType() == AmmoType.T_IGAUSS_HEAVY)) {
                return false;
            }

            return true;
        }
        return false;
    }

    public static boolean isEntityEquipment(EquipmentType eq, Entity en) {
        if (en instanceof Mech) {
            return isMechEquipment(eq, (Mech) en);
        } else if (en instanceof Protomech) {
            return isProtomechEquipment(eq, (Protomech) en);
        } else if (en instanceof Tank) {
            return isTankEquipment(eq, (Tank) en);
        } else if (en instanceof BattleArmor) {
            return isBAEquipment(eq, (BattleArmor) en);
        }
        return true;
    }

    public static boolean isMechEquipment(EquipmentType eq, Mech unit) {

        if (UnitUtil.isArmorOrStructure(eq)) {
            return false;
        }


        return true;
    }

    public static boolean isProtomechEquipment(EquipmentType eq, Protomech proto) {
        return isProtomechEquipment(eq, proto, false);
    }

    public static boolean isProtomechEquipment(EquipmentType eq, Protomech proto, boolean checkConfiguration) {
        if (checkConfiguration && (eq instanceof MiscType)) {
            if (eq.hasFlag(MiscType.F_MAGNETIC_CLAMP) && (proto.isQuad() || proto.isGlider())) {
                return false;
            }
            if (eq.hasFlag(MiscType.F_CLUB) && eq.hasSubType(MiscType.S_PROTOMECH_WEAPON) && proto.isQuad()) {
                return false;
            }
            if (eq.hasFlag(MiscType.F_CLUB) && eq.hasSubType(MiscType.S_PROTO_QMS) && !proto.isQuad()) {
                return false;
            }
        }
        if (eq instanceof MiscType) {
            return eq.hasFlag(MiscType.F_PROTOMECH_EQUIPMENT);
        } else if (eq instanceof WeaponType) {
            return eq.hasFlag(WeaponType.F_PROTO_WEAPON);
        }
        return true;
    }

    public static boolean isTankWeapon(EquipmentType eq, Entity unit) {
        if (eq instanceof InfantryWeapon) {
            return false;
        }
        // Some weapons such as TAG and C3M should show as non-weapon equipment
        if (isTankMiscEquipment(eq, unit)) {
            return false;
        }

        if (eq instanceof WeaponType) {

            WeaponType weapon = (WeaponType) eq;

            if (!weapon.hasFlag(WeaponType.F_TANK_WEAPON)) {
                return false;
            }

            if (weapon.getTonnage(unit) <= 0) {
                return false;
            }

            if (weapon.isCapital() || weapon.isSubCapital()) {
                return false;
            }

            if (((weapon instanceof LRMWeapon) || (weapon instanceof LRTWeapon))
                    && (weapon.getRackSize() != 5)
                    && (weapon.getRackSize() != 10)
                    && (weapon.getRackSize() != 15)
                    && (weapon.getRackSize() != 20)) {
                return false;
            }
            if (((weapon instanceof SRMWeapon) || (weapon instanceof SRTWeapon))
                    && (weapon.getRackSize() != 2)
                    && (weapon.getRackSize() != 4)
                    && (weapon.getRackSize() != 6)) {
                return false;
            }
            if ((weapon instanceof MRMWeapon) && (weapon.getRackSize() < 10)) {
                return false;
            }

            if ((weapon instanceof RLWeapon) && (weapon.getRackSize() < 10)) {
                return false;
            }

            if (weapon.hasFlag(WeaponType.F_ENERGY)
                    || (weapon.hasFlag(WeaponType.F_PLASMA) && (weapon
                    .getAmmoType() == AmmoType.T_PLASMA))) {

                if (weapon.hasFlag(WeaponType.F_ENERGY)
                        && weapon.hasFlag(WeaponType.F_PLASMA)
                        && (weapon.getAmmoType() == AmmoType.T_NA)) {
                    return false;
                }
            }

            return TestTank.legalForMotiveType(weapon, unit.getMovementMode());
        }
        return false;
    }

    /**
     * @param eq A {@link WeaponType} or {@link MiscType}
     * @param ba The BattleArmor instance
     * @return Whether the BA can use the equipment
     */
    public static boolean isBAEquipment(EquipmentType eq, BattleArmor ba) {
        if (eq instanceof MiscType) {
            return eq.hasFlag(MiscType.F_BA_EQUIPMENT);
        } else if (eq instanceof WeaponType) {
            return isBattleArmorWeapon(eq, ba);
        }
        // This leaves ammotype, which is filtered according to having a weapon that can use it
        return false;
    }

    public static boolean isBattleArmorAPWeapon(EquipmentType etype) {
        InfantryWeapon infWeap = null;
        if ((etype == null) || !(etype instanceof InfantryWeapon)) {
            return false;
        } else {
            infWeap = (InfantryWeapon) etype;
        }
        return infWeap.hasFlag(WeaponType.F_INFANTRY)
                && !infWeap.hasFlag(WeaponType.F_INF_POINT_BLANK)
                && !infWeap.hasFlag(WeaponType.F_INF_ARCHAIC)
                && (infWeap.getCrew() < 2);
    }

    public static boolean isBattleArmorWeapon(EquipmentType eq, Entity unit) {

        if (eq instanceof WeaponType) {

            WeaponType weapon = (WeaponType) eq;

            if (!weapon.hasFlag(WeaponType.F_BA_WEAPON)) {
                return false;
            }

            if (weapon.getTonnage(unit) <= 0) {
                return false;
            }

            if (weapon.isCapital() || weapon.isSubCapital()) {
                return false;
            }

            if ((eq instanceof SwarmAttack) || (eq instanceof StopSwarmAttack)
                    || (eq instanceof LegAttack)) {
                return false;
            }

            if (weapon.hasFlag(WeaponType.F_ENERGY)
                    || (weapon.hasFlag(WeaponType.F_PLASMA) && (weapon
                    .getAmmoType() == AmmoType.T_PLASMA))) {
                return true;
            }

            if (weapon.hasFlag(WeaponType.F_ENERGY) && (weapon.hasFlag(WeaponType.F_PLASMA))
                    && (weapon.hasFlag(WeaponType.F_BA_WEAPON))) {
                return true;
            }

            if (weapon.hasFlag(WeaponType.F_ENERGY)
                    && weapon.hasFlag(WeaponType.F_PLASMA)
                    && (weapon.getAmmoType() == AmmoType.T_NA)) {
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Tests whether equipment should be shown on the equipment tab for the unit. This is
     * used for both combat vehicles and non-aerospace support vehicles.
     *
     * @param eq   The equipment to show
     * @param tank The tank
     * @return Whether the equipment should show on the table
     */
    public static boolean isTankEquipment(EquipmentType eq, Tank tank) {
        return isTankMiscEquipment(eq, tank) || isTankWeapon(eq, tank);
    }

    /**
     * Tests whether equipment should be shown on the equipment tab for the unit as non-weapon
     * equipment. This is used for both combat vehicles and non-aerospace support vehicles.
     *
     * @param eq   The equipment to show
     * @param tank The tank
     * @return Whether the equipment should show on the table
     */
    public static boolean isTankMiscEquipment(EquipmentType eq, Entity tank) {
        if (UnitUtil.isArmorOrStructure(eq)) {
            return false;
        }

        // Display AMS as equipment (even though it's a weapon)
        if (eq.hasFlag(WeaponType.F_AMS)
                && eq.hasFlag(WeaponType.F_TANK_WEAPON)) {
            return true;
        }

        if ((eq instanceof CLTAG) || (eq instanceof ISC3M)
                || (eq instanceof ISC3MBS)
                || (eq instanceof ISTAG) || (eq instanceof CLLightTAG)) {
            return true;
        }

        if (eq instanceof MiscType) {
            if (!TestTank.legalForMotiveType(eq, tank.getMovementMode())) {
                return false;
            }
            // Can't use supercharger with solar or external power pickup
            if (eq.hasFlag(MiscType.F_MASC) && (!tank.hasEngine()
                    || tank.getEngine().getEngineType() == Engine.SOLAR
                    || tank.getEngine().getEngineType() == Engine.EXTERNAL)) {
                return false;
            }
            // External fuel tanks are only allowed on ICE and fuel cell engines
            if (eq.hasFlag(MiscType.F_FUEL) && (!tank.hasEngine()
                    || (tank.getEngine().getEngineType() != Engine.COMBUSTION_ENGINE
                    && tank.getEngine().getEngineType() != Engine.FUEL_CELL))) {
                return false;
            }
            if (eq.hasFlag(MiscType.F_VTOL_EQUIPMENT) && (tank instanceof VTOL)) {
                return true;
            } else {
                return eq.hasFlag(MiscType.F_TANK_EQUIPMENT);
            }
        }
        return false;
    }

    public static boolean canSwarm(BattleArmor ba) {

        for (Mounted eq : ba.getEquipment()) {
            if ((eq.getType() instanceof SwarmAttack)
                    || (eq.getType() instanceof StopSwarmAttack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canLegAttack(BattleArmor ba) {

        for (Mounted eq : ba.getEquipment()) {
            if (eq.getType() instanceof LegAttack) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasInfantryWeapons(BattleArmor ba) {

        for (Mounted eq : ba.getEquipment()) {
            if (eq.getType() instanceof InfantryWeapon) {
                return true;
            }
        }
        return false;
    }

    public static int getShieldDamageAbsorption(Mech mech, int location) {
        final String METHOD_NAME = "getShieldDamageAbsorption(Mech, int)";
        for (int slot = 0; slot < mech.getNumberOfCriticals(location); slot++) {
            CriticalSlot cs = mech.getCritical(location, slot);

            if (cs == null) {
                continue;
            }

            if (cs.getType() != CriticalSlot.TYPE_EQUIPMENT) {
                continue;
            }

            Mounted m = cs.getMount();

            if (m == null) {
                log.error("Null Mount index: {}", +cs.getIndex());
                m = cs.getMount();
            }

            EquipmentType type = m.getType();
            if ((type instanceof MiscType) && ((MiscType) type).isShield()) {
                return m.getBaseDamageAbsorptionRate();
            }
        }

        return 0;
    }

    public static int getShieldDamageCapacity(Mech mech, int location) {
        final String METHOD_NAME = "getShieldDamageCapacity(Mech, int)";
        for (int slot = 0; slot < mech.getNumberOfCriticals(location); slot++) {
            CriticalSlot cs = mech.getCritical(location, slot);

            if (cs == null) {
                continue;
            }

            if (cs.getType() != CriticalSlot.TYPE_EQUIPMENT) {
                continue;
            }

            Mounted m = cs.getMount();

            if (m == null) {
                log.error("Null Mount index: {}", cs.getIndex());
                m = cs.getMount();
            }

            EquipmentType type = m.getType();
            if ((type instanceof MiscType) && ((MiscType) type).isShield()) {
                return m.getBaseDamageCapacity();
            }
        }

        return 0;
    }

    /**
     * remove all Mounted on the passed unit that are internal structure or
     * armor
     *
     * @param unit              the Entity
     * @param internalStructure true to remove IS, false to remove armor
     */
    public static void removeISorArmorMounts(Entity unit,
                                             boolean internalStructure) {
        UnitUtil.removeISorArmorCrits(unit, internalStructure);
        ArrayList<String> mountList = new ArrayList<String>();

        mountList.add("Standard");

        List<String> names;
        if (internalStructure) {
            names = Arrays.asList(EquipmentType.structureNames);
        } else {
            names = Arrays.asList(EquipmentType.armorNames);
        }
        for (String name : names) {
            mountList.add(String.format("Clan %1s", name));
            mountList.add(String.format("IS %1s", name));
            mountList.add(name);
        }

        for (int pos = 0; pos < unit.getEquipment().size(); ) {
            Mounted mount = unit.getEquipment().get(pos);
            if (mountList.contains(mount.getType().getInternalName())) {
                unit.getEquipment().remove(pos);
            } else {
                pos++;
            }
        }

        for (int pos = 0; pos < unit.getMisc().size(); ) {
            Mounted mount = unit.getMisc().get(pos);
            if ((mount.getType() instanceof MiscType)
                    && mountList.contains(mount.getType().getInternalName())) {
                unit.getMisc().remove(pos);
            } else {
                pos++;
            }
        }
        if (internalStructure) {
            unit.setStructureType(EquipmentType.T_STRUCTURE_STANDARD);
        } else {
            unit.setArmorType(EquipmentType.T_ARMOR_STANDARD);
            unit.setArmorTechLevel(unit.getTechLevel());
        }
    }

    /**
     * remove all CriticalSlots on the passed unit that are internal structure or
     * armor
     *
     * @param unit              the Entity
     * @param internalStructure true to remove IS, false to remove armor
     */
    public static void removeISorArmorCrits(Entity unit,
                                            boolean internalStructure) {
        ArrayList<String> mountList = new ArrayList<String>();
        if (internalStructure) {
            for (String struc : EquipmentType.structureNames) {
                mountList.add("IS " + struc);
                mountList.add("Clan " + struc);
            }
        } else {
            for (String armor : EquipmentType.armorNames) {
                mountList.add("IS " + armor);
                mountList.add("Clan " + armor);
            }
        }

        for (int location = Mech.LOC_HEAD; location < unit.locations(); location++) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                CriticalSlot crit = unit.getCritical(location, slot);
                if ((crit != null)
                        && (crit.getType() == CriticalSlot.TYPE_EQUIPMENT)) {
                    Mounted mount = crit.getMount();

                    if ((mount != null)
                            && (mount.getType() instanceof MiscType)
                            && mountList.contains(mount.getType()
                            .getInternalName())) {
                        crit = null;
                        unit.setCritical(location, slot, crit);
                    }
                }
            }
        }
    }

    /**
     * Remove all mounts for the current armor type from a single location on the passed unit
     * and sets the armor type in that location to standard.
     *
     * @param unit The <code>Entity</code>
     * @param loc  The location from which to remove the armor mounts.
     */
    public static void resetArmor(Entity unit, int loc) {
        String name = EquipmentType.getArmorTypeName(unit.getArmorType(loc),
                TechConstants.isClan(unit.getArmorTechLevel(loc)));
        EquipmentType eq = EquipmentType.get(name);
        if (null != eq) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(loc); slot++) {
                final CriticalSlot crit = unit.getCritical(loc, slot);
                if ((null != crit) && (crit.getType() == CriticalSlot.TYPE_EQUIPMENT)
                        && (null != crit.getMount()) && crit.getMount().getType().equals(eq)) {
                    unit.getMisc().remove(crit.getMount());
                    unit.setCritical(loc, slot, null);
                }
            }
        }
        unit.setArmorType(EquipmentType.T_ARMOR_STANDARD, loc);
        unit.setArmorTechLevel(TechConstants.T_INTRO_BOXSET, loc);
    }

    public static boolean hasManipulator(BattleArmor ba) {

        for (Mounted mount : ba.getMisc()) {
            MiscType eq = (MiscType) mount.getType();

            if (eq.hasFlag(MiscType.F_BA_EQUIPMENT)
                    && (eq.hasFlag(MiscType.F_ARMORED_GLOVE)
                    || eq.hasFlag(MiscType.F_BASIC_MANIPULATOR) || eq
                    .hasFlag(MiscType.F_BATTLE_CLAW))) {
                return true;
            }
        }

        return false;
    }

    public static boolean isManipulator(Mounted mount) {

        if (!(mount.getType() instanceof MiscType)) {
            return false;
        }

        MiscType eq = (MiscType) mount.getType();

        if (eq.hasFlag(MiscType.F_BA_EQUIPMENT)
                && (eq.hasFlag(MiscType.F_ARMORED_GLOVE)
                || eq.hasFlag(MiscType.F_BASIC_MANIPULATOR)
                || eq.hasFlag(MiscType.F_BATTLE_CLAW) || eq
                .hasFlag(MiscType.F_CARGOLIFTER))) {
            return true;
        }

        return false;
    }

    public static int getNumberOfEquipmentLikeThis(Entity unit,
                                                   EquipmentType baseEQ) {
        int numberOfEq = 0;

        for (Mounted mount : unit.getEquipment()) {
            if (mount.getType().equals(baseEQ)) {
                numberOfEq++;
            }
        }

        return numberOfEq;
    }

    public static void removeAllMiscMounteds(Entity unit, BigInteger flag) {
        for (int pos = unit.getEquipment().size() - 1; pos >= 0; pos--) {
            Mounted mount = unit.getEquipment().get(pos);
            if ((mount.getType() instanceof MiscType)
                    && mount.getType().hasFlag(flag)) {
                UnitUtil.removeMounted(unit, mount);
            }
        }
    }

    public static void removeAllMounteds(Entity unit, EquipmentType et) {
        for (int pos = unit.getEquipment().size() - 1; pos >= 0; pos--) {
            Mounted mount = unit.getEquipment().get(pos);
            if (mount.getType().equals(et)) {
                UnitUtil.removeMounted(unit, mount);
            }
        }
    }

    public static void removeTC(Entity unit) {
        for (int pos = unit.getEquipment().size() - 1; pos >= 0; pos--) {
            Mounted mount = unit.getEquipment().get(pos);
            if ((mount.getType() instanceof MiscType)
                    && mount.getType()
                    .hasFlag(MiscType.F_TARGCOMP)) {
                UnitUtil.removeMounted(unit, mount);
            }
        }
    }

    /**
     * Checks whether the equipment can be added to the location on the build tab
     *
     * @param unit     The Entity being designed
     * @param eq       The equipment
     * @param location The location to add it
     * @return Whether the location is valid
     */
    public static boolean isValidLocation(Entity unit, EquipmentType eq, int location) {
        if (unit instanceof BattleArmor) {
            // Can only be mounted in APM or armored glove; can't be added directly to location
            return !(eq instanceof WeaponType && eq.hasFlag(WeaponType.F_INFANTRY));
        }
        return TestEntity.isValidLocation(unit, eq, location, null);
    }

    /**
     * Makes the equipment mounted in one location identical to that in another location. Any equipment
     * previously in the target location that is does not match the source location is removed and
     * assigned to Entity.LOC_NONE.
     *
     * @param entity  The unit being modified
     * @param fromLoc The source location index
     * @param toLoc   The target location index
     * @throws LocationFullException If the target location is full
     */
    public static void copyLocationEquipment(Entity entity, int fromLoc, int toLoc)
            throws LocationFullException {
        copyLocationEquipment(entity, fromLoc, toLoc, true, true);
    }

    /**
     * Makes the equipment mounted in one location identical to that in another location. Any equipment
     * previously in the target location that is does not match the source location is removed and
     * assigned to Entity.LOC_NONE. This does not handle split location equipment.
     *
     * @param entity         The unit being modified
     * @param fromLoc        The source location index
     * @param toLoc          The target location index
     * @param includeForward Whether to include forward-mounted equipment
     * @param includeRear    Whether to include rear-mounted equipment
     * @throws LocationFullException If the target location is full
     */
    public static void copyLocationEquipment(final Entity entity, final int fromLoc, final int toLoc,
                                             final boolean includeForward, final boolean includeRear) throws LocationFullException {
        final String METHOD_NAME = "copyLocationEquipment(Entity, int, int, boolean, boolean)"; //$NON-NLS-1$

        /* First we remove any equipment already in the location, but keep a list of it
         * to use as much as possible.
         */
        List<Mounted> removed = new ArrayList<>();
        // Create copy to iterate since we may be modifying it.
        List<Mounted> mountList = new ArrayList<>(entity.getEquipment());
        for (Mounted m : mountList) {
            if ((m.getLocation() == toLoc)
                    && (m.isRearMounted() ? includeRear : includeForward)) {
                removed.add(m);
                UnitUtil.removeCriticals(entity, m);
                if (m.getType() instanceof BayWeapon) {
                    removeMounted(entity, m);
                } else {
                    changeMountStatus(entity, m, Entity.LOC_NONE, Entity.LOC_NONE, false);
                }
            }
        }

        /* Now we go through the equipment in the location to copy and add it to the other location.
         * If there is a match in what we removed, use that. Otherwise add the equipment to the unit.
         * If the unit uses weapon bays, we need to create them in the new location and fill them. If
         * the unit doesn't use bays we will iterate through the crit slots to get the equipment
         * in the same order to be nice and tidy.
         */
        if (entity.usesWeaponBays()) {
            mountList = entity.getWeaponBayList().stream()
                    .filter(bay -> (bay.getLocation() == fromLoc) && (bay.isRearMounted() ? includeRear : includeForward))
                    .collect(Collectors.toList());
            for (Mounted bay : mountList) {
                if ((bay.getLocation() == fromLoc)
                        && (bay.isRearMounted() ? includeRear : includeForward)) {
                    Mounted newBay = new Mounted(entity, bay.getType());
                    entity.addEquipment(newBay, toLoc, bay.isRearMounted());
                    for (Integer eqNum : bay.getBayWeapons()) {
                        Mounted toAdd = copyEquipment(entity, toLoc, entity.getEquipment(eqNum), removed);
                        newBay.addWeaponToBay(entity.getEquipmentNum(toAdd));
                    }
                    for (Integer eqNum : bay.getBayAmmo()) {
                        Mounted toAdd = copyEquipment(entity, toLoc, entity.getEquipment(eqNum), removed);
                        newBay.addAmmoToBay(entity.getEquipmentNum(toAdd));
                    }
                }
            }
            // Now we copy any other equipment
            mountList = new ArrayList<>(entity.getMisc());
            for (Mounted m : mountList) {
                if ((m.getLocation() == fromLoc)
                        && (m.isRearMounted() ? includeRear : includeForward)) {
                    copyEquipment(entity, toLoc, m, removed);
                }
            }
        } else {
            for (int slot = 0; slot < entity.getNumberOfCriticals(fromLoc); slot++) {
                final CriticalSlot crit = entity.getCritical(fromLoc, slot);
                if ((null != crit) && (crit.getType() == CriticalSlot.TYPE_EQUIPMENT)) {
                    copyEquipment(entity, toLoc, crit.getMount(), removed);
                }
            }
        }
        // Link up Artemis, etc.
        try {
            MechFileParser.postLoadInit(entity);
        } catch (EntityLoadingException e) {
            log.error("Error", e);
        }
    }

    /**
     * Used by {@link #copyLocationEquipment(Entity, int, int, boolean, boolean)} to perform the actual
     * copy of equipment from one location to another.
     *
     * @param entity The entity be processed
     * @param toLoc  The location to copy the equipment to
     * @param toCopy The equipment to copy
     * @param reuse  A list of equipment to reuse if there is a copy available. If not, a new item will
     *               be created. Note that this modifies the contents of the list by removing the equipment
     *               mount that was reused
     * @return The new equipment mount created in the new location
     * @throws LocationFullException If there are not enough slots in the new location to add the equipment.
     */
    private static Mounted copyEquipment(Entity entity, int toLoc, Mounted toCopy, List<Mounted> reuse)
            throws LocationFullException {
        Mounted toAdd = reuse.stream().filter(m -> m.getType().equals(toCopy.getType()))
                .findFirst().orElse(null);
        if (null != toAdd) {
            reuse.remove(toAdd);
        } else {
            toAdd = new Mounted(entity, toCopy.getType());
        }
        if (toCopy.getType() instanceof AmmoType) {
            toAdd.setAmmoCapacity(toCopy.getAmmoCapacity());
            toAdd.setShotsLeft(toCopy.getBaseShotsLeft());
        }
        entity.addEquipment(toAdd, toLoc, toCopy.isRearMounted());
        changeMountStatus(entity, toAdd, toLoc, Entity.LOC_NONE, toCopy.isRearMounted());
        return toAdd;
    }

    /**
     * Checks whether the space has room for the equipment within the slot and weight limits.
     *
     * @param location A Protomech location
     * @param mount    The equipment to be added to the location
     * @return Whether the equipment can be added without exceeding the limits.
     */
    public static boolean protomechHasRoom(Protomech proto, int location, Mounted mount) {
        if (!TestProtomech.requiresSlot(mount.getType())) {
            return true;
        }
        int slots = TestProtomech.maxSlotsByLocation(location, proto) - 1;
        double weight = TestProtomech.maxWeightByLocation(location, proto)
                - mount.getTonnage();
        if ((slots < 0) || (weight < 0)) {
            return false;
        }
        for (Mounted m : proto.getEquipment()) {
            if (m.getLocation() == location) {
                slots--;
                weight -= m.getTonnage();
                if ((slots < 0) || (weight < 0)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void showValidation(Entity entity, JFrame frame) {
        String sb = UnitUtil.validateUnit(entity);

        if (sb.length() > 0) {
            JOptionPane.showMessageDialog(frame, sb, "Unit Validation",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Validation Passed",
                    "Unit Validation", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    /**
     * check that the unit is vaild
     *
     * @param unit
     * @return
     */
    public static String validateUnit(Entity unit) {
        StringBuffer sb = new StringBuffer();
        TestEntity testEntity = getEntityVerifier(unit);

        if (testEntity != null) {
            testEntity.correctEntity(sb, unit.getTechLevel());
        }

        return sb.toString();
    }

    /**
     * Returns a TestEntity instance for the supplied Entity.
     *
     * @param unit
     * @return
     */
    public static TestEntity getEntityVerifier(Entity unit) {
        EntityVerifier entityVerifier = EntityVerifier.getInstance(new File(
                "data/mechfiles/UnitVerifierOptions.xml"));
        TestEntity testEntity = null;

        if (unit.hasETypeFlag(Entity.ETYPE_MECH)) {
            testEntity = new TestMech((Mech) unit, entityVerifier.mechOption,
                    null);
        } else if (unit.hasETypeFlag(Entity.ETYPE_PROTOMECH)) {
            testEntity = new TestProtomech((Protomech) unit,
                    entityVerifier.protomechOption, null);
        } else if (unit.hasETypeFlag(Entity.ETYPE_TANK)) {
            testEntity = new TestTank((Tank) unit,
                    entityVerifier.tankOption, null);
        } else if (unit.hasETypeFlag(Entity.ETYPE_BATTLEARMOR)) {
            testEntity = new TestBattleArmor((BattleArmor) unit,
                    entityVerifier.baOption, null);
        }
        return testEntity;
    }

    public static void showUnitSpecs(Entity unit, JFrame frame) {
        HTMLEditorKit kit = new HTMLEditorKit();

        MechView mechView = null;
        try {
            mechView = new MechView(unit, true);
        } catch (Exception e) {
            // error unit didn't load right. this is bad news.
        }

        StringBuffer unitSpecs = new StringBuffer("<html><body>");
        unitSpecs.append(mechView.getMechReadoutBasic());
        unitSpecs.append(mechView.getMechReadoutLoadout());
        unitSpecs.append("</body></html>");

        // System.err.println(unitSpecs.toString());
        JEditorPane textPane = new JEditorPane("text/html", "");
        JScrollPane scroll = new JScrollPane();

        textPane.setEditable(false);
        textPane.setCaret(new DefaultCaret());
        textPane.setEditorKit(kit);

        scroll.setViewportView(textPane);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        textPane.setText(unitSpecs.toString());

        scroll.setVisible(true);

        JDialog jdialog = new JDialog();

        jdialog.add(scroll);
        /*
         * if (unit instanceof Mech) { EntityVerifier entityVerifier = new
         * EntityVerifier(new File("data/mechfiles/UnitVerifierOptions.xml"));
         * //$NON-NLS-1$ TestMech test = new TestMech((Mech)unit,
         * entityVerifier.mechOption, null); JEditorPane pane2 = new
         * JEditorPane();
         * pane2.setText(test.printWeightCalculation().toString());
         * jdialog.add(pane2); }
         */
        Dimension size = new Dimension(CConfig.getIntParam("WINDOWWIDTH") / 2,
                CConfig.getIntParam("WINDOWHEIGHT"));

        jdialog.setPreferredSize(size);
        jdialog.setMinimumSize(size);
        scroll.setPreferredSize(size);
        scroll.setMinimumSize(size);
        // text.setPreferredSize(size);

        jdialog.setLocationRelativeTo(frame);
        jdialog.setVisible(true);

        try {
            textPane.setSelectionStart(0);
            textPane.setSelectionEnd(0);
        } catch (Exception ex) {
        }

    }

    public static void showUnitCostBreakDown(Entity unit, JFrame frame) {
        HTMLEditorKit kit = new HTMLEditorKit();
        unit.calculateBattleValue(true, true);

        unit.getCost(true);

        JEditorPane textPane = new JEditorPane("text/html", "");
        JScrollPane scroll = new JScrollPane();

        textPane.setEditable(false);
        textPane.setCaret(new DefaultCaret());
        textPane.setEditorKit(kit);

        scroll.setViewportView(textPane);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        textPane.setText(unit.getBVText());

        scroll.setVisible(true);

        JDialog jdialog = new JDialog();

        jdialog.add(scroll);
        Dimension size = new Dimension(CConfig.getIntParam("WINDOWWIDTH") / 2,
                CConfig.getIntParam("WINDOWHEIGHT"));

        jdialog.setPreferredSize(size);
        jdialog.setMinimumSize(size);
        scroll.setPreferredSize(size);
        scroll.setMinimumSize(size);

        jdialog.setLocationRelativeTo(frame);
        jdialog.setVisible(true);

        try {
            textPane.setSelectionStart(0);
            textPane.setSelectionEnd(0);
        } catch (Exception ex) {
        }
    }

    public static void showUnitWeightBreakDown(Entity unit, JFrame frame) {
        TestEntity testEntity = getEntityVerifier(unit);

        JTextPane textPane = new JTextPane();
        JScrollPane scroll = new JScrollPane();

        textPane.setText(testEntity.printEntity().toString());
        textPane.setEditable(false);
        textPane.setCaret(new DefaultCaret());

        scroll.setViewportView(textPane);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        scroll.setVisible(true);

        JDialog jdialog = new JDialog();

        jdialog.add(scroll);
        Dimension size = new Dimension(CConfig.getIntParam("WINDOWWIDTH") / 2,
                CConfig.getIntParam("WINDOWHEIGHT"));

        jdialog.setPreferredSize(size);
        jdialog.setMinimumSize(size);
        scroll.setPreferredSize(size);
        scroll.setMinimumSize(size);

        jdialog.setLocationRelativeTo(frame);
        jdialog.setVisible(true);

        try {
            textPane.setSelectionStart(0);
            textPane.setSelectionEnd(0);
        } catch (Exception ex) {
        }

    }

    public static void showBVCalculations(String bvText, JFrame frame) {
        HTMLEditorKit kit = new HTMLEditorKit();

        JEditorPane textPane = new JEditorPane("text/html", "");
        JScrollPane scroll = new JScrollPane();

        textPane.setEditable(false);
        textPane.setCaret(new DefaultCaret());
        textPane.setEditorKit(kit);

        scroll.setViewportView(textPane);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        textPane.setText(bvText);

        scroll.setVisible(true);

        JDialog jdialog = new JDialog();

        jdialog.add(scroll);
        Dimension size = new Dimension(
                (int) (CConfig.getIntParam("WINDOWWIDTH") / 1.5),
                CConfig.getIntParam("WINDOWHEIGHT"));

        jdialog.setPreferredSize(size);
        jdialog.setMinimumSize(size);
        scroll.setPreferredSize(size);
        scroll.setMinimumSize(size);
        // text.setPreferredSize(size);

        jdialog.setLocationRelativeTo(frame);
        jdialog.setVisible(true);

        try {
            textPane.setSelectionStart(0);
            textPane.setSelectionEnd(0);
        } catch (Exception ex) {
        }

    }

    public static boolean hasBAR(Entity unit) {

        for (int loc = 0; loc < unit.locations(); loc++) {
            if (unit.hasBARArmor(loc)) {
                return true;
            }
        }

        return false;
    }

    public static int getLowestBARRating(Entity unit) {
        int bar = 10;

        for (int loc = 0; loc < unit.locations(); loc++) {
            if (unit.getBARRating(loc) < bar) {
                bar = unit.getBARRating(loc);
            }
        }
        return bar;
    }

    public static String getArmorString(Mech mech, int loc) {
        if (!mech.hasPatchworkArmor()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        switch (mech.getArmorType(loc)) {
            case EquipmentType.T_ARMOR_REFLECTIVE:
                sb.append("LR");
                break;
            case EquipmentType.T_ARMOR_HARDENED:
                sb.append("HD");
                break;
            case EquipmentType.T_ARMOR_LIGHT_FERRO:
                sb.append("LF");
                break;
            case EquipmentType.T_ARMOR_HEAVY_FERRO:
                sb.append("HF");
                break;
            case EquipmentType.T_ARMOR_FERRO_FIBROUS:
            case EquipmentType.T_ARMOR_FERRO_FIBROUS_PROTO:
                sb.append("FF");
                break;
            case EquipmentType.T_ARMOR_STEALTH:
                sb.append("SA");
                break;
            case EquipmentType.T_ARMOR_INDUSTRIAL:
                sb.append("IN");
                break;
            case EquipmentType.T_ARMOR_COMMERCIAL:
                sb.append("CO");
                break;
            case EquipmentType.T_ARMOR_FERRO_LAMELLOR:
                sb.append("FL");
                break;
            case EquipmentType.T_ARMOR_REACTIVE:
                sb.append("RE");
                break;
            default:
                return "";
        }
        if (mech.hasBARArmor(loc)) {
            sb.append(" B" + mech.getBARRating(loc));
        }
        return sb.toString();
    }

    /**
     * Checks whether the unit has an weapon that uses the ammo type and the munition is legal for the
     * type of unit.
     *
     * @param unit           The unit
     * @param atype          The ammo
     * @param includeOneShot If false, ignores one-shot weapons
     * @return Whether the unit can make use of the ammo
     */
    public static boolean canUseAmmo(Entity unit, AmmoType atype, boolean includeOneShot) {
        if ((unit instanceof BattleArmor)
                && !atype.hasFlag(AmmoType.F_BATTLEARMOR)) {
            return false;
        }
        if (!(unit instanceof BattleArmor)
                && atype.hasFlag(AmmoType.F_BATTLEARMOR)) {
            return false;
        }

        for (Mounted m : unit.getTotalWeaponList()) {
            if (m.getType() instanceof AmmoWeapon) {
                WeaponType wtype = (WeaponType) m.getType();
                if ((wtype.getAmmoType() == atype.getAmmoType())
                        && (wtype.getRackSize() == atype.getRackSize())
                        && (includeOneShot || !m.getType().hasFlag(WeaponType.F_ONESHOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int countUsedCriticals(Mech unit) {
        int nCrits = 0;
        for (int i = 0; i < unit.locations(); i++) {
            for (int j = 0; j < unit.getNumberOfCriticals(i); j++) {
                CriticalSlot cs = unit.getCritical(i, j);
                if (null != cs) {
                    nCrits++;
                }
            }
        }
        return nCrits + countUnallocatedCriticals(unit);
    }

    public static int countUnallocatedCriticals(Mech unit) {
        int nCrits = 0;
        int engineHeatSinkCount = UnitUtil.getCriticalFreeHeatSinks(unit,
                unit.hasCompactHeatSinks());
        for (Mounted mount : unit.getMisc()) {
            if (UnitUtil.isHeatSink(mount)
                    && (mount.getLocation() == Entity.LOC_NONE)) {
                if (engineHeatSinkCount > 0) {
                    engineHeatSinkCount--;
                    continue;
                }
            }
            if ((mount.getLocation() == Entity.LOC_NONE)) {
                nCrits += UnitUtil.getCritsUsed(unit, mount.getType());
            }
        }
        for (Mounted mount : unit.getWeaponList()) {
            if (mount.getLocation() == Entity.LOC_NONE) {
                nCrits += UnitUtil.getCritsUsed(unit, mount.getType());
            }
        }
        for (Mounted mount : unit.getAmmo()) {
            if ((mount.getLocation() == Entity.LOC_NONE) && !mount.isOneShotAmmo()) {
                nCrits += UnitUtil.getCritsUsed(unit, mount.getType());
            }
        }
        return nCrits;
    }

    /**
     * Returns the number of crits used by EquipmentType eq, 1 if armor or
     * structure EquipmentType
     *
     * @param unit
     * @param eq
     * @return
     */
    public static int getCritsUsed(Entity unit, EquipmentType eq) {

        boolean isMisc = eq instanceof MiscType;
        double toReturn = eq.getCriticals(unit);

        //if it's 0, we can return now (e.g. standard armor or IS, we don't
        //want that to count as 1 later on
        if (toReturn == 0) {
            return 0;
        }

        if (isMisc
                && eq.hasFlag(MiscType.F_PARTIAL_WING)
                && TechConstants
                .isClan(eq.getTechLevel(unit.getTechLevelYear()))) {
            toReturn = 3;
        } else if (isMisc
                && eq.hasFlag(MiscType.F_PARTIAL_WING)
                && !TechConstants.isClan(eq.getTechLevel(unit
                .getTechLevelYear()))) {
            toReturn = 4;
        } else if (isMisc
                && (eq.hasFlag(MiscType.F_JUMP_BOOSTER)
                || eq.hasFlag(MiscType.F_TALON)
                // Stealth armor is allocated 2 slots/location in mechs, but by individual slot for BA
                || (eq.hasFlag(MiscType.F_STEALTH) && !(unit instanceof BattleArmor)))) {
            toReturn = 2;
        } else if (UnitUtil.isFixedLocationSpreadEquipment(eq) || UnitUtil.isTSM(eq)
                || UnitUtil.isArmorOrStructure(eq)) {
            toReturn = 1;
        }
        if ((unit instanceof Mech) && unit.isSuperHeavy()) {
            toReturn = Math.ceil(toReturn / 2.0);
        }
        return (int) toReturn;
    }

    // gives total number of sinks, not just critical slots
    public static int countActualHeatSinks(Mech unit) {
        int sinks = 0;
        for (Mounted mounted : unit.getMisc()) {
            if (!UnitUtil.isHeatSink(mounted)) {
                continue;
            }
            if (mounted.getType().hasFlag(MiscType.F_COMPACT_HEAT_SINK)) {
                if (mounted.getType().hasFlag(MiscType.F_HEAT_SINK)) {
                    sinks++;
                } else if (mounted.getType().hasFlag(
                        MiscType.F_DOUBLE_HEAT_SINK)) {
                    sinks++;
                    sinks++;
                }
            } else {
                sinks++;
            }
        }
        return sinks;
    }

    /**
     * @deprecated Use {@link UnitUtil#checkEquipmentByTechLevel(Entity, ITechManager)} instead
     */
    @Deprecated
    public static void checkEquipmentByTechLevel(Entity unit) {
        Vector<Mounted> toRemove = new Vector<Mounted>();
        for (Mounted m : unit.getEquipment()) {
            EquipmentType etype = m.getType();
            if (UnitUtil.isArmorOrStructure(etype)
                    || UnitUtil.isHeatSink(etype) || UnitUtil.isJumpJet(etype)) {
                continue;
            }
            if (etype.hasFlag(MiscType.F_TSM)
                    || etype.hasFlag(MiscType.F_INDUSTRIAL_TSM)
                    || etype.hasFlag(MiscType.F_MASC)) {
                continue;
            }
            if (!UnitUtil.isLegal(unit, etype)) {
                toRemove.add(m);
            }
        }
        for (Mounted m : toRemove) {
            UnitUtil.removeMounted(unit, m);
        }
    }

    /**
     * Checks for any equipment that is added on the equipment tab and removes any that is
     * no longer legal for the current year/tech base/tech level
     *
     * @param unit        The unit to check
     * @param techManager The manager that handles the checking
     * @return Whether any changes were made
     */
    public static boolean checkEquipmentByTechLevel(Entity unit, ITechManager techManager) {
        List<Mounted> toRemove = new ArrayList<>();
        ITechnology acTA = Entity.getArmoredComponentTechAdvancement();
        boolean dirty = false;
        for (Mounted m : unit.getEquipment()) {
            if (m.isArmored() && !techManager.isLegal(acTA)) {
                m.setArmored(false);
                updateCritsArmoredStatus(unit, m);
                dirty = true;
            }
            EquipmentType etype = m.getType();
            if (UnitUtil.isArmorOrStructure(etype)
                    || UnitUtil.isHeatSink(etype) || UnitUtil.isJumpJet(etype)) {
                continue;
            }
            if (etype instanceof MiscType
                    && (etype.hasFlag(MiscType.F_TSM)
                    || etype.hasFlag(MiscType.F_INDUSTRIAL_TSM)
                    || (etype.hasFlag(MiscType.F_MASC)
                    && !etype.hasSubType(MiscType.S_SUPERCHARGER) && !etype.hasSubType(MiscType.S_JETBOOSTER))
                    || etype.hasFlag(MiscType.F_SCM))) {
                continue;
            }
            if (!techManager.isLegal(etype)) {
                toRemove.add(m);
            }
        }
        dirty |= toRemove.size() > 0;
        for (Mounted m : toRemove) {
            UnitUtil.removeMounted(unit, m);
        }
        return dirty;
    }

    public static String trimInfantryWeaponNames(String wname) {
        return wname.replace("Infantry ", "");
    }

    public static void removeOmniArmActuators(Mech mech) {
        if ((mech instanceof BipedMech) || (mech instanceof TripodMech)) {
            boolean leftACGaussPPC = false;
            boolean rightACGaussPPC = false;
            for (Mounted weapon : mech.getWeaponList()) {
                if ((weapon.getLocation() == Mech.LOC_LARM)
                        && ((weapon.getType() instanceof ACWeapon)
                        || (weapon.getType() instanceof GaussWeapon)
                        || (weapon.getType() instanceof LBXACWeapon)
                        || (weapon.getType() instanceof UACWeapon) || (weapon
                        .getType() instanceof PPCWeapon))) {
                    leftACGaussPPC = true;
                }
                if ((weapon.getLocation() == Mech.LOC_RARM)
                        && ((weapon.getType() instanceof ACWeapon)
                        || (weapon.getType() instanceof GaussWeapon)
                        || (weapon.getType() instanceof LBXACWeapon)
                        || (weapon.getType() instanceof UACWeapon) || (weapon
                        .getType() instanceof PPCWeapon))) {
                    rightACGaussPPC = true;
                }
            }
            if (leftACGaussPPC) {
                removeArm(mech, Mech.LOC_LARM);
                UnitUtil.compactCriticals(mech, Mech.LOC_LARM);
            }
            if (rightACGaussPPC) {
                removeArm(mech, Mech.LOC_RARM);
                UnitUtil.compactCriticals(mech, Mech.LOC_RARM);
            }
        }

    }

    public static void removeArm(Mech mech, int location) {
        if (mech.hasSystem(Mech.ACTUATOR_LOWER_ARM, location)) {
            mech.setCritical(location, 2, null);
            // Only remove the next slot of it actually is a hand
            if (mech.hasSystem(Mech.ACTUATOR_HAND, location)) {
                removeHand(mech, location);
            }
        }

    }

    public static void removeHand(Mech mech, int location) {
        if (mech.hasSystem(Mech.ACTUATOR_HAND, location)) {
            mech.setCritical(location, 3, null);
        }
    }
}
