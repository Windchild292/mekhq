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
package mekhq.campaign.market.unitMarket;

import megamek.common.MechSummary;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import mekhq.campaign.market.enums.UnitMarketMarketType;
import mekhq.campaign.market.enums.UnitMarketMethod;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractUnitMarket implements Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = 1583989355384117937L;

    private UnitMarketMethod method;
    private List<UnitMarketOffer> offers;
    //endregion Variable Declarations

    //region Constructors
    protected AbstractUnitMarket(UnitMarketMethod method) {
        setMethod(method);
        setOffers(new ArrayList<>());
    }
    //endregion Constructors

    //region Getters/Setters
    public UnitMarketMethod getMethod() {
        return method;
    }

    public void setMethod(UnitMarketMethod method) {
        this.method = method;
    }

    public List<UnitMarketOffer> getOffers() {
        return offers;
    }

    public void setOffers(List<UnitMarketOffer> offers) {
        this.offers = offers;
    }

    public void removeOffer(UnitMarketOffer o) {
        getOffers().remove(o);
    }
    //endregion Getters/Setters

    public abstract void generateUnitOffers(Campaign campaign);

    protected abstract void addOffers(Campaign campaign, int num, UnitMarketMarketType market,
                                      int unitType, String faction, int quality, int priceTarget);

    public String addSingleUnit(Campaign campaign, UnitMarketMarketType market, int unitType,
                                String faction, int quality, int percent) {
        return addSingleUnit(campaign, market, unitType, generateWeight(campaign, unitType, faction),
                faction, quality, percent);
    }

    public String addSingleUnit(Campaign campaign, UnitMarketMarketType market, int unitType,
                                int weight, String faction, int quality, int percent) {
        MechSummary ms = campaign.getUnitGenerator().generate(faction, unitType, weight,
                campaign.getGameYear(), quality);
        if (ms == null) {
            return null;
        } else {
            getOffers().add(new UnitMarketOffer(market, unitType, weight, ms, percent));
            return ms.getName();
        }
    }

    public abstract int generateWeight(Campaign campaign, int unitType, String faction);

    //region File I/O
    public void writeToXML(PrintWriter pw1, int indent) {
        MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent++, "unitMarket");
        for (UnitMarketOffer offer : getOffers()) {
            offer.writeToXML(pw1, indent);
        }
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, --indent, "unitMarket");
    }
    //endregion File I/O
}
