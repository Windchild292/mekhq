/*
 * KillEntryDialog.java
 *
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

import mekhq.campaign.Kill;
import mekhq.campaign.mission.Scenario;
import mekhq.gui.baseComponents.AbstractMHQButtonDialog;

import javax.swing.*;
import java.awt.*;

public class KillEntryDialog extends AbstractMHQButtonDialog {
    //region Getters/Setters
    private final Kill kill;
    private Scenario scenario;

    private JTextField txtKilledUnitName;
    private JTextField txtKillingUnitName;
    //endregion Getters/Setters

    //region Constructors
    protected KillEntryDialog(final JFrame frame, final Kill kill) {
        super(frame, "KillEntryDialog", "KillEntryDialog.title");
        this.kill = kill;
        setScenario(kill.getScenario());
        initialize();
    }
    //endregion Constructors

    //region Getters/Setters
    public Kill getKill() {
        return kill;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(final Scenario scenario) {
        this.scenario = scenario;
    }

    public JTextField getTxtKilledUnitName() {
        return txtKilledUnitName;
    }

    public void setTxtKilledUnitName(final JTextField txtKilledUnitName) {
        this.txtKilledUnitName = txtKilledUnitName;
    }

    public JTextField getTxtKillingUnitName() {
        return txtKillingUnitName;
    }

    public void setTxtKillingUnitName(final JTextField txtKillingUnitName) {
        this.txtKillingUnitName = txtKillingUnitName;
    }
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected Container createCenterPane() {
        return null;
    }
    //endregion Initialization

    //region Button Actions
    @Override
    protected void okAction() {
        getKill().setScenario(getScenario());
        getKill().setKilledUnitName(getTxtKilledUnitName().getText());
        getKill().setKillingUnitName(getTxtKillingUnitName().getText());
    }
    //endregion Button Actions
}
