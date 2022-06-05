/*
 * Copyright (c) 2022 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.personnel.names;

import megamek.codeUtilities.StringUtility;
import mekhq.utilities.MHQXMLUtility;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;

public class Bloodname {
    //region File I/O
    public void writeToXML(final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "name");
        if (!StringUtility.isNullOrBlank(getPreNominal())) {
            MHQXMLUtility.writeSimpleXMLTag(pw, indent, "preNominal", getPreNominal());
        }
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "givenName", getGivenName());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "surname", getSurname());
        if (!StringUtility.isNullOrBlank(getPostNominal())) {
            MHQXMLUtility.writeSimpleXMLTag(pw, indent, "postNominal", getPostNominal());
        }

        if (getMaidenName() != null) { // this is only a != null comparison because empty is a use case for divorce
            MHQXMLUtility.writeSimpleXMLTag(pw, indent, "maidenName", getMaidenName());
        }

        if (!StringUtility.isNullOrBlank(getCallsign())) {
            MHQXMLUtility.writeSimpleXMLTag(pw, indent, "callsign", getCallsign());
        }

        if (getBloodname() != null) {
            getBloodname().writeToXML(pw, indent);
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "name");
    }

    /**
     * @param wn the node to create the Name object from
     * @throws Exception if the nodes are malformed, or there aren't any nodes
     */
    public void fillFromXML(final Node wn) throws Exception {
        if (!wn.hasChildNodes()) {
            throw new Exception("Cannot create a Name from an empty node list");
        }

        final NodeList nl = wn.getChildNodes();

        for (int x = 0; x < nl.getLength(); x++) {
            final Node wn = nl.item(x);
            if (wn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (wn.getNodeName()) {
                case "preNominal":
                    setPreNominalDirect(wn.getTextContent().trim());
                    break;
                case "givenName":
                    setGivenNameDirect(wn.getTextContent().trim());
                    break;
                case "surname":
                    setSurnameDirect(wn.getTextContent().trim());
                    break;
                case "postNominal":
                    setPostNominalDirect(wn.getTextContent().trim());
                    break;
                case "maidenName":
                    setMaidenName(wn.getTextContent().trim());
                    break;
                case "callsign":
                    setCallsignDirect(wn.getTextContent().trim());
                    break;
                case "bloodname":
                    final Bloodname bloodname = new Bloodname();
                    setBloodnameDirect(bloodname.fillFromXML(wn));
                    break;
                default:
                    LogManager.getLogger().error("Failed to parse unknown node" + wn.getNodeName());
                    break;
            }
        }

        setFullName();
    }
    //endregion File I/O

    @Override
    public String toString() {
        return getName();
    }
}
