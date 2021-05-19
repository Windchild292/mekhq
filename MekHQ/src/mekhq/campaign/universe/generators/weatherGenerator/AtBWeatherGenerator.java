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

public class AtBWeatherGenerator extends AbstractWeatherGenerator {
    //region Constructors
    public AtBWeatherGenerator() {
        super(WeatherGenerationMethod.ATB);
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

    private void generateRain(final AtBScenario scenario) {
        switch (Compute.randomInt(6)) {
            case 0:
            case 1:
            case 2:
                scenario.setWeather(PlanetaryConditions.WE_LIGHT_RAIN);
                break;
            case 3:
            case 4:
                scenario.setWeather(PlanetaryConditions.WE_MOD_RAIN);
                break;
            case 5:
            default:
                scenario.setWeather(PlanetaryConditions.WE_HEAVY_RAIN);
                break;
        }
    }

    private void generateSnowfall(final AtBScenario scenario) {
        switch (Compute.randomInt(6)) {
            case 0:
            case 1:
            case 2:
                scenario.setWeather(PlanetaryConditions.WE_LIGHT_SNOW);
                break;
            case 3:
            case 4:
                scenario.setWeather(PlanetaryConditions.WE_MOD_SNOW);
                break;
            case 5:
            default:
                scenario.setWeather(PlanetaryConditions.WE_HEAVY_SNOW);
                break;
        }
    }


    private void generateGale(final AtBScenario scenario) {
        switch (Compute.randomInt(6)) {
            case 0:
            case 1:
            case 2:
                scenario.setWind(PlanetaryConditions.WI_LIGHT_GALE);
                break;
            case 3:
            case 4:
                scenario.setWind(PlanetaryConditions.WI_MOD_GALE);
                break;
            case 5:
            default:
                scenario.setWind(PlanetaryConditions.WI_STRONG_GALE);
                break;
        }
    }

    private void generateStorm(final AtBScenario scenario) {
        switch (Compute.randomInt(6)) {
            case 0:
                scenario.setWind(PlanetaryConditions.WI_STORM);
                break;
            case 1:
                scenario.setWeather(PlanetaryConditions.WE_DOWNPOUR);
                break;
            case 2:
                scenario.setWeather(PlanetaryConditions.WE_SLEET);
                break;
            case 3:
                scenario.setWeather(PlanetaryConditions.WE_ICE_STORM);
                break;
            case 4:
                scenario.setWind(PlanetaryConditions.WI_TORNADO_F13);
                break;
            case 5:
            default:
                scenario.setWind(PlanetaryConditions.WI_TORNADO_F4);
                break;
        }
    }

    private void generateFog(final AtBScenario scenario) {
        switch (Compute.randomInt(6)) {
            case 0:
            case 1:
            case 2:
            case 3:
                scenario.setFog(PlanetaryConditions.FOG_LIGHT);
                break;
            case 4:
            case 5:
            default:
                scenario.setFog(PlanetaryConditions.FOG_HEAVY);
                break;
        }
    }
}
