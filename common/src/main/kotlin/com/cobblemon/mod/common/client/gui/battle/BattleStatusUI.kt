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

// Helper function to call getId() reflectively.
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

    private val statLabels = listOf("HP", "ATK", "DEF", "SP. ATK", "SP. DEF", "SPD")
    private val statsList = listOf(
        Stats.HP, Stats.ATTACK, Stats.DEFENCE,
        Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
    )

    // Map Stats -> "atk", "def", etc.
    private val statIdMap = mapOf(
        Stats.ATTACK to "atk",
        Stats.DEFENCE to "def",
        Stats.SPECIAL_ATTACK to "spa",
        Stats.SPECIAL_DEFENCE to "spd",
        Stats.SPEED to "spe"
    )

    // Background texture (the same file you're using).
    private val battleStatusFrameResource = cobblemonResource("textures/gui/battle/stat_log.png")

    fun render(context: GuiGraphics) {
        val battleIdClient = CobblemonClient.battle?.battleId
        val battle = battleIdClient?.let { Cobblemon.battleRegistry.getBattle(it) } ?: return

        val battleType = battle.format.battleType.pokemonPerSide

        val playerColumns = mutableListOf<List<MutableComponent>>()
        val opponentColumns = mutableListOf<List<MutableComponent>>()

        // Build columns for each slot, pulling boost contexts individually.
        for (i in 0 until battleType) {
            // Player side
            val playerSlot = battle.side1.actors.firstOrNull()?.activePokemon?.getOrNull(i)
            val playerPokemon = playerSlot?.battlePokemon?.effectedPokemon
            val playerPosBoostContext = playerSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.BOOST)
            val playerNegBoostContext = playerSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.UNBOOST)

            val playerTitle = Component.literal("Your Pokémon: ${playerPokemon?.getDisplayName()?.string ?: "???"}")
            val playerLines = buildStatLines(
                title = playerTitle,
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

            val oppTitle = Component.literal("Opponent Pokémon: ${oppPokemon?.getDisplayName()?.string ?: "???"}")
                .withStyle { it.withColor(0xFF5555) }
            val oppLines = buildStatLines(
                title = oppTitle,
                pkmn = oppPokemon,
                posContext = oppPosBoostContext,
                negContext = oppNegBoostContext
            )
            opponentColumns.add(oppLines)
        }

        // Draw smaller backgrounds that more tightly wrap the text.
        // Let’s do 160×110 for each column.
        val baseBgWidth = 160
        val baseBgHeight = 110
        val playerBgWidth = baseBgWidth * playerColumns.size
        val opponentBgWidth = baseBgWidth * opponentColumns.size

        val mc = Minecraft.getInstance()
        val screenWidth = mc.window.guiScaledWidth
        val screenHeight = mc.window.guiScaledHeight

        val playerBgX = 12
        val playerBgY = screenHeight / 5
        val opponentBgX = screenWidth - opponentBgWidth - 12
        val opponentBgY = playerBgY

        // Draw the background for player columns
        context.blit(
            battleStatusFrameResource,
            playerBgX, playerBgY,
            0f, 0f,
            playerBgWidth, baseBgHeight,
            baseBgWidth, baseBgHeight
        )
        // Draw the background for opponent columns
        context.blit(
            battleStatusFrameResource,
            opponentBgX, opponentBgY,
            0f, 0f,
            opponentBgWidth, baseBgHeight,
            baseBgWidth, baseBgHeight
        )

        // Now render each column’s lines.
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
     * Build the lines for each Pokémon’s stats, using reflection to read "id" from each boost.
     */
    private fun buildStatLines(
        title: MutableComponent,
        pkmn: Any?,
        posContext: Any?,
        negContext: Any?
    ): List<MutableComponent> {
        val lines = mutableListOf<MutableComponent>()
        lines.add(title)

        if (pkmn == null) {
            lines.add(Component.literal("No Pokémon found."))
            return lines
        }

        for ((i, stat) in statsList.withIndex()) {
            val label = statLabels[i]
            val baseValue = Cobblemon.statProvider.getStatForPokemon(pkmn as Pokemon, stat) ?: 0
            if (stat == Stats.HP) {
                lines.add(Component.literal("$label: $baseValue").withStyle { it.withColor(0xFFFFFF) })
            } else {
                val statId = statIdMap[stat] ?: ""
                val plus = (posContext as? List<*>)?.count { readId(it) == statId } ?: 0
                val minus = (negContext as? List<*>)?.count { readId(it) == statId } ?: 0

                val boost = plus - minus
                val multiplier = statMultipliers[boost] ?: 1.0
                val newValue = (baseValue * multiplier).toInt()

                // Compose the line with partial coloring.
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

                val combined = Component.literal("")
                combined.append(identifier)
                combined.append(baseComp)
                combined.append(openParen)
                combined.append(multiplierComp)
                combined.append(xComp)
                combined.append(closeParen)
                combined.append(equalsComp)
                combined.append(finalComp)
                lines.add(combined)
            }
        }
        return lines
    }
}
