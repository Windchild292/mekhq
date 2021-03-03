/*
 * AtBUnitMarket.java
 *
 * Copyright (c) 2020-2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.market;

import megamek.common.annotations.Nullable;
import mekhq.campaign.market.enums.UnitMarketMethod;
import mekhq.campaign.market.enums.UnitMarketType;

import megamek.client.ratgenerator.MissionRole;
import megamek.common.Compute;
import megamek.common.EntityWeightClass;
import megamek.common.UnitType;
import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.rating.IUnitRating;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.Factions;
import mekhq.campaign.universe.IUnitGenerator;
import mekhq.campaign.universe.RandomFactionGenerator;
import mekhq.campaign.universe.UnitGeneratorParameters;

public class AtBUnitMarket extends AbstractUnitMarket {
    //region Variable Declarations
    private static final long serialVersionUID = -2085002038852079114L;
    //endregion Variable Declarations

    //region Constructors
    public AtBUnitMarket() {
        super(UnitMarketMethod.ATB_MONTHLY);
    }
    //endregion Constructors

    //region Process New Day
    /**
     * This market runs monthly, so it only executes removal and generation on the first day of the
     * month
     * @param campaign the campaign to process the Unit Market new day using
     */
    @Override
    public void processNewDay(final Campaign campaign) {
        if (campaign.getLocalDate().getDayOfMonth() == 1) {
            removeUnitOffers(campaign);
            generateUnitOffers(campaign);
        }
    }

    //region Generate Offers
    /**
     * This generates Unit Offers as per the AtB Unit Market rules
     * @param campaign the campaign to generate the unit offers for
     */
    @Override
    public void generateUnitOffers(final Campaign campaign) {
        AtBContract contract = null;
        for (Mission m : campaign.getMissions()) {
            if (m.isActive() && (m instanceof AtBContract)) {
                contract = (AtBContract) m;
                break;
            }
        }

        addOffers(campaign, Compute.d6() - 2, UnitMarketType.OPEN, UnitType.MEK,
                null, IUnitRating.DRAGOON_F, 7);
        addOffers(campaign, Compute.d6() - 1, UnitMarketType.OPEN, UnitType.TANK,
                null, IUnitRating.DRAGOON_F, 7);
        addOffers(campaign, Compute.d6() - 2, UnitMarketType.OPEN, UnitType.AERO,
                null, IUnitRating.DRAGOON_F, 7);

        if (contract != null) {
            addOffers(campaign, Compute.d6() - 3, UnitMarketType.EMPLOYER, UnitType.MEK,
                    contract.getEmployerFaction(), IUnitRating.DRAGOON_D, 7);
            addOffers(campaign, Compute.d6() - 2, UnitMarketType.EMPLOYER, UnitType.TANK,
                    contract.getEmployerFaction(), IUnitRating.DRAGOON_D, 7);
            addOffers(campaign, Compute.d6() - 3, UnitMarketType.EMPLOYER, UnitType.AERO,
                    contract.getEmployerFaction(), IUnitRating.DRAGOON_D, 7);
        }

        if (!campaign.getFaction().isClan()) {
            addOffers(campaign, Compute.d6(3) - 9, UnitMarketType.MERCENARY,
                    UnitType.MEK, Factions.getInstance().getFaction("MERC"),
                    IUnitRating.DRAGOON_C, 5);
            addOffers(campaign, Compute.d6(3) - 6, UnitMarketType.MERCENARY,
                    UnitType.TANK, Factions.getInstance().getFaction("MERC"),
                    IUnitRating.DRAGOON_C, 5);
            addOffers(campaign, Compute.d6(3) - 9, UnitMarketType.MERCENARY,
                    UnitType.AERO, Factions.getInstance().getFaction("MERC"),
                    IUnitRating.DRAGOON_C, 5);
        }

        if (campaign.getUnitRatingMod() >= IUnitRating.DRAGOON_B) {
            final Faction faction = Utilities.getRandomItem(campaign.getCurrentSystem()
                    .getFactionSet(campaign.getLocalDate()));
            if (campaign.getFaction().isClan() || !faction.isClan()) {
                addOffers(campaign, Compute.d6() - 3, UnitMarketType.FACTORY, UnitType.MEK,
                        faction, IUnitRating.DRAGOON_A, 6);
                addOffers(campaign, Compute.d6() - 2, UnitMarketType.FACTORY, UnitType.TANK,
                        faction, IUnitRating.DRAGOON_A, 6);
                addOffers(campaign, Compute.d6() - 3, UnitMarketType.FACTORY, UnitType.AERO,
                        faction, IUnitRating.DRAGOON_A, 6);
            }
        }

        if (!campaign.getFaction().isClan()) {
            addOffers(campaign, Compute.d6(2) - 6, UnitMarketType.BLACK_MARKET, UnitType.MEK,
                    null, IUnitRating.DRAGOON_C, 6);
            addOffers(campaign, Compute.d6(2) - 4, UnitMarketType.BLACK_MARKET, UnitType.TANK,
                    null, IUnitRating.DRAGOON_C, 6);
            addOffers(campaign, Compute.d6(2) - 6, UnitMarketType.BLACK_MARKET, UnitType.AERO,
                    null, IUnitRating.DRAGOON_C, 6);
        }

        writeRefreshReport(campaign);
    }

    @Override
    protected void addOffers(final Campaign campaign, final int num, UnitMarketType market,
                             final int unitType, @Nullable Faction faction, final int quality,
                             final int priceTarget) {
        if (faction == null) {
            faction = RandomFactionGenerator.getInstance().getEmployerFaction();
        }

        if (faction == null) {
            faction = campaign.getFaction();
            market = UnitMarketType.EMPLOYER;
        }

        final UnitGeneratorParameters parameters = createUnitGeneratorParameters(campaign, unitType, faction, quality);
        for (int i = 0; i < num; i++) {
            parameters.setWeightClass(generateWeight(campaign, unitType, faction));
            parameters.clearMissionRoles();
            if (unitType == UnitType.TANK) {
                parameters.setMovementModes(IUnitGenerator.MIXED_TANK_VTOL);
                parameters.addMissionRole(MissionRole.MIXED_ARTILLERY);
            } else {
                parameters.clearMovementModes();
            }
            final int percent = 100 - (Compute.d6(2) - priceTarget) * 5;
            addSingleUnit(campaign, market, unitType, parameters, percent);
        }
    }

    //region Random Weight
    /**
     * This generates a random weight using the static weight generation methods in this market
     * @param campaign the campaign to generate the unit weight based on
     * @param unitType the unit type to determine the format of weight to generate
     * @param faction the faction to generate the weight for
     * @return the generated weight
     */
    @Override
    public int generateWeight(final Campaign campaign, final int unitType, final Faction faction) {
        return getRandomWeight(campaign, unitType, faction);
    }

    /**
     * @param campaign the campaign to generate the unit weight based on
     * @param unitType the unit type to determine the format of weight to generate
     * @param faction the faction to generate the weight for
     * @return the generated weight
     */
    public static int getRandomWeight(final Campaign campaign, final int unitType, final Faction faction) {
        if (unitType == UnitType.AERO) {
            return getRandomAerospaceWeight();
        } else if ((unitType == UnitType.MEK) && campaign.getCampaignOptions().useUnitMarketRegionalMechVariations()) {
            return getRegionalMechWeight(faction);
        } else {
            return getRandomMechWeight();
        }
    }

    /**
     * @return the generated weight for a BattleMech
     */
    public static int getRandomMechWeight() {
        int roll = Compute.randomInt(10);
        if (roll < 3) {
            return EntityWeightClass.WEIGHT_LIGHT;
        } else if (roll < 7) {
            return EntityWeightClass.WEIGHT_MEDIUM;
        } else if (roll < 9) {
            return EntityWeightClass.WEIGHT_HEAVY;
        } else {
            return EntityWeightClass.WEIGHT_ASSAULT;
        }
    }

    /**
     * @param faction the faction to determine the regional BattleMech weight for
     * @return the generated weight for a BattleMech
     */
    public static int getRegionalMechWeight(final Faction faction) {
        int roll = Compute.randomInt(100);
        switch (faction.getShortName()) {
            case "DC":
                if (roll < 40) {
                    return EntityWeightClass.WEIGHT_LIGHT;
                } else if (roll < 60) {
                    return EntityWeightClass.WEIGHT_MEDIUM;
                } else if (roll < 90) {
                    return EntityWeightClass.WEIGHT_HEAVY;
                } else {
                    return EntityWeightClass.WEIGHT_ASSAULT;
                }
            case "LA":
                if (roll < 20) {
                    return EntityWeightClass.WEIGHT_LIGHT;
                } else if (roll < 50) {
                    return EntityWeightClass.WEIGHT_MEDIUM;
                } else if (roll < 85) {
                    return EntityWeightClass.WEIGHT_HEAVY;
                } else {
                    return EntityWeightClass.WEIGHT_ASSAULT;
                }
            case "FWL":
                if (roll < 30) {
                    return EntityWeightClass.WEIGHT_LIGHT;
                } else if (roll < 70) {
                    return EntityWeightClass.WEIGHT_MEDIUM;
                } else if (roll < 92) {
                    return EntityWeightClass.WEIGHT_HEAVY;
                } else {
                    return EntityWeightClass.WEIGHT_ASSAULT;
                }
            default:
                if (roll < 30) {
                    return EntityWeightClass.WEIGHT_LIGHT;
                } else if (roll < 70) {
                    return EntityWeightClass.WEIGHT_MEDIUM;
                } else if (roll < 90) {
                    return EntityWeightClass.WEIGHT_HEAVY;
                } else {
                    return EntityWeightClass.WEIGHT_ASSAULT;
                }
        }
    }

    /**
     * @return the generated random weight for an Aerospace Fighter
     */
    public static int getRandomAerospaceWeight() {
        int roll = Compute.randomInt(8);
        if (roll < 3) {
            return EntityWeightClass.WEIGHT_LIGHT;
        } else if (roll < 7) {
            return EntityWeightClass.WEIGHT_MEDIUM;
        } else {
            return EntityWeightClass.WEIGHT_HEAVY;
        }
    }
    //endregion Random Weight
    //endregion Offer Generation

    //region Offer Removal
    /**
     * The AtB Unit Market clears all offers from the unit market each month
     * @param campaign the campaign to use in determining the offers to remove
     */
    @Override
    public void removeUnitOffers(final Campaign campaign) {
        getOffers().clear();
    }
    //endregion Offer Removal
    //endregion Process New Day
}
