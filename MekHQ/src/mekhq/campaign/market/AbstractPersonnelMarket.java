/*
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
package mekhq.campaign.market;

import mekhq.campaign.Campaign;
import mekhq.module.api.PersonnelMarketMethod;

public abstract class AbstractPersonnelMarket implements PersonnelMarketMethod {
    protected boolean canGenerateBattleArmor(Campaign campaign) {
        return campaign.getGameYear() > (campaign.getFaction().isClan() ? 2870 : 3050);
    }

    protected boolean canGenerateProtoMechs(Campaign campaign) {
        return campaign.getGameYear() >
    }
}
