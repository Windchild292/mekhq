/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.view;

import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.gui.preferences.JIntNumberSpinnerPreference;
import mekhq.gui.preferences.JTablePreference;
import mekhq.gui.preferences.JToggleButtonPreference;
import mekhq.preferences.PreferencesNode;

import javax.swing.*;

public class UnitMarketPanel extends JPanel {
    //region Variable Declarations
    private Campaign campaign;

    // Filters
    private JCheckBox chkShowMechs;
    private JCheckBox chkShowVehicles;
    private JCheckBox chkShowAerospace;
    //endregion Variable Declarations

    //region Constructors
    public UnitMarketPanel(final JFrame frame, final Campaign campaign) {
        setCampaign(campaign);
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(final Campaign campaign) {
        this.campaign = campaign;
    }

    //region Filters
    public JCheckBox getChkShowMechs() {
        return chkShowMechs;
    }

    public void setChkShowMechs(JCheckBox chkShowMechs) {
        this.chkShowMechs = chkShowMechs;
    }

    public JCheckBox getChkShowVehicles() {
        return chkShowVehicles;
    }

    public void setChkShowVehicles(JCheckBox chkShowVehicles) {
        this.chkShowVehicles = chkShowVehicles;
    }

    public JCheckBox getChkShowAerospace() {
        return chkShowAerospace;
    }

    public void setChkShowAerospace(JCheckBox chkShowAerospace) {
        this.chkShowAerospace = chkShowAerospace;
    }
    //endregion Filters
    //endregion Getters/Setters

    //region Initialization
    private void initialize() {
        setPreferences();
    }

    private void setPreferences() {
        final PreferencesNode preferences = MekHQ.getPreferences().forClass(getClass());

        preferences.manage(new JToggleButtonPreference(getChkShowMechs()));
        preferences.manage(new JToggleButtonPreference(getChkShowVehicles()));
        preferences.manage(new JToggleButtonPreference(getChkShowAerospace()));


        preferences.manage(new JToggleButtonPreference(chkPctThreshold));

        preferences.manage(new JIntNumberSpinnerPreference(spnThreshold));

        preferences.manage(new JTablePreference(tableUnits));
    }
    //endregion Initialization
}
