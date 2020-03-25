/*
 * Copyright (c) 2020 - The MegaMek Team. All rights reserved.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.randomEvents;

import mekhq.MekHQ;

public class EmptyEvent extends RandomEvent {
    //region Constructors
    public EmptyEvent() {
        super();
    }
    //endregion Constructors

    //region Process
    @Override
    public void process() {
        MekHQ.getLogger().error(getClass(), "process",
                "An Empty Event was generated. This is likely the result of an error, "
                + "please open a ticket on the GitHub including your .cnpx file and this log file.");
    }
    //endregion Process
}
