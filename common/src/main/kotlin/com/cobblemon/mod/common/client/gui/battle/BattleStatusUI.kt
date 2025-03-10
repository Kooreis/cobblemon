package com.cobblemon.mod.common.client.gui.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

// Reflection helper to call getId() if no interface is available.
private fun readId(obj: Any?): String? {
    if (obj == null) return null
    return try {
        val method = obj::class.java.getMethod("getId")
        method.invoke(obj) as? String
    } catch (e: Exception) {
        null
    }
}

class BattleStatusUI {

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

    /**
     * We’re omitting HP from our displayed stats.
     * The layout will be:
     *  - Row 1: ATK + SP. ATK
     *  - Row 2: DEF + SP. DEF
     *  - Row 3: SPD alone
     *  - Row 4: Ability & Nature on the same line
     */
    private val statOrder = listOf(
        Stats.ATTACK,
        Stats.SPECIAL_ATTACK,
        Stats.DEFENCE,
        Stats.SPECIAL_DEFENCE,
        Stats.SPEED
    )

    // Labels for each stat.
    private val statLabelMap = mapOf(
        Stats.ATTACK to "ATK",
        Stats.DEFENCE to "DEF",
        Stats.SPECIAL_ATTACK to "SP. ATK",
        Stats.SPECIAL_DEFENCE to "SP. DEF",
        Stats.SPEED to "SPD"
    )

    // For non-HP stats, these IDs are used to count boosts in pos/neg contexts.
    private val statIdMap = mapOf(
        Stats.ATTACK to "atk",
        Stats.DEFENCE to "def",
        Stats.SPECIAL_ATTACK to "spa",
        Stats.SPECIAL_DEFENCE to "spd",
        Stats.SPEED to "spe"
    )

    // Background texture
    private val battleStatusFrameResource = cobblemonResource("textures/gui/battle/stat_log.png")

    fun render(context: GuiGraphics) {
        val battleIdClient = CobblemonClient.battle?.battleId
        val battle = battleIdClient?.let { Cobblemon.battleRegistry.getBattle(it) } ?: return

        val battleType = battle.format.battleType.pokemonPerSide

        val playerColumns = mutableListOf<List<MutableComponent>>()
        val opponentColumns = mutableListOf<List<MutableComponent>>()

        // For each slot, retrieve that Pokémon’s data and build lines
        for (i in 0 until battleType) {
            // Player side
            val playerSlot = battle.side1.actors.firstOrNull()?.activePokemon?.getOrNull(i)
            val playerPokemon = playerSlot?.battlePokemon?.effectedPokemon
            val playerPosBoostContext = playerSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.BOOST)
            val playerNegBoostContext = playerSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.UNBOOST)
            val playerLines = buildStatLines(
                pkmn = playerPokemon,
                posContext = playerPosBoostContext,
                negContext = playerNegBoostContext
            )
            playerColumns.add(playerLines)

