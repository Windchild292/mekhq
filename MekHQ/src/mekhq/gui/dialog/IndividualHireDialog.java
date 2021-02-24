/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.dialog;

import megamek.client.generator.RandomNameGenerator;
import megamek.client.ui.swing.dialog.imageChooser.AbstractIconChooserDialog;
import megamek.client.ui.swing.dialog.imageChooser.PortraitChooserDialog;
import megamek.common.annotations.Nullable;
import megamek.common.enums.Gender;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.ranks.Ranks;
import mekhq.gui.CampaignGUI;
import mekhq.gui.baseComponents.AbstractButtonDialog;
import mekhq.gui.preferences.JComboBoxPreference;
import mekhq.gui.preferences.JSplitPanePreference;
import mekhq.gui.view.PersonViewPanel;
import mekhq.preferences.PreferencesNode;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class IndividualHireDialog extends AbstractButtonDialog {
    //region Variable Declarations
    private CampaignGUI gui;
    private Person person;

    private JSplitPane individualHireSplitPane;

    // Left Component Components
    private JComboBox<PrimaryRole> comboPrimaryRole;
    private JComboBox<String> comboRank;

    // Right Component Components
    private JScrollPane scrollPerson;
    //endregion Variable Declarations

    //region Constructors
    public IndividualHireDialog(final JFrame frame, final CampaignGUI gui) {
        super(frame, "IndividualHireDialog", "IndividualHireDialog.title");
        setGUI(gui);
        initialize();
    }
    //endregion Constructors

    //region Getters/Setters
    public CampaignGUI getGUI() {
        return gui;
    }

    public void setGUI(final CampaignGUI gui) {
        this.gui = gui;
    }

    public @Nullable Person getPerson() {
        return person;
    }

    public void setPerson(final Person person) {
        this.person = Objects.requireNonNull(person);
    }

    public JSplitPane getIndividualHireSplitPane() {
        return individualHireSplitPane;
    }

    public void setIndividualHireSplitPane(final JSplitPane individualHireSplitPane) {
        this.individualHireSplitPane = individualHireSplitPane;
    }

    //region Left Component Components
    public JComboBox<PrimaryRole> getComboPrimaryRole() {
        return comboPrimaryRole;
    }

    private PrimaryRole getPrimaryRole() {
        return (PrimaryRole) Objects.requireNonNull(getComboPrimaryRole().getSelectedItem());
    }

    public void setComboPrimaryRole(final JComboBox<PrimaryRole> comboPrimaryRole) {
        this.comboPrimaryRole = comboPrimaryRole;
    }

    public JComboBox<String> getComboRank() {
        return comboRank;
    }

    private String getRank() {
        return (String) Objects.requireNonNull(getComboRank().getSelectedItem());
    }

    public void setComboRank(final JComboBox<String> comboRank) {
        this.comboRank = comboRank;
    }

    private void updateComboRank(final boolean usePersonProfession) {
        DefaultComboBoxModel<String> ranksModel = new DefaultComboBoxModel<>();
        int profession = usePersonProfession ? getPerson().getProfession()
                : Person.getProfessionFromPrimaryRole(getPrimaryRole().role);
        while (getGUI().getCampaign().getRanks().isEmptyProfession(profession) && (profession != Ranks.RPROF_MW)) {
            profession = getGUI().getCampaign().getRanks().getAlternateProfession(profession);
        }
        for (final String rankName : getGUI().getCampaign().getAllRankNamesFor(profession)) {
            ranksModel.addElement(rankName);
        }
        getComboRank().setModel(ranksModel);
    }
    //endregion Left Component Components

    //region Right Component Components
    public JScrollPane getScrollPerson() {
        return scrollPerson;
    }

    public void setScrollPerson(final JScrollPane scrollPerson) {
        this.scrollPerson = scrollPerson;
    }
    //endregion Right Component Components

    //endregion Getters/Setters

    //region Initialization
    @Override
    protected Container createCenterPane() {
        setIndividualHireSplitPane(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createLeftComponent(), createRightComponent()));
        getIndividualHireSplitPane().setName("individualHireSplitPane");
        return getIndividualHireSplitPane();
    }

    private Component createLeftComponent() {
        final JPanel leftPanel = new JPanel();
        leftPanel.setName("leftPanel");
        leftPanel.setLayout(new GridLayout(0, 1));

        final DefaultComboBoxModel<PrimaryRole> primaryRoleModel = new DefaultComboBoxModel<>();
        for (int i = 1; i < Person.T_NUM; i++) {
            primaryRoleModel.addElement(new PrimaryRole(i, getGUI()));
        }
        setComboPrimaryRole(new JComboBox<>(primaryRoleModel));
        getComboPrimaryRole().setToolTipText(resources.getString("comboPrimaryRole.toolTipText"));
        getComboPrimaryRole().setName("comboPrimaryRole");
        getComboPrimaryRole().addActionListener(evt -> {
            updateComboRank(getPerson() != null);
            createNewPerson();
        });
        leftPanel.add(getComboPrimaryRole());

        setComboRank(new JComboBox<>());
        getComboRank().setToolTipText(resources.getString("comboRank.toolTipText"));
        getComboRank().setName("comboRank");
        getComboRank().addActionListener(evt -> {
            getGUI().getCampaign().changeRank(getPerson(),
                    getGUI().getCampaign().getRanks().getRankNumericFromNameAndProfession(
                            getPerson().getProfession(), getRank()),
                    false);
            refreshView();
        });
        leftPanel.add(getComboRank());

        leftPanel.add(createButton("btnRandomName", evt -> {
            String factionCode = getGUI().getCampaign().getCampaignOptions().useOriginFactionForNames()
                    ? getPerson().getOriginFaction().getShortName()
                    : RandomNameGenerator.getInstance().getChosenFaction();

            String[] name = RandomNameGenerator.getInstance().generateGivenNameSurnameSplit(
                    getPerson().getGender(), getPerson().isClanner(), factionCode);
            getPerson().setGivenName(name[0]);
            getPerson().setSurname(name[1]);
            refreshView();
        }));

        leftPanel.add(createButton("btnRandomPortrait", evt -> {
            getGUI().getCampaign().assignRandomPortraitFor(getPerson());
            refreshView();
        }));

        if (getGUI().getCampaign().getCampaignOptions().randomizeOrigin()) {
            leftPanel.add(createButton("btnRandomOrigin", evt -> {
                getGUI().getCampaign().assignRandomOriginFor(getPerson());
                refreshView();
            }));
        }

        leftPanel.add(createButton("btnChoosePortrait", evt -> {
            AbstractIconChooserDialog portraitDialog = new PortraitChooserDialog(getFrame(), getPerson().getPortrait());
            if ((portraitDialog.showDialog() == JOptionPane.OK_OPTION) && (portraitDialog.getSelectedItem() != null)) {
                getPerson().setPortrait(portraitDialog.getSelectedItem());
                refreshView();
            }
        }));

        if (getGUI().getCampaign().isGM()) {
            leftPanel.add(createButton("btnEditPerson", evt -> {
                final Gender gender = person.getGender();
                CustomizePersonDialog customizePersonDialog = new CustomizePersonDialog(
                        getFrame(), true, getPerson(), getGUI().getCampaign());
                customizePersonDialog.setVisible(true);
                if (gender != getPerson().getGender()) {
                    getGUI().getCampaign().assignRandomPortraitFor(getPerson());
                }
                refreshView();
            }));

            leftPanel.add(createButton("btnRegenerate", evt -> createNewPerson()));
        }

        return leftPanel;
    }

    private Component createRightComponent() {
        setScrollPerson(new JScrollPane(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        getScrollPerson().setName("scrollPerson");
        getScrollPerson().setMinimumSize(new Dimension(450, 180));
        getScrollPerson().setPreferredSize(new Dimension(450, 180));
        return getScrollPerson();
    }

    @Override
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, getGUI().getCampaign().isGM() ? 3 : 2));
        panel.add(createButton("Hire.text", "Hire.toolTipText", "hireButton", this::okButtonActionPerformed));
        if (getGUI().getCampaign().isGM()) {
            panel.add(createButton("AddGM.text", "AddGM.toolTipText", "addGMButton", evt -> {
                if (getGUI().getCampaign().recruitPerson(getPerson(), true)) {
                    createNewPerson();
                }
            }));
        }
        panel.add(createButton("Cancel.text", "Cancel.toolTipText", "cancelButton", this::cancelActionPerformed));
        return panel;
    }

    @Override
    protected void finalizeInitialization() {
        super.finalizeInitialization();
        createNewPerson();
    }

    @Override
    protected void setCustomPreferences(final PreferencesNode preferences) {
        super.setCustomPreferences(preferences);
        preferences.manage(new JSplitPanePreference(getIndividualHireSplitPane()));
        preferences.manage(new JComboBoxPreference(getComboPrimaryRole()));
        updateComboRank(false); // this needs to occur post-primary role and pre-combo rank preferences
        preferences.manage(new JComboBoxPreference(getComboRank()));
    }
    //endregion Initialization

    private void createNewPerson() {
        setPerson(getGUI().getCampaign().newPerson(getPrimaryRole().role));
        getGUI().getCampaign().changeRank(getPerson(),
                getGUI().getCampaign().getRanks().getRankNumericFromNameAndProfession(
                        getPerson().getProfession(), getRank()),
                false);
        refreshView();
    }

    private void refreshView() {
        getScrollPerson().setViewportView(new PersonViewPanel(getPerson(), getGUI().getCampaign(), getGUI()));
    }

    @Override
    protected void okAction() {
        super.okAction();
        if (getGUI().getCampaign().recruitPerson(getPerson(), false)) {
            createNewPerson();
        }
    }

    /**
     * TODO : Windchild : PrimaryRole
     * TODO : This is a temporary class until PrimaryRole is an Enum, and is designed for easy swapover
     */
    public static class PrimaryRole {
        public int role;
        public String name;

        PrimaryRole(int role, CampaignGUI gui) {
            this.role = role;
            this.name = Person.getRoleDesc(role, gui.getCampaign().getFaction().isClan());
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
