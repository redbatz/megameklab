/*
 * MegaMekLab - Copyright (C) 2008
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is  free software; you can redistribute it and/or modify it
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

import org.redbat.roguetech.megamek.common.*;
import org.redbat.roguetech.megameklab.data.ComponentManager;
import org.redbat.roguetech.megameklab.data.category.Category;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIGroup;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIMapping;
import org.redbat.roguetech.megameklab.data.category.gui.CategoryGUIType;
import org.redbat.roguetech.megameklab.data.category.gui.DefaultCategoryGUIType;
import org.redbat.roguetech.megameklab.data.component.ComponentTags;
import org.redbat.roguetech.megameklab.data.component.ComponentType;
import org.redbat.roguetech.megameklab.data.component.type.Component;
import org.redbat.roguetech.megameklab.data.repository.CategoryGUIMappingsRepository;
import org.redbat.roguetech.megameklab.ui.EntitySource;
import org.redbat.roguetech.megameklab.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EquipmentTab extends ITab implements ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = 3978675469713289404L;

    final private JCheckBox chkShowBlacklisted = new JCheckBox("Show Blacklisted");
    final private JCheckBox chkShowLootable = new JCheckBox("Show Lootable");
    private RefreshListener refresh;
    private JButton addButton = new JButton("Add");
    private JButton removeButton = new JButton("Remove");
    private JButton removeAllButton = new JButton("Remove All");
    private JComboBox<CategoryGUIGroup> catGrpChoiceType = new JComboBox<>();
    private JComboBox<CategoryGUIType> catTypChoiceType = new JComboBox<>();
    private JTextField txtFilter = new JTextField();
    private TableRowSorter<EquipmentTableModel> equipmentSorter;

    private CriticalTableModel equipmentList;
    private EquipmentTableModel masterEquipmentList;
    private JTable masterEquipmentTable = new JTable();
    private JScrollPane masterEquipmentScroll = new JScrollPane();
    private JTable equipmentTable = new JTable();
    private JScrollPane equipmentScroll = new JScrollPane();

    private String ADD_COMMAND = "ADD";
    private String REMOVE_COMMAND = "REMOVE";
    private String REMOVEALL_COMMAND = "REMOVEALL";
    private ListSelectionListener selectionListener = new ListSelectionListener() {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int selected = masterEquipmentTable.getSelectedRow();
            EquipmentType etype = null;
            if (selected >= 0) {
                etype = masterEquipmentList.getType(masterEquipmentTable.convertRowIndexToModel(selected));
            }
            addButton.setEnabled((null != etype));
        }
    };

    public EquipmentTab(EntitySource eSource) {
        super(eSource);

        equipmentList = new CriticalTableModel(eSource.getEntity(), CriticalTableModel.WEAPONTABLE);
        equipmentTable.setModel(equipmentList);
        equipmentTable.setIntercellSpacing(new Dimension(0, 0));
        equipmentTable.setShowGrid(false);
        equipmentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        equipmentTable.setDoubleBuffered(true);
        TableColumn column;
        for (int i = 0; i < equipmentList.getColumnCount(); i++) {
            column = equipmentTable.getColumnModel().getColumn(i);
            if (i == CriticalTableModel.NAME) {
                column.setPreferredWidth(300);
            } else if (i == CriticalTableModel.SIZE) {
                column.setCellEditor(equipmentList.new SpinnerCellEditor());
            }
            column.setCellRenderer(equipmentList.getRenderer());

        }
        equipmentList.addTableModelListener(ev -> {
            if (refresh != null) {
                refresh.refreshStatus();
                refresh.refreshPreview();
                refresh.refreshBuild();
                refresh.refreshSummary();
            }
        });
        equipmentScroll.setViewportView(equipmentTable);
        equipmentScroll.setMinimumSize(new java.awt.Dimension(600, 200));
        equipmentScroll.setPreferredSize(new java.awt.Dimension(600, 200));

        masterEquipmentList = new EquipmentTableModel(eSource.getEntity(), eSource.getTechManager());
        masterEquipmentTable.setModel(masterEquipmentList);
        masterEquipmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        equipmentSorter = new TableRowSorter<>(masterEquipmentList);
        for (int col = 0; col < EquipmentTableModel.N_COL; col++) {
            equipmentSorter.setComparator(col, masterEquipmentList.getSorter(col));
        }
        masterEquipmentTable.setRowSorter(equipmentSorter);
        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(EquipmentTableModel.COL_ID, SortOrder.ASCENDING));
        equipmentSorter.setSortKeys(sortKeys);
        XTableColumnModel equipColumnModel = new XTableColumnModel();
        masterEquipmentTable.setColumnModel(equipColumnModel);
        masterEquipmentTable.createDefaultColumnsFromModel();
        for (int i = 0; i < EquipmentTableModel.N_COL; i++) {
            column = masterEquipmentTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(masterEquipmentList.getColumnWidth(i));
            column.setCellRenderer(masterEquipmentList.getRenderer());
        }
        masterEquipmentTable.setIntercellSpacing(new Dimension(0, 0));
        masterEquipmentTable.setShowGrid(false);
        masterEquipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        masterEquipmentTable.getSelectionModel().addListSelectionListener(selectionListener);
        masterEquipmentTable.setDoubleBuffered(true);
        masterEquipmentScroll.setViewportView(masterEquipmentTable);

        masterEquipmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int view = target.getSelectedRow();
                    int selected = masterEquipmentTable.convertRowIndexToModel(view);
                    EquipmentType equip = masterEquipmentList.getType(selected);
                    addEquipment(equip);
                    fireTableRefresh();
                }
            }
        });

        masterEquipmentTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "add");
        masterEquipmentTable.getActionMap().put("add", new EnterAction());

        masterEquipmentList.setData(ComponentManager.getAll());

        loadEquipmentTable();

        DefaultComboBoxModel<CategoryGUIGroup> categorySectionModel = new DefaultComboBoxModel<>();
        CategoryGUIGroup[] categoryGUIGroups = CategoryGUIGroup.values();
        for (CategoryGUIGroup categoryGUIGroup : categoryGUIGroups) {
            categorySectionModel.addElement(categoryGUIGroup);

        }
        catGrpChoiceType.setModel(categorySectionModel);
        catGrpChoiceType.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getName()));
        catGrpChoiceType.setSelectedIndex(0);

        DefaultComboBoxModel<CategoryGUIType> categoryGroupModel = buildCategoryComboBoxModel(new CategoryGUIType[0]);
        catTypChoiceType.setModel(categoryGroupModel);
        catTypChoiceType.setSelectedIndex(0);
        catTypChoiceType.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getName()));
        catTypChoiceType.setMinimumSize(new java.awt.Dimension(250, 20));
        catTypChoiceType.setPreferredSize(new java.awt.Dimension(250, 20));
        catTypChoiceType.addActionListener(evt -> filterEquipment());
        catGrpChoiceType.addActionListener(this::updateCategoryGroupList);

        txtFilter.setText("");
        txtFilter.setMinimumSize(new java.awt.Dimension(200, 28));
        txtFilter.setPreferredSize(new java.awt.Dimension(200, 28));
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterEquipment();
            }

            public void removeUpdate(DocumentEvent e) {
                filterEquipment();
            }

            public void changedUpdate(DocumentEvent e) {
                filterEquipment();
            }
        });

        filterEquipment();
        addButton.setMnemonic('A');
        removeButton.setMnemonic('R');
        removeAllButton.setMnemonic('l');

        chkShowBlacklisted.addActionListener(ev -> filterEquipment());
        chkShowLootable.addActionListener(ev -> filterEquipment());
        JPanel viewPanel = new JPanel(new GridLayout(0, 2));
        viewPanel.add(chkShowBlacklisted);
        viewPanel.add(chkShowLootable);
        setEquipmentView();

        //layout
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();

        JPanel loadoutPanel = new JPanel(new GridBagLayout());
        JPanel databasePanel = new JPanel(new GridBagLayout());

        loadoutPanel.setBorder(BorderFactory.createTitledBorder("Current Loadout"));
        databasePanel.setBorder(BorderFactory.createTitledBorder("Equipment Database"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        databasePanel.add(addButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        databasePanel.add(catGrpChoiceType, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        databasePanel.add(catTypChoiceType, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        databasePanel.add(txtFilter, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        databasePanel.add(viewPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        loadoutPanel.add(removeButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        loadoutPanel.add(removeAllButton, gbc);

        gbc.insets = new Insets(2, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        databasePanel.add(masterEquipmentScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        loadoutPanel.add(equipmentScroll, gbc);

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(loadoutPanel),
                new JScrollPane(databasePanel));
        pane.setOneTouchExpandable(true);
        setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
    }

    private DefaultComboBoxModel<CategoryGUIType> buildCategoryComboBoxModel(CategoryGUIType[] categoryGUITypes) {
        DefaultComboBoxModel<CategoryGUIType> categoryGroupModel = new DefaultComboBoxModel<>();
        categoryGroupModel.addElement(DefaultCategoryGUIType.ALL);

        for (CategoryGUIType categoryGUIType : categoryGUITypes) {
            categoryGroupModel.addElement(categoryGUIType);

        }
        return categoryGroupModel;
    }

    @SuppressWarnings("unchecked")
    private void updateCategoryGroupList(ActionEvent evt) {
        if (evt.getActionCommand().equals("comboBoxChanged")) {
            JComboBox<CategoryGUIGroup> source = (JComboBox<CategoryGUIGroup>) evt.getSource();
            CategoryGUIGroup selectedItem = (CategoryGUIGroup) source.getSelectedItem();
            if (selectedItem == null) {
                return;
            }

            CategoryGUIType[] categoryGUITypes = new CategoryGUIType[0];
            if (selectedItem != CategoryGUIGroup.ALL) {
                categoryGUITypes = CategoryGUIType.getCategoryGroup(selectedItem);
            }
            DefaultComboBoxModel<CategoryGUIType> model = buildCategoryComboBoxModel(categoryGUITypes);
            catTypChoiceType.setModel(model);
            filterEquipment();
        }
    }

    private void loadEquipmentTable() {

        for (Mounted mount : getMech().getWeaponList()) {
            equipmentList.addCrit(mount);
        }

        for (Mounted mount : getMech().getAmmo()) {
            equipmentList.addCrit(mount);
        }

        List<EquipmentType> spreadAlreadyAdded = new ArrayList<>();

        for (Mounted mount : getMech().getMisc()) {

            EquipmentType etype = mount.getType();
            if (UnitUtil.isHeatSink(mount)
                    || etype.hasFlag(MiscType.F_JUMP_JET)
                    || etype.hasFlag(MiscType.F_JUMP_BOOSTER)
                    || etype.hasFlag(MiscType.F_TSM)
                    || etype.hasFlag(MiscType.F_INDUSTRIAL_TSM)
                    || (etype.hasFlag(MiscType.F_MASC)
                    && !etype.hasSubType(MiscType.S_SUPERCHARGER))
                    || ((getMech().getEntityType() & Entity.ETYPE_QUADVEE) == Entity.ETYPE_QUADVEE
                    && etype.hasFlag(MiscType.F_TRACKS))
                    || UnitUtil.isArmorOrStructure(etype)) {
                continue;
            }
            //if (UnitUtil.isUnitEquipment(mount.getType(), unit) || UnitUtil.isUn) {
            if (UnitUtil.isFixedLocationSpreadEquipment(etype)
                    && !spreadAlreadyAdded.contains(etype)) {
                equipmentList.addCrit(mount);
                // keep track of spreadable equipment here, so it doesn't
                // show up multiple times in the table
                spreadAlreadyAdded.add(etype);
            } else {
                equipmentList.addCrit(mount);
            }
            //}
        }


    }

    private void addEquipment(EquipmentType equip) {
        boolean success = false;
        Mounted mount = null;
        boolean isMisc = equip instanceof MiscType;
        if (isMisc && equip.hasFlag(MiscType.F_TARGCOMP)) {
            if (!UnitUtil.hasTargComp(getMech())) {
                mount = UnitUtil.updateTC(getMech(), equip);
                success = mount != null;
            }
        } else if (isMisc && UnitUtil.isFixedLocationSpreadEquipment(equip)) {
            mount = UnitUtil.createSpreadMounts(getMech(), equip);
            success = mount != null;
        } else {
            try {
                mount = new Mounted(getMech(), equip);
                getMech().addEquipment(mount, Entity.LOC_NONE, false);
                success = true;
            } catch (LocationFullException lfe) {
                // this can't happen, we add to Entity.LOC_NONE
            }
        }
        if (success) {
            equipmentList.addCrit(mount);
        }
    }

    private void fireTableRefresh() {
        equipmentList.updateUnit(getMech());
        equipmentList.refreshModel();
        if (refresh != null) {
            refresh.refreshStatus();
            refresh.refreshBuild();
            refresh.refreshPreview();
            refresh.refreshSummary();
        }
    }

    private void filterEquipment() {
        RowFilter<EquipmentTableModel, Integer> equipmentTypeFilter = new EquipmentTableModelFilter();
        equipmentSorter.setRowFilter(equipmentTypeFilter);
    }

    public void setEquipmentView() {
        XTableColumnModel columnModel = (XTableColumnModel) masterEquipmentTable.getColumnModel();
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_ID), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_DAMAGE), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_DIVISOR), false);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_SPECIAL), false);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_HEAT), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_MRANGE), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_RANGE), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_SHOTS), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_TECH), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_COST), false);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_BV), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_TON), true);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(EquipmentTableModel.COL_CRIT), true);

    }

    public void addRefreshedListener(RefreshListener l) {
        refresh = l;
    }

    public void refresh() {
        removeAllListeners();
        filterEquipment();
        updateEquipment();
        addAllListeners();
        fireTableRefresh();
    }

    private void removeAllListeners() {
        addButton.removeActionListener(this);
        removeButton.removeActionListener(this);
        removeAllButton.removeActionListener(this);
    }

    private void addAllListeners() {
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        removeAllButton.addActionListener(this);
        addButton.setActionCommand(ADD_COMMAND);
        removeButton.setActionCommand(REMOVE_COMMAND);
        removeAllButton.setActionCommand(REMOVEALL_COMMAND);
        addButton.setMnemonic('A');
        removeButton.setMnemonic('R');
        removeAllButton.setMnemonic('L');
    }

    public void updateEquipment() {
        removeHeatSinks();
        equipmentList.removeAllCrits();
        loadEquipmentTable();
    }

    private void removeHeatSinks() {
        int location = 0;
        for (; location < equipmentList.getRowCount(); ) {

            Mounted mount = (Mounted) equipmentList.getValueAt(location, CriticalTableModel.EQUIPMENT);
            EquipmentType eq = mount.getType();
            if ((eq instanceof MiscType) && (UnitUtil.isHeatSink(mount))) {
                try {
                    equipmentList.removeCrit(location);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                location++;
            }
        }
    }

    public void refreshTable() {
        filterEquipment();
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals(ADD_COMMAND)) {
            int view = masterEquipmentTable.getSelectedRow();
            if (view < 0) {
                //selection got filtered away
                return;
            }
            int selected = masterEquipmentTable.convertRowIndexToModel(view);
            EquipmentType equip = masterEquipmentList.getType(selected);
            addEquipment(equip);
        } else if (e.getActionCommand().equals(REMOVE_COMMAND)) {
            int selectedRows[] = equipmentTable.getSelectedRows();
            for (Integer row : selectedRows) {
                equipmentList.removeMounted(row);
            }
            equipmentList.removeCrits(selectedRows);
        } else if (e.getActionCommand().equals(REMOVEALL_COMMAND)) {
            removeAllEquipment();
        } else {
            return;
        }
        fireTableRefresh();
    }

    public void removeAllEquipment() {
        removeHeatSinks();
        for (int count = 0; count < equipmentList.getRowCount(); count++) {
            equipmentList.removeMounted(count);
        }
        equipmentList.removeAllCrits();
    }

    public CriticalTableModel getEquipmentList() {
        return equipmentList;
    }

    private class EnterAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 8247993757008802162L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int view = masterEquipmentTable.getSelectedRow();
            if (view < 0) {
                //selection got filtered away
                return;
            }
            int selected = masterEquipmentTable.convertRowIndexToModel(view);
            EquipmentType equip = masterEquipmentList.getType(selected);
            addEquipment(equip);
            fireTableRefresh();
        }
    }

    private class EquipmentTableModelFilter extends RowFilter<EquipmentTableModel, Integer> {
        @Override
        public boolean include(Entry<? extends EquipmentTableModel, ? extends Integer> entry) {
            EquipmentTableModel equipModel = entry.getModel();
            EquipmentType etype = equipModel.getType(entry.getIdentifier());
            if (etype instanceof Component) {
                Component component = (Component) etype;
                if (component.isLinked() || component.isDeprecated()) {
                    return false;
                }
                boolean blacklisted = component.getTags().stream().anyMatch(tag -> tag.equals(ComponentTags.BLACKLISTED));
                if (component.isLootable() || blacklisted) {
                    boolean override = false;
                    if (chkShowLootable.isSelected() && component.isLootable()) {
                        override = true;
                    }
                    if (chkShowBlacklisted.isSelected() && blacklisted) {
                        override = true;
                    }
                    if (!override) {
                        return false;
                    }
                }
                ComponentType componentType = component.getComponentType();
                CategoryGUIGroup categoryGUIGroup = (CategoryGUIGroup) catGrpChoiceType.getSelectedItem();
                if (categoryGUIGroup == null) {
                    return false;
                }
                if (categoryGUIGroup == CategoryGUIGroup.ALL) {
                    return true;
                }
                CategoryGUIType categoryGUIType = (CategoryGUIType) catTypChoiceType.getSelectedItem();
                if (categoryGUIType == null) {
                    return false;
                }
                Collection<CategoryGUIMapping> mappings;
                if (categoryGUIType == DefaultCategoryGUIType.ALL) {
                    mappings = CategoryGUIMappingsRepository.findByGroup(categoryGUIGroup);
                } else {
                    mappings = Collections.singleton(CategoryGUIMappingsRepository.find(categoryGUIGroup, categoryGUIType));
                }
                if (mappings.isEmpty()) {
                    return false;
                }
                for (CategoryGUIMapping mapping : mappings) {
                    List<ComponentType> componentTypesOverrides = mapping.getComponentTypesOverrides();
                    if (componentTypesOverrides.isEmpty() && !categoryGUIGroup.getBaseComponentTypes().contains(componentType)) {
                        continue;
                    } else if (!componentTypesOverrides.isEmpty() && !componentTypesOverrides.contains(componentType)) {
                        continue;
                    }
                    List<Category> categories = component.getCategories();
                    List<Category> excludeCategories = mapping.getExcludeCategories();
                    if (!Collections.disjoint(categories, excludeCategories)) {
                        continue;
                    }
                    List<Category> includeCategories = mapping.getIncludeCategories();
                    if (!Collections.disjoint(categories, includeCategories)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}