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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui.sorter.enums;

/**
 * This is used to select how the WeightSorter will sort two Entities
 */
public enum WeightSorterStyle {
    /**
     * This sorts by Weight ascending without sorting by Weight Class
     */
    WEIGHT_ASC,
    /**
     * This sorts by Weight descending without sorting by Weight Class
     */
    WEIGHT_DESC,
    /**
     * The list is sorted by Weight Class ascending without sorting by Weight
     */
    WEIGHT_CLASS_ASC,
    /**
     * This sorts by Weight Class descending without sorting by Weight
     */
    WEIGHT_CLASS_DESC,
    /**
     * The list is sorted by Weight Class ascending, then by Weight ascending
     */
    WEIGHT_CLASS_ASC_WEIGHT_ASC,
    /**
     * The list is sorted by Weight Class ascending, then by Weight descending
     */
    WEIGHT_CLASS_ASC_WEIGHT_DESC,
    /**
     * This sorts by Weight Class descending, then by Weight Ascending
     */
    WEIGHT_CLASS_DESC_WEIGHT_ASC,
    /**
     * This sorts by Weight Class descending, then by Weight descending
     */
    WEIGHT_CLASS_DESC_WEIGHT_DESC,
    /**
     * This does not sort the entities based on their Weight nor Weight Class
     */
    NONE
}
