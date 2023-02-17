/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.model.actor

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import java.util.UUID

abstract class AIBattleActor(
    gameId: UUID,
    pokemonList: List<BattlePokemon>,
    val battleAI: BattleAI
) : BattleActor(gameId, pokemonList.toMutableList()) {
    override fun sendUpdate(packet: NetworkPacket) {
        super.sendUpdate(packet)

        if (packet is BattleMakeChoicePacket) {
            setActionResponses(request!!.iterate(this.activePokemon, battleAI::choose) )
        }
    }
}