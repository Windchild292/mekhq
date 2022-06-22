package mekhq.gui.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;

import java.util.ResourceBundle;

/**
 *
 * I did some noodling on the personnel view panel (the core of the issue in question), and... I think this design would provide all of the information it does now in a far easier to read and use setup without reducing the available data.

 * 1. Convert to tabbed pane setup, using the following tabs:
 * 1.1. Summary
 * 1.2. Family (Optional - Campaign Options)
 * 1.3. Log Types (Under Separate Tabs)
 * 1.4. Kills (Optional - Requires one kill)
 * 1.5. Assigned Unit Tab (Optional - Setup Required)
 * 1.5.1 This requires a combat role and an assigned unit
 * 1.5.2. This should be expanded as unit tab assignments are improved and standardized between MHQ and the rest of the suite.
 *
 * TODO : Kills needs to be migrated to a proper setup recorded under the actual people instead of
 * TODO : being a setup under the campaign. This is a 'fun' swapover because of relationships it
 * TODO : currently has (expanded on below).
 *
 * I'd first modernize kills, then handle it better by converting scenarios into an optional
 * scenario link using a flag with a name like "Historical", and moving kills to not relying on a
 * scenario link). There's also a number of bugs because of issues in the current setup.
 */
public class PersonnelPaneTab {
    //region Enum Declarations
    RANK("PersonnelPaneTab.RANK.text", true),
    FIRST_NAME("PersonnelPaneTab.FIRST_NAME.text");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final String toolTipText;
    private final boolean implemented;
    //endregion Variable Declarations

    //region Constructors
    PersonnelPaneTab(final String name, final String toolTipText) {
        this(name, toolTipText, false);
    }

    PersonnelPaneTab(final String name, final String toolTipText, final boolean implemented) {
        final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.GUI",
                MekHQ.getMHQOptions().getLocale(), new EncodeControl());
        this.name = resources.getString(name);
        this.toolTipText = resources.getString(toolTipText);
        this.implemented = implemented;
    }
    //endregion Constructors

    //region Getters
    public String getToolTipText() {
        return toolTipText;
    }

    public boolean isImplemented() {
        return implemented;
    }
    //endregion Getters

    //region Boolean Comparison Methods
    public boolean is() {
        return this == GRAPHIC;
    }
    //endregion Boolean Comparison Methods

    @Override
    public String toString() {
        return name;
    }
}
