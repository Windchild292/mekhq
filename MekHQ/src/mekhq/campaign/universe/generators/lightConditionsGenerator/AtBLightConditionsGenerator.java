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

import megamek.common.Compute;
import megamek.common.PlanetaryConditions;
import mekhq.campaign.universe.enums.LightConditionsGenerationMethod;

public class AtBLightConditionsGenerator extends AbstractLightConditionsGenerator {
    //region Constructors
    public AtBLightConditionsGenerator() {
        super(LightConditionsGenerationMethod.ATB);
    }
    //endregion Constructors

    @Override
    public int generate() {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return PlanetaryConditions.L_DAY;
            case 5:
            case 6:
                return PlanetaryConditions.L_DUSK;
            case 7:
                return PlanetaryConditions.L_FULL_MOON;
            case 8:
                return PlanetaryConditions.L_MOONLESS;
            case 9:
            default: // useless default, but required for Java parsing
                return PlanetaryConditions.L_PITCH_BLACK;
        }
    }
}
