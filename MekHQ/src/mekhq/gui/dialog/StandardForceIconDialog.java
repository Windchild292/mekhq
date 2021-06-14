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
package mekhq.gui.dialog;

import megamek.client.ui.dialogs.AbstractIconChooserDialog;
import megamek.common.annotations.Nullable;
import megamek.common.icons.AbstractIcon;
import megamek.common.util.EncodeControl;

import javax.swing.*;
import java.util.ResourceBundle;

public class StandardForceIconDialog extends AbstractIconChooserDialog {
    private static final long serialVersionUID = 2690083417720266231L;

    public StandardForceIconDialog(final JFrame parent, final @Nullable AbstractIcon icon) {
        super(parent, ResourceBundle.getBundle("mekhq.resources.GUI", new EncodeControl())
                .getString("StandardForceIconChooserDialog.title"), new mekhq.gui.dialog.icons.StandardForceIconChooser(icon));
    }

    //region Getters
    @Override
    protected StandardForceIconChooser getChooser() {
        return (StandardForceIconChooser) super.getChooser();
    }
    //endregion Getters
}