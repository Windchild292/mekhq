/*
 * Copyright (c) 2019-2020 - The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.view;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * This is an extension of JPanel that implements scrollable so that all of our ViewPanel objects
 * play nicely with the scroll panes that they are embedded within
 * @author aarong
 */
public class ScrollablePanel extends JPanel implements Scrollable {
    //region Variable Declarations
    private static final long serialVersionUID = -1422419969984249050L;

    //by default, track the width, and re-size as needed.
    private boolean trackViewportWidth = true;
    //endregion Variable Declarations

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        // tell the JScrollPane that we want to be our 'preferredSize'
        // but later, we'll say that vertically, it should scroll.
        return super.getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    public void setScrollableTracksViewportWidth(boolean value) {
        trackViewportWidth = false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return trackViewportWidth;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false; //we don't want to track the height, because we want to scroll vertically.
    }
}
