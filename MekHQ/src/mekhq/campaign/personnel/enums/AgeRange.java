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
package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;

import java.util.ResourceBundle;

public enum AgeRange {
    //region Enum Declarations
    ELDER("AgeRanges.ELDER.text", 65),
    ADULT("AgeRanges.ADULT.text", 20),
    TEENAGE("AgeRanges.TEENAGER.text", 13),
    PRETEEN("AgeRanges.PRETEEN.text", 10),
    CHILD("AgeRanges.CHILD.text", 3),
    TODDLER("AgeRanges.TODDLER.text", 1),
    BABY("AgeRanges.BABY.text");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String rangeName;
    private final int rangeLowerBound;
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel",
            new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    AgeRange(String rangeName) {
        this(rangeName, -1);
    }

    AgeRange(String rangeName, int rangeLowerBound) {
        this.rangeName = resources.getString(rangeName);
        this.rangeLowerBound = rangeLowerBound;
    }
    //endregion Constructors

    public String getRangeName() {
        return rangeName;
    }

    public int getRangeLowerBound() {
        return rangeLowerBound;
    }

    public static AgeRange determineAgeRange(int age) {
        if (age > -1) {
            for (AgeRange range : AgeRange.values()) {
                if (age > range.getRangeLowerBound()) {
                    return range;
                }
            }
        } else {
            MekHQ.getLogger().error(AgeRange.class, "determineAgeRange",
                    "Illegal age of " + age + " entered for a person");
        }

        // This is a default return, which will only happen on error cases
        return ADULT;
    }
}
