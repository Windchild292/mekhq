/*
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui;

import megamek.common.Crew;
import megamek.common.annotations.Nullable;
import megamek.common.util.DirectoryItems;
import megamek.common.util.StringUtil;
import mekhq.MekHQ;
import mekhq.campaign.personnel.Person;

import javax.swing.*;
import java.awt.*;

public class PortraitPanel extends JPanel {
    public static final String ROOT_PORTRAIT_CATEGORY = "";
    public static final String DEFAULT_PORTRAIT_FILENAME = "default.gif";

    /**
     * @return the PortraitPanel for the given filename and category
     */
    public static JPanel createPortraitPanel(String category, String filename, int width, DirectoryItems portraits) {
        JPanel portraitPanel = new JPanel();
        // We are creating it this way to permit the addition of ribbons
        portraitPanel.setName("portraitPanel");
        portraitPanel.setLayout(new GridBagLayout());

        JLabel portraitLabel = new JLabel();
        portraitLabel.setName("portraitLabel");

        Icon portrait = getPortraitIcon(category, filename, width, portraits);
        if (portrait != null) {
            portraitLabel.setIcon(portrait);
        } else {
            MekHQ.getLogger().error(PortraitPanel.class, "createPortraitPanel",
                    "Cannot locate the portrait file in file " + filename + ", category " + category);
        }

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        portraitPanel.add(portraitLabel, gridBagConstraints);

        return portraitPanel;
    }

    /**
     * @param category the intended portrait category
     * @param filename the intended file name
     * @param width the width of the image icon
     * @param portraits the portraits directory item
     * @return an ImageIcon if one is found, otherwise null
     */
    public static @Nullable Icon getPortraitIcon(String category, String filename, int width,
                                                           DirectoryItems portraits) {
        if (Crew.ROOT_PORTRAIT.equals(category)) {
            category = ROOT_PORTRAIT_CATEGORY;
        }

        // Return the default file if PORTRAIT_NONE is selected
        if ((filename == null) || Crew.PORTRAIT_NONE.equals(filename)) {
            category = null;
        }

        // Try to get the player's portrait file.
        Image portrait = null;
        try {
            // First, we check based on the input parameters
            if (category != null) {
                portrait = (Image) portraits.getItem(category, filename);
            }

            // If we don't find a portrait (or were told to skip the above), we try to grab the default file
            if (portrait == null) {
                portrait = (Image) portraits.getItem(ROOT_PORTRAIT_CATEGORY, DEFAULT_PORTRAIT_FILENAME);
            }

            // If we grab a file, resize it based on the inputs
            if (portrait != null) {
                portrait = portrait.getScaledInstance(width, -1, Image.SCALE_DEFAULT);
                return new ImageIcon(portrait);
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(PortraitPanel.class, "createPortraitPanel", e);
        }

        // Can't find a portrait, so we return null
        return null;
    }
}
