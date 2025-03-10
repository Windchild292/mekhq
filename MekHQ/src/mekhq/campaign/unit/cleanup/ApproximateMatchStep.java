/*
 * Copyright (C) 2021-2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.unit.cleanup;

import megamek.common.AmmoType;
import megamek.common.Mounted;
import mekhq.campaign.parts.equipment.AmmoBin;
import mekhq.campaign.parts.equipment.EquipmentPart;

public class ApproximateMatchStep extends UnscrambleStep {
    @Override
    public void visit(final EquipmentProposal proposal, final EquipmentPart part) {
        if (part instanceof AmmoBin) {
            visit(proposal, (AmmoBin) part);
        }
    }

    public void visit(final EquipmentProposal proposal, final AmmoBin ammoBin) {
        final Mounted<?> mount = proposal.getEquipment(ammoBin.getEquipmentNum());
        if ((mount != null) && (mount.getType() instanceof AmmoType)
                && ammoBin.canChangeMunitions((AmmoType) mount.getType())) {
            proposal.proposeMapping(ammoBin, ammoBin.getEquipmentNum());
        }
    }
}
