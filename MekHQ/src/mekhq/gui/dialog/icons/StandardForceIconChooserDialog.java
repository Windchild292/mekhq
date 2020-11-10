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
package mekhq.gui.dialog.icons;

import megamek.client.ui.swing.dialog.imageChooser.AbstractIconChooserDialog;
import megamek.client.ui.swing.dialog.imageChooser.PortraitChooser;
import megamek.common.icons.AbstractIcon;
import megamek.common.util.EncodeControl;

import java.awt.*;
import java.util.ResourceBundle;

public class StandardForceIconChooserDialog extends AbstractIconChooserDialog {
    private static final long serialVersionUID = 2690083417720266231L;

    /** Creates a dialog that allows players to choose a StandardForceIcon. */
    public StandardForceIconChooserDialog(Window parent, AbstractIcon icon) {
        super(parent, ResourceBundle.getBundle("mekhq.resources.GUIDialogs", new EncodeControl())
                        .getString("StandardForceIconChooserDialog.title"), new StandardForceIconChooser(icon));
    }

    //region Getters
    @Override
    protected StandardForceIconChooser getChooser() {
        return (StandardForceIconChooser) super.getChooser();
    }
    //endregion Getters
}
