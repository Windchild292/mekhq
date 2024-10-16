package mekhq.gui.panes.campaignOptions;

import megamek.client.generator.RandomGenderGenerator;
import megamek.client.generator.RandomNameGenerator;
import megamek.client.ui.baseComponents.MMComboBox;
import megamek.common.annotations.Nullable;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.enums.AgeGroup;
import mekhq.campaign.personnel.enums.FamilialRelationshipDisplayLevel;
import mekhq.campaign.personnel.enums.PersonnelRole;
import mekhq.campaign.personnel.enums.RandomDeathMethod;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.Planet;
import mekhq.campaign.universe.PlanetarySystem;
import mekhq.gui.panes.RankSystemsPane;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static mekhq.gui.panes.campaignOptions.CampaignOptionsUtilities.*;

/**
 * The `BiographyTab` class represents the biography settings tab in campaign options.
 * It is responsible for maintaining and allowing updates to the settings related to biographies
 * in a campaign. This includes general settings, name and portrait settings, rank settings,
 * background settings, death settings, and education settings.
 * <p>
 * Each area of settings is initialized and managed in its own tab, which allows for organization
 * and easy navigation settings related to biographic information.
 * <p>
 * The class also has a set of fields that dictate the state of the individual settings.
 * <p>
 * Some of the key settings managed by this tab include:
 * - Random XP: a setting that controls the use of Dylan's random XP.
 * - Gender settings: a set of settings that controls the gender ratio and non-binary dice size.
 * - Name and portrait settings: a set of settings to control the use of origin factions for names,
 *   and the behavior of random portraits.
 * - Education settings: a set of settings to control the usage and specifics of the educational module.
 * <p>
 * And many more settings are controlled within this class.
 */
public class BiographyTab {
    Campaign campaign;
    JFrame frame;
    String name;

    //start General Tab
    private JCheckBox chkUseDylansRandomXP;
    private JLabel lblGender;
    private JSlider sldGender;
    private JLabel lblNonBinaryDiceSize;
    private JSpinner spnNonBinaryDiceSize;
    private JLabel lblFamilyDisplayLevel;
    private MMComboBox<FamilialRelationshipDisplayLevel> comboFamilyDisplayLevel;
    private JPanel pnlAnniversariesPanel;
    private JCheckBox chkAnnounceOfficersOnly;
    private JCheckBox chkAnnounceBirthdays;
    private JCheckBox chkAnnounceChildBirthdays;
    private JCheckBox chkAnnounceRecruitmentAnniversaries;
    //end General Tab

    //start Backgrounds Tab
    private JPanel pnlRandomBackgrounds;
    private JCheckBox chkUseRandomPersonalities;
    private JCheckBox chkUseRandomPersonalityReputation;
    private JCheckBox chkUseIntelligenceXpMultiplier;
    private JCheckBox chkUseSimulatedRelationships;
    private JPanel pnlRandomOriginOptions;
    private JCheckBox chkRandomizeOrigin;
    private JCheckBox chkRandomizeDependentsOrigin;
    private JCheckBox chkRandomizeAroundSpecifiedPlanet;
    private JCheckBox chkSpecifiedSystemFactionSpecific;
    private JLabel lblSpecifiedSystem;
    private MMComboBox<PlanetarySystem> comboSpecifiedSystem;
    private JLabel lblSpecifiedPlanet;
    private MMComboBox<Planet> comboSpecifiedPlanet;
    private JLabel lblOriginSearchRadius;
    private JSpinner spnOriginSearchRadius;
    private JLabel lblOriginDistanceScale;
    private JSpinner spnOriginDistanceScale;
    private JCheckBox chkAllowClanOrigins;
    private JCheckBox chkExtraRandomOrigin;
    //end Backgrounds Tab

    //start Death Tab
    private JCheckBox chkKeepMarriedNameUponSpouseDeath;
    private JLabel lblRandomDeathMethod;
    private MMComboBox<RandomDeathMethod> comboRandomDeathMethod;
    private JCheckBox chkUseRandomClanPersonnelDeath;
    private JCheckBox chkUseRandomPrisonerDeath;
    private JCheckBox chkUseRandomDeathSuicideCause;
    private JLabel lblPercentageRandomDeathChance;
    private JSpinner spnPercentageRandomDeathChance;

    private JPanel pnlDeathAgeGroup;
    private Map<AgeGroup, JCheckBox> chkEnabledRandomDeathAgeGroups;
    //end Death Tab

    //start Education Tab
    private JCheckBox chkUseEducationModule;
    private JLabel lblCurriculumXpRate;
    private JSpinner spnCurriculumXpRate;
    private JLabel lblMaximumJumpCount;
    private JSpinner spnMaximumJumpCount;
    private JCheckBox chkUseReeducationCamps;
    private JCheckBox chkEnableOverrideRequirements;
    private JCheckBox chkShowIneligibleAcademies;
    private JLabel lblEntranceExamBaseTargetNumber;
    private JSpinner spnEntranceExamBaseTargetNumber;
    private JLabel lblEntranceExamBaseTargetNumberPost;

    private JPanel pnlEnableStandardSets;
    private JCheckBox chkEnableLocalAcademies;
    private JCheckBox chkEnablePrestigiousAcademies;
    private JCheckBox chkEnableUnitEducation;

    private JPanel pnlXpAndSkillBonuses;
    private JCheckBox chkEnableBonuses;
    private JLabel lblFacultyXpMultiplier;
    private JSpinner spnFacultyXpMultiplier;

    private JPanel pnlDropoutChance;
    private JLabel lblAdultDropoutChance;
    private JSpinner spnAdultDropoutChance;
    private JLabel lblChildrenDropoutChance;
    private JSpinner spnChildrenDropoutChance;

    private JPanel pnlAccidentsAndEvents;
    private JCheckBox chkAllAges;
    private JLabel lblMilitaryAcademyAccidents;
    private JSpinner spnMilitaryAcademyAccidents;
    //end Education Tab

