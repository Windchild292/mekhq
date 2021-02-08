/*
 * Copyright (C) 2019-2021 - The MegaMek Team. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;

import megamek.common.Compute;
import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.SkillType;

public class DefaultSkillGenerator extends AbstractSkillGenerator {
    @Override
    public void generateSkills(Campaign campaign, Person person, int expLvl) {
        int type = person.getPrimaryRole();
        int secondary = person.getSecondaryRole();

        int bonus = 0;
        int mod = 0;

        if ((type == Person.T_MECHWARRIOR)
            && (secondary == Person.T_AERO_PILOT)) {
            mod = -2;
        }

        generateDefaultSkills(campaign, person, type, expLvl, bonus, mod);

        if (secondary != Person.T_NONE) {
            generateDefaultSkills(campaign, person, secondary, expLvl, bonus, mod);
        }

        bonus = getPhenotypeBonus(person);

        // roll small arms skill
        if (!person.getSkills().hasSkill(SkillType.S_SMALL_ARMS)) {
            int sarmsLvl = -12;
            if (Person.isSupportRole(type) || Person.isSupportRole(secondary)) {
                sarmsLvl = Utilities.generateExpLevel(campaign.getCampaignOptions().getSupportSmallArmsBonus());
            } else {
                sarmsLvl = Utilities.generateExpLevel(campaign.getCampaignOptions().getCombatSmallArmsBonus());
            }
            if (sarmsLvl > SkillType.EXP_ULTRA_GREEN) {
                addSkill(person, SkillType.S_SMALL_ARMS, sarmsLvl, campaign.getCampaignOptions().randomizeSkill(), bonus);
            }
        }

        // roll tactics skill
        if (!(Person.isSupportRole(type) || Person.isSupportRole(secondary))) {
            int tacLvl = Utilities.generateExpLevel(campaign.getCampaignOptions().getTacticsModifiers()[expLvl]);
            if (tacLvl > SkillType.EXP_ULTRA_GREEN) {
                addSkill(person, SkillType.S_TACTICS, tacLvl, campaign.getCampaignOptions().randomizeSkill(), bonus);
            }
        }

        // roll artillery skill
        if (campaign.getCampaignOptions().useArtillery()
                && (type == Person.T_MECHWARRIOR || type == Person.T_VEE_GUNNER || type == Person.T_INFANTRY)
                && Utilities.rollProbability(campaign.getCampaignOptions().getArtilleryProbability())) {
            int artyLvl = Utilities.generateExpLevel(campaign.getCampaignOptions().getArtilleryBonus());
            if (artyLvl > SkillType.EXP_ULTRA_GREEN) {
                addSkill(person, SkillType.S_ARTILLERY, artyLvl, campaign.getCampaignOptions().randomizeSkill(), bonus);
            }
        }

        // roll random secondary skill
        if (Utilities.rollProbability(campaign.getCampaignOptions().getSecondarySkillProbability())) {
            final List<String> possibleSkills = new ArrayList<>();
            for (String stype : SkillType.skillList) {
                if (!person.getSkills().hasSkill(stype)) {
                    possibleSkills.add(stype);
                }
            }
            String selSkill = possibleSkills.get(Compute.randomInt(possibleSkills.size()));
            int secondLvl = Utilities.generateExpLevel(campaign.getCampaignOptions().getSecondarySkillBonus());
            addSkill(person, selSkill, secondLvl, campaign.getCampaignOptions().randomizeSkill(), bonus);
        }
    }
}
