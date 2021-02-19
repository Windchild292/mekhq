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
import mekhq.gui.view.ContractMarketPanel;

import javax.swing.*;
import java.awt.*;

public class ContractMarketDialog extends BaseButtonDialog {
    //region Variable Declarations
    private Campaign campaign;
    private ContractMarketPanel contractMarketPanel;
    //endregion Variable Declarations

    //region Constructors
    public ContractMarketDialog(final JFrame frame, final Campaign campaign) {
        super(frame, "ContractMarketDialog.title");
        initialize("ContractMarketDialog");
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(final Campaign campaign) {
        this.campaign = campaign;
    }

    public ContractMarketPanel getContractMarketPanel() {
        return contractMarketPanel;
    }

    public void setContractMarketPanel(final ContractMarketPanel contractMarketPanel) {
        this.contractMarketPanel = contractMarketPanel;
    }
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected Container createCenterPane() {
        setContractMarketPanel(new ContractMarketPanel(getFrame(), getCampaign()));
        return getContractMarketPanel();
    }

    @Override
    protected JPanel createButtonPanel() {
        return null;
    }
    //endregion Initialization
}
