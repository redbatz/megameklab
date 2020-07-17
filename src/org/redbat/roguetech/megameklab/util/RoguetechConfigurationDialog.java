package org.redbat.roguetech.megameklab.util;

import org.redbat.roguetech.megamek.common.util.EncodeControl;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

public final class RoguetechConfigurationDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = -6504846822457360057L;

    private final static String saveCommand = "Save"; //$NON-NLS-1$
    private final static String cancelCommand = "Cancel"; //$NON-NLS-1$

    private final JPanel panRoguetech = new JPanel(new GridBagLayout());

    private final JTextField txtRoguetechLocation = new JTextField(50);
    private final JTextField txtCabLocation = new JTextField(50);
    private final JTextField txtBattletechLocation = new JTextField(50);
    private final JTextField txtHbsLocation = new JTextField(50);
    private boolean cancelled = false;

    public RoguetechConfigurationDialog(JFrame frame) {
        super(frame, true);

        ResourceBundle resourceMap = ResourceBundle.getBundle("megameklab.resources.Roguetech", new EncodeControl()); //$NON-NLS-1$
        setTitle(resourceMap.getString("RoguetechConfigurationDialog.windowName.text")); //$NON-NLS-1$

        JPanel panButtons = new JPanel();
        // BUTTONS
        JButton button = new JButton(saveCommand);
        button.setText(resourceMap.getString("RoguetechConfigurationDialog.btnSave.text")); //$NON-NLS-1$
        button.setToolTipText(resourceMap.getString("RoguetechConfigurationDialog.btnSave.tooltip")); //$NON-NLS-1$
        button.setActionCommand(saveCommand);
        button.addActionListener(this);
        panButtons.add(button);

        button = new JButton(cancelCommand);
        button.setText(resourceMap.getString("RoguetechConfigurationDialog.btnCancel.text")); //$NON-NLS-1$
        button.setToolTipText(resourceMap.getString("RoguetechConfigurationDialog.btnCancel.tooltip")); //$NON-NLS-1$
        button.setActionCommand(cancelCommand);
        button.addActionListener(this);
        panButtons.add(button);
        add(panButtons, BorderLayout.SOUTH);

        loadRoguetechConfigurationPanel(resourceMap);

        add(panRoguetech);

        pack();
    }

    private void loadRoguetechConfigurationPanel(ResourceBundle resourceMap) {
        createConfigPanel(resourceMap, CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY);


        createConfigPanel(resourceMap, CConfig.CONFIG_ROGUETECH_CAB_DIRECTORY);
        createConfigPanel(resourceMap, CConfig.CONFIG_ROGUETECH_BATTLETECH_DIRECTORY);
        createConfigPanel(resourceMap, CConfig.CONFIG_ROGUETECH_HBS_DIRECTORY);
    }

    private void createConfigPanel(ResourceBundle resourceMap, String configKey) {
        String resourceKey = findResourceKey(configKey);
        JTextField txtField = findTextField(configKey);


        JLabel labLocation = new JLabel(resourceMap.getString("RoguetechConfigurationDialog." + resourceKey + ".text"));
        panRoguetech.add(labLocation, getConstraint(configKey, 0));

        String text = CConfig.getParam(configKey);
        if (!text.isEmpty()) {
            txtField.setText(text);
        }
        txtField.setToolTipText(resourceMap.getString("RoguetechConfigurationDialog." + resourceKey + ".tooltip"));
        txtField.setEnabled(false);
        panRoguetech.add(txtField, getConstraint(configKey, 1));

        JButton butRoguetechLocation = new JButton(resourceMap.getString("RoguetechConfigurationDialog.btnBrowse.text"));
        butRoguetechLocation.addActionListener(al -> {
            JFileChooser fileRoguetechLocation = new JFileChooser();
            fileRoguetechLocation.setDialogTitle(resourceMap.getString("RoguetechConfigurationDialog." + resourceKey + ".window"));
            fileRoguetechLocation.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileRoguetechLocation.showDialog(panRoguetech, resourceMap.getString("RoguetechConfigurationDialog.btnSelect.text"));
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileRoguetechLocation.getSelectedFile();
                txtField.setText(selectedFile.toPath().toAbsolutePath().toString());
            }
        });
        panRoguetech.add(butRoguetechLocation, getConstraint(configKey, 2));

    }

    private GridBagConstraints getConstraint(String configKey, int column) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, column == 2 ? 0 : 5, 0, column == 1 ? 0 : 5);
        constraints.ipadx = 1;
        constraints.gridx = column;
        constraints.gridy = findPanelRow(configKey);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        return constraints;
    }

    private int findPanelRow(String configKey) {
        switch (configKey) {
            case CConfig.CONFIG_ROGUETECH_BATTLETECH_DIRECTORY:
                return 2;
            case CConfig.CONFIG_ROGUETECH_CAB_DIRECTORY:
                return 1;
            case CConfig.CONFIG_ROGUETECH_HBS_DIRECTORY:
                return 3;
            case CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY:
                return 0;
            default:
                throw new IllegalArgumentException("Not supported argument");
        }
    }

    private String findResourceKey(String configKey) {
        switch (configKey) {
            case CConfig.CONFIG_ROGUETECH_BATTLETECH_DIRECTORY:
                return "fileBattletechLocation";
            case CConfig.CONFIG_ROGUETECH_CAB_DIRECTORY:
                return "fileCabLocation";
            case CConfig.CONFIG_ROGUETECH_HBS_DIRECTORY:
                return "fileHbsLocation";
            case CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY:
                return "fileRoguetechLocation";
            default:
                throw new IllegalArgumentException("Not supported argument");
        }
    }

    private JTextField findTextField(String configKey) {
        switch (configKey) {
            case CConfig.CONFIG_ROGUETECH_BATTLETECH_DIRECTORY:
                return txtBattletechLocation;
            case CConfig.CONFIG_ROGUETECH_CAB_DIRECTORY:
                return txtCabLocation;
            case CConfig.CONFIG_ROGUETECH_HBS_DIRECTORY:
                return txtHbsLocation;
            case CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY:
                return txtRoguetechLocation;
            default:
                throw new IllegalArgumentException("Not supported argument");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals(saveCommand)) {
            saveConfig();
            setVisible(false);
        } else if (command.equals(cancelCommand)) {
            CConfig.loadConfigFile();
            setCancelled(true);
            setVisible(false);
        }
    }

    private void saveConfig() {
        CConfig.setParam(CConfig.CONFIG_ROGUETECH_ROGUETECH_DIRECTORY, txtRoguetechLocation.getText());
        CConfig.setParam(CConfig.CONFIG_ROGUETECH_BATTLETECH_DIRECTORY, txtBattletechLocation.getText());
        CConfig.setParam(CConfig.CONFIG_ROGUETECH_CAB_DIRECTORY, txtCabLocation.getText());
        CConfig.setParam(CConfig.CONFIG_ROGUETECH_HBS_DIRECTORY, txtHbsLocation.getText());
        CConfig.saveConfig();
    }

    public boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}