    //start Name and Portrait Tab
    private JCheckBox chkUseOriginFactionForNames;
    private JLabel lblFactionNames;
    private MMComboBox<String> comboFactionNames;

    private JPanel pnlRandomPortrait;
    private JCheckBox[] chkUsePortrait;
    private JCheckBox allPortraitsBox;
    private JCheckBox noPortraitsBox;
    private JCheckBox chkAssignPortraitOnRoleChange;
    //end Name and Portrait Tab

    //start Rank Tab
    private RankSystemsPane rankSystemsPane;
    //end Rank Tab

    /**
     * Initializes a new {@link BiographyTab} with the specified campaign, frame, and name.
     *
     * @param campaign the campaign associated with the {@link BiographyTab}
     * @param frame the {@link JFrame} used for displaying the {@link BiographyTab}
     * @param name the name of the {@link BiographyTab}
     */
    BiographyTab(Campaign campaign, JFrame frame, String name) {
        this.campaign = campaign;
        this.frame = frame;
        this.name = name;

        initialize();
    }

    /**
     * Initializes the components for the NameAndPortraitTab panel.
     * The panel contains various settings related to names and portraits.
     */
    private void initialize() {
        initializeGeneralTab();
        initializeBackgroundsTab();
        initializeDeathTab();
        initializeEducationTab();
        initializeNameAndPortraitTab();
    }

    /**
     * Initializes the components for the EducationTab panel.
     * The panel contains various settings related to the educational module.
     */
    private void initializeNameAndPortraitTab() {
        chkUseOriginFactionForNames = new JCheckBox();
        lblFactionNames = new JLabel();
        comboFactionNames = new MMComboBox<>("comboFactionNames", getFactionNamesModel());
        chkAssignPortraitOnRoleChange = new JCheckBox();

        pnlRandomPortrait = new JPanel();
        chkUsePortrait = new JCheckBox[1]; // We're going to properly initialize this later
        allPortraitsBox = new JCheckBox();
        noPortraitsBox = new JCheckBox();
    }

    /**
     * Initializes the components for the DeathTab panel.
     * The panel deals with various settings related to in-game character death.
     */
    private void initializeEducationTab() {
        chkUseEducationModule = new JCheckBox();
        lblCurriculumXpRate = new JLabel();
        spnCurriculumXpRate = new JSpinner();
        lblMaximumJumpCount = new JLabel();
        spnMaximumJumpCount = new JSpinner();
        chkUseReeducationCamps = new JCheckBox();
        chkEnableOverrideRequirements = new JCheckBox();
        chkShowIneligibleAcademies = new JCheckBox();
        lblEntranceExamBaseTargetNumber = new JLabel();
        spnEntranceExamBaseTargetNumber = new JSpinner();
        lblEntranceExamBaseTargetNumberPost = new JLabel();

        pnlEnableStandardSets = new JPanel();
        chkEnableLocalAcademies = new JCheckBox();
        chkEnablePrestigiousAcademies = new JCheckBox();
        chkEnableUnitEducation = new JCheckBox();

        pnlXpAndSkillBonuses = new JPanel();
        chkEnableBonuses = new JCheckBox();
        lblFacultyXpMultiplier = new JLabel();
        spnFacultyXpMultiplier = new JSpinner();

        pnlDropoutChance = new JPanel();
        lblAdultDropoutChance = new JLabel();
        spnAdultDropoutChance = new JSpinner();
        lblChildrenDropoutChance = new JLabel();
        spnChildrenDropoutChance = new JSpinner();

        pnlAccidentsAndEvents = new JPanel();
        chkAllAges = new JCheckBox();
        lblMilitaryAcademyAccidents = new JLabel();
        spnMilitaryAcademyAccidents = new JSpinner();
    }

    /**
     * Initializes the components for the BackgroundsTab panel.
     * The panel contains various settings related to backgrounds and origins.
     */
    private void initializeDeathTab() {
        chkKeepMarriedNameUponSpouseDeath = new JCheckBox();
        lblRandomDeathMethod = new JLabel();
        comboRandomDeathMethod = new MMComboBox<>("comboRandomDeathMethod", RandomDeathMethod.values());
        chkUseRandomClanPersonnelDeath = new JCheckBox();
        chkUseRandomPrisonerDeath = new JCheckBox();
        chkUseRandomDeathSuicideCause = new JCheckBox();
        lblPercentageRandomDeathChance = new JLabel();
        spnPercentageRandomDeathChance = new JSpinner();

        pnlDeathAgeGroup = new JPanel();
        chkEnabledRandomDeathAgeGroups = new HashMap<>();
    }

    /**
     * Initializes the components for the GeneralTab panel.
     * The panel contains various general settings.
     */
    private void initializeBackgroundsTab() {
        pnlRandomBackgrounds = new JPanel();
        chkUseRandomPersonalities = new JCheckBox();
        chkUseRandomPersonalityReputation = new JCheckBox();
        chkUseIntelligenceXpMultiplier = new JCheckBox();
        chkUseSimulatedRelationships = new JCheckBox();

        pnlRandomOriginOptions = new JPanel();
        chkRandomizeOrigin = new JCheckBox();
        chkRandomizeDependentsOrigin = new JCheckBox();
        chkRandomizeAroundSpecifiedPlanet = new JCheckBox();
        chkSpecifiedSystemFactionSpecific = new JCheckBox();
        lblSpecifiedSystem = new JLabel();
        comboSpecifiedSystem = new MMComboBox<>("comboSpecifiedSystem");
        lblSpecifiedPlanet = new JLabel();
        comboSpecifiedPlanet = new MMComboBox<>("comboSpecifiedPlanet");
        lblOriginSearchRadius = new JLabel();
        spnOriginSearchRadius = new JSpinner();
        lblOriginDistanceScale = new JLabel();
        spnOriginDistanceScale = new JSpinner();
        chkAllowClanOrigins = new JCheckBox();
        chkExtraRandomOrigin = new JCheckBox();
    }

