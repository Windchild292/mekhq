/*
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
package mekhq.campaign.market.contractMarket;

import megamek.common.Compute;
import megamek.common.annotations.Nullable;
import megamek.common.enums.SkillLevel;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.Version;
import mekhq.campaign.Campaign;
import mekhq.campaign.JumpPath;
import mekhq.campaign.market.enums.AtBContractMarketClause;
import mekhq.campaign.market.enums.ContractMarketMethod;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.enums.AtBContractType;
import mekhq.campaign.mission.enums.ContractCommandRights;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.personnel.enums.PersonnelRole;
import mekhq.campaign.rating.IUnitRating;
import mekhq.campaign.universe.*;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AtBMonthlyContractMarket extends AbstractContractMarket {
    //region Variable Declarations
    private static final long serialVersionUID = 1303462872220110093L;

    /**
     * An arbitrary maximum number of attempts to find a random employer faction that
     * is not a Mercenary.
     */
    private final static int MAXIMUM_ATTEMPTS_TO_FIND_NON_MERC_EMPLOYER = 20;

    private Map<Contract, ClauseModifiers> clauseModifiers;

    /*
     * It is possible to call addFollowup more than once for the same contract by canceling the
     * dialog and running it again; This is the easiest place to track it to prevent multiple
     * followup contracts.
     * key: Followup Contract
     * value: Main Contract
     */
    private Map<Contract, Contract> followupContracts;
    //endregion Variable Declarations

    //region Constructors
    public AtBMonthlyContractMarket() {
        super(ContractMarketMethod.ATB_MONTHLY);
        setClauseModifiers(new HashMap<>());
        setFollowupContracts(new HashMap<>());
    }
    //endregion Constructors

    //region Getters/Setters
    @Override
    public void removeContract(final Contract contract) {
        super.removeContract(contract);
        getClauseModifiers().remove(contract);
        getFollowupContracts().remove(contract);
    }

    public Map<Contract, ClauseModifiers> getClauseModifiers() {
        return clauseModifiers;
    }

    public void setClauseModifiers(final Map<Contract, ClauseModifiers> clauseModifiers) {
        this.clauseModifiers = clauseModifiers;
    }

    public Map<Contract, Contract> getFollowupContracts() {
        return followupContracts;
    }

    public void setFollowupContracts(final Map<Contract, Contract> followupContracts) {
        this.followupContracts = followupContracts;
    }
    //endregion Getters/Setters
    //region Process New Day
    /**
     * This market runs monthly, so it only executes removal and generation on the first day of the
     * month
     * @param campaign the campaign to process the Contract Market new day using
     */
    @Override
    public void processNewDay(final Campaign campaign) {
        if (campaign.getLocalDate().getDayOfMonth() == 1) {
            removeContractOffers(campaign);
            generateContractOffers(campaign);
        }
    }

    //region Generate Contracts
    @Override
    public void generateContractOffers(final Campaign campaign, final int unitRatingModifier,
                                       final int numberOfContracts) {
        campaign.getActiveAtBContracts().forEach(contract ->
                checkForSubcontracts(campaign, contract, unitRatingModifier));
        /*
         * If located on a faction's capital (interpreted as the starting planet for that faction),
         * generate one contract offer for that faction.
         */
        for (final Faction faction : campaign.getCurrentSystem().getFactionSet(campaign.getLocalDate())) {
            if (faction.getStartingPlanet(campaign.getLocalDate()).equals(campaign.getCurrentSystem().getId())
                    && RandomFactionGenerator.getInstance().getEmployerSet().contains(faction)) {
                final Contract contract = generateAtBContract(campaign, faction, unitRatingModifier);
                if (contract != null) {
                    getContracts().add(contract);
                    break;
                }
            }
        }

        for (int i = 0; i < numberOfContracts; i++) {
            final Contract contract = generateAtBContract(campaign, unitRatingModifier);
            if (contract != null) {
                getContracts().add(contract);
            }
        }
        writeRefreshReport(campaign);
    }

    @Override
    protected int determineNumberOfOffers(final Campaign campaign, final int unitRatingModifier) {
        int numberOfContracts = Compute.d6() - 4 + unitRatingModifier;

        final Set<Faction> currentFactions = campaign.getCurrentSystem().getFactionSet(campaign.getLocalDate());

        if (currentFactions.stream().noneMatch(faction -> faction.isISMajorOrSuperPower() || faction.isClan())) {
            numberOfContracts--;
        }

        boolean inBackwater = true;
        if (currentFactions.size() > 1) {
            // More than one faction, all must be periphery to be a backwater
            inBackwater = currentFactions.stream().allMatch(Faction::isPeriphery);
        } else if (!currentFactions.isEmpty()) {
            // Just one faction. Are there any others nearby?
            final Faction onlyFaction = currentFactions.iterator().next();
            if (!onlyFaction.isPeriphery()) {
                for (final PlanetarySystem key : Systems.getInstance().getNearbySystems(campaign.getCurrentSystem(), 30)) {
                    inBackwater = key.getFactionSet(campaign.getLocalDate()).stream()
                            .allMatch(onlyFaction::equals);
                    if (!inBackwater) {
                        break;
                    }
                }
            }
        } else {
            MekHQ.getLogger().warning("Unable to find any factions around "
                    + campaign.getCurrentSystem().getName(campaign.getLocalDate()) + " on "
                    + campaign.getLocalDate());
        }

        if (inBackwater) {
            numberOfContracts--;
        }

        if (campaign.getFaction().isMercenary() || campaign.getFaction().isPirate()) {
            if (campaign.getAtBConfig().isHiringHall(campaign.getCurrentSystem().getId(), campaign.getLocalDate())) {
                numberOfContracts++;
                /*
                 * Though the rules do not state these modifiers are mutually exclusive, the fact
                 * that the distance of Galatea from a border means that it has no advantage for
                 * Mercenaries over border worlds. Common sense dictates that worlds with hiring
                 * halls should not be subject to the -1 for backwater/interior.
                 */
                if (inBackwater) {
                    numberOfContracts++;
                }
            }
        } else {
            // Per IOps Beta, government units determine number of contracts as on a system with a great hall
            numberOfContracts++;
        }

        return numberOfContracts;
    }
    //endregion Generate Contracts

    //region Contract Removal
    /**
     * The AtB Contract Market clears all offers from the Contract Market each month
     * @param campaign the campaign to use in determining the offers to remove
     */
    @Override
    public void removeContractOffers(final Campaign campaign) {
        getContracts().clear();
        getClauseModifiers().clear();
        getFollowupContracts().clear();
    }
    //endregion Contract Removal
    //endregion Process New Day

    public int getRerollsUsed(final Contract contract, final AtBContractMarketClause clause) {
        return (getClauseModifiers().get(contract) == null) ? 0
                : getClauseModifiers().get(contract).getRerollsUsed().get(clause);
    }

    public void rerollClause(final Campaign campaign, final Contract contract,
                             final AtBContractMarketClause clause) {
        if (getClauseModifiers().get(contract) == null) {
            return;
        }

        switch (clause) {
            case COMMAND:
                rollCommandClause(contract, getClauseModifiers().get(contract).getModifiers().get(clause));
                break;
            case SALVAGE:
                rollSalvageClause(contract, getClauseModifiers().get(contract).getModifiers().get(clause));
                break;
            case TRANSPORT:
                rollTransportClause(contract, getClauseModifiers().get(contract).getModifiers().get(clause));
                break;
            case SUPPORT:
                rollSupportClause(contract, getClauseModifiers().get(contract).getModifiers().get(clause));
                break;
        }
        getClauseModifiers().get(contract).getRerollsUsed().get(clause);
        contract.calculateContract(campaign);
    }

    private void checkForSubcontracts(final Campaign campaign, final AtBContract contract,
                                      final int unitRatingModifier) {
        if (contract.getContractType().isGarrisonType()) {
            final int numSubcontracts = Math.toIntExact(campaign.getAtBContracts().stream()
                    .filter(subcontract -> contract.equals(subcontract.getParentContract())).count());

            for (int i = numSubcontracts; i < unitRatingModifier - 1; i++) {
                final int roll = Compute.d6(2);
                if (roll > 9) {
                    final AtBContract subcontract = generateAtBSubcontract(campaign, contract, unitRatingModifier);
                    if (subcontract != null) {
                        getContracts().add(subcontract);
                    }
                }
            }
        }
    }

    @Override
    public AtBContract addContract(final Campaign campaign) {
        final AtBContract contract = generateAtBContract(campaign, campaign.getUnitRatingMod());
        if (contract != null) {
            getContracts().add(contract);
        }
        return contract;
    }

    /**
     * If no suitable planet can be found or no jump path to the planet can be calculated after
     * the indicated number of retries, this will return null.
     */
    private @Nullable AtBContract generateAtBContract(final Campaign campaign,
                                                      final int unitRatingModifier) {
        if (campaign.getFaction().isMercenary()) {
            if (campaign.getRetainerEmployerCode() == null) {
                for (int i = 0; i < campaign.getCampaignOptions().getMaximumContractGenerationRetries(); i++) {
                    final AtBContract contract = generateAtBContract(campaign,
                            RandomFactionGenerator.getInstance().getEmployerFaction(),
                            unitRatingModifier, 1);
                    if (contract != null) {
                        return contract;
                    }
                }
                return null;
            } else {
                return generateAtBContract(campaign, campaign.getRetainerEmployer(), unitRatingModifier);
            }
        } else {
            return generateAtBContract(campaign, campaign.getFaction(), unitRatingModifier);
        }
    }

    private @Nullable AtBContract generateAtBContract(final Campaign campaign,
                                                      final @Nullable Faction employer,
                                                      final int unitRatingModifier) {
        return generateAtBContract(campaign, employer, unitRatingModifier,
                campaign.getCampaignOptions().getMaximumContractGenerationRetries());
    }

    private @Nullable AtBContract generateAtBContract(final Campaign campaign,
                                                      @Nullable Faction employer,
                                                      final int unitRatingModifier, final int retries) {
        if (employer == null) {
            MekHQ.getLogger().warning("Could not generate an AtB Contract because there was no employer!");
            return null;
        } else if (retries <= 0) {
            MekHQ.getLogger().warning("Could not generate an AtB Contract because we ran out of retries!");
            return null;
        }

        final AtBContract contract = new AtBContract("UnnamedContract");
        incrementLastId();
        contract.setId(getLastId());

        if (employer.isMercenary()) {
            contract.setMercSubcontract(true);
            for (int attempts = 0; attempts < MAXIMUM_ATTEMPTS_TO_FIND_NON_MERC_EMPLOYER; attempts++) {
                employer = RandomFactionGenerator.getInstance().getEmployerFaction();
                if ((employer != null) && !employer.isMercenary()) {
                    break;
                }
            }

            if ((employer == null) || employer.isMercenary()) {
                MekHQ.getLogger().warning("Could not generate an AtB Contract because we could not find a non-MERC employer!");
                return null;
            }
        }
        contract.setEmployerCode(employer.getShortName(), campaign.getGameYear());
        contract.setContractType(findAtBContractType(unitRatingModifier,
                contract.getEmployerFaction().isISMajorOrSuperPower()));

        if (contract.getContractType().isPirateHunting()) {
            contract.setEnemyCode("PIR");
        } else if (contract.getContractType().isRiotDuty()) {
            contract.setEnemyCode("REB");
        } else {
            contract.setEnemyCode(RandomFactionGenerator.getInstance().getEnemy(contract.getEmployerFaction(),
                    contract.getContractType().isGarrisonType()));
        }

        if (contract.getContractType().isGarrisonDuty() && contract.getEnemy().isRebel()) {
            contract.setContractType(AtBContractType.RIOT_DUTY);
        }

        /*
         * Addition to AtB rules: factions which are generally neutral
         * (ComStar, Mercs not under contract) are more likely to have garrison-type
         * contracts and less likely to have battle-type contracts unless at war.
         */
        if (RandomFactionGenerator.getInstance().getFactionHints().isNeutral(employer)
                && !RandomFactionGenerator.getInstance().getFactionHints().isAtWarWith(
                        employer, contract.getEnemy(), campaign.getLocalDate())) {
            if (contract.getContractType().isPlanetaryAssault()) {
                contract.setContractType(AtBContractType.GARRISON_DUTY);
            } else if (contract.getContractType().isReliefDuty()) {
                contract.setContractType(AtBContractType.SECURITY_DUTY);
            }
        }

        // TODO I make no sense whatsoever
        final boolean isAttacker = !contract.getContractType().isGarrisonType()
                && (contract.getContractType().isReliefDuty() && (Compute.d6() < 4))
                || contract.getEnemy().isRebel();
        if (isAttacker) {
            contract.setSystemId(RandomFactionGenerator.getInstance().getMissionTarget(
                    contract.getEmployerCode(), contract.getEnemyCode()));
        } else {
            contract.setSystemId(RandomFactionGenerator.getInstance().getMissionTarget(
                    contract.getEnemyCode(), contract.getEmployerCode()));
        }

        if (contract.getSystem() == null) {
            MekHQ.getLogger().warning("Could not find contract location for "
                    + contract.getEmployerCode() + " vs. " + contract.getEnemyCode());
            return generateAtBContract(campaign, employer, unitRatingModifier, retries - 1);
        }
        JumpPath jp = null;
        try {
            jp = contract.getJumpPath(campaign);
        } catch (Exception e) {
            // could not calculate jump path; leave jp null
            MekHQ.getLogger().warning("Could not calculate jump path to contract location: "
                    + contract.getSystem().getName(campaign.getLocalDate()), e);
        }

        if (jp == null) {
            return generateAtBContract(campaign, employer, unitRatingModifier, retries - 1);
        }

        setAllyRating(contract, isAttacker, campaign.getGameYear());
        setEnemyRating(contract, isAttacker, campaign.getGameYear());

        if (contract.getContractType().isCadreDuty()) {
            contract.setAllySkill(SkillLevel.GREEN);
            contract.setAllyQuality(IUnitRating.DRAGOON_F);
        }

        contract.calculateLength(campaign.getCampaignOptions().getVariableContractLength());
        setAtBContractClauses(campaign, contract, unitRatingModifier);
        contract.calculatePaymentMultiplier(campaign);
        contract.setPartsAvailabilityLevel(contract.getContractType().calculatePartsAvailabilityLevel());
        contract.initContractDetails(campaign);
        contract.calculateContract(campaign);
        contract.setName(String.format("%s - %s - %s %s",
                contract.getStartDate().format(DateTimeFormatter.ofPattern("yyyy")), employer,
                contract.getSystem().getName(contract.getStartDate()), contract.getContractType()));

        return contract;
    }

    protected @Nullable AtBContract generateAtBSubcontract(final Campaign campaign,
                                                           final AtBContract parent,
                                                           final int unitRatingModifier) {
        final AtBContract contract = new AtBContract("New Subcontract");
        contract.setEmployerCode(parent.getEmployerCode(), campaign.getGameYear());
        contract.setContractType(findAtBContractType(unitRatingModifier,
                contract.getEmployerFaction().isISMajorOrSuperPower()));

        if (contract.getContractType().isPirateHunting()) {
            contract.setEnemyCode("PIR");
        } else if (contract.getContractType().isRiotDuty()) {
            contract.setEnemyCode("REB");
        } else {
            contract.setEnemyCode(RandomFactionGenerator.getInstance().getEnemy(contract.getEmployerFaction(),
                    contract.getContractType().isGarrisonType()));
        }

        if (contract.getContractType().isGarrisonDuty() && contract.getEnemy().isRebel()) {
            contract.setContractType(AtBContractType.RIOT_DUTY);
        }

        contract.setParentContract(parent);
        contract.initContractDetails(campaign);
        incrementLastId();
        contract.setId(getLastId());

        /*
         * The AtB rules say to roll the enemy, but also that the subcontract
         * takes place in the same planet/sector. Rebels and pirates can
         * appear anywhere, but others should be limited to what's within a
         * jump.
         */

        /*
         * TODO : When MekHQ gets the capability of splitting the unit to
         * TODO : different locations, this restriction can be lessened or lifted.
         */
        if (!contract.getEnemy().isRebelOrPirate()) {
            if (Systems.getInstance().getNearbySystems(campaign.getCurrentSystem(), 30).stream()
                    .noneMatch(planet -> planet.getFactionSet(campaign.getLocalDate()).stream()
                            .anyMatch(faction -> faction.equals(contract.getEnemy())))) {
                contract.setEnemyCode(parent.getEnemyCode());
            }
        }

        // TODO : I don't make any sense
        final boolean isAttacker = !contract.getContractType().isGarrisonType()
                || (contract.getContractType().isReliefDuty() && (Compute.d6() < 4))
                || contract.getEnemy().isRebel();
        contract.setSystemId(parent.getSystemId());
        setAllyRating(contract, isAttacker, campaign.getGameYear());
        setEnemyRating(contract, isAttacker, campaign.getGameYear());

        if (contract.getContractType().isCadreDuty()) {
            contract.setAllySkill(SkillLevel.GREEN);
            contract.setAllyQuality(IUnitRating.DRAGOON_F);
        }
        contract.calculateLength(campaign.getCampaignOptions().getVariableContractLength());

        contract.setCommandRights(ContractCommandRights.values()[Math.max(parent.getCommandRights().ordinal() - 1, 0)]);
        contract.setSalvageExchange(parent.isSalvageExchange());
        contract.setSalvagePct(Math.max(parent.getSalvagePct() - 10, 0));
        contract.setStraightSupport(Math.max(parent.getStraightSupport() - 20, 0));
        if (parent.getBattleLossComp() <= 10) {
            contract.setBattleLossComp(0);
        } else if (parent.getBattleLossComp() <= 20) {
            contract.setBattleLossComp(10);
        } else {
            contract.setBattleLossComp(parent.getBattleLossComp() - 20);
        }
        contract.setTransportComp(100);

        contract.calculatePaymentMultiplier(campaign);
        contract.setPartsAvailabilityLevel(contract.getContractType().calculatePartsAvailabilityLevel());
        contract.calculateContract(campaign);
        contract.setName(String.format("%s - %s - %s Subcontract %s",
                contract.getStartDate().format(DateTimeFormatter.ofPattern("yyyy")), contract.getEmployer(),
                contract.getSystem().getName(parent.getStartDate()), contract.getContractType()));

        return contract;
    }

    public void addFollowup(final Campaign campaign, final AtBContract contract,
                            final int unitRatingModifier) {
        if (getFollowupContracts().containsValue(contract)) {
            return;
        }
        AtBContract followup = new AtBContract("Followup Contract");
        followup.setEmployerCode(contract.getEmployerCode(), campaign.getGameYear());
        followup.setEnemyCode(contract.getEnemyCode());
        followup.setSystemId(contract.getSystemId());
        switch (contract.getContractType()) {
            case DIVERSIONARY_RAID:
                followup.setContractType(AtBContractType.OBJECTIVE_RAID);
                break;
            case RECON_RAID:
                followup.setContractType(AtBContractType.PLANETARY_ASSAULT);
                break;
            case RIOT_DUTY:
                followup.setContractType(AtBContractType.GARRISON_DUTY);
                break;
            default:
                break;
        }
        followup.setAllySkill(contract.getAllySkill());
        followup.setAllyQuality(contract.getAllyQuality());
        followup.setEnemySkill(contract.getEnemySkill());
        followup.setEnemyQuality(contract.getEnemyQuality());
        followup.calculateLength(campaign.getCampaignOptions().getVariableContractLength());
        setAtBContractClauses(campaign, followup, unitRatingModifier);

        followup.calculatePaymentMultiplier(campaign);

        followup.setPartsAvailabilityLevel(followup.getContractType().calculatePartsAvailabilityLevel());

        followup.initContractDetails(campaign);
        followup.calculateContract(campaign);
        incrementLastId();
        followup.setId(getLastId());

        getContracts().add(followup);
        getFollowupContracts().put(followup, contract);
    }

    protected AtBContractType findAtBContractType(final int unitRatingModifier, final boolean majorPower) {
        final AtBContractType[][] table = {
                //col 0: IS Houses
                {AtBContractType.GUERRILLA_WARFARE, AtBContractType.RECON_RAID, AtBContractType.PIRATE_HUNTING,
                        AtBContractType.PLANETARY_ASSAULT, AtBContractType.OBJECTIVE_RAID, AtBContractType.OBJECTIVE_RAID,
                        AtBContractType.EXTRACTION_RAID, AtBContractType.RECON_RAID, AtBContractType.GARRISON_DUTY,
                        AtBContractType.CADRE_DUTY, AtBContractType.RELIEF_DUTY},
                //col 1: Others
                {AtBContractType.GUERRILLA_WARFARE, AtBContractType.RECON_RAID, AtBContractType.PLANETARY_ASSAULT,
                        AtBContractType.OBJECTIVE_RAID, AtBContractType.EXTRACTION_RAID, AtBContractType.PIRATE_HUNTING,
                        AtBContractType.SECURITY_DUTY, AtBContractType.OBJECTIVE_RAID, AtBContractType.GARRISON_DUTY,
                        AtBContractType.CADRE_DUTY, AtBContractType.DIVERSIONARY_RAID}
        };

        int roll = Compute.d6(2) + unitRatingModifier - IUnitRating.DRAGOON_C;
        roll = Math.min(Math.max(roll, 2), 12); // clamp the roll to 2 to 12
        roll -= 2;
        return table[majorPower ? 0 : 1][roll];
    }

    public void setAllyRating(final AtBContract contract, final boolean isAttacker, final int year) {
        int mod = 0;
        if (contract.getEnemy().isRebelOrPirate()) {
            mod -= 1;
        } else if (contract.getEnemy().isIndependent()) {
            mod -= 2;
        }

        if (contract.getContractType().isGuerrillaWarfare() || contract.getContractType().isCadreDuty()) {
            mod -= 3;
        } else if (contract.getContractType().isGarrisonDuty() || contract.getContractType().isSecurityDuty()) {
            mod -= 2;
        } else if (contract.getContractType().isPlanetaryAssault()) {
            mod += 1;
        }

        if (AtBContract.isMinorPower(contract.getEmployerCode())) {
            mod -= 1;
        }

        if (contract.getEmployerFaction().isClan() && !isAttacker) {
            //facing front-line units
            mod += 1;
        }
        contract.setAllySkill(getSkillRating(Compute.d6(2) + mod));
        if ((year > 2950) && (year < 3039) && !contract.getEmployerFaction().isClan()) {
            mod -= 1;
        }
        contract.setAllyQuality(getQualityRating(Compute.d6(2) + mod));
    }

    public void setEnemyRating(final AtBContract contract, final boolean attacker, final int year) {
        int mod = 0;
        if (contract.getEnemy().isRebelOrPirate()) {
            mod -= 2;
        }

        if (contract.getContractType().isGuerrillaWarfare()) {
            mod += 2;
        } else if (contract.getContractType().isPlanetaryAssault()) {
            mod += 1;
        }

        if (contract.getEmployerFaction().isClan()) {
            mod += attacker ? 2 : 4;
        } else if (!contract.getEmployerFaction().isMajorOrSuperPower()) {
            mod -= 1;
        }

        contract.setEnemySkill(getSkillRating(Compute.d6(2) + mod));
        if ((year > 2950) && (year < 3039) && !contract.getEnemy().isClan()) {
            mod -= 1;
        }
        contract.setEnemyQuality(getQualityRating(Compute.d6(2) + mod));
    }

    protected SkillLevel getSkillRating(final int roll) {
        if (roll <= 5) {
            return SkillLevel.GREEN;
        } else if (roll <= 9) {
            return SkillLevel.REGULAR;
        } else if (roll <= 11) {
            return SkillLevel.VETERAN;
        } else {
            return SkillLevel.ELITE;
        }
    }

    protected int getQualityRating(final int roll) {
        if (roll <= 5) {
            return IUnitRating.DRAGOON_F;
        } else if (roll <= 8) {
            return IUnitRating.DRAGOON_D;
        } else if (roll <= 10) {
            return IUnitRating.DRAGOON_C;
        } else if (roll == 11) {
            return IUnitRating.DRAGOON_B;
        } else {
            return IUnitRating.DRAGOON_A;
        }
    }

    protected void setAtBContractClauses(final Campaign campaign, final AtBContract contract,
                                         final int unitRatingModifier) {
        final ClauseModifiers clauseModifiers = new ClauseModifiers();
        getClauseModifiers().put(contract, clauseModifiers);

        /*
         * AtB rules seem to indicate one admin in each role (though this
         * is not explicitly stated that I have seen) but MekHQ allows
         * assignment of multiple admins to each role. Therefore we go
         * through all the admins and for each role select the one with
         * the highest admin skill, or higher negotiation if the admin
         * skills are equal.
         */
        final Person adminCommand = campaign.findBestInRole(PersonnelRole.ADMINISTRATOR_COMMAND,
                SkillType.S_ADMIN, SkillType.S_NEG);
        final Person adminLogistics = campaign.findBestInRole(PersonnelRole.ADMINISTRATOR_LOGISTICS,
                SkillType.S_ADMIN, SkillType.S_NEG);
        final Person adminTransport = campaign.findBestInRole(PersonnelRole.ADMINISTRATOR_TRANSPORT,
                SkillType.S_ADMIN, SkillType.S_NEG);
        final int adminCommandExp = (adminCommand == null) ? SkillType.EXP_ULTRA_GREEN
                : adminCommand.getSkill(SkillType.S_ADMIN).getExperienceLevel();
        final int adminLogisticsExp = (adminLogistics == null) ? SkillType.EXP_ULTRA_GREEN
                : adminLogistics.getSkill(SkillType.S_ADMIN).getExperienceLevel();
        final int adminTransportExp = (adminTransport == null) ? SkillType.EXP_ULTRA_GREEN
                : adminTransport.getSkill(SkillType.S_ADMIN).getExperienceLevel();

        /* Treat government units like Mercenary units that have a retainer contract */
        if ((!campaign.getFaction().isMercenary() && !campaign.getFaction().isPirate())
                || (campaign.getRetainerEmployerCode() != null)) {
            for (int i = 0; i < CLAUSE_NUM; i++) {
                clauseModifiers.getModifiers()[i]++;
            }
        }

        if (campaign.getCampaignOptions().isMercSizeLimited() && campaign.getFaction().isMercenary()) {
            final int max = (unitRatingModifier + 1) * 12;
            int numberOfModifiers = (AtBContract.getEffectiveNumUnits(campaign) - max) / 2;
            while (numberOfModifiers-- > 0) {
                clauseModifiers.getModifiers()[Compute.randomInt(4)]--;
            }
        }

        clauseModifiers.getModifiers()[CLAUSE_COMMAND] = adminCommandExp - SkillType.EXP_REGULAR;
        clauseModifiers.getModifiers()[CLAUSE_SALVAGE] = 0;
        clauseModifiers.getModifiers()[CLAUSE_TRANSPORT] = adminTransportExp - SkillType.EXP_REGULAR;
        clauseModifiers.getModifiers()[CLAUSE_SUPPORT] = adminLogisticsExp - SkillType.EXP_REGULAR;
        if (unitRatingModifier >= IUnitRating.DRAGOON_A) {
            clauseModifiers.getModifiers()[Compute.randomInt(4)] += 2;
            clauseModifiers.getModifiers()[Compute.randomInt(4)] += 2;
        } else if (unitRatingModifier == IUnitRating.DRAGOON_B) {
            clauseModifiers.getModifiers()[Compute.randomInt(4)] += 1;
            clauseModifiers.getModifiers()[Compute.randomInt(4)] += 1;
        } else if (unitRatingModifier == IUnitRating.DRAGOON_C) {
            clauseModifiers.getModifiers()[Compute.randomInt(4)] += 1;
        } else if (unitRatingModifier <= IUnitRating.DRAGOON_F) {
            clauseModifiers.getModifiers()[Compute.randomInt(4)] -= 1;
        }

        if (contract.getEnemy().isClan() && !contract.getEmployerFaction().isClan()) {
            for (int i = 0; i < 4; i++) {
                clauseModifiers.getModifiers()[i] += (i == CLAUSE_SALVAGE) ? -2 : 1;
            }
        } else {
            if (contract.getEnemySkill().isVeteranOrGreater()) {
                clauseModifiers.getModifiers()[Compute.randomInt(4)] += 1;
            }

            if (contract.getEnemySkill().isEliteOrGreater()) {
                clauseModifiers.getModifiers()[Compute.randomInt(4)] += 1;
            }
        }

        final int[][] missionModifiers = {
                {1, 0, 1, 0}, {0, 1, -1, -3}, {-3, 0, 2, 1}, {-2, 1, -1, -1},
                {-2, 0, 2, 3}, {-1, 1, 1, 1}, {-2, 3, -2, -1}, {2, 2, -1, -1},
                {0, 2, 2, 1}, {-1, 0, 1, 2}, {-1, -2, 1, -1}, {-1, -1, 2, 1}
        };

        for (int i = 0; i < 4; i++) {
            clauseModifiers.getModifiers()[i] += missionModifiers[contract.getContractType().ordinal()][i];
        }

        if (contract.getEmployerFaction().isISMajorOrSuperPower()) {
            clauseModifiers.getModifiers()[CLAUSE_SALVAGE] += -1;
            clauseModifiers.getModifiers()[CLAUSE_TRANSPORT] += 1;
        }

        if (AtBContract.isMinorPower(contract.getEmployerCode())) {
            clauseModifiers.getModifiers()[CLAUSE_SALVAGE] += -2;
        }

        if (contract.getEmployerFaction().isMercenary()) {
            clauseModifiers.getModifiers()[CLAUSE_COMMAND] += -1;
            clauseModifiers.getModifiers()[CLAUSE_SALVAGE] += 2;
            clauseModifiers.getModifiers()[CLAUSE_SUPPORT] += 1;
            clauseModifiers.getModifiers()[CLAUSE_TRANSPORT] += 1;
        }

        if (contract.getEmployerFaction().isIndependent()) {
            clauseModifiers.getModifiers()[CLAUSE_SALVAGE] += -1;
            clauseModifiers.getModifiers()[CLAUSE_SUPPORT] += -1;
        }

        if (campaign.getFaction().isMercenary()) {
            rollCommandClause(contract, clauseModifiers.getModifiers()[CLAUSE_COMMAND]);
        } else {
            contract.setCommandRights(ContractCommandRights.INTEGRATED);
        }
        rollSalvageClause(contract, clauseModifiers.getModifiers()[CLAUSE_SALVAGE]);
        rollSupportClause(contract, clauseModifiers.getModifiers()[CLAUSE_SUPPORT]);
        rollTransportClause(contract, clauseModifiers.getModifiers()[CLAUSE_TRANSPORT]);
    }

    private void rollCommandClause(final Contract contract, final int modifier) {
        final int roll = Compute.d6(2) + modifier;
        if (roll < 3) {
            contract.setCommandRights(ContractCommandRights.INTEGRATED);
        } else if (roll < 8) {
            contract.setCommandRights(ContractCommandRights.HOUSE);
        } else if (roll < 12) {
            contract.setCommandRights(ContractCommandRights.LIAISON);
        } else {
            contract.setCommandRights(ContractCommandRights.INDEPENDENT);
        }
    }

    private void rollSalvageClause(final Contract contract, final int modifier) {
        final int roll = Math.min(Compute.d6(2) + modifier, 13);
        contract.setSalvageExchange(false);
        if (roll < 2) {
            contract.setSalvagePct(0);
        } else if (roll < 4) {
            contract.setSalvageExchange(true);
            int r;
            do {
                r = Compute.d6(2);
            } while (r < 4);
            contract.setSalvagePct(Math.min((r - 3) * 10, 100));
        } else {
            contract.setSalvagePct(Math.min((roll - 3) * 10, 100));
        }
    }

    private void rollSupportClause(final Contract contract, final int modifier) {
        final int roll = Compute.d6(2) + modifier;
        contract.setStraightSupport(0);
        contract.setBattleLossComp(0);
        if (roll < 3) {
            contract.setStraightSupport(0);
        } else if (roll < 8) {
            contract.setStraightSupport((roll - 2) * 20);
        } else if (roll == 8) {
            contract.setBattleLossComp(10);
        } else {
            contract.setBattleLossComp(Math.min((roll - 8) * 20, 100));
        }
    }

    private void rollTransportClause(final Contract contract, final int modifier) {
        final int roll = Compute.d6(2) + modifier;
        if (roll < 2) {
            contract.setTransportComp(0);
        } else if (roll < 6) {
            contract.setTransportComp((20 + (roll - 2) * 5));
        } else if (roll < 10) {
            contract.setTransportComp((45 + (roll - 6) * 5));
        } else {
            contract.setTransportComp(100);
        }
    }

    //region File I/O
    @Override
    protected void writeBodyToXML(final PrintWriter pw, int indent) {
        super.writeBodyToXML(pw, indent);
        for (final Contract key : getContracts()) {
            if (getClauseModifiers().containsKey(key)) {
                getClauseModifiers().get(key).writeToXML(pw, indent, key);
            }
        }
    }

    @Override
    protected void parseXMLNode(final Node wn, final Campaign campaign, final Version version) {
        super.parseXMLNode(wn, campaign, version);

        if (wn.getNodeName().equalsIgnoreCase("clauseModifiers")
                || wn.getNodeName().equalsIgnoreCase("clauseMods")) { // Legacy - 0.49.X removal
            final ClauseModifiers clauseModifiers = ClauseModifiers.parseFromXML(wn);
            if (clauseModifiers != null) {
                getClauseModifiers().put(
                        getContracts().get(Integer.parseInt(wn.getAttributes().getNamedItem("id").getTextContent())),
                        clauseModifiers);
            }
        }
    }
    //endregion File I/O

    //region Local Classes
    /**
     * Keep track of how many rerolls remain for each contract clause based on the administrator's
     * negotiation skill. Also tracks bonuses, as the random clause bonuses should be persistent.
     *         TODO : Windchild Contract Market
     */
    public static class ClauseModifiers {
        //region Variable Declarations
        private final Map<AtBContractMarketClause, Integer> rerollsUsed;
        private final Map<AtBContractMarketClause, Integer> modifiers;
        //endregion Variable Declarations

        //region Constructors
        public ClauseModifiers() {
            this.rerollsUsed = new HashMap<>();
            this.modifiers = new HashMap<>();
            for (final AtBContractMarketClause clause : AtBContractMarketClause.values()) {
                getRerollsUsed().put(clause, 0);
                getModifiers().put(clause, 0);
            }
        }
        //endregion Constructors

        //region Getters/Setters
        public Map<AtBContractMarketClause, Integer> getRerollsUsed() {
            return rerollsUsed;
        }

        public Map<AtBContractMarketClause, Integer> getModifiers() {
            return modifiers;
        }
        //endregion Getters/Setters

        //region File I/O
        public void writeToXML(final PrintWriter pw, int indent, final Contract key) {
            pw.println(MekHqXmlUtil.indentStr(indent++) + "<clauseModifiers id=\"" + key.getId() + "\">");
            MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "modifiers", StringUtils.join(getModifiers()));
            MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "rerollsUsed", StringUtils.join(getRerollsUsed()));
            MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw, --indent, "clauseModifiers");
        }

        public static ClauseModifiers parseFromXML(final Node wn) {
            ClauseModifiers clauseModifiers = new ClauseModifiers();
            try {
                NodeList nl = wn.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node wn2 = nl.item(i);
                    if (wn2.getNodeName().equalsIgnoreCase("modifiers")
                            || wn2.getNodeName().equalsIgnoreCase("mods")) { // Legacy, 0.49.x removal
                        String[] s = wn2.getTextContent().split(",");
                        for (int j = 0; j < s.length; j++) {
                            clauseModifiers.setModifier(j, Integer.parseInt(s[j]));
                        }
                    } else if (wn2.getNodeName().equalsIgnoreCase("rerollsUsed")) {
                        String[] s = wn2.getTextContent().split(",");
                        for (int j = 0; j < s.length; j++) {
                            clauseModifiers.setRerollsUsed(j, Integer.parseInt(s[j]));
                        }
                    }
                }
                return clauseModifiers;
            } catch (Exception e) {
                MekHQ.getLogger().error(e);
                return null;
            }
        }
        //endregion File I/O
    }
    //endregion Local Classes
}
