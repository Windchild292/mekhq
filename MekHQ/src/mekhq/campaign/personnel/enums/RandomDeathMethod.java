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
package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.randomDeath.AbstractRandomDeathMethod;
import mekhq.campaign.personnel.randomDeath.SixthOrderDifferentialRandomDeath;

import java.util.ResourceBundle;

public enum RandomDeathMethod {
    //region Enum Declarations
    /**
     * This disables Random Deaths
     */
    NONE("RandomDeathType.NONE.text", "RandomDeathType.NONE.toolTipText"),
    /**
     * This is the standard type for Random Deaths, which uses a sixth order differential equation
     * to determine the chance of random deaths
     */
    STANDARD("RandomDeathType.STANDARD.text", "RandomDeathType.STANDARD.toolTipText");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final String toolTip;
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    RandomDeathMethod(final String name, final String toolTip) {
        this.name = resources.getString(name);
        this.toolTip = resources.getString(toolTip);
    }
    //endregion Constructors

    //region Getters
    public String getToolTip() {
        return toolTip;
    }
    //endregion Getters

    //region Boolean Comparison Methods
    public boolean isEnabled() {
        return this != NONE;
    }
    //endregion Boolean Comparison Methods

    public AbstractRandomDeathMethod getMethod(final Campaign campaign) {
        switch (this) {
            case STANDARD:
                return new SixthOrderDifferentialRandomDeath(campaign);
            case NONE:
            default:
                MekHQ.getLogger().error("RandomDeath: Error: Attempted to get the method while disabled."
                        + "Returning the standard sixth order method, and please report this on our GitHub");
                return new SixthOrderDifferentialRandomDeath(campaign);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
