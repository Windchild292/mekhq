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
package mekhq.campaign.randomEvents;

import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.randomEvents.enums.RandomEventType;

public abstract class AbstractRandomEvent {
    //region Variable Declarations
    protected String eventName;
    protected Campaign campaign;
    protected RandomEventType type;
    //endregion Variable Declarations

    //region Constructors
    protected AbstractRandomEvent(String name, Campaign campaign, RandomEventType type) {
        this.eventName = name;
        this.campaign = campaign;
        this.type = type;
    }
    //endregion Constructors

    //region Process
    public abstract void process();

    protected void logProcessEvent() {
        MekHQ.getLogger().info(getClass(), "process",
                "Processing random event named " + eventName);
    }
    //endregion Process
}