    /**
     * Initializes components of the GeneralTab.
     * The panel contains general settings.
     */
    private void initializeGeneralTab() {
        chkUseDylansRandomXP = new JCheckBox();
        lblGender = new JLabel();
        sldGender = new JSlider();
        lblNonBinaryDiceSize = new JLabel();
        spnNonBinaryDiceSize = new JSpinner();
        lblFamilyDisplayLevel = new JLabel();
        comboFamilyDisplayLevel = new MMComboBox<>("comboFamilyDisplayLevel",
            FamilialRelationshipDisplayLevel.values());

        pnlAnniversariesPanel = new JPanel();
        chkAnnounceOfficersOnly = new JCheckBox();
        chkAnnounceBirthdays = new JCheckBox();
        chkAnnounceChildBirthdays = new JCheckBox();
        chkAnnounceRecruitmentAnniversaries = new JCheckBox();
    }

    /**
     * Retrieves a {@link DefaultComboBoxModel} containing faction names generated by the
     * {@link RandomNameGenerator} instance.
     *
     * @return DefaultComboBoxModel<String> containing faction names
     */
    private static DefaultComboBoxModel<String> getFactionNamesModel() {
        DefaultComboBoxModel<String> factionNamesModel = new DefaultComboBoxModel<>();
        for (final String faction : RandomNameGenerator.getInstance().getFactions()) {
            factionNamesModel.addElement(faction);
        }
        factionNamesModel.setSelectedItem(RandomNameGenerator.getInstance().getChosenFaction());
        return factionNamesModel;
    }

    /**
     * Creates a general tab for the Biography category containing various components
     *
     * @return a {@link JPanel} representing the general tab with multiple input components and panels
     */
    JPanel createGeneralTab() {
        // Header
        JPanel headerPanel = new CampaignOptionsHeaderPanel("BiographyGeneralTab",
            getImageDirectory() + "logo_federated_suns.png",
            true);

        // Contents
        chkUseDylansRandomXP = new CampaignOptionsCheckBox("UseDylansRandomXP");

        lblGender = new CampaignOptionsLabel("Gender");
        sldGender = new JSlider(SwingConstants.HORIZONTAL, 0, 100, RandomGenderGenerator.getPercentFemale());
        sldGender.setMajorTickSpacing(25);
        sldGender.setPaintTicks(true);
        sldGender.setPaintLabels(true);

        lblNonBinaryDiceSize = new CampaignOptionsLabel("NonBinaryDiceSize");
        spnNonBinaryDiceSize = new CampaignOptionsSpinner("NonBinaryDiceSize",
            60, 0, 100000, 1);

        lblFamilyDisplayLevel = new CampaignOptionsLabel("FamilyDisplayLevel");

        pnlAnniversariesPanel = createAnniversariesPanel();

        // Layout the Panel
        final JPanel panelLeft = new CampaignOptionsStandardPanel("BiographyGeneralTabLeft", true);
        final GridBagConstraints layoutLeft = new CampaignOptionsGridBagConstraints(panelLeft);

        layoutLeft.gridy = 0;
        layoutLeft.gridx = 0;
        layoutLeft.gridwidth = 1;
        panelLeft.add(chkUseDylansRandomXP, layoutLeft);

        layoutLeft.gridx = 0;
        layoutLeft.gridy++;
        panelLeft.add(lblGender, layoutLeft);
        layoutLeft.gridx++;
        panelLeft.add(sldGender, layoutLeft);

        layoutLeft.gridx = 0;
        layoutLeft.gridy++;
        panelLeft.add(lblNonBinaryDiceSize, layoutLeft);
        layoutLeft.gridx++;
        panelLeft.add(spnNonBinaryDiceSize, layoutLeft);

        layoutLeft.gridx = 0;
        layoutLeft.gridy++;
        panelLeft.add(lblFamilyDisplayLevel, layoutLeft);
        layoutLeft.gridx++;
        panelLeft.add(comboFamilyDisplayLevel, layoutLeft);

        final JPanel panelParent = new CampaignOptionsStandardPanel("BiographyGeneralTab", true);
        final GridBagConstraints layoutParent = new CampaignOptionsGridBagConstraints(panelParent);
        layoutParent.gridwidth = 5;
        layoutParent.gridx = 0;
        layoutParent.gridy = 0;
        panelParent.add(headerPanel, layoutParent);

        layoutParent.gridy++;
        layoutParent.gridwidth = 1;
        panelParent.add(panelLeft, layoutParent);
        layoutParent.gridx++;
        panelParent.add(pnlAnniversariesPanel, layoutParent);

        // Create Parent Panel and return
        return createParentPanel(panelParent, "BiographyGeneralTab");
    }

    /**
     * @return a {@link JPanel} representing the Anniversaries panel with checkboxes for different
     * anniversary options
     */
    private JPanel createAnniversariesPanel() {
        // Contents
        chkAnnounceBirthdays = new CampaignOptionsCheckBox("AnnounceBirthdays");
        chkAnnounceRecruitmentAnniversaries = new CampaignOptionsCheckBox("AnnounceRecruitmentAnniversaries");
        chkAnnounceOfficersOnly = new CampaignOptionsCheckBox("AnnounceOfficersOnly");
        chkAnnounceChildBirthdays = new CampaignOptionsCheckBox("AnnounceChildBirthdays");

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("AnniversariesPanel", true,
            "AnniversariesPanel");
        final GridBagConstraints layoutParent = new CampaignOptionsGridBagConstraints(panel);

        layoutParent.gridwidth = 5;
        layoutParent.gridx = 0;
        layoutParent.gridy = 0;
        panel.add(chkAnnounceBirthdays, layoutParent);

        layoutParent.gridy++;
        layoutParent.gridwidth = 1;
        panel.add(chkAnnounceRecruitmentAnniversaries, layoutParent);

        layoutParent.gridy++;
        panel.add(chkAnnounceOfficersOnly, layoutParent);

        layoutParent.gridy++;
        panel.add(chkAnnounceChildBirthdays, layoutParent);

        return panel;
    }

