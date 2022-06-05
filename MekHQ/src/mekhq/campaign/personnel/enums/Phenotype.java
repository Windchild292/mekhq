/*
 * Copyright (c) 2020-2022 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public enum Phenotype {
    //region Enum Declarations
    // External Phenotypes
    MECHWARRIOR("Phenotype.MECHWARRIOR.text", "TRUEBORN.text", "Phenotype.MECHWARRIOR.text", "Phenotype.MECHWARRIOR.toolTipText"),
    ELEMENTAL("Phenotype.ELEMENTAL.text", "TRUEBORN.text", "Phenotype.ELEMENTAL.text", "Phenotype.ELEMENTAL.toolTipText"),
    AEROSPACE("Phenotype.AEROSPACE.text", "TRUEBORN.text", "Phenotype.AEROSPACE.groupingNameText", "Phenotype.AEROSPACE.toolTipText"),
    VEHICLE("Phenotype.VEHICLE.text", "TRUEBORN.text", "Phenotype.VEHICLE.groupingNameText", "Phenotype.VEHICLE.toolTipText"),
    PROTOMECH("Phenotype.PROTOMECH.text", "TRUEBORN.text", "Phenotype.PROTOMECH.groupingNameText", "Phenotype.PROTOMECH.toolTipText"),
    NAVAL("Phenotype.NAVAL.text", "TRUEBORN.text", "Phenotype.NAVAL.groupingNameText", "Phenotype.NAVAL.toolTipText"),
    // Internal Phenotypes
    NONE("Phenotype.NONE.text", "Phenotype.FREEBORN", "Phenotype.NONE.text",  "Phenotype.NONE.toolTipText" , false),
    GENERAL("Phenotype.GENERAL.text", "Phenotype.TRUEBORN","Phenotype.GENERAL.text", "Phenotype.GENERAL.toolTipText", false);
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final String shortName;
    private final String groupingName;
    private final String toolTipText;
    private final boolean external;
    //endregion Variable Declarations

    //region Constructors
    Phenotype(final String name, final String shortName, final String groupingName,
              final String toolTipText) {
        this(name, shortName, groupingName, toolTipText, true);
    }
    Phenotype(final String name, final String shortName, final String groupingName,
              final String toolTipText, final boolean external) {
        final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel",
                MekHQ.getMHQOptions().getLocale(), new EncodeControl());
        this.name = resources.getString(name);
        this.shortName = resources.getString(shortName);
        this.groupingName = resources.getString(groupingName);
        this.toolTipText = resources.getString(toolTipText);
        this.external = external;
    }
    //endregion Constructors

    //region Getters
    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getGroupingName() {
        return groupingName;
    }

    public String getToolTipText() {
        return toolTipText;
    }

    public boolean isExternal() {
        return external;
    }
    //endregion Getters

    //region Boolean Comparison Methods
    public boolean isMechWarrior() {
        return this == MECHWARRIOR;
    }

    public boolean isElemental() {
        return this == ELEMENTAL;
    }

    public boolean isAerospace() {
        return this == AEROSPACE;
    }

    public boolean isVehicle() {
        return this == VEHICLE;
    }

    public boolean isProtoMech() {
        return this == PROTOMECH;
    }

    public boolean isNaval() {
        return this == NAVAL;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public boolean isGeneral() {
        return this == GENERAL;
    }
    //endregion Boolean Comparison Methods

    public static List<Phenotype> getExternalPhenotypes() {
        return Arrays.stream(values())
                .filter(Phenotype::isExternal)
                .collect(Collectors.toList());
    }

    //region File I/O
    public static Phenotype parseFromString(final String text) {
        try {
            return valueOf(text);
        } catch (Exception ignored) {

        }

        try {
            switch (Integer.parseInt(text)) {
                case 1:
                    return MECHWARRIOR;
                case 2:
                    return ELEMENTAL;
                case 3:
                    return AEROSPACE;
                case 4:
                    return VEHICLE;
                case 0:
                default:
                    return NONE;
            }
        } catch (Exception ignored) {

        }

        LogManager.getLogger().error("Unable to parse the phenotype from string " + text
                + ". Returning NONE");

        return NONE;
    }
    //endregion File I/O

    @Override
    public String toString() {
        return (isNone() || isGeneral()) ? getShortName() : getShortName() + ' ' + getGroupingName();
    }
}
