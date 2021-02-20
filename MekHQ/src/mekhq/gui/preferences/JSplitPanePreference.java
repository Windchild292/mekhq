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
package mekhq.gui.preferences;

import mekhq.preferences.PreferenceElement;

import javax.swing.*;
import java.lang.ref.WeakReference;

public class JSplitPanePreference extends PreferenceElement {
    //region Variable Declarations
    private final WeakReference<JSplitPane> weakReference;
    private int dividerLocation;
    //endregion Variable Declarations

    //region Constructors
    public JSplitPanePreference(final JSplitPane splitPane) {
        super(splitPane.getName());
        setDividerLocation(splitPane.getDividerLocation());
        weakReference = new WeakReference<>(splitPane);
    }
    //endregion Constructors

    //region Getters/Setters
    public WeakReference<JSplitPane> getWeakReference() {
        return weakReference;
    }

    public int getDividerLocation() {
        return dividerLocation;
    }

    public void setDividerLocation(final int dividerLocation) {
        this.dividerLocation = dividerLocation;
    }
    //endregion Getters/Setters

    @Override
    protected String getValue() {
        return Integer.toString(getDividerLocation());
    }

    @Override
    protected void initialize(final String value) {
        assert (value != null) && !value.trim().isEmpty();

        final JSplitPane element = getWeakReference().get();
        if (element != null) {
            setDividerLocation(Integer.parseInt(value));
            element.setDividerLocation(getDividerLocation());
        }
    }

    @Override
    protected void dispose() {
        final JSplitPane element = getWeakReference().get();
        if (element != null) {
            // TODO : Remove any listeners required
            getWeakReference().clear();
        }
    }
}
