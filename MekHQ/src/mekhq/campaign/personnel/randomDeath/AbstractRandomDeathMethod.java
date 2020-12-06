/*
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved.
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
import mekhq.campaign.personnel.enums.AgeRange;
import mekhq.campaign.personnel.enums.InjuryLevel;
import mekhq.campaign.personnel.enums.PersonnelStatus;
import mekhq.campaign.personnel.enums.RandomDeathType;

public abstract class AbstractRandomDeathMethod {
    //region Variable Declarations
    protected RandomDeathType type;
    //endregion Variable Declarations

    protected AbstractRandomDeathMethod(RandomDeathType type) {
        this.type = type;
    }

    public abstract boolean randomDeath(int age, Gender gender);

    public PersonnelStatus getCause(Person person, Campaign campaign) {
        if (person.isPregnant() && person.getPregnancyWeek(campaign.getLocalDate()) > 22) {
            return PersonnelStatus.PREGNANCY_COMPLICATIONS;
        } else if (AgeRange.determineAgeRange(person.getAge(campaign.getLocalDate())) == AgeRange.ELDER) {
            // First, we need to see if they succumb to a disease or injury
            PersonnelStatus status = determineIfInjuriesCausedTheDeath(person);
            if (status != null) {
                return status;
            }

            // otherwise, they are claimed by old age
            return PersonnelStatus.OLD_AGE;
        } else if (person.hasInjuries(false)) {
            PersonnelStatus status = determineIfInjuriesCausedTheDeath(person);
            if (status != null) {
                return status;
            }
        }

        return PersonnelStatus.NATURAL_CAUSES;
    }

    private PersonnelStatus determineIfInjuriesCausedTheDeath(Person person) {
        for (Injury injury : person.getInjuries()) {
            InjuryLevel level = injury.getLevel();
            if ((level == InjuryLevel.DEADLY) || (level == InjuryLevel.MAJOR)) {
                // TODO : add diseases
                return PersonnelStatus.WOUNDS;
            }
        }

        return null;
    }
}
