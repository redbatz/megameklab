/*
 * MegaMekLab - Copyright (C) 2017 - The MegaMek Team
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
package org.redbat.roguetech.megameklab.ui.view.listeners;

import org.redbat.roguetech.megamek.common.EquipmentType;
import org.redbat.roguetech.megamek.common.FuelType;
import org.redbat.roguetech.megamek.common.SimpleTechLevel;
import org.redbat.roguetech.megameklab.ui.view.HeatSinkView;

/**
 * Combined listener interface for the various subviews of the structure tab. Includes callbacks
 * used by multiple unit types. Listeners for specific unit types extend this one.
 * 
 * @author Neoancient
 *
 */
public interface BuildListener {

    void refreshSummary();
    void chassisChanged(String chassis);
    void modelChanged(String model);
    void yearChanged(int year);
    void updateTechLevel();
    void sourceChanged(String source);
    void techBaseChanged(boolean clan, boolean mixed);
    void techLevelChanged(SimpleTechLevel techLevel);
    void manualBVChanged(int manualBV);

    void walkChanged(int walkMP);
    void jumpChanged(int jumpMP, EquipmentType jumpJet);
    void jumpTypeChanged(EquipmentType jumpJet);
    
    /*
     * Methods used by multiple unit types but not all are given default implementations that
     * ignore them.
     */

    /**
     * Notifies of a change in heat sink type or count for aerospace units
     * @param index Either {@link HeatSinkView#TYPE_SINGLE} or {@link HeatSinkView#TYPE_DOUBLE_AERO}
     * @param count The number of heat sinks
     */
    default void heatSinksChanged(int index, int count) {};

    /**
     * Notifies of a change in heat sink type or count for mechs
     * @param hsType        The type of heat sink
     * @param count         The total number of heat sinks
     */
    default void heatSinksChanged(EquipmentType hsType, int count) {};

    /**
     * Notifies of a change in the distribution between single and double heat sinks on a unit with
     * prototype double heat sinks.
     * @param prototype  The number of prototype double heat sinks
     */
    default void redistributePrototypeHS(int prototype) {};

    /**
     * Notifies of a change in the number of heat sinks that are part of the base chassis of an omni unit
     * @param count The number of fixed heat sinks
     */
    default void heatSinkBaseCountChanged(int count) {};

    // For aerospace units and support vehicles
    default void fuelTonnageChanged(double tonnage) {};

    /**
     * Notify of a change in the amount of fuel. For aerospace this is number of fuel points.
     * For ground support vehicles this is the range in km.
     *
     * @param capacity The number of fuel points or range as appropriate to the unit type
     */
    default void fuelCapacityChanged(int capacity) {};

    /**
     * Notify of a change in ICE engine fuel type. This is only used for vehicles.
     *
     * @param fuelType The engine fuel type
     */
    default void fuelTypeChanged(FuelType fuelType) {}
}
