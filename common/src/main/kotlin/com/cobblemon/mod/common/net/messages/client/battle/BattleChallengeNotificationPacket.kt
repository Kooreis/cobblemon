/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent
import java.util.*

/**
 * Packet send when a player has challenged to battle. The responsibility
 * of this packet currently is to send a battle challenge message that includes
 * the keybind to challenge them back. In future this is likely to include information
 * about the battle.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleChallengeNotificationHandler].
 *
 * @author Hiroku
 * @since August 5th, 2022
 */
class BattleChallengeNotificationPacket(
    val battleChallengeId: UUID,
    val challengerId: UUID,
    val challengerName: MutableComponent
): NetworkPacket<BattleChallengeNotificationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(battleChallengeId)
        buffer.writeUUID(challengerId)
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, challengerName)
    }

    companion object {
        val ID = cobblemonResource("battle_challenge_notification")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleChallengeNotificationPacket(buffer.readUUID(), buffer.readUUID(), ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer).copy())
    }
}