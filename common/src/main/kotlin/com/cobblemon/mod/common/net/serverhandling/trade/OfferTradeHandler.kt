/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.trade

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.trade.OfferTradePacket
import com.cobblemon.mod.common.net.serverhandling.RequestInteractionsHandler
import com.cobblemon.mod.common.trade.TradeManager
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import net.minecraft.entity.LivingEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.RaycastContext

object OfferTradeHandler : ServerNetworkPacketHandler<OfferTradePacket> {
    override fun handle(packet: OfferTradePacket, server: MinecraftServer, player: ServerPlayer) {
        if (player.isSpectator) return
        // Check if player has los and in range
        val targetPlayerEntity = packet.offeredPlayerId.getPlayer() ?: return
        if (player.traceFirstEntityCollision(
                        entityClass = LivingEntity::class.java,
                        ignoreEntity = player,
                        maxDistance = RequestInteractionsHandler.MAX_TRADE_DISTANCE.toFloat(),
                        collideBlock = RaycastContext.FluidHandling.NONE
                ) == targetPlayerEntity) {
            TradeManager.offerTrade(player, packet.offeredPlayerId.getPlayer() ?: return)
        }
    }
}