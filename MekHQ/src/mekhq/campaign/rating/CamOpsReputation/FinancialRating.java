/*
 * Copyright (C) 2024-2025 The MegaMek Team
 *
 * This file is part of MekHQ.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community. BattleMech,
 * BattleTech, and MechWarrior are trademarks of The Topps Company, Inc.
 * The MegaMek organization is not affiliated with The Topps Company, Inc.
 * or Catalyst Game Labs.
 */
package mekhq.campaign.rating.CamOpsReputation;

import java.util.Map;
import java.util.stream.Collectors;

import megamek.logging.MMLogger;
import mekhq.campaign.finances.Finances;

public class FinancialRating {
    private static final MMLogger logger = MMLogger.create(FinancialRating.class);

    /**
     * Calculates the financial rating based on the current financial status.
     * Negative financial status (having a loan or a negative balance) affects the
     * rating negatively.
     *
     * @param finances the financial status.
     * @return a map of the financial rating.
     */
    protected static Map<String, Integer> calculateFinancialRating(Finances finances) {
        boolean hasLoan = finances.isInDebt();
        boolean inDebt = finances.getBalance().isNegative();

        Map<String, Integer> financeMap = Map.of(
                "hasLoan", hasLoan ? 1 : 0,
                "inDebt", inDebt ? 1 : 0,
                "total", (hasLoan || inDebt) ? -10 : 0);

        logger.debug("Financial Rating = {}",
                financeMap.entrySet().stream()
                        .map(entry -> String.format("%s: %d\n", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining()));

        return financeMap;
    }
}
