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

import mekhq.MekHqConstants;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Optional;

/**
 * This is the base Popup Menu Adapter.
 * Provides a popup menu adapter for a component which also ensures that the accessibility chord
 * SHIFT+F10 opens the popup as well.
 */
public abstract class AbstractPopupMenuAdapter extends MouseInputAdapter implements ActionListener, Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = -5554688878860465002L;
    //endregion Variable Declarations

    //region Constructors
    private AbstractPopupMenuAdapter() {
        // This should never be initialized, rather one should call connect.
    }
    //endregion Constructors

    //region Initialization
    /**
     * Connect the popup menu adapter to the component. Implementations should call this to connect
     * the popup menu to both right click and the SHIFT+F10 accessibility chord.
     * @param component The component to trap context menu actions.
     */
    protected void connect(final JComponent component) {
        component.addMouseListener(this);

        // Setup SHIFT+F10 for context menu support
        final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_DOWN_MASK);
        component.getInputMap(JComponent.WHEN_FOCUSED).put(keystroke, MekHqConstants.COMMAND_OPEN_POPUP);
        component.getActionMap().put(MekHqConstants.COMMAND_OPEN_POPUP, new AbstractAction() {
            private static final long serialVersionUID = -3125065077717163143L;

            @Override
            public void actionPerformed(final ActionEvent evt) {
                createPopupMenu().ifPresent(popup -> popup.show(component, component.getX(), component.getY()));
            }
        });
    }

    /**
     * A {@link JPopupMenu} to show, if applicable.
     * @return An optional {@link JPopupMenu} to show.
     */
    protected abstract Optional<JPopupMenu> createPopupMenu();
    //endregion Initialization

    private void maybeShowPopup(final MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            createPopupMenu().ifPresent(popup -> popup.show(evt.getComponent(), evt.getX(), evt.getY()));
        }
    }

    //region ActionListener
    @Override
    public void actionPerformed(final ActionEvent evt) {
        // Do nothing
    }
    //endregion ActionListener

    //region MouseListener
    @Override
    public void mousePressed(final MouseEvent evt) {
        // Implement mousePressed per: https://docs.oracle.com/javase/tutorial/uiswing/components/menu.html#popup
        maybeShowPopup(evt);
    }

    @Override
    public void mouseReleased(final MouseEvent evt) {
        // Implement mouseReleased per: https://docs.oracle.com/javase/tutorial/uiswing/components/menu.html#popup
        maybeShowPopup(evt);
    }
    //endregion MouseListener
}
