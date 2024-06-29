/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.block.ShapeContext
import net.minecraft.world.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class CoinPouchBlock(settings: Properties, val small: Boolean) : HorizontalDirectionalBlock(settings) {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(NATURAL, false)
            .setValue(FACING, Direction.NORTH))

    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        if (!small) super.getShape(state, world, pos, context) else
        Shapes.box(0.3125, 0.0, 0.3125, 0.6875, 0.3125, 0.6875)


    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var blockState = defaultBlockState()
        val worldView = ctx.level
        val blockPos = ctx.clickedPos
        ctx.nearestLookingDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState.setValue(FACING, direction.opposite) as BlockState
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    override fun codec(): MapCodec<out HorizontalDirectionalBlock> {
        return CODEC
    }

    @Deprecated("Deprecated in Java")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (direction == state.getValue(FACING) && !state.canSurvive(world, pos)) Blocks.AIR.defaultBlockState()
            else super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, NATURAL)
    }

    companion object {
        val NATURAL: BooleanProperty = BooleanProperty.of("natural")

        val CODEC: MapCodec<CoinPouchBlock> = RecordCodecBuilder.mapCodec { it.group(
            propertiesCodec(),
            PrimitiveCodec.BOOL.fieldOf("small").forGetter(CoinPouchBlock::small),
            ).apply(it, ::CoinPouchBlock) }
    }
}