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
package mekhq.campaign.universe.generators.lightConditionsGenerator;

import megamek.common.PlanetaryConditions;
import mekhq.campaign.universe.enums.LightConditionsGenerationMethod;

public abstract class AbstractLightConditionsGenerator {
    //region Variable Declarations
    private final LightConditionsGenerationMethod method;
    //endregion Variable Declarations

    //region Constructors
    protected AbstractLightConditionsGenerator(final LightConditionsGenerationMethod method) {
        this.method = method;
    }
    //endregion Constructors

    //region Getters
    public LightConditionsGenerationMethod getMethod() {
        return method;
    }
    //endregion Getters

    /**
     * This generates a random light level for a scenario
     * @return one of the {@link PlanetaryConditions} Light Magic Numbers
     */
    public abstract int generate();
}
