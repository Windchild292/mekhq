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

import megamek.common.annotations.Nullable;
import megamek.common.icons.AbstractIcon;
import mekhq.MHQStaticDirectoryManager;
import mekhq.MekHQ;

import java.awt.*;

public class StandardForceIcon extends AbstractIcon {
    //region Variable Declarations
    private static final long serialVersionUID = -9134346451335731650L;
    public static final String DEFAULT_FORCE_ICON_FILENAME = "empty.png";
    //endregion Variable Declarations

    //region Constructors
    public StandardForceIcon(String category, String filename) {
        super(category, filename);
    }
    //endregion Constructors

    //region Getters/Setters
    @Override
    public void setFilename(@Nullable String filename) {
        // We allow filename to be null here as part of the UnitIcon Code
        this.filename = filename;
    }
    //endregion Getters/Setters

    //region Boolean Methods
    @Override
    public boolean hasDefaultFilename() {
        return super.hasDefaultFilename() || DEFAULT_FORCE_ICON_FILENAME.equals(getFilename());
    }
    //endregion Boolean Methods

    @Override
    public Image getImage(int width, int height) {
        if (getFilename() == null) {
            return null;
        }

        Image image = getBaseImage();

        return (image == null) ? null : super.getImage(image, width, height);
    }

    @Override
    public Image getBaseImage() {
        // If we can't create the force icon directory, return null
        if (MHQStaticDirectoryManager.getForceIcons() == null) {
            return null;
        }

        String category = hasDefaultCategory() ? "" : getCategory();
        String filename = hasDefaultFilename() ? DEFAULT_FORCE_ICON_FILENAME : getFilename();

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
}
