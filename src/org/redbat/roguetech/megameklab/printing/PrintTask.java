/*
 * MegaMekLab - Copyright (C) 2017 - The MegaMek Team
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
package org.redbat.roguetech.megameklab.printing;

import lombok.extern.slf4j.Slf4j;
import org.redbat.roguetech.megameklab.MegaMekLab;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import java.awt.print.PrinterJob;
import java.util.concurrent.ExecutionException;

/**
 * Runs a print job as a background task.
 * 
 * TODO: Display a progress bar showing printing progress. The % progress can be determined by
 * comparing the attributes attributes JobKOctets and JobKOctetsProcessed.
 * 
 * @author Neoancient
 *
 */
@Slf4j
public class PrintTask extends SwingWorker<Void, Integer> {
    
    private final PrinterJob job;
    private final PrintRequestAttributeSet aset;
    
    public PrintTask(PrinterJob job, PrintRequestAttributeSet aset) {
        this.job = job;
        this.aset = aset;
    }

    @Override
    protected Void doInBackground() throws Exception {
        job.print(aset);
        return null;
    }
    
    @Override
    protected void done() {
        try {
            get();
        } catch (ExecutionException e) {
            log.error("Error", e);
        } catch (InterruptedException e) {
            // Shouldn't get here because we're done...
        }
    }

}
