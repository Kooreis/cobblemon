/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import com.cobblemon.mod.common.util.*
import com.mojang.serialization.MapCodec
import net.minecraft.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemPlacementContext
import net.minecraft.world.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Property
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.util.Mirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("DEPRECATED", "OVERRIDE_DEPRECATION")
class HealingMachineBlock(settings: Properties) : BaseEntityBlock(properties) {
    companion object {
        val CODEC: MapCodec<HealingMachineBlock> = createCodec(::HealingMachineBlock)

        private val NORTH_SOUTH_AABB = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.625, 1.0),
            VoxelShapes.cuboid(0.0625, 0.625, 0.0, 0.9375, 0.875, 0.125),
            VoxelShapes.cuboid(0.0625, 0.625, 0.875, 0.9375, 0.875, 1.0),
            VoxelShapes.cuboid(0.0625, 0.625, 0.125, 0.1875, 0.75, 0.875),
            VoxelShapes.cuboid(0.8125, 0.625, 0.125, 0.9375, 0.75, 0.875),
            VoxelShapes.cuboid(0.1875, 0.625, 0.125, 0.8125, 0.6875, 0.875)
        )

        private val WEST_EAST_AABB = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.625, 1.0),
            VoxelShapes.cuboid(0.875, 0.625, 0.0625, 1.0, 0.875, 0.9375),
            VoxelShapes.cuboid(0.0, 0.625, 0.0625, 0.125, 0.875, 0.9375),
            VoxelShapes.cuboid(0.125, 0.625, 0.0625, 0.875, 0.75, 0.1875),
            VoxelShapes.cuboid(0.125, 0.625, 0.8125, 0.875, 0.75, 0.9375),
            VoxelShapes.cuboid(0.125, 0.625, 0.1875, 0.875, 0.6875, 0.8125)
        )

        // Charge level 6 is used only when healing machine is active
        const val MAX_CHARGE_LEVEL = 5
        val CHARGE_LEVEL: IntProperty = IntProperty.of("charge", 0, MAX_CHARGE_LEVEL + 1)
    }

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
            .setValue(CHARGE_LEVEL, 0))
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(blockState: BlockState, blockGetter: BlockGetter, blockPos: BlockPos, collisionContext: CollisionContext): VoxelShape {
        return when (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
            Direction.WEST -> WEST_EAST_AABB
            Direction.EAST -> WEST_EAST_AABB
            else -> NORTH_SOUTH_AABB
        }
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return HealingMachineBlockEntity(blockPos, blockState)
    }

    override fun getStateForPlacement(blockPlaceContext: BlockPlaceContext): BlockState {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.horizontalDirection)
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun isPathfindable(
        blockState: BlockState,
        pathComputationType: PathComputationType
    ): Boolean {
        return false
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HorizontalDirectionalBlock.FACING)
        builder.add(*arrayOf<BlcokS<*>>(CHARGE_LEVEL))
    }

    override fun rotate(blockState: BlockState, rotation: Rotation): BlockState {
        return blockState.with(
            HorizontalDirectionalBlock.FACING, rotation.rotate(blockState.getValue(
                HorizontalDirectionalBlock.FACING)))
    }

    override fun mirror(blockState: BlockState, mirror: Mirror): BlockState {
        return blockState.rotate(mirror.getRotation(blockState.getValue(HorizontalDirectionalBlock.FACING)))
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: Level, pos: BlockPos?, newState: BlockState, moved: Boolean) {
        if (!state.`is`(newState.block)) super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun useWithoutItem(
        blockState: BlockState,
        world: Level,
        blockPos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS
        }

        val blockEntity = world.getBlockEntity(blockPos)
        if (blockEntity !is HealingMachineBlockEntity) {
            return InteractionResult.SUCCESS
        }

        if (blockEntity.isInUse) {
            player.sendSystemMessage(lang("healingmachine.alreadyinuse").red(), true)
            return InteractionResult.SUCCESS
        }

        val serverPlayerEntity = player as ServerPlayer
        if (serverPlayerEntity.isInBattle()) {
            player.sendSystemMessage(lang("healingmachine.inbattle").red(), true)
            return InteractionResult.SUCCESS
        }
        val party = serverPlayerEntity.party()
        if (party.none()) {
            player.sendSystemMessage(lang("healingmachine.nopokemon").red(), true)
            return InteractionResult.SUCCESS
        }

        if (party.none { pokemon -> pokemon.canBeHealed() }) {
            player.sendSystemMessage(lang("healingmachine.alreadyhealed").red(), true)
            return InteractionResult.SUCCESS
        }

        if (HealingMachineBlockEntity.isUsingHealer(player)) {
            player.sendSystemMessage(lang("healingmachine.alreadyhealing").red(), true)
            return InteractionResult.SUCCESS
        }

        if (blockEntity.canHeal(player)) {
            blockEntity.activate(player)
            player.sendSystemMessage(lang("healingmachine.healing").green(), true)
        } else {
            val neededCharge = player.party().getHealingRemainderPercent() - blockEntity.healingCharge
            player.sendSystemMessage(lang("healingmachine.notenoughcharge", "${((neededCharge/party.count())*100f).toInt()}%").red(), true)
        }
        party.forEach { it.tryRecallWithAnimation() }
        return InteractionResult.CONSUME
    }

    override fun onPlaced(world: Level, blockPos: BlockPos, blockState: BlockState, livingEntity: LivingEntity?, itemStack: ItemStack) {
        super.onPlaced(world, blockPos, blockState, livingEntity, itemStack)

        if (!world.isClient && livingEntity is ServerPlayer && livingEntity.isCreative) {
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity !is HealingMachineBlockEntity) {
                return
            }
            blockEntity.infinite = true
        }
    }

    override fun randomDisplayTick(state: BlockState, world: Level, pos: BlockPos, random: Random) {
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity !is HealingMachineBlockEntity) return

        if (random.nextInt(2) == 0 && blockEntity.healTimeLeft > 0) {
            val posX = pos.x.toDouble() + 0.5 + ((random.nextFloat() * 0.3F) * (if (random.nextInt(2) > 0) 1 else (-1))).toDouble()
            val posY = pos.y.toDouble() + 0.9
            val posZ = pos.z.toDouble() + 0.5 + ((random.nextFloat() * 0.3F) * (if (random.nextInt(2) > 0) 1 else (-1))).toDouble()
            world.addParticle(ParticleTypes.HAPPY_VILLAGER, posX, posY, posZ, 0.0, 0.0, 0.0)
        }
    }

    override fun hasComparatorOutput(state: BlockState) = true

    override fun getComparatorOutput(state: BlockState, world: Level, pos: BlockPos): Int = (world.getBlockEntity(pos) as? HealingMachineBlockEntity)?.currentSignal ?: 0

    override fun <T : BlockEntity> getTicker(world: Level, blockState: BlockState, blockWithEntityType: BlockEntityType<T>): BlockEntityTicker<T>? = validateTicker(blockWithEntityType, CobblemonBlockEntities.HEALING_MACHINE, HealingMachineBlockEntity.TICKER::tick)

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltip: MutableList<Component>,
        options: TooltipType
    ) {
        tooltip.add("block.${Cobblemon.MODID}.healing_machine.tooltip1".asTranslated().gray())
        tooltip.add("block.${Cobblemon.MODID}.healing_machine.tooltip2".asTranslated().gray())
    }

}