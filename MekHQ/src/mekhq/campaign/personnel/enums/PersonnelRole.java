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
package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public enum PersonnelRole {
    //region Enum Declarations
    MECHWARRIOR("PersonnelRole.MECHWARRIOR.text", KeyEvent.VK_M),
    LAM_PILOT("PersonnelRole.LAM_PILOT.text", KeyEvent.VK_UNDEFINED),
    GROUND_VEHICLE_DRIVER("PersonnelRole.GROUND_VEHICLE_DRIVER.text", KeyEvent.VK_V),
    NAVAL_VEHICLE_DRIVER("PersonnelRole.NAVAL_VEHICLE_DRIVER.text", KeyEvent.VK_N),
    VTOL_PILOT("PersonnelRole.VTOL_PILOT.text", KeyEvent.VK_UNDEFINED),
    VEHICLE_GUNNER("PersonnelRole.VEHICLE_GUNNER.text", KeyEvent.VK_G),
    VEHICLE_CREW("PersonnelRole.VEHICLE_CREW.text", KeyEvent.VK_UNDEFINED),
    AEROSPACE_PILOT("PersonnelRole.AEROSPACE_PILOT.text", KeyEvent.VK_A),
    CONVENTIONAL_AIRCRAFT_PILOT("PersonnelRole.CONVENTIONAL_AIRCRAFT_PILOT.text", KeyEvent.VK_C),
    PROTOMECH_PILOT("PersonnelRole.PROTOMECH_PILOT.text", "PersonnelRole.BATTLE_ARMOUR.clan.text", KeyEvent.VK_P),
    BATTLE_ARMOUR("PersonnelRole.BATTLE_ARMOUR.text", KeyEvent.VK_B),
    SOLDIER("PersonnelRole.SOLDIER.text", KeyEvent.VK_S),
    VESSEL_PILOT("PersonnelRole.VESSEL_PILOT.text", KeyEvent.VK_I),
    VESSEL_GUNNER("PersonnelRole.VESSEL_GUNNER.text", KeyEvent.VK_U),
    VESSEL_CREW("PersonnelRole.VESSEL_CREW.text", KeyEvent.VK_W),
    VESSEL_NAVIGATOR("PersonnelRole.VESSEL_NAVIGATOR.text", KeyEvent.VK_Y),
    MECH_TECH("PersonnelRole.MECH_TECH.text", KeyEvent.VK_T),
    MECHANIC("PersonnelRole.MECHANIC.text", KeyEvent.VK_E),
    AERO_TECH("PersonnelRole.AERO_TECH.text", KeyEvent.VK_O),
    BA_TECH("PersonnelRole.BA_TECH.text", KeyEvent.VK_UNDEFINED),
    ASTECH("PersonnelRole.ASTECH.text", KeyEvent.VK_UNDEFINED),
    DOCTOR("PersonnelRole.DOCTOR.text", KeyEvent.VK_D),
    MEDIC("PersonnelRole.MEDIC.text", KeyEvent.VK_UNDEFINED),
    ADMINISTRATOR_COMMAND("PersonnelRole.ADMINISTRATOR_COMMAND.text", KeyEvent.VK_C),
    ADMINISTRATOR_LOGISTICS("PersonnelRole.ADMINISTRATOR_LOGISTICS.text", KeyEvent.VK_L),
    ADMINISTRATOR_TRANSPORT("PersonnelRole.ADMINISTRATOR_TRANSPORT.text", KeyEvent.VK_R),
    ADMINISTRATOR_HR("PersonnelRole.ADMINISTRATOR_HR.text", KeyEvent.VK_H),

    // Anything following are ignored during Personnel Market Generation. If you add a role here you
    // MUST increase the value in getGenerationIgnoredCount by one
    DEPENDENT("PersonnelRole.DEPENDENT.text", KeyEvent.VK_UNDEFINED),
    NONE("PersonnelRole.NONE.text", KeyEvent.VK_UNDEFINED);
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    private final String clanName;
    private final int mnemonic; // Unused: J, K, Q, X, Z

    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    PersonnelRole(String name, int mnemonic) {
        this(name, null, mnemonic);
    }

    PersonnelRole(String name, String clanName, int mnemonic) {
        this.name = resources.getString(name);
        this.clanName = (clanName != null) ? resources.getString(clanName) : this.name;
        this.mnemonic = mnemonic;
    }
    //endregion Constructors

    //region Getters
    public String getName(boolean isClan) {
        return isClan ? clanName : name;
    }

    public int getMnemonic() {
        return mnemonic;
    }
    //endregion Getters

    //region Boolean Comparisons
    public boolean isMechWarrior() {
        return this == MECHWARRIOR;
    }

    public boolean isLAMPilot() {
        return this == LAM_PILOT;
    }

    public boolean isGroundVehicleDriver() {
        return this == GROUND_VEHICLE_DRIVER;
    }

    public boolean isNavalVehicleDriver() {
        return this == NAVAL_VEHICLE_DRIVER;
    }

    public boolean isVTOLPilot() {
        return this == VTOL_PILOT;
    }

    public boolean isVehicleGunner() {
        return this == VEHICLE_GUNNER;
    }

    public boolean isVehicleCrew() {
        return this == VEHICLE_CREW;
    }

    public boolean isAerospacePilot() {
        return this == AEROSPACE_PILOT;
    }

    public boolean isConventionalAircraftPilot() {
        return this == CONVENTIONAL_AIRCRAFT_PILOT;
    }

    public boolean isProtoMechPilot() {
        return this == PROTOMECH_PILOT;
    }

    public boolean isBattleArmour() {
        return this == BATTLE_ARMOUR;
    }

    public boolean isSoldier() {
        return this == SOLDIER;
    }

    public boolean isVesselPilot() {
        return this == VESSEL_PILOT;
    }

    public boolean isVesselGunner() {
        return this == VESSEL_GUNNER;
    }

    public boolean isVesselCrew() {
        return this == VESSEL_CREW;
    }

    public boolean isVesselNavigator() {
        return this == VESSEL_NAVIGATOR;
    }

    public boolean isMechTech() {
        return this == MECH_TECH;
    }

    public boolean isMechanic() {
        return this == MECHANIC;
    }

    public boolean isAeroTech() {
        return this == AERO_TECH;
    }

    public boolean isBATech() {
        return this == BA_TECH;
    }

    public boolean isAstech() {
        return this == ASTECH;
    }

    public boolean isDoctor() {
        return this == DOCTOR;
    }

    public boolean isMedic() {
        return this == MEDIC;
    }

    public boolean isAdministratorCommand() {
        return this == ADMINISTRATOR_COMMAND;
    }

    public boolean isAdministratorLogistics() {
        return this == ADMINISTRATOR_LOGISTICS;
    }

    public boolean isAdministratorTransport() {
        return this == ADMINISTRATOR_TRANSPORT;
    }

    public boolean isAdministratorHR() {
        return this == ADMINISTRATOR_HR;
    }

    public boolean isDependent() {
        return this == DEPENDENT;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public boolean isCombat() {
        switch (this) {
            case MECHWARRIOR:
            case LAM_PILOT:
            case GROUND_VEHICLE_DRIVER:
            case NAVAL_VEHICLE_DRIVER:
            case VTOL_PILOT:
            case VEHICLE_GUNNER:
            case VEHICLE_CREW:
            case AEROSPACE_PILOT:
            case CONVENTIONAL_AIRCRAFT_PILOT:
            case PROTOMECH_PILOT:
            case BATTLE_ARMOUR:
            case SOLDIER:
            case VESSEL_PILOT:
            case VESSEL_GUNNER:
            case VESSEL_CREW:
            case VESSEL_NAVIGATOR:
                return true;
            case MECH_TECH:
            case MECHANIC:
            case AERO_TECH:
            case BA_TECH:
            case ASTECH:
            case DOCTOR:
            case MEDIC:
            case ADMINISTRATOR_COMMAND:
            case ADMINISTRATOR_LOGISTICS:
            case ADMINISTRATOR_TRANSPORT:
            case ADMINISTRATOR_HR:
            case DEPENDENT:
            case NONE:
            default:
                return false;
        }
    }

    public boolean isAdministrator() {
        return isAdministratorCommand() || isAdministratorLogistics()
                || isAdministratorTransport() || isAdministratorHR();
    }

    public boolean isTech() {
        return isMechTech() || isMechanic() || isAeroTech() || isBATech() || isVesselCrew();
    }

    public boolean isTechSecondary() {
        return isMechTech() || isMechanic() || isAeroTech() || isBATech();
    }

    public boolean isMedicalStaff() {
        return isDoctor() || isMedic();
    }

    public boolean isMechWarriorGrouping() {
        return isMechWarrior() || isLAMPilot();
    }

    public boolean isVehicleCrewmember() {
        return isGroundVehicleDriver() || isNavalVehicleDriver() || isVTOLPilot()
                || isVehicleGunner() || isVehicleCrew();
    }

    public boolean isVesselCrewmember() {
        return isVesselPilot() || isVesselGunner() || isVesselCrew() || isVesselNavigator();
    }
    //endregion Boolean Comparisons

    public static List<PersonnelRole> getAdministratorRoles() {
        List<PersonnelRole> administratorRoles = new ArrayList<>();
        for (PersonnelRole role : values()) {
            if (role.isAdministrator()) {
                administratorRoles.add(role);
            }
        }
        return administratorRoles;
    }

    public static int getGenerationIgnoredCount() {
        return 2;
    }

    public static PersonnelRole parseFromString(String text) {
        return NONE;
    }

    @Override
    public String toString() {
        return name;
    }
}
