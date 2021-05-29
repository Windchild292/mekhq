/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.universe.enums;

import megamek.common.util.EncodeControl;

import java.util.ResourceBundle;

public enum ForceNamingMethod {
    //region Enum Declarations
    CCB_1943("ForceNamingMethod.CCB_1943.text", "ForceNamingMethod.CCB_1943.toolTipText"),
    ICAO_1956("ForceNamingMethod.ICAO_1956.text", "ForceNamingMethod.ICAO_1956.toolTipText"),
    GREEK_ALPHABET("ForceNamingMethod.GREEK_ALPHABET.text", "ForceNamingMethod.GREEK_ALPHABET.toolTipText");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final String toolTipText;

    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Universe", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    ForceNamingMethod(final String name, final String toolTipText) {
        this.name = resources.getString(name);
        this.toolTipText = resources.getString(toolTipText);
    }
    //endregion Constructors

    //region Getters
    public String getToolTipText() {
        return toolTipText;
    }
    //endregion Getters

    public String getValue(final Alphabet alphabet) {
        switch (this) {
            case ICAO_1956:
                return alphabet.getICAO1956();
            case GREEK_ALPHABET:
                return alphabet.getGreek();
            case CCB_1943:
            default:
                return alphabet.getCCB1943();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}