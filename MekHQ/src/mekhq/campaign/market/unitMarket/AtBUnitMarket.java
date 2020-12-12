/*
 * AtBUnitMarket.java
 *
 * Copyright (c) 2014 Carl Spain. All rights reserved.
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.market.unitMarket;

import java.util.Set;

import mekhq.campaign.market.enums.UnitMarketMarketType;
import mekhq.campaign.market.enums.UnitMarketMethod;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.client.ratgenerator.MissionRole;
import megamek.common.Compute;
import megamek.common.EntityWeightClass;
import megamek.common.MechSummary;
import megamek.common.UnitType;
import mekhq.MekHQ;
import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.rating.IUnitRating;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.IUnitGenerator;
import mekhq.campaign.universe.RandomFactionGenerator;
import mekhq.campaign.universe.UnitGeneratorParameters;

/**
 * Generates units available for sale.
 *
 * @author Neoancient
 */
public class AtBUnitMarket extends AbstractUnitMarket {
    //region Variable Declarations
    private static final long serialVersionUID = -2085002038852079114L;
    //endregion Variable Declarations

    //region Constructors
    public AtBUnitMarket() {
        super(UnitMarketMethod.ATB_MONTHLY);
    }
    //endregion Constructors

    @Override
    public void generateUnitOffers(Campaign campaign) {
        if (campaign.getLocalDate().getDayOfMonth() == 1) {
            getOffers().clear();

            AtBContract contract = null;
            for (Mission m : campaign.getMissions()) {
                if (m.isActive() && m instanceof AtBContract) {
                    contract = (AtBContract)m;
                    break;
                }
            }

            addOffers(campaign, Compute.d6() - 2, UnitMarketMarketType.OPEN, UnitType.MEK,
                    null, IUnitRating.DRAGOON_F, 7);
            addOffers(campaign, Compute.d6() - 1, UnitMarketMarketType.OPEN, UnitType.TANK,
                    null, IUnitRating.DRAGOON_F, 7);
            addOffers(campaign, Compute.d6() - 2, UnitMarketMarketType.OPEN, UnitType.AERO,
                    null, IUnitRating.DRAGOON_F, 7);

            if (contract != null) {
                addOffers(campaign, Compute.d6() - 3, UnitMarketMarketType.EMPLOYER, UnitType.MEK,
                        contract.getEmployerCode(), IUnitRating.DRAGOON_D, 7);
                addOffers(campaign, Compute.d6() - 2, UnitMarketMarketType.EMPLOYER,
                        UnitType.TANK, contract.getEmployerCode(),
                        IUnitRating.DRAGOON_D, 7);
                addOffers(campaign, Compute.d6() - 3, UnitMarketMarketType.EMPLOYER, UnitType.AERO,
                        contract.getEmployerCode(), IUnitRating.DRAGOON_D, 7);
            }

            if (!campaign.getFaction().isClan()) {
                addOffers(campaign, Compute.d6(3) - 9, UnitMarketMarketType.MERCENARY, UnitType.MEK,
                        "MERC", IUnitRating.DRAGOON_C, 5);
                addOffers(campaign, Compute.d6(3) - 6, UnitMarketMarketType.MERCENARY, UnitType.TANK,
                        "MERC", IUnitRating.DRAGOON_C, 5);
                addOffers(campaign, Compute.d6(3) - 9, UnitMarketMarketType.MERCENARY, UnitType.AERO,
                        "MERC", IUnitRating.DRAGOON_C, 5);
            }

            if (campaign.getUnitRatingMod() >= IUnitRating.DRAGOON_B) {
                Set<Faction> factions = campaign.getCurrentSystem().getFactionSet(campaign.getLocalDate());
                String faction = Utilities.getRandomItem(factions).getShortName();
                if (campaign.getFaction().isClan() || !Faction.getFaction(faction).isClan()) {
                    addOffers(campaign, Compute.d6() - 3, UnitMarketMarketType.FACTORY, UnitType.MEK,
                            faction, IUnitRating.DRAGOON_A, 6);
                    addOffers(campaign, Compute.d6() - 2, UnitMarketMarketType.FACTORY, UnitType.TANK,
                            faction, IUnitRating.DRAGOON_A, 6);
                    addOffers(campaign, Compute.d6() - 3, UnitMarketMarketType.FACTORY, UnitType.AERO,
                            faction, IUnitRating.DRAGOON_A, 6);
                }
            }

            if (!campaign.getFaction().isClan()) {
                addOffers(campaign, Compute.d6(2) - 6, UnitMarketMarketType.BLACK_MARKET, UnitType.MEK,
                        null, IUnitRating.DRAGOON_C, 6);
                addOffers(campaign, Compute.d6(2) - 4, UnitMarketMarketType.BLACK_MARKET, UnitType.TANK,
                        null, IUnitRating.DRAGOON_C, 6);
                addOffers(campaign, Compute.d6(2) - 6, UnitMarketMarketType.BLACK_MARKET, UnitType.AERO,
                        null, IUnitRating.DRAGOON_C, 6);
            }

            if (campaign.getCampaignOptions().getUnitMarketReportRefresh()) {
                campaign.addReport("<a href='UNIT_MARKET'>Unit market updated</a>");
            }
        }
    }

