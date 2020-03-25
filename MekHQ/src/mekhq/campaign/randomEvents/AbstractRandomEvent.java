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
    protected Campaign campaign;
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
    //endregion Variable Declarations

    //region Constructors
    protected AbstractRandomEvent(String name) {
        this(name, null);
    }

    protected AbstractRandomEvent(String name, Campaign campaign) {
        this.eventName = name;
        this.campaign = campaign;
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

    //region Process
    /**
     * This is used to process the random event
     */
    public abstract void process();

    /**
     * This is used to debug
     */
    protected void logProcessEvent() {
        if (logProcessEvent) {
            MekHQ.getLogger().info(getClass(), "process",
                    "Processing random event named " + eventName);
        }
    }
    //endregion Process
}
