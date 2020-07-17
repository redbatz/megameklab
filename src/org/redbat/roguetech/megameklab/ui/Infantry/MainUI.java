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

package org.redbat.roguetech.megameklab.ui.Infantry;

import org.redbat.roguetech.megamek.common.*;
import org.redbat.roguetech.megamek.common.weapons.infantry.InfantryWeapon;
import org.redbat.roguetech.megameklab.ui.Infantry.tabs.StructureTab;
import org.redbat.roguetech.megameklab.ui.MegaMekLabMainUI;
import org.redbat.roguetech.megameklab.ui.tabs.FluffTab;
import org.redbat.roguetech.megameklab.ui.tabs.PreviewTab;

import javax.swing.*;
import java.awt.BorderLayout;

public class MainUI extends MegaMekLabMainUI {

    /**
	 *
	 */
    private static final long serialVersionUID = 5338040000652349619L;

    StructureTab structureTab;
    PreviewTab previewTab;
    FluffTab fluffTab;
    StatusBar statusbar;
    JTabbedPane ConfigPane = new JTabbedPane(SwingConstants.TOP);

    public MainUI() {

        super();
        createNewUnit(Entity.ETYPE_INFANTRY);
        setTitle(getEntity().getChassis() + " " + getEntity().getModel() + ".mtf");
        finishSetup();

    }

    @Override
    public void reloadTabs() {
        masterPanel.removeAll();
        ConfigPane.removeAll();

        masterPanel.setLayout(new BorderLayout());

        statusbar = new StatusBar(this);
        structureTab = new StructureTab(this);
        fluffTab = new FluffTab(this);
        previewTab = new PreviewTab(this);

        structureTab.addRefreshedListener(this);
        fluffTab.setRefreshedListener(this);

        ConfigPane.addTab("Build", structureTab);
        ConfigPane.addTab("Fluff", fluffTab);
        ConfigPane.addTab("Preview", previewTab);

        masterPanel.add(ConfigPane, BorderLayout.CENTER);
        masterPanel.add(statusbar, BorderLayout.SOUTH);

        refreshHeader();
        this.repaint();
    }

    @Override
    public void createNewUnit(long entityType, boolean isPrimitive, boolean isIndustrial, Entity oldEntity) {
        setEntity(new Infantry());
        getEntity().setYear(3145);
        getEntity().setTechLevel(TechConstants.T_IS_TW_NON_BOX);
        getEntity().setArmorTechLevel(TechConstants.T_IS_TW_NON_BOX);
        ((Infantry) getEntity()).setSquadN(4);
        ((Infantry) getEntity()).setSquadSize(7);
        ((Infantry) getEntity()).setPrimaryWeapon((InfantryWeapon) EquipmentType
                .get("InfantryAssaultRifle"));
        try {
            getEntity().addEquipment(EquipmentType.get(EquipmentTypeLookup.INFANTRY_ASSAULT_RIFLE),
                    Infantry.LOC_INFANTRY);
        } catch (LocationFullException ex) {
        }
        getEntity().autoSetInternal();
        getEntity().setChassis("New");
        getEntity().setModel("Infantry");
    }

    @Override
    public void refreshAll() {
        statusbar.refresh();
        structureTab.refresh();
        previewTab.refresh();
    }

    @Override
    public void refreshArmor() {
        // armorTab.refresh();
    }

    @Override
    public void refreshBuild() {

    }

    @Override
    public void refreshEquipment() {

    }

    @Override
    public void refreshTransport() {
        // not used for infantry
    }

    @Override
    public void refreshHeader() {
        String title = getEntity().getChassis() + " " + getEntity().getModel()
                + ".blk";
        setTitle(title);

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
        // weaponTab.refresh();
    }

    @Override
    public void refreshPreview() {
        previewTab.refresh();
    }

    @Override
    public void refreshSummary() {
    }

    @Override
    public void refreshEquipmentTable() {
        structureTab.refreshEquipmentTable();
    }

    @Override
    public ITechManager getTechManager() {
        if (null != structureTab) {
            return structureTab.getTechManager();
        }
        return null;
    }

}
