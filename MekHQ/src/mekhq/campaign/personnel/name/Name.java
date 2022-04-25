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
package mekhq.campaign.personnel.name;

import megamek.client.generator.RandomNameGenerator;
import megamek.codeUtilities.StringUtility;
import megamek.common.annotations.Nullable;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.personnel.Bloodname;
import mekhq.campaign.personnel.Person;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;

public class Name {
    //region Variable Declarations
    private transient String fullName;
    private String preNominal;
    private String givenName;
    private String surname;
    private String postNominal;
    private String maidenName;
    private String callsign;
    private Bloodname bloodname;
    //endregion Variable Declarations

    //region Constructors
    public Name() {
        this(RandomNameGenerator.UNNAMED, RandomNameGenerator.UNNAMED_SURNAME);
    }

    public Name(final String givenName, final String surname) {
        this("", givenName, surname, "");
    }

    public Name(final String preNominal, final String givenName, final String surname,
                final String postNominal) {
        setPreNominalDirect(preNominal);
        setGivenNameDirect(givenName);
        setSurnameDirect(surname);
        setPostNominalDirect(postNominal);
        setMaidenName(null); // this is set to null to handle divorce cases
        setCallsignDirect("");
        setBloodnameDirect(null);
    }
    //endregion Constructors


    //region Name
    /**
     * @return the person's full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param person the person whose name this is
     * @return a hyperlinked string for the person's name
     */
    public String getHyperlinkedName(final Person person) {
        return String.format("<a href='PERSON:%s'>%s</a>", person.getId(), this);
    }

    /**
     * This is used to create the full name of the person, based on their first and last names
     */
    public void setFullName() {
        final String lastName = getLastName();
        setFullNameDirect(getFirstName()
                + (getCallsign().isBlank() ? "" : (" \"" + getCallsign() + '"'))
                + (lastName.isBlank() ? "" : ' ' + lastName));
    }

