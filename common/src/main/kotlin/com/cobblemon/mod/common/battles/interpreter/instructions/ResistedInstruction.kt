package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-resisted|POKEMON
 *
 * POKEMON resisted the attack.
 * @author Hunter
 * @since August 18th, 2022
 */
class ResistedInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchGo {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchGo
            battle.broadcastChatMessage(battleLang("resisted"))
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}