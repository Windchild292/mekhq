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
package mekhq.gui.view;

import mekhq.campaign.Campaign;
import mekhq.campaign.unit.Unit;

import javax.swing.*;
import java.util.ResourceBundle;

public abstract class AbstractUnitInformationPanel extends ScrollablePanel {
    //region Variable Declarations
    protected Campaign campaign;
    protected Unit unit;

    protected static final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.UnitInformationPanel");
    //endregion Variable Declarations

    //region Constructors
    protected AbstractUnitInformationPanel(String panelName, Campaign campaign, Unit unit) {
        this.campaign = (campaign != null) ? campaign : new Campaign();
        this.unit = (unit != null) ? unit : new Unit(null, getCampaign());

        setName(panelName);
        initializeComponents();
    }
    //endregion Constructors

    //region Initialization
    private void initializeComponents() {
        // Layout the UI
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(labelDisplay)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelStartGameDelay)
                                .addComponent(optionStartGameDelay, GroupLayout.Alignment.TRAILING))
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(labelDisplay)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(labelStartGameDelay)
                                .addComponent(optionStartGameDelay))
        );
    }
    //endregion Initialization

    //region Getters/Setters
    protected Campaign getCampaign() {
        return campaign;
    }

    protected void getCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    protected Unit getUnit() {
        return unit;
    }

    protected void getUnit(Unit unit) {
        this.unit = unit;
    }
    //endregion Getters/Setters
}
