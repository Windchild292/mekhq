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

public class JIntNumberSpinnerPreference extends PreferenceElement implements ChangeListener {
    //region Variable Declarations
    private final WeakReference<JSpinner> weakReference;
    private int intValue;
    //endregion Variable Declarations

    //region Constructors
    public JIntNumberSpinnerPreference(final JSpinner spinner) {
        super(spinner.getName());
        assert spinner.getModel() instanceof SpinnerNumberModel;
        setIntValue((Integer) spinner.getValue());
        weakReference = new WeakReference<>(spinner);
        spinner.addChangeListener(this);
    }
    //endregion Constructors

    //region Getters/Setters
    public WeakReference<JSpinner> getWeakReference() {
        return weakReference;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(final int intValue) {
        this.intValue = intValue;
    }
    //endregion Getters/Setters

    //region PreferenceElement
    @Override
    protected String getValue() {
        return Integer.toString(getIntValue());
    }

    @Override
    protected void initialize(final String value) {
        // TODO : Java 11 : Swap to isBlank
        assert (value != null) && !value.trim().isEmpty();

        final JSpinner element = getWeakReference().get();
        if (element != null) {
            final int newValue = Integer.parseInt(value);
            final SpinnerNumberModel model = (SpinnerNumberModel) element.getModel();
            if (((Integer) model.getMinimum() <= newValue) && ((Integer) model.getMaximum() >= newValue)) {
                setIntValue(newValue);
                element.setValue(getIntValue());
            }
        }
    }

    @Override
    protected void dispose() {
        final JSpinner element = getWeakReference().get();
        if (element != null) {
            element.removeChangeListener(this);
            getWeakReference().clear();
        }
    }
    //endregion PreferenceElement

    //region ChangeListener
    @Override
    public void stateChanged(final ChangeEvent evt) {
        final JSpinner element = getWeakReference().get();
        if (element != null) {
            setIntValue((Integer) element.getValue());
        }
    }
    //endregion ChangeListener
}
