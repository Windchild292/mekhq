/*
 * Person.java
 *
 * Copyright (c) 2009 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.personnel;

import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import megamek.common.*;
import megamek.common.util.EncodeControl;
import megamek.common.util.StringUtil;
import megamek.common.util.WeightedMap;
import mekhq.campaign.*;
import mekhq.campaign.finances.Money;
import mekhq.campaign.log.*;
import mekhq.campaign.personnel.enums.*;
import org.joda.time.DateTime;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.annotations.Nullable;
import megamek.common.logging.LogLevel;
import megamek.common.options.IOption;
import megamek.common.options.IOptionGroup;
import megamek.common.options.PilotOptions;
import mekhq.MekHQ;
import mekhq.MekHqXmlSerializable;
import mekhq.MekHqXmlUtil;
import mekhq.Utilities;
import mekhq.Version;
import mekhq.campaign.event.PersonChangedEvent;
import mekhq.campaign.mod.am.InjuryUtil;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.work.IPartWork;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.Planet;

/**
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Person implements Serializable, MekHqXmlSerializable {
    //region Variable Declarations
    private static final long serialVersionUID = -847642980395311152L;

    /* If any new roles are added they should go at the end. They should also be accounted for
     * in isCombatRole(int) or isSupportRole(int). You should also increase the value of T_NUM
     * if you add new roles.
     */
    public static final int T_NONE = 0; // Support Role
    public static final int T_MECHWARRIOR = 1; // Start of Combat Roles
    public static final int T_AERO_PILOT = 2;
    public static final int T_GVEE_DRIVER = 3;
    public static final int T_NVEE_DRIVER = 4;
    public static final int T_VTOL_PILOT = 5;
    public static final int T_VEE_GUNNER = 6;
    public static final int T_BA = 7;
    public static final int T_INFANTRY = 8;
    public static final int T_PROTO_PILOT = 9;
    public static final int T_CONV_PILOT = 10;
    public static final int T_SPACE_PILOT = 11;
    public static final int T_SPACE_CREW = 12;
    public static final int T_SPACE_GUNNER = 13;
    public static final int T_NAVIGATOR = 14; // End of Combat Roles
    public static final int T_MECH_TECH = 15; // Start of Support Roles
    public static final int T_MECHANIC = 16;
    public static final int T_AERO_TECH = 17;
    public static final int T_BA_TECH = 18;
    public static final int T_ASTECH = 19;
    public static final int T_DOCTOR = 20;
    public static final int T_MEDIC = 21;
    public static final int T_ADMIN_COM = 22;
    public static final int T_ADMIN_LOG = 23;
    public static final int T_ADMIN_TRA = 24;
    public static final int T_ADMIN_HR = 25; // End of support roles
    public static final int T_LAM_PILOT = 26; // Not a separate type, but an alias for MW + Aero pilot
                                              // Does not count as either combat or support role
    public static final int T_VEHICLE_CREW = 27; // non-gunner/non-driver support vehicle crew

    // This value should always be +1 of the last defined role
    public static final int T_NUM = 28;

    // Prisoners, Bondsmen, and Normal Personnel
    public static final int PRISONER_NOT = 0;
    public static final int PRISONER_YES = 1;
    public static final int PRISONER_BONDSMAN = 2;

    private static final Map<Integer, Money> MECHWARRIOR_AERO_RANSOM_VALUES;
    private static final Map<Integer, Money> OTHER_RANSOM_VALUES;

    public PersonAwardController awardController;

    //region Family Variables
    // Lineage
    protected UUID ancestorsId;
    protected UUID spouse;
    protected List<FormerSpouse> formerSpouses;

    //region Procreation
    // this is a flag used in random procreation to determine whether or not to attempt to procreate
    private boolean tryingToConceive;
    protected LocalDate dueDate;
    protected LocalDate expectedDueDate;

    private static final int PREGNANCY_STANDARD_DURATION = 268; //standard duration of a pregnancy in days

    // This creates a random range of approximately six weeks with which to modify the standard pregnancy duration
    // To create randomized pregnancy duration
    private static final IntSupplier PREGNANCY_MODIFY_DURATION = () -> {
        double gaussian = Math.sqrt(-2 * Math.log(Math.nextUp(Math.random())))
            * Math.cos(2.0 * Math.PI * Math.random());
        // To not get weird results, we limit the values to +/- 4.0 (almost 6 weeks)
        return (int) Math.round(Math.max(-4.0, Math.min(4.0, gaussian)) * 10);
    };

    private static final IntSupplier PREGNANCY_SIZE = () -> {
        int children = 1;
        // Hellin's law says it's 1:89 chance, to not make it appear too seldom, we use 1:50
        while(Compute.randomInt(50) == 0) {
            ++ children;
        }
        return Math.min(children, 10); // Limit to decuplets, for the sake of sanity
    };

    private static final String[] PREGNANCY_MULTIPLE_NAMES = {null, null,
        "twins", "triplets", "quadruplets", "quintuplets",
        "sextuplets", "septuplets", "octuplets", "nonuplets", "decuplets"
    };

    public static final ExtraData.IntKey PREGNANCY_CHILDREN_DATA = new ExtraData.IntKey("procreation:children");
    public static final ExtraData.StringKey PREGNANCY_FATHER_DATA = new ExtraData.StringKey("procreation:father");
    //endregion Procreation

    //region Marriage
    // this is a flag used in determine whether or not a person is a potential marriage candidate
    // provided that they are not married, are old enough, etc.
    private boolean tryingToMarry;
    // Marriage Surnames
    public static final int SURNAME_NO_CHANGE = 0;
    public static final int SURNAME_YOURS = 1;
    public static final int SURNAME_SPOUSE = 2;
    public static final int SURNAME_HYP_YOURS = 3;
    public static final int SURNAME_BOTH_HYP_YOURS = 4;
    public static final int SURNAME_HYP_SPOUSE = 5;
    public static final int SURNAME_BOTH_HYP_SPOUSE = 6;
    public static final int SURNAME_MALE = 7;
    public static final int SURNAME_FEMALE = 8;
    public static final int SURNAME_WEIGHTED = 9; //should be equal to NUM_SURNAME at all times
    public static final int NUM_SURNAME = 9; //number of surname options not counting the SURNAME_WEIGHTED OPTION

    public static final String[] SURNAME_TYPE_NAMES = new String[] {
        "No Change", "Yours", "Spouse",
        "Yours-Spouse", "Both Yours-Spouse", "Spouse-Yours",
        "Both Spouse-Yours", "Male", "Female"
    };
    //endregion Marriage Variables

    //region Divorce Variables
    public static final String OPT_SELECTED_CHANGE_SURNAME = "selected_change_surname";
    public static final String OPT_SPOUSE_CHANGE_SURNAME = "spouse_change_surname";
    public static final String OPT_BOTH_CHANGE_SURNAME = "both_change_surname";
    public static final String OPT_KEEP_SURNAME = "keep_surname";
    //endregion Divorce Variables
    //endregion Family Variables

    protected UUID id;
    protected int oldId;

    private String fullName;
    private String givenName;
    private String surname;
    private String honorific;
    private String maidenName;
    private String callsign;
    private int gender;

    private int primaryRole;
    private int secondaryRole;

    private ROMDesignation primaryDesignator;
    private ROMDesignation secondaryDesignator;

    protected String biography;
    protected LocalDate birthday;
    protected LocalDate dateOfDeath;
    protected LocalDate recruitment;
    protected LocalDate lastRankChangeDate;
    protected List<LogEntry> personnelLog;
    protected List<LogEntry> missionLog;

    private Skills skills;
    private PersonnelOptions options;
    private int toughness;

    private PersonnelStatus status;
    protected int xp;
    protected int engXp;
    protected int acquisitions;
    protected Money salary;
    private Money totalEarnings;
    private int hits;
    private int prisonerStatus;
    // Is this person willing to defect? Only for prisoners ...
    private boolean willingToDefect;

    boolean dependent;
    boolean commander;

    // Supports edge usage by a ship's engineer composite crewman
    int edgeUsedThisRound;
    // To track how many edge points support personnel have left until next refresh
    int currentEdge;

    //phenotype and background
    private int phenotype;
    private boolean clan;
    private String bloodname;
    private Faction originFaction;
    private Planet originPlanet;

    //assignments
    private UUID unitId;
    protected UUID doctorId;
    private List<UUID> techUnitIds;

    //days of rest
    protected int idleMonths;
    protected int daysToWaitForHealing;

    //region portrait
    protected String portraitCategory;
    protected String portraitFile;
    // runtime override (not saved)
    protected transient String portraitCategoryOverride = null;
    protected transient String portraitFileOverride = null;
    //endregion portrait

    // Our rank
    private int rank;
    private int rankLevel;
    // If this Person uses a custom rank system (-1 for no)
    private int rankSystem;
    private Ranks ranks;

    // Manei Domini "Classes"
    public static final int MD_NONE			= 0;
    public static final int MD_GHOST		= 1;
    public static final int MD_WRAITH		= 2;
    public static final int MD_BANSHEE		= 3;
    public static final int MD_ZOMBIE		= 4;
    public static final int MD_PHANTOM		= 5;
    public static final int MD_SPECTER		= 6;
    public static final int MD_POLTERGEIST	= 7;
    public static final int MD_NUM			= 8;
    private int maneiDominiClass;
    private int maneiDominiRank;

    //stuff to track for support teams
    protected int minutesLeft;
    protected int overtimeLeft;
    protected int nTasks;
    protected boolean engineer;
    public static final int PRIMARY_ROLE_SUPPORT_TIME = 480;
    public static final int PRIMARY_ROLE_OVERTIME_SUPPORT_TIME = 240;
    public static final int SECONDARY_ROLE_SUPPORT_TIME = 240;
    public static final int SECONDARY_ROLE_OVERTIME_SUPPORT_TIME = 120;

    //region Advanced Medical
    private List<Injury> injuries;
    //endregion Advanced Medical

    //region Against the Bot
    private boolean founder; // +1 share if using shares system
    private int originalUnitWeight; // uses EntityWeightClass with 0 (Extra-Light) for no original unit
    public static final int TECH_IS1 = 0;
    public static final int TECH_IS2 = 1;
    public static final int TECH_CLAN = 2;
    private int originalUnitTech;
    private UUID originalUnitId;
    //endregion Against the Bot

    // Generic extra data, for use with plugins and mods
    private ExtraData extraData;

    //lets just go ahead and pass in the campaign - to hell with OOP
    private Campaign campaign;

    // For upgrading personnel entries to missing log entries
    private static String missionParticipatedString;
    private static String getMissionParticipatedString() {
        if (missionParticipatedString == null) {
            ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.LogEntries", new EncodeControl());
            missionParticipatedString = resourceMap.getString("participatedInMission.text");
            missionParticipatedString = missionParticipatedString.substring(0, missionParticipatedString.indexOf(" "));
        }

        return missionParticipatedString;
    }

    // initializes the AtB ransom values
    static {
        MECHWARRIOR_AERO_RANSOM_VALUES = new HashMap<>();
        MECHWARRIOR_AERO_RANSOM_VALUES.put(SkillType.EXP_ULTRA_GREEN, Money.of(5000)); // no official AtB rules for really inexperienced scrubs, but...
        MECHWARRIOR_AERO_RANSOM_VALUES.put(SkillType.EXP_GREEN, Money.of(10000));
        MECHWARRIOR_AERO_RANSOM_VALUES.put(SkillType.EXP_REGULAR, Money.of(25000));
        MECHWARRIOR_AERO_RANSOM_VALUES.put(SkillType.EXP_VETERAN, Money.of(75000));
        MECHWARRIOR_AERO_RANSOM_VALUES.put(SkillType.EXP_ELITE, Money.of(150000));

        OTHER_RANSOM_VALUES = new HashMap<>();
        OTHER_RANSOM_VALUES.put(SkillType.EXP_ULTRA_GREEN, Money.of(2500));
        OTHER_RANSOM_VALUES.put(SkillType.EXP_GREEN, Money.of(5000));
        OTHER_RANSOM_VALUES.put(SkillType.EXP_REGULAR, Money.of(10000));
        OTHER_RANSOM_VALUES.put(SkillType.EXP_VETERAN, Money.of(25000));
        OTHER_RANSOM_VALUES.put(SkillType.EXP_ELITE, Money.of(50000));
    }

    private final static String DATE_DISPLAY_FORMAT = "yyyy-MM-dd";

    //region Reverse Compatibility
    private int oldUnitId = -1;
    private int oldDoctorId = -1;
    //endregion Reverse Compatibility
    //endregion Variable Declarations

    //region Constructors
    //default constructor
    public Person(Campaign campaign) {
        this(Crew.UNNAMED, Crew.UNNAMED_SURNAME, campaign);
    }

    public Person(Campaign campaign, String factionCode) {
        this(Crew.UNNAMED, Crew.UNNAMED_SURNAME, campaign, factionCode);
    }

    public Person(String givenName, String surname, Campaign campaign) {
        this(givenName, surname, campaign, campaign.getFactionCode());
    }

    public Person(String givenName, String surname, Campaign campaign, String factionCode) {
        this(givenName, surname, "", campaign, factionCode);
    }

    /**
     * Primary Person constructor, variables are initialized in the exact same order as they are
     * saved to the XML file
     * @param givenName     the person's given name
     * @param surname       the person's surname
     * @param honorific     the person's honorific
     * @param campaign      the campaign this person is a part of
     * @param factionCode   the faction this person was borne into
     */
    public Person(String givenName, String surname, String honorific, Campaign campaign,
                  String factionCode) {
        // First, we assign campaign
        this.campaign = campaign;

        // Then, we assign the variables in XML file order
        id = null;
        this.givenName = givenName;
        this.surname = surname;
        this.honorific = honorific;
        maidenName = null; // this is set to null to handle divorce cases
        callsign = "";
        primaryRole = T_NONE;
        secondaryRole = T_NONE;
        primaryDesignator = ROMDesignation.NONE;
        secondaryDesignator = ROMDesignation.NONE;
        commander = false;
        dependent = false;
        originFaction = Faction.getFaction(factionCode);
        originPlanet = null;
        clan = originFaction.isClan();
        phenotype = Phenotype.P_NONE;
        bloodname = "";
        biography = "";
        idleMonths = -1;
        ancestorsId = null;
        spouse = null;
        formerSpouses = new ArrayList<>();
        tryingToMarry = true;
        tryingToConceive = true;
        dueDate = null;
        expectedDueDate = null;
        portraitCategory = Crew.ROOT_PORTRAIT;
        portraitFile = Crew.PORTRAIT_NONE;
        xp = 0;
        daysToWaitForHealing = 0;
        gender = Crew.G_MALE;
        rank = 0;
        rankLevel = 0;
        rankSystem = -1;
        maneiDominiRank = Rank.MD_RANK_NONE;
        maneiDominiClass = MD_NONE;
        nTasks = 0;
        doctorId = null;
        unitId = null;
        salary = Money.of(-1);
        totalEarnings = Money.of(0);
        status = PersonnelStatus.ACTIVE;
        prisonerStatus = PRISONER_NOT;
        willingToDefect = false;
        hits = 0;
        toughness = 0;
        resetMinutesLeft(); // this assigns minutesLeft and overtimeLeft
        birthday = null;
        dateOfDeath = null;
        recruitment = null;
        lastRankChangeDate = null;
        skills = new Skills();
        options = new PersonnelOptions();
        currentEdge = 0;
        techUnitIds = new ArrayList<>();
        personnelLog = new ArrayList<>();
        missionLog = new ArrayList<>();
        awardController = new PersonAwardController(this);
        injuries = new ArrayList<>();
        founder = false;
        originalUnitWeight = EntityWeightClass.WEIGHT_ULTRA_LIGHT;
        originalUnitTech = TECH_IS1;
        originalUnitId = null;
        acquisitions = 0;
        extraData = new ExtraData();

        // Initialize Data based on these settings
        setFullName();
    }
    //endregion Constructors

    public Campaign getCampaign(){return campaign;}

    public int getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(int i) {
        phenotype = i;
    }

    public boolean isClanner() {
        return clan;
    }

    public void setClanner(boolean b) {
        clan = b;
    }

    public String getBloodname() {
        return bloodname;
    }

    public void setBloodname(String bn) {
        bloodname = bn;
    }

    public Faction getOriginFaction() {
        return originFaction;
    }

    public void setOriginFaction(Faction f) {
        originFaction = f;
    }

    public Planet getOriginPlanet() {
        return originPlanet;
    }

    public void setOriginPlanet(Planet p) {
        originPlanet = p;
    }

    public boolean isCommander() {
        return commander;
    }

    public void setCommander(boolean tf) {
        commander = tf;
    }

    public boolean isDependent() {
        return dependent;
    }

    public void setDependent(boolean tf) {
        dependent = tf;
        if (dependent) {
            setRecruitment(null);
            setLastRankChangeDate(null);
        } else {
            setRecruitment(getCampaign().getLocalDate());
            setLastRankChangeDate(getCampaign().getLocalDate());
        }
    }

    public boolean isPrisoner() {
        return prisonerStatus == PRISONER_YES;
    }

    public void setPrisoner() {
        prisonerStatus = PRISONER_YES;
        setRankNumeric(Ranks.RANK_PRISONER);
    }

    public boolean isBondsman() {
        return prisonerStatus == PRISONER_BONDSMAN;
    }

    public void setBondsman() {
        prisonerStatus = PRISONER_BONDSMAN;
        willingToDefect = false;
        setRankNumeric(Ranks.RANK_BONDSMAN);
    }

    public boolean isFree() {
        return (!isPrisoner() && !isBondsman());
    }

    public void setFreeMan() {
        prisonerStatus = PRISONER_NOT;
        willingToDefect = false;
    }

    public int getPrisonerStatus() {
        return prisonerStatus;
    }

    public boolean isWillingToDefect() {
        return willingToDefect;
    }

    public void setWillingToDefect(boolean willingToDefect) {
        this.willingToDefect = willingToDefect && (prisonerStatus == PRISONER_YES);
    }

    //region Text Getters
    public String pregnancyStatus() {
        return isPregnant() ? " (Pregnant)" : "";
    }

    public static String getPrisonerStatusName(int status) {
        switch (status) {
            case PRISONER_NOT:
                return "Free";
            case PRISONER_YES:
                return "Prisoner";
            case PRISONER_BONDSMAN:
                return "Bondsman";
            default:
                return "?";
        }
    }
    //endregion Text Getters

    //region Names
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String n) {
        this.givenName = n;
        setFullName();
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String n) {
        this.surname = n;
        setFullName();
    }

    public String getHonorific() {
        return honorific;
    }

    public void setHonorific(String n) {
        this.honorific = n;
        setFullName();
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String n) {
        this.maidenName = n;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName() {
        if (isClanner()) {
            if (!StringUtil.isNullOrEmpty(bloodname)) {
                fullName = givenName + " " + bloodname;
            } else {
                fullName = givenName;
            }
        } else {
            if (!StringUtil.isNullOrEmpty(surname)) {
                fullName = givenName + " " + surname;
            } else {
                fullName = givenName;
            }
        }

        if (!StringUtil.isNullOrEmpty(honorific)) {
            fullName += " " + honorific;
        }
    }

    /**
     * This method is used to migrate names from being a joined name to split between given name and
     * surname.
     * This is required to migrate personnel between MegaMek and MekHQ, as MekHQ tracks divided names
     * and MegaMek does not.
     * @param n the name to be migrated
     */
    public void migrateName(String n) {
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
        String[] name = n.trim().split("\\s+");

        givenName = name[0];

        if (isClanner()) {
            if (name.length > 1) {
                int i;
                for (i = 1; i < name.length - 1; i++) {
                    givenName += " " + name[i];
                }

                if (!(!StringUtil.isNullOrEmpty(getBloodname()) && getBloodname().equals(name[i]))) {
                    givenName += " " + name[i];
                }
            }
        } else {
            if (name.length == 2) {
                surname = name[1];
            } else if (name.length == 3) {
                surname = name[1] + " " + name[2];
            } else if (name.length > 3) {
                int i;
                for (i = 1; i < name.length - 2; i++) {
                    givenName += " " + name[i];
                }

                surname = name[i] + " " + name[i + 1];
            }
        }

        if ((surname == null) || (surname.equals(Crew.UNNAMED_SURNAME))) {
            surname = "";
        }

        setFullName();
    }

    public String getHyperlinkedName() {
        return String.format("<a href='PERSON:%s'>%s</a>", getId().toString(), getFullName());
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String n) {
        this.callsign = n;
    }
    //endregion Names

    public String getPortraitCategory() {
        return Utilities.nonNull(portraitCategoryOverride, portraitCategory);
    }

    public String getPortraitFileName() {
        return Utilities.nonNull(portraitFileOverride, portraitFile);
    }

    public void setPortraitCategory(String s) {
        this.portraitCategory = s;
    }

    public void setPortraitFileName(String s) {
        this.portraitFile = s;
    }

    public void setPortraitCategoryOverride(String s) {
        this.portraitCategoryOverride = s;
    }

    public void setPortraitFileNameOverride(String s) {
        this.portraitFileOverride = s;
    }

    //region Personnel Roles
    // TODO : Move me into an enum with checks for hasAdministratorRole, hasTechRole, hasMedicalRole, etc.
    public int getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(int t) {
        this.primaryRole = t;
        //you can't be primary tech and a secondary astech
        //you can't be a primary astech and a secondary tech
        if ((isTechPrimary() && secondaryRole == T_ASTECH)
            || (isTechSecondary() && primaryRole == T_ASTECH)) {
            secondaryRole = T_NONE;
        }
        if ((primaryRole == T_DOCTOR && secondaryRole == T_MEDIC)
            || (secondaryRole == T_DOCTOR && primaryRole == T_MEDIC)) {
            secondaryRole = T_NONE;
        }
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public int getSecondaryRole() {
        return secondaryRole;
    }

    public void setSecondaryRole(int t) {
        this.secondaryRole = t;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    /**
     * This is used to determine if a person has a specific role as either their primary OR their
     * secondary role
     * @param role the role to determine
     * @return true if the person has the specific role either as their primary or secondary role
     */
    public boolean hasRole(int role) {
        return (getPrimaryRole() == role) || (getSecondaryRole() == role);
    }

    /**
     * This is used to determine whether the person has either a primary or secondary role within the
     * two bounds. This is inclusive of the two bounds
     * @param minimumRole the minimum role bound (inclusive)
     * @param maximumRole the maximum role bound (inclusive)
     * @return true if they have a role within the bounds (inclusive), otherwise false.
     */
    public boolean hasRoleWithin(int minimumRole, int maximumRole) {
        return hasPrimaryRoleWithin(minimumRole, maximumRole)
                || hasSecondaryRoleWithin(minimumRole, maximumRole);
    }

    /**
     * @param minimumRole the minimum role bound (inclusive)
     * @param maximumRole the maximum role bound (inclusive)
     * @return true if they have a primary role within the bounds (inclusive), otherwise false
     */
    public boolean hasPrimaryRoleWithin(int minimumRole, int maximumRole) {
        return (getPrimaryRole() >= minimumRole) && (getPrimaryRole() <= maximumRole);
    }

    /**
     * @param minimumRole the minimum role bound (inclusive)
     * @param maximumRole the maximum role bound (inclusive)
     * @return true if they have a secondary role within the bounds (inclusive), otherwise false
     */
    public boolean hasSecondaryRoleWithin(int minimumRole, int maximumRole) {
        return (getSecondaryRole() >= minimumRole) && (getSecondaryRole() <= maximumRole);
    }

    /**
     * @return true if the person has a primary or secondary combat role
     */
    public boolean hasCombatRole() {
        return hasPrimaryCombatRole() || hasSecondaryCombatRole();
    }

    /**
     * @return true if the person has a primary combat role
     */
    public boolean hasPrimaryCombatRole() {
        return hasPrimaryRoleWithin(T_MECHWARRIOR, T_SPACE_GUNNER)
                || hasPrimaryRoleWithin(T_LAM_PILOT, T_VEHICLE_CREW);
    }

    /**
     * @return true if the person has a secondary combat role
     */
    public boolean hasSecondaryCombatRole() {
        return hasSecondaryRoleWithin(T_MECHWARRIOR, T_SPACE_GUNNER)
                || hasSecondaryRoleWithin(T_LAM_PILOT, T_VEHICLE_CREW);
    }

    /**
     * @param includeNoRole whether to include T_NONE in the primary check or not
     * @return true if the person has a primary or secondary support role, except for secondary T_NONE
     */
    public boolean hasSupportRole(boolean includeNoRole) {
        return hasPrimarySupportRole(includeNoRole) || hasSecondarySupportRole();
    }

    /**
     * @param includeNoRole whether to include T_NONE in the check check or not
     * @return true if the person has a primary support role
     */
    public boolean hasPrimarySupportRole(boolean includeNoRole) {
        return hasPrimaryRoleWithin(T_MECH_TECH, T_ADMIN_HR) || (includeNoRole && (getPrimaryRole() == T_NONE));
    }

    /**
     * @return true if the person has a secondary support role. Note that T_NONE is NOT a support
     * role if it is a secondary role
     */
    public boolean hasSecondarySupportRole() {
        return hasSecondaryRoleWithin(T_MECH_TECH, T_ADMIN_HR);
    }

    /**
     * Determines whether a role is considered a combat role. Note that T_LAM_PILOT is a special
     * placeholder which is not used for either primary or secondary role and will return false.
     *
     * @param role A value that can be used for a person's primary or secondary role.
     * @return     Whether the role is considered a combat role
     */
    public static boolean isCombatRole(int role) {
        return ((role > T_NONE) && (role <= T_NAVIGATOR))
                || (role == T_VEHICLE_CREW);
    }

    /**
     * @param role A value that can be used for a person's primary or secondary role.
     * @return     Whether the role is considered a support role
     */
    public static boolean isSupportRole(int role) {
        return (role >= T_MECH_TECH) && (role < T_LAM_PILOT);
    }
    //endregion Personnel Roles

    public PersonnelStatus getStatus() {
        return status;
    }

    public void setStatus(PersonnelStatus status) {
        this.status = status;
    }

    public int getIdleMonths() {
        return idleMonths;
    }

    public void setIdleMonths(int m) {
        this.idleMonths = m;
    }

    public int getDaysToWaitForHealing() {
        return daysToWaitForHealing;
    }

    public void setDaysToWaitForHealing(int d) {
        this.daysToWaitForHealing = d;
    }

    public static String getRoleDesc(int type, boolean clan) {
        switch (type) {
            case (T_NONE):
                return "None";
            case (T_MECHWARRIOR):
                return "MechWarrior";
            case (T_GVEE_DRIVER):
                return "Vehicle Driver";
            case (T_NVEE_DRIVER):
                return "Naval Driver";
            case (T_VTOL_PILOT):
                return "VTOL Pilot";
            case (T_VEE_GUNNER):
                return "Vehicle Gunner";
            case (T_CONV_PILOT):
                return "Conventional Aircraft Pilot";
            case (T_AERO_PILOT):
                return "Aerospace Pilot";
            case (T_PROTO_PILOT):
                return "ProtoMech Pilot";
            case (T_BA):
                if (clan) {
                    return "Elemental";
                } else {
                    return "Battle Armor Pilot";
                }
            case (T_INFANTRY):
                return "Soldier";
            case (T_SPACE_PILOT):
                return "Vessel Pilot";
            case (T_SPACE_CREW):
                return "Vessel Crewmember";
            case (T_SPACE_GUNNER):
                return "Vessel Gunner";
            case (T_NAVIGATOR):
                return "Hyperspace Navigator";
            case (T_MECH_TECH):
                return "Mech Tech";
            case (T_MECHANIC):
                return "Mechanic";
            case (T_AERO_TECH):
                return "Aero Tech";
            case (T_BA_TECH):
                return "Battle Armor Tech";
            case (T_ASTECH):
                return "Astech";
            case (T_DOCTOR):
                return "Doctor";
            case (T_MEDIC):
                return "Medic";
            case (T_ADMIN_COM):
                return "Admin/Command";
            case (T_ADMIN_LOG):
                return "Admin/Logistical";
            case (T_ADMIN_TRA):
                return "Admin/Transport";
            case (T_ADMIN_HR):
                return "Admin/HR";
            case (T_LAM_PILOT):
                return "LAM Pilot";
            case (T_VEHICLE_CREW):
                return "Vehicle Crew";
            default:
                return "??";
        }
    }

    public String getRoleDesc() {
        String role = getPrimaryRoleDesc();
        if (secondaryRole != T_NONE && secondaryRole != -1) {
            role += "/" + getSecondaryRoleDesc();
        }
        return role;
    }

    public String getPrimaryRoleDesc() {
        String bgPrefix = "";
        if (isClanner()) {
            bgPrefix = Phenotype.getPhenotypeShortName(phenotype) + " ";
        }
        return bgPrefix + getRoleDesc(primaryRole, campaign.getFaction().isClan());
    }

    public String getSecondaryRoleDesc() {
        return getRoleDesc(secondaryRole, campaign.getFaction().isClan());
    }

    public static int getRoleMnemonic(int type) {
        // The following characters are unused:
        // J, K, Q, X, Z
        switch (type) {
            case T_MECHWARRIOR:
                return KeyEvent.VK_M;
            case T_GVEE_DRIVER:
                return KeyEvent.VK_V;
            case T_NVEE_DRIVER:
                return KeyEvent.VK_N;
            case T_VEE_GUNNER:
                return KeyEvent.VK_G;
            case T_AERO_PILOT:
                return KeyEvent.VK_A;
            case T_PROTO_PILOT:
                return KeyEvent.VK_P;
            case T_CONV_PILOT:
                return KeyEvent.VK_F;
            case T_BA:
                return KeyEvent.VK_B;
            case T_INFANTRY:
                return KeyEvent.VK_S;
            case T_SPACE_PILOT:
                return KeyEvent.VK_I;
            case T_SPACE_CREW:
                return KeyEvent.VK_W;
            case T_SPACE_GUNNER:
                return KeyEvent.VK_U;
            case T_NAVIGATOR:
                return KeyEvent.VK_Y;
            case T_MECH_TECH:
                return KeyEvent.VK_T;
            case T_MECHANIC:
                return KeyEvent.VK_E;
            case T_AERO_TECH:
                return KeyEvent.VK_O;
            case T_DOCTOR:
                return KeyEvent.VK_D;
            case T_ADMIN_COM:
                return KeyEvent.VK_C;
            case T_ADMIN_LOG:
                return KeyEvent.VK_L;
            case T_ADMIN_TRA:
                return KeyEvent.VK_R;
            case T_ADMIN_HR:
                return KeyEvent.VK_H;
            case T_VTOL_PILOT:
            case T_BA_TECH:
            case T_ASTECH:
            case T_MEDIC:
            case T_LAM_PILOT:
            case T_VEHICLE_CREW:
            case T_NONE:
            default:
                return KeyEvent.VK_UNDEFINED;
        }
    }

    public boolean canPerformRole(int role) {
        switch (role) {
            case (T_NONE):
                return true;
            case (T_MECHWARRIOR):
                return hasSkill(SkillType.S_GUN_MECH) && hasSkill(SkillType.S_PILOT_MECH);
            case (T_GVEE_DRIVER):
                return hasSkill(SkillType.S_PILOT_GVEE);
            case (T_NVEE_DRIVER):
                return hasSkill(SkillType.S_PILOT_NVEE);
            case (T_VTOL_PILOT):
                return hasSkill(SkillType.S_PILOT_VTOL);
            case (T_VEE_GUNNER):
                return hasSkill(SkillType.S_GUN_VEE);
            case (T_AERO_PILOT):
                return hasSkill(SkillType.S_GUN_AERO) && hasSkill(SkillType.S_PILOT_AERO);
            case (T_CONV_PILOT):
                return hasSkill(SkillType.S_GUN_JET) && hasSkill(SkillType.S_PILOT_JET);
            case (T_PROTO_PILOT):
                return hasSkill(SkillType.S_GUN_PROTO);
            case (T_BA):
                return hasSkill(SkillType.S_GUN_BA);
            case (T_INFANTRY):
                return hasSkill(SkillType.S_SMALL_ARMS);
            case (T_SPACE_PILOT):
                return hasSkill(SkillType.S_PILOT_SPACE);
            case (T_SPACE_CREW):
                return hasSkill(SkillType.S_TECH_VESSEL);
            case (T_SPACE_GUNNER):
                return hasSkill(SkillType.S_GUN_SPACE);
            case (T_NAVIGATOR):
                return hasSkill(SkillType.S_NAV);
            case (T_MECH_TECH):
                return hasSkill(SkillType.S_TECH_MECH) && getSkill(SkillType.S_TECH_MECH).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case (T_MECHANIC):
            case T_VEHICLE_CREW:
                return hasSkill(SkillType.S_TECH_MECHANIC) && getSkill(SkillType.S_TECH_MECHANIC).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case (T_AERO_TECH):
                return hasSkill(SkillType.S_TECH_AERO) && getSkill(SkillType.S_TECH_AERO).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case (T_BA_TECH):
                return hasSkill(SkillType.S_TECH_BA) && getSkill(SkillType.S_TECH_BA).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case (T_ASTECH):
                return hasSkill(SkillType.S_ASTECH);
            case (T_DOCTOR):
                return hasSkill(SkillType.S_DOCTOR) && getSkill(SkillType.S_DOCTOR).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case (T_MEDIC):
                return hasSkill(SkillType.S_MEDTECH);
            case (T_ADMIN_COM):
            case (T_ADMIN_LOG):
            case (T_ADMIN_TRA):
            case (T_ADMIN_HR):
                return hasSkill(SkillType.S_ADMIN);
            default:
                return false;
        }
    }

    public void setGender(int g) {
        this.gender = g;
    }

    public int getGender() {
        return gender;
    }

    public void setBirthday(LocalDate date) {
        this.birthday = date;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public LocalDate getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(LocalDate date) {
        this.dateOfDeath = date;
    }

    public void setRecruitment(LocalDate date) {
        this.recruitment = date;
    }

    public LocalDate getRecruitment() {
        return recruitment;
    }

    public String getRecruitmentAsString() {
        if (getRecruitment() == null) {
            return null;
        } else {
            return getRecruitment().format(DateTimeFormatter.ofPattern(DATE_DISPLAY_FORMAT));
        }
    }

    public void setLastRankChangeDate(LocalDate date) {
        this.lastRankChangeDate = date;
    }

    public LocalDate getLastRankChangeDate() {
        return lastRankChangeDate;
    }

    public String getLastRankChangeDateAsString() {
        if (getLastRankChangeDate() == null) {
            return null;
        } else {
            return getLastRankChangeDate().format(DateTimeFormatter.ofPattern(DATE_DISPLAY_FORMAT));
        }
    }

    public int getAge(LocalDate today) {
        // Get age based on year
        if (getDateOfDeath() != null) {
            //use date of death instead of birthday
            today = getDateOfDeath();
        }

        return Math.toIntExact(ChronoUnit.YEARS.between(getBirthday(), today));
    }

    public String getTimeInService(Campaign campaign) {
        // Get time in service based on year
        if (getRecruitment() == null) {
            //use "" they haven't been recruited or are dependents
            return "";
        }

        LocalDate today = campaign.getLocalDate();

        // If the person is dead, we only care about how long they spent in service to the company
        if (getDateOfDeath() != null) {
            //use date of death instead of the current day
            today = getDateOfDeath();
        }

        return campaign.getCampaignOptions().getTimeInServiceDisplayFormat()
                .getDisplayFormattedOutput(getRecruitment(), today);
    }

    public String getTimeInRank(Campaign campaign) {
        if (getLastRankChangeDate() == null) {
            return "";
        }

        LocalDate today = campaign.getLocalDate();

        // If the person is dead, we only care about how long it was from their last promotion till they died
        if (getDateOfDeath() != null) {
            //use date of death instead of the current day
            today = getDateOfDeath();
        }

        return campaign.getCampaignOptions().getTimeInRankDisplayFormat()
                .getDisplayFormattedOutput(getLastRankChangeDate(), today);
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public boolean isChild() {
        return (getAge(getCampaign().getLocalDate()) <= 13);
    }

    //region Pregnancy
    public boolean isTryingToConceive() {
        return tryingToConceive;
    }

    public void setTryingToConceive(boolean tryingToConceive) {
        this.tryingToConceive = tryingToConceive;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getExpectedDueDate() {
        return expectedDueDate;
    }

    public void setExpectedDueDate(LocalDate expectedDueDate) {
        this.expectedDueDate = expectedDueDate;
    }

    public boolean isPregnant() {
        return dueDate != null;
    }

    /**
     * This is used to determine if a person can procreate
     * @return true if they can, otherwise false
     */
    public boolean canProcreate() {
        return isFemale() && tryingToConceive && !isPregnant() && !isDeployed()
                && !isChild() && (getAge(getCampaign().getLocalDate()) < 51);
    }

    public void procreate() {
        if (canProcreate()) {
            boolean conceived = false;
            if (hasSpouse()) {
                if (!getSpouse().isDeployed() && !getSpouse().isDeadOrMIA() && !getSpouse().isChild()
                        && !(getSpouse().getGender() == getGender())) {
                    // setting is the decimal chance that this procreation attempt will create a child, base is 0.05%
                    conceived = (Compute.randomFloat() < (campaign.getCampaignOptions().getChanceProcreation()));
                }
            } else if (campaign.getCampaignOptions().useUnofficialProcreationNoRelationship()) {
                // setting is the decimal chance that this procreation attempt will create a child, base is 0.005%
                conceived = (Compute.randomFloat() < (campaign.getCampaignOptions().getChanceProcreationNoRelationship()));
            }

            if (conceived) {
                addPregnancy();
            }
        }
    }

    public void addPregnancy() {
        LocalDate dueDate = getCampaign().getLocalDate();
        dueDate = dueDate.plus(PREGNANCY_STANDARD_DURATION, ChronoUnit.DAYS);
        setExpectedDueDate(dueDate);
        dueDate = dueDate.plus(PREGNANCY_MODIFY_DURATION.getAsInt(), ChronoUnit.DAYS);
        setDueDate(dueDate);

        int size = PREGNANCY_SIZE.getAsInt();
        extraData.set(PREGNANCY_CHILDREN_DATA, size);
        extraData.set(PREGNANCY_FATHER_DATA, (hasSpouse()) ? getSpouseId().toString() : null);

        String sizeString = (size < PREGNANCY_MULTIPLE_NAMES.length) ? PREGNANCY_MULTIPLE_NAMES[size] : null;

        campaign.addReport(getHyperlinkedName() + " has conceived" + (sizeString == null ? "" : (" " + sizeString)));
        if (campaign.getCampaignOptions().logConception()) {
            MedicalLogger.hasConceived(this, campaign.getDate(), sizeString);
            if (hasSpouse()) {
                PersonalLogger.spouseConceived(getSpouse(), getFullName(), campaign.getDate(), sizeString);
            }
        }
    }

    /**
     * Removes a pregnancy and clears all related data from the current person
     */
    public void removePregnancy() {
        setDueDate(null);
        setExpectedDueDate(null);
        extraData.set(PREGNANCY_CHILDREN_DATA, null);
        extraData.set(PREGNANCY_FATHER_DATA, null);
    }

    public Collection<Person> birth() {
        int size = extraData.get(PREGNANCY_CHILDREN_DATA, 1);
        String fatherIdString = extraData.get(PREGNANCY_FATHER_DATA);
        UUID fatherId = (fatherIdString != null) ? UUID.fromString(fatherIdString) : null;
        Ancestors anc = campaign.getAncestors(fatherId, id);
        if (null == anc) {
            anc = campaign.createAncestors(fatherId, id);
        }
        final UUID ancId = anc.getId();

        final String surname = generateBabySurname(fatherId);

        // Cleanup
        removePregnancy();

        return IntStream.range(0, size).mapToObj(i -> {
            Person baby = campaign.newDependent(T_NONE, true);
            baby.setSurname(surname);
            baby.setBirthday(getCampaign().getLocalDate());
            UUID babyId = UUID.randomUUID();

            baby.setId(babyId);
            baby.setAncestorsId(ancId);

            campaign.addReport(String.format("%s has given birth to %s, a baby %s!", getHyperlinkedName(),
                    baby.getHyperlinkedName(), GenderDescriptors.BOY_GIRL.getDescriptor(baby.getGender())));
            if (campaign.getCampaignOptions().logConception()) {
                MedicalLogger.deliveredBaby(this, baby, campaign.getDate());
                if (fatherId != null) {
                    PersonalLogger.ourChildBorn(campaign.getPerson(fatherId), baby, getFullName(), campaign.getDate());
                }
            }
            return baby;
        }).collect(Collectors.toList());
    }

    /**
     * This determines a baby's surname based on their parents names and the selected method
     * @param fatherId the father's UUID
     * @return the surname to use
     */
    private String generateBabySurname(UUID fatherId) {
        switch (campaign.getCampaignOptions().getBabySurnameStyle()) {
            case CampaignOptions.BABY_SURNAME_SPOUSE: {
                Person father = campaign.getPerson(fatherId);
                if (father != null) {
                    return father.getSurname();
                } else {
                    return surname;
                }
            }
            case CampaignOptions.BABY_SURNAME_MOTHER_DASH_FATHER: {
                Person father = campaign.getPerson(fatherId);
                if (father != null) {
                    if (!StringUtil.isNullOrEmpty(surname) && !StringUtil.isNullOrEmpty(father.getSurname())) {
                        return surname + "-" + father.getSurname();
                    } else if (!StringUtil.isNullOrEmpty(father.getSurname())) {
                        return father.getSurname();
                    }
                }
                return surname;
            }
            case CampaignOptions.BABY_SURNAME_FATHER_DASH_MOTHER: {
                Person father = campaign.getPerson(fatherId);
                if (father != null) {
                    if (!StringUtil.isNullOrEmpty(surname) && !StringUtil.isNullOrEmpty(father.getSurname())) {
                        return father.getSurname() + "-" + surname;
                    } else if (!StringUtil.isNullOrEmpty(surname)) {
                        return surname;
                    } else {
                        return father.getSurname();
                    }
                } else {
                    return surname;
                }
            }
            case CampaignOptions.BABY_SURNAME_MINE:
            default:
                return surname;
        }
    }
    //endregion Pregnancy

    //region Marriage
    public boolean isTryingToMarry() {
        return tryingToMarry;
    }

    public void setTryingToMarry(boolean tryingToMarry) {
        this.tryingToMarry = tryingToMarry;
    }

    /**
     * Determines if another person is a safe spouse for the current person
     * @param p the person to determine if they are a safe spouse
     */
    public boolean safeSpouse(Person p) {
        // Huge convoluted return statement, with the following restrictions
        // can't marry yourself
        // can't marry someone who is already married
        // can't marry someone who doesn't want to be married
        // can't marry a prisoner, unless you are also a prisoner (this is purposely left open for prisoners to marry who they want)
        // can't marry a person who is dead or MIA
        // can't marry inactive personnel (this is to show how they aren't part of the force anymore)
        // TODO : can't marry anyone who is not located at the same planet as the person - GitHub #1672: Implement current planet tracking for personnel
        // can't marry a close relative
        return (
                !this.equals(p)
                && !p.hasSpouse()
                && p.isTryingToMarry()
                && p.oldEnoughToMarry()
                && (!p.isPrisoner() || isPrisoner())
                && !p.isDeadOrMIA()
                && p.isActive()
                && ((getAncestorsId() == null)
                    || !getCampaign().getAncestors(getAncestorsId()).checkMutualAncestors(
                            getCampaign().getAncestors(p.getAncestorsId())))
        );
    }

    public boolean oldEnoughToMarry() {
        return (getAge(getCampaign().getLocalDate()) >= getCampaign().getCampaignOptions().getMinimumMarriageAge());
    }

    public void randomMarriage() {
        // Don't attempt to generate is someone isn't trying to marry, has a spouse,
        // isn't old enough to marry, or is actively deployed
        if (!tryingToMarry || hasSpouse() || !oldEnoughToMarry() || isDeployed()) {
            return;
        }

        // setting is the fractional chance that this attempt at finding a marriage will result in one
        if (Compute.randomFloat() < (getCampaign().getCampaignOptions().getChanceRandomMarriages())) {
            addRandomSpouse(false);
        } else if (getCampaign().getCampaignOptions().useRandomSameSexMarriages()) {
            if (Compute.randomFloat() < (getCampaign().getCampaignOptions().getChanceRandomSameSexMarriages())) {
                addRandomSpouse(true);
            }
        }
    }

    public void addRandomSpouse(boolean sameSex) {
        List<Person> potentials = new ArrayList<>();
        int gender = sameSex ? getGender() : (isMale() ? Crew.G_FEMALE : Crew.G_MALE);
        for (Person p : getCampaign().getActivePersonnel()) {
            if (isPotentialRandomSpouse(p, gender)) {
                potentials.add(p);
            }
        }

        int n = potentials.size();
        if (n > 0) {
            marry(potentials.get(Compute.randomInt(n)), SURNAME_WEIGHTED);
        }
    }

    public boolean isPotentialRandomSpouse(Person p, int gender) {
        if ((p.getGender() != gender) || !safeSpouse(p) || !(isFree() || (isPrisoner() && p.isPrisoner()))) {
            return false;
        }

        int ageDifference = Math.abs(p.getAge(getCampaign().getLocalDate()) - getAge(getCampaign().getLocalDate()));

        return (ageDifference <= getCampaign().getCampaignOptions().getMarriageAgeRange());
    }

    public void marry(Person spouse, int surnameOption) {
        String surname = getSurname();
        String spouseSurname = spouse.getSurname();

        if (surnameOption == SURNAME_WEIGHTED) {
            WeightedMap<Integer> map = createWeightedSurnameMap();
            surnameOption = map.randomItem();
        }

        switch(surnameOption) {
            case SURNAME_NO_CHANGE:
                break;
            case SURNAME_SPOUSE:
                setSurname(spouseSurname);
                setMaidenName(surname); //"" is handled in the divorce code
                break;
            case SURNAME_YOURS:
                spouse.setSurname(surname);
                spouse.setMaidenName(spouseSurname); //"" is handled in the divorce code
                break;
            case SURNAME_HYP_YOURS:
                if (!StringUtil.isNullOrEmpty(surname) && !StringUtil.isNullOrEmpty(spouseSurname)) {
                    setSurname(surname + "-" + spouseSurname);
                } else {
                    setSurname(spouseSurname);
                }

                setMaidenName(surname); //"" is handled in the divorce code
                break;
            case SURNAME_BOTH_HYP_YOURS:
                if (!StringUtil.isNullOrEmpty(surname) && !StringUtil.isNullOrEmpty(spouseSurname)) {
                    setSurname(surname + "-" + spouseSurname);
                    spouse.setSurname(surname + "-" + spouseSurname);
                } else if (!StringUtil.isNullOrEmpty(spouseSurname)) {
                    setSurname(spouseSurname);
                } else if (!StringUtil.isNullOrEmpty(surname)) {
                    spouse.setSurname(surname);
                }
                //both null or "" is ignored as a case, as it would lead to no changes

                setMaidenName(surname); //"" is handled in the divorce code
                spouse.setMaidenName(spouseSurname); //"" is handled in the divorce code
                break;
            case SURNAME_HYP_SPOUSE:
                if (!StringUtil.isNullOrEmpty(surname) && !StringUtil.isNullOrEmpty(spouseSurname)) {
                    spouse.setSurname(spouseSurname + "-" + surname);
                } else {
                    spouse.setSurname(surname);
                }

                spouse.setMaidenName(spouseSurname); //"" is handled in the divorce code
                break;
            case SURNAME_BOTH_HYP_SPOUSE:
                if (!StringUtil.isNullOrEmpty(surname) && !StringUtil.isNullOrEmpty(spouseSurname)) {
                    setSurname(spouseSurname + "-" + surname);
                    spouse.setSurname(spouseSurname + "-" + surname);
                } else if (!StringUtil.isNullOrEmpty(spouseSurname)) {
                    setSurname(spouseSurname);
                } else if (!StringUtil.isNullOrEmpty(surname)) {
                    spouse.setSurname(surname);
                }
                //both null or "" is ignored as a case, as it would lead to no changes

                setMaidenName(surname); //"" is handled in the divorce code
                spouse.setMaidenName(spouseSurname); //"" is handled in the divorce code
                break;
            case SURNAME_MALE:
                if (isMale()) {
                    spouse.setSurname(surname);
                    spouse.setMaidenName(spouseSurname); //"" is handled in the divorce code
                } else {
                    setSurname(spouseSurname);
                    setMaidenName(surname); //"" is handled in the divorce code
                }
                break;
            case SURNAME_FEMALE:
                if (isMale()) {
                    setSurname(spouseSurname);
                    setMaidenName(surname); //"" is handled in the divorce code
                } else {
                    spouse.setSurname(surname);
                    spouse.setMaidenName(spouseSurname); //"" is handled in the divorce code
                }
                break;
            default:
                MekHQ.getLogger().log(getClass(), "marry", LogLevel.ERROR,
                        String.format("Unknown error in Surname chooser between \"%s\" and \"%s\"",
                        getFullName(), spouse.getFullName()));
                break;
        }

        if (getCampaign().getCampaignOptions().logMarriageNameChange()) {
            if (!spouse.getSurname().equals(spouseSurname)) {
                PersonalLogger.marriageNameChange(spouse, this, getCampaign().getDate());

            }
            if (!getSurname().equals(surname)) {
                PersonalLogger.marriageNameChange(this, spouse, getCampaign().getDate());
            }
        }

        setSpouseId(spouse.getId());
        spouse.setSpouseId(getId());

        PersonalLogger.marriage(this, spouse, getCampaign().getDate());
        PersonalLogger.marriage(spouse, this, getCampaign().getDate());

        campaign.addReport(String.format("%s has married %s!", getHyperlinkedName(),
                spouse.getHyperlinkedName()));

        MekHQ.triggerEvent(new PersonChangedEvent(this));
        MekHQ.triggerEvent(new PersonChangedEvent(spouse));
    }

    private WeightedMap<Integer> createWeightedSurnameMap() {
        WeightedMap<Integer> map = new WeightedMap<>();

        int[] weights = campaign.getCampaignOptions().getRandomMarriageSurnameWeights();

        for (int i = 0; i < NUM_SURNAME; i++) {
            map.add(weights[i], i);
        }

        return map;
    }
    //endregion Marriage

    //region Divorce
    public void divorce(String divorceOption) {
        Person spouse = getSpouse();
        int reason = FormerSpouse.REASON_WIDOWED;

        switch (divorceOption) {
            case OPT_SELECTED_CHANGE_SURNAME:
                if (getMaidenName() != null) {
                    setSurname(getMaidenName());
                }
                break;
            case OPT_SPOUSE_CHANGE_SURNAME:
                if (spouse.getMaidenName() != null) {
                    spouse.setSurname(spouse.getMaidenName());
                }
                break;
            case OPT_BOTH_CHANGE_SURNAME:
                if (getMaidenName() != null) {
                    setSurname(getMaidenName());
                }
                if (spouse.getMaidenName() != null) {
                    spouse.setSurname(spouse.getMaidenName());
                }
                break;
            case OPT_KEEP_SURNAME:
            default:
                break;
        }

        if (!(spouse.isDeadOrMIA() && isDeadOrMIA())) {
            reason = FormerSpouse.REASON_DIVORCE;

            PersonalLogger.divorcedFrom(this, spouse, getCampaign().getDate());
            PersonalLogger.divorcedFrom(spouse, this, getCampaign().getDate());

            campaign.addReport(String.format("%s has divorced %s!", getHyperlinkedName(),
                    spouse.getHyperlinkedName()));

            spouse.setMaidenName(null);
            setMaidenName(null);

            spouse.setSpouseId(null);
            setSpouseId(null);
        } else if (spouse.isDeadOrMIA()) {
            setMaidenName(null);
            setSpouseId(null);
        } else if (isDeadOrMIA()) {
            spouse.setMaidenName(null);
            spouse.setSpouseId(null);
        }

        // Output a message for Spouses who are KIA
        if (reason == FormerSpouse.REASON_WIDOWED) {
            PersonalLogger.spouseKia(spouse, this, getCampaign().getDate());
        }

        // Add to former spouse list
        spouse.addFormerSpouse(new FormerSpouse(getId(), getCampaign().getLocalDate(), reason));
        addFormerSpouse(new FormerSpouse(spouse.getId(), getCampaign().getLocalDate(), reason));

        MekHQ.triggerEvent(new PersonChangedEvent(this));
        MekHQ.triggerEvent(new PersonChangedEvent(spouse));
    }
    //endregion Divorce

    public boolean isFemale() {
        return gender == Crew.G_FEMALE;
    }

    public boolean isMale() {
        return gender == Crew.G_MALE;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void awardXP(int xp) {
        this.xp += xp;
    }

    public int getEngineerXp() {
        return engXp;
    }

    public void setEngineerXp(int xp) {
        engXp = xp;
    }

    public int getAcquisitions() {
        return acquisitions;
    }

    public void setAcquisition(int a) {
        acquisitions = a;
    }

    public void incrementAcquisition() {
        acquisitions++;
    }

    public void setDoctorId(UUID t, int daysToWait) {
        this.doctorId = t;
        this.daysToWaitForHealing = daysToWait;
    }

    public boolean checkNaturalHealing(int daysToWait) {
        if (needsFixing() && daysToWaitForHealing <= 0 && doctorId == null) {
            heal();
            daysToWaitForHealing = daysToWait;
            return true;
        }
        return false;
    }

    public void decrementDaysToWaitForHealing() {
        if (daysToWaitForHealing > 0) {
            daysToWaitForHealing--;
        }
    }

    public boolean isDeployed() {
        Unit u = campaign.getUnit(unitId);
        if (null != u) {
            return (u.getScenarioId() != -1);
        }
        return false;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String s) {
        this.biography = s;
    }

    public boolean isActive() {
        return getStatus() == PersonnelStatus.ACTIVE;
    }

    public boolean isInActive() {
        return getStatus() != PersonnelStatus.ACTIVE;
    }

    public ExtraData getExtraData() {
        return extraData;
    }

    //region XML I/O
    //region XML Save
    @Override
    public void writeToXml(PrintWriter pw1, int indent) {
        pw1.println(MekHqXmlUtil.indentStr(indent) + "<person id=\"" + id.toString()
                + "\" type=\"" + this.getClass().getName() + "\">");
        try {
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "id", id.toString());
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "givenName", givenName);
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "surname", surname);
            if (!StringUtil.isNullOrEmpty(honorific)) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "honorific", honorific);
            }
            if (maidenName != null) { // this is only a != null comparison because empty is a use case for divorce
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maidenName", maidenName);
            }
            if (!StringUtil.isNullOrEmpty(callsign)) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "callsign", callsign);
            }
            // Always save the primary role
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "primaryRole", primaryRole);
            if (secondaryRole != T_NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "secondaryRole", secondaryRole);
            }
            if (primaryDesignator != ROMDesignation.NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "primaryDesignator", primaryDesignator.name());
            }
            if (secondaryDesignator != ROMDesignation.NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "secondaryDesignator", secondaryDesignator.name());
            }
            if (commander) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "commander", true);
            }
            if (dependent) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "dependent", true);
            }
            // Always save the person's origin faction
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "faction", originFaction.getShortName());
            if (originPlanet != null) {
                pw1.println(MekHqXmlUtil.indentStr(indent + 1)
                        + "<planetId systemId=\""
                        + originPlanet.getParentSystem().getId()
                        + "\">"
                        + originPlanet.getId()
                        + "</planetId>");
            }
            // Always save whether or not someone is a clanner
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "clan", clan);
            if (phenotype != Phenotype.P_NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "phenotype", phenotype);
            }
            if (!StringUtil.isNullOrEmpty(bloodname)) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "bloodname", bloodname);
            }
            if (!StringUtil.isNullOrEmpty(biography)) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "biography", biography);
            }
            if (idleMonths > 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "idleMonths", idleMonths);
            }
            if (ancestorsId != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "ancestors", ancestorsId.toString());
            }
            if (spouse != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "spouse", spouse.toString());
            }
            if (!formerSpouses.isEmpty()) {
                MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent + 1, "formerSpouses");
                for (FormerSpouse ex : formerSpouses) {
                    ex.writeToXml(pw1, indent + 2);
                }
                MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, indent + 1, "formerSpouses");
            }
            if (dueDate != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "dueDate",
                        MekHqXmlUtil.saveFormattedDate(dueDate));
            }
            if (expectedDueDate != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "expectedDueDate",
                        MekHqXmlUtil.saveFormattedDate(expectedDueDate));
            }
            if (!portraitCategory.equals(Crew.ROOT_PORTRAIT)) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "portraitCategory", portraitCategory);
            }
            if (!portraitFile.equals(Crew.PORTRAIT_NONE)) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "portraitFile", portraitFile);
            }
            // Always save the current XP
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "xp", xp);
            if (daysToWaitForHealing != 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "daysToWaitForHealing", daysToWaitForHealing);
            }
            // Always save the person's gender, as it would otherwise get confusing fast
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "gender", gender);
            // Always save a person's rank
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "rank", rank);
            if (rankLevel != 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "rankLevel", rankLevel);
            }
            if (rankSystem != -1) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "rankSystem", rankSystem);
            }
            if (maneiDominiRank != Rank.MD_RANK_NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maneiDominiRank", maneiDominiRank);
            }
            if (maneiDominiClass != MD_NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maneiDominiClass", maneiDominiClass);
            }
            if (nTasks > 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "nTasks", nTasks);
            }
            if (doctorId != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "doctorId", doctorId.toString());
            }
            if (unitId != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "unitId", unitId.toString());
            }
            if (!salary.equals(Money.of(-1))) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "salary", salary.toXmlString());
            }
            if (!totalEarnings.equals(Money.of(0))) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "totalEarnings", totalEarnings.toXmlString());
            }
            // Always save a person's status, to make it easy to parse the personnel saved data
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "status", status.name());
            if (prisonerStatus != PRISONER_NOT) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "prisonerStatus", prisonerStatus);
            }
            if (willingToDefect) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "willingToDefect", true);
            }
            if (hits > 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "hits", hits);
            }
            if (toughness != 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "toughness", toughness);
            }
            if (minutesLeft > 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "minutesLeft", minutesLeft);
            }
            if (overtimeLeft > 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "overtimeLeft", overtimeLeft);
            }
            if (birthday != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "birthday",
                        MekHqXmlUtil.saveFormattedDate(birthday));
            }
            if (dateOfDeath != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "deathday",
                        MekHqXmlUtil.saveFormattedDate(dateOfDeath));
            }
            if (recruitment != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "recruitment",
                        MekHqXmlUtil.saveFormattedDate(recruitment));
            }
            if (lastRankChangeDate != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "lastRankChangeDate",
                        MekHqXmlUtil.saveFormattedDate(lastRankChangeDate));
            }
            for (Skill skill : skills.getSkills()) {
                skill.writeToXml(pw1, indent + 1);
            }
            if (countOptions(PilotOptions.LVL3_ADVANTAGES) > 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "advantages",
                        getOptionList("::", PilotOptions.LVL3_ADVANTAGES));
            }
            if (countOptions(PilotOptions.EDGE_ADVANTAGES) > 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "edge",
                        getOptionList("::", PilotOptions.EDGE_ADVANTAGES));
                // For support personnel, write an available edge value
                if (hasSupportRole(false) || isEngineer()) {
                    MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "edgeAvailable", getCurrentEdge());
                }
            }
            if (countOptions(PilotOptions.MD_ADVANTAGES) > 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "implants",
                        getOptionList("::", PilotOptions.MD_ADVANTAGES));
            }
            if (!techUnitIds.isEmpty()) {
                MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent + 1, "techUnitIds");
                for (UUID id : techUnitIds) {
                    MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 2, "id", id.toString());
                }
                MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, indent + 1, "techUnitIds");
            }
            if (!personnelLog.isEmpty()) {
                MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent + 1, "personnelLog");
                for (LogEntry entry : personnelLog) {
                    entry.writeToXml(pw1, indent + 2);
                }
                MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, indent + 1, "personnelLog");
            }
            if (!missionLog.isEmpty()) {
                MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent + 1, "missionLog");
                for (LogEntry entry : missionLog) {
                    entry.writeToXml(pw1, indent + 2);
                }
                MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, indent + 1, "missionLog");
            }
            if (!awardController.getAwards().isEmpty()) {
                MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent + 1, "awards");
                for (Award award : awardController.getAwards()) {
                    award.writeToXml(pw1, indent + 2);
                }
                MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, indent + 1, "awards");
            }
            if (injuries.size() > 0) {
                MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent + 1, "injuries");
                for (Injury injury : injuries) {
                    injury.writeToXml(pw1, indent + 2);
                }
                MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, indent + 1, "injuries");
            }
            if (founder) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "founder", true);
            }
            if (originalUnitWeight != EntityWeightClass.WEIGHT_ULTRA_LIGHT) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "originalUnitWeight", originalUnitWeight);
            }
            if (originalUnitTech != TECH_IS1) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "originalUnitTech", originalUnitTech);
            }
            if (originalUnitId != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "originalUnitId", originalUnitId.toString());
            }
            if (acquisitions != 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "acquisitions", acquisitions);
            }
            if (!extraData.isEmpty()) {
                extraData.writeToXml(pw1);
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(Person.class, "writeToXml",
                    "Failed to write " + getFullName() + " to the XML File");
            throw e; // we want to rethrow to ensure that that the save fails
        }
        pw1.println(MekHqXmlUtil.indentStr(indent) + "</person>");
    }
    //endregion XML Save

    //region XML Load
    /**
     * This generates a Person from an XML file
     * @param wn the top level node of the person XML
     * @param campaign the campaign to add this person to
     * @param version the version of the file being loaded from
     * @return the generated person
     */
    public static Person generateInstanceFromXML(Node wn, Campaign campaign, Version version) {
        final String METHOD_NAME = "generateInstanceFromXML(Node, Campaign, Version)"; //$NON-NLS-1$

        Person retVal = new Person(campaign);

        try {
            // Okay, now load Person-specific fields!
            NodeList nl = wn.getChildNodes();

            // Determine if we need to use any migration functions for backwards compatibility.
            // This MUST be updated to equal the release version required for the latest potential
            // migration calls
            final String minimumMigrationVersion = "0.47.6";
            final boolean migrationPotentiallyRequired = version.isLowerThan(minimumMigrationVersion);

            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);

                // This is used to move all migration cases into their own method, which will be
                // accessed first if potentially required
                if (migrationPotentiallyRequired) {
                    // If we migrated the node, we can continue with the next node
                    if (checkNodeMigration(retVal, nl, wn2, campaign, version)) {
                        continue;
                    }
                }

                if (wn2.getNodeName().equalsIgnoreCase("givenName")) {
                    retVal.givenName = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("surname")) {
                    retVal.surname = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("honorific")) {
                    retVal.honorific = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("maidenName")) {
                    retVal.maidenName = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("callsign")) {
                    retVal.callsign = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("commander")) {
                    retVal.commander = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("dependent")) {
                    retVal.dependent = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("faction")) {
                    retVal.originFaction = Faction.getFaction(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("planetId")) {
                    String systemId = wn2.getAttributes().getNamedItem("systemId").getTextContent().trim();
                    String planetId = wn2.getTextContent().trim();
                    retVal.originPlanet = campaign.getSystemById(systemId).getPlanetById(planetId);
                } else if (wn2.getNodeName().equalsIgnoreCase("clan")) {
                    retVal.clan = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("phenotype")) {
                    retVal.phenotype = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("bloodname")) {
                    retVal.bloodname = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("biography")) {
                    retVal.biography = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("primaryRole")) {
                    retVal.primaryRole = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("secondaryRole")) {
                    retVal.secondaryRole = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquisitions")) {
                    retVal.acquisitions = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("primaryDesignator")) {
                    retVal.primaryDesignator = ROMDesignation.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("secondaryDesignator")) {
                    retVal.secondaryDesignator = ROMDesignation.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("daysToWaitForHealing")) {
                    retVal.daysToWaitForHealing = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("idleMonths")) {
                    retVal.idleMonths = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("id")) {
                    retVal.id = UUID.fromString(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("ancestors")) {
                    retVal.ancestorsId = UUID.fromString(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("spouse")) {
                    retVal.spouse = UUID.fromString(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("formerSpouses")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (!wn3.getNodeName().equalsIgnoreCase("formerSpouse")) {
                            // Error condition of sorts!
                            // Errr, what should we do here?
                            MekHQ.getLogger().log(Person.class, METHOD_NAME, LogLevel.ERROR,
                                    "Unknown node type not loaded in formerSpouses nodes: "
                                            + wn3.getNodeName());
                            continue;
                        }
                        retVal.formerSpouses.add(FormerSpouse.generateInstanceFromXML(wn3));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("tryingToMarry")) {
                    retVal.tryingToMarry = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("tryingToConceive")) {
                    retVal.tryingToConceive = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("dueDate")) {
                    retVal.dueDate = MekHqXmlUtil.parseDate(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("expectedDueDate")) {
                    retVal.expectedDueDate = MekHqXmlUtil.parseDate(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("portraitCategory")) {
                    retVal.setPortraitCategory(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("portraitFile")) {
                    retVal.setPortraitFileName(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("xp")) {
                    retVal.xp = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("nTasks")) {
                    retVal.nTasks = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("hits")) {
                    retVal.hits = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("gender")) {
                    retVal.gender = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("rank")) {
                    retVal.rank = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("rankLevel")) {
                    retVal.rankLevel = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("rankSystem")) {
                    retVal.setRankSystem(Integer.parseInt(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("maneiDominiRank")) {
                    retVal.maneiDominiRank = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("maneiDominiClass")) {
                    retVal.maneiDominiClass = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("doctorId")) {
                    retVal.doctorId = UUID.fromString(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("unitId")) {
                    retVal.unitId = UUID.fromString(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("status")) {
                    // TODO : remove inline migration
                    if (version.isLowerThan("0.47.6")) {
                        switch (Integer.parseInt(wn2.getTextContent())) {
                            case 1:
                                retVal.status = PersonnelStatus.RETIRED;
                                break;
                            case 2:
                                retVal.status = PersonnelStatus.KIA;
                                break;
                            case 3:
                                retVal.status = PersonnelStatus.MIA;
                                break;
                            default:
                                retVal.status = PersonnelStatus.ACTIVE;
                                break;
                        }
                    } else {
                        retVal.status = PersonnelStatus.valueOf(wn2.getTextContent());
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("prisonerStatus")) {
                    retVal.prisonerStatus = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("willingToDefect")) {
                    retVal.willingToDefect = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("salary")) {
                    retVal.salary = Money.fromXmlString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("totalEarnings")) {
                    retVal.totalEarnings = Money.fromXmlString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("minutesLeft")) {
                    retVal.minutesLeft = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("overtimeLeft")) {
                    retVal.overtimeLeft = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("birthday")) {
                    retVal.birthday = MekHqXmlUtil.parseDate(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("deathday")) {
                    retVal.dateOfDeath = MekHqXmlUtil.parseDate(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("recruitment")) {
                    retVal.recruitment = MekHqXmlUtil.parseDate(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("lastRankChangeDate")) {
                    retVal.lastRankChangeDate = MekHqXmlUtil.parseDate(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("advantages")) {
                    loadAdvantages(retVal, wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("edge")) {
                    loadEdge(retVal, wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("edgeAvailable")) {
                    retVal.currentEdge = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("implants")) {
                    loadImplants(retVal, wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("toughness")) {
                    retVal.toughness = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("pilotHits")) {
                    retVal.hits = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("skill")) {
                    Skill s = Skill.generateInstanceFromXML(wn2);
                    if ((s != null) && (s.getType() != null)) {
                        retVal.skills.addSkill(s.getType().getName(), s);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("techUnitIds")) {
                    loadTechUnitIds(retVal, wn2.getChildNodes());
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelLog")) {
                    loadPersonnelLog(retVal, wn2.getChildNodes());
                } else if (wn2.getNodeName().equalsIgnoreCase("missionLog")) {
                    loadMissionLog(retVal, wn2.getChildNodes());
                } else if (wn2.getNodeName().equalsIgnoreCase("awards")) {
                    loadAwards(retVal, wn2.getChildNodes());
                } else if (wn2.getNodeName().equalsIgnoreCase("injuries")) {
                    loadInjuries(retVal, wn2.getChildNodes(), campaign);
                } else if (wn2.getNodeName().equalsIgnoreCase("founder")) {
                    retVal.founder = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("originalUnitWeight")) {
                    retVal.originalUnitWeight = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("originalUnitTech")) {
                    retVal.originalUnitTech = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("originalUnitId")) {
                    retVal.originalUnitId = UUID.fromString(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("extraData")) {
                    retVal.extraData = ExtraData.createFromXml(wn2);
                }
            }

            retVal.setFullName(); // this sets the name based on the loaded values

            // This will migrate any data fields that are reliant on having the data loaded
            // completely before running migration
            if (migrationPotentiallyRequired) {
                migrateFieldsOnLoad(retVal, campaign, version);
            }

            if (retVal.id == null) {
                MekHQ.getLogger().log(Person.class, METHOD_NAME, LogLevel.ERROR,
                        "Id not pre-defined; generating person's Id."); //$NON-NLS-1$
                retVal.id = UUID.randomUUID();
            }
        } catch (Exception ex) {
            MekHQ.getLogger().error(Person.class, METHOD_NAME, ex);
            // we shouldn't return a half instantiated person, after all
        }

        return retVal;
    }

    /**
     * This loads former spouses for a person from a node list
     * @param retVal the person to load former spouses for
     * @param nl2 the nodes to load former spouses from
     */
    private static void loadFormerSpouses(Person retVal, NodeList nl2) {
        for (int y = 0; y < nl2.getLength(); y++) {
            Node wn3 = nl2.item(y);
            // If it's not an element node, we ignore it.
            if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (wn3.getNodeName().equalsIgnoreCase("formerSpouse")) {
                retVal.formerSpouses.add(FormerSpouse.generateInstanceFromXML(wn3));
            } else {
                MekHQ.getLogger().log(Person.class, "loadFormerSpouses", LogLevel.ERROR,
                        "Unknown node type not loaded in formerSpouses nodes: "
                                + wn3.getNodeName());
            }
        }
    }

    /**
     * This loads the values for advantages from a string
     * @param retVal the person to load advantages for
     * @param advantages the string to load from
     */
    private static void loadAdvantages(Person retVal, String advantages) {
        if (!StringUtil.isNullOrEmpty(advantages)) {
            StringTokenizer st = new StringTokenizer(advantages, "::");
            while (st.hasMoreTokens()) {
                String adv = st.nextToken();
                String advName = Crew.parseAdvantageName(adv);
                Object value = Crew.parseAdvantageValue(adv);

                try {
                    retVal.getOptions().getOption(advName).setValue(value);
                } catch (Exception e) {
                    MekHQ.getLogger().log(Person.class, "loadAdvantages", LogLevel.ERROR,
                            "Error restoring advantage: " + adv);
                }
            }
        }
    }

    /**
     * This loads the values for edge from a string
     * @param retVal the person to load edge for
     * @param edge the string to load from
     */
    private static void loadEdge(Person retVal, String edge) {
        if (!StringUtil.isNullOrEmpty(edge)) {
            StringTokenizer st = new StringTokenizer(edge, "::");
            while (st.hasMoreTokens()) {
                String adv = st.nextToken();
                String advName = Crew.parseAdvantageName(adv);
                Object value = Crew.parseAdvantageValue(adv);

                try {
                    retVal.getOptions().getOption(advName).setValue(value);
                } catch (Exception e) {
                    MekHQ.getLogger().log(Person.class, "loadEdge", LogLevel.ERROR,
                            "Error restoring edge: " + adv);
                }
            }
        }
    }

    /**
     * This loads the values for implants from a string
     * @param retVal the person to load implants for
     * @param implants the string to load from
     */
    private static void loadImplants(Person retVal, String implants) {
        if (!StringUtil.isNullOrEmpty(implants)) {
            StringTokenizer st = new StringTokenizer(implants, "::");
            while (st.hasMoreTokens()) {
                String adv = st.nextToken();
                String advName = Crew.parseAdvantageName(adv);
                Object value = Crew.parseAdvantageValue(adv);

                try {
                    retVal.getOptions().getOption(advName).setValue(value);
                } catch (Exception e) {
                    MekHQ.getLogger().log(Person.class, "loadImplants", LogLevel.ERROR,
                            "Error restoring implants: " + adv);
                }
            }
        }
    }

    /**
     * This loads tech unit ids for a person from a node list
     * @param retVal the person to load tech unit ids for
     * @param nl2 the nodes to load unit ids from
     */
    private static void loadTechUnitIds(Person retVal, NodeList nl2) {
        for (int y = 0; y < nl2.getLength(); y++) {
            Node wn3 = nl2.item(y);
            // If it's not an element node, we ignore it.
            if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (wn3.getNodeName().equalsIgnoreCase("id")) {
                retVal.addTechUnitID(UUID.fromString(wn3.getTextContent()));
            } else {
                MekHQ.getLogger().log(Person.class, "loadTechUnitIds", LogLevel.ERROR,
                        "Unknown node type not loaded in techUnitIds nodes: " + wn3.getNodeName());
            }
        }
    }

    /**
     * This loads the personnel log for a person from a node list
     * @param retVal the person to the personnel log for
     * @param nl2 the nodes to load the personnel log from
     */
    private static void loadPersonnelLog(Person retVal, NodeList nl2) {
        for (int y = 0; y < nl2.getLength(); y++) {
            Node wn3 = nl2.item(y);
            // If it's not an element node, we ignore it.
            if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (wn3.getNodeName().equalsIgnoreCase("logEntry")) {
                retVal.addLogEntry(LogEntryFactory.getInstance().generateInstanceFromXML(wn3));
            } else {
                MekHQ.getLogger().log(Person.class, "loadPersonnelLog", LogLevel.ERROR,
                        "Unknown node type not loaded in personnel log nodes: " + wn3.getNodeName());
            }
        }
    }

    /**
     * This loads the mission log for a person from a node list
     * @param retVal the person to the mission log for
     * @param nl2 the nodes to load the mission log from
     */
    private static void loadMissionLog(Person retVal, NodeList nl2) {
        for (int y = 0; y < nl2.getLength(); y++) {
            Node wn3 = nl2.item(y);
            // If it's not an element node, we ignore it.
            if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (wn3.getNodeName().equalsIgnoreCase("logEntry")) {
                retVal.addMissionLogEntry(LogEntryFactory.getInstance().generateInstanceFromXML(wn3));
            } else {
                MekHQ.getLogger().log(Person.class, "loadMissionLog", LogLevel.ERROR,
                        "Unknown node type not loaded in mission log nodes: " + wn3.getNodeName());
            }
        }
    }

    /**
     * This loads awards for a person from a node list
     * @param retVal the person to awards for
     * @param nl2 the nodes to load awards from
     */
    private static void loadAwards(Person retVal, NodeList nl2) {
        for (int y = 0; y < nl2.getLength(); y++) {
            Node wn3 = nl2.item(y);
            // If it's not an element node, we ignore it.
            if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (wn3.getNodeName().equalsIgnoreCase("award")) {
                retVal.awardController.addAwardFromXml(AwardsFactory.getInstance().generateNewFromXML(wn3));
            } else {
                MekHQ.getLogger().log(Person.class, "loadAwards", LogLevel.ERROR,
                        "Unknown node type not loaded in personnel log nodes: " + wn3.getNodeName());
            }
        }
    }

    /**
     * This loads injuries for a person from a node list
     * @param retVal the person to injuries for
     * @param nl2 the nodes to load injuries from
     * @param campaign the campaign containing the calendar to use for the current date
     */
    private static void loadInjuries(Person retVal, NodeList nl2, Campaign campaign) {
        for (int y = 0; y < nl2.getLength(); y++) {
            Node wn3 = nl2.item(y);
            // If it's not an element node, we ignore it.
            if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (wn3.getNodeName().equalsIgnoreCase("injury")) {
                retVal.injuries.add(Injury.generateInstanceFromXML(wn3));
            } else {
                MekHQ.getLogger().log(Person.class, "loadInjuries", LogLevel.ERROR,
                        "Unknown node type not loaded in injury nodes: " + wn3.getNodeName());
            }
        }
        DateTime now = new DateTime(campaign.getCalendar());
        retVal.injuries.stream().filter(inj -> (null == inj.getStart()))
                .forEach(inj -> inj.setStart(now.minusDays(inj.getOriginalTime() - inj.getTime())));
    }

    //region Person Version Migration
    /**
     * This determines if any fields contain older-style values that need to be migrated, and then
     * migrates them. If the field does not require migration, it will instead be parsed in the
     * {@link #generateInstanceFromXML(Node, Campaign, Version) generateInstanceFromXML} method
     * @param retVal the person that is being generated from XML
     * @param nl the node list of nodes being parsed (required for a 0.1.8 or earlier migration)
     * @param wn2 the node to parse
     * @param campaign the campaign to parse to
     * @param version the version number that the file is from
     * @return true if the node is migrated, otherwise false
     */
    private static boolean checkNodeMigration(Person retVal, NodeList nl, Node wn2,
                                              Campaign campaign, Version version) {
        final String METHOD_NAME = "checkNodeMigration";

        boolean valueChanged = false;
        if (wn2.getNodeName().equalsIgnoreCase("name")) {
            retVal.migrateName(wn2.getTextContent());
            valueChanged = true;
        } else if (wn2.getNodeName().equalsIgnoreCase("id")) {
            if (version.isLowerThan("0.2.14")) {
                retVal.oldId = Integer.parseInt(wn2.getTextContent());
                valueChanged = true;
            }
        } else if (wn2.getNodeName().equalsIgnoreCase("doctorId")) {
            if (version.isLowerThan("0.2.14")) {
                retVal.oldDoctorId = Integer.parseInt(wn2.getTextContent());
                valueChanged = true;
            } else if (wn2.getTextContent().equals("null")) {
                valueChanged = true; //handles the possibility of bad data in pre-0.47.5 saves
            }
        } else if (wn2.getNodeName().equalsIgnoreCase("unitId")) {
            if (version.isLowerThan("0.2.14")) {
                retVal.oldUnitId = Integer.parseInt(wn2.getTextContent());
                valueChanged = true;
            } else if (wn2.getTextContent().equals("null")) {
                valueChanged = true; //handles the possibility of bad data in pre-0.47.5 saves
            }
        } else if (wn2.getNodeName().equalsIgnoreCase("personnelLog")) {
            if (version.isLowerThan("0.45.4")) {
                // If the version of this campaign is earlier than 0.45.4,
                // we didn't have the mission log separated from the personnel log,
                // so we need to separate the log entries manually
                NodeList nl2 = wn2.getChildNodes();
                for (int y = 0; y < nl2.getLength(); y++) {
                    Node wn3 = nl2.item(y);
                    // If it's not an element node, we ignore it.
                    if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    if (!wn3.getNodeName().equalsIgnoreCase("logEntry")) {
                        MekHQ.getLogger().log(Person.class, METHOD_NAME, LogLevel.ERROR,
                                "Unknown node type not loaded in personnel log nodes: " + wn3.getNodeName()); //$NON-NLS-1$
                        continue;
                    }

                    LogEntry entry = LogEntryFactory.getInstance().generateInstanceFromXML(wn3);

                    if (entry.getDesc().startsWith(getMissionParticipatedString())) {
                        retVal.addMissionLogEntry(entry);
                    } else {
                        retVal.addLogEntry(entry);
                    }
                }
                valueChanged = true;
            }
        } else if (wn2.getNodeName().equalsIgnoreCase("pilotName")) {
            retVal.migrateName(wn2.getTextContent());
            valueChanged = true;
        } else if (wn2.getNodeName().equalsIgnoreCase("pilotNickname")) {
            retVal.setCallsign(wn2.getTextContent());
            valueChanged = true;
        } else if (wn2.getNodeName().equalsIgnoreCase("rank")) {
            if (version.isLowerThan("0.3.4-r1782")) {
                RankTranslator rt = new RankTranslator(campaign);
                try {
                    retVal.rank = rt.getNewRank(campaign.getRanks().getOldRankSystem(),
                            Integer.parseInt(wn2.getTextContent()));
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
                valueChanged = true;
            }
        } else if (wn2.getNodeName().equalsIgnoreCase("type")) {
            int pilotGunnery = -1;
            int pilotPiloting = -1;
            int pilotCommandBonus = -1;
            int type = Integer.parseInt(wn2.getTextContent());

            for (int x = 0; x < nl.getLength(); x++) {
                Node wn3 = nl.item(x);
                if (wn3.getNodeName().equalsIgnoreCase("pilotGunnery")) {
                    pilotGunnery = Integer.parseInt(wn3.getTextContent());
                } else if (wn3.getNodeName().equalsIgnoreCase("pilotPiloting")) {
                    pilotPiloting = Integer.parseInt(wn3.getTextContent());
                } else if (wn3.getNodeName().equalsIgnoreCase("pilotCommandBonus")) {
                    pilotCommandBonus = Integer.parseInt(wn3.getTextContent());
                }
            }

            //check to see if we are dealing with a PilotPerson from 0.1.8 or earlier
            if (pilotGunnery != -1) {
                switch (type) {
                    case 0:
                        retVal.addSkill(SkillType.S_GUN_MECH, 7 - pilotGunnery, 0);
                        retVal.addSkill(SkillType.S_PILOT_MECH, 8 - pilotPiloting, 0);
                        retVal.primaryRole = T_MECHWARRIOR;
                        break;
                    case 1:
                        retVal.addSkill(SkillType.S_GUN_VEE, 7 - pilotGunnery, 0);
                        retVal.addSkill(SkillType.S_PILOT_GVEE, 8 - pilotPiloting, 0);
                        retVal.primaryRole = T_GVEE_DRIVER;
                        break;
                    case 2:
                        retVal.addSkill(SkillType.S_GUN_AERO, 7 - pilotGunnery, 0);
                        retVal.addSkill(SkillType.S_PILOT_AERO, 8 - pilotPiloting, 0);
                        retVal.primaryRole = T_AERO_PILOT;
                        break;
                    case 4:
                        retVal.addSkill(SkillType.S_GUN_BA, 7 - pilotGunnery, 0);
                        retVal.addSkill(SkillType.S_ANTI_MECH, 8 - pilotPiloting, 0);
                        retVal.primaryRole = T_BA;
                        break;
                }
                retVal.addSkill(SkillType.S_TACTICS, pilotCommandBonus, 0);
            }
            valueChanged = true;
        } else if (wn2.getNodeName().equalsIgnoreCase("phenotype")) {
            if (version.isLowerThan("0.47.6") && version.isHigherThan("0.3.4")) {
                int phenotype = Integer.parseInt(wn2.getTextContent());
                if ((retVal.getPrimaryRole() == Person.T_PROTO_PILOT) && (phenotype == Phenotype.P_AEROSPACE)) {
                    phenotype = Phenotype.P_PROTOMECH;
                }
                retVal.phenotype = phenotype;
            }
        }

        return valueChanged;
    }

    /**
     * This is used to migrate data fields following load based on the version supplied. Anything
     * here is not reliant on a node, but instead modifies the data from a fully loaded person
     * @param retVal the person to check for field migration
     * @param version the version number of the file being migrated
     */
    private static void migrateFieldsOnLoad(Person retVal, Campaign campaign, Version version) {
        if ((version.getMajorVersion() == 0) && (version.getMinorVersion() == 3) && (version.getSnapshot() < 1)) {
            //adjust for conventional fighter pilots
            if ((retVal.primaryRole == T_CONV_PILOT) && retVal.hasSkill(SkillType.S_PILOT_SPACE)
                    && !retVal.hasSkill(SkillType.S_PILOT_JET)) {
                retVal.primaryRole += 1;
            }
            if ((retVal.secondaryRole == T_CONV_PILOT) && retVal.hasSkill(SkillType.S_PILOT_SPACE)
                    && !retVal.hasSkill(SkillType.S_PILOT_JET)) {
                retVal.secondaryRole += 1;
            }
            if ((retVal.primaryRole == T_AERO_PILOT) && !retVal.hasSkill(SkillType.S_PILOT_SPACE)
                    && retVal.hasSkill(SkillType.S_PILOT_JET)) {
                retVal.primaryRole += 8;
            }
            if ((retVal.secondaryRole == T_AERO_PILOT) && !retVal.hasSkill(SkillType.S_PILOT_SPACE)
                    && retVal.hasSkill(SkillType.S_PILOT_JET)) {
                retVal.secondaryRole += 8;
            }
        }

        if ((version.getMajorVersion() == 0) && (version.getMinorVersion() == 2)) {
            //adjust for conventional fighter pilots
            if (retVal.primaryRole >= T_CONV_PILOT) {
                retVal.primaryRole += 1;
            }
            if (retVal.secondaryRole >= T_CONV_PILOT) {
                retVal.secondaryRole += 1;
            }
        }

        if ((version.getMajorVersion() == 0) && (version.getMinorVersion() < 2) && (version.getSnapshot() < 13)) {
            if (retVal.primaryRole > T_INFANTRY) {
                retVal.primaryRole += 4;
            }
            if (retVal.secondaryRole > T_INFANTRY) {
                retVal.secondaryRole += 4;
            }
        }

        //versions before 0.3.4 did not have proper clan phenotypes
        if (version.isLowerThan("0.3.4") && campaign.getFaction().isClan()) {
            //assume personnel are clan and trueborn if the right role
            retVal.setClanner(true);
            switch (retVal.getPrimaryRole()) {
                case Person.T_MECHWARRIOR:
                    retVal.setPhenotype(Phenotype.P_MECHWARRIOR);
                    break;
                case Person.T_AERO_PILOT:
                case Person.T_CONV_PILOT:
                    retVal.setPhenotype(Phenotype.P_AEROSPACE);
                    break;
                case Person.T_BA:
                    retVal.setPhenotype(Phenotype.P_ELEMENTAL);
                    break;
                case Person.T_VEE_GUNNER:
                case Person.T_GVEE_DRIVER:
                case Person.T_NVEE_DRIVER:
                case Person.T_VTOL_PILOT:
                    retVal.setPhenotype(Phenotype.P_VEHICLE);
                    break;
                case Person.T_PROTO_PILOT:
                    retVal.setPhenotype(Phenotype.P_PROTOMECH);
                    break;
                default:
                    retVal.setPhenotype(Phenotype.P_NONE);
                    break;
            }
        }

        // Prisoner and Bondsman rank updating
        if ((retVal.prisonerStatus != PRISONER_NOT) && (retVal.rank == 0)) {
            if (retVal.prisonerStatus == PRISONER_BONDSMAN) {
                retVal.setRankNumeric(Ranks.RANK_BONDSMAN);
            } else {
                retVal.setRankNumeric(Ranks.RANK_PRISONER);
            }
        }
    }
    //endregion Person Version Migration
    //endregion XML Load
    //endregion XML I/O

    public void setSalary(Money s) {
        salary = s;
    }

    public Money getSalary() {
        if (!isFree() || isDependent()) {
            return Money.zero();
        }

        if (salary.isPositiveOrZero()) {
            return salary;
        }

        //if salary is negative, then use the standard amounts
        Money primaryBase = campaign.getCampaignOptions().getBaseSalaryMoney(getPrimaryRole());
        primaryBase = primaryBase.multipliedBy(campaign.getCampaignOptions().getSalaryXpMultiplier(getExperienceLevel(false)));
        if (hasSkill(SkillType.S_ANTI_MECH) && (getPrimaryRole() == T_INFANTRY || getPrimaryRole() == T_BA)) {
            primaryBase = primaryBase.multipliedBy(campaign.getCampaignOptions().getSalaryAntiMekMultiplier());
        }

        Money secondaryBase = campaign.getCampaignOptions().getBaseSalaryMoney(getSecondaryRole()).dividedBy(2);
        secondaryBase = secondaryBase.multipliedBy(campaign.getCampaignOptions().getSalaryXpMultiplier(getExperienceLevel(true)));
        if (hasSkill(SkillType.S_ANTI_MECH) && (getSecondaryRole() == T_INFANTRY || getSecondaryRole() == T_BA)) {
            secondaryBase = secondaryBase.multipliedBy(campaign.getCampaignOptions().getSalaryAntiMekMultiplier());
        }

        Money totalBase = primaryBase.plus(secondaryBase);

        if (getRank().isOfficer()) {
            totalBase = totalBase.multipliedBy(campaign.getCampaignOptions().getSalaryCommissionMultiplier());
        } else {
            totalBase = totalBase.multipliedBy(campaign.getCampaignOptions().getSalaryEnlistedMultiplier());
        }

        totalBase = totalBase.multipliedBy(getRank().getPayMultiplier());

        return totalBase;
        //TODO: distinguish DropShip, JumpShip, and WarShip crew
        //TODO: Add era mod to salary calc..
    }

    /**
     * @return the person's total earnings
     */
    public Money getTotalEarnings() {
        return totalEarnings;
    }

    /**
     * This is used to pay a person
     * @param money the amount of money to add to their total earnings
     */
    public void payPerson(Money money) {
        totalEarnings = getTotalEarnings().plus(money);
    }

    /**
     * This is used to pay a person their salary
     */
    public void payPersonSalary() {
        if (isActive()) {
            payPerson(getSalary());
        }
    }

    /**
     * This is used to pay a person their share value based on the value of a single share
     * @param money the value of a single share
     * @param sharesForAll whether or not all personnel have shares
     */
    public void payPersonShares(Money money, boolean sharesForAll) {
        int shares = getNumShares(sharesForAll);
        if (shares > 0) {
            payPerson(money.multipliedBy(shares));
        }
    }

    public int getRankNumeric() {
        return rank;
    }

    public void setRankNumeric(int r) {
        rank = r;
        rankLevel = 0; // Always reset to 0 so that a call to setRankLevel() isn't mandatory.
    }

    public int getRankLevel() {
        // If we're somehow above the max level for this rank, drop to that level
        int profession = getProfession();
        while (profession != Ranks.RPROF_MW && getRanks().isEmptyProfession(profession)) {
            profession = getRanks().getAlternateProfession(profession);
        }

        if (rankLevel > getRank().getRankLevels(profession)) {
            rankLevel = getRank().getRankLevels(profession);
        }

        return rankLevel;
    }

    public void setRankLevel(int level) {
        rankLevel = level;
    }

    public int getRankSystem() {
        if (rankSystem == -1) {
            return campaign.getRanks().getRankSystem();
        }
        return rankSystem;
    }

    public void setRankSystem(int system) {
        rankSystem = system;
        if (system == campaign.getRanks().getRankSystem()) {
            rankSystem = -1;
        }

        // Set the ranks too
        if (rankSystem == -1) {
            ranks = null;
        } else {
            ranks = new Ranks(rankSystem);
        }
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public Ranks getRanks() {
        if (rankSystem != -1) {
            // Null protection
            if (ranks == null) {
                ranks = new Ranks(rankSystem);
            }
            return ranks;
        }
        return campaign.getRanks();
    }

    public Rank getRank() {
        if (rankSystem != -1) {
            return Ranks.getRanksFromSystem(rankSystem).getRank(rank);
        }
        return campaign.getRanks().getRank(rank);
    }

    public String getRankName() {
        String rankName;
        int profession = getProfession();

        /* Track number of times the profession has been redirected so we don't get caught
         * in a loop by self-reference or loops due to bad configuration */
        int redirects = 0;

        // If we're using an "empty" profession, default to MechWarrior
        while (getRanks().isEmptyProfession(profession) && redirects < Ranks.RPROF_NUM) {
            profession = campaign.getRanks().getAlternateProfession(profession);
            redirects++;
        }

        // If we're set to a rank that no longer exists, demote ourself
        while (getRank().getName(profession).equals("-") && (rank > 0)) {
            setRankNumeric(--rank);
        }

        redirects = 0;
        // re-route through any profession redirections
        while (getRank().getName(profession).startsWith("--") && profession != Ranks.RPROF_MW
                && redirects < Ranks.RPROF_NUM) {
            // We've hit a rank that defaults to the MechWarrior table, so grab the equivalent name from there
            if (getRank().getName(profession).equals("--")) {
                profession = getRanks().getAlternateProfession(profession);
            } else if (getRank().getName(profession).startsWith("--")) {
                profession = getRanks().getAlternateProfession(getRank().getName(profession));
            }
            redirects++;
        }
        if (getRank().getName(profession).startsWith("--")) {
            profession = Ranks.RPROF_MW;
        }

        rankName = getRank().getName(profession);

        // Manei Domini Additions
        if (getRankSystem() != Ranks.RS_WOB) {
            // Oops, clear our MD variables
            maneiDominiClass = MD_NONE;
            maneiDominiRank = Rank.MD_RANK_NONE;
        }
        if (maneiDominiClass != MD_NONE) {
            rankName = getManeiDominiClassNames() + " " + rankName;
        }
        if (maneiDominiRank != Rank.MD_RANK_NONE) {
            rankName += " " + Rank.getManeiDominiRankName(maneiDominiRank);
        }
        if (getRankSystem() == Ranks.RS_COM || getRankSystem() == Ranks.RS_WOB) {
            rankName += ROMDesignation.getComStarBranchDesignation(this, campaign);
        }

        // If we have a rankLevel, add it
        if (rankLevel > 0) {
            if (getRank().getRankLevels(profession) > 0)
                rankName += Utilities.getRomanNumeralsFromArabicNumber(rankLevel, true);
            else // Oops! Our rankLevel didn't get correctly cleared, they's remedy that.
                rankLevel = 0;
        }

        // We have our name, return it
        return rankName;
    }

    public int getManeiDominiClass() {
        return maneiDominiClass;
    }

    public void setManeiDominiClass(int maneiDominiClass) {
        this.maneiDominiClass = maneiDominiClass;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public int getManeiDominiRank() {
        return maneiDominiRank;
    }

    public void setManeiDominiRank(int maneiDominiRank) {
        this.maneiDominiRank = maneiDominiRank;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public String getManeiDominiClassNames() {
        return getManeiDominiClassNames(maneiDominiClass, getRankSystem());
    }

    public static String getManeiDominiClassNames(int maneiDominiClass, int rankSystem) {
        // Only WoB
        if (rankSystem != Ranks.RS_WOB)
            return "";

        switch (maneiDominiClass) {
            case MD_GHOST:
                return "Ghost";
            case MD_WRAITH:
                return "Wraith";
            case MD_BANSHEE:
                return "Banshee";
            case MD_ZOMBIE:
                return "Zombie";
            case MD_PHANTOM:
                return "Phantom";
            case MD_SPECTER:
                return "Specter";
            case MD_POLTERGEIST:
                return "Poltergeist";
            case MD_NONE:
            default:
                return "";
        }
    }

    /**
     * Determines whether this person outranks another, taking into account the seniority rank for
     * ComStar and WoB ranks.
     *
     * @param other The <code>Person</code> to compare ranks with
     * @return      true if <code>other</code> has a lower rank, or if <code>other</code> is null.
     */
    public boolean outRanks(@Nullable Person other) {
        if (null == other) {
            return true;
        }
        if (getRankNumeric() == other.getRankNumeric()) {
            return getRankLevel() > other.getRankLevel();
        }
        return getRankNumeric() > other.getRankNumeric();
    }

    public String getSkillSummary() {
        return SkillType.getExperienceLevelName(getExperienceLevel(false));
    }

    @Override
    public String toString() {
        return getFullName();
    }

    /**
     * Two people are determined to be equal if they have the same id
     * @param object the object to check if it is equal to the person or not
     * @return true if they have the same id, otherwise false
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Person)) {
            return false;
        } else {
            return getId().equals(((Person) object).getId());
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public int getExperienceLevel(boolean secondary) {
        int role = primaryRole;
        if (secondary) {
            role = secondaryRole;
        }
        switch (role) {
            case T_MECHWARRIOR:
                if (hasSkill(SkillType.S_GUN_MECH) && hasSkill(SkillType.S_PILOT_MECH)) {
                    /* Attempt to use higher precision averaging, but if it doesn't provide a clear result
                    due to non-standard experience thresholds then fall back on lower precision averaging
                    See Bug #140 */
                    if (campaign.getCampaignOptions().useAltQualityAveraging()) {
                        int rawScore = (int) Math.floor(
                            (getSkill(SkillType.S_GUN_MECH).getLevel() + getSkill(SkillType.S_PILOT_MECH).getLevel()) / 2.0
                        );
                        if (getSkill(SkillType.S_GUN_MECH).getType().getExperienceLevel(rawScore) ==
                            getSkill(SkillType.S_PILOT_MECH).getType().getExperienceLevel(rawScore)) {
                            return getSkill(SkillType.S_GUN_MECH).getType().getExperienceLevel(rawScore);
                        }
                    }

                    return (int) Math.floor((getSkill(SkillType.S_GUN_MECH).getExperienceLevel()
                                             + getSkill(SkillType.S_PILOT_MECH).getExperienceLevel()) / 2.0);
                } else {
                    return -1;
                }
            case T_GVEE_DRIVER:
                if (hasSkill(SkillType.S_PILOT_GVEE)) {
                    return getSkill(SkillType.S_PILOT_GVEE).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_NVEE_DRIVER:
                if (hasSkill(SkillType.S_PILOT_NVEE)) {
                    return getSkill(SkillType.S_PILOT_NVEE).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_VTOL_PILOT:
                if (hasSkill(SkillType.S_PILOT_VTOL)) {
                    return getSkill(SkillType.S_PILOT_VTOL).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_VEE_GUNNER:
                if (hasSkill(SkillType.S_GUN_VEE)) {
                    return getSkill(SkillType.S_GUN_VEE).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_AERO_PILOT:
                if (hasSkill(SkillType.S_GUN_AERO) && hasSkill(SkillType.S_PILOT_AERO)) {
                    if (campaign.getCampaignOptions().useAltQualityAveraging()) {
                        int rawScore = (int) Math.floor(
                            (getSkill(SkillType.S_GUN_AERO).getLevel() + getSkill(SkillType.S_PILOT_AERO)
                                    .getLevel()) / 2.0
                        );
                        if (getSkill(SkillType.S_GUN_AERO).getType().getExperienceLevel(rawScore) ==
                            getSkill(SkillType.S_PILOT_AERO).getType().getExperienceLevel(rawScore)) {
                            return getSkill(SkillType.S_GUN_AERO).getType().getExperienceLevel(rawScore);
                        }
                    }

                    return (int) Math.floor((getSkill(SkillType.S_GUN_AERO).getExperienceLevel()
                                                 + getSkill(SkillType.S_PILOT_AERO).getExperienceLevel()) / 2.0);
                } else {
                    return -1;
                }
            case T_CONV_PILOT:
                if (hasSkill(SkillType.S_GUN_JET) && hasSkill(SkillType.S_PILOT_JET)) {
                    if (campaign.getCampaignOptions().useAltQualityAveraging()) {
                        int rawScore = (int) Math.floor(
                            (getSkill(SkillType.S_GUN_JET).getLevel() + getSkill(SkillType.S_PILOT_JET)
                                    .getLevel()) / 2.0
                        );
                        if (getSkill(SkillType.S_GUN_JET).getType().getExperienceLevel(rawScore) ==
                            getSkill(SkillType.S_PILOT_JET).getType().getExperienceLevel(rawScore)) {
                            return getSkill(SkillType.S_GUN_JET).getType().getExperienceLevel(rawScore);
                        }
                    }

                    return (int) Math.floor((getSkill(SkillType.S_GUN_JET).getExperienceLevel()
                                             + getSkill(SkillType.S_PILOT_JET).getExperienceLevel()) / 2.0);
                } else {
                    return -1;
                }
            case T_BA:
                if (hasSkill(SkillType.S_GUN_BA) && hasSkill(SkillType.S_ANTI_MECH)) {
                    if (campaign.getCampaignOptions().useAltQualityAveraging()) {
                        int rawScore = (int) Math.floor(
                            (getSkill(SkillType.S_GUN_BA).getLevel() + getSkill(SkillType.S_ANTI_MECH)
                                    .getLevel()) / 2.0
                        );
                        if (getSkill(SkillType.S_GUN_BA).getType().getExperienceLevel(rawScore) ==
                            getSkill(SkillType.S_ANTI_MECH).getType().getExperienceLevel(rawScore)) {
                            return getSkill(SkillType.S_GUN_BA).getType().getExperienceLevel(rawScore);
                        }
                    }

                    return (int) Math.floor((getSkill(SkillType.S_GUN_BA).getExperienceLevel()
                                             + getSkill(SkillType.S_ANTI_MECH).getExperienceLevel()) / 2.0);
                } else {
                    return -1;
                }
            case T_PROTO_PILOT:
                if (hasSkill(SkillType.S_GUN_PROTO)) {
                    return getSkill(SkillType.S_GUN_PROTO).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_INFANTRY:
                if (hasSkill(SkillType.S_SMALL_ARMS)) {
                    return getSkill(SkillType.S_SMALL_ARMS).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_SPACE_PILOT:
                if (hasSkill(SkillType.S_PILOT_SPACE)) {
                    return getSkill(SkillType.S_PILOT_SPACE).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_SPACE_CREW:
                if (hasSkill(SkillType.S_TECH_VESSEL)) {
                    return getSkill(SkillType.S_TECH_VESSEL).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_SPACE_GUNNER:
                if (hasSkill(SkillType.S_GUN_SPACE)) {
                    return getSkill(SkillType.S_GUN_SPACE).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_NAVIGATOR:
                if (hasSkill(SkillType.S_NAV)) {
                    return getSkill(SkillType.S_NAV).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_MECH_TECH:
                if (hasSkill(SkillType.S_TECH_MECH)) {
                    return getSkill(SkillType.S_TECH_MECH).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_MECHANIC:
            case T_VEHICLE_CREW:
                if (hasSkill(SkillType.S_TECH_MECHANIC)) {
                    return getSkill(SkillType.S_TECH_MECHANIC).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_AERO_TECH:
                if (hasSkill(SkillType.S_TECH_AERO)) {
                    return getSkill(SkillType.S_TECH_AERO).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_BA_TECH:
                if (hasSkill(SkillType.S_TECH_BA)) {
                    return getSkill(SkillType.S_TECH_BA).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_ASTECH:
                if (hasSkill(SkillType.S_ASTECH)) {
                    return getSkill(SkillType.S_ASTECH).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_DOCTOR:
                if (hasSkill(SkillType.S_DOCTOR)) {
                    return getSkill(SkillType.S_DOCTOR).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_MEDIC:
                if (hasSkill(SkillType.S_MEDTECH)) {
                    return getSkill(SkillType.S_MEDTECH).getExperienceLevel();
                } else {
                    return -1;
                }
            case T_ADMIN_COM:
            case T_ADMIN_LOG:
            case T_ADMIN_TRA:
            case T_ADMIN_HR:
                if (hasSkill(SkillType.S_ADMIN)) {
                    return getSkill(SkillType.S_ADMIN).getExperienceLevel();
                } else {
                    return -1;
                }
            default:
                return -1;
        }
    }

    /**
     * returns a full description in HTML format that will be used for the graphical display in the
     * personnel table among other places
     * @param htmlRank if the rank will be wrapped in an HTML DIV and id
     * @return String
     */
    public String getFullDesc(boolean htmlRank) {
        return "<b>" + getFullTitle(htmlRank) + "</b><br/>" + getSkillSummary() + " " + getRoleDesc();
    }

    public String getFullTitle() {
        return getFullTitle(false);
    }

    public String getFullTitle(boolean html) {
        String rank = getRankName();

        // Do prisoner checks
        if (rank.equalsIgnoreCase("None")) {
            if (isPrisoner()) {
                return "Prisoner " + getFullName();
            }
            if (isBondsman()) {
                return "Bondsman " + getFullName();
            }
            return getFullName();
        }

        // This is used for the rank sorter. If you have a better way to accomplish it, by all means...
        // Of course, nothing that uses Full Title actually uses the rank sorter yet I guess...
        // Still, I've turned it back on and I don't see it messing anything up anywhere.
        // - Dylan
        // If we need it in html for any reason, make it so.
        if (html)
            rank = makeHTMLRankDiv();

        return rank + " " + getFullName();
    }

    public String makeHTMLRank() {
        return String.format("<html>%s</html>", makeHTMLRankDiv());
    }

    public String makeHTMLRankDiv() {
        return String.format("<div id=\"%s\">%s%s</div>", getId().toString(), getRankName(), (isPrisoner() && isWillingToDefect() ? "*" : ""));
    }

    public String getHyperlinkedFullTitle() {
        return String.format("<a href='PERSON:%s'>%s</a>", getId().toString(), getFullTitle());
    }

    /**
     * @return the primaryDesignator
     */
    public ROMDesignation getPrimaryDesignator() {
        return primaryDesignator;
    }

    /**
     * @param primaryDesignator the primaryDesignator to set
     */
    public void setPrimaryDesignator(ROMDesignation primaryDesignator) {
        this.primaryDesignator = primaryDesignator;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    /**
     * @return the secondaryDesignator
     */
    public ROMDesignation getSecondaryDesignator() {
        return secondaryDesignator;
    }

    /**
     * @param secondaryDesignator the secondaryDesignator to set
     */
    public void setSecondaryDesignator(ROMDesignation secondaryDesignator) {
        this.secondaryDesignator = secondaryDesignator;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public int getHealingDifficulty() {
        if (campaign.getCampaignOptions().useTougherHealing()) {
            return Math.max(0, getHits() - 2);
        }
        return 0;
    }

    public TargetRoll getHealingMods() {
        return new TargetRoll(getHealingDifficulty(), "difficulty");
    }

    public String fail() {
        return " <font color='red'><b>Failed to heal.</b></font>";
    }

    //region skill
    public boolean hasSkill(String skillName) {
        return skills.hasSkill(skillName);
    }

    public Skills getSkills() {
        return skills;
    }

    @Nullable
    public Skill getSkill(String skillName) {
        return skills.getSkill(skillName);
    }

    public void addSkill(String skillName, Skill skill) {
        skills.addSkill(skillName, skill);
    }

    public void addSkill(String skillName, int level, int bonus) {
        skills.addSkill(skillName, new Skill(skillName, level, bonus));
    }

    public void removeSkill(String skillName) {
        skills.removeSkill(skillName);
    }

    public int getSkillNumber() {
        return skills.size();
    }

    /**
     * Remove all skills
     */
    public void removeAllSkills() {
        skills.clear();
    }

    /**
     * Limit skills to the maximum of the given level
     */
    public void limitSkills(int maxLvl) {
        for (Skill skill : skills.getSkills()) {
            if (skill.getLevel() > maxLvl) {
                skill.setLevel(maxLvl);
            }
        }
    }

    public void improveSkill(String skillName) {
        if (hasSkill(skillName)) {
            getSkill(skillName).improve();
        } else {
            addSkill(skillName, 0, 0);
        }
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public int getCostToImprove(String skillName) {
        if (hasSkill(skillName)) {
            return getSkill(skillName).getCostToImprove();
        } else {
            return -1;
        }
    }
    //endregion skill

    public int getHits() {
        return hits;
    }

    public void setHits(int h) {
        this.hits = h;
    }

    /**
      * @return <tt>true</tt> if the location (or any of its parent locations) has an injury
      * which implies that the location (most likely a limb) is severed.
      */
    public boolean isLocationMissing(BodyLocation loc) {
        if (null == loc) {
            return false;
        }
        for (Injury i : getInjuriesByLocation(loc)) {
            if (i.getType().impliesMissingLocation(loc)) {
                return true;
            }
        }
        // Check parent locations as well (a hand can be missing if the corresponding arm is)
        return isLocationMissing(loc.Parent());
    }

    public void heal() {
        hits = Math.max(hits - 1, 0);
        if (!needsFixing()) {
            doctorId = null;
        }
    }

    public boolean needsFixing() {
        return ((hits > 0) || needsAMFixing()) && (status == PersonnelStatus.ACTIVE);
    }

    public String succeed() {
        heal();
        return " <font color='green'><b>Successfully healed one hit.</b></font>";
    }

    public PersonnelOptions getOptions() {
        return options;
    }

    /**
     * Returns the options of the given category that this pilot has
     */
    public Enumeration<IOption> getOptions(String grpKey) {
        return options.getOptions(grpKey);
    }

    public int countOptions(String grpKey) {
        int count = 0;

        for (Enumeration<IOptionGroup> i = options.getGroups(); i.hasMoreElements(); ) {
            IOptionGroup group = i.nextElement();

            if (!group.getKey().equalsIgnoreCase(grpKey)) {
                continue;
            }

            for (Enumeration<IOption> j = group.getOptions(); j.hasMoreElements(); ) {
                IOption option = j.nextElement();

                if (option.booleanValue()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Returns a string of all the option "codes" for this pilot, for a given group, using sep as the separator
     */
    public String getOptionList(String sep, String grpKey) {
        StringBuilder adv = new StringBuilder();

        if (null == sep) {
            sep = "";
        }

        for (Enumeration<IOptionGroup> i = options.getGroups(); i.hasMoreElements(); ) {
            IOptionGroup group = i.nextElement();
            if (!group.getKey().equalsIgnoreCase(grpKey)) {
                continue;
            }
            for (Enumeration<IOption> j = group.getOptions(); j.hasMoreElements(); ) {
                IOption option = j.nextElement();

                if (option.booleanValue()) {
                    if (adv.length() > 0) {
                        adv.append(sep);
                    }

                    adv.append(option.getName());
                    if ((option.getType() == IOption.STRING) || (option.getType() == IOption.CHOICE) || (option.getType() == IOption.INTEGER)) {
                        adv.append(" ").append(option.stringValue());
                    }
                }
            }
        }

        return adv.toString();
    }

    //region edge
    public int getEdge() {
        return getOptions().intOption("edge");
    }

    public void setEdge(int e) {
        for (Enumeration<IOption> i = getOptions(PilotOptions.EDGE_ADVANTAGES); i.hasMoreElements(); ) {
            IOption ability = i.nextElement();
            if (ability.getName().equals("edge")) {
                ability.setValue(e);
            }
        }
    }

    /**
     * Resets support personnel edge points to the purchased level. Used for weekly refresh.
     *
     */
    public void resetCurrentEdge() {
        setCurrentEdge(getEdge());
    }

    /**
     * Sets support personnel edge points to the value 'e'. Used for weekly refresh.
     * @param e - integer used to track this person's edge points available for the current week
     */
    public void setCurrentEdge(int e) {
        currentEdge = e;
    }

    /**
     *  Returns this person's currently available edge points. Used for weekly refresh.
     *
     */
    public int getCurrentEdge() {
        return currentEdge;
    }

    public void setEdgeUsed(int e) {
        edgeUsedThisRound = e;
    }

    public int getEdgeUsed() {
        return edgeUsedThisRound;
    }

    /**
     * This will set a specific edge trigger, regardless of the current status
     */
    public void setEdgeTrigger(String name, boolean status) {
        for (Enumeration<IOption> i = getOptions(PilotOptions.EDGE_ADVANTAGES); i.hasMoreElements(); ) {
            IOption ability = i.nextElement();
            if (ability.getName().equals(name)) {
                ability.setValue(status);
            }
        }
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    /**
     * This will flip the boolean status of the current edge trigger
     *
     * @param name of the trigger condition
     */
    public void changeEdgeTrigger(String name) {
        for (Enumeration<IOption> i = getOptions(PilotOptions.EDGE_ADVANTAGES); i.hasMoreElements(); ) {
            IOption ability = i.nextElement();
            if (ability.getName().equals(name)) {
                ability.setValue(!ability.booleanValue());
            }
        }
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    /**
     *
     * @return an html-coded tooltip that says what edge will be used
     */
    public String getEdgeTooltip() {
        StringBuilder edgett = new StringBuilder();
        for (Enumeration<IOption> i = getOptions(PilotOptions.EDGE_ADVANTAGES); i.hasMoreElements(); ) {
            IOption ability = i.nextElement();
            //yuck, it would be nice to have a more fool-proof way of identifying edge triggers
            if (ability.getName().contains("edge_when") && ability.booleanValue()) {
                edgett.append(ability.getDescription()).append("<br>");
            }
        }
        if (edgett.toString().equals("")) {
            return "No triggers set";
        }
        return "<html>" + edgett + "</html>";
    }
    //endregion edge

    /**
     *
     * @return an html-coded list that says what abilities are enabled for this pilot
     */
    public String getAbilityList(String type) {
        StringBuilder abilityString = new StringBuilder();
        for (Enumeration<IOption> i = getOptions(type); i.hasMoreElements(); ) {
            IOption ability = i.nextElement();
            if (ability.booleanValue()) {
                abilityString.append(Utilities.getOptionDisplayName(ability)).append("<br>");
            }
        }
        if (abilityString.length() == 0) {
            return null;
        }
        return "<html>" + abilityString + "</html>";
    }

    public boolean canDrive(Entity ent) {
        if (ent instanceof LandAirMech) {
            return hasSkill(SkillType.S_PILOT_MECH) && hasSkill(SkillType.S_PILOT_AERO);
        } else if (ent instanceof Mech) {
            return hasSkill(SkillType.S_PILOT_MECH);
        } else if (ent instanceof VTOL) {
            return hasSkill(SkillType.S_PILOT_VTOL);
        } else if (ent instanceof Tank) {
            if (ent.getMovementMode() == EntityMovementMode.NAVAL
                || ent.getMovementMode() == EntityMovementMode.HYDROFOIL
                || ent.getMovementMode() == EntityMovementMode.SUBMARINE) {
                return hasSkill(SkillType.S_PILOT_NVEE);
            } else {
                return hasSkill(SkillType.S_PILOT_GVEE);
            }
        } else if (ent instanceof ConvFighter) {
            return hasSkill(SkillType.S_PILOT_JET) || hasSkill(SkillType.S_PILOT_AERO);
        } else if (ent instanceof SmallCraft || ent instanceof Jumpship) {
            return hasSkill(SkillType.S_PILOT_SPACE);
        } else if (ent instanceof Aero) {
            return hasSkill(SkillType.S_PILOT_AERO);
        } else if (ent instanceof BattleArmor) {
            return hasSkill(SkillType.S_GUN_BA);
        } else if (ent instanceof Infantry) {
            return hasSkill(SkillType.S_SMALL_ARMS);
        } else if (ent instanceof Protomech) {
            return hasSkill(SkillType.S_GUN_PROTO);
        }
        return false;
    }

    public boolean canGun(Entity ent) {
        if (ent instanceof LandAirMech) {
            return hasSkill(SkillType.S_GUN_MECH) && hasSkill(SkillType.S_GUN_AERO);
        } else if (ent instanceof Mech) {
            return hasSkill(SkillType.S_GUN_MECH);
        } else if (ent instanceof Tank) {
            return hasSkill(SkillType.S_GUN_VEE);
        } else if (ent instanceof ConvFighter) {
            return hasSkill(SkillType.S_GUN_JET) || hasSkill(SkillType.S_GUN_AERO);
        } else if (ent instanceof SmallCraft || ent instanceof Jumpship) {
            return hasSkill(SkillType.S_GUN_SPACE);
        } else if (ent instanceof Aero) {
            return hasSkill(SkillType.S_GUN_AERO);
        } else if (ent instanceof BattleArmor) {
            return hasSkill(SkillType.S_GUN_BA);
        } else if (ent instanceof Infantry) {
            return hasSkill(SkillType.S_SMALL_ARMS);
        } else if (ent instanceof Protomech) {
            return hasSkill(SkillType.S_GUN_PROTO);
        }
        return false;
    }

    public boolean canTech(Entity ent) {
        if (ent instanceof Mech || ent instanceof Protomech) {
            return hasSkill(SkillType.S_TECH_MECH);
        } else if (ent instanceof Aero) {
            return hasSkill(SkillType.S_TECH_AERO);
        } else if (ent instanceof BattleArmor) {
            return hasSkill(SkillType.S_TECH_BA);
        } else if (ent instanceof Tank) {
            return hasSkill(SkillType.S_TECH_MECHANIC);
        }
        return false;
    }

    public int getMaintenanceTimeUsing() {
        int time = 0;
        for (UUID id : getTechUnitIDs()) {
            Unit u = campaign.getUnit(id);
            if (null != u) {
                time += u.getMaintenanceTime();
            }
        }
        return time;
    }

    public boolean isMothballing() {
        if (!isTech()) {
            return false;
        }
        for (UUID unitId : techUnitIds) {
            Unit u = campaign.getUnit(unitId);
            if ((u != null) && u.isMothballing()) {
                return true;
            }
        }
        return false;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID i) {
        unitId = i;
    }

    public void removeTechUnitId(UUID i) {
        techUnitIds.remove(i);
    }

    public void addTechUnitID(UUID id) {
        if (!techUnitIds.contains(id)) {
            techUnitIds.add(id);
        }
    }

    public void clearTechUnitIDs() {
        techUnitIds.clear();
    }

    public List<UUID> getTechUnitIDs() {
        return techUnitIds;
    }

    public int getMinutesLeft() {
        return minutesLeft;
    }

    public void setMinutesLeft(int m) {
        this.minutesLeft = m;
        if(engineer && null != getUnitId()) {
            //set minutes for all crewmembers
            Unit u = campaign.getUnit(getUnitId());
            if(null != u) {
                for(Person p : u.getActiveCrew()) {
                    p.setMinutesLeft(m);
                }
            }
        }
    }

    public int getOvertimeLeft() {
        return overtimeLeft;
    }

    public void setOvertimeLeft(int m) {
        this.overtimeLeft = m;
        if(engineer && null != getUnitId()) {
            //set minutes for all crewmembers
            Unit u = campaign.getUnit(getUnitId());
            if(null != u) {
                for(Person p : u.getActiveCrew()) {
                    p.setMinutesLeft(m);
                }
            }
        }
    }

    public void resetMinutesLeft() {
        if (isTechPrimary() || (getPrimaryRole() == T_DOCTOR)) {
            this.minutesLeft = PRIMARY_ROLE_SUPPORT_TIME;
            this.overtimeLeft = PRIMARY_ROLE_OVERTIME_SUPPORT_TIME;
        }
        if (isTechSecondary() || (getSecondaryRole() == T_DOCTOR)) {
            this.minutesLeft = SECONDARY_ROLE_SUPPORT_TIME;
            this.overtimeLeft = SECONDARY_ROLE_OVERTIME_SUPPORT_TIME;
        }
    }

    public boolean isAdmin() {
        return (isAdminPrimary() || isAdminSecondary());
    }

    public boolean isMedic() {
        return (T_MEDIC == primaryRole) || (T_MEDIC == secondaryRole);
    }

    public boolean isAdminPrimary() {
        return primaryRole == T_ADMIN_HR || primaryRole == T_ADMIN_COM || primaryRole == T_ADMIN_LOG || primaryRole == T_ADMIN_TRA;
    }

    public boolean isAdminSecondary() {
        return secondaryRole == T_ADMIN_HR || secondaryRole == T_ADMIN_COM || secondaryRole == T_ADMIN_LOG || secondaryRole == T_ADMIN_TRA;
    }

    public Skill getBestTechSkill() {
        Skill skill = null;
        int lvl = -1;
        if (hasSkill(SkillType.S_TECH_MECH) && getSkill(SkillType.S_TECH_MECH).getExperienceLevel() > lvl) {
            skill = getSkill(SkillType.S_TECH_MECH);
            lvl = getSkill(SkillType.S_TECH_MECH).getExperienceLevel();
        }
        if (hasSkill(SkillType.S_TECH_AERO) && getSkill(SkillType.S_TECH_AERO).getExperienceLevel() > lvl) {
            skill = getSkill(SkillType.S_TECH_AERO);
            lvl = getSkill(SkillType.S_TECH_AERO).getExperienceLevel();
        }
        if (hasSkill(SkillType.S_TECH_MECHANIC) && getSkill(SkillType.S_TECH_MECHANIC).getExperienceLevel() > lvl) {
            skill = getSkill(SkillType.S_TECH_MECHANIC);
            lvl = getSkill(SkillType.S_TECH_MECHANIC).getExperienceLevel();
        }
        if (hasSkill(SkillType.S_TECH_BA) && getSkill(SkillType.S_TECH_BA).getExperienceLevel() > lvl) {
            skill = getSkill(SkillType.S_TECH_BA);
        }
        return skill;
    }

    public boolean isTech() {
        //type must be correct and you must be more than ultra-green in the skill
        boolean isMechTech = hasSkill(SkillType.S_TECH_MECH) && getSkill(SkillType.S_TECH_MECH).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
        boolean isAeroTech = hasSkill(SkillType.S_TECH_AERO) && getSkill(SkillType.S_TECH_AERO).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
        boolean isMechanic = hasSkill(SkillType.S_TECH_MECHANIC) && getSkill(SkillType.S_TECH_MECHANIC).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
        boolean isBATech = hasSkill(SkillType.S_TECH_BA) && getSkill(SkillType.S_TECH_BA).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
        // At some point we may want to re-write things to include this
        /*boolean isEngineer = hasSkill(SkillType.S_TECH_VESSEL) && getSkill(SkillType.S_TECH_VESSEL).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN
                && campaign.getUnit(getUnitId()).getEngineer() != null
                && campaign.getUnit(getUnitId()).getEngineer().equals(this);*/
        return (isTechPrimary() || isTechSecondary()) && (isMechTech || isAeroTech || isMechanic || isBATech);
    }

    public boolean isTechPrimary() {
        return primaryRole == T_MECH_TECH || primaryRole == T_AERO_TECH || primaryRole == T_MECHANIC || primaryRole == T_BA_TECH || primaryRole == T_SPACE_CREW;
    }

    public boolean isTechSecondary() {
        return secondaryRole == T_MECH_TECH || secondaryRole == T_AERO_TECH || secondaryRole == T_MECHANIC || secondaryRole == T_BA_TECH;
    }

    public boolean isTaskOvertime(IPartWork partWork) {
        return partWork.getTimeLeft() > getMinutesLeft()
               && (partWork.getTimeLeft() - getMinutesLeft()) <= getOvertimeLeft();
    }

    public Skill getSkillForWorkingOn(IPartWork part) {
        Unit unit = part.getUnit();
        Skill skill = getSkillForWorkingOn(unit);
        if (null != skill) {
            return skill;
        }
        //check spare parts
        //return the best one
        if (part.isRightTechType(SkillType.S_TECH_MECH) && hasSkill(SkillType.S_TECH_MECH)) {
            skill = getSkill(SkillType.S_TECH_MECH);
        }
        if (part.isRightTechType(SkillType.S_TECH_BA) && hasSkill(SkillType.S_TECH_BA)) {
            if (null == skill || skill.getFinalSkillValue() > getSkill(SkillType.S_TECH_BA).getFinalSkillValue()) {
                skill = getSkill(SkillType.S_TECH_BA);
            }
        }
        if (part.isRightTechType(SkillType.S_TECH_AERO) && hasSkill(SkillType.S_TECH_AERO)) {
            if (null == skill || skill.getFinalSkillValue() > getSkill(SkillType.S_TECH_AERO).getFinalSkillValue()) {
                skill = getSkill(SkillType.S_TECH_AERO);
            }
        }
        if (part.isRightTechType(SkillType.S_TECH_MECHANIC) && hasSkill(SkillType.S_TECH_MECHANIC)) {
            if (null == skill || skill.getFinalSkillValue() > getSkill(SkillType.S_TECH_MECHANIC).getFinalSkillValue()) {
                skill = getSkill(SkillType.S_TECH_MECHANIC);
            }
        }
        if (part.isRightTechType(SkillType.S_TECH_VESSEL) && hasSkill(SkillType.S_TECH_VESSEL)) {
            if (null == skill || skill.getFinalSkillValue() > getSkill(SkillType.S_TECH_VESSEL).getFinalSkillValue()) {
                skill = getSkill(SkillType.S_TECH_VESSEL);
            }
        }
        if (null != skill) {
            return skill;
        }
        //if we are still here then we didn't have the right tech skill, so return the highest
        //of any tech skills that we do have
        if (hasSkill(SkillType.S_TECH_MECH)) {
            skill = getSkill(SkillType.S_TECH_MECH);
        }
        if (hasSkill(SkillType.S_TECH_BA)) {
            if (null == skill || skill.getFinalSkillValue() > getSkill(SkillType.S_TECH_BA).getFinalSkillValue()) {
                skill = getSkill(SkillType.S_TECH_BA);
            }
        }
        if (hasSkill(SkillType.S_TECH_MECHANIC)) {
            if (null == skill || skill.getFinalSkillValue() > getSkill(SkillType.S_TECH_MECHANIC).getFinalSkillValue()) {
                skill = getSkill(SkillType.S_TECH_MECHANIC);
            }
        }
        if (hasSkill(SkillType.S_TECH_AERO)) {
            if (null == skill || skill.getFinalSkillValue() > getSkill(SkillType.S_TECH_AERO).getFinalSkillValue()) {
                skill = getSkill(SkillType.S_TECH_AERO);
            }
        }
        return skill;
    }

    public Skill getSkillForWorkingOn(Unit unit) {
        if (unit == null) {
            return null;
        }
        if ((unit.getEntity() instanceof Mech || unit.getEntity() instanceof Protomech)
            && hasSkill(SkillType.S_TECH_MECH)) {
            return getSkill(SkillType.S_TECH_MECH);
        }
        if (unit.getEntity() instanceof BattleArmor && hasSkill(SkillType.S_TECH_BA)) {
            return getSkill(SkillType.S_TECH_BA);
        }
        if (unit.getEntity() instanceof Tank && hasSkill(SkillType.S_TECH_MECHANIC)) {
            return getSkill(SkillType.S_TECH_MECHANIC);
        }
        if ((unit.getEntity() instanceof Dropship || unit.getEntity() instanceof Jumpship)
            && hasSkill(SkillType.S_TECH_VESSEL)) {
            return getSkill(SkillType.S_TECH_VESSEL);
        }
        if (unit.getEntity() instanceof Aero
            && !(unit.getEntity() instanceof Dropship)
            && !(unit.getEntity() instanceof Jumpship)
            && hasSkill(SkillType.S_TECH_AERO)) {
            return getSkill(SkillType.S_TECH_AERO);
        }
        return null;
    }

    public Skill getSkillForWorkingOn(String skillName) {
        if (skillName.equals(CampaignOptions.S_TECH)) {
            return getBestTechSkill();
        }
        if (hasSkill(skillName)) {
            return getSkill(skillName);
        }
        return null;
    }

    public int getBestTechLevel() {
        int lvl = -1;
        Skill mechSkill = getSkill(SkillType.S_TECH_MECH);
        Skill mechanicSkill = getSkill(SkillType.S_TECH_MECHANIC);
        Skill baSkill = getSkill(SkillType.S_TECH_BA);
        Skill aeroSkill = getSkill(SkillType.S_TECH_AERO);
        if (null != mechSkill && mechSkill.getLevel() > lvl) {
            lvl = mechSkill.getLevel();
        }
        if (null != mechanicSkill && mechanicSkill.getLevel() > lvl) {
            lvl = mechanicSkill.getLevel();
        }
        if (null != baSkill && baSkill.getLevel() > lvl) {
            lvl = baSkill.getLevel();
        }
        if (null != aeroSkill && aeroSkill.getLevel() > lvl) {
            lvl = aeroSkill.getLevel();
        }
        return lvl;
    }

    public boolean isRightTechTypeFor(IPartWork part) {
        Unit unit = part.getUnit();
        if (null == unit) {
            return (hasSkill(SkillType.S_TECH_MECH) && part.isRightTechType(SkillType.S_TECH_MECH))
                    || (hasSkill(SkillType.S_TECH_AERO) && part.isRightTechType(SkillType.S_TECH_AERO))
                    || (hasSkill(SkillType.S_TECH_MECHANIC) && part.isRightTechType(SkillType.S_TECH_MECHANIC))
                    || (hasSkill(SkillType.S_TECH_BA) && part.isRightTechType(SkillType.S_TECH_BA))
                    || (hasSkill(SkillType.S_TECH_VESSEL) && part.isRightTechType(SkillType.S_TECH_VESSEL));
        }
        if (unit.getEntity() instanceof Mech || unit.getEntity() instanceof Protomech) {
            return hasSkill(SkillType.S_TECH_MECH);
        }
        if (unit.getEntity() instanceof BattleArmor) {
            return hasSkill(SkillType.S_TECH_BA);
        }
        if (unit.getEntity() instanceof Tank || unit.getEntity() instanceof Infantry) {
            return hasSkill(SkillType.S_TECH_MECHANIC);
        }
        if (unit.getEntity() instanceof Dropship || unit.getEntity() instanceof Jumpship) {
            return hasSkill(SkillType.S_TECH_VESSEL);
        }
        if (unit.getEntity() instanceof Aero) {
            return hasSkill(SkillType.S_TECH_AERO);
        }
        return false;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public boolean isDoctor() {
        return hasSkill(SkillType.S_DOCTOR) && (primaryRole == T_DOCTOR || secondaryRole == T_DOCTOR);
    }

    public int getToughness() {
        return toughness;
    }

    public void setToughness(int t) {
        toughness = t;
    }

    public void resetSkillTypes() {
        for (Skill s : skills.getSkills()) {
            s.updateType();
        }
    }

    public int getOldId() {
        return oldId;
    }

    public void fixIdReferences(Map<Integer, UUID> uHash, Map<Integer, UUID> pHash) {
        unitId = uHash.get(oldUnitId);
        doctorId = pHash.get(oldDoctorId);
    }

    public int getNTasks() {
        return nTasks;
    }

    public void setNTasks(int n) {
        nTasks = n;
    }

    public List<LogEntry> getPersonnelLog() {
        personnelLog.sort(Comparator.comparing(LogEntry::getDate));
        return personnelLog;
    }

    public List<LogEntry> getMissionLog() {
        missionLog.sort(Comparator.comparing(LogEntry::getDate));
        return missionLog;
    }

    public void addLogEntry(LogEntry entry) {
        personnelLog.add(entry);
    }

    public void addMissionLogEntry(LogEntry entry) {
        missionLog.add(entry);
    }

    //region injuries
    /**
     * All methods below are for the Advanced Medical option **
     */

    public List<Injury> getInjuries() {
        return new ArrayList<>(injuries);
    }

    public void clearInjuries() {
        injuries.clear();

        // Clear the doctor if there is one
        doctorId = null;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public void removeInjury(Injury i) {
        injuries.remove(i);
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public void diagnose(int hits) {
        InjuryUtil.resolveAfterCombat(campaign, this, hits);
        InjuryUtil.resolveCombatDamage(campaign, this, hits);
        setHits(0);
    }

    public void changeStatus(PersonnelStatus status) {
        if (status == getStatus()) {
            return;
        }
        Unit u = campaign.getUnit(getUnitId());
        if (status == PersonnelStatus.KIA) {
            MedicalLogger.diedFromWounds(this, campaign.getDate());
            //set the date of death
            setDateOfDeath(getCampaign().getLocalDate());
        }
        if (status == PersonnelStatus.RETIRED) {
            ServiceLogger.retireDueToWounds(this, campaign.getDate());
        }
        setStatus(status);
        if (status != PersonnelStatus.ACTIVE) {
            setDoctorId(null, campaign.getCampaignOptions().getNaturalHealingWaitingPeriod());
            // If we're assigned to a unit, remove us from it
            if (null != u) {
                u.remove(this, true);
            }
            // If we're assigned as a tech for any unit, remove us from it/them
            if (!techUnitIds.isEmpty()) {
                for (UUID tuuid : techUnitIds) {
                    Unit t = campaign.getUnit(tuuid);
                    t.remove(this, true);
                }
            }
        }
    }

    public int getAbilityTimeModifier() {
        int modifier = 100;
        if (campaign.getCampaignOptions().useToughness()) {
            if (getToughness() == 1) {
                modifier -= 10;
            }
            if (getToughness() > 1) {
                modifier -= 15;
            }
        } // TODO: Fully implement this for advanced healing
        if (getOptions().booleanOption("pain_resistance")) {
            modifier -= 15;
        } else if (getOptions().booleanOption("iron_man")) {
            modifier -= 10;
        }

        return modifier;
    }

    public boolean hasInjury(BodyLocation loc) {
        return (null != getInjuryByLocation(loc));
    }

    public boolean needsAMFixing() {
        boolean retVal = false;
        if (injuries.size() > 0) {
            for (Injury i : injuries) {
                if (i.getTime() > 0 || !(i.isPermanent())) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    public int getPilotingInjuryMod() {
        return Modifier.calcTotalModifier(injuries.stream().flatMap(i -> i.getModifiers().stream()), ModifierValue.PILOTING);
    }

    public int getGunneryInjuryMod() {
        return Modifier.calcTotalModifier(injuries.stream().flatMap(i -> i.getModifiers().stream()), ModifierValue.GUNNERY);
    }

    public boolean hasInjuries(boolean permCheck) {
        boolean tf = false;
        if (injuries.size() > 0) {
            if (permCheck) {
                for (Injury injury : injuries) {
                    if (!injury.isPermanent() || injury.getTime() > 0) {
                        tf = true;
                        break;
                    }
                }
            } else {
                tf = true;
            }
        }
        return tf;
    }

    public boolean hasOnlyHealedPermanentInjuries() {
        if (injuries.size() == 0) {
            return false;
        }
        for (Injury injury : injuries) {
            if (!injury.isPermanent() || injury.getTime() > 0) {
                return false;
            }
        }
        return true;
    }

    public List<Injury> getInjuriesByLocation(BodyLocation loc) {
        return injuries.stream()
            .filter((i) -> (i.getLocation() == loc)).collect(Collectors.toList());
    }

    // Returns only the first injury in a location
    public Injury getInjuryByLocation(BodyLocation loc) {
        return injuries.stream()
            .filter((i) -> (i.getLocation() == loc)).findFirst().orElse(null);
    }

    public void addInjury(Injury i) {
        injuries.add(i);
        if (null != getUnitId()) {
            campaign.getUnit(getUnitId()).resetPilotAndEntity();
        }
    }
    //endregion injuries

    public int getProfession() {
        return getProfessionFromPrimaryRole(primaryRole);
    }

    public static int getProfessionFromPrimaryRole(int role) {
        switch (role) {
            case T_AERO_PILOT:
            case T_CONV_PILOT:
                return Ranks.RPROF_ASF;
            case T_GVEE_DRIVER:
            case T_NVEE_DRIVER:
            case T_VTOL_PILOT:
            case T_VEE_GUNNER:
            case T_VEHICLE_CREW:
                return Ranks.RPROF_VEE;
            case T_BA:
            case T_INFANTRY:
                return Ranks.RPROF_INF;
            case T_SPACE_PILOT:
            case T_SPACE_CREW:
            case T_SPACE_GUNNER:
            case T_NAVIGATOR:
                return Ranks.RPROF_NAVAL;
            case T_MECH_TECH:
            case T_MECHANIC:
            case T_AERO_TECH:
            case T_BA_TECH:
            case T_ASTECH:
            case T_ADMIN_COM:
            case T_ADMIN_LOG:
            case T_ADMIN_TRA:
            case T_ADMIN_HR:
                return Ranks.RPROF_TECH;
            case T_MECHWARRIOR:
            case T_PROTO_PILOT:
            case T_DOCTOR:
            case T_MEDIC:
            default:
                return Ranks.RPROF_MW;
        }
    }

    /* For use by Against the Bot retirement/defection rolls */

    public boolean isFounder() {
        return founder;
    }

    public void setFounder(boolean founder) {
        this.founder = founder;
    }

    public int getOriginalUnitWeight() {
        return originalUnitWeight;
    }

    public void setOriginalUnitWeight(int weight) {
        originalUnitWeight = weight;
    }

    public int getOriginalUnitTech() {
        return originalUnitTech;
    }

    public void setOriginalUnitTech(int tech) {
        originalUnitTech = tech;
    }

    public UUID getOriginalUnitId() {
        return originalUnitId;
    }

    public void setOriginalUnitId(UUID id) {
        originalUnitId = id;
    }

    public void setOriginalUnit(Unit unit) {
        originalUnitId = unit.getId();
        if (unit.getEntity().isClan()) {
            originalUnitTech = TECH_CLAN;
        } else if (unit.getEntity().getTechLevel() > megamek.common.TechConstants.T_INTRO_BOXSET) {
            originalUnitTech = TECH_IS2;
        } else {
            originalUnitTech = TECH_IS1;
        }
        originalUnitWeight = unit.getEntity().getWeightClass();
    }

    /**
     * This is used to get the number of shares the person has
     * @param sharesForAll true if all combat and support personnel have shares, otherwise false if
     *                     just MechWarriors have shares
     * @return the number of shares the person has
     */
    public int getNumShares(boolean sharesForAll) {
        if (!isActive() || !isFree() || (!sharesForAll && !hasRole(T_MECHWARRIOR))) {
            return 0;
        }
        int shares = 1;
        if (isFounder()) {
            shares++;
        }
        shares += Math.max(-1, getExperienceLevel(false) - 2);

        if (getRank().isOfficer()) {
            Ranks ranks = getRanks();
            int rankOrder = ranks.getOfficerCut();
            while ((rankOrder <= getRankNumeric()) && (rankOrder < Ranks.RC_NUM)) {
                Rank rank = ranks.getAllRanks().get(rankOrder);
                if (!rank.getName(getProfession()).equals("-")) {
                    shares++;
                }
                rankOrder++;
            }
        }
        if (getOriginalUnitWeight() >= 1) {
            shares++;
        }
        if (getOriginalUnitWeight()  >= 3) {
            shares++;
        }
        shares += getOriginalUnitTech();

        return shares;
    }

    public boolean isDeadOrMIA() {
        return (status == PersonnelStatus.KIA) || (status == PersonnelStatus.MIA);
    }

    public boolean isEngineer() {
        return engineer;
    }

    public void setEngineer(boolean b) {
        engineer = b;
    }

    /**
     *
     * @return the ransom value of this individual
     * Useful for prisoner who you want to ransom or hand off to your employer in an AtB context
     */
    public Money getRansomValue() {
        // MechWarriors and aero pilots are worth more than the other types of scrubs
        if ((primaryRole == T_MECHWARRIOR) || (primaryRole == T_AERO_PILOT)) {
            return MECHWARRIOR_AERO_RANSOM_VALUES.get(getExperienceLevel(false));
        } else {
            return OTHER_RANSOM_VALUES.get(getExperienceLevel(false));
        }
    }

    //region Family
    //region setFamily
    /**
     *
     * @param id is the new ancestor id for the current person
     */
    public void setAncestorsId(UUID id) {
        ancestorsId = id;
    }

    /**
     *
     * @param spouse the new spouse id for the current person
     */
    public void setSpouseId(UUID spouse) {
        this.spouse = spouse;
    }

    /**
     *
     * @param formerSpouse a former spouse to add the the current person's list
     */
    public void addFormerSpouse(FormerSpouse formerSpouse) {
        formerSpouses.add(formerSpouse);
    }
    //endregion setFamily

    //region hasFamily
    /**
     *
     * @return true if the person has either a spouse, any children, or specified parents.
     *          These are required for any extended family to exist.
     */
    public boolean hasAnyFamily() {
        return hasChildren() || hasSpouse() || hasParents();
    }

    /**
     *
     * @return true if the person has a spouse, false otherwise
     */
    public boolean hasSpouse() {
        return (getSpouseId() != null);
    }

    /**
     *
     * @return true if the person has a former spouse, false otherwise
     */
    public boolean hasFormerSpouse() {
        return !formerSpouses.isEmpty();
    }

    /**
     *
     * @return true if the person has at least one kid, false otherwise
     */
    public boolean hasChildren() {
        if (getId() != null) {
            for (Ancestors a : campaign.getAncestors()) {
                if (getId().equals(a.getMotherId()) || getId().equals(a.getFatherId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @return true if the person has at least one grandchild, false otherwise
     */
    public boolean hasGrandchildren() {
        for (Ancestors a : campaign.getAncestors()) {
            if (getId().equals(a.getMotherId()) || getId().equals(a.getFatherId())) {
                for (Person p : campaign.getPersonnel()) {
                    if (a.getId().equals(p.getAncestorsId())) {
                        if (p.hasChildren()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @return true if the Person has either a mother or father, otherwise false
     */
    public boolean hasParents() {
        return hasFather() || hasMother();
    }

    /**
     *
     * @return true if the person has a listed father, false otherwise
     */
    public boolean hasFather() {
        return getFather() != null;
    }

    /**
     *
     * @return true if the Person has a listed mother, false otherwise
     */
    public boolean hasMother() {
        return getMother() != null;
    }

    /**
     *
     * @return true if the person has siblings, false otherwise
     */
    public boolean hasSiblings() {
        return !getSiblings().isEmpty();
    }

    /**
     *
     * @return true if the Person has a grandparent, false otherwise
     */
    public boolean hasGrandparent() {
        if (hasFather()) {
            if (hasFathersParents()) {
                return true;
            }
        }

        if (hasMother()) {
            return hasMothersParents();
        }
        return false;
    }

    /**
     *
     * @return true if the person's father has any parents, false otherwise
     */
    public boolean hasFathersParents() {
        return getFather().hasParents();
    }

    /**
     *
     * @return true if the person's mother has any parents, false otherwise
     */
    public boolean hasMothersParents() {
        return getMother().hasParents();
    }

    /**
     *
     * @return true if the Person has an Aunt or Uncle, false otherwise
     */
    public boolean hasAuntOrUncle() {
        if (hasFather()) {
            if (hasFathersSiblings()) {
                return true;
            }
        }

        if (hasMother()) {
            return hasMothersSiblings();
        }
        return false;
    }

    /**
     *
     * @return true if the person's father has siblings, false otherwise
     */
    public boolean hasFathersSiblings() {
        return getFather().hasSiblings();
    }

    /**
     *
     * @return true if the person's mother has siblings, false otherwise
     */
    public boolean hasMothersSiblings() {
        return getMother().hasSiblings();
    }

    /**
     *
     * @return true if the person has cousins, false otherwise
     */
    public boolean hasCousins() {
        if (hasFather() && getFather().hasSiblings()) {
            for (Person sibling : getFather().getSiblings()) {
                if (sibling.hasChildren()) {
                    return true;
                }
            }
        }

        if (hasMother() && getMother().hasSiblings()) {
            for (Person sibling : getMother().getSiblings()) {
                if (sibling.hasChildren()) {
                    return true;
                }
            }
        }

        return false;
    }
    //endregion hasFamily

    //region getFamily
    /**
     *
     * @return the person's ancestor id
     */
    public UUID getAncestorsId() {
        return ancestorsId;
    }

    /**
     *
     * @return the person's ancestors
     */
    public Ancestors getAncestors() {
        return campaign.getAncestors(ancestorsId);
    }

    /**
     *
     * @return the current person's spouse
     */
    @Nullable
    public Person getSpouse() {
        return campaign.getPerson(spouse);
    }

    /**
     *
     * @return the current person's spouse's id
     */
    @Nullable
    public UUID getSpouseId() {
        return spouse;
    }

    /**
     *
     * @return a list of FormerSpouse objects for all the former spouses of the current person
     */
    public List<FormerSpouse> getFormerSpouses() {
        return formerSpouses;
    }

    /**
     * getChildren creates a list of all children from the current person
     * @return a list of Person objects for all children of the current person
     */
    public List<Person> getChildren() {
        List<UUID> ancestors = new ArrayList<>();
        for (Ancestors a : campaign.getAncestors()) {
            if ((a != null) && (getId().equals(a.getMotherId()) || getId().equals(a.getFatherId()))) {
                ancestors.add(a.getId());
            }
        }

        List<Person> children = new ArrayList<>();
        for (Person p : campaign.getPersonnel()) {
            if (ancestors.contains(p.getAncestorsId())) {
                children.add(p);
            }
        }

        return children;
    }

    public List<Person> getGrandchildren() {
        List<Person> grandchildren = new ArrayList<>();
        List<Person> tempChildList;

        for (Ancestors a : campaign.getAncestors()) {
            if ((a != null) && (getId().equals(a.getMotherId()) || getId().equals(a.getFatherId()))) {
                for (Person p : campaign.getPersonnel()) {
                    if ((a.getId().equals(p.getAncestorsId())) && p.hasChildren()) {
                        tempChildList = p.getChildren();
                        //prevents duplicates, if anyone uses a small number of depth for their ancestry
                        tempChildList.removeAll(grandchildren);
                        grandchildren.addAll(tempChildList);
                    }
                }
            }
        }

        return grandchildren;
    }

    /**
     *
     * @return the current person's father
     */
    public Person getFather() {
        Ancestors a = getAncestors();

        if (a != null) {
            return campaign.getPerson(a.getFatherId());
        }
        return null;
    }

    /**
     *
     * @return the current person's mother
     */
    public Person getMother() {
        Ancestors a = getAncestors();

        if (a != null) {
            return campaign.getPerson(a.getMotherId());
        }
        return null;
    }

    /**
     * getSiblings creates a list of all the siblings from the current person
     * @return a list of Person objects for all the siblings of the current person
     */
    public List<Person> getSiblings() {
        List<UUID> parents = new ArrayList<>();
        List<Person> siblings = new ArrayList<>();
        Person father = getFather();
        Person mother = getMother();

        for (Ancestors a : campaign.getAncestors()) {
            if ((a != null)
                    && (((father != null) && father.getId().equals(a.getFatherId()))
                        || ((mother != null) && mother.getId().equals(a.getMotherId())))) {

                parents.add(a.getId());
            }
        }

        for (Person p : campaign.getPersonnel()) {
            if (parents.contains(p.getAncestorsId()) && !(p.getId().equals(getId()))) {
                siblings.add(p);
            }
        }

        return siblings;
    }

    /**
     *
     * @return a list of the person's siblings with spouses (if any
     */
    public List<Person> getSiblingsAndSpouses(){
        List<UUID> parents = new ArrayList<>();
        List<Person> siblingsAndSpouses = new ArrayList<>();

        Person father = getFather();
        Person mother = getMother();

        for (Ancestors a : campaign.getAncestors()) {
            if ((a != null)
                    && (((father != null) && father.getId().equals(a.getFatherId()))
                        || ((mother != null) && mother.getId().equals(a.getMotherId())))) {

                parents.add(a.getId());
            }
        }

        for (Person p : campaign.getPersonnel()) {
            if (parents.contains(p.getAncestorsId()) && !(p.getId().equals(getId()))) {
                siblingsAndSpouses.add(p);
                if (p.hasSpouse()) {
                    siblingsAndSpouses.add(campaign.getPerson(p.getSpouseId()));
                }
            }
        }

        return siblingsAndSpouses;
    }

    /**
     *
     * @return a list of the person's grandparents
     */
    public List<Person> getGrandparents() {
        List<Person> grandparents = new ArrayList<>();
        if (hasFather()) {
            grandparents.addAll(getFathersParents());
        }

        if (hasMother()) {
            List<Person> mothersParents = getMothersParents();
            //prevents duplicates, if anyone uses a small number of depth for their ancestry
            mothersParents.removeAll(grandparents);
            grandparents.addAll(mothersParents);
        }
        return grandparents;
    }

    /**
     *
     * @return a list of the person's father's parents
     */
    public List<Person> getFathersParents() {
        List<Person> fathersParents = new ArrayList<>();
        if (getFather().hasFather()) {
            fathersParents.add(getFather().getFather());
        }
        if (getFather().hasMother()) {
            fathersParents.add(getFather().getMother());
        }

        return fathersParents;
    }

    /**
     *
     * @return a list of the person's mother's parents
     */
    public List<Person> getMothersParents() {
        List<Person> mothersParents = new ArrayList<>();
        if (getMother().hasFather()) {
            mothersParents.add(getMother().getFather());
        }
        if (getMother().hasMother()) {
            mothersParents.add(getMother().getMother());
        }

        return mothersParents;
    }

    /**
     *
     * @return a list of the person's Aunts and Uncles
     */
    public List<Person> getsAuntsAndUncles() {
        List<Person> auntsAndUncles = new ArrayList<>();
        if (hasFather()) {
            auntsAndUncles.addAll(getFathersSiblings());
        }

        if (hasMother()) {
            List<Person> mothersSiblings = getMothersSiblings();
            //prevents duplicates, if anyone uses a small number of depth for their ancestry
            mothersSiblings.removeAll(auntsAndUncles);
            auntsAndUncles.addAll(mothersSiblings);
        }

        return auntsAndUncles;
    }

    /**
     *
     * @return a list of the person's father's siblings and their current spouses
     */
    public List<Person> getFathersSiblings() {
        return getFather().getSiblingsAndSpouses();
    }

    /**
     *
     * @return a list of the person's mothers's siblings and their current spouses
     */
    public List<Person> getMothersSiblings() {
        return getMother().getSiblingsAndSpouses();
    }

    /**
     *
     * @return a list of the person'c cousins
     */
    public List<Person> getCousins() {
        List<Person> cousins = new ArrayList<>();
        List<Person> tempCousins;
        if (hasFather() && getFather().hasSiblings()) {
            for (Person sibling : getFather().getSiblings()) {
                tempCousins = sibling.getChildren();
                tempCousins.removeAll(cousins);
                cousins.addAll(tempCousins);
            }
        }

        if (hasMother() && getMother().hasSiblings()) {
            for (Person sibling : getMother().getSiblings()) {
                tempCousins = sibling.getChildren();
                tempCousins.removeAll(cousins);
                cousins.addAll(tempCousins);
            }
        }

        return cousins;
    }
    //endregion getFamily
    //endregion Family
}
