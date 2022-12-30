/*
 * Copyright (c) 2017-2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.universe;

import megamek.client.generator.RandomUnitGenerator;
import megamek.common.MechSummary;
import megamek.common.enums.SkillLevel;
import mekhq.campaign.rating.AbstractUnitRating;
import org.apache.logging.log4j.LogManager;

import java.util.*;

/**
 * Base class for unit generators containing common functionality.
 * Currently, only turret-related code.
 * @author NickAragua
 */
public abstract class AbstractUnitGenerator implements IUnitGenerator {
    private Map<Integer, String> ratRatingMappings = null;
    private TreeSet<Integer> turretRatYears = new TreeSet<>();
    private Map<Integer, Map<String, String>> turretRatNames = new HashMap<>();

    /**
     * Worker function to initialize the mapping between a numeric quality rating level
     * and an alphabetic one (such as one used in the RATs)
     */
    private void initializeRatRatingMappings() {
        // TODO : Switch this with a call to a new AbstractUnitRating array
        if (ratRatingMappings == null) {
            ratRatingMappings = new HashMap<>();
            ratRatingMappings.put(AbstractUnitRating.DRAGOON_ASTAR, "A");
            ratRatingMappings.put(AbstractUnitRating.DRAGOON_A, "A");
            ratRatingMappings.put(AbstractUnitRating.DRAGOON_B, "B");
            ratRatingMappings.put(AbstractUnitRating.DRAGOON_C, "C");
            ratRatingMappings.put(AbstractUnitRating.DRAGOON_D, "D");
            ratRatingMappings.put(AbstractUnitRating.DRAGOON_F, "F");
        }
    }

    /**
     * Generates a list of turrets given a skill level, quality and year
     * @param num How many turrets to generate
     * @param skill The skill level of the turret operator
     * @param quality The quality level of the turret
     * @param currentYear The current year
     * @return List of turrets
     */
    @Override
    public List<MechSummary> generateTurrets(int num, SkillLevel skill, int quality, int currentYear) {
        int ratYear;

        // less dirty hack
        // we loop through the names of available turret RATs
        // and pick the latest one
        // turret rat file names appear to follow the pattern of "Turrets YYYY Q"
        // where YYYY is the four-digit year
        // and Q is the quality level of the force.
        // This way, as long as the turret RAT names follow the above-described pattern, we can handle any number of them.
        initializeRatRatingMappings();

        for (Iterator<String> rats = RandomUnitGenerator.getInstance().getRatList(); rats.hasNext();) {
            String currentName = rats.next();
            if (currentName.contains("Turrets")) {
                String turretQuality = currentName.substring(currentName.length() - 1);
                int year = Integer.parseInt(currentName.replaceAll("\\D", ""));

                turretRatYears.add(year);

                if (!turretRatNames.containsKey(year)) {
                    turretRatNames.put(year, new HashMap<>());
                }

                turretRatNames.get(year).put(turretQuality, currentName);
            }
        }

        // We don't have rats for *every* year, so we find the nearest previous one. If there is no
        // RAT for the current or previous year, use the earliest available.
        // If there are no turret RATs, return an empty list
        if (turretRatYears.isEmpty()) {
            LogManager.getLogger().warn("No turret RATs found.");
            return Collections.emptyList();
        } else if (currentYear < turretRatYears.first()) {
            LogManager.getLogger().warn("Earliest turret RAT is later than campaign year.");
            ratYear = turretRatYears.first();
        } else {
            ratYear = turretRatYears.floor(currentYear);
        }

        // now that we have the year, we need to determine which turret RAT we're going to use
        String ratName = turretRatNames.get(ratYear).get(ratRatingMappings.get(quality));

        RandomUnitGenerator.getInstance().setChosenRAT(ratName);
        return RandomUnitGenerator.getInstance().generate(num);
    }
}
