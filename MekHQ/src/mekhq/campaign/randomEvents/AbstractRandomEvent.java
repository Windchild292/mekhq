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

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.randomEvents.enums.RandomEventType;

import java.util.ResourceBundle;

public abstract class AbstractRandomEvent {
    //region Variable Declarations
    // Event Information
    protected String eventName;
    protected static Campaign campaign;
    protected RandomEventType type;
    protected int eventWeight; //this is the weighting for generating the specific random event

    // Dialog Text
    protected String randomEventDescription;
    protected String[] randomEventOptions;

    // Localization Map
    protected static ResourceBundle resourceMap = ResourceBundle.getBundle(
            "mekhq.resources.RandomEvent", new EncodeControl());

    // Debugging
    private static final boolean logProcessEvent = true;

    // Constants
    public static final int ERROR_INDEX = -1;
    //endregion Variable Declarations

    //region Constructors
    protected AbstractRandomEvent(String name) {
        this(name, null);
    }

    protected AbstractRandomEvent(String name, Campaign campaign) {
        this.eventName = name;
        setCampaign(campaign);
        initializeRandomEventType();
        initializeRandomEventWeight();
        initializeDescriptionAndOptions();
    }
    //endregion Constructors

    //region Initialisation Methods
    /**
     * This is used to ensure that the RandomEventType is overwritten for each category of event
     */
    protected abstract void initializeRandomEventType();

    /**
     * This is used to ensure that every random event initializes their weight
     */
    protected abstract void initializeRandomEventWeight();

    /**
     * This is used to ensure that every random event initializes their description and options for
     * the dialog
     */
    protected abstract void initializeDescriptionAndOptions();
    //endregion Initialisation Methods

    //region Getters/Setters
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public static void setCampaign(Campaign c) {
        campaign = c;
    }

    public RandomEventType getType() {
        return type;
    }

    public int getEventWeight() {
        return eventWeight;
    }

    public void setEventWeight(int eventWeight) {
        this.eventWeight = eventWeight;
    }
    //endregion Getters/Setters

    //region Generation
    /**
     * This is used to determine if the event is viable or not for the campaign the event is being
     * generated for
     */
    public abstract boolean isViable();
    //endregion Generation

    //region Processing
    /**
     * This is used to process the random event
     * @param optionIndex the index of the selected option, or ERROR_INDEX if no option was selected
     */
    public abstract void process(int optionIndex);

    /**
     * This is used to debug
     */
    protected void logProcessEvent() {
        if (logProcessEvent) {
            MekHQ.getLogger().info(getClass(), "process",
                    "Processing random event named " + eventName);
        }
    }
    //endregion Processing
}
