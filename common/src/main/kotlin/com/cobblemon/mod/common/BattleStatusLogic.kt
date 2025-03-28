/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.cobblemon.mod.common.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object BattleStatusLogic {

    // Multipliers for boost levels.
    private val statMultipliers = mapOf(
        -6 to 0.25,
        -5 to 0.2857,
        -4 to 0.3333,
        -3 to 0.4,
        -2 to 0.5,
        -1 to 0.6667,
        0 to 1.0,
        1 to 1.5,
        2 to 2.0,
        3 to 2.5,
        4 to 3.0,
        5 to 3.5,
        6 to 4.0
    )

    // Order in which stats may be displayed (if needed elsewhere). Works as of 3/27
    private val statOrder = listOf(
        Stats.ATTACK,
        Stats.SPECIAL_ATTACK,
        Stats.DEFENCE,
        Stats.SPECIAL_DEFENCE,
        Stats.SPEED
    )

    // Mapping for display labels.
    private val statLabelMap = mapOf(
        Stats.ATTACK to "ATK",
        Stats.DEFENCE to "DEF",
        Stats.SPECIAL_ATTACK to "SP. ATK",
        Stats.SPECIAL_DEFENCE to "SP. DEF",
        Stats.SPEED to "SPD"
    )

    // Mapping for stat IDs used in boost counting.
    private val statIdMap = mapOf(
        Stats.ATTACK to "atk",
        Stats.DEFENCE to "def",
        Stats.SPECIAL_ATTACK to "spa",
        Stats.SPECIAL_DEFENCE to "spd",
        Stats.SPEED to "spe"
    )

    /**
     * Reflection helper: If the given object has a getId() method,
     * this returns its value as a String.
     */
    fun readId(obj: Any?): String? {
        if (obj == null) return null
        return try {
            val method = obj::class.java.getMethod("getId")
            method.invoke(obj) as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Builds the list of stat lines for a Pokémon. The layout is:
     *  - Row 1: ATK + SP. ATK
     *  - Row 2: DEF + SP. DEF
     *  - Row 3: SPD alone
     *  - Row 4+: Ability with description (possibly over multiple lines)
     *
     * This method is pure logic and returns a list of text components.
     */
    fun buildStatLines(
        pkmn: Pokemon?,
        posContext: Any?,
        negContext: Any?,
        isOpponent: Boolean = false
    ): List<MutableComponent> {
        if (pkmn == null) {
            return listOf(Component.literal("No Pokémon found."))
        }

        // Pokémon name (bold)
        val nameLine = Component.literal(pkmn.species.name)
            .withStyle { it.withBold(true).withColor(0xFFFFFF) }

        // Build stat lines for each stat.
        val atkLine = buildSingleStatLine(pkmn, Stats.ATTACK, posContext, negContext)
        val spAtkLine = buildSingleStatLine(pkmn, Stats.SPECIAL_ATTACK, posContext, negContext)
        val defLine = buildSingleStatLine(pkmn, Stats.DEFENCE, posContext, negContext)
        val spDefLine = buildSingleStatLine(pkmn, Stats.SPECIAL_DEFENCE, posContext, negContext)
        val spdLine = buildSingleStatLine(pkmn, Stats.SPEED, posContext, negContext)

        // Merge two stats side by side.
        val row1 = mergeTwoStatsSideBySide(atkLine, spAtkLine, spacing = 3)
        val row2 = mergeTwoStatsSideBySide(defLine, spDefLine, spacing = 3)

        // Ability lines
        val abilityObj = pkmn.ability
        val abilityDescKey = pkmn.ability?.description
        val abilityLine1 = buildAbilityLine(abilityObj, abilityDescKey)
        val abilityDescLength = abilityDescKey?.let { Component.translatable(it).string.length } ?: 0

        // Define split lengths as in your original constraints.
        val splitLength1 = "Lorem ipsum odor amet, consectetuer adipis".length
        val splitLength2 = "cing elit. Elit pretium hac; habitant primis et risus. Ac feugiat la".length

        val abilityLine2 = if (abilityDescLength > splitLength1) {
            buildAbilityLinePart2(abilityObj, abilityDescKey)
        } else {
            null
        }
        val abilityLine3 = if (abilityDescLength > splitLength1 + splitLength2) {
            buildAbilityLinePart3(abilityObj, abilityDescKey)
        } else {
            null
        }

        // Collect the lines.
        val lines = mutableListOf<MutableComponent>()
        lines.add(nameLine)
        row1?.let { lines.add(it) }
        row2?.let { lines.add(it) }
        spdLine?.let { lines.add(it) }
        abilityLine1?.let { lines.add(it) }
        abilityLine2?.let { lines.add(it) }
        abilityLine3?.let { lines.add(it) }

        return lines
    }

    /**
     * Builds a single stat line, e.g. "ATK: 56 (1.0x) = 56".
     */
    fun buildSingleStatLine(
        pkmn: Pokemon,
        stat: Stats,
        posContext: Any?,
        negContext: Any?
    ): MutableComponent? {
        val label = statLabelMap[stat] ?: return null
        val baseValue = Cobblemon.statProvider.getStatForPokemon(pkmn, stat) ?: 0

        // Count positive and negative boosts.
        val statId = statIdMap[stat] ?: ""
        val plus = (posContext as? List<*>)?.count { readId(it) == statId } ?: 0
        val minus = (negContext as? List<*>)?.count { readId(it) == statId } ?: 0
        val boost = plus - minus
        val multiplier = statMultipliers[boost] ?: 1.0
        val newValue = (baseValue * multiplier).toInt()

        // Construct the stat line as a combination of text components.
        val identifier = Component.literal("$label:")
            .withStyle { it.withBold(true).withColor(0xFFFFFF) }
        val baseComp = Component.literal(" $baseValue ")
            .withStyle { it.withColor(0xFFFFFF) }
        val openParen = Component.literal("(")
            .withStyle { it.withColor(0xFFFFFF) }
        val multiplierColor = when {
            multiplier > 1.0 -> 0x55FF55
            multiplier < 1.0 -> 0xFF5555
            else -> 0xFFFFFF
        }
        val multiplierComp = Component.literal("$multiplier")
            .withStyle { it.withColor(multiplierColor) }
        val xComp = Component.literal("x")
            .withStyle { it.withColor(0xFFFFFF) }
        val closeParen = Component.literal(")")
            .withStyle { it.withColor(0xFFFFFF) }
        val equalsComp = Component.literal(" = ")
            .withStyle { it.withColor(0xFFFFFF) }
        val finalComp = Component.literal("$newValue")
            .withStyle { it.withColor(multiplierColor) }

        val combined = Component.literal("")
        combined.append(identifier)
        combined.append(baseComp)
        combined.append(openParen)
        combined.append(multiplierComp)
        combined.append(xComp)
        combined.append(closeParen)
        combined.append(equalsComp)
        combined.append(finalComp)
        return combined
    }

    /**
     * Builds the first line for the Pokémon's ability:
     * "Ability: <Name> - <Description Part 1>".
     */
    fun buildAbilityLine(
        abilityObj: Any?,
        abilityDescKey: String?
    ): MutableComponent? {
        if (abilityObj == null || abilityDescKey == null) {
            return null
        }
        val abilityName = try {
            abilityObj.let {
                val method = it::class.java.getMethod("getName")
                method.invoke(it) as? String
            }
        } catch (ex: Exception) {
            null
        } ?: "???"

        val abilityDesc = Component.translatable(abilityDescKey).string
        val splitLength1 = "Lorem ipsum odor amet, consectetuer adipis".length
        val abilityDescPart1 = if (abilityDesc.length >= splitLength1) {
            abilityDesc.substring(0, splitLength1)
        } else {
            abilityDesc
        }
        val abilityLabel = Component.literal("Ability: ")
            .withStyle { it.withBold(true).withColor(0xFFFFFF) }
        val abilityNameComp = Component.literal("$abilityName ")
            .withStyle { it.withUnderlined(true).withColor(0xFFFFFF) }
        val descPart1 = Component.literal("- $abilityDescPart1")
            .withStyle { it.withColor(0xFFFFFF) }

        val line1 = Component.literal("")
        line1.append(abilityLabel)
        line1.append(abilityNameComp)
        line1.append(descPart1)
        return line1
    }

    /**
     * Builds the second part of the ability description, if applicable.
     */
    fun buildAbilityLinePart2(
        abilityObj: Any?,
        abilityDescKey: String?
    ): MutableComponent? {
        if (abilityObj == null || abilityDescKey == null) {
            return null
        }
        val abilityDesc = Component.translatable(abilityDescKey).string
        val splitLength1 = "Lorem ipsum odor amet, consectetuer adipis".length
        val splitLength2 = "cing elit. Elit pretium hac; habitant primis et risus. Ac feugiat la".length
        if (abilityDesc.length <= splitLength1) {
            return null
        }
        val abilityDescPart2 = if (abilityDesc.length >= splitLength1 + splitLength2) {
            abilityDesc.substring(splitLength1, splitLength1 + splitLength2)
        } else {
            abilityDesc.substring(splitLength1)
        }
        return Component.literal(abilityDescPart2)
            .withStyle { it.withColor(0xFFFFFF) }
    }

    /**
     * Builds the third part of the ability description, if the text is long enough.
     */
    fun buildAbilityLinePart3(
        abilityObj: Any?,
        abilityDescKey: String?
    ): MutableComponent? {
        if (abilityObj == null || abilityDescKey == null) {
            return null
        }
        val abilityDesc = Component.translatable(abilityDescKey).string
        val splitLength1 = "Lorem ipsum odor amet, consectetuer adipis".length
        val splitLength2 = "cing elit. Elit pretium hac; habitant primis et risus. Ac feugiat la".length
        if (abilityDesc.length <= splitLength1 + splitLength2) {
            return null
        }
        val abilityDescPart3 = abilityDesc.substring(splitLength1 + splitLength2)
        return Component.literal(abilityDescPart3)
            .withStyle { it.withColor(0xFFFFFF) }
    }

    /**
     * Merges two stat lines side by side on a single line, with a given spacing.
     * If one of the lines is null, returns the other.
     */
    fun mergeTwoStatsSideBySide(
        leftStat: MutableComponent?,
        rightStat: MutableComponent?,
        spacing: Int
    ): MutableComponent? {
        if (null == leftStat && null == rightStat) {
            return null
        }
        if (null == leftStat) {
            return rightStat
        }
        if (null == rightStat) {
            return leftStat
        }
        val combined = Component.literal("")
        combined.append(leftStat)
        repeat(spacing) {
            combined.append(Component.literal(" "))
        }
        combined.append(rightStat)
        return combined
    }
}
