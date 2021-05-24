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
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.personnel.randomDeath.AbstractRandomDeath;
import mekhq.campaign.personnel.randomDeath.AgeRangeRandomDeath;
import mekhq.campaign.personnel.randomDeath.DisabledRandomDeath;
import mekhq.campaign.personnel.randomDeath.ExponentialRandomDeath;

import java.util.ResourceBundle;

public enum RandomDeathMethod {
    //region Enum Declarations
    NONE("RandomDeathMethod.NONE.text", "RandomDeathMethod.NONE.toolTipText"),
    EXPONENTIAL("RandomDeathMethod.EXPONENTIAL.text", "RandomDeathMethod.EXPONENTIAL.toolTipText"),
    AGE_RANGE("RandomDeathMethod.AGE_RANGE.text", "RandomDeathMethod.AGE_RANGE.toolTipText");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final String toolTipText;
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    RandomDeathMethod(final String name, final String toolTipText) {
        this.name = resources.getString(name);
        this.toolTipText = resources.getString(toolTipText);
    }
    //endregion Constructors

    //region Getters
    public String getToolTipText() {
        return toolTipText;
    }
    //endregion Getters

    //region Boolean Comparison Methods
    public boolean isNone() {
        return this == NONE;
    }

    public boolean isExponential() {
        return this == EXPONENTIAL;
    }

    public boolean isAgeRange() {
        return this == AGE_RANGE;
    }
    //endregion Boolean Comparison Methods

    public AbstractRandomDeath getMethod(final CampaignOptions campaignOptions) {
        switch (this) {
            case EXPONENTIAL:
                return new ExponentialRandomDeath(campaignOptions);
            case AGE_RANGE:
                return new AgeRangeRandomDeath(campaignOptions);
            case NONE:
            default:
                return new DisabledRandomDeath();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