            // Opponent side
            val oppSlot = battle.side2.actors.firstOrNull()?.activePokemon?.getOrNull(i)
            val oppPokemon = oppSlot?.battlePokemon?.effectedPokemon
            val oppPosBoostContext = oppSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.BOOST)
            val oppNegBoostContext = oppSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.UNBOOST)
            val oppLines = buildStatLines(
                pkmn = oppPokemon,
                posContext = oppPosBoostContext,
                negContext = oppNegBoostContext,
                isOpponent = true
            )
            opponentColumns.add(oppLines)
        }

        // Position near top-left or top-right corners. Adjust as needed.
        val boxX = 10
        val boxY = 50

        // We'll guess 160×110 for each column. Adjust if you need more space.
        val baseBgWidth = 270
        val baseBgHeight = 60

        val playerBgWidth = baseBgWidth * playerColumns.size
        val opponentBgWidth = baseBgWidth * opponentColumns.size

        val mc = Minecraft.getInstance()
        val screenWidth = mc.window.guiScaledWidth

        val playerBgX = boxX
        val playerBgY = boxY
        val opponentBgX = screenWidth - opponentBgWidth - 10
        val opponentBgY = boxY

        // Draw backgrounds for each side
        context.blit(
            battleStatusFrameResource,
            playerBgX, playerBgY,
            0f, 0f,
            playerBgWidth, baseBgHeight,
            baseBgWidth, baseBgHeight
        )
        context.blit(
            battleStatusFrameResource,
            opponentBgX, opponentBgY,
            0f, 0f,
            opponentBgWidth, baseBgHeight,
            baseBgWidth, baseBgHeight
        )

        // Render text columns
        val fontResource = CobblemonResources.DEFAULT_LARGE
        val padding = 8
        val lineHeight = 10

        // Player columns
        playerColumns.forEachIndexed { i, columnLines ->
            val colX = playerBgX + padding + i * baseBgWidth
            var currentY = playerBgY + padding
            columnLines.forEach { line ->
                val fallbackColor = line.style.color?.value ?: 0xFFFFFF
                drawScaledText(
                    context = context,
                    font = fontResource,
                    text = line,
                    x = colX,
                    y = currentY,
                    scale = 1f,
                    colour = fallbackColor,
                    centered = false,
                    shadow = false
                )
                currentY += lineHeight
            }
        }

        // Opponent columns
        opponentColumns.forEachIndexed { i, columnLines ->
            val colX = opponentBgX + padding + i * baseBgWidth
            var currentY = opponentBgY + padding
            columnLines.forEach { line ->
                val fallbackColor = line.style.color?.value ?: 0xFF5555
                drawScaledText(
                    context = context,
                    font = fontResource,
                    text = line,
                    x = colX,
                    y = currentY,
                    scale = 1f,
                    colour = fallbackColor,
                    centered = false,
                    shadow = false
                )
                currentY += lineHeight
            }
        }
    }

    /**
     * Builds lines for one Pokémon’s stats using a 3-row layout plus an Ability/Nature row:
     *
     * Row 1: ATK + SP. ATK
     * Row 2: DEF + SP. DEF
     * Row 3: SPD alone
     * Row 4: Ability & Nature on the same line
     */
    private fun buildStatLines(
        pkmn: Pokemon?,
        posContext: Any?,
        negContext: Any?,
        isOpponent: Boolean = false
    ): List<MutableComponent> {
        if (pkmn == null) {
            return listOf(Component.literal("No Pokémon found."))
        }

        // Evs might be a custom class, e.g. data class Evs(...) with fields
        val evs = pkmn.evs // The object that holds EV fields
        val abilityObj = pkmn.ability
        val natureObj = pkmn.nature

        // Build lines for each relevant stat
        val atkLine = buildSingleStatLine(pkmn, Stats.ATTACK, posContext, negContext, evs)
        val spAtkLine = buildSingleStatLine(pkmn, Stats.SPECIAL_ATTACK, posContext, negContext, evs)
        val defLine = buildSingleStatLine(pkmn, Stats.DEFENCE, posContext, negContext, evs)
        val spDefLine = buildSingleStatLine(pkmn, Stats.SPECIAL_DEFENCE, posContext, negContext, evs)
        val spdLine = buildSingleStatLine(pkmn, Stats.SPEED, posContext, negContext, evs)

        // Row 1: ATK + SP. ATK
        val row1 = mergeTwoStatsSideBySide(atkLine, spAtkLine, spacing = 3)

        // Row 2: DEF + SP. DEF
        val row2 = mergeTwoStatsSideBySide(defLine, spDefLine, spacing = 3)

        // Row 3: SPD alone
        // Just add SPD line directly

        // Row 4: Ability + Nature
        val abilityNatureLine = buildAbilityNatureLine(abilityObj, natureObj)

        // Collect final lines
        val lines = mutableListOf<MutableComponent>()
        row1?.let { lines.add(it) }
        row2?.let { lines.add(it) }
        spdLine?.let { lines.add(it) }
        abilityNatureLine?.let { lines.add(it) }

        return lines
    }

    /**
     * Builds a single stat line, e.g. "ATK: 56 (1.0x) = 56 (252 EVs)"
     * - If EV is 0, show "(0 EVs)" in red
     * - If EV is > 0, show "(NN EVs)" in green
     */
    private fun buildSingleStatLine(
        pkmn: Pokemon,
        stat: Stats,
        posContext: Any?,
        negContext: Any?,
        evsObj: Any? // We'll interpret this as a custom "Evs" class
    ): MutableComponent? {
        val label = statLabelMap[stat] ?: return null
        val baseValue = Cobblemon.statProvider.getStatForPokemon(pkmn, stat) ?: 0

        // Calculate boost multiplier
        val statId = statIdMap[stat] ?: ""
        val plus = (posContext as? List<*>)?.count { readId(it) == statId } ?: 0
        val minus = (negContext as? List<*>)?.count { readId(it) == statId } ?: 0
        val boost = plus - minus
        val multiplier = statMultipliers[boost] ?: 1.0
        val newValue = (baseValue * multiplier).toInt()

        // Retrieve EV from the custom Evs class
        val evVal = getEvValue(evsObj, stat)
        val evColor = if (evVal == 0) 0xFF5555 else 0x55FF55

        // Build partial coloring
        val identifier = Component.literal("$label:").withStyle { it.withBold(true).withColor(0xFFFFFF) }
        val baseComp = Component.literal(" $baseValue ").withStyle { it.withColor(0xFFFFFF) }
        val openParen = Component.literal("(").withStyle { it.withColor(0xFFFFFF) }
        val multiplierColor = when {
            multiplier > 1.0 -> 0x55FF55
            multiplier < 1.0 -> 0xFF5555
            else -> 0xFFFFFF
        }
        val multiplierComp = Component.literal("$multiplier").withStyle { it.withColor(multiplierColor) }
        val xComp = Component.literal("x").withStyle { it.withColor(0xFFFFFF) }
        val closeParen = Component.literal(")").withStyle { it.withColor(0xFFFFFF) }
        val equalsComp = Component.literal(" = ").withStyle { it.withColor(0xFFFFFF) }
        val finalComp = Component.literal("$newValue").withStyle { it.withColor(multiplierColor) }

        // EV portion e.g. " (252 EVs)"
        val evString = " ($evVal EVs)"
        val evComp = Component.literal(evString).withStyle { it.withColor(evColor) }

        val combined = Component.literal("")
        combined.append(identifier)
        combined.append(baseComp)
        combined.append(openParen)
        combined.append(multiplierComp)
        combined.append(xComp)
        combined.append(closeParen)
        combined.append(equalsComp)
        combined.append(finalComp)
        combined.append(evComp) // Add the EV info
        return combined
    }

    /**
     * Extracts the EV value for a given stat from your custom Evs object.
     * Example:
     *   data class Evs(val hp: Int, val attack: Int, val defence: Int, ...)
     */
    private fun getEvValue(evsObj: Any?, stat: Stats): Int {
        if (evsObj == null) return 0
        return try {
            // If you have a data class, do a 'when' on the stat to call the correct property:
            when (stat) {
                Stats.HP -> evsObj::class.java.getMethod("getHp").invoke(evsObj) as? Int
                Stats.ATTACK -> evsObj::class.java.getMethod("getAttack").invoke(evsObj) as? Int
                Stats.DEFENCE -> evsObj::class.java.getMethod("getDefence").invoke(evsObj) as? Int
                Stats.SPECIAL_ATTACK -> evsObj::class.java.getMethod("getSpecialAttack").invoke(evsObj) as? Int
                Stats.SPECIAL_DEFENCE -> evsObj::class.java.getMethod("getSpecialDefence").invoke(evsObj) as? Int
                Stats.SPEED -> evsObj::class.java.getMethod("getSpeed").invoke(evsObj) as? Int
                else -> 0
            } ?: 0
        } catch (ex: Exception) {
            0
        }
    }

    /**
     * Creates a single line: "Ability: <AB>    Nature: <NA>"
     * with bold labels. If ability/nature are null, display "???".
     */
    private fun buildAbilityNatureLine(
        abilityObj: Any?,
        natureObj: Any?
    ): MutableComponent? {
        // If both are null, we can skip
        if (abilityObj == null && natureObj == null) {
            return null
        }

        // Retrieve ability name. Adjust if you have a direct .name property or getDisplayName, etc.
        val abilityName = try {
            abilityObj?.let {
                val method = it::class.java.getMethod("getName")
                method.invoke(it) as? String
            }
        } catch (ex: Exception) {
            null
        } ?: "???"

        // Retrieve nature name similarly
        val natureName = try {
            natureObj?.let {
                val method = it::class.java.getMethod("getName")
                method.invoke(it) as? String
            }
        } catch (ex: Exception) {
            null
        } ?: "???"

        // Build: "Ability: X" (bold) + spacing + "Nature: Y" (bold)
        val abilityLabel = Component.literal("Ability:").withStyle { it.withBold(true).withColor(0xFFFFFF) }
        val abilityVal = Component.literal(" $abilityName ").withStyle { it.withColor(0xFFFFFF) }

        val natureLabel = Component.literal("Nature:").withStyle { it.withBold(true).withColor(0xFFFFFF) }
        val natureVal = Component.literal(" $natureName").withStyle { it.withColor(0xFFFFFF) }

        val space = Component.literal("    ") // some spacing between them

        val line = Component.literal("")
        line.append(abilityLabel)
        line.append(abilityVal)
        line.append(space)
        line.append(natureLabel)
        line.append(natureVal)

        return line
    }

    /**
     * Merges two stat lines side by side on a single line, with a small spacing in between.
     * If either line is null, we just show the one that exists.
     * Using === for null checks to avoid "no method equals(Any?)" issues on components.
     */
    private fun mergeTwoStatsSideBySide(
        leftStat: MutableComponent?,
        rightStat: MutableComponent?,
        spacing: Int
    ): MutableComponent? {
        if (leftStat === null && rightStat === null) {
            return null
        }
        if (leftStat === null) {
            return rightStat
        }
        if (rightStat === null) {
            return leftStat
        }
        // Both exist, so combine them with spacing
        val combined = Component.literal("")
        combined.append(leftStat)
        repeat(spacing) { combined.append(Component.literal(" ")) }
        combined.append(rightStat)
        return combined
    }
}