    @Override
    protected void addOffers(Campaign campaign, int num, UnitMarketMarketType market, int unitType,
                           String faction, int quality, int priceTarget) {
        if (faction == null) {
            faction = RandomFactionGenerator.getInstance().getEmployer();
        }
        if (faction == null) {
            faction = campaign.getFactionCode();
            market = UnitMarketMarketType.EMPLOYER;
        }

        UnitGeneratorParameters params = new UnitGeneratorParameters();
        params.setFaction(faction);
        params.setYear(campaign.getGameYear());
        params.setUnitType(unitType);
        params.setQuality(quality);

        for (int i = 0; i < num; i++) {
            params.setWeightClass(getRandomWeight(unitType, faction,
                    campaign.getCampaignOptions().getRegionalMechVariations()));
            params.clearMissionRoles();

            MechSummary ms;
            if (unitType == UnitType.TANK) {
                params.setMovementModes(IUnitGenerator.MIXED_TANK_VTOL);
                params.addMissionRole(MissionRole.MIXED_ARTILLERY);

            } else {
                params.clearMovementModes();
            }
            ms = campaign.getUnitGenerator().generate(params);
            if (ms != null) {
                if (campaign.getCampaignOptions().limitByYear()
                        && (campaign.getGameYear() < ms.getYear())) {
                    continue;
                }
                if ((campaign.getCampaignOptions().allowClanPurchases() && ms.isClan())
                        || (campaign.getCampaignOptions().allowISPurchases() && !ms.isClan())) {
                    int percent = 100 - (Compute.d6(2) - priceTarget) * 5;
                    /*Some RATs, particularly ASF, group multiple weight classes together
                     * so we need to get the actual weight class from the generated unit
                     * (-1 because EntityWeightClass starts with ultra-light).
                     */
                    addSingleUnit(campaign, market, unitType, faction, quality, percent);
                }
            }
        }
    }

    //region Random Weight
    @Override
    public int generateWeight(Campaign campaign, int unitType, String faction) {
        return getRandomWeight(unitType, faction, campaign.getCampaignOptions().getRegionalMechVariations());
    }

    public static int getRandomWeight(int unitType, String faction, boolean regionalVariations) {
        if (unitType == UnitType.AERO) {
            return getRandomAeroWeight();
        } else if ((unitType == UnitType.MEK) && regionalVariations) {
            return getRegionalMechWeight(faction);
        } else {
            return getRandomMechWeight();
        }
    }

    public static int getRandomMechWeight() {
        final int roll = Compute.randomInt(10);
        if (roll <= 2) {
            return EntityWeightClass.WEIGHT_LIGHT;
        } else if (roll <= 6) {
            return EntityWeightClass.WEIGHT_MEDIUM;
        } else if (roll <= 8) {
            return EntityWeightClass.WEIGHT_HEAVY;
        } else {
            return EntityWeightClass.WEIGHT_ASSAULT;
        }
    }

    public static int getRegionalMechWeight(String faction) {
        int roll = Compute.randomInt(100);
        switch (faction) {
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

    public static int getRandomAeroWeight() {
        int roll = Compute.randomInt(8);
        if (roll <= 2) {
            return EntityWeightClass.WEIGHT_LIGHT;
        } else if (roll <= 6) {
            return EntityWeightClass.WEIGHT_MEDIUM;
        } else {
            return EntityWeightClass.WEIGHT_HEAVY;
        }
    }
    //endregion Random Weight

    //region File I/O
    public static AbstractUnitMarket generateInstanceFromXML(Node wn) {
        AbstractUnitMarket retVal = new AtBUnitMarket();

        try {
            NodeList nl = wn.getChildNodes();

            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);

                if (wn2.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                if (!wn2.getNodeName().equalsIgnoreCase("offer")) {
                    MekHQ.getLogger().error("Unknown node type not loaded in offer nodes: " + wn2.getNodeName());
                    continue;
                }

                retVal.getOffers().add(UnitMarketOffer.generateInstanceFromXML(wn2));
            }
        } catch (Exception ex) {
            MekHQ.getLogger().error(ex);
        }

        return retVal;
    }
    //endregion File I/O
}
