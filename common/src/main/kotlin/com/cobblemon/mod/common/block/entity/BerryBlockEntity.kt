/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.api.berry.GrowthPoint
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.berry.BerryHarvestEvent
import com.cobblemon.mod.common.block.BerryBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent

class BerryBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(CobblemonBlockEntities.BERRY, pos, state) {
    private val ticksPerMinute = 1200
    /**
     * The number of life cycles on this plant.
     * @throws IllegalArgumentException if the cycles are set to less than 0.
     */

    /*
    var lifeCycles: Int = this.berry()?.lifeCycles?.random() ?: 1
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("You cannot set the life cycles to less than 0")
            }
            if (field != value) {
                this.markDirty()
            }
            field = value
        }
    */

    private val lowerGrowthLimit = this.berry()!!.growthTime.first * ticksPerMinute / BerryBlock.FRUIT_AGE
    private val upperGrowthLimit = this.berry()!!.growthTime.last * ticksPerMinute / BerryBlock.FRUIT_AGE
    private val growthRange: IntRange = lowerGrowthLimit..upperGrowthLimit

    var growthTimer: Int = growthRange.random() ?: 72000
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("You cannot set the growth time to less than zero")
            }
            if (field != value) {
                this.markDirty()
            }
            field = value
        }

    private val lowerRefreshLimit = this.berry()!!.growthTime.first * ticksPerMinute
    private val upperRefreshLimit = this.berry()!!.growthTime.last * ticksPerMinute
    private val refreshRange: IntRange = lowerRefreshLimit..upperRefreshLimit

    var refreshTimer: Int = refreshRange.random() ?: 24000
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("You cannot set the refresh time to less than zero")
            }
            if (field != value) {
                this.markDirty()
            }
            field = value
        }

    private val growthPoints = arrayListOf<Identifier>()

    // Just a cheat to not invoke markDirty unnecessarily
    private var wasLoading = false

    /**
     * Returns the [BerryBlock] behind this entity.
     *
     * @return The [BerryBlock].
     */
    fun berryBlock() = this.cachedState.block as BerryBlock

    /**
     * Returns the [Berry] behind this entity,
     * This will be null if it doesn't exist in the [Berries] registry.
     *
     * @return The [Berry] if existing.
     */
    fun berry() = this.berryBlock().berry()

    /**
     * Generates a random amount of growth points for this tree.
     *
     * @param world The [World] the tree is in.
     * @param state The [BlockState] of the tree.
     * @param pos The [BlockPos] of the tree.
     * @param placer The [LivingEntity] tending to the tree if any.
     */
    fun generateGrowthPoints(world: World, state: BlockState, pos: BlockPos, placer: LivingEntity?) {
        val berry = this.berry() ?: return
        val yield = berry.calculateYield(world, state, pos, placer)
        this.growthPoints.clear()
        repeat(yield) {
            this.growthPoints += berry.identifier
        }
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS)
        this.markDirty()
    }

    /**
     * Calculates the berry produce in [ItemStack] form.
     *
     * @param world The [World] the tree is in.
     * @param state The [BlockState] of the tree.
     * @param pos The [BlockPos] of the tree.
     * @param player The [PlayerEntity] harvesting the tree.
     * @return The resulting [ItemStack]s to be dropped.
     */
    fun harvest(world: World, state: BlockState, pos: BlockPos, player: PlayerEntity): Collection<ItemStack> {
        val drops = arrayListOf<ItemStack>()
        //if (this.lifeCycles <= 0) {
        //    this.consumeLife(world, pos, state, player)
        //    return drops
        //}
        refresh(world, pos, state, player)
        val unique = this.growthPoints.groupingBy { it }.eachCount()
        unique.forEach { (identifier, amount) ->
            val berryItem = Berries.getByIdentifier(identifier)?.item()
            if (berryItem != null) {
                var remain = amount
                while (remain > 0) {
                    val count = remain.coerceAtMost(berryItem.maxCount)
                    drops += ItemStack(berryItem, count)
                    remain -= count
                }
            }
        }
        this.berry()?.let { berry ->
            if (player is ServerPlayerEntity) {
                CobblemonEvents.BERRY_HARVEST.post(BerryHarvestEvent(berry, player, world, pos, state, this, drops))
            }
        }
        //this.consumeLife(world, pos, state, player)
        return drops
    }

    override fun readNbt(nbt: NbtCompound) {
        this.wasLoading = true
        this.growthPoints.clear()
        this.growthTimer = nbt.getInt(GROWTH_TIMER).coerceAtLeast(0)
        this.refreshTimer = nbt.getInt(REFRESH_TIMER).coerceAtLeast(0)
        //this.lifeCycles = nbt.getInt(LIFE_CYCLES).coerceAtLeast(0)
        nbt.getList(GROWTH_POINTS, NbtList.STRING_TYPE.toInt()).filterIsInstance<NbtString>().forEach { element ->
            // In case some 3rd party mutates the NBT incorrectly
            try {
                val identifier = Identifier(element.asString())
                this.growthPoints += identifier
            } catch (ignored: InvalidIdentifierException) {}
        }
        this.wasLoading = false
    }

    override fun writeNbt(nbt: NbtCompound) {
        //nbt.putInt(LIFE_CYCLES, this.lifeCycles)
        nbt.putInt(GROWTH_TIMER, this.growthTimer)
        nbt.putInt(REFRESH_TIMER, this.refreshTimer)
        val list = NbtList()
        list += this.growthPoints.map { NbtString.of(it.toString()) }
        nbt.put(GROWTH_POINTS, list)
    }

    override fun markDirty() {
        if (!this.wasLoading) {
            super.markDirty()
        }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return this.createNbt()
    }

    /**
     * Collects each [Berry] and [GrowthPoint].
     *
     * @return Collection of the [Berry] and [GrowthPoint].
     */
    internal fun berryAndGrowthPoint(): Collection<Pair<Berry, GrowthPoint>> {
        val baseBerry = this.berry() ?: return emptyList()
        val berryPoints = arrayListOf<Pair<Berry, GrowthPoint>>()
        for ((index, identifier) in this.growthPoints.withIndex()) {
            val berry = Berries.getByIdentifier(identifier) ?: continue
            berryPoints.add(berry to baseBerry.growthPoints[index])
        }
        return berryPoints
    }

    /**
     * Inserts the given [berry] as a mutation in a random [growthPoints].
     *
     * @param berry The [Berry] being mutated.
     */
    internal fun mutate(berry: Berry) {
        if (this.growthPoints.isEmpty()) {
            return
        }
        val index = this.growthPoints.indices.random()
        this.growthPoints[index] = berry.identifier
        this.markDirty()
    }

    private fun refresh(world: World, pos: BlockPos, state:BlockState, player: PlayerEntity) {
        world.setBlockState(pos, state.with(BerryBlock.AGE, 3), Block.NOTIFY_LISTENERS)
        world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos)
        this.generateGrowthPoints(world, state, pos, player)
        this.growthTimer = this.refreshTimer / BerryBlock.FRUIT_AGE
        return
    }

    /*
    private fun consumeLife(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        if (this.lifeCycles > 1) {
            this.lifeCycles--
            world.setBlockState(pos, state.with(BerryBlock.AGE, 0), Block.NOTIFY_LISTENERS)
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos)
            this.generateGrowthPoints(world, state, pos, player)
            return
        }
        world.breakBlock(pos, false)
    }
    */

    //To-Do BlockEntityTicker (see HealingMachineEntity.kt)
    companion object {

        //private const val LIFE_CYCLES = "life_cycles"
        private const val GROWTH_POINTS = "growth_points"
        private const val GROWTH_TIMER = "growth_timer"
        private const val REFRESH_TIMER = "refresh_timer"

    }

}