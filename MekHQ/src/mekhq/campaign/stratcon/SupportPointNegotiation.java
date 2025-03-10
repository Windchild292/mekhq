/*
 * Copyright (C) 2024-2025 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 */
package mekhq.campaign.stratcon;

import megamek.common.Compute;
import megamek.common.annotations.Nullable;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.Skill;

import java.util.*;

import static mekhq.campaign.personnel.SkillType.S_ADMIN;
import static mekhq.utilities.ReportingUtilities.CLOSING_SPAN_TAG;
import static mekhq.utilities.ReportingUtilities.spanOpeningWithCustomColor;

/**
 * This class handles Support Point negotiations for StratCon.
 * <p>
 * It includes functionality to negotiate both initial and weekly support points for contracts,
 * based on the skill levels of available Admin/Transport personnel.
 *
 * <p>The workflow includes:</p>
 * <ul>
 *     <li>Filtering and sorting Admin/Transport personnel by their skill levels.</li>
 *     <li>Negotiating support points for either a single contract (initial negotiation) or all
 *     active contracts (weekly negotiation).</li>
 *     <li>Calculating support points based on dice rolls and personnel skill levels.</li>
 *     <li>Generating appropriate campaign reports reflecting the success or failure of negotiations.</li>
 * </ul>
 */
