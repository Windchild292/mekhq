/*
 * Copyright (c) 2020 - The MegaMek Team
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
package mekhq.gui.sorter;

import megamek.common.Entity;
import mekhq.gui.sorter.enums.WeightSorterStyle;

import java.util.Comparator;

public class WeightSorter implements Comparator<Entity> {
    private WeightSorterStyle style;

    //region Constructors
    public WeightSorter(WeightSorterStyle style) {
        this.style = style;
    }
    //endregion Constructors

    @Override
    public int compare(Entity lhs, Entity rhs) {
        int weightClass1, weightClass2;
        switch (style) {
            case WEIGHT_ASC:
                return (int) Math.round(lhs.getWeight() - rhs.getWeight());
            case WEIGHT_DESC:
                return (int) Math.round(rhs.getWeight() - lhs.getWeight());
            case WEIGHT_CLASS_ASC:
                return lhs.getWeightClass() - rhs.getWeightClass();
            case WEIGHT_CLASS_DESC:
                return rhs.getWeightClass() - lhs.getWeightClass();
            case WEIGHT_CLASS_ASC_WEIGHT_ASC:
                weightClass1 = lhs.getWeightClass();
                weightClass2 = rhs.getWeightClass();
                return (weightClass1 == weightClass2) ? (int) (lhs.getWeight() - rhs.getWeight())
                        : (weightClass1 - weightClass2);
            case WEIGHT_CLASS_ASC_WEIGHT_DESC:
                weightClass1 = lhs.getWeightClass();
                weightClass2 = rhs.getWeightClass();
                return (weightClass1 == weightClass2) ? (int) (rhs.getWeight() - lhs.getWeight())
                        : (weightClass1 - weightClass2);
            case WEIGHT_CLASS_DESC_WEIGHT_ASC:
                weightClass1 = lhs.getWeightClass();
                weightClass2 = rhs.getWeightClass();
                return (weightClass1 == weightClass2) ? (int) (lhs.getWeight() - rhs.getWeight())
                        : (weightClass2 - weightClass1);
            case WEIGHT_CLASS_DESC_WEIGHT_DESC:
            default:
                weightClass1 = lhs.getWeightClass();
                weightClass2 = rhs.getWeightClass();
                return (weightClass1 == weightClass2) ? (int) (rhs.getWeight() - lhs.getWeight())
                        : (weightClass2 - weightClass1);
        }
    }
}
