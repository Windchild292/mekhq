/*
 * Copyright (c) 2013 - Jay Lawson <jaylawson39 at yahoo.com>. All Rights Reserved.
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
package mekhq.campaign.personnel.ranks;

import megamek.common.annotations.Nullable;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * A specific rank with information about officer status and payment multipliers
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Rank implements Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = 1677999967776587426L;

    private List<String> rankNames;
    private boolean officer;
    private double payMultiplier;
    private List<Integer> rankLevels;
    //endregion Variable Declarations

    //region Constructors
    public Rank() {
        this(new ArrayList<>(), false, 1.0);
    }

    public Rank(String[] names) {
        this(names, false, 1.0);
    }

    public Rank(List<String> names) {
        this(names, false, 1.0);
    }

    public Rank(String[] name, boolean b, double mult) {
        this(Arrays.asList(name), b, mult);
    }

    public Rank(List<String> names, boolean b, double mult) {
    	rankNames = names;
        officer = b;
        payMultiplier = mult;
        rankLevels = new ArrayList<>();
        for (int i = 0; i < rankNames.size(); i++) {
        	rankLevels.add(0);
        	if (rankNames.get(i).matches(".+:\\d+\\s*$")) {
        		String[] temp = rankNames.get(i).split(":");
        		rankNames.set(i, temp[0].trim());
        		rankLevels.set(i, Integer.parseInt(temp[1].trim()));
        	}
        }
    }
    //endregion Constructors

    public String getName(int profession) {
    	if (profession >= rankNames.size()) {
    		return "Profession Out of Bounds";
    	}
    	return rankNames.get(profession);
    }

    public String getNameWithLevels(int profession) {
    	if (profession >= rankNames.size()) {
    		return "Profession Out of Bounds";
    	}
    	return rankNames.get(profession) + (rankLevels.get(profession) > 0 ? ":" + rankLevels.get(profession) : "");
    }

    public boolean isOfficer() {
        return officer;
    }

    public void setOfficer(boolean b) {
        officer = b;
    }

    public double getPayMultiplier() {
        return payMultiplier;
    }

    public void setPayMultiplier(double d) {
        payMultiplier = d;
    }

    public int getRankLevels(int profession) {
    	return rankLevels.get(profession);
    }

    public String getRankNamesAsString() {
    	StringJoiner joiner = new StringJoiner(",");
    	for (String name : rankNames) {
    		if (rankLevels.size() > 0 && rankLevels.get(rankNames.indexOf(name)) > 0) {
    			joiner.add(name + rankLevels.get(rankNames.indexOf(name)).toString());
    		} else {
                joiner.add(name);
            }
    	}
    	return joiner.toString();
    }

    //region File IO
    public void writeToXML(final PrintWriter pw, int indent) {
        MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw, indent++, "rank");
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "rankNames", getRankNamesAsString());
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "officer", isOfficer());
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "payMultiplier", getPayMultiplier());
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw, --indent, "rank");
    }

    public static @Nullable Rank generateInstanceFromXML(final Node wn) {
        final Rank rank = new Rank();
        try {
            final NodeList nl = wn.getChildNodes();

            for (int x = 0; x < nl.getLength(); x++) {
                final Node wn2 = nl.item(x);

                if (wn2.getNodeName().equalsIgnoreCase("rankName")) {
                	String[] rNames = { wn2.getTextContent(), "--MW", "--MW", "--MW", "--MW", "--MW" };
                    rank.rankNames = Arrays.asList(rNames);
                } else if (wn2.getNodeName().equalsIgnoreCase("rankNames")) {
                    rank.rankNames = Arrays.asList(wn2.getTextContent().split(",", -1));
                    for (int i = 0; i < rank.rankNames.size(); i++) {
                    	rank.rankLevels.add(0);
                    	if (rank.rankNames.get(i).matches(".+:\\d+\\s*$")) {
                    		String[] temp = rank.rankNames.get(i).split(":");
                    		rank.rankNames.set(i, temp[0].trim());
                    		rank.rankLevels.set(i, Integer.parseInt(temp[1].trim()));
                    	}
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("officer")) {
                    rank.officer = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payMultiplier")) {
                    rank.payMultiplier = Double.parseDouble(wn2.getTextContent());
                }
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
            return null;
        }

        return rank;
    }
    //endregion File IO
}