public class SupportPointNegotiation {
    private static final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.AtBStratCon",
        MekHQ.getMHQOptions().getLocale());

    /**
     * Negotiates weekly additional support points for all active AtB contracts.
     *
     * <p>Uses available Admin/Transport personnel to negotiate support points for contracts, with older contracts
     * being processed first. Personnel are removed from the available pool as they are assigned to contracts.
     * If no Admin/Transport personnel are available, an error report is generated, and the method exits early.</p>
     *
     * <p>Calculated support points are added to the contract if successful, and reports detailing the
     * outcome are appended to the campaign reports.</p>
     *
     * @param campaign The {@link Campaign} instance managing the current game state.
     */
    public static void negotiateAdditionalSupportPoints(Campaign campaign) {
        // Fetch all active contracts and sort them by start date (oldest -> newest)
        List<AtBContract> activeContracts = campaign.getActiveAtBContracts();

        if (activeContracts.isEmpty()) {
            return;
        }

        List<AtBContract> sortedContracts = getSortedContractsByStartDate(activeContracts);

        // Get sorted Admin/Transport personnel
        List<Person> adminTransport = getSortedAdminTransportPersonnel(campaign);

        // If no Admin/Transport personnel, exit early
        if (adminTransport.isEmpty()) {
            addReportNoPersonnel(campaign, null);
            return;
        }

        // Iterate over contracts and negotiate support points
        for (AtBContract contract : sortedContracts) {
            if (adminTransport.isEmpty()) {
                break;
            }

            processContractSupportPoints(campaign, contract, adminTransport, false);
        }
    }

    /**
     * Negotiates initial support points for a specific AtB contract.
     *
     * <p>This method processes a single contract and uses available Admin/Transport personnel to negotiate
     * support points. If no Admin/Transport personnel are available, an error report is generated, and
     * the method exits early.</p>
     *
     * <p>Calculated support points are added to the contract if successful, and a report detailing the
     * outcome is appended to the campaign reports.</p>
     *
     * @param campaign The {@link Campaign} instance managing the current game state.
     * @param contract The {@link AtBContract} instance representing the contract for which initial support
     *                 points are being negotiated.
     */
    public static void negotiateInitialSupportPoints(Campaign campaign, AtBContract contract) {
        // Get sorted Admin/Transport personnel
        List<Person> adminTransport = getSortedAdminTransportPersonnel(campaign);

        // If no Admin/Transport personnel, exit early
        if (adminTransport.isEmpty()) {
            addReportNoPersonnel(campaign, contract);
            return;
        }

        // Negotiate support points for the specific contract
        processContractSupportPoints(campaign, contract, adminTransport, true);
    }

    /**
     * Processes the negotiation of support points for a given AtB contract.
     *
     * <p>Rolls dice for assigned personnel to determine successful negotiations. Support points
     * are calculated based on skill levels and the success of the dice rolls. Personnel are
     * removed from the pool once assigned, and support points are added to the contract if
     * successfully negotiated.</p>
     *
     * @param campaign       The {@link Campaign} instance managing the current game state.
     * @param contract       The {@link AtBContract} instance for which support points are being processed.
     * @param adminTransport A {@link List} of available {@link Person} objects representing Admin/Transport personnel.
     * @param isInitialNegotiation {@code true} if the negotiation took place at the beginning of
     *                                        the contract, otherwise {@code false}
     */
    private static void processContractSupportPoints(Campaign campaign, AtBContract contract,
                                                     List<Person> adminTransport, boolean isInitialNegotiation) {
        int negotiatedSupportPoints = 0;
        int maxSupportPoints = isInitialNegotiation
            ? contract.getRequiredCombatTeams() * 3
            : contract.getRequiredCombatTeams();

        StratconCampaignState campaignState = contract.getStratconCampaignState();

        if (campaignState == null) {
            return;
        }

        if (campaignState.getSupportPoints() >= maxSupportPoints) {
            String pluralizer = (maxSupportPoints > 1) || (maxSupportPoints == 0) ? "s" : "";

            campaign.addReport(String.format(
                resources.getString("supportPoints.maximum"),
                contract.getName(),
                spanOpeningWithCustomColor(MekHQ.getMHQOptions().getFontColorWarningHexColor()),
                CLOSING_SPAN_TAG,
                maxSupportPoints,
                pluralizer));

            return;
        }

        Iterator<Person> iterator = adminTransport.iterator();

        while (iterator.hasNext() && negotiatedSupportPoints < maxSupportPoints) {
            Person admin = iterator.next();
            int rollResult = Compute.d6(2);
            negotiatedSupportPoints += calculateSupportPoints(admin, rollResult);
            iterator.remove();
        }

        // Determine font color based on success or failure
        String fontColor = (negotiatedSupportPoints > 0)
            ? MekHQ.getMHQOptions().getFontColorPositiveHexColor()
            : MekHQ.getMHQOptions().getFontColorNegativeHexColor();

        // Add points to the contract if positive
        if (negotiatedSupportPoints > 0) {
            campaignState.changeSupportPoints(negotiatedSupportPoints);
        }

        // Add a report
        String pluralizer = (negotiatedSupportPoints > 1) || (negotiatedSupportPoints == 0) ? "s" : "";
        if (isInitialNegotiation) {
            campaign.addReport(String.format(
                resources.getString("supportPoints.initial"),
                contract.getName(),
                spanOpeningWithCustomColor(fontColor),
                negotiatedSupportPoints,
                CLOSING_SPAN_TAG,
                pluralizer));
        } else {
            campaign.addReport(String.format(
                resources.getString("supportPoints.weekly"),
                spanOpeningWithCustomColor(fontColor),
                negotiatedSupportPoints,
                CLOSING_SPAN_TAG,
                pluralizer,
                contract.getName()));
        }
    }

    /**
     * Filters and sorts Admin/Transport personnel from the campaign by their skill levels in descending order.
     *
     * @param campaign The {@link Campaign} instance containing personnel to be filtered and sorted.
     * @return A {@link List} of {@link Person} objects representing Admin/Transport personnel, sorted by skill.
     */
    private static List<Person> getSortedAdminTransportPersonnel(Campaign campaign) {
        List<Person> adminTransport = new ArrayList<>();
        for (Person person : campaign.getAdmins()) {
            if (person.getPrimaryRole().isAdministratorTransport()
                || person.getSecondaryRole().isAdministratorTransport()) {
                // Each character gets to roll three times, so we add them to the list three times.
                adminTransport.add(person);
                adminTransport.add(person);
                adminTransport.add(person);
            }
        }

        adminTransport.sort((p1, p2) -> Integer.compare(getSkillValue(p2), getSkillValue(p1)));
        return adminTransport;
    }

    /**
     * Sorts all active AtB contracts by their start date in ascending order.
     *
     * @return A {@link List} of {@link AtBContract} instances, sorted by start date.
     */
    private static List<AtBContract> getSortedContractsByStartDate(List<AtBContract> activeContracts) {
        activeContracts.sort(Comparator.comparing(AtBContract::getStartDate));
        return activeContracts;
    }

    /**
     * Calculates the number of support points based on a die roll and the skill level of a given Admin/Transport person.
     *
     * <p>If the dice roll meets or exceeds the admin's skill level, at least one support point is awarded,
     * with additional support points depending on the margin of success.</p>
     *
     * @param admin      The {@link Person} representing the Admin/Transport personnel rolling for support points.
     * @param rollResult The result of rolling two six-sided dice (2d6).
     * @return The number of support points awarded based on the roll and skill level.
     */
    private static int calculateSupportPoints(Person admin, int rollResult) {
        int adminSkill = admin.getSkill(S_ADMIN).getFinalSkillValue();
        if (rollResult < adminSkill) {
            return 0;
        }

        int points = 1; // Base success
        int marginOfSuccess = (rollResult - adminSkill) / 4;
        points += marginOfSuccess;
        return points;
    }

    /**
     * Adds a report to the campaign log indicating the absence of Admin/Transport personnel for support point negotiations.
     *
     * <p>If a contract is specified, the report is related to that contract. Otherwise, the report is general
     * (e.g., for weekly negotiations).</p>
     *
     * @param campaign The {@link Campaign} instance managing the current game state.
     * @param contract An optional {@link AtBContract} instance representing the affected contract (can be {@code null}).
     */
    private static void addReportNoPersonnel(Campaign campaign, @Nullable AtBContract contract) {
        String reportKey = String.format("supportPoints.%s.noAdministrators",
            contract == null ? "weekly" : "initial");

        if (contract == null) {
            campaign.addReport(String.format(resources.getString(reportKey),
                spanOpeningWithCustomColor(MekHQ.getMHQOptions().getFontColorNegativeHexColor()),
                CLOSING_SPAN_TAG
            ));
        } else {
            campaign.addReport(String.format(resources.getString(reportKey),
                contract.getName(),
                spanOpeningWithCustomColor(MekHQ.getMHQOptions().getFontColorNegativeHexColor()),
                CLOSING_SPAN_TAG
            ));
        }
    }

    /**
     * Calculates the total skill value for a given person by summing their skill level and bonuses.
     *
     * @param person The {@link Person} whose skill value is being calculated.
     * @return An {@code int} representing the total skill value (level + bonus).
     */
    private static int getSkillValue(Person person) {
        Skill skill = person.getSkill(S_ADMIN);
        return skill.getTotalSkillLevel();
    }
}
