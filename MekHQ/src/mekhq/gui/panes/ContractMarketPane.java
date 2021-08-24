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
package mekhq.gui.panes;

import megamek.client.ui.baseComponents.MMComboBox;
import megamek.common.Entity;
import megamek.common.icons.Camouflage;
import mekhq.campaign.Campaign;
import mekhq.campaign.market.unitMarket.UnitMarketOffer;
import mekhq.gui.baseComponents.AbstractMHQSplitPane;
import mekhq.gui.displayWrappers.FactionDisplay;
import mekhq.gui.model.UnitMarketTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ContractMarketPane extends AbstractMHQSplitPane {
    //region Variable Declarations
    private final Campaign campaign;

    //region Left Panel
    // Settings
    private JCheckBox chkMRBCFee;
    private JSpinner spnAdvancePercentage;
    private JSpinner spnSigningBonusPercentage;
    private MMComboBox<FactionDisplay> comboRetainers;

    // Contract Table
    private JTable marketTable;
    private UnitMarketTableModel marketModel;
    //endregion Left Panel

    //region Right Panel
    //endregion Right Panel
    //endregion Variable Declarations

    //region Constructors
    public ContractMarketPane(final JFrame frame, final Campaign campaign) {
        super(frame, "ContractMarketPane");
        this.campaign = campaign;
        initialize();
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    //region Left Panel
    //endregion Left Panel

    //region Right Panel
    //endregion Right Panel
    //endregion Getters/Setters


    //region Initialization
    @Override
    protected Component createLeftComponent() {
        return null;
    }

    @Override
    protected Component createRightComponent() {
        return null;
    }
    //endregion Initialization

    //region Button Actions
    public void acceptSelectedContracts() {

    }

    public void customizeSelectedContract() {

    }

    public void removeSelectedContracts() {
        final List<UnitMarketOffer> offers = getSelectedOffers();
        if (offers.isEmpty()) {
            return;
        }
        getCampaign().getUnitMarket().getOffers().removeAll(offers);
        getMarketModel().setData(getCampaign().getUnitMarket().getOffers());
    }
    //endregion Button Actions

    private void updateDisplay() {
        final Entity entity = getSelectedEntity();
        getEntityViewPane().updateDisplayedEntity(entity);
        getEntityImagePanel().updateDisplayedEntity(entity,
                (entity == null) ? new Camouflage() : entity.getCamouflageOrElse(getCampaign().getCamouflage()));
    }
}
