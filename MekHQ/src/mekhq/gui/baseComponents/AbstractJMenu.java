package mekhq.gui.baseComponents;

import megamek.client.ui.swing.util.MenuScroller;
import mekhq.MekHqConstants;

import javax.swing.*;

public class AbstractJMenu extends JMenu {
    //region Constructors
    public AbstractJMenu() {
        super();
    }

    public AbstractJMenu(final String text) {
        super(text);
    }

    public AbstractJMenu(final Action action) {
        super(action);
    }

    public AbstractJMenu(final String text, final boolean b) {
        super(text, b);
    }
    //endregion Constructors

    /**
     * This is used to add a JMenu to this, provided the former isn't empty, and then add a scroller
     * to the former if it is above the default minimum threshold
     * @param child the JMenu to add
     */
    public void add(JMenu child) {
        add(child, MekHqConstants.BASE_SCROLLER_THRESHOLD);
    }

    /**
     * This is used to add a JMenu to this, provided the former isn't empty, and then add a scroller
     * to the former if it is above the minimum threshold
     * @param child the JMenu to add
     * @param scrollerThreshold the threshold for adding a scroller
     */
    public void add(final JMenu child, final int scrollerThreshold) {
        if (child.getItemCount() > 0) {
            super.add(child);
            if (child.getItemCount() > scrollerThreshold) {
                MenuScroller.setScrollerFor(child, scrollerThreshold);
            }
        }
    }
}
