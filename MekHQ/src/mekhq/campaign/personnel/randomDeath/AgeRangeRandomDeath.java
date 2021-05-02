/*
 * Copyright (c) 2021 - The MegaMek Team. All rights reserved.
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
import mekhq.campaign.Campaign;
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.personnel.enums.AgeGroup;
import mekhq.campaign.personnel.enums.RandomDeathMethod;

public class AgeRangeRandomDeath extends AbstractRandomDeathMethod {
    //region Variable Declarations
    private final double[] male;
    private final double[] female;
    //endregion Variable Declarations

    //region Constructors
    public AgeRangeRandomDeath(final CampaignOptions campaignOptions) {
        this(campaignOptions.getAgeRangeRandomDeathMaleValues(),
                campaignOptions.getAgeRangeRandomDeathFemaleValues());
    }

    public AgeRangeRandomDeath(final double[] male, final double[] female) {
        super(RandomDeathMethod.AGE_RANGE);

        // Odds are over an entire year per 100,000 people, so we need to adjust the numbers to be
        // the individual odds for a day. We do this now so it only occurs once
        final double baseAdjustment = 365.25 * 100000.0;
        final double decadeAdjustment = 10.0 * baseAdjustment;
        final double[] adjustedMale = new double[male.length];
        final double[] adjustedFemale = new double[female.length];
        adjustedMale[0] = male[0] / baseAdjustment;
        adjustedFemale[0] = female[0] / baseAdjustment;
        adjustedMale[1] = male[1] / (4.0 * baseAdjustment);
        adjustedFemale[1] = female[1] / (4.0 * baseAdjustment);
        for (int i = 2; i < 9; i++) {
            adjustedMale[i] = male[i] / decadeAdjustment;
            adjustedFemale[i] = female[i] / decadeAdjustment;
        }
        adjustedMale[10] = male[10] / (15.0 * baseAdjustment);
        adjustedFemale[10] = female[10] / (15.0 * baseAdjustment);
        this.male = adjustedMale;
        this.female = adjustedFemale;
    }
    //endregion Constructors

    //region Getters
    public double[] getMale() {
        return male;
    }

    public double[] getFemale() {
        return female;
    }
    //endregion Getters

    /**
     * @param campaign the campaign the person is in
     * @param ageGroup the person's age grouping
     * @param age the person's age
     * @param gender the person's gender
     * @return true if the person is selected to randomly die, otherwise false
     */
    @Override
    public boolean randomDeath(final Campaign campaign, final AgeGroup ageGroup, final int age,
                               final Gender gender) {
        if (!validateAgeEnabled(campaign, ageGroup)) {
            return false;
        }

        final int index;
        if (age < 1) {
            index = 0;
        } else if (age < 5) {
            index = 1;
        } else if (age < 15) {
            index = 2;
        } else if (age < 25) {
            index = 3;
        } else if (age < 35) {
            index = 4;
        } else if (age < 45) {
            index = 5;
        } else if (age < 55) {
            index = 6;
        } else if (age < 65) {
            index = 7;
        } else if (age < 75) {
            index = 8;
        } else if (age < 85) {
            index = 9;
        } else {
            index = 10;
        }

        return Compute.randomFloat() < (gender.isMale() ? getMale()[index] : getFemale()[index]);
    }
}
