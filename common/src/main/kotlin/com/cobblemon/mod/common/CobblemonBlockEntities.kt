/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.block.entity.*
import com.cobblemon.mod.common.block.entity.fossil.FossilMultiblockEntity
import com.cobblemon.mod.common.block.entity.fossil.FossilTubeBlockEntity
import com.cobblemon.mod.common.block.multiblock.builder.ResurrectionMachineMultiblockBuilder
import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object CobblemonBlockEntities : PlatformRegistry<Registry<BlockEntityType<*>>, RegistryKey<Registry<BlockEntityType<*>>>, BlockEntityType<*>>() {

    override val registry: Registry<BlockEntityType<*>> = Registries.BLOCK_ENTITY_TYPE
    override val registryKey: RegistryKey<Registry<BlockEntityType<*>>> = RegistryKeys.BLOCK_ENTITY_TYPE

    //@JvmField
    //val RESURRECTION_MACHINE = this.create("resurrection_machine", BlockEntityType.Builder.create(::ResurrectionMachineBlockEntity, CobblemonBlocks.RESURRECTION_MACHINE).build(null))
    @JvmField
    val HEALING_MACHINE: BlockEntityType<HealingMachineBlockEntity> = this.create("healing_machine", BlockEntityType.Builder.create(::HealingMachineBlockEntity, CobblemonBlocks.HEALING_MACHINE).build(null))
    @JvmField
    val PC: BlockEntityType<PCBlockEntity> = this.create("pc", BlockEntityType.Builder.create(::PCBlockEntity, CobblemonBlocks.PC).build(null))
    @JvmField
    val PASTURE: BlockEntityType<PokemonPastureBlockEntity> = this.create("pasture", BlockEntityType.Builder.create(::PokemonPastureBlockEntity, CobblemonBlocks.PASTURE).build(null))
    val FOSSIL_MULTIBLOCK: BlockEntityType<FossilMultiblockEntity> = this.create(
        "fossil_multiblock",
        BlockEntityType.Builder.create({ pos, state ->
            FossilMultiblockEntity(pos, state, ResurrectionMachineMultiblockBuilder(pos))
            },
            CobblemonBlocks.FOSSIL_COMPARTMENT,
            CobblemonBlocks.FOSSIL_MONITOR
        ).build(null)
    )
    val FOSSIL_TUBE: BlockEntityType<FossilTubeBlockEntity> = this.create(
        "fossil_tube",
        BlockEntityType.Builder.create({ pos, state ->
            FossilTubeBlockEntity(pos, state, ResurrectionMachineMultiblockBuilder(pos))
        },
            CobblemonBlocks.FOSSIL_TUBE
        ).build(null)
    )
}
