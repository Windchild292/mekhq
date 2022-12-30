/*
 * Skill.java
 *
 * Copyright (c) 2009 Jay Lawson (jaylawson39 at yahoo.com). All rights reserved.
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
package mekhq.campaign.personnel;

import megamek.Version;
import megamek.common.Compute;
import megamek.common.annotations.Nullable;
import megamek.common.enums.SkillLevel;
import mekhq.utilities.MHQXMLUtility;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;

/**
 * As ov v0.1.9, we will be tracking a group of skills on the person. These skills will define
 * personnel rather than subtypes wrapped around pilots and teams. This will allow for considerably
 * more flexibility in the kinds of personnel available.
 *
 * Four important characteristics will determine how each skill works
 * level - this is the level of the skill. By default this will go from 0 to 10, but the max will
 *         be customizable. These won't necessarily correspond to named levels (e.g. Green, Elite)
 *         By assigning skill costs of 0 to some levels, these can basically be skipped and by
 *         assigning skill costs of -1, they can be made inaccessible.
 * bonus - this is a bonus that the given person has for this skill which is separable from level.
 *         Primarily this allows for rpg-style attribute bonuses to come into play.
 * target - this is the baseline target number for the skill when level and bonus are zero.
 * countUp - this is a boolean that defines whether this skill's target is a btech style
 *           "roll greater than or equal to" (false) or an rpg-style bonus to a roll (true)
 * The actual target number for a skill is given by
 *         countUp: target+lvl+bonus
 *         !countUp: target - level - bonus
 * by clever manipulation of these values and skillcosts in campaignOptions, players should be
 * able to recreate any of the rpg versions or their own homebrew system. The default setup
 * will follow the core rulebooks (not aToW).
 * @author Jay Lawson (jaylawson39 at yahoo.com)
 */
public class Skill {
    //region Variable Declarations
    private SkillType type;
    private SkillLevel level;
    private int bonus;
    //endregion Variable Declarations

    //region Constructors
    protected Skill() {

    }

    public Skill(final String type) {
        this(type, SkillLevel.REGULAR, 0);
    }

    public Skill(final String type, final SkillLevel level, final int bonus) {
        this(SkillType.getType(type), level, bonus);
    }

    public Skill(final SkillType type, final SkillLevel level, final int bonus) {
        setType(type);
        setLevel(level);
        setBonus(bonus);
    }
    //endregion Constructors

    //region Getters/Setters
    public SkillLevel getLevel() {
        return level;
    }

    public void setLevel(final SkillLevel level) {
        this.level = level;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(final int bonus) {
        this.bonus = bonus;
    }

    public SkillType getType() {
        return type;
    }

    public void setType(final SkillType type) {
        this.type = type;
    }
    //endregion Getters/Setters

    /**
     * Creates a new {@link Skill} from the given experience level and bonus.
     * @param type The {@link SkillType} name.
     * @param experienceLevel An experience level (e.g. {@link SkillType#EXP_GREEN}).
     * @param bonus The bonus for the resulting {@link Skill}.
     * @return A new {@link Skill} of the appropriate type, with a level based on {@code experienceLevel}
     *         and the bonus.
     */
    public static Skill createFromExperience(String type, int experienceLevel, int bonus) {
        SkillType skillType = SkillType.getType(type);
        SkillLevel level = skillType.getLevelFromExperience(experienceLevel);
        return new Skill(skillType, level, bonus);
    }

    /**
     * Creates a new {@link Skill} with a randomized level.
     * @param type The {@link SkillType} name.
     * @param experienceLevel An experience level (e.g. {@link SkillType#EXP_GREEN}).
     * @param bonus The bonus for the resulting {@link Skill}.
     * @param rollModifier The roll modifier on a 1D6.
     * @return A new {@link Skill} of the appropriate type, with a randomized level based on
     *         the experience level and a 1D6 roll.
     */
    public static Skill randomizeLevel(String type, int experienceLevel, int bonus, int rollModifier) {
        SkillType skillType = SkillType.getType(type);
        SkillLevel level = skillType.getLevelFromExperience(experienceLevel);

        int roll = Compute.d6() + rollModifier;
        if ((roll < 2) && !level.isNone()) {
            level = Skills.SKILL_LEVELS[level.ordinal() - 1];
        } else if ((roll > 5) && !level.isLegendary()) {
            level = Skills.SKILL_LEVELS[level.ordinal() + 1];
        }

        return new Skill(skillType, level, bonus);
    }

    public int getFinalSkillValue() {
        if (type.countUp()) {
            return type.getTarget() + getLevel().getAdjustedValue() + bonus;
        } else {
            return type.getTarget() - getLevel().getAdjustedValue() - bonus;
        }
    }

    public void improve() {
        if (getLevel().isLegendary()) {
            // Can't improve past the max
            return;
        }
        setLevel(Skills.SKILL_LEVELS[getLevel().ordinal() + 1]);
        // if the cost for the next level is zero (or less than zero), then
        // keep improve until you hit a non-zero cost
        if (getType().getCost(getLevel()) <= 0) {
            improve();
        }
    }

    public int getCostToImprove() {
        int cost = 0;
        int i = 1;
        while ((cost <= 0) && (level.ordinal() + i) < Skills.SKILL_LEVELS.length) {
            cost = type.getCost(Skills.SKILL_LEVELS[level.ordinal() + i]);
            i++;
        }
        return cost;
    }

    public void updateType() {
        type = SkillType.getType(type.getName());
    }

    //region File IO
    public void writeToXML(final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "skill");
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "type", getType().getName());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "level", getLevel().name());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "bonus", getBonus());
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "skill");
    }

    public static @Nullable Skill generateInstanceFromXML(final Node wn, final Version version) {
        try {
            final Skill skill = new Skill();

            // Okay, now load Skill-specific fields!
            final NodeList nl = wn.getChildNodes();

            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);

                if (wn2.getNodeName().equalsIgnoreCase("type")) {
                    skill.setType(SkillType.getType(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("level")) {
                    skill.setLevel(version.isLowerThan("0.49.12")
                            ? Skills.SKILL_LEVELS[Integer.parseInt(wn2.getTextContent().trim()) + 1]
                            : SkillLevel.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("bonus")) {
                    skill.setBonus(Integer.parseInt(wn2.getTextContent().trim()));
                }
            }

            return skill;
        } catch (Exception ex) {
            LogManager.getLogger().error("", ex);
            return null;
        }
    }
    //endregion File IO

    @Override
    public String toString() {
        if (getType().countUp()) {
            return "+" + getFinalSkillValue();
        } else {
            return getFinalSkillValue() + "+";
        }
    }
}
