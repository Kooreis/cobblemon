package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.battle.BattleStatusSyncPacket
import net.minecraft.client.Minecraft

object BattleStatusSyncHandler : ClientNetworkPacketHandler<BattleStatusSyncPacket> {
    override fun handle(packet: BattleStatusSyncPacket, client: Minecraft) {
        CobblemonClient.battle?.battleId = packet.battleId // <- Save the received battle ID
    }
}
