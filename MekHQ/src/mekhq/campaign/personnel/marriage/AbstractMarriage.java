/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.personnel.marriage;

import megamek.common.Compute;
import megamek.common.enums.Gender;
import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.event.PersonChangedEvent;
import mekhq.campaign.log.PersonalLogger;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.enums.MarriageSurnameStyle;
import mekhq.campaign.personnel.enums.RandomMarriageMethod;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public abstract class AbstractMarriage {
    //region Variable Declarations
    private final RandomMarriageMethod method;
    private int minimumAge;
    private int checkMutualAncestorsDepth;
    private boolean logNameChanges;
    private Map<MarriageSurnameStyle, Double> surnameWeights;
    private int ageRange;
    private boolean useSameSexRandomMarriages;

    private final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel", new EncodeControl());
    //endregion Variable Declarations

    //region Constructors
    protected AbstractMarriage(final RandomMarriageMethod method,
                               final boolean useSameSexRandomMarriages) {
        this.method = method;
        this.useSameSexRandomMarriages = useSameSexRandomMarriages;
    }
    //endregion Constructors

    //region Getters
    public RandomMarriageMethod getMethod() {
        return method;
    }

    public int getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(final int minimumAge) {
        this.minimumAge = minimumAge;
    }

    public int getCheckMutualAncestorsDepth() {
        return checkMutualAncestorsDepth;
    }

    public void setCheckMutualAncestorsDepth(final int checkMutualAncestorsDepth) {
        this.checkMutualAncestorsDepth = checkMutualAncestorsDepth;
    }

    public int getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(final int ageRange) {
        this.ageRange = ageRange;
    }

    public boolean isUseSameSexRandomMarriages() {
        return useSameSexRandomMarriages;
    }

    public void setUseSameSexRandomMarriages(final boolean useSameSexRandomMarriages) {
        this.useSameSexRandomMarriages = useSameSexRandomMarriages;
    }
    //endregion Getters

    /**
     * Determines if a person is old enough to marry
     * @param today the current day
     * @param person the person to determine if they are old enough
     * @return true if they are, otherwise false
     */
    public boolean oldEnoughToMarry(final LocalDate today, final Person person) {
        return person.getAge(today) >= getMinimumAge();
    }

    /**
     * Determines if the potential spouse is a safe spouse for a person
     * @param today the current day
     * @param person the person trying to marry
     * @param potentialSpouse the person to determine if they are a safe spouse
     * @return true if the potential spouse is a safe spouse for the provided person
     */
    public boolean safeSpouse(final LocalDate today, final Person person,
                              final Person potentialSpouse) {
        // Huge convoluted return statement, with the following restrictions
        // can't marry yourself
        // can't marry someone who is already married
        // can't marry someone who doesn't want to be married
        // can't marry a prisoner, unless you are also a prisoner (this is purposely left open for prisoners to marry who they want)
        // can't marry a person who is dead or MIA
        // can't marry inactive personnel (this is to show how they aren't part of the force anymore)
        // TODO : can't marry anyone who is not located at the same planet as the person - GitHub #1672: Implement current planet tracking for personnel
        // can't marry a close relative
        return (
                !person.equals(potentialSpouse)
                        && !potentialSpouse.getGenealogy().hasSpouse()
                        && potentialSpouse.isMarriageable()
                        && oldEnoughToMarry(today, potentialSpouse)
                        && (!potentialSpouse.getPrisonerStatus().isPrisoner() || person.getPrisonerStatus().isPrisoner())
                        && !potentialSpouse.getStatus().isDeadOrMIA()
                        && potentialSpouse.getStatus().isActive()
                        && !person.getGenealogy().checkMutualAncestors(potentialSpouse, getCheckMutualAncestorsDepth())
        );
    }

    public void marry(final Campaign campaign, final LocalDate today, final Person origin,
                      final Person spouse, final MarriageSurnameStyle surnameStyle) {
        // Immediately set both Maiden Names, to avoid any divorce bugs (as the default is now an empty string)
        origin.setMaidenName(origin.getSurname());
        spouse.setMaidenName(spouse.getSurname());

        // Then add them as spouses
        origin.getGenealogy().setSpouse(spouse);
        spouse.getGenealogy().setSpouse(origin);

        // Do the logging
        PersonalLogger.marriage(origin, spouse, today);
        PersonalLogger.marriage(spouse, origin, today);

        campaign.addReport(String.format("%s has married %s!", origin.getHyperlinkedName(),
                spouse.getHyperlinkedName()));

        // Apply the surname style changes
        surnameStyle.apply(campaign, today, origin, spouse);

        // And finally we trigger person changed events
        MekHQ.triggerEvent(new PersonChangedEvent(origin));
        MekHQ.triggerEvent(new PersonChangedEvent(spouse));
    }

    //region New Day
    public void processNewDay(final Campaign campaign, final LocalDate today, final Person person) {
        // Don't attempt to generate is someone isn't marriageable, has a spouse, isn't old enough
        // to marry, or is actively deployed
        if (!person.isMarriageable() || person.getGenealogy().hasSpouse()
                || !oldEnoughToMarry(today, person) || person.isDeployed()) {
            return;
        }

        // setting is the fractional chance that this attempt at finding a marriage will result in one
        if (Compute.randomFloat() < (campaign.getCampaignOptions().getChanceRandomMarriages())) {
            addRandomSpouse(campaign, today, person, false);
        } else if (campaign.getCampaignOptions().useRandomSameSexMarriages()) {
            if (Compute.randomFloat() < (campaign.getCampaignOptions().getChanceRandomSameSexMarriages())) {
                addRandomSpouse(campaign, today, person, true);
            }
        }
    }

    //region Random Marriage
    private void addRandomSpouse(final Campaign campaign, final LocalDate today,
                                 final Person person, final boolean sameSex) {
        final Gender gender = sameSex ? person.getGender() : (person.getGender().isMale() ? Gender.FEMALE : Gender.MALE);
        final List<Person> potentials = campaign.getActivePersonnel().stream()
                .filter(potentialSpouse -> isPotentialRandomSpouse(today, person, potentialSpouse, gender))
                .collect(Collectors.toList());
        if (!potentials.isEmpty()) {
            marry(campaign, today, person, potentials.get(Compute.randomInt(potentials.size())),
                    MarriageSurnameStyle.WEIGHTED);
        }
    }

    private boolean isPotentialRandomSpouse(final LocalDate today, final Person person,
                                            final Person potentialSpouse, final Gender gender) {
        if ((potentialSpouse.getGender() != gender) || !safeSpouse(today, person, potentialSpouse)
                || !(person.getPrisonerStatus().isFree()
                || (person. getPrisonerStatus().isPrisoner() && potentialSpouse.getPrisonerStatus().isPrisoner()))) {
            return false;
        }

        final int ageDifference = Math.abs(potentialSpouse.getAge(today) - person.getAge(today));
        return ageDifference <= getAgeRange();
    }
    //endregion Random Marriage
    //endregion New Day
}
