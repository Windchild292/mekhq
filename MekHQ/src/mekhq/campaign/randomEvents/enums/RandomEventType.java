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
package mekhq.campaign.randomEvents.enums;

public enum RandomEventType {
    /**
     * Events that primarily or only have a positive effect
     */
    POSITIVE,
    /**
     * Events that have a positive and negative effect that offset each other
     */
    NEUTRAL,
    /**
     * Events that primarily or only have a negative effect
     */
    NEGATIVE,
    /**
     * Events that have a trade off, but lead towards POSITIVE or NEGATIVE options
     */
    TRADE_OFF,
    /**
     * This is used for any events that are based about missions and scenarios
     */
    MISSION,
    /**
     * Events that do nothing, but are included for fluff reasons and/or to bring players further into
     * the world
     */
    NOTHING,
    /**
     * This is used for the Example event, and should NEVER be used outside of examples
     */
    EXAMPLE,
    /**
     * This is used for the Empty event, and should NEVER be used elsewhere
     */
    EMPTY
}
