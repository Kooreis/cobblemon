/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class KabutopsModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("kabutops")
    override val head = getPart("head")

    override val leftLeg = getPart("left_upper_leg")
    override val rightLeg = getPart("right_upper_leg")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3d(-0.35, 0.2, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3d(0.0, 0.6, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
//    lateinit var float: PokemonPose
//    lateinit var swim: PokemonPose

    override val cryAnimation = CryProvider { _, _ -> bedrockStateful("kabutops", "cry") }

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("kabutops", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("kabutops", "ground_idle"),
                BipedWalkAnimation(this, periodMultiplier = 0.9F, amplitudeMultiplier = 1.0f),
            )
        )

//        float = registerPose(
//            poseName = "float",
//            poseTypes = setOf(PoseType.FLOAT, PoseType.HOVER),
//            idleAnimations = arrayOf(
//                singleBoneLook(),
//                bedrock("kabutops", "water_idle")
//            )
//        )
//
//        swim = registerPose(
//            poseName = "swim",
//            poseTypes = setOf(PoseType.SWIM, PoseType.FLOAT),
//            idleAnimations = arrayOf(
//                singleBoneLook(),
//                bedrock("kabutops", "water_swim")
//            )
//        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("kabutops", "faint") else null
}