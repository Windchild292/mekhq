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
package mekhq.campaign.randomEvents.generators;


import megamek.common.util.WeightedMap;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.randomEvents.AbstractRandomEvent;
import mekhq.campaign.randomEvents.enums.RandomEventType;
import mekhq.campaign.randomEvents.events.emptyEvents.EmptyEvent;
import mekhq.campaign.randomEvents.events.exampleEvents.ExampleEvent;

public class RandomEventGenerator {
    //region Variable Declarations
    private static RandomEventGenerator randomEventGenerator;

    private static volatile boolean initialized = false; // volatile to ensure readers get the current version

    private static WeightedMap<AbstractRandomEvent> positiveEventMap = new WeightedMap<>();
    private static WeightedMap<AbstractRandomEvent> neutralEventMap = new WeightedMap<>();
    private static WeightedMap<AbstractRandomEvent> negativeEventMap = new WeightedMap<>();
    private static WeightedMap<AbstractRandomEvent> tradeOffEventMap = new WeightedMap<>();
    private static WeightedMap<AbstractRandomEvent> missionEventMap = new WeightedMap<>();
    private static WeightedMap<AbstractRandomEvent> nothingEventMap = new WeightedMap<>();
    private static WeightedMap<AbstractRandomEvent> exampleEventMap = new WeightedMap<>();
    private static WeightedMap<AbstractRandomEvent> emptyEventMap = new WeightedMap<>();

    // TODO : Remove these and replace them with campaign options
    private static final int POSITIVE_EVENTS_WEIGHT = 1;
    private static final int NEUTRAL_EVENTS_WEIGHT = 1;
    private static final int NEGATIVE_EVENTS_WEIGHT = 1;
    private static final int TRADE_OFF_EVENTS_WEIGHT = 1;
    private static final int MISSION_EVENTS_WEIGHT = 1;
    private static final int NOTHING_EVENTS_WEIGHT = 1;
    private static final int EXAMPLE_EVENTS_WEIGHT = 1;
    private static final int EMPTY_EVENTS_WEIGHT = 0;

    //endregion Variable Declarations

    //region Initialization
    /**
     * @return the instance of the RandomEventGenerator to use
     */
    public static synchronized RandomEventGenerator getInstance() {
        // only this code reads and writes randomEventGenerator
        if (randomEventGenerator == null) {
            // synchronized ensures this will only be entered exactly once
            randomEventGenerator = new RandomEventGenerator();
            randomEventGenerator.runThreadLoader();
        }
        // when getInstance returns, rng will always be non-null
        return randomEventGenerator;
    }

    private void runThreadLoader() {
        Thread loader = new Thread(() -> randomEventGenerator.initializeEvents(),
                "Random Event Generator initializer");
        loader.setPriority(Thread.NORM_PRIORITY - 1);
        loader.start();
    }

    private void initializeEvents() {
        initializePositiveEvents();
        initializeNeutralEvents();
        initializeNegativeEvents();
        initializeTradeOffEvents();
        initializeMissionEvents();
        initializeNothingEvents();
        initializeEmptyEvents();
        initializeExampleEvents();

        initialized = true;
    }

    private void initializePositiveEvents() {
        AbstractRandomEvent event;

        //positiveEventMap.add(event.getEventWeight(), event);
    }

    private void initializeNeutralEvents() {
        AbstractRandomEvent event;

        //neutralEventMap.add(event.getEventWeight(), event);
    }

    private void initializeNegativeEvents() {
        AbstractRandomEvent event;

        //negativeEventMap.add(event.getEventWeight(), event);
    }

    private void initializeTradeOffEvents() {
        AbstractRandomEvent event;

        //tradeOffEventMap.add(event.getEventWeight(), event);
    }

    private void initializeMissionEvents() {
        AbstractRandomEvent event;

        //missionEventMap.add(event.getEventWeight(), event);
    }

    private void initializeNothingEvents() {
        AbstractRandomEvent event;

        //nothingEventMap.add(event.getEventWeight(), event);
    }

    private void initializeExampleEvents() {
        AbstractRandomEvent event;

        event = new ExampleEvent();
        exampleEventMap.add(event.getEventWeight(), event);
    }

    private void initializeEmptyEvents() {
        AbstractRandomEvent event;

        event = new EmptyEvent();
        emptyEventMap.add(event.getEventWeight(), event);
    }
    //endregion Initialization

    //region Generation
    public AbstractRandomEvent generate() {
        return generate(null);
    }

    public AbstractRandomEvent generate(Campaign campaign) {
        if (initialized) {
            // Create Weighted Map of the other maps
            WeightedMap<WeightedMap<AbstractRandomEvent>> randomEventMap = new WeightedMap<>();
            randomEventMap.add(POSITIVE_EVENTS_WEIGHT, positiveEventMap);
            randomEventMap.add(NEUTRAL_EVENTS_WEIGHT, neutralEventMap);
            randomEventMap.add(NEGATIVE_EVENTS_WEIGHT, negativeEventMap);
            randomEventMap.add(TRADE_OFF_EVENTS_WEIGHT, tradeOffEventMap);
            randomEventMap.add(MISSION_EVENTS_WEIGHT, missionEventMap);
            randomEventMap.add(NOTHING_EVENTS_WEIGHT, nothingEventMap);
            randomEventMap.add(EXAMPLE_EVENTS_WEIGHT, exampleEventMap);
            randomEventMap.add(EMPTY_EVENTS_WEIGHT, emptyEventMap);

            // Set the Campaign to generate for
            AbstractRandomEvent.setCampaign(campaign);

            // Return the generated random event
            return generateFromMap(randomEventMap.randomItem());
        } else {
            MekHQ.getLogger().error(getClass(), "generate",
                    "Attempted to generate events before initializing them.");
            return null;
        }
    }

    public AbstractRandomEvent generateType(RandomEventType type) {
        return generateType(type, null);
    }

    public AbstractRandomEvent generateType(RandomEventType type, Campaign campaign) {
        if (initialized) {
            // Set the Campaign to generate the random event for
            AbstractRandomEvent.setCampaign(campaign);

            // Determine which event map to use
            WeightedMap<AbstractRandomEvent> eventMap;
            switch (type) {
                case POSITIVE:
                    eventMap = positiveEventMap;
                    break;
                case NEUTRAL:
                    eventMap = neutralEventMap;
                    break;
                case NEGATIVE:
                    eventMap = negativeEventMap;
                    break;
                case TRADE_OFF:
                    eventMap = tradeOffEventMap;
                    break;
                case MISSION:
                    eventMap = missionEventMap;
                    break;
                case NOTHING:
                    eventMap = nothingEventMap;
                    break;
                case EXAMPLE:
                    eventMap = exampleEventMap;
                    break;
                case EMPTY:
                default:
                    eventMap = emptyEventMap;
                    break;
            }

            // Generate based on that map
            return generateFromMap(eventMap);
        } else {
            MekHQ.getLogger().error(getClass(), "generateType",
                    "Attempted to generate events before initializing them.");
            return null;
        }
    }

    private AbstractRandomEvent generateFromMap(WeightedMap<AbstractRandomEvent> eventMap) {
        AbstractRandomEvent event = eventMap.randomItem();
        int count = -1;
        while (!event.isViable() && (++count < 10)) {
            event = eventMap.randomItem();
        }

        return (count < 10) ? event : null;
    }
    //endregion Generation
}
