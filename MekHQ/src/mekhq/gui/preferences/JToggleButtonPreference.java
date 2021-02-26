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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.lang.ref.WeakReference;

public class JToggleButtonPreference extends PreferenceElement implements ChangeListener {
    //region Variable Declarations
    private final WeakReference<JToggleButton> weakReference;
    private boolean selected;
    //endregion Variable Declarations

    //region Constructors
    public JToggleButtonPreference(final JToggleButton toggleButton) {
        super(toggleButton.getName());
        setSelected(toggleButton.isSelected());
        weakReference = new WeakReference<>(toggleButton);
        toggleButton.addChangeListener(this);
    }
    //endregion Constructors

    //region Getters/Setters
    public WeakReference<JToggleButton> getWeakReference() {
        return weakReference;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }
    //endregion Getters/Setters

    //region PreferenceElement
    @Override
    protected String getValue() {
        return Boolean.toString(isSelected());
    }

    @Override
    protected void initialize(final String value) {
        // TODO : Java 11 : Swap to isBlank
        assert (value != null) && !value.trim().isEmpty();

        final JToggleButton element = getWeakReference().get();
        if (element != null) {
            setSelected(Boolean.parseBoolean(value));
            element.setSelected(isSelected());
        }
    }

    @Override
    protected void dispose() {
        final JToggleButton element = getWeakReference().get();
        if (element != null) {
            element.removeChangeListener(this);
            getWeakReference().clear();
        }
    }
    //endregion PreferenceElement

    //region ChangeListener
    @Override
    public void stateChanged(final ChangeEvent evt) {
        final JToggleButton element = getWeakReference().get();
        if (element != null) {
            setSelected(element.isSelected());
        }
    }
    //endregion ChangeListener
}
