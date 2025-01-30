package com.cobblemon.mod.common.client.gui.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.stats.StatProvider
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.ifIsType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class BattleStatusUI {

    fun render(context: GuiGraphics) {
        val battle = CobblemonClient.battle ?: return
        val playerPokemon = battle.side1.actors.firstOrNull()?.activePokemon?.firstOrNull()?.battlePokemon
        val truePlayerPokemon = playerPokemon?.actor?.pokemon?.find { it.uuid == playerPokemon?.uuid }
        val opponentPokemon = battle.side2.actors.firstOrNull()?.activePokemon?.firstOrNull()?.battlePokemon
        val trueOpponentPokemon = opponentPokemon?.actor?.pokemon?.find { it.uuid == opponentPokemon?.uuid }

        val screenWidth = Minecraft.getInstance().window.guiScaledWidth
        val screenHeight = Minecraft.getInstance().window.guiScaledHeight

        val x = screenWidth / 2 - 100  // Offset from the center
        val y = screenHeight / 5       // Position near the top

        context.drawString(
            Minecraft.getInstance().font,
            Component.literal("Your Pokémon: ${truePlayerPokemon?.getDisplayName()?.string ?: "Unknown"}"),
            x,
            y,
            0xFFFFFF, // White color
            false
        )

        context.drawString(
            Minecraft.getInstance().font,
            Component.literal("Opponent: ${trueOpponentPokemon?.getDisplayName()?.string ?: "Unknown"}"),
            x,
            y + 15,
            0xFF5555, // Red color for opponent
            false
        )

        val statLabels = listOf("HP", "ATK", "DEF", "SP. ATK", "SP. DEF", "SPD")
        val statsList = listOf(
            Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
        )

        for (i in statsList.indices) {
            val playerStatValue = truePlayerPokemon?.let { Cobblemon.statProvider.getStatForPokemon(truePlayerPokemon, statsList[i]) } ?: 0
            val opponentStatValue = trueOpponentPokemon?.let { Cobblemon.statProvider.getStatForPokemon(trueOpponentPokemon, statsList[i]) } ?: 0

            context.drawString(
                Minecraft.getInstance().font,
                Component.literal("${statLabels[i]}: $playerStatValue"),
                x,
                y + 30 + (i * 15),
                0xFFFFFF, // White color
                false
            )

            context.drawString(
                Minecraft.getInstance().font,
                Component.literal("${statLabels[i]}: $opponentStatValue"),
                x + 150,
                y + 30 + (i * 15),
                0xFF5555, // Red color for opponent
                false
            )
        }
    }
}

// Integrate into BattleOverlay

