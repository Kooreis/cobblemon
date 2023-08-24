package com.cobblemon.mod.common.block.multiblock

import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity
import com.cobblemon.mod.common.block.entity.MultiblockEntity
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * A block that can be part of a [MultiblockStructure]
 */
abstract class MultiblockBlock(properties: Settings) : BlockWithEntity(properties) {

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world is ServerWorld) {
            val multiblockEntity = world.getBlockEntity(pos) as MultiblockEntity
            multiblockEntity.multiblockBuilder?.validate(world)
        }
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        val entity = world.getBlockEntity(pos) as MultiblockEntity
        if (entity.multiblockStructure != null) {
            return entity.multiblockStructure!!.onUse(state, world, pos, player, hand, hit)
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return createMultiBlockEntity(pos, state)
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    abstract fun createMultiBlockEntity(pos: BlockPos, state: BlockState): FossilMultiblockEntity

}
