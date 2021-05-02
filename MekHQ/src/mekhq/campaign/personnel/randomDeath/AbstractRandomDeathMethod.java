/*
 * Copyright (c) 2020-2021 - The MegaMek Team. All Rights Reserved.
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
import mekhq.campaign.personnel.Injury;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.enums.AgeGroup;
import mekhq.campaign.personnel.enums.InjuryLevel;
import mekhq.campaign.personnel.enums.PersonnelStatus;
import mekhq.campaign.personnel.enums.RandomDeathMethod;

import java.time.LocalDate;

public abstract class AbstractRandomDeathMethod {
    //region Variable Declarations
    private final RandomDeathMethod method;
    //endregion Variable Declarations

    //region Constructors
    protected AbstractRandomDeathMethod(final RandomDeathMethod method) {
        this.method = method;
    }
    //endregion Constructors

    //region Getters
    public RandomDeathMethod getMethod() {
        return method;
    }
    //endregion Getters

    /**
     * @param campaign the campaign the person is in
     * @param ageGroup the person's age grouping
     * @param age the person's age
     * @param gender the person's gender
     * @return true if the person is selected to randomly die, otherwise false
     */
    public abstract boolean randomDeath(final Campaign campaign, final AgeGroup ageGroup,
                                        final int age, final Gender gender);

    /**
     * @param campaign the campaign the person is in
     * @param ageGroup the person's age grouping
     * @return true if the random death is enabled for the age group
     */
    public boolean validateAgeGroupEnabled(final Campaign campaign, final AgeGroup ageGroup) {
        switch (ageGroup) {
            case ELDER:
            case ADULT:
                return true;
            case TEENAGER:
                return campaign.getCampaignOptions().isEnableTeenRandomDeaths();
            case PRETEEN:
                return campaign.getCampaignOptions().isEnablePreteenRandomDeaths();
            case CHILD:
                return campaign.getCampaignOptions().isEnableChildRandomDeaths();
            case TODDLER:
                return campaign.getCampaignOptions().isEnableToddlerRandomDeaths();
            case BABY:
                return campaign.getCampaignOptions().isEnableInfantMortality();
            default:
                return false;
        }
    }

    //region Cause
    /**
     * @param today the current day
     * @param person the person who has died
     * @param ageGroup the person's age grouping
     * @return the cause of the Person's random death
     */
    public PersonnelStatus getCause(final LocalDate today, final Person person, final AgeGroup ageGroup) {
        if (person.getStatus().isMIA()) {
            return PersonnelStatus.KIA;
        } else if (person.hasInjuries(false)) {
            final PersonnelStatus status = determineIfInjuriesCausedTheDeath(person);
            if (!status.isActive()) {
                return status;
            }
        }

        if (person.isPregnant() && (person.getPregnancyWeek(today) > 22)) {
            return PersonnelStatus.PREGNANCY_COMPLICATIONS;
        } else if (ageGroup.isElder()) {
            return PersonnelStatus.OLD_AGE;
        }

        return PersonnelStatus.NATURAL_CAUSES;
    }

    /**
     * @param person the person from whom may have died of injuries
     * @return the personnel status applicable to the form of injury that caused the death, or
     * ACTIVE if it wasn't determined that injuries caused the death
     */
    private PersonnelStatus determineIfInjuriesCausedTheDeath(final Person person) {
        for (final Injury injury : person.getInjuries()) {
            final InjuryLevel level = injury.getLevel();

            // We care about injuries that are major or deadly. We do not want any chronic
            // conditions nor scratches
            if ((level == InjuryLevel.DEADLY) || (level == InjuryLevel.MAJOR)) {
                return PersonnelStatus.WOUNDS;
            }
        }

        return PersonnelStatus.ACTIVE;
    }
    //endregion Cause
}
