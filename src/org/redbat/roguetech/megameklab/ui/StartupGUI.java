/*
 * MegaMekLab - Copyright (C) 2019 - The MegaMek Team
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
package org.redbat.roguetech.megameklab.ui;

import org.redbat.roguetech.megamek.client.ui.swing.UnitLoadingDialog;
import org.redbat.roguetech.megamek.client.ui.swing.UnitSelectorDialog;
import org.redbat.roguetech.megamek.client.ui.swing.widget.MegamekButton;
import org.redbat.roguetech.megamek.client.ui.swing.widget.SkinSpecification;
import org.redbat.roguetech.megamek.client.ui.swing.widget.SkinXMLHandler;
import org.redbat.roguetech.megamek.common.*;
import org.redbat.roguetech.megamek.common.util.EncodeControl;
import org.redbat.roguetech.megamek.common.util.ImageUtil;
import org.redbat.roguetech.megamek.common.util.MegaMekFile;
import org.redbat.roguetech.megameklab.MegaMekLab;
import org.redbat.roguetech.megameklab.data.DataManager;
import org.redbat.roguetech.megameklab.ui.dialog.LoadingDialog;
import org.redbat.roguetech.megameklab.util.RoguetechConfigurationDialog;
import org.redbat.roguetech.megameklab.util.UnitUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ResourceBundle;
import java.util.TreeMap;

import static org.redbat.roguetech.megameklab.util.CConfig.isRoguetechConfigSet;

/**
 * A startup splash screen for MegaMekLab
 * @author Taharqa
 */
public class StartupGUI extends javax.swing.JPanel {

    private static final long serialVersionUID = 8376874926997734492L;
    JFrame frame;
    Image imgSplash;
    BufferedImage backgroundIcon;

    /** A map of resolution widths to file names for the startup screen */
    private final TreeMap<Integer, String> startupScreenImages = new TreeMap<>();
    {
        startupScreenImages.put(0, "data/images/misc/mml_start_spooky_hd.jpg");
        startupScreenImages.put(1441, "data/images/misc/mml_start_spooky_fhd.jpg");
        startupScreenImages.put(1921, "data/images/misc/mml_start_spooky_uhd.jpg");
    }

    private final ResourceBundle resourceMap = ResourceBundle.getBundle("megameklab.resources.Splash", new EncodeControl());

    public StartupGUI() {
        initComponents();
    }

