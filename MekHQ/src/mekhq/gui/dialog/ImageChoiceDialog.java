/*
 * Copyright (c) 2009, 2016, 2020 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
package mekhq.gui.dialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import megamek.common.icons.AbstractIcon;
import megamek.common.util.EncodeControl;
import mekhq.MHQStaticDirectoryManager;
import mekhq.MekHQ;
import mekhq.campaign.force.Force;
import mekhq.gui.enums.LayeredForceIconEnum;
import mekhq.gui.preferences.JWindowPreference;
import mekhq.gui.utilities.MekHqTableCellRenderer;
import mekhq.icons.LayeredForceIcon;
import mekhq.preferences.PreferencesNode;

/**
 * @author  Jay Lawson <jaylawson39 at yahoo.com>
 */
/*
public class ImageChoiceDialog extends JDialog {
    //region Variable Declarations
    private static final long serialVersionUID = 7316667282566479439L;

    private static final String PANEL_IMAGES = "panel_images";
    private static final String PANEL_LAYERED = "panel_layered";

    /**
     * The categorized image patterns.
     */
/*    private ImageTableModel imageTableModel = new ImageTableModel();
    private ImageTableMouseAdapter imagesMouseAdapter;

    private JTable tableImages;

    //region Layered Images Support
    private JLabel preview = new JLabel();
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JTabbedPane layerTabs = new JTabbedPane();
    private JPanel layerPanel = new JPanel();

    // Combined array format
    private JTable[] layeredTables;
    //endregion Layered Images Support
    //endregion Variable Declarations

    private void initComponents() {
        ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.ImageChoiceDialog", new EncodeControl());
        GridBagConstraints gbc;

        getContentPane().setLayout(new GridBagLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form");
        setTitle(resourceMap.getString("Force.title"));

        // Background setup for the layered options
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        LayeredForceIconEnum[] layeredForceIconEnums = LayeredForceIconEnum.values();
        layeredTables = new JTable[layeredForceIconEnums.length];

        for (int i = 0; i < layeredForceIconEnums.length; i++) {
            JScrollPane scrollPane = new JScrollPane();
            layeredTables[i] = new JTable();
            ImageTableModel tableModel = new ImageTableModel();
            JPanel panel = new JPanel();

            layeredTables[i].setModel(tableModel);
            layeredTables[i].setName(layeredForceIconEnums[i].getTableName());
            layeredTables[i].setSelectionMode(layeredForceIconEnums[i].getListSelectionModel());
            layeredTables[i].setRowHeight(76);
            layeredTables[i].getColumnModel().getColumn(0).setCellRenderer(tableModel.getRenderer());
            layeredTables[i].addMouseListener(new ImageTableMouseAdapter());
            scrollPane.setViewportView(layeredTables[i]);
            panel.add(scrollPane, gbc);
            tableModel.reset();
            tableModel.setCategory(layeredForceIconEnums[i].getLayerPath());
            tableModel.addImage(AbstractIcon.DEFAULT_ICON_FILENAME);
            Iterator<String> imageIterator = MHQStaticDirectoryManager.getForceIcons()
                    .getItemNames(layeredForceIconEnums[i].getLayerPath());
            while (imageIterator.hasNext()) {
                tableModel.addImage(imageIterator.next());
            }
            layerTabs.addTab(layeredForceIconEnums[i].toString(), panel);

            // Initialize Initial Values, provided the Icon Map is not empty on input or it
            // is the frame (as we set that value otherwise)
            if (!emptyInitialIconMap || (layeredForceIconEnums[i] == LayeredForceIconEnum.FRAME)) {
                if (iconMap.containsKey(layeredForceIconEnums[i].getLayerPath())) {
                    if (layeredForceIconEnums[i].getListSelectionModel() == ListSelectionModel.SINGLE_SELECTION) {
                        // Determine the current selected value
                        String selected = iconMap.get(layeredForceIconEnums[i].getLayerPath()).get(0);
                        for (int k = 0; k < tableModel.getRowCount(); k++) {
                            if (tableModel.getValueAt(k, 0).equals(selected)) {
                                // This adds k as a selected row, with the backend considering it
                                // as selecting the interval between k and k, inclusively
                                layeredTables[i].setRowSelectionInterval(k, k);
                                break;
                            }
                        }
                    } else {
                        Vector<String> mapVector = iconMap.get(layeredForceIconEnums[i].getLayerPath());
                        for (int k = 0; k < tableModel.getRowCount(); k++) {
                            if (mapVector.contains((String) tableModel.getValueAt(k, 0))) {
                                // This adds k as a selected row, with the backend considering it
                                // as selecting the interval between k and k, inclusively
                                layeredTables[i].addRowSelectionInterval(k, k);
                            }
                        }
                    }
                }
            }
        }

        // Put it all together nice and pretty on the layerPanel
        layerPanel.setLayout(new GridBagLayout());
        layerPanel.add(layerTabs, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weighty = 0.0;
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 225));
        preview.setMinimumSize(new Dimension(300, 225));
        layerPanel.add(preview, gbc);
        layerPanel.setName(PANEL_LAYERED);

        // Add single and layered options to the dialog
        tabbedPane.addTab(resourceMap.getString("Force.single"), imagesPanel);
        tabbedPane.addTab(resourceMap.getString("Force.layered"), layerPanel);

        // Set currently selected tab based on the initial category
        if (!emptyInitialIconMap) {
            tabbedPane.setSelectedComponent(layerPanel);
        }

        // Add the tabbed pane to the content pane
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        getContentPane().add(tabbedPane, gbc);

        // Then trigger the initial refresh
        refreshLayeredPreview();

        // And add the missing ListSelectionListeners, which must be done after the initial refresh
        for (JTable table : layeredTables) {
            table.getSelectionModel().addListSelectionListener(event -> refreshLayeredPreview());
        }

        pack();
    }

    private void btnSelectActionPerformed(ActionEvent evt) {
        category = ((null != tabbedPane.getSelectedComponent())
                && PANEL_LAYERED.equals(tabbedPane.getSelectedComponent().getName()))
            ? Force.ROOT_LAYERED : imageTableModel.getCategory();
        if (tableImages.getSelectedRow() != -1) {
            filename = (String) imageTableModel.getValueAt(tableImages.getSelectedRow(), 0);
        } else {
            filename = AbstractIcon.DEFAULT_ICON_FILENAME;
        }
        changed = true;
        setVisible(false);
    }


    private void comboCategoriesItemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            fillTable((String) evt.getItem());
        }
    }

    private void refreshLayeredPreview() {
        // Clear the icon map
        iconMap.clear();

        // Check each table for what is, or is not, selected
        Vector<String> temp;
        LayeredForceIconEnum[] layeredForceIconEnums = LayeredForceIconEnum.values();
        for (int i = 0; i < layeredTables.length; i++) {
            // If we are in the first row, we have the None option selected. Therefore, we need to
            // Ignore the selected index.
            if (layeredTables[i].getSelectedRow() <= 0) {
                iconMap.remove(layeredForceIconEnums[i].getLayerPath());
            } else {
                temp = new Vector<>();
                for (int index : layeredTables[i].getSelectedRows()) {
                    temp.add((String) layeredTables[i].getValueAt(index, 0));
                }
                iconMap.put(layeredForceIconEnums[i].getLayerPath(), temp);
            }
        }

        category = Force.ROOT_LAYERED;
        filename = AbstractIcon.DEFAULT_ICON_FILENAME;

        // Build the layered image
        ImageIcon imageIcon = getForceIcon().getImageIcon();

        // Disable selection of a static icon
        tableImages.clearSelection();

        // Update the preview
        preview.setIcon(imageIcon);
        preview.validate();
    }

    private void fillTable(String category) {
        imageTableModel.reset();
        imageTableModel.setCategory(category);
        // Translate the "root image" category name.
        Iterator<String> imageNames;
        if (AbstractIcon.ROOT_CATEGORY.equals(category)) {
            imageTableModel.addImage(AbstractIcon.DEFAULT_ICON_FILENAME);
            imageNames = MHQStaticDirectoryManager.getForceIcons().getItemNames("");
        } else {
            imageNames = MHQStaticDirectoryManager.getForceIcons().getItemNames(category);
        }

        // Get the image names for this category.
        while (imageNames.hasNext()) {
            imageTableModel.addImage(imageNames.next());
        }
        if (imageTableModel.getRowCount() > 0) {
            tableImages.setRowSelectionInterval(0, 0);
        }
    }

    public class ImageTableModel extends AbstractTableModel {
        private static final long serialVersionUID = -7469653910161174678L;
        private String[] columnNames;
        private String category;
        private List<String> names;

        public ImageTableModel() {
            columnNames = new String[] {"Images"};
            category = AbstractIcon.ROOT_CATEGORY;
            names = new ArrayList<>();
        }

        @Override
        public int getRowCount() {
            return names.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        public void reset() {
            category = AbstractIcon.ROOT_CATEGORY;
            names = new ArrayList<>();
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return names.get(row);
        }

        public void setCategory(String c) {
            category = c;
        }

        public String getCategory() {
            return category;
        }

        public void addImage(String name) {
            names.add(name);
            fireTableDataChanged();
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public ImageTableModel.Renderer getRenderer() {
            return new ImageTableModel.Renderer();
        }

        public class Renderer extends ImagePanel implements TableCellRenderer {
            private static final long serialVersionUID = -6025788865509594987L;

            public Renderer() {
                super();
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                lblImage.setText(getValueAt(row, column).toString());
                lblImage.setIcon(getForceIcon().getImageIcon(getForceIcon().getCategory().startsWith("Pieces") ? 110 : 76));

                MekHqTableCellRenderer.setupTableColors(this, table, isSelected, hasFocus, row);
                return this;
            }
        }
    }

    public class ImageTableMouseAdapter extends MouseInputAdapter {
        @Override
        public void mouseClicked(MouseEvent evt) {
            if ((evt.getButton() == MouseEvent.BUTTON1) && (evt.getClickCount() == 2)) {
                if (tableImages.equals(evt.getSource())) {
                    int row = tableImages.rowAtPoint(evt.getPoint());
                    if (row < imageTableModel.getRowCount()) {
                        category = imageTableModel.getCategory();
                        filename = (String) imageTableModel.getValueAt(row, 0);
                        changed = true;
                        setVisible(false);
                    }
                }
            }
        }
    }

    public static class ImagePanel extends JPanel {
        private static final long serialVersionUID = -3724175393116586310L;
        protected JLabel lblImage;

        public ImagePanel() {
            initComponents();
        }

        private void initComponents() {
            GridBagConstraints gbc;

            lblImage = new JLabel();

            setName("Form");
            setLayout(new GridBagLayout());

            lblImage.setText("");
            lblImage.setName("lblImage");
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            add(lblImage, gbc);
        }
    }
}
*/
