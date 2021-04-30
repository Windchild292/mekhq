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
package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;

import java.util.ResourceBundle;

public enum AgeGroup {
    //region Enum Declarations
    ELDER("AgeGroup.ELDER.text", 65),
    ADULT("AgeGroup.ADULT.text", 20),
    TEENAGER("AgeGroup.TEENAGER.text", 13),
    PRETEEN("AgeGroup.PRETEEN.text", 10),
    CHILD("AgeGroup.CHILD.text", 3),
    TODDLER("AgeGroup.TODDLER.text", 1),
    BABY("AgeGroup.BABY.text", -1);
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final int groupLowerBound; // the lower bound of the age range for this age group, inclusive
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    AgeGroup(final String name, final int groupLowerBound) {
        this.name = resources.getString(name);
        this.groupLowerBound = groupLowerBound;
    }
    //endregion Constructors

    //region Getters
    public int getGroupLowerBound() {
        return groupLowerBound;
    }
    //endregion Getters

    //region Boolean Comparison Methods
    public boolean isElder() {
        return this == ELDER;
    }

    public boolean isAdult() {
        return this == ADULT;
    }

    public boolean isTeenager() {
        return this == TEENAGER;
    }

    public boolean isPreteen() {
        return this == PRETEEN;
    }

    public boolean isChild() {
        return this == CHILD;
    }

    public boolean isToddler() {
        return this == TODDLER;
    }

    public boolean isBaby() {
        return this == BABY;
    }
    //endregion Boolean Comparison Methods

    public static AgeGroup determineAgeGroup(final int age) {
        for (final AgeGroup ageGroup : AgeGroup.values()) {
            if (age >= ageGroup.getGroupLowerBound()) {
                return ageGroup;
            }
        }

        MekHQ.getLogger().error("Illegal age of " + age + " entered for a person. Returning Adult");

        // This is a default return, which will only happen on error cases
        return ADULT;
    }

    @Override
    public String toString() {
        return name;
    }
}
