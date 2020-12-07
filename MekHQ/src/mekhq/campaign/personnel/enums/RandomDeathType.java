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
package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.randomDeath.AbstractRandomDeathMethod;
import mekhq.campaign.personnel.randomDeath.SixthOrderDifferentialRandomDeath;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public enum RandomDeathType {
    //region Enum Declarations
    /**
     * This disables Random Deaths
     */
    NONE("RandomDeathType.NONE.text", "RandomDeathType.NONE.toolTipText"),
    /**
     * This is the standard type for Random Deaths, which uses a sixth order differential equation
     * to determine the chance of random deaths
     */
    STANDARD("RandomDeathType.STANDARD.text", "RandomDeathType.STANDARD.toolTipText"),
    /**
     * This type uses weights based on the current era to determine the formula to calculate
     * the chance of random deaths occurring
     */
    ERA_WEIGHTED("RandomDeathType.ERA_WEIGHTED.text", "RandomDeathType.ERA_WEIGHTED.toolTipText", false),
    /**
     * This uses weights based on the selected faction to determine the formula to calculate the chance
     * of random deaths occurring
     */
    FACTION_WEIGHTED("RandomDeathType.FACTION_WEIGHTED.text", "RandomDeathType.FACTION_WEIGHTED.toolTipText", false),
    /**
     * This uses weightings based on both the faction and era to determine to formula to calculate the
     * change of random deaths occurring
     */
    ERA_FACTION_WEIGHTED("RandomDeathType.ERA_FACTION_WEIGHTED.text", "RandomDeathType.ERA_FACTION_WEIGHTED.toolTipText", false),
    /**
     * This uses a standard weighted grouping to determine the randomization for random deaths
     */
    GENERAL_WEIGHTED("RandomDeathType.GENERAL_WEIGHTED.text", "RandomDeathType.GENERAL_WEIGHTED.toolTipText", false);
    //endregion Enum Declarations

    //region Variable Declarations
    private String name;
    private String toolTip;
    private boolean implemented;
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    RandomDeathType(String name, String toolTip) {
        this(name, toolTip, true);
    }

    RandomDeathType(String name, String toolTip, boolean implemented) {
        this.name = resources.getString(name);
        this.toolTip = resources.getString(toolTip);
        this.implemented = implemented;
    }
    //endregion Constructors

    //region Getters
    public String getToolTip() {
        return toolTip;
    }

    public boolean isImplemented() {
        return implemented;
    }
    //endregion Getters

    //region Boolean Comparison Methods
    public boolean isNone() {
        return this == NONE;
    }
    //endregion Boolean Comparison Methods

    public AbstractRandomDeathMethod getMethod(Campaign campaign) {
        switch (this) {
            case ERA_WEIGHTED:
            case FACTION_WEIGHTED:
            case ERA_FACTION_WEIGHTED:
            case GENERAL_WEIGHTED:
                MekHQ.getLogger().warning("RandomDeath: Method " + toString()
                        + "is not currently supported. Using the standard sixth order method instead.");
            case STANDARD:
                return new SixthOrderDifferentialRandomDeath(campaign);
            case NONE:
            default:
                MekHQ.getLogger().warning("RandomDeath: Error: Attempted to get the method while disabled."
                        + "Returning the standard sixth order method, and please report this on our GitHub");
                return new SixthOrderDifferentialRandomDeath(campaign);
        }
    }

    public static List<RandomDeathType> getImplementedValues() {
        List<RandomDeathType> implementedTypes = new ArrayList<>();
        for (RandomDeathType type : values()) {
            if (type.isImplemented()) {
                implementedTypes.add(type);
            }
        }
        return implementedTypes;
    }

    @Override
    public String toString() {
        return name;
    }
}
