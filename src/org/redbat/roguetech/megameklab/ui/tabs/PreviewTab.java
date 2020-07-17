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

package org.redbat.roguetech.megameklab.ui.tabs;

import org.redbat.roguetech.megamek.client.ui.swing.MechViewPanel;
import org.redbat.roguetech.megamek.common.Entity;
import org.redbat.roguetech.megamek.common.MechView;
import org.redbat.roguetech.megamek.common.templates.TROView;
import org.redbat.roguetech.megameklab.ui.EntitySource;
import org.redbat.roguetech.megameklab.util.ITab;

import javax.swing.*;
import java.awt.BorderLayout;

public class PreviewTab extends ITab {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7410436201331568734L;

    private MechViewPanel panelMekView;
    private MechViewPanel panelTROView;

	public PreviewTab(EntitySource eSource) {
	    super(eSource);
		this.setLayout(new BorderLayout());
        JTabbedPane panPreview = new JTabbedPane();

        panelMekView = new MechViewPanel();
        panelMekView.setMinimumSize(new java.awt.Dimension(300, 500));
        panelMekView.setPreferredSize(new java.awt.Dimension(300, 600));
        panPreview.addTab("Summary", panelMekView);
        
        panelTROView = new MechViewPanel();
        panPreview.addTab("TRO", panelTROView);

        add(panPreview, BorderLayout.CENTER);
        setBackground(UIManager.getColor("TabbedPane.background"));
        refresh();
	}
	
	public void refresh() {
        boolean populateTextFields = true;
        final Entity selectedUnit = eSource.getEntity();
        selectedUnit.recalculateTechAdvancement();
        MechView mechView = null;
        TROView troView = null;
        try {
            mechView = new MechView(selectedUnit, false);
            troView = TROView.createView(selectedUnit, true);
        } catch (Exception e) {
            e.printStackTrace();
            // error unit didn't load right. this is bad news.
            populateTextFields = false;
        }
        if (populateTextFields) {
            panelMekView.setMech(selectedUnit, mechView);
            panelTROView.setMech(selectedUnit, troView);
        } else {
            panelMekView.reset();
            panelTROView.reset();
        }
	}
	
}