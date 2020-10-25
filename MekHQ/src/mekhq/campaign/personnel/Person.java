/*
 * Person.java
 *
 * Copyright (c) 2009 - Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
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
package mekhq.campaign.personnel;

import java.io.PrintWriter;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

import megamek.client.generator.RandomNameGenerator;
import megamek.common.*;
import megamek.common.enums.Gender;
import megamek.common.icons.AbstractIcon;
import megamek.common.util.EncodeControl;
import megamek.common.util.StringUtil;
import mekhq.campaign.*;
import mekhq.campaign.finances.Money;
import mekhq.campaign.io.CampaignXmlParser;
import mekhq.campaign.log.*;
import mekhq.campaign.parts.Part;
import mekhq.campaign.personnel.enums.*;
import mekhq.campaign.personnel.familyTree.Genealogy;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.annotations.Nullable;
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

    private static final Map<Integer, Money> MECHWARRIOR_AERO_RANSOM_VALUES;
    private static final Map<Integer, Money> OTHER_RANSOM_VALUES;

    private PersonAwardController awardController;

    //region Family Variables
    // Lineage
    private Genealogy genealogy;

    //region Procreation
    // this is a flag used in random procreation to determine whether or not to attempt to procreate
    private boolean tryingToConceive;
    private LocalDate dueDate;
    private LocalDate expectedDueDate;

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
        while (Compute.randomInt(50) == 0) {
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
    //endregion Marriage
    //endregion Family Variables

    private UUID id;

    private String fullName;
    private String givenName;
    private String surname;
    private String honorific;
    private String maidenName;
    private String callsign;
    private Gender gender;

    private PersonnelRole primaryRole;
    private PersonnelRole secondaryRole;

    private ROMDesignation primaryDesignator;
    private ROMDesignation secondaryDesignator;

    private String biography;
    private LocalDate birthday;
    private LocalDate dateOfDeath;
    private LocalDate recruitment;
    private LocalDate lastRankChangeDate;
    private LocalDate retirement;
    private List<LogEntry> personnelLog;
    private List<LogEntry> missionLog;

    private Skills skills;
    private PersonnelOptions options;
    private int toughness;

    private PersonnelStatus status;
    private int xp;
    private int acquisitions;
    private Money salary;
    private Money totalEarnings;
    private int hits;
    private PrisonerStatus prisonerStatus;

    private boolean dependent;
    private boolean commander;

    // Supports edge usage by a ship's engineer composite crewman
    int edgeUsedThisRound;
    // To track how many edge points support personnel have left until next refresh
    int currentEdge;

    //phenotype and background
    private Phenotype phenotype;
    private boolean clan;
    private String bloodname;
    private Faction originFaction;
    private Planet originPlanet;

    //assignments
    private UUID unitId;
    private UUID doctorId;
    private List<UUID> techUnitIds;

    //days of rest
    private int idleMonths;
    private int daysToWaitForHealing;

    //region portrait
    private String portraitCategory;
    private String portraitFile;
    //endregion portrait

    // Our rank
    private int rank;
    private int rankLevel;
    // If this Person uses a custom rank system (-1 for no)
    private int rankSystem;
    private Ranks ranks;

    private ManeiDominiClass maneiDominiClass;
    private ManeiDominiRank maneiDominiRank;

    //stuff to track for support teams
    private int minutesLeft;
    private int overtimeLeft;
    private int nTasks;
    private boolean engineer;
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
    @Deprecated // May 1st, 2020 - As part of moving Person to be a fully OOP class
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

    private static final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel",
            new EncodeControl());

    //region Reverse Compatibility
    // Unknown version
    private int oldId;
    //endregion Reverse Compatibility
    //endregion Variable Declarations

    //region Constructors
    //default constructor
    protected Person(UUID id) {
        this.id = id;
    }

    public Person(Campaign campaign) {
        this(RandomNameGenerator.UNNAMED, RandomNameGenerator.UNNAMED_SURNAME, campaign);
    }

    public Person(Campaign campaign, String factionCode) {
        this(RandomNameGenerator.UNNAMED, RandomNameGenerator.UNNAMED_SURNAME, campaign, factionCode);
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
        id = UUID.randomUUID();
        this.givenName = givenName;
        this.surname = surname;
        this.honorific = honorific;
        maidenName = null; // this is set to null to handle divorce cases
        callsign = "";
        primaryRole = PersonnelRole.NONE;
        secondaryRole = PersonnelRole.NONE;
        primaryDesignator = ROMDesignation.NONE;
        secondaryDesignator = ROMDesignation.NONE;
        commander = false;
        dependent = false;
        originFaction = Faction.getFaction(factionCode);
        originPlanet = null;
        clan = originFaction.isClan();
        phenotype = Phenotype.NONE;
        bloodname = "";
        biography = "";
        idleMonths = -1;
        genealogy = new Genealogy(getId());
        tryingToMarry = true;
        tryingToConceive = true;
        dueDate = null;
        expectedDueDate = null;
        portraitCategory = AbstractIcon.ROOT_CATEGORY;
        portraitFile = AbstractIcon.DEFAULT_ICON_FILENAME;
        xp = 0;
        daysToWaitForHealing = 0;
        gender = Gender.MALE;
        rank = 0;
        rankLevel = 0;
        rankSystem = -1;
        maneiDominiRank = ManeiDominiRank.NONE;
        maneiDominiClass = ManeiDominiClass.NONE;
        nTasks = 0;
        doctorId = null;
        unitId = null;
        salary = Money.of(-1);
        totalEarnings = Money.of(0);
        status = PersonnelStatus.ACTIVE;
        prisonerStatus = PrisonerStatus.FREE;
        hits = 0;
        toughness = 0;
        resetMinutesLeft(); // this assigns minutesLeft and overtimeLeft
        birthday = null;
        dateOfDeath = null;
        recruitment = null;
        lastRankChangeDate = null;
        retirement = null;
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

    @Deprecated // May 1st, 2020 - as part of turning Person into a fully OOP class
    public Campaign getCampaign() {
        return campaign;
    }

    public Phenotype getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(Phenotype phenotype) {
        this.phenotype = phenotype;
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

    public void setBloodname(String bloodname) {
        this.bloodname = bloodname;
        setFullName();
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

    public PrisonerStatus getPrisonerStatus() {
        return prisonerStatus;
    }

    public void setPrisonerStatus(PrisonerStatus prisonerStatus) {
        setPrisonerStatus(prisonerStatus, true);
    }

    /**
     * This requires expanded checks because a number of functionalities are strictly dependant on
     * the current person's prisoner status.
     * @param prisonerStatus The new prisoner status for the person in question
     * @param log whether to log the change or not
     */
    public void setPrisonerStatus(PrisonerStatus prisonerStatus, boolean log) {
        // This must be processed completely, as the unchanged prisoner status of Free to Free is
        // used during recruitment

        final boolean freed = !getPrisonerStatus().isFree();
        final boolean isPrisoner = prisonerStatus.isPrisoner();
        this.prisonerStatus = prisonerStatus;

        // Now, we need to fix values and ranks based on the Person's status
        switch (prisonerStatus) {
            case PRISONER:
            case PRISONER_DEFECTOR:
            case BONDSMAN:
                setRecruitment(null);
                setLastRankChangeDate(null);
                if (log) {
                    if (isPrisoner) {
                        ServiceLogger.madePrisoner(this, getCampaign().getLocalDate(),
                                getCampaign().getName(), "");
                    } else {
                        ServiceLogger.madeBondsman(this, getCampaign().getLocalDate(),
                                getCampaign().getName(), "");
                    }
                }
                break;
            case FREE:
                if (!isDependent()) {
                    if (getCampaign().getCampaignOptions().getUseTimeInService()) {
                        setRecruitment(getCampaign().getLocalDate());
                    }
                    if (getCampaign().getCampaignOptions().getUseTimeInRank()) {
                        setLastRankChangeDate(getCampaign().getLocalDate());
                    }
                }
                if (log) {
                    if (freed) {
                        ServiceLogger.freed(this, getCampaign().getLocalDate(),
                                getCampaign().getName(), "");
                    } else {
                        ServiceLogger.joined(this, getCampaign().getLocalDate(),
                                getCampaign().getName(), "");
                    }
                }
                break;
        }

        if (!prisonerStatus.isFree()) {
            Unit u = getCampaign().getUnit(getUnitId());
            if (u != null) {
                u.remove(this, true);
            } else {
                setUnitId(null);
            }
        }

        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    //region Text Getters
    public String pregnancyStatus() {
        return isPregnant() ? " (Pregnant)" : "";
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

    /**
     * return a full last name which may be a bloodname or a surname with or without honorifics.
     * A bloodname will overrule a surname but we do not disallow surnames for clanners, if the
     * player wants to input them
     * @return a String of the person's last name
     */
    public String getLastName() {
        String lastName = "";
        if (!StringUtil.isNullOrEmpty(bloodname)) {
            lastName = bloodname;
        } else if (!StringUtil.isNullOrEmpty(surname)) {
            lastName = surname;
        }

        if (!StringUtil.isNullOrEmpty(honorific)) {
            lastName += " " + honorific;
        }
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName() {
        String lastName = getLastName();
        if (!StringUtil.isNullOrEmpty(lastName)) {
            fullName = givenName + " " + lastName;
        } else {
            fullName = givenName;
        }
    }

    /**
     * This method is used to migrate names from being a joined name to split between given name and surname,
     * as part of the Personnel changes in MekHQ 0.47.4.
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

        if ((surname == null) || (surname.equals(RandomNameGenerator.UNNAMED_SURNAME))) {
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
        return portraitCategory;
    }

    public String getPortraitFileName() {
        return portraitFile;
    }

    public void setPortraitCategory(String s) {
        this.portraitCategory = s;
    }

    public void setPortraitFileName(String s) {
        this.portraitFile = s;
    }

    //region Personnel Roles
    public PersonnelRole getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(PersonnelRole role) {
        this.primaryRole = role;
        //you can't be primary tech and a secondary astech
        //you can't be a primary astech and a secondary tech
        if ((getPrimaryRole().isTech() && getSecondaryRole().isAstech())
                || (getSecondaryRole().isTechSecondary() && getPrimaryRole().isAstech())
                || (getPrimaryRole().isDoctor() && getSecondaryRole().isMedic())
                || (getSecondaryRole().isDoctor() && getPrimaryRole().isMedic())) {
            secondaryRole = PersonnelRole.NONE;
        }
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public PersonnelRole getSecondaryRole() {
        return secondaryRole;
    }

    public void setSecondaryRole(PersonnelRole role) {
        this.secondaryRole = role;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    /**
     * This is used to determine if a person has a specific role as either their primary OR their
     * secondary role
     * @param role the role to determine
     * @return true if the person has the specific role either as their primary or secondary role
     */
    public boolean hasRole(PersonnelRole role) {
        return (getPrimaryRole() == role) || (getSecondaryRole() == role);
    }

    /**
     * @return true if the person has a primary or secondary combat role
     */
    public boolean hasCombatRole() {
        return getPrimaryRole().isCombat() || getSecondaryRole().isCombat();
    }

    /**
     * @return true if the person has a primary or secondary support role
     */
    public boolean hasSupportRole() {
        return !getPrimaryRole().isCombat() || (!getSecondaryRole().isCombat() && !getSecondaryRole().isNone());
    }

    public boolean isTech() {
        return getPrimaryRole().isTech() || getSecondaryRole().isTechSecondary();
    }

    public boolean isDoctor() {
        return getPrimaryRole().isDoctor() || getSecondaryRole().isDoctor();
    }

    public boolean isAdministrator() {
        return getPrimaryRole().isAdministrator() || getSecondaryRole().isAdministrator();
    }
    //endregion Personnel Roles

    public PersonnelStatus getStatus() {
        return status;
    }

    /**
     * This is used to change the person's PersonnelStatus
     * @param campaign the campaign the person is part of
     * @param status the person's new PersonnelStatus
     */
    public void changeStatus(Campaign campaign, PersonnelStatus status) {
        if (status == getStatus()) { // no change means we don't need to process anything
            return;
        } else if (getStatus().isKIA()) {
            // remove date of death for resurrection
            setDateOfDeath(null);
        }

        switch (status) {
            case ACTIVE:
                if (getStatus().isMIA()) {
                    ServiceLogger.recoveredMia(this, campaign.getLocalDate());
                } else if (getStatus().isDead()) {
                    ServiceLogger.resurrected(this, campaign.getLocalDate());
                } else {
                    ServiceLogger.rehired(this, campaign.getLocalDate());
                }
                setRetirement(null);
                break;
            case RETIRED:
                ServiceLogger.retired(this, campaign.getLocalDate());
                if (campaign.getCampaignOptions().useRetirementDateTracking()) {
                    setRetirement(campaign.getLocalDate());
                }
                break;
            case MIA:
                ServiceLogger.mia(this, campaign.getLocalDate());
                break;
            case KIA:
                ServiceLogger.kia(this, campaign.getLocalDate());
                break;
            case NATURAL_CAUSES:
                MedicalLogger.diedOfNaturalCauses(this, campaign.getLocalDate());
                ServiceLogger.passedAway(this, campaign.getLocalDate(), status.toString());
                break;
            case WOUNDS:
                MedicalLogger.diedFromWounds(this, campaign.getLocalDate());
                ServiceLogger.passedAway(this, campaign.getLocalDate(), status.toString());
                break;
            case DISEASE:
                MedicalLogger.diedFromDisease(this, campaign.getLocalDate());
                ServiceLogger.passedAway(this, campaign.getLocalDate(), status.toString());
                break;
            case OLD_AGE:
                MedicalLogger.diedOfOldAge(this, campaign.getLocalDate());
                ServiceLogger.passedAway(this, campaign.getLocalDate(), status.toString());
                break;
        }

        setStatus(status);

        if (status.isDead()) {
            setDateOfDeath(campaign.getLocalDate());
            // Don't forget to tell the spouse
            if (getGenealogy().hasSpouse() && !getGenealogy().getSpouse(campaign).getStatus().isDeadOrMIA()) {
                Divorce divorceType = campaign.getCampaignOptions().getKeepMarriedNameUponSpouseDeath()
                        ? Divorce.ORIGIN_CHANGE_SURNAME : Divorce.SPOUSE_CHANGE_SURNAME;
                divorceType.divorce(this, campaign);
            }
        }

        if (!status.isActive()) {
            setDoctorId(null, campaign.getCampaignOptions().getNaturalHealingWaitingPeriod());
            // If we're assigned to a unit, remove us from it
            Unit unit = campaign.getUnit(getUnitId());
            if (unit != null) {
                unit.remove(this, true);
            }

            // If we're assigned as a tech for any unit, remove us from it/them
            for (UUID techUnitId : new ArrayList<>(getTechUnitIDs())) {
                unit = campaign.getUnit(techUnitId);
                unit.remove(this, true);
            }
            // If we're assigned to any repairs or refits, remove that assignment
            for (Part part : campaign.getParts()) {
                if (this == part.getTech()) {
                    part.cancelAssignment();
                }
            }
        }

        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    /**
     * This is used to directly set the Person's PersonnelStatus without any processing
     * @param status the person's new status
     */
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

    public String getRoleDesc() {
        String role = getPrimaryRoleDesc();
        if (getSecondaryRole().isNone()) {
            role += "/" + getSecondaryRole().getName(isClanner());
        }
        return role;
    }

    public String getPrimaryRoleDesc() {
        String bgPrefix = "";
        if (isClanner()) {
            bgPrefix = getPhenotype().getShortName() + " ";
        }
        return bgPrefix + getPrimaryRole().getName(isClanner());
    }

    public boolean canPerformRole(PersonnelRole role) {
        switch (role) {
            case MECHWARRIOR:
                return hasSkill(SkillType.S_GUN_MECH) && hasSkill(SkillType.S_PILOT_MECH);
            case LAM_PILOT:
                return hasSkill(SkillType.S_GUN_MECH) && hasSkill(SkillType.S_PILOT_MECH)
                        && hasSkill(SkillType.S_GUN_AERO) && hasSkill(SkillType.S_PILOT_AERO);
            case GROUND_VEHICLE_DRIVER:
                return hasSkill(SkillType.S_PILOT_GVEE);
            case NAVAL_VEHICLE_DRIVER:
                return hasSkill(SkillType.S_PILOT_NVEE);
            case VTOL_PILOT:
                return hasSkill(SkillType.S_PILOT_VTOL);
            case VEHICLE_GUNNER:
                return hasSkill(SkillType.S_GUN_VEE);
            case AEROSPACE_PILOT:
                return hasSkill(SkillType.S_GUN_AERO) && hasSkill(SkillType.S_PILOT_AERO);
            case CONVENTIONAL_AIRCRAFT_PILOT:
                return hasSkill(SkillType.S_GUN_JET) && hasSkill(SkillType.S_PILOT_JET);
            case PROTOMECH_PILOT:
                return hasSkill(SkillType.S_GUN_PROTO);
            case BATTLE_ARMOUR:
                return hasSkill(SkillType.S_GUN_BA);
            case SOLDIER:
                return hasSkill(SkillType.S_SMALL_ARMS);
            case VESSEL_PILOT:
                return hasSkill(SkillType.S_PILOT_SPACE);
            case VESSEL_CREW:
                return hasSkill(SkillType.S_TECH_VESSEL);
            case VESSEL_GUNNER:
                return hasSkill(SkillType.S_GUN_SPACE);
            case VESSEL_NAVIGATOR:
                return hasSkill(SkillType.S_NAV);
            case MECH_TECH:
                return hasSkill(SkillType.S_TECH_MECH) && getSkill(SkillType.S_TECH_MECH).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case MECHANIC:
            case VEHICLE_CREW:
                return hasSkill(SkillType.S_TECH_MECHANIC) && getSkill(SkillType.S_TECH_MECHANIC).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case AERO_TECH:
                return hasSkill(SkillType.S_TECH_AERO) && getSkill(SkillType.S_TECH_AERO).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case BA_TECH:
                return hasSkill(SkillType.S_TECH_BA) && getSkill(SkillType.S_TECH_BA).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case ASTECH:
                return hasSkill(SkillType.S_ASTECH);
            case DOCTOR:
                return hasSkill(SkillType.S_DOCTOR) && getSkill(SkillType.S_DOCTOR).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN;
            case MEDIC:
                return hasSkill(SkillType.S_MEDTECH);
            case ADMINISTRATOR_COMMAND:
            case ADMINISTRATOR_LOGISTICS:
            case ADMINISTRATOR_TRANSPORT:
            case ADMINISTRATOR_HR:
                return hasSkill(SkillType.S_ADMIN);
            case DEPENDENT:
            case NONE:
                return true;
            default:
                return false;
        }
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getGender() {
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

    public String getDeathDateAsString() {
        if (getDateOfDeath() == null) {
            return "";
        } else {
            return MekHQ.getMekHQOptions().getDisplayFormattedDate(getDateOfDeath());
        }
    }

    public void setDateOfDeath(LocalDate date) {
        this.dateOfDeath = date;
    }

    public int getAge(LocalDate today) {
        // Get age based on year
        if (getDateOfDeath() != null) {
            //use date of death instead of birthday
            today = getDateOfDeath();
        }

        return Math.toIntExact(ChronoUnit.YEARS.between(getBirthday(), today));
    }

    public void setRecruitment(LocalDate date) {
        this.recruitment = date;
    }

    public LocalDate getRecruitment() {
        return recruitment;
    }

    public String getRecruitmentAsString() {
        if (getRecruitment() == null) {
            return "";
        } else {
            return MekHQ.getMekHQOptions().getDisplayFormattedDate(getRecruitment());
        }
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

    public void setLastRankChangeDate(LocalDate date) {
        this.lastRankChangeDate = date;
    }

    public LocalDate getLastRankChangeDate() {
        return lastRankChangeDate;
    }

    public String getLastRankChangeDateAsString() {
        if (getLastRankChangeDate() == null) {
            return "";
        } else {
            return MekHQ.getMekHQOptions().getDisplayFormattedDate(getLastRankChangeDate());
        }
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

    public void setRetirement(LocalDate date) {
        this.retirement = date;
    }

    public LocalDate getRetirement() {
        return retirement;
    }

    public String getRetirementAsString() {
        if (getRetirement() == null) {
            return "";
        } else {
            return MekHQ.getMekHQOptions().getDisplayFormattedDate(getRetirement());
        }
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

    public Genealogy getGenealogy() {
        return genealogy;
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
     * @param campaign the campaign the person was in
     * @return true if they can, otherwise false
     */
    public boolean canProcreate(Campaign campaign) {
        return getGender().isFemale() && isTryingToConceive() && !isPregnant() && !isDeployed()
                && !isChild() && (getAge(campaign.getLocalDate()) < 51);
    }

    public void procreate(Campaign campaign) {
        if (canProcreate(campaign)) {
            boolean conceived = false;
            if (getGenealogy().hasSpouse()) {
                Person spouse = getGenealogy().getSpouse(campaign);
                if (!spouse.isDeployed() && !spouse.getStatus().isDeadOrMIA() && !spouse.isChild()
                        && !(spouse.getGender() == getGender())) {
                    // setting is the decimal chance that this procreation attempt will create a child, base is 0.05%
                    conceived = (Compute.randomFloat() < (campaign.getCampaignOptions().getChanceProcreation()));
                }
            } else if (campaign.getCampaignOptions().useUnofficialProcreationNoRelationship()) {
                // setting is the decimal chance that this procreation attempt will create a child, base is 0.005%
                conceived = (Compute.randomFloat() < (campaign.getCampaignOptions().getChanceProcreationNoRelationship()));
            }

            if (conceived) {
                addPregnancy(campaign);
            }
        }
    }

    public void addPregnancy(Campaign campaign) {
        LocalDate dueDate = campaign.getLocalDate();
        dueDate = dueDate.plus(PREGNANCY_STANDARD_DURATION, ChronoUnit.DAYS);
        setExpectedDueDate(dueDate);
        dueDate = dueDate.plus(PREGNANCY_MODIFY_DURATION.getAsInt(), ChronoUnit.DAYS);
        setDueDate(dueDate);

        int size = PREGNANCY_SIZE.getAsInt();
        extraData.set(PREGNANCY_CHILDREN_DATA, size);
        extraData.set(PREGNANCY_FATHER_DATA, (getGenealogy().hasSpouse())
                ? getGenealogy().getSpouseId().toString() : null);

        String sizeString = (size < PREGNANCY_MULTIPLE_NAMES.length) ? PREGNANCY_MULTIPLE_NAMES[size] : null;

        campaign.addReport(getHyperlinkedName() + " has conceived" + (sizeString == null ? "" : (" " + sizeString)));
        if (campaign.getCampaignOptions().logConception()) {
            MedicalLogger.hasConceived(this, campaign.getLocalDate(), sizeString);
            if (getGenealogy().hasSpouse()) {
                PersonalLogger.spouseConceived(getGenealogy().getSpouse(campaign),
                        getFullName(), getCampaign().getLocalDate(), sizeString);
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

    /**
     * This method is how a person gives birth to a number of babies and have them added to the campaign
     * @param campaign the campaign to add the baby in question to
     */
    public void birth(Campaign campaign) {
        // Determine the number of children
        int size = extraData.get(PREGNANCY_CHILDREN_DATA, 1);

        // Determine father information
        String fatherIdString = getExtraData().get(PREGNANCY_FATHER_DATA);
        UUID fatherId = (fatherIdString != null) ? UUID.fromString(fatherIdString) : null;
        fatherId = campaign.getCampaignOptions().determineFatherAtBirth()
                ? Utilities.nonNull(getGenealogy().getSpouseId(), fatherId) : fatherId;

        // Determine Prisoner Status
        PrisonerStatus prisonerStatus = campaign.getCampaignOptions().getPrisonerBabyStatus()
                ? PrisonerStatus.FREE : getPrisonerStatus();

        // Output a specific report to the campaign if they are giving birth to multiple children
        if (PREGNANCY_MULTIPLE_NAMES[size] != null) {
            campaign.addReport(String.format("%s has given birth to %s!", getHyperlinkedName(),
                    PREGNANCY_MULTIPLE_NAMES[size]));
        }

        // Create Babies
        for (int i = 0; i < size; i++) {
            // Create the specific baby
            Person baby = campaign.newDependent(true);
            String surname = campaign.getCampaignOptions().getBabySurnameStyle()
                    .generateBabySurname(this, campaign.getPerson(fatherId), baby.getGender());
            baby.setSurname(surname);
            baby.setBirthday(campaign.getLocalDate());

            // Recruit the baby
            campaign.recruitPerson(baby, prisonerStatus, baby.isDependent(), true, true);

            // Create genealogy information
            baby.getGenealogy().addFamilyMember(FamilialRelationshipType.PARENT, getId());
            getGenealogy().addFamilyMember(FamilialRelationshipType.CHILD, baby.getId());
            if (fatherId != null) {
                baby.getGenealogy().addFamilyMember(FamilialRelationshipType.PARENT, fatherId);
                campaign.getPerson(fatherId).getGenealogy()
                        .addFamilyMember(FamilialRelationshipType.CHILD, baby.getId());
            }

            // Create reports and log the birth
            campaign.addReport(String.format("%s has given birth to %s, a baby %s!", getHyperlinkedName(),
                    baby.getHyperlinkedName(), GenderDescriptors.BOY_GIRL.getDescriptor(baby.getGender())));
            if (campaign.getCampaignOptions().logConception()) {
                MedicalLogger.deliveredBaby(this, baby, campaign.getLocalDate());
                if (fatherId != null) {
                    PersonalLogger.ourChildBorn(campaign.getPerson(fatherId), baby, getFullName(),
                            campaign.getLocalDate());
                }
            }
        }

        // Cleanup Data
        removePregnancy();
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
     * @param person the person to determine if they are a safe spouse
     * @param campaign the campaign to use to determine if they are a safe spouse
     */
    public boolean safeSpouse(Person person, Campaign campaign) {
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
                !this.equals(person)
                && !person.getGenealogy().hasSpouse()
                && person.isTryingToMarry()
                && person.oldEnoughToMarry(campaign)
                && (!person.getPrisonerStatus().isPrisoner() || getPrisonerStatus().isPrisoner())
                && !person.getStatus().isDeadOrMIA()
                && person.getStatus().isActive()
                && !getGenealogy().checkMutualAncestors(person, getCampaign())
        );
    }

    public boolean oldEnoughToMarry(Campaign campaign) {
        return (getAge(campaign.getLocalDate()) >= campaign.getCampaignOptions().getMinimumMarriageAge());
    }

    public void randomMarriage(Campaign campaign) {
        // Don't attempt to generate is someone isn't trying to marry, has a spouse,
        // isn't old enough to marry, or is actively deployed
        if (!isTryingToMarry() || getGenealogy().hasSpouse() || !oldEnoughToMarry(campaign) || isDeployed()) {
            return;
        }

        // setting is the fractional chance that this attempt at finding a marriage will result in one
        if (Compute.randomFloat() < (campaign.getCampaignOptions().getChanceRandomMarriages())) {
            addRandomSpouse(false, campaign);
        } else if (campaign.getCampaignOptions().useRandomSameSexMarriages()) {
            if (Compute.randomFloat() < (campaign.getCampaignOptions().getChanceRandomSameSexMarriages())) {
                addRandomSpouse(true, campaign);
            }
        }
    }

    public void addRandomSpouse(boolean sameSex, Campaign campaign) {
        List<Person> potentials = new ArrayList<>();
        Gender gender = sameSex ? getGender() : (getGender().isMale() ? Gender.FEMALE : Gender.MALE);
        for (Person p : campaign.getActivePersonnel()) {
            if (isPotentialRandomSpouse(p, gender, campaign)) {
                potentials.add(p);
            }
        }

        int n = potentials.size();
        if (n > 0) {
            Marriage.WEIGHTED.marry(this, potentials.get(Compute.randomInt(n)), campaign);
        }
    }

    public boolean isPotentialRandomSpouse(Person p, Gender gender, Campaign campaign) {
        if ((p.getGender() != gender) || !safeSpouse(p, campaign)
                || !(getPrisonerStatus().isFree()
                || (getPrisonerStatus().isPrisoner() && p.getPrisonerStatus().isPrisoner()))) {
            return false;
        }

        int ageDifference = Math.abs(p.getAge(campaign.getLocalDate()) - getAge(campaign.getLocalDate()));

        return (ageDifference <= campaign.getCampaignOptions().getMarriageAgeRange());
    }
    //endregion Marriage

    //region Experience
    public int getXP() {
        return xp;
    }

    public void setXP(int xp) {
        this.xp = xp;
    }

    public void awardXP(int xp) {
        this.xp += xp;
    }
    //endregion Experience

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

    public ExtraData getExtraData() {
        return extraData;
    }

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
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "primaryRole", primaryRole.name());
            if (secondaryRole.isNone()) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "secondaryRole", secondaryRole.name());
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
            if (phenotype != Phenotype.NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "phenotype", phenotype.name());
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
            if (!genealogy.isEmpty()) {
                genealogy.writeToXml(pw1, indent + 1);
            }
            if (!isTryingToMarry()) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "tryingToMarry", false);
            }
            if (!isTryingToConceive()) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "tryingToConceive", false);
            }
            if (dueDate != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "dueDate",
                        MekHqXmlUtil.saveFormattedDate(dueDate));
            }
            if (expectedDueDate != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "expectedDueDate",
                        MekHqXmlUtil.saveFormattedDate(expectedDueDate));
            }
            if (!AbstractIcon.ROOT_CATEGORY.equals(portraitCategory)) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "portraitCategory", portraitCategory);
            }
            if (!AbstractIcon.DEFAULT_ICON_FILENAME.equals(portraitFile)) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "portraitFile", portraitFile);
            }
            // Always save the current XP
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "xp", xp);
            if (daysToWaitForHealing != 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "daysToWaitForHealing", daysToWaitForHealing);
            }
            // Always save the person's gender, as it would otherwise get confusing fast
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "gender", gender.name());
            // Always save a person's rank
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "rank", rank);
            if (rankLevel != 0) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "rankLevel", rankLevel);
            }
            if (rankSystem != -1) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "rankSystem", rankSystem);
            }
            if (maneiDominiRank != ManeiDominiRank.NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maneiDominiRank", maneiDominiRank.name());
            }
            if (maneiDominiClass != ManeiDominiClass.NONE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maneiDominiClass", maneiDominiClass.name());
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
            if (prisonerStatus != PrisonerStatus.FREE) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "prisonerStatus", prisonerStatus.name());
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
            if (getRetirement() != null) {
                MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "retirement",
                        MekHqXmlUtil.saveFormattedDate(getRetirement()));
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
                if (hasSupportRole() || isEngineer()) {
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
            if (!getAwardController().getAwards().isEmpty()) {
                MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent + 1, "awards");
                for (Award award : getAwardController().getAwards()) {
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
            MekHQ.getLogger().error("Failed to write " + getFullName() + " to the XML File", e);
            throw e; // we want to rethrow to ensure that that the save fails
        }
        pw1.println(MekHqXmlUtil.indentStr(indent) + "</person>");
    }

    public static Person generateInstanceFromXML(Node wn, Campaign c, Version version) {
        Person retVal = new Person(c);

        try {
            // Okay, now load Person-specific fields!
            NodeList nl = wn.getChildNodes();

            String advantages = null;
            String edge = null;
            String implants = null;

            //backwards compatibility
            String pilotName = null;
            String pilotNickname = null;
            int pilotGunnery = -1;
            int pilotPiloting = -1;
            int pilotCommandBonus = -1;
            int type = 0;

            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);

                if (wn2.getNodeName().equalsIgnoreCase("name")) { // legacy - 0.47.5 removal
                    retVal.migrateName(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("givenName")) {
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
                    retVal.originPlanet = c.getSystemById(systemId).getPlanetById(planetId);
                } else if (wn2.getNodeName().equalsIgnoreCase("clan")) {
                    retVal.clan = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("phenotype")) {
                    retVal.phenotype = Phenotype.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("bloodname")) {
                    retVal.bloodname = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("biography")) {
                    retVal.biography = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("primaryRole")) {
                    retVal.primaryRole = PersonnelRole.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("secondaryRole")) {
                    retVal.secondaryRole = PersonnelRole.parseFromString(wn2.getTextContent().trim());
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
                    if (version.getMajorVersion() == 0 && version.getMinorVersion() < 2 && version.getSnapshot() < 14) {
                        retVal.oldId = Integer.parseInt(wn2.getTextContent());
                    } else {
                        retVal.id = UUID.fromString(wn2.getTextContent());
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("ancestors")) { // legacy - 0.47.6 removal
                    CampaignXmlParser.addToAncestryMigrationMap(UUID.fromString(wn2.getTextContent().trim()), retVal);
                } else if (wn2.getNodeName().equalsIgnoreCase("spouse")) { // legacy - 0.47.6 removal
                    retVal.genealogy.setSpouse(UUID.fromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("formerSpouses")) { // legacy - 0.47.6 removal
                    Genealogy.loadFormerSpouses(retVal.genealogy, wn2.getChildNodes());
                } else if (wn2.getNodeName().equalsIgnoreCase("genealogy")) {
                    retVal.genealogy = Genealogy.generateInstanceFromXML(wn2.getChildNodes());
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
                    retVal.gender = Gender.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("rank")) {
                    if (version.isLowerThan("0.3.4-r1782")) {
                        RankTranslator rt = new RankTranslator(c);
                        try {
                            retVal.rank = rt.getNewRank(c.getRanks().getOldRankSystem(),
                                    Integer.parseInt(wn2.getTextContent()));
                        } catch (ArrayIndexOutOfBoundsException e) {
                            // Do nothing
                        }
                    } else {
                        retVal.rank = Integer.parseInt(wn2.getTextContent());
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("rankLevel")) {
                    retVal.rankLevel = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("rankSystem")) {
                    retVal.setRankSystem(Integer.parseInt(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("maneiDominiRank")) {
                    retVal.maneiDominiRank = ManeiDominiRank.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maneiDominiClass")) {
                    retVal.maneiDominiClass = ManeiDominiClass.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("doctorId")) {
                    if (!wn2.getTextContent().equals("null")) {
                        retVal.doctorId = UUID.fromString(wn2.getTextContent());
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("unitId")) {
                    if (!wn2.getTextContent().equals("null")) {
                        retVal.unitId = UUID.fromString(wn2.getTextContent());
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("status")) {
                    retVal.setStatus(PersonnelStatus.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("prisonerStatus")) {
                    retVal.prisonerStatus = PrisonerStatus.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("willingToDefect")) { // Legacy
                    if (Boolean.parseBoolean(wn2.getTextContent().trim())) {
                        retVal.prisonerStatus = PrisonerStatus.PRISONER_DEFECTOR;
                    }
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
                } else if (wn2.getNodeName().equalsIgnoreCase("retirement")) {
                    retVal.setRetirement(MekHqXmlUtil.parseDate(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("advantages")) {
                    advantages = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("edge")) {
                    edge = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("edgeAvailable")) {
                    retVal.currentEdge = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("implants")) {
                    implants = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("toughness")) {
                    retVal.toughness = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("pilotGunnery")) {
                    pilotGunnery = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("pilotPiloting")) {
                    pilotPiloting = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("pilotHits")) {
                    retVal.hits = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("pilotCommandBonus")) {
                    pilotCommandBonus = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("pilotName")) {
                    pilotName = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("pilotNickname")) {
                    pilotNickname = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("type")) {
                    type = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("skill")) {
                    Skill s = Skill.generateInstanceFromXML(wn2);
                    if ((s != null) && (s.getType() != null)) {
                        retVal.skills.addSkill(s.getType().getName(), s);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("techUnitIds")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (!wn3.getNodeName().equalsIgnoreCase("id")) {
                            MekHQ.getLogger().error("Unknown node type not loaded in techUnitIds nodes: " + wn3.getNodeName());
                            continue;
                        }
                        retVal.addTechUnitID(UUID.fromString(wn3.getTextContent()));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelLog")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (!wn3.getNodeName().equalsIgnoreCase("logEntry")) {
                            MekHQ.getLogger().error("Unknown node type not loaded in personnel log nodes: " + wn3.getNodeName());
                            continue;
                        }

                        LogEntry entry = LogEntryFactory.getInstance().generateInstanceFromXML(wn3);

                        // If the version of this campaign is earlier than 0.45.4,
                        // we didn't have the mission log separated from the personnel log,
                        // so we need to separate the log entries manually
                        if (version.isLowerThan("0.45.4")) {
                            if (entry.getDesc().startsWith(getMissionParticipatedString())) {
                                retVal.addMissionLogEntry(entry);
                            } else {
                                retVal.addLogEntry(entry);
                            }
                        } else {
                            retVal.addLogEntry(entry);
                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("missionLog")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (!wn3.getNodeName().equalsIgnoreCase("logEntry")) {
                            MekHQ.getLogger().error("Unknown node type not loaded in mission log nodes: " + wn3.getNodeName());
                            continue;
                        }
                        retVal.addMissionLogEntry(LogEntryFactory.getInstance().generateInstanceFromXML(wn3));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("awards")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {

                        Node wn3 = nl2.item(y);

                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (!wn3.getNodeName().equalsIgnoreCase("award")) {
                            MekHQ.getLogger().error("Unknown node type not loaded in personnel log nodes: " + wn3.getNodeName());
                            continue;
                        }

                        retVal.awardController.addAwardFromXml(AwardsFactory.getInstance().generateNewFromXML(wn3));
                    }

                } else if (wn2.getNodeName().equalsIgnoreCase("injuries")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (!wn3.getNodeName().equalsIgnoreCase("injury")) {
                            MekHQ.getLogger().error("Unknown node type not loaded in injury nodes: " + wn3.getNodeName());
                            continue;
                        }
                        retVal.injuries.add(Injury.generateInstanceFromXML(wn3));
                    }
                    LocalDate now = c.getLocalDate();
                    retVal.injuries.stream().filter(inj -> (null == inj.getStart()))
                        .forEach(inj -> inj.setStart(now.minusDays(inj.getOriginalTime() - inj.getTime())));
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

            if (version.isLowerThan("0.47.5") && (retVal.getExpectedDueDate() == null)
                    && (retVal.getDueDate() != null)) {
                retVal.setExpectedDueDate(retVal.getDueDate());
            }

            //versions before 0.3.4 did not have proper clan phenotypes
            if (version.isLowerThan("0.3.4") && c.getFaction().isClan()) {
                //assume personnel are clan and trueborn if the right role
                retVal.setClanner(true);
                switch (retVal.getPrimaryRole()) {
                    case MECHWARRIOR:
                        retVal.setPhenotype(Phenotype.MECHWARRIOR);
                        break;
                    case AEROSPACE_PILOT:
                    case CONVENTIONAL_AIRCRAFT_PILOT:
                        retVal.setPhenotype(Phenotype.AEROSPACE);
                        break;
                    case BATTLE_ARMOUR:
                        retVal.setPhenotype(Phenotype.ELEMENTAL);
                        break;
                    case VEHICLE_GUNNER:
                    case GROUND_VEHICLE_DRIVER:
                    case NAVAL_VEHICLE_DRIVER:
                    case VTOL_PILOT:
                        retVal.setPhenotype(Phenotype.VEHICLE);
                        break;
                    case PROTOMECH_PILOT:
                        retVal.setPhenotype(Phenotype.PROTOMECH);
                        break;
                    default:
                        retVal.setPhenotype(Phenotype.NONE);
                        break;
                }
            }

            if ((null != advantages) && (advantages.trim().length() > 0)) {
                StringTokenizer st = new StringTokenizer(advantages, "::");
                while (st.hasMoreTokens()) {
                    String adv = st.nextToken();
                    String advName = Crew.parseAdvantageName(adv);
                    Object value = Crew.parseAdvantageValue(adv);

                    try {
                        retVal.getOptions().getOption(advName).setValue(value);
                    } catch (Exception e) {
                        MekHQ.getLogger().error("Error restoring advantage: " + adv);
                    }
                }
            }
            if ((null != edge) && (edge.trim().length() > 0)) {
                StringTokenizer st = new StringTokenizer(edge, "::");
                while (st.hasMoreTokens()) {
                    String adv = st.nextToken();
                    String advName = Crew.parseAdvantageName(adv);
                    Object value = Crew.parseAdvantageValue(adv);

                    try {
                        retVal.getOptions().getOption(advName).setValue(value);
                    } catch (Exception e) {
                        MekHQ.getLogger().error("Error restoring edge: " + adv);
                    }
                }
            }
            if ((null != implants) && (implants.trim().length() > 0)) {
                StringTokenizer st = new StringTokenizer(implants, "::");
                while (st.hasMoreTokens()) {
                    String adv = st.nextToken();
                    String advName = Crew.parseAdvantageName(adv);
                    Object value = Crew.parseAdvantageValue(adv);

                    try {
                        retVal.getOptions().getOption(advName).setValue(value);
                    } catch (Exception e) {
                        MekHQ.getLogger().error("Error restoring implants: " + adv);
                    }
                }
            }
            //check to see if we are dealing with a PilotPerson from 0.1.8 or earlier
            if (pilotGunnery != -1) {
                switch (type) {
                    case 0:
                        retVal.addSkill(SkillType.S_GUN_MECH, 7 - pilotGunnery, 0);
                        retVal.addSkill(SkillType.S_PILOT_MECH, 8 - pilotPiloting, 0);
                        retVal.primaryRole = PersonnelRole.MECHWARRIOR;
                        break;
                    case 1:
                        retVal.addSkill(SkillType.S_GUN_VEE, 7 - pilotGunnery, 0);
                        retVal.addSkill(SkillType.S_PILOT_GVEE, 8 - pilotPiloting, 0);
                        retVal.primaryRole = PersonnelRole.GROUND_VEHICLE_DRIVER;
                        break;
                    case 2:
                        retVal.addSkill(SkillType.S_GUN_AERO, 7 - pilotGunnery, 0);
                        retVal.addSkill(SkillType.S_PILOT_AERO, 8 - pilotPiloting, 0);
                        retVal.primaryRole = PersonnelRole.AEROSPACE_PILOT;
                        break;
                    case 4:
                        retVal.addSkill(SkillType.S_GUN_BA, 7 - pilotGunnery, 0);
                        retVal.addSkill(SkillType.S_ANTI_MECH, 8 - pilotPiloting, 0);
                        retVal.primaryRole = PersonnelRole.BATTLE_ARMOUR;
                        break;
                    default:
                        break;
                }
                retVal.addSkill(SkillType.S_TACTICS, pilotCommandBonus, 0);
            }
            if (pilotName != null) {
                retVal.migrateName(pilotName);
            }
            if (null != pilotNickname) {
                retVal.setCallsign(pilotNickname);
            }

            // Ensure the Genealogy Origin Id is set to the proper id
            retVal.getGenealogy().setOrigin(retVal.getId());

            // Prisoner and Bondsman updating
            if (retVal.rank < 0) {
                retVal.setRankNumeric(0);
            }
        } catch (Exception e) {
            MekHQ.getLogger().error("Failed to read person " + retVal.getFullName() + " from file", e);
            retVal = null;
        }

        return retVal;
    }

    public void setSalary(Money s) {
        salary = s;
    }

    public Money getSalary() {
        if (!getPrisonerStatus().isFree() || isDependent()) {
            return Money.zero();
        }

        if (salary.isPositiveOrZero()) {
            return salary;
        }

        //if salary is negative, then use the standard amounts
        Money primaryBase = campaign.getCampaignOptions().getBaseSalaryMoney(getPrimaryRole());
        primaryBase = primaryBase.multipliedBy(campaign.getCampaignOptions().getSalaryXpMultiplier(getExperienceLevel(false)));
        if (hasSkill(SkillType.S_ANTI_MECH) && (getPrimaryRole().isSoldier() || getPrimaryRole().isBattleArmour())) {
            primaryBase = primaryBase.multipliedBy(campaign.getCampaignOptions().getSalaryAntiMekMultiplier());
        }

        Money secondaryBase = campaign.getCampaignOptions().getBaseSalaryMoney(getSecondaryRole()).dividedBy(2);
        secondaryBase = secondaryBase.multipliedBy(campaign.getCampaignOptions().getSalaryXpMultiplier(getExperienceLevel(true)));
        if (hasSkill(SkillType.S_ANTI_MECH) && (getSecondaryRole().isSoldier() || getSecondaryRole().isBattleArmour())) {
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
        if (getStatus().isActive()) {
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
        while ((profession != Ranks.RPROF_MW) && getRanks().isEmptyProfession(profession)) {
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
        while (getRanks().isEmptyProfession(profession) && (redirects < Ranks.RPROF_NUM)) {
            profession = campaign.getRanks().getAlternateProfession(profession);
            redirects++;
        }

        // If we're set to a rank that no longer exists, demote ourself
        while (getRank().getName(profession).equals("-") && (rank > 0)) {
            setRankNumeric(--rank);
        }

        redirects = 0;
        // re-route through any profession redirections
        while (getRank().getName(profession).startsWith("--") && (profession != Ranks.RPROF_MW)
                && (redirects < Ranks.RPROF_NUM)) {
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
        if (getRankSystem() == Ranks.RS_WOB) {
            if (maneiDominiClass != ManeiDominiClass.NONE) {
                rankName = maneiDominiClass.toString() + " " + rankName;
            }
            if (maneiDominiRank != ManeiDominiRank.NONE) {
                rankName += " " + maneiDominiRank.toString();
            }
        } else {
            maneiDominiClass = ManeiDominiClass.NONE;
            maneiDominiRank = ManeiDominiRank.NONE;
        }

        if ((getRankSystem() == Ranks.RS_COM) || (getRankSystem() == Ranks.RS_WOB)) {
            rankName += ROMDesignation.getComStarBranchDesignation(this, campaign);
        }

        // If we have a rankLevel, add it
        if (rankLevel > 0) {
            if (getRank().getRankLevels(profession) > 0)
                rankName += Utilities.getRomanNumeralsFromArabicNumber(rankLevel, true);
            else // Oops! Our rankLevel didn't get correctly cleared, they's remedy that.
                rankLevel = 0;
        }

        if (rankName.equalsIgnoreCase("None")) {
            if (!getPrisonerStatus().getTitleExtension().equals("")) {
                rankName = getPrisonerStatus().getTitleExtension();
            }
        } else {
            rankName = getPrisonerStatus().getTitleExtension() + rankName;
        }

        // We have our name, return it
        return rankName;
    }

    public ManeiDominiClass getManeiDominiClass() {
        return maneiDominiClass;
    }

    public void setManeiDominiClass(ManeiDominiClass maneiDominiClass) {
        this.maneiDominiClass = maneiDominiClass;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
    }

    public ManeiDominiRank getManeiDominiRank() {
        return maneiDominiRank;
    }

    public void setManeiDominiRank(ManeiDominiRank maneiDominiRank) {
        this.maneiDominiRank = maneiDominiRank;
        MekHQ.triggerEvent(new PersonChangedEvent(this));
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
        PersonnelRole role = secondary ? getSecondaryRole() : getPrimaryRole();
        switch (role) {
            case MECHWARRIOR:
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
            case GROUND_VEHICLE_DRIVER:
                return hasSkill(SkillType.S_PILOT_GVEE) ? getSkill(SkillType.S_PILOT_GVEE).getExperienceLevel() : -1;
            case NAVAL_VEHICLE_DRIVER:
                return hasSkill(SkillType.S_PILOT_NVEE) ? getSkill(SkillType.S_PILOT_NVEE).getExperienceLevel() : -1;
            case VTOL_PILOT:
                return hasSkill(SkillType.S_PILOT_VTOL) ? getSkill(SkillType.S_PILOT_VTOL).getExperienceLevel() : -1;
            case VEHICLE_GUNNER:
                return hasSkill(SkillType.S_GUN_VEE) ? getSkill(SkillType.S_GUN_VEE).getExperienceLevel() : -1;
            case AEROSPACE_PILOT:
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
            case CONVENTIONAL_AIRCRAFT_PILOT:
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
            case BATTLE_ARMOUR:
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
            case PROTOMECH_PILOT:
                return hasSkill(SkillType.S_GUN_PROTO) ? getSkill(SkillType.S_GUN_PROTO).getExperienceLevel() : -1;
            case SOLDIER:
                return hasSkill(SkillType.S_SMALL_ARMS) ? getSkill(SkillType.S_SMALL_ARMS).getExperienceLevel() : -1;
            case VESSEL_PILOT:
                return hasSkill(SkillType.S_PILOT_SPACE) ? getSkill(SkillType.S_PILOT_SPACE).getExperienceLevel() : -1;
            case VESSEL_CREW:
                return hasSkill(SkillType.S_TECH_VESSEL) ? getSkill(SkillType.S_TECH_VESSEL).getExperienceLevel() : -1;
            case VESSEL_GUNNER:
                return hasSkill(SkillType.S_GUN_SPACE) ? getSkill(SkillType.S_GUN_SPACE).getExperienceLevel() : -1;
            case VESSEL_NAVIGATOR:
                return hasSkill(SkillType.S_NAV) ? getSkill(SkillType.S_NAV).getExperienceLevel() : -1;
            case MECH_TECH:
                return hasSkill(SkillType.S_TECH_MECH) ? getSkill(SkillType.S_TECH_MECH).getExperienceLevel() : -1;
            case MECHANIC:
            case VEHICLE_CREW:
                return hasSkill(SkillType.S_TECH_MECHANIC) ? getSkill(SkillType.S_TECH_MECHANIC).getExperienceLevel() : -1;
            case AERO_TECH:
                return hasSkill(SkillType.S_TECH_AERO) ? getSkill(SkillType.S_TECH_AERO).getExperienceLevel() : -1;
            case BA_TECH:
                return hasSkill(SkillType.S_TECH_BA) ? getSkill(SkillType.S_TECH_BA).getExperienceLevel() : -1;
            case ASTECH:
                return hasSkill(SkillType.S_ASTECH) ? getSkill(SkillType.S_ASTECH).getExperienceLevel() : -1;
            case DOCTOR:
                return hasSkill(SkillType.S_DOCTOR) ? getSkill(SkillType.S_DOCTOR).getExperienceLevel() : -1;
            case MEDIC:
                return hasSkill(SkillType.S_MEDTECH) ? getSkill(SkillType.S_MEDTECH).getExperienceLevel() : -1;
            case ADMINISTRATOR_COMMAND:
            case ADMINISTRATOR_LOGISTICS:
            case ADMINISTRATOR_TRANSPORT:
            case ADMINISTRATOR_HR:
                return hasSkill(SkillType.S_ADMIN) ? getSkill(SkillType.S_ADMIN).getExperienceLevel() : -1;
            default:
                return -1;
        }
    }

    /**
     * returns a full description in HTML format that will be used for the graphical display in the
     * personnel table among other places
     * @return String
     */
    public String getFullDesc() {
        return "<b>" + getFullTitle() + "</b><br/>" + getSkillSummary() + " " + getRoleDesc();
    }

    public String getFullTitle() {
        String rank = getRankName();

        if (rank.equalsIgnoreCase("None")) {
            rank = "";
        } else {
            rank = rank.trim() + " ";
        }

        return rank + getFullName();
    }

    public String makeHTMLRank() {
        return String.format("<html>%s</html>", makeHTMLRankDiv());
    }

    public String makeHTMLRankDiv() {
        return String.format("<div id=\"%s\">%s</div>", getId().toString(), getRankName().trim());
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

    //region Awards
    public PersonAwardController getAwardController() {
        return awardController;
    }
    //endregion Awards

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
        return ((hits > 0) || needsAMFixing()) && getStatus().isActive();
    }

    public String succeed() {
        heal();
        return " <font color='green'><b>Successfully healed one hit.</b></font>";
    }

    //region Personnel Options
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

    /**
     * @return an html-coded list that says what abilities are enabled for this pilot
     */
    public String getAbilityListAsString(String type) {
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
    //endregion Personnel Options

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
        if (engineer && null != getUnitId()) {
            //set minutes for all crewmembers
            Unit u = campaign.getUnit(getUnitId());
            if (null != u) {
                for (Person p : u.getActiveCrew()) {
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
        if (engineer && null != getUnitId()) {
            //set minutes for all crewmembers
            Unit u = campaign.getUnit(getUnitId());
            if (null != u) {
                for (Person p : u.getActiveCrew()) {
                    p.setMinutesLeft(m);
                }
            }
        }
    }

    public void resetMinutesLeft() {
        if (getPrimaryRole().isTech() || getPrimaryRole().isDoctor()) {
            this.minutesLeft = PRIMARY_ROLE_SUPPORT_TIME;
            this.overtimeLeft = PRIMARY_ROLE_OVERTIME_SUPPORT_TIME;
        }

        if (getSecondaryRole().isTech() || getSecondaryRole().isDoctor()) {
            this.minutesLeft = SECONDARY_ROLE_SUPPORT_TIME;
            this.overtimeLeft = SECONDARY_ROLE_OVERTIME_SUPPORT_TIME;
        }
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
     * All methods below are for the Advanced Medical option
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

    public static int getProfessionFromPrimaryRole(PersonnelRole role) {
        switch (role) {
            case AEROSPACE_PILOT:
            case CONVENTIONAL_AIRCRAFT_PILOT:
                return Ranks.RPROF_ASF;
            case GROUND_VEHICLE_DRIVER:
            case NAVAL_VEHICLE_DRIVER:
            case VTOL_PILOT:
            case VEHICLE_GUNNER:
            case VEHICLE_CREW:
                return Ranks.RPROF_VEE;
            case BATTLE_ARMOUR:
            case SOLDIER:
                return Ranks.RPROF_INF;
            case VESSEL_PILOT:
            case VESSEL_CREW:
            case VESSEL_GUNNER:
            case VESSEL_NAVIGATOR:
                return Ranks.RPROF_NAVAL;
            case MECH_TECH:
            case MECHANIC:
            case AERO_TECH:
            case BA_TECH:
            case ASTECH:
            case ADMINISTRATOR_COMMAND:
            case ADMINISTRATOR_LOGISTICS:
            case ADMINISTRATOR_HR:
            case ADMINISTRATOR_TRANSPORT:
                return Ranks.RPROF_TECH;
            case MECHWARRIOR:
            case LAM_PILOT:
            case PROTOMECH_PILOT:
            case DOCTOR:
            case MEDIC:
            case DEPENDENT:
            case NONE:
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
        if (!getStatus().isActive() || !getPrisonerStatus().isFree()
                || (!sharesForAll && !hasRole(PersonnelRole.MECHWARRIOR))) {
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
        return (getPrimaryRole().isMechWarrior() || getPrimaryRole().isAerospacePilot()
                ? MECHWARRIOR_AERO_RANSOM_VALUES : OTHER_RANSOM_VALUES).get(getExperienceLevel(false));
    }
}
