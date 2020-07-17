/*
 * MegaMekLab - Copyright (C) 2010
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

package org.redbat.roguetech.megameklab.ui.BattleArmor;

import org.redbat.roguetech.megamek.common.*;
import org.redbat.roguetech.megameklab.ui.BattleArmor.tabs.BuildTab;
import org.redbat.roguetech.megameklab.ui.BattleArmor.tabs.EquipmentTab;
import org.redbat.roguetech.megameklab.ui.BattleArmor.tabs.StructureTab;
import org.redbat.roguetech.megameklab.ui.MegaMekLabMainUI;
import org.redbat.roguetech.megameklab.ui.tabs.FluffTab;

import javax.swing.*;
import java.awt.BorderLayout;

public class MainUI extends MegaMekLabMainUI {

    /**
     *
     */
    private static final long serialVersionUID = -5836932822468918198L;

    JTabbedPane ConfigPane = new JTabbedPane(SwingConstants.TOP);
    JPanel contentPane;
    private StructureTab structureTab;
    private BuildTab buildTab;
    private EquipmentTab equipTab;
    private FluffTab fluffTab;
    private StatusBar statusbar;

    public MainUI() {

        super();
        createNewUnit(Entity.ETYPE_BATTLEARMOR);
        setTitle(getEntity().getChassis() + " " + getEntity().getModel() + ".blk");
        finishSetup();
    }

    @Override
    public void reloadTabs() {
        masterPanel.removeAll();
        ConfigPane.removeAll();

        masterPanel.setLayout(new BorderLayout());
        structureTab = new StructureTab(this);
        equipTab = new EquipmentTab(this);
        fluffTab = new FluffTab(this);

        statusbar = new StatusBar(this);
        buildTab = new BuildTab(this);
        structureTab.addRefreshedListener(this);
        equipTab.addRefreshedListener(this);
        buildTab.addRefreshedListener(this);
        fluffTab.setRefreshedListener(this);

        ConfigPane.addTab("Structure/Armor", structureTab);
        ConfigPane.addTab("Equipment", equipTab);
        ConfigPane.addTab("Assign Criticals", buildTab);
        ConfigPane.addTab("Fluff", fluffTab);

        masterPanel.add(ConfigPane, BorderLayout.CENTER);
        masterPanel.add(statusbar, BorderLayout.SOUTH);

        refreshHeader();
        this.repaint();
    }

    @Override
    public void createNewUnit(long entityType, boolean isPrimitive, boolean isIndustrial, Entity oldEntity) {
        setEntity(new BattleArmor());
        BattleArmor ba = (BattleArmor) getEntity();

        ba.setYear(3145);
        ba.setTechLevel(TechConstants.T_IS_TW_NON_BOX);
        ba.setStructureType(EquipmentType.T_STRUCTURE_STANDARD);
        ba.setWeightClass(EntityWeightClass.WEIGHT_LIGHT);
        ba.setTroopers(4);
        ba.setChassisType(BattleArmor.CHASSIS_TYPE_BIPED);

        ba.autoSetInternal();
        for (int loc = 0; loc < ba.locations(); loc++) {
            ba.initializeArmor(0, loc);
        }

        ba.setChassis("New");
        ba.setModel("BattleArmor");
    }

    @Override
    public void refreshAll() {

        statusbar.refresh();
        structureTab.refresh();
        refreshEquipment();
        refreshBuild();
        refreshPreview();
        refreshHeader();
    }

    @Override
    public void refreshArmor() {

    }

    @Override
    public void refreshBuild() {
        buildTab.refresh();
    }

    @Override
    public void refreshEquipment() {
        equipTab.refresh();
    }
    
    @Override
    public void refreshTransport() {
        // not used for ba
    }

    @Override
    public void refreshHeader() {
        setTitle(getEntity().getChassis() + " " + getEntity().getModel() + ".blk");
    }

    @Override
    public void refreshStatus() {
        statusbar.refresh();
    }

    @Override
    public void refreshStructure() {
        structureTab.refresh();
    }

    @Override
    public void refreshWeapons() {
    }

    @Override
    public void refreshPreview() {
        structureTab.refreshPreview();
    }
    
    @Override
    public void refreshSummary() {
    }
    
    @Override
    public void refreshEquipmentTable() {
        equipTab.refreshTable();
    }

    @Override
    public ITechManager getTechManager() {
        if (structureTab != null) {
            return structureTab.getTechManager();
        }
        return null;
    }

}