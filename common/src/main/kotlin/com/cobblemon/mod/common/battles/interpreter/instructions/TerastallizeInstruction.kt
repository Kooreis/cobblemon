package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-terastallize|POKEMON|TYPE
 *
 * POKEMON terastallized into TYPE.
 * @author Segfault Guy
 * @since September 10th, 2023
 */
class TerastallizeInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = message.battlePokemon(0, battle) ?: return
        val type = message.effectAt(1)?.let { ElementalTypes.get(it.id) } ?: return
        battle.dispatchWaiting {
            val pokemonName = battlePokemon.getName()
            battle.broadcastChatMessage(battleLang("terastallize", pokemonName, type.displayName).yellow())
            battle.minorBattleActions[battlePokemon.uuid] = message
        }
    }
}