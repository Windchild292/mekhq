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
package mekhq.campaign.universe.generators.battleMechQualityGenerators;

import mekhq.campaign.rating.AbstractUnitRating;
import mekhq.campaign.universe.enums.BattleMechQualityGenerationMethod;

/**
 * @author Justin "Windchild" Bowen
 */
public class DBattleMechQualityGenerator extends AbstractBattleMechQualityGenerator {
    //region Constructors
    public DBattleMechQualityGenerator() {
        super(BattleMechQualityGenerationMethod.D);
    }
    //endregion Constructors

    @Override
    public int generate(final int roll) {
        return AbstractUnitRating.DRAGOON_D;
    }
}
