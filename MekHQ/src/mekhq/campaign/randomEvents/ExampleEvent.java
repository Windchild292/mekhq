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

public class ExampleEvent extends RandomEvent {
    //region Constructors
    // The following are the list of potential constructors for Random Events
    public ExampleEvent() {
        super();
    }

    public ExampleEvent(String name, RandomEventType type) {
        super(name, type);
    }

    public ExampleEvent(Campaign campaign, RandomEventType type) {
        super(campaign, type);
    }

    public ExampleEvent(String eventName, Campaign campaign, RandomEventType type) {
        super(eventName, campaign, type);
    }
    //endregion Constructors

    //region Process
    @Override
    public void process() {

    }
    //endregion Process
}
