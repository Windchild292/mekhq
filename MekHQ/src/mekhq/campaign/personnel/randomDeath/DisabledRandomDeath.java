/*
 * Copyright (c) 2020-2021 - The MegaMek Team. All rights reserved.
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
package mekhq.campaign.personnel.randomDeath;

import megamek.common.enums.Gender;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.enums.AgeGroup;
import mekhq.campaign.personnel.enums.RandomDeathMethod;

public class DisabledRandomDeath extends AbstractRandomDeath {
    //region Constructors
    public DisabledRandomDeath() {
        super(RandomDeathMethod.NONE);
    }
    //endregion Constructors

    @Override
    public boolean randomDeath(final AgeGroup ageGroup, final int age, final Gender gender) {
        return false;
    }
}
