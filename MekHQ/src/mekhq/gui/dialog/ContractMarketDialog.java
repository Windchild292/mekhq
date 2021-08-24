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

import megamek.client.ui.baseComponents.MMButton;
import mekhq.campaign.Campaign;
import mekhq.gui.baseComponents.AbstractMHQValidationButtonDialog;
import mekhq.gui.panes.ContractMarketPane;

import javax.swing.*;
import java.awt.*;

public class ContractMarketDialog extends AbstractMHQValidationButtonDialog {
    //region Variable Declarations
    private final Campaign campaign;

    private ContractMarketPane contractMarketPane;

    // Buttons
    private JButton btnAccept;
    private JButton btnCustomize;
    private JButton btnRemove;
    //endregion Variable Declarations

    //region Constructors
    public ContractMarketDialog(final JFrame frame, final Campaign campaign) {
        super(frame, "ContractMarketDialog", "ContractMarketDialog.title");
        this.campaign = campaign;
        initialize();
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public ContractMarketPane getContractMarketPane() {
        return contractMarketPane;
    }

    public void setContractMarketPane(final ContractMarketPane contractMarketPane) {
        this.contractMarketPane = contractMarketPane;
    }

    //region Buttons
    public JButton getBtnCustomize() {
        return btnCustomize;
    }

    public void setBtnCustomize(final JButton btnCustomize) {
        this.btnCustomize = btnCustomize;
    }

    public JButton getBtnAccept() {
        return btnAccept;
    }

    public void setBtnAccept(final JButton btnAccept) {
        this.btnAccept = btnAccept;
    }

    public JButton getBtnRemove() {
        return btnRemove;
    }

    public void setBtnRemove(final JButton btnRemove) {
        this.btnRemove = btnRemove;
    }
    //endregion Buttons
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected Container createCenterPane() {
        setContractMarketPane(new ContractMarketPane(getFrame(), getCampaign()));
        getContractMarketPane().getContractTable().getSelectionModel().addListSelectionListener(evt -> refreshView());
        return getContractMarketPane();
    }

    @Override
    protected JPanel createButtonPanel() {
        final JPanel panel = new JPanel(new GridLayout(1, getCampaign().isGM() ? 4 : 2));
        setBtnAccept(new MMButton("btnAccept", resources.getString("Accept.text"),
                resources.getString("ContractMarketDialog.btnAccept.toolTipText"), evt -> okAction()));
        panel.add(getBtnAccept());

        if (getCampaign().isGM()) {
            setBtnCustomize(new MMButton("btnCustomize", resources.getString("Customize.text"),
                    resources.getString("ContractMarketDialog.btnCustomize.toolTipText"),
                    evt -> getContractMarketPane().customizeSelectedContract()));
            panel.add(getBtnCustomize());

            setBtnRemove(new MMButton("btnRemove", resources.getString("Remove.text"),
                    resources.getString("Remove.toolTipText"), evt -> getContractMarketPane().removeSelectedContracts()));
            panel.add(getBtnRemove());
        }

        panel.add(new MMButton("btnClose", resources.getString("Close.text"),
                resources.getString("Close.toolTipText"), this::cancelActionPerformed));
        return panel;
    }

    @Override
    protected void finalizeInitialization() {
        super.finalizeInitialization();
        refreshView();
    }
    //endregion Initialization

    @Override
    protected void okAction() {
        getContractMarketPane().acceptSelectedContracts();
    }

    private void refreshView() {
        final boolean enabled = getContractMarketPane().getSelectedContract() != null;
        getBtnAccept().setEnabled(enabled);

        if (getBtnAccept() != null) {
            getBtnCustomize().setEnabled(enabled);
        }

        if (getBtnRemove() != null) {
            getBtnRemove().setEnabled(enabled);
        }
    }
}
