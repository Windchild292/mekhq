/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.universe.generators.weatherGenerator;

import megamek.common.Compute;
import megamek.common.PlanetaryConditions;
import mekhq.campaign.mission.AtBScenario;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.universe.enums.WeatherGenerationMethod;

import java.util.Set;
import java.util.stream.Stream;

public class LimiterDriveWeatherGenerator extends AbstractWeatherGenerator {
    //region Constructors
    public LimiterDriveWeatherGenerator() {
        super(WeatherGenerationMethod.LIMITER_DRIVE);
    }
    //endregion Constructors

    @Override
    public void generate(final Mission mission, final AtBScenario scenario) {
        // Clear the skies by default
        clearSkies(scenario);

        // Then determine the generation from a shifted table (starting at 0 instead of 1)
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                break;
            case 5:
                generateRain(scenario);
                break;
            case 6:
                generateSnowfall(scenario);
                break;
            case 7:
                generateGale(scenario);
                break;
            case 8:
                generateStorm(scenario);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                generateFog(scenario);
                break;
        }
    }

    private void generateWind(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
                break;
            case 3:
            case 4:
            case 5:
                scenario.setWind(PlanetaryConditions.WI_LIGHT_GALE);
                break;
            case 6:
            case 7:
                scenario.setWind(PlanetaryConditions.WI_MOD_GALE);
                break;
            case 8:
                scenario.setWind(PlanetaryConditions.WI_STRONG_GALE);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                scenario.setWind(PlanetaryConditions.WI_STORM);
                break;
        }
    }

    private void generateFog(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                break;
            case 7:
            case 8:
                scenario.setFog(PlanetaryConditions.FOG_LIGHT);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                scenario.setFog(PlanetaryConditions.FOG_HEAVY);
                break;
        }
    }

    private void generateCalmerWeather(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
                clearSkies(scenario);
                break;
            case 2:
            case 3:
            case 4:
                break;
            case 5:
            case 6:
            case 7:
                scenario.setFog(PlanetaryConditions.FOG_NONE);
                break;
            case 8:
                scenario.setWind(PlanetaryConditions.WI_NONE);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                scenario.setWeather(PlanetaryConditions.WE_NONE);
                break;
        }
    }

    private void generateSpringFallConditions(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
                clearSkies(scenario);
                break;
            case 3:
                break;
            case 4:
            case 5:
            case 6:
                generateRain(scenario);
                break;
            case 7:
                generateSnowfall(scenario);
                break;
            case 8:
                generateHeatwave(scenario);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                generateExtremeWeather(scenario);
                break;
        }
    }

    private void generateSummerConditions(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
                clearSkies(scenario);
                break;
            case 3:
            case 4:
            case 5:
                break;
            case 6:
            case 7:
                generateRain(scenario);
                break;
            case 8:
                generateHeatwave(scenario);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                generateExtremeWeather(scenario);
                break;
        }
    }

    private void generateWinterConditions(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
                clearSkies(scenario);
                break;
            case 3:
            case 4:
            case 5:
                break;
            case 6:
                generateRain(scenario);
                break;
            case 7:
            case 8:
                generateSnowfall(scenario);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                generateExtremeWeather(scenario);
                break;
        }
    }

    private void generateEquatorialConditions(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
                clearSkies(scenario);
                break;
            case 3:
                break;
            case 4:
            case 5:
            case 6:
                generateRain(scenario);
                break;
            case 7:
            case 8:
                generateHeatwave(scenario);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                generateExtremeWeather(scenario);
                break;
        }
    }

    private void generateAridConditions(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
                clearSkies(scenario);
                break;
            case 2:
            case 3:
                break;
            case 4:
                scenario.setWeather(PlanetaryConditions);
            case 5:
                break;
            case 6:
                generateRain(scenario);
                break;
            case 7:
            case 8:
                generateSnowfall(scenario);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                generateExtremeWeather(scenario);
                break;
        }
    }

    private void generateRain(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
                break;
            case 3:
            case 4:
            case 5:
                scenario.setWind(PlanetaryConditions.WI_LIGHT_GALE);
                break;
            case 6:
            case 7:
                scenario.setWind(PlanetaryConditions.WI_MOD_GALE);
                break;
            case 8:
                scenario.setWind(PlanetaryConditions.WI_STRONG_GALE);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                scenario.setWind(PlanetaryConditions.WI_STORM);
                break;
        }
    }

    private void generateSnowfall(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
                break;
            case 3:
            case 4:
            case 5:
                scenario.setWind(PlanetaryConditions.WI_LIGHT_GALE);
                break;
            case 6:
            case 7:
                scenario.setWind(PlanetaryConditions.WI_MOD_GALE);
                break;
            case 8:
                scenario.setWind(PlanetaryConditions.WI_STRONG_GALE);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                scenario.setWind(PlanetaryConditions.WI_STORM);
                break;
        }
    }

    private void generateExtremeWeather(final AtBScenario scenario) {
        switch (Compute.randomInt(10)) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                break;
            case 7:
            case 8:
                scenario.setFog(PlanetaryConditions.FOG_LIGHT);
                break;
            case 9:
            default: // useless default, but required for Java parsing
                scenario.setFog(PlanetaryConditions.FOG_HEAVY);
                break;
        }
    }
}
