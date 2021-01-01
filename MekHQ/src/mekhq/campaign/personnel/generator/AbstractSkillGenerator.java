/*
 * Copyright (C) 2019 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.personnel.generator;

import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.Skill;
import mekhq.campaign.personnel.SkillType;

/**
 * Represents a class which can generate new {@link Skill} objects
 * for a {@link Person}.
 */
public abstract class AbstractSkillGenerator {
    /**
     * Generates skills for a {@link Person} given their experience level.
     * @param campaign The {@link Campaign} the {@link Person} is a member of
     * @param person The {@link Person} to add skills.
     * @param expLvl The experience level of the person (e.g. {@link SkillType#EXP_GREEN}).
     */
    public abstract void generateSkills(Campaign campaign, Person person, int expLvl);

    /**
     * Generates the default skills for a {@link Person} based on their primary role.
     * @param campaign The {@link Campaign} the {@link Person} is a member of
     * @param person The {@link Person} to add default skills.
     * @param primaryRole The primary role of the person (e.g. {@link Person#T_MECHWARRIOR}).
     * @param expLvl The experience level of the person (e.g. {@link SkillType#EXP_GREEN}).
     * @param bonus The bonus to use for the default skills.
     * @param rollModifier A roll modifier to apply to any randomizations.
     */
    protected void generateDefaultSkills(Campaign campaign, Person person, int primaryRole, int expLvl, int bonus, int rollModifier) {
        switch (primaryRole) {
            case (Person.T_MECHWARRIOR):
                addSkill(person, SkillType.S_PILOT_MECH, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                addSkill(person, SkillType.S_GUN_MECH, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_GVEE_DRIVER):
                addSkill(person, SkillType.S_PILOT_GVEE, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                addSkill(person, SkillType.S_GUN_VEE, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_NVEE_DRIVER):
                addSkill(person, SkillType.S_PILOT_NVEE, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                addSkill(person, SkillType.S_GUN_VEE, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_VTOL_PILOT):
                addSkill(person, SkillType.S_PILOT_VTOL, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                addSkill(person, SkillType.S_GUN_VEE, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_VEE_GUNNER):
                addSkill(person, SkillType.S_GUN_VEE, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_CONV_PILOT):
                addSkill(person, SkillType.S_PILOT_JET, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                addSkill(person, SkillType.S_GUN_JET, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_AERO_PILOT):
                addSkill(person, SkillType.S_PILOT_AERO, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                addSkill(person, SkillType.S_GUN_AERO, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_PROTO_PILOT):
                addSkill(person, SkillType.S_GUN_PROTO, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_BA):
                addSkill(person, SkillType.S_GUN_BA, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                addSkill(person, SkillType.S_ANTI_MECH, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                addSkill(person, SkillType.S_SMALL_ARMS, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_INFANTRY):
                if (Utilities.rollProbability(campaign.getCampaignOptions().getAntiMekProbability())) {
                    addSkill(person, SkillType.S_ANTI_MECH, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                }
                addSkill(person, SkillType.S_SMALL_ARMS, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_SPACE_PILOT):
                addSkill(person, SkillType.S_PILOT_SPACE, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_SPACE_CREW):
                addSkill(person, SkillType.S_TECH_VESSEL, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_SPACE_GUNNER):
                addSkill(person, SkillType.S_GUN_SPACE, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_NAVIGATOR):
                addSkill(person, SkillType.S_NAV, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_MECH_TECH):
                addSkill(person, SkillType.S_TECH_MECH, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_MECHANIC):
            case Person.T_VEHICLE_CREW:
                addSkill(person, SkillType.S_TECH_MECHANIC, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_AERO_TECH):
                addSkill(person, SkillType.S_TECH_AERO, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_BA_TECH):
                addSkill(person, SkillType.S_TECH_BA, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_ASTECH):
                addSkill(person, SkillType.S_ASTECH, 0, 0);
                break;
            case (Person.T_DOCTOR):
                addSkill(person, SkillType.S_DOCTOR, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
            case (Person.T_MEDIC):
                addSkill(person, SkillType.S_MEDTECH, 0, 0);
                break;
            case (Person.T_ADMIN_COM):
            case (Person.T_ADMIN_LOG):
            case (Person.T_ADMIN_TRA):
            case (Person.T_ADMIN_HR):
                addSkill(person, SkillType.S_ADMIN, expLvl, campaign.getCampaignOptions().randomizeSkill(), bonus, rollModifier);
                break;
        }
    }

    protected static void addSkill(Person person, String skillName, int level, int bonus) {
        person.addSkill(skillName, new Skill(skillName, level, bonus));
    }

    protected static void addSkill(Person person, String skillName, int experienceLevel, boolean randomizeLevel, int bonus) {
        addSkill(person, skillName, experienceLevel, randomizeLevel, bonus, 0);
    }

    protected static void addSkill(Person person, String skillName, int experienceLevel, boolean randomizeLevel, int bonus, int rollMod) {
        person.addSkill(skillName, randomizeLevel
                ? Skill.randomizeLevel(skillName, experienceLevel, bonus, rollMod)
                : Skill.createFromExperience(skillName, experienceLevel, bonus));
    }

    /**
     * Gets the clan phenotype bonus for a {@link Person}, if applicable.
     * @param person A {@link Person} to calculate a phenotype bonus.
     * @return The bonus to a {@link Skill} due to clan phenotypes matching the primary role.
     */
    protected int getPhenotypeBonus(Person person) {
        if (person.isClanner()) {
            // apply phenotype bonus only to primary skills
            switch (person.getPrimaryRoleInt()) {
                case Person.T_MECHWARRIOR:
                    if (person.getPhenotype().isMechWarrior()) {
                        return 1;
                    }
                    break;
                case Person.T_BA:
                    if (person.getPhenotype().isElemental()) {
                        return 1;
                    }
                    break;
                case Person.T_CONV_PILOT:
                case Person.T_AERO_PILOT:
                    if (person.getPhenotype().isAerospace()) {
                        return 1;
                    }
                    break;
                case Person.T_GVEE_DRIVER:
                case Person.T_NVEE_DRIVER:
                case Person.T_VTOL_PILOT:
                case Person.T_VEE_GUNNER:
                    if (person.getPhenotype().isVehicle()) {
                        return 1;
                    }
                    break;
                case Person.T_PROTO_PILOT:
                    if (person.getPhenotype().isProtoMech()) {
                        return 1;
                    }
                case Person.T_SPACE_CREW:
                case Person.T_SPACE_GUNNER:
                case Person.T_SPACE_PILOT:
                case Person.T_NAVIGATOR:
                    if (person.getPhenotype().isNaval()) {
                        return 1;
                    }
                default:
                    break;
            }
        }
        return 0;
    }
}
