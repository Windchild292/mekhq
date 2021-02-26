/*
 * Copyright (c) 2019-2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.preferences;

import mekhq.preferences.PreferenceElement;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.lang.ref.WeakReference;

public class JTextFieldPreference extends PreferenceElement implements DocumentListener {
    //region Variable Declarations
    private final WeakReference<JTextField> weakReference;
    private String text;
    //endregion Variable Declarations

    //region Constructors
    public JTextFieldPreference(final JTextField textField) {
        super(textField.getName());
        setText(textField.getText());
        weakReference = new WeakReference<>(textField);
        textField.getDocument().addDocumentListener(this);
    }
    //endregion Constructors

    //region Getters/Setters
    public WeakReference<JTextField> getWeakReference() {
        return weakReference;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }
    //endregion Getters/Setters

    //region PreferenceElement
    @Override
    protected String getValue() {
        return getText();
    }

    @Override
    protected void initialize(final String value) {
        final JTextField element = getWeakReference().get();
        if (element != null) {
            element.setText(value);
        }
    }

    @Override
    protected void dispose() {
        final JTextField element = getWeakReference().get();
        if (element != null) {
            element.getDocument().removeDocumentListener(this);
            getWeakReference().clear();
        }
    }
    //endregion PreferenceElement

    //region DocumentListener
    @Override
    public void insertUpdate(final DocumentEvent evt) {
        final JTextField element = getWeakReference().get();
        if (element != null) {
            setText(element.getText());
        }
    }

    @Override
    public void removeUpdate(final DocumentEvent evt) {
        final JTextField element = getWeakReference().get();
        if (element != null) {
            setText(element.getText());
        }
    }

    @Override
    public void changedUpdate(final DocumentEvent evt) {
        final JTextField element = getWeakReference().get();
        if (element != null) {
            setText(element.getText());
        }
    }
    //endregion DocumentListener
}
