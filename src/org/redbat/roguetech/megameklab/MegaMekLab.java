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

package org.redbat.roguetech.megameklab;

import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megamek.MegaMek;
import org.redbat.roguetech.megamek.common.Configuration;
import org.redbat.roguetech.megamek.common.EquipmentType;
import org.redbat.roguetech.megamek.common.MechSummaryCache;
import org.redbat.roguetech.megamek.common.QuirksHandler;
import org.redbat.roguetech.megamek.common.logging.MMLogger;
import org.redbat.roguetech.megamek.common.preference.PreferenceManager;
import org.redbat.roguetech.megameklab.ui.StartupGUI;
import org.redbat.roguetech.megameklab.util.CConfig;
import org.redbat.roguetech.megameklab.util.UnitUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Slf4j
public class MegaMekLab {
    public static final String VERSION = "0.1.1-SNAPSHOT";

    public static void main(String[] args) {
    	System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name","RoguetechMegaMekLab");
        // Register any fonts in the fonts directory
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        List<Font> fontList = new ArrayList<>();
        collectFontsFromDir(Configuration.fontsDir(), fontList);
        for (Font font : fontList) {
            ge.registerFont(font);
        }
        startup();
    }

    /**
     * Recursively search a directory and attempt to create a truetype font from
     * every file with the ttf suffix
     *
     * @param dir  The directory to search
     * @param list The list to add fonts to as they are created
     */
    private static void collectFontsFromDir(File dir, List<Font> list) {
        final String METHOD_NAME = "collectFontsFromDir(File, List<Font>)"; //$NON-NLS-1$
        File[] files = dir.listFiles();
        if (null != files) {
            for (File f : files) {
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    collectFontsFromDir(f, list);
                } else if (f.getName().toLowerCase().endsWith(".ttf")) {
                    try {
                        list.add(Font.createFont(Font.TRUETYPE_FONT, f));
                    } catch (IOException | FontFormatException ex) {
                        log.error("Error creating font from " + f, ex);
                    }
                }
            }
        }
    }

    /**
     * Prints some information about MegaMekLab. Used in logfiles to figure out the
     * JVM and version of MegaMekLab.
     */
    private static void showInfo() {
        final String METHOD_NAME = "showInfo";
        final long TIMESTAMP = new File(PreferenceManager
                .getClientPreferences().getLogDirectory()
                + File.separator
                + "timestamp").lastModified();
        // echo some useful stuff
        String msg = "Starting Roguetech MegaMekLab v" + VERSION + " ..."; //$NON-NLS-1$ //$NON-NLS-2$
        if (TIMESTAMP > 0) {
            msg += "\n\tCompiled on " + new Date(TIMESTAMP).toString(); //$NON-NLS-1$
        }
        msg += "\n\tToday is " + new Date().toString(); //$NON-NLS-1$
        msg += "\n\tJava vendor " + System.getProperty("java.vendor"); //$NON-NLS-1$ //$NON-NLS-2$
        msg += "\n\tJava version " + System.getProperty("java.version"); //$NON-NLS-1$ //$NON-NLS-2$
        msg += "\n\tPlatform " //$NON-NLS-1$
               + System.getProperty("os.name") //$NON-NLS-1$
               + " " //$NON-NLS-1$
               + System.getProperty("os.version") //$NON-NLS-1$
               + " (" //$NON-NLS-1$
               + System.getProperty("os.arch") //$NON-NLS-1$
               + ")"; //$NON-NLS-1$
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
        msg += "\n\tTotal memory available to MegaMek: " + NumberFormat.getInstance().format(maxMemory) + " kB"; //$NON-NLS-1$ //$NON-NLS-2$
        log.info(msg);
    }
    
    private static void startup() {
        showInfo();
        Locale.setDefault(Locale.US);
        EquipmentType.initializeTypes();
        MechSummaryCache.getInstance();
        try {
            QuirksHandler.initQuirksList();
        } catch (IOException e) {
            log.warn("Could not load quirks");
        }
        new CConfig();
        UnitUtil.loadFonts();

        // Add additional themes
        UIManager.installLookAndFeel("Flat Light", "com.formdev.flatlaf.FlatLightLaf");
        UIManager.installLookAndFeel("Flat IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf");
        UIManager.installLookAndFeel("Flat Dark", "com.formdev.flatlaf.FlatDarkLaf");
        UIManager.installLookAndFeel("Flat Darcula", "com.formdev.flatlaf.FlatDarculaLaf");

        setLookAndFeel();
        //create a start up frame and display it
        StartupGUI sud = new StartupGUI();
        sud.setVisible(true);
    }
    
    private static void setLookAndFeel() {
        try {
            String plaf = CConfig.getParam(CConfig.CONFIG_PLAF, UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel(plaf);
        } catch (Exception e) {
            log.error("Unable to set look and feel", e);
       }
    }
    
    /**
     * Helper function that calculates the maximum screen width available locally.
     * @return Maximum screen width.
     */
    public static double calculateMaxScreenWidth() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        double maxWidth = 0;
        for (GraphicsDevice g : gs) {
            Rectangle b = g.getDefaultConfiguration().getBounds();
            if (b.getWidth() > maxWidth) {   // Update the max size found on this monitor
                maxWidth = b.getWidth();
            }
        }
        
        return maxWidth;
    }
}