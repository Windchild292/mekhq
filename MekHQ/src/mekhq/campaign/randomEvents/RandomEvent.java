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

import mekhq.campaign.Campaign;
import mekhq.campaign.randomEvents.enums.RandomEventType;

public class RandomEvent extends AbstractRandomEvent{
    //region Constructors
    public RandomEvent() {
        super("EmptyEvent", null, RandomEventType.EMPTY);
    }

    public RandomEvent(String name, RandomEventType type) {
        super(name, null, type);
    }

    public RandomEvent(Campaign campaign, RandomEventType type) {
        super("UnnamedEvent", campaign, type);
    }

    public RandomEvent(String eventName, Campaign campaign, RandomEventType type) {
        super(eventName, campaign, type);
    }
    //endregion Constructors

    //region Process
    @Override
    public void process() {
        logProcessEvent();
    }
    //endregion Process
}
