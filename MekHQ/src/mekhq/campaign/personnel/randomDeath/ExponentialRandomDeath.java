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

public class ExponentialRandomDeath extends AbstractRandomDeathMethod {
    //region Variable Declarations
    private final double[] male;
    private final double[] female;
    //endregion Variable Declarations

    //region Constructors
    public ExponentialRandomDeath(final CampaignOptions campaignOptions) {
        this(campaignOptions.getRandomDeathExponentialMaleValues(),
                campaignOptions.getRandomDeathExponentialFemaleValues());
    }

    public ExponentialRandomDeath(final double[] male, final double[] female) {
        super(RandomDeathMethod.EXPONENTIAL);
        this.male = male;
        this.female = female;
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
     * Determines if a person dies a random death based on gender-dependent exponential equations in
     * the format c * 10^n * e^(k * age).
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

        return Compute.randomFloat() < (gender.isMale()
                ? (getMale()[0] * Math.pow(10, getMale()[1]) * Math.exp(getMale()[2] * age))
                : (getFemale()[0] * Math.pow(10, getFemale()[1]) * Math.exp(getFemale()[2] * age)));
    }
}
