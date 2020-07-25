/*
 * MegaMekLab - Copyright (C) 2008
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free  software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package org.redbat.roguetech.megameklab.ui.Mek.tabs;

import org.redbat.roguetech.megamek.common.Entity;
import org.redbat.roguetech.megamek.common.Mech;
import org.redbat.roguetech.megamek.common.Mounted;
import org.redbat.roguetech.megameklab.ui.EntitySource;
import org.redbat.roguetech.megameklab.ui.Mek.views.BuildView;
import org.redbat.roguetech.megameklab.ui.Mek.views.CriticalView;
import org.redbat.roguetech.megameklab.util.ITab;
import org.redbat.roguetech.megameklab.util.RefreshListener;
import org.redbat.roguetech.megameklab.util.UnitUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BuildTab extends ITab implements ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = -6756011847500605874L;

    private RefreshListener refresh = null;
    private CriticalView critView = null;
    private BuildView buildView = null;
    private JPanel buttonPanel = new JPanel();
    private JPanel mainPanel = new JPanel();

    private JButton autoFillButton = new JButton("Auto Fill");
    private JButton resetButton = new JButton("Reset");
    private JButton compactButton = new JButton("Compact");

    private String AUTOFILLCOMMAND = "autofillbuttoncommand";
    private String RESETCOMMAND = "resetbuttoncommand";
    private String COMPACTCOMMAND = "compactbuttoncommand";

    public BuildTab(EntitySource eSource, EquipmentTab equipment) {
        super(eSource);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        mainPanel.setLayout(new GridBagLayout());
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        GridBagConstraints gbc = new GridBagConstraints();

        critView = new CriticalView(eSource, true, refresh);
        buildView = new BuildView(eSource, refresh);

        autoFillButton.setMnemonic('A');
        autoFillButton.setActionCommand(AUTOFILLCOMMAND);
        resetButton.setMnemonic('R');
        resetButton.setActionCommand(RESETCOMMAND);
        compactButton.setMnemonic('C');
        compactButton.setActionCommand(COMPACTCOMMAND);
        buttonPanel.add(autoFillButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(compactButton);
        buttonPanel.setBorder(new EmptyBorder(0, 3, 1, 0));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(buildView, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0.0;
        mainPanel.add(buttonPanel, gbc);
        this.add(critView);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        this.add(mainPanel);
        refresh();
    }

    public void refresh() {
        removeAllActionListeners();
        critView.refresh();
        buildView.refresh();
        addAllActionListeners();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(AUTOFILLCOMMAND)) {
            autoFillCrits();
        } else if (e.getActionCommand().equals(RESETCOMMAND)) {
            resetCrits();
        } else if (e.getActionCommand().equals(COMPACTCOMMAND)) {
            compactCrits();
        }
    }

    private void autoFillCrits() {

        for (Mounted mount : buildView.getTableModel().getCrits()) {
            int externalEngineHS = UnitUtil.getCriticalFreeHeatSinks(getMech(), getMech().hasCompactHeatSinks());
            for (int location = Mech.LOC_HEAD; location < getMech().locations(); location++) {

                if (!UnitUtil.isValidLocation(getMech(), mount.getType(), location)) {
                    continue;
                }

                int continuousNumberOfCrits = UnitUtil.getHighestContinuousNumberOfCrits(getMech(), location);
                int critsUsed = UnitUtil.getCritsUsed(getMech(), mount.getType());
                if (continuousNumberOfCrits < critsUsed) {
                    continue;
                }
                if ((mount.getLocation() == Entity.LOC_NONE)) {
                    if (UnitUtil.isHeatSink(mount) && (externalEngineHS-- > 0)) {
                        continue;
                    }
                }

                try {
                    if (mount.getType().isSpreadable() || (mount.isSplitable() && (critsUsed > 1))) {
                        for (int count = 0; count < critsUsed; count++) {
                            UnitUtil.addMounted(getMech(), mount, location, false);
                        }
                    } else {
                        UnitUtil.addMounted(getMech(), mount, location, false);
                    }
                    UnitUtil.changeMountStatus(getMech(), mount, location, Entity.LOC_NONE, false);
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
        refresh.refreshAll();

    }

    private void resetCrits() {
        for (Mounted mount : getMech().getEquipment()) {
            if (!UnitUtil.isFixedLocationSpreadEquipment(mount.getType())) {
                UnitUtil.removeCriticals(getMech(), mount);
                UnitUtil.changeMountStatus(getMech(), mount, Entity.LOC_NONE, Entity.LOC_NONE, false);
            }
        }

        refresh.refreshAll();
    }

    private void compactCrits() {
        UnitUtil.compactCriticals(getMech());
        refresh.refreshAll();
    }

    public void removeAllActionListeners() {
        autoFillButton.removeActionListener(this);
        resetButton.removeActionListener(this);
        compactButton.removeActionListener(this);
    }

    public void addAllActionListeners() {
        autoFillButton.addActionListener(this);
        resetButton.addActionListener(this);
        compactButton.addActionListener(this);
    }

    public void addRefreshedListener(RefreshListener l) {
        refresh = l;
        critView.updateRefresh(refresh);
        buildView.addRefreshedListener(refresh);
    }

    public void refreshAll() {
        if (refresh != null) {
            refresh.refreshAll();
        }
    }

}