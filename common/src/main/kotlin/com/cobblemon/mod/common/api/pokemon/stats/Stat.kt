package com.cobblemon.mod.common.api.pokemon.stats

import net.minecraft.text.Text
import net.minecraft.util.Identifier

/**
 * Represents a stat of a Pokémon.
 * If you wish to implement custom stats be sure to implement your own [StatNetworkSerializer] and [StatProvider].
 * Any custom implementation must be provided to both sides.
 *
 * @author Licious
 * @since November 6th, 2022
 */
interface Stat {

    /**
     * The [Identifier] of this stat.
     */
    val identifier: Identifier

    /**
     * The display name of this stat.
     * This should ideally provide the lang.
     */
    val displayName: Text

    /**
     * The type of this stat.
     */
    val type: Type

    /**
     * Represents the type of this stat.
     */
    enum class Type {

        /**
         * Represents stats that always exist.
         * For more information see this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Stat#Permanent_stats) page.
         */
        PERMANENT,

        /**
         * Represents stats that only exist during a battle.
         * For more information see this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Stat#In-battle_stats) page.
         */
        BATTLE_ONLY

    }

}