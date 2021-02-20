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
package mekhq.gui.dialog;

import mekhq.campaign.Campaign;
import mekhq.gui.baseComponents.AbstractButtonDialog;
import mekhq.gui.view.UnitMarketPane;

import javax.swing.*;
import java.awt.*;

public class UnitMarketDialog extends AbstractButtonDialog {
    //region Variable Declarations
    private Campaign campaign;
    private UnitMarketPane unitMarketPane;
    //endregion Variable Declarations

    //region Constructors
    public UnitMarketDialog(final JFrame frame, final Campaign campaign) {
        super(frame, "UnitMarketDialog", "UnitMarketDialog.title");
        setCampaign(campaign);
        initialize();
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(final Campaign campaign) {
        this.campaign = campaign;
    }

    public UnitMarketPane getUnitMarketPane() {
        return unitMarketPane;
    }

    public void setUnitMarketPane(UnitMarketPane unitMarketPane) {
        this.unitMarketPane = unitMarketPane;
    }
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected Container createCenterPane() {
        setUnitMarketPane(new UnitMarketPane(getFrame(), getCampaign()));
        return getUnitMarketPane();
    }

    @Override
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, getCampaign().isGM() ? 3 : 2));
        panel.add(createOkButton("Purchase"));
        if (getCampaign().isGM()) {
            JButton addButton = new JButton(resources.getString("AddGM"));
            addButton.setName("addButton");
            addButton.addActionListener(evt -> {
                ???
            });
            panel.add(addButton);
        }
        panel.add(createCancelButton("Close"));
        return panel;
    }
    //endregion Initialization

    @Override
    protected void okAction() {
        ???
    }

    @Override
    protected void cancelAction() {
        ???
    }
}
