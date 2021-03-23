/*
 * ForceStub.java
 *
 * Copyright (c) 2011 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
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
package mekhq.campaign.force;

import megamek.common.icons.AbstractIcon;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import mekhq.campaign.force.icons.LayeredForceIcon;
import mekhq.campaign.force.icons.StandardForceIcon;
import mekhq.campaign.unit.Unit;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.Vector;

/**
 * this is a hierarchical object that represents forces from the TO&E using
 * strings rather than unit objects. This makes it static and thus usable to
 * keep track of forces involved in completed scenarios
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class ForceStub implements Serializable {
    private static final long serialVersionUID = -7283462987261602481L;

    private String name;
    private AbstractIcon forceIcon;
    private Vector<ForceStub> subForces;
    private Vector<UnitStub> units;

    public ForceStub() {
        name = "";
        setForceIcon(new LayeredForceIcon());
        subForces = new Vector<>();
        units = new Vector<>();
    }

    public ForceStub(Force force, Campaign c) {
        this();
        name = force.getFullName();
        if (force.getForceIcon() instanceof StandardForceIcon) {
            setForceIcon(((StandardForceIcon) force.getForceIcon()).clone());
        }
        for (Force sub : force.getSubForces()) {
            ForceStub stub = new ForceStub(sub, c);
            //stub.setParentForce(this);
            subForces.add(stub);
        }
        for (UUID uid : force.getUnits()) {
            Unit u = c.getUnit(uid);
            if (null != u) {
                units.add(new UnitStub(u));
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public AbstractIcon getForceIcon() {
        return forceIcon;
    }

    public void setForceIcon(final AbstractIcon forceIcon) {
        this.forceIcon = Objects.requireNonNull(forceIcon);
    }

    public Vector<Object> getAllChildren() {
        Vector<Object> children = new Vector<>();
        children.addAll(subForces);
        children.addAll(units);

        return children;
    }

    public void writeToXml(PrintWriter pw1, int indent) {
        pw1.println(MekHqXmlUtil.indentStr(indent) + "<forceStub>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<name>"
                +MekHqXmlUtil.escape(name)
                +"</name>");
        getForceIcon().writeToXML(pw1, indent + 1);
        if (units.size() > 0) {
            pw1.println(MekHqXmlUtil.indentStr(indent+1)
                    +"<units>");
            for (UnitStub ustub : units) {
                ustub.writeToXml(pw1, indent+2);
            }
            pw1.println(MekHqXmlUtil.indentStr(indent+1)
                    +"</units>");
        }
        if (subForces.size() > 0) {
            pw1.println(MekHqXmlUtil.indentStr(indent+1)
                    +"<subforces>");
            for (ForceStub sub : subForces) {
                sub.writeToXml(pw1, indent+2);
            }
            pw1.println(MekHqXmlUtil.indentStr(indent+1)
                    +"</subforces>");
        }
        pw1.println(MekHqXmlUtil.indentStr(indent) + "</forceStub>");
    }

    public static ForceStub generateInstanceFromXML(Node wn) {
        ForceStub retVal = null;

        try {
            retVal = new ForceStub();
            NodeList nl = wn.getChildNodes();

            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);
                if (wn2.getNodeName().equalsIgnoreCase("name")) {
                    retVal.name = wn2.getTextContent();
                    if (LayeredForceIcon.LAYERED_CATEGORY.equalsIgnoreCase(wn2.getTextContent())) {
                        retVal.setForceIcon(new LayeredForceIcon());
                    } else {
                        retVal.setForceIcon(new StandardForceIcon());
                        retVal.getForceIcon().setCategory(wn2.getTextContent().trim());
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase(StandardForceIcon.XML_TAG)) {
                    final AbstractIcon icon = StandardForceIcon.parseFromXML(wn);
                    if (!icon.isDefault()) {
                        retVal.setForceIcon(icon);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase(LayeredForceIcon.XML_TAG)) {
                    final AbstractIcon icon = LayeredForceIcon.parseFromXML(wn);
                    if (!icon.isDefault()) {
                        retVal.setForceIcon(icon);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("iconHashMap")) { // Legacy, 0.49.X removal
                    if (retVal.getForceIcon() instanceof LayeredForceIcon) {
                        ((LayeredForceIcon) retVal.getForceIcon()).parseIconMapSubNodes(wn2);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("iconFileName")) { // Legacy, 0.49.X removal
                    retVal.getForceIcon().setFilename(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("units")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        } else if (!wn3.getNodeName().equalsIgnoreCase("unitStub")) {
                            MekHQ.getLogger().error("Unknown node type not loaded in ForceStub nodes: " + wn3.getNodeName());
                            continue;
                        }

                        retVal.units.add(UnitStub.generateInstanceFromXML(wn3));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("subforces")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        } else if (!wn3.getNodeName().equalsIgnoreCase("forceStub")) {
                            MekHQ.getLogger().error("Unknown node type not loaded in ForceStub nodes: " + wn3.getNodeName());
                            continue;
                        }

                        retVal.addSubForce(generateInstanceFromXML(wn3));
                    }
                }
            }
        } catch (Exception ex) {
            MekHQ.getLogger().error(ex);
        }

        return retVal;
    }

    public void addSubForce(ForceStub sub) {
        subForces.add(sub);
    }

}
