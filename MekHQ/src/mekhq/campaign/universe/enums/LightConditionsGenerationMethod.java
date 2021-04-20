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
package mekhq.campaign.universe.enums;

import megamek.common.util.EncodeControl;
import mekhq.campaign.universe.generators.lightConditionsGenerator.AbstractLightConditionsGenerator;
import mekhq.campaign.universe.generators.lightConditionsGenerator.AtBLightConditionsGenerator;
import mekhq.campaign.universe.generators.lightConditionsGenerator.DisabledLightConditionsGenerator;

import java.util.ResourceBundle;

public enum LightConditionsGenerationMethod {
    //region Enum Declarations
    NONE("LightConditionsGenerationMethod.NONE.text", "LightConditionsGenerationMethod.NONE.toolTipText"),
    ATB("LightConditionsGenerationMethod.ATB.text", "LightConditionsGenerationMethod.ATB.toolTipText");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final String toolTipText;

    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Universe", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    LightConditionsGenerationMethod(final String name, final String toolTipText) {
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

    public boolean isAtB() {
        return this == ATB;
    }
    //endregion Boolean Comparison Methods

    public AbstractLightConditionsGenerator getGenerator() {
        switch (this) {
            case ATB:
                return new AtBLightConditionsGenerator();
            case NONE:
            default:
                return new DisabledLightConditionsGenerator();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
