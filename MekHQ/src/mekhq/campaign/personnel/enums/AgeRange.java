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

public enum AgeRange {
    //region Enum Declarations
    ELDER("AgeRange.ELDER.text", 65),
    ADULT("AgeRange.ADULT.text", 20),
    TEENAGER("AgeRange.TEENAGER.text", 13),
    PRETEEN("AgeRange.PRETEEN.text", 10),
    CHILD("AgeRange.CHILD.text", 3),
    TODDLER("AgeRange.TODDLER.text", 1),
    BABY("AgeRange.BABY.text", -1);
    //endregion Enum Declarations

    //region Variable Declarations
    private final String rangeName;
    private final int rangeLowerBound; // the lower bound, inclusive
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    AgeRange(final String rangeName, final int rangeLowerBound) {
        this.rangeName = resources.getString(rangeName);
        this.rangeLowerBound = rangeLowerBound;
    }
    //endregion Constructors

    //region Getters
    public int getRangeLowerBound() {
        return rangeLowerBound;
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

    public static AgeRange determineAgeRange(final int age) {
        if (age > -1) {
            for (final AgeRange range : AgeRange.values()) {
                if (age >= range.getRangeLowerBound()) {
                    return range;
                }
            }
        } else {
            MekHQ.getLogger().error("Illegal age of " + age + " entered for a person");
        }

        // This is a default return, which will only happen on error cases
        return ADULT;
    }

    @Override
    public String toString() {
        return rangeName;
    }
}
