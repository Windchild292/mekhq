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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.personnel.randomDeath;

import megamek.common.Compute;
import megamek.common.enums.Gender;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.enums.AgeRange;
import mekhq.campaign.personnel.enums.RandomDeathType;

public class SixthOrderDifferentialRandomDeath extends AbstractRandomDeathMethod {
    //region Variable Declarations
    // the following are a list of variables in the format m * 10^n
    private double[] maleM;
    private double[] maleN;
    private double[] femaleM;
    private double[] femaleN;
    //endregion Variable Declarations

    //region Constructors
    public SixthOrderDifferentialRandomDeath(Campaign campaign) {
        super(RandomDeathType.STANDARD);
        maleM = campaign.getCampaignOptions().getRandomDeathMaleMValues();
        maleN = campaign.getCampaignOptions().getRandomDeathMaleNValues();
        femaleM = campaign.getCampaignOptions().getRandomDeathFemaleMValues();
        femaleN = campaign.getCampaignOptions().getRandomDeathFemaleNValues();
    }
    //endregion Constructors

    @Override
    public boolean randomDeath(Campaign campaign, AgeRange ageRange, int age, Gender gender) {
        if (!validateAgeEnabled(campaign, ageRange)) {
            return false;
        }

        // The chance is calculated in the format:
        // sum from 0 to M.length of m * 10^n * age^i
        double chance = 0.0;
        if (gender.isMale()) {
            for (int i = 0; i < maleM.length; i++) {
                chance += maleM[i] * Math.pow(10, maleN[i]) * Math.pow(age, i);
            }
        } else if (gender.isFemale()) {
            for (int i = 0; i < femaleM.length; i++) {
                chance += femaleM[i] * Math.pow(10, femaleN[i]) * Math.pow(age, i);
            }
        } else {
            MekHQ.getLogger().error("Unable to process random death for unknown biological gender " + gender.toString());
        }

        MekHQ.getLogger().warning("The odds of randomly dying were calculated to be " + chance + " for a "
                + (gender.isFemale() ? "female" : "male") + " person of " + age + " years.");

        return Compute.randomFloat() < chance;
    }
}
