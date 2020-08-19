package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.SkillType;

import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

/**
 * The following characters are unused for Mnemonics:  J, K, Q, X, Z
*/
public enum PersonnelRole {
    // Combat Roles
    MECHWARRIOR("PersonnelRole.MECHWARRIOR.text", KeyEvent.VK_M),
    AEROSPACE_PILOT("PersonnelRole.AEROSPACE_PILOT.text", KeyEvent.VK_A),
    G_VEHICLE_DRIVER("PersonnelRole.G_VEHICLE_DRIVER.text", KeyEvent.VK_V),
    N_VEHICLE_DRIVER("PersonnelRole.N_VEHICLE_DRIVER.text", KeyEvent.VK_N),
    VEHICLE_GUNNER("PersonnelRole.VEHICLE_GUNNER.text", KeyEvent.VK_G),
    VEHICLE_CREW("PersonnelRole.VEHICLE_CREW.text", KeyEvent.VK_UNDEFINED),
    VTOL_PILOT("PersonnelRole.VTOL_PILOT.text", KeyEvent.VK_UNDEFINED),
    BA("PersonnelRole.BA.text", KeyEvent.VK_B),
    ELEMENTAL("PersonnelRole.ELEMENTAL.text", KeyEvent.VK_B),
    INFANTRY("PersonnelRole.INFANTRY.text", KeyEvent.VK_S),
    PROTOMECH_PILOT("PersonnelRole.PROTOMECH_PILOT.text", KeyEvent.VK_P),
    CONVENTIONAL_AEROSPACE_PILOT("PersonnelRole.CONVENTIONAL_AEROSPACE_PILOT.text", KeyEvent.VK_F),
    LAM_PILOT("PersonnelRole.LAM_PILOT.text", KeyEvent.VK_UNDEFINED),
    SPACECRAFT_PILOT("PersonnelRole.SPACECRAFT_PILOT.text", KeyEvent.VK_I),
    SPACECRAFT_CREW("PersonnelRole.SPACECRAFT_CREW.text", KeyEvent.VK_W),
    SPACECRAFT_GUNNER("PersonnelRole.SPACECRAFT_GUNNER.text", KeyEvent.VK_U),
    SPACECRAFT_NAVIGATOR("PersonnelRole.SPACECRAFT_NAVIGATOR.text", KeyEvent.VK_Y),

    // Support Roles
    MECH_TECH("PersonnelRole.MECH_TECH.text", KeyEvent.VK_T),
    MECHANIC("PersonnelRole.MECHANIC.text", KeyEvent.VK_E),
    AEROSPACE_TECH("PersonnelRole.AEROSPACE_TECH.text", KeyEvent.VK_O),
    BA_TECH("PersonnelRole.BA_TECH.text", KeyEvent.VK_UNDEFINED),
    ASTECH("PersonnelRole.ASTECH.text", KeyEvent.VK_UNDEFINED),
    DOCTOR("PersonnelRole.DOCTOR.text", KeyEvent.VK_D),
    MEDIC("PersonnelRole.MEDIC.text", KeyEvent.VK_UNDEFINED),
    ADMIN_COMMAND("PersonnelRole.ADMIN_COMMAND.text", KeyEvent.VK_C),
    ADMIN_LOGISTICS("PersonnelRole.ADMIN_LOGISTICS.text", KeyEvent.VK_L),
    ADMIN_TRANSPORT("PersonnelRole.ADMIN_TRANSPORT.text", KeyEvent.VK_R),
    ADMIN_HR("PersonnelRole.ADMIN_HR.text", KeyEvent.VK_H),
    DEPENDENT("PersonnelRole.DEPENDENT.text", KeyEvent.VK_UNDEFINED),
    NONE("PersonnelRole.NONE.text", KeyEvent.VK_UNDEFINED);
    //endregion Enum Declarations


    //region Variable Declarations
    private final String name;
    private final int mnemonic;
    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel",
            new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    PersonnelRole(String name, int mnemonic) {
        this.name = resources.getString(name);
        this.mnemonic = mnemonic;
    }
    //endregion Constructors

