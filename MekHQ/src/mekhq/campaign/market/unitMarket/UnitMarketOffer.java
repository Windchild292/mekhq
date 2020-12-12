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
import megamek.common.MechSummaryCache;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.market.enums.UnitMarketMarketType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;

public class UnitMarketOffer {
    //region Variable Declarations
    private UnitMarketMarketType marketType;
    private int unitType;
    private int unitWeight;
    private MechSummary unit;
    private int percent;
    //endregion Variable Declarations

    //region Constructors
    public UnitMarketOffer() {

    }

    public UnitMarketOffer(UnitMarketMarketType marketType, int unitType, int unitWeight,
                           MechSummary unit, int percent) {
        setMarketType(marketType);
        setUnitType(unitType);
        setUnitWeight(unitWeight);
        setUnit(unit);
        setPercent(percent);
    }
    //endregion Constructors

    //region Getters/Setters
    public UnitMarketMarketType getMarketType() {
        return marketType;
    }

    public void setMarketType(UnitMarketMarketType marketType) {
        this.marketType = marketType;
    }

    public int getUnitType() {
        return unitType;
    }

    public void setUnitType(int unitType) {
        this.unitType = unitType;
    }

    public int getUnitWeight() {
        return unitWeight;
    }

    public void setUnitWeight(int unitWeight) {
        this.unitWeight = unitWeight;
    }

    public MechSummary getUnit() {
        return unit;
    }

    public void setUnit(MechSummary unit) {
        this.unit = unit;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
    //endregion Getters/Setters

    //region File I/O
    public void writeToXML(PrintWriter pw1, int indent) {
        MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent++, "offer");
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "market", getMarketType().name());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "unitType", getUnitType());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "unitWeight", getUnitWeight());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "unit", getUnit().getName());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "pct", getPercent());
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, --indent, "offer");
    }

    public static UnitMarketOffer generateInstanceFromXML(Node wn) {
        UnitMarketOffer retVal = new UnitMarketOffer();
        NodeList nl = wn.getChildNodes();

        try {
            for (int i = 0; i < nl.getLength(); i++) {
                Node wn3 = nl.item(i);
                if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                if (wn3.getNodeName().equalsIgnoreCase("market")) {
                    retVal.setMarketType(UnitMarketMarketType.parseFromString(wn3.getTextContent().trim()));
                } else if (wn3.getNodeName().equalsIgnoreCase("unitType")) {
                    retVal.setUnitType(Integer.parseInt(wn3.getTextContent().trim()));
                } else if (wn3.getNodeName().equalsIgnoreCase("unitWeight")) {
                    retVal.setUnitWeight(Integer.parseInt(wn3.getTextContent().trim()));
                } else if (wn3.getNodeName().equalsIgnoreCase("unit")) {
                    retVal.setUnit(MechSummaryCache.getInstance().getMech(wn3.getTextContent().trim()));
                } else if (wn3.getNodeName().equalsIgnoreCase("pct")) {
                    retVal.setPercent(Integer.parseInt(wn3.getTextContent().trim()));
                }
            }
        } catch (Exception ex) {
            MekHQ.getLogger().error(ex);
        }

        return retVal;
    }
    //endregion File I/O
}
