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
import kotlin.reflect.full.memberProperties

// Helper function that uses reflection to read an "id" property from an object.
private fun getId(item: Any?): String? {
    return item?.let {
        it::class.memberProperties.find { prop -> prop.name == "id" }?.getter?.call(it) as? String
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

    // For non-HP stats, these IDs are used to count boosts.
    private val statIdMap = mapOf(
        Stats.ATTACK to "atk",
        Stats.DEFENCE to "def",
        Stats.SPECIAL_ATTACK to "spa",
        Stats.SPECIAL_DEFENCE to "spd",
        Stats.SPEED to "spe"
    )

    // Use the visual background texture.
    private val battleStatusFrameResource = cobblemonResource("textures/gui/battle/stat_log_singles.png")

    fun render(context: GuiGraphics) {
        // 1) Retrieve the current battle.
        val battleIdClient = CobblemonClient.battle?.battleId
        val battle = battleIdClient?.let { Cobblemon.battleRegistry.getBattle(it) } ?: return

        // 2) Determine how many Pokémon per side (singles/doubles/triples).
        val battleType = battle.format.battleType.pokemonPerSide

        // Build columns for each Pokémon on both sides.
        val playerColumns = mutableListOf<List<MutableComponent>>()
        val opponentColumns = mutableListOf<List<MutableComponent>>()

        // 3) For each slot, retrieve that Pokémon’s boost contexts individually.
        for (i in 0 until battleType) {
            // Player side (side1)
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

            // Opponent side (side2)
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

        // 4) Decide how wide to make the backgrounds. Each column is 200 wide.
        val baseBgWidth = 200
        val baseBgHeight = 300 // Taller to fit more lines.
        val playerBgWidth = baseBgWidth * playerColumns.size
        val opponentBgWidth = baseBgWidth * opponentColumns.size

        val mc = Minecraft.getInstance()
        val screenWidth = mc.window.guiScaledWidth
        val screenHeight = mc.window.guiScaledHeight

        val playerBgX = 12
        val playerBgY = screenHeight / 5
        val opponentBgX = screenWidth - opponentBgWidth - 12
        val opponentBgY = playerBgY

        // 5) Draw the backgrounds for each side.
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

        // 6) Render text columns for each side.
        val fontResource = CobblemonResources.DEFAULT_LARGE
        val padding = 8
        val lineHeight = 10 // spacing between lines

        // Player columns.
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

        // Opponent columns.
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
     * Build a list of stat lines for one Pokémon.
     * For non-HP stats, the base stat from StatProvider is multiplied by the boost multiplier,
     * which is computed by counting the occurrences in the provided boost lists using their "id".
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
                // Use the boost lists directly, computing the count by checking each boost's id.
                val statId = statIdMap[stat] ?: ""
                val plus = (posContext as? List<*>)?.count { getId(it) == statId } ?: 0
                val minus = (negContext as? List<*>)?.count { getId(it) == statId } ?: 0
                val boost = plus - minus
                val multiplier = statMultipliers[boost] ?: 1.0
                val newValue = (baseValue * multiplier).toInt()

                val basePart = "$label: $baseValue "
                val boostColor = when {
                    multiplier > 1.0 -> 0x55FF55
                    multiplier < 1.0 -> 0xFF5555
                    else -> 0xFFFFFF
                }
                val boostPart = "(${multiplier}x) = $newValue"
                val combined = Component.literal(basePart).withStyle { it.withColor(0xFFFFFF) }
                combined.append(Component.literal(boostPart).withStyle { it.withColor(boostColor) })
                lines.add(combined)
            }
        }
        return lines
    }
}
