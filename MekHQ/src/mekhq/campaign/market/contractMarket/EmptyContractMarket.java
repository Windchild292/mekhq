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
package mekhq.campaign.market.contractMarket;

import megamek.common.annotations.Nullable;
import mekhq.Version;
import mekhq.campaign.Campaign;
import mekhq.campaign.market.enums.ContractMarketMethod;
import mekhq.campaign.mission.Contract;
import org.w3c.dom.Node;

import java.io.PrintWriter;
import java.util.List;

/**
 * This is a completely empty contract market, which is used when the market is disabled.
 */
public class EmptyContractMarket extends AbstractContractMarket {
    //region Variable Declarations
    private static final long serialVersionUID = 7526665298322946291L;
    //endregion Variable Declarations

    //region Constructors
    public EmptyContractMarket() {
        super(ContractMarketMethod.NONE);
    }
    //endregion Constructors

    //region Getters/Setters
    @Override
    public void setContracts(final List<Contract> contracts) {

    }
    //endregion Getters/Setters

    //region Process New Day
    //region Generate Offers
    @Override
    public void processNewDay(final Campaign campaign) {

    }

    @Override
    public void generateContractOffers(final Campaign campaign, final int unitRatingModifier,
                                       final int numberOfContracts) {

    }

    @Override
    protected int determineNumberOfOffers(final Campaign campaign, final int unitRatingModifier) {
        return 0;
    }
    //endregion Generate Offers

    //region Offer Removal
    @Override
    public void removeContractOffers(final Campaign campaign) {

    }
    //endregion Offer Removal
    //endregion Process New Day

    @Override
    public @Nullable Contract addContract(Campaign campaign) {
        return null;
    }

    //region File I/O
    @Override
    public void writeToXML(final PrintWriter pw, int indent) {

    }

    @Override
    public void fillFromXML(final Node wn, final Campaign campaign, final Version version) {

    }
    //endregion File I/O
}
