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
package mekhq.campaign.market.enums;

import megamek.common.util.EncodeControl;
import mekhq.campaign.market.unitMarket.AbstractUnitMarket;
import mekhq.campaign.market.unitMarket.AtBUnitMarket;

import java.util.ResourceBundle;

public enum UnitMarketMethod {
    //region Enum Declarations
    NONE("UnitMarketMethod.NONE.text"),
    ATB_MONTHLY("UnitMarketMethod.ATB_MONTHLY.text");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Market", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    UnitMarketMethod(String name) {
        this.name = resources.getString(name);
    }
    //endregion Constructors

    //region Boolean Comparisons
    public boolean isNone() {
        return this == NONE;
    }

    public boolean isAtB() {
        return this == ATB_MONTHLY;
    }
    //endregion Boolean Comparisons

    public AbstractUnitMarket getUnitMarket() {
        switch (this) {
            case ATB_MONTHLY:
                return new AtBUnitMarket();
            case NONE:
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