    /**
     * @return a JPanel representing the Backgrounds tab with specific components like checkboxes and
     * options panel
     */
    JPanel createBackgroundsTab() {
        // Header
        JPanel headerPanel = new CampaignOptionsHeaderPanel("BackgroundsTab",
            getImageDirectory() + "logo_clan_goliath_scorpion.png", true);

        // Contents
        pnlRandomOriginOptions = createRandomOriginOptionsPanel();
        pnlRandomBackgrounds = createRandomBackgroundsPanel();

        // Layout the Panels
        final JPanel panel = new CampaignOptionsStandardPanel("BackgroundsTab", true);
        final GridBagConstraints layout = new CampaignOptionsGridBagConstraints(panel);

        layout.gridwidth = 5;
        layout.gridx = 0;
        layout.gridy = 0;
        panel.add(headerPanel, layout);

        layout.gridy++;
        layout.gridwidth = 1;
        panel.add(pnlRandomOriginOptions, layout);

        layout.gridx++;
        panel.add(pnlRandomBackgrounds, layout);

        // Create Parent Panel and return
        return createParentPanel(panel, "BackgroundsTab");
    }

    JPanel createRandomBackgroundsPanel() {
        // Contents
        chkUseRandomPersonalities = new CampaignOptionsCheckBox("UseRandomPersonalities");
        chkUseRandomPersonalityReputation = new CampaignOptionsCheckBox("UseRandomPersonalityReputation");
        chkUseIntelligenceXpMultiplier = new CampaignOptionsCheckBox("UseIntelligenceXpMultiplier");
        chkUseSimulatedRelationships = new CampaignOptionsCheckBox("UseSimulatedRelationships");

        // Layout the Panels
        final JPanel panel = new CampaignOptionsStandardPanel("RandomBackgroundsPanel", true,
            "RandomBackgroundsPanel");
        final GridBagConstraints layout = new CampaignOptionsGridBagConstraints(panel);

        layout.gridwidth = 1;
        layout.gridx = 0;
        layout.gridy = 0;
        panel.add(chkUseRandomPersonalities, layout);

        layout.gridy++;
        panel.add(chkUseRandomPersonalityReputation, layout);

        layout.gridy++;
        panel.add(chkUseIntelligenceXpMultiplier, layout);

        layout.gridy++;
        panel.add(chkUseSimulatedRelationships, layout);

        return panel;
    }

