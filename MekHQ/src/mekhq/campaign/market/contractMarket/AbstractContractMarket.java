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
package mekhq.campaign.market.contractMarket;

import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import mekhq.campaign.market.enums.ContractMarketMethod;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.Mission;

import org.w3c.dom.Node;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContractMarket implements Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = 8622795835319978360L;

    private ContractMarketMethod method;
    private List<Contract> contracts;

    private int lastId = 0;
    //endregion Variable Declarations

    //region Constructors
    protected AbstractContractMarket(ContractMarketMethod method) {
        setMethod(method);
        setContracts(new ArrayList<>());
    }
    //endregion Constructors

    //region Getters/Setters
    public ContractMarketMethod getMethod() {
        return method;
    }

    public void setMethod(ContractMarketMethod method) {
        this.method = method;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    public void incrementLastId() {
        lastId++;
    }
    //endregion Getters/Setters

    public abstract Contract addContract(Campaign campaign);

    public void generateContractOffers(Campaign campaign) {
        generateContractOffers(campaign, false);
    }

    public abstract void generateContractOffers(Campaign campaign, boolean newCampaign);

    //region File I/O
    public void writeToXML(PrintWriter pw1, int indent) {
        MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent++, "contractMarket");
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "lastId", getLastId());
        for (Contract c : getContracts()) {
            c.writeToXml(pw1, indent);
        }
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, --indent, "contractMarket");
    }
    //endregion File I/O
}
