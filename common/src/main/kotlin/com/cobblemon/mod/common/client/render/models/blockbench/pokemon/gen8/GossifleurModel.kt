/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class GossifleurModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("gossifleur")
    override val head = getPart("head")

    override val portraitScale = 1.2F
    override val portraitTranslation = Vec3d(-0.85, 0.1, 0.0)

    override val profileScale = 0.55F
    override val profileTranslation = Vec3d(0.0, 0.85, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("gossifleur", "blink") }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("gossifleur", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("gossifleur", "ground_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("gossifleur", "faint") else null
}