/*
 * This file is part of Fallout.
 *
 * Copyright (c) 2013-2017 <http://github.com/ampayne2/Fallout//>
 *
 * Fallout is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fallout is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fallout.  If not, see <http://www.gnu.org/licenses/>.
 */
package ninja.amp.fallout.character;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Perks a fallout character can select, one per tier.
 *
 * @author Austin Payne
 */
public enum Perk {
    SIGHT_ADAPT(1, "Adaptive Eyes", "Eyes adapt to changes in light quickly"),
    ANTI_COLD(1, "Advanced Homeostasis", "Feel the effects of cold less than others"),
    INCREASED_JUMP(1, "Gymnast", "Jump higher and leap farther than others"),
    INCREASED_STEALTH(1, "Light Steps", "Running does not affect your stealthiness"),
    ENERGY_REPLENISH(1, "Metabolizer", "Regain energy faster than others"),

    SIGHT_DISTANCE(2, "Eagle Eye", "+2 to Non-combat Perception rolls"),
    INCREASED_STRENGTH(2, "Intense Training", "+2 to Non-combat Strength rolls"),
    INCREASED_SURVIVAL(2, "Survivalist", "+1 to First Aid and", "+1 to Logical Thinking rolls"),
    INCREASED_ENDURANCE(2, "Thick Skinned", "+1 to Endurance rolls"),
    INCREASED_AIM(2, "Wasteland Cowboy", "+1 to Regular and Aimed", "Conventional Gun rolls"),

    DECREASED_BLEEDING(3, "Hoover", "Stop bleeding very quickly"),
    ANTI_RADIATION(3, "Radio-Inactive", "An environmental suit will", "protect against most radiation"),
    INCREASED_SENSES(3, "Sensory Overload", "Hear through walls and", "detect poison. Others have a", "harder time sneaking past"),
    PERFECT_MEMORY(3, "Snapshot", "All images and sounds can", "be remembered without fail"),
    FEIGN_DEATH(3, "Survivor", "Slow your heart and feign", "death, stopping bleeding.", "Cannot wake for two hours"),

    INCREASED_LOGIC(4, "Detective", "+3 to Logical Thinking rolls"),
    INCREASED_CUNNING(4, "Gambler", "+2 to Lockpicking and", "+2 to Speech rolls"),
    INCREASED_HEAL(4, "Healer", "+2 to First Aid and", "+2 to Surgery rolls"),
    INNOCENT_DEFENSE(4, "Hero", "+2 to Combat rolls when defending", "the weak and innocent"),
    INCREASED_SNEAK(4, "Shadow", "+3 to Daytime Sneak and", "+5 to Nighttime Sneak rolls"),

    BATTLEFIELD_TERROR(5, "Death or Glory", "You are war-incarnate.", "See full guide"),
    GENERAL(5, "Old-World General", "Your word is law, your orders absolute.", "See full guide"),
    CREATURES(5, "One With the Wasteland", "You are one with the wasteland,", "its power defends you.", "See full guide"),
    TELEKINESIS(5, "The Master's Legacy", "The work of the master lives on", "through you, gain telekinesis.", "See full guide"),
    TELEPATHY(5, "The Master's Legacy", "The work of the master lives on", "through you, gain telepathy.", "See full guide"),
    SPIRIT_FORM(5, "Undying Will", "You refuse the embrace of", "death and become a spirit.", "See full guide");

    private static final Map<Integer, Set<Perk>> tiers;
    private static final List<String> perkNames;
    private final String name;
    private final String[] description;
    private int tier;

    Perk(int tier, String name, String... description) {
        this.name = name;
        this.tier = tier;
        this.description = description;
    }

    /**
     * Gets the display name of the perk.
     *
     * @return The perk's display name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the tier of the perk.
     *
     * @return The perk's tier
     */
    public int getTier() {
        return tier;
    }

    /**
     * Gets the description of the perk.
     *
     * @return The perk's description
     */
    public String[] getDescription() {
        return description;
    }

    /**
     * Gets a perk from its name.
     *
     * @param name The name of the perk
     * @return The perk
     */
    public static Perk fromName(String name) {
        for (Perk perk : Perk.class.getEnumConstants()) {
            if (perk.getName().equalsIgnoreCase(name)) {
                return perk;
            }
        }
        return null;
    }

    /**
     * Gets the perks of a certain tier.
     *
     * @param tier The tier
     * @return The perks in the tier
     */
    public static Set<Perk> getPerks(int tier) {
        return Collections.unmodifiableSet(tiers.get(tier));
    }

    /**
     * Gets a list of perk names.
     *
     * @return The list of perk names
     */
    public static List<String> getPerkNames() {
        return perkNames;
    }

    static {
        tiers = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            tiers.put(i, new HashSet<Perk>());
        }
        perkNames = new ArrayList<>();
        for (Perk perk : Perk.class.getEnumConstants()) {
            tiers.get(perk.getTier()).add(perk);
            perkNames.add(perk.getName());
        }
    }

}