    //region Getters
    /**
     * @param isClan whether the current player faction is clan or not
     * @return the mnemonic to use for this role
     */
    public int getMnemonic(boolean isClan) {
        // We want to return the mnemonic if it is either not Battle Armour, is a Clan Faction and
        // the role is Elemental, or isn't a Clan faction and the role is BA.
        if (!isBattleArmor() || (isClan && isElemental()) || (!isClan && isBattleArmor())) {
            return mnemonic;
        } else {
            return KeyEvent.VK_UNDEFINED;
        }
    }
    //endregion Getters

    //region Boolean Information Methods
    public boolean isMechWarrior(boolean includeLAM) {
        return (this == MECHWARRIOR) || (includeLAM && isLAMPilot());
    }

    public boolean isAerospacePilot(boolean includeLAM) {
        return (this == AEROSPACE_PILOT) || (includeLAM && isLAMPilot());
    }

    public boolean isLAMPilot() {
        return (this == LAM_PILOT);
    }

    public boolean isBattleArmor() {
        return isBattleArmorDirect() || isElemental();
    }

    public boolean isBattleArmorDirect() {
        return (this == BA);
    }

    public boolean isElemental() {
        return (this == ELEMENTAL);
    }

    public boolean isSpacecraftCrew() {
        return (this == SPACECRAFT_CREW);
    }

    public boolean isMechTech() {
        return (this == MECH_TECH);
    }

    public boolean isAerospaceTech() {
        return (this == AEROSPACE_TECH);
    }

    public boolean isMechanic() {
        return (this == MECHANIC);
    }

    public boolean isBATech() {
        return (this == BA_TECH);
    }

    public boolean isAstech() {
        return (this == ASTECH);
    }

    public boolean isMedic() {
        return (this == MEDIC);
    }

    public boolean isTech() {
        return isMechTech() || isAerospaceTech() || isMechanic() || isBATech() || isSpacecraftCrew();
    }

    public boolean isCombatRole() {
        switch (this) {
            case MECHWARRIOR:
            case AEROSPACE_PILOT:
            case G_VEHICLE_DRIVER:
            case N_VEHICLE_DRIVER:
            case VEHICLE_GUNNER:
            case VEHICLE_CREW:
            case VTOL_PILOT:
            case BA:
            case ELEMENTAL:
            case INFANTRY:
            case PROTOMECH_PILOT:
            case CONVENTIONAL_AEROSPACE_PILOT:
            case LAM_PILOT:
            case SPACECRAFT_PILOT:
            case SPACECRAFT_CREW:
            case SPACECRAFT_GUNNER:
            case SPACECRAFT_NAVIGATOR:
                return true;
            case MECH_TECH:
            case MECHANIC:
            case AEROSPACE_TECH:
            case BA_TECH:
            case ASTECH:
            case DOCTOR:
            case MEDIC:
            case ADMIN_COMMAND:
            case ADMIN_LOGISTICS:
            case ADMIN_TRANSPORT:
            case ADMIN_HR:
            case DEPENDENT:
            case NONE:
            default:
                return false;
        }
    }

    public boolean isSupportRole() {
        switch (this) {
            case MECHWARRIOR:
            case AEROSPACE_PILOT:
            case G_VEHICLE_DRIVER:
            case N_VEHICLE_DRIVER:
            case VEHICLE_GUNNER:
            case VEHICLE_CREW:
            case VTOL_PILOT:
            case BA:
            case ELEMENTAL:
            case INFANTRY:
            case PROTOMECH_PILOT:
            case CONVENTIONAL_AEROSPACE_PILOT:
            case LAM_PILOT:
            case SPACECRAFT_PILOT:
            case SPACECRAFT_CREW:
            case SPACECRAFT_GUNNER:
            case SPACECRAFT_NAVIGATOR:
                return false;
            case MECH_TECH:
            case MECHANIC:
            case AEROSPACE_TECH:
            case BA_TECH:
            case ASTECH:
            case DOCTOR:
            case MEDIC:
            case ADMIN_COMMAND:
            case ADMIN_LOGISTICS:
            case ADMIN_TRANSPORT:
            case ADMIN_HR:
            case DEPENDENT:
            case NONE:
            default:
                return true;
        }
    }

