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
package mekhq.gui.baseComponents;

import mekhq.gui.enums.DialogResult;
import megamek.common.util.EncodeControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * This is the Base Dialog for a dialog with buttons in MekHQ. It extends Base Dialog, and adds a
 * button panel with base Ok and Cancel buttons. It also includes an enum tracker for the result of
 * the dialog.
 *
 * Inheriting classes must call initialize() in their constructor and override createCenterPane()
 *
 * The resources associated with this dialog need to contain at least the following keys:
 * - "Ok.text" -> text for the ok button
 * - "Ok.toolTipText" -> toolTipText for the ok button
 * - "Cancel.text" -> text for the cancel button
 * - "Cancel.toolTipText" -> toolTipText for the cancel button
 */
public abstract class AbstractButtonDialog extends AbstractDialog {
    //region Variable Declarations
    private DialogResult result;
    //endregion Variable Declarations

    //region Constructors
    protected AbstractButtonDialog(final JFrame frame, final String name, final String title) {
        this(frame, true, name, title);
    }

    protected AbstractButtonDialog(final JFrame frame, final boolean modal, final String name,
                                   final String title) {
        this(frame, modal, ResourceBundle.getBundle("mekhq.resources.GUI", new EncodeControl()), name, title);
    }

    protected AbstractButtonDialog(final JFrame frame, final ResourceBundle resources,
                                   final String name, final String title) {
        this(frame, true, resources, name, title);
    }

    protected AbstractButtonDialog(final JFrame frame, final boolean modal, final ResourceBundle resources,
                                   final String name, final String title) {
        super(frame, modal, resources, name, title);
        setResult(DialogResult.CANCELLED); // Default result is cancelled
    }
    //endregion Constructors

    //region Getters/Setters
    public DialogResult getResult() {
        return result;
    }

    public void setResult(final DialogResult result) {
        this.result = result;
    }
    //endregion Getters/Setters

    //region Initialization
    /**
     * Initializes the dialog's UI and preferences. Needs to be called by child classes for initial
     * setup.
     */
    @Override
    protected void initialize() {
        setLayout(new BorderLayout());
        add(createCenterPane(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.PAGE_END);
        finalizeInitialization();
    }

    /**
     * @return the created Button Panel
     */
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(createButton("Ok.text", "Ok.toolTipText", "okButton", this::okButtonActionPerformed));
        panel.add(createButton("Cancel.text", "Cancel.toolTipText", "cancelButton", this::cancelActionPerformed));
        return panel;
    }

    /**
     * This creates a standard button for use in the dialog
     * @param text the text resource string
     * @param toolTipText the toolTipText resource string
     * @param name the name of the button
     * @param actionListener the {@link ActionListener} to assign to the button
     * @return the created button
     */
    protected JButton createButton(final String text, final String toolTipText, final String name,
                                   final ActionListener actionListener) {
        JButton button = new JButton(resources.getString(text));
        button.setToolTipText(resources.getString(toolTipText));
        button.setName(name);
        button.addActionListener(actionListener);
        return button;
    }
    //endregion Initialization

    //region Button Actions
    /**
     * This is the default Action Event Listener for the Ok Button's action. This triggers the Ok Action,
     * sets the result to confirmed, and then sets the dialog so that it is no longer visible.
     * @param evt the event triggering this
     */
    protected void okButtonActionPerformed(final ActionEvent evt) {
        okAction();
        setResult(DialogResult.CONFIRMED);
        setVisible(false);
    }

    /**
     * Action performed when the Ok button is clicked.
     */
    protected void okAction() {

    }
    //endregion Button Actions

    /**
     * Sets the dialog to be visible, before returning the result
     * @return the result of showing the dialog
     */
    public DialogResult showDialog() {
        setVisible(true);
        return getResult();
    }
}
