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
package mekhq.campaign.personnel.death;

import megamek.common.Compute;
import megamek.common.enums.Gender;
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.personnel.enums.AgeGroup;
import mekhq.campaign.personnel.enums.RandomDeathMethod;
import mekhq.campaign.personnel.enums.TenYearAgeRange;

import java.util.HashMap;
import java.util.Map;

public class AgeRangeRandomDeath extends AbstractRandomDeath {
    //region Variable Declarations
    private final Map<TenYearAgeRange, Double> male;
    private final Map<TenYearAgeRange, Double> female;
    //endregion Variable Declarations

    //region Constructors
    public AgeRangeRandomDeath(final CampaignOptions campaignOptions) {
        this(campaignOptions.getAgeRangeRandomDeathMaleValues(),
                campaignOptions.getAgeRangeRandomDeathFemaleValues());
    }

    public AgeRangeRandomDeath(final Map<TenYearAgeRange, Double> male,
                               final Map<TenYearAgeRange, Double> female) {
        super(RandomDeathMethod.AGE_RANGE);

        // Odds are over an entire year per 100,000 people, so we need to adjust the numbers to be
        // the individual odds for a day. We do this now so it only occurs once
        final double adjustment = 365.25 * 100000.0;
        final Map<TenYearAgeRange, Double> adjustedMale = new HashMap<>();
        final Map<TenYearAgeRange, Double> adjustedFemale = new HashMap<>();
        for (final TenYearAgeRange ageRange : TenYearAgeRange.values()) {
            adjustedMale.put(ageRange, male.get(ageRange) / adjustment);
            adjustedFemale.put(ageRange, female.get(ageRange) / adjustment);
        }
        this.male = adjustedMale;
        this.female = adjustedFemale;
    }
    //endregion Constructors

    //region Getters
    public Map<TenYearAgeRange, Double> getMale() {
        return male;
    }

    public Map<TenYearAgeRange, Double> getFemale() {
        return female;
    }
    //endregion Getters

    /**
     * @param ageGroup the person's age grouping
     * @param age the person's age
     * @param gender the person's gender
     * @return true if the person is selected to randomly die, otherwise false
     */
    @Override
    public boolean randomDeath(final AgeGroup ageGroup, final int age, final Gender gender) {
        if (!getEnabledAgeGroups().get(ageGroup)) {
            return false;
        }

        final TenYearAgeRange ageRange = TenYearAgeRange.determineAgeRange(age);
        return Compute.randomFloat() < ((gender.isMale() ? getMale() : getFemale()).get(ageRange));
    }
}
