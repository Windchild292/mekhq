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
package mekhq.icons;

import megamek.common.icons.AbstractIcon;
import mekhq.MekHQ;
import mekhq.campaign.force.Force;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Vector;

public class LayeredForceIcon extends StandardForceIcon {
    //region Variable Declarations
    private static final long serialVersionUID = -2366003293807482568L;
    //endregion Variable Declarations

    @Override
    public Image getBaseImage() {

    }

    public static Image buildForceIcon(String category, String filename,
                                       LinkedHashMap<String, Vector<String>> iconMap) {
        Image retVal = null;

        if (AbstractIcon.ROOT_CATEGORY.equals(category)) {
            category = "";
        }

        // Return a null if the player has selected no force icon file.
        if ((null == category) || (null == filename)
                || (AbstractIcon.DEFAULT_ICON_FILENAME.equals(filename) && !Force.ROOT_LAYERED.equals(category))) {
            filename = "empty.png";
        }

        // Layered force icon
        if (Force.ROOT_LAYERED.equals(category)) {
            GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage base = null;
            Graphics2D g2d = null;
            try {
                int width = 0;
                int height = 0;
                // Gather height/width
                for (mekhq.gui.enums.LayeredForceIcon layeredForceIcon : mekhq.gui.enums.LayeredForceIcon.getInDrawOrder()) {
                    String layer = layeredForceIcon.getLayerPath();
                    if (iconMap.containsKey(layer)) {
                        for (String value : iconMap.get(layer)) {
                            // Load up the image piece
                            BufferedImage img = (BufferedImage) getForceIcons().getItem(layer, value);
                            width = Math.max(img.getWidth(), width);
                            height = Math.max(img.getHeight(), height);
                        }
                    }
                }
                base = config.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
                g2d = base.createGraphics();
                for (mekhq.gui.enums.LayeredForceIcon layeredForceIcon : mekhq.gui.enums.LayeredForceIcon.getInDrawOrder()) {
                    String layer = layeredForceIcon.getLayerPath();
                    if (iconMap.containsKey(layer)) {
                        for (String value : iconMap.get(layer)) {
                            BufferedImage img = (BufferedImage) getForceIcons().getItem(layer, value);
                            // Draw the current buffered image onto the base, aligning bottom and right side
                            g2d.drawImage(img, width - img.getWidth() + 1, height - img.getHeight() + 1, null);
                        }
                    }
                }
            } catch (Exception e) {
                MekHQ.getLogger().error(e);
            } finally {
                if (null != g2d) {
                    g2d.dispose();
                }
                if (null == base) {
                    try {
                        base = (BufferedImage) getForceIcons().getItem("", "empty.png");
                    } catch (Exception e) {
                        MekHQ.getLogger().error(e);
                    }
                }
                retVal = base;
            }
        } else {
            // Write as an error, then return the standard force icon

            return super.getBaseImage();
        }

        return retVal;
    }
}

/*

# LayeredForceIcon Enum
LayeredForceIcon.types=Types
LayeredForceIcon.formations=Formations
LayeredForceIcon.adjustments=Adjustments
LayeredForceIcon.alphanumerics=Alphanumerics
LayeredForceIcon.special=Special
LayeredForceIcon.backgrounds=Backgrounds
LayeredForceIcon.frame=Frames
LayeredForceIcon.logos=Logos


 */
