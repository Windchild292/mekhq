/*
 * Copyright (C) 2009-2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.panes;

import mekhq.campaign.Campaign;
import mekhq.campaign.JumpPath;
import mekhq.campaign.universe.PlanetarySystem;
import mekhq.gui.baseComponents.AbstractMHQScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * A custom panel that gets filled in with goodies from a JumpPath record
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class JumpPathPane extends AbstractMHQScrollPane {
    //region Variable Declarations
    private static final long serialVersionUID = 7004741688464105277L;

    private final Campaign campaign;
    private final JumpPath path;
    //endregion Variable Declarations

    //region Constructors
    public JumpPathPane(final JFrame frame, final Campaign campaign, final JumpPath path) {
        super(frame, "JumpPathPane");
        this.campaign = campaign;
        this.path = path;
        initialize();
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public JumpPath getPath() {
        return path;
    }
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected void initialize() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setName("jumpPathPanel");

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(createSummaryPanel(), gbc);

        gbc.gridy++;
        panel.add(createPathPanel(), gbc);

        setViewportView(panel);
        setPreferences();
    }

    private JPanel createSummaryPanel() {
        final JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.setName("summaryPanel");

        final JLabel lblJumps1 = new JLabel(resources.getString("lblJumps1.text"));
        lblJumps1.setName("lblJumps1");
        panel.add(lblJumps1);

        final JLabel lblJumps2 = new JLabel("<html>" + path.getJumps() + " jumps" + "</html>");
        lblJumps2.setName("lblJumps2");
        panel.add(lblJumps2);

        final JLabel lblTimeStart1 = new JLabel(resources.getString("lblTimeStart1.text"));
        lblTimeStart1.setName("lblTimeStart1");
        panel.add(lblTimeStart1);

        final JLabel lblTimeStart2 = new JLabel(String.format("<html>%.2f days from %s to jump point.</html>",
                Math.round(getPath().getStartTime(getCampaign().getLocation().getTransitTime()) * 100.0) / 100.0,
                ((path.getFirstSystem() == null) ? "?" : path.getFirstSystem().getPrintableName(campaign.getLocalDate()))));
        lblTimeStart2.setName("lblTimeStart2");
        panel.add(lblTimeStart2);

        final JLabel lblTimeEnd1 = new JLabel(resources.getString("lblTimeEnd1.text"));
        lblTimeEnd1.setName("lblTimeEnd1");
        panel.add(lblTimeEnd1);

        final JLabel lblTimeEnd2 = new JLabel(String.format("<html>%.2f days from final jump point to %s.</html>",
                Math.round(path.getEndTime() * 100.0) / 100.0,
                ((path.getLastSystem() == null) ? "?" : path.getLastSystem().getPrintableName(campaign.getLocalDate()))));
        lblTimeEnd2.setName("lblTimeEnd2");
        panel.add(lblTimeEnd2);

        final JLabel lblRechargeTime1 = new JLabel(resources.getString("lblRechargeTime1.text"));
        lblRechargeTime1.setName("lblRechargeTime1");
        panel.add(lblRechargeTime1);

        final JLabel lblRechargeTime2 = new JLabel(String.format("<html>%.2f days</html>",
                Math.round(path.getTotalRechargeTime(campaign.getLocalDate()) * 100.0) / 100.0));
        lblRechargeTime2.setName("lblRechargeTime2");
        panel.add(lblRechargeTime2);

        final JLabel lblTotalTime1 = new JLabel(resources.getString("lblTotalTime1.text"));
        lblTotalTime1.setName("lblTotalTime1");
        panel.add(lblTotalTime1);

        final JLabel lblTotalTime2 = new JLabel(String.format("<html>%.2f days.</html>",
                Math.round(path.getTotalTime(campaign.getLocalDate(), campaign.getLocation().getTransitTime()) * 100.0) / 100.0));
        lblTotalTime2.setName("lblTotalTime2");
        panel.add(lblTotalTime2);

        if (campaign.getCampaignOptions().payForTransport()) {
            final JLabel lblCost = new JLabel();
            lblCost.setName("lblCost1");
            lblCost.setText(resources.getString("lblCost1.text"));
            panel.add(lblCost);

            final JLabel lblCost2 = new JLabel(String.format("<html>%s</html>", campaign
                    .calculateCostPerJump(true, campaign.getCampaignOptions().useEquipmentContractBase())
                    .multipliedBy(path.getJumps()).toAmountAndSymbolString()));
            lblCost2.setName("lblCost2");
            panel.add(lblCost2);
        }

        return panel;
    }

    private JPanel createPathPanel() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setName("pathPanel");

        for (final PlanetarySystem system : getPath().getSystems()) {
            final JLabel lblPlanet = new JLabel(String.format("%s (%s)",
                    system.getPrintableName(getCampaign().getLocalDate()),
                    system.getRechargeTimeText(getCampaign().getLocalDate())));
            lblPlanet.setName("lblPlanet");
            panel.add(lblPlanet);
        }

        return panel;
    }
    //endregion Initialization
}
