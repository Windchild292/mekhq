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
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.enums.PersonnelStatus;
import mekhq.campaign.personnel.enums.RandomDeathRandomizationType;

public abstract class AbstractRandomDeathGenerator {
    //region Variable Declarations
    protected RandomDeathRandomizationType type;
    //endregion Variable Declarations

    protected AbstractRandomDeathGenerator() {

    }

    public abstract boolean randomDeath(int age, int gender);

    public PersonnelStatus getCause(Person person, Campaign campaign) {
        // First, we check if the person is pregnant
        if (person.isPregnant()) {
            if (campaign.getCampaignOptions().useUnofficialProcreation()) {
                int pregnancyWeek = person.getPregnancyWeek(campaign.getLocalDate());
                double babyBornChance;
                if (pregnancyWeek > 35) {
                    babyBornChance = 1;
                } else if (pregnancyWeek > 29) {
                    babyBornChance = 0.95;
                } else if (pregnancyWeek > 25) {
                    babyBornChance = 0.9;
                } else if (pregnancyWeek == 25) {
                    babyBornChance = 0.8; // TODO : Windchild make me an option
                } else if (pregnancyWeek == 24) {
                    babyBornChance = 0.5; // TODO : Windchild make me an option
                } else if (pregnancyWeek == 23) {
                    babyBornChance = 0.25; // TODO : Windchild make me an option
                } else {
                    babyBornChance = 0;
                }

                if (Compute.randomFloat() < babyBornChance) {
                    person.birth();
                }
            }

            return PersonnelStatus.PREGNANCY_COMPLICATIONS;
        }

        return PersonnelStatus.NATURAL_CAUSES;
    }
}
