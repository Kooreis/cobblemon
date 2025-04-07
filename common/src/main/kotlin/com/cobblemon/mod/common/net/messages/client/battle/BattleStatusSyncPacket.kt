package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.*

class BattleStatusSyncPacket(val battleId: UUID) : NetworkPacket<BattleStatusSyncPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(battleId)
    }

    companion object {
        val ID = cobblemonResource("battle_status_sync")

        fun decode(buffer: RegistryFriendlyByteBuf): BattleStatusSyncPacket {
            val battleId = buffer.readUUID()
            return BattleStatusSyncPacket(battleId)
        }
    }
}