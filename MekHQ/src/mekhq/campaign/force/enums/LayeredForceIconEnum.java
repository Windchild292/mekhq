/*
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved
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
package mekhq.campaign.force.enums;

import megamek.common.util.EncodeControl;

import javax.swing.*;
import java.util.List;
import java.util.ResourceBundle;

public enum LayeredForceIconEnum {
    //region Enum Declarations
    TYPE("LayeredForceIconEnum.TYPE.text", "Pieces/Type/", "tableTypes", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION),
    FORMATION("LayeredForceIconEnum.FORMATION.text", "Pieces/Formations/", "tableFormations", ListSelectionModel.SINGLE_SELECTION),
    ADJUSTMENT("LayeredForceIconEnum.ADJUSTMENT.text", "Pieces/Adjustments/", "tableAdjustments", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION),
    ALPHANUMERIC("LayeredForceIconEnum.ALPHANUMERIC.text", "Pieces/Alphanumerics/", "tableAlphanumerics", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION),
    SPECIAL_MODIFIER("LayeredForceIconEnum.SPECIAL_MODIFIER.text", "Pieces/Special Modifiers/", "tableSpecialModifiers", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION),
    BACKGROUND("LayeredForceIconEnum.BACKGROUND.text", "Pieces/Backgrounds/", "tableBackgrounds", ListSelectionModel.SINGLE_SELECTION),
    FRAME("LayeredForceIconEnum.FRAME.text", "Pieces/Frames/", "tableFrames", ListSelectionModel.SINGLE_SELECTION),
    LOGO("LayeredForceIconEnum.LOGO.text", "Pieces/Logos/", "tableLogos", ListSelectionModel.SINGLE_SELECTION);
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name; // The name of the tab
    private final String layerPath; // The String containing the individual layer's path
    private final String tableName; // The String used in JTable::setName for accessibility purposes
    private final int listSelectionModel; // The int used to determine how the selection
    //endregion Variable Declarations

    LayeredForceIconEnum(String name, String layerPath, String tableName, int listSelectionModel) {
        this.name = ResourceBundle.getBundle("mekhq.resources.GUIEnums", new EncodeControl())
                .getString(name);
        this.layerPath = layerPath;
        this.tableName = tableName;
        this.listSelectionModel = listSelectionModel;
    }

    public String getLayerPath() {
        return layerPath;
    }

    public String getTableName() {
        return tableName;
    }

    public int getListSelectionModel() {
        return listSelectionModel;
    }

    /**
     * @return the layered force icon enum values in the order they are drawn in
     */
    public static List<LayeredForceIconEnum> getInDrawOrder() {
        return List.of(BACKGROUND, FRAME, TYPE, FORMATION, ADJUSTMENT, ALPHANUMERIC, SPECIAL_MODIFIER, LOGO);
    }

    @Override
    public String toString() {
        return name;
    }
}
