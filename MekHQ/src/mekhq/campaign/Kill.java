/*
 * Kill.java
 *
 * Copyright (c) 2011 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
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
package mekhq.campaign;

import java.io.PrintWriter;
import java.io.Serializable;

import mekhq.campaign.mission.Scenario;
import mekhq.campaign.personnel.Person;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;

/**
 * A kill record
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Kill implements Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = 4680018605784351078L;

    private Person slayer;
    private Scenario scenario;
    private String killedUnitName;
    private String killingUnitName;
    //endregion Variable Declarations

    //region Constructors
    /**
     * This is used in legacy parsing
     */
    public Kill() {

    }

    /**
     * @param slayer the person who killed the unit in question
     */
    public Kill(final Person slayer) {
        setSlayer(slayer);
    }

    /**
     * Creates a clone of the input kill assigned to a separate person
     * @param kill the kill to clone
     * @param slayer the person who killed the specified unit
     */
    public Kill(final Kill kill, final Person slayer) {
        this(slayer, kill.getScenario(), kill.getKilledUnitName(), kill.getKillingUnitName());
    }

    /**
     * @param slayer the person who killed the unit in question
     * @param scenario the scenario the kill occurred during
     * @param killedUnitName the name of the killed unit
     * @param killingUnitName the name of the killing unit
     */
    public Kill(final Person slayer, final Scenario scenario, final String killedUnitName,
                final String killingUnitName) {
        setSlayer(slayer);
        setScenario(scenario);
        setKilledUnitName(killedUnitName);
        setKillingUnitName(killingUnitName);
    }
    //endregion Constructors

    //region Getters/Setters
    public Person getSlayer() {
        return slayer;
    }

    public void setSlayer(final Person slayer) {
        this.slayer = slayer;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(final Scenario scenario) {
        this.scenario = scenario;
    }

    public String getKilledUnitName() {
        return killedUnitName;
    }

    public void setKilledUnitName(final String killedUnitName) {
        this.killedUnitName = killedUnitName;
    }

    public String getKillingUnitName() {
        return killingUnitName;
    }

    public void setKillingUnitName(final String killingUnitName) {
        this.killingUnitName = killingUnitName;
    }
    //endregion Getters/Setters

    /**
     * @param kill the kill to compare
     * @return true if the kills are against the same target, otherwise false
     */
    public boolean isSameKill(final Kill kill) {
        return getScenario().equals(kill.getScenario())
                && getKilledUnitName().equals(kill.getKilledUnitName())
                && getKillingUnitName().equals(kill.getKillingUnitName());
    }

    //region File I/O
    public void writeToXML(final PrintWriter pw, int indent) {
        MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw, indent++, "kill");
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "scenario", getScenario().getId());
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "killedUnitName", getKilledUnitName());
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "killingUnitName", getKillingUnitName());
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw, --indent, "kill");
    }

    public static Kill generateInstanceFromXML(final Node wn, final Campaign campaign, final Person slayer) {
        Kill kill = new Kill(slayer);
        final NodeList nl = wn.getChildNodes();
        try {
            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);
                if (wn2.getNodeName().equalsIgnoreCase("scenario")) {
                    final Scenario scenario = campaign.getScenario(Integer.parseInt(wn2.getTextContent().trim()));
                    if (scenario == null) {
                        return null;
                    }
                    kill.setScenario(scenario);
                } else if (wn2.getNodeName().equalsIgnoreCase("killedUnitName")) {
                    kill.setKilledUnitName(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("killingUnitName")) {
                    kill.setKillingUnitName(wn2.getTextContent().trim());
                }
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
            kill = null;
        }
        return kill;
    }
    //endregion File I/O

    @Override
    public boolean equals() {

    }
}
