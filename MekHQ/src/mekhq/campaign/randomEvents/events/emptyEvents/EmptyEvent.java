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
package mekhq.campaign.randomEvents.events.emptyEvents;

import mekhq.MekHQ;
import mekhq.campaign.randomEvents.AbstractRandomEvent;
import mekhq.campaign.randomEvents.events.emptyEvents.AbstractEmptyEvent;

public class EmptyEvent extends AbstractEmptyEvent {
    //region Constructors
    public EmptyEvent() {
        super(resourceMap.getString("EmptyEvent.name"));
    }
    //endregion Constructors

    //region Initialisation Methods
    /**
     * @see AbstractRandomEvent#initializeRandomEventWeight() for more information
     */
    @Override
    protected void initializeRandomEventWeight() {
        //the weight only needs to be 1 for the empty event, as it is the only one of its type
        this.eventWeight = 1;
    }

    /**
     * @see AbstractRandomEvent#initializeDescriptionAndOptions() for more information
     */
    @Override
    protected void initializeDescriptionAndOptions(){
        randomEventDescription = resourceMap.getString("EmptyEvent.text");
        randomEventOptions = new String[]{};
    }
    //endregion Initialisation Methods

    //region Generation
    /**
     * This event is always viable, but is only used as an error condition
     * @see AbstractRandomEvent#isViable() for more information
     */
    @Override
    public boolean isViable() {
        return true;
    }
    //endregion Generation

    //region Processing
    /**
     * This writes an error to the logs
     * @see AbstractRandomEvent#process(int) for more information
     */
    @Override
    public void process(int optionIndex) {
        logProcessEvent();

        MekHQ.getLogger().error(getClass(), "process",
                "An Empty Event was generated. This is likely the result of an error. " +
                        "Please open a ticket on the GitHub including your .cnpx file and this log " +
                        "file, and we will try to fix the issue.");
    }
    //endregion Processing
}
