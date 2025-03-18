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

public enum PersonnelTagType {
    ORIGIN_REGION,
    ORIGIN_FACTION,
    BACKGROUND,
    CHILDHOOD,
    EDUCATION,
    HIGHER_EDUCATION,
    REAL_LIFE,
    PERSONALITY_TRAITS,
    HOBBIES,
    SEXUAL_ORIENTATION,
    MERCENARY,
    MANEI_DOMINI_CLASSIFICATION,
    ROM_DESIGNATION,
    CUSTOM;
}

/*
This will be used in the following type of system:
Map<PersonnelTagType, List<String>>

With each type permitting individual divisions and custom logic

we will be dragging in the ManeiDominiClass and ROMDesignation Enums as trait groupings
*/

/*
Tag Ideas by type:
    ORIGIN_REGION:
        Clanner
        Spheroid
        Periphery
        Deep Periphery
    ORIGIN_FACTION:
        { List of all Factions }
    BACKGROUND:
        Royalty
        Noble
        Commoner
        Slave
    CHILDHOOD:
    EDUCATION:
    HIGHER_EDUCATION:
    REAL_LIFE:
        Chef
    POSITIVE_PERSONALITY_TRAITS:
        Brave
        Cautious
        Dependable
        Honest
        Lucky
    NEUTRAL_PERSONALITY_TRAITS:

    NEGATIVE_PERSONALITY_TRAITS:
        Alcoholic

    HOBBIES:
        Athlete
        DJ
        Foodie
        Gambler
        Gamer
        Hunter
        Holovid Lover
        Home Cook
        Miniature Painter
        Miniature Wargamer
        Modellist
        Musician
        Painter
        Reader
        RPG Enthusiast
        Wine Connoisseur
    SEXUAL_ORIENTATION:
        Asexual
        Bisexual
        Gay
        Lesbian
        Pansexual
        Transgender
    MERCENARY:
        Founder

Tag Ideas without sorting:
    Boring
    Tasteless

Example tags from Destiny:
    Tanker: He’s a tank commander, so this one’s a no-brainer. This means he probably doesn’t get along with MechWarriors, since their superiority complex rubs him the wrong way.
    Silver Hawks Coalition: This is where he was born.
    Gruff: A grizzled sergeant isn’t going to be a very amicable person.
    No-Nonsense: Edwin’s not going to put up with excuses or tomfoolery.
    Career Soldier: Some folks serve a single tour of duty, but not Edwin. He’ll be an enlisted soldier until he retires or his coffin is nailed shut, whichever happens first.
    Skilled
    Haunted
    Jaded
    Blooded
    Flirty
    Attractive
    Ugly
    Eager
    Gambler
    Cocky
    Honorable
    Idealistic
    Confident
    Eager
    Disciplined
    Assassin
    Demolitions
    Stealth
    Spy
    Chameleon
    Saboteur
    Officer
    MechWarrior
    Diplomatic
    Socialite
    Connected
    Thorough
    Sarcastic
    Blunt
    Genius
    Patriotic
    Warrior House Dai Da Chi
    Fierce
    Deceptive
    Tough
    Fearless
    Infantry
    Committed
    Drillmaster
    Rebel
    Guerrilla
    Perceptive
    Stealthy
    Clever
    Confident
    Brawler
    Rowdy
    Steady
    Reflective
    Maverick
    Calm
    Confident Exterior
    Black Widow Company
    Uncertain
    Stiff
    Quick
    Wide-Eyed
    Social
    Quick
    Alert
    Observant
    Scout
    Runner
    Adventurous
    Survivor
    Force Recon
    Unexpected
    Former Slave
    Passionate
    Parent
    Quiet
    Devout
    Believer
    Student
    Gentle
    Survivor
    Scalawag
    Intimidating
    Resourceful
    Tank
    Big Guns
    Sultry
    Flexible
    Professional
    Careful
    Committed
    Loner
    Haunted
    Outdoorsman
    Survivalist
    Ace Pilot
    Fidgety
    Unorthodox
    Lucky
    Spotter
    Gladiator
    Champion
    Solaris VII
    Aggressive
    Determined
    Introvert

Clanner Tags from Destiny
    Trueborn
    Warrior Caste
    Militaristic
    Bloodnamed
    Political
    Warden
    Tip of the Spear
    Optimistic
    Proud
    Dreamer
    Driven
    Stubborn
    Freebirth
    Quiet
    Conqueror
    Cruel
    Focused
    Fearless
    Haughty
    Jury-Rigger
    Gifted
    Washout
    Restless
    Subservient
    Mountainous
    Fearsome
    Aggressive
    Commanding
    Infantry
    Brawler
    Vicious
    Devil-May-Care
    Superior
    Damaged
    Risk-Taker
    Fatalist
    Speedster
    Daredevil
    Unreliable
    Somber
    Patient
    Assured
    Serene
    Confident




*/
