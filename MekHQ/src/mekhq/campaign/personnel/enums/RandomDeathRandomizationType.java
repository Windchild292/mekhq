/*
 * Copyright (c) 2020 - The MegaMek Team. All rights reserved.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.personnel.generator.AbstractRandomDeathGenerator;
import mekhq.campaign.personnel.generator.SixthOrderDifferentialRandomDeathGenerator;

import java.util.ResourceBundle;

public enum RandomDeathRandomizationType {
    //region Enum Declarations
    /**
     * This is the standard type for Random Deaths, which uses a sixth order differential equation
     * to determine the chance of random deaths
     */
    STANDARD("RandomDeathRandomizationType.STANDARD.text",
            "RandomDeathRandomizationType.STANDARD.toolTipText"),
     /**
     * This type uses weights based on the current era to determine the formula to calculate
     * the chance of random deaths occurring
     */
    ERA_WEIGHTED("RandomDeathRandomizationType.ERA_WEIGHTED.text",
             "RandomDeathRandomizationType.ERA_WEIGHTED.toolTipText"),
    /**
     * This uses weights based on the selected faction to determine the formula to calculate the chance
     * of random deaths occurring
     */
    FACTION_WEIGHTED("RandomDeathRandomizationType.FACTION_WEIGHTED.text",
            "RandomDeathRandomizationType.FACTION_WEIGHTED.toolTipText"),
    /**
     * This uses weightings based on both the faction and era to determine to formula to calculate the
     * change of random deaths occurring
     */
    ERA_FACTION_WEIGHTED("RandomDeathRandomizationType.ERA_FACTION_WEIGHTED.text",
            "RandomDeathRandomizationType.ERA_FACTION_WEIGHTED.toolTipText"),
    /**
     * This uses a standard weighted grouping to determine the randomization for random deaths
     */
    GENERAL_WEIGHTED("RandomDeathRandomizationType.GENERAL_WEIGHTED.text",
            "RandomDeathRandomizationType.GENERAL_WEIGHTED.toolTipText");
    //endregion Enum Declarations

    //region Variable Declarations
    private String name;
    private String toolTip;
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel",
            new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    RandomDeathRandomizationType(String name, String toolTip) {
        this.name = resources.getString(name);
        this.toolTip = resources.getString(toolTip);
    }
    //endregion Constructors

    @Override
    public String toString() {
        return name;
    }

    public String getToolTip() {
        return toolTip;
    }

    public AbstractRandomDeathGenerator getGenerator(CampaignOptions campaignOptions) {
        switch (this) {
            case ERA_WEIGHTED:
            case FACTION_WEIGHTED:
            case ERA_FACTION_WEIGHTED:
            case GENERAL_WEIGHTED:
                MekHQ.getLogger().warning(getClass(), "processNewDayPersonnel",
                        "RandomDeath: Util Type " + toString()
                                + "is not currently supported. Using the standard util type instead.");
            case STANDARD:
            default:
                return new SixthOrderDifferentialRandomDeathGenerator(campaignOptions);
        }
    }
}