    /**
     * @param fullName this sets the full name to be equal to the input string. This can ONLY be
     *                 called by {@link #setFullName()} or its overrides.
     */
    protected void setFullNameDirect(final String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return a String containing the person's first name including their pre-nominal
     */
    public String getFirstName() {
        return (getPreNominal().isBlank() ? "" : (getPreNominal() + ' ')) + getGivenName();
    }

    /**
     * Return a full last name which may be a bloodname or a surname with or without a post-nominal.
     * A bloodname will overrule a surname, but we do not disallow surnames for clanners, if the
     * player wants to input them
     * @return a String of the person's last name
     */
    public String getLastName() {
        String lastName = (getBloodname() != null) ? getBloodname().getName()
                : !StringUtility.isNullOrBlank(getSurname()) ? getSurname()
                : "";
        if (!StringUtility.isNullOrBlank(getPostNominal())) {
            lastName += (lastName.isBlank() ? "" : " ") + getPostNominal();
        }
        return lastName;
    }

    /**
     * @return the person's pre-nominal
     */
    public String getPreNominal() {
        return preNominal;
    }

    /**
     * @param preNominal the person's new pre-nominal
     */
    public void setPreNominal(final String preNominal) {
        setPreNominalDirect(preNominal);
        setFullName();
    }

    public void setPreNominalDirect(final String preNominal) {
        this.preNominal = preNominal;
    }

    /**
     * @return the person's given name
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * @param givenName the person's new given name
     */
    public void setGivenName(final String givenName) {
        setGivenNameDirect(givenName);
        setFullName();
    }

    public void setGivenNameDirect(final String givenName) {
        this.givenName = givenName;
    }

    /**
     * @return the person's surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the person's new surname
     */
    public void setSurname(final String surname) {
        setSurnameDirect(surname);
        setFullName();
    }

    public void setSurnameDirect(final String surname) {
        this.surname = surname;
    }

    /**
     * @return the person's post-nominal
     */
    public String getPostNominal() {
        return postNominal;
    }

    /**
     * @param postNominal the person's new post-nominal
     */
    public void setPostNominal(final String postNominal) {
        setPostNominalDirect(postNominal);
        setFullName();
    }

    public void setPostNominalDirect(final String postNominal) {
        this.postNominal = postNominal;
    }

    /**
     * @return the person's maiden name
     */
    public @Nullable String getMaidenName() {
        return maidenName;
    }

    /**
     * @param maidenName the person's new maiden name
     */
    public void setMaidenName(final @Nullable String maidenName) {
        this.maidenName = maidenName;
    }

    /**
     * @return the person's callsign
     */
    public String getCallsign() {
        return callsign;
    }

    /**
     * @param callsign the person's new callsign
     */
    public void setCallsign(final String callsign) {
        setCallsignDirect(callsign);
        setFullName();
    }

    public void setCallsignDirect(final String callsign) {
        this.callsign = callsign;
    }

    public @Nullable Bloodname getBloodname() {
        return bloodname;
    }

    public void setBloodname(final @Nullable Bloodname bloodname) {
        setBloodnameDirect(bloodname);
        setFullName();
    }

    public void setBloodnameDirect(final @Nullable Bloodname bloodname) {
        this.bloodname = bloodname;
    }

    /**
     * This method is used to migrate names from being a joined name to split between given name and
     * surname, as part of the Personnel changes in MekHQ 0.47.4, and is used to migrate from
     * MM-style names to MHQ-style names
     * @param text text containing the name to be migrated
     */
    public void migrateName(final Person person, final String text) {
        // How this works:
        // Takes the input name, and splits it into individual parts.
        // Then, it depends on whether the person is a Clanner or not.
        // For Clan names:
        // Takes the input name, and assumes that person does not have a surname
        // Bloodnames are assumed to have been assigned by MekHQ
        // For Inner Sphere names:
        // Depending on the length of the resulting array, the name is processed differently
        // Array of length 1: the name is assumed to not have a surname, just a given name
        // Array of length 2: the name is assumed to be a given name and a surname
        // Array of length 3: the name is assumed to be a given name and two surnames
        // Array of length 4+: the name is assumed to be as many given names as possible and two surnames
        //
        // Then, the full name is set
        final String[] name = text.trim().split("\\s+");
        final StringBuilder givenName = new StringBuilder(name[0]);

        if (person.isClanner()) {
            if (name.length > 1) {
                int i;
                for (i = 1; i < name.length - 1; i++) {
                    givenName.append(' ').append(name[i]);
                }

                if (!((getBloodname() != null) && getBloodname().getName().equals(name[i]))) {
                    givenName.append(' ').append(name[i]);
                }
            }
        } else {
            if (name.length == 2) {
                setSurnameDirect(name[1]);
            } else if (name.length == 3) {
                setSurnameDirect(name[1] + ' ' + name[2]);
            } else if (name.length > 3) {
                int i;
                for (i = 1; i < name.length - 2; i++) {
                    givenName.append(' ').append(name[i]);
                }
                setSurnameDirect(name[i] + ' ' + name[i + 1]);
            }
        }

        if ((getSurname() == null) || getSurname().equals(RandomNameGenerator.UNNAMED_SURNAME)) {
            setSurnameDirect("");
        }

        setGivenNameDirect(givenName.toString());
        setFullName();
    }

    public String getHTMLTitle(final Person person) {
        return String.format("<html><div id=\"%s\" style=\"white-space: nowrap;\">%s</div></html>",
                person.getId(), getFullTitle(person));
    }

    public String getFullTitle(final Person person) {
        String rank = person.getRankName().trim();

        if (!rank.isBlank()) {
            rank = rank + ' ';
        }

        return rank + this;
    }

    public String makeHTMLRank(final Person person) {
        return String.format("<html><div id=\"%s\">%s</div></html>", person.getId(), person.getRankName().trim());
    }

    public String getHyperlinkedFullTitle(final Person person) {
        return String.format("<a href='PERSON:%s'>%s</a>", person.getId(), getFullTitle(person));
    }
    //endregion Names

    //region File I/O
    public void writeToXML(final PrintWriter pw, int indent) {
        MekHqXmlUtil.writeSimpleXMLOpenTag(pw, indent++, "name");
        if (!StringUtility.isNullOrBlank(getPreNominal())) {
            MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "preNominal", getPreNominal());
        }
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "givenName", getGivenName());
        MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "surname", getSurname());
        if (!StringUtility.isNullOrBlank(getPostNominal())) {
            MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "postNominal", getPostNominal());
        }

        if (getMaidenName() != null) { // this is only a != null comparison because empty is a use case for divorce
            MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "maidenName", getMaidenName());
        }

        if (!StringUtility.isNullOrBlank(getCallsign())) {
            MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "callsign", getCallsign());
        }

        if (getBloodname() != null) {
            getBloodname().writeToXML(pw, indent);
        }
        MekHqXmlUtil.writeSimpleXMLCloseTag(pw, --indent, "name");
    }

    /**
     * @param nl the node list to create the Name object from
     * @throws Exception if the nodes are malformed, or there aren't any nodes
     */
    public void fillFromXML(final NodeList nl) throws Exception {
        if (nl.getLength() == 0) {
            throw new Exception("Cannot create a Name from an empty node list");
        }

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
                    setBloodnameDirect(Bloodname.fillFromXML(wn));
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
        return getFullName();
    }

    @Override
    public Name clone() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
