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
package mekhq.campaign.market;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import megamek.common.util.StringUtil;
import mekhq.campaign.market.enums.ContractMarketMethod;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.client.generator.RandomSkillsGenerator;
import megamek.common.Compute;
import megamek.common.annotations.Nullable;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.Version;
import mekhq.campaign.Campaign;
import mekhq.campaign.JumpPath;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.rating.IUnitRating;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.Factions;
import mekhq.campaign.universe.PlanetarySystem;
import mekhq.campaign.universe.RandomFactionGenerator;
import mekhq.campaign.universe.Systems;

public class AtBContractMarket extends AbstractContractMarket {
    //region Variable Declarations
    private static final long serialVersionUID = 1303462872220110093L;

    public final static int CLAUSE_COMMAND = 0;
    public final static int CLAUSE_SALVAGE = 1;
    public final static int CLAUSE_SUPPORT = 2;
    public final static int CLAUSE_TRANSPORT = 3;
    public final static int CLAUSE_NUM = 4;

    /**
     * An arbitrary maximum number of attempts to generate a contract.
     */
    private final static int MAXIMUM_GENERATION_RETRIES = 3;

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
    public AtBContractMarket() {
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
        for (Mission m : campaign.getMissions()) {
            if (m instanceof AtBContract && m.isActive()) {
                checkForSubcontracts(campaign, (AtBContract) m, unitRatingModifier);
            }
        }

        /*
         * If located on a faction's capital (interpreted as the starting planet for that faction),
         * generate one contract offer for that faction.
         */
        for (Faction faction : campaign.getCurrentSystem().getFactionSet(campaign.getLocalDate())) {
            if (faction.getStartingPlanet(campaign.getLocalDate()).equals(campaign.getCurrentSystem().getId())
                    && RandomFactionGenerator.getInstance().getEmployerSet().contains(faction.getShortName())) {
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

        Set<Faction> currentFactions = campaign.getCurrentSystem().getFactionSet(campaign.getLocalDate());
        boolean inMinorFaction = true;
        for (Faction f : currentFactions) {
            if (RandomFactionGenerator.getInstance().getFactionHints().isISMajorPower(f) || f.isClan()) {
                inMinorFaction = false;
                break;
            }
        }
        if (inMinorFaction) {
            numberOfContracts--;
        }

        boolean inBackwater = true;
        if (currentFactions.size() > 1) {
            // More than one faction, if any is *not* periphery, we're not in backwater either
            for (Faction f : currentFactions) {
                if (!f.isPeriphery()) {
                    inBackwater = false;
                }
            }
        } else if (currentFactions.size() > 0) {
            // Just one faction. Are there any others nearby?
            Faction onlyFaction = currentFactions.iterator().next();
            if (!onlyFaction.isPeriphery()) {
                for (PlanetarySystem key : Systems.getInstance().getNearbySystems(campaign.getCurrentSystem(), 30)) {
                    for (Faction f : key.getFactionSet(campaign.getLocalDate())) {
                        if (!onlyFaction.equals(f)) {
                            inBackwater = false;
                            break;
                        }
                    }
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

    public int getRerollsUsed(final Contract contract, final int clause) {
        return (getClauseModifiers().get(contract.getId()) == null) ? 0
                : getClauseModifiers().get(contract.getId()).getRerollsUsed()[clause];
    }

    public void rerollClause(final Campaign campaign, final Contract contract, final int clause) {
        if (getClauseModifiers().get(contract.getId()) == null) {
            return;
        }

        switch (clause) {
            case CLAUSE_COMMAND:
                rollCommandClause(contract, getClauseModifiers().get(contract.getId()).getModifiers()[clause]);
                break;
            case CLAUSE_SALVAGE:
                rollSalvageClause(contract, getClauseModifiers().get(contract.getId()).getModifiers()[clause]);
                break;
            case CLAUSE_TRANSPORT:
                rollTransportClause(contract, getClauseModifiers().get(contract.getId()).getModifiers()[clause]);
                break;
            case CLAUSE_SUPPORT:
                rollSupportClause(contract, getClauseModifiers().get(contract.getId()).getModifiers()[clause]);
                break;
        }
        getClauseModifiers().get(contract.getId()).getRerollsUsed()[clause]++;
        contract.calculateContract(campaign);
    }

    private void checkForSubcontracts(final Campaign campaign, final AtBContract contract,
                                      final int unitRatingMod) {
        if (contract.getMissionType() == AtBContract.MT_GARRISONDUTY) {
            int numSubcontracts = 0;
            for (Mission m : campaign.getMissions()) {
                if ((m instanceof AtBContract)
                        && contract.equals(((AtBContract) m).getParentContract())) {
                    numSubcontracts++;
                }
            }
            for (int i = numSubcontracts; i < unitRatingMod - 1; i++) {
                final int roll = Compute.d6(2);
                if (roll > 9) {
                    final AtBContract subcontract = generateAtBSubcontract(campaign, contract, unitRatingMod);
                    // TODO : Windchild take a look and see if I can be removed, instead just ensure
                    // TODO : the end date is early enough
                    if (subcontract.getEndingDate().isBefore(contract.getEndingDate())) {
                        getContracts().add(subcontract);
                    }
                }
            }
        }
    }

    @Override
    public AtBContract addContract(final Campaign campaign) {
        final AtBContract c = generateAtBContract(campaign, campaign.getUnitRatingMod());
        if (c != null) {
            getContracts().add(c);
        }
        return c;
    }

    /**
     * If no suitable planet can be found or no jump path to the planet can be calculated after
     * the indicated number of retries, this will return null.
     */
    private @Nullable AtBContract generateAtBContract(final Campaign campaign,
                                                      final int unitRatingModifier) {
        if (campaign.getFaction().isMercenary()) {
            if (campaign.getRetainerEmployerCode() == null) {
                int retries = MAXIMUM_GENERATION_RETRIES;
                AtBContract retVal = null;
                while ((retries > 0) && (retVal == null)) {
                    // Send only 1 retry down because we're handling retries in our loop
                    retVal = generateAtBContract(campaign, RandomFactionGenerator.getInstance().getEmployerFaction(),
                            unitRatingModifier, 1);
                    retries--;
                }
                return retVal;
            } else {
                return generateAtBContract(campaign, campaign.getRetainerEmployer(), unitRatingModifier);
            }
        } else {
            return generateAtBContract(campaign, campaign.getFaction(), unitRatingModifier);
        }
    }

    private @Nullable AtBContract generateAtBContract(final Campaign campaign,
                                                      final @Nullable Faction employer,
                                                      final int unitRatingMod) {
        return generateAtBContract(campaign, employer, unitRatingMod, MAXIMUM_GENERATION_RETRIES);
    }

    private @Nullable AtBContract generateAtBContract(final Campaign campaign,
                                                      @Nullable Faction employer,
                                                      final int unitRatingMod, final int retries) {
        if (employer == null) {
            MekHQ.getLogger().warning("Could not generate an AtB Contract because there was no employer!");
            return null;
        } else if (retries <= 0) {
            MekHQ.getLogger().warning("Could not generate an AtB Contract because we ran out of retries!");
            return null;
        }

        AtBContract contract = new AtBContract(employer.getShortName() + "-"
                + Contract.generateRandomContractName() + "-"
                + MekHQ.getMekHQOptions().getDisplayFormattedDate(campaign.getLocalDate()));
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
        contract.setMissionType(findAtBMissionType(unitRatingMod, contract.getEmployerFaction().isISMajorOrSuperPower()));

        if (contract.getMissionType() == AtBContract.MT_PIRATEHUNTING) {
            contract.setEnemyCode("PIR");
        } else if (contract.getMissionType() == AtBContract.MT_RIOTDUTY) {
            contract.setEnemyCode("REB");
        } else {
            contract.setEnemyCode(RandomFactionGenerator.getInstance().getEnemy(
                    contract.getEmployerCode(), contract.getMissionType() <= AtBContract.MT_RIOTDUTY));
        }
        if ((contract.getMissionType() == AtBContract.MT_GARRISONDUTY) && contract.getEnemyFaction().isRebel()) {
            contract.setMissionType(AtBContract.MT_RIOTDUTY);
        }

        /*
         * Addition to AtB rules: factions which are generally neutral
         * (ComStar, Mercs not under contract) are more likely to have garrison-type
         * contracts and less likely to have battle-type contracts unless at war.
         */
        if (RandomFactionGenerator.getInstance().getFactionHints().isNeutral(employer)
                && !RandomFactionGenerator.getInstance().getFactionHints().isAtWarWith(employer,
                        contract.getEnemyFaction(), campaign.getLocalDate())) {
            if (contract.getMissionType() == AtBContract.MT_PLANETARYASSAULT) {
                contract.setMissionType(AtBContract.MT_GARRISONDUTY);
            } else if (contract.getMissionType() == AtBContract.MT_RELIEFDUTY) {
                contract.setMissionType(AtBContract.MT_SECURITYDUTY);
            }
        }

        final boolean isAttacker = (contract.getMissionType() == AtBContract.MT_PLANETARYASSAULT)
                || (contract.getMissionType() >= AtBContract.MT_PLANETARYASSAULT)
                || ((contract.getMissionType() == AtBContract.MT_RELIEFDUTY) && (Compute.d6() < 4))
                || contract.getEnemyFaction().isRebel();
        if (isAttacker) {
            contract.setSystemId(RandomFactionGenerator.getInstance().getMissionTarget(contract.getEmployerCode(), contract.getEnemyCode()));
        } else {
            contract.setSystemId(RandomFactionGenerator.getInstance().getMissionTarget(contract.getEnemyCode(), contract.getEmployerCode()));
        }
        if (contract.getSystem() == null) {
            MekHQ.getLogger().warning("Could not find contract location for "
                            + contract.getEmployerCode() + " vs. " + contract.getEnemyCode());
            return generateAtBContract(campaign, employer, unitRatingMod, retries - 1);
        }
        JumpPath jp = null;
        try {
            jp = contract.getJumpPath(campaign);
        } catch (NullPointerException ex) {
            // could not calculate jump path; leave jp null
            MekHQ.getLogger().warning("Could not calculate jump path to contract location: "
                            + contract.getSystem().getName(campaign.getLocalDate()), ex);
        }

        if (jp == null) {
            return generateAtBContract(campaign, employer, unitRatingMod, retries - 1);
        }

        setAllyRating(contract, isAttacker, campaign.getGameYear());
        setEnemyRating(contract, isAttacker, campaign.getGameYear());

        if (contract.getMissionType() == AtBContract.MT_CADREDUTY) {
            contract.setAllySkill(RandomSkillsGenerator.L_GREEN);
            contract.setAllyQuality(IUnitRating.DRAGOON_F);
        }

        contract.calculateLength(campaign.getCampaignOptions().getVariableContractLength());
        setAtBContractClauses(campaign, contract, unitRatingMod);

        contract.calculatePaymentMultiplier(campaign);

        contract.calculatePartsAvailabilityLevel(campaign);

        contract.initContractDetails(campaign);
        contract.calculateContract(campaign);

        return contract;
    }

    protected AtBContract generateAtBSubcontract(final Campaign campaign, final AtBContract parent,
                                                 final int unitRatingModifier) {
        AtBContract contract = new AtBContract("New Subcontract");
        contract.setEmployerCode(parent.getEmployerCode(), campaign.getGameYear());
        contract.setMissionType(findAtBMissionType(unitRatingModifier,
                contract.getEmployerFaction().isISMajorOrSuperPower()));

        if (contract.getMissionType() == AtBContract.MT_PIRATEHUNTING)
            contract.setEnemyCode("PIR");
        else if (contract.getMissionType() == AtBContract.MT_RIOTDUTY)
            contract.setEnemyCode("REB");
        else {
            boolean rebsAllowed = contract.getMissionType() <= AtBContract.MT_RIOTDUTY;
            contract.setEnemyCode(RandomFactionGenerator.getInstance().getEnemy(contract.getEmployerCode(), rebsAllowed));
        }
        if (contract.getMissionType() == AtBContract.MT_GARRISONDUTY && contract.getEnemyCode().equals("REB")) {
            contract.setMissionType(AtBContract.MT_RIOTDUTY);
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
        if (!contract.getEnemyFaction().isRebelOrPirate()) {
            boolean factionValid = false;
            for (PlanetarySystem p : Systems.getInstance().getNearbySystems(campaign.getCurrentSystem(), 30)) {
                if (factionValid) break;
                for (Faction f : p.getFactionSet(campaign.getLocalDate())) {
                    if (f.getShortName().equals(contract.getEnemyCode())) {
                        factionValid = true;
                        break;
                    }
                }
            }
            if (!factionValid) {
                contract.setEnemyCode(parent.getEnemyCode());
            }
        }
        boolean isAttacker = (contract.getMissionType() == AtBContract.MT_PLANETARYASSAULT ||
                contract.getMissionType() >= AtBContract.MT_PLANETARYASSAULT ||
                (contract.getMissionType() == AtBContract.MT_RELIEFDUTY && Compute.d6() < 4) ||
                contract.getEnemyCode().equals("REB"));
        contract.setSystemId(parent.getSystemId());
        setAllyRating(contract, isAttacker, campaign.getGameYear());
        setEnemyRating(contract, isAttacker, campaign.getGameYear());

        if (contract.getMissionType() == AtBContract.MT_CADREDUTY) {
            contract.setAllySkill(RandomSkillsGenerator.L_GREEN);
            contract.setAllyQuality(IUnitRating.DRAGOON_F);
        }
        contract.calculateLength(campaign.getCampaignOptions().getVariableContractLength());

        contract.setCommandRights(Math.max(parent.getCommandRights() - 1,
                Contract.COM_INTEGRATED));
        contract.setSalvageExchange(parent.isSalvageExchange());
        contract.setSalvagePct(Math.max(parent.getSalvagePct() - 10, 0));
        contract.setStraightSupport(Math.max(parent.getStraightSupport() - 20,
                0));
        if (parent.getBattleLossComp() <= 10) {
            contract.setBattleLossComp(0);
        } else if (parent.getBattleLossComp() <= 20) {
            contract.setBattleLossComp(10);
        } else {
            contract.setBattleLossComp(parent.getBattleLossComp() - 20);
        }
        contract.setTransportComp(100);

        contract.calculatePaymentMultiplier(campaign);
        contract.calculatePartsAvailabilityLevel(campaign);
        contract.calculateContract(campaign);

        return contract;
    }

    public void addFollowup(final Campaign campaign, final AtBContract contract,
                            final int unitRatingModifier) {
        if (followupContracts.containsValue(contract.getId())) {
            return;
        }
        AtBContract followup = new AtBContract("Followup Contract");
        followup.setEmployerCode(contract.getEmployerCode(), campaign.getGameYear());
        followup.setEnemyCode(contract.getEnemyCode());
        followup.setSystemId(contract.getSystemId());
        switch (contract.getMissionType()) {
            case AtBContract.MT_DIVERSIONARYRAID:
                followup.setMissionType(AtBContract.MT_OBJECTIVERAID);
                break;
            case AtBContract.MT_RECONRAID:
                followup.setMissionType(AtBContract.MT_PLANETARYASSAULT);
                break;
            case AtBContract.MT_RIOTDUTY:
                followup.setMissionType(AtBContract.MT_GARRISONDUTY);
                break;
        }
        followup.setAllySkill(contract.getAllySkill());
        followup.setAllyQuality(contract.getAllyQuality());
        followup.setEnemySkill(contract.getEnemySkill());
        followup.setEnemyQuality(contract.getEnemyQuality());
        followup.calculateLength(campaign.getCampaignOptions().getVariableContractLength());
        setAtBContractClauses(campaign, followup, unitRatingModifier);

        followup.calculatePaymentMultiplier(campaign);

        followup.calculatePartsAvailabilityLevel(campaign);

        followup.initContractDetails(campaign);
        followup.calculateContract(campaign);
        incrementLastId();
        followup.setId(getLastId());

        getContracts().add(followup);
        getFollowupContracts().put(followup, contract);
    }

    protected int findAtBMissionType(final int unitRatingModifier, final boolean majorPower) {
        final int[][] table = {
            //col 0: IS Houses
            {AtBContract.MT_GUERRILLAWARFARE, AtBContract.MT_RECONRAID, AtBContract.MT_PIRATEHUNTING,
                AtBContract.MT_PLANETARYASSAULT, AtBContract.MT_OBJECTIVERAID, AtBContract.MT_OBJECTIVERAID,
                AtBContract.MT_EXTRACTIONRAID, AtBContract.MT_RECONRAID, AtBContract.MT_GARRISONDUTY,
                AtBContract.MT_CADREDUTY, AtBContract.MT_RELIEFDUTY},
            //col 1: Others
                {AtBContract.MT_GUERRILLAWARFARE, AtBContract.MT_RECONRAID, AtBContract.MT_PLANETARYASSAULT,
                    AtBContract.MT_OBJECTIVERAID, AtBContract.MT_EXTRACTIONRAID, AtBContract.MT_PIRATEHUNTING,
                    AtBContract.MT_SECURITYDUTY, AtBContract.MT_OBJECTIVERAID, AtBContract.MT_GARRISONDUTY,
                    AtBContract.MT_CADREDUTY, AtBContract.MT_DIVERSIONARYRAID}
        };
        int roll = Compute.d6(2) + unitRatingModifier - IUnitRating.DRAGOON_C;
        if (roll > 12) {
            roll = 12;
        }
        if (roll < 2) {
            roll = 2;
        }

        roll -= 2;
        return table[majorPower ? 0 : 1][roll];
    }

    public void setAllyRating(final AtBContract contract, final boolean isAttacker, final int year) {
        int mod = 0;
        if (contract.getEnemyFaction().isRebelOrPirate()) {
            mod -= 1;
        }
        if ((contract.getMissionType() == AtBContract.MT_GUERRILLAWARFARE)
                || (contract.getMissionType() == AtBContract.MT_CADREDUTY)) {
            mod -= 3;
        } else if ((contract.getMissionType() == AtBContract.MT_GARRISONDUTY)
                || (contract.getMissionType() == AtBContract.MT_SECURITYDUTY)) {
            mod -= 2;
        }
        if (AtBContract.isMinorPower(contract.getEmployerCode())) {
            mod -= 1;
        }
        if (contract.getEnemyCode().equals("IND") || contract.getEnemyCode().equals("PIND")) {
            mod -= 2;
        }
        if (contract.getMissionType() == AtBContract.MT_PLANETARYASSAULT) {
            mod += 1;
        }
        if (contract.getEmployerFaction().isClan() && !isAttacker) {
            //facing front-line units
            mod += 1;
        }
        contract.setAllySkill(getSkillRating(Compute.d6(2) + mod));
        if (year > 2950 && year < 3039 &&
                !Factions.getInstance().getFaction(contract.getEmployerCode()).isClan()) {
            mod -= 1;
        }
        contract.setAllyQuality(getQualityRating(Compute.d6(2) + mod));
    }

    public void setEnemyRating(final AtBContract contract, final boolean attacker, final int year) {
        int mod = 0;
        if (contract.getEnemyFaction().isRebelOrPirate()) {
            mod -= 2;
        }

        if (contract.getMissionType() == AtBContract.MT_GUERRILLAWARFARE) {
            mod += 2;
        } else if (contract.getMissionType() == AtBContract.MT_PLANETARYASSAULT) {
            mod += 1;
        }

        if (!contract.getEmployerFaction().isClan() && !contract.getEmployerFaction().isMajorOrSuperPower()) {
            mod -= 1;
        }

        if (contract.getEmployerFaction().isClan()) {
            mod += attacker ? 2 : 4;
        }

        contract.setEnemySkill(getSkillRating(Compute.d6(2) + mod));
        if ((year > 2950) && (year < 3039) && !contract.getEnemyFaction().isClan()) {
            mod -= 1;
        }
        contract.setEnemyQuality(getQualityRating(Compute.d6(2) + mod));
    }

    protected int getSkillRating(final int roll) {
        if (roll <= 5) {
            return RandomSkillsGenerator.L_GREEN;
        } else if (roll <= 9) {
            return RandomSkillsGenerator.L_REG;
        } else if (roll <= 11) {
            return RandomSkillsGenerator.L_VET;
        } else {
            return RandomSkillsGenerator.L_ELITE;
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
        final Person adminCommand = campaign.findBestInRole(Person.T_ADMIN_COM, SkillType.S_ADMIN, SkillType.S_NEG);
        final Person adminTransport = campaign.findBestInRole(Person.T_ADMIN_TRA, SkillType.S_ADMIN, SkillType.S_NEG);
        final Person adminLogistics = campaign.findBestInRole(Person.T_ADMIN_LOG, SkillType.S_ADMIN, SkillType.S_NEG);
        final int adminCommandExp = (adminCommand == null) ? SkillType.EXP_ULTRA_GREEN
                : adminCommand.getSkill(SkillType.S_ADMIN).getExperienceLevel();
        final int adminTransportExp = (adminTransport == null) ? SkillType.EXP_ULTRA_GREEN
                : adminTransport.getSkill(SkillType.S_ADMIN).getExperienceLevel();
        final int adminLogisticsExp = (adminLogistics == null) ? SkillType.EXP_ULTRA_GREEN
                : adminLogistics.getSkill(SkillType.S_ADMIN).getExperienceLevel();

        /* Treat government units like merc units that have a retainer contract */
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

        if (contract.getEnemyFaction().isClan() && !contract.getEmployerFaction().isClan()) {
            for (int i = 0; i < 4; i++) {
                clauseModifiers.getModifiers()[i] += (i == CLAUSE_SALVAGE) ? -2 : 1;
            }
        } else {
            if (contract.getEnemySkill() >= SkillType.EXP_VETERAN) {
                clauseModifiers.getModifiers()[Compute.randomInt(4)] += 1;
            } else if (contract.getEnemySkill() == SkillType.EXP_ELITE) {
                clauseModifiers.getModifiers()[Compute.randomInt(4)] += 1;
            }
        }

        final int[][] missionModifiers = {
            {1, 0, 1, 0}, {0, 1, -1, -3}, {-3, 0, 2, 1}, {-2, 1, -1, -1},
            {-2, 0, 2, 3}, {-1, 1, 1, 1}, {-2, 3, -2, -1}, {2, 2, -1, -1},
            {0, 2, 2, 1}, {-1, 0, 1, 2}, {-1, -2, 1, -1}, {-1, -1, 2, 1}
        };
        for (int i = 0; i < 4; i++) {
            clauseModifiers.getModifiers()[i] += missionModifiers[contract.getMissionType()][i];
        }

        if (RandomFactionGenerator.getInstance().getFactionHints().isISMajorPower(contract.getEmployerFaction())) {
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

        if (contract.getEmployerCode().equals("IND") || contract.getEmployerCode().equals("PIND")) {
            clauseModifiers.getModifiers()[CLAUSE_COMMAND] += 0;
            clauseModifiers.getModifiers()[CLAUSE_SALVAGE] += -1;
            clauseModifiers.getModifiers()[CLAUSE_SUPPORT] += -1;
            clauseModifiers.getModifiers()[CLAUSE_TRANSPORT] += 0;
        }

        if (campaign.getFaction().isMercenary()) {
            rollCommandClause(contract, clauseModifiers.getModifiers()[CLAUSE_COMMAND]);
        } else {
            contract.setCommandRights(Contract.COM_INTEGRATED);
        }
        rollSalvageClause(contract, clauseModifiers.getModifiers()[CLAUSE_SALVAGE]);
        rollSupportClause(contract, clauseModifiers.getModifiers()[CLAUSE_SUPPORT]);
        rollTransportClause(contract, clauseModifiers.getModifiers()[CLAUSE_TRANSPORT]);
    }

    private void rollCommandClause(final Contract contract, final int modifier) {
        final int roll = Compute.d6(2) + modifier;
        if (roll < 3) {
            contract.setCommandRights(Contract.COM_INTEGRATED);
        } else if (roll < 8) {
            contract.setCommandRights(Contract.COM_HOUSE);
        } else if (roll < 12) {
            contract.setCommandRights(Contract.COM_LIAISON);
        } else {
            contract.setCommandRights(Contract.COM_INDEP);
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
        for (final Contract key : getClauseModifiers().keySet()) {
            if (!getContracts().contains(key)) {
                continue;
            }
            pw.println(MekHqXmlUtil.indentStr(indent++) + "<clauseModifiers id=\"" + key.getId() + "\">");
            MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "modifiers", StringUtils.join(getClauseModifiers().get(key).getModifiers()));
            MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "rerollsUsed", StringUtils.join(getClauseModifiers().get(key).getRerollsUsed()));
            MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw, --indent, "clauseModifiers");
        }
    }

    public void writeToXml(PrintWriter pw1, int indent) {
        for (final Contract key : getClauseModifiers().keySet()) {
            StringBuilder rerolls = new StringBuilder();
            StringBuilder mods = new StringBuilder();
            for (int i = 0; i < CLAUSE_NUM; i++) {
                rerolls.append(getClauseModifiers().get(key).getRerollsUsed()[i]).append((i < CLAUSE_NUM - 1) ? "," : "");
                mods.append(getClauseModifiers().get(key).getModifiers()[i]).append((i < CLAUSE_NUM - 1) ? "," : "");
            }
            MekHqXmlUtil.writeSimpleXMLTag(pw1, indent+2, "mods", mods.toString());
            MekHqXmlUtil.writeSimpleXMLTag(pw1, indent+2, "rerollsUsed", rerolls.toString());
            pw1.println(MekHqXmlUtil.indentStr(indent+1) + "</clauseMods>");
        }
        pw1.println(MekHqXmlUtil.indentStr(indent) + "</contractMarket>");
    }

    public static AtBContractMarket generateInstanceFromXML(Node wn, Campaign c, Version version) {
        ContractMarket retVal = null;

        try {
            // Instantiate the correct child class, and call its parsing function.
            retVal = new ContractMarket();

            // Okay, now load Part-specific fields!
            NodeList nl = wn.getChildNodes();

            // Loop through the nodes and load our contract offers
            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);

                // If it's not an element node, we ignore it.
                if (wn2.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                if (wn2.getNodeName().equalsIgnoreCase("lastId")) {
                    retVal.lastId = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("mission")) {
                    Mission m = Mission.generateInstanceFromXML(wn2, c, version);

                    if (m != null && m instanceof Contract) {
                        retVal.contracts.add((Contract)m);
                        retVal.contractIds.put(m.getId(), (Contract)m);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("clauseMods")) {
                    int key = Integer.parseInt(wn2.getAttributes().getNamedItem("id").getTextContent());
                    ClauseMods cm = retVal.new ClauseMods();
                    NodeList nl2 = wn2.getChildNodes();
                    for (int i = 0; i < nl2.getLength(); i++) {
                        Node wn3 = nl2.item(i);
                        if (wn3.getNodeName().equalsIgnoreCase("mods")) {
                            String [] s = wn3.getTextContent().split(",");
                            for (int j = 0; j < s.length; j++) {
                                cm.getMods()[j] = Integer.parseInt(s[j]);
                            }
                        } else if (wn3.getNodeName().equalsIgnoreCase("rerollsUsed")) {
                            String [] s = wn3.getTextContent().split(",");
                            for (int j = 0; j < s.length; j++) {
                                cm.getRerollsUsed()[j] = Integer.parseInt(s[j]);
                            }
                        }
                    }
                    retVal.clauseMods.put(key, cm);
                }
            }

            // Restore any parent contract references
            for (Contract contract : retVal.contracts) {
                if (contract instanceof AtBContract) {
                    final AtBContract atbContract = (AtBContract) contract;
                    atbContract.restore(c);
                }
            }
        } catch (Exception ex) {
            // Errrr, apparently either the class name was invalid...
            // Or the listed name doesn't exist.
            // Doh!
            MekHQ.getLogger().error(ex);
        }

        return retVal;
    }
    //endregion File I/O

    //region Local Classes
    /**
     * Keep track of how many rerolls remain for each contract clause based on the administrator's
     * negotiation skill. Also track bonuses, as the random clause bonuses should be persistent.
     */
    public static class ClauseModifiers {
        private int[] rerollsUsed = {0, 0, 0, 0};
        private int[] modifiers = {0, 0, 0, 0};

        //region Constructors
        //endregion Constructors

        //region Getters/Setters
        public int[] getRerollsUsed() {
            return rerollsUsed;
        }

        public void setRerollsUsed(final int... rerollsUsed) {
            this.rerollsUsed = rerollsUsed;
        }

        public int[] getModifiers() {
            return modifiers;
        }

        public void setModifiers(final int... modifiers) {
            this.modifiers = modifiers;
        }
        //endregion Getters/Setters
    }
    //endregion Local Classes
}
