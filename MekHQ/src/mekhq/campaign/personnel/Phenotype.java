/*
 * Copyright (c) 2020 The MegaMek Team. All rights reserved.
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
package mekhq.campaign.personnel;

public class Phenotype {
    public static final int P_NONE = 0; // No phenotype
    public static final int P_MECHWARRIOR = 1;
    public static final int P_ELEMENTAL = 2;
    public static final int P_AEROSPACE = 3;
    public static final int P_VEHICLE = 4;
    public static final int P_PROTOMECH = 5;
    public static final int P_NAVAL = 6;
    public static final int P_GENERAL = 7; // This is used during Bloodname generation, and shouldn't be saved to file
    public static final int P_NUM = 7; // This should be EQUAL to P_GENERAL

    public static String getPhenotypeName(int phenotype) {
        switch (phenotype) {
            case P_NONE:
                return "Freeborn";
            case P_MECHWARRIOR:
                return "Trueborn MechWarrior";
            case P_ELEMENTAL:
                return "Trueborn Elemental";
            case P_AEROSPACE:
                return "Trueborn Aerospace Pilot";
            case P_VEHICLE:
                return "Trueborn Vehicle Crew";
            case P_PROTOMECH:
                return "Trueborn ProtoMech Pilot";
            case P_NAVAL:
                return "Trueborn Naval Commander";
            default:
                return "?";
        }
    }

    public static String getPhenotypeShortName(int phenotype) {
        switch (phenotype) {
            case P_NONE:
                return "Freeborn";
            case P_MECHWARRIOR:
            case P_ELEMENTAL:
            case P_AEROSPACE:
            case P_VEHICLE:
            case P_PROTOMECH:
            case P_NAVAL:
                return "Trueborn";
            default:
                return "?";
        }
    }
}
