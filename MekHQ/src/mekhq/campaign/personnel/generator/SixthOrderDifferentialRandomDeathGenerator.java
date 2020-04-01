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
package mekhq.campaign.personnel.generator;

import megamek.common.Compute;
import megamek.common.Crew;
import mekhq.MekHQ;
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.personnel.enums.RandomDeathRandomizationType;

public class SixthOrderDifferentialRandomDeathGenerator extends AbstractRandomDeathGenerator {
    //region Variable Declarations
    // the following are a list of variables in the format m * 10^n
    private static double[] maleM;
    private static double[] maleN;
    private static double[] femaleM;
    private static double[] femaleN;
    //endregion Variable Declarations

    //region Constructors
    public SixthOrderDifferentialRandomDeathGenerator() {
        this(null);
    }

    public SixthOrderDifferentialRandomDeathGenerator(CampaignOptions campaignOptions) {
        this.type = RandomDeathRandomizationType.STANDARD;
        if (campaignOptions != null) {
            maleM = campaignOptions.getRandomDeathMaleMValues();
            maleN = campaignOptions.getRandomDeathMaleNValues();
            femaleM = campaignOptions.getRandomDeathFemaleMValues();
            femaleN = campaignOptions.getRandomDeathFemaleNValues();
        }
    }
    //endregion Constructors

    @Override
    public boolean randomDeath(int x, int gender) {
        double chance = 0.0;
        if (gender == Crew.G_MALE) {
            for (int i = 0; i < maleM.length; i++) {
                chance += maleM[i] * Math.pow(10, maleN[i]) * Math.pow(x, i);
            }
        } else {
            for (int i = 0; i < femaleM.length; i++) {
                chance += femaleM[i] * Math.pow(10, femaleN[i]) * Math.pow(x, i);
            }
        }

        MekHQ.getLogger().warning(getClass(), "randomDeath",
                "The odds of randomly dying were calculated to be " + chance + " for a "
                        + (gender == Crew.G_FEMALE ? "female" : "male") + " person");

        return Compute.randomFloat() < chance;
    }
}