    private void initComponents() {
        SkinSpecification skinSpec = SkinXMLHandler.getSkin(SkinSpecification.UIComponents.MainMenuBorder.getComp(),
                true);

        frame = new JFrame("Roguetech MegaMekLab");
        setBackground(UIManager.getColor("controlHighlight"));

        imgSplash = getToolkit().getImage(startupScreenImages.floorEntry((int)MegaMekLab.calculateMaxScreenWidth()).getValue());
        // wait for splash image to load completely
        MediaTracker tracker = new MediaTracker(frame);
        tracker.addImage(imgSplash, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            // really should never come here
        }

        // make splash image panel
        ImageIcon icon = new ImageIcon(imgSplash);
        JLabel panTitle = new JLabel(icon);
        add(panTitle, BorderLayout.CENTER);

        if (skinSpec.hasBackgrounds()) {
            if (skinSpec.backgrounds.size() > 1) {
                File file = new MegaMekFile(Configuration.widgetsDir(),
                        skinSpec.backgrounds.get(1)).getFile();
                if (!file.exists()){
                    System.err.println("MainMenu Error: background icon doesn't exist: "
                            + file.getAbsolutePath());
                } else {
                    backgroundIcon = (BufferedImage) ImageUtil.loadImageFromFile(file.toString());
                }
            }
        } else {
            backgroundIcon = null;
        }

        while (!isRoguetechConfigSet()) {
            if (openConfiguration()) {
                System.exit(0);
            }
        }

        DataManager.initialize();

        JLabel labVersion = new JLabel(resourceMap.getString("version.text") + MegaMekLab.VERSION, JLabel.CENTER); //$NON-NLS-1$
        labVersion.setPreferredSize(new Dimension(250,15));
        if (skinSpec.fontColors.size() > 0) {
            labVersion.setForeground(skinSpec.fontColors.get(0));
        }

        MegamekButton btnConfiguration = new MegamekButton(resourceMap.getString("btnConfiguration.text"), //$NON-NLS-1$
                SkinSpecification.UIComponents.MainMenuButton.getComp(), true);
        btnConfiguration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openConfiguration();
            }
        });

        MegamekButton btnLoadUnit = new MegamekButton(resourceMap.getString("btnLoadUnit.text"), //$NON-NLS-1$
                SkinSpecification.UIComponents.MainMenuButton.getComp(), true);
        btnLoadUnit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadUnit();
            }
        });

        MegamekButton btnNewMek = new MegamekButton(resourceMap.getString("btnNewMek.text"), //$NON-NLS-1$
                SkinSpecification.UIComponents.MainMenuButton.getComp(), true);
        btnNewMek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newUnit(Entity.ETYPE_MECH);
            }
        });

        MegamekButton btnNewVee = new MegamekButton(resourceMap.getString("btnNewVee.text"), //$NON-NLS-1$
                SkinSpecification.UIComponents.MainMenuButton.getComp(), true);
        btnNewVee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newUnit(Entity.ETYPE_TANK);
            }
        });

        MegamekButton btnQuit = new MegamekButton(resourceMap.getString("btnQuit.text"), //$NON-NLS-1$
                SkinSpecification.UIComponents.MainMenuButton.getComp(), true);
        btnQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(0);
            }
        });

        // Use the current monitor so we don't "overflow" computers whose primary
        // displays aren't as large as their secondary displays.
        DisplayMode currentMonitor = frame.getGraphicsConfiguration().getDevice().getDisplayMode();
        FontMetrics metrics = btnNewVee.getFontMetrics(btnNewVee.getFont());
        int width = metrics.stringWidth(btnNewVee.getText());
        int height = metrics.getHeight();
        Dimension textDim =  new Dimension(width+50, height+10);

        // Strive for no more than ~90% of the screen and use golden ratio to make
        // the button width "look" reasonable.
        int imageWidth = imgSplash.getWidth(frame);
        int maximumWidth = (int)(0.9 * currentMonitor.getWidth()) - imageWidth;
        //no more than 50% of image width
        if(maximumWidth > (int) (0.5 * imageWidth)) {
            maximumWidth = (int) (0.5 * imageWidth);
        }
        Dimension minButtonDim = new Dimension((int)(maximumWidth / 1.618), 25);
        if (textDim.getWidth() > minButtonDim.getWidth()) {
            minButtonDim = textDim;
        }

        btnConfiguration.setMinimumSize(minButtonDim);
        btnConfiguration.setPreferredSize(minButtonDim);
        btnLoadUnit.setMinimumSize(minButtonDim);
        btnLoadUnit.setPreferredSize(minButtonDim);
        btnNewMek.setMinimumSize(minButtonDim);
        btnNewMek.setPreferredSize(minButtonDim);
        btnNewVee.setMinimumSize(minButtonDim);
        btnNewVee.setPreferredSize(minButtonDim);
        btnQuit.setMinimumSize(minButtonDim);
        btnQuit.setPreferredSize(minButtonDim);

        btnNewVee.setEnabled(false);

        // layout
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        // Left Column
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 5, 10, 10);
        c.ipadx = 10; c.ipady = 5;
        c.gridx = 0;  c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0; c.weighty = 0.0;
        c.gridwidth = 1;
        c.gridheight = 12;
        add(panTitle, c);
        // Right Column
        c.insets = new Insets(2, 2, 2, 10);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0; c.weighty = 1.0;
        c.ipadx = 0; c.ipady = 0;
        c.gridheight = 1;
        c.gridx = 1; c.gridy = 0;
        add(labVersion, c);
        c.insets = new Insets(5, 2, 2, 10);
        c.gridy++;
        add(btnConfiguration, c);
        c.insets = new Insets(20, 2, 2, 10);
        c.gridy++;
        add(btnLoadUnit, c);
        c.insets = new Insets(2, 2, 2, 10);
        c.gridy++;
        add(btnNewMek, c);
        c.gridy++;
        add(btnNewVee, c);
        c.insets = new Insets(100, 2, 2, 10);
        c.gridy++;
        add(btnQuit, c);

        frame.setResizable(false);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                System.exit(0);
            }
        });
        frame.validate();
        frame.pack();

        // Determine the location of the window
        int w = frame.getSize().width;
        int h = frame.getSize().height;
        int x = (currentMonitor.getWidth()-w)/2;
        int y = (currentMonitor.getHeight()-h)/2;
        frame.setLocation(x, y);

        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundIcon == null){
            return;
        }
        int w = getWidth();
        int h = getHeight();
        int iW = backgroundIcon.getWidth();
        int iH = backgroundIcon.getHeight();
        // If the image isn't loaded, prevent an infinite loop
        if ((iW < 1) || (iH < 1)) {
            return;
        }
        for (int x = 0; x < w; x+=iW){
            for (int y = 0; y < h; y+=iH){
                g.drawImage(backgroundIcon, x, y,null);
            }
        }
     }

    /**
     * This function will create a new mainUI frame (via the loading dialog) for the
     * given unit type and get rid of the splash screen
     * @param type an <code>int</code> corresponding to the unit type to construct
     */
    private void newUnit(long type) {
        newUnit(type, false, false, null);
    }

    private void newUnit(long type, boolean primitive, boolean industrial, Entity en) {
        frame.setVisible(false);
        LoadingDialog ld = new LoadingDialog(frame, type, primitive, industrial, en);
        ld.setVisible(true);
    }

    private void loadUnit() {
        //EquipmentType.initializeTypes();
        UnitLoadingDialog unitLoadingDialog = new UnitLoadingDialog(frame);
        unitLoadingDialog.setVisible(true);
        UnitSelectorDialog viewer = new UnitSelectorDialog(frame, unitLoadingDialog, true);

        Entity newUnit = viewer.getChosenEntity();
        viewer.setVisible(false);
        viewer.dispose();

        if (null == newUnit) {
            return;
        }

        if (UnitUtil.validateUnit(newUnit).trim().length() > 0) {
            JOptionPane.showMessageDialog(frame, String.format(
                    resourceMap.getString("message.invalidUnit.format"),
                            UnitUtil.validateUnit(newUnit)));
        }

        if (newUnit instanceof Mech) {
            newUnit(Entity.ETYPE_MECH, false, false, newUnit);
        } else if ((newUnit instanceof Tank)
                && !(newUnit instanceof GunEmplacement)) {
            newUnit(Entity.ETYPE_TANK, false, false, newUnit);
        }
        return;
    }

    private boolean openConfiguration() {
        RoguetechConfigurationDialog roguetechConfigurationDialog = new RoguetechConfigurationDialog(frame);
        roguetechConfigurationDialog.setLocationRelativeTo(this);
        roguetechConfigurationDialog.setVisible(true);
        return roguetechConfigurationDialog.getCancelled();
    }
}
