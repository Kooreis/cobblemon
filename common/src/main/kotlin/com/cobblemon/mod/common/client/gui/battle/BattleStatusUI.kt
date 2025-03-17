/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.cobblemon.mod.common.client.gui.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.battle.BattleStatusLogic
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.MutableComponent

/**
 * BattleStatsUI is a client-side renderer that draws battle stat information
 * by leveraging the common BattleStatusLogic. It retrieves battle data,
 * builds stat lines for each Pokémon on both sides, then renders them along with
 * background frames.
 */
class BattleStatsUI {

    // Client-only background texture for the stat box.
    private val battleStatusFrameResource = cobblemonResource("textures/gui/battle/stat_log.png")

    /**
     * Renders the battle stat UI.
     */
    fun render(context: GuiGraphics) {
        // Get the current battle instance via the client
        val battleIdClient = CobblemonClient.battle?.battleId
        val battle = battleIdClient?.let { Cobblemon.battleRegistry.getBattle(it) } ?: return

        // Determine the number of Pokémon per side based on the battle format.
        val pokemonPerSide = battle.format.battleType.pokemonPerSide

        // Prepare columns for the player and opponent stat lines.
        val playerColumns = mutableListOf<List<MutableComponent>>()
        val opponentColumns = mutableListOf<List<MutableComponent>>()

        // Loop over each Pokémon slot and build stat lines using BattleStatusLogic.
        for (i in 0 until pokemonPerSide) {
            // Player side: Retrieve the Pokémon and its boost contexts.
            val playerSlot = battle.side1.actors.firstOrNull()?.activePokemon?.getOrNull(i)
            val playerPokemon = playerSlot?.battlePokemon?.effectedPokemon
            val playerBoost = playerSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.BOOST)
            val playerUnboost = playerSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.UNBOOST)
            val playerLines = BattleStatusLogic.buildStatLines(playerPokemon, playerBoost, playerUnboost, isOpponent = false)
            playerColumns.add(playerLines)

            // Opponent side: Retrieve the Pokémon and its boost contexts.
            val oppSlot = battle.side2.actors.firstOrNull()?.activePokemon?.getOrNull(i)
            val oppPokemon = oppSlot?.battlePokemon?.effectedPokemon
            val oppBoost = oppSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.BOOST)
            val oppUnboost = oppSlot?.battlePokemon?.contextManager?.get(BattleContext.Type.UNBOOST)
            val oppLines = BattleStatusLogic.buildStatLines(oppPokemon, oppBoost, oppUnboost, isOpponent = true)
            opponentColumns.add(oppLines)
        }

        // Define positions and dimensions for drawing.
        val boxX = 20
        val boxY = 150
        val baseBgWidth = 270
        val baseBgHeight = 75

        val playerBgWidth = baseBgWidth
        val opponentBgWidth = baseBgWidth

        val mc = Minecraft.getInstance()
        val screenWidth = mc.window.guiScaledWidth

        val playerBgX = boxX
        val playerBgY = boxY
        val opponentBgX = screenWidth - opponentBgWidth - 10
        val opponentBgY = boxY

        // Draw background frames for player and opponent columns.
        context.blit(
            battleStatusFrameResource,
            playerBgX, playerBgY,
            0f, 0f,
            playerBgWidth, baseBgHeight * playerColumns.size,
            baseBgWidth, baseBgHeight
        )
        context.blit(
            battleStatusFrameResource,
            opponentBgX, opponentBgY,
            0f, 0f,
            opponentBgWidth, baseBgHeight * opponentColumns.size,
            baseBgWidth, baseBgHeight
        )

        // Render text columns using the shared drawing function.
        val fontResource = CobblemonResources.DEFAULT_LARGE
        val padding = 8
        val lineHeight = 10

        // Draw the player's stat columns.
        var currentY = playerBgY + padding
        for (column in playerColumns) {
            val colX = playerBgX + padding
            for (line in column) {
                // drawScaledText is assumed to be a client-side utility method for rendering text.
                com.cobblemon.mod.common.client.render.drawScaledText(
                    context = context,
                    font = fontResource,
                    text = line,
                    x = colX,
                    y = currentY,
                    scale = 1f,
                    colour = line.style.color?.value ?: 0xFFFFFF,
                    centered = false,
                    shadow = false
                )
                currentY += lineHeight
            }
            currentY += lineHeight // Extra spacing between Pokémon columns.
        }

        // Draw the opponent's stat columns.
        currentY = opponentBgY + padding
        for (column in opponentColumns) {
            val colX = opponentBgX + padding
            for (line in column) {
                com.cobblemon.mod.common.client.render.drawScaledText(
                    context = context,
                    font = fontResource,
                    text = line,
                    x = colX,
                    y = currentY,
                    scale = 1f,
                    colour = line.style.color?.value ?: 0xFF5555,
                    centered = false,
                    shadow = false
                )
                currentY += lineHeight
            }
            currentY += 20 // Extra spacing between opponent Pokémon.
        }
    }
}
