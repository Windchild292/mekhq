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
package mekhq.campaign.randomEvents.events.exampleEvents;

import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.randomEvents.AbstractRandomEvent;

public class ExampleEvent extends AbstractExampleEvent {
    //region Constructors
    // The following are the list of potential constructors for Random Events
    protected ExampleEvent() {
        super(resourceMap.getString("ExampleEvent.name"));
    }

    protected ExampleEvent(Campaign campaign) {
        super(resourceMap.getString("ExampleEvent.name"), campaign);
    }
    //endregion Constructors

    //region Initialisation Methods
    /**
     * @see AbstractRandomEvent#initializeRandomEventWeight() for more information
     */
    @Override
    protected void initializeRandomEventWeight() {
        this.eventWeight = 1;
    }

    /**
     * @see AbstractRandomEvent#initializeDescriptionAndOptions() for more information
     */
    @Override
    protected void initializeDescriptionAndOptions() {
        this.randomEventDescription = resourceMap.getString("ExampleEvent.text");
        this.randomEventOptions = new String[] {
                resourceMap.getString("ExampleEvent.option0"),
                resourceMap.getString("ExampleEvent.option1"),
                resourceMap.getString("ExampleEvent.option2"),
                resourceMap.getString("ExampleEvent.option3"),
                resourceMap.getString("ExampleEvent.option4")
        };
    }
    //endregion Initialisation Methods

    //region Generation
    /**
     * @see AbstractRandomEvent#isViable() for more information
     */
    @Override
    public boolean isViable() {
        return campaign != null;
    }
    //endregion Generation

    //region Processing
    /**
     * @see AbstractRandomEvent#process(int) for more information
     */
    @Override
    public void process(int optionIndex) {
        logProcessEvent();

        switch (optionIndex) {
            case 0:
                // You would implement your logic for case 0 below, instead of the logger statement
                MekHQ.getLogger().info(getClass(), "process",
                        eventName + ": " + "Option " + randomEventOptions[optionIndex]
                                + ", at index 0 selected.");
                break;
            case 1:
                // You would implement your logic for case 1 below, instead of the logger statement
                MekHQ.getLogger().info(getClass(), "process",
                        eventName + ": " + "Option " + randomEventOptions[optionIndex]
                                + ", at index 1 selected.");
                break;
            case 2:
                // You would implement your logic for case 2 below, instead of the logger statement
                MekHQ.getLogger().info(getClass(), "process",
                        eventName + ": " + "Option " + randomEventOptions[optionIndex]
                                + ", at index 2 selected.");
                break;
            case 3:
                // You would implement your logic for case 3 below, instead of the logger statement
                MekHQ.getLogger().info(getClass(), "process",
                        eventName + ": " + "Option " + randomEventOptions[optionIndex]
                                + ", at index 3 selected.");
                break;
            case 4:
                // You would implement your logic for case 4 below, instead of the logger statement
                MekHQ.getLogger().info(getClass(), "process",
                        eventName + ": " + "Option " + randomEventOptions[optionIndex]
                                + ", at index 4 selected.");
                break;
            case ERROR_INDEX:
                MekHQ.getLogger().error(getClass(), "process",
                        "Cannot process the random event " + eventName
                                + "with a selected index of " + ERROR_INDEX);
                break;
            default:
                MekHQ.getLogger().warning(getClass(), "process",
                        "Unknown index " + optionIndex + ". Cannot process the event "
                                + eventName);
                break;
        }
    }
    //endregion Processing
}
