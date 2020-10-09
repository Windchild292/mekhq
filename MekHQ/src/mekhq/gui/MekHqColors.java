/*
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved.
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
package mekhq.gui;

import java.awt.Color;

import javax.swing.UIManager;

import mekhq.MekHQ;
import mekhq.gui.preferences.ColorPreference;
import mekhq.preferences.PreferencesNode;

public class MekHQColors {
    //region Variable Declarations
    //region General Colours
    private static ColorPreference iconButtonColors;
    private static ColorPreference mekhqWarningColours;
    private static ColorPreference mekhqIssueColours;
    //endregion General Colours

    //region Force Colours
    private static ColorPreference deployedColors;
    private static ColorPreference belowContractMinimumColors;
    //endregion Force Colours

    //region Unit Colours
    private static ColorPreference inTransitColors;
    private static ColorPreference refittingColors;
    private static ColorPreference mothballingColors;
    private static ColorPreference mothballedColors;
    private static ColorPreference notRepairableColors;
    private static ColorPreference nonfunctionalColors;
    private static ColorPreference needsPartsFixedColors;
    private static ColorPreference unmaintainedColors;
    private static ColorPreference uncrewedColors;
    //endregion Unit Colours

    //region Financial Colours
    private static ColorPreference loanOverdueColors;
    //endregion Financial Colours

    //region Personnel Colours
    private static ColorPreference injuredColors;
    private static ColorPreference healedInjuriesColors;
    private static ColorPreference paidRetirementColors;
    //endregion Personnel Colours
    //endregion Variable Declarations

    //region Static Initialization
    static {
        final PreferencesNode preferences = MekHQ.getPreferences().forClass(MekHQColors.class);

        //region General Colours
        iconButtonColors = new ColorPreference("iconButton", Color.LIGHT_GRAY, Color.BLACK);
        mekhqWarningColours = new ColorPreference("mekhqWarning", Color.ORANGE, Color.BLACK);
        mekhqIssueColours = new ColorPreference("mekhqIssue", Color.RED, Color.BLACK);
        //endregion General Colours

        //region Force Colours
        deployedColors = new ColorPreference("deployed", Color.LIGHT_GRAY, Color.BLACK);
        belowContractMinimumColors = new ColorPreference("belowContractMinimum", UIManager.getColor("Table.background"), Color.RED);
        //endregion Force Colours

        //region Unit Colours
        inTransitColors = new ColorPreference("inTransit", Color.MAGENTA, Color.BLACK);
        refittingColors = new ColorPreference("refitting", Color.CYAN, Color.BLACK);
        mothballingColors = new ColorPreference("mothballing", new Color(153,153,255), Color.BLACK);
        mothballedColors = new ColorPreference("mothballed", new Color(204, 204, 255), Color.BLACK);
        notRepairableColors = new ColorPreference("notRepairable", new Color(190, 150, 55), Color.BLACK);
        nonfunctionalColors = new ColorPreference("nonfunctional", new Color(205, 92, 92), Color.BLACK);
        needsPartsFixedColors = new ColorPreference("needsPartsFixed", new Color(238, 238, 0), Color.BLACK);
        unmaintainedColors = new ColorPreference("unmaintainedColors", Color.ORANGE, Color.BLACK);
        uncrewedColors = new ColorPreference("uncrewed", new Color(218, 130, 255), Color.BLACK);
        //endregion Unit Colours

        //region Financial Colours
        loanOverdueColors = new ColorPreference("loanOverdue", Color.RED, Color.BLACK);
        //endregion Financial Colours

        //region Personnel Colours
        injuredColors = new ColorPreference("injured", Color.RED, Color.BLACK);
        healedInjuriesColors = new ColorPreference("healed", new Color(0xee9a00), Color.BLACK);
        paidRetirementColors = new ColorPreference("paidRetirement", Color.LIGHT_GRAY, Color.BLACK);
        //endregion Personnel Colours

        preferences.manage(
                //region General Colours
                iconButtonColors,
                mekhqWarningColours,
                mekhqIssueColours,
                //endregion General Colours

                //region Force Colours
                deployedColors,
                belowContractMinimumColors,
                //endregion Force Colours

                //region Unit Colours
                inTransitColors,
                refittingColors,
                mothballingColors,
                mothballedColors,
                notRepairableColors,
                nonfunctionalColors,
                needsPartsFixedColors,
                unmaintainedColors,
                uncrewedColors,
                //endregion Unit Colours

                //region Financial Colours
                loanOverdueColors,
                //endregion Financial Colours

                //region Personnel Colours
                injuredColors,
                healedInjuriesColors,
                paidRetirementColors
                //endregion Personnel Colours
        );
    }
    //endregion Static Initialization=

    //region Getters
    //region General Colours
    public ColorPreference getIconButton() {
        return iconButtonColors;
    }

    public ColorPreference getMekHQWarning() {
        return mekhqWarningColours;
    }

    public Color getMekHQWarningColour() {
        return getMekHQWarning().getColor().orElse(Color.RED);
    }

    public ColorPreference getMekHQIssueColour() {
        return mekhqIssueColours;
    }
    //endregion General Colours

    //region Force Colours
    public ColorPreference getDeployed() {
        return deployedColors;
    }

    public ColorPreference getBelowContractMinimum() {
        return belowContractMinimumColors;
    }
    //endregion Force Colours

    //region Unit Colours
    public ColorPreference getInTransit() {
        return inTransitColors;
    }

    public ColorPreference getRefitting() {
        return refittingColors;
    }

    public ColorPreference getMothballing() {
        return mothballingColors;
    }

    public ColorPreference getMothballed() {
        return mothballedColors;
    }

    public ColorPreference getNotRepairable() {
        return notRepairableColors;
    }

    public ColorPreference getNonFunctional() {
        return nonfunctionalColors;
    }

    public ColorPreference getNeedsPartsFixed() {
        return needsPartsFixedColors;
    }

    public ColorPreference getUnmaintained() {
        return unmaintainedColors;
    }

    public ColorPreference getUncrewed() {
        return uncrewedColors;
    }
    //endregion Unit Colours

    //region Financial Colours
    public ColorPreference getLoanOverdue() {
        return loanOverdueColors;
    }
    //endregion Financial Colours

    //region Personnel Colours
    public ColorPreference getInjured() {
        return injuredColors;
    }

    public ColorPreference getHealedInjuries() {
        return healedInjuriesColors;
    }

    public ColorPreference getPaidRetirement() {
        return paidRetirementColors;
    }
    //endregion Personnel Colours
    //endregion Getters
}
