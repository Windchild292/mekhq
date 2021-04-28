package mekhq.gui.dialog;

import megamek.client.ui.preferences.JWindowPreference;
import megamek.client.ui.preferences.PreferencesNode;
import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.campaign.Kill;
import mekhq.campaign.personnel.Person;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

@Deprecated
public class AddOrEditKillEntryDialog extends JDialog {
    public AddOrEditKillEntryDialog(JFrame parent, boolean modal, Person slayer, String killerUnit, LocalDate entryDate) {
        this(parent, modal, ADD_OPERATION, new Kill(slayer, "?", killerUnit, entryDate));
    }

    public AddOrEditKillEntryDialog(JFrame parent, boolean modal, Kill kill) {
        this(parent, modal, EDIT_OPERATION, kill);
    }

    private AddOrEditKillEntryDialog(JFrame parent, boolean modal, int operationType, Kill kill) {
        super(parent, modal);

        assert kill != null;

        this.frame = parent;
        this.kill = kill;
        this.date = kill.getDate();
        this.operationType = operationType;
        initComponents();
        setLocationRelativeTo(parent);
        setUserPreferences();
    }

    public Optional<Kill> getKill() {
        return Optional.ofNullable(kill);
    }

    private void initComponents() {
        ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.AddOrEditKillEntryDialog", new EncodeControl());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N
        if (this.operationType == ADD_OPERATION) {
            setTitle(resourceMap.getString("dialogAdd.title"));
        } else {
            setTitle(resourceMap.getString("dialogEdit.title"));
        }
        getContentPane().setLayout(new GridBagLayout());

        lblKill.setText(resourceMap.getString("lblKill.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(lblKill, gridBagConstraints);

        txtKill.setText(kill.getWhatKilled());
        txtKill.setMinimumSize(new Dimension(150, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(txtKill, gridBagConstraints);

        lblKiller.setText(resourceMap.getString("lblKiller.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(lblKiller, gridBagConstraints);

        txtKiller.setText(kill.getKilledByWhat());
        txtKiller.setMinimumSize(new Dimension(150, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(txtKiller, gridBagConstraints);

        btnDate.setText(MekHQ.getMekHQOptions().getDisplayFormattedDate(date));
        btnDate.setName("btnDate");
        btnDate.addActionListener(evt -> changeDate());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        getContentPane().add(btnDate, gridBagConstraints);

        btnOK.setText(resourceMap.getString("btnOK.text"));
        btnOK.setName("btnOK");
        btnOK.addActionListener(this::btnOKActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(btnOK, gridBagConstraints);

        btnClose.setText(resourceMap.getString("btnClose.text"));
        btnClose.setName("btnClose");
        btnClose.addActionListener(this::btnCloseActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(btnClose, gridBagConstraints);

        pack();
    }

    private void btnOKActionPerformed(ActionEvent evt) {
        kill.setWhatKilled(txtKill.getText());
        kill.setKilledByWhat(txtKiller.getText());
        kill.setDate(date);
        this.setVisible(false);
    }

    private void btnCloseActionPerformed(ActionEvent evt) {
        kill = null;
        this.setVisible(false);
    }

    private void changeDate() {
        DateChooser dc = new DateChooser(frame, date);
        if (dc.showDateChooser() == DateChooser.OK_OPTION) {
            date = dc.getDate();
            btnDate.setText(MekHQ.getMekHQOptions().getDisplayFormattedDate(date));
        }
    }
}
