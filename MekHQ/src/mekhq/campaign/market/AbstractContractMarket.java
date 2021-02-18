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

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.Version;
import mekhq.campaign.Campaign;
import mekhq.campaign.market.enums.ContractMarketMethod;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.Mission;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class AbstractContractMarket implements Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = 8622795835319978360L;

    private ContractMarketMethod method;
    private List<Contract> contracts;

    private int lastId = 0;

    protected final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Market", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    protected AbstractContractMarket(final ContractMarketMethod method) {
        setMethod(method);
        setContracts(new ArrayList<>());
    }
    //endregion Constructors

    //region Getters/Setters
    public ContractMarketMethod getMethod() {
        return method;
    }

    public void setMethod(final ContractMarketMethod method) {
        this.method = method;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(final List<Contract> contracts) {
        this.contracts = contracts;
    }

    public void removeContract(final Contract contract) {
        getContracts().remove(contract);
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(final int lastId) {
        this.lastId = lastId;
    }

    public void incrementLastId() {
        lastId++;
    }
    //endregion Getters/Setters

    //region Process New Day
    /**
     * This is the primary method for processing the Contract Market. It is executed as part of
     * {@link Campaign#newDay()}
     * @param campaign the campaign to process the Contract Market new day using
     */
    public abstract void processNewDay(final Campaign campaign);

    //region Generate Contracts
    public void generateContractOffers(final Campaign campaign) {
        final int unitRatingModifier = campaign.getUnitRatingMod();
        generateContractOffers(campaign, unitRatingModifier,
                determineNumberOfOffers(campaign, unitRatingModifier));
    }

    public void generateContractOffers(final Campaign campaign, final int numberOfContracts) {
        generateContractOffers(campaign, campaign.getUnitRatingMod(), numberOfContracts);
    }

    public abstract void generateContractOffers(final Campaign campaign, final int unitRatingModifier,
                                                final int numberOfContracts);

    protected abstract int determineNumberOfOffers(final Campaign campaign, final int unitRatingModifier);

    /**
     * @param campaign the campaign to write the refresh report to
     */
    protected void writeRefreshReport(final Campaign campaign) {
        if (campaign.getCampaignOptions().getContractMarketReportRefresh()) {
            campaign.addReport(resources.getString("AbstractContractMarket.RefreshReport.report"));
        }
    }
    //endregion Generate Contracts

    //region Contract Removal
    /**
     * This is the primary Contract Market removal method, which is how the market specified
     * removes contract offers
     * @param campaign the campaign to use in determining the offers to remove
     */
    public abstract void removeContractOffers(final Campaign campaign);
    //endregion Contract Removal
    //endregion Process New Day

    public abstract Contract addContract(final Campaign campaign);

    //region File I/O
    /**
     * This writes the Contract Market to XML
     * @param pw the PrintWriter to write to
     * @param indent the base indent level to write at
     */
    public void writeToXML(final PrintWriter pw, int indent) {
        MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw, indent++, "contractMarket");
        writeBodyToXML(pw, indent);
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw, --indent, "contractMarket");
    }


    /**
     * This is meant to be overridden so that a market can have additional elements added to it,
     * albeit with this called by super.writeBodyToXML(pw, indent) first.
     * @param pw the PrintWriter to write to
     * @param indent the base indent level to write at
     */
    protected void writeBodyToXML(final PrintWriter pw, int indent) {
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "lastId", getLastId());
        for (final Contract contract : getContracts()) {
            contract.writeToXml(pw, indent);
        }
    }

    /**
     * This method fills the market based on the supplied XML node. The market is initialized as
     * empty before this is called.
     * @param wn the node to fill the market from
     */
    public void fillFromXML(final Node wn, final Campaign campaign, final Version version) {
        try {
            final NodeList nl = wn.getChildNodes();
            for (int x = 0; x < nl.getLength(); x++) {
                final Node wn2 = nl.item(x);
                if (wn2.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                parseXMLNode(wn2, campaign, version);
            }
        } catch (Exception e) {
            MekHQ.getLogger().error("Failed to parse Contract Market, keeping currently parsed market", e);
        }
    }

    /**
     * This is meant to be overridden so that a market can have additional elements added to it,
     * albeit with this called by super.parseXMLNode(wn) first.
     * @param wn the node to parse from XML
     */
    protected void parseXMLNode(final Node wn, final Campaign campaign, final Version version) {
        if (wn.getNodeName().equalsIgnoreCase("lastId")) {
            setLastId(Integer.parseInt(wn.getTextContent()));
        } else if (wn.getNodeName().equalsIgnoreCase("mission")) {
            Mission m = Mission.generateInstanceFromXML(wn, campaign, version);
            if (m instanceof Contract) {
                getContracts().add((Contract) m);
            }
        }
    }
    //endregion File I/O
}
