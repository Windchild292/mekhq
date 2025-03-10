/*
 * Copyright (C) 2024-2025 The MegaMek Team. All Rights Reserved.
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
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 */
package mekhq.gui.dialog.nagDialogs.nagLogic;

import mekhq.campaign.Campaign;

public class InsufficientAstechsNagLogic {
    /**
     * Determines whether the campaign has a need for astechs.
     *
     * <p>
     * This method checks the number of astechs needed in the given campaign
     * and returns {@code true} if the number of needed astechs is greater than zero,
     * indicating that there is a requirement for astechs. If the number is zero or negative,
     * the method returns {@code false}.
     * </p>
     *
     * @param campaign the campaign object to retrieve the astech need from
     * @return {@code true} if the campaign requires astechs (astech need > 0),
     *         {@code false} otherwise
     */
    public static boolean hasAsTechsNeeded(Campaign campaign) {
        int asTechsNeeded = campaign.getAstechNeed();

        return asTechsNeeded > 0;
    }
}
