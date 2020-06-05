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
package mekhq.campaign.personnel.generator;

import megamek.common.Compute;
import megamek.common.enums.Gender;
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
    public SixthOrderDifferentialRandomDeathGenerator(CampaignOptions campaignOptions) {
        super(RandomDeathRandomizationType.STANDARD);
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
    public boolean randomDeath(int age, Gender gender) {
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
            MekHQ.getLogger().error(getClass(), "randomDeath",
                    "Unable to process random death for unknown biological gender " + gender.toString());
        }

        MekHQ.getLogger().warning(getClass(), "randomDeath",
                "The odds of randomly dying were calculated to be " + chance + " for a "
                        + (gender == Gender.FEMALE ? "female" : "male") + " person");

        return Compute.randomFloat() < chance;
    }
}