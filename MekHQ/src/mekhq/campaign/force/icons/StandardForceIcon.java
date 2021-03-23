/*
 * Copyright (c) 2020-2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.force.icons;

import megamek.common.annotations.Nullable;
import megamek.common.icons.AbstractIcon;
import mekhq.MHQStaticDirectoryManager;
import mekhq.MekHQ;
import org.w3c.dom.Node;

import java.awt.*;
import java.io.PrintWriter;

public class StandardForceIcon extends AbstractIcon {
    //region Variable Declarations
    private static final long serialVersionUID = -9134346451335731650L;
    public static final String DEFAULT_FORCE_ICON_FILENAME = "empty.png";
    public static final String XML_TAG = "standardForceIcon";
    //endregion Variable Declarations

    //region Constructors
    public StandardForceIcon() {
        this(null, null);
    }

    public StandardForceIcon(final @Nullable String category, final @Nullable String filename) {
        super(category, filename);
    }
    //endregion Constructors

    //region Getters/Setters
    @Override
    public void setFilename(final @Nullable String filename) {
        // We allow filename to be null here as part of the UnitIcon Code
        this.filename = filename;
    }
    //endregion Getters/Setters

    //region Boolean Methods
    @Override
    public boolean hasDefaultCategory() {
        return (getCategory() == null) || super.hasDefaultCategory();
    }

    @Override
    public boolean hasDefaultFilename() {
        return (getFilename() == null) || super.hasDefaultFilename() || DEFAULT_FORCE_ICON_FILENAME.equals(getFilename());
    }
    //endregion Boolean Methods

    @Override
    public @Nullable Image getImage(final int width, final int height) {
        if (getFilename() == null) {
            return null;
        }

        final Image image = getBaseImage();

        return (image == null) ? null : super.getImage(image, width, height);
    }

    @Override
    public @Nullable Image getBaseImage() {
        // If we can't create the force icon directory, return null
        if (MHQStaticDirectoryManager.getForceIcons() == null) {
            return null;
        }

        final String category = hasDefaultCategory() ? "" : getCategory();
        final String filename = hasDefaultFilename() ? DEFAULT_FORCE_ICON_FILENAME : getFilename();

        // Try to get the player's force icon file.
        Image forceIcon = null;
        try {
            forceIcon = (Image) MHQStaticDirectoryManager.getForceIcons().getItem(category, filename);
            if (forceIcon == null) {
                forceIcon = (Image) MHQStaticDirectoryManager.getForceIcons().getItem("",
                        DEFAULT_FORCE_ICON_FILENAME);
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
        }

        return forceIcon;
    }

    //region File IO
    @Override
    public void writeToXML(final PrintWriter pw, final int indent) {
        writeToXML(pw, indent, XML_TAG);
    }

    public static StandardForceIcon parseFromXML(final Node wn) {
        final StandardForceIcon icon = new StandardForceIcon();
        try {
            icon.parseNodes(wn.getChildNodes());
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
            return new StandardForceIcon();
        }
        return icon;
    }
    //endregion File IO

    @Override
    public StandardForceIcon clone() {
        return new StandardForceIcon(getCategory(), getFilename());
    }
}
