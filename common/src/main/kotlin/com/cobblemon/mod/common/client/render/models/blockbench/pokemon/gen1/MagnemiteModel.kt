/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class MagnemiteModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("magnemite")

    override val portraitScale = 2.2F
    override val portraitTranslation = Vec3d(-0.1, -1.5, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.18, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var sleep: PokemonPose


    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("magnemite", "blink").setPreventsIdle(false) }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("magnemite", "sleep"))
        )

        registerPose(
            poseName = "hover",
            poseTypes = PoseType.ALL_POSES - PoseType.FLY - PoseType.SLEEP - PoseType.SWIM,
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("magnemite", "air_idle")
            )
        )

        registerPose(
            poseName = "fly",
            poseTypes = setOf(PoseType.FLY, PoseType.SWIM),
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("magnemite", "air_fly")
            )
        )

    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = bedrockStateful("magnemite", "faint")
}