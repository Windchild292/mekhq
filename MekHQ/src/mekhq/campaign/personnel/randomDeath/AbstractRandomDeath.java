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
import megamek.common.util.weightedMaps.WeightedDoubleMap;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.enums.AgeGroup;
import mekhq.campaign.personnel.enums.PersonnelStatus;
import mekhq.campaign.personnel.enums.RandomDeathMethod;
import mekhq.campaign.personnel.enums.TenYearAgeRange;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRandomDeath {
    //region Variable Declarations
    private final RandomDeathMethod method;
    private final Map<AgeGroup, Boolean> enabledAgeGroups;
    private final Map<Gender, Map<TenYearAgeRange, WeightedDoubleMap<PersonnelStatus>>> causes;
    //endregion Variable Declarations

    //region Constructors
    protected AbstractRandomDeath(final RandomDeathMethod method) {
        this(method, new HashMap<>(), false);
    }

    protected AbstractRandomDeath(final RandomDeathMethod method,
                                  final Map<AgeGroup, Boolean> enabledAgeGroups,
                                  final boolean enableSuicideClause) {
        this.method = method;
        this.enabledAgeGroups = enabledAgeGroups;
        this.causes = new HashMap<>();
        initializeCauses(enableSuicideClause);
    }
    //endregion Constructors

    //region Getters
    public RandomDeathMethod getMethod() {
        return method;
    }

    public Map<AgeGroup, Boolean> getEnabledAgeGroups() {
        return enabledAgeGroups;
    }

    public Map<Gender, Map<TenYearAgeRange, WeightedDoubleMap<PersonnelStatus>>> getCauses() {
        return causes;
    }
    //endregion Getters

    /**
     * @param ageGroup the person's age grouping
     * @param age the person's age
     * @param gender the person's gender
     * @return true if the person is selected to randomly die, otherwise false
     */
    public abstract boolean randomDeath(final AgeGroup ageGroup, final int age, final Gender gender);

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
     * @param person the person from whom may have died of injuries (which may be diseases, once
     *               that is implemented)
     * @return the personnel status applicable to the form of injury that caused the death, or
     * ACTIVE if it wasn't determined that injuries caused the death
     */
    private PersonnelStatus determineIfInjuriesCausedTheDeath(final Person person) {
        // We care about injuries that are major or deadly. We do not want any chronic conditions
        // nor scratches
        return person.getInjuries().stream().anyMatch(injury -> injury.getLevel().isMajorOrDeadly())
                ? PersonnelStatus.WOUNDS : PersonnelStatus.ACTIVE;
    }
    //endregion Cause

    //region File I/O
    public void initializeCauses(final boolean enableSuicideCause) {
        //RANDOM_DEATH_CAUSES_FILE_PATH
        //MegaMekFile
    }
    //endregion File I/O
}