    public boolean canPerformRole(Person person) {
        switch (this) {
            case MECHWARRIOR:
                return person.hasSkill(SkillType.S_GUN_MECH) && person.hasSkill(SkillType.S_PILOT_MECH);
            case G_VEHICLE_DRIVER:
                return person.hasSkill(SkillType.S_PILOT_GVEE);
            case N_VEHICLE_DRIVER:
                return person.hasSkill(SkillType.S_PILOT_NVEE);
            case VTOL_PILOT:
                return person.hasSkill(SkillType.S_PILOT_VTOL);
            case VEHICLE_GUNNER:
                return person.hasSkill(SkillType.S_GUN_VEE);
            case AEROSPACE_PILOT:
                return person.hasSkill(SkillType.S_GUN_AERO) && person.hasSkill(SkillType.S_PILOT_AERO);
            case CONVENTIONAL_AEROSPACE_PILOT:
                return person.hasSkill(SkillType.S_GUN_JET) && person.hasSkill(SkillType.S_PILOT_JET);
            case LAM_PILOT:
                return person.hasSkill(SkillType.S_GUN_MECH) && person.hasSkill(SkillType.S_PILOT_MECH)
                        && person.hasSkill(SkillType.S_GUN_JET) && person.hasSkill(SkillType.S_PILOT_AERO);
            case PROTOMECH_PILOT:
                return person.hasSkill(SkillType.S_GUN_PROTO);
            case BA:
            case ELEMENTAL:
                return person.hasSkill(SkillType.S_GUN_BA);
            case INFANTRY:
                return person.hasSkill(SkillType.S_SMALL_ARMS);
            case SPACECRAFT_PILOT:
                return person.hasSkill(SkillType.S_PILOT_SPACE);
            case SPACECRAFT_CREW:
                return person.hasSkill(SkillType.S_TECH_VESSEL);
            case SPACECRAFT_GUNNER:
                return person.hasSkill(SkillType.S_GUN_SPACE);
            case SPACECRAFT_NAVIGATOR:
                return person.hasSkill(SkillType.S_NAV);
            case MECH_TECH:
                return person.hasSkill(SkillType.S_TECH_MECH) && (person.getSkill(SkillType.S_TECH_MECH).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN);
            case MECHANIC:
            case VEHICLE_CREW:
                return person.hasSkill(SkillType.S_TECH_MECHANIC) && (person.getSkill(SkillType.S_TECH_MECHANIC).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN);
            case AEROSPACE_TECH:
                return person.hasSkill(SkillType.S_TECH_AERO) && (person.getSkill(SkillType.S_TECH_AERO).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN);
            case BA_TECH:
                return person.hasSkill(SkillType.S_TECH_BA) && (person.getSkill(SkillType.S_TECH_BA).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN);
            case ASTECH:
                return person.hasSkill(SkillType.S_ASTECH);
            case DOCTOR:
                return person.hasSkill(SkillType.S_DOCTOR) && (person.getSkill(SkillType.S_DOCTOR).getExperienceLevel() > SkillType.EXP_ULTRA_GREEN);
            case MEDIC:
                return person.hasSkill(SkillType.S_MEDTECH);
            case ADMIN_COMMAND:
            case ADMIN_LOGISTICS:
            case ADMIN_TRANSPORT:
            case ADMIN_HR:
                return person.hasSkill(SkillType.S_ADMIN);
            case NONE:
            case DEPENDENT:
                return true;
            default:
                return false;
        }
    }
    //endregion Boolean Information Methods

    @Override
    public String toString() {
        return name;
    }

    public static PersonnelRole parseFromString(String role) {
        try {
            return valueOf(role);
        } catch (Exception ignored) {

        }

        try {

        } catch (Exception ignored) {

        }

        return PersonnelRole.NONE;
    }
}
