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
package mekhq.gui.dialog.reportDialogs;

import mekhq.MHQConstants;
import mekhq.campaign.Campaign;

import javax.swing.*;
import java.awt.*;

public class UnitRatingReportDialog extends AbstractReportDialog {
    //region Variable Declarations
    private final Campaign campaign;
    //endregion Variable Declarations

    //region Constructors
    public UnitRatingReportDialog(final JFrame frame, final Campaign campaign) {
        super(frame, "UnitRatingReportDialog", "UnitRatingReportDialog.title");
        this.campaign = campaign;
        initialize();
    }
    //endregion Constructors

    //region Getters
    public Campaign getCampaign() {
        return campaign;
    }

    @Override
    protected JTextPane createTxtReport() {
        final JTextPane txtReport = new JTextPane();
        txtReport.setText(getCampaign().getUnitRating().getDetails());
        txtReport.setName("txtReport");
        txtReport.setFont(new Font(MHQConstants.FONT_COURIER_NEW, Font.PLAIN, 12));
        txtReport.setEditable(false);
        txtReport.setCaretPosition(0);
        return txtReport;
    }
    //endregion Getters
}
