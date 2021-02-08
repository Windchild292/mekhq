/*
 * Copyright (C) 2019-2021 - The MegaMek Team. All Rights Reserved.
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

import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.SkillType;

/**
 * Represents a class which can generate new Special Abilities
 * for a {@link Person}.
 */
public abstract class AbstractSpecialAbilityGenerator {
    /**
     * Generates special abilities for the {@link Person} given their
     * experience level.
     * @param campaign The {@link Campaign} the {@link Person} is a member of
     * @param person The {@link Person} to add special abilities.
     * @param expLvl The experience level of the person (e.g. {@link SkillType#EXP_GREEN}).
     * @return A value indicating whether or not a special ability was assigned.
     */
    public abstract boolean generateSpecialAbilities(Campaign campaign, Person person, int expLvl);
}
