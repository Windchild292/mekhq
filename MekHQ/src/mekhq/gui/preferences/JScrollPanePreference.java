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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

public class JScrollPanePreference extends PreferenceElement implements PropertyChangeListener {
    //region Variable Declarations
    private final WeakReference<JScrollPane> weakReference;
    private int horizontalValue;
    private int horizontalMaximum;
    private int verticalValue;
    private int verticalMaximum;
    //endregion Variable Declarations

    //region Constructors
    public JScrollPanePreference(final JScrollPane scrollPane) {
        super(scrollPane.getName());
        setHorizontalValue((scrollPane.getHorizontalScrollBarPolicy() == JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
                ? 0 : scrollPane.getHorizontalScrollBar().getValue());
        setHorizontalMaximum(scrollPane.getHorizontalScrollBar().getMaximum());
        setVerticalValue((scrollPane.getVerticalScrollBarPolicy() == JScrollPane.VERTICAL_SCROLLBAR_NEVER)
                ? 0 : scrollPane.getVerticalScrollBar().getValue());
        setVerticalMaximum(scrollPane.getVerticalScrollBar().getMaximum());
        weakReference = new WeakReference<>(scrollPane);
        scrollPane.addPropertyChangeListener(this);
    }
    //endregion Constructors

    //region Getters/Setters
    public WeakReference<JScrollPane> getWeakReference() {
        return weakReference;
    }

    public int getHorizontalValue() {
        return horizontalValue;
    }

    public void setHorizontalValue(final int horizontalValue) {
        this.horizontalValue = horizontalValue;
    }

    public int getHorizontalMaximum() {
        return horizontalMaximum;
    }

    public void setHorizontalMaximum(int horizontalMaximum) {
        this.horizontalMaximum = horizontalMaximum;
    }

    public int getVerticalValue() {
        return verticalValue;
    }

    public void setVerticalValue(final int verticalValue) {
        this.verticalValue = verticalValue;
    }

    public int getVerticalMaximum() {
        return verticalMaximum;
    }

    public void setVerticalMaximum(int verticalMaximum) {
        this.verticalMaximum = verticalMaximum;
    }
    //endregion Getters/Setters

    //region PreferenceElement
    @Override
    protected String getValue() {
        return String.format("%d|%d|%d|%d", getHorizontalValue(), getHorizontalMaximum(), getVerticalValue(), getVerticalMaximum());
    }

    @Override
    protected void initialize(final String value) {
        // TODO : Java 11 : Swap to isBlank
        assert (value != null) && !value.trim().isEmpty();

        final JScrollPane element = getWeakReference().get();
        if (element != null) {
            final String[] parts = value.split("\\|", -1);
            setHorizontalValue(Integer.parseInt(parts[0]));
            setHorizontalMaximum(Integer.parseInt(parts[1]));
            setVerticalValue(Integer.parseInt(parts[2]));
            setVerticalMaximum(Integer.parseInt(parts[3]));

            SwingUtilities.invokeLater(() -> {
                element.getHorizontalScrollBar().setMaximum(getHorizontalMaximum());
                element.getHorizontalScrollBar().setValue(getHorizontalValue());
                element.getVerticalScrollBar().setMaximum(getVerticalMaximum());
                element.getVerticalScrollBar().setValue(getVerticalValue());
            });
        }
    }

    @Override
    protected void dispose() {
        final JScrollPane element = getWeakReference().get();
        if (element != null) {
            element.removePropertyChangeListener(this);
            getWeakReference().clear();
        }
    }
    //endregion PreferenceElement

    //region PropertyChangeListener
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final JScrollPane element = getWeakReference().get();
        if (element != null) {
            setHorizontalValue((element.getHorizontalScrollBarPolicy() == JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
                    ? 0 : element.getHorizontalScrollBar().getValue());
            setHorizontalMaximum(element.getHorizontalScrollBar().getMaximum());
            setVerticalValue((element.getVerticalScrollBarPolicy() == JScrollPane.VERTICAL_SCROLLBAR_NEVER)
                    ? 0 : element.getVerticalScrollBar().getValue());
            setVerticalMaximum(element.getVerticalScrollBar().getMaximum());
        }
    }
    //endregion PropertyChangeListener
}
