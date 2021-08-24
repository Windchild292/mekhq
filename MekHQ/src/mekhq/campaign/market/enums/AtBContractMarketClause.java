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
package mekhq.campaign.market.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;

import java.util.ResourceBundle;

public enum AtBContractMarketClause {
    //region Enum Declarations
    COMMAND("AtBContractMarketClause.COMMAND.text", "AtBContractMarketClause.COMMAND.toolTipText"),
    SALVAGE("AtBContractMarketClause.SALVAGE.text", "AtBContractMarketClause.SALVAGE.toolTipText"),
    SUPPORT("AtBContractMarketClause.SUPPORT.text", "AtBContractMarketClause.SUPPORT.toolTipText"),
    TRANSPORT("AtBContractMarketClause.TRANSPORT.text", "AtBContractMarketClause.TRANSPORT.toolTipText");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final String toolTipText;
    //endregion Variable Declarations

    //region Constructors
    AtBContractMarketClause(final String name, final String toolTipText) {
        final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Market", new EncodeControl());
        this.name = resources.getString(name);
        this.toolTipText = resources.getString(toolTipText);
    }
    //endregion Constructors

    //region Getters
    public String getToolTipText() {
        return toolTipText;
    }
    //endregion Getters

    //region Boolean Comparison Methods
    public boolean isCommand() {
        return this == COMMAND;
    }

    public boolean isSalvage() {
        return this == SALVAGE;
    }

    public boolean isSupport() {
        return this == SUPPORT;
    }

    public boolean isTransport() {
        return this == TRANSPORT;
    }
    //endregion Boolean Comparison Methods

    //region File I/O
    public static AtBContractMarketClause parseFromString(final String text) {
        try {
            return valueOf(text);
        } catch (Exception ignored) {

        }

        try {
            switch (Integer.parseInt(text)) {
                case 0:
                    return COMMAND;
                case 1:
                    return SALVAGE;
                case 2:
                    return SUPPORT;
                case 3:
                    return TRANSPORT;
                default:
                    break;
            }
        } catch (Exception ignored) {

        }

        MekHQ.getLogger().error("Failed to parse " + text + " into an AtBContractMarketClause. Returning COMMAND.");

        return COMMAND;
    }
    //endregion File I/O

    @Override
    public String toString() {
        return name;
    }
}