    /**
     * Creates a panel containing options for randomizing personnel origins.
     *
     * @return a {@link JPanel} containing the random origin options, configured with the necessary components
     */
    private JPanel createRandomOriginOptionsPanel() {
        // Contents
        chkRandomizeOrigin = new CampaignOptionsCheckBox("RandomizeOrigin");
        chkRandomizeDependentsOrigin = new CampaignOptionsCheckBox("RandomizeDependentsOrigin");
        chkRandomizeAroundSpecifiedPlanet = new CampaignOptionsCheckBox("RandomizeAroundSpecifiedPlanet");

        chkSpecifiedSystemFactionSpecific = new CampaignOptionsCheckBox("SpecifiedSystemFactionSpecific");
        chkSpecifiedSystemFactionSpecific.addActionListener(evt -> {
            final PlanetarySystem planetarySystem = comboSpecifiedSystem.getSelectedItem();
            if ((planetarySystem == null)
                || !planetarySystem.getFactionSet(campaign.getLocalDate()).contains(campaign.getFaction())) {
                restoreComboSpecifiedSystem();
            }
        });


        lblSpecifiedSystem = new CampaignOptionsLabel("SpecifiedSystem");
        comboSpecifiedSystem.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                          final int index, final boolean isSelected,
                                                          final boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PlanetarySystem) {
                    setText(((PlanetarySystem) value).getName(campaign.getLocalDate()));
                }
                return this;
            }
        });
        comboSpecifiedSystem.addActionListener(evt -> {
            final PlanetarySystem planetarySystem = comboSpecifiedSystem.getSelectedItem();
            final Planet planet = comboSpecifiedPlanet.getSelectedItem();
            if ((planetarySystem == null)
                || ((planet != null) && !planet.getParentSystem().equals(planetarySystem))) {
                restoreComboSpecifiedPlanet();
            }
        });

        lblSpecifiedPlanet = new CampaignOptionsLabel("SpecifiedPlanet");
        comboSpecifiedPlanet.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                          final int index, final boolean isSelected,
                                                          final boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Planet) {
                    setText(((Planet) value).getName(campaign.getLocalDate()));
                }
                return this;
            }
        });

        lblOriginSearchRadius = new CampaignOptionsLabel("OriginSearchRadius");
        spnOriginSearchRadius = new CampaignOptionsSpinner("OriginSearchRadius",
            0, 0, 2000, 25);

        lblOriginDistanceScale = new CampaignOptionsLabel("OriginDistanceScale");
        spnOriginDistanceScale = new CampaignOptionsSpinner("OriginDistanceScale",
            0.6, 0.1, 2.0, 0.1);

        chkAllowClanOrigins = new CampaignOptionsCheckBox("AllowClanOrigins");
        chkExtraRandomOrigin = new CampaignOptionsCheckBox("ExtraRandomOrigin");

        // Layout the Panel
        final JPanel panelSystemPlanetOrigins = new CampaignOptionsStandardPanel(
            "RandomOriginOptionsPanelSystemPlanetOrigins", false, "");
        final GridBagConstraints layoutSystemPlanetOrigins = new CampaignOptionsGridBagConstraints(panelSystemPlanetOrigins);

        layoutSystemPlanetOrigins.gridwidth = 1;
        layoutSystemPlanetOrigins.gridx = 0;
        layoutSystemPlanetOrigins.gridy = 0;
        panelSystemPlanetOrigins.add(lblSpecifiedSystem, layoutSystemPlanetOrigins);
        layoutSystemPlanetOrigins.gridx++;
        panelSystemPlanetOrigins.add(comboSpecifiedSystem, layoutSystemPlanetOrigins);
        layoutSystemPlanetOrigins.gridx++;
        panelSystemPlanetOrigins.add(lblSpecifiedPlanet, layoutSystemPlanetOrigins);
        layoutSystemPlanetOrigins.gridx++;
        panelSystemPlanetOrigins.add(comboSpecifiedPlanet, layoutSystemPlanetOrigins);

        layoutSystemPlanetOrigins.gridx = 0;
        layoutSystemPlanetOrigins.gridy++;
        panelSystemPlanetOrigins.add(lblOriginSearchRadius, layoutSystemPlanetOrigins);
        layoutSystemPlanetOrigins.gridx++;
        panelSystemPlanetOrigins.add(spnOriginSearchRadius, layoutSystemPlanetOrigins);
        layoutSystemPlanetOrigins.gridx++;
        panelSystemPlanetOrigins.add(lblOriginDistanceScale, layoutSystemPlanetOrigins);
        layoutSystemPlanetOrigins.gridx++;
        panelSystemPlanetOrigins.add(spnOriginDistanceScale, layoutSystemPlanetOrigins);

        final JPanel panel = new CampaignOptionsStandardPanel("RandomOriginOptionsPanel", true,
            "RandomOriginOptionsPanel");
        final GridBagConstraints layout = new CampaignOptionsGridBagConstraints(panel);

        layout.gridwidth = 1;
        layout.gridx = 0;
        layout.gridy = 0;
        panel.add(chkRandomizeOrigin, layout);

        layout.gridy++;
        panel.add(chkRandomizeDependentsOrigin, layout);

        layout.gridy++;
        panel.add(chkRandomizeAroundSpecifiedPlanet, layout);

        layout.gridy++;
        panel.add(chkSpecifiedSystemFactionSpecific, layout);

        layout.gridy++;
        panel.add(panelSystemPlanetOrigins, layout);

        layout.gridx = 0;
        layout.gridy++;
        panel.add(chkAllowClanOrigins, layout);

        layout.gridy++;
        panel.add(chkExtraRandomOrigin, layout);

        return panel;
    }

    /**
     * Restores the list of planets in the combo box based on the selected planetary system.
     * If no planetary system is selected, clear the combo box.
     * If a planetary system is selected, populates the combo box with the planets from that system,
     * and selects the primary planet of the system.
     */
    private void restoreComboSpecifiedPlanet() {
        final PlanetarySystem planetarySystem = comboSpecifiedSystem.getSelectedItem();

        if (planetarySystem == null) {
            comboSpecifiedPlanet.removeAllItems();
        } else {
            comboSpecifiedPlanet.setModel(new DefaultComboBoxModel<>(
                planetarySystem.getPlanets().toArray(new Planet[] {})));
            comboSpecifiedPlanet.setSelectedItem(planetarySystem.getPrimaryPlanet());
        }
    }

    /**
     * Removes all items from the combo box, then populates it with planetary systems
     * based on the selected faction filter.
     * Restores the combo box for specified planets after updating the specified system combo box.
     */
    private void restoreComboSpecifiedSystem() {
        comboSpecifiedSystem.removeAllItems();

        comboSpecifiedSystem.setModel(new DefaultComboBoxModel<>(getPlanetarySystems(
            chkSpecifiedSystemFactionSpecific.isSelected() ? campaign.getFaction() : null)));

        restoreComboSpecifiedPlanet();
    }

    /**
     * Retrieves an array of {@link PlanetarySystem} objects based on the provided {@link Faction}.
     *
     * @param faction The {@link Faction} to filter the {@link PlanetarySystem} by. Specify null for
     *               no filtering.
     * @return An array of {@link PlanetarySystem} objects that match the filtering criteria, sorted
     * by system name.
     */
    private PlanetarySystem[] getPlanetarySystems(final @Nullable Faction faction) {
        ArrayList<PlanetarySystem> systems = campaign.getSystems();
        ArrayList<PlanetarySystem> filteredSystems = new ArrayList<>();

        // Filter systems
        for (PlanetarySystem planetarySystem : systems) {
            if ((faction == null) || planetarySystem.getFactionSet(campaign.getLocalDate()).contains(faction)) {
                filteredSystems.add(planetarySystem);
            }
        }

        // Sort systems
        filteredSystems.sort(Comparator.comparing(p -> p.getName(campaign.getLocalDate())));

        // Convert to array
        return filteredSystems.toArray(new PlanetarySystem[0]);
    }

    /**
     * Creates and returns a {@link JPanel} for the Death Tab.
     * The panel includes header, various checkboxes, labels, spinners, and a custom combo box.
     *
     * @return {@link JPanel} representing the Death Tab with all its components
     */
    JPanel createDeathTab() {
        // Header
        JPanel headerPanel = new CampaignOptionsHeaderPanel("DeathTab",
            getImageDirectory() + "logo_filtvelt_coalition.png",
            true);

        // Contents
        chkKeepMarriedNameUponSpouseDeath = new CampaignOptionsCheckBox("KeepMarriedNameUponSpouseDeath");

        lblRandomDeathMethod = new CampaignOptionsLabel("RandomDeathMethod");
        comboRandomDeathMethod.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                          final int index, final boolean isSelected,
                                                          final boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof RandomDeathMethod) {
                    list.setToolTipText(((RandomDeathMethod) value).getToolTipText());
                }
                return this;
            }
        });

        chkUseRandomClanPersonnelDeath = new CampaignOptionsCheckBox("UseRandomClanPersonnelDeath");
        chkUseRandomPrisonerDeath = new CampaignOptionsCheckBox("UseRandomPrisonerDeath");
        chkUseRandomDeathSuicideCause = new CampaignOptionsCheckBox("UseRandomDeathSuicideCause");

        pnlDeathAgeGroup = createDeathAgeGroupsPanel();

        lblPercentageRandomDeathChance = new CampaignOptionsLabel("PercentageRandomDeathChance");
        spnPercentageRandomDeathChance = new CampaignOptionsSpinner("PercentageRandomDeathChance",
            0, 0, 100, 0.000001);

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("DeathTab", true);
        final GroupLayout layout = createGroupLayout(panel);
        panel.setLayout(layout);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(headerPanel)
                .addComponent(chkKeepMarriedNameUponSpouseDeath)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblRandomDeathMethod)
                    .addComponent(comboRandomDeathMethod))
                .addComponent(chkUseRandomClanPersonnelDeath)
                .addComponent(chkUseRandomPrisonerDeath)
                .addComponent(chkUseRandomDeathSuicideCause)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblPercentageRandomDeathChance)
                    .addComponent(spnPercentageRandomDeathChance))
                .addComponent(pnlDeathAgeGroup));

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(headerPanel, Alignment.CENTER)
                    .addComponent(chkKeepMarriedNameUponSpouseDeath)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblRandomDeathMethod)
                        .addComponent(comboRandomDeathMethod)
                        )
                    .addComponent(chkUseRandomClanPersonnelDeath)
                    .addComponent(chkUseRandomPrisonerDeath)
                    .addComponent(chkUseRandomDeathSuicideCause)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblPercentageRandomDeathChance)
                            .addComponent(spnPercentageRandomDeathChance)
                            )
                        .addComponent(pnlDeathAgeGroup)));

        // Create Parent Panel and return
        return createParentPanel(panel, "DeathTab");
    }

    /**
     * Creates a panel for the Death Age Groups tab with checkboxes for different age groups
     *
     * @return a {@link JPanel} representing the Death Age Groups tab with checkboxes for each age group
     */
    private JPanel createDeathAgeGroupsPanel() {
        final AgeGroup[] ageGroups = AgeGroup.values();

        // Create the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("DeathAgeGroupsPanel", true,
            "DeathAgeGroupsPanel");
        panel.setLayout(new GridLayout(1, ageGroups.length));

        // Contents
        for (final AgeGroup ageGroup : ageGroups) {
            final JCheckBox checkBox = new JCheckBox(ageGroup.toString());
            checkBox.setToolTipText(ageGroup.getToolTipText());
            checkBox.setName("chk" + ageGroup);

            panel.add(checkBox);
            chkEnabledRandomDeathAgeGroups.put(ageGroup, checkBox);
        }

        return panel;
    }

    /**
     * Constructs and configures an "Education" {@link JPanel}.
     * The panel includes various controls related to education settings,
     * including checkboxes for enabling specific modules and settings,
     * spinners for configuring values such as XP rate and jump count,
     * and separate panels for managing standard sets, XP and skill bonuses,
     * dropout chances, accidents, and events.
     * <p>
     * The JPanel configuration is done using a {@link GroupLayout}, setting up
     * the components in a structured layout with optimized vertical and
     * horizontal alignments.
     *
     * @return {@link JPanel} The newly created and configured parent JPanel for
     * the Education tab, containing all the educational settings controls.
     */
    JPanel createEducationTab() {
        // Header
        JPanel headerPanel = new CampaignOptionsHeaderPanel("EducationTab",
            getImageDirectory() + "logo_free_worlds_league.png",
            true);

        // Contents
        chkUseEducationModule = new CampaignOptionsCheckBox("UseEducationModule");

        lblCurriculumXpRate = new CampaignOptionsLabel("CurriculumXpRate");
        spnCurriculumXpRate = new CampaignOptionsSpinner("CurriculumXpRate",
            3, 1, 10, 1);

        lblMaximumJumpCount = new CampaignOptionsLabel("MaximumJumpCount");
        spnMaximumJumpCount = new CampaignOptionsSpinner("MaximumJumpCount",
            5, 1, 200, 1);

        chkUseReeducationCamps = new CampaignOptionsCheckBox("UseReeducationCamps");

        pnlEnableStandardSets = createEnableStandardSetsPanel();

        chkEnableOverrideRequirements = new CampaignOptionsCheckBox("EnableOverrideRequirements");

        chkShowIneligibleAcademies = new CampaignOptionsCheckBox("ShowIneligibleAcademies");

        lblEntranceExamBaseTargetNumber = new CampaignOptionsLabel("EntranceExamBaseTargetNumber");
        spnEntranceExamBaseTargetNumber = new CampaignOptionsSpinner("EntranceExamBaseTargetNumber",
            14, 0, 20, 1);
        lblEntranceExamBaseTargetNumberPost = new CampaignOptionsLabel("EntranceExamBaseTargetNumberPost");

        pnlXpAndSkillBonuses = createXpAndSkillBonusesPanel();

        pnlDropoutChance = createDropoutChancePanel();

        pnlAccidentsAndEvents = createAccidentsAndEventsPanel();

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("EducationTab", true);
        final GroupLayout layout = createGroupLayout(panel);
        panel.setLayout(layout);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(headerPanel)
                .addComponent(chkUseEducationModule)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblCurriculumXpRate)
                    .addComponent(spnCurriculumXpRate)
                    .addComponent(lblMaximumJumpCount)
                    .addComponent(spnMaximumJumpCount))
                .addComponent(chkUseReeducationCamps)
                .addComponent(pnlEnableStandardSets)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(chkShowIneligibleAcademies)
                    .addComponent(chkEnableOverrideRequirements))
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblEntranceExamBaseTargetNumber)
                    .addComponent(spnEntranceExamBaseTargetNumber)
                    .addComponent(lblEntranceExamBaseTargetNumberPost))
                .addComponent(pnlXpAndSkillBonuses)
                .addComponent(pnlDropoutChance)
                .addComponent(pnlAccidentsAndEvents));

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(headerPanel, Alignment.CENTER)
                    .addComponent(chkUseEducationModule)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblCurriculumXpRate)
                        .addComponent(spnCurriculumXpRate)
                        .addComponent(lblMaximumJumpCount)
                        .addComponent(spnMaximumJumpCount)
                        )
                    .addComponent(chkUseReeducationCamps)
                    .addComponent(pnlEnableStandardSets)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chkShowIneligibleAcademies)
                        .addComponent(chkEnableOverrideRequirements)
                        )
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblEntranceExamBaseTargetNumber)
                        .addComponent(spnEntranceExamBaseTargetNumber)
                        .addComponent(lblEntranceExamBaseTargetNumberPost)
                        )
                    .addComponent(pnlXpAndSkillBonuses)
                    .addComponent(pnlDropoutChance)
                    .addComponent(pnlAccidentsAndEvents)));

        // Create Parent Panel and return
        return createParentPanel(panel, "EducationTab");
    }

    /**
     * Constructs and configures an "Enable Standard Sets" {@link JPanel}.
     * The panel consists of three checkboxes: to enable local academies,
     * prestigious academies, and unit education.
     * <p>
     * A {@link GroupLayout} is used to arrange these components in an
     * optimal manner, ensuring good readability and usability in both
     * vertical and horizontal alignments.
     *
     * @return {@link JPanel} The newly constructed and configured JPanel for
     * Enable Standard Sets settings.
     */
    private JPanel createEnableStandardSetsPanel() {
        // Contents
        chkEnableLocalAcademies = new CampaignOptionsCheckBox("EnableLocalAcademies");
        chkEnablePrestigiousAcademies = new CampaignOptionsCheckBox("EnablePrestigiousAcademies");
        chkEnableUnitEducation = new CampaignOptionsCheckBox("EnableUnitEducation");

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("EnableStandardSetsPanel", true,
            "EnableStandardSetsPanel");
        final GroupLayout layout = createGroupLayout(panel);
        panel.setLayout(layout);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(chkEnableLocalAcademies)
                    .addComponent(chkEnablePrestigiousAcademies)
                    .addComponent(chkEnableUnitEducation)));

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chkEnableLocalAcademies)
                        .addComponent(chkEnablePrestigiousAcademies)
                        .addComponent(chkEnableUnitEducation)
                        )));

        return panel;
    }

    /**
     * Constructs and configures an "XP and Skill Bonuses" {@link JPanel}.
     * The panel includes a checkbox to enable or disable bonuses, a label
     * for faculty XP multiplier, and a spinner to adjust the faculty XP multiplier value.
     * <p>
     * The layout of the panel is set up with a {@link GroupLayout}, arranging
     * the checkbox, label, and spinner for optimal vertical and horizontal alignment.
     *
     * @return {@link JPanel} The newly created and configured JPanel
     * containing the XP and Skill Bonuses settings.
     */
    private JPanel createXpAndSkillBonusesPanel() {
        // Contents
        chkEnableBonuses = new CampaignOptionsCheckBox("EnableBonuses");
        lblFacultyXpMultiplier = new CampaignOptionsLabel("FacultyXpMultiplier");
        spnFacultyXpMultiplier = new CampaignOptionsSpinner("FacultyXpMultiplier",
            1.00, 0.00, 10.00, 0.01);

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("XpAndSkillBonusesPanel", true,
            "XpAndSkillBonusesPanel");
        final GroupLayout layout = createGroupLayout(panel);
        panel.setLayout(layout);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(chkEnableBonuses)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblFacultyXpMultiplier)
                    .addComponent(spnFacultyXpMultiplier)));

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(chkEnableBonuses)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFacultyXpMultiplier)
                        .addComponent(spnFacultyXpMultiplier)
                        )));

        return panel;
    }

    /**
     * Constructs and configures a "Dropout Chance" {@link JPanel}.
     * The created panel includes two labels and spinners: one set for adult dropout chances,
     * and the other for child dropout chances.
     * <p>
     * The JPanel layout is arranged using a {@link GroupLayout}, positioning the labels and spinners
     * in a way that optimizes horizontal and vertical alignments.
     *
     * @return {@link JPanel} The newly constructed and configured JPanel that contains the Dropout Chance settings.
     */
    private JPanel createDropoutChancePanel() {
        // Contents
        lblAdultDropoutChance = new CampaignOptionsLabel("AdultDropoutChance");
        spnAdultDropoutChance = new CampaignOptionsSpinner("AdultDropoutChance",
            1000, 0, 100000, 1);
        lblChildrenDropoutChance = new CampaignOptionsLabel("ChildrenDropoutChance");
        spnChildrenDropoutChance = new CampaignOptionsSpinner("ChildrenDropoutChance",
            10000, 0, 100000, 1);

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("DropoutChancePanel", true,
            "DropoutChancePanel");
        final GroupLayout layout = createGroupLayout(panel);
        panel.setLayout(layout);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblAdultDropoutChance)
                    .addComponent(spnAdultDropoutChance))
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblChildrenDropoutChance)
                    .addComponent(spnChildrenDropoutChance)));

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblAdultDropoutChance)
                        .addComponent(spnAdultDropoutChance)
                        )
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblChildrenDropoutChance)
                        .addComponent(spnChildrenDropoutChance)
                        )));

        return panel;
    }

    /**
     * Constructs and configures an "Accidents and Events" {@link JPanel}. The panel includes a checkbox
     * indicating whether to include all ages, a label for military academy accidents, and also a spinner
     * for adjusting the count of military academy accidents.
     * <p>
     * The layout of the panel is configured with a {@link GroupLayout}, organizing the checkbox and
     * other components for optimized alignments in vertical and horizontal layouts.
     *
     * @return {@link JPanel} The constructed, configured JPanel containing "Accidents and Events"
     * settings
     */
    private JPanel createAccidentsAndEventsPanel() {
        // Contents
        chkAllAges = new CampaignOptionsCheckBox("AllAges");
        lblMilitaryAcademyAccidents = new CampaignOptionsLabel("MilitaryAcademyAccidents");
        spnMilitaryAcademyAccidents = new CampaignOptionsSpinner("MilitaryAcademyAccidents",
            10000, 0, 100000, 1);

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("AccidentsAndEventsPanel", true,
            "AccidentsAndEventsPanel");
        final GroupLayout layout = createGroupLayout(panel);
        panel.setLayout(layout);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(chkAllAges)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblMilitaryAcademyAccidents)
                    .addComponent(spnMilitaryAcademyAccidents)));

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(chkAllAges)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblMilitaryAcademyAccidents)
                        .addComponent(spnMilitaryAcademyAccidents)
                        )));

        return panel;
    }

    /**
     * Constructs and configures a "Name and Portrait Generation" {@link JPanel}. This panel contains
     * various components for managing how names and portraits are generated. It includes a checkbox
     * for using the origin faction for names, a label and combo box for faction names, a panel for
     * random portrait selection, and a checkbox for assigning a portrait on role change.
     * <p>
     * A {@link GroupLayout} is used to optimally arrange these components for effective vertical
     * and horizontal alignment.
     *
     * @return {@link JPanel} The newly created and configured parent JPanel for the Name and Portrait
     * Generation tab, containing all relevant name and portrait generation controls.
     */
    JPanel createNameAndPortraitGenerationTab() {
        // Header
        JPanel headerPanel = new CampaignOptionsHeaderPanel("NameAndPortraitGenerationTab",
            getImageDirectory() + "logo_clan_mongoose.png",
            true);

        // Contents
        chkUseOriginFactionForNames = new CampaignOptionsCheckBox("UseOriginFactionForNames");

        lblFactionNames = new CampaignOptionsLabel("FactionNames");

        pnlRandomPortrait = createRandomPortraitPanel();

        chkAssignPortraitOnRoleChange = new CampaignOptionsCheckBox("AssignPortraitOnRoleChange");

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("NameAndPortraitGenerationTab", true,
            "");
        final GroupLayout layout = createGroupLayout(panel);
        panel.setLayout(layout);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(headerPanel)
                .addComponent(chkUseOriginFactionForNames)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblFactionNames)
                    .addComponent(comboFactionNames))
                .addComponent(pnlRandomPortrait)
                .addComponent(chkAssignPortraitOnRoleChange));

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(headerPanel, Alignment.CENTER)
                    .addComponent(chkUseOriginFactionForNames)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFactionNames)
                        .addComponent(comboFactionNames)
                        )
                    .addComponent(pnlRandomPortrait)
                    .addComponent(chkAssignPortraitOnRoleChange)));

        // Create Parent Panel and return
        return createParentPanel(panel, "NameAndPortraitGenerationTab");
    }

    /**
     * Constructs and configures a {@link JPanel} for managing random portrait settings.
     * The panel contains individual checkboxes for each personnel role defined in {@link PersonnelRole},
     * as well as two special checkboxes: one for selecting all portraits, and one for selecting none.
     * Selecting either of these special checkboxes will override the individual role selections and
     * disable them.
     * <p>
     * The panel uses a grid layout, arranging the checkboxes in a grid that extends up to 4 columns,
     * with each checkbox
     * representing a particular role. It also has a border around it, titled with the panel's name.
     *
     * @return {@link JPanel} The newly constructed and configured JPanel for managing random portrait
     * settings.
     */
    private JPanel createRandomPortraitPanel() {
        // Contents
        final PersonnelRole[] personnelRoles = PersonnelRole.values();

        chkUsePortrait = new JCheckBox[personnelRoles.length];

        allPortraitsBox = new JCheckBox(resources.getString("lblAllPortraitsBox.text"));
        allPortraitsBox.addActionListener(evt -> {
            final boolean selected = allPortraitsBox.isSelected();
            for (final JCheckBox box : chkUsePortrait) {
                if (selected) {
                    box.setSelected(true);
                }
                box.setEnabled(!selected);
            }
            if (selected) {
                noPortraitsBox.setSelected(false);
            }
        });

        noPortraitsBox = new JCheckBox(resources.getString("lblNoPortraitsBox.text"));
        noPortraitsBox.addActionListener(evt -> {
            final boolean selected = noPortraitsBox.isSelected();
            for (final JCheckBox box : chkUsePortrait) {
                if (selected) {
                    box.setSelected(false);
                }
                box.setEnabled(!selected);
            }
            if (selected) {
                allPortraitsBox.setSelected(false);
            }
        });

        // Layout the Panel
        JPanel panel = new JPanel(
            new GridLayout((int) Math.ceil((personnelRoles.length + 2) / 4.0), 4));
        panel.setBorder(BorderFactory.createTitledBorder(
            String.format(String.format("<html>%s</html>",
                resources.getString("lblRandomPortraitPanel.text")))));

        panel.add(allPortraitsBox);
        panel.add(noPortraitsBox);

        // Add remaining checkboxes
        JCheckBox jCheckBox;
        for (final PersonnelRole role : PersonnelRole.values()) {
            jCheckBox = new JCheckBox(role.toString());
            panel.add(jCheckBox);
            chkUsePortrait[role.ordinal()] = jCheckBox;
        }

        return panel;
    }

    /**
     * This method is responsible for creating and setting up the RankTab
     * and its components.
     *
     * <p>The method creates a header panel with a specified logo and then initializes
     * {@code rankSystemsPane} with a {@code RankSystemsPane} object. The preferred size of the
     * {@code rankSystemsPane} is set as its minimum and maximum size.
     *
     * <p>The layout is created with a vertical and horizontal group,
     * to which the components are added sequentially. The {@code headerPanel} and
     * the view-port component of {@code rankSystemsPane} are added to both layout groups.
     *
     * <p>Finally, the layout components are added to a parent panel which is then returned.
     *
     * @return JPanel The parent panel with all the layout components added.
     */
    JPanel createRankTab() {
        // Header
        JPanel headerPanel = new CampaignOptionsHeaderPanel("RankTab",
            getImageDirectory() + "logo_hanseatic_league.png",
            true);

        // Contents
        rankSystemsPane = new RankSystemsPane(frame, campaign);
        Component rankSystemsViewport = rankSystemsPane.getViewport().getView();

        // Layout the Panel
        final JPanel panel = new CampaignOptionsStandardPanel("RankTab", true,
            "");
        final GroupLayout layout = createGroupLayout(panel);
        panel.setLayout(layout);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(headerPanel)
                .addComponent(rankSystemsViewport));

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(headerPanel, Alignment.CENTER)
                    .addComponent(rankSystemsViewport)));

        // Create Parent Panel and return
        return createParentPanel(panel, "RankTab");
    }
}